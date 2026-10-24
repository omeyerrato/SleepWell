package org.sleepwell.app;

import org.junit.Test;
import java.net.*;
import java.io.*;
import java.security.cert.Certificate;
import javax.net.ssl.*;
import static org.junit.Assert.*;

public class WebhookPostTest {
    private final AutomationRule rule=new AutomationRule("12345678-1234-1234-1234-123456789012","Webhook",AutomationEvent.Kind.ALARM_DISMISSED,AutomationRule.Transport.WEBHOOK,true,1,"https://example.org/hook","","","top-secret");
    private final AutomationEvent event=AutomationEvent.occurrence(AutomationEvent.Kind.ALARM_DISMISSED,"occurrence",1000,false);
    private static final class Fake extends HttpsURLConnection {final ByteArrayOutputStream body=new ByteArrayOutputStream();int status;boolean disconnected;Fake(int status)throws Exception{super(new URL("https://example.org"));this.status=status;}public void connect(){}public void disconnect(){disconnected=true;}public boolean usingProxy(){return false;}public String getCipherSuite(){return "fake";}public Certificate[] getLocalCertificates(){return null;}public Certificate[] getServerCertificates(){return new Certificate[0];}public OutputStream getOutputStream(){return body;}public int getResponseCode(){return status;}}
    @Test public void sendsMinimalJsonIdempotencyAndTokenOverBoundedPost()throws Exception{Fake fake=new Fake(204);WebhookPost.send(rule,event,url->fake);assertEquals("POST",fake.getRequestMethod());assertFalse(fake.getInstanceFollowRedirects());assertEquals(8000,fake.getConnectTimeout());assertEquals(8000,fake.getReadTimeout());assertEquals("Bearer top-secret",fake.getRequestProperty("Authorization"));assertEquals(event.id,fake.getRequestProperty("Idempotency-Key"));assertEquals(event.json(),fake.body.toString(java.nio.charset.StandardCharsets.UTF_8));assertFalse(fake.body.toString().contains("top-secret"));assertTrue(fake.disconnected);}
    @Test public void neverFollowsRedirectAndDoesNotRetryPermanentErrors()throws Exception{for(int status:new int[]{301,302,307,400,401,403,404}){Fake fake=new Fake(status);try{WebhookPost.send(rule,event,url->fake);fail();}catch(AutomationDelivery.Failure e){assertFalse(e.retry);assertEquals("Webhook HTTP "+status,e.getMessage());assertTrue(fake.disconnected);}}}
    @Test public void retriesOnlyTransientHttpErrors()throws Exception{for(int status:new int[]{408,429,500,503}){Fake fake=new Fake(status);try{WebhookPost.send(rule,event,url->fake);fail();}catch(AutomationDelivery.Failure e){assertTrue(e.retry);}}}
    @Test public void networkAndTlsFailuresUseCredentialFreeMessages()throws Exception{for(IOException failure:new IOException[]{new IOException("secret endpoint"),new SSLException("secret certificate")})try{WebhookPost.send(rule,event,url->{throw failure;});fail();}catch(AutomationDelivery.Failure e){assertEquals(!(failure instanceof SSLException),e.retry);assertFalse(e.getMessage().contains("secret"));}}
}
