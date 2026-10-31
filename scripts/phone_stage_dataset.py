"""Read-only ScienceDB V5 inventory and prospective acoustic-pilot crop planning.

Never executes the author's helper, downloads a complete night, or reads test
subjects. Actual audio range acquisition and fitting are separate steps.
"""
import argparse
import csv
import hashlib
import io
import json
import struct
import threading
import urllib.request
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor

DATASET = "7b5f1df9c3d4435baa0ff2dbecf487d9"
VERSION = "V5"
PROTOCOL = "sleepwell-acoustic-pilot-v1"
API = "https://www.scidb.cn/api/gin-sdb-filetree/public/file/childrenFileListByPath"
DOWNLOAD = "https://china.scidb.cn/download?fileId="
STAGES = {"W": 0, "R": 1, "N1": 2, "N2": 2, "N3": 3}
NETWORK_LIMIT = 20_000_000
network_bytes = 0
network_lock = threading.Lock()


def reserve(size):
    global network_bytes
    with network_lock:
        if network_bytes + size > NETWORK_LIMIT:
            raise ValueError("Metadata/header acquisition budget exhausted; no audio acquired")
        network_bytes += size


def release(size):
    global network_bytes
    with network_lock:
        network_bytes -= size


def digest(data):
    return hashlib.sha256(data).hexdigest()


def save_json(path, value):
    path.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")


def tree(path):
    reserve(1_000_001)
    body = json.dumps(dict(dataSetId=DATASET, version=VERSION, path=path,
                           lastIndex=0, pageSize=100)).encode()
    request = urllib.request.Request(API, body, {"Content-Type": "application/json"})
    with urllib.request.urlopen(request, timeout=45) as response:
        raw = response.read(1_000_001)
    release(1_000_001 - len(raw))
    if len(raw) > 1_000_000:
        raise ValueError("Unexpectedly large file inventory")
    result = json.loads(raw)
    if result.get("code") != 20000:
        raise ValueError(f"Public file inventory failed: {result.get('messageEn')}")
    rows = result["data"]
    if rows and max(row.get("parentFileCount", 0) for row in rows) > len(rows):
        raise ValueError("Inventory pagination required; do not silently omit files")
    return raw, rows


def small_file(row):
    expected = row["size"]
    if expected > 2_000_000:
        raise ValueError("Only bounded metadata/annotation files are admitted here")
    reserve(expected + 1)
    with urllib.request.urlopen(DOWNLOAD + row["id"], timeout=45) as response:
        data = response.read(expected + 1)
    release(expected + 1 - len(data))
    if len(data) != expected or hashlib.md5(data).hexdigest() != row["md5"]:
        raise ValueError("Metadata size/digest differs from the public inventory")
    return data


def header(row):
    reserve(4097)
    request = urllib.request.Request(DOWNLOAD + row["id"], headers={"Range": "bytes=0-4095"})
    with urllib.request.urlopen(request, timeout=45) as response:
        expected = f"bytes 0-4095/{row['size']}"
        if response.status != 206 or response.headers.get("Content-Range") != expected:
            raise ValueError("Server did not honor the bounded header range; body not read")
        data = response.read(4097)
    release(4097 - len(data))
    if len(data) != 4096:
        raise ValueError("Incomplete WAV header range")
    return data


def wav_contract(data, file_size):
    if data[:4] != b"RIFF" or data[8:12] != b"WAVE":
        raise ValueError("Expected an actual RIFF/WAVE input")
    at, fmt = 12, None
    while at + 8 <= len(data):
        kind, size = data[at:at+4], struct.unpack_from("<I", data, at+4)[0]
        start = at + 8
        if kind == b"fmt ":
            fmt = struct.unpack_from("<HHIIHH", data, start)
        if kind == b"data":
            if fmt != (1, 1, 8000, 16000, 2, 16):
                raise ValueError(f"Protocol requires released PCM16 mono8k, received {fmt}")
            if start + size > file_size or size % 2:
                raise ValueError("Invalid published PCM extent")
            return dict(format="pcm_s16le", channels=1, sample_rate=8000,
                        byte_rate=16000, data_offset=start, data_bytes=size,
                        samples=size // 2, duration_seconds=size / 16000)
        at = start + size + (size % 2)
    raise ValueError("Required WAV chunks do not fit within the bounded header")


def epochs(csv_bytes, annotation, contract):
    # This reproduces the explicitly inspected AUTHOR timebase, without running
    # their helper or claiming the WAV embeds an independently verified clock.
    rows = list(csv.reader(io.StringIO(csv_bytes.decode("utf-8-sig"))))
    result, excluded, blank = [], 0, 0
    previous = None
    for index, row in enumerate(rows[1:], 1):
        if len(row) != 3 or int(row[0]) != index:
            raise ValueError("Unexpected stage columns/epoch sequence")
        h, m, s = row[1].split(":")
        hour = int(h)
        if hour < 12:
            hour += 24
        start = hour * 3600 + int(m) * 60 + float(s) - annotation["record_start"]
        if previous is not None and abs(start - previous - 30) > 1e-6:
            raise ValueError("Stage timestamp gap or non-30-second epoch")
        previous = start
        stage = row[2].strip()
        if stage not in STAGES:
            blank += 1
            continue
        if start < 30 or start + 60 > contract["duration_seconds"]:
            excluded += 1
            continue
        result.append(dict(epoch=index, start_seconds=start, source_stage=stage,
                           label=STAGES[stage]))
    return result, dict(csv_epochs=len(rows)-1, unknown_stage=blank,
                        outside_90s_pcm_context=excluded, eligible_90s_contexts=len(result))


def inventory_one(cache, subject):
    folder = cache / subject
    folder.mkdir(parents=True, exist_ok=True)
    raw, rows = tree(f"/V5/Data/{subject}")
    (folder / "inventory.json").write_bytes(raw)
    named = {row["fileName"]: row for row in rows}
    chosen = {}
    for suffix in ("sleep_stage.csv", "annotation.json"):
        name = f"{subject}_{suffix}"
        row = named[name]
        data = small_file(row)
        (folder / name).write_bytes(data)
        chosen[suffix] = dict(file_id=row["id"], bytes=len(data), md5=row["md5"], sha256=digest(data))
    phone = named[f"{subject}_phone.wav"]
    wave_header = header(phone)
    (folder / "phone-header.bin").write_bytes(wave_header)
    contract = wav_contract(wave_header, phone["size"])
    annotation = json.loads((folder / f"{subject}_annotation.json").read_bytes())
    candidates, coverage = epochs((folder / f"{subject}_sleep_stage.csv").read_bytes(), annotation, contract)
    result = dict(subject=subject, dataset=DATASET, version=VERSION,
                  inventory_sha256=digest(raw), sidecars=chosen,
                  phone=dict(file_id=phone["id"], published_bytes=phone["size"],
                             published_md5=phone["md5"], whole_file_digest_verified=False,
                             header_sha256=digest(wave_header), **contract),
                  record_start=annotation["record_start"],
                  timebase="Author-supplied annotation/helper; not independently measured from WAV",
                  coverage=coverage, candidates=candidates)
    save_json(folder / "contract.json", result)
    return dict(subject=subject, **coverage)


def plan_one(contract, split):
    subject = contract["subject"]
    candidates = contract["candidates"]
    ranked = sorted(candidates, key=lambda row: hashlib.sha256(
        f"{PROTOCOL}/{split}/{subject}/{row['epoch']}".encode()).digest())
    accepted = []
    if split == "train":
        for label in range(4):
            count = 0
            for row in ranked:
                if row["label"] != label:
                    continue
                if any(abs(row["start_seconds"] - old["start_seconds"]) < 90 for old in accepted):
                    continue
                accepted.append(row)
                count += 1
                if count == 2:
                    break
    else:
        for row in ranked:
            if all(abs(row["start_seconds"] - old["start_seconds"]) >= 90 for old in accepted):
                accepted.append(row)
                if len(accepted) == 10:
                    break
    result = []
    for row in sorted(accepted, key=lambda row: row["epoch"]):
        byte_start = contract["phone"]["data_offset"] + round((row["start_seconds"] - 30) * 16000)
        result.append(dict(subject=subject, split=split, file_id=contract["phone"]["file_id"],
                           published_file_bytes=contract["phone"]["published_bytes"],
                           byte_start=byte_start, byte_end=byte_start + 90 * 16000 - 1, **row))
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", choices=("inventory", "plan"))
    parser.add_argument("--cache", type=Path, required=True)
    parser.add_argument("--subjects", default="01-03")
    args = parser.parse_args()
    lo, hi = map(int, args.subjects.split("-"))
    if not 1 <= lo <= hi <= 40:
        raise ValueError("Test41-50 remain untouched until model/protocol freeze")
    subjects = [f"{n:02}" for n in range(lo, hi+1)]
    args.cache.mkdir(parents=True, exist_ok=True)
    if args.command == "inventory":
        with ThreadPoolExecutor(max_workers=2) as workers:
            for summary in workers.map(lambda subject: inventory_one(args.cache, subject), subjects):
                print(json.dumps(summary), flush=True)
        print(json.dumps(dict(downloaded_body_bytes=network_bytes)), flush=True)
    else:
        rows = []
        for subject in subjects:
            split = "train" if int(subject) <= 30 else "validation"
            contract = json.loads((args.cache / subject / "contract.json").read_bytes())
            rows.extend(plan_one(contract, split))
        save_json(args.cache / "crop-plan.json", dict(protocol=PROTOCOL, windows=rows,
                  planned_pcm_bytes=sum(row["byte_end"]-row["byte_start"]+1 for row in rows)))
        print(json.dumps(dict(windows=len(rows), planned_pcm_bytes=len(rows)*90*16000)), flush=True)


if __name__ == "__main__":
    main()
