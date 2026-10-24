package org.sleepwell.app;

import java.net.URL;
import javax.net.ssl.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

final class HomeAssistantHttp {
    interface Connections {HttpsURLConnection open(URL url)throws IOException;}
    static final Connections DEFAULT=url->(HttpsURLConnection)url.openConnection();
    static String request(SmartLightConfig config,String path,String body,Connections connections)throws IOException{
        config.validate();HttpsURLConnection connection=null;try{connection=connections.open(new URL(config.base()+path));connection.setConnectTimeout(5000);connection.setReadTimeout(5000);connection.setInstanceFollowRedirects(false);connection.setUseCaches(false);connection.setRequestProperty("Authorization","Bearer "+config.token);connection.setRequestProperty("Accept","application/json");connection.setRequestMethod(body==null?"GET":"POST");if(body!=null){byte[] bytes=body.getBytes(StandardCharsets.UTF_8);connection.setRequestProperty("Content-Type","application/json; charset=utf-8");connection.setDoOutput(true);connection.setFixedLengthStreamingMode(bytes.length);try(OutputStream out=connection.getOutputStream()){out.write(bytes);}}
            int status=connection.getResponseCode();if(status<200||status>=300)throw new IOException(status==401||status==403?"Home Assistant rejected access":status==404?"Home Assistant light was not found":"Home Assistant request failed (HTTP "+status+")");try(InputStream in=connection.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buffer=new byte[2048];int read,total=0;while((read=in.read(buffer))!=-1){total+=read;if(total>65536)throw new IOException("Home Assistant response was too large");out.write(buffer,0,read);}return new String(out.toByteArray(),StandardCharsets.UTF_8);}
        }catch(SSLException e){throw new IOException("Home Assistant TLS verification failed");}catch(java.net.SocketTimeoutException e){throw new IOException("Home Assistant timed out");}finally{if(connection!=null)connection.disconnect();}
    }
}
