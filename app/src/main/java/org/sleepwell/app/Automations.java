package org.sleepwell.app;

import android.app.job.*;
import android.content.*;
import java.util.concurrent.*;

final class Automations {
    static final int JOB=7301;
    static final ExecutorService io=Executors.newSingleThreadExecutor(r->new Thread(r,"SleepWell automations"));
    static final java.util.concurrent.atomic.AtomicInteger runningJobs=new java.util.concurrent.atomic.AtomicInteger();
    static void emit(Context context,AutomationEvent.Kind kind,String occurrence){Context app=context.getApplicationContext();long now=System.currentTimeMillis();io.execute(()->{try(AutomationStore store=new AutomationStore(app)){store.enqueue(AutomationEvent.occurrence(kind,occurrence,now,false),null);schedule(app,store);}catch(Exception ignored){app.getSharedPreferences("automation_status",0).edit().putString("error","An automation event could not be queued. Open Automations to retry scheduling.").apply();}});}
    /** A lifecycle hook cannot make the owning alarm or capture fail if its queue is unavailable. */
    static void confirmed(Context context,AutomationEvent.Kind kind,String occurrence){try{emit(context,kind,occurrence);}catch(RuntimeException unavailable){try{context.getSharedPreferences("automation_status",0).edit().putString("error","A confirmed event could not enter the delivery queue. The source action continues.").apply();}catch(RuntimeException ignored){}}}
    static void recover(Context context){Context app=context.getApplicationContext();io.execute(()->{try(AutomationStore store=new AutomationStore(app)){schedule(app,store);}catch(Exception ignored){}});}
    static void schedule(Context context,AutomationStore store){if(runningJobs.get()>0)return;JobScheduler scheduler=context.getSystemService(JobScheduler.class);long due=store.due();if(due<0){scheduler.cancel(JOB);return;}int result=scheduler.schedule(new JobInfo.Builder(JOB,new ComponentName(context,AutomationJobService.class)).setMinimumLatency(Math.max(0,due-System.currentTimeMillis())).setPersisted(true).build());context.getSharedPreferences("automation_status",0).edit().putString("error",result==JobScheduler.RESULT_SUCCESS?"":"Android did not schedule automation delivery. Reopen Automations to retry.").apply();}
}
