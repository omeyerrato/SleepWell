"""Offline TRAIN-only annotation feasibility. Never opens PCM, VAL/TEST or models.

Reads the pre-existing stage pilot's fixed TRAIN windows without changing their
selection. Labels a new, separate source respiratory-event-overlap target.
This module has no acquisition, feature extraction, fitting or prediction path.
"""
import argparse
from bisect import bisect_right
from collections import Counter
from decimal import Decimal
import hashlib
import json
from pathlib import Path


VERSION = "sleepwell-respiratory-overlap-proposal-v1"
KNOWN = frozenset(("hypo", "osa"))


def sha(raw):
    return hashlib.sha256(raw).hexdigest()


def seconds(value):
    if isinstance(value, bool) or not isinstance(value, (int, float, Decimal)):
        raise ValueError("Non-numeric event time")
    result = Decimal(str(value))
    if not result.is_finite():
        raise ValueError("Nonfinite event time")
    return result


def respiratory_intervals(annotation):
    if set(annotation) != {"record_start", "awake_intervals", "events"}:
        raise ValueError("Unreviewed annotation schema")
    origin = seconds(annotation["record_start"])
    intervals, types, durations = [], Counter(), []
    seen, duplicates = set(), 0
    for event in annotation["events"]:
        if set(event) != {"event_type", "evnet_start", "event_duration", "sleep_stage"}:
            raise ValueError("Unreviewed event schema")
        kind = event["event_type"]
        if kind not in KNOWN:
            raise ValueError("Unreviewed event type; never classify it as absent")
        start = seconds(event["evnet_start"]) - origin
        duration = seconds(event["event_duration"])
        if duration <= 0:
            raise ValueError("Nonpositive event duration")
        end = start + duration
        key = kind, start, end
        if key in seen:
            duplicates += 1
        seen.add(key)
        types[kind] += 1
        durations.append(duration)
        intervals.append((start, end))
    # Half-open union: overlapping types and duplicate rows never double-count.
    merged = []
    for start, end in sorted(intervals):
        if merged and start <= merged[-1][1]:
            merged[-1] = merged[-1][0], max(end, merged[-1][1])
        else:
            merged.append((start, end))
    return merged, [end for _, end in merged], types, durations, duplicates


def label(start, intervals, ends):
    start = seconds(start)
    end = start + 30
    overlap = Decimal(0)
    for index in range(bisect_right(ends, start), len(intervals)):
        left, right = intervals[index]
        if left >= end:
            break
        overlap += max(Decimal(0), min(end, right) - max(start, left))
    if not 0 <= overlap <= 30:
        raise ValueError("Invalid union overlap")
    name = "event_overlap" if overlap >= 10 else "no_annotated_overlap" if overlap == 0 else "partial_boundary_excluded"
    return name, str(overlap)


def inspect(cache, train_inputs):
    original = train_inputs.read_bytes()
    fixed = json.loads(original)
    rows = fixed["accepted"]
    if len(rows) != 217 or any(row["window"]["split"] != "train" or not 1 <= int(row["window"]["subject"]) <= 30 for row in rows):
        raise ValueError("Expected only the original217 fixed TRAIN windows")
    selected, natural, types = Counter(), Counter(), Counter()
    subjects, targets, durations = [], [], []
    short_durations = Counter()
    for number in range(1, 31):
        subject = f"{number:02}"
        folder = cache / subject
        raw = (folder / f"{subject}_annotation.json").read_bytes()
        annotation = json.loads(raw, parse_float=Decimal)
        contract_raw = (folder / "contract.json").read_bytes()
        contract = json.loads(contract_raw)
        if sha(raw) != contract["sidecars"]["annotation.json"]["sha256"]:
            raise ValueError("TRAIN annotation hash differs from original acquisition")
        intervals, ends, counts, spans, duplicates = respiratory_intervals(annotation)
        types.update(counts)
        durations.extend(spans)
        for event in annotation["events"]:
            if seconds(event["event_duration"]) < 10:
                short_durations[event["event_type"]] += 1
        local, all_eligible = Counter(), Counter()
        if contract["phone"] is not None:
            if seconds(contract["record_start"]) != seconds(annotation["record_start"]):
                raise ValueError("Author origin differs from fixed phone contract")
            for epoch in contract["candidates"]:
                category, _ = label(epoch["start_seconds"], intervals, ends)
                all_eligible[category] += 1
            for row in rows:
                window = row["window"]
                if window["subject"] != subject:
                    continue
                start = seconds(window["start_seconds"])
                if start < 30 or start + 60 > seconds(contract["phone"]["duration_seconds"]):
                    raise ValueError("Original TRAIN target lacks complete actual90s context")
                category, overlap = label(start, intervals, ends)
                local[category] += 1
                targets.append(dict(subject=subject, epoch=window["epoch"], target_start_seconds=str(start),
                    event_union_overlap_seconds=overlap, label=category, input_sha256=row["input_sha256"],
                    source_annotation_sha256=sha(raw)))
        elif any(row["window"]["subject"] == subject for row in rows):
            raise ValueError("Missing phone input cannot be a labeled TRAIN sample")
        selected.update(local)
        natural.update(all_eligible)
        subjects.append(dict(subject=subject,phone_available=contract["phone"] is not None,
            annotation_sha256=sha(raw),original_contract_sha256=sha(contract_raw),event_types=dict(counts),
            exact_duplicate_event_rows=duplicates,unique_union_intervals=len(intervals),
            union_intervals_not_fully_within_phone=None if contract["phone"] is None else sum(
                left < 0 or right > seconds(contract["phone"]["duration_seconds"]) for left,right in intervals),
            selected_counts=dict(local),natural_eligible_counts=dict(all_eligible)))
    if len(targets) != len(rows):
        raise ValueError("Every fixed TRAIN target must have one explicit annotation outcome")
    return dict(protocol_proposal=VERSION,status="Conditional seconds-unit TRAIN metadata only; parent review required before any fit or VAL/TEST event label access",
        original_train_inputs_sha256=sha(original),source_event_types=dict(types),
        source_event_duration_range_seconds=[str(min(durations)),str(max(durations))],
        duration_values_below10_by_type=dict(short_durations),
        duration_unit_evidence="Author helper says every timestamp uses s; event reader returns JSON unchanged. Field-specific event_duration units are not explicitly documented in cached primary material; counts assume seconds pending review.",
        selected_counts=dict(selected),natural_train_eligible_counts=dict(natural),subjects=subjects,targets=targets,
        participant_support={name:sum(subject["selected_counts"].get(name,0)>0 for subject in subjects)
            for name in ("event_overlap","no_annotated_overlap","partial_boundary_excluded")},
        actions=dict(annotations_read="01-30 only",pcm_read=False,features_extracted=False,model_fit=False,
            model_inference=False,validation_test_event_labels_read=False,network=False),
        limitations=["Author record_start alignment, not independent synchronization",
            "No-event label means absence from source event list, not proof of normal breathing",
            "Original TRAIN window selection was stage-balanced; never reselected by this new target",
            "No central/mixed apnea taxonomy is present in inspected TRAIN; no subtype is invented",
            "Prior study used these subjects for stage outcomes; new event targets are distinct, not an entirely unexposed corpus"])


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--cache", type=Path, required=True)
    parser.add_argument("--train-inputs", type=Path, default=Path("docs/research/2026-10-31-acoustic-pilot/train-inputs.json"))
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    if args.output.exists():
        raise ValueError("Do not overwrite a saved TRAIN feasibility record")
    result = inspect(args.cache, args.train_inputs)
    result["source_sha256"] = sha(Path(__file__).read_bytes())
    args.output.write_bytes((json.dumps(result, indent=2) + "\n").encode())
    print(json.dumps({key:result[key] for key in ("selected_counts","natural_train_eligible_counts","source_event_types","source_event_duration_range_seconds","actions")}))


if __name__ == "__main__":
    main()
