package org.sleepwell.app;

import java.net.URI;

final class SmartLightConfig {
    final String endpoint,token,entity;final boolean enabled,restore;final int minutes,maximum;final long revision;
    SmartLightConfig(String endpoint,String token,String entity,boolean enabled,int minutes,int maximum,boolean restore,long revision){this.endpoint=endpoint;this.token=token;this.entity=entity;this.enabled=enabled;this.minutes=minutes;this.maximum=maximum;this.restore=restore;this.revision=revision;}
    static SmartLightConfig empty(){return new SmartLightConfig("","","light.bedroom",false,10,80,true,0);}
    void validate(){URI uri;try{uri=URI.create(endpoint);}catch(Exception e){throw new IllegalArgumentException("Enter your Home Assistant HTTPS address.");}if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null||uri.getQuery()!=null||uri.getFragment()!=null||uri.getPort()==0||uri.getPort()>65535||endpoint.length()>2048)throw new IllegalArgumentException("Use an HTTPS server address without a query or embedded credentials.");if(token==null||token.isEmpty()||token.length()>2048||token.contains("\r")||token.contains("\n")||token.contains("\u0000"))throw new IllegalArgumentException("Enter a valid Home Assistant access token.");if(entity==null||!entity.matches("light\\.[a-z0-9_]{1,120}"))throw new IllegalArgumentException("Choose one light entity, such as light.bedroom.");if(minutes<1||minutes>30||maximum<1||maximum>100||revision<0)throw new IllegalArgumentException("Choose a 1–30 min sunrise and 1–100% maximum.");}
    String base(){return endpoint.replaceAll("/+$","");}
    int brightness(long elapsed){double fraction=Math.max(0,Math.min(1,elapsed/(minutes*60000d)));return Math.max(1,(int)Math.round(1+(maximum*255d/100-1)*fraction));}
}
