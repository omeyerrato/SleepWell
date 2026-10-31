"""Describe saved frozen probabilities. Never fits, predicts, or recalibrates."""
import os
for _name in ("OMP_NUM_THREADS", "OPENBLAS_NUM_THREADS", "MKL_NUM_THREADS"):
    os.environ[_name] = "2"
import argparse
import hashlib
import json
from pathlib import Path
import numpy as np
from sklearn.metrics import confusion_matrix, precision_recall_fscore_support, f1_score


def reliability(confidence, correct):
    bins=[]
    for index in range(10):
        selected=(confidence >= index/10) & ((confidence < (index+1)/10) if index < 9 else (confidence <= 1))
        count=int(selected.sum())
        bins.append(dict(lower=index/10,upper=(index+1)/10,count=count,
                         mean_score=float(confidence[selected].mean()) if count else None,
                         observed_fraction=float(correct[selected].mean()) if count else None))
    ece=sum(row["count"]*abs(row["mean_score"]-row["observed_fraction"]) for row in bins if row["count"])/len(confidence)
    return dict(equal_width_bins=10,expected_calibration_error=ece,bins=bins)


def scores(y,p):
    truth=np.eye(4)[y]
    prediction=p.argmax(axis=1)
    precision,recall,f1,support=precision_recall_fscore_support(y,prediction,labels=range(4),zero_division=0)
    return dict(accuracy=float(np.mean(prediction==y)),macro_f1=float(f1_score(y,prediction,labels=range(4),average="macro",zero_division=0)),
                confusion=confusion_matrix(y,prediction,labels=range(4)).tolist(),
                precision=precision.tolist(),recall=recall.tolist(),f1=f1.tolist(),support=support.tolist(),
                multiclass_brier_sum=float(np.mean(np.sum((p-truth)**2,axis=1))),
                per_class_brier=np.mean((p-truth)**2,axis=0).tolist(),
                negative_log_likelihood=float(-np.log(np.maximum(p[np.arange(len(y)),y],np.finfo(float).eps)).mean()),
                top_score_reliability=reliability(p.max(axis=1),prediction==y),
                per_class_reliability=[reliability(p[:,index],y==index) for index in range(4)])


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--cache",type=Path,required=True)
    parser.add_argument("--phase",choices=("validation","test"),required=True)
    args=parser.parse_args()
    report_path=args.cache/(args.phase+".json")
    raw=report_path.read_bytes()
    report=json.loads(raw)
    model_raw=(args.cache/"model.json").read_bytes()
    model=json.loads(model_raw)
    if hashlib.sha256(model_raw).hexdigest() != report["model_sha256"]:
        raise ValueError("Saved probability report references a different model")
    records=report["probabilities"]
    if not records:
        raise ValueError("No saved probabilities to assess")
    p=np.array([row["probabilities"] for row in records])
    y=np.array([row["window"]["label"] for row in records])
    prior=np.array(model["train_natural_eligible_counts"],dtype=float)
    prior/=prior.sum()
    subjects=sorted({row["window"]["subject"] for row in records})
    result=dict(phase=args.phase,classes=model["classes"],targets=len(y),subjects=subjects,
                report_sha256=hashlib.sha256(raw).hexdigest(),model_sha256=report["model_sha256"],
                model=scores(y,p),natural_training_prior=prior.tolist(),
                constant_natural_prior_baseline=scores(y,np.tile(prior,(len(y),1))),
                per_subject=[dict(subject=subject,targets=sum(row["window"]["subject"]==subject for row in records),
                                  accuracy=float(np.mean([int(np.argmax(row["probabilities"]))==row["window"]["label"]
                                                          for row in records if row["window"]["subject"]==subject]))) for subject in subjects],
                recalibration_applied=False,new_inference=False,new_fit=False,
                scope="Saved sampled30s target probabilities only; not complete-night or clinical accuracy.")
    output=args.cache/(args.phase+"-metrics.json")
    output.write_text(json.dumps(result,indent=2)+"\n",encoding="utf-8")
    print(json.dumps(dict(phase=args.phase,targets=len(y),model_accuracy=result["model"]["accuracy"],
                         baseline_accuracy=result["constant_natural_prior_baseline"]["accuracy"],
                         brier=result["model"]["multiclass_brier_sum"],baseline_brier=result["constant_natural_prior_baseline"]["multiclass_brier_sum"],
                         negative_log_likelihood=result["model"]["negative_log_likelihood"],
                         baseline_negative_log_likelihood=result["constant_natural_prior_baseline"]["negative_log_likelihood"],
                         top_score_ece=result["model"]["top_score_reliability"]["expected_calibration_error"])),flush=True)


if __name__ == "__main__":
    main()
