package org.sleepwell.app;

import android.graphics.Rect;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import androidx.camera.core.ImageInfo;
import androidx.camera.core.ImageProxy;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/** Retains exactly one attempt through asynchronous drain, including delegated ML Kit frames.
 * No waiting on the UI thread and no unbinding or stopping another camera owner.
 */
final class CameraCaptureCleanup {
 interface Step {void close() throws Exception;}
 private static final Set<CameraCaptureCleanup> PENDING=Collections.newSetFromMap(new IdentityHashMap<>());
 private final Object owner;
 private final ExecutorService worker;
 private final Step mainClose,workerClose;
 private final Handler main=new Handler(Looper.getMainLooper());
 private final Set<Frame> frames=Collections.newSetFromMap(new IdentityHashMap<>());
 private boolean closing,mainClosed,queued,workerClosed;
 private volatile boolean released;
 private int attempts;
 private volatile String issue="";
 CameraCaptureCleanup(Object owner,ExecutorService worker,Step mainClose,Step workerClose){this.owner=owner;this.worker=worker;this.mainClose=mainClose;this.workerClose=workerClose;}
 ImageProxy track(ImageProxy image){Frame frame=new Frame(image);synchronized(frames){frames.add(frame);}return frame;}
 boolean released(){return released;}
 String issue(){return issue;}
 void close(){
  if(Looper.myLooper()!=Looper.getMainLooper())throw new IllegalStateException("Camera cleanup must begin on its UI owner");
  if(released)return;
  if(!closing){closing=true;synchronized(PENDING){PENDING.add(this);}CameraCaptureLease.closing(owner,this::retry);}
  attempts=0;retry();
 }
 private void retry(){
  if(Looper.myLooper()!=Looper.getMainLooper()){main.post(this::retry);return;}
  if(released)return;
  main.removeCallbacks(poll);attempts=0;poll.run();
 }
 private final Runnable poll=()->poll();
 private void poll(){
  if(released)return;
  if(!mainClosed){try{mainClose.close();mainClosed=true;}catch(Exception|LinkageError failure){issue="Camera release is pending. Retry cleanup.";}}
  if(mainClosed&&!queued&&!workerClosed){
   queued=true;
   try{worker.execute(()->{
    boolean complete=false;
    try{
     // An SDK-delegated image is not closed prematurely. Only failed closes are retried.
     Frame[] snapshot;synchronized(frames){snapshot=frames.toArray(new Frame[0]);}
     for(Frame frame:snapshot)if(frame.closeAttempted)frame.close();
     synchronized(frames){if(!frames.isEmpty())return;}
     workerClose.close();complete=true;
    }catch(Exception|LinkageError failure){issue="Camera analysis cleanup is pending. Retry cleanup.";}
    finally{boolean done=complete;main.post(()->{queued=false;if(done){workerClosed=true;worker.shutdown();}schedule();});}
   });}catch(RejectedExecutionException failure){queued=false;issue="Camera worker cleanup could not be confirmed.";}
  }
  if(mainClosed&&workerClosed&&worker.isTerminated()){
   released=true;issue="";main.removeCallbacks(poll);CameraCaptureLease.release(owner);synchronized(PENDING){PENDING.remove(this);}return;
  }
  schedule();
 }
 private void schedule(){
  if(released)return;main.removeCallbacks(poll);
  // Bounded automatic recovery keeps a dropped old caller reachable without an endless loop.
  if(attempts++<12)main.postDelayed(poll,Math.min(1000,50L<<Math.min(attempts,4)));
 }
 private final class Frame implements ImageProxy {
  private final ImageProxy image;
  private volatile boolean closeAttempted;
  private boolean closed;
  Frame(ImageProxy image){this.image=image;}
  @Override public synchronized void close(){
   if(closed)return;closeAttempted=true;image.close();closed=true;
   synchronized(frames){frames.remove(this);}
  }
  public Rect getCropRect(){return image.getCropRect();}
  public void setCropRect(Rect crop){image.setCropRect(crop);}
  public int getFormat(){return image.getFormat();}
  public int getHeight(){return image.getHeight();}
  public int getWidth(){return image.getWidth();}
  public PlaneProxy[] getPlanes(){return image.getPlanes();}
  public ImageInfo getImageInfo(){return image.getImageInfo();}
  @androidx.camera.core.ExperimentalGetImage public Image getImage(){return image.getImage();}
 }
}
