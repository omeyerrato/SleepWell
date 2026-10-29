package org.sleepwell.app;

/** One app-owned camera attempt. A pending close is never permission to preempt it. */
final class CameraCaptureLease {
 private static Object owner;
 private static Runnable pendingRetry;
 static synchronized boolean acquire(Object candidate){
  if(owner==null){owner=candidate;pendingRetry=null;return true;}
  if(pendingRetry!=null)pendingRetry.run();
  return false;
 }
 static java.util.concurrent.ExecutorService worker(Object candidate){
  try{return java.util.concurrent.Executors.newSingleThreadExecutor();}
  catch(RuntimeException|Error failure){release(candidate);throw failure;}
 }
 static synchronized void closing(Object candidate,Runnable retry){if(owner==candidate)pendingRetry=retry;}
 static synchronized void release(Object candidate){if(owner==candidate){owner=null;pendingRetry=null;}}
 static synchronized boolean idle(){return owner==null;}
 private CameraCaptureLease(){}
}
