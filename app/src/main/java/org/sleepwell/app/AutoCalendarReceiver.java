package org.sleepwell.app;
import android.content.*;
/** System lifecycle recovery can queue a future job, never replay an uncertain insertion. */
public final class AutoCalendarReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){if(i==null||!java.util.Set.of(Intent.ACTION_BOOT_COMPLETED,Intent.ACTION_MY_PACKAGE_REPLACED).contains(i.getAction()))return;PendingResult pending=goAsync();AutoCalendar.resume(c);AutoCalendar.worker.execute(pending::finish);}
}
