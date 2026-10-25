package org.sleepwell.app;
import android.app.*;import android.content.*;import android.content.pm.ServiceInfo;import android.os.*;

/** One explicit visible microphone attempt; no restart or implicit admission. */
public final class SpeakCaptureService extends Service {
    private SpeakAccess.NativeOperation operation;private final SpeakServiceSlot<SpeakAccess.NativeOperation> owner=new SpeakServiceSlot<>();private final Handler handler=new Handler(Looper.getMainLooper());
    @Override public IBinder onBind(Intent intent){return null;}
    @Override public int onStartCommand(Intent intent,int flags,int startId){String ticket=intent==null?null:intent.getStringExtra("ticket");if(owner.occupied(SpeakAccess.NativeOperation::released)){SpeakAccess.reject(ticket);return START_NOT_STICKY;}SpeakAccess.NativeOperation admitted=SpeakAccess.claim(ticket);if(admitted==null){stopSelf(startId);return START_NOT_STICKY;}handler.removeCallbacksAndMessages(null);operation=admitted;owner.adopt(admitted,startId);try{Notifications.channels(this);Notification notification=new Notification.Builder(this,Notifications.TRACK).setSmallIcon(R.drawable.ic_moon).setContentTitle("Speak challenge").setContentText("Listening for speech for up to 15 seconds").setOngoing(true).setCategory(Notification.CATEGORY_SERVICE).build();startForeground(108,notification,ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);admitted.start(()->{if(owner.owns(admitted,startId)){handler.removeCallbacksAndMessages(null);stopSelf(startId);}});handler.post(tick);}catch(RuntimeException failure){admitted.fail("Android did not allow microphone listening. Use Math.");stopSelf(startId);}return START_NOT_STICKY;}
    private final Runnable tick=new Runnable(){public void run(){if(operation==null)return;if(!operation.current())operation.cancel();if(operation.runtime!=null)operation.runtime.tick();if(!operation.released())handler.postDelayed(this,100);}};
    @Override public void onDestroy(){handler.removeCallbacksAndMessages(null);if(operation!=null)operation.cancel();super.onDestroy();}
}
