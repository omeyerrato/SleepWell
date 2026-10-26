package org.sleepwell.app;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Looper;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/** Explicit, offline supplementary estimates for recorded audio, never a live alarm/cue authority. */
final class RecordedSoundAnalyzer implements AutoCloseable {
    static final int SAMPLE_RATE = 16000, MAX_SAMPLES = 1_920_000;
    static final String MODEL_VERSION = "recorded-experimental-v1-f9dd5647-7bb42270";
    private static final int WINDOW = 160000, MIN_TAIL = 32000, CLASSES = 527;
    private static final AtomicBoolean LEASE = new AtomicBoolean();
    private final Thread owner = Thread.currentThread();
    private final OrtEnvironment environment;
    private final RecordedSoundFrontend frontend;
    private final String[] labels;
    private OrtSession pann, efficient;
    private boolean closed;

    interface Progress { void update(int completed, int total); }

    RecordedSoundAnalyzer(Context context) throws Exception {
        if (Looper.myLooper() == Looper.getMainLooper())
            throw new IllegalStateException("Recorded sound analysis requires a worker");
        if (!LEASE.compareAndSet(false, true))
            throw new IllegalStateException("Another recorded sound analysis is running");
        try {
            AssetManager assets = context.getApplicationContext().getAssets();
            frontend = new RecordedSoundFrontend(
                    floats(assets, "resample.f32", 164, "053fb2664abea7baaaacd8ab2c3db08672c50f342e785a03c284edbedbd06ba5"),
                    floats(assets, "pann-window.f32", 4096, "1f0e971af59f6c758249e2fefac7479a16984255ead2c5ec82715b2b613d311b"),
                    floats(assets, "efficient-window.f32", 4096, "d8d7d93900a463a83ba04971a3d5c42cbd1ed8a8819df689d42edf0baf02dbf0"),
                    floats(assets, "pann-mel.f32", 131328, "bd0892e4dbf4fd016fc8fb21105b1f3cad415609aa6e01dfcfd85c62f9795f7d"),
                    floats(assets, "efficient-mel.f32", 262656, "ac487dbcdc9c3e6fa3cc9c3fc6226e5bb16f38b5b02a051dd838be9e75f27017"));
            labels = new String(read(assets, "labels.txt", 6864,
                    "31560a0197c803832cedbba10d1fc8a70469983abeb954368be7bae83d975648"), StandardCharsets.UTF_8).split("\\r?\\n");
            if (labels.length != CLASSES) throw new IllegalStateException("Invalid sound class map");
            environment = OrtEnvironment.getEnvironment();
            try (OrtSession.SessionOptions options = new OrtSession.SessionOptions()) {
                options.setIntraOpNumThreads(1); options.setInterOpNumThreads(1);
                options.setExecutionMode(OrtSession.SessionOptions.ExecutionMode.SEQUENTIAL);
                pann = environment.createSession(read(assets, "panns-laughter.onnx", 16372202,
                        "f9dd56478e434e067cef505489d0ae4483d80d357e2c35bc3d94ba432eb08f3f"), options);
                efficient = environment.createSession(read(assets, "efficientat-instruments.onnx", 19516171,
                        "7bb4227052d796a1ef35f0a5b703fdd724cd200fc7e6f85087289222e6c5b5c8"), options);
            }
        } catch (Exception | LinkageError failure) {
            try { if (pann != null) pann.close(); } catch (Exception cleanup) { failure.addSuppressed(cleanup); }
            try { if (efficient != null) efficient.close(); } catch (Exception cleanup) { failure.addSuppressed(cleanup); }
            LEASE.set(false); throw failure;
        }
    }

    Result analyze(short[] monoPcm16, BooleanSupplier cancelled, Progress progress) throws Exception {
        owned();
        if (monoPcm16 == null || monoPcm16.length > MAX_SAMPLES || cancelled == null || progress == null)
            throw new IllegalArgumentException("Expected at most 120 seconds of mono 16 kHz PCM16");
        int total = monoPcm16.length / WINDOW + (monoPcm16.length % WINDOW >= MIN_TAIL ? 1 : 0);
        List<Window> windows = new ArrayList<>();
        int processed = 0;
        RecordedSoundFrontend.check(cancelled); progress.update(0, total);
        for (int start = 0; start < monoPcm16.length; start += WINDOW) {
            int count = Math.min(WINDOW, monoPcm16.length - start);
            if (count < MIN_TAIL) break;
            float[] samples = frontend.resample(monoPcm16, start, count, cancelled);
            float[] pm = frontend.pann(samples, cancelled);
            float[] ps = infer(pann, pm, new long[]{1, 1, pm.length / 64, 64}, cancelled);
            float[] em = frontend.efficient(samples, cancelled);
            float[] es = infer(efficient, em, new long[]{1, 1, 128, em.length / 128}, cancelled);
            Estimate laugh = estimate(ps, false), instrument = estimate(es, true);
            int topInstrument = 138;
            for (int i = 139; i < CLASSES; i++)
                if (instrumentIndex(i) && es[i] > es[topInstrument]) topInstrument = i;
            String candidate = laugh.qualifies ? "laughter" : instrument.qualifies ? "instrument" : "uncertain";
            RecordedSoundFrontend.check(cancelled);
            windows.add(new Window(start / 16L, count / 16L, laugh, instrument, candidate, labels[topInstrument]));
            processed += count;
            progress.update(windows.size(), total);
        }
        RecordedSoundFrontend.check(cancelled);
        return new Result(windows, processed / 16L, (monoPcm16.length - processed) / 16L);
    }

    private float[] infer(OrtSession session, float[] mel, long[] shape, BooleanSupplier cancelled) throws Exception {
        RecordedSoundFrontend.check(cancelled);
        try (OnnxTensor input = OnnxTensor.createTensor(environment, FloatBuffer.wrap(mel), shape);
             OrtSession.Result output = session.run(Map.of("mel", input))) {
            RecordedSoundFrontend.check(cancelled);
            Object value = output.get(0).getValue();
            if (!(value instanceof float[][])) throw new IllegalStateException("Invalid sound model output");
            float[][] rows = (float[][]) value;
            if (rows.length != 1 || rows[0].length != CLASSES)
                throw new IllegalStateException("Invalid sound class count");
            float[] scores = rows[0];
            for (float score : scores) if (!Float.isFinite(score) || score < 0 || score > 1)
                throw new IllegalStateException("Invalid sound estimate");
            return scores;
        }
    }

    private static boolean instrumentIndex(int i) {
        return (i >= 138 && i <= 199) || (i >= 208 && i <= 215);
    }
    private static Estimate estimate(float[] scores, boolean instruments) {
        double score = 0, competitor = 0;
        for (int i = 0; i < scores.length; i++) {
            boolean member = instruments ? instrumentIndex(i) : i >= 16 && i <= 21;
            if (member) score = Math.max(score, scores[i]);
            else if (!instruments || i != 137) competitor = Math.max(competitor, scores[i]);
        }
        return new Estimate(score, competitor, instruments
                ? score >= .65 && score - competitor >= .15
                : score >= .10 && score - competitor >= .025);
    }

    private static float[] floats(AssetManager assets, String name, int length, String sha) throws Exception {
        byte[] bytes = read(assets, name, length, sha);
        float[] result = new float[length / 4];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(result);
        for (float value : result) if (!Float.isFinite(value)) throw new IllegalStateException("Invalid sound frontend");
        return result;
    }
    private static byte[] read(AssetManager assets, String name, int length, String sha) throws Exception {
        try (InputStream input = assets.open("recorded-sound/" + name);
             ByteArrayOutputStream output = new ByteArrayOutputStream(length)) {
            byte[] block = new byte[8192]; int size = 0, count;
            while ((count = input.read(block)) != -1) {
                size += count;
                if (size > length) throw new IllegalStateException("Invalid sound model size");
                output.write(block, 0, count);
            }
            byte[] bytes = output.toByteArray();
            if (size != length) throw new IllegalStateException("Incomplete sound model");
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder actual = new StringBuilder(64);
            for (byte b : digest) actual.append(Character.forDigit((b >>> 4) & 15, 16)).append(Character.forDigit(b & 15, 16));
            if (!sha.contentEquals(actual)) throw new IllegalStateException("Sound model integrity check failed");
            return bytes;
        }
    }
    private void owned() {
        if (Thread.currentThread() != owner || closed) throw new IllegalStateException("Sound analyzer worker is closed or changed");
    }
    @Override public void close() throws Exception {
        if (Thread.currentThread() != owner) throw new IllegalStateException("Sound analyzer worker changed");
        if (closed) return;
        closed = true;
        try { if (pann != null) pann.close(); }
        finally { try { if (efficient != null) efficient.close(); } finally { LEASE.set(false); } }
        // OrtEnvironment is process-owned; closing it here would invalidate another ONNX feature.
    }

    static final class Estimate {
        final double score, competitor; final boolean qualifies;
        Estimate(double score, double competitor, boolean qualifies) { this.score = score; this.competitor = competitor; this.qualifies = qualifies; }
        JSONObject json() throws JSONException { return new JSONObject().put("score", score).put("competitor", competitor).put("qualifies", qualifies); }
    }
    static final class Window {
        final long startMs, durationMs; final Estimate laughter, instrument;
        final String candidate, instrumentLabel;
        Window(long startMs, long durationMs, Estimate laughter, Estimate instrument, String candidate, String instrumentLabel) {
            this.startMs = startMs; this.durationMs = durationMs; this.laughter = laughter; this.instrument = instrument;
            this.candidate = candidate; this.instrumentLabel = instrumentLabel;
        }
        JSONObject json() throws JSONException {
            return new JSONObject().put("startMs", startMs).put("durationMs", durationMs).put("candidate", candidate)
                    .put("instrumentLabel", instrumentLabel).put("laughter", laughter.json()).put("instrument", instrument.json());
        }
    }
    static final class Result {
        final List<Window> windows; final long analyzedMs, omittedMs;
        final String modelVersion = MODEL_VERSION;
        Result(List<Window> windows, long analyzedMs, long omittedMs) {
            this.windows = Collections.unmodifiableList(new ArrayList<>(windows)); this.analyzedMs = analyzedMs; this.omittedMs = omittedMs;
        }
        JSONObject json() throws JSONException {
            JSONArray rows = new JSONArray(); for (Window window : windows) rows.put(window.json());
            return new JSONObject().put("schema", 1).put("modelVersion", modelVersion).put("analyzedMs", analyzedMs)
                    .put("omittedMs", omittedMs).put("windows", rows);
        }
    }
}
