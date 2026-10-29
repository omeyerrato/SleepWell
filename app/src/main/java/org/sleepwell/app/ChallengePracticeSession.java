package org.sleepwell.app;

import android.content.Context;
import android.os.Bundle;
import org.json.JSONArray;
import java.util.*;

/** A private draft exercise. Its units and results never authorize an alarm occurrence. */
final class ChallengePracticeSession {
    private static final int VERSION=1,MAX_CONFIG=262144;
    final String run,snapshot,selectionNotice;
    final ChallengeRoutine configured,routine;
    private int completed,generation;
    private Bundle draft;
    private String pending="";

    /** A new render invalidates every callback from the previous card, even for the same unit. */
    record Ticket(String run,int unit,int generation,String task){}

    private ChallengePracticeSession(String run,String snapshot,ChallengeRoutine configured,
                                     ChallengeRoutine routine,String notice,int completed,Bundle draft){
        this.run=run;this.snapshot=snapshot;this.configured=configured;this.routine=routine;
        selectionNotice=notice;this.completed=completed;this.draft=boundedDraft(draft);
    }

    static String canonical(String raw){
        if(raw==null||raw.length()>MAX_CONFIG)throw new IllegalArgumentException("Routine preview settings are unavailable.");
        try{return ChallengeRoutineJson.encode(ChallengeRoutineJson.decode(new JSONArray(raw))).toString();}
        catch(Exception invalid){throw new IllegalArgumentException("Routine preview settings could not be read.",invalid);}
    }

    static List<String> unwired(ChallengeRoutine routine,Set<String> supported){
        List<String> missing=new ArrayList<>();
        // Check the entire configured pool, not just a lucky supported random selection.
        for(int n=0;n<routine.tasks.size();n++)if(!supported.contains(routine.tasks.get(n).type))
            missing.add("Task "+(n+1)+" · "+routine.tasks.get(n).type);
        return List.copyOf(missing);
    }

    static ChallengePracticeSession open(Context context,String raw,Bundle restored,Set<String> supported){
        String snapshot=canonical(raw);
        try{
            ChallengeRoutine configured=ChallengeRoutineJson.decode(new JSONArray(snapshot));
            List<String> missing=unwired(configured,supported);
            if(!missing.isEmpty())throw new IllegalArgumentException("Routine preview is not available for "+String.join(", ",missing)+".");
            if(restored!=null){
                if(restored.getInt("version")!=VERSION||!snapshot.equals(restored.getString("snapshot")))
                    throw new IllegalArgumentException("This routine changed. Start a new preview.");
                String run=restored.getString("run");
                if(run==null||!UUID.fromString(run).toString().equals(run))throw new IllegalArgumentException("Preview ownership is unavailable.");
                ChallengeRoutine selected=ChallengeRoutineJson.decode(new JSONArray(canonical(restored.getString("selected",""))));
                if(selected.randomCount!=0||selected.tasks.size()!=(configured.randomCount>0?configured.randomCount:configured.tasks.size()))
                    throw new IllegalArgumentException("Preview selection is unavailable.");
                if(configured.randomCount==0&&!snapshot.equals(ChallengeRoutineJson.encode(selected).toString()))
                    throw new IllegalArgumentException("Preview task settings changed.");
                if(configured.randomCount>0&&!selectionBelongs(configured,selected))
                    throw new IllegalArgumentException("Preview selection does not belong to this pool.");
                int completed=restored.getInt("completed",-1);
                if(completed<0||completed>selected.units())throw new IllegalArgumentException("Preview progress is unavailable.");
                ChallengePracticeSession session=new ChallengePracticeSession(run,snapshot,configured,selected,restored.getString("notice",""),completed,restored.getBundle("draft"));
                String pending=restored.getString("pending","");
                if(!pending.isEmpty()){if(!UUID.fromString(pending).toString().equals(pending))throw new IllegalArgumentException("Invalid preview request.");session.pending=pending;}
                return session;
            }
            ChallengeRoutine selected=configured;String notice="";
            if(configured.randomCount>0){
                RandomChallenges.Selection sample=RandomChallenges.select(configured,task->{
                    if(RoutineAudioPracticeActivity.TYPES.contains(task.type)&&context instanceof android.app.Activity owner){
                        try{return PracticeAudioAccess.factory.create(owner).unavailable(task.type);}catch(RuntimeException unreadable){return "Listening readiness could not be checked";}
                    }
                    return RandomChallenges.unavailable(context,task);
                });
                selected=sample.routine;
                notice="Example only · "+sample.notice.replace("this occurrence","this preview")+" The next alarm makes its own selection.";
            }
            return new ChallengePracticeSession(UUID.randomUUID().toString(),snapshot,configured,selected,notice,0,null);
        }catch(IllegalArgumentException invalid){throw invalid;}
        catch(Exception invalid){throw new IllegalArgumentException("Preview state could not be restored. Start a new preview.",invalid);}
    }

    private static boolean selectionBelongs(ChallengeRoutine pool,ChallengeRoutine selected){
        List<String> available=new ArrayList<>();
        for(ChallengeRoutine.Task task:pool.tasks)available.add(taskKey(task));
        for(ChallengeRoutine.Task task:selected.tasks){
            // RandomChallenges may explicitly replace an unavailable entry with one Math unit.
            if(available.remove(taskKey(task)))continue;
            if(!task.type.equals("Math")||task.rounds!=1||!task.math.equals(MathSettings.LEGACY))return false;
        }
        return true;
    }

    private static String taskKey(ChallengeRoutine.Task task){return ChallengeRoutineJson.encode(new ChallengeRoutine(List.of(task))).toString();}
    private static Bundle boundedDraft(Bundle state){
        if(state==null)return null;
        android.os.Parcel parcel=android.os.Parcel.obtain();
        try{parcel.writeBundle(state);if(parcel.dataSize()>65536)throw new IllegalArgumentException("This preview answer is too large to retain. Shorten it before continuing.");return new Bundle(state);}
        finally{parcel.recycle();}
    }
    int completed(){return completed;}
    boolean finished(){return completed==routine.units();}
    ChallengeRoutine.Position position(){return routine.at(completed);}
    Bundle draft(){return draft==null?null:new Bundle(draft);}
    Ticket render(){if(android.os.Looper.myLooper()!=android.os.Looper.getMainLooper())throw new IllegalStateException("Preview rendering belongs to its UI owner");generation++;ChallengeRoutine.Position at=position();return at==null?null:new Ticket(run,completed,generation,taskKey(at.task));}
    void invalidate(){generation++;}
    boolean owns(Ticket ticket){if(android.os.Looper.myLooper()!=android.os.Looper.getMainLooper()||!pending.isEmpty())return false;ChallengeRoutine.Position at=position();return ticket!=null&&at!=null&&run.equals(ticket.run())&&completed==ticket.unit()&&generation==ticket.generation()&&taskKey(at.task).equals(ticket.task());}
    boolean checkpoint(Ticket ticket,Bundle state){if(!owns(ticket))return false;draft=boundedDraft(state);return true;}
    boolean advance(Ticket ticket){if(!owns(ticket))return false;int next=routine.advance(completed,ticket.unit());if(next==completed)return false;completed=next;generation++;draft=null;return true;}
    String pending(){return pending;}
    boolean sameUnit(String run,int unit,String task){return android.os.Looper.myLooper()==android.os.Looper.getMainLooper()&&this.run.equals(run)&&completed==unit&&position()!=null&&taskKey(position().task).equals(task);}
    boolean issueChild(String nonce,int unit,String task){if(!pending.isEmpty()||!sameUnit(run,unit,task))return false;pending=nonce;generation++;return true;}
    boolean child(String nonce,int unit,String task){return nonce!=null&&nonce.equals(pending)&&sameUnit(run,unit,task);}
    boolean rememberChild(String nonce,int unit,String task,Bundle checkpoint){if(!child(nonce,unit,task))return false;Bundle next=draft==null?new Bundle():new Bundle(draft);if(checkpoint!=null)next.putBundle("motionCheckpoint",new Bundle(checkpoint));draft=boundedDraft(next);return true;}
    boolean childReturned(String nonce,int unit,String task,boolean success,boolean fallback){
        if(!child(nonce,unit,task)||success&&fallback)return false;
        pending="";generation++;
        if(success){completed=routine.advance(completed,unit);draft=null;}
        else if(fallback){draft=new Bundle();draft.putBoolean("practiceFallback",true);}
        return true;
    }
    void abandonChild(){pending="";generation++;}
    Bundle save(){Bundle state=new Bundle();state.putInt("version",VERSION);state.putString("run",run);state.putString("snapshot",snapshot);state.putString("selected",ChallengeRoutineJson.encode(routine).toString());state.putString("notice",selectionNotice);state.putInt("completed",completed);state.putString("pending",pending);if(draft!=null)state.putBundle("draft",new Bundle(draft));return state;}
}
