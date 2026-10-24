package org.sleepwell.app;

import java.net.URL;
import java.io.*;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.*;

/** Injectable connection factory lets tests exercise the real HTTP policy without networking. */
final class WebhookPost {
    interface Connections {HttpsURLConnection open(URL url)throws IOException;}
    static void send(AutomationRule rule,AutomationEvent event,Connections connections)throws AutomationDelivery.Failure{
        HttpsURLConnection connection=null;try{connection=connections.open(new URL(rule.destination));connection.setConnectTimeout(8000);connection.setReadTimeout(8000);connection.setInstanceFollowRedirects(false);connection.setRequestMethod("POST");connection.setDoOutput(true);connection.setRequestProperty("Content-Type","application/json; charset=utf-8");connection.setRequestProperty("Idempotency-Key",event.id);if(!rule.secret.isEmpty())connection.setRequestProperty("Authorization","Bearer "+rule.secret);byte[] payload=event.json().getBytes(StandardCharsets.UTF_8);connection.setFixedLengthStreamingMode(payload.length);try(OutputStream out=connection.getOutputStream()){out.write(payload);}int status=connection.getResponseCode();if(status<200||status>=300)throw new AutomationDelivery.Failure("Webhook HTTP "+status,status==408||status==429||status>=500);}catch(AutomationDelivery.Failure failure){throw failure;}catch(SSLException failure){throw new AutomationDelivery.Failure("Webhook TLS failed; check destination",false);}catch(IOException failure){throw new AutomationDelivery.Failure("Webhook could not connect",true);}finally{if(connection!=null)connection.disconnect();}
    }
}
