package org.sleepwell.app;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Sound settings only: a chevron opens a page; a down arrow opens a choice list. */
final class SoundSettingsUi {
    static void header(Activity activity, LinearLayout body, String title, String parent, Runnable back) {
        LinearLayout row = RecordUi.header(body, title, back);
        row.setMinimumHeight(Design.dp(activity, 52));
        row.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        if (row.getChildCount() > 0 && parent != null) row.getChildAt(0).setContentDescription("Back to " + parent);
        if (parent != null) {
            TextView context = Design.text(body, parent, 12, 400, Design.MUTED);
            context.setPadding(Design.dp(activity, 48), 0, 0, Design.dp(activity, 8));
        }
        Design.space(body, 12);
    }
    static View navigation(LinearLayout body, String icon, String title, String summary, Runnable action) {
        return row(body, icon, title, summary, "chevron", action);
    }
    static View choice(LinearLayout body, String icon, String title, String summary, Runnable action) {
        return row(body, icon, title, summary, "down", action);
    }
    private static View row(LinearLayout body, String icon, String title, String summary, String affordance, Runnable action) {
        android.content.Context c = body.getContext();
        LinearLayout row = Design.row(c); row.setMinimumHeight(Design.dp(c, 72));
        row.setPadding(0, Design.dp(c, 10), 0, Design.dp(c, 10));
        if (icon != null) {
            LinearLayout.LayoutParams slot = new LinearLayout.LayoutParams(Design.dp(c, 26), Design.dp(c, 26));
            slot.rightMargin = Design.dp(c, 16); row.addView(new Design.Icon(c, icon, Design.BROWN), slot);
        }
        LinearLayout copy = Design.col(c); Design.text(copy, title, 16, 500, Design.INK);
        if (summary != null && !summary.isEmpty()) { Design.space(copy, 4); Design.text(copy, summary, 13, 400, Design.MUTED); }
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout.LayoutParams arrow = new LinearLayout.LayoutParams(Design.dp(c, 22), Design.dp(c, 22));
        arrow.leftMargin = Design.dp(c, 12); row.addView(new Design.Icon(c, affordance, Design.INK), arrow);
        row.setBackground(Design.touch(c, Color.TRANSPARENT, 12)); row.setFocusable(true); row.setContentDescription(title);
        row.setOnClickListener(v -> { InteractionFeedback.action(v); action.run(); });
        body.addView(row, new LinearLayout.LayoutParams(-1, -2)); return row;
    }
    static TextView status(LinearLayout body, String message, boolean error) {
        TextView text = Design.text(body, message, 13, 500, error ? NightDialTokens.NEGATIVE : Design.BROWN);
        text.setVisibility(message.isEmpty() ? View.GONE : View.VISIBLE);
        text.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE); return text;
    }
    private SoundSettingsUi() {}
}
