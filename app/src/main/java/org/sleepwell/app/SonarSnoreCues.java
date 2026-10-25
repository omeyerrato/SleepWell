package org.sleepwell.app;

import android.content.Context;
import android.os.SystemClock;
import java.util.function.BooleanSupplier;

/** Explicit vibration-only contactless consent. Cue/settling intervals are missing sensor evidence. */
final class SonarSnoreCues implements SonarNight.Cues {
    private final Context app;private final BooleanSupplier owner;private final boolean admitted;
    private final SnoreCuePolicy.Budget budget=new SnoreCuePolicy.Budget();private SnoreCuePolicy policy;
    private volatile long maskFrom=Long.MAX_VALUE,maskTo;private volatile boolean closed;
    SonarSnoreCues(Context context,boolean analysis,BooleanSupplier owner){app=context.getApplicationContext();this.owner=owner;admitted=analysis&&Prefs.flag(app,"sonarSnoreVibration")&&Prefs.flag(app,"snoreCues");}
    private boolean allowed(){return admitted&&!closed&&owner.getAsBoolean()&&Prefs.flag(app,"sonarSnoreVibration")&&Prefs.flag(app,"snoreCues")&&!Prefs.flag(app,"ringing");}
    @Override public synchronized void event(long at,long duration,String category,double score){
        if(!allowed()||DreamCues.masks(SystemClock.elapsedRealtimeNanos(),SystemClock.elapsedRealtimeNanos()+1)||!"snoring".equals(category))return;long now=SystemClock.elapsedRealtime(),age=System.currentTimeMillis()-at-duration;if(age< -2000||age>15000||masks(now*1000000L,(now+1)*1000000L))return;
        SnoreCuePolicy.Settings saved=SnoreCueOutput.settings(app);SnoreCuePolicy.Settings vibration=new SnoreCuePolicy.Settings(true,SnoreCuePolicy.Mode.VIBRATION,saved.intensity,saved.repeats,saved.sensitivity,false);
        if(policy==null)policy=new SnoreCuePolicy(budget,()->new SnoreCueOutput(app,this::allowed),()->Prefs.put(app,"snoreCueError","Contactless vibration cue unavailable. Tracking continues."));
        if(!Double.isFinite(score)||score<vibration.threshold())return;
        long previousFrom=maskFrom,previousTo=maskTo;maskFrom=now*1000000L;maskTo=(now+saved.repeats*900L+2000L)*1000000L;
        if(!policy.event(category,score,now,vibration)){maskFrom=previousFrom;maskTo=previousTo;}
    }
    @Override public boolean masks(long fromNanos,long toNanos){return fromNanos<maskTo&&toNanos>maskFrom;}
    @Override public synchronized void pause(){if(policy!=null){policy.close();policy=null;}}
    @Override public synchronized void close(){closed=true;pause();}
}
