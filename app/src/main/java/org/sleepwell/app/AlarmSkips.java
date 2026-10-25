package org.sleepwell.app;

import android.content.Context;
import java.time.*;
import java.util.*;

/** Persist the user's intent before scheduling. A queued old FIRE is fenced by saved state. */
final class AlarmSkips {
 static final class Changed extends IllegalStateException{Changed(String message){super(message);}}
 static java.util.function.LongSupplier clock=System::currentTimeMillis;
 static java.util.function.Supplier<ZoneId> zone=ZoneId::systemDefault;
 static String scheduleKey(Alarm a){return a.hour+":"+a.minute+":"+new TreeSet<>(a.days)+":"+a.enabled+":"+(a.dateInvalid?"invalid-date:"+a.json().opt("date"):a.date==null?"":a.date)+(a.solarInvalid?":invalid-solar:"+a.json().opt("solar"):a.solar==null?"":":"+a.solar.key());}
 static String owner(Alarm a){return a.skipInvalid?"invalid:"+a.json().opt("skip"):a.skip==null?"":a.skip.nonce;}
 static boolean usable(Alarm a){return a!=null&&a.nap==null&&!a.napInvalid&&!a.skipInvalid&&!a.dateInvalid&&!a.solarInvalid;}
 static void clear(Alarm a){a.skip=null;a.skipInvalid=false;a.invalidSkip=null;a.scheduleError="";}
 static void requireSame(Alarm current,Alarm expected){if(!usable(current)||!scheduleKey(current).equals(scheduleKey(expected))||!owner(current).equals(owner(expected)))throw new Changed("This alarm changed. Reopen it before changing its occurrence.");}
 static long next(Context c,Alarm a){long now=clock.getAsLong();return CalendarAlarms.preview(c,a,a.next(Math.max(now,Prefs.of(c).getLong("lastOccurrence_"+a.id,0)),zone.get()),now,zone.get());}
 static Alarm skip(Context c,Alarm expected,long chosen){synchronized(Scheduler.class){try(Store store=Records.open(c)){Alarm saved=commit(store,()->{Alarm a=store.alarm(expected.id);requireSame(a,expected);long now=clock.getAsLong();ZoneId z=zone.get();if(!a.enabled||a.skip!=null&&CalendarAlarms.skippedDeadline(c,a,a.skip.at(z))>now||chosen<=now||chosen!=next(c,a))throw new Changed("The next alarm changed. Review its new time.");if(Prefs.flag(c,"ringing"))throw new Changed("Finish the current alarm first.");long base=a.solar==null?chosen:CalendarAlarms.skipBase(c,a,chosen,now,z);a.skip=new AlarmSkip(UUID.randomUUID().toString(),Instant.ofEpochMilli(base).atZone(z).toLocalDate(),a.hour,a.minute,a.days,z.getId(),a.solar==null?0:base,a.solar==null?"":a.solar.key());if(a.days.isEmpty())a.enabled=false;return a;});CalendarAlarms.recordSkip(c,saved,chosen);return scheduleSaved(c,store,saved);}}}
 static Alarm undo(Context c,Alarm expected){synchronized(Scheduler.class){try(Store store=Records.open(c)){Alarm saved=commit(store,()->{Alarm a=store.alarm(expected.id);requireSame(a,expected);if(a.skip==null)throw new Changed("This skip is no longer active.");if(CalendarAlarms.skippedDeadline(c,a,a.skip.at(zone.get()))<=clock.getAsLong())throw new Changed("That skipped time has passed. Choose a new alarm time.");boolean oneTime=a.days.isEmpty();clear(a);if(oneTime)a.enabled=true;return a;});return scheduleSaved(c,store,saved);}}}
 static Alarm retry(Context c,Alarm expected){synchronized(Scheduler.class){try(Store store=Records.open(c)){Alarm saved=commit(store,()->{Alarm a=store.alarm(expected.id);requireSame(a,expected);return a;});return scheduleSaved(c,store,saved);}}}
 static Alarm toggle(Context c,Store store,Alarm expected,boolean enabled){synchronized(Scheduler.class){Alarm saved=commit(store,()->{Alarm a=store.alarm(expected.id);requireSame(a,expected);clear(a);a.enabled=enabled;return a;});return scheduleSaved(c,store,saved);}}
 static Alarm edit(Context c,Store store,Alarm draft,Alarm original){return edit(c,store,draft,original,true);}
 static Alarm edit(Context c,Store store,Alarm draft,Alarm original,boolean existed){synchronized(Scheduler.class){Alarm saved=commit(store,()->{prepareEdit(store,draft,original,existed);return draft;});return scheduleSaved(c,store,saved);}}
 private static Alarm commit(Store store,java.util.function.Supplier<Alarm> transition){android.database.sqlite.SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{Alarm a=transition.get();a.scheduleError="Saved. Alarm scheduling has not been confirmed. Retry to reconcile this alarm.";store.save(a);verify(store,a);db.setTransactionSuccessful();return a;}finally{db.endTransaction();}}
 private static Alarm scheduleSaved(Context c,Store store,Alarm saved){Alarm current=store.alarm(saved.id);requireSame(current,saved);String error="";try{AlarmScheduling.port.schedule(c,current);}catch(RuntimeException failure){error="Saved. Alarm scheduling needs attention. Check Alarms & reminders access, then retry.";}
  // Never replace concurrent non-schedule edits or imported/disabled state just to update status.
  android.database.sqlite.SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{current=store.alarm(saved.id);requireSame(current,saved);current.scheduleError=error;store.save(current);verify(store,current);db.setTransactionSuccessful();return current;}finally{db.endTransaction();}
 }
 static void verify(Store store,Alarm a){Alarm saved=store.alarm(a.id);if(saved==null||!saved.json().toString().equals(a.json().toString()))throw new Changed("The alarm could not be saved.");}
 static void prepareEdit(Store store,Alarm draft,Alarm original,boolean existed){Alarm current=store.alarm(draft.id);if(!existed){if(current!=null)throw new Changed("This alarm slot changed. Reopen Alarms.");clear(draft);return;}if(current==null||!scheduleKey(current).equals(scheduleKey(original)))throw new Changed("This alarm changed. Reopen it before saving.");boolean scheduleChanged=!scheduleKey(draft).equals(scheduleKey(original));if(scheduleChanged){if(!owner(current).equals(owner(original)))throw new Changed("Its skipped occurrence changed. Reopen it before changing the schedule.");clear(draft);}else{draft.skip=current.skip;draft.skipInvalid=current.skipInvalid;draft.invalidSkip=current.invalidSkip;draft.scheduleError=current.scheduleError;}if(draft.skipInvalid)throw new Changed("The skipped occurrence needs review. Change its schedule to clear it.");}
 static boolean eligible(Alarm a,long deadline,long now,long last,ZoneId zone){return usable(a)&&a.enabled&&deadline>0&&deadline>last&&a.occurs(deadline,zone)&&(a.skip==null||!a.skip.excludes(deadline,zone))&&now>=deadline-a.smartWindow*60000L;}
 private AlarmSkips(){}
}
