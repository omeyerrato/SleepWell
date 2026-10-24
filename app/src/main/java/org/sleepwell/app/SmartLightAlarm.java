package org.sleepwell.app;

import android.content.Context;
import android.os.SystemClock;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

/** All lamp I/O is serial and off the phone-alarm thread. No delayed light work survives its owner. */
final class SmartLightAlarm {
    static final ScheduledExecutorService worker=Executors.newSingleThreadScheduledExecutor(r->new Thread(r,"SleepWell bedroom light"));
    private static volatile SmartLightAlarm current;
    private final Context context;private final long started;private final BooleanSupplier alive;private volatile boolean closed;private SmartLightConfig config;private SmartLightRun run;private long queuedAt=Long.MIN_VALUE;private final java.util.concurrent.atomic.AtomicBoolean pending=new java.util.concurrent.atomic.AtomicBoolean();
    private SmartLightAlarm(Context context,long started,BooleanSupplier alive){this.context=context.getApplicationContext();this.started=started;this.alive=alive;}
    static synchronized SmartLightAlarm start(Context context,Alarm alarm,String occurrence,long started,BooleanSupplier alive){if(alarm.id<=0||occurrence.isEmpty()||!SmartLightSettings.factory.preferences(context).getBoolean("enabled",false))return null;SmartLightTest.stop();if(current!=null)current.close();SmartLightAlarm owner=new SmartLightAlarm(context,started,alive);current=owner;owner.tick();return owner;}
    private boolean owns(){synchronized(SmartLightAlarm.class){return !closed&&current==this&&alive.getAsBoolean();}}
    void tick(){if(!owns())return;long now=SystemClock.elapsedRealtime();if(queuedAt!=Long.MIN_VALUE&&now-queuedAt<5000)return;if(!pending.compareAndSet(false,true))return;queuedAt=now;worker.execute(()->{try{if(!owns())return;if(config==null){config=SmartLightSettings.load(context);if(!config.enabled){close();return;}run=new SmartLightRun(config,new HomeAssistantLight(config),()->owns()&&SmartLightSettings.current(context,config.revision),message->{if(SmartLightSettings.factory.preferences(context).getLong("revision",0)==config.revision)SmartLightSettings.status(context,message);if(message.contains("unavailable")||message.contains("could not"))Prefs.put(context,"lastError",message);});}run.tick(Math.max(0,SystemClock.elapsedRealtime()-started));}catch(Exception failure){SmartLightSettings.status(context,"Bedroom light disconnected; check saved configuration");Prefs.put(context,"lastError","Bedroom light unavailable. Phone alarm continues.");close();}finally{pending.set(false);}}); }
    void close(){if(closed)return;closed=true;synchronized(SmartLightAlarm.class){if(current==this)current=null;}worker.execute(()->{if(run!=null)run.close();});}
    static synchronized void stop(){if(current!=null)current.close();}
    static boolean active(){return current!=null&&!current.closed;}
}
