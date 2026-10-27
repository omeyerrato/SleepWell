package org.sleepwell.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.DocumentsContract;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Locale;

/** Test APK only: bounded private fixture provisioning, never a file/query/open surface. */
public final class RecordedAnalysisFixtureProvider extends ContentProvider {
    static final String AUTHORITY = "org.sleepwell.app.test.recorded-analysis-fixture";
    private static final String TARGET = "org.sleepwell.app";
    @Override public boolean onCreate() { return true; }
    private void guard() {
        if (Build.VERSION.SDK_INT != 36 || !Build.MODEL.startsWith("sdk_gphone64") || !Build.HARDWARE.equals("ranchu"))
            throw new SecurityException("Dedicated API36 emulator fixture only");
        int caller = Binder.getCallingUid();
        try {
            int target = getContext().getPackageManager().getApplicationInfo(TARGET, 0).uid;
            String[] packages = getContext().getPackageManager().getPackagesForUid(caller);
            if (caller < 10000 || caller == Process.myUid() || caller != target || packages == null
                    || packages.length != 1 || !TARGET.equals(packages[0])) throw new SecurityException("Exact nonshared target UID required");
        } catch (android.content.pm.PackageManager.NameNotFoundException absent) { throw new SecurityException("Target app unavailable", absent); }
    }
    private void documentGrant(Uri uri, boolean grant) {
        // Caller was authenticated above; the test-APK owner grants/revokes its own document URI.
        long identity = Binder.clearCallingIdentity();
        try { if (grant) getContext().grantUriPermission(TARGET, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            else getContext().revokeUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } finally { Binder.restoreCallingIdentity(identity); }
    }
    @Override public Bundle call(String method, String uuid, Bundle extras) {
        guard();
        if (!"prepareRecordedAnalysis".equals(method) && !"clearRecordedAnalysis".equals(method))
            throw new IllegalArgumentException("Unsupported fixture operation");
        File file = ImportDocuments.analysisFile(getContext(), uuid);
        String id = "recorded-analysis-" + uuid;
        Uri uri = DocumentsContract.buildDocumentUri(ImportDocuments.AUTHORITY, id);
        synchronized (ImportDocuments.class) {
            try {
                Bundle result = new Bundle();
                if (method.equals("clearRecordedAnalysis")) {
                    documentGrant(uri, false);
                    if (file.exists() && !file.delete()) throw new IOException("Fixture cleanup failed");
                    result.putBoolean("absent", !file.exists()); return result;
                }
                byte[] bytes = extras == null ? null : extras.getByteArray("bytes");
                try {
                    String expected = extras == null ? null : extras.getString("sha256");
                    if (bytes == null || bytes.length < 32 || bytes.length > 256 * 1024 || expected == null || !expected.matches("[a-f0-9]{64}"))
                        throw new IllegalArgumentException("Bounded encoded fixture required");
                    StringBuilder hash = new StringBuilder();
                    for (byte value : MessageDigest.getInstance("SHA-256").digest(bytes)) hash.append(String.format(Locale.ROOT, "%02x", value & 255));
                    if (!hash.toString().equals(expected) || bytes[4] != 'f' || bytes[5] != 't' || bytes[6] != 'y' || bytes[7] != 'p')
                        throw new IllegalArgumentException("M4A fixture/hash mismatch");
                    if (!file.createNewFile()) throw new IOException("Fixture UUID already exists");
                    try (FileOutputStream output = new FileOutputStream(file)) { output.write(bytes); output.getFD().sync(); }
                    catch (IOException failure) { file.delete(); throw failure; }
                    documentGrant(uri, true);
                    result.putString("uri", uri.toString()); result.putString("documentId", id);
                    result.putLong("size", file.length()); result.putString("sha256", expected); return result;
                } finally { if (bytes != null) Arrays.fill(bytes, (byte) 0); }
            } catch (IOException | java.security.NoSuchAlgorithmException failure) { throw new IllegalStateException("Private CI fixture unavailable", failure); }
        }
    }
    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] args, String sort) { throw new UnsupportedOperationException("No query surface"); }
    @Override public String getType(Uri uri) { throw new UnsupportedOperationException("No file surface"); }
    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException("No insert surface"); }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] args) { throw new UnsupportedOperationException("No update surface"); }
    @Override public int delete(Uri uri, String selection, String[] args) { throw new UnsupportedOperationException("Use UUID-only cleanup"); }
}
