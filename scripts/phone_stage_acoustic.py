"""Bounded original acoustic pilot. Research only; no Android stage claim.

Consumes the frozen crop plan and actual PCM, never the author's executable.
All inputs, missing windows, probabilities and prospective gates are recorded.
"""
import os
for _name in ("OMP_NUM_THREADS", "OPENBLAS_NUM_THREADS", "MKL_NUM_THREADS", "NUMEXPR_NUM_THREADS"):
    os.environ[_name] = "2"
import argparse
import hashlib
import json
import urllib.request
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
import numpy as np
import scipy
from scipy.signal.windows import hann
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import balanced_accuracy_score, confusion_matrix, f1_score, recall_score
import sklearn
import phone_stage_dataset as data


def configure(cache):
    data.traffic_path = cache.parent / "traffic-ledger.json"
    data.traffic = json.loads(data.traffic_path.read_bytes())
    data.NETWORK_LIMIT = 650_000_000


def stem(row):
    return f"{row['split']}-{row['subject']}-{row['epoch']:04}"


def acquire(cache, row):
    folder = cache / "pcm"
    folder.mkdir(exist_ok=True)
    target = folder / (stem(row) + ".pcm")
    receipt_path = folder / (stem(row) + ".json")
    if target.exists() and receipt_path.exists():
        receipt = json.loads(receipt_path.read_bytes())
        if receipt["window"] != row or data.digest(target.read_bytes()) != receipt["sha256"]:
            raise ValueError("Cached PCM differs from its pinned crop receipt")
        return receipt
    size = row["byte_end"] - row["byte_start"] + 1
    data.reserve(size + 1)
    request = urllib.request.Request(data.DOWNLOAD + row["file_id"], headers={
        "Range": f"bytes={row['byte_start']}-{row['byte_end']}"})
    expected = "bytes {byte_start}-{byte_end}/{published_file_bytes}".format(**row)
    with urllib.request.urlopen(request, timeout=45) as response:
        if (response.status != 206 or response.headers.get("Content-Range") != expected
                or response.headers.get("Content-Length") != str(size)):
            raise ValueError("Range/status/length not honored; response closed before reading body")
        raw = response.read(size + 1)
    data.release(size + 1 - len(raw))
    if len(raw) != size:
        raise ValueError("Incomplete bounded PCM range; no padding or retry")
    target.write_bytes(raw)
    receipt = dict(window=row, bytes=size, sha256=data.digest(raw), content_range=expected)
    data.save_json(receipt_path, receipt)
    return receipt


def block_features(x):
    if len(x) != 240000 or not np.all(np.isfinite(x)):
        raise ValueError("Incomplete/nonfinite30s block")
    envelope = np.sqrt(np.mean(x.reshape(120, 2000) ** 2, axis=1))
    if np.count_nonzero(envelope == 0) > 40:
        raise ValueError("More than10s exact-zero audio in30s block")
    frames = np.lib.stride_tricks.sliding_window_view(x, 512)[::160]
    power = np.abs(np.fft.rfft(frames * hann(512, sym=False), axis=1)) ** 2 / 192.0
    frequencies = np.fft.rfftfreq(512, 1/8000)
    mel = lambda f: 2595 * np.log10(1 + f/700)
    edges = 700 * (10 ** (np.linspace(mel(50), mel(3500), 34)/2595) - 1)
    banks = np.maximum(0, np.minimum(
        (frequencies[None, :] - edges[:-2, None]) / np.diff(edges)[:-1, None],
        (edges[2:, None] - frequencies[None, :]) / np.diff(edges)[1:, None]))
    logmel = np.log10(power @ banks.T + 1e-12)
    centroid = (power @ frequencies) / (power.sum(axis=1) + 1e-12)
    flatness = np.exp(np.mean(np.log(power + 1e-12), axis=1)) / (power.mean(axis=1) + 1e-12)
    zcr = np.mean(np.diff(np.signbit(frames), axis=1), axis=1)
    centered = envelope - envelope.mean()
    norm = np.dot(centered, centered)
    correlations = [np.dot(centered[:-lag], centered[lag:]) / norm if norm > 0 else 0
                    for lag in (4, 8, 12, 16, 20, 24)]
    return np.concatenate((logmel.mean(axis=0), logmel.std(axis=0),
                           np.percentile(envelope, [5,25,50,75,95]),
                           [centroid.mean(),centroid.std(),flatness.mean(),flatness.std(),zcr.mean(),zcr.std()],
                           correlations))


def features(cache, rows):
    matrix, labels, accepted, missing = [], [], [], []
    for row in rows:
        source = cache / "pcm" / (stem(row) + ".pcm")
        receipt = json.loads(source.with_suffix(".json").read_bytes())
        raw = source.read_bytes()
        if receipt["window"] != row or data.digest(raw) != receipt["sha256"]:
            raise ValueError("Input digest/target differs from acquired range")
        x = np.frombuffer(raw, dtype="<i2").astype(np.float64) / 32768
        try:
            if len(x) != 720000:
                raise ValueError("Incomplete90s context")
            vector = np.concatenate([block_features(x[start:start+240000]) for start in (0,240000,480000)])
            if len(vector) != 243 or not np.all(np.isfinite(vector)):
                raise ValueError("Invalid acoustic feature vector")
        except ValueError as failure:
            missing.append(dict(window=row, reason=str(failure), input_sha256=receipt["sha256"]))
            continue
        matrix.append(vector)
        labels.append(row["label"])
        accepted.append(dict(window=row, input_sha256=receipt["sha256"],
                             feature_sha256=data.digest(vector.astype("<f8").tobytes())))
    return np.asarray(matrix, dtype=np.float64), np.asarray(labels), accepted, missing


def portable_predict(model, matrix):
    # sklearn trees compare float32 input values against their fitted thresholds.
    x = matrix.astype(np.float32)
    probabilities = np.zeros((len(x), 4))
    for tree in model["trees"]:
        for index, vector in enumerate(x):
            node = 0
            while tree["left"][node] >= 0:
                node = tree["left"][node] if vector[tree["feature"][node]] <= tree["threshold"][node] else tree["right"][node]
            probabilities[index] += tree["value"][node]
    return probabilities / len(model["trees"])


def assess(model, x, y, accepted, missing):
    if len(y) == 0:
        return dict(accepted=0, missing=missing, coverage=0, prospective_gate_passed=False,
                    probabilities=[], issue="No actual usable validation inputs")
    probabilities = portable_predict(model, x)
    prediction = probabilities.argmax(axis=1)
    baseline = np.full(len(y), model["train_majority"])
    recalls = recall_score(y, prediction, labels=range(4), average=None, zero_division=0)
    macro = f1_score(y, prediction, labels=range(4), average="macro", zero_division=0)
    majority_macro = f1_score(y, baseline, labels=range(4), average="macro", zero_division=0)
    balanced = balanced_accuracy_score(y, prediction)
    coverage = len(y) / (len(y) + len(missing))
    useful = bool(coverage >= .8 and macro >= majority_macro + .05 and balanced >= .35
                  and np.all(recalls > 0) and len(set(prediction)) == 4)
    return dict(accepted=len(y), missing=missing, coverage=coverage,
                source_counts=np.bincount(y, minlength=4).tolist(),
                prediction_counts=np.bincount(prediction, minlength=4).tolist(),
                confusion=confusion_matrix(y,prediction,labels=range(4)).tolist(),
                macro_f1=macro, majority_macro_f1=majority_macro,
                balanced_accuracy=balanced, per_class_recall=recalls.tolist(),
                prospective_gate_passed=useful,
                probabilities=[dict(**receipt, probabilities=p.tolist()) for receipt,p in zip(accepted, probabilities)])


def fit(cache, plan, protocol_path):
    output = cache / "model.json"
    if output.exists():
        raise ValueError("A fixed fit already exists; no overwrite/parameter search")
    train = [row for row in plan["windows"] if row["split"] == "train"]
    validation = [row for row in plan["windows"] if row["split"] == "validation"]
    x,y,accepted,missing = features(cache, train)
    vx,vy,vaccepted,vmissing = features(cache,validation)
    if set(y) != set(range(4)):
        raise ValueError("Missing training class; no fabricated class")
    classifier = RandomForestClassifier(n_estimators=128,max_depth=8,min_samples_leaf=4,
        max_features="sqrt",class_weight="balanced_subsample",n_jobs=2,random_state=20261031)
    started=cache/"fit-started.json"
    if started.exists():
        raise ValueError("A fixed fit was already started; do not silently refit")
    data.save_json(started,dict(protocol_sha256=data.digest(protocol_path.read_bytes()),
                               source_sha256=data.digest(Path(__file__).read_bytes()),
                               train_windows=len(y),validation_windows=len(vy)))
    classifier.fit(x,y)
    trees = []
    for estimator in classifier.estimators_:
        tree = estimator.tree_
        values = tree.value[:,0,:]
        values = values / values.sum(axis=1,keepdims=True)
        trees.append(dict(left=tree.children_left.tolist(),right=tree.children_right.tolist(),
                          feature=tree.feature.tolist(),threshold=tree.threshold.tolist(),value=values.tolist()))
    model = dict(protocol=data.PROTOCOL,classes=["Wake","REM","Light","Deep"],
                 feature_count=243,feature_dtype="float64; tree comparisons cast to float32",
                 feature_source_sha256=data.digest(Path(__file__).read_bytes()),
                 protocol_sha256=data.digest(protocol_path.read_bytes()),sklearn_version=sklearn.__version__,
                 numpy_version=np.__version__,scipy_version=scipy.__version__,
                 crop_plan_sha256=data.digest((cache/"crop-plan.json").read_bytes()),
                 train_counts=np.bincount(y,minlength=4).tolist(),train_majority=int(np.bincount(y,minlength=4).argmax()),
                 train_accepted=accepted,train_missing=missing,trees=trees)
    error = float(np.max(np.abs(classifier.predict_proba(x)-portable_predict(model,x))))
    if error > 1e-12:
        raise ValueError("Portable tree decoder differs from primary sklearn runtime")
    model["primary_runtime_max_probability_difference"] = error
    data.save_json(output,model)
    report = assess(model,vx,vy,vaccepted,vmissing)
    report["model_sha256"] = data.digest(output.read_bytes())
    report["scope"] = "Prospective validation only; test subjects not read. No clinical accuracy or Android integration claimed."
    data.save_json(cache/"validation.json",report)
    print(json.dumps({key:value for key,value in report.items() if key not in ("probabilities","missing")}),flush=True)


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command",choices=("fetch","fit"))
    parser.add_argument("--cache",type=Path,required=True)
    parser.add_argument("--protocol",type=Path,default=Path("docs/research/2026-10-31-phone-stage-pilot-protocol.json"))
    args=parser.parse_args()
    configure(args.cache)
    plan=json.loads((args.cache/"crop-plan.json").read_bytes())
    if plan["protocol"] != data.PROTOCOL or any(int(row["subject"]) > 40 for row in plan["windows"]):
        raise ValueError("Unexpected protocol/test access before model freeze")
    if args.command == "fetch":
        with ThreadPoolExecutor(max_workers=2) as workers:
            for receipt in workers.map(lambda row:acquire(args.cache,row),plan["windows"]):
                print(json.dumps(dict(window=stem(receipt["window"]),bytes=receipt["bytes"],sha256=receipt["sha256"])),flush=True)
    else:
        fit(args.cache,plan,args.protocol)


if __name__ == "__main__":
    main()
