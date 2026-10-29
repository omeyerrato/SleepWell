package org.sleepwell.app;
import android.content.Context;import android.graphics.Bitmap;import com.google.ai.edge.litert.*;import java.io.*;import java.security.*;import java.util.*;
/** Pinned UINT8 RGB contract from the bundled model's metadata; one CPU worker owns every handle. */
final class ObjectClassifier implements AutoCloseable {
 private final Thread owner=Thread.currentThread();private Environment environment;private CompiledModel model;private List<TensorBuffer> inputs=new ArrayList<>(),outputs=new ArrayList<>();private static boolean checked;
 /** Carries partially initialized handles back to the same camera worker for retry. */
 static final class InitFailure extends IllegalStateException {
  final ObjectClassifier pending;
  InitFailure(Throwable cause,ObjectClassifier pending){super("The bundled object classifier is unavailable. Use Math.",cause);this.pending=pending;}
 }
 ObjectClassifier(Context context){if(android.os.Looper.myLooper()==android.os.Looper.getMainLooper())throw new IllegalStateException("Object model must load on its camera worker");try{verify(context);environment=Environment.create();model=CompiledModel.create(context.getAssets(),"object_efficientnet_lite0.tflite",new CompiledModel.Options(Accelerator.CPU),environment);inputs=new ArrayList<>(model.createInputBuffers());outputs=new ArrayList<>(model.createOutputBuffers());if(inputs.size()!=1||outputs.size()!=1)throw new IllegalStateException("Unsupported object model tensors");byte[] warm=new byte[224*224*3];Arrays.fill(warm,(byte)131);run(warm);}catch(Exception|LinkageError failure){try{close();}catch(RuntimeException|LinkageError cleanup){failure.addSuppressed(cleanup);throw new InitFailure(failure,this);}throw new IllegalStateException("The bundled object classifier is unavailable. Use Math.",failure);}}
 private static synchronized void verify(Context context)throws Exception{if(checked)return;for(String[] entry:new String[][]{{"object_efficientnet_lite0.tflite","aa03e4017f54fd5c743bd397b0a3e136fab0942f9c7cd715064a31ca3e4fc996"},{"object_labels.txt","e697a491aa735cc6c2aaf982f8e86e8fc7b0a1ea7750a2cc6a2bdfc1e109012f"}}){MessageDigest digest=MessageDigest.getInstance("SHA-256");try(InputStream input=context.getAssets().open(entry[0])){byte[] bytes=new byte[8192];for(int n;(n=input.read(bytes))!=-1;)digest.update(bytes,0,n);}if(!NightAudioArchive.hash(digest.digest()).equals(entry[1]))throw new IOException("Pinned object model or label map changed");}List<String> labels=new ArrayList<>();try(BufferedReader in=new BufferedReader(new InputStreamReader(context.getAssets().open("object_labels.txt"),java.nio.charset.StandardCharsets.UTF_8))){for(String line;(line=in.readLine())!=null;)labels.add(line);}if(labels.size()!=1000)throw new IOException("Expected1000 model labels");for(int n=0;n<ObjectTarget.INDICES.size();n++)if(!labels.get(ObjectTarget.INDICES.get(n)).equals(ObjectTarget.LABELS.get(n)))throw new IOException("Object label index changed");checked=true;}
 float[] infer(Bitmap bitmap){owned();if(bitmap.getWidth()!=224||bitmap.getHeight()!=224)throw new IllegalArgumentException("Expected224-square RGB image");int[] pixels=new int[224*224];byte[] values=new byte[pixels.length*3];try{bitmap.getPixels(pixels,0,224,0,0,224,224);for(int n=0;n<pixels.length;n++){int color=pixels[n];for(int c=0;c<3;c++){int rgb=(color>>(16-c*8))&255;int q=(int)Math.round(((rgb-127)/128d)/0.012566016986966133)+131;values[n*3+c]=(byte)Math.max(0,Math.min(255,q));}}return run(values);}finally{Arrays.fill(pixels,0);Arrays.fill(values,(byte)0);}}
 private float[] run(byte[] values){owned();try{inputs.get(0).writeInt8(values);model.run(inputs,outputs);byte[] raw=outputs.get(0).readInt8();if(raw.length!=1000)throw new IllegalStateException("Expected1000 object scores");float[] scores=new float[1000];for(int n=0;n<1000;n++)scores[n]=(raw[n]&255)/256f;return scores;}catch(LiteRtException failure){throw new IllegalStateException("Object inference failed",failure);}}
 private void owned(){if(Thread.currentThread()!=owner)throw new IllegalStateException("Object model left its camera worker");}
 public void close(){
  owned();RuntimeException failure=null;
  failure=closeBuffers(inputs,failure);failure=closeBuffers(outputs,failure);
  // A dependent native allocation must survive while any tensor still needs retry.
  if(inputs.isEmpty()&&outputs.isEmpty()&&model!=null){try{model.close();model=null;}catch(Exception|LinkageError error){failure=failed(failure,error);}}
  if(model==null&&inputs.isEmpty()&&outputs.isEmpty()&&environment!=null){try{environment.close();environment=null;}catch(Exception|LinkageError error){failure=failed(failure,error);}}
  if(failure!=null)throw failure;
 }
 private static RuntimeException closeBuffers(List<TensorBuffer> buffers,RuntimeException failure){
  for(Iterator<TensorBuffer> iterator=buffers.iterator();iterator.hasNext();){try{iterator.next().close();iterator.remove();}catch(Exception|LinkageError error){failure=failed(failure,error);}}return failure;
 }
 private static RuntimeException failed(RuntimeException prior,Throwable error){if(prior==null)return new IllegalStateException("Object model cleanup is pending",error);prior.addSuppressed(error);return prior;}
}
