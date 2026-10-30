package org.sleepwell.app;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
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
    private SharedPreferences automaticDraftPreferences;
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
    private Rect screenBounds(View view) {
        int[] origin = new int[2]; view.getLocationOnScreen(origin);
        return new Rect(origin[0], origin[1], origin[0] + view.getWidth(), origin[1] + view.getHeight());
    }
    private Rect measured(View root, String value, boolean description) {
        View view = clickable(root, value, description); Rect visible = new Rect(); assertTrue(view.getGlobalVisibleRect(visible));
        assertEquals(value + " complete width", view.getWidth(), visible.width());
        assertEquals(value + " complete height", view.getHeight(), visible.height());
        assertTrue(value + " 48dp width", visible.width() >= Design.dp(c, 48));
        assertTrue(value + " 48dp height", visible.height() >= Design.dp(c, 48));
        // Global-visible bounds are relative to this window's root, including for floating dialogs.
        // UiDevice taps and dockTop use screen coordinates, so preserve visibility checks separately.
        Rect click = screenBounds(view);
        assertTrue(value + " clear of navigation dock", click.bottom <= dockTop()); return click;
    }
    private void expose(View root, String value, boolean description) throws Exception {
        await(() -> { boolean[] yes = {false}; i.runOnMainSync(() -> {
            if (focused() != root) return;
            View view = find(root, value, description); if (view == null) return;
            while (!view.isClickable() && view.getParent() instanceof View) view = (View) view.getParent();
            if (!view.isEnabled() || !view.isClickable()) return;
            view.requestRectangleOnScreen(new Rect(0, 0, view.getWidth(), view.getHeight()), true);
            Rect shown = new Rect(); yes[0] = view.getGlobalVisibleRect(shown)
                    && shown.width() == view.getWidth() && shown.height() == view.getHeight() && screenBounds(view).bottom <= dockTop();
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
        boolean automaticSoundDraft = value.equals("Record sounds") && activity instanceof AutomaticTrackingActivity && automaticDraftPreferences != null;
        if (automaticSoundDraft) { java.lang.reflect.Field owner = AutomaticTrackingActivity.class.getDeclaredField("owner"), page = AutomaticTrackingActivity.class.getDeclaredField("page"); owner.setAccessible(true); page.setAccessible(true);
            assertSame("Only the private automatic draft owner may change this switch", automaticDraftPreferences, ((AutomaticTrackingActivity.Port)owner.get(activity)).preferences(activity)); assertEquals("sounds", page.get(activity)); assertTrue(description); }
        assertFalse("Activation and output controls are forbidden", Set.of("Preview cue", "Arm tonight", "Nightly tracking",
                "Microphone analysis", "Record sounds", "Play sleep sounds", "Import audio file",
                "Start tracking  \u2192", "Use sonar", "Test again", "Analyze sleep sounds", "Save sound clips this night").contains(value) && !automaticSoundDraft);
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
    private void duration(String category, int windows) throws Exception {
        step = "require " + category + " draft duration " + SoundSensitivity.choice(windows); ready();
        i.runOnMainSync(() -> {
            View row = find(activity.getWindow().getDecorView(), "Minimum duration " + category, true);
            assertNotNull(category + " duration row", row);
            assertNotNull(category + " actual duration summary", find(row, SoundSensitivity.choice(windows), false));
        });
    }
    private void selected(String text, boolean value) throws Exception {
        ready(); i.runOnMainSync(() -> assertEquals(text + " actual selected state",
                value, clickable(activity.getWindow().getDecorView(), text, false).isSelected()));
    }
    private void launch(Class<? extends Activity> type) throws Exception {
        launch(type, new Intent(c, type));
    }
    private void launch(Class<? extends Activity> type, Intent intent) throws Exception {
        step = "launch " + type.getSimpleName(); Instrumentation.ActivityMonitor monitor = i.addMonitor(type.getName(), null, false);
        try { c.startActivity(intent.putExtra("visualReview", true)
                .putExtra("verificationLabel", "CI private settings · no output").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            activity = monitor.waitForActivityWithTimeout(5000); assertNotNull(activity);
        } finally { i.removeMonitor(monitor); }
        ready();
    }
    private Activity openChild(String description, Class<? extends Activity> type) throws Exception {
        Activity parent = activity; Instrumentation.ActivityMonitor monitor = i.addMonitor(type.getName(), null, false);
        try { desc(description); activity = monitor.waitForActivityWithTimeout(5000); assertNotNull(activity); }
        finally { i.removeMonitor(monitor); }
        ready(); return parent;
    }
    private void cancelChild(Activity parent) throws Exception {
        Activity child = activity; ready(); device.pressBack(); await(child::isDestroyed); drain(child); activity = parent; ready();
    }
    private void trackingRadio(String description, boolean selected) throws Exception {
        ready(); i.runOnMainSync(() -> {
            View card = find(activity.getWindow().getDecorView(), description, true); assertNotNull(description, card);
            android.view.accessibility.AccessibilityNodeInfo node = card.createAccessibilityNodeInfo();
            assertEquals("Actual radio role", android.widget.RadioButton.class.getName(), node.getClassName().toString());
            assertTrue("Sensor selection is checkable", node.isCheckable()); assertEquals(description, selected, node.isChecked());
            assertEquals("Visual state agrees with actual radio", selected, card.isSelected());
        });
    }
    private void trackingMain(String name, boolean lightDraft) throws Exception {
        require("Tracking setup"); step = "Tracking main " + name + " full-control bounds"; ready(); View root = activity.getWindow().getDecorView(); frame(root);
        trackingRadio("Phone movement selected", true); trackingRadio("Contactless sonar", false); checked("Record sounds", false);
        checked("Record light levels", lightDraft);
        i.runOnMainSync(() -> {
            Rect start = measured(root, "Start tracking  \u2192", false);
            for (String text : new String[]{"Choose sensor", "Phone placement", "Additional sensors", "On mattress, uncovered", "Manual"}) {
                View view = find(root, text, false); assertNotNull(text, view); Rect visible = new Rect();
                assertTrue(text + " visible in main frame", view.getGlobalVisibleRect(visible));
                assertEquals(text + " complete width", view.getWidth(), visible.width());
                assertEquals(text + " complete height", view.getHeight(), visible.height());
                assertTrue(text + " above Start", screenBounds(view).bottom <= start.top);
            }
            for (String desc : new String[]{"Phone movement selected", "Contactless sonar", "Record sounds", "Record light levels", "Choose automatic start", "More tracking settings"}) {
                Rect control = measured(root, desc, true); assertTrue(desc + " above Start", control.bottom <= start.top);
            }
        });
        captureWindow(root, "tracking-" + name);
    }
    private void recreate() throws Exception {
        Activity before = activity; Instrumentation.ActivityMonitor monitor = i.addMonitor(before.getClass().getName(), null, false);
        try { i.runOnMainSync(before::recreate); activity = monitor.waitForActivityWithTimeout(5000); assertNotNull(activity); }
        finally { i.removeMonitor(monitor); }
        await(before::isDestroyed); ready();
    }
    private void blockedRoute(String label, Class<? extends Activity> type) throws Exception {
        Instrumentation.ActivityMonitor monitor = i.addMonitor(type.getName(), null, true);
        try { click(label); assertEquals("One actual route to " + type.getSimpleName(), 1, monitor.getHits()); ready(); }
        finally { i.removeMonitor(monitor); }
    }
    private void blockedActivitySettings() throws Exception {
        android.content.IntentFilter filter = new android.content.IntentFilter(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        filter.addDataScheme("package"); filter.addDataSchemeSpecificPart(c.getPackageName(), android.os.PatternMatcher.PATTERN_LITERAL);
        Instrumentation.ActivityMonitor monitor = i.addMonitor(filter, null, true);
        try { click("Manage activity access"); assertEquals("Actual settings action and package URI, OS window blocked", 1, monitor.getHits()); ready(); }
        finally { i.removeMonitor(monitor); }
    }
    private void automaticMainFits() throws Exception {
        ready(); View root = activity.getWindow().getDecorView(); frame(root);
        i.runOnMainSync(() -> { for(String title : List.of("Manual only", "At bedtime", "Before smart window", "After settling down", "Source schedule", "Start conditions", "Activity access")) measured(root,title,false);
            measured(root,"Automatic tracking options",true); Rect save=measured(root,"Save settings",false); View arm=find(root,"Arm tonight",false);
            if(arm!=null){Rect bounds=screenBounds(arm); Rect shown=new Rect(); assertTrue(arm.isEnabled()&&arm.getGlobalVisibleRect(shown)); assertEquals(arm.getWidth(),shown.width());assertEquals(arm.getHeight(),shown.height()); assertTrue(bounds.width()>=Design.dp(c,48)&&bounds.height()>=Design.dp(c,48)); assertFalse("Footer actions do not overlap",Rect.intersects(save,bounds));assertEquals(save.top,bounds.top);}
        });
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
        assertTrue("At most twenty-eight representative frames", captures < 28); frame(root); silent();
        boolean trackingDark = activity instanceof TrackingActivity && root == activity.getWindow().getDecorView();
        boolean automaticLight = activity instanceof AutomaticTrackingActivity && root == activity.getWindow().getDecorView();
        int requested = automaticLight ? WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS : 0;
        int[] bars = new int[2];
        if (trackingDark || automaticLight) {
            i.runOnMainSync(() -> { assertSame(root, focused()); WindowInsets insets = root.getRootWindowInsets(); assertNotNull(insets);
                bars[0] = insets.getInsets(WindowInsets.Type.statusBars()).top; bars[1] = insets.getInsets(WindowInsets.Type.navigationBars()).bottom; });
            // Require actual contrasting clock/Wi-Fi/gesture pixels after this owner gains focus.
            await(() -> { boolean[] owned = {false}; i.runOnMainSync(() -> owned[0] = root == focused() && trackingAppearance(root) == requested);
                if (!owned[0]) return false; android.graphics.Bitmap screen = i.getUiAutomation().takeScreenshot(); if (screen == null) return false;
                android.graphics.Bitmap readable = screen;
                try { if (screen.getConfig() == android.graphics.Bitmap.Config.HARDWARE) readable = screen.copy(android.graphics.Bitmap.Config.ARGB_8888, false);
                    return automaticLight ? darkAutomaticBars(readable, bars[0], bars[1]) : brightTrackingBars(readable, bars[0], bars[1]);
                } finally { if (readable != null && readable != screen) readable.recycle(); screen.recycle(); } });
            i.runOnMainSync(() -> { assertSame(root, focused()); assertEquals("Owned surface requests contrasting status and gesture glyphs", requested, trackingAppearance(root)); });
        }
        File original = new File(folder(), "organized-settings-" + name + ".png"); assertTrue(device.takeScreenshot(original));
        if (trackingDark || automaticLight) { android.graphics.Bitmap saved = android.graphics.BitmapFactory.decodeFile(original.getAbsolutePath());
            try { assertTrue("Saved original has contrasting system glyphs and gesture", automaticLight ? darkAutomaticBars(saved, bars[0], bars[1]) : brightTrackingBars(saved, bars[0], bars[1])); }
            finally { if (saved != null) saved.recycle(); } }
        captures++;
    }
    private int trackingAppearance(View root) { WindowInsetsController controller = root.getWindowInsetsController(); assertNotNull(controller);
        return controller.getSystemBarsAppearance() & (WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS); }
    private boolean brightTrackingBars(android.graphics.Bitmap bitmap, int status, int navigation) {
        return bitmap != null && status > 0 && navigation > 0 && status + navigation < bitmap.getHeight()
                && brightTrackingRegion(bitmap, 0, status) && brightTrackingRegion(bitmap, bitmap.getHeight() - navigation, bitmap.getHeight());
    }
    // Same rendered-bar predicate as SonarNightFlowTest; reject dark glyphs even if other icons are light.
    private boolean brightTrackingRegion(android.graphics.Bitmap bitmap, int top, int bottom) {
        int[] row = new int[bitmap.getWidth()]; long bright = 0, plum = 0, dark = 0, total = (long) row.length * (bottom - top);
        int background = Design.PLUM;
        for (int y = top; y < bottom; y++) { bitmap.getPixels(row, 0, row.length, 0, y, row.length, 1);
            for (int rgb : row) { int red = android.graphics.Color.red(rgb), green = android.graphics.Color.green(rgb), blue = android.graphics.Color.blue(rgb);
                if (red > 180 && green > 180 && blue > 180) bright++; if (red < 20 && green < 20 && blue < 20) dark++;
                if (Math.abs(red - android.graphics.Color.red(background)) <= 12 && Math.abs(green - android.graphics.Color.green(background)) <= 12 && Math.abs(blue - android.graphics.Color.blue(background)) <= 12) plum++;
            }
        }
        return bright >= 24 && plum * 100 >= total * 60 && dark <= 2;
    }
    private boolean darkAutomaticBars(android.graphics.Bitmap bitmap,int status,int navigation){
        return bitmap!=null&&status>0&&navigation>0&&status+navigation<bitmap.getHeight()
                &&darkAutomaticRegion(bitmap,0,status)&&darkAutomaticRegion(bitmap,bitmap.getHeight()-navigation,bitmap.getHeight());
    }
    private boolean darkAutomaticRegion(android.graphics.Bitmap bitmap,int top,int bottom){
        int[] row=new int[bitmap.getWidth()];long dark=0,cream=0,white=0,total=(long)row.length*(bottom-top);
        for(int y=top;y<bottom;y++){bitmap.getPixels(row,0,row.length,0,y,row.length,1);
            for(int rgb:row){int red=android.graphics.Color.red(rgb),green=android.graphics.Color.green(rgb),blue=android.graphics.Color.blue(rgb);
                if(red<170&&green<170&&blue<170)dark++;if(red>250&&green>250&&blue>250)white++;
                if(Math.abs(red-android.graphics.Color.red(Design.CREAM))<=12&&Math.abs(green-android.graphics.Color.green(Design.CREAM))<=12&&Math.abs(blue-android.graphics.Color.blue(Design.CREAM))<=12)cream++;
            }
        }
        return dark>=24&&cream*100>=total*60&&white<=2;
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
        List<String> names = List.of("organized-sensitivity-" + id, "organized-mix-" + id, "organized-auto-" + id, "organized-recurring-" + id, "organized-motion-" + id, "organized-delay-" + id);
        SharedPreferences sensitivity = c.getSharedPreferences(names.get(0), 0), mix = c.getSharedPreferences(names.get(1), 0),
                automatic = c.getSharedPreferences(names.get(2), 0), recurring = c.getSharedPreferences(names.get(3), 0),
                motion = c.getSharedPreferences(names.get(4), 0), delay = c.getSharedPreferences(names.get(5), 0);
        boolean[] automaticNotifications = {true}; String[] automaticPhase = {""}; automaticDraftPreferences = automatic;
        Records.Factory oldRecords = Records.factory; SoundSensitivitySettings.Factory oldSensitivity = SoundSensitivitySettings.factory;
        SoundPreferences.Factory oldMix = SoundPreferences.factory; SoundRetentionJob.Factory oldRetention = SoundRetentionJob.factory;
        SnoringCuesActivity.PreviewFactory oldPreview = SnoringCuesActivity.previewFactory;
        AutomaticTrackingActivity.Port oldAutomatic = AutomaticTrackingActivity.port; RecurringTracking.Port oldRecurring = RecurringTracking.port;
        TrackingActivity.Commands oldTracking = TrackingActivity.commands; TrackingService.StoreFactory oldTrackingStores = TrackingActivity.stores;
        MotionSettings.Factory oldMotionSettings = MotionSettings.factory; MotionPort.Factory oldMotionPorts = MotionSensorActivity.ports;
        TrackingDelaySettings.Factory oldDelay = TrackingDelaySettings.factory, privateDelay = context -> delay;
        Application app = (Application) c.getApplicationContext();
        Application.ActivityLifecycleCallbacks lifecycle = new Application.ActivityLifecycleCallbacks() {
            public void onActivityCreated(Activity a, Bundle state) { owned.add(a);
                assertTrue("Only inert settings windows", a instanceof SoundSensitivityActivity || a instanceof SoundLibrarySettingsActivity
                        || a instanceof SnoringCuesActivity || a instanceof AutomaticTrackingActivity || a instanceof RecurringTrackingActivity || a instanceof SleepSoundsActivity
                        || a instanceof TrackingActivity || a instanceof MotionSensorActivity);
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
                    .putBoolean("snoreCues", false).putBoolean("noiseEnabled", false).putBoolean("recordLight", false)
                    .putBoolean("saveClips", false).putBoolean("sonarSnoreVibration", false)
                    .putBoolean("sonarSnoreSound", false).putInt("snoreCueMode", 1).putInt("snoreCueIntensity", 1)
                    .putInt("snoreCueRepeats", 1).putInt("snoreCueSensitivity", 1).commit());
            Map<String, ?> inert = new HashMap<>(personal.getAll());
            i.runOnMainSync(() -> {
                Records.factory = context -> new Store(context, database);
                TrackingActivity.stores = context -> new Store(context, database); TrackingDelaySettings.factory = privateDelay;
                TrackingActivity.commands = new TrackingActivity.Commands() {
                    public TrackingService.Snapshot snapshot() { return null; }
                    public void start(TrackingActivity a, Intent intent) { throw new AssertionError("Tracking Start is forbidden"); }
                    public void command(TrackingActivity a, Intent intent) { throw new AssertionError("Tracking command is forbidden"); }
                    public void review(TrackingActivity a) { throw new AssertionError("Runtime report is forbidden"); }
                };
                MotionSettings.factory = context -> motion;
                MotionSensorActivity.ports = context -> new MotionPort() {
                    public boolean available() { return true; } public boolean begin(Sink sink) { return true; }
                    public void close() {} public long now() { return 100000L; }
                };
                SoundSensitivitySettings.factory = context -> SoundSensitivitySettings.of(sensitivity);
                SoundPreferences.factory = context -> mix;
                SoundRetentionJob.factory = (context, records) -> policy -> { throw new AssertionError("Retention was not authorized to apply"); };
                SnoringCuesActivity.previewFactory = (context, allowed) -> { throw new AssertionError("Cue output is forbidden"); };
                AutomaticTrackingActivity.port = new AutomaticTrackingActivity.Port() {
                    public SharedPreferences preferences(Activity a) { return automatic; }
                    public long schedule(Activity a, AutomaticSleep.Mode mode) { throw new AssertionError("Arming is forbidden"); }
                    public void arm(Activity a, AutomaticSleep.Mode mode, long at, boolean sounds, boolean clips) { throw new AssertionError("Arming is forbidden"); }
                    public boolean movementAvailable(Activity a) { return true; } public boolean notificationsAllowed(Activity a) { return automaticNotifications[0]; }
                    public boolean microphoneAllowed(Activity a) { throw new AssertionError("Capture admission is forbidden"); }
                    public String currentPhase(Activity a) { return automaticPhase[0]; }
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
            choose("Snoring duration", "High · about 1 second"); duration("Snoring", 1);
            assertTrue("Draft is not saved", sensitivity.getAll().isEmpty());
            back(); click("Save sensitivity"); require("Saved for the next night.");
            SoundSensitivitySettings.Read storedSensitivity = SoundSensitivitySettings.read(sensitivity);
            assertEquals("Saved private profile decodes without fallback", "", storedSensitivity.error);
            assertFalse("Saved private profile keeps category choice", storedSensitivity.policy.includes("laughter"));
            assertEquals("Saved private profile keeps selected snoring duration", 1, storedSensitivity.policy.windows("snoring")); finish();
            launch(SoundSensitivityActivity.class); click("Detected categories"); checked("Include Laughter", false); back(); click("Event duration");
            duration("Snoring", 1); back(); capture("sensitivity-reopened", "Save sensitivity", false); finish();
            assertFalse(SoundSensitivitySettings.read(sensitivity).policy.includes("laughter")); assertEquals(1, SoundSensitivitySettings.read(sensitivity).policy.windows("snoring"));

            launch(SoundLibrarySettingsActivity.class); click("Keep recordings"); choose("Keep recordings", "3 days"); recreate(); require("3 days");
            click("Retention details"); require("Retention details"); capture("retention-details", "Save & apply retention", false); back(); require("3 days"); finish();
            assertEquals("Retention draft did not apply", 7, personal.getInt("audioRetentionDays", 0));

            launch(SnoringCuesActivity.class); click("Both"); selected("Both", true); desc("Contactless & output");
            checked("Contactless vibration cues", false); recreate(); require("Cue options"); capture("snoring-options", "Save settings", false);
            back(); selected("Both", true); checked("Respond to snoring estimates", false); finish(); assertEquals(1, personal.getInt("snoreCueMode", -1));

            launch(AutomaticTrackingActivity.class); click("At bedtime"); automaticMainFits(); capture("automatic-main", "Save settings", false);
            click("Source schedule"); require("Saved bedtime · " + Design.clock(c,SleepPlans.load(c).bedtime)); recreate(); capture("automatic-source", "Done", false); blockedRoute("Review sleep plan",PlanActivity.class); back(); selected("At bedtime",true);
            click("Before smart window"); click("Source schedule"); blockedRoute("Open alarms",MainActivity.class); back(); selected("Before smart window",true); click("Manual only"); selected("Manual only",true); click("At bedtime");
            click("Start conditions"); desc("Wait for charging"); checked("Wait for charging", true);
            recreate(); checked("Wait for charging", true); capture("automatic-conditions", "Done", false); click("Done"); selected("At bedtime", true);
            // Synthetic denied result + existing fake access port only: no Arm tap, OS request or system grant change.
            java.lang.reflect.Field pending = AutomaticTrackingActivity.class.getDeclaredField("permissionPending"), permissionRequest = AutomaticTrackingActivity.class.getDeclaredField("permissionRequest"); pending.setAccessible(true); permissionRequest.setAccessible(true);
            AutomaticTrackingActivity beforePermissionRotation = (AutomaticTrackingActivity) activity;
            i.runOnMainSync(() -> { automaticNotifications[0] = false; try { pending.setBoolean(beforePermissionRotation,true); permissionRequest.setInt(beforePermissionRotation,91); } catch (IllegalAccessException bad) { throw new AssertionError(bad); } });
            recreate(); AutomaticTrackingActivity denied = (AutomaticTrackingActivity) activity;
            i.runOnMainSync(() -> { try { assertTrue("Pending request survives framework recreation",pending.getBoolean(denied));assertEquals(91,permissionRequest.getInt(denied)); }catch(IllegalAccessException bad){throw new AssertionError(bad);}
                automaticNotifications[0]=true;beforePermissionRotation.onRequestPermissionsResult(91,new String[]{Manifest.permission.POST_NOTIFICATIONS},new int[]{PackageManager.PERMISSION_GRANTED});automaticNotifications[0]=false;
                denied.onRequestPermissionsResult(91,new String[]{Manifest.permission.RECORD_AUDIO},new int[]{PackageManager.PERMISSION_DENIED});
                try{assertTrue("Wrong permission cannot own the pending request",pending.getBoolean(denied));}catch(IllegalAccessException bad){throw new AssertionError(bad);}
                denied.onRequestPermissionsResult(91, new String[]{Manifest.permission.POST_NOTIFICATIONS}, new int[]{PackageManager.PERMISSION_DENIED}); });
            require("Open app settings"); View recovery = activity.getWindow().getDecorView(); expose(recovery, "Open app settings", false); frame(recovery);
            i.runOnMainSync(() -> measured(recovery, "Open app settings", false));
            recreate(); require("Open app settings"); View rebuiltRecovery = activity.getWindow().getDecorView(); expose(rebuiltRecovery, "Open app settings", false); frame(rebuiltRecovery);
            i.runOnMainSync(() -> { measured(rebuiltRecovery, "Open app settings", false); int count = 0; List<View> views = new ArrayList<>(); views.add(rebuiltRecovery);
                for (int n = 0; n < views.size(); n++) { View view = views.get(n); if (view instanceof TextView text && "Open app settings".contentEquals(text.getText())) count++;
                    if (view instanceof ViewGroup group) for (int child = 0; child < group.getChildCount(); child++) views.add(group.getChildAt(child)); }
                assertEquals("One rebuilt recovery action", 1, count); });
            capture("automatic-permission-recovery", "Open app settings", false);
            AutomaticTrackingActivity cancelledRequest=(AutomaticTrackingActivity)activity;i.runOnMainSync(()->{try{pending.setBoolean(cancelledRequest,true);permissionRequest.setInt(cancelledRequest,91);}catch(IllegalAccessException bad){throw new AssertionError(bad);}cancelledRequest.onRequestPermissionsResult(91,new String[0],new int[0]);try{assertFalse("Canceled request releases pending ownership",pending.getBoolean(cancelledRequest));}catch(IllegalAccessException bad){throw new AssertionError(bad);}});
            click("Start conditions"); checked("Wait for charging", true); back(); require("Automatic tracking"); selected("At bedtime", true);
            i.runOnMainSync(() -> automaticNotifications[0] = true); recreate(); require("Access is available. Review your choices before arming tonight.");
            i.runOnMainSync(() -> assertNull("Access recovery action clears only after current access is available", find(activity.getWindow().getDecorView(), "Open app settings", false)));
            assertTrue("Denied result, recreation and access refresh never save the draft", automatic.getAll().isEmpty());
            Activity cancelledAutomatic = activity; ready(); device.pressBack(); await(cancelledAutomatic::isDestroyed); drain(cancelledAutomatic); assertTrue(automatic.getAll().isEmpty());

            launch(AutomaticTrackingActivity.class); click("At bedtime");
            assertTrue(automatic.edit().putBoolean("automaticArmed",true).commit());recreate();i.runOnMainSync(()->assertNull("A stale saved marker is not a live wait",find(activity.getWindow().getDecorView(),"Automatic wait active",false)));
            desc("Automatic tracking options");require("The previous wait stopped. Arm again to resume automatic starts.");recreate();capture("automatic-options","Done",false);blockedRoute("Repeat nightly",RecurringTrackingActivity.class);blockedRoute("Missing-night estimate",MissedNightActivity.class);assertTrue(automatic.edit().clear().commit());
            click("Sound recording");desc("Record sounds");checked("Record sounds",true);desc("Keep sound clips");checked("Keep sound clips",true);recreate();capture("automatic-sounds","Done",false);click("Done");require("Automatic tracking options");
            click("Sleep confirmation");desc("Require Google sleep estimate");desc("Reject awake starts");recreate();checked("Require Google sleep estimate",true);checked("Reject awake starts",true);capture("automatic-detection","Done",false);back();require("Automatic tracking options");back();require("Automatic tracking");click("After settling down");selected("After settling down",true);
            click("Start conditions");desc("Wait for charging");checked("Wait for charging",true);back();click("Activity access");recreate();i.runOnMainSync(()->{TextView state=(TextView)find(activity.getWindow().getDecorView(),"Physical activity access state",true);assertNotNull(state);assertEquals(c.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)==PackageManager.PERMISSION_GRANTED?"Allowed":"Not allowed",state.getText().toString());});capture("automatic-access","Done",false);blockedActivitySettings();click("Google estimate settings");checked("Require Google sleep estimate",true);back();require("Activity access");back();require("Automatic tracking");assertTrue("All child changes remain a private draft",automatic.getAll().isEmpty());
            // Synthetic retained callback with a modeled owner phase: native Arm remains forbidden.
            View[] staleArm={null};i.runOnMainSync(()->{staleArm[0]=find(activity.getWindow().getDecorView(),"Arm tonight",false);assertNotNull(staleArm[0]);assertTrue(staleArm[0].isEnabled());automaticPhase[0]="ARMED";staleArm[0].performClick();});require("A recording or automatic wait is already active. Open tracking to manage it.");assertTrue("Fresh active-phase guard did not save or dispatch",automatic.getAll().isEmpty());recreate();require("Automatic wait active");i.runOnMainSync(()->assertFalse("A live wait disables another Arm",find(activity.getWindow().getDecorView(),"Arm tonight",false).isEnabled()));blockedRoute("Automatic wait active",TrackingActivity.class);capture("automatic-active-wait","Save settings",false);
            Activity futureSaved=activity;click("Save settings");await(futureSaved::isDestroyed);drain(futureSaved);assertEquals("ARMED",automaticPhase[0]);assertEquals("STILLNESS",automatic.getString("automaticMode",""));assertTrue(automatic.getBoolean("automaticRecordSounds",false));assertTrue(automatic.getBoolean("automaticKeepClips",false));assertTrue(automatic.getBoolean("automaticGoogleSleep",false));assertTrue(ArmedWaitSettings.read(automatic).charging);
            launch(AutomaticTrackingActivity.class);selected("After settling down",true);require("Automatic wait active");capture("automatic-saved-reopened","Save settings",false);desc("Automatic tracking options");click("Sound recording");checked("Record sounds",true);checked("Keep sound clips",true);back();click("Sleep confirmation");checked("Require Google sleep estimate",true);checked("Reject awake starts",true);back();back();click("Manual only");Activity discarded=activity;ready();device.pressBack();await(discarded::isDestroyed);drain(discarded);assertEquals("STILLNESS",automatic.getString("automaticMode",""));automaticPhase[0]="";assertTrue(automatic.edit().clear().commit());

            launch(RecurringTrackingActivity.class); click("After settling down"); click("Start conditions"); desc("Only when charging"); checked("Only when charging", true);
            recreate(); checked("Only when charging", true); capture("recurring-conditions", "Done", false); click("Done"); selected("After settling down", true);
            checked("Nightly tracking", false); click("Start conditions"); checked("Only when charging", true); back(); finish(); assertTrue(recurring.getAll().isEmpty());

            launch(SleepSoundsActivity.class); desc("Sound settings"); click("Sources & playlists"); require("Sources & playlists");
            capture("sounds-sources", "Back to sound settings", false); back(); require("Sound settings"); click("Playback & tracking");
            capture("sounds-playback", "Back to sound settings", false); back(); require("Sound settings"); back(); require("Sleep sounds");
            assertTrue("Navigation did not save a source", mix.getAll().isEmpty());
            click("Timer"); desc("45 minutes"); selected("45 min", true); assertEquals(30, SoundPreferences.load(mix).timerMinutes);
            recreate(); selected("45 min", true); capture("timer-recreated", "45 minutes", true); click("Use 45 min timer");
            click("Fade out"); desc("10 minutes"); selected("10 min", true); click("Use 10 min fade"); finish();
            assertEquals(45, SoundPreferences.load(mix).timerMinutes); assertEquals(10, SoundPreferences.load(mix).fadeMinutes);
            launch(SleepSoundsActivity.class); click("Timer"); selected("45 min", true); click("Cancel"); click("Fade out"); selected("10 min", true);
            capture("fade-reopened", "Use 10 min fade", false); click("Cancel"); finish();
            // Configuration only: the actual Start/Arm/microphone/preflight actions are never pressed.
            launch(TrackingActivity.class); trackingMain("main", false);
            Activity setup = activity; int windows = owned.size(); desc("Phone movement selected"); ready();
            assertSame("Selecting the current phone sensor does not navigate", setup, activity);
            assertEquals("No unexpected sensor-settings window", windows, owned.size()); trackingRadio("Phone movement selected", true);
            desc("Record light levels"); checked("Record light levels", true);
            desc("More tracking settings"); require("Tracking settings"); capture("tracking-more", "Movement sensitivity", true);
            Activity settingsParent = openChild("Movement sensitivity", MotionSensorActivity.class);
            require("Motion sensor"); require("Waiting for motion readings. Missing data stays blank.");
            // A native SeekBar is a range control, not an OnClick action. Inspect its own
            // complete geometry and ACTION_SET_PROGRESS semantics; keep the capture action on the fixed footer.
            i.runOnMainSync(() -> {
                View root = activity.getWindow().getDecorView(); View range = find(root, "Motion sensitivity", true);
                assertTrue("Actual enabled native sensitivity range", range instanceof android.widget.SeekBar && range.isEnabled() && range.isFocusable());
                Rect visible = new Rect(); assertTrue("Sensitivity range is visible", range.getGlobalVisibleRect(visible));
                assertEquals("Sensitivity range complete width", range.getWidth(), visible.width());
                assertEquals("Sensitivity range complete height", range.getHeight(), visible.height());
                assertTrue("Sensitivity range 48dp width", visible.width() >= Design.dp(c, 48));
                assertTrue("Sensitivity range 48dp height", visible.height() >= Design.dp(c, 48));
                Rect bounds = screenBounds(range), save = measured(root, "Use this sensor", false);
                assertTrue("Sensitivity range clear of dock", bounds.bottom <= dockTop());
                assertTrue("Sensitivity range above fixed Use action", bounds.bottom <= save.top);
                android.view.accessibility.AccessibilityNodeInfo node = range.createAccessibilityNodeInfo();
                assertNotNull("Native sensitivity range semantics", node.getRangeInfo());
                assertEquals(0, node.getRangeInfo().getMin(), 0); assertEquals(2, node.getRangeInfo().getMax(), 0);
                assertEquals("Initial Medium position", 1, node.getRangeInfo().getCurrent(), 0);
                assertTrue("Native range supports actual progress input", node.getActionList().contains(android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS));
                View heading = find(root, "Sensitivity", false); assertNotNull(heading);
                assertNotNull("Actual current sensitivity label", find((View) heading.getParent(), "Medium", false));
            });
            capture("tracking-sensitivity", "Use this sensor", false);
            i.runOnMainSync(() -> {
                View target = find(activity.getWindow().getDecorView(), "Motion sensitivity", true); assertTrue(target instanceof android.widget.SeekBar);
                Bundle args = new Bundle(); args.putFloat(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_PROGRESS_VALUE, 2);
                assertTrue(target.performAccessibilityAction(android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS.getId(), args));
                assertEquals("Actual sensitivity draft changes", 2, ((android.widget.SeekBar) target).getProgress());
                View heading = find(activity.getWindow().getDecorView(), "Sensitivity", false); assertNotNull(heading);
                assertNotNull("Current sensitivity label changes, independently of static High endpoint", find((View) heading.getParent(), "High", false));
            });
            cancelChild(settingsParent); require("Tracking settings"); assertTrue("Cancelled sensitivity draft is not saved", motion.getAll().isEmpty());
            desc("Sampling & battery"); require("Sampling & battery"); capture("tracking-power", "Done", false);
            recreate(); require("Sampling & battery"); capture("tracking-power-recreated", "Done", false);
            back(); require("Tracking settings"); back(); recreate(); trackingMain("recreated", true);
            trackingRadio("Phone movement selected", true); checked("Record sounds", false); checked("Record light levels", true);
            Activity manualParent = openChild("Choose automatic start", AutomaticTrackingActivity.class);
            require("Automatic tracking"); selected("Manual only", true); assertNull("Manual mode has no Arm action", find(activity.getWindow().getDecorView(), "Arm tonight", false));
            capture("tracking-automatic-manual", "Save settings", false); cancelChild(manualParent);
            require("Manual"); checked("Record light levels", true); assertTrue("Automatic draft cancelled without Save/Arm", automatic.getAll().isEmpty()); finish();
            launch(TrackingActivity.class, new Intent(c, TrackingActivity.class).putExtra("sensor", "sonar"));
            trackingRadio("Contactless sonar selected", true); trackingRadio("Phone movement", false);
            Activity sonarSetup = activity; windows = owned.size(); desc("Phone movement");
            assertSame("Unselected phone card selects in place", sonarSetup, activity); assertEquals(windows, owned.size()); trackingMain("phone-selected", false); finish();
            TrackingDelayRuntime.drain(privateDelay); assertTrue("No delayed admission was written", delay.getAll().isEmpty());
            try (Store records = new Store(c, database)) { assertTrue("Setup creates no recording", records.rows("SELECT id FROM sessions").isEmpty()); }
            assertEquals("All twenty-eight actual frames captured", 28, captures); assertEquals("Only private setting stores changed", inert, personal.getAll()); silent();
        } catch (Exception | AssertionError failure) {
            try { assertTrue(device.takeScreenshot(new File(folder(), "organized-settings-failure.png")));
                device.dumpWindowHierarchy(new File(folder(), "organized-settings-failure.xml"));
                try (PrintWriter out = new PrintWriter(new File(folder(), "organized-settings-failure.txt"), "UTF-8")) { out.println(step); failure.printStackTrace(out); }
            } catch (Exception | AssertionError diagnostic) { failure.addSuppressed(diagnostic); }
            throw failure;
        } finally {
            boolean drained = false;
            try { closeAll(); TrackingDelayRuntime.drain(privateDelay); TrackingDelayRuntime.forget(privateDelay); drained = true; }
            finally {
                app.unregisterActivityLifecycleCallbacks(lifecycle);
                i.runOnMainSync(() -> { Records.factory = oldRecords; SoundSensitivitySettings.factory = oldSensitivity;
                    SoundPreferences.factory = oldMix; SoundRetentionJob.factory = oldRetention; SnoringCuesActivity.previewFactory = oldPreview;
                    automaticDraftPreferences=null; AutomaticTrackingActivity.port = oldAutomatic; RecurringTracking.port = oldRecurring;
                    TrackingActivity.commands = oldTracking; TrackingActivity.stores = oldTrackingStores;
                    MotionSettings.factory = oldMotionSettings; MotionSensorActivity.ports = oldMotionPorts; TrackingDelaySettings.factory = oldDelay; });
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
