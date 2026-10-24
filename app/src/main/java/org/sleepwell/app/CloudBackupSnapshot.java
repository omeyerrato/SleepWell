package org.sleepwell.app;

import android.content.Context;
import java.io.*;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.json.JSONObject;

/** Uses the actual BackupDocuments JSON writer, including its format and 8 MiB admission. */
final class CloudBackupSnapshot {
    static byte[] prepare(Context context,Records.Factory records,BooleanSupplier active)throws Exception{
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();BackupDocuments.Transport staging=new BackupDocuments.Transport(){public InputStream input(String uri)throws IOException{throw new IOException("Snapshot is write-only");}public OutputStream output(String uri){return BackupIo.limited(bytes);}public void cancel(){}};
        BackupDocuments.Effects unused=new BackupDocuments.Effects(){public void idle(long generation){throw new IllegalStateException("No restore during snapshot");}public void retired(List<Alarm> old,Store store){throw new IllegalStateException("No restore during snapshot");}public void recover(Store store){throw new IllegalStateException("No restore during snapshot");}};
        new BackupDocuments(context,records,staging,unused).write(BackupJob.Kind.JSON,"content://org.sleepwell.app/cloud-snapshot",active);byte[] result=bytes.toByteArray();Backup.validate(new JSONObject(new String(result,java.nio.charset.StandardCharsets.UTF_8)));return result;
    }
}
