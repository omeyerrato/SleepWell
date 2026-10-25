package org.sleepwell.app;

import android.content.*;

final class RecordInsightSettings {
    interface Factory {SharedPreferences open(Context context);}
    interface Cutoff {int read(Context context);}
    static Cutoff cutoff=context->Math.max(0,Math.min(1439,Prefs.number(context,"statsCutoff",0)));
    static Factory factory=context->context.getSharedPreferences("record_insight_view",0);
    static int days(SharedPreferences prefs){int days=prefs.getInt("days",30);if(!RecordInsights.WINDOWS.contains(days))throw new IllegalArgumentException("Saved insight range needs attention");return days;}
    static void save(SharedPreferences prefs,int days){if(!RecordInsights.WINDOWS.contains(days))throw new IllegalArgumentException("Invalid insight range");if(!prefs.edit().putInt("days",days).commit())throw new IllegalStateException("Insight range could not be saved");}
}
