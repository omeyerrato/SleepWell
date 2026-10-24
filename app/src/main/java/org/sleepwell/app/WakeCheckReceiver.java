package org.sleepwell.app;
import android.content.*;
/** Explicit immutable PendingIntents target this nonexported receiver. */
public final class WakeCheckReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context context,Intent intent){if(intent==null)return;String action=intent.getAction();WakeCheckEngine.Stage stage=WakeChecks.PROMPT.equals(action)?WakeCheckEngine.Stage.PROMPT:WakeChecks.EXPIRE.equals(action)?WakeCheckEngine.Stage.EXPIRE:null;if(stage==null)return;WakeChecks.receive(context,intent.getStringExtra("nonce"),stage);}
}
