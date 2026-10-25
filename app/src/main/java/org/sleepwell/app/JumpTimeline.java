package org.sleepwell.app;
import android.os.*;
/** One foreground animation owner. Tests replace time/frame delivery, never game outcomes. */
final class JumpTimeline {
 interface Port{long now();void request(Runnable frame);void cancel();}interface Factory{Port create();}
 static Factory factory=()->new Port(){final Handler handler=new Handler(Looper.getMainLooper());public long now(){return SystemClock.uptimeMillis();}public void request(Runnable frame){handler.postDelayed(frame,16);}public void cancel(){handler.removeCallbacksAndMessages(null);}};
 private JumpTimeline(){}
}
