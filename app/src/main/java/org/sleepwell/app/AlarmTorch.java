package org.sleepwell.app;

import android.content.Context;
import android.hardware.camera2.*;
import android.os.*;
import java.util.function.BooleanSupplier;

/** Public torch API only. No CameraDevice, capture session or camera-content permission. */
final class AlarmTorch {
    private final Context context;private final BooleanSupplier alive;private final HandlerThread thread=new HandlerThread("SleepWell alarm light");private final Handler worker;
    private CameraManager manager;private String camera;private TorchLease lease;private volatile boolean closed;private boolean registered;
    AlarmTorch(Context context,BooleanSupplier alive){this.context=context.getApplicationContext();this.alive=alive;thread.start();worker=new Handler(thread.getLooper());worker.post(this::start);}
    private void start(){
        if(closed||!alive.getAsBoolean())return;
        try{manager=context.getSystemService(CameraManager.class);for(String id:manager.getCameraIdList()){CameraCharacteristics info=manager.getCameraCharacteristics(id);if(Boolean.TRUE.equals(info.get(CameraCharacteristics.FLASH_INFO_AVAILABLE))&&Integer.valueOf(CameraCharacteristics.LENS_FACING_BACK).equals(info.get(CameraCharacteristics.LENS_FACING))){camera=id;break;}}
            if(camera==null){error("This phone has no available rear flashlight.");return;}
            lease=new TorchLease(enabled->{if(enabled&&(closed||!alive.getAsBoolean()))throw new IllegalStateException("Alarm light cancelled");try{manager.setTorchMode(camera,enabled);}catch(CameraAccessException|SecurityException|IllegalArgumentException error){throw new IllegalStateException(error);}});
            manager.registerTorchCallback(callback,worker);registered=true;
        }catch(CameraAccessException|RuntimeException error){error("The alarm flashlight is unavailable. Sound and the challenge continue.");}
    }
    private final CameraManager.TorchCallback callback=new CameraManager.TorchCallback(){
        @Override public void onTorchModeChanged(String id,boolean enabled){if(closed||!id.equals(camera)||lease==null)return;try{lease.observed(enabled);}catch(RuntimeException error){error("Android could not enable the alarm flashlight.");}}
        @Override public void onTorchModeUnavailable(String id){if(closed||!id.equals(camera)||lease==null)return;lease.unavailable();error("The flashlight is busy or unavailable. Sound and the challenge continue.");}
    };
    private void error(String value){Prefs.put(context,"lastError",value);}
    void close(){closed=true;worker.post(()->{try{if(lease!=null)lease.close();}catch(RuntimeException error){error("Android could not release the alarm flashlight.");}finally{try{if(registered)manager.unregisterTorchCallback(callback);}catch(RuntimeException ignored){}thread.quitSafely();}});}
}
