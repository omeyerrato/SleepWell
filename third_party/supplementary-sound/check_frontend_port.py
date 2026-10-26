from pathlib import Path
import numpy as np,json,onnxruntime as ort,urllib.request
root=Path.cwd();asset=root/'app/src/main/assets/recorded-sound';report=[]
opts=ort.SessionOptions();opts.intra_op_num_threads=1;opts.inter_op_num_threads=1
for name in ['laugh-frenkfurth','guitar-qubodup']:
 row={'source':name,'frontend':{},'score':{}}
 for suffix in ['upsampled','pann','efficient']:
  expected=np.fromfile(root/f'.android-local/{name}.{suffix}.f32',dtype='<f4');actual=np.fromfile(root/f'.android-local/{name}.java-{suffix}.f32',dtype='<f4')
  assert expected.shape==actual.shape
  row['frontend'][suffix]={'maximumAbsoluteDifference':float(np.max(abs(expected-actual))),'meanAbsoluteDifference':float(np.mean(abs(expected-actual)))}
  if suffix=='upsampled':assert np.max(abs(expected-actual))<1e-6;continue
  bands=64 if suffix=='pann' else 128;frames=len(actual)//bands;shape=(1,1,frames,64) if suffix=='pann' else (1,1,128,frames)
  session=ort.InferenceSession(str(asset/('panns-laughter.onnx' if suffix=='pann' else 'efficientat-instruments.onnx')),sess_options=opts,providers=['CPUExecutionProvider'])
  a=session.run(None,{'mel':actual.reshape(shape)})[0];b=session.run(None,{'mel':expected.reshape(shape)})[0]
  delta=float(np.max(abs(a-b)));assert delta<.001,(name,suffix,delta)
  row['score'][suffix]={'maximumAbsoluteDifference':delta}
 report.append(row)
(root/'.android-local/frontend-port-parity.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2))
try:
 response=urllib.request.urlopen('https://repo.maven.apache.org/maven2/com/microsoft/onnxruntime/onnxruntime-android/1.23.2/onnxruntime-android-1.23.2.pom',timeout=30)
 text=response.read(10000).decode();assert '<version>1.23.2</version>' in text;print('Official Maven artifact verified',response.status)
except Exception as e:print('Maven probe',str(e))
