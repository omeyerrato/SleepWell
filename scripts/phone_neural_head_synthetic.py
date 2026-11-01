"""Mechanical logistic decoder check using only fixed synthetic TRAIN arrays.

No subjects, checkpoints, study caches, labels, outcomes, or freeze execution.
"""
import argparse
import inspect
import json
from pathlib import Path
import sys

import phone_neural_transfer as workflow
import numpy as np
import scipy
from scipy.special import expit
import sklearn
from sklearn.linear_model import LogisticRegression
from sklearn.linear_model._base import LinearClassifierMixin
from sklearn.preprocessing import StandardScaler


def run(output):
    output.mkdir(parents=True,exist_ok=False)
    rng=np.random.default_rng(20261101)
    x=rng.normal(size=(199,32)).astype(np.float32)
    y=np.asarray([0]*145+[1]*54,dtype=int);rng.shuffle(y)
    scaler=StandardScaler().fit(x);z=scaler.transform(x)
    classifier=LogisticRegression(C=.1,penalty='l2',solver='lbfgs',max_iter=1000,class_weight=None,random_state=20261101)
    classifier.fit(z,y)
    model=dict(embedding_mean=scaler.mean_.tolist(),embedding_scale=scaler.scale_.tolist(),
        coefficient=classifier.coef_[0].tolist(),intercept=float(classifier.intercept_[0]),
        coefficient_dtype=str(classifier.coef_.dtype),intercept_dtype=str(classifier.intercept_.dtype),
        synthetic_only=True,seed=20261101,n_iter=classifier.n_iter_.tolist())
    workflow.save(output/'model-fitted.json',model)
    model['model_fitted_sha256']=workflow.sha((output/'model-fitted.json').read_bytes())
    model=json.loads(json.dumps(model))
    error=workflow.head_parity(output,model,x,classifier,z)
    original=expit(z@np.asarray(model['coefficient'],dtype=np.float64).reshape(32)+float(model['intercept']))
    original=np.column_stack((1-original,original))
    legacy_error=float(np.max(np.abs(classifier.predict_proba(z)-original)))
    batch_errors={str(count):float(np.max(np.abs(classifier.predict_proba(z[:count])-workflow.head_probability(model,x[:count]))))
        for count in (1,7,64,199)}
    restored=LogisticRegression(C=.1,penalty='l2',solver='lbfgs',max_iter=1000,class_weight=None,random_state=20261101)
    restored.coef_,restored.intercept_=workflow.head_arrays(model)
    restored.classes_=np.asarray([0,1]);restored.n_features_in_=32
    restored_error=float(np.max(np.abs(classifier.predict_proba(z)-restored.predict_proba(z))))
    # Deliberately wrong synthetic comparison proves a failure still leaves the
    # immutable fitted artifact and failed parity receipt, without another fit.
    negative=output/'deliberately-failed-synthetic-parity';negative.mkdir()
    workflow.save(negative/'model-fitted.json',model)
    negative_model=dict(model,model_fitted_sha256=workflow.sha((negative/'model-fitted.json').read_bytes()))
    try:
        workflow.head_parity(negative,negative_model,x+np.float32(1),classifier,z)
    except ValueError as failure:
        workflow.save(negative/'fit-error.json',workflow.failure_receipt(negative,failure))
    else:
        raise AssertionError('Synthetic negative control did not reject wrong inputs')
    failed=workflow.load(negative/'fit-error.json')
    assert set(failed['persisted_fit_receipts'])=={'model-fitted.json','head-parity.json'}
    assert not workflow.load(negative/'head-parity.json')['passed']
    normalizer_error=float(np.max(np.abs(z-workflow.normalized(x,model['embedding_mean'],model['embedding_scale']))))
    sources={name:workflow.sha(inspect.getsource(value).encode()) for name,value in (
        ('LogisticRegression.predict_proba',LogisticRegression.predict_proba),
        ('LinearClassifierMixin.decision_function',LinearClassifierMixin.decision_function),
        ('LinearClassifierMixin._predict_proba_lr',LinearClassifierMixin._predict_proba_lr))}
    report=dict(synthetic_only=True,subject_data_read=False,outcomes_read=False,checkpoint_read=False,
        study_cache_read=False,seed=20261101,shape=list(x.shape),input_sha256=workflow.sha(x.astype('<f4').tobytes()),
        synthetic_label_sha256=workflow.sha(y.astype('<i8').tobytes()),
        coefficient_dtype=model['coefficient_dtype'],intercept_dtype=model['intercept_dtype'],
        primary_probability_dtype=str(classifier.predict_proba(z).dtype),legacy_max_error=legacy_error,
        corrected_max_error=error,batch_max_errors=batch_errors,restored_primary_max_error=restored_error,
        scaler_max_error=normalizer_error,tolerance=1e-12,
        fitted_artifact_sha256=model['model_fitted_sha256'],failed_parity_receipt_verified=True,
        runtime=dict(python=sys.version,numpy=np.__version__,scipy=scipy.__version__,sklearn=sklearn.__version__),
        installed_primary_source_sha256=sources,
        workflow_sha256=workflow.sha(Path(workflow.__file__).read_bytes()),
        synthetic_check_sha256=workflow.sha(Path(__file__).read_bytes()))
    report['passed']=bool(error<=1e-12 and all(value<=1e-12 for value in batch_errors.values())
        and restored_error<=1e-12 and normalizer_error<=1e-6 and legacy_error>1e-12)
    workflow.save(output/'synthetic-parity.json',report)
    print(json.dumps(report,indent=2),flush=True)
    if not report['passed']:raise AssertionError('Synthetic arithmetic parity failed; no study replay')


if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output',type=Path,required=True)
    run(parser.parse_args().output)
