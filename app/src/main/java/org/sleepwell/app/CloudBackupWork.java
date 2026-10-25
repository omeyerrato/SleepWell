package org.sleepwell.app;

import android.content.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.BooleanSupplier;

final class CloudBackupWork implements CloudBackupJob.Work {
    private final Context app;private final Records.Factory records;private volatile CloudBackupCopy.Provider provider;
    CloudBackupWork(Context context,Records.Factory records){app=context.getApplicationContext();this.records=records;}
    public CloudBackupCopy.Receipt save(BooleanSupplier active)throws Exception{Object lease=new Object();if(!CloudBackupLease.acquire(lease))throw new IllegalStateException("Another metadata backup is active. Retry when it finishes.");try{return saveOwned(active);}finally{CloudBackupLease.release(lease);}}
    private CloudBackupCopy.Receipt saveOwned(BooleanSupplier active)throws Exception{SharedPreferences prefs=CloudBackupSettings.factory.preferences(app);CloudBackupSettings.Destination destination=CloudBackupSettings.destination(prefs);if(destination.tree.isEmpty())throw new IllegalStateException("Choose a destination first.");long now=System.currentTimeMillis();String name="sleepwell-"+DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC).format(Instant.ofEpochMilli(now))+"-"+UUID.randomUUID()+".json";BooleanSupplier owned=()->active.getAsBoolean()&&CloudBackupSettings.current(prefs,destination);CloudBackupProvider selected=new CloudBackupProvider(app,destination.tree);provider=selected;if(!prefs.edit().putBoolean("in_progress",true).commit())throw new IllegalStateException("Backup status could not be saved.");
        try{CloudBackupCopy.Receipt receipt=CloudBackupCopy.save(check->CloudBackupSnapshot.prepare(app,records,check),selected,name,owned,created->CloudBackupSettings.record(prefs,new CloudBackupSettings.Entry(destination.tree,created.uri,created.name,now,0,"",false)));BackupIo.active(owned);CloudBackupSettings.record(prefs,new CloudBackupSettings.Entry(destination.tree,receipt.document.uri,receipt.document.name,now,receipt.size,receipt.sha,true));return receipt;}finally{provider=null;prefs.edit().putBoolean("in_progress",false).commit();}}
    public void cancel(){CloudBackupCopy.Provider active=provider;if(active!=null)active.cancel();}
}
