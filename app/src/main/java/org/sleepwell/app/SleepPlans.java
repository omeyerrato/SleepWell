package org.sleepwell.app;

import android.content.*;
import org.json.*;
import java.time.*;
import java.util.*;

/** One atomic preferences file; tests inject a private file and a non-delivering scheduler. */
final class SleepPlans {
    interface Factory {SharedPreferences open(Context context);}
    interface Reminder {void apply(Context context);}
    static final Factory PRODUCTION=Prefs::of;
    static Factory factory=PRODUCTION;
    static Reminder reminders=Scheduler::bedtime;
    static final class Repository {
        private final SharedPreferences preferences;
        Repository(SharedPreferences preferences){this.preferences=preferences;}
        String reminderError(){return preferences.getString("bedReminderError","");}
        void reminderError(String message){SharedPreferences.Editor edit=preferences.edit();if(message.isEmpty())edit.remove("bedReminderError");else edit.putString("bedReminderError",message);if(!edit.commit())throw new IllegalStateException("Reminder status could not be stored.");}
        SleepPlan load(){return SleepPlans.load(preferences);}
        void save(SleepPlan plan){SleepPlans.save(preferences,plan);}
        List<GoalCommitment> goals(){return SleepPlans.goals(preferences);}
        void add(GoalCommitment goal){SleepPlans.add(preferences,goal);}
        void stop(String id,LocalDate day){SleepPlans.stop(preferences,id,day);}
    }
    static Repository repository(Context c){return new Repository(factory.open(c));}
    static SleepPlan load(Context c){return repository(c).load();}
    private static SleepPlan load(SharedPreferences p){SleepPlan plan=new SleepPlan();plan.goal=p.getInt("goal",480);plan.bedtime=p.getInt("bedtime",1350);plan.wake=p.getInt("wakeTime",420);plan.windDown=p.getInt("windDownMinutes",30);plan.reminder=p.getBoolean("bedReminder",false);plan.smartBedtime=p.getBoolean("smartBedtime",false);plan.noAlarmBedtime=p.getBoolean("noAlarmBedtime",false);plan.reminderAdvance=p.getInt("bedReminderAdvance",p.getBoolean("bedReminder",false)?0:30);plan.revision=p.getLong("planRevision",0);return plan;}
    static void save(Context c,SleepPlan plan){repository(c).save(plan);}
    private static synchronized void save(SharedPreferences p,SleepPlan plan){plan.validate();if(p.getLong("planRevision",0)!=plan.revision)throw new IllegalArgumentException("Your plan changed while it was open. Reopen it before saving.");long revision=Math.max(System.currentTimeMillis(),plan.revision+1);if(!p.edit().putInt("goal",plan.goal).putInt("bedtime",plan.bedtime).putInt("wakeTime",plan.wake).putInt("windDownMinutes",plan.windDown).putInt("bedReminderAdvance",plan.reminderAdvance).putBoolean("bedReminder",plan.reminder).putBoolean("smartBedtime",plan.smartBedtime).putBoolean("noAlarmBedtime",plan.noAlarmBedtime).putLong("planRevision",revision).commit())throw new IllegalStateException("The plan could not be stored.");plan.revision=revision;}
    static List<GoalCommitment> goals(Context c){return repository(c).goals();}
    private static List<GoalCommitment> goals(SharedPreferences p){try{JSONArray rows=new JSONArray(p.getString("goalCommitments","[]"));List<GoalCommitment> goals=new ArrayList<>();for(int i=0;i<rows.length();i++){JSONObject g=rows.getJSONObject(i);goals.add(new GoalCommitment(g.getString("id"),g.getString("metric"),g.getInt("target"),LocalDate.parse(g.getString("start")),g.getInt("days"),g.getString("zone"),g.getInt("cutoff"),g.optString("motivation",""),g.isNull("stopped")?null:LocalDate.parse(g.getString("stopped"))));}return goals;}catch(Exception e){throw new IllegalStateException("Goal history could not be read. Existing data was kept.",e);}}
    static void add(Context c,GoalCommitment goal){repository(c).add(goal);}
    private static synchronized void add(SharedPreferences p,GoalCommitment goal){List<GoalCommitment> all=goals(p);for(GoalCommitment g:all){if(g.id.equals(goal.id))return;if(!g.through().isBefore(g.start)&&!goal.through().isBefore(goal.start)&&g.metric.equals(goal.metric)&&!g.through().isBefore(goal.start)&&!goal.through().isBefore(g.start))throw new IllegalArgumentException("There is already a goal for this metric during these dates.");}all.add(goal);write(p,all);}
    static void stop(Context c,String id,LocalDate day){repository(c).stop(id,day);}
    private static synchronized void stop(SharedPreferences p,String id,LocalDate day){List<GoalCommitment> all=goals(p);boolean found=false;for(int i=0;i<all.size();i++)if(all.get(i).id.equals(id)){if(all.get(i).stopped==null)all.set(i,all.get(i).stop(day));found=true;break;}if(!found)throw new IllegalArgumentException("This goal no longer exists.");write(p,all);}
    private static void write(SharedPreferences p,List<GoalCommitment> goals){try{JSONArray out=new JSONArray();for(GoalCommitment g:goals)out.put(new JSONObject().put("id",g.id).put("metric",g.metric).put("target",g.target).put("start",g.start.toString()).put("days",g.days).put("zone",g.zone).put("cutoff",g.cutoff).put("motivation",g.motivation).put("stopped",g.stopped==null?JSONObject.NULL:g.stopped.toString()));if(!p.edit().putString("goalCommitments",out.toString()).commit())throw new IllegalStateException("Goal history could not be stored.");}catch(JSONException e){throw new IllegalStateException(e);}}
}
