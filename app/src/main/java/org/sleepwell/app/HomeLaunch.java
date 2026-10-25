package org.sleepwell.app;

import android.content.*;

/** An explicit screen route; launcher/widget input can never request an alarm or service command. */
final class HomeLaunch {
    static final String ACTION="org.sleepwell.app.OPEN_HOME_SCREEN";
    enum Screen { ADD_ALARM, TRACKING, HISTORY, ALARMS, REPORT, RELIABILITY, WIDGETS }
    static Intent intent(Context context,Screen screen,long record){return new Intent(context,HomeLaunchActivity.class).setAction(ACTION).setData(android.net.Uri.parse("sleepwell-home:"+screen.name().toLowerCase(java.util.Locale.ROOT)+":"+Math.max(0,record))).putExtra("screen",screen.name()).putExtra("record",Math.max(0,record));}
    static Screen read(Intent intent){if(intent==null||!ACTION.equals(intent.getAction()))return null;try{return Screen.valueOf(intent.getStringExtra("screen"));}catch(Exception invalid){return null;}}
    static Intent target(Context context,Screen screen,long record){return switch(screen){case TRACKING->new Intent(context,TrackingActivity.class);case HISTORY->new Intent(context,HistoryActivity.class);case ALARMS->new Intent(context,MainActivity.class).putExtra("destination",1);case REPORT->record>0?new Intent(context,RecordReportActivity.class).putExtra("record",record):new Intent(context,HistoryActivity.class);case RELIABILITY->new Intent(context,MainActivity.class).putExtra("homeSurfacePage","reliability");case WIDGETS->new Intent(context,HomeWidgetSettingsActivity.class);case ADD_ALARM->throw new IllegalArgumentException("Add alarm requires a new saved-state draft");};}
    static Alarm draft(Context context,Store store){int maximum=store.rows("SELECT COALESCE(MAX(id),0) AS id FROM alarms").get(0).optInt("id");if(maximum==Integer.MAX_VALUE)throw new IllegalStateException("No alarm slots");return AlarmDefaultsSettings.load(context).create(maximum+1);}
}
