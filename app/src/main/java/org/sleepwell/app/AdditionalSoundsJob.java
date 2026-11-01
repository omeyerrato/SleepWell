package org.sleepwell.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.util.List;
import java.util.concurrent.*;

/** Retained UI work only. Never constructs capture, inference, playback or a permission owner. */
final class AdditionalSoundsJob {
    interface Observer { void changed(); }
    final Context context;
    final Records.Factory records;
    final long recordId, rawStart;
    final String token;
    final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private Observer observer;
    private boolean closed;
    boolean busy, loaded, preference;
    int offset, saveRevision;
    String error = "";
    RecordEntry record;
    AdditionalSoundStore.Snapshot snapshot;
    List<AdditionalSoundStore.Window> windows = List.of();

    AdditionalSoundsJob(Context context, long recordId, long rawStart, String token) {
        this.context = context.getApplicationContext();
        records = Records.factory;
        this.recordId = recordId;
        this.rawStart = rawStart;
        this.token = token == null ? "" : token;
    }
    void observe(Observer next) { observer = next; next.changed(); }
    void detach(Observer previous) { if (observer == previous) observer = null; }
    private void notifyObserver() { if (observer != null) observer.changed(); }
    void load(int requestedOffset) {
        if (busy || closed) return;
        busy = true; error = "";
        if (recordId != 0) { loaded = false; snapshot = null; windows = List.of(); }
        notifyObserver();
        worker.execute(() -> {
            try {
                if (recordId == 0) {
                    boolean value = AdditionalSoundSettings.requested(context);
                    deliver(() -> { preference = value; loaded = true; });
                    return;
                }
                if (recordId < 1 || rawStart < 1 || token.isEmpty() || Records.factory != records)
                    throw new IllegalStateException("This recording is unavailable. Reopen it from Sleep history.");
                try (Store store = records.open(context)) {
                    android.database.sqlite.SQLiteDatabase db = store.getReadableDatabase();
                    RecordEntry entry;
                    AdditionalSoundStore.Snapshot result;
                    List<AdditionalSoundStore.Window> page;
                    db.beginTransactionNonExclusive();
                    try {
                        entry = Records.get(store, recordId);
                        if (entry.rawStart != rawStart) throw new IllegalStateException("This recording changed. Reopen it from Sleep history.");
                        result = AdditionalSoundStore.read(store, recordId);
                        if (result.requested && (!token.equals(result.token) || result.rawStart != rawStart))
                            throw new IllegalStateException("These local results changed. Reopen this night from Sleep history.");
                        page = result.requested ? AdditionalSoundStore.page(store, result, requestedOffset, 20) : List.of();
                        db.setTransactionSuccessful();
                    } finally { db.endTransaction(); }
                    RecordEntry checkedRecord = entry;
                    AdditionalSoundStore.Snapshot checkedResult = result;
                    List<AdditionalSoundStore.Window> checkedPage = page;
                    deliver(() -> {
                        if (Records.factory != records) { unavailable(); return; }
                        record = checkedRecord; snapshot = checkedResult; windows = checkedPage;
                        offset = requestedOffset; loaded = true;
                    });
                }
            } catch (Exception unavailable) {
                deliver(() -> {
                    loaded = false; snapshot = null; windows = List.of();
                    error = recordId == 0 ? "Settings could not be read. Try again."
                            : "These local results are unavailable. The recording may have changed or been removed. Reopen it from Sleep history, or try again.";
                });
            }
        });
    }
    void save(boolean requested) {
        if (recordId != 0 || busy || closed || !loaded) return;
        busy = true; error = ""; notifyObserver();
        worker.execute(() -> {
            try {
                AdditionalSoundSettings.save(context, requested);
                deliver(() -> { preference = requested; saveRevision++; });
            } catch (RuntimeException failure) {
                deliver(() -> error = "Additional sounds could not be saved. Your draft is kept; try again.");
            }
        });
    }
    private void unavailable() {
        loaded = false; snapshot = null; windows = List.of();
        error = "This recording is no longer available. Reopen it from Sleep history.";
    }
    private void deliver(Runnable result) {
        main.post(() -> { if (closed) return; result.run(); busy = false; notifyObserver(); });
    }
    void awaitIdle(long timeoutMillis) throws Exception {
        if (worker.isShutdown()) {
            if (!worker.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS)) throw new TimeoutException("Additional sounds reader still active");
        } else worker.submit(() -> {}).get(timeoutMillis, TimeUnit.MILLISECONDS);
    }
    void close() { closed = true; observer = null; worker.shutdownNow(); }
}
