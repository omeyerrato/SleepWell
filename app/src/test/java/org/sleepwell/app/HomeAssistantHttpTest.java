package org.sleepwell.app;

import org.junit.Test;
import java.net.*;
import java.io.*;
import java.security.cert.Certificate;
import javax.net.ssl.*;
import static org.junit.Assert.*;

public class HomeAssistantHttpTest {
    private final SmartLightConfig config=new SmartLightConfig("https://example.org/ha/","secret","light.bedroom",false,10,80,true,0);
    private static final class Fake extends HttpsURLConnection {final ByteArrayOutputStream body=new ByteArrayOutputStream();int status=200;String response="[]";boolean disconnected;Fake()throws Exception{super(new URL("https://example.org"));}public void connect(){}public void disconnect(){disconnected=true;}public boolean usingProxy(){return false;}public String getCipherSuite(){return "fake";}public Certificate[] getLocalCertificates(){return null;}public Certificate[] getServerCertificates(){return new Certificate[0];}public OutputStream getOutputStream(){return body;}public int getResponseCode(){return status;}public InputStream getInputStream(){return new ByteArrayInputStream(response.getBytes(java.nio.charset.StandardCharsets.UTF_8));}}
    @Test public void readOnlyCheckUsesGetNoBodyAndVerifiedHttpsPolicy()throws Exception{Fake f=new Fake();assertEquals("[]",HomeAssistantHttp.request(config,"/api/states/light.bedroom",null,url->{assertEquals("https://example.org/ha/api/states/light.bedroom",url.toString());return f;}));assertEquals("GET",f.getRequestMethod());assertEquals(0,f.body.size());assertFalse(f.getInstanceFollowRedirects());assertFalse(f.getUseCaches());assertEquals(5000,f.getReadTimeout());assertEquals(5000,f.getConnectTimeout());assertEquals("Bearer secret",f.getRequestProperty("Authorization"));assertTrue(f.disconnected);}
    @Test public void sendsOnlyExplicitServiceBodyAndClosesConnection()throws Exception{Fake f=new Fake();String body="{\"entity_id\":\"light.bedroom\",\"brightness\":12,\"transition\":0}";HomeAssistantHttp.request(config,"/api/services/light/turn_on",body,url->f);assertEquals("POST",f.getRequestMethod());assertEquals(body,f.body.toString(java.nio.charset.StandardCharsets.UTF_8));assertFalse(f.body.toString().contains("secret"));assertTrue(f.disconnected);}
    @Test public void rejectsRedirectErrorsAndOversizedResponse()throws Exception{for(int status:new int[]{301,302,401,403,404,500}){Fake f=new Fake();f.status=status;try{HomeAssistantHttp.request(config,"/api/",null,url->f);fail();}catch(IOException e){assertFalse(e.getMessage().contains("secret"));assertTrue(f.disconnected);}}Fake f=new Fake();f.response="x".repeat(65537);try{HomeAssistantHttp.request(config,"/api/",null,url->f);fail();}catch(IOException e){assertEquals("Home Assistant response was too large",e.getMessage());assertTrue(f.disconnected);}}
    @Test public void tlsAndTimeoutErrorsDoNotExposeCredentials()throws Exception{for(IOException cause:new IOException[]{new SSLException("private certificate"),new SocketTimeoutException("secret URL")})try{HomeAssistantHttp.request(config,"/api/",null,url->{throw cause;});fail();}catch(IOException e){assertFalse(e.getMessage().contains("private"));assertFalse(e.getMessage().contains("secret"));}}
}
