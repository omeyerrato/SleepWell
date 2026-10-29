package org.sleepwell.app;

import android.app.Instrumentation;
import android.graphics.Rect;
import android.media.Image;
import android.os.SystemClock;
import androidx.camera.core.ImageInfo;
import androidx.camera.core.ImageProxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import static org.junit.Assert.*;

/** Production cleanup exercised inside the existing native routine method, without a camera. */
final class CameraCleanupProof {
 static void verify(Instrumentation instrumentation)throws Exception {
  assertTrue("No earlier camera lease",CameraCaptureLease.idle());
  Object owner=new Object();assertTrue(CameraCaptureLease.acquire(owner));
  ExecutorService worker=CameraCaptureLease.worker(owner);
  AtomicBoolean unbindAllowed=new AtomicBoolean(),modelAllowed=new AtomicBoolean();
  AtomicBoolean unbound=new AtomicBoolean(),modelClosed=new AtomicBoolean();
  AtomicInteger modelAttempts=new AtomicInteger();
  Frame failed=new Frame(),delegated=new Frame();delegated.allowed=true;
  CameraCaptureCleanup cleanup=new CameraCaptureCleanup(owner,worker,()->{
   if(!unbindAllowed.get())throw new IllegalStateException("Owned unbind is still pending");
   unbound.set(true);
  },()->{
   assertTrue("Owned use cases unbind before model",unbound.get());
   assertTrue("Failed ImageProxy closes before model",failed.closed);
   assertTrue("SDK-delegated ImageProxy closes before model",delegated.closed);
   modelAttempts.incrementAndGet();
   if(!modelAllowed.get())throw new IllegalStateException("Owned model close is still pending");
   modelClosed.set(true);
  });
  ImageProxy retained=cleanup.track(failed),sdkFrame=cleanup.track(delegated);
  try {
   try{retained.close();fail("The first frame close must fail");}catch(IllegalStateException expected){}
   instrumentation.runOnMainSync(cleanup::close);
   assertFalse(cleanup.released());assertFalse(worker.isShutdown());assertEquals(0,modelAttempts.get());
   instrumentation.runOnMainSync(()->assertFalse("A different owner cannot preempt closing capture",CameraCaptureLease.acquire(new Object())));
   assertFalse(CameraCaptureLease.idle());

   failed.allowed=true;unbindAllowed.set(true);instrumentation.runOnMainSync(cleanup::close);
   await(()->failed.closed,"The exact failed frame is retried");
   assertTrue(failed.closes.get()>=2);assertFalse(delegated.closed);
   assertEquals("SDK frame keeps the model alive",0,modelAttempts.get());
   assertFalse(cleanup.released());assertFalse(worker.isShutdown());assertFalse(CameraCaptureLease.idle());

   sdkFrame.close();instrumentation.runOnMainSync(cleanup::close);
   await(()->modelAttempts.get()>0,"The original model close is attempted");
   assertFalse(modelClosed.get());assertFalse(cleanup.released());assertFalse(worker.isShutdown());
   assertFalse(CameraCaptureLease.idle());

   modelAllowed.set(true);instrumentation.runOnMainSync(cleanup::close);
   await(cleanup::released,"Release waits for frame/model cleanup and worker termination");
   assertTrue(modelClosed.get());assertTrue(worker.isTerminated());assertTrue(CameraCaptureLease.idle());
   assertEquals("The delegated frame closes once",1,delegated.closes.get());
  } finally {
   failed.allowed=true;unbindAllowed.set(true);modelAllowed.set(true);
   retained.close();sdkFrame.close();instrumentation.runOnMainSync(cleanup::close);
   await(cleanup::released,"Fixture must confirm its own cleanup");
  }
 }
 private static void await(BooleanSupplier ready,String reason)throws InterruptedException {
  long end=SystemClock.elapsedRealtime()+8000;
  while(!ready.getAsBoolean()&&SystemClock.elapsedRealtime()<end)Thread.sleep(20);
  assertTrue(reason,ready.getAsBoolean());
 }
 private static final class Frame implements ImageProxy {
  final AtomicInteger closes=new AtomicInteger();volatile boolean allowed,closed;
  public void close(){closes.incrementAndGet();if(!allowed)throw new IllegalStateException("Frame close is pending");closed=true;}
  public Rect getCropRect(){return new Rect(0,0,1,1);}public void setCropRect(Rect crop){}
  public int getFormat(){return android.graphics.ImageFormat.YUV_420_888;}public int getHeight(){return 1;}public int getWidth(){return 1;}
  public PlaneProxy[] getPlanes(){return new PlaneProxy[0];}public ImageInfo getImageInfo(){return null;}
  @androidx.camera.core.ExperimentalGetImage public Image getImage(){return null;}
 }
 private CameraCleanupProof(){}
}
