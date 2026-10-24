package org.sleepwell.app;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.provider.CalendarContract;
import java.time.*;
import java.util.*;

/** Calendar provider reads belong to the settings/sync worker, never alarm delivery. */
final class CalendarAccess {
    static final class Source {final long id;final String name;Source(long id,String name){this.id=id;this.name=name;}}
    interface Port {boolean allowed();List<Source> sources(CancellationSignal cancel);CalendarPolicy.Snapshot snapshot(CalendarPolicy.Rules rules,long now,ZoneId zone,CancellationSignal cancel);}
    interface Factory {Port open(Context context);}
    static Factory factory=CalendarAccess::platform;
    static Port platform(Context c){Context app=c.getApplicationContext();return new Port(){
        public boolean allowed(){return app.checkSelfPermission(Manifest.permission.READ_CALENDAR)==PackageManager.PERMISSION_GRANTED;}
        public List<Source> sources(CancellationSignal cancel){if(!allowed())throw new SecurityException("Allow calendar access first");List<Source> out=new ArrayList<>();
            try(Cursor rows=app.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI,new String[]{CalendarContract.Calendars._ID,CalendarContract.Calendars.CALENDAR_DISPLAY_NAME},CalendarContract.Calendars.VISIBLE+"=1",null,CalendarContract.Calendars.CALENDAR_DISPLAY_NAME+" ASC",cancel)){if(rows==null)throw new IllegalStateException("Calendar provider unavailable");while(rows.moveToNext()){if(out.size()>=128)throw new IllegalStateException("Too many calendars; choose visible calendars in your calendar app");out.add(new Source(rows.getLong(0),rows.isNull(1)?"Calendar "+rows.getLong(0):rows.getString(1)));}}return List.copyOf(out);
        }
        public CalendarPolicy.Snapshot snapshot(CalendarPolicy.Rules rules,long now,ZoneId zone,CancellationSignal cancel){if(!allowed())throw new SecurityException("Calendar permission is unavailable");LocalDate today=Instant.ofEpochMilli(now).atZone(zone).toLocalDate();long from=today.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli(),through=today.plusDays(35).atStartOfDay(zone).toInstant().toEpochMilli();Set<Long> selected=new TreeSet<>(rules.holidays);selected.addAll(rules.meetings);List<CalendarPolicy.Event> out=new ArrayList<>();
            if(!selected.isEmpty()){Uri.Builder uri=CalendarContract.Instances.CONTENT_URI.buildUpon();ContentUris.appendId(uri,from);ContentUris.appendId(uri,through);String selection=CalendarContract.Instances.CALENDAR_ID+" IN ("+String.join(",",Collections.nCopies(selected.size(),"?"))+")";String[] args=selected.stream().map(String::valueOf).toArray(String[]::new);
                String[] projection={CalendarContract.Instances.CALENDAR_ID,CalendarContract.Instances.BEGIN,CalendarContract.Instances.END,CalendarContract.Instances.ALL_DAY,CalendarContract.Instances.STATUS,CalendarContract.Instances.AVAILABILITY,CalendarContract.Instances.SELF_ATTENDEE_STATUS};
                try(Cursor rows=app.getContentResolver().query(uri.build(),projection,selection,args,CalendarContract.Instances.BEGIN+" ASC",cancel)){if(rows==null)throw new IllegalStateException("Calendar provider unavailable");while(rows.moveToNext()){if(rows.getInt(4)==CalendarContract.Events.STATUS_CANCELED||rows.getInt(6)==CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED)continue;boolean allDay=rows.getInt(3)!=0;if(!allDay&&rows.getInt(5)==CalendarContract.Events.AVAILABILITY_FREE)continue;if(out.size()>=4096)throw new IllegalStateException("Calendar range is too busy to plan safely");long begin=rows.getLong(1),end=rows.getLong(2);if(begin>=0&&end>begin)out.add(new CalendarPolicy.Event(rows.getLong(0),begin,end,allDay));}}
            }return new CalendarPolicy.Snapshot(from,through,now,zone,rules.revision,out);
        }
    };}
    private CalendarAccess(){}
}
