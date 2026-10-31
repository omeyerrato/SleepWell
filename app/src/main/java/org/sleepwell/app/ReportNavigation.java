package org.sleepwell.app;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/** The three everyday report destinations, visible without opening an overflow menu. */
final class ReportNavigation {
    /** Replace peer report sections; a record's explicit Back still returns to its caller. */
    static void open(Activity activity,int section){Intent intent=section==0?RecordUi.intent(activity,RecordReportActivity.class).putExtra("reportRoot",true):RecordUi.intent(activity,HistoryActivity.class).putExtra("trends",section==2);activity.startActivity(intent);activity.finish();}
    static void append(Activity activity, LinearLayout body, int selected,
                       Runnable night, Runnable history, Runnable trends) {
        LinearLayout rail = Design.row(activity);
        rail.setBackground(Design.shape(activity, NightDialTokens.SURFACE, 16));
        String[] names = {"Night", "History", "Trends"};
        Runnable[] actions = {night, history, trends};
        for (int index = 0; index < names.length; index++) {
            final int destination = index;
            FrameLayout target = new FrameLayout(activity);
            TextView label = Design.text(activity, names[index], 13,
                    index == selected ? 600 : 400,
                    index == selected ? NightDialTokens.ACTION_INK : NightDialTokens.SECONDARY);
            label.setGravity(Gravity.CENTER);
            if (index == selected) label.setBackground(Design.shape(activity, NightDialTokens.ACCENT, 12));
            FrameLayout.LayoutParams face = new FrameLayout.LayoutParams(-1, Design.dp(activity, 36), Gravity.CENTER);
            face.setMargins(Design.dp(activity, 4), 0, Design.dp(activity, 4), 0);
            target.addView(label, face);
            target.setSelected(index == selected);
            SleepPicker.radio(target,index==selected);
            target.setFocusable(true);
            target.setContentDescription(names[index]);
            target.setOnClickListener(view -> {
                if (destination == selected) return;
                InteractionFeedback.selection(view);
                actions[destination].run();
            });
            rail.addView(target, new LinearLayout.LayoutParams(0, Design.dp(activity, 48), 1));
        }
        body.addView(rail);
        Design.space(body, 8);
    }

    private ReportNavigation() {}
}
