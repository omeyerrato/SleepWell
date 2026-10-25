package org.sleepwell.app;

/** Minimal saved-state projection. No sleep notes, tags, recordings or inferred stages. */
final class HomeWidgetSummary {
    final String alarmTitle,alarmTime,alarmDetail,reportTitle,reportDuration,reportDetail;final long record;final boolean alarmNeedsAttention;
    HomeWidgetSummary(String alarmTitle,String alarmTime,String alarmDetail,boolean attention,String reportTitle,String reportDuration,String reportDetail,long record){this.alarmTitle=alarmTitle;this.alarmTime=alarmTime;this.alarmDetail=alarmDetail;this.alarmNeedsAttention=attention;this.reportTitle=reportTitle;this.reportDuration=reportDuration;this.reportDetail=reportDetail;this.record=record;}
    static String duration(long millis){long minutes=Math.max(0,millis)/60000;return minutes/60+" h "+minutes%60+" min";}
    static HomeWidgetSummary error(){return new HomeWidgetSummary("Alarms unavailable","Open SleepWell","Refresh or check saved alarms",false,"Report unavailable","Open history","Refresh or check saved records",0);}
}
