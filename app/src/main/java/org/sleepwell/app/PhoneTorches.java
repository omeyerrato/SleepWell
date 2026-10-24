package org.sleepwell.app;

import android.content.Context;
import android.hardware.camera2.*;
import android.os.*;

/** One lazy serial hardware adapter shared by alarm and experimental travel clients. */
final class PhoneTorches {
 interface Handle {void set(boolean enabled);void close();}
 interface Events {void ready(Handle handle);void lost(String reason);}
 interface Request {void close();}
 interface Provider {Request acquire(Context c,PhoneTorchCoordinator.Kind kind,java.util.function.BooleanSupplier alive,Events events);}
 static Provider provider=PhoneTorches::acquire;
 private static Android owner;
 private static synchronized Request acquire(Context c,PhoneTorchCoordinator.Kind kind,java.util.function.BooleanSupplier alive,Events events){for(int attempt=0;attempt<2;attempt++){if(owner==null||owner.requests.closed())owner=new Android(c.getApplicationContext());Request request=owner.request(kind,alive,events);if(request!=null)return request;}throw new IllegalStateException("The flashlight worker is unavailable.");}
 private static final class Android {
  final HandlerThread thread=new HandlerThread("SleepWell phone light");final Handler worker;PhoneTorchCoordinator broker;CameraManager manager;String camera;boolean registered,known;volatile boolean closed;final PhoneTorchRequests requests=new PhoneTorchRequests();
  Android(Context c){thread.start();worker=new Handler(thread.getLooper());worker.post(()->initialize(c));}
  void initialize(Context c){if(closed)return;try{manager=c.getSystemService(CameraManager.class);for(String id:manager.getCameraIdList()){CameraCharacteristics info=manager.getCameraCharacteristics(id);if(Boolean.TRUE.equals(info.get(CameraCharacteristics.FLASH_INFO_AVAILABLE))&&Integer.valueOf(CameraCharacteristics.LENS_FACING_BACK).equals(info.get(CameraCharacteristics.LENS_FACING))){camera=id;break;}}if(camera==null)throw new IllegalStateException("No rear flashlight is available.");broker=new PhoneTorchCoordinator(enabled->{try{manager.setTorchMode(camera,enabled);}catch(CameraAccessException|RuntimeException failure){throw new IllegalStateException(failure);}});manager.registerTorchCallback(callback,worker);registered=true;}catch(CameraAccessException|RuntimeException failure){known=false;}}
  final CameraManager.TorchCallback callback=new CameraManager.TorchCallback(){public void onTorchModeChanged(String id,boolean enabled){if(closed||!id.equals(camera)||broker==null)return;known=true;broker.observed(enabled);}public void onTorchModeUnavailable(String id){if(closed||!id.equals(camera)||broker==null)return;known=false;broker.unavailable();}};
  Request request(PhoneTorchCoordinator.Kind kind,java.util.function.BooleanSupplier alive,Events events){if(!requests.reserve())return null;Client client=new Client(kind,alive,events);if(!worker.post(()->client.await(SystemClock.elapsedRealtime()+3000))){requests.release();client.cancelled=true;client.finished=true;try{events.lost("The flashlight worker stopped before this request started.");}finally{if(requests.closeIfIdle()){closed=true;new Thread(this::shutdown,"SleepWell light cleanup").start();}}}return client;}
  final class Client implements Request,Handle {
   final PhoneTorchCoordinator.Kind kind;final java.util.function.BooleanSupplier alive;final Events events;volatile boolean cancelled;boolean finished;PhoneTorchCoordinator.Lease lease;
   Client(PhoneTorchCoordinator.Kind kind,java.util.function.BooleanSupplier alive,Events events){this.kind=kind;this.alive=alive;this.events=events;}
   void await(long deadline){if(finished)return;if(cancelled){finish("");return;}if(closed||broker==null){finish("This phone's flashlight is unavailable.");return;}if(!known){if(SystemClock.elapsedRealtime()>=deadline){finish("Flashlight status could not be read.");return;}worker.postDelayed(()->await(deadline),25);return;}lease=broker.acquire(kind,()->!cancelled&&alive.getAsBoolean(),new PhoneTorchCoordinator.Events(){public void ready(PhoneTorchCoordinator.Lease value){lease=value;if(cancelled){finish("");return;}events.ready(Client.this);}public void lost(String reason){finish(reason);}});if(lease==null&&!finished)finish("The flashlight could not be acquired.");}
   public void set(boolean enabled){worker.post(()->{if(!cancelled&&!finished&&lease!=null)lease.set(enabled);});}
   public void close(){cancelled=true;worker.post(()->finish(""));}
   void finish(String reason){if(finished)return;finished=true;if(lease!=null)lease.close();requests.release();try{if(!reason.isEmpty())events.lost(reason);}finally{closeIdle();}}
  }
  void closeIdle(){if(!requests.closeIfIdle())return;closed=true;shutdown();}
  void shutdown(){try{if(registered&&manager!=null)manager.unregisterTorchCallback(callback);}catch(RuntimeException ignored){}thread.quitSafely();}
 }
 private PhoneTorches(){}
}
