package org.sleepwell.app;
import android.content.Context;
import org.json.JSONObject;
import java.io.*;
import java.util.*;
import static org.junit.Assert.*;
final class SoundLabelFixture implements AutoCloseable {
    final Context context;final String database="sleepwell-verification-"+UUID.randomUUID()+".db";final File file;final long start=System.currentTimeMillis()-3600000;final long session=1;
    SoundLabelFixture(Context context)throws Exception{this.context=context;File dir=new File(context.getFilesDir(),"clips");assertTrue(dir.isDirectory()||dir.mkdirs());file=new File(dir,"verification-label-"+UUID.randomUUID()+".wav");byte[] pcm=new byte[128000];for(int n=0;n<64000;n++){short value=(short)(Math.sin(n*.04)*12000);pcm[n*2]=(byte)value;pcm[n*2+1]=(byte)(value>>8);}NoiseRecorder.writeWave(file,pcm);try(Store store=open()){store.getWritableDatabase().execSQL("INSERT INTO sessions(id,start,end,source,origin,zone,active_ms,note) VALUES(1,?,?,'tracked','local','UTC',10000,'Preserve me')",new Object[]{start,start+10000});store.clip(1,start+4000,file.getName(),.4,4000);store.soundEvent(1,start+1000,1950,"snoring",.9,null);store.soundEvent(1,start+6000,1950,"speech",.8,null);SoundClips.favoriteEvent(store,event(store),true);store.epoch(1,start,start+1000,.2,.3,2d,10);store.getWritableDatabase().execSQL("INSERT INTO record_create_requests VALUES('preserved',1)");}}
    Store open(){return new Store(context,database);}
    JSONObject event(Store store){return store.rows("SELECT * FROM sound_events WHERE session=1 ORDER BY time LIMIT 1").get(0);}
    SoundLabels.Snapshot snapshot(Store store){RecordEntry night=Records.get(store,1);return new SoundLabels.Snapshot(event(store),night.updated,night.edit.start,night.edit.end);}
    public void close(){context.deleteDatabase(database);if(file.exists())assertTrue(file.delete());}
}
