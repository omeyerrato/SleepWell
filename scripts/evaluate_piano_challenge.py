"""Fixed preregistered Piano policy / real whole CC0 PCM, host CPU only. Never plays or captures."""
import argparse,hashlib,importlib.metadata,io,json,struct,subprocess,sys,tempfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
MODEL_SHA="10c95ea3eb9a7bb4cb8bddf6feb023250381008177ac162ce169694d05c317de"
PROBE=r'''
package org.sleepwell.app;
import java.io.*;
public final class PianoProbe {
 public static void main(String[] args)throws Exception{
  DataInputStream in=new DataInputStream(System.in);int fixtures=in.readInt();
  for(int f=0;f<fixtures;f++){int frames=in.readInt();PianoRule rule=new PianoRule();for(int w=0;w<frames;w++){float[] scores=new float[521];for(int n=0;n<521;n++)scores[n]=in.readFloat();System.out.println(f+"\t"+w+"\t"+PianoRule.window(scores)+"\t"+rule.accept(scores));}}
  if(in.read()!=-1)throw new IOException("Unexpected data");
 }
}
'''
def sha(data):return hashlib.sha256(data).hexdigest()
def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument("--runtime-dir",type=Path,required=True);p.add_argument("--jdk",type=Path,required=True);p.add_argument("--output",type=Path,required=True);a=p.parse_args()
 sys.path.insert(0,str(a.runtime_dir));import numpy as np
 from ai_edge_litert.interpreter import Interpreter
 if importlib.metadata.version("ai-edge-litert")!="2.3.0":raise ValueError("Runtime version")
 declaration=json.loads((ROOT/"third_party/audio-fixtures/piano-preregistered.json").read_text())
 rows=declaration['fixtures'];old={}
 for manifest in ['manifest.json','laughter-holdout.json']:
  for row in json.loads((ROOT/'third_party/audio-fixtures'/manifest).read_text())['fixtures']:old[row['name']]=row
 for name in declaration['frozen_negatives']+['music-archtects']:rows.append(old[name])
 model=ROOT/'app/src/main/assets/yamnet.tflite'
 if sha(model.read_bytes())!=MODEL_SHA:raise ValueError('Model changed')
 engine=Interpreter(model_path=str(model),num_threads=1);engine.allocate_tensors();i=engine.get_input_details()[0];o=engine.get_output_details()[0]
 if list(i['shape'])!=[15600] or list(o['shape'])!=[1,521]:raise ValueError('Tensor shape')
 packet=io.BytesIO();packet.write(struct.pack('>I',len(rows)));results=[]
 for row in rows:
  data=(ROOT/'app/src/androidTest/assets/classifier-fixtures'/(row['name']+'.pcm')).read_bytes()
  if sha(data)!=row['pcm_sha256'] or len(data)>3*1024*1024 or len(data)>80*32000 or row['license']!='CC0-1.0':raise ValueError('Fixture changed')
  pcm=np.frombuffer(data,dtype='<i2').astype(np.float32)/32768.0;frames=len(pcm)//15600;packet.write(struct.pack('>I',frames));windows=[]
  for n in range(frames):
   engine.set_tensor(i['index'],pcm[n*15600:(n+1)*15600]);engine.invoke();scores=engine.get_tensor(o['index']).reshape(-1)
   packet.write(struct.pack('>521f',*scores));windows.append({'start_ms':n*975,'scores':[float(x) for x in scores],'top8':[{'index':int(k),'score':float(scores[k])} for k in np.argsort(scores)[-8:][::-1]]})
  results.append({'fixture':row['name'],'pcm_sha256':row['pcm_sha256'],'expected_piano':row.get('expected_piano',False) if row['name']!='music-archtects' else None,'windows':windows})
 with tempfile.TemporaryDirectory(prefix='sleepwell-piano-') as tmp:
  folder=Path(tmp);source=folder/'PianoProbe.java';source.write_text(PROBE,encoding='utf-8')
  subprocess.run([str(a.jdk/'bin/javac.exe'),'-d',tmp,str(ROOT/'scripts/evaluation/PianoRule.java'),str(source)],check=True)
  output=subprocess.run([str(a.jdk/'bin/java.exe'),'-cp',tmp,'org.sleepwell.app.PianoProbe'],input=packet.getvalue(),stdout=subprocess.PIPE,check=True).stdout.decode()
 for line in output.splitlines():
  f,w,qualified,accepted=line.split('\t');row=results[int(f)];row['windows'][int(w)].update(qualified=qualified=='true',accepted=accepted=='true')
 for row in results:row['detected']=any(w['accepted'] for w in row['windows'])
 checks={r['fixture']:r['detected']==r['expected_piano'] for r in results if r['expected_piano'] is not None}
 report={'model_sha256':MODEL_SHA,'declaration_commit':'0ed1093','pcm_pinned_before_inference_commit':'064eb5d','policy_sha256':sha((ROOT/'scripts/evaluation/PianoRule.java').read_bytes()),'runtime':'Host ai-edge-litert2.3.0 CPU1thread and experimental Java PianoRule (not in app); not Android/microphone execution','policy':declaration['policy'],'checks':checks,'candidate_accepted':all(checks.values()),'scope':'Two independent uploader-described whole piano sources and frozen distractors; no accuracy/authentication/performance claim. Mixed music context is observed without invented instrument truth. All failures preserved.','fixtures':results}
 a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
 print(json.dumps({'checks':checks,'accepted':all(checks.values()),'observations':[{k:v for k,v in r.items() if k!='windows'} for r in results]},indent=2))
 return 0 if all(checks.values()) else 2
if __name__=='__main__':sys.exit(main())
