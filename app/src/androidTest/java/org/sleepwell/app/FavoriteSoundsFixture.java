package org.sleepwell.app;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.JSONObject;
import java.io.*;
import java.util.*;
import static org.junit.Assert.*;

final class FavoriteSoundsFixture implements AutoCloseable {
    final Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();final String database="sleepwell-verification-"+UUID.randomUUID()+".db";final long now=System.currentTimeMillis();final List<File> files=new ArrayList<>();final Store store=new Store(context,database);long recent,old,analysis;JSONObject recentEvent,oldEvent,analysisEvent,recentClip;
    long night(long id,int days,long duration){long start=now-days*86400000L;store.getWritableDatabase().execSQL("INSERT INTO sessions(id,start,end,source,zone) VALUES(?,?,?,'tracked','UTC')",new Object[]{id,start,start+duration});return start;}
    File wav()throws Exception{File dir=new File(context.getFilesDir(),"clips");assertTrue(dir.isDirectory()||dir.mkdirs());File file=new File(dir,"verification-album-"+UUID.randomUUID()+".wav");byte[] pcm=new byte[320000];for(int n=0;n<pcm.length/2;n++){short sample=(short)(Math.sin(n*.12)*(4000+(n/7000%5)*4000));pcm[n*2]=(byte)sample;pcm[n*2+1]=(byte)(sample>>8);}NoiseRecorder.writeWave(file,pcm);files.add(file);return file;}
    JSONObject event(long session,long at,String category,boolean favorite){store.soundEvent(session,at,1950,category,.9,null);JSONObject row=store.rows("SELECT * FROM sound_events WHERE session=? AND time=?",""+session,""+at).get(0);if(favorite)SoundClips.favoriteEvent(store,row,true);return store.rows("SELECT * FROM sound_events WHERE id=?",""+row.optLong("id")).get(0);}
    void standard()throws Exception{recent=night(1,1,30000);old=night(2,20,30000);analysis=night(3,2,30000);File first=wav(),second=wav();store.clip(1,recent+10000,first.getName(),.2,10000);store.clip(2,old+10000,second.getName(),.2,10000);recentEvent=event(1,recent+1000,"snoring",true);oldEvent=event(2,old+2000,"speech",true);analysisEvent=event(3,analysis+1000,"snoring",true);recentClip=store.rows("SELECT * FROM clips WHERE session=1").get(0);SoundClips.favorite(store,recentClip,true);recentClip=store.rows("SELECT * FROM clips WHERE session=1").get(0);}
    @Override public void close(){store.close();context.deleteDatabase(database);for(File file:files)if(file.exists())assertTrue(file.delete());}
}
