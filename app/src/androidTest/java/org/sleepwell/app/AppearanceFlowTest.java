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
    private void capture(String name)throws Exception{device.waitForIdle();Activity owner=resumed();java.util.concurrent.CountDownLatch frame=new java.util.concurrent.CountDownLatch(1);InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{View decor=owner.getWindow().getDecorView();assertTrue("Actual focused native Appearance/Tonight frame",owner.hasWindowFocus()&&decor.isAttachedToWindow()&&decor.isLaidOut()&&decor.isHardwareAccelerated());decor.getViewTreeObserver().registerFrameCommitCallback(frame::countDown);decor.invalidate();});assertTrue(frame.await(5,java.util.concurrent.TimeUnit.SECONDS));device.waitForIdle();String[] layout={null};InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{try{layout[0]=layout(owner.getWindow().getDecorView()).toString(2);}catch(Exception error){throw new AssertionError("Actual layout diagnostics",error);}});try(java.io.PrintWriter out=new java.io.PrintWriter(new File(folder,"appearance-"+name+"-layout.json"))){out.print(layout[0]);}assertTrue(device.takeScreenshot(new File(folder,"appearance-"+name+".png")));}
    private org.json.JSONObject layout(View view)throws Exception{int[] location=new int[2];view.getLocationOnScreen(location);android.graphics.Rect visible=new android.graphics.Rect();boolean shown=view.getGlobalVisibleRect(visible);org.json.JSONObject result=new org.json.JSONObject().put("class",view.getClass().getSimpleName()).put("x",location[0]).put("y",location[1]).put("width",view.getWidth()).put("height",view.getHeight()).put("measuredHeight",view.getMeasuredHeight()).put("minimumHeight",view.getMinimumHeight()).put("shown",shown).put("visible",visible.flattenToString());if(view.getContentDescription()!=null)result.put("description",view.getContentDescription());if(view instanceof TextView){TextView text=(TextView)view;result.put("text",text.getText()).put("textSizePx",text.getTextSize());}if(view instanceof android.view.ViewGroup){org.json.JSONArray children=new org.json.JSONArray();android.view.ViewGroup group=(android.view.ViewGroup)view;for(int n=0;n<group.getChildCount();n++)children.put(layout(group.getChildAt(n)));result.put("children",children);}return result;}
    private void clickText(String text){UiObject2 v=device.wait(Until.findObject(By.text(text)),5000);assertNotNull(text,v);v.click();device.waitForIdle();}
    private void enableToggle(String description){Activity owner=resumed();long until=android.os.SystemClock.elapsedRealtime()+5000;boolean[] focused={false};while(!focused[0]&&android.os.SystemClock.elapsedRealtime()<until){InstrumentationRegistry.getInstrumentation().runOnMainSync(()->focused[0]=owner instanceof AppearanceActivity&&owner.hasWindowFocus()&&owner.getWindow().getDecorView().isAttachedToWindow());if(!focused[0])android.os.SystemClock.sleep(25);}assertTrue("Appearance must regain focus after its picker",focused[0]);UiObject2 control=device.wait(Until.findObject(By.desc(description)),Math.max(1,until-android.os.SystemClock.elapsedRealtime()));assertNotNull("Current Appearance toggle: "+description,control);control.click();device.waitForIdle();assertTrue("Appearance toggle changed: "+description,device.wait(Until.hasObject(By.desc(description).checked(true)),5000));}
    private View described(View root,String description){if(root.getContentDescription()!=null&&description.contentEquals(root.getContentDescription()))return root;if(root instanceof android.view.ViewGroup){android.view.ViewGroup group=(android.view.ViewGroup)root;for(int n=0;n<group.getChildCount();n++){View found=described(group.getChildAt(n),description);if(found!=null)return found;}}return null;}
    private boolean completeControl(String description){Activity owner=resumed();boolean[] complete={false};InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{View target=described(owner.getWindow().getDecorView(),description);assertNotNull("Owned native action: "+description,target);android.graphics.Rect visible=new android.graphics.Rect();complete[0]=owner.hasWindowFocus()&&target.isAttachedToWindow()&&target.isLaidOut()&&target.getGlobalVisibleRect(visible)&&visible.width()==target.getWidth()&&visible.height()==target.getHeight()&&visible.height()>=Design.dp(context,48);});return complete[0];}
    private void exposeControl(String description){for(int attempt=0;attempt<4;attempt++){if(completeControl(description))return;UiObject2 scroll=device.findObject(By.scrollable(true));assertNotNull("Actual scroll area for "+description,scroll);scroll.scroll(Direction.DOWN,.6f);device.waitForIdle();}assertTrue("Complete native action must clear the dock/footer: "+description,completeControl(description));}
    private void settings(){for(UiObject2 v:device.findObjects(By.desc("Settings")))if(v.getVisibleBounds().centerY()>device.getDisplayHeight()*.7f){v.click();break;}assertTrue(device.wait(Until.hasObject(By.desc("Settings").selected(true)),5000));}
    private void appearance()throws Exception{settings();clickText("Appearance");clickText("Theme & text");assertTrue("Appearance controls must render",device.wait(Until.hasObject(By.desc("Time of day")),5000));boolean ready=device.wait(Until.hasObject(By.desc("Bedroom lamp summary ready")),5000);if(!ready)capture("pending-lamp");assertTrue("Attached lamp summary must finish its actual saved-settings read",ready);}
    private void tonight(){if(device.wait(Until.hasObject(By.desc("Tonight").selected(true)),1000))return;UiObject2 target=device.wait(Until.findObject(By.desc("Tonight")),5000);assertNotNull(target);target.click();assertTrue(device.wait(Until.hasObject(By.desc("Tonight").selected(true)),5000));}
    private void save(){UiObject2 save=device.findObject(By.textStartsWith("Save appearance"));assertNotNull(save);save.click();device.waitForIdle();assertTrue(device.wait(Until.hasObject(By.desc("Settings").selected(true)),5000));}
    private Activity resumed(){final Activity[] result={null};InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))result[0]=a;});assertNotNull(result[0]);return result[0];}
    private void relaunch(){android.app.Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();android.app.Instrumentation.ActivityMonitor monitor=instrumentation.addMonitor(MainActivity.class.getName(),null,false);try{context.startActivity(new Intent(context,MainActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));assertNotNull("Main activity must be recreated",monitor.waitForActivityWithTimeout(5000));instrumentation.waitForIdleSync();device.waitForIdle();}finally{instrumentation.removeMonitor(monitor);}}
    private TextView findText(View v,String text){if(v instanceof TextView&&((TextView)v).getText().toString().equals(text))return (TextView)v;if(v instanceof android.view.ViewGroup){android.view.ViewGroup group=(android.view.ViewGroup)v;for(int n=0;n<group.getChildCount();n++){TextView result=findText(group.getChildAt(n),text);if(result!=null)return result;}}return null;}
    private float actualTitleSize(){Activity a=resumed();final float[] size={0};InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{TextView title=findText(a.getWindow().getDecorView(),"Tonight");assertNotNull(title);size[0]=title.getTextSize();});return size[0];}
    private void assertActualRootColor(int expected){Activity a=resumed();InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{android.view.ViewGroup content=a.findViewById(android.R.id.content);assertEquals(expected,((ColorDrawable)content.getChildAt(0).getBackground()).getColor());});}
    @Test public void saveReopenRelaunchCancelAndRenderAllThemes() throws Exception {
        context=InstrumentationRegistry.getInstrumentation().getTargetContext();device=UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());folder=new File(context.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());
        String[] keys={Appearance.AUTO,Appearance.THEME,Appearance.SIZE,Appearance.MOTION,Appearance.CONTRAST};Map<String,?> original=Prefs.of(context).getAll();
        assertFalse("No active alarm may be disturbed",Prefs.flag(context,"ringing"));assertFalse("No recording may be disturbed",TrackingService.running);
        try{
            Appearance.save(context,true,0,0,false,false);
            relaunch();
            appearance();capture("automatic");UiObject2 save=device.findObject(By.textStartsWith("Save appearance"));assertNotNull(save);for(String action:new String[]{"Configure lamp sunrise duration","Configure lamp maximum intensity","Configure smart bedroom light"}){UiObject2 control=device.findObject(By.desc(action));assertNotNull(action,control);assertTrue("Complete lamp control on the first default frame: "+action,control.getVisibleBounds().height()>=Design.dp(context,48)&&control.getVisibleBounds().bottom<=save.getVisibleBounds().top);}
            // Back cancels a draft, including a tile's explicit switch to manual mode.
            device.findObject(By.desc("Dawn theme")).click();assertFalse(device.findObject(By.desc("Time of day")).isChecked());device.pressBack();device.waitForIdle();assertTrue(Appearance.automatic(context));
            int[] expected={0xff241e30,0xff483768,0xffffd291};float normalSize=0;
            for(int phase=0;phase<3;phase++){
                appearance();device.findObject(By.desc(AppearancePolicy.THEMES[phase]+" theme")).click();assertTrue(device.findObject(By.desc(AppearancePolicy.THEMES[phase]+" theme")).isSelected());capture("selected-"+phase);save();
                assertFalse(Appearance.automatic(context));assertEquals(phase,Prefs.number(context,Appearance.THEME,-1));tonight();assertActualRootColor(expected[phase]);capture("tonight-"+phase);if(phase==0)normalSize=actualTitleSize();
                appearance();assertTrue(device.findObject(By.desc(AppearancePolicy.THEMES[phase]+" theme")).isSelected());device.pressBack();device.waitForIdle();
            }
            appearance();clickText("Text size");clickText("Extra large");enableToggle("Reduce motion");enableToggle("High-contrast mode");save();
            assertTrue(Prefs.flag(context,Appearance.MOTION));assertTrue(Prefs.flag(context,Appearance.CONTRAST));assertEquals(2,Prefs.number(context,Appearance.SIZE,-1));
            tonight();assertEquals(normalSize*1.3f,actualTitleSize(),.5f);assertTrue(Appearance.reduced(context));assertTrue(Design.touch(context,Color.WHITE,8) instanceof StateListDrawable);capture("large-contrast-tonight");
            exposeControl("Start sleep tracking");capture("large-contrast-tonight-action");assertTrue(device.hasObject(By.desc("Tonight").selected(true)));
            relaunch();assertTrue(device.wait(Until.hasObject(By.desc("Tonight").selected(true)),5000));assertActualRootColor(expected[2]);appearance();assertTrue(device.findObject(By.desc("Reduce motion")).isChecked());assertTrue(device.findObject(By.desc("High-contrast mode")).isChecked());assertTrue(device.hasObject(By.text("Extra large")));exposeControl("Configure smart bedroom light");capture("large-contrast");
            for(int phase=0;phase<3;phase++){Appearance.Palette p=new Appearance.Palette(phase,true);assertTrue(androidx.core.graphics.ColorUtils.calculateContrast(p.softInk,p.panel)>=4.5);assertTrue(androidx.core.graphics.ColorUtils.calculateContrast(p.buttonInk,p.accent)>=4.5);}
        }finally{
            SharedPreferences.Editor edit=Prefs.of(context).edit();for(String key:keys){Object value=original.get(key);if(value instanceof Boolean)edit.putBoolean(key,(Boolean)value);else if(value instanceof Integer)edit.putInt(key,(Integer)value);else edit.remove(key);}assertTrue(edit.commit());
            context.startActivity(new Intent(context,MainActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));device.waitForIdle();
        }
    }
}
