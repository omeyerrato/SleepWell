package org.sleepwell.app;
import android.content.*;
/** Explicit immutable one-shot only; the receiver is private. */
public final class RecurringTrackingReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){if(intent==null||!RecurringTracking.ACTION.equals(intent.getAction())||intent.getData()==null||!"sleepwell-recurring".equals(intent.getData().getScheme())||!"occurrence".equals(intent.getData().getHost()))return;try{RecurringTracking.receive(context.getApplicationContext(),RecurringTracking.port,intent.getData().getLastPathSegment());}catch(RuntimeException failure){android.util.Log.e("SleepWell","Nightly schedule delivery failed",failure);}}
}
