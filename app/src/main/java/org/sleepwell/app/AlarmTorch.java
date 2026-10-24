package org.sleepwell.app;

import android.content.Context;
import java.util.function.BooleanSupplier;

/** Steady alarm torch. The shared serial broker preempts experimental cues before acquisition. */
final class AlarmTorch {
 private final Context context;private final BooleanSupplier alive;private PhoneTorches.Request request;private volatile PhoneTorches.Handle handle;private volatile boolean closed;
 AlarmTorch(Context c,BooleanSupplier alive){context=c.getApplicationContext();this.alive=alive;try{request=PhoneTorches.provider.acquire(context,PhoneTorchCoordinator.Kind.ALARM,()->!closed&&alive.getAsBoolean(),new PhoneTorches.Events(){public void ready(PhoneTorches.Handle value){handle=value;if(closed||!alive.getAsBoolean())value.close();}public void lost(String reason){if(reason.equals("The flashlight could not be released.")||!closed&&alive.getAsBoolean())Prefs.put(context,"lastError","Alarm flashlight unavailable. Sound and the challenge continue.");}});if(closed)request.close();}catch(RuntimeException failure){Prefs.put(context,"lastError","Alarm flashlight unavailable. Sound and the challenge continue.");}}
 void close(){closed=true;PhoneTorches.Handle current=handle;if(current!=null)current.close();PhoneTorches.Request pending=request;if(pending!=null)pending.close();}
}
