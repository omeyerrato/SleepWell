package org.sleepwell.app;

import android.content.*;

/** Only an explicit immutable notification action can stop its current pair link. */
public final class PairMotionStopReceiver extends BroadcastReceiver {
    static final String ACTION="org.sleepwell.app.STOP_PAIRED_MOTION";
    @Override public void onReceive(Context context,Intent intent){if(intent==null||!ACTION.equals(intent.getAction())||intent.getData()==null)return;String link=intent.getStringExtra("link");if(link==null||!intent.getData().toString().equals("sleepwell://stop-pair/"+link))return;PairMotionRuntime.stop(link);}
}
