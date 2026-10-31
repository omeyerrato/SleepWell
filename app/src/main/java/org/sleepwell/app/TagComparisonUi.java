package org.sleepwell.app;

import android.app.Activity;
import android.widget.LinearLayout;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The exact recorded groups used by the descriptive comparison, without changing any record. */
final class TagComparisonUi {
    static void render(Activity activity, LinearLayout body, TagJobs.State state,
                       String tag, boolean with, Runnable back, Runnable retry) {
        RecordUi.label(activity, body);
        RecordUi.header(body, with ? "With tag" : "Without tag", back);
        Design.space(body, 10);
        Design.text(body, tag, 22, 600, Design.INK);
        if (state.busy) Design.text(body, "Updating saved records…", 13, 400, Design.MUTED);
        if (!state.error.isEmpty()) {
            Design.text(body, "Showing the last available comparison. " + state.error, 14, 400, Design.MUTED);
            Design.button(body, "Retry comparison", false, retry).setEnabled(!state.busy);
            Design.space(body, 10);
        }
        TagImpact impact = state.impacts.get(tag);
        if (impact == null) {
            Design.text(body, "This tag is no longer in the saved records.", 15, 400, Design.MUTED);
            return;
        }
        TagImpact.Group group = with ? impact.tagged : impact.other;
        Set<Long> included = new HashSet<>(with ? impact.taggedRecords : impact.otherRecords);
        List<RecordEntry> rows = new ArrayList<>();
        for (RecordEntry record : state.records) if (included.contains(record.id)) rows.add(record);
        rows.sort(Comparator.comparingLong((RecordEntry record) -> record.edit.start).reversed());
        Design.text(body, group.count + (group.count == 1 ? " contributing night" : " contributing nights"),
                14, 500, Design.MUTED);
        if (group.count > 0) {
            Design.space(body, 8);
            Design.text(body, RecordUi.duration(group.mean) + " average recorded duration", 18, 600, Design.INK);
        }
        Design.space(body, 18);
        if (rows.isEmpty()) Design.text(body, "No comparable nights in this group.", 15, 400, Design.MUTED);
        for (RecordEntry record : rows) {
            ZoneId zone;
            try { zone = ZoneId.of(record.zone); } catch (RuntimeException invalid) { zone = ZoneId.systemDefault(); }
            long id = record.id;
            LinearLayout row = PlanActivity.detail(body, "moon", RecordUi.date(record.edit.start, zone),
                    RecordUi.clock(activity, record.edit.start, zone) + " – " + RecordUi.clock(activity, record.edit.end, zone),
                    RecordUi.duration(record.duration()),
                    () -> activity.startActivity(RecordUi.intent(activity, RecordReportActivity.class).putExtra("record", id)));
            row.setContentDescription("Open contributing night " + id);
            Design.line(body);
        }
        Design.space(body, 14);
        Design.text(body, "Same eligibility as the comparison: naps, unfinished, future, empty and overlapping periods are excluded.",
                12, 400, Design.MUTED);
    }

    private TagComparisonUi() {}
}
