package org.sleepwell.app;

import android.content.Context;
import android.provider.Settings;
import android.view.accessibility.AccessibilityNodeInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import static org.junit.Assert.*;

/** In-memory selection only. Reads the existing clock preference; never changes system settings. */
@RunWith(AndroidJUnit4.class)
public class RecordClockTest {
    @Test public void selectedEpochUsesRecordZoneAndExistingPhoneClockFormat() throws Exception {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        String clockBefore=Settings.System.getString(context.getContentResolver(),Settings.System.TIME_12_24);
        long sample=Instant.parse("2020-10-21T04:05:00Z").toEpochMilli();
        ZoneId deviceZone=ZoneId.systemDefault();
        ZoneId honolulu=ZoneId.of("Pacific/Honolulu");
        ZoneId recordZone=deviceZone.getRules().getOffset(Instant.ofEpochMilli(sample)).equals(honolulu.getRules().getOffset(Instant.ofEpochMilli(sample)))?ZoneId.of("Asia/Tokyo"):honolulu;
        java.text.DateFormat format=android.text.format.DateFormat.getTimeFormat(context);
        format.setTimeZone(TimeZone.getTimeZone(recordZone));String expected=normalize(format.format(new Date(sample)));
        format.setTimeZone(TimeZone.getTimeZone(deviceZone));String wrongZone=normalize(format.format(new Date(sample)));
        assertNotEquals("Choose a fixture that distinguishes the record and device clocks",expected,wrongZone);
        List<JSONObject> epochs=List.of(new JSONObject().put("start",sample-60000).put("time",sample).put("movement",.1));
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            RecordChart chart=new RecordChart(context,epochs,"movement",sample-5*60000,sample+5*60000,false,recordZone);
            chart.layout(0,0,800,400);
            assertTrue(chart.performAccessibilityAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD,null));
            String selected=normalize(chart.getContentDescription().toString());
            assertTrue("Selection must use the saved zone and the phone's current clock format: "+selected,selected.startsWith("movement at "+expected+": "));
            assertFalse("The device zone must not replace the saved recording zone",selected.startsWith("movement at "+wrongZone+": "));
        });
        assertEquals("The regression must never change the user's clock preference",clockBefore,Settings.System.getString(context.getContentResolver(),Settings.System.TIME_12_24));
    }
    private String normalize(String text){return text.replace('\u00a0',' ').replace('\u202f',' ').toLowerCase(Locale.ROOT);}
}
