package org.sleepwell.app;

import android.app.PendingIntent;
import android.content.*;
import android.widget.RemoteViews;

final class HomeWidgetViews {
    static PendingIntent open(Context context,HomeLaunch.Screen screen,long record){return PendingIntent.getActivity(context,screen.ordinal(),HomeLaunch.intent(context,screen,record),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    static RemoteViews render(Context context,HomeWidgetSummary data){RemoteViews views=new RemoteViews(context.getPackageName(),R.layout.home_widget);views.setTextViewText(R.id.widget_alarm_title,data.alarmTitle);views.setTextViewText(R.id.widget_alarm_value,data.alarmTime);views.setTextViewText(R.id.widget_alarm_detail,data.alarmDetail);views.setTextViewText(R.id.widget_report_title,data.reportTitle);views.setTextViewText(R.id.widget_report_value,data.reportDuration);views.setTextViewText(R.id.widget_report_detail,data.reportDetail);views.setContentDescription(R.id.widget_alarm,data.alarmTitle+", "+data.alarmTime+", "+data.alarmDetail);views.setContentDescription(R.id.widget_report,data.reportTitle+", "+data.reportDuration+", "+data.reportDetail);views.setOnClickPendingIntent(R.id.widget_alarm,open(context,data.alarmNeedsAttention?HomeLaunch.Screen.RELIABILITY:HomeLaunch.Screen.ALARMS,0));views.setOnClickPendingIntent(R.id.widget_report,open(context,data.record>0?HomeLaunch.Screen.REPORT:HomeLaunch.Screen.HISTORY,data.record));views.setOnClickPendingIntent(R.id.widget_add,open(context,HomeLaunch.Screen.ADD_ALARM,0));views.setOnClickPendingIntent(R.id.widget_settings,open(context,HomeLaunch.Screen.WIDGETS,0));views.setOnClickPendingIntent(R.id.widget_refresh,PendingIntent.getBroadcast(context,7335,new Intent(context,HomeWidgetProvider.class).setAction(HomeWidgetProvider.REFRESH),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));return views;}
}
