"""Pinned host-only study. No playback/capture, imports, network upload or weight downloads.

Run with the existing scientific Python runtime. Supply --assets, --java and --javac.
Phases: download; dev; calibrate; eval. Eval requires a frozen development-only policy.
"""
import argparse
import hashlib
import io
import json
import os
from pathlib import Path
import struct
import subprocess
import time
import urllib.request
import zlib

HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[2]


def sha(data):
    return hashlib.sha256(data).hexdigest()


def read(path):
    return json.loads(path.read_text(encoding="utf-8"))


def save(path,value):
    path.write_text(json.dumps(value,indent=2)+"\n",encoding="utf-8")


def fetch(url,start,size,total):
    request=urllib.request.Request(url+"?study-range="+str(start),headers={"Range":f"bytes={start}-{start+size-1}"})
    with urllib.request.urlopen(request,timeout=30) as response:
        if response.status!=206 or response.headers.get("Content-Range")!=f"bytes {start}-{start+size-1}/{total}":
            raise ValueError("Server did not honor the bounded byte range")
        data=response.read(size+1)
    if len(data)!=size:
        raise ValueError("Incomplete range")
    return data


def download(args,manifest):
    record=read(args.cache/"zenodo-record.json")
    files={row["key"]:row for row in record["files"]}
    receipt=[];network=sum((args.cache/name).stat().st_size for name in ["FSD50K.metadata.zip","FSD50K.ground_truth.zip","FSD50K.doc.zip","FSD50K.dev_audio.zip.central","FSD50K.eval_audio.zip.central","FSD50K.dev_audio.zip.tail","FSD50K.eval_audio.zip.tail","zenodo-record.json"])
    def part(split,disk):
        last=5 if split=="dev" else 1
        return files[f"FSD50K.{split}_audio."+("zip" if disk==last else f"z{disk+1:02}")]
    for row in manifest["clips"]:
        directory=args.cache/row["split"]/row["id"];directory.mkdir(parents=True,exist_ok=True)
        entry=row["entry"];target=directory/"original.wav";start=time.perf_counter()
        if not target.exists():
            source=part(row["split"],entry["disk"]);count=min(entry["compressed"]+4096,source["size"]-entry["offset"])
            if count<entry["compressed"]+512:
                raise ValueError("Selected entry crosses a ZIP part; preserve it as unavailable")
            network+=count
            if network>manifest["max_download_bytes"]:
                raise ValueError("Initial network budget exceeded")
            archive=fetch(source["links"]["self"],entry["offset"],count,source["size"])
            header=struct.unpack_from("<4s5H3L2H",archive)
            if header[0]!=b"PK\x03\x04" or header[3]!=8:
                raise ValueError("Unexpected source ZIP entry")
            offset=30+header[-2]+header[-1]
            filename=archive[30:30+header[-2]].decode("utf-8")
            if filename!=f'FSD50K.{row["split"]}_audio/{row["id"]}.wav':
                raise ValueError("Wrong original source entry")
            data=zlib.decompress(archive[offset:offset+entry["compressed"]],-15)
            if len(data)!=entry["bytes"] or zlib.crc32(data)!=entry["crc32"]:
                raise ValueError("Source ZIP size/CRC mismatch")
            target.write_bytes(data)
        data=target.read_bytes()
        if len(data)!=entry["bytes"] or zlib.crc32(data)!=entry["crc32"]:
            raise ValueError("Cached source changed")
        receipt.append({"id":row["id"],"split":row["split"],"bytes":len(data),"wav_sha256":sha(data),"fetch_seconds":time.perf_counter()-start})
        print("downloaded",row["split"],row["id"],len(data),flush=True)
    save(args.cache/"download-receipt.json",{"selection_sha256":sha((HERE/"selection.json").read_bytes()),"network_bytes":network,"sources":receipt})


def features(scores,instruments):
    family=lambda n:138<=n<=199 or 208<=n<=215 if instruments else 16<=n<=21
    return {"score":max(float(v) for n,v in enumerate(scores) if family(n)),"competitor":max(float(v) for n,v in enumerate(scores) if not family(n) and (not instruments or n!=137))}


def infer(args,manifest,split):
    import numpy as np
    import soundfile as sf
    import onnxruntime as ort
    if ort.__version__!="1.23.2":
        raise ValueError("Use pinned ONNX Runtime 1.23.2")
    hashes={"panns-laughter.onnx":"f9dd56478e434e067cef505489d0ae4483d80d357e2c35bc3d94ba432eb08f3f","efficientat-instruments.onnx":"7bb4227052d796a1ef35f0a5b703fdd724cd200fc7e6f85087289222e6c5b5c8"}
    for name,digest in hashes.items():
        if sha((args.assets/name).read_bytes())!=digest:raise ValueError("Model changed")
    classes=args.cache/"classes";classes.mkdir(exist_ok=True)
    java_sources=[ROOT/"app/src/main/java/org/sleepwell/app"/name for name in ["RecordedSoundFrontend.java","RecordedPcmResampler.java"]]+[HERE/"StudyFrontend.java"]
    subprocess.run([str(args.javac),"-d",str(classes)]+list(map(str,java_sources)),check=True)
    options=ort.SessionOptions();options.intra_op_num_threads=1;options.inter_op_num_threads=1;options.execution_mode=ort.ExecutionMode.ORT_SEQUENTIAL
    engines={name:ort.InferenceSession(str(args.assets/file),sess_options=options,providers=["CPUExecutionProvider"]) for name,file in [("pann","panns-laughter.onnx"),("efficient","efficientat-instruments.onnx")]}
    report={"split":split,"source_commit":subprocess.check_output(["git","rev-parse","HEAD"],cwd=ROOT,text=True).strip(),"selection_sha256":sha((HERE/"selection.json").read_bytes()),"frontend_sha256":{p.name:sha(p.read_bytes()) for p in java_sources[:2]},"models":hashes,"threads_per_session":1,"clips":[]}
    if split=="eval":
        policy=read(args.cache/"frozen-policy.json");report["frozen_policy_sha256"]=sha((args.cache/"frozen-policy.json").read_bytes())
        if policy["dev_results_sha256"]!=sha((args.cache/"dev-results.json").read_bytes()):raise ValueError("Development changed after freezing")
        if (args.cache/"eval-results.json").exists():raise ValueError("Evaluation already run; do not rerun/tune")
    for row in manifest["clips"]:
        if row["split"]!=split:continue
        directory=args.cache/split/row["id"];wav=directory/"original.wav";pcm,rate=sf.read(wav,dtype="int16")
        if rate!=44100 or pcm.ndim!=1:raise ValueError("Unexpected FSD50K original PCM")
        pcm.astype("<i2").tofile(directory/"original.pcm");start=time.perf_counter()
        subprocess.run([str(args.java),"-XX:ActiveProcessorCount=2","-Xmx512m","-cp",str(classes),"org.sleepwell.app.StudyFrontend",str(args.assets),str(directory/"original.pcm"),str(directory)],check=True)
        origin,count=map(int,(directory/"origin.txt").read_text().split());windows=[]
        for start_sample in range(0,count,160000):
            samples=min(160000,count-start_sample)
            if samples<32000:break
            scores={}
            for name,bands in [("pann",64),("efficient",128)]:
                mel=np.fromfile(directory/f"{start_sample}.{name}.f32",dtype="<f4");shape=(1,1,len(mel)//bands,bands) if name=="pann" else (1,1,bands,len(mel)//bands)
                output=engines[name].run(None,{"mel":mel.reshape(shape)})[0][0]
                if output.shape!=(527,) or not np.all(np.isfinite(output)):raise ValueError("Invalid model scores")
                scores[name]=output.tolist()
            windows.append({"start_sample":start_sample,"samples":samples,"laughter":features(scores["pann"],False),"instrument":features(scores["efficient"],True),"scores":scores})
        result={"id":row["id"],"role":row["role"],"original_frames":len(pcm),"wav_sha256":sha(wav.read_bytes()),"pcm16_sha256":sha((directory/"input16.pcm").read_bytes()),"first_output_frame":origin,"converted_samples":count,"omitted_tail_samples":count-sum(w["samples"] for w in windows),"cpu_seconds":time.perf_counter()-start,"windows":windows};report["clips"].append(result)
        save(args.cache/(split+"-partial.json"),report);print("inferred",split,row["id"],row["role"],len(windows),flush=True)
    save(args.cache/(split+"-results.json"),report)


def accepted(clip,family,threshold,margin,model="original"):
    for window in clip["windows"]:
        estimate=window[family]
        if model=="pann-fallback":
            # Preserve every originally qualified EfficientAT instrument decision.
            if estimate["score"]>=.65 and estimate["score"]-estimate["competitor"]>=.15:
                return True
            estimate=features(window["scores"]["pann"],True)
        if estimate["score"]>=threshold and estimate["score"]-estimate["competitor"]>=margin:
            return True
    return False


def calibrate(args):
    dev=read(args.cache/"dev-results.json");result={"dev_results_sha256":sha((args.cache/"dev-results.json").read_bytes()),"policy":{},"candidates":{}}
    for family,thresholds,margins,baseline in [("laughter",[.10,.15,.20,.30],[.025,.05,.10],[.10,.025]),("instrument",[.35,.45,.55,.65],[.15,.25],[.65,.15])]:
        candidates=[]
        for model in (["original","pann-fallback"] if family=="instrument" else ["original"]):
            for threshold in thresholds:
                for margin in margins:
                    true=[r["id"] for r in dev["clips"] if r["role"]==family and accepted(r,family,threshold,margin,model)]
                    false=[r["id"] for r in dev["clips"] if r["role"]!=family and accepted(r,family,threshold,margin,model)]
                    candidates.append({"model":model,"threshold":threshold,"margin":margin,"true":true,"false":false})
        eligible=[r for r in candidates if not r["false"]]
        unchanged=next(r for r in candidates if r["model"]=="original" and r["threshold"]==baseline[0] and r["margin"]==baseline[1])
        winner=max(eligible,key=lambda r:(len(r["true"]),r["threshold"],r["margin"])) if eligible else unchanged
        if len(winner["true"])<=len(unchanged["true"]):
            winner=unchanged
        result["policy"][family]=winner;result["candidates"][family]=candidates
    save(args.cache/"frozen-policy.json",result);print(json.dumps(result["policy"],indent=2),flush=True)


if __name__=="__main__":
    parser=argparse.ArgumentParser();parser.add_argument("phase",choices=["download","dev","calibrate","eval"]);parser.add_argument("--cache",type=Path,required=True);parser.add_argument("--assets",type=Path);parser.add_argument("--java",type=Path);parser.add_argument("--javac",type=Path);args=parser.parse_args();args.cache=args.cache.resolve();manifest=read(HERE/"selection.json")
    if args.phase=="download":download(args,manifest)
    elif args.phase=="calibrate":calibrate(args)
    else:infer(args,manifest,args.phase)
