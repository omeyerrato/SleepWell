package org.sleepwell.app;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.function.Consumer;

/** The same accessible multi-choice cards for a Math task and its QR fallback. */
final class MathOperatorGrid {
    static void add(LinearLayout body, MathSettings settings,
                    Consumer<MathSettings> changed, Runnable rejected) {
        Context context = body.getContext();
        MathSettings.Operator[] operators = MathSettings.Operator.values();
        for (int index = 0; index < 2; index++) {
            LinearLayout row = Design.row(context);
            row.setGravity(Gravity.TOP);
            row.setBaselineAligned(false);
            for (int column = 0; column < 2; column++) {
                MathSettings.Operator operator = operators[index * 2 + column];
                boolean checked = settings.permits(operator);
                LinearLayout tile = Design.row(context);
                tile.setBaselineAligned(false);
                Design.pad(tile, 10, 10);
                tile.setMinimumHeight(Design.dp(context, 56));
                tile.setBackground(Design.outline(context,
                    checked ? Design.SOFT : Color.TRANSPARENT,
                    checked ? Design.BROWN : Design.LINE, 14));

                TextView symbol = Design.text(context, operator.symbol, 22, 600, Design.BROWN);
                symbol.setGravity(Gravity.CENTER);
                tile.addView(symbol, new LinearLayout.LayoutParams(Design.dp(context, 26), -2));
                TextView label = Design.text(context, operator.label, 12, 500, Design.INK);
                label.setPadding(Design.dp(context, 6), 0, Design.dp(context, 4), 0);
                tile.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
                tile.addView(new Design.Icon(context, "check",
                    checked ? Design.BROWN : Color.TRANSPARENT),
                    new LinearLayout.LayoutParams(Design.dp(context, 16), Design.dp(context, 16)));

                tile.setContentDescription(operator.label);
                tile.setSelected(checked);
                tile.setFocusable(true);
                tile.setAccessibilityDelegate(new View.AccessibilityDelegate() {
                    @Override public void onInitializeAccessibilityNodeInfo(
                            View host, AccessibilityNodeInfo info) {
                        super.onInitializeAccessibilityNodeInfo(host, info);
                        info.setClassName(CheckBox.class.getName());
                        info.setCheckable(true);
                        info.setChecked(checked);
                    }
                });
                tile.setOnClickListener(view -> {
                    MathSettings next;
                    try {
                        next = settings.toggle(operator, !checked);
                    } catch (IllegalArgumentException invalid) {
                        rejected.run();
                        return;
                    }
                    changed.accept(next);
                });
                LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(0, -2, 1);
                size.setMargins(Design.dp(context, column == 0 ? 0 : 4), 0,
                    Design.dp(context, column == 0 ? 4 : 0), Design.dp(context, 8));
                row.addView(tile, size);
            }
            body.addView(row);
        }
    }

    private MathOperatorGrid() {}
}
