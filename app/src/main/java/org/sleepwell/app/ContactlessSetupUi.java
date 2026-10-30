package org.sleepwell.app;

import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Presentation of actual setup state; no capability, qualification or audio decisions. */
final class ContactlessSetupUi {
    private ContactlessSetupUi() {}
    static void line(LinearLayout body) {
        View line = new View(body.getContext()); line.setBackgroundColor(0xff51445e);
        body.addView(line, new LinearLayout.LayoutParams(-1, Design.dp(body.getContext(), 1)));
    }
    static final class AccessRow {
        final LinearLayout row;private final TextView value;private final String title;
        AccessRow(LinearLayout body,String icon,String title,Runnable action){
            android.content.Context c=body.getContext();this.title=title;row=Design.row(c);row.setMinimumHeight(Design.dp(c,48));row.setBackground(Design.touch(c,android.graphics.Color.TRANSPARENT,12));
            Design.Icon image=new Design.Icon(c,icon,NightDialTokens.TEXT);image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);LinearLayout.LayoutParams glyph=new LinearLayout.LayoutParams(Design.dp(c,24),Design.dp(c,24));glyph.rightMargin=Design.dp(c,12);row.addView(image,glyph);
            row.addView(Design.text(c,title,14,500,NightDialTokens.TEXT),new LinearLayout.LayoutParams(0,-2,1));value=Design.text(c,"Checking…",12,400,Design.LILAC);value.setGravity(Gravity.END);value.setMaxWidth(Design.dp(c,160));value.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);row.addView(value);
            Design.Icon chevron=new Design.Icon(c,"chevron",Design.LILAC);chevron.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);LinearLayout.LayoutParams end=new LinearLayout.LayoutParams(Design.dp(c,18),Design.dp(c,18));end.leftMargin=Design.dp(c,8);row.addView(chevron,end);row.setOnClickListener(ignored->action.run());row.setFocusable(true);row.setAccessibilityDelegate(new View.AccessibilityDelegate(){@Override public void onInitializeAccessibilityNodeInfo(View view,android.view.accessibility.AccessibilityNodeInfo node){super.onInitializeAccessibilityNodeInfo(view,node);node.setClassName(Button.class.getName());}});body.addView(row,new LinearLayout.LayoutParams(-1,-2));
        }
        void show(String state,boolean enabled){value.setText(state);row.setContentDescription(title+" · "+state);row.setEnabled(enabled);row.setAlpha(enabled?1:.5f);}
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
