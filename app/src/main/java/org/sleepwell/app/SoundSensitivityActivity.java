package org.sleepwell.app;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** One draft across category and duration pages; only Save changes the next night's policy. */
public final class SoundSensitivityActivity extends androidx.activity.ComponentActivity implements SoundSensitivitySave.Observer {
    private SoundSensitivity draft;
    private String readError = "", selectionError = "", route = "main", pickerKey = "";
    private SoundSensitivitySave job;
    private SoundSensitivitySave.State operation;
    private Dialog picker;
    private Bundle restoredPicker;
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            public void handleOnBackPressed() { back(); }
        });
        RecordUi.review(this); Object old = getLastCustomNonConfigurationInstance();
        job = old instanceof SoundSensitivitySave ? (SoundSensitivitySave) old
                : new SoundSensitivitySave(SoundSensitivitySettings.editor(getApplicationContext()));
        SoundSensitivitySettings.Read initial;
        try { initial = job.port.read(); }
        catch (RuntimeException failure) { initial = new SoundSensitivitySettings.Read(SoundSensitivity.DEFAULT,
                "Saved settings could not be read. Review and save to enable analysis."); }
        draft = initial.policy; readError = initial.error;
        route = getIntent().getStringExtra("soundSettingsPage");
        if (saved != null) {
            route = saved.getString("route", saved.getBoolean("categories") ? "categories" : "main");
            selectionError = saved.getString("selectionError", ""); readError = saved.getString("readError", readError);
            pickerKey = saved.getString("pickerKey", ""); restoredPicker = saved.getBundle("picker");
            try { draft = SoundSensitivityJson.decode(saved.getString("draft")); }
            catch (IllegalArgumentException invalid) { draft = SoundSensitivity.DEFAULT; readError = "Draft settings are invalid. Defaults are being used."; }
        }
        if (!"categories".equals(route) && !"durations".equals(route)) route = "main";
        job.observe(this);
    }
    @Override public Object onRetainCustomNonConfigurationInstance() { return job; }
    @Override public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out); out.putString("draft", SoundSensitivityJson.encode(draft)); out.putString("readError", readError);
        out.putString("route", route); out.putBoolean("categories", route.equals("categories")); out.putString("selectionError", selectionError); out.putString("pickerKey", pickerKey);
        if (picker instanceof SleepPicker.StateDialog && picker.isShowing()) out.putBundle("picker", picker.onSaveInstanceState());
    }
    @Override public void changed(SoundSensitivitySave.State state) {
        if (isDestroyed()) return;
        operation = state; if (state.busy && state.policy != null) draft = state.policy; if (state.saved) readError = ""; render();
    }
    private void go(String next) { route = next; selectionError = ""; render(); }
    private String durationSummary() {
        int first = draft.windows(SoundSensitivity.CATEGORIES.get(0));
        for (String key : SoundSensitivity.CATEGORIES) if (draft.windows(key) != first) return "Custom durations";
        return SoundSensitivity.choice(first) + " for all categories";
    }
    private void render() {
        boolean busy = operation != null && operation.busy;
        LinearLayout body = Design.page(this, false, null, n -> {}); RecordUi.label(this, body);
        String title = route.equals("categories") ? "Detected categories" : route.equals("durations") ? "Event duration" : "Sound sensitivity";
        SoundSettingsUi.header(this, body, title, route.equals("main") ? null : "Sound sensitivity", this::back);
        if (route.equals("categories")) {
            Design.text(body, "Keep selected estimates next night.", 14, 400, Design.MUTED); Design.space(body, 14);
            for (String key : SoundSensitivity.SELECTABLE) {
                android.widget.Switch toggle = Design.toggle(body, SoundSensitivity.label(key), null, draft.includes(key), on -> {
                    try { draft = draft.include(key, on); selectionError = ""; }
                    catch (IllegalArgumentException last) { selectionError = "Keep at least one category on."; }
                    render();
                });
                toggle.setContentDescription("Include " + SoundSensitivity.label(key));
                toggle.setMinimumWidth(Design.dp(this, 48)); toggle.setMinimumHeight(Design.dp(this, 48));
                RecordUi.enabled(toggle, !busy); Design.space(body, 6);
            }
            Design.space(body, 8); Design.text(body, "Unselected labels are dropped. Other sounds is independent.", 13, 400, Design.MUTED);
        } else if (route.equals("durations")) {
            Design.text(body, "Minimum accepted duration. Applies next night.", 14, 400, Design.MUTED); Design.space(body, 12);
            for (String key : SoundSensitivity.CATEGORIES) {
                View row = SoundSettingsUi.choice(body, "wave", SoundSensitivity.label(key), SoundSensitivity.choice(draft.windows(key)), () -> choose(key));
                row.setContentDescription("Minimum duration " + SoundSensitivity.label(key)); RecordUi.enabled(row, !busy); Design.line(body);
            }
            Design.space(body, 14); Design.text(body, "Shorter events can increase false detections. Model scores stay unchanged.", 13, 400, Design.MUTED);
        } else {
            Design.text(body, "Choose what to keep from the next night.", 14, 400, Design.MUTED); Design.space(body, 12);
            SoundSettingsUi.navigation(body, "wave", "Detected categories", draft.selectedCount() + " of 6 selected", () -> go("categories")); Design.line(body);
            SoundSettingsUi.navigation(body, "clock", "Event duration", durationSummary(), () -> go("durations")); Design.line(body);
            Design.space(body, 14);
            View reset = Design.button(body, "Restore defaults", false, () -> { draft = SoundSensitivity.DEFAULT; selectionError = ""; render(); });
            reset.setContentDescription("Restore sound sensitivity defaults"); RecordUi.enabled(reset, !busy);
            Design.text(body, "Resets categories and durations in this draft.", 12, 400, Design.MUTED);
        }
        if (!draft.allIncluded()) { Design.space(body, 12); Design.text(body, "Selected estimates only · sound clips off next night.", 13, 500, Design.BROWN); }
        LinearLayout footer = RecordUi.footer(body);
        String message = !selectionError.isEmpty() ? selectionError : operation != null && !operation.error.isEmpty() ? operation.error : readError;
        boolean saved = operation != null && operation.saved && draft.equals(operation.policy);
        if (saved && selectionError.isEmpty()) message = "Saved for the next night.";
        TextView status = SoundSettingsUi.status(footer, message, !readError.isEmpty() || operation != null && !operation.error.isEmpty());
        status.setContentDescription("Sound sensitivity status");
        View save = Design.button(footer, busy ? "Saving…" : "Save sensitivity", true, () -> { selectionError = ""; job.start(draft); }); RecordUi.enabled(save, !busy);
        if (restoredPicker != null && !busy) {
            Bundle state = restoredPicker; restoredPicker = null;
            for (String key : SoundSensitivity.CATEGORIES) if (key.equals(pickerKey)) { choose(key); if (picker != null) picker.onRestoreInstanceState(state); break; }
        }
    }
    private void choose(String key) {
        if (operation != null && operation.busy || picker != null && picker.isShowing()) return;
        pickerKey = key;
        String[] values = {"High · about 1 second", "Default · about 2 seconds", "Low · about 3 seconds"};
        picker = SleepPicker.choices(this, SoundSensitivity.label(key) + " duration", values, draft.windows(key) - 1,
                n -> { draft = draft.with(key, n + 1); render(); });
    }
    private void back() { if (!route.equals("main")) go("main"); else finish(); }
    boolean awaitWorkers(long timeout) throws InterruptedException { return job.awaitClosed(timeout); }
    @Override protected void onDestroy() { if (picker != null) picker.dismiss(); job.detach(this); if (!isChangingConfigurations()) job.close(); super.onDestroy(); }
    @Override protected void onPostResume() { super.onPostResume(); RecordUi.lightBars(this); }
    @Override public void onWindowFocusChanged(boolean focused) { super.onWindowFocusChanged(focused); if (focused) RecordUi.lightBars(this); }
}
