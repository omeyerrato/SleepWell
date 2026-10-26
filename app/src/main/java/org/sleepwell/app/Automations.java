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
    /** Source occurrence time is retained rather than replaced by asynchronous delivery time. */
    static void confirmedAt(Context context,AutomationEvent.Kind kind,String occurrence,long at){
        try{queue(context,AutomationEvent.occurrence(kind,occurrence,at,false));}catch(RuntimeException unavailable){/* Never interrupt the owning output. */}
    }
    private static void queue(Context context,AutomationEvent event){Context app=context.getApplicationContext();io.execute(()->{try(AutomationStore store=new AutomationStore(app)){store.enqueue(event,null);schedule(app,store);}catch(Exception ignored){app.getSharedPreferences("automation_status",0).edit().putString("error","A confirmed sound event could not be queued.").apply();}});}
    private static final java.util.concurrent.atomic.AtomicInteger recognitionPending=new java.util.concurrent.atomic.AtomicInteger();
    /** Called once after a production sound_events insert returns successfully, never on raw windows. */
    static void soundRecognized(Context context,long session,long row,long at,String category){
        if(session<=0||row<=0||at<=0||!java.util.Set.of("snoring","speech","laughter","baby_cry","cough").contains(category))return;
        // At most eight waiting handoffs; persisted recordings are never dropped by this gate.
        if(recognitionPending.incrementAndGet()>8){recognitionPending.decrementAndGet();return;}
        Context app=context.getApplicationContext();
        try{io.execute(()->{try(AutomationStore store=new AutomationStore(app)){
            boolean enabled=false;for(AutomationStore.Row rule:store.rows())if(rule.enabled&&rule.event.equals(AutomationEvent.Kind.SOUND_RECOGNIZED.name())){enabled=true;break;}if(!enabled)return;
            AutomationEvent event=AutomationEvent.occurrence(AutomationEvent.Kind.SOUND_RECOGNIZED,session+":"+row+":"+at+":"+category,at,false).soundClass(category);
            android.content.SharedPreferences gate=app.getSharedPreferences("automation_sound_gate",0);long now=System.currentTimeMillis(),last=gate.getLong("at",0);
            if(event.id.equals(gate.getString("event",""))||last>0&&now-last<30000L)return;
            // Claim before queueing: process death may lose an event, but cannot replay it on restart.
            if(!gate.edit().putLong("at",now).putString("event",event.id).commit())return;
            store.enqueue(event,null);schedule(app,store);
        }catch(Exception ignored){app.getSharedPreferences("automation_status",0).edit().putString("error","A saved sound estimate could not enter its automation queue.").apply();}finally{recognitionPending.decrementAndGet();}});}catch(RuntimeException unavailable){recognitionPending.decrementAndGet();}
    }
    static void recover(Context context){Context app=context.getApplicationContext();io.execute(()->{try(AutomationStore store=new AutomationStore(app)){schedule(app,store);}catch(Exception ignored){}});}
    static void schedule(Context context,AutomationStore store){if(runningJobs.get()>0)return;JobScheduler scheduler=context.getSystemService(JobScheduler.class);long due=store.due();if(due<0){scheduler.cancel(JOB);return;}int result=scheduler.schedule(new JobInfo.Builder(JOB,new ComponentName(context,AutomationJobService.class)).setMinimumLatency(Math.max(0,due-System.currentTimeMillis())).setPersisted(true).build());context.getSharedPreferences("automation_status",0).edit().putString("error",result==JobScheduler.RESULT_SUCCESS?"":"Android did not schedule automation delivery. Reopen Automations to retry.").apply();}
}
