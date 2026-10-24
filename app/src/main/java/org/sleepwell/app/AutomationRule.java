package org.sleepwell.app;

import java.net.URI;

final class AutomationRule {
    enum Transport { INTENT, WEBHOOK, MQTT }
    final String id,name,destination,topic,username,secret; final AutomationEvent.Kind event; final Transport transport; final boolean enabled; final long revision;
    AutomationRule(String id,String name,AutomationEvent.Kind event,Transport transport,boolean enabled,long revision,String destination,String topic,String username,String secret){
        this.id=id;this.name=name;this.event=event;this.transport=transport;this.enabled=enabled;this.revision=revision;this.destination=destination;this.topic=topic;this.username=username;this.secret=secret;
    }
    void validate(){
        if(id==null||!id.matches("[a-f0-9-]{36}")||name==null||name.trim().isEmpty()||name.length()>60||event==null||transport==null||revision<0)throw new IllegalArgumentException("Choose a rule name and event.");
        if(destination==null||destination.length()>2048||topic==null||username==null||secret==null||username.length()>256||secret.length()>2048)throw new IllegalArgumentException("Destination is too long.");
        if(username.contains("\u0000")||secret.contains("\r")||secret.contains("\n")||secret.contains("\u0000"))throw new IllegalArgumentException("Credentials contain unsupported characters.");
        if(transport==Transport.INTENT){if(!destination.matches("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+"))throw new IllegalArgumentException("Enter the receiving app package.");return;}
        URI uri;try{uri=URI.create(destination);}catch(Exception e){throw new IllegalArgumentException("Enter a valid destination.");}
        if(uri.getHost()==null||uri.getUserInfo()!=null||uri.getFragment()!=null||uri.getPort()==0||uri.getPort()>65535)throw new IllegalArgumentException("Use a host without embedded credentials or fragments.");
        if(transport==Transport.WEBHOOK){if(!"https".equalsIgnoreCase(uri.getScheme()))throw new IllegalArgumentException("Webhook requires HTTPS.");}
        else {if(!"ssl".equalsIgnoreCase(uri.getScheme())||uri.getQuery()!=null||uri.getPath()!=null&&!uri.getPath().isEmpty()&&!uri.getPath().equals("/"))throw new IllegalArgumentException("Use ssl://broker:8883 for MQTT.");if(topic.isEmpty()||topic.length()>256||topic.indexOf('+')>=0||topic.indexOf('#')>=0||topic.indexOf('\u0000')>=0)throw new IllegalArgumentException("Choose a publish topic without wildcards.");if(username.isEmpty()&&!secret.isEmpty())throw new IllegalArgumentException("MQTT password needs a username.");}
    }
    AutomationRule enabled(boolean value){return new AutomationRule(id,name,event,transport,value,revision,destination,topic,username,secret);}
    AutomationRule revision(long value){return new AutomationRule(id,name,event,transport,enabled,value,destination,topic,username,secret);}
}
