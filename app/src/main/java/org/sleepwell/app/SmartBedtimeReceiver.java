package org.sleepwell.app;
/** Private exact-reminder delivery; all record access happens on the owned worker. */
public final class SmartBedtimeReceiver extends android.content.BroadcastReceiver {
 @Override public void onReceive(android.content.Context c,android.content.Intent intent){if(intent==null)return;if(SmartBedtimeRuntime.DISMISS.equals(intent.getAction())){PendingResult pending=goAsync();SmartBedtimeRuntime.dismiss(c,intent.getStringExtra("series"),pending::finish);return;}if(!SmartBedtimeRuntime.ACTION.equals(intent.getAction()))return;PendingResult pending=goAsync();SmartBedtimeRuntime.deliver(c,intent.getStringExtra("nonce"),intent.getLongExtra("at",0),pending::finish);}
}
