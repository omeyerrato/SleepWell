package org.sleepwell.app;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/** The three everyday report destinations, visible without opening an overflow menu. */
final class ReportNavigation {
    static void append(Activity activity, LinearLayout body, int selected,
                       Runnable night, Runnable history, Runnable trends) {
        LinearLayout rail = Design.row(activity);
        rail.setBackground(Design.shape(activity, Design.SOFT, 16));
        String[] names = {"Night", "History", "Trends"};
        Runnable[] actions = {night, history, trends};
        for (int index = 0; index < names.length; index++) {
            final int destination = index;
            FrameLayout target = new FrameLayout(activity);
            TextView label = Design.text(activity, names[index], 13,
                    index == selected ? 600 : 400,
                    index == selected ? Design.INK : Design.MUTED);
            label.setGravity(Gravity.CENTER);
            if (index == selected) label.setBackground(Design.shape(activity, Design.CREAM, 12));
            FrameLayout.LayoutParams face = new FrameLayout.LayoutParams(-1, Design.dp(activity, 36), Gravity.CENTER);
            face.setMargins(Design.dp(activity, 4), 0, Design.dp(activity, 4), 0);
            target.addView(label, face);
            target.setSelected(index == selected);
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
