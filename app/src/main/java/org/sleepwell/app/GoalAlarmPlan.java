package org.sleepwell.app;
import android.content.*;
/** Real saved-plan boundary. Default values are not presented as a saved user commitment. */
final class GoalAlarmPlan {
 static final class Snapshot {final int bedtime,goal,wake;final long revision;Snapshot(int bedtime,int goal,int wake,long revision){new GoalAlarmTime(bedtime,goal);if(wake<0||wake>=1440||revision<0)throw new IllegalArgumentException("Saved sleep plan needs review.");this.bedtime=bedtime;this.goal=goal;this.wake=wake;this.revision=revision;}String identity(){return revision+":"+bedtime+":"+goal+":"+wake;}GoalAlarmTime time(){return new GoalAlarmTime(bedtime,goal);}}
 static Snapshot load(Context c){SharedPreferences p=SleepPlans.factory.open(c);if(!p.contains("goal")||!p.contains("bedtime"))throw new IllegalArgumentException("Save a sleep plan with a bedtime and sleep goal first.");try{SleepPlan plan=new SleepPlans.Repository(p).load();plan.validate();return new Snapshot(plan.bedtime,plan.goal,plan.wake,plan.revision);}catch(RuntimeException invalid){throw new IllegalArgumentException("Saved sleep plan needs review. Open Sleep goals and save valid times.");}}
 private GoalAlarmPlan(){}
}
