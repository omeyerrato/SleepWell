package org.sleepwell.app;
/** Nonexported explicit callback; it never starts a service, microphone or recording. */
public final class GoogleSleepSignalReceiver extends android.content.BroadcastReceiver {
 @Override public void onReceive(android.content.Context c,android.content.Intent intent){GoogleSleepSignal.receive(c,intent);}
}
