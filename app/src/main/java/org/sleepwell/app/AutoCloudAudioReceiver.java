package org.sleepwell.app;
import android.content.*;
/** Reconciles an existing journal only. It never scans history or creates an upload request. */
public final class AutoCloudAudioReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context context,Intent intent){if(intent!=null&&(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())||Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())))AutoCloudAudio.recoverExisting(context);}
}
