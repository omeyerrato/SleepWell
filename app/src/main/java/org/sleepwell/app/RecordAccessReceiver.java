package org.sleepwell.app;

import android.content.*;

/** Protected system events invalidate saved authority; no automatic URI regrant. */
public final class RecordAccessReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){if(intent==null||!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())&&!Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction()))return;PendingResult pending=goAsync();Context app=context.getApplicationContext();new Thread(()->{try{RecordAccessGrants.revokeAll(app,"Access expired after reboot or SleepWell update. Verify packages and explicitly Allow again.");}catch(RuntimeException unavailable){/* Boot/signature/private-state checks still deny mismatching callers. */}finally{pending.finish();}},"SleepWell record grant cleanup").start();}
}
