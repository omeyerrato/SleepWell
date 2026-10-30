package org.sleepwell.app;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

/** A report projection of existing Health Connect evidence, without changing the source or local edits. */
final class ProviderSleepReport {
    static void append(Activity host, LinearLayout body, HealthSleep source,
                       long record, long start, long end, ZoneId recordZone, boolean edited) {
        LinearLayout heading = Design.row(host);
        heading.addView(Design.text(host, "Sleep stages", 16, 600, Design.INK),
                new LinearLayout.LayoutParams(0, -2, 1));
        TextView details = Design.text(host, "Source details", 12, 500, Design.BROWN);
        details.setGravity(Gravity.CENTER);
        details.setPadding(Design.dp(host, 8), 0, Design.dp(host, 8), 0);
        details.setMinimumHeight(Design.dp(host, 48));
        details.setBackground(Design.touch(host, android.graphics.Color.TRANSPARENT, 14));
        details.setContentDescription("Open original Health Connect sleep details");
        details.setOnClickListener(v -> host.startActivity(RecordUi.intent(host, HealthSleepSourceActivity.class)
                .putExtra("record", record)));
        heading.addView(details);
        body.addView(heading);

        Map<String, Integer> kinds = new LinkedHashMap<>();
        for (HealthSleep.Stage stage : source.stages) {
            if (stage.start >= end || stage.end <= start) continue;
            String name = stage.awake() ? "Awake" : ProviderStageChart.name(stage.kind);
            kinds.putIfAbsent(name, stage.kind);
        }
        if (kinds.isEmpty()) {
            Design.text(body, source.stages.isEmpty() ? "No stages supplied by the source app."
                    : "No source stages within this period.", 14, 400, Design.MUTED);
        } else {
            // Offsets alone do not identify the DST/timezone rules between two source endpoints.
            // Use UTC when the source offsets differ instead of silently applying the end offset to the start.
            boolean offsetChange = source.startOffset != null && source.endOffset != null
                    && !source.startOffset.equals(source.endOffset);
            ZoneId zone = offsetChange ? ZoneOffset.UTC : recordZone;
            body.addView(new ProviderStageChart(host, source, start, end, zone),
                    new LinearLayout.LayoutParams(-1, Design.dp(host, 74 * Math.max(1, Design.fontScale(host)))));
            HorizontalScrollView scroll = new HorizontalScrollView(host);
            scroll.setHorizontalScrollBarEnabled(false);
            LinearLayout legend = Design.row(host);
            java.util.List<Map.Entry<String, Integer>> orderedKinds = new java.util.ArrayList<>(kinds.entrySet());
            orderedKinds.sort(java.util.Comparator.comparingInt(kind -> legendOrder(kind.getValue())));
            for (Map.Entry<String, Integer> kind : orderedKinds) {
                LinearLayout item = Design.row(host);
                View mark = new View(host);
                mark.setBackground(Design.shape(host, ProviderStageChart.color(kind.getValue()), 6));
                LinearLayout.LayoutParams swatch = new LinearLayout.LayoutParams(Design.dp(host, 10), Design.dp(host, 10));
                swatch.rightMargin = Design.dp(host, 6);
                item.addView(mark, swatch);
                item.addView(Design.text(host, kind.getKey(), 12, 400, Design.MUTED));
                item.setPadding(0, Design.dp(host, 4), Design.dp(host, 16), Design.dp(host, 4));
                legend.addView(item);
            }
            scroll.addView(legend);
            body.addView(scroll, new LinearLayout.LayoutParams(-1, -2));
            if (offsetChange) Design.text(body, "Timeline in UTC · original offsets in Source details", 12, 400, Design.MUTED);
        }
        Design.space(body, 4);
        Design.text(body, "Reported by " + source.origin, 12, 400, Design.MUTED);
        if (edited) Design.text(body, "Original source intervals · local edits do not relabel stages", 12, 400, Design.MUTED);
        Design.space(body, 12);
    }

    private static int legendOrder(int kind) { return kind == 1 || kind == 3 || kind == 7 ? 0 : kind == 4 ? 1 : kind == 5 ? 2 : kind == 6 ? 3 : kind == 2 ? 4 : 5; }

    private ProviderSleepReport() {}
}
