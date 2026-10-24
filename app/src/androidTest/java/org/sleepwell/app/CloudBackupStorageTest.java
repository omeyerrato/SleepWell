package org.sleepwell.app;

import android.content.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.*;
import org.junit.runner.RunWith;
import java.util.*;
import org.json.JSONObject;
import static org.junit.Assert.*;

/** UUID preferences and database only. No real account, chooser, provider or upload. */
@RunWith(AndroidJUnit4.class)
public class CloudBackupStorageTest {
    private Context context;private SharedPreferences prefs;private String name;
    @Before public void prepare(){context=InstrumentationRegistry.getInstrumentation().getTargetContext();name="cloud-backup-storage-"+UUID.randomUUID();prefs=context.getSharedPreferences(name,0);}
    @After public void cleanup(){context.deleteSharedPreferences(name);context.deleteDatabase(name+".db");}
    @Test public void rememberedDestinationReopensAndRevisionFencesChanges(){CloudBackupSettings.Destination empty=CloudBackupSettings.destination(prefs);assertTrue(empty.tree.isEmpty());CloudBackupSettings.remember(prefs,"content://fixture/tree/one","Cloud folder",3);CloudBackupSettings.Destination chosen=CloudBackupSettings.destination(context.getSharedPreferences(name,0));assertEquals("Cloud folder",chosen.label);assertEquals(3,chosen.owned);assertTrue(CloudBackupSettings.current(prefs,chosen));CloudBackupSettings.remember(prefs,"content://fixture/tree/two","Another folder",2);assertFalse(CloudBackupSettings.current(prefs,chosen));}
    @Test public void disconnectKeepsHistoryAndDoesNotDeleteDocuments()throws Exception{CloudBackupSettings.remember(prefs,"content://fixture/tree/one","Cloud folder",3);CloudBackupSettings.Entry entry=new CloudBackupSettings.Entry("content://fixture/tree/one","content://fixture/version","version.json",1000,15,"a".repeat(64),true);CloudBackupSettings.record(prefs,entry);CloudBackupSettings.Destination old=CloudBackupSettings.disconnect(prefs);assertEquals(3,old.owned);assertTrue(CloudBackupSettings.destination(prefs).tree.isEmpty());assertEquals(1,CloudBackupSettings.history(prefs).size());assertTrue(CloudBackupSettings.history(prefs).get(0).verified);}
    @Test public void historyIsBoundedAndUnverifiedReceiptUpgradesInPlace()throws Exception{String tree="content://fixture/tree/one";for(int n=0;n<25;n++)CloudBackupSettings.record(prefs,new CloudBackupSettings.Entry(tree,"content://fixture/"+n,"version"+n,1000+n,0,"",false));assertEquals(20,CloudBackupSettings.history(prefs).size());CloudBackupSettings.record(prefs,new CloudBackupSettings.Entry(tree,"content://fixture/24","version24",1024,8,"b".repeat(64),true));List<CloudBackupSettings.Entry> reopened=CloudBackupSettings.history(context.getSharedPreferences(name,0));assertEquals(20,reopened.size());assertTrue(reopened.get(0).verified);assertEquals(8,reopened.get(0).size);assertEquals(1,reopened.stream().filter(e->e.uri.equals("content://fixture/24")).count());}
    @Test public void corruptHistoryIsNotSilentlyDiscarded()throws Exception{assertTrue(prefs.edit().putString("history","broken").commit());try{CloudBackupSettings.record(prefs,new CloudBackupSettings.Entry("tree","uri","name",1,0,"",false));fail();}catch(Exception expected){}assertEquals("broken",prefs.getString("history",""));}
    @Test public void snapshotUsesActualPortableFormatWithoutChangingPrivateDatabase()throws Exception{Records.Factory isolated=c->new Store(c,name+".db");String original;try(Store store=isolated.open(context)){Alarm alarm=new Alarm(31);alarm.label="Cloud fixture alarm";store.save(alarm);original=store.backup().toString();}byte[] bytes=CloudBackupSnapshot.prepare(context,isolated,()->true);JSONObject copy=new JSONObject(new String(bytes,java.nio.charset.StandardCharsets.UTF_8));Backup.validate(copy);assertEquals(1,copy.getJSONArray("alarms").length());assertEquals("Cloud fixture alarm",copy.getJSONArray("alarms").getJSONObject(0).getString("label"));try(Store store=isolated.open(context)){assertEquals(original,store.backup().toString());}}
}
