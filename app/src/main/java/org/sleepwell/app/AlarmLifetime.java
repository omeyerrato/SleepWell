package org.sleepwell.app;

/** No catch-up/replay: sound bursts follow this occurrence's monotonic timeline, not worker arrival. */
final class AlarmLifetime {
    interface Owner {boolean current(String run);boolean backupIfCurrent(String run,boolean backup);boolean timeoutIfCurrent(String run);}
    enum Phase {PRIMARY,BACKUP,EXPIRED}
    private final AlarmTiming timing;private final String run;private final long after;private final boolean quiet;private final Owner owner;private boolean closed;private Phase last;
    AlarmLifetime(Alarm alarm,String run,Owner owner){if(run==null||run.isEmpty()||owner==null)throw new IllegalArgumentException("Missing alarm owner");this.run=run;this.owner=owner;quiet=alarm.id<=0;timing=alarm.timingInvalid?AlarmTiming.DEFAULT:alarm.timing;after=Math.max(0,Math.min(15,alarm.backupMinutes))*60000L;}
    Phase phase(long elapsed,boolean forced){elapsed=Math.max(0,elapsed);if(quiet)return Phase.PRIMARY;if(timing.timeoutMinutes>0&&elapsed>=timing.timeoutMinutes*60000L)return Phase.EXPIRED;if(!forced)return Phase.PRIMARY;if(timing.backupSeconds==0)return Phase.BACKUP;if(elapsed<after)return Phase.PRIMARY;long relative=elapsed-after,duration=timing.backupSeconds*1000L,cycle=duration+timing.backupGapMinutes*60000L;return relative/cycle<=timing.backupRepeats&&relative%cycle<duration?Phase.BACKUP:Phase.PRIMARY;}
    boolean tick(long elapsed,boolean forced){if(closed)return false;if(!owner.current(run)){closed=true;return false;}Phase phase=phase(elapsed,forced);if(phase==Phase.EXPIRED){closed=true;owner.timeoutIfCurrent(run);return false;}if(phase!=last){if(!owner.backupIfCurrent(run,phase==Phase.BACKUP)){closed=true;return false;}last=phase;}return true;}
    void close(){closed=true;}
}
