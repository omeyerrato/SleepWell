package org.sleepwell.app;
import java.util.*;
/** Bounded standard request fields. External requests never carry alarm identity or run authority. */
final class AlarmRequest {
    final Integer hour,minute;final String message;final Set<Integer> days;final Boolean vibrate;
    AlarmRequest(Integer hour,Integer minute,String message,List<Integer> calendarDays,Boolean vibrate){if(hour!=null&&(hour<0||hour>23)||minute!=null&&(minute<0||minute>59)||message!=null&&message.length()>80||calendarDays!=null&&calendarDays.size()>7)throw new IllegalArgumentException("Review the requested alarm time and days.");this.hour=hour;this.minute=hour!=null&&minute==null?0:minute;this.message=message==null?null:message.replaceAll("[\\p{Cc}\\p{Cf}\\p{Zl}\\p{Zp}]"," ").trim().replaceAll("\\s+"," ");Set<Integer> mapped=new HashSet<>();if(calendarDays!=null)for(Integer day:calendarDays){if(day==null||day<1||day>7)throw new IllegalArgumentException("Use valid repeating weekdays.");mapped.add((day+5)%7+1);}days=Set.copyOf(mapped);this.vibrate=vibrate;}
    Alarm draft(Alarm template){Alarm out=Alarm.read(template.json());if(hour!=null)out.hour=hour;if(minute!=null)out.minute=minute;out.days.clear();out.days.addAll(days);out.date=null;out.dateInvalid=false;AlarmSkips.clear(out);if(message!=null)out.label=message.isEmpty()?"Alarm":message;if(vibrate!=null)out.vibrate=vibrate;out.enabled=true;return out;}
}
