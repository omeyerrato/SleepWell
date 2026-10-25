package org.sleepwell.app;
import android.app.Activity;import android.content.Intent;import android.provider.CalendarContract;
/** Calendar-app handoff only. No provider write, record URI, account or caller-supplied Intent. */
final class CalendarEventExport {
    interface Port {void open(Activity activity,Intent draft);}
    static Port port=(activity,draft)->activity.startActivity(draft);
    static CalendarEventDraft draft(RecordEntry entry){if(entry==null||entry.rawEnd<=0)throw new IllegalArgumentException("This record is unavailable.");return new CalendarEventDraft(entry.edit.start,entry.edit.end,entry.duration(),entry.zone().getId(),entry.edit.note);}
    static Intent intent(CalendarEventDraft draft,boolean notes){return new Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI).putExtra(CalendarContract.Events.TITLE,"Sleep period").putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,draft.start).putExtra(CalendarContract.EXTRA_EVENT_END_TIME,draft.end).putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY,false).putExtra(CalendarContract.Events.EVENT_TIMEZONE,draft.zone).putExtra(CalendarContract.Events.AVAILABILITY,CalendarContract.Events.AVAILABILITY_FREE).putExtra(CalendarContract.Events.DESCRIPTION,draft.description(notes));}
    private CalendarEventExport(){}
}
