package org.sleepwell.app;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

/** UUID preference files only. Never schedules, launches an Activity or changes personal settings. */
@RunWith(AndroidJUnit4.class)
public class GoalStorageTest {
    private Context context;
    private String name;
    private SharedPreferences preferences;
    private SleepPlans.Factory previous;
    private SleepPlans.Reminder previousReminder;
    private final AtomicInteger deliveries=new AtomicInteger();

    @Before public void isolate(){
        context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        name="sleepwell-goal-verification-"+UUID.randomUUID();
        preferences=context.getSharedPreferences(name,Context.MODE_PRIVATE);
        previous=SleepPlans.factory;previousReminder=SleepPlans.reminders;
        SleepPlans.factory=c->c.getSharedPreferences(name,Context.MODE_PRIVATE);
        SleepPlans.reminders=c->deliveries.incrementAndGet();
    }
    @After public void restore(){
        SleepPlans.factory=previous;SleepPlans.reminders=previousReminder;
        if(context!=null&&name!=null)context.deleteSharedPreferences(name);
        assertEquals("Storage tests must never invoke reminder delivery",0,deliveries.get());
    }
    private GoalCommitment goal(String metric,int target,String start,String zone,int cutoff){
        return new GoalCommitment(UUID.randomUUID().toString(),metric,target,LocalDate.parse(start),14,zone,cutoff,"Remember why this matters",null);
    }
    private Map<String,?> snapshot(){return new HashMap<>(preferences.getAll());}
    private void rejected(Runnable action){try{action.run();fail("Operation must reject without replacing saved data");}catch(IllegalArgumentException|IllegalStateException expected){}}

    @Test public void legacyMinutePrecisionAndUnrelatedPreferencesSurvivePlanSave(){
        assertTrue(preferences.edit().putInt("goal",487).putInt("bedtime",1350).putInt("wakeTime",420).putBoolean("bedReminder",true)
            .putStringSet("blocked",Set.of("verification.one","verification.two")).putLong("focusPauseUntil",123456L).putString("appearanceTheme","dawn").commit());
        SleepPlan loaded=SleepPlans.load(context);
        assertEquals(487,loaded.goal);assertEquals(1350,loaded.bedtime);assertEquals(420,loaded.wake);
        assertEquals("Legacy enabled reminders were at bedtime",0,loaded.reminderAdvance);
        loaded.windDown=45;SleepPlans.save(context,loaded);
        SleepPlan reopened=SleepPlans.load(context);
        assertEquals(487,reopened.goal);assertEquals(1350,reopened.bedtime);assertEquals(420,reopened.wake);assertEquals(45,reopened.windDown);
        assertTrue(reopened.revision>0);assertEquals(Set.of("verification.one","verification.two"),preferences.getStringSet("blocked",Set.of()));
        assertEquals(123456L,preferences.getLong("focusPauseUntil",0));assertEquals("dawn",preferences.getString("appearanceTheme",""));
    }

    @Test public void stalePlanRevisionRejectsAllFieldsTogether(){
        SleepPlan first=SleepPlans.load(context),stale=SleepPlans.load(context);long staleRevision=stale.revision;
        first.duration(450);first.windDown=60;first.reminder=true;first.reminderAdvance=15;SleepPlans.save(context,first);
        Map<String,?> committed=snapshot();stale.goal=600;stale.bedtime=100;stale.wake=700;stale.windDown=90;stale.reminderAdvance=120;
        rejected(()->SleepPlans.save(context,stale));
        assertEquals(committed,snapshot());assertEquals("Rejection must not silently arm the stale draft",staleRevision,stale.revision);
        SleepPlan current=SleepPlans.load(context);assertEquals(450,current.goal);assertEquals(60,current.windDown);assertEquals(15,current.reminderAdvance);
    }

    @Test public void invalidDraftCannotPartiallyWriteTheSavedPlan(){
        SleepPlan initial=SleepPlans.load(context);SleepPlans.save(context,initial);Map<String,?> before=snapshot();
        SleepPlan invalid=SleepPlans.load(context);invalid.goal=0;invalid.bedtime=120;invalid.windDown=90;
        rejected(()->SleepPlans.save(context,invalid));assertEquals(before,snapshot());
    }

    @Test public void createReplayKeepsTheFirstCommittedGoalAcrossReopen(){
        GoalCommitment first=goal("duration",480,"2026-05-01","Pacific/Honolulu",720);SleepPlans.add(context,first);Map<String,?> original=snapshot();
        preferences=context.getSharedPreferences(name,Context.MODE_PRIVATE);
        GoalCommitment changed=new GoalCommitment(first.id,"bedtime",1430,LocalDate.parse("2026-06-01"),30,"Asia/Tokyo",0,"Changed replay must not overwrite",null);
        SleepPlans.add(context,changed);assertEquals(original,snapshot());
        List<GoalCommitment> goals=SleepPlans.goals(context);assertEquals(1,goals.size());assertEquals("duration",goals.get(0).metric);assertEquals(480,goals.get(0).target);
        assertEquals("Pacific/Honolulu",goals.get(0).zone);assertEquals(720,goals.get(0).cutoff);assertEquals(first.start,goals.get(0).start);
    }

    @Test public void sameMetricOverlapRejectsButAdjacentPeriodsAndOtherMetricsRemainIndependent(){
        SleepPlans.add(context,goal("duration",480,"2026-05-01","UTC",0));Map<String,?> before=snapshot();
        rejected(()->SleepPlans.add(context,goal("duration",420,"2026-05-14","UTC",0)));assertEquals(before,snapshot());
        SleepPlans.add(context,goal("duration",420,"2026-05-15","UTC",0));
        SleepPlans.add(context,goal("bedtime",1320,"2026-05-01","UTC",0));assertEquals(3,SleepPlans.goals(context).size());
    }

    @Test public void stoppedHistoryIsKeptAndTheNextDayCanStartANewPeriod(){
        GoalCommitment first=goal("duration",480,"2026-05-01","UTC",0);SleepPlans.add(context,first);
        SleepPlans.stop(context,first.id,LocalDate.parse("2026-05-03"));SleepPlans.stop(context,first.id,LocalDate.parse("2026-05-05"));
        assertEquals("Repeated stop must not move the original stopping boundary",LocalDate.parse("2026-05-03"),SleepPlans.goals(context).get(0).through());
        SleepPlans.add(context,goal("duration",450,"2026-05-04","UTC",0));assertEquals(2,SleepPlans.goals(context).size());
        Map<String,?> before=snapshot();rejected(()->SleepPlans.stop(context,UUID.randomUUID().toString(),LocalDate.parse("2026-05-04")));assertEquals(before,snapshot());
    }

    @Test public void cancelledFuturePeriodCannotBlockAReplacementOverItsFormerDates(){
        GoalCommitment cancelled=goal("duration",480,"2026-05-10","UTC",0);SleepPlans.add(context,cancelled);
        SleepPlans.stop(context,cancelled.id,LocalDate.parse("2026-05-01"));
        assertEquals(LocalDate.parse("2026-05-09"),SleepPlans.goals(context).get(0).through());
        // The replacement straddles the cancelled start; an empty interval must not be treated as overlapping.
        SleepPlans.add(context,goal("duration",450,"2026-05-01","UTC",0));assertEquals(2,SleepPlans.goals(context).size());
    }

    @Test public void laterPlanChangesLeaveFrozenGoalMetadataUntouched(){
        GoalCommitment commitment=goal("duration",480,"2026-05-01","Pacific/Honolulu",720);SleepPlans.add(context,commitment);
        String original=preferences.getString("goalCommitments","");SleepPlan plan=SleepPlans.load(context);plan.duration(600);SleepPlans.save(context,plan);
        assertEquals(original,preferences.getString("goalCommitments",""));GoalCommitment reopened=SleepPlans.goals(context).get(0);
        assertEquals(480,reopened.target);assertEquals("Pacific/Honolulu",reopened.zone);assertEquals(720,reopened.cutoff);
    }

    @Test public void corruptOrInvalidGoalHistoryRejectsMutationsWithoutReplacingRawData(){
        for(String corrupt:new String[]{"not JSON","[{\"id\":\"not-a-uuid\"}]"}){
            assertTrue(preferences.edit().putString("goalCommitments",corrupt).putString("unrelated","kept").commit());Map<String,?> original=snapshot();
            rejected(()->SleepPlans.goals(context));
            rejected(()->SleepPlans.add(context,goal("duration",480,"2026-05-01","UTC",0)));
            rejected(()->SleepPlans.stop(context,UUID.randomUUID().toString(),LocalDate.parse("2026-05-01")));
            assertEquals(original,snapshot());assertEquals(corrupt,preferences.getString("goalCommitments",""));
        }
    }

    @Test public void boundRepositoryCannotFollowAChangedPreferenceFactory(){
        String otherName="sleepwell-goal-verification-"+UUID.randomUUID();
        SharedPreferences other=context.getSharedPreferences(otherName,Context.MODE_PRIVATE);
        try{
            assertTrue(other.edit().putInt("goal",333).putString("sentinel","The second file must remain unchanged").commit());
            Map<String,?> untouched=new HashMap<>(other.getAll());
            SleepPlans.Repository bound=SleepPlans.repository(context);SleepPlan draft=bound.load();draft.windDown=90;
            SleepPlans.factory=c->c.getSharedPreferences(otherName,Context.MODE_PRIVATE);
            bound.save(draft);
            GoalCommitment commitment=goal("duration",480,"2026-05-01","UTC",0);bound.add(commitment);bound.stop(commitment.id,LocalDate.parse("2026-05-03"));
            assertEquals(90,bound.load().windDown);assertEquals(1,bound.goals().size());assertEquals(LocalDate.parse("2026-05-03"),bound.goals().get(0).through());
            assertEquals("Queued operations must remain bound to their original private file",untouched,other.getAll());
            assertEquals(90,preferences.getInt("windDownMinutes",0));
        }finally{
            SleepPlans.factory=c->c.getSharedPreferences(name,Context.MODE_PRIVATE);
            context.deleteSharedPreferences(otherName);
        }
    }
}
