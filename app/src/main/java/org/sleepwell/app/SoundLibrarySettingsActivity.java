package org.sleepwell.app;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

/** Draft choices plus a retained immutable retention operation. */
public final class SoundLibrarySettingsActivity extends Activity implements SoundRetentionJob.Observer {
    private SoundRetentionJob job;
    private int days;
    private boolean favorites, details;
    private SoundRetentionJob.State operation;
    private Dialog picker;
    private Bundle restoredPicker;
    private static final int[] DAYS = {1, 3, 7, 14, 30, 0};
    private static final String[] LABELS = {"1 day", "3 days", "7 days", "14 days", "30 days", "Keep all"};
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved); RecordUi.review(this);
        Object retained = getLastNonConfigurationInstance();
        job = retained instanceof SoundRetentionJob ? (SoundRetentionJob) retained
                : new SoundRetentionJob(getApplicationContext(), Records.factory);
        days = saved == null ? Prefs.number(this, "audioRetentionDays", 7) : saved.getInt("days", 7);
        favorites = saved == null ? Prefs.of(this).getBoolean("keepAudioFavorites", true) : saved.getBoolean("favorites", true);
        details = saved == null ? "details".equals(getIntent().getStringExtra("soundSettingsPage")) : saved.getBoolean("details");
        restoredPicker = saved == null ? null : saved.getBundle("picker");
        if (android.os.Build.VERSION.SDK_INT >= 33)
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::back);
        job.observe(this);
    }
    @Override public Object onRetainNonConfigurationInstance() { return job; }
    @Override public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out); out.putInt("days", days); out.putBoolean("favorites", favorites); out.putBoolean("details", details);
        if (picker instanceof SleepPicker.StateDialog && picker.isShowing()) out.putBundle("picker", picker.onSaveInstanceState());
    }
    @Override public void changed(SoundRetentionJob.State state) {
        if (isDestroyed()) return;
        operation = state;
        if (state.busy && state.policy != null) { days = state.policy.days; favorites = state.policy.favorites; }
        render();
    }
    private int selectedDay() { for (int i = 0; i < DAYS.length; i++) if (days == DAYS[i]) return i; return 2; }
    private void render() {
        boolean busy = operation != null && operation.busy;
        LinearLayout body = Design.page(this, false, null, n -> {}); RecordUi.label(this, body);
        SoundSettingsUi.header(this, body, details ? "Retention details" : "Audio retention", details ? "Audio retention" : null, this::back);
        if (details) {
            RecordUi.heading(body, "Recordings");
            Design.text(body, "Expired audio is permanently deleted when you apply retention.", 14, 400, Design.MUTED);
            RecordUi.heading(body, "Favorites");
            Design.text(body, "When Keep favorites is on, a favorite estimate keeps its shared recording, including other sounds in that file.", 14, 400, Design.MUTED);
            RecordUi.heading(body, "Sleep history");
            Design.text(body, "Sleep records and sound estimates stay on this phone. Retention removes audio files only.", 14, 400, Design.MUTED);
        } else {
            View choice = SoundSettingsUi.choice(body, "clock", "Keep recordings", LABELS[selectedDay()], this::choose);
            RecordUi.enabled(choice, !busy); Design.line(body); Design.space(body, 14);
            View toggle = Design.toggle(body, "Keep favorites", "Keep audio with a favorite estimate.", favorites, value -> favorites = value);
            RecordUi.enabled(toggle, !busy); Design.space(body, 12);
            SoundSettingsUi.navigation(body, "info", "Retention details", "What is deleted and what stays", () -> { details = true; render(); });
            Design.space(body, 12); Design.text(body, "Applying retention permanently deletes expired audio.", 13, 400, Design.MUTED);
        }
        LinearLayout footer = RecordUi.footer(body);
        if (operation != null && operation.result != null && operation.policy != null
                && operation.policy.days == days && operation.policy.favorites == favorites)
            SoundSettingsUi.status(footer, operation.result.message, !operation.result.applied);
        View save = Design.button(footer, busy ? "Applying…" : "Save & apply retention", true,
                () -> job.start(new SoundRetentionJob.Policy(days, favorites)));
        RecordUi.enabled(save, !busy);
        if (restoredPicker != null && !busy) {
            Bundle state = restoredPicker; restoredPicker = null; choose();
            if (picker != null) picker.onRestoreInstanceState(state);
        }
    }
    private void choose() {
        if (operation != null && operation.busy || picker != null && picker.isShowing()) return;
        picker = SleepPicker.choices(this, "Keep recordings", LABELS, selectedDay(), n -> { days = DAYS[n]; render(); });
    }
    private void back() { if (details) { details = false; render(); } else finish(); }
    @android.annotation.SuppressLint("GestureBackNavigation") @Override public void onBackPressed() { back(); }
    boolean awaitReaders(long timeout) throws InterruptedException { return job.awaitClosed(timeout); }
    @Override protected void onDestroy() {
        if (picker != null) picker.dismiss(); job.detach(this); if (!isChangingConfigurations()) job.close(); super.onDestroy();
    }
    @Override public void onWindowFocusChanged(boolean focused) { super.onWindowFocusChanged(focused); if (focused) RecordUi.lightBars(this); }
}
