package org.sleepwell.app;
import java.time.*;import java.util.*;
/** Exact local-clock snooze, today only. No rollover, minute rounding or platform effects. */
final class SnoozeUntil {
 static final class State {
  final String run,revision;final int used,base,limit;final boolean shorten,enabled,current,focused;final long wall,elapsed;final ZoneId zone;
  State(String run,String revision,int used,int base,boolean shorten,int limit,boolean enabled,boolean current,boolean focused,long wall,long elapsed,ZoneId zone){if(run==null||run.isEmpty()||revision==null||zone==null||wall<0||elapsed<0)throw new IllegalArgumentException("Unreadable snooze offer");this.run=run;this.revision=revision;this.used=used;this.base=base;this.shorten=shorten;this.limit=limit;this.enabled=enabled;this.current=current;this.focused=focused;this.wall=wall;this.elapsed=elapsed;this.zone=zone;}
 }
 interface Port {State state();void schedule(long deadline);}
 final State offered;final LocalDate date;private boolean cancelled,consumed;
 SnoozeUntil(State offered){if(!offered.enabled||!AlarmInputs.canSnooze(offered.current,offered.used,offered.limit))throw new IllegalArgumentException("No snooze is available");this.offered=offered;date=Instant.ofEpochMilli(offered.wall).atZone(offered.zone).toLocalDate();}
 int initialMinute(){ZonedDateTime suggested=Instant.ofEpochMilli(offered.wall).atZone(offered.zone).plusMinutes(SnoozePolicy.minutes(offered.base,offered.shorten,offered.used));return suggested.toLocalDate().equals(date)?suggested.getHour()*60+suggested.getMinute():offered.wall==0?0:Instant.ofEpochMilli(offered.wall).atZone(offered.zone).getHour()*60+Instant.ofEpochMilli(offered.wall).atZone(offered.zone).getMinute();}
 long resolve(int minute,State now){
  if(cancelled||consumed||!now.current||!now.focused||!now.enabled||!offered.run.equals(now.run)||offered.used!=now.used||!offered.revision.equals(now.revision)||offered.base!=now.base||offered.limit!=now.limit||offered.shorten!=now.shorten||!AlarmInputs.canSnooze(true,now.used,now.limit))throw new IllegalArgumentException("This snooze offer changed. Reopen Snooze.");
  long drift;try{drift=Math.subtractExact(Math.subtractExact(now.wall,offered.wall),Math.subtractExact(now.elapsed,offered.elapsed));}catch(ArithmeticException overflow){throw new IllegalArgumentException("Phone time changed. Reopen Snooze.");}
  if(!offered.zone.equals(now.zone)||now.elapsed<offered.elapsed||drift>2000||drift< -2000||!Instant.ofEpochMilli(now.wall).atZone(now.zone).toLocalDate().equals(date))throw new IllegalArgumentException("Phone time changed. Reopen Snooze.");
  if(minute<0||minute>=1440)throw new IllegalArgumentException("Choose a valid clock time.");LocalDateTime local=date.atTime(minute/60,minute%60);List<ZoneOffset> offsets=now.zone.getRules().getValidOffsets(local);if(offsets.size()!=1)throw new IllegalArgumentException("That clock time is skipped or repeated. Choose a duration instead.");long target=local.toInstant(offsets.get(0)).toEpochMilli();if(target<=now.wall)throw new IllegalArgumentException("Choose a later time today.");long maximum=SnoozePolicy.minutes(now.base,now.shorten,now.used)*60000L;if(target-now.wall>maximum)throw new IllegalArgumentException("Choose within "+(maximum/60000)+" minutes from now.");return target;
 }
 String confirm(int minute,Port port){try{long target=resolve(minute,port.state());port.schedule(target);consumed=true;return null;}catch(RuntimeException failure){return failure.getMessage()==null?"Snooze could not be scheduled. Try again.":failure.getMessage();}}
 void cancel(){cancelled=true;}boolean completed(){return consumed;}
}
