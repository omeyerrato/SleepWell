package org.sleepwell.app;
import java.util.*;
/** One manually identified HTTPS LAN bridge and one v1-compatible dimmable light. */
final class HueBridgeConfig {
 final String address,identity,pin,key,light,name;final boolean enabled,restore;final int minutes,maximum;final long revision;
 HueBridgeConfig(String address,String identity,String pin,String key,String light,String name,boolean enabled,int minutes,int maximum,boolean restore,long revision){this.address=address;this.identity=identity;this.pin=pin;this.key=key;this.light=light;this.name=name;this.enabled=enabled;this.minutes=minutes;this.maximum=maximum;this.restore=restore;this.revision=revision;}
 static HueBridgeConfig empty(){return new HueBridgeConfig("","","","","","",false,10,80,true,0);}
 byte[] addressBytes(){String[] parts=address.split("\\.",-1);if(parts.length!=4)throw new IllegalArgumentException("Enter a private LAN IPv4 address without a scheme, port or path.");byte[] bytes=new byte[4];int[] n=new int[4];for(int i=0;i<4;i++){if(!parts[i].matches("0|[1-9][0-9]{0,2}"))throw new IllegalArgumentException("Use an unambiguous private IPv4 address.");n[i]=Integer.parseInt(parts[i]);if(n[i]>255)throw new IllegalArgumentException("Invalid bridge address.");bytes[i]=(byte)n[i];}if(!(n[0]==10||n[0]==172&&n[1]>=16&&n[1]<=31||n[0]==192&&n[1]==168))throw new IllegalArgumentException("Only an explicitly entered private LAN IPv4 bridge on port443 is supported.");return bytes;}
 void source(){addressBytes();if(!identity.matches("[a-f0-9]{16}"))throw new IllegalArgumentException("Enter the bridge’s exact 16-hex TLS identity.");if(!pin.matches("[a-f0-9]{64}"))throw new IllegalArgumentException("Enter the independently verified SHA-256 leaf-certificate fingerprint.");if(minutes<1||minutes>30||maximum<1||maximum>100||revision<0)throw new IllegalArgumentException("Choose a 1–30 minute sunrise and 1–100% maximum.");}
 void paired(){source();if(!key.matches("[A-Za-z0-9_-]{10,128}"))throw new IllegalArgumentException("Pair with this verified bridge first.");}
 void validate(){paired();if(!light.matches("[1-9][0-9]{0,5}")||name==null||name.length()>100)throw new IllegalArgumentException("Load and explicitly choose one dimmable v1 light.");}
 String destination(){return address+":"+identity+":"+pin;}
 HueBridgeConfig revision(long value){return new HueBridgeConfig(address,identity,pin,key,light,name,enabled,minutes,maximum,restore,value);}
 static String pin(String text){return text.strip().replace(":","").replace(" ","").toLowerCase(Locale.ROOT);}
}
