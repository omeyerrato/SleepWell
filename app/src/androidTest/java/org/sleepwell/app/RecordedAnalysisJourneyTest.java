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
import android.widget.TextView;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.security.MessageDigest;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import static org.junit.Assert.*;

/** API36 CI only. Real bundled CPU inference on a licensed fixture; no recording or audio output. */
public final class RecordedAnalysisJourneyTest {
    private static final String ESTIMATES = "recorded-sound-estimates-v1.db";
    private static final String PCM_SHA = "ddf4f22ae871bc993329571af9660f1daba0b5a6f89ea2d11f77e3e2d222c305";
    private static final String MODEL = "recorded-experimental-v1-f9dd5647-7bb42270";
    private static final int SAMPLES = 218720, DURATION = 13670;
    private final Instrumentation i = InstrumentationRegistry.getInstrumentation();
    private final Context c = i.getTargetContext();
    private final UiDevice device = UiDevice.getInstance(i);
    private final List<Activity> owned = new CopyOnWriteArrayList<>();
    private final Set<String> progress = new HashSet<>();
    private RecordedRecognitionActivity activity;
    private String step = "CI admission";
    private int frames;

    private void await(long limit, BooleanSupplier condition) throws Exception {
        long until = SystemClock.elapsedRealtime() + limit;
        while (!condition.getAsBoolean() && SystemClock.elapsedRealtime() < until) Thread.sleep(20);
        assertTrue(step, condition.getAsBoolean());
    }
    private void silent() {
        assertFalse(AlarmService.active); assertFalse(TrackingService.running);
        assertFalse(Prefs.flag(c, "ringing")); assertFalse(Prefs.flag(c, "tracking"));
        assertFalse(MicrophoneLease.occupied()); assertFalse(SoundImports.pending()); assertNull(SleepSoundService.snapshot());
    }
    private View find(View root, String text) {
        if (root instanceof TextView && text.contentEquals(((TextView) root).getText())) return root;
        if (root instanceof ViewGroup group) for (int n = 0; n < group.getChildCount(); n++) {
            View found = find(group.getChildAt(n), text); if (found != null) return found;
        }
        return null;
    }
    private void recordProgress(View root) {
        if (root instanceof TextView) {
            String text = ((TextView) root).getText().toString();
            if (text.startsWith("Analyzing window ") || text.equals("Loading the bundled offline model…")) progress.add(text);
        }
        if (root instanceof ViewGroup group) for (int n = 0; n < group.getChildCount(); n++) recordProgress(group.getChildAt(n));
    }
    private void ready() throws Exception {
        await(8000, () -> { boolean[] yes = {false}; i.runOnMainSync(() -> yes[0] = activity != null && !activity.isDestroyed()
                && !activity.isFinishing() && activity.hasWindowFocus() && activity.getWindow().getDecorView().isLaidOut()); return yes[0]; });
        silent();
    }
    private void require(String text) throws Exception {
        step = "require " + text; ready();
        await(8000, () -> { boolean[] yes = {false}; i.runOnMainSync(() -> yes[0] = find(activity.getWindow().getDecorView(), text) != null); return yes[0]; });
    }
    private View action(String text) {
        View view = find(activity.getWindow().getDecorView(), text); assertNotNull(text, view);
        while (!view.isClickable() && view.getParent() instanceof View) view = (View) view.getParent();
        assertTrue(text + " enabled action", view.isEnabled() && view.isClickable()); return view;
    }
    private void expose(String text) throws Exception {
        ready(); await(8000, () -> { boolean[] yes = {false}; i.runOnMainSync(() -> {
            View view = find(activity.getWindow().getDecorView(), text); if (view == null) return;
            view.requestRectangleOnScreen(new Rect(0, 0, view.getWidth(), view.getHeight()), true);
            Rect shown = new Rect(); yes[0] = view.getGlobalVisibleRect(shown) && shown.height() == view.getHeight();
        }); return yes[0]; });
    }
    private void frame() throws Exception {
        CountDownLatch committed = new CountDownLatch(1);
        i.runOnMainSync(() -> { View root = activity.getWindow().getDecorView(); assertTrue(activity.hasWindowFocus());
            assertTrue(root.isHardwareAccelerated()); root.getViewTreeObserver().registerFrameCommitCallback(committed::countDown); root.invalidate(); });
        assertTrue(step + " committed native frame", committed.await(5, TimeUnit.SECONDS));
    }
    private void click(String text) throws Exception {
        step = "single native input " + text;
        assertFalse("No output or document provider controls", Set.of("Choose a WAV file", "Preview cue", "Play sleep sounds").contains(text));
        ready();
        await(8000, () -> { boolean[] yes = {false}; i.runOnMainSync(() -> {
            View view = find(activity.getWindow().getDecorView(), text); if (view == null) return;
            while (!view.isClickable() && view.getParent() instanceof View) view = (View) view.getParent();
            view.requestRectangleOnScreen(new Rect(0, 0, view.getWidth(), view.getHeight()), true);
            Rect shown = new Rect(); yes[0] = view.getGlobalVisibleRect(shown) && shown.height() == view.getHeight() && shown.width() == view.getWidth();
        }); return yes[0]; });
        frame(); Rect[] bounds = {null};
        i.runOnMainSync(() -> {
            View view = action(text), root = activity.getWindow().getDecorView(); Rect shown = new Rect(); assertTrue(view.getGlobalVisibleRect(shown));
            assertEquals(view.getHeight(), shown.height()); assertEquals(view.getWidth(), shown.width());
            assertTrue(text + " 48dp width", shown.width() >= Design.dp(c, 48)); assertTrue(text + " 48dp height", shown.height() >= Design.dp(c, 48));
            assertNotNull(root.getRootWindowInsets()); int[] origin = new int[2]; root.getLocationOnScreen(origin);
            int dock = origin[1] + root.getHeight() - root.getRootWindowInsets().getInsets(WindowInsets.Type.navigationBars()).bottom;
            assertTrue(text + " above dock", shown.bottom <= dock); bounds[0] = shown;
        });
        assertTrue(device.click(bounds[0].centerX(), bounds[0].centerY())); i.waitForIdleSync(); silent();
    }
    private File folder() { File dir = new File(c.getExternalFilesDir(null), "verification"); assertTrue(dir.isDirectory() || dir.mkdirs()); return dir; }
    private void capture(String name, String visible) throws Exception {
        assertTrue("At most three real captures", frames < 3); step = "capture " + name; expose(visible); frame();
        assertTrue(device.takeScreenshot(new File(folder(), "recorded-analysis-" + name + ".png"))); frames++;
    }
    private RecordedRecognitionJob job(Activity a) throws Exception {
        Field field = RecordedRecognitionActivity.class.getDeclaredField("job"); field.setAccessible(true);
        return (RecordedRecognitionJob) field.get(a);
    }
    private void drain(Activity a) throws Exception {
        // Existing production job has no public drain hook. Read its worker only after Activity close.
        Field field = RecordedRecognitionJob.class.getDeclaredField("worker"); field.setAccessible(true);
        ExecutorService worker = (ExecutorService) field.get(job(a)); assertTrue(worker.isShutdown());
        assertTrue("Owned model/reader worker terminates before fixture deletion", worker.awaitTermination(20000, TimeUnit.MILLISECONDS));
    }
    private void launch(long record, long updated) throws Exception {
        Instrumentation.ActivityMonitor monitor = i.addMonitor(RecordedRecognitionActivity.class.getName(), null, false);
        try {
            c.startActivity(new Intent(c, RecordedRecognitionActivity.class).putExtra("record", record).putExtra("updated", updated)
                    .putExtra("visualReview", true).putExtra("verificationLabel", "CI licensed recording fixture · no output").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            activity = (RecordedRecognitionActivity) monitor.waitForActivityWithTimeout(5000); assertNotNull(activity);
        } finally { i.removeMonitor(monitor); }
        ready();
        RecordedRecognitionActivity screen = activity;
        i.runOnMainSync(() -> screen.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(
                () -> recordProgress(screen.getWindow().getDecorView())));
    }
    private void finish() throws Exception {
        Activity old = activity; i.runOnMainSync(old::finish); await(8000, old::isDestroyed); drain(old);
    }
    private String sha(byte[] bytes) throws Exception {
        StringBuilder out = new StringBuilder(); for (byte b : MessageDigest.getInstance("SHA-256").digest(bytes))
            out.append(Character.forDigit((b >>> 4) & 15, 16)).append(Character.forDigit(b & 15, 16)); return out.toString();
    }
    private byte[] fixture() throws Exception {
        // Whole, already pinned CC0 preview from third_party/audio-fixtures/laughter-holdout.json.
        try (InputStream input = i.getContext().getAssets().open("classifier-fixtures/laughter-morning.pcm");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192]; for (int n; (n = input.read(buffer)) != -1;) { assertTrue(output.size() + n <= SAMPLES * 2); output.write(buffer, 0, n); }
            byte[] pcm = output.toByteArray(); assertEquals(SAMPLES * 2, pcm.length); assertEquals(PCM_SHA, sha(pcm)); return pcm;
        }
    }
    private byte[] wav(byte[] pcm) {
        ByteBuffer out = ByteBuffer.allocate(44 + pcm.length).order(ByteOrder.LITTLE_ENDIAN);
        out.put(new byte[]{'R','I','F','F'}).putInt(36 + pcm.length).put(new byte[]{'W','A','V','E','f','m','t',' '})
                .putInt(16).putShort((short) 1).putShort((short) 1).putInt(16000).putInt(32000).putShort((short) 2).putShort((short) 16)
                .put(new byte[]{'d','a','t','a'}).putInt(pcm.length).put(pcm); return out.array();
    }
    private void finite(JSONObject estimate) throws Exception {
        for (String key : List.of("score", "competitor")) { double value = estimate.getDouble(key); assertTrue(Double.isFinite(value) && value >= 0 && value <= 1); }
        assertTrue(estimate.get("qualifies") instanceof Boolean);
    }
    private List<String> snapshotRows(RecordedRecognitionStore store) throws Exception {
        List<String> rows = new ArrayList<>(); for (RecordedRecognitionStore.Entry entry : store.entries())
            rows.add(entry.id + "|" + entry.created + "|" + entry.source + "|" + entry.result); return rows;
    }
    private void restorePreferences(Map<String, ?> original) {
        SharedPreferences.Editor edit = Prefs.of(c).edit().clear();
        for (Map.Entry<String, ?> entry : original.entrySet()) {
            String key = entry.getKey(); Object value = entry.getValue();
            if (value instanceof Boolean) edit.putBoolean(key, (Boolean) value);
            else if (value instanceof Integer) edit.putInt(key, (Integer) value);
            else if (value instanceof Long) edit.putLong(key, (Long) value);
            else if (value instanceof Float) edit.putFloat(key, (Float) value);
            else if (value instanceof String) edit.putString(key, (String) value);
            else if (value instanceof Set<?>) { Set<String> strings = new HashSet<>(); for (Object item : (Set<?>) value) strings.add((String) item); edit.putStringSet(key, strings); }
            else throw new AssertionError("Unsupported original preference type");
        }
        assertTrue("Restore original preference values and absence", edit.commit());
        assertEquals(original, Prefs.of(c).getAll());
    }

    @Test public void savedClipRunsBundledModelsPersistsAndDeletesSummary() throws Exception {
        assertEquals("true", InstrumentationRegistry.getArguments().getString("ciReview"));
        assertEquals("1", device.executeShellCommand("getprop ro.kernel.qemu").trim()); assertEquals(36, android.os.Build.VERSION.SDK_INT); silent();
        assertFalse(Prefs.flag(c, "trackingPaused")); assertEquals(0L, Prefs.of(c).getLong("session", 0));
        Map<String, ?> originalPreferences = new HashMap<>(Prefs.of(c).getAll());
        boolean estimatesExisted = c.getDatabasePath(ESTIMATES).exists(); long originalRevision; List<String> originalRows;
        try (RecordedRecognitionStore store = new RecordedRecognitionStore(c)) { originalRevision = store.revision(); originalRows = snapshotRows(store); }
        assertTrue("This native CI journey requires an initially empty dedicated estimate DB", originalRows.isEmpty());
        String database = "recorded-analysis-fixture-" + UUID.randomUUID() + ".db";
        long record = 1_000_000_000_000L + Math.floorMod(UUID.randomUUID().getMostSignificantBits(), 1_000_000_000L), start = 1700000000000L;
        byte[] wave = wav(fixture()); String wavHash = sha(wave); File audio = FileSafety.clip(c, "native-analysis-" + UUID.randomUUID() + ".wav");
        assertTrue(audio.getParentFile().isDirectory() || audio.getParentFile().mkdirs()); Files.write(audio.toPath(), wave);
        Records.Factory previous = Records.factory; long clip, updated;
        try (Store store = new Store(c, database)) {
            store.getWritableDatabase().execSQL("INSERT INTO sessions(id,start,end,active_ms,source,origin,sensor,zone) VALUES(?,?,?,?,?,?,?,?)",
                    new Object[]{record, start, start + DURATION, DURATION, "tracked", "local", SensorProvenance.MOTION, "UTC"});
            store.clip(record, start + DURATION, audio.getName(), .2, DURATION);
            clip = store.rows("SELECT id FROM clips WHERE session=?", "" + record).get(0).getLong("id"); updated = Records.get(store, record).updated;
        }
        List<String> sourceBefore;
        try (Store store = new Store(c, database)) { sourceBefore = List.of(store.rows("SELECT * FROM sessions").toString(), store.rows("SELECT * FROM clips").toString(), store.rows("SELECT * FROM sound_events").toString()); }
        Application app = (Application) c.getApplicationContext();
        Application.ActivityLifecycleCallbacks observer = new Application.ActivityLifecycleCallbacks() {
            public void onActivityCreated(Activity a, Bundle state) { owned.add(a); assertTrue("Only inert recorded-analysis window", a instanceof RecordedRecognitionActivity);
                assertTrue(a.getIntent().getBooleanExtra("visualReview", false)); }
            public void onActivityStarted(Activity a) {} public void onActivityResumed(Activity a) {} public void onActivityPaused(Activity a) {}
            public void onActivityStopped(Activity a) {} public void onActivitySaveInstanceState(Activity a, Bundle state) {} public void onActivityDestroyed(Activity a) {}
        };
        app.registerActivityLifecycleCallbacks(observer); boolean drained = false; String savedId = "";
        try {
            // Own only these four inert CI defaults during the flow. A delayed BOOT_COMPLETED
            // may write the same values; every key is still compared against this full baseline.
            SharedPreferences idle = Prefs.of(c); SharedPreferences.Editor initialize = idle.edit();
            for (String key : List.of("ringing", "tracking", "trackingPaused")) if (!idle.contains(key)) initialize.putBoolean(key, false);
            if (!idle.contains("session")) initialize.putLong("session", 0L);
            assertTrue(initialize.commit()); Map<String, ?> preferences = new HashMap<>(idle.getAll());
            i.runOnMainSync(() -> Records.factory = context -> new Store(context, database));
            launch(record, updated); String recording = "Recording ending " + RecordUi.preciseClock(c, start + DURATION, java.time.ZoneId.systemDefault());
            require(recording); click(recording); require("Review this copy, then explicitly start analysis."); require("Analyze this copy");
            try (RecordedRecognitionStore store = new RecordedRecognitionStore(c)) { assertEquals(originalRows, snapshotRows(store)); }
            capture("prepared", "Analyze this copy"); click("Analyze this copy");
            step = "real bundled CPU inference and successful private Save";
            await(180000, () -> { boolean[] done = {false}; i.runOnMainSync(() -> {
                try { RecordedRecognitionJob job = job(activity); assertNotEquals("Native inference failed: " + job.message, RecordedRecognitionJob.Phase.ERROR, job.phase);
                    recordProgress(activity.getWindow().getDecorView()); done[0] = find(activity.getWindow().getDecorView(), "Saved on this phone. These are experimental estimates, not confirmed events.") != null;
                } catch (RuntimeException | AssertionError failure) { throw failure; } catch (Exception failure) { throw new AssertionError(failure); }
            }); return done[0]; });
            i.runOnMainSync(() -> assertTrue("Actual in-flight native progress reached the UI", progress.stream().anyMatch(s -> s.matches("Analyzing window [01] of 2…"))));
            assertEquals("Native analysis preserves every baseline preference", preferences, Prefs.of(c).getAll());
            RecordedRecognitionStore.Entry saved;
            try (RecordedRecognitionStore store = new RecordedRecognitionStore(c)) { List<RecordedRecognitionStore.Entry> rows = store.entries(); assertEquals(1, rows.size()); saved = rows.get(0); assertEquals(originalRevision, store.revision()); }
            savedId = saved.id; JSONObject source = saved.source, result = saved.result;
            assertEquals("saved_clip", source.getString("kind")); assertEquals(record, source.getLong("record")); assertEquals(clip, source.getLong("clip"));
            assertEquals(0, source.getLong("event")); assertEquals(updated, source.getLong("updated")); assertEquals(0, source.getLong("fromMs"));
            assertEquals(start, source.getLong("startWall")); assertEquals(DURATION, source.getLong("durationMs")); assertEquals(16000, source.getInt("sampleRate"));
            assertEquals(SAMPLES, source.getInt("samples")); assertEquals(PCM_SHA, source.getString("pcmSha256")); assertEquals(wavHash, source.getString("wavSha256"));
            assertTrue(source.getString("fingerprint").matches("[a-f0-9]{64}")); assertEquals(1, result.getInt("schema")); assertEquals(MODEL, result.getString("modelVersion"));
            assertEquals(DURATION, result.getLong("analyzedMs")); assertEquals(0, result.getLong("omittedMs")); JSONArray windows = result.getJSONArray("windows"); assertEquals(2, windows.length());
            boolean pannResponded = false, efficientResponded = false;
            for (int n = 0; n < windows.length(); n++) { JSONObject window = windows.getJSONObject(n); assertEquals(n * 10000L, window.getLong("startMs"));
                assertEquals(n == 0 ? 10000L : 3670L, window.getLong("durationMs")); assertTrue(Set.of("uncertain", "laughter", "instrument").contains(window.getString("candidate")));
                finite(window.getJSONObject("laughter")); finite(window.getJSONObject("instrument"));
                pannResponded |= window.getJSONObject("laughter").getDouble("score") > 0;
                efficientResponded |= window.getJSONObject("instrument").getDouble("score") > 0; }
            assertTrue("Both real model families returned nonzero finite estimates", pannResponded && efficientResponded);
            // Functional execution is independent of accuracy. No candidate label is required to pass.
            require("Model: " + MODEL); capture("result", "Experimental window estimates"); finish();
            launch(record, updated); require(RecordUi.date(saved.created, java.time.ZoneId.systemDefault())); click(RecordUi.date(saved.created, java.time.ZoneId.systemDefault()));
            require("Experimental window estimates"); require("Model: " + MODEL); capture("reopened", "Experimental window estimates");
            try (RecordedRecognitionStore store = new RecordedRecognitionStore(c)) { RecordedRecognitionStore.Entry reopened = store.entries().get(0);
                assertEquals(savedId, reopened.id); assertEquals(saved.source.toString(), reopened.source.toString()); assertEquals(saved.result.toString(), reopened.result.toString()); }
            click("Delete this analysis"); require("Saved analysis deleted. Original audio is unchanged.");
            try (RecordedRecognitionStore store = new RecordedRecognitionStore(c)) { assertEquals(originalRows, snapshotRows(store)); assertEquals(originalRevision, store.revision()); }
            assertEquals(wavHash, sha(Files.readAllBytes(audio.toPath())));
            try (Store store = new Store(c, database)) { assertEquals(sourceBefore, List.of(store.rows("SELECT * FROM sessions").toString(), store.rows("SELECT * FROM clips").toString(), store.rows("SELECT * FROM sound_events").toString())); }
            JSONArray[] seenProgress = {null}; i.runOnMainSync(() -> seenProgress[0] = new JSONArray(new ArrayList<>(progress)));
            JSONObject proof = new JSONObject().put("fixture", "laughter-morning").put("pcmSha256", PCM_SHA).put("wavSha256", wavHash)
                    .put("modelVersion", result.getString("modelVersion")).put("samples", SAMPLES).put("analyzedMs", result.getLong("analyzedMs"))
                    .put("omittedMs", result.getLong("omittedMs")).put("windows", result.getJSONArray("windows")).put("observedUiProgress", seenProgress[0])
                    .put("savedAndReopened", true).put("deleted", true).put("originalAudioUnchanged", true).put("accuracyAcceptanceRequired", false);
            try (PrintWriter out = new PrintWriter(new File(folder(), "recorded-analysis-proof.json"), "UTF-8")) { out.println(proof.toString(2)); }
            assertEquals(3, frames); assertEquals(preferences, Prefs.of(c).getAll()); silent();
        } catch (Exception | AssertionError failure) {
            try { device.dumpWindowHierarchy(new File(folder(), "recorded-analysis-failure.xml"));
                try (PrintWriter out = new PrintWriter(new File(folder(), "recorded-analysis-failure.txt"), "UTF-8")) { out.println(step); failure.printStackTrace(out); }
            } catch (Exception diagnostic) { failure.addSuppressed(diagnostic); } throw failure;
        } finally {
            try { i.runOnMainSync(() -> { List<Activity> reverse = new ArrayList<>(owned); Collections.reverse(reverse); for (Activity a : reverse) if (!a.isDestroyed()) a.finish(); });
                await(8000, () -> owned.stream().allMatch(Activity::isDestroyed)); for (Activity a : owned) drain(a); drained = true;
            } finally {
                app.unregisterActivityLifecycleCallbacks(observer); i.runOnMainSync(() -> Records.factory = previous);
                if (drained) {
                    try {
                        // Delete only this journey's UUID result, even when failure happened before it was assigned above.
                        synchronized (RecordedRecognitionStore.LOCK) {
                            try (RecordedRecognitionStore store = new RecordedRecognitionStore(c)) {
                                for (RecordedRecognitionStore.Entry entry : store.entries())
                                    if (entry.source.optLong("record") == record && entry.source.optLong("clip") == clip
                                            && wavHash.equals(entry.source.optString("wavSha256")) && PCM_SHA.equals(entry.source.optString("pcmSha256"))) store.delete(entry.id);
                                assertEquals(originalRows, snapshotRows(store)); assertEquals(originalRevision, store.revision());
                            }
                            if (!estimatesExisted) assertTrue("Remove only our newly created empty estimate DB", c.deleteDatabase(ESTIMATES));
                        }
                        assertTrue(audio.delete()); c.deleteDatabase(database);
                    } finally { restorePreferences(originalPreferences); }
                }
                assertEquals(originalPreferences, Prefs.of(c).getAll()); silent();
            }
        }
    }
}
