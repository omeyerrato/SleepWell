package org.sleepwell.app;

import android.content.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.*;
import org.junit.runner.RunWith;
import java.util.*;
import static org.junit.Assert.*;

/** Private UUID prefs and fake vault only: no lamp, alarm, server or output is used. */
@RunWith(AndroidJUnit4.class)
public class SmartLightStorageTest {
    private Context context;private SharedPreferences prefs;private String name;private SmartLightSettings.Factory previous;
    private final AutomationStore.Vault vault=new AutomationStore.Vault(){public String seal(String text){return android.util.Base64.encodeToString(text.getBytes(java.nio.charset.StandardCharsets.UTF_8),android.util.Base64.NO_WRAP);}public String open(String text){return new String(android.util.Base64.decode(text,android.util.Base64.NO_WRAP),java.nio.charset.StandardCharsets.UTF_8);}};
    @Before public void prepare(){context=InstrumentationRegistry.getInstrumentation().getTargetContext();name="lamp-storage-"+UUID.randomUUID();prefs=context.getSharedPreferences(name,0);previous=SmartLightSettings.factory;SmartLightSettings.factory=c->prefs;}
    @After public void cleanup(){SmartLightSettings.factory=previous;context.deleteSharedPreferences(name);}
    private SmartLightConfig config(long revision){return new SmartLightConfig("https://example.org/ha","private-token","light.bedroom",true,15,60,true,revision);}
    @Test public void freshSettingsAreOffWithoutConfiguration()throws Exception{SmartLightConfig empty=SmartLightSettings.load(prefs,vault);assertFalse(empty.enabled);assertEquals(10,empty.minutes);assertEquals(80,empty.maximum);assertEquals(0,empty.revision);assertTrue(prefs.getAll().isEmpty());}
    @Test public void settingsPersistWithNoPlaintextDestinationOrToken()throws Exception{SmartLightSettings.save(prefs,vault,config(0));assertFalse(prefs.getAll().toString().contains("private-token"));assertFalse(prefs.getAll().toString().contains("https://example"));SmartLightConfig reopened=SmartLightSettings.load(context.getSharedPreferences(name,0),vault);assertEquals(15,reopened.minutes);assertEquals(60,reopened.maximum);assertEquals("private-token",reopened.token);assertEquals("light.bedroom",reopened.entity);assertTrue(reopened.restore);assertTrue(SmartLightSettings.current(context,1));}
    @Test public void staleSaveIsRejectedWithoutLosingConnection()throws Exception{SmartLightSettings.save(prefs,vault,config(0));Map<String,?> original=new HashMap<>(prefs.getAll());try{SmartLightSettings.save(prefs,vault,config(0));fail();}catch(IllegalStateException expected){}assertEquals(original,prefs.getAll());}
    @Test public void vaultFailureLeavesAllPreviousPreferencesUntouched()throws Exception{SmartLightSettings.save(prefs,vault,config(0));Map<String,?> original=new HashMap<>(prefs.getAll());try{SmartLightSettings.save(prefs,new AutomationStore.Vault(){public String seal(String text)throws Exception{throw new Exception("key unavailable");}public String open(String text){return text;}},config(1));fail();}catch(Exception expected){}assertEquals(original,prefs.getAll());}
    @Test public void disconnectInvalidatesRunAndAllowsReconnection()throws Exception{SmartLightSettings.save(prefs,vault,config(0));SmartLightSettings.disconnect(context);assertFalse(SmartLightSettings.current(context,1));assertFalse(prefs.contains("config"));SmartLightConfig empty=SmartLightSettings.load(prefs,vault);assertEquals(2,empty.revision);assertFalse(empty.enabled);SmartLightSettings.save(prefs,vault,config(empty.revision));assertTrue(SmartLightSettings.current(context,3));}
    @Test public void corruptConfigurationDoesNotBecomeEnabledFallback()throws Exception{assertTrue(prefs.edit().putString("config","not-an-encrypted-config").putBoolean("enabled",true).commit());Map<String,?> original=new HashMap<>(prefs.getAll());try{SmartLightSettings.load(prefs,vault);fail();}catch(Exception expected){}assertEquals(original,prefs.getAll());}
}
