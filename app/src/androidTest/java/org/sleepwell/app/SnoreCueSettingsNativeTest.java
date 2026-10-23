package org.sleepwell.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import androidx.test.uiautomator.*;
import org.junit.Test;
import java.io.File;
import java.util.*;
import static org.junit.Assert.*;

/** CI configuration only. Settings feed a fake output; no microphone, tone or vibration is started. */
public class SnoreCueSettingsNativeTest {
    private final Instrumentation i=InstrumentationRegistry.getInstrumentation();private final Context c=i.getTargetContext();private final UiDevice device=UiDevice.getInstance(i);
    private void choose(String section,String value){UiObject2 group=device.wait(Until.findObject(By.desc(section+" options")),3000);assertNotNull(group);group.findObject(By.desc(value)).click();device.waitForIdle();}
    private void open(){c.startActivity(new Intent(c,SnoringCuesActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));assertTrue(device.wait(Until.hasObject(By.text("Snoring cues")),5000));device.waitForIdle();}
    private void capture(String name){File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());device.waitForIdle();assertTrue(device.takeScreenshot(new File(folder,name+".png")));}
    @SuppressWarnings("unchecked") private void restore(Map<String,?> values){SharedPreferences.Editor e=Prefs.of(c).edit().clear();for(Map.Entry<String,?> item:values.entrySet()){String k=item.getKey();Object v=item.getValue();if(v instanceof Boolean)e.putBoolean(k,(Boolean)v);else if(v instanceof Integer)e.putInt(k,(Integer)v);else if(v instanceof Long)e.putLong(k,(Long)v);else if(v instanceof Float)e.putFloat(k,(Float)v);else if(v instanceof Set)e.putStringSet(k,(Set<String>)v);else e.putString(k,(String)v);}assertTrue(e.commit());}
    @Test public void savedSettingsSurviveRecreationAndControlFakeCueBehavior() throws Exception {
        org.junit.Assume.assumeTrue(android.os.Build.HARDWARE.contains("ranchu")||android.os.Build.HARDWARE.contains("goldfish"));org.junit.Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("ciReview","").equals("true"));
        assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"ringing"));Map<String,?> before=new HashMap<>(Prefs.of(c).getAll());boolean granted=c.checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED;
        try{
            if(!granted)i.getUiAutomation().grantRuntimePermission(c.getPackageName(),Manifest.permission.RECORD_AUDIO);
            Prefs.of(c).edit().putBoolean("snoreCues",false).putBoolean("noiseEnabled",true).putInt("snoreCueMode",1).putInt("snoreCueIntensity",1).putInt("snoreCueRepeats",1).putInt("snoreCueSensitivity",1).commit();open();capture("snore-settings-default");
            device.findObject(By.desc("Respond to snoring estimates")).click();choose("Alert type","Both");choose("Intensity","Medium");choose("Repeats","3");choose("Detection sensitivity","High");capture("snore-settings-selected");
            i.runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(a instanceof SnoringCuesActivity)a.recreate();});assertTrue(device.wait(Until.hasObject(By.desc("Respond to snoring estimates").checked(true)),5000));assertTrue(device.findObject(By.desc("Alert type options")).findObject(By.desc("Both")).isSelected());capture("snore-settings-recreated");
            device.findObject(By.text("Save settings")).click();assertTrue(device.wait(Until.gone(By.text("Snoring cues")),5000));SnoreCuePolicy.Settings settings=SnoreCueOutput.settings(c);assertTrue(settings.enabled);assertEquals(SnoreCuePolicy.Mode.BOTH,settings.mode);assertEquals(2,settings.intensity);assertEquals(3,settings.repeats);assertEquals(2,settings.sensitivity);
            int[] plays={0};SnoreCuePolicy policy=new SnoreCuePolicy(new SnoreCuePolicy.Budget(),()->new SnoreCuePolicy.Device(){public void play(SnoreCuePolicy.Settings value){assertEquals(3,value.repeats);plays[0]++;}public void close(){}},()->fail());assertTrue(policy.event("snoring",.7,0,settings));assertFalse(policy.event("snoring",.9,1000,settings));assertFalse(policy.event("speech",.9,120000,settings));policy.close();assertEquals(1,plays[0]);open();assertTrue(device.hasObject(By.desc("Respond to snoring estimates").checked(true)));capture("snore-settings-reopened");choose("Repeats","1");device.pressBack();assertTrue(device.wait(Until.gone(By.text("Snoring cues")),5000));assertEquals(3,SnoreCueOutput.settings(c).repeats);
        }finally{i.runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(a instanceof SnoringCuesActivity)a.finish();});restore(before);/* CI emulator is disposable. Revoking here kills the instrumentation process; the grant is discarded with the emulator. */assertEquals(before,Prefs.of(c).getAll());}
    }
}
