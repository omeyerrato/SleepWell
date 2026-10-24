package org.sleepwell.app;
import android.content.*;
import java.util.UUID;

/** Private immutable notification action; never creates a tracking service or light output. */
public final class TravelCueStopReceiver extends BroadcastReceiver {
 static final String STOP="org.sleepwell.app.STOP_TRAVEL_CUES";
 @Override public void onReceive(Context context,Intent intent){if(intent==null||!STOP.equals(intent.getAction()))return;String nonce=intent.getStringExtra("nonce");if(nonce==null)return;try{if(!UUID.fromString(nonce).toString().equals(nonce))return;}catch(IllegalArgumentException invalid){return;}TravelCues.control.stop(nonce);}
}
