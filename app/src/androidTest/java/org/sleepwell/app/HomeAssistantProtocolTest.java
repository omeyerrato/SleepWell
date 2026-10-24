package org.sleepwell.app;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.net.*;
import java.io.*;
import java.security.cert.Certificate;
import javax.net.ssl.*;
import org.json.*;
import static org.junit.Assert.*;

/** Real Android JSON over fake HTTPS connections; never connects to an external server. */
@RunWith(AndroidJUnit4.class)
public class HomeAssistantProtocolTest {
    @Test public void parsesDimmableOnAndOffWithExactEntity()throws Exception{SmartLightRun.State on=HomeAssistantLight.parse("light.bedroom","{\"entity_id\":\"light.bedroom\",\"state\":\"on\",\"attributes\":{\"brightness\":123,\"supported_color_modes\":[\"color_temp\"]}}");assertTrue(on.on);assertTrue(on.dimmable);assertEquals(123,on.brightness);SmartLightRun.State off=HomeAssistantLight.parse("light.bedroom","{\"entity_id\":\"light.bedroom\",\"state\":\"off\",\"attributes\":{\"supported_color_modes\":[\"brightness\"]}}");assertFalse(off.on);assertTrue(off.dimmable);}
    @Test public void onOffOnlyLampIsNotMisreportedAsDimmable()throws Exception{SmartLightRun.State state=HomeAssistantLight.parse("light.bedroom","{\"entity_id\":\"light.bedroom\",\"state\":\"off\",\"attributes\":{\"supported_color_modes\":[\"onoff\"]}}");assertFalse(state.dimmable);}
    @Test public void rejectsWrongEntityUnavailableAndMissingOnBrightness()throws Exception{for(String value:new String[]{"{\"entity_id\":\"light.other\",\"state\":\"off\",\"attributes\":{}}","{\"entity_id\":\"light.bedroom\",\"state\":\"unavailable\",\"attributes\":{}}","{\"entity_id\":\"light.bedroom\",\"state\":\"on\",\"attributes\":{}}"})try{HomeAssistantLight.parse("light.bedroom",value);fail();}catch(Exception expected){}}
    private static final class Fake extends HttpsURLConnection {final ByteArrayOutputStream body=new ByteArrayOutputStream();Fake()throws Exception{super(new URL("https://example.org"));}public void connect(){}public void disconnect(){}public boolean usingProxy(){return false;}public String getCipherSuite(){return "fake";}public Certificate[] getLocalCertificates(){return null;}public Certificate[] getServerCertificates(){return new Certificate[0];}public int getResponseCode(){return 200;}public OutputStream getOutputStream(){return body;}public InputStream getInputStream(){return new ByteArrayInputStream("[]".getBytes());}}
    @Test public void turnOnAndOffUseRealServiceSchemaWithoutTokenInPayload()throws Exception{SmartLightConfig config=new SmartLightConfig("https://example.org","secret","light.bedroom",true,10,80,true,1);Fake on=new Fake();new HomeAssistantLight(config,url->{assertEquals("/api/services/light/turn_on",url.getPath());return on;}).set(true,42);JSONObject json=new JSONObject(on.body.toString("UTF-8"));assertEquals(3,json.length());assertEquals("light.bedroom",json.getString("entity_id"));assertEquals(42,json.getInt("brightness"));assertEquals(0,json.getInt("transition"));assertFalse(json.toString().contains("secret"));Fake off=new Fake();new HomeAssistantLight(config,url->{assertEquals("/api/services/light/turn_off",url.getPath());return off;}).set(false,0);assertFalse(new JSONObject(off.body.toString("UTF-8")).has("brightness"));}
}
