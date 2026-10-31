"""One approved offline conditional respiratory-overlap fit; never phone inference.

Consumes existing PCM and exact frozen243-feature receipts. No acquisition,
parameter search, calibration fitting, stage weights or Android integration.
"""
import os
for _key in ("OMP_NUM_THREADS", "OPENBLAS_NUM_THREADS", "MKL_NUM_THREADS", "NUMEXPR_NUM_THREADS"):
    os.environ[_key] = "2"
import argparse
from collections import Counter
from decimal import Decimal
import hashlib
import json
from pathlib import Path
import pickle
import subprocess
import numpy as np
import scipy
import sklearn
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import average_precision_score, confusion_matrix, precision_recall_fscore_support, roc_auc_score
import phone_apnea_train_contract as labels
import phone_stage_acoustic as acoustic

ROOT = Path(__file__).resolve().parent.parent
PROTOCOL = ROOT / "docs/research/2026-11-01-apnea-protocol-proposal.json"
APPROVAL = ROOT / "docs/research/2026-11-01-apnea-conditional-approval.json"
TRAIN_RECORD = ROOT / "docs/research/2026-11-01-apnea-train-contract.json"
OLD_RECORDS = ROOT / "docs/research/2026-10-31-acoustic-pilot"
FEATURE_SHA = "cded615f226bf2e886a3625ac8a19ac65fe479201678c0bc4b77af263d48dbe7"


def sha(raw):
    return hashlib.sha256(raw).hexdigest()


def load(path):
    return json.loads(path.read_bytes())


def save(path, value):
    with path.open("xb") as output:
        output.write((json.dumps(value, indent=2) + "\n").encode())


def freeze():
    approval = load(APPROVAL)
    paths = (PROTOCOL, APPROVAL, TRAIN_RECORD, Path(__file__), Path(labels.__file__), Path(acoustic.__file__)) + tuple(
        OLD_RECORDS/name for name in ("train-inputs.json","validation.json","test.json","subject-sources.json","manifest.json"))
    pinned = {path.relative_to(ROOT).as_posix(): sha(path.read_bytes()) for path in paths}
    if (not approval["one_conditional_offline_fit_authorized"] or not approval["test_only_if_all_gates_pass"]
            or approval["protocol_sha256"] != sha(PROTOCOL.read_bytes())
            or approval["train_metadata_source_sha256"] != sha(Path(labels.__file__).read_bytes())
            or sha(Path(acoustic.__file__).read_bytes()) != FEATURE_SHA):
        raise ValueError("Approval or frozen source contract differs")
    # Approval and complete workflow must actually be committed before fitting.
    for path in paths:
        name = path.relative_to(ROOT).as_posix()
        committed = subprocess.check_output(["git", "show", "HEAD:" + name], cwd=ROOT)
        if sha(committed) != pinned[name]:
            raise ValueError("Uncommitted workflow/approval change before execution")
    return dict(files=pinned,source_commit=subprocess.check_output(["git","rev-parse","HEAD"],cwd=ROOT).decode().strip(),
        units="Seconds inferred under explicit root conditional approval, not field-specifically independently proved",
        task="source_annotated_respiratory_event_overlap",clinical_or_android_acceptance=False)


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


def phase_data(cache, phase):
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


def portable(model, matrix):
    result = np.zeros((len(matrix),2))
    matrix = matrix.astype(np.float32)
    for tree in model["trees"]:
        for index, vector in enumerate(matrix):
            node = 0
            while tree["left"][node] >= 0:
                node = tree["left"][node] if vector[tree["feature"][node]] <= tree["threshold"][node] else tree["right"][node]
            result[index] += tree["value"][node]
    return result / len(model["trees"])


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


def fit(cache, output):
    stamp = freeze()
    save(output/"fit-started.json",stamp)
    x,y,targets,coverage = phase_data(cache,"train")
    reference = load(TRAIN_RECORD)
    expected_labels = {(row["subject"],row["epoch"]):row for row in reference["targets"]}
    for row in targets:
        old = expected_labels[(row["subject"],row["epoch"])]
        if row["event_union_overlap_seconds"] != old["event_union_overlap_seconds"] or row["annotation_sha256"] != old["source_annotation_sha256"]:
            raise ValueError("TRAIN target contract differs from approved metadata")
    if np.bincount(y,minlength=2).tolist() != [145,54] or coverage["quality_missing"]:
        raise ValueError("Approved TRAIN label/quality counts differ")
    class_subjects = [len({row["subject"] for row in targets if row["label"]==index}) for index in (0,1)]
    if min(class_subjects)<10:
        raise ValueError("Insufficient TRAIN participant support")
    classifier = RandomForestClassifier(n_estimators=128,max_depth=8,min_samples_leaf=4,max_features="sqrt",
        class_weight="balanced_subsample",random_state=20261101,n_jobs=2)
    classifier.fit(x,y)
    trees = []
    for estimator in classifier.estimators_:
        tree=estimator.tree_; values=tree.value[:,0,:]; values=values/values.sum(axis=1,keepdims=True)
        trees.append(dict(left=tree.children_left.tolist(),right=tree.children_right.tolist(),
            feature=tree.feature.tolist(),threshold=tree.threshold.tolist(),value=values.tolist()))
    primary=pickle.dumps(classifier,protocol=5)
    model=dict(freeze=stamp,classes=["no_annotated_overlap","event_overlap"],feature_count=243,
        trees=trees,train_counts=[145,54],train_class_participants=class_subjects,train_targets=targets,
        train_coverage=coverage,train_selected_prior=54/199,train_natural_prior=7250/23734,
        primary_sha256=sha(primary),runtime=dict(python=__import__('sys').version,numpy=np.__version__,scipy=scipy.__version__,sklearn=sklearn.__version__),
        probability_provenance="Uncalibrated mean tree-leaf proportions; not a confidence/diagnosis probability")
    error=float(np.max(np.abs(classifier.predict_proba(x)-portable(model,x))))
    if error>1e-12:
        raise ValueError("Binary portable decoder differs on TRAIN")
    model["train_primary_portable_max_difference"]=error
    with (output/"own-primary.pkl").open("xb") as target:
        target.write(primary)
    save(output/"model.json",model)
    print(json.dumps(dict(stage="fit",train_counts=model["train_counts"],class_participants=class_subjects,
        model_sha256=sha((output/"model.json").read_bytes()),portable_max_difference=error)),flush=True)


def evaluate(cache, output, phase):
    stamp=freeze(); raw=(output/"model.json").read_bytes(); model=json.loads(raw)
    if model["freeze"]["files"] != stamp["files"]:
        raise ValueError("Frozen model/source/protocol/approval changed")
    if phase=="test":
        validation=load(output/"validation.json")
        if not validation["all_gates_passed"] or validation["model_sha256"]!=sha(raw):
            raise ValueError("Validation failed; TEST event labels remain closed")
    save(output/(phase+"-started.json"),dict(freeze=stamp,model_sha256=sha(raw)))
    x,y,targets,coverage=phase_data(cache,phase)
    if not len(y):
        raise ValueError("No actual labeled inputs; stop without predictions")
    primary=(output/"own-primary.pkl").read_bytes()
    if sha(primary)!=model["primary_sha256"]:
        raise ValueError("Own primary fitted model hash differs")
    # Only this process's own digest-verified fit; never third-party pickle/code.
    reference=pickle.loads(primary)
    probabilities=portable(model,x)
    difference=float(np.max(np.abs(reference.predict_proba(x)-probabilities)))
    if difference>1e-12:
        raise ValueError("Binary portable decoder differs on held-out PCM")
    result=metrics(y,probabilities)
    baselines={}
    for name,prior in (("selected_train_prior",model["train_selected_prior"]),("natural_train_prior",model["train_natural_prior"]),("majority",float(np.argmax(model["train_counts"])))):
        baselines[name]=metrics(y,np.tile([1-prior,prior],(len(y),1)))
    subjects=[len({row["subject"] for row in targets if row["label"]==index}) for index in (0,1)]
    gates=dict(coverage=coverage["coverage"]>=.8,target_support=min(result["support"])>=10,participant_support=min(subjects)>=3,
        auroc=result["auroc"] is not None and result["auroc"]>=.70,balanced_accuracy=result["balanced_accuracy"]>=.60,
        sensitivity=result["sensitivity"]>=.50,specificity=result["specificity"]>=.70,
        macro_f1=result["macro_f1"]>=baselines["majority"]["macro_f1"]+.05,
        brier=all(result["binary_brier"]<baselines[name]["binary_brier"] for name in ("selected_train_prior","natural_train_prior")),
        nll=all(result["negative_log_likelihood"]<baselines[name]["negative_log_likelihood"] for name in ("selected_train_prior","natural_train_prior")))
    per_subject=[]
    for subject in sorted({row["subject"] for row in targets}):
        indices=[index for index,row in enumerate(targets) if row["subject"]==subject]
        predicted=(probabilities[indices,1]>=.5).astype(int)
        per_subject.append(dict(subject=subject,targets=len(indices),support=np.bincount(y[indices],minlength=2).tolist(),
            confusion=confusion_matrix(y[indices],predicted,labels=[0,1]).tolist(),accuracy=float(np.mean(predicted==y[indices]))))
    report=dict(phase=phase,freeze=stamp,model_sha256=sha(raw),coverage=coverage,metrics=result,baselines=baselines,
        class_participants=subjects,gates=gates,all_gates_passed=all(gates.values()),per_subject=per_subject,
        primary_portable_max_difference=difference,
        probabilities=[dict(**row,probabilities=p.tolist()) for row,p in zip(targets,probabilities)],
        scope="Conditional source-event-overlap sampled30s targets only; no clinical/apnea diagnosis, night sensitivity, AHI or Android acceptance")
    save(output/(phase+".json"),report)
    print(json.dumps(dict(phase=phase,targets=len(y),support=result["support"],confusion=result["confusion"],
        accuracy=result["accuracy"],auroc=result["auroc"],balanced_accuracy=result["balanced_accuracy"],macro_f1=result["macro_f1"],
        binary_brier=result["binary_brier"],nll=result["negative_log_likelihood"],gates=gates,all_gates_passed=report["all_gates_passed"])),flush=True)


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command",choices=("fit","validation","test"))
    parser.add_argument("--cache",type=Path,required=True)
    parser.add_argument("--output",type=Path,required=True)
    args=parser.parse_args(); args.output.mkdir(parents=True,exist_ok=True)
    if args.command=="fit":
        fit(args.cache,args.output)
    else:
        evaluate(args.cache,args.output,args.command)


if __name__=="__main__":
    main()
