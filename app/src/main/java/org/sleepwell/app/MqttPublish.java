package org.sleepwell.app;

import java.io.*;
import java.nio.charset.StandardCharsets;

/** MQTT 3.1.1, one clean-session non-retained QoS 1 publish; no subscriptions. */
final class MqttPublish {
    static void exchange(InputStream input,OutputStream output,String client,String username,String password,String topic,String json)throws IOException{
        ByteArrayOutputStream connect=new ByteArrayOutputStream();DataOutputStream fields=new DataOutputStream(connect);utf(fields,"MQTT");fields.writeByte(4);fields.writeByte(2|(username.isEmpty()?0:128)|(password.isEmpty()?0:64));fields.writeShort(15);utf(fields,client);if(!username.isEmpty())utf(fields,username);if(!password.isEmpty())utf(fields,password);packet(output,0x10,connect.toByteArray());
        byte[] ack=read(input,0x20);if(ack.length!=2||ack[0]!=0||ack[1]!=0)throw new Refused();
        ByteArrayOutputStream publish=new ByteArrayOutputStream();DataOutputStream data=new DataOutputStream(publish);utf(data,topic);data.writeShort(1);data.write(json.getBytes(StandardCharsets.UTF_8));packet(output,0x32,publish.toByteArray());
        byte[] puback=read(input,0x40);if(puback.length!=2||puback[0]!=0||puback[1]!=1)throw new IOException("MQTT acknowledgement mismatch");packet(output,0xe0,new byte[0]);
    }
    static final class Refused extends IOException {Refused(){super("MQTT broker rejected connection");}}
    private static void utf(DataOutputStream data,String text)throws IOException{byte[] bytes=text.getBytes(StandardCharsets.UTF_8);if(bytes.length>65535||text.indexOf('\u0000')>=0)throw new IOException("Invalid MQTT field");data.writeShort(bytes.length);data.write(bytes);}
    private static void packet(OutputStream out,int header,byte[] payload)throws IOException{out.write(header);int length=payload.length;do{int digit=length%128;length/=128;out.write(digit|(length>0?128:0));}while(length>0);out.write(payload);out.flush();}
    private static byte[] read(InputStream in,int expected)throws IOException{if(in.read()!=expected)throw new IOException("Unexpected MQTT packet");int length=0,multiplier=1;for(int n=0;n<4;n++){int b=in.read();if(b<0)throw new EOFException();length+=(b&127)*multiplier;if(length>1024)throw new IOException("MQTT response too large");if((b&128)==0){byte[] data=new byte[length];new DataInputStream(in).readFully(data);return data;}multiplier*=128;}throw new IOException("Invalid MQTT length");}
}
