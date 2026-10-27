package org.sleepwell.app;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Presentation of actual setup state; no capability, qualification or audio decisions. */
final class ContactlessSetupUi {
    private ContactlessSetupUi() {}
    static void line(LinearLayout body) {
        View line = new View(body.getContext()); line.setBackgroundColor(0xff51445e);
        body.addView(line, new LinearLayout.LayoutParams(-1, Design.dp(body.getContext(), 1)));
    }
    static final class ResultCard {
        final TextView detail;
        private final TextView title;
        private final View check;
        ResultCard(LinearLayout body) {
            android.content.Context c = body.getContext(); LinearLayout row = Design.row(c); Design.pad(row, 12, 12);
            row.setBackground(Design.outline(c, 0xff2c2536, 0xff5e506d, 14));
            check = new Design.Icon(c, "check", Design.PLUM);
            check.setBackground(Design.shape(c, 0xffb9e08e, 20)); check.setPadding(Design.dp(c, 5), Design.dp(c, 5), Design.dp(c, 5), Design.dp(c, 5));
            check.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            LinearLayout.LayoutParams icon = new LinearLayout.LayoutParams(Design.dp(c, 30), Design.dp(c, 30)); icon.rightMargin = Design.dp(c, 12); row.addView(check, icon);
            LinearLayout words = Design.col(c); title = Design.text(words, "", 15, 600, Design.CREAM); Design.space(words, 4);
            detail = Design.text(words, "", 12, 400, Design.LILAC); detail.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
            row.addView(words, new LinearLayout.LayoutParams(0, -2, 1)); body.addView(row, new LinearLayout.LayoutParams(-1, -2));
        }
        void show(boolean qualified, boolean testing, boolean saving, boolean issue) {
            check.setVisibility(qualified && !testing && !saving ? View.VISIBLE : View.GONE);
            title.setText(saving ? "Saving settings" : testing ? "Testing signal" : qualified ? "Signal ready" : issue ? "Review setup" : "Before you test");
        }
    }
}
