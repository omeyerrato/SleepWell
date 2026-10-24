"""Verify pinned PCM, or evaluate the frozen candidate offline. Never captures/plays audio.

Inference requires an explicitly installed ai-edge-litert==2.3.0 Python runtime.
The actual production Java SoundAnalysis/Group owns decisions; Python only runs
the bundled model and packages scores. Candidate failure exits 2 AFTER evidence
is saved. This command does not change production thresholds or claim accuracy.
"""
import argparse
import hashlib
import io
import json
import math
from pathlib import Path
import struct
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[1]
MODEL_SHA = "10c95ea3eb9a7bb4cb8bddf6feb023250381008177ac162ce169694d05c317de"
KINDS = ("snoring", "speech", "cough", "baby_cry", "laughter", "noise")
POSITIVE = ("laughter-jody", "laughter-morning", "laughter-eneasz")
NEGATIVE = ("speech-balloon", "music-archtects", "crying-alucard")

# No copy of snore/speech/cry/cough rules: both decision streams call the actual
# production classifier. The frozen candidate may replace only an unclassified
# or Other result. A qualified existing class cannot be stolen by a <0.65
# laughter candidate while retaining the 0.15 unrelated-class margin.
PROBE = r'''
package org.sleepwell.app;
import java.io.*;
import java.nio.charset.StandardCharsets;
public final class LaughterHoldoutProbe {
 static SoundAnalysis.Result candidate(float[] scores,SoundAnalysis.Result baseline){
  if(baseline!=null&&!baseline.category.equals("noise"))return baseline;
  float positive=0,competitor=0;
  for(int n=0;n<scores.length;n++)if(n>=13&&n<=18)positive=Math.max(positive,scores[n]);else competitor=Math.max(competitor,scores[n]);
  return positive>=.30f&&positive-competitor>=.15f?new SoundAnalysis.Result("laughter",positive):baseline;
 }
 public static void main(String[] args)throws Exception{
  DataInputStream in=new DataInputStream(new BufferedInputStream(System.in));
  int fixtures=in.readInt();for(int f=0;f<fixtures;f++){
   String name=new String(in.readNBytes(in.readInt()),StandardCharsets.UTF_8);int frames=in.readInt();
   SoundAnalysis.Group original=new SoundAnalysis.Group((at,count,category,score)->System.out.println("E\t"+name+"\tbaseline\t"+at+"\t"+count+"\t"+category+"\t"+score));
   SoundAnalysis.Group proposed=new SoundAnalysis.Group((at,count,category,score)->System.out.println("E\t"+name+"\tcandidate\t"+at+"\t"+count+"\t"+category+"\t"+score));
   for(int frame=0;frame<frames;frame++){
    float[] scores=new float[521];for(int n=0;n<scores.length;n++)scores[n]=in.readFloat();
    SoundAnalysis.Result baseline=SoundAnalysis.classify(scores),changed=candidate(scores,baseline);
    System.out.println("W\t"+name+"\t"+frame+"\t"+(baseline==null?"none":baseline.category)+"\t"+(changed==null?"none":changed.category));
    original.add(1+frame*975L,baseline);proposed.add(1+frame*975L,changed);
   }original.flush();proposed.flush();
  }if(in.read()!=-1)throw new IOException("Unexpected score bytes");
 }
}
'''


def sha(data):
    return hashlib.sha256(data).hexdigest()


def verify():
    original = json.loads((ROOT / "third_party/audio-fixtures/manifest.json").read_text(encoding="utf-8"))
    holdout = json.loads((ROOT / "third_party/audio-fixtures/laughter-holdout.json").read_text(encoding="utf-8"))
    rows = original["fixtures"] + holdout["fixtures"]
    if len(rows) != 16 or len({r["name"] for r in rows}) != 16:
        raise ValueError("All ten original and six predeclared sources must remain")
    for row in rows:
        data = (ROOT / "app/src/androidTest/assets/classifier-fixtures" / (row["name"] + ".pcm")).read_bytes()
        if row["license"] != "CC0-1.0" or len(data) != row["samples"] * 2 or sha(data) != row["pcm_sha256"]:
            raise ValueError("Changed fixture or licence: " + row["name"])
        if len(data) > 3 * 1024 * 1024 or len(data) > 80 * 32000:
            raise ValueError("Fixture exceeds predeclared bound")
        print(row["name"] + ": pinned CC0 PCM verified", flush=True)
    model = ROOT / "app/src/main/assets/yamnet.tflite"
    if sha(model.read_bytes()) != MODEL_SHA:
        raise ValueError("Bundled model changed; this is a different experiment")
    return rows, model


def infer(rows, model, args):
    if args.runtime_dir:
        sys.path.insert(0, str(args.runtime_dir.resolve()))
    import importlib.metadata
    import numpy as np
    from ai_edge_litert.interpreter import Interpreter

    if importlib.metadata.version("ai-edge-litert") != "2.3.0":
        raise ValueError("Experiment requires ai-edge-litert==2.3.0")
    engine = Interpreter(model_path=str(model), num_threads=1)
    engine.allocate_tensors()
    inputs, outputs = engine.get_input_details(), engine.get_output_details()
    if len(inputs) != 1 or len(outputs) != 1 or list(inputs[0]["shape"]) != [15600] or list(outputs[0]["shape"]) != [1, 521]:
        raise ValueError("Unexpected model shape")
    if inputs[0]["dtype"] != np.float32 or outputs[0]["dtype"] != np.float32:
        raise ValueError("Unexpected model tensor type")
    packet = io.BytesIO()
    packet.write(struct.pack(">I", len(rows)))
    reports = {}
    for row in rows:
        pcm = np.frombuffer((ROOT / "app/src/androidTest/assets/classifier-fixtures" / (row["name"] + ".pcm")).read_bytes(), dtype="<i2").astype(np.float32) / 32768
        frames = len(pcm) // 15600
        name = row["name"].encode("utf-8")
        packet.write(struct.pack(">I", len(name)) + name + struct.pack(">I", frames))
        windows = []
        for n in range(frames):
            engine.set_tensor(inputs[0]["index"], pcm[n * 15600:(n + 1) * 15600])
            engine.invoke()
            scores = engine.get_tensor(outputs[0]["index"]).reshape(-1)
            if not np.all(np.isfinite(scores)) or np.any(scores < 0) or np.any(scores > 1):
                raise ValueError("Invalid model score")
            packet.write(scores.astype(">f4").tobytes())
            order = sorted(range(521), key=lambda k: (-float(scores[k]), k))[:8]
            windows.append({"start_ms": n * 975, "laughter_family_score": float(max(scores[13:19])), "top8": [{"index": k, "score": float(scores[k])} for k in order]})
        reports[row["name"]] = {"fixture": row["name"], "pcm_sha256": row["pcm_sha256"], "frames": frames, "windows": windows, "events": {v: [] for v in ("baseline", "candidate")}, "counts": {v: {k: 0 for k in KINDS} for v in ("baseline", "candidate")}}
    del engine

    # Compile only the Android-free actual policy and the tiny analysis driver.
    # No Gradle, APK, device, model download, provider or microphone is involved.
    with tempfile.TemporaryDirectory(prefix="sleepwell-policy-probe-") as folder:
        work = Path(folder)
        source = work / "LaughterHoldoutProbe.java"
        source.write_text(PROBE, encoding="utf-8")
        policy = ROOT / "app/src/main/java/org/sleepwell/app/SoundAnalysis.java"
        subprocess.run([str(args.jdk / "bin/javac.exe"), "-J-Xmx64m", "-encoding", "UTF-8", "-d", str(work), str(policy), str(source)], check=True)
        completed = subprocess.run([str(args.jdk / "bin/java.exe"), "-Xmx64m", "-cp", str(work), "org.sleepwell.app.LaughterHoldoutProbe"], input=packet.getvalue(), stdout=subprocess.PIPE, check=True)
    for line in completed.stdout.decode("utf-8").splitlines():
        fields = line.split("\t")
        report = reports[fields[1]]
        if fields[0] == "W":
            report["windows"][int(fields[2])].update(baseline=fields[3], candidate=fields[4])
        elif fields[0] == "E":
            _, _, variant, at, count, category, score = fields
            report["events"][variant].append({"start_ms": int(at) - 1, "duration_ms": int(count) * 975, "category": category, "score": float(score)})
            report["counts"][variant][category] += 1
        else:
            raise ValueError("Unexpected Java probe output")
    return list(reports.values())


def compare_android(by_name, archived, original):
    """Exact evidence comparison; truncated/reordered/duplicate input cannot pass."""
    fixtures = archived.get("fixtures", [])
    names = [row.get("fixture") for row in fixtures]
    compatible = len(names) == len(original) and len(set(names)) == len(names) and set(names) == set(original)
    differences = []
    for old in fixtures:
        name = old.get("fixture")
        if name not in original or name not in by_name:
            compatible = False
            differences.append({"fixture": name, "reason": "unexpected_fixture"})
            continue
        new = by_name[name]
        fields_match = old.get("frames") == new["frames"] and old.get("counts") == new["counts"]["baseline"] and old.get("pcm_sha256") == new["pcm_sha256"]
        compatible &= fields_match
        if not fields_match:
            differences.append({"fixture": name, "reason": "frame_count_counts_or_pcm_hash"})
        before, after = old.get("windows", []), new["windows"]
        lengths_match = len(before) == old.get("frames") == len(after) == new["frames"]
        compatible &= lengths_match
        if not lengths_match:
            differences.append({"fixture": name, "reason": "truncated_window_array"})
            continue
        for n, (a, b) in enumerate(zip(before, after)):
            if type(a.get("start_ms")) is not int or type(b.get("start_ms")) is not int or a["start_ms"] != n * 975 or b["start_ms"] != n * 975:
                compatible = False
                differences.append({"fixture": name, "window": n, "reason": "window_position"})
            def valid_top(values):
                return len(values) == 8 and len({x.get("index") for x in values}) == 8 and all(type(x.get("index")) is int and 0 <= x["index"] < 521 and type(x.get("score")) in (int, float) and math.isfinite(x["score"]) and 0 <= x["score"] <= 1 for x in values)
            if not valid_top(a.get("top8", [])) or not valid_top(b.get("top8", [])):
                compatible = False
                differences.append({"fixture": name, "window": n, "reason": "incomplete_or_invalid_top8"})
                continue
            for y in a["top8"]:
                actual = next((x["score"] for x in b["top8"] if x["index"] == y["index"]), None)
                if actual is None or abs(actual - y["score"]) >= 1e-6:
                    compatible = False
                    differences.append({"fixture": name, "window": n, "reason": "top8_score", "index": y["index"], "android": y["score"], "host": actual})
    return {"compatible": compatible, "exact_fixture_set": set(names) == set(original) and len(names) == len(original), "differences": differences}


def check(reports, android_baseline):
    by_name = {r["fixture"]: r for r in reports}
    original = [r["name"] for r in json.loads((ROOT / "third_party/audio-fixtures/manifest.json").read_text(encoding="utf-8"))["fixtures"]]
    def count(name, category, variant="candidate"):
        return by_name[name]["counts"][variant][category]
    checks = {
        "old_solo_laughter_recovered": count("laughter-solo", "laughter") > 0,
        "at_least_two_predeclared_new_laughter_sources": sum(count(n, "laughter") > 0 for n in POSITIVE) >= 2,
        "zero_laughter_on_all_three_new_distractors": all(count(n, "laughter") == 0 for n in NEGATIVE),
        "unchanged_old_snore_speech_cough_baby_counts": all(count(n, k) == count(n, k, "baseline") for n in original for k in ("snoring", "speech", "cough", "baby_cry")),
        "retained_original_cough_positive": count("cough-fabrizio", "cough") + count("cough-crowd", "cough") > 0,
        "retained_both_infant_positives": all(count(n, "baby_cry") > 0 for n in ("babycry-bonehead", "babycry-ciccarelli")),
        "old_solo_cross_negatives": all(count("laughter-solo", k) == 0 for k in ("baby_cry", "cough")),
        "old_independent_cry_cross_negatives": all(count("babycry-ciccarelli", k) == 0 for k in ("laughter", "cough")),
        "old_snore_speech_cross_negatives": all(count(n, k) == 0 for n in ("snoring-sorts", "snoring-pencil", "speech-countdown", "speech-crowd") for k in ("laughter", "cough", "baby_cry")),
        "all_non_snore_sources_reject_snoring": all(count(n, "snoring") == 0 for n in by_name if not n.startswith("snoring-")),
        "old_snore_and_solo_speech_positives": count("snoring-sorts", "snoring") + count("snoring-pencil", "snoring") > 0 and count("speech-countdown", "speech") > 0,
    }
    android = None
    if android_baseline:
        archived = json.loads(android_baseline.read_text(encoding="utf-8"))
        android = compare_android(by_name, archived, original)
        checks["host_baseline_matches_archived_android_scores_and_counts"] = android["compatible"]
    return checks, android


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--infer", action="store_true")
    parser.add_argument("--runtime-dir", type=Path)
    parser.add_argument("--jdk", type=Path)
    parser.add_argument("--android-baseline", type=Path)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    rows, model = verify()
    if not args.infer:
        return 0
    if not args.jdk or not args.output:
        parser.error("--infer requires --jdk and --output; no dependencies are installed automatically")
    reports = infer(rows, model, args)
    checks, android = check(reports, args.android_baseline)
    report = {"model_sha256": MODEL_SHA, "production_policy_sha256": sha((ROOT / "app/src/main/java/org/sleepwell/app/SoundAnalysis.java").read_bytes()), "plan_commit": "bc92056874aa83005c99fbffbc788cac9251a33a", "fixtures_pinned_before_inference_commit": "60f929a2ccc637b567d97a12c53e89c2903dc1b4", "runtime": "Host ai-edge-litert2.3.0 CPU, one thread; actual production Java policy/group; not Android execution", "candidate": "Only laughter >=0.30 with existing0.15 unrelated margin; two975ms windows; all other rules unchanged", "android_baseline_sha256": sha(args.android_baseline.read_bytes()) if args.android_baseline else None, "android_baseline_comparison": android, "checks": checks, "candidate_accepted": all(checks.values()), "scope": "Source-description compatibility, not calibrated recognition/bedroom/clinical accuracy. Every failure retained. Production unchanged.", "fixtures": reports}
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(checks, indent=2))
    print("CANDIDATE " + ("ACCEPTED FOR FURTHER NATIVE EVALUATION" if report["candidate_accepted"] else "REJECTED; PRODUCTION UNCHANGED"))
    return 0 if report["candidate_accepted"] else 2


if __name__ == "__main__":
    raise SystemExit(main())
