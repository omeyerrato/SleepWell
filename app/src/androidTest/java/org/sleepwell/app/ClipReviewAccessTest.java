package org.sleepwell.app;

import android.content.Context;
import android.os.Build;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Real private SQLite and WAV bytes; invokes the shared playback guard without allocating a player. */
public class ClipReviewAccessTest {
    private ClipTarget target(FavoriteSoundsFixture f,String key)throws Exception {
        for(FavoriteSoundRows.Item item:FavoriteSoundRows.read(f.context,f.store,new FavoriteSoundRows.Filter(0,"all"),0,f.now).items)
            if(item.row.key.equals(key)){assertNotNull(item.row.target);return item.row.target;}
        throw new AssertionError(key);
    }
    private void rejected(FavoriteSoundsFixture f,ClipTarget target)throws Exception {
        try{ClipReviewAccess.check(f.store,target);fail("A stale interval outside the saved trim must not open or resume.");}
        catch(IOException expected){assertTrue(expected.getMessage().contains("sleep period changed"));}
    }
    @Test public void anUnchangedClipAndEventCannotBypassNewStartOrEndTrims()throws Exception {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));
        assertTrue(Build.HARDWARE.contains("ranchu")||Build.HARDWARE.contains("goldfish"));
        assertEquals("1",UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).executeShellCommand("getprop ro.kernel.qemu").trim());
        assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(Prefs.flag(context,"ringing"));assertFalse(Prefs.flag(context,"tracking"));
        Map<String,?> preferences=new HashMap<>(Prefs.of(context).getAll());
        try(FavoriteSoundsFixture f=new FavoriteSoundsFixture()){
            f.standard();String eventKey="event:"+f.recentEvent.getLong("id"),clipKey="clip:"+f.recentClip.getLong("id");
            ClipTarget event=target(f,eventKey),clip=target(f,clipKey);ClipReviewAccess.check(f.store,event);ClipReviewAccess.check(f.store,clip);
            byte[] bytes=MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(f.files.get(0).toPath()));
            RecordEntry record=Records.get(f.store,1);RecordDraft draft=record.edit.copy();draft.start=f.recent+1500;draft.end=f.recent+9000;
            Records.save(f.store,1,draft,record.updated,f.now);SoundClips.identity(SoundClips.get(f.store,1,f.recentClip.getLong("id")),clip.clip);SoundClips.eventIdentity(SoundClips.event(f.store,1,f.recentEvent.getLong("id")),event.event);
            rejected(f,event);rejected(f,clip);
            ClipTarget refreshed=target(f,eventKey);assertEquals(1500,refreshed.fromMs);assertEquals(1450,refreshed.durationMs);ClipReviewAccess.check(f.store,refreshed);ClipReviewAccess.check(f.store,target(f,clipKey));
            // This already-prepared selection is rechecked by begin() on Play/Resume.
            record=Records.get(f.store,1);draft=record.edit.copy();draft.end=f.recent+2000;Records.save(f.store,1,draft,record.updated,f.now+1);
            rejected(f,refreshed);ClipTarget shortened=target(f,eventKey);assertEquals(500,shortened.durationMs);ClipReviewAccess.check(f.store,shortened);
            record=Records.get(f.store,1);Records.reset(f.store,1,record.updated);ClipReviewAccess.check(f.store,event);ClipReviewAccess.check(f.store,clip);
            assertArrayEquals(bytes,MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(f.files.get(0).toPath())));
            assertEquals(3,f.store.rows("SELECT id FROM sessions").size());assertEquals(2,f.store.rows("SELECT id FROM clips").size());assertEquals(3,f.store.rows("SELECT id FROM sound_events").size());
        }
        assertEquals(preferences,Prefs.of(context).getAll());
    }
}
