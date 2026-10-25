package org.sleepwell.app;
import android.content.*;
/** Reconciles only the existing owned journal; never scans history or creates new triggers. */
public final class AutoCloudBackupReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){if(i!=null&&(Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())||Intent.ACTION_MY_PACKAGE_REPLACED.equals(i.getAction())))AutoCloudBackups.recoverExisting(c);}
}
