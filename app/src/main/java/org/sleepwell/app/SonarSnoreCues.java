package org.sleepwell.app;

import android.content.Context;
import android.os.SystemClock;
import java.util.function.BooleanSupplier;

/** Separately admitted contactless channels. Cue/settling intervals are missing sensor evidence. */
final class SonarSnoreCues implements SonarNight.Cues {
    private final Context app;private final BooleanSupplier owner;private final boolean admittedSound,admittedVibration;
    private final SnoreCuePolicy.Budget budget=new SnoreCuePolicy.Budget();private SnoreCuePolicy policy;
    private volatile long maskFrom=Long.MAX_VALUE,maskTo,outputRevision;private volatile boolean closed;
    SonarSnoreCues(Context context,boolean analysis,BooleanSupplier owner){app=context.getApplicationContext();this.owner=owner;boolean admitted=analysis&&Prefs.flag(app,"snoreCues");admittedSound=admitted&&Prefs.flag(app,"sonarSnoreSound");admittedVibration=admitted&&Prefs.flag(app,"sonarSnoreVibration");}
    private boolean allowed(boolean sound){
        if(closed||!owner.getAsBoolean()||!Prefs.flag(app,"snoreCues")||Prefs.flag(app,"ringing"))return false;
        int mode=Prefs.number(app,"snoreCueMode",1);
        return sound?admittedSound&&Prefs.flag(app,"sonarSnoreSound")&&(mode==0||mode==2):admittedVibration&&Prefs.flag(app,"sonarSnoreVibration")&&mode!=0;
    }
    private boolean outputAllowed(boolean sound,boolean headphones,long revision){
        long now=SystemClock.elapsedRealtimeNanos();
        return revision==outputRevision&&now>=maskFrom&&now<maskTo-2000000000L&&allowed(sound)&&(!sound||Prefs.flag(app,"snoreHeadphoneSound")==headphones);
    }
    @Override public synchronized void event(long at,long duration,String category,double score){
        boolean sound=allowed(true),vibration=allowed(false);
        if((!sound&&!vibration)||DreamCues.masks(SystemClock.elapsedRealtimeNanos(),SystemClock.elapsedRealtimeNanos()+1)||!"snoring".equals(category))return;long now=SystemClock.elapsedRealtime(),age=System.currentTimeMillis()-at-duration;if(age< -2000||age>15000||masks(now*1000000L,(now+1)*1000000L))return;
        SnoreCuePolicy.Settings saved=SnoreCueOutput.settings(app);SnoreCuePolicy.Mode mode=sound?(vibration?SnoreCuePolicy.Mode.BOTH:SnoreCuePolicy.Mode.SOUND):SnoreCuePolicy.Mode.VIBRATION;
        SnoreCuePolicy.Settings requested=new SnoreCuePolicy.Settings(true,mode,saved.intensity,saved.repeats,saved.sensitivity,saved.headphoneSound);
        if(policy==null)policy=new SnoreCuePolicy(budget,Channels::new,()->cueError("Contactless cue unavailable. Tracking continues."));
        if(!Double.isFinite(score)||score<requested.threshold())return;
        long previousFrom=maskFrom,previousTo=maskTo;maskFrom=now*1000000L;
        // Headphone routing may take 1.5 seconds, followed by at most 3.5 seconds of output.
        // Install the entire output and settling gap before any asynchronous playback.
        maskTo=(now+(sound&&saved.headphoneSound?5000L:saved.repeats*900L)+2000L)*1000000L;
        if(!policy.event(category,score,now,requested)){maskFrom=previousFrom;maskTo=previousTo;}
    }
    private void cueError(String message){try{Prefs.put(app,"snoreCueError",message);}catch(RuntimeException ignored){/* Keep masking and tracking even if error storage is unavailable. */}}
    private final class Channels implements SnoreCuePolicy.Device {
        private SnoreCueOutput sound,vibration;
        @Override public void play(SnoreCuePolicy.Settings settings){
            closeOutputs();long revision=++outputRevision;
            try{
                // Independent predicates are also checked by the existing asynchronous output
                // and each repeat. Remaining vibration consent can never authorize a sound.
                if(settings.mode!=SnoreCuePolicy.Mode.VIBRATION)sound=new SnoreCueOutput(app,()->outputAllowed(true,settings.headphoneSound,revision));
                if(settings.mode!=SnoreCuePolicy.Mode.SOUND)vibration=new SnoreCueOutput(app,()->outputAllowed(false,false,revision));
            }catch(RuntimeException failure){closeOutputs();throw failure;}
            // Once dispatched, retain the mask even if another channel cannot be queued.
            try{if(sound!=null)sound.play(new SnoreCuePolicy.Settings(true,SnoreCuePolicy.Mode.SOUND,settings.intensity,settings.repeats,settings.sensitivity,settings.headphoneSound));}catch(RuntimeException failure){cueError("Contactless sound cue unavailable. Tracking continues.");}
            try{if(vibration!=null)vibration.play(new SnoreCuePolicy.Settings(true,SnoreCuePolicy.Mode.VIBRATION,settings.intensity,settings.repeats,settings.sensitivity,false));}catch(RuntimeException failure){cueError("Contactless vibration cue unavailable. Tracking continues.");}
        }
        private void closeOutputs(){SnoreCueOutput oldSound=sound,oldVibration=vibration;sound=null;vibration=null;try{if(oldSound!=null)oldSound.close();}catch(RuntimeException failure){cueError("Contactless sound cleanup unavailable.");}try{if(oldVibration!=null)oldVibration.close();}catch(RuntimeException failure){cueError("Contactless vibration cleanup unavailable.");}}
        @Override public void close(){++outputRevision;closeOutputs();}
    }
    @Override public boolean masks(long fromNanos,long toNanos){return fromNanos<maskTo&&toNanos>maskFrom;}
    @Override public synchronized void pause(){++outputRevision;if(policy!=null){policy.close();policy=null;}}
    @Override public synchronized void close(){closed=true;pause();}
}
