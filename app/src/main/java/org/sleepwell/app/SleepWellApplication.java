package org.sleepwell.app;
/** Recovery is queued before later tracking commands on the same owned worker; it never enables DND. */
public final class SleepWellApplication extends android.app.Application {
 @Override public void onCreate(){super.onCreate();TrackingDndRuntime.recover(this);}
}
