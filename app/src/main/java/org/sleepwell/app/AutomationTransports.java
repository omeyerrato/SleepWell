package org.sleepwell.app;

import android.content.Context;
import android.content.Intent;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import javax.net.ssl.*;

final class AutomationTransports implements AutomationDelivery.Transport {
    private final Context context;
    AutomationTransports(Context context){this.context=context.getApplicationContext();}
    public void send(AutomationRule rule,AutomationEvent event)throws AutomationDelivery.Failure{
        rule.validate();
        switch(rule.transport){case INTENT->intent(rule,event);case WEBHOOK->webhook(rule,event);case MQTT->mqtt(rule,event);}
    }
    private void intent(AutomationRule rule,AutomationEvent event)throws AutomationDelivery.Failure{
        try{Intent intent=new Intent("org.sleepwell.app.event."+event.kind.name()).setPackage(rule.destination).putExtra("event_id",event.id).putExtra("event",event.kind.name().toLowerCase(java.util.Locale.ROOT)).putExtra("occurred_at",event.time).putExtra("test",event.test).putExtra("schema",1);if(!event.soundClass.isEmpty())intent.putExtra("sound_class",event.soundClass);context.sendBroadcast(intent);}catch(RuntimeException e){throw new AutomationDelivery.Failure("Android rejected the broadcast",false);}
    }
    private void webhook(AutomationRule rule,AutomationEvent event)throws AutomationDelivery.Failure{
        WebhookPost.send(rule,event,url->(HttpsURLConnection)url.openConnection());
    }
    private void mqtt(AutomationRule rule,AutomationEvent event)throws AutomationDelivery.Failure{
        URI uri=URI.create(rule.destination);String host=uri.getHost();int port=uri.getPort()<0?8883:uri.getPort();
        try(java.net.Socket raw=new java.net.Socket()){raw.connect(new InetSocketAddress(host,port),8000);raw.setSoTimeout(8000);SSLSocketFactory factory=(SSLSocketFactory)SSLSocketFactory.getDefault();try(SSLSocket socket=(SSLSocket)factory.createSocket(raw,host,port,true)){socket.setSoTimeout(8000);SSLParameters params=socket.getSSLParameters();params.setEndpointIdentificationAlgorithm("HTTPS");socket.setSSLParameters(params);socket.startHandshake();MqttPublish.exchange(socket.getInputStream(),socket.getOutputStream(),"sw-"+event.id.substring(0,20),rule.username,rule.secret,rule.topic,event.json());}}catch(MqttPublish.Refused failure){throw new AutomationDelivery.Failure("MQTT connection rejected; check credentials",false);}catch(SSLException failure){throw new AutomationDelivery.Failure("MQTT TLS failed; check broker certificate",false);}catch(IOException failure){throw new AutomationDelivery.Failure("MQTT delivery not acknowledged",true);}
    }
}
