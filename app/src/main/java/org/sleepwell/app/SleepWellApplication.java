package org.sleepwell.app;
/** Owned recovery never enables DND or creates a backup trigger from recorded history. */
public final class SleepWellApplication extends android.app.Application {
 @Override public void onCreate(){super.onCreate();TrackingDndRuntime.recover(this);AutoCloudBackups.recoverExisting(this);SmartBedtimeRuntime.observe(this);GoogleSleepSignal.recover(this);}
}
