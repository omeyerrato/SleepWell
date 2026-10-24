package org.sleepwell.app;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import org.junit.Test;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import static org.junit.Assert.*;

/** Actual bundled PCM/hash checks only; never constructs any output adapter/player. */
public class AlarmToneAssetsTest {
    @Test public void realAssetsDecodeAndExistingAlarmBytesArePreserved()throws Exception{assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).executeShellCommand("getprop ro.kernel.qemu").trim());Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"ringing"));Map<String,String> hashes=Map.of("daybreak","219d51b28313bbbc57d4df72db4a67bbdb97be0dd613f54d2fd18093e3285e8b","pulse","fe6d5fd277d36bb1e46628ae21dc572d21bd51551d0bafd480eea1d32fe441eb","chime","64c45df18fb96c4121fb1c9df28fabdcb6a0408423d2a4e94c5cd57a762cdd3d");for(String tone:hashes.keySet()){byte[] bytes;try(InputStream in=c.getAssets().open(AlarmTones.asset(tone));ByteArrayOutputStream data=new ByteArrayOutputStream()){byte[] chunk=new byte[8192];for(int count;(count=in.read(chunk))!=-1;)data.write(chunk,0,count);bytes=data.toByteArray();}StringBuilder hash=new StringBuilder();for(byte value:MessageDigest.getInstance("SHA-256").digest(bytes))hash.append(String.format(Locale.ROOT,"%02x",value&255));assertEquals(hashes.get(tone),hash.toString());ToneWaveform wave=ToneWaveform.read(new ByteArrayInputStream(bytes));assertEquals(tone, tone.equals("chime")?4000:8000,wave.durationMs);assertEquals(32,wave.peaks.length);float maximum=0;for(float value:wave.peaks){assertTrue(Float.isFinite(value)&&value>=0&&value<=1);maximum=Math.max(maximum,value);}assertTrue("Actual non-silent PCM",maximum>0);}String database="sleepwell-verification-"+UUID.randomUUID()+".db";try{try(Store store=new Store(c,database)){Alarm alarm=new Alarm(1);alarm.tone="chime";store.save(alarm);}try(Store store=new Store(c,database)){assertEquals("chime",store.alarm(1).tone);assertEquals("chime.wav",AlarmTones.asset(store.alarm(1).tone));}}finally{c.deleteDatabase(database);}}
}
