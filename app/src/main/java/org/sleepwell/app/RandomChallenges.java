package org.sleepwell.app;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.nfc.NfcAdapter;
import android.media.AudioManager;
import org.json.*;
import java.security.SecureRandom;
import java.util.*;

/** Portable configuration is separate from the concrete, occurrence-owned challenge. */
final class RandomChallenges {
    static final String TYPE="Random tasks";
    private static final String KEY="randomChallengeOccurrenceV1";
    static final List<String> SCREEN=List.of("Math","Memory","Sequence","Retype","Visual search","Counting","Flags","Mirror text","Dream journal","Jumping","Type code");
    static final List<String> HARDWARE=List.of("Shake","Walk","NFC","QR code","Tilt maze","Light","Spin around");
    static final List<String> CAMERA=List.of("Picture match","Object recognition","Smile","Pose","Camera light");
    static final List<String> AUDIO=List.of("Speak","Laughter","Loud sound","Spoken phrase","Musical note");
    static final class Prepared {
        final Alarm alarm;final String ledger;
        Prepared(Alarm alarm,String ledger){this.alarm=alarm;this.ledger=ledger;}
        void write(SharedPreferences.Editor edit){if(ledger.isEmpty())edit.remove(KEY);else edit.putString(KEY,ledger);}
    }
    static JSONArray encode(ChallengeRoutine routine){try{return new JSONArray().put(new JSONObject().put("type",TYPE).put("rounds",routine.randomCount).put("pool",ChallengeRoutineJson.encode(new ChallengeRoutine(routine.tasks))));}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
    static ChallengeRoutine decode(JSONArray array)throws JSONException{
        if(array.length()!=1)throw new JSONException("Random mode is one complete routine, not a nested task");
        JSONObject row=array.getJSONObject(0);if(row.length()!=3||!TYPE.equals(row.get("type")))throw new JSONException("Invalid random routine");
        Object raw=row.get("rounds");if(!(raw instanceof Number))throw new JSONException("Random task count must be an integer");double n=((Number)raw).doubleValue();if(!Double.isFinite(n)||n!=Math.rint(n)||n<1||n>8)throw new JSONException("Choose 1–8 random tasks");
        JSONArray pool=row.getJSONArray("pool");if(pool.length()<1||pool.length()>8)throw new JSONException("Choose 1–8 pool entries");
        for(int i=0;i<pool.length();i++){
            JSONObject task=pool.getJSONObject(i);String type=task.getString("type");if(TYPE.equals(type)||!supported(type))throw new JSONException("This task is not supported in a random pool");
            Set<String> keys=new HashSet<>(List.of("type","rounds"));switch(type){case "Math":keys.add("math");break;case "Walk":keys.add("steps");break;case "Dream journal":keys.add("minimum");break;case "QR code":keys.add("code");break;case "Picture match":keys.add("picture");break;case "Object recognition":keys.add("objectTarget");break;default:break;}
            Iterator<String> names=task.keys();while(names.hasNext())if(!keys.contains(names.next()))throw new JSONException("Unknown random task setting");
        }
        ChallengeRoutine fixed=ChallengeRoutineJson.decode(pool);Set<String> seen=new HashSet<>();for(ChallengeRoutine.Task task:fixed.tasks)if(!seen.add(ChallengeRoutineJson.encode(new ChallengeRoutine(List.of(task))).toString()))throw new JSONException("Duplicate random pool entry");try{return ChallengeRoutine.random(fixed.tasks,(int)n);}catch(IllegalArgumentException invalid){throw new JSONException(invalid.getMessage());}
    }
    static boolean supported(String type){return SCREEN.contains(type)||HARDWARE.contains(type)||CAMERA.contains(type)||AUDIO.contains(type);}
    static boolean needsTemplate(String type){return type.equals("QR code")||type.equals("Picture match")||type.equals("Object recognition");}
    /** Capability checks deliberately do not initialize a detector, recognizer, camera or microphone. */
    static String readinessNote(ChallengeRoutine.Task task){
        if(task.type.equals(PhraseRule.TYPE))return "Offline English readiness is checked only after explicit Listen. An installed device recognizer does not guarantee its English model; Math remains available.";
        if(CAMERA.contains(task.type))return "Camera and local detector availability are checked again only after explicit Start. Basic hardware and permission checks do not prove the model can run.";
        if(AUDIO.contains(task.type))return "Listening starts only on an explicit tap. Microphone and local processing can still become unavailable; Math remains available.";
        return "";
    }
    /** Read-only eligibility; never requests permissions or starts hardware. Rechecked at the occurrence. */
    static String unavailable(Context context,ChallengeRoutine.Task task){
        if(!supported(task.type))return "Not supported in a random pool yet";
        if(SCREEN.contains(task.type))return "";
        try{
            if(AUDIO.contains(task.type))return microphone(context,task);
            if(CAMERA.contains(task.type)){
                if(task.type.equals("Picture match")&&task.picture==null)return "Capture a picture reference in a fixed routine first";
                if(task.type.equals("Object recognition")&&task.objectTarget==null)return "Choose an object target in a fixed routine first";
                String feature=task.type.equals("Smile")||task.type.equals("Pose")?PackageManager.FEATURE_CAMERA_FRONT:task.type.equals("Object recognition")?PackageManager.FEATURE_CAMERA:PackageManager.FEATURE_CAMERA_ANY;
                if(!context.getPackageManager().hasSystemFeature(feature))return feature.equals(PackageManager.FEATURE_CAMERA_FRONT)?"No front camera available":feature.equals(PackageManager.FEATURE_CAMERA)?"No rear camera available":"No camera available";
                return context.checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED?"":"Camera access not granted";
            }
            if(task.type.equals("QR code")){if(task.code==null)return "Enroll a morning code in a fixed routine first";if(!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))return "No camera available";return context.checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED?"":"Camera access not granted";}
            if(task.type.equals("NFC")){NfcAdapter adapter=NfcAdapter.getDefaultAdapter(context);if(adapter==null)return "No NFC reader";if(!adapter.isEnabled())return "NFC is switched off";return Prefs.text(context,"nfcTag","").isEmpty()?"No morning tag enrolled":"";}
            SensorManager sensors=context.getSystemService(SensorManager.class);if(sensors==null)return "Sensor service unavailable";
            if(task.type.equals("Walk")){if(context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)!=PackageManager.PERMISSION_GRANTED)return "Physical activity access not granted";return sensors.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)==null&&sensors.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)==null?"No step sensor":"";}
            if(task.type.equals("Light"))return sensors.getDefaultSensor(Sensor.TYPE_LIGHT)==null?"No ambient-light sensor":"";
            if(task.type.equals("Spin around"))return sensors.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)==null&&sensors.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)==null?"No rotation-vector sensor":"";
            return sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)==null?"No accelerometer":"";
        }catch(RuntimeException unavailable){return "Prerequisite could not be checked";}
    }
    private static String microphone(Context context,ChallengeRoutine.Task task){
        if(!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE))return "No microphone available";
        if(context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)return "Microphone access not granted";
        if(TrackingService.running)return "Sleep tracking currently owns the recording flow";
        if(MicrophoneLease.occupied())return "An existing microphone capture is still owned or closing";
        AudioManager audio=context.getSystemService(AudioManager.class);if(audio==null)return "Audio service unavailable";
        if(audio.getMode()!=AudioManager.MODE_NORMAL)return "Microphone unavailable during a call or communication session";
        if(audio.isMicrophoneMute())return "Microphone is muted";
        if(!audio.getActiveRecordingConfigurations().isEmpty())return "Another microphone capture is active";
        return task.type.equals(PhraseRule.TYPE)?PhraseRecognition.unavailable(context,false):"";
    }
    private static String canonical(Alarm alarm){return alarm.json().toString();}
    private static JSONObject read(Context context){return read(Prefs.of(context));}
    private static JSONObject read(SharedPreferences prefs){try{String raw=prefs.getString(KEY,"");return raw.length()>262144?null:new JSONObject(raw);}catch(JSONException|RuntimeException invalid){return null;}}
    private static boolean nonce(String run){try{return UUID.fromString(run).toString().equals(run);}catch(RuntimeException invalid){return false;}}
    /** Must be called under Scheduler.class for a genuinely accepted occurrence, then write() on its publishing edit. */
    static Prepared prepare(Context context,Alarm source,String run){return prepare(context,source,run,true);}
    static Prepared prepare(Context context,Alarm source,String run,boolean carrySource){
        if(!nonce(run))throw new IllegalArgumentException("An owned alarm occurrence is required");
        Alarm concrete=Alarm.read(source.json());ChallengeRoutine configured=source.challenges();JSONObject old=read(context);
        if(configured.randomCount==0){
            // A snooze carries the already selected concrete alarm, not the saved random pool.
            if(carrySource&&old!=null&&Prefs.text(context,"alarmRun","").equals(old.optString("run"))&&canonical(source).equals(old.optString("selected"))){try{return new Prepared(concrete,new JSONObject(old.toString()).put("run",run).toString());}catch(JSONException invalid){/* Keep the concrete routine even if the optional notice is unreadable. */}}
            return new Prepared(concrete,"");
        }
        if(old!=null&&run.equals(old.optString("run"))&&canonical(source).equals(old.optString("source"))){try{Alarm selected=Alarm.read(new JSONObject(old.getString("selected")));if(!selected.routineInvalid&&selected.challenges().randomCount==0&&selected.id==source.id)return new Prepared(selected,old.toString());}catch(JSONException invalid){/* A corrupt selection never authorizes an external task. */}}
        Selection selection=select(context,configured);concrete.setRoutine(selection.routine);
        try{return new Prepared(concrete,new JSONObject().put("version",1).put("run",run).put("source",canonical(source)).put("selected",canonical(concrete)).put("notice",selection.notice).toString());}catch(JSONException impossible){throw new IllegalStateException(impossible);}
    }
    static final class Selection {final ChallengeRoutine routine;final String notice;Selection(ChallengeRoutine routine,String notice){this.routine=routine;this.notice=notice;}}
    static Selection select(Context context,ChallengeRoutine configured){return select(configured,task->unavailable(context,task));}
    /** Same sampling/fallback policy, with a read-only per-preview readiness composition. */
    static Selection select(ChallengeRoutine configured,java.util.function.Function<ChallengeRoutine.Task,String> readiness){
        if(configured.randomCount<1)throw new IllegalArgumentException("Choose a random pool first");
        List<ChallengeRoutine.Task> eligible=new ArrayList<>();List<String> excluded=new ArrayList<>();
        for(ChallengeRoutine.Task task:configured.tasks){String reason=readiness.apply(task);if(reason.isEmpty())eligible.add(task);else excluded.add(task.type+": "+reason);}
        List<ChallengeRoutine.Task> selected=new ArrayList<>();String notice;
        try{SecureRandom random=new SecureRandom();while(!eligible.isEmpty()&&selected.size()<configured.randomCount)selected.add(eligible.remove(random.nextInt(eligible.size())));}
        catch(RuntimeException unavailable){selected.clear();excluded.add("Random selection unavailable");}
        int missing=configured.randomCount-selected.size();notice="Random selection: "+selected.size()+" eligible pool entries, fixed for this occurrence.";
        if(missing>0){for(int i=0;i<missing;i++)selected.add(new ChallengeRoutine.Task("Math",1));notice+=" "+missing+" Math fallback "+(missing==1?"task replaces":"tasks replace")+" unavailable entries to keep the requested task count.";}
        if(!excluded.isEmpty())notice+=" Excluded: "+String.join("; ",excluded)+".";
        return new Selection(new ChallengeRoutine(selected),notice);
    }
    /** Map only this current occurrence's exact selected snapshot back to its configured source for schedule comparisons. */
    static Alarm configured(Context context,Alarm concrete){return configured(Prefs.of(context),concrete);}
    static Alarm configured(SharedPreferences prefs,Alarm concrete){
        try{JSONObject ledger=read(prefs);if(ledger==null||!prefs.getString("alarmRun","").equals(ledger.optString("run"))||!canonical(concrete).equals(ledger.optString("selected")))return concrete;
            Alarm source=Alarm.read(new JSONObject(ledger.getString("source")));return !source.routineInvalid&&source.id==concrete.id&&source.challenges().randomCount>0?source:concrete;
        }catch(JSONException|RuntimeException invalid){return concrete;}
    }
    static String notice(Context context,String run){JSONObject ledger=read(context);return ledger!=null&&run.equals(ledger.optString("run"))?ledger.optString("notice",""):"";}
    /** Upgrade an old/unmaterialized active wrapper before any task or proof can start. Never rerolls an existing concrete run. */
    static Alarm active(Context context,Alarm read){synchronized(Scheduler.class){
        if(read.challenges().randomCount==0)return read;
        String run=Prefs.text(context,"alarmRun","");if(!Prefs.flag(context,"ringing")||!nonce(run))return null;
        try{Alarm current=Alarm.read(new JSONObject(Prefs.text(context,"active_alarm","{}")));if(current.challenges().randomCount==0)return current;if(!canonical(current).equals(canonical(read)))return null;
            Prepared prepared=prepare(context,current,run);SharedPreferences.Editor edit=Prefs.of(context).edit().putString("active_alarm",canonical(prepared.alarm)).putInt("challengeCompleted",0);prepared.write(edit);if(edit.commit())return prepared.alarm;
        }catch(JSONException|RuntimeException unavailable){/* No child receives an unresolved wrapper. */}
        Alarm fallback=fallback(read);Prefs.put(context,"lastError","Random routine could not be saved. Complete Math instead.");return fallback;
    }}
    private static Alarm fallback(Alarm source){Alarm alarm=Alarm.read(source.json());alarm.setRoutine(new ChallengeRoutine(List.of(new ChallengeRoutine.Task("Math",1))));alarm.routineInvalid=true;return alarm;}
    private RandomChallenges(){}
}
