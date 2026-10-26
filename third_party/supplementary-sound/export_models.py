# Run from repository root with the pinned CPU dependencies in README.md.
from pathlib import Path
import os,sys,json,hashlib,csv,io,ast
import numpy as np
from scipy.signal import firwin, resample_poly
import torch,torch.nn as nn,torch.nn.functional as F
root=Path.cwd()
import torch.nn.functional as F
from torchlibrosa.stft import Spectrogram,LogmelFilterBank
from torchlibrosa.augmentation import SpecAugmentation
torch.set_num_threads(1);torch.set_num_interop_threads(1)
pdir=root/'.android-local/panns'
source=(pdir/'models.py').read_bytes()
assert hashlib.sha256(source).hexdigest()=='7f9af440395ace5160bbb51d654a0dc35fb887fbf5edecb12da61ff6efb306d9'
names={'init_layer','init_bn','InvertedResidual','MobileNetV2'}
selected=[n for n in ast.parse(source).body if isinstance(n,(ast.ClassDef,ast.FunctionDef)) and n.name in names]
assert {n.name for n in selected}==names
namespace=dict(torch=torch,nn=nn,F=F,Spectrogram=Spectrogram,LogmelFilterBank=LogmelFilterBank,SpecAugmentation=SpecAugmentation)
exec(compile(ast.Module(body=selected,type_ignores=[]),str(pdir/'models.py'),'exec'),namespace)
pnet=namespace['MobileNetV2'](32000,1024,320,64,50,14000,527)
assert hashlib.sha256((pdir/'MobileNetV2.pth').read_bytes()).hexdigest()=='22437af9eb5ed1854b5ad285b4a17d5fc566f1a3b23df40db5541f5705ca340f'
pnet.load_state_dict(torch.load(pdir/'MobileNetV2.pth',map_location='cpu',weights_only=True)['model'],strict=True);pnet.eval()
labels=list(csv.DictReader(io.StringIO((pdir/'labels.csv').read_text())))
assert hashlib.sha256((pdir/'labels.csv').read_bytes()).hexdigest()=='cdd1049833c4b86127c2773ac0d14a2754b6a6d0d1798002ed5c66e699708429'
instrument_names=set((root/'third_party/yamnet/labels.txt').read_text().splitlines()[133:195]+(root/'third_party/yamnet/labels.txt').read_text().splitlines()[203:211])
instrument={int(row['index']) for row in labels if row['display_name'] in instrument_names}
assert instrument==set(range(138,200))|set(range(208,216))
music=next(int(row['index']) for row in labels if row['mid']=='/m/04rlf')
assets=root/'.android-local/efficientat'
for name,sha in {
 'models/mn/model.py':'471b2c482248d782b93881c5e834c0edc23c1e7e6f42a437e4c096b9d8ed4c3e',
 'models/mn/utils.py':'deb551bd854f90ca1473d731de1d7586af9449fc95fd2432f4cc9d0ccd38c11b',
 'models/mn/block_types.py':'e13ae49b6885217d6936d9203567aad1b6cb2f1229979d6d1ae77b836f3d1344',
 'models/mn/attention_pooling.py':'52b6903d5d62b3639ec609166cd08c36342588bd7988fe41ac72f3cfc496ce4f',
 'models/preprocess.py':'f4d5942eab7b916cb67fdda0f7ddfee152a31fa9a90f0f23ad21019d7a1081cf',
 'helpers/utils.py':'5d1e0e9198d098b92837fea0d002f116e2d0e2df9d9ee16775e519c45c171011',
 'metadata/class_labels_indices.csv':'cdd1049833c4b86127c2773ac0d14a2754b6a6d0d1798002ed5c66e699708429',
 'mn10_as_mAP_471.pt':'0bd7dc2443af498c289a2e739f02ebb515d6aa3fd3ab9db539c86123ae368a4e'
}.items():assert hashlib.sha256((assets/name).read_bytes()).hexdigest()==sha,name
os.chdir(assets);sys.path.insert(0,str(assets))
from models.mn.model import get_model
from models.preprocess import AugmentMelSTFT
enet=get_model(pretrained_name=None,width_mult=1.0)
enet.load_state_dict(torch.load(assets/'mn10_as_mAP_471.pt',map_location='cpu',weights_only=True),strict=True);enet.eval()
emel=AugmentMelSTFT(n_mels=128,sr=32000,win_length=800,hopsize=320).eval()
import torchaudio,onnxruntime as ort
out=root/'app/src/main/assets/recorded-sound';out.mkdir(parents=True,exist_ok=True)
class PannExport(nn.Module):
 def __init__(self):
  super().__init__();self.bn=pnet.bn0;self.features=pnet.features;self.fc=pnet.fc1;self.out=pnet.fc_audioset
 def forward(self,x):
  x=self.bn(x.transpose(1,3)).transpose(1,3);x=self.features(x).mean(dim=3)
  x=x.max(dim=2).values+x.mean(dim=2);return torch.sigmoid(self.out(F.relu(self.fc(x))))
class EfficientExport(nn.Module):
 def __init__(self):super().__init__();self.model=enet
 def forward(self,x):return torch.sigmoid(self.model(x)[0])
pw=PannExport().eval();ew=EfficientExport().eval()
torch.onnx.export(pw,torch.zeros(1,1,1001,64),str(out/'panns-laughter.onnx'),dynamo=False,opset_version=17,input_names=['mel'],output_names=['scores'],dynamic_axes={'mel':{2:'frames'}})
torch.onnx.export(ew,torch.zeros(1,1,128,1000),str(out/'efficientat-instruments.onnx'),dynamo=False,opset_version=17,input_names=['mel'],output_names=['scores'],dynamic_axes={'mel':{3:'frames'}})
# Export frontend constants, each a little-endian float32 array. No opaque Python pickle ships.
fir=(firwin(41,0.5,window=('kaiser',5.0))*2).astype('<f4')
pwindow=np.hanning(1025)[:-1].astype('<f4')
ewindow=np.pad(emel.window.numpy(),(112,112)).astype('<f4')
pmel=pnet.logmel_extractor.melW.detach().numpy().astype('<f4')
eb,_=torchaudio.compliance.kaldi.get_mel_banks(128,1024,32000,0.,15000.,vtln_low=100.,vtln_high=-500.,vtln_warp_factor=1.)
ematrix=F.pad(eb,(0,1)).numpy().T.copy().astype('<f4')
for name,a in [('resample',fir),('pann-window',pwindow),('efficient-window',ewindow),('pann-mel',pmel),('efficient-mel',ematrix)]:a.tofile(out/(name+'.f32'))
(out/'labels.txt').write_text('\n'.join(r['display_name'] for r in labels)+'\n',encoding='utf-8')
metadata={'instrumentIndices':sorted(instrument),'musicIndex':music,'files':{p.name:{'bytes':p.stat().st_size,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in sorted(out.iterdir())}}
(root/'.android-local/supplement-assets.json').write_text(json.dumps(metadata,indent=2))
