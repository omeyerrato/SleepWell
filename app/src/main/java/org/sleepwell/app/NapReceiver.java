package org.sleepwell.app;
import android.content.*;
public final class NapReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){if(!Naps.DEADLINE.equals(i.getAction()))return;int id=i.getIntExtra("id",0);String nonce=i.getStringExtra("nonce");if(id<=0||nonce==null)return;try{Naps.receive(c,id,nonce);}catch(RuntimeException failure){Prefs.put(c,"lastError","Nap delivery needs attention. Open Naps to review it.");}}
}
