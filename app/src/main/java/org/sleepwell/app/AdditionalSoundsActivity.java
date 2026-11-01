package org.sleepwell.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import java.util.Locale;

/** Device-local future-night opt-in and silent, read-only original-capture estimates. */
public final class AdditionalSoundsActivity extends androidx.activity.ComponentActivity implements AdditionalSoundsJob.Observer {
    private AdditionalSoundsJob job;
    private boolean draft, original, initialized, resumed, about, dirtyReview;
    private int handledSave, renderEpoch, scrollPosition, parentScroll;
    private String message = "";
    private AlertDialog confirmation;

    static Intent forRecord(android.app.Activity owner, AdditionalSoundStore.Snapshot snapshot) {
        if (snapshot == null || !snapshot.requested || snapshot.session <= 0 || snapshot.token.isEmpty() || snapshot.rawStart <= 0)
            throw new IllegalArgumentException("Open a night with local additional sound results.");
        return RecordUi.intent(owner, AdditionalSoundsActivity.class).putExtra("record", snapshot.session)
                .putExtra("additionalStart", snapshot.rawStart).putExtra("additionalToken", snapshot.token);
    }
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved); RecordUi.review(this);
        Object retained = getLastCustomNonConfigurationInstance();
        job = retained instanceof AdditionalSoundsJob ? (AdditionalSoundsJob) retained
                : new AdditionalSoundsJob(getApplicationContext(), getIntent().getLongExtra("record", 0),
                getIntent().getLongExtra("additionalStart", 0), getIntent().getStringExtra("additionalToken"));
        if (saved != null) {
            draft = saved.getBoolean("draft"); original = saved.getBoolean("original"); initialized = saved.getBoolean("initialized");
            about = saved.getBoolean("about"); dirtyReview = saved.getBoolean("dirtyReview"); handledSave = retained instanceof AdditionalSoundsJob ? saved.getInt("handledSave") : job.saveRevision;
            scrollPosition = Math.max(0, saved.getInt("scroll")); parentScroll = Math.max(0, saved.getInt("parentScroll")); message = retained instanceof AdditionalSoundsJob ? saved.getString("message", "") : "";
        }
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { back(); }
        });
        job.observe(this);
        if (!job.loaded && !job.busy) job.load(saved == null ? 0 : saved.getInt("offset", 0));
    }
    @Override public Object onRetainCustomNonConfigurationInstance() { return job; }
    @Override public void changed() {
        if (isDestroyed() || isFinishing()) return;
        if (job.recordId == 0 && job.loaded) { if (!initialized) draft = job.preference; original = job.preference; initialized = true; }
        if (job.saveRevision != handledSave) {
            handledSave = job.saveRevision; original = job.preference;
            message = original ? "Saved for future nights on this phone." : "Off for future nights on this phone.";
        }
        render();
    }
    private boolean live(int epoch) { return resumed && !isDestroyed() && !isFinishing() && hasWindowFocus() && epoch == renderEpoch; }
    private Runnable action(int epoch, Runnable task) { return () -> { if (live(epoch)) task.run(); }; }
    private void render() {
        int epoch = ++renderEpoch;
        LinearLayout body = Design.page(this, false, null, ignored -> {}); RecordUi.label(this, body);
        SoundSettingsUi.header(this, body, about ? "About additional sounds" : "Additional sounds", null, action(epoch, this::back));
        if (about) about(body);
        else if (job.recordId == 0) settings(body, epoch);
        else recording(body, epoch);
        ScrollView scroll = (ScrollView) body.getParent();
        scroll.post(() -> { if (!isDestroyed() && scroll.isAttachedToWindow() && epoch == renderEpoch) scroll.scrollTo(0, scrollPosition); });
        if (dirtyReview && resumed && confirmation == null && !job.busy) showDiscard();
    }
    private void settings(LinearLayout body, int epoch) {
        Design.text(body, "Experimental estimates", 21, 600, Design.INK); Design.space(body, 10);
        Design.text(body, "Look for possible laughter and instrument sounds during future nights. These estimates can miss or confuse sounds; uncertain results never mean a sound was absent.", 14, 400, Design.MUTED); Design.space(body, 14);
        android.widget.Switch toggle = Design.toggle(body, "Experimental estimates", "Off by default · this phone only", draft, enabled -> {
            if (!live(epoch) || job.busy || !initialized) return;
            draft = enabled; message = "";
        });
        toggle.setContentDescription("Experimental additional sound estimates"); RecordUi.enabled(toggle, initialized && !job.busy);
        Design.space(body, 18);
        Design.text(body, "Requires your existing Sound analysis choice and microphone access. Contactless nights also need their existing microphone analysis opt-in. Saving here never enables those choices or asks for permission.", 14, 400, Design.MUTED); Design.space(body, 12);
        Design.text(body, "Starts with a future recording, using its existing microphone stream. No extra microphone owner, audio upload or saved audio copy is created. Turning this off applies to future nights; past nights are not reanalyzed.", 14, 400, Design.MUTED); Design.space(body, 12);
        Design.text(body, "Results and this opt-in stay on this device. They are excluded from portable backups and settings profiles. Existing sound events, your labels and snoring cues keep their own settings.", 14, 400, Design.MUTED); Design.space(body, 14);
        SoundSettingsUi.navigation(body, "info", "About additional sounds", "Limits and local analysis", action(epoch, () -> goAbout(true)));
        LinearLayout footer = RecordUi.footer(body);
        SoundSettingsUi.status(footer, job.busy ? "Saving or loading…" : !job.error.isEmpty() ? job.error : message, !job.error.isEmpty());
        if (!job.loaded && !job.busy) Design.button(footer, "Try again", false, action(epoch, () -> job.load(0)));
        View save = Design.button(footer, "Save additional sounds", true, action(epoch, () -> { dirtyReview = false; job.save(draft); }));
        RecordUi.enabled(save, initialized && job.loaded && !job.busy);
        RecordUi.enabled(Design.button(footer, "Cancel", false, action(epoch, this::finish)), !job.busy);
    }
    private void recording(LinearLayout body, int epoch) {
        Design.text(body, "Experimental estimates", 21, 600, Design.INK); Design.space(body, 8);
        Design.text(body, "Read-only · original capture. These coarse windows can miss or confuse sounds. They do not change sound events, labels or cues.", 14, 400, Design.MUTED); Design.space(body, 14);
        if (!job.error.isEmpty()) { SoundSettingsUi.status(body, job.error, true); Design.button(body, "Try again", true, action(epoch, () -> job.load(job.offset))); return; }
        if (!job.loaded || job.snapshot == null) { Design.text(body, "Loading local results…", 14, 400, Design.MUTED); return; }
        AdditionalSoundStore.Snapshot result = job.snapshot;
        Design.text(body, RecordUi.date(job.record.rawStart, job.record.zone()), 14, 500, Design.MUTED); Design.space(body, 10);
        if (!result.requested) {
            Design.text(body, "No local additional results", 20, 600, Design.INK); Design.space(body, 8);
            Design.text(body, "These estimates were not requested for this recording, or their device-local results were excluded from a restored backup. This does not mean no laughter or instruments occurred.", 14, 400, Design.MUTED);
        } else {
            Design.text(body, stateLabel(result.state), 18, 600, Design.INK);
            if (!result.issue.isEmpty()) { Design.space(body, 8); SoundSettingsUi.status(body, result.issue, !result.state.equals("complete")); }
            RecordUi.heading(body, "Audio coverage");
            fact(body, "Captured for analysis", RecordUi.duration(result.capturedMs));
            fact(body, "Analyzed", RecordUi.duration(result.analyzedMs));
            fact(body, "Not analyzed", RecordUi.duration(result.skippedMs));
            Design.space(body, 8); Design.text(body, "Coverage is from the original microphone capture, including any intervals outside a later edited period. Pauses, short tails and resource limits can leave gaps.", 12, 400, Design.MUTED);
            RecordUi.heading(body, "Candidate windows");
            fact(body, "Laughter", result.laughterCount + " windows"); fact(body, "Instruments", result.instrumentCount + " windows");
            fact(body, "Uncertain", Math.max(0, result.windowCount - result.laughterCount - result.instrumentCount) + " windows");
            Design.space(body, 8); Design.text(body, "Counts describe analyzed windows, not separate events. Zero candidates never proves absence.", 12, 400, Design.MUTED);
            RecordUi.heading(body, "Analyzed windows");
            if (result.windowCount == 0) Design.text(body, "No completed window is saved. Results are unknown; review coverage and the status above.", 14, 400, Design.MUTED);
            else if (job.windows.isEmpty()) Design.text(body, "No windows are available on this page. Refresh to check saved results.", 14, 400, Design.MUTED);
            else {
                Design.text(body, (job.offset + 1) + "–" + (job.offset + job.windows.size()) + " of " + result.windowCount, 13, 500, Design.MUTED); Design.space(body, 10);
                for (AdditionalSoundStore.Window window : job.windows) {
                    LinearLayout card = Design.col(this); Design.pad(card, 14, 12); card.setBackground(Design.shape(this, Design.SOFT, 14));
                    Design.text(card, RecordUi.date(window.start, job.record.zone()) + " · " + RecordUi.preciseClock(this, window.start, job.record.zone()) + " – " + RecordUi.preciseClock(this, window.start + window.durationMs, job.record.zone()), 13, 500, Design.MUTED);
                    Design.space(card, 6); Design.text(card, candidate(window.candidate), 17, 600, Design.INK);
                    if (window.candidate.equals("instrument") && !window.instrumentLabel.isEmpty()) Design.text(card, window.instrumentLabel + " · possible class", 13, 400, Design.MUTED);
                    Design.text(card, RecordUi.duration(window.durationMs) + " analyzed · not an exact event boundary", 12, 400, Design.MUTED);
                    body.addView(card); Design.space(body, 10);
                }
            }
            if (job.offset > 0) RecordUi.enabled(Design.button(body, "Earlier windows", false, action(epoch, () -> changePage(Math.max(0, job.offset - 20)))), !job.busy);
            if (job.offset + job.windows.size() < result.windowCount) RecordUi.enabled(Design.button(body, "Later windows", false, action(epoch, () -> changePage(job.offset + 20))), !job.busy);
        }
        Design.space(body, 12); SoundSettingsUi.navigation(body, "info", "About additional sounds", "Source, versions and limits", action(epoch, () -> goAbout(true)));
        LinearLayout footer = RecordUi.footer(body);
        SoundSettingsUi.status(footer, job.busy ? "Loading local results…" : "", false);
        RecordUi.enabled(Design.button(footer, "Refresh results", false, action(epoch, () -> { rememberScroll(); job.load(job.offset); })), !job.busy);
    }
    private void about(LinearLayout body) {
        Design.text(body, "Experimental and device-local", 21, 600, Design.INK); Design.space(body, 10);
        Design.text(body, "Existing bundled models estimate laughter and instrument candidates in separate 10-second microphone windows. Scores are uncalibrated model outputs, not confidence percentages. Resource limits and interrupted capture can leave unanalyzed audio. No sleep-stage, medical, identity or reliable bedroom-recognition claim is made.", 14, 400, Design.MUTED); Design.space(body, 12);
        Design.text(body, "Original sound analysis and saved-recording analysis keep their own authority. These derived results never relabel your events, play audio, trigger cues or satisfy alarm challenges. No new weights are downloaded and no audio is uploaded. Portable backups and settings profiles exclude these results and this opt-in.", 14, 400, Design.MUTED);
        if (job.snapshot != null && job.snapshot.requested) {
            AdditionalSoundStore.Snapshot value = job.snapshot; RecordUi.heading(body, "This recording");
            fact(body, "Source", value.source.equals(AdditionalSoundStore.SOURCE_SONAR) ? "Filtered contactless microphone" : value.source.equals(AdditionalSoundStore.SOURCE_MOTION) ? "Phone microphone" : "Unknown recorded source");
            provenance(body, "Model version", value.modelVersion); provenance(body, "Policy version", value.policyVersion);
            fact(body, "Longest inference", value.windowCount == 0 ? "No completed inference" : value.maxInferenceMs + " ms");
            fact(body, "Largest sampled Java heap", value.maxJavaHeapBytes == 0 ? "Not sampled" : String.format(Locale.ROOT, "%.1f MiB", value.maxJavaHeapBytes / 1048576d));
            Design.text(body, "Java heap samples exclude native model allocations; they are not total process-memory measurements.", 12, 400, Design.MUTED);
            RecordUi.heading(body, "Current page provenance");
            for (AdditionalSoundStore.Window window : job.windows) {
                Design.text(body, "Window " + window.sequence + " · " + candidate(window.candidate), 14, 500, Design.INK);
                provenance(body, "Capture segment", window.segment);
                Design.text(body, window.inferenceMs + " ms · Java heap " + String.format(Locale.ROOT, "%.1f MiB", window.javaHeapBytes / 1048576d), 12, 400, Design.MUTED);
                provenance(body, "Model / policy", window.modelVersion + " / " + window.policyVersion);
                Design.text(body, window.estimates.toString(), 12, 400, Design.MUTED).setTextIsSelectable(true); Design.space(body, 12);
            }
        }
        Design.space(body, 12); Design.text(body, "Credits: PANNs weights (CC BY 4.0), PANNs code (MIT), EfficientAT (MIT), and ONNX Runtime (MIT). Full notices are available in the existing recorded-audio analysis tool under About analysis → Model credits and licenses.", 12, 400, Design.MUTED);
    }
    private void provenance(LinearLayout body, String name, String value) { Design.space(body, 8); Design.text(body, name, 13, 600, Design.INK); Design.text(body, value.isEmpty() ? "Unavailable" : value, 12, 400, Design.MUTED).setTextIsSelectable(true); }
    private void fact(LinearLayout body, String label, String value) { Design.space(body, 6); Design.text(body, label + " · " + value, 14, 400, Design.MUTED); }
    private static String candidate(String value) { return value.equals("laughter") ? "Laughter candidate" : value.equals("instrument") ? "Instrument candidate" : "Uncertain"; }
    private static String stateLabel(String value) { return switch (value) { case "complete" -> "Analysis finished"; case "pending" -> "Waiting for audio"; case "active" -> "Analysis in progress"; case "paused" -> "Analysis paused"; case "insufficient" -> "Not enough usable audio"; case "interrupted" -> "Analysis interrupted"; case "error" -> "Analysis could not finish"; default -> "Analysis unavailable"; }; }
    private void changePage(int offset) { scrollPosition = 0; job.load(offset); }
    private void goAbout(boolean next) { rememberScroll(); parentScroll = scrollPosition; about = next; scrollPosition = 0; render(); }
    private void back() {
        if (!resumed || isDestroyed() || isFinishing() || job.busy) return;
        if (about) { about = false; scrollPosition = parentScroll; render(); return; }
        if (job.recordId == 0 && initialized && draft != original) { dirtyReview = true; showDiscard(); }
        else finish();
    }
    private void showDiscard() {
        if (!resumed || isDestroyed() || isFinishing() || confirmation != null) return;
        int epoch = renderEpoch;
        confirmation = ReportDialogs.show(new AlertDialog.Builder(this).setTitle("Discard changes?")
                .setMessage("Your additional sound choice has not been saved.")
                .setPositiveButton("Discard changes", (dialog, which) -> { if (resumed && !isDestroyed() && !isFinishing() && epoch == renderEpoch) { dirtyReview = false; finish(); } })
                .setNegativeButton("Keep editing", (dialog, which) -> dirtyReview = false));
        confirmation.setOnDismissListener(dialog -> { confirmation = null; if (!isChangingConfigurations()) dirtyReview = false; });
    }
    private void rememberScroll() { if (!about && job != null && job.recordId != 0 && job.busy && !job.loaded) return; android.view.View root = findViewById(android.R.id.content); ScrollView scroll = findScroll(root); if (scroll != null) scrollPosition = scroll.getScrollY(); }
    private ScrollView findScroll(View view) { if (view instanceof ScrollView) return (ScrollView) view; if (view instanceof android.view.ViewGroup group) for (int i = 0; i < group.getChildCount(); i++) { ScrollView scroll = findScroll(group.getChildAt(i)); if (scroll != null) return scroll; } return null; }
    @Override protected void onResume() { super.onResume(); resumed = true; if (job.recordId != 0 && job.loaded && !job.busy) job.load(job.offset); if (dirtyReview && !job.busy) showDiscard(); }
    @Override protected void onPause() { rememberScroll(); resumed = false; super.onPause(); }
    @Override public void onSaveInstanceState(Bundle state) { rememberScroll(); state.putBoolean("draft", draft); state.putBoolean("original", original); state.putBoolean("initialized", initialized); state.putBoolean("about", about); state.putBoolean("dirtyReview", dirtyReview); state.putInt("handledSave", handledSave); state.putInt("offset", job.offset); state.putInt("scroll", scrollPosition); state.putInt("parentScroll", parentScroll); state.putString("message", message); super.onSaveInstanceState(state); }
    void awaitWorkers(long timeoutMillis) throws Exception { job.awaitIdle(timeoutMillis); }
    @Override protected void onDestroy() { if (confirmation != null) confirmation.dismiss(); job.detach(this); if (!isChangingConfigurations()) job.close(); super.onDestroy(); }
    @Override public void onWindowFocusChanged(boolean focused) { super.onWindowFocusChanged(focused); if (focused) RecordUi.lightBars(this); }
}
