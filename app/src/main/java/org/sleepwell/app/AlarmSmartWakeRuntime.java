package org.sleepwell.app;

import java.util.*;

/** Per-alarm movement evidence only; never changes a saved reading or the OS alarm deadline. */
final class AlarmSmartWakeRuntime {
    private static final long SECOND=1000000000L;
    private final Map<Integer,Evidence> alarms=new HashMap<>();
    private long firstCapture,lastCapture,lastObserved;
    private boolean captureGap;

    /** Independent quality gate: recording retains its existing raw sensor/storage policy. */
    void capture(long captured,long delivered,double raw){
        if(captured<=0||captured>delivered+SECOND/20||delivered-captured>2*SECOND||!Double.isFinite(raw)||raw<0){gap();return;}
        if(lastObserved>0&&(captured<=lastObserved||captured-lastObserved>2*SECOND))gap();
        lastObserved=captured;
        if(firstCapture==0)firstCapture=captured;
        lastCapture=captured;
    }
    void gap(){captureGap=true;alarms.clear();}
    /** Ordinary/paired early wake is deliberately stricter than the historical epoch counter:
     * 45+ seconds of fresh captures in a 45–75 second current interval, no >2 second capture gaps.
     * FIFO callers retain their existing 45-second/100-sample/freshness admission in the service. */
    boolean complete(long from,long to,long elapsed){
        return !captureGap&&to>=from&&to-from>=45000&&to-from<=75000
                &&firstCapture>0&&lastCapture-firstCapture>=45*SECOND
                &&elapsed>=lastCapture&&elapsed-lastCapture<=2*SECOND;
    }
    void nextCaptureWindow(){firstCapture=lastCapture=0;captureGap=false;}
    void reset(){alarms.clear();lastObserved=0;nextCaptureWindow();}
    void retain(List<Alarm> current){Set<Integer> ids=new HashSet<>();for(Alarm alarm:current)if(alarm.enabled&&alarm.nap==null&&!alarm.smartWakeInvalid&&alarm.smartWindow>0)ids.add(alarm.id);alarms.keySet().retainAll(ids);}

    boolean measured(Alarm alarm,MotionProfile tracking,long deadline,boolean inheritedGoal,boolean goalReached,
                     long from,long to,boolean complete,double raw,int samples,long now){
        if(alarm.nap!=null||alarm.smartWakeInvalid||alarm.smartWake==null||!alarm.enabled||alarm.smartWindow<=0||deadline<=0
                ||!complete||from<0||to<=from||samples<20||!Double.isFinite(raw)||raw<0){alarms.remove(alarm.id);return false;}
        AlarmSmartWake options=alarm.smartWake;
        double gain=options.gain(tracking);
        if(!Double.isFinite(gain)||gain<=0){alarms.remove(alarm.id);return false;}
        boolean needsGoal=options.requiresGoal(inheritedGoal);
        String identity=options.config()+"|"+tracking.config()+"|"+Double.doubleToLongBits(gain)+"|"+needsGoal+"|"+alarm.smartWindow;
        Evidence evidence=alarms.get(alarm.id);
        if(evidence==null||evidence.deadline!=deadline||!evidence.identity.equals(identity)){
            evidence=new Evidence(identity,deadline,gain);alarms.put(alarm.id,evidence);
        }
        // Duplicate/overlapping callbacks are not new evidence. A gap starts a fresh sequence.
        if(evidence.to>0&&from<evidence.to){alarms.remove(alarm.id);return false;}
        if(evidence.to>0&&from-evidence.to>2000)evidence.active=0;
        double normalized=raw*evidence.gain;
        evidence.active=Double.isFinite(normalized)&&normalized>=.12?Math.min(5,evidence.active+1):0;
        evidence.to=to;
        return (!needsGoal||goalReached)&&SleepMath.smartWake(now,deadline,alarm.smartWindow,normalized,evidence.active,samples);
    }
    private static final class Evidence {
        final String identity;final long deadline;final double gain;long to;int active;
        Evidence(String identity,long deadline,double gain){this.identity=identity;this.deadline=deadline;this.gain=gain;}
    }
}
