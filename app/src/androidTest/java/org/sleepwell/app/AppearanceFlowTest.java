package org.sleepwell.app;

import android.app.Activity;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.*;
import android.view.View;
import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import androidx.test.uiautomator.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.util.*;
import static org.junit.Assert.*;

/** Silent visual/settings flow. Original key presence and values are restored in finally. */
@RunWith(AndroidJUnit4.class)
public class AppearanceFlowTest {
    private Context context;private UiDevice device;private File folder;
    private void capture(String name){device.waitForIdle();assertTrue(device.takeScreenshot(new File(folder,"appearance-"+name+".png")));}
    private void clickText(String text){UiObject2 v=device.wait(Until.findObject(By.text(text)),5000);assertNotNull(text,v);v.click();device.waitForIdle();}
    private void settings(){for(UiObject2 v:device.findObjects(By.desc("Settings")))if(v.getVisibleBounds().centerY()>device.getDisplayHeight()*.7f){v.click();break;}assertTrue(device.wait(Until.hasObject(By.desc("Settings").selected(true)),5000));}
    private void appearance(){settings();clickText("Appearance");assertTrue(device.wait(Until.hasObject(By.desc("Time of day")),5000));}
    private void tonight(){if(device.wait(Until.hasObject(By.desc("Tonight").selected(true)),1000))return;UiObject2 target=device.wait(Until.findObject(By.desc("Tonight")),5000);assertNotNull(target);target.click();assertTrue(device.wait(Until.hasObject(By.desc("Tonight").selected(true)),5000));}
    private void save(){UiObject2 save=device.findObject(By.textStartsWith("Save appearance"));assertNotNull(save);save.click();device.waitForIdle();assertTrue(device.wait(Until.hasObject(By.desc("Settings").selected(true)),5000));}
    private Activity resumed(){final Activity[] result={null};InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))result[0]=a;});assertNotNull(result[0]);return result[0];}
    private void relaunch(){android.app.Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();android.app.Instrumentation.ActivityMonitor monitor=instrumentation.addMonitor(MainActivity.class.getName(),null,false);try{context.startActivity(new Intent(context,MainActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));assertNotNull("Main activity must be recreated",monitor.waitForActivityWithTimeout(5000));instrumentation.waitForIdleSync();device.waitForIdle();}finally{instrumentation.removeMonitor(monitor);}}
    private TextView findText(View v,String text){if(v instanceof TextView&&((TextView)v).getText().toString().equals(text))return (TextView)v;if(v instanceof android.view.ViewGroup){android.view.ViewGroup group=(android.view.ViewGroup)v;for(int n=0;n<group.getChildCount();n++){TextView result=findText(group.getChildAt(n),text);if(result!=null)return result;}}return null;}
    private float actualTitleSize(){Activity a=resumed();final float[] size={0};InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{TextView title=findText(a.getWindow().getDecorView(),"Tonight");assertNotNull(title);size[0]=title.getTextSize();});return size[0];}
    private void assertActualRootColor(int expected){Activity a=resumed();InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{android.view.ViewGroup content=a.findViewById(android.R.id.content);assertEquals(expected,((ColorDrawable)content.getChildAt(0).getBackground()).getColor());});}
    @Test public void saveReopenRelaunchCancelAndRenderAllThemes() throws Exception {
        context=InstrumentationRegistry.getInstrumentation().getTargetContext();device=UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());folder=new File(context.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());
        String[] keys={Appearance.AUTO,Appearance.THEME,Appearance.SIZE,Appearance.MOTION,Appearance.CONTRAST};Map<String,?> original=Prefs.of(context).getAll();
        assertFalse("No active alarm may be disturbed",Prefs.flag(context,"ringing"));assertFalse("No recording may be disturbed",TrackingService.running);
        try{
            Appearance.save(context,true,0,0,false,false);
            relaunch();
            appearance();capture("automatic");
            // Back cancels a draft, including a tile's explicit switch to manual mode.
            device.findObject(By.desc("Dawn theme")).click();assertFalse(device.findObject(By.desc("Time of day")).isChecked());device.pressBack();device.waitForIdle();assertTrue(Appearance.automatic(context));
            int[] expected={0xff241e30,0xff483768,0xffffd291};float normalSize=0;
            for(int phase=0;phase<3;phase++){
                appearance();device.findObject(By.desc(AppearancePolicy.THEMES[phase]+" theme")).click();assertTrue(device.findObject(By.desc(AppearancePolicy.THEMES[phase]+" theme")).isSelected());capture("selected-"+phase);save();
                assertFalse(Appearance.automatic(context));assertEquals(phase,Prefs.number(context,Appearance.THEME,-1));tonight();assertActualRootColor(expected[phase]);capture("tonight-"+phase);if(phase==0)normalSize=actualTitleSize();
                appearance();assertTrue(device.findObject(By.desc(AppearancePolicy.THEMES[phase]+" theme")).isSelected());device.pressBack();device.waitForIdle();
            }
            appearance();clickText("Text size");clickText("Extra large");device.findObject(By.desc("Reduce motion")).click();device.findObject(By.desc("High-contrast mode")).click();save();
            assertTrue(Prefs.flag(context,Appearance.MOTION));assertTrue(Prefs.flag(context,Appearance.CONTRAST));assertEquals(2,Prefs.number(context,Appearance.SIZE,-1));
            tonight();assertEquals(normalSize*1.3f,actualTitleSize(),.5f);assertTrue(Appearance.reduced(context));assertTrue(Design.touch(context,Color.WHITE,8) instanceof StateListDrawable);capture("large-contrast-tonight");
            assertTrue(new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().description("Start sleep tracking")));capture("large-contrast-tonight-action");assertTrue(device.hasObject(By.desc("Tonight").selected(true)));
            relaunch();assertTrue(device.wait(Until.hasObject(By.desc("Tonight").selected(true)),5000));assertActualRootColor(expected[2]);appearance();assertTrue(device.findObject(By.desc("Reduce motion")).isChecked());assertTrue(device.findObject(By.desc("High-contrast mode")).isChecked());assertTrue(device.hasObject(By.text("Extra large")));capture("large-contrast");
            for(int phase=0;phase<3;phase++){Appearance.Palette p=new Appearance.Palette(phase,true);assertTrue(androidx.core.graphics.ColorUtils.calculateContrast(p.softInk,p.panel)>=4.5);assertTrue(androidx.core.graphics.ColorUtils.calculateContrast(p.buttonInk,p.accent)>=4.5);}
        }finally{
            SharedPreferences.Editor edit=Prefs.of(context).edit();for(String key:keys){Object value=original.get(key);if(value instanceof Boolean)edit.putBoolean(key,(Boolean)value);else if(value instanceof Integer)edit.putInt(key,(Integer)value);else edit.remove(key);}assertTrue(edit.commit());
            context.startActivity(new Intent(context,MainActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));device.waitForIdle();
        }
    }
}
