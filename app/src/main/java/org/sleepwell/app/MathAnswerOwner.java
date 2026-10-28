package org.sleepwell.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Looper;
import android.view.View;
import java.util.function.*;

/** A synchronous Math input belongs to one visible card, question and durable occurrence unit. */
final class MathAnswerOwner {
    private final Activity activity;private final View card;private final String run,cache;private final int unit;
    private final MathTask task;private final SharedPreferences prefs;private final BooleanSupplier resumed;
    private final Supplier<String> occurrence;private final IntSupplier completed;private final Supplier<MathTask> currentTask;
    MathAnswerOwner(Activity activity,View card,String run,int unit,MathTask task,SharedPreferences prefs,
                    BooleanSupplier resumed,Supplier<String> occurrence,IntSupplier completed,Supplier<MathTask> currentTask){
        this.activity=activity;this.card=card;this.run=run;this.unit=unit;this.task=task;this.prefs=prefs;this.resumed=resumed;
        this.occurrence=occurrence;this.completed=completed;this.currentTask=currentTask;cache=prefs.getString("mathTask","");
    }
    /** Keep the guard and the caller's selection/build/advance indivisible with occurrence replacement. */
    boolean submit(Runnable mutation){synchronized(Scheduler.class){if(!current())return false;mutation.run();return true;}}
    boolean current(){
        if(Looper.myLooper()!=Looper.getMainLooper())return false;
        if(run==null||run.isEmpty()||unit<0||task==null||!resumed.getAsBoolean()||activity.isFinishing()||activity.isDestroyed()
                ||!activity.hasWindowFocus()||!card.isAttachedToWindow()||!card.isShown()
                ||card.getRootView()!=activity.getWindow().getDecorView()||currentTask.get()!=task
                ||!run.equals(occurrence.get())||completed.getAsInt()!=unit)return false;
        try{return prefs.getBoolean("ringing",false)&&run.equals(prefs.getString("alarmRun",""))
                    &&prefs.getInt("challengeCompleted",0)==unit&&!cache.isEmpty()&&cache.equals(prefs.getString("mathTask",""));}
        catch(RuntimeException unreadable){return false;}
    }
}
