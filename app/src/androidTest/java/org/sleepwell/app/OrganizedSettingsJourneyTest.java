package org.sleepwell.app;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.inspector.WindowInspector;
import android.widget.Switch;
import android.widget.TextView;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import org.junit.Test;
import java.io.File;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import static org.junit.Assert.*;

/** One disposable CI journey: real native input/storage, never playback, capture or activation. */
public final class OrganizedSettingsJourneyTest {
    private final Instrumentation i = InstrumentationRegistry.getInstrumentation();
    private final Context c = i.getTargetContext();
    private final UiDevice device = UiDevice.getInstance(i);
    private final List<Activity> owned = new CopyOnWriteArrayList<>();
    private Activity activity;
    private String step = "CI admission";
    private int captures;

    private void await(BooleanSupplier condition) throws Exception {
        long until = SystemClock.elapsedRealtime() + 8000;
        while (!condition.getAsBoolean() && SystemClock.elapsedRealtime() < until) Thread.sleep(20);
        assertTrue(step, condition.getAsBoolean());
    }
    private void silent() {
        assertFalse("No alarm runtime", AlarmService.active);
        assertFalse("No tracking runtime", TrackingService.running);
        assertFalse(Prefs.flag(c, "ringing")); assertFalse(Prefs.flag(c, "tracking"));
        assertFalse("No microphone lease", MicrophoneLease.occupied());
        assertFalse("No import job", SoundImports.pending());
        assertNull("No sleep-sound playback", SleepSoundService.snapshot());
    }
    private View find(View root, String value, boolean description) {
        if (description ? value.contentEquals(root.getContentDescription() == null ? "" : root.getContentDescription())
                : root instanceof TextView && value.contentEquals(((TextView) root).getText())) return root;
        if (root instanceof ViewGroup group) for (int n = 0; n < group.getChildCount(); n++) {
            View found = find(group.getChildAt(n), value, description); if (found != null) return found;
        }
        return null;
    }
    private View focused() {
        View found = null;
        for (View root : WindowInspector.getGlobalWindowViews())
            if (root.getContext().getPackageName().equals(c.getPackageName()) && root.hasWindowFocus()
                    && root.isAttachedToWindow() && root.isLaidOut()) {
                assertNull("One focused app window", found); found = root;
            }
        return found;
    }
    private void ready() throws Exception {
        await(() -> { boolean[] yes = {false}; i.runOnMainSync(() -> yes[0] = activity != null && !activity.isDestroyed()
                && !activity.isFinishing() && activity.hasWindowFocus() && focused() == activity.getWindow().getDecorView()); return yes[0]; });
        silent();
    }
    private void require(String value) throws Exception {
        step = "require " + value; ready();
        await(() -> { boolean[] yes = {false}; i.runOnMainSync(() -> yes[0] = find(activity.getWindow().getDecorView(), value, false) != null); return yes[0]; });
    }
    private View clickable(View root, String value, boolean description) {
        View view = find(root, value, description); assertNotNull(value, view);
        while (!view.isClickable() && view.getParent() instanceof View) view = (View) view.getParent();
        assertTrue(value + " enabled action", view.isEnabled() && view.isClickable()); return view;
    }
    private int dockTop() {
        View decor = activity.getWindow().getDecorView();
        assertNotNull(decor.getRootWindowInsets()); int[] origin = new int[2]; decor.getLocationOnScreen(origin);
        return origin[1] + decor.getHeight() - decor.getRootWindowInsets().getInsets(WindowInsets.Type.navigationBars()).bottom;
    }
    private Rect measured(View root, String value, boolean description) {
        View view = clickable(root, value, description); Rect visible = new Rect(); assertTrue(view.getGlobalVisibleRect(visible));
        assertEquals(value + " complete width", view.getWidth(), visible.width());
        assertEquals(value + " complete height", view.getHeight(), visible.height());
        assertTrue(value + " 48dp width", visible.width() >= Design.dp(c, 48));
        assertTrue(value + " 48dp height", visible.height() >= Design.dp(c, 48));
        assertTrue(value + " clear of navigation dock", visible.bottom <= dockTop()); return visible;
    }
    private void expose(View root, String value, boolean description) throws Exception {
        await(() -> { boolean[] yes = {false}; i.runOnMainSync(() -> {
            if (focused() != root) return;
            View view = find(root, value, description); if (view == null) return;
            while (!view.isClickable() && view.getParent() instanceof View) view = (View) view.getParent();
            if (!view.isEnabled() || !view.isClickable()) return;
            view.requestRectangleOnScreen(new Rect(0, 0, view.getWidth(), view.getHeight()), true);
            Rect shown = new Rect(); yes[0] = view.getGlobalVisibleRect(shown)
                    && shown.width() == view.getWidth() && shown.height() == view.getHeight() && shown.bottom <= dockTop();
        }); return yes[0]; });
    }
    private void frame(View root) throws Exception {
        CountDownLatch committed = new CountDownLatch(1);
        i.runOnMainSync(() -> { assertSame(root, focused()); assertTrue(root.isHardwareAccelerated());
            root.getViewTreeObserver().registerFrameCommitCallback(committed::countDown); root.invalidate(); });
        assertTrue(step + " actual committed frame", committed.await(5, TimeUnit.SECONDS));
        i.runOnMainSync(() -> assertSame(root, focused()));
    }
    private void input(View root, String value, boolean description) throws Exception {
        step = "single native input " + value;
        assertFalse("Activation and output controls are forbidden", Set.of("Preview cue", "Arm tonight", "Nightly tracking",
                "Microphone analysis", "Record sounds", "Play sleep sounds", "Import audio file").contains(value));
        silent(); expose(root, value, description); frame(root); Rect[] area = {null};
        i.runOnMainSync(() -> area[0] = measured(root, value, description));
        assertTrue("Single delivered native tap", device.click(area[0].centerX(), area[0].centerY()));
        i.waitForIdleSync(); silent();
    }
    private void click(String value) throws Exception { ready(); input(activity.getWindow().getDecorView(), value, false); }
    private void desc(String value) throws Exception { ready(); input(activity.getWindow().getDecorView(), value, true); }
    private View dialog(String title) throws Exception {
        View[] result = {null};
        await(() -> { i.runOnMainSync(() -> { View root = focused(); if (root != null && root != activity.getWindow().getDecorView()
                && find(root, title, false) != null) result[0] = root; }); return result[0] != null; });
        return result[0];
    }
    private void choose(String title, String value) throws Exception {
        View root = dialog(title); input(root, value, false); await(() -> !root.isAttachedToWindow()); ready();
    }
    private void checked(String description, boolean value) throws Exception {
        ready(); i.runOnMainSync(() -> { View view = find(activity.getWindow().getDecorView(), description, true);
            assertTrue(description + " real switch", view instanceof Switch); assertEquals(description, value, ((Switch) view).isChecked()); });
    }
    private void selected(String text, boolean value) throws Exception {
        ready(); i.runOnMainSync(() -> assertEquals(text + " actual selected state",
                value, clickable(activity.getWindow().getDecorView(), text, false).isSelected()));
    }
    private void launch(Class<? extends Activity> type) throws Exception {
        step = "launch " + type.getSimpleName(); Instrumentation.ActivityMonitor monitor = i.addMonitor(type.getName(), null, false);
        try { c.startActivity(new Intent(c, type).putExtra("visualReview", true)
                .putExtra("verificationLabel", "CI private settings · no output").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            activity = monitor.waitForActivityWithTimeout(5000); assertNotNull(activity);
        } finally { i.removeMonitor(monitor); }
        ready();
    }
    private void recreate() throws Exception {
        Activity before = activity; Instrumentation.ActivityMonitor monitor = i.addMonitor(before.getClass().getName(), null, false);
        try { i.runOnMainSync(before::recreate); activity = monitor.waitForActivityWithTimeout(5000); assertNotNull(activity); }
        finally { i.removeMonitor(monitor); }
        await(before::isDestroyed); ready();
    }
    private void finish() throws Exception { Activity before = activity; i.runOnMainSync(before::finish); await(before::isDestroyed); drain(before); }
    private void back() throws Exception { ready(); device.pressBack(); i.waitForIdleSync(); ready(); }
    private void drain(Activity a) throws Exception {
        if (a instanceof SoundSensitivityActivity) assertTrue(((SoundSensitivityActivity) a).awaitWorkers(8000));
        if (a instanceof SoundLibrarySettingsActivity) assertTrue(((SoundLibrarySettingsActivity) a).awaitReaders(8000));
        if (a instanceof SleepSoundsActivity) ((SleepSoundsActivity) a).awaitReaders(8000);
    }
    private File folder() { File dir = new File(c.getExternalFilesDir(null), "verification"); assertTrue(dir.isDirectory() || dir.mkdirs()); return dir; }
    private void capture(String name, String action, boolean description) throws Exception {
        step = "capture organized-settings-" + name; ready(); View root = activity.getWindow().getDecorView();
        if (action != null) { expose(root, action, description); i.runOnMainSync(() -> measured(root, action, description)); }
        captureWindow(root, name);
    }
    private void captureWindow(View root, String name) throws Exception {
        assertTrue("At most eleven representative frames", captures < 11); frame(root); silent();
        assertTrue(device.takeScreenshot(new File(folder(), "organized-settings-" + name + ".png"))); captures++;
    }
    private void closeAll() throws Exception {
        i.runOnMainSync(() -> { List<Activity> reverse = new ArrayList<>(owned); Collections.reverse(reverse);
            for (Activity a : reverse) if (!a.isDestroyed()) a.finish(); });
        await(() -> owned.stream().allMatch(Activity::isDestroyed)); for (Activity a : owned) drain(a);
    }
    private void restore(SharedPreferences prefs, Map<String, ?> snapshot) {
        SharedPreferences.Editor edit = prefs.edit().clear();
        for (Map.Entry<String, ?> entry : snapshot.entrySet()) {
            String key = entry.getKey(); Object value = entry.getValue();
            if (value instanceof Boolean) edit.putBoolean(key, (Boolean) value);
            else if (value instanceof Integer) edit.putInt(key, (Integer) value);
            else if (value instanceof Long) edit.putLong(key, (Long) value);
            else if (value instanceof Float) edit.putFloat(key, (Float) value);
            else if (value instanceof String) edit.putString(key, (String) value);
            else if (value instanceof Set<?>) { Set<String> strings = new HashSet<>(); for (Object item : (Set<?>) value) strings.add((String) item); edit.putStringSet(key, strings); }
            else throw new AssertionError("Unsupported saved preference type");
        }
        assertTrue("Restore exact original preferences", edit.commit()); assertEquals(snapshot, prefs.getAll());
    }

    @Test public void organizedMenusPreserveDraftsAndSafeChoicesWithoutOutputs() throws Exception {
        assertEquals("true", InstrumentationRegistry.getArguments().getString("ciReview"));
        assertEquals("1", device.executeShellCommand("getprop ro.kernel.qemu").trim());
        assertEquals("This bounded journey targets API36", 36, android.os.Build.VERSION.SDK_INT); silent();
        assertTrue("No Google subscription to recover", Prefs.text(c, "googleSleepSubscription", "").isEmpty());
        assertFalse(Prefs.flag(c, "trackingPaused")); assertEquals(0L, Prefs.of(c).getLong("session", 0));
        SharedPreferences personal = Prefs.of(c); Map<String, ?> original = new HashMap<>(personal.getAll());
        String id = UUID.randomUUID().toString(), database = "organized-settings-" + id + ".db";
        List<String> names = List.of("organized-sensitivity-" + id, "organized-mix-" + id, "organized-auto-" + id, "organized-recurring-" + id);
        SharedPreferences sensitivity = c.getSharedPreferences(names.get(0), 0), mix = c.getSharedPreferences(names.get(1), 0),
                automatic = c.getSharedPreferences(names.get(2), 0), recurring = c.getSharedPreferences(names.get(3), 0);
        Records.Factory oldRecords = Records.factory; SoundSensitivitySettings.Factory oldSensitivity = SoundSensitivitySettings.factory;
        SoundPreferences.Factory oldMix = SoundPreferences.factory; SoundRetentionJob.Factory oldRetention = SoundRetentionJob.factory;
        SnoringCuesActivity.PreviewFactory oldPreview = SnoringCuesActivity.previewFactory;
        AutomaticTrackingActivity.Port oldAutomatic = AutomaticTrackingActivity.port; RecurringTracking.Port oldRecurring = RecurringTracking.port;
        Application app = (Application) c.getApplicationContext();
        Application.ActivityLifecycleCallbacks lifecycle = new Application.ActivityLifecycleCallbacks() {
            public void onActivityCreated(Activity a, Bundle state) { owned.add(a);
                assertTrue("Only inert settings windows", a instanceof SoundSensitivityActivity || a instanceof SoundLibrarySettingsActivity
                        || a instanceof SnoringCuesActivity || a instanceof AutomaticTrackingActivity || a instanceof RecurringTrackingActivity || a instanceof SleepSoundsActivity);
                assertTrue("Every window suppresses output haptics", a.getIntent().getBooleanExtra("visualReview", false)); }
            public void onActivityStarted(Activity a) {} public void onActivityResumed(Activity a) {} public void onActivityPaused(Activity a) {}
            public void onActivityStopped(Activity a) {} public void onActivitySaveInstanceState(Activity a, Bundle state) {} public void onActivityDestroyed(Activity a) {}
        };
        app.registerActivityLifecycleCallbacks(lifecycle);
        try {
            // Include initial boot-default absence/presence in the original snapshot above.
            // Define inert values during the journey so delayed BOOT_COMPLETED cannot race its baseline.
            assertTrue(personal.edit().putBoolean("ringing", false).putBoolean("tracking", false).putBoolean("trackingPaused", false)
                    .putLong("session", 0).putInt("audioRetentionDays", 7).putBoolean("keepAudioFavorites", true)
                    .putBoolean("snoreCues", false).putBoolean("noiseEnabled", false).putBoolean("sonarSnoreVibration", false)
                    .putBoolean("sonarSnoreSound", false).putInt("snoreCueMode", 1).putInt("snoreCueIntensity", 1)
                    .putInt("snoreCueRepeats", 1).putInt("snoreCueSensitivity", 1).commit());
            Map<String, ?> inert = new HashMap<>(personal.getAll());
            i.runOnMainSync(() -> {
                Records.factory = context -> new Store(context, database);
                SoundSensitivitySettings.factory = context -> SoundSensitivitySettings.of(sensitivity);
                SoundPreferences.factory = context -> mix;
                SoundRetentionJob.factory = (context, records) -> policy -> { throw new AssertionError("Retention was not authorized to apply"); };
                SnoringCuesActivity.previewFactory = (context, allowed) -> { throw new AssertionError("Cue output is forbidden"); };
                AutomaticTrackingActivity.port = new AutomaticTrackingActivity.Port() {
                    public SharedPreferences preferences(Activity a) { return automatic; }
                    public long schedule(Activity a, AutomaticSleep.Mode mode) { throw new AssertionError("Arming is forbidden"); }
                    public void arm(Activity a, AutomaticSleep.Mode mode, long at, boolean sounds, boolean clips) { throw new AssertionError("Arming is forbidden"); }
                    public boolean movementAvailable(Activity a) { return true; } public boolean notificationsAllowed(Activity a) { return true; }
                    public boolean microphoneAllowed(Activity a) { throw new AssertionError("Capture admission is forbidden"); }
                };
                RecurringTracking.port = new RecurringTracking.Port() {
                    public SharedPreferences preferences(Context context) { return recurring; } public long now() { return 1793000000000L; }
                    public ZoneId zone() { return ZoneId.of("Asia/Riyadh"); } public boolean exact(Context context) { return true; }
                    public boolean notifications(Context context) { return true; } public boolean movement(Context context) { return true; }
                    public boolean charging(Context context) { return true; } public boolean occupied(Context context) { return false; }
                    public boolean ownsRun(Context context, String token) { return false; }
                    public void schedule(Context context, long at, String token) { throw new AssertionError("Scheduling is forbidden"); }
                    public void cancel(Context context, String token) { throw new AssertionError("No schedule exists to cancel"); }
                    public void launch(Context context, RecurringTracking.Run run) { throw new AssertionError("Capture is forbidden"); }
                    public void stopOwned(Context context, String token) { throw new AssertionError("No recording exists to stop"); }
                };
            });

            launch(SoundSensitivityActivity.class); click("Detected categories"); desc("Include Laughter"); checked("Include Laughter", false);
            recreate(); checked("Include Laughter", false); capture("sensitivity-categories", "Include Laughter", true);
            back(); require("Sound sensitivity"); click("Event duration"); desc("Minimum duration Snoring");
            View duration = dialog("Snoring duration"); captureWindow(duration, "sensitivity-duration");
            choose("Snoring duration", "High · about 1 second"); assertTrue("Draft is not saved", sensitivity.getAll().isEmpty());
            back(); click("Save sensitivity"); require("Saved for the next night."); finish();
            launch(SoundSensitivityActivity.class); click("Detected categories"); checked("Include Laughter", false); back(); click("Event duration");
            require("About 1 sec · High"); back(); capture("sensitivity-reopened", "Save sensitivity", false); finish();
            assertFalse(SoundSensitivitySettings.read(sensitivity).policy.includes("laughter")); assertEquals(1, SoundSensitivitySettings.read(sensitivity).policy.windows("snoring"));

            launch(SoundLibrarySettingsActivity.class); click("Keep recordings"); choose("Keep recordings", "3 days"); recreate(); require("3 days");
            click("Retention details"); require("Retention details"); capture("retention-details", "Save & apply retention", false); back(); require("3 days"); finish();
            assertEquals("Retention draft did not apply", 7, personal.getInt("audioRetentionDays", 0));

            launch(SnoringCuesActivity.class); click("Both"); selected("Both", true); click("Contactless & output");
            checked("Contactless vibration cues", false); recreate(); require("Contactless & output"); capture("snoring-options", "Save settings", false);
            back(); selected("Both", true); checked("Respond to snoring estimates", false); finish(); assertEquals(1, personal.getInt("snoreCueMode", -1));

            launch(AutomaticTrackingActivity.class); click("At bedtime"); click("Start conditions"); desc("Wait for charging"); checked("Wait for charging", true);
            recreate(); checked("Wait for charging", true); capture("automatic-conditions", "Done", false); click("Done"); selected("At bedtime", true);
            click("Start conditions"); checked("Wait for charging", true); back(); require("Automatic tracking"); finish(); assertTrue(automatic.getAll().isEmpty());

            launch(RecurringTrackingActivity.class); click("After settling down"); click("Start conditions"); desc("Only when charging"); checked("Only when charging", true);
            recreate(); checked("Only when charging", true); capture("recurring-conditions", "Done", false); click("Done"); selected("After settling down", true);
            checked("Nightly tracking", false); click("Start conditions"); checked("Only when charging", true); back(); finish(); assertTrue(recurring.getAll().isEmpty());

            launch(SleepSoundsActivity.class); click("More sound settings"); click("Sources & playlists"); require("Sources & playlists");
            capture("sounds-sources", "Back to sound settings", false); back(); require("Sound settings"); click("Playback & tracking");
            capture("sounds-playback", "Back to sound settings", false); back(); require("Sound settings"); back(); require("Sleep sounds");
            assertTrue("Navigation did not save a source", mix.getAll().isEmpty());
            click("Timer"); desc("45 minutes"); selected("45 min", true); assertEquals(30, SoundPreferences.load(mix).timerMinutes);
            recreate(); selected("45 min", true); capture("timer-recreated", "45 minutes", true); click("Use 45 min timer");
            click("Fade out"); desc("10 minutes"); selected("10 min", true); click("Use 10 min fade"); finish();
            assertEquals(45, SoundPreferences.load(mix).timerMinutes); assertEquals(10, SoundPreferences.load(mix).fadeMinutes);
            launch(SleepSoundsActivity.class); click("Timer"); selected("45 min", true); click("Cancel"); click("Fade out"); selected("10 min", true);
            capture("fade-reopened", "Use 10 min fade", false); click("Cancel"); finish();
            assertEquals("All eleven actual frames captured", 11, captures); assertEquals("Only private setting stores changed", inert, personal.getAll()); silent();
        } catch (Exception | AssertionError failure) {
            try { device.dumpWindowHierarchy(new File(folder(), "organized-settings-failure.xml"));
                try (PrintWriter out = new PrintWriter(new File(folder(), "organized-settings-failure.txt"), "UTF-8")) { out.println(step); failure.printStackTrace(out); }
            } catch (Exception diagnostic) { failure.addSuppressed(diagnostic); }
            throw failure;
        } finally {
            boolean drained = false;
            try { closeAll(); drained = true; }
            finally {
                app.unregisterActivityLifecycleCallbacks(lifecycle);
                i.runOnMainSync(() -> { Records.factory = oldRecords; SoundSensitivitySettings.factory = oldSensitivity;
                    SoundPreferences.factory = oldMix; SoundRetentionJob.factory = oldRetention; SnoringCuesActivity.previewFactory = oldPreview;
                    AutomaticTrackingActivity.port = oldAutomatic; RecurringTracking.port = oldRecurring; });
                // Drain all owned workers before restoring exact absence as well as values.
                restore(personal, original);
                if (drained) {
                    for (String name : names) { assertTrue(c.getSharedPreferences(name, 0).edit().clear().commit()); assertTrue(c.deleteSharedPreferences(name)); }
                    c.deleteDatabase(database);
                }
                assertEquals(original, personal.getAll()); silent();
            }
        }
    }
}
