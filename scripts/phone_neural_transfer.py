"""Frozen candidate2: pretrained32 neural representation + one TRAIN logistic head.
Offline only. No network, upstream execution, weight updates, search or Android.
"""
import os
for _key in ('OMP_NUM_THREADS','OPENBLAS_NUM_THREADS','MKL_NUM_THREADS','NUMEXPR_NUM_THREADS','NUMBA_NUM_THREADS','TF_NUM_INTRAOP_THREADS'):
    os.environ[_key]='2'
os.environ['TF_NUM_INTEROP_THREADS']='1'
os.environ['TF_CPP_MIN_LOG_LEVEL']='2'
import argparse
from decimal import Decimal
import hashlib
import io
import json
from pathlib import Path
import subprocess
import sys
import time
import zipfile
import numpy as np
import scipy
from scipy.signal import resample_poly
from scipy.special import expit
import h5py
import librosa
import sklearn
from threadpoolctl import threadpool_limits
_thread_limit=threadpool_limits(limits=2)
from sklearn.preprocessing import StandardScaler
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import average_precision_score, confusion_matrix, precision_recall_fscore_support, roc_auc_score
import phone_apnea_train_contract as labels
import phone_stage_acoustic as acoustic
ROOT=Path(__file__).resolve().parent.parent
DOC=ROOT/'docs/research/2026-11-01-neural-transfer'
OLD_RECORDS=ROOT/'docs/research/2026-10-31-acoustic-pilot'
FEATURE_SHA='cded615f226bf2e886a3625ac8a19ac65fe479201678c0bc4b77af263d48dbe7'
WEIGHT_SHA='c1938a82248bb3526b2c9ff7a76f5e5d7ceee35cf552706e6587766f0fedf664'
TRAIN_RECORD=DOC/'original-train-contract.json'
def sha(raw):
    return hashlib.sha256(raw).hexdigest()


def load(path):
    return json.loads(path.read_bytes())


def save(path, value):
    with path.open("xb") as output:
        output.write((json.dumps(value, indent=2) + "\n").encode())


def known_windows(phase):
    # Extract only old window/input/feature receipts; never old stage predictions.
    old = load(OLD_RECORDS / ("train-inputs.json" if phase == "train" else phase + ".json"))
    records = old["accepted"] if phase == "train" else old["probabilities"]
    expected = {}
    for row in records:
        window = row["window"]
        subject = int(window["subject"])
        if not ((1 <= subject <= 30) if phase == "train" else (31 <= subject <= 40) if phase == "validation" else (41 <= subject <= 50)):
            raise ValueError("Wrong participant scope")
        key = acoustic.stem(window)
        if key in expected:
            raise ValueError("Duplicate frozen target")
        expected[key] = dict(window=window,input_sha256=row["input_sha256"],feature_sha256=row["feature_sha256"])
    if len(expected) != (217 if phase == "train" else 100):
        raise ValueError("Fixed window count differs")
    return expected


def base_phase_data(cache, phase):
    expected = known_windows(phase)
    source_contracts = {row["subject"]:row for row in load(OLD_RECORDS / "subject-sources.json")}
    annotations, annotation_hashes, exclusions, rows, target_labels = {}, {}, [], [], {}
    for key, receipt in expected.items():
        window = receipt["window"]
        subject = window["subject"]
        if subject not in annotations:
            folder = cache / subject
            contract_raw = (folder / "contract.json").read_bytes()
            contract = json.loads(contract_raw)
            raw = (folder / f"{subject}_annotation.json").read_bytes()
            if (sha(contract_raw) != source_contracts[subject]["original_contract_sha256"]
                    or sha(raw) != source_contracts[subject]["sidecars"]["annotation.json"]["sha256"]
                    or contract["phone"] is None):
                raise ValueError("Source annotation/phone contract changed")
            annotation = json.loads(raw,parse_float=Decimal)
            if labels.seconds(annotation["record_start"]) != labels.seconds(contract["record_start"]):
                raise ValueError("Unknown source time origin")
            intervals, ends, _, _, _ = labels.respiratory_intervals(annotation)
            annotations[subject] = (intervals,ends,contract)
            annotation_hashes[subject] = sha(raw)
        intervals, ends, contract = annotations[subject]
        start = labels.seconds(window["start_seconds"])
        if start < 30 or start + 60 > labels.seconds(contract["phone"]["duration_seconds"]):
            raise ValueError("Missing actual complete90s coverage")
        category, overlap = labels.label(start,intervals,ends)
        label = 1 if category == "event_overlap" else 0 if category == "no_annotated_overlap" else None
        target_labels[key] = dict(label=label,event_union_overlap_seconds=overlap,
            annotation_sha256=annotation_hashes[subject])
        if label is None:
            exclusions.append(dict(subject=subject,epoch=window["epoch"],reason=category,overlap_seconds=overlap))
        else:
            rows.append(window)
    # Original implementation verifies each cached PCM against its exact206 receipt.
    matrix, _, accepted, missing = acoustic.features(cache, rows)
    targets, truth = [], []
    for row in accepted:
        window = row["window"]
        key = acoustic.stem(window)
        if any(row[name] != expected[key][name] for name in ("input_sha256","feature_sha256")):
            raise ValueError("Paired original PCM/243-feature hash differs")
        label = target_labels[key]
        truth.append(label["label"])
        targets.append(dict(subject=window["subject"],epoch=window["epoch"],target_start_seconds=window["start_seconds"],
            input_sha256=row["input_sha256"],feature_sha256=row["feature_sha256"],**label))
    return matrix,np.asarray(truth,dtype=int),targets,dict(selected=len(expected),boundary_excluded=exclusions,
        quality_missing=missing,annotation_sha256=annotation_hashes,
        coverage=len(targets)/len(expected),units="Conditional seconds inference; not clinical truth")


def reliability(p, outcome):
    bins = []
    for index in range(10):
        included = (p >= index/10) & ((p < (index+1)/10) if index < 9 else (p <= 1))
        count = int(included.sum())
        bins.append(dict(lower=index/10,upper=(index+1)/10,count=count,
            mean_score=float(p[included].mean()) if count else None,
            observed_fraction=float(outcome[included].mean()) if count else None))
    ece = sum(row["count"]*abs(row["mean_score"]-row["observed_fraction"]) for row in bins if row["count"])/len(p)
    return dict(equal_width_bins=10,ece=ece,bins=bins)


def metrics(y, probabilities):
    p = probabilities[:,1]
    prediction = (p >= .5).astype(int)
    precision,recall,f1,support = precision_recall_fscore_support(y,prediction,labels=[0,1],zero_division=0)
    chosen = np.where(y == 1,p,1-p)
    return dict(accuracy=float(np.mean(prediction == y)),confusion=confusion_matrix(y,prediction,labels=[0,1]).tolist(),
        precision=precision.tolist(),recall=recall.tolist(),f1=f1.tolist(),support=support.tolist(),
        macro_f1=float(f1.mean()),balanced_accuracy=float(recall.mean()),
        sensitivity=float(recall[1]),specificity=float(recall[0]),positive_prevalence=float(y.mean()),
        auroc=float(roc_auc_score(y,p)) if len(set(y))==2 else None,
        average_precision=float(average_precision_score(y,p)) if np.any(y==1) else None,
        binary_brier=float(np.mean((p-y)**2)),
        multiclass_brier_sum=float(2*np.mean((p-y)**2)),
        negative_log_likelihood=float(-np.log(np.maximum(chosen,np.finfo(float).eps)).mean()),
        positive_score_reliability=reliability(p,y),
        top_score_reliability=reliability(probabilities.max(axis=1),prediction == y))


def freeze():
    paths=(Path(__file__),Path(labels.__file__),Path(acoustic.__file__),Path(acoustic.data.__file__),DOC/'protocol.json',DOC/'approval.json',DOC/'mechanical-retry.json',TRAIN_RECORD,DOC/'original-label-protocol.json')+tuple(
        OLD_RECORDS/name for name in ('train-inputs.json','validation.json','test.json','subject-sources.json','manifest.json'))
    hashes={p.relative_to(ROOT).as_posix():sha(p.read_bytes()) for p in paths}
    for p in paths:
        if sha(subprocess.check_output(['git','show','HEAD:'+p.relative_to(ROOT).as_posix()],cwd=ROOT))!=hashes[p.relative_to(ROOT).as_posix()]:
            raise ValueError('Workflow/protocol/approval must be committed unchanged before execution')
    if sha(Path(acoustic.__file__).read_bytes())!=FEATURE_SHA or sha(Path(labels.__file__).read_bytes())!='effa4c22766c6b664df9476817b42e6408ff727002141f3ac535705d18220726':
        raise ValueError('Original PCM/243-feature or event-label source changed')
    protocol=load(DOC/'protocol.json');approval=load(DOC/'approval.json')
    if (np.__version__,scipy.__version__,librosa.__version__,h5py.__version__,sklearn.__version__)!=('1.26.4','1.13.1','0.10.2.post1','3.11.0','1.9.1'):
        raise ValueError('Pinned frontend/runtime versions differ')
    if approval['protocol_sha256']!=sha((DOC/'protocol.json').read_bytes()) or not approval['one_fixed_candidate_authorized']:
        raise ValueError('Missing exact conditional approval')
    return dict(files=hashes,source_commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT).decode().strip(),
        candidate=2,validation_previously_inspected=True,test_respiratory_labels_closed_until_val_pass=True,
        unit_assumption='Source event_duration seconds under root conditional inference; field-specific unit not independently proved')


def weights(path):
    raw=path.read_bytes()
    if sha(raw)!=WEIGHT_SHA:raise ValueError('Pinned checkpoint changed')
    expected={}
    for name,a,b in [('dense',160,256),('dense_1',256,128),('dense_2',128,64),('dense_3',64,32),('dense_4',32,1)]:
        expected[f'layers/{name}/vars/0']=(a,b);expected[f'layers/{name}/vars/1']=(b,)
    for name,n in [('batch_normalization',256),('batch_normalization_1',128),('batch_normalization_2',64)]:
        for i in range(4):expected[f'layers/{name}/vars/{i}']=(n,)
    with zipfile.ZipFile(io.BytesIO(raw)) as archive:
        config=json.loads(archive.read('config.json'))
        if config['class_name']!='Sequential':raise ValueError('Unexpected model class')
        layers=config['config']['layers']
        if [x['class_name'] for x in layers]!=['InputLayer','Dense','BatchNormalization','Dropout','Dense','BatchNormalization','Dropout','Dense','BatchNormalization','Dropout','Dense','Dropout','Dense']:
            raise ValueError('Unexpected layer architecture; no custom execution')
        if any(x['module']!='keras.layers' or x.get('registered_name') for x in layers):raise ValueError('Non-whitelisted layer module')
        if [x['config']['units'] for x in layers if x['class_name']=='Dense']!=[256,128,64,32,1]:raise ValueError('Wrong widths')
        if [x['config']['activation'] for x in layers if x['class_name']=='Dense']!=['relu']*4+['sigmoid']:raise ValueError('Wrong activations')
        if any(x['config']['epsilon']!=.001 or x['config']['axis']!=-1 or not x['config']['center'] or not x['config']['scale'] for x in layers if x['class_name']=='BatchNormalization'):
            raise ValueError('Wrong BN configuration')
        with h5py.File(io.BytesIO(archive.read('model.weights.h5')),'r') as model:
            def check_links(group):
                for name in group:
                    if not isinstance(group.get(name,getlink=True),h5py.HardLink):raise ValueError('External/soft model link')
                    item=group[name]
                    if isinstance(item,h5py.Group):check_links(item)
                    elif isinstance(item,h5py.Dataset) and item.external is not None:raise ValueError('External model storage')
            check_links(model);result={}
            for name,shape in expected.items():
                item=model[name]
                if item.shape!=shape or item.dtype!=np.dtype('float32'):raise ValueError('Wrong numeric tensor shape/type')
                value=item[...]
                if not np.isfinite(value).all() or 'batch_normalization' in name and name.endswith('/3') and np.any(value<0):raise ValueError('Invalid model values')
                value.flags.writeable=False;result[name]=value
    return result


def forward(matrix, tensors, with_probability=False):
    x=np.asarray(matrix,dtype=np.float32)
    if x.ndim!=2 or x.shape[1]!=160 or not np.isfinite(x).all():raise ValueError('Invalid normalized160 input')
    for index,name in enumerate(('dense','dense_1','dense_2','dense_3')):
        x=np.maximum(x@tensors[f'layers/{name}/vars/0']+tensors[f'layers/{name}/vars/1'],np.float32(0))
        if index<3:
            bn='batch_normalization'+('_'+str(index) if index else '')
            gamma,beta,mean,variance=[tensors[f'layers/{bn}/vars/{n}'] for n in range(4)]
            x=gamma*(x-mean)/np.sqrt(variance+np.float32(.001))+beta
    embedding=x
    sigmoid=expit(x@tensors['layers/dense_4/vars/0']+tensors['layers/dense_4/vars/1']).reshape(-1) if with_probability else None
    if not np.isfinite(embedding).all() or sigmoid is not None and not np.isfinite(sigmoid).all():raise ValueError('Nonfinite neural output')
    return embedding,sigmoid


def parity(output,checkpoint):
    stamp=freeze();save(output/'parity-started.json',stamp);tensors=weights(checkpoint)
    import tensorflow as tf
    tf.config.threading.set_inter_op_parallelism_threads(1);tf.config.threading.set_intra_op_parallelism_threads(2)
    import keras
    # Architecture and all tensors were whitelisted above, before any safe Keras load.
    model=keras.saving.load_model(checkpoint,safe_mode=True,compile=False)
    representation=keras.Model(inputs=model.inputs,outputs=model.get_layer('dense_3').output)
    synthetic=np.random.default_rng(20261101).normal(size=(64,160)).astype(np.float32)
    actual_embedding=np.asarray(representation(synthetic,training=False));actual_probability=np.asarray(model(synthetic,training=False)).reshape(-1)
    computed_embedding,computed_probability=forward(synthetic,tensors,True)
    embedding_error=float(np.max(np.abs(actual_embedding-computed_embedding)));probability_error=float(np.max(np.abs(actual_probability-computed_probability)))
    passed=embedding_error<=1e-4 and probability_error<=1e-5
    report=dict(freeze=stamp,checkpoint_sha256=sha(checkpoint.read_bytes()),synthetic_sha256=sha(synthetic.astype('<f4').tobytes()),
        seed=20261101,shape=[64,160],embedding_absolute_max_error=embedding_error,original_sigmoid_absolute_max_error=probability_error,
        tolerance_embedding=1e-4,tolerance_sigmoid=1e-5,passed=passed,
        runtime=dict(python=sys.version,numpy=np.__version__,scipy=scipy.__version__,sklearn=sklearn.__version__,librosa=librosa.__version__,h5py=h5py.__version__,tensorflow=tf.__version__,keras=keras.__version__),
        original_sigmoid_used_for_target=False,subject_data_read=False,outcomes_read=False)
    save(output/'parity.json',report);print(json.dumps(report),flush=True)
    if not passed:raise ValueError('Primary runtime parity failed; no features/outcomes authorized')


def require_parity(output):
    stamp=freeze();report=load(output/'parity.json')
    if not report['passed'] or report['freeze']['files']!=stamp['files'] or report['checkpoint_sha256']!=WEIGHT_SHA:raise ValueError('Frozen primary parity missing/different')
    return stamp


def chunk_features(audio):
    # New pinned frontend. Original notebook's family order, with explicit failure handling.
    if audio.shape!=(80000,) or not np.isfinite(audio).all():raise ValueError('Incomplete actual10s chunk')
    y=resample_poly(audio,2,1,window=('kaiser',5.0),padtype='constant',cval=0).astype(np.float32)
    if y.shape!=(160000,) or not np.isfinite(y).all():raise ValueError('Invalid bounded16k representation')
    mfcc=librosa.feature.mfcc(y=y,sr=16000,n_mfcc=13)
    mel=librosa.power_to_db(librosa.feature.melspectrogram(y=y,sr=16000,n_mels=64),ref=np.max)
    zcr=librosa.feature.zero_crossing_rate(y);centroid=librosa.feature.spectral_centroid(y=y,sr=16000);rms=librosa.feature.rms(y=y)
    if mfcc.shape[0]!=13 or mel.shape[0]!=64 or not all(np.isfinite(x).all() for x in (mfcc,mel,zcr,centroid,rms)):raise ValueError('Invalid frontend family')
    vector=np.concatenate((mfcc.mean(axis=1),mfcc.std(axis=1),mel.mean(axis=1),mel.std(axis=1),[zcr.mean(),zcr.std(),centroid.mean(),centroid.std(),rms.mean(),rms.std()])).astype(np.float32)
    if vector.shape!=(160,) or not np.isfinite(vector).all():raise ValueError('Invalid160 feature vector')
    return vector


def phase_data(cache,phase,deadline):
    # Preserve original243-feature quality rules and exact paired feature/PCM hashes.
    _,truth,targets,coverage=base_phase_data(cache,phase)
    expected=known_windows(phase);by_key={(r['window']['subject'],r['window']['epoch']):r for r in expected.values()}
    features=[];kept=[];labels_kept=[]
    for index,target in enumerate(targets):
        if time.monotonic()>deadline:raise TimeoutError('Fixed900s phase budget exhausted; no retry/refit')
        known=by_key[(target['subject'],target['epoch'])];window=known['window'];raw=(cache/'pcm'/(acoustic.stem(window)+'.pcm')).read_bytes()
        if sha(raw)!=target['input_sha256']:raise ValueError('PCM changed after paired feature verification')
        audio=np.frombuffer(raw,dtype='<i2').astype(np.float32)/np.float32(32768)
        try:
            vectors=np.stack([chunk_features(audio[start:start+80000]) for start in (240000,320000,400000)])
        except Exception as failure:
            coverage['quality_missing'].append(dict(subject=target['subject'],epoch=target['epoch'],reason=str(failure),family='new160'))
            continue
        features.append(vectors);kept.append(dict(**target,new160_sha256=sha(vectors.astype('<f4').tobytes())));labels_kept.append(int(truth[index]))
        if (index+1)%25==0:print(json.dumps(dict(phase=phase,feature_targets_processed=index+1,of=len(targets))),flush=True)
    coverage['coverage']=len(kept)/coverage['selected']
    return np.asarray(features,dtype=np.float32),np.asarray(labels_kept,dtype=int),kept,coverage


def normalized(value,mean,scale):
    # sklearn1.9 casts fitted statistics to X.dtype BEFORE both arithmetic steps.
    result=np.asarray(value,dtype=np.float32).copy();result-=np.asarray(mean,dtype=result.dtype);result/=np.asarray(scale,dtype=result.dtype)
    if not np.isfinite(result).all():raise ValueError('Invalid normalized input')
    return result


def embedding(matrix,tensors,mean,scale):
    chunk=normalized(matrix.reshape(-1,160),mean,scale)
    return forward(chunk,tensors)[0].reshape(-1,3,32).mean(axis=1)


def head_probability(model,embedding_matrix):
    x=normalized(embedding_matrix,model['embedding_mean'],model['embedding_scale'])
    coefficient,intercept=head_arrays(model)
    # sklearn's binary decision_function uses the fitted 2D matrix and dtype,
    # then applies expit in place. Upcasting or a 1D dot changes rounding.
    logit=(x@coefficient.T+intercept).reshape(-1)
    p=expit(logit,out=logit);return np.stack((1-p,p),axis=1)


def head_arrays(model):
    if model['coefficient_dtype'] not in ('float32','float64') or model['intercept_dtype'] not in ('float32','float64'):
        raise ValueError('Unknown fitted logistic arithmetic dtype')
    coefficient=np.asarray([model['coefficient']],dtype=model['coefficient_dtype'])
    intercept=np.asarray([model['intercept']],dtype=model['intercept_dtype'])
    if coefficient.shape!=(1,32) or intercept.shape!=(1,) or not np.isfinite(coefficient).all() or not np.isfinite(intercept).all():
        raise ValueError('Invalid fitted binary logistic parameters')
    return coefficient,intercept


def head_parity(output,model,embedding_matrix,classifier,head_input):
    primary=classifier.predict_proba(head_input)
    difference=float(np.max(np.abs(primary-head_probability(model,embedding_matrix))))
    passed=np.isfinite(difference) and difference<=1e-12
    save(output/'head-parity.json',dict(model_fitted_sha256=model['model_fitted_sha256'],
        primary_probability_dtype=str(primary.dtype),
        coefficient_dtype=model['coefficient_dtype'],intercept_dtype=model['intercept_dtype'],
        absolute_max_error=difference,tolerance=1e-12,passed=bool(passed)))
    if not passed:raise ValueError('Primary logistic portable probability mismatch')
    return difference


def fit(cache,output,checkpoint):
    stamp=require_parity(output);save(output/'fit-started.json',stamp);deadline=time.monotonic()+900;tensors=weights(checkpoint)
    x,y,targets,coverage=phase_data(cache,'train',deadline)
    reference={(r['subject'],r['epoch']):r for r in load(TRAIN_RECORD)['targets']}
    for target in targets:
        old=reference[(target['subject'],target['epoch'])]
        if target['event_union_overlap_seconds']!=old['event_union_overlap_seconds'] or target['annotation_sha256']!=old['source_annotation_sha256']:raise ValueError('Original TRAIN label source differs')
    if np.bincount(y,minlength=2).tolist()!=[145,54] or coverage['quality_missing']:raise ValueError('Fixed TRAIN coverage/support differs; stop')
    persist_features(output,'train',x,y,targets,coverage,stamp)
    first=StandardScaler().fit(x.reshape(-1,160));z=first.transform(x.reshape(-1,160))
    first_difference=float(np.max(np.abs(z-normalized(x.reshape(-1,160),first.mean_,first.scale_))))
    if first_difference>1e-6:raise ValueError('Raw scaler portable mismatch')
    embeddings=embedding(x,tensors,first.mean_,first.scale_)
    second=StandardScaler().fit(embeddings);head_input=second.transform(embeddings)
    classifier=LogisticRegression(C=.1,penalty='l2',solver='lbfgs',max_iter=1000,class_weight=None,random_state=20261101)
    classifier.fit(head_input,y)
    model=dict(freeze=stamp,checkpoint_sha256=WEIGHT_SHA,raw_mean=first.mean_.tolist(),raw_scale=first.scale_.tolist(),
        embedding_mean=second.mean_.tolist(),embedding_scale=second.scale_.tolist(),coefficient=classifier.coef_[0].tolist(),intercept=float(classifier.intercept_[0]),
        coefficient_dtype=str(classifier.coef_.dtype),intercept_dtype=str(classifier.intercept_.dtype),
        n_iter=classifier.n_iter_.tolist(),classes=['no_annotated_overlap','event_overlap'],train_counts=[145,54],train_targets=targets,train_coverage=coverage,
        train_selected_prior=54/199,train_natural_prior=7250/23734,raw_scaler_vectors=len(z),embedding_scaler_vectors=len(embeddings),
        embeddings_sha256=sha(embeddings.astype('<f4').tobytes()),raw_vectors_sha256=sha(x.astype('<f4').tobytes()),
        probability_provenance='Uncalibrated TRAIN logistic head on frozen pretrained32 representations; conditional source-overlap score, not apnea diagnosis',
        first_scaler_portable_max_difference=first_difference)
    # A completed fit must survive later diagnostic failures without another fit.
    save(output/'model-fitted.json',model)
    model['model_fitted_sha256']=sha((output/'model-fitted.json').read_bytes())
    if classifier.n_iter_[0]>=1000:raise ValueError('Fixed logistic fit did not converge; no new parameters')
    difference=head_parity(output,model,embeddings,classifier,head_input)
    model['train_primary_portable_max_difference']=difference
    save(output/'model.json',model)
    print(json.dumps(dict(phase='fit',targets=len(y),support=model['train_counts'],iterations=model['n_iter'],primary_portable_max_difference=difference,model_sha256=sha((output/'model.json').read_bytes()))),flush=True)


def persist_features(output,phase,x,y,targets,coverage,stamp):
    path=output/(phase+'-features.npz')
    with path.open('xb') as stream:np.savez(stream,features=x,labels=y)
    save(output/(phase+'-features.json'),dict(freeze=stamp,npz_sha256=sha(path.read_bytes()),shape=list(x.shape),
        features_sha256=sha(x.astype('<f4').tobytes()),labels_sha256=sha(y.astype('<i8').tobytes()),targets=targets,coverage=coverage))


def evaluate(cache,output,checkpoint,phase):
    stamp=require_parity(output);raw=(output/'model.json').read_bytes();model=json.loads(raw)
    if model['freeze']['files']!=stamp['files'] or model['checkpoint_sha256']!=WEIGHT_SHA:raise ValueError('Fit/source contract changed')
    if phase=='test':
        validation=load(output/'validation.json')
        if not validation['all_gates_passed'] or validation['model_sha256']!=sha(raw):raise ValueError('VAL failed; respiratory TEST labels remain unopened')
    save(output/(phase+'-started.json'),dict(freeze=stamp,model_sha256=sha(raw)));deadline=time.monotonic()+900
    x,y,targets,coverage=phase_data(cache,phase,deadline);embeddings=embedding(x,weights(checkpoint),model['raw_mean'],model['raw_scale'])
    persist_features(output,phase,x,y,targets,coverage,stamp)
    probabilities=head_probability(model,embeddings)
    primary=LogisticRegression(C=.1,penalty='l2',solver='lbfgs',max_iter=1000,class_weight=None,random_state=20261101)
    primary.coef_,primary.intercept_=head_arrays(model);primary.classes_=np.asarray([0,1]);primary.n_features_in_=32
    difference=float(np.max(np.abs(primary.predict_proba(normalized(embeddings,model['embedding_mean'],model['embedding_scale']))-probabilities)))
    if difference>1e-12:raise ValueError('Held-out portable head differs')
    result=metrics(y,probabilities);baselines={}
    for name,prior in [('selected_train_prior',model['train_selected_prior']),('natural_train_prior',model['train_natural_prior']),('majority',0.)]:
        baselines[name]=metrics(y,np.tile([1-prior,prior],(len(y),1)))
    subjects=[len({r['subject'] for r in targets if r['label']==i}) for i in [0,1]]
    gates=dict(coverage=coverage['coverage']>=.8,target_support=min(result['support'])>=10,participant_support=min(subjects)>=3,
        auroc=result['auroc'] is not None and result['auroc']>=.7,balanced_accuracy=result['balanced_accuracy']>=.6,sensitivity=result['sensitivity']>=.5,specificity=result['specificity']>=.7,
        macro_f1=result['macro_f1']>=baselines['majority']['macro_f1']+.05,
        brier=all(result['binary_brier']<baselines[name]['binary_brier'] for name in ['selected_train_prior','natural_train_prior']),
        nll=all(result['negative_log_likelihood']<baselines[name]['negative_log_likelihood'] for name in ['selected_train_prior','natural_train_prior']))
    per_subject=[]
    for subject in sorted({r['subject'] for r in targets}):
        indices=[i for i,r in enumerate(targets) if r['subject']==subject];prediction=(probabilities[indices,1]>=.5).astype(int)
        per_subject.append(dict(subject=subject,targets=len(indices),support=np.bincount(y[indices],minlength=2).tolist(),confusion=confusion_matrix(y[indices],prediction,labels=[0,1]).tolist(),accuracy=float(np.mean(prediction==y[indices]))))
    report=dict(phase=phase,freeze=stamp,model_sha256=sha(raw),coverage=coverage,metrics=result,baselines=baselines,class_participants=subjects,
        gates=gates,all_gates_passed=all(gates.values()),per_subject=per_subject,primary_portable_max_difference=difference,
        embeddings_sha256=sha(embeddings.astype('<f4').tobytes()),probabilities=[dict(**r,probabilities=p.tolist()) for r,p in zip(targets,probabilities)],
        scope='Candidate2 on previously inspected VAL. Conditional sampled source-overlap target; no clinical/night/AHI/Android acceptance')
    save(output/(phase+'.json'),report);print(json.dumps(dict(phase=phase,metrics=result,gates=gates,all_gates_passed=report['all_gates_passed'])),flush=True)


def failure_receipt(output,error):
    receipts={name:dict(bytes=(output/name).stat().st_size,sha256=sha((output/name).read_bytes()))
        for name in ('model-fitted.json','head-parity.json') if (output/name).is_file()}
    return dict(type=type(error).__name__,message=str(error),persisted_fit_receipts=receipts,
        action='Stopped; no fallback/retry/refit authorized')


def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('command',choices=['parity','fit','validation','test']);parser.add_argument('--cache',type=Path,required=True);parser.add_argument('--output',type=Path,required=True);parser.add_argument('--checkpoint',type=Path,required=True)
    args=parser.parse_args();args.output.mkdir(parents=True,exist_ok=True)
    try:
        if args.command=='parity':parity(args.output,args.checkpoint)
        elif args.command=='fit':fit(args.cache,args.output,args.checkpoint)
        else:evaluate(args.cache,args.output,args.checkpoint,args.command)
    except Exception as error:
        save(args.output/(args.command+'-error.json'),failure_receipt(args.output,error))
        raise


if __name__=='__main__':main()
