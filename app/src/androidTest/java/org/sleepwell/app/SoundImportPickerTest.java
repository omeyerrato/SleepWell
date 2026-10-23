package org.sleepwell.app;

import android.content.*;
import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.*;
import org.junit.*;
import org.junit.runner.RunWith;
import java.io.*;
import java.util.*;
import static org.junit.Assert.*;

/** Actual DocumentsUI interactions on a fresh, host-muted CI emulator. Never physical hardware. */
@RunWith(AndroidJUnit4.class)
public class SoundImportPickerTest extends ImportFlowSupport {
    @Test public void pickerValidationCancellationRotationAndSavedReadGrant()throws Exception{
        setup("sleepwell-audio-import-"+UUID.randomUUID());boolean success=false;
        try{
            assertTrue(c.getContentResolver().getPersistedUriPermissions().isEmpty());SoundMix mix=new SoundMix();mix.primary="Ocean";SoundPreferences.save(fixture,mix);launch();
            File abandoned=new File(c.getCacheDir(),"sleepwell-audio-abandoned-"+UUID.randomUUID()+".tmp");try(FileOutputStream out=new FileOutputStream(abandoned)){out.write(1);}
            Map<String,?> baseline=new HashMap<>(fixture.getAll());picker();device.pressBack();assertTrue(device.wait(Until.hasObject(By.desc("Ocean sound")),5000));assertEquals("Picker cancel keeps every mix key",baseline,fixture.getAll());
            for(String bad:new String[]{"Broken audio.wav","Too large.wav","Hidden large.wav","Denied.wav"}){pick(bad);settled();assertEquals("Rejected document preserves the entire mix",baseline,fixture.getAll());assertTrue(device.wait(Until.hasObject(By.textContains("could not be read as audio")),5000));assertTrue("Rejected document grants must remain transient",c.getContentResolver().getPersistedUriPermissions().isEmpty());}assertFalse("Next import removes only abandoned private snapshots",abandoned.exists());capture("rejected");
            pick("Delayed.wav");new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().description("Cancel audio import"));assertTrue(device.wait(Until.hasObject(By.desc("Cancel audio import")),2000));assertTrue(device.takeScreenshot(new File(folder(),"sounds-import-checking.png")));desc("Cancel audio import");settled();assertEquals(baseline,fixture.getAll());assertTrue(c.getContentResolver().getPersistedUriPermissions().isEmpty());
            pick("Delayed.wav");new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().description("Cancel audio import"));assertTrue(device.wait(Until.hasObject(By.desc("Cancel audio import")),2000));desc("Rain sound");settled();assertEquals("Late result cannot replace a newer source","Rain",SoundPreferences.load(fixture).primary);assertEquals("",SoundPreferences.load(fixture).uri);assertTrue(c.getContentResolver().getPersistedUriPermissions().isEmpty());
            pick("Delayed.wav");new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().description("Cancel audio import"));assertTrue(device.wait(Until.hasObject(By.desc("Cancel audio import")),2000));SleepSoundsActivity old=current();assertNotNull(old);recreate(old);settled();assertEquals("Delayed.wav",SoundPreferences.load(fixture).title);assertEquals("My audio",SoundPreferences.load(fixture).primary);assertNull("Import/recreation never starts playback",snapshot());capture("rotated");
            pick("Unknown size.wav");settled();assertEquals("Unknown size.wav",SoundPreferences.load(fixture).title);capture("unknown-size");
            pick("Quiet sea.wav");settled();assertEquals("Quiet sea.wav",SoundPreferences.load(fixture).title);assertEquals("My audio",SoundPreferences.load(fixture).primary);assertTrue(c.getContentResolver().getPersistedUriPermissions().stream().anyMatch(p->p.isReadPermission()&&!p.isWritePermission()&&p.getUri().toString().equals(SoundPreferences.load(fixture).uri)));capture("selected");
            closeScreens();launch();assertEquals("Quiet sea.wav",SoundPreferences.load(fixture).title);assertNotNull(device.wait(Until.findObject(By.text("Quiet sea.wav")),5000));assertNull(snapshot());
            assertTrue("Flush isolated fixture before process restart",fixture.edit().putBoolean("importVerificationReady",true).commit());try(FileWriter out=new FileWriter(handoff())){out.write(fixtureName);}preserved();success=true;
        }catch(AssertionError|Exception error){diagnostic("picker",error);throw error;}
        finally{cleanup(!success);}
    }
}
