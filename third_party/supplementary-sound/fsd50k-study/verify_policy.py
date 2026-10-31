"""Bounded host verification, no downloads/playback. Reuses frozen study scores; infers retained controls only."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess
import time

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
read = lambda p: json.loads(p.read_text(encoding="utf-8"))


def save(path, value):
    path.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")


def main(args):
    import numpy as np
    import onnxruntime as ort
    if ort.__version__ != "1.23.2":
        raise ValueError("Expected pinned ONNX Runtime 1.23.2")
    output = args.cache / "policy-verification"
    output.mkdir(exist_ok=True)
    prior = read(output / "summary.json") if (output / "summary.json").exists() else None
    if prior is not None and not args.resume_controls:
        raise ValueError("Verification already complete; inspect retained output")
    classes = output / "classes"
    classes.mkdir(exist_ok=True)
    source = ROOT / "app/src/main/java/org/sleepwell/app"
    java_sources = [source / name for name in ["RecordedSoundPolicy.java", "RecordedSoundFrontend.java", "RecordedPcmResampler.java"]]
    java_sources += [HERE / name for name in ["StudyFrontend.java", "ControlFrontend.java", "RecordedPolicyOracle.java"]]
    subprocess.run([str(args.javac), "-d", str(classes), *map(str, java_sources)], check=True)

    def oracle(windows, name):
        vectors = output / (name + ".f32")
        np.asarray([w["scores"][m] for w in windows for m in ["pann", "efficient"]], dtype="<f4").tofile(vectors)
        rows = subprocess.check_output([str(args.java), "-XX:ActiveProcessorCount=2", "-cp", str(classes),
                                       "org.sleepwell.app.RecordedPolicyOracle", str(vectors)], text=True).splitlines()
        if len(rows) != len(windows):
            raise ValueError("Policy output count changed")
        results = []
        for window, line in zip(windows, rows):
            fields = line.split("\t")
            result = {"candidate": fields[0], "instrument_model": fields[1]}
            for n, family in enumerate(["laughter", "instrument", "efficient_instrument", "pann_instrument"]):
                score, competitor, qualifies, index = fields[2 + 4*n:6 + 4*n]
                result[family] = {"score": float(score), "competitor": float(competitor), "qualifies": qualifies == "true", "index": int(index)}
            results.append(result)
        return results

    summary = {"policy_sha256": sha(source / "RecordedSoundPolicy.java"), "frozen_policy_sha256": sha(HERE / "frozen-policy.json"),
               "frontend_sha256": {p.name: sha(p) for p in java_sources[1:3]}, "cached_splits": {}, "controls": []}
    if prior is not None:
        if any(prior[key] != summary[key] for key in ["policy_sha256", "frozen_policy_sha256", "frontend_sha256"]):
            raise ValueError("Cannot resume controls after policy/frontend changes")
        summary["controls"] = prior["controls"]
    import study
    frozen = read(HERE / "frozen-policy.json")
    for split in ["dev", "eval"]:
        path = args.cache / (split + "-results.json")
        data = read(path)
        checked = 0
        for clip in data["clips"]:
            decisions = oracle(clip["windows"], split + "-" + clip["id"])
            for window, decision in zip(clip["windows"], decisions):
                for family in ["laughter", "instrument"]:
                    policy = frozen["policy"][family]
                    expected = study.accepted({"windows": [window]}, family, policy["threshold"], policy["margin"], policy["model"])
                    if decision[family]["qualifies"] != expected:
                        raise ValueError("Shipping policy differs from frozen policy")
                if decision["efficient_instrument"]["qualifies"] and decision["instrument"] != decision["efficient_instrument"]:
                    raise ValueError("Accepted EfficientAT decision changed")
                checked += 1
        summary["cached_splits"][split] = {"results_sha256": sha(path), "sources": len(data["clips"]), "windows_matched": checked,
                                             "new_inference": False}

    # These are old negative controls, never a development or evaluation retuning set.
    manifest_paths = [args.original / "third_party/audio-fixtures" / name for name in ["manifest.json", "laughter-holdout.json"]]
    controls = [row for path in manifest_paths for row in read(path)["fixtures"]
                if row["expected_broad_class"] in ["snoring", "speech", "cough", "baby_cry", "adult_cry"]]
    completed = {row["source"]["name"] for row in summary["controls"]}
    controls = [row for row in controls if row["name"] not in completed]
    models = {"pann": ("panns-laughter.onnx", "f9dd56478e434e067cef505489d0ae4483d80d357e2c35bc3d94ba432eb08f3f"),
              "efficient": ("efficientat-instruments.onnx", "7bb4227052d796a1ef35f0a5b703fdd724cd200fc7e6f85087289222e6c5b5c8")}
    options = ort.SessionOptions()
    options.intra_op_num_threads = options.inter_op_num_threads = 1
    options.execution_mode = ort.ExecutionMode.ORT_SEQUENTIAL
    engines = {}
    for model, (name, digest) in models.items():
        if sha(args.assets / name) != digest:
            raise ValueError("Bundled model changed")
        engines[model] = ort.InferenceSession(str(args.assets / name), sess_options=options, providers=["CPUExecutionProvider"])
    summary["model_sha256"] = {name: digest for name, digest in models.values()}
    for control in controls:
        name = control["name"]
        pcm = args.original / "app/src/androidTest/assets/classifier-fixtures" / (name + ".pcm")
        if sha(pcm) != control["pcm_sha256"] or pcm.stat().st_size != control["samples"] * 2:
            raise ValueError("Retained control changed")
        directory = output / name
        directory.mkdir(exist_ok=True)
        started = time.perf_counter()
        subprocess.run([str(args.java), "-XX:ActiveProcessorCount=2", "-Xmx512m", "-cp", str(classes),
                        "org.sleepwell.app.ControlFrontend", str(args.assets), str(pcm), str(directory)], check=True)
        windows = []
        for start in range(0, control["samples"], 160000):
            count = min(160000, control["samples"] - start)
            if count < 32000:
                break
            scores = {}
            for model, bands in [("pann", 64), ("efficient", 128)]:
                mel = np.fromfile(directory / f"{start}.{model}.f32", dtype="<f4")
                shape = (1, 1, len(mel)//bands, bands) if model == "pann" else (1, 1, bands, len(mel)//bands)
                values = engines[model].run(None, {"mel": mel.reshape(shape)})[0][0]
                if values.shape != (527,) or not np.all(np.isfinite(values)) or np.any(values < 0) or np.any(values > 1):
                    raise ValueError("Invalid model scores")
                scores[model] = values.tolist()
            windows.append({"start_sample": start, "samples": count, "scores": scores})
        decisions = oracle(windows, name)
        full = {"source": control, "windows": [{**w, **d} for w, d in zip(windows, decisions)]}
        save(directory / "scores.json", full)
        new_instruments = sum(d["instrument"]["qualifies"] and not d["efficient_instrument"]["qualifies"] for d in decisions)
        summary["controls"].append({"source": control, "samples": control["samples"],
                                    "omitted_tail_samples": control["samples"] - sum(w["samples"] for w in windows),
                                    "processing_seconds": time.perf_counter() - started, "scores_sha256": sha(directory / "scores.json"),
                                    "new_instrument_windows": new_instruments, "windows": decisions})
        print("control", name, "windows", len(windows), "new_instrument_windows", new_instruments, flush=True)
    summary["all_controls_no_new_instruments"] = all(r["new_instrument_windows"] == 0 for r in summary["controls"])
    save(output / "summary.json", summary)
    if not summary["all_controls_no_new_instruments"]:
        raise ValueError("Retained controls reject shipping; do not tune on them")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    for name in ["cache", "assets", "original", "java", "javac"]:
        parser.add_argument("--" + name, required=True, type=Path)
    parser.add_argument("--resume-controls", action="store_true", help="Run only missing controls with unchanged policy/frontend")
    main(parser.parse_args())
