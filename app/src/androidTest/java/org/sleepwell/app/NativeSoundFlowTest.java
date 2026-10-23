package org.sleepwell.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.media.*;
import android.os.*;
import android.service.notification.StatusBarNotification;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.util.*;
import java.util.function.BooleanSupplier;
import static org.junit.Assert.*;

/** Real MediaPlayer/FGS integration, restricted to the disposable CI emulator with host audio disabled.
 * Never add this class to the physical-phone allowlist. No alarms, microphone, tracking or stream-volume changes.
 */
@RunWith(AndroidJUnit4.class)
public class NativeSoundFlowTest {
    private final Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
    private final Context c=instrumentation.getTargetContext();
    private final UiDevice device=UiDevice.getInstance(instrumentation);
    private final List<SleepSoundsActivity> owned=new ArrayList<>();
    private AudioManager audio;
    private void until(String message,BooleanSupplier condition){long deadline=SystemClock.elapsedRealtime()+12000;while(!condition.getAsBoolean()){if(SystemClock.elapsedRealtime()>deadline)fail(message);SystemClock.sleep(100);}}
    private SleepSoundService.Snapshot snapshot(){SleepSoundService.Snapshot[] value={null};instrumentation.runOnMainSync(()->value[0]=SleepSoundService.snapshot());return value[0];}
    private boolean state(SoundPlayback.State value){SleepSoundService.Snapshot s=snapshot();return s!=null&&s.state==value;}
    private void click(String label){UiObject2 control=device.wait(Until.findObject(By.pkg(c.getPackageName()).desc(label)),5000);assertNotNull(label,control);control.click();device.waitForIdle();}
    private SleepSoundsActivity launch(){Instrumentation.ActivityMonitor monitor=instrumentation.addMonitor(SleepSoundsActivity.class.getName(),null,false);try{c.startActivity(new Intent(c,SleepSoundsActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));Activity a=monitor.waitForActivityWithTimeout(5000);assertTrue(a instanceof SleepSoundsActivity);owned.add((SleepSoundsActivity)a);assertTrue(device.wait(Until.hasObject(By.desc("Ocean sound")),5000));return (SleepSoundsActivity)a;}finally{instrumentation.removeMonitor(monitor);}}
    private Notification notification(){for(StatusBarNotification posted:c.getSystemService(NotificationManager.class).getActiveNotifications())if(posted.getId()==104)return posted.getNotification();return null;}
    private void action(String title)throws Exception{Notification n=notification();assertNotNull("A playback notification must exist",n);for(Notification.Action a:n.actions)if(title.contentEquals(a.title)){a.actionIntent.send();return;}fail("Missing notification action "+title);}
    private boolean stopped(){boolean[] result={false};instrumentation.runOnMainSync(()->result[0]=c.getSystemService(ActivityManager.class).getRunningServices(20).stream().noneMatch(s->s.service.getClassName().equals(SleepSoundService.class.getName())));return result[0]&&!audio.isMusicActive()&&notification()==null;}
    private void capture(String name){until("Artwork must finish loading before capture",()->{boolean[] ready={false};instrumentation.runOnMainSync(()->ready[0]=owned.get(owned.size()-1).artworkReady());return ready[0];});device.waitForIdle();File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"sounds-native-"+name+".png")));}
    @Test public void realPlaybackBackgroundNotificationFocusTimerAndError()throws Exception{
        assertEquals("This class requires the explicit CI-only host-audio-disabled runner","true",InstrumentationRegistry.getArguments().getString("ciSilentPlayback"));
        assertEquals("Refusing physical hardware","1",device.executeShellCommand("getprop ro.kernel.qemu").trim());
        assertEquals(PackageManager.PERMISSION_GRANTED,c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS));
        assertFalse(Prefs.flag(c,"ringing"));assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"tracking"));assertNull(snapshot());
        try(Store store=new Store(c)){assertTrue(store.alarms().stream().noneMatch(a->a.enabled));assertTrue(store.rows("SELECT id FROM sessions WHERE end IS NULL").isEmpty());}
        audio=c.getSystemService(AudioManager.class);assertFalse("The fresh emulator must have no other music",audio.isMusicActive());
        int mediaVolume=audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        Map<String,?> personal=new HashMap<>(Prefs.of(c).getAll());String name="sleepwell-native-sound-"+UUID.randomUUID();SharedPreferences fixture=c.getSharedPreferences(name,Context.MODE_PRIVATE);SoundPreferences.Factory previous=SoundPreferences.factory;SoundPreferences.factory=context->fixture;
        AudioFocusRequest competitor=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build()).setOnAudioFocusChangeListener(change->{},new Handler(Looper.getMainLooper())).build();
        try{
            SoundMix mix=new SoundMix();mix.primaryGain=5;mix.brownGain=5;SoundPreferences.save(fixture,mix);launch();click("Play sleep sounds");
            until("Native media must start",()->state(SoundPlayback.State.PLAYING)&&audio.isMusicActive());
            long began=snapshot().elapsed;until("Playing time must advance",()->snapshot().elapsed>began+1200);capture("playing");
            device.pressHome();device.waitForIdle();long background=snapshot().elapsed;until("Foreground playback must continue outside the screen",()->snapshot().elapsed>background+1200&&audio.isMusicActive());
            action("Pause");until("Notification Pause must release media",()->state(SoundPlayback.State.PAUSED)&&!audio.isMusicActive());long paused=snapshot().elapsed;SystemClock.sleep(1200);assertEquals("Pause must freeze playing time",paused,snapshot().elapsed);
            action("Play");until("Notification Play must resume",()->state(SoundPlayback.State.PLAYING)&&audio.isMusicActive());launch();click("Ocean sound");until("A live source change must keep playing",()->state(SoundPlayback.State.PLAYING)&&audio.isMusicActive()&&notification()!=null&&"Ocean".contentEquals(notification().extras.getCharSequence(Notification.EXTRA_TITLE)));assertEquals("Ocean",SoundPreferences.load(fixture).primary);
            assertEquals(AudioManager.AUDIOFOCUS_REQUEST_GRANTED,audio.requestAudioFocus(competitor));until("Competing audio focus must pause playback",()->state(SoundPlayback.State.PAUSED)&&!audio.isMusicActive());audio.abandonAudioFocusRequest(competitor);SystemClock.sleep(1200);assertTrue("Regaining focus must not auto-play",state(SoundPlayback.State.PAUSED));capture("paused");
            action("Play");until("Explicit Play after focus loss must resume",()->state(SoundPlayback.State.PLAYING)&&audio.isMusicActive());action("Stop");until("Stop must remove media, foreground service and notification",this::stopped);assertNull(snapshot());
            // Real native clock/decoder completion near the end of a valid one-minute profile.
            mix.primary="Wind";mix.timerMinutes=1;mix.fadeMinutes=1;SoundPreferences.save(fixture,mix);instrumentation.runOnMainSync(()->c.startForegroundService(new Intent(c,SleepSoundService.class).setAction(SleepSoundService.PLAY).putExtra("offset",57000L)));
            until("Timed native playback must start",()->state(SoundPlayback.State.PLAYING)&&audio.isMusicActive());until("Native timer must finish and release resources",()->state(SoundPlayback.State.FINISHED)&&stopped());assertEquals(60000,snapshot().elapsed);
            mix.primary="My audio";mix.uri="content://org.sleepwell.missing-provider/missing.wav";SoundPreferences.save(fixture,mix);instrumentation.runOnMainSync(()->SleepSoundService.command(c,SleepSoundService.PLAY));until("Unreadable audio must fail and release resources",()->state(SoundPlayback.State.ERROR)&&stopped());assertFalse(SoundPreferences.error(c).isEmpty());launch();assertNotNull(device.wait(Until.findObject(By.text(SoundPreferences.error(c))),5000));capture("error");
            assertEquals("No system volume modification",mediaVolume,audio.getStreamVolume(AudioManager.STREAM_MUSIC));assertEquals("Personal preferences are unchanged",personal,Prefs.of(c).getAll());
        }finally{
            audio.abandonAudioFocusRequest(competitor);instrumentation.runOnMainSync(()->SleepSoundService.command(c,SleepSoundService.STOP));until("Cleanup must stop native playback",this::stopped);
            until("Sound Activities must close before deleting the fixture",()->{boolean[] closed={true};instrumentation.runOnMainSync(()->{for(SleepSoundsActivity a:owned)if(!a.isDestroyed()){closed[0]=false;a.finish();}});return closed[0];});instrumentation.waitForIdleSync();for(SleepSoundsActivity a:owned)a.awaitReaders(10000);
            SoundPreferences.factory=previous;c.deleteSharedPreferences(name);assertEquals("Personal preferences survive cleanup",personal,Prefs.of(c).getAll());assertEquals(mediaVolume,audio.getStreamVolume(AudioManager.STREAM_MUSIC));
            c.startActivity(new Intent(c,MainActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
}
