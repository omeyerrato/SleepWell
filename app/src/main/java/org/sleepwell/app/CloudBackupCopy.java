package org.sleepwell.app;

import java.io.*;
import java.security.MessageDigest;
import java.util.function.*;

/** Creates a new version and proves read-back equality before accepting a receipt. Never deletes. */
final class CloudBackupCopy {
    static final class Document {final String uri,name;Document(String uri,String name){this.uri=uri;this.name=name;}}
    static final class Receipt {final Document document;final int size;final String sha;Receipt(Document document,int size,String sha){this.document=document;this.size=size;this.sha=sha;}}
    interface Provider {Document create(String name)throws Exception;OutputStream output(Document document)throws Exception;InputStream input(Document document)throws Exception;void cancel();}
    interface Source {byte[] prepare(BooleanSupplier active)throws Exception;}
    interface Created {void created(Document document)throws Exception;}
    static Receipt save(Source source,Provider provider,String name,BooleanSupplier active,Created created)throws Exception{
        byte[] bytes=source.prepare(active);if(bytes.length==0||bytes.length>BackupIo.LIMIT)throw new IOException("Backup has an invalid size.");BackupIo.active(active);String expected=sha(bytes);Document document=provider.create(name);if(document==null||document.uri==null||!document.uri.startsWith("content:"))throw new IOException("Provider did not create a document.");created.created(document);BackupIo.active(active);try(OutputStream out=provider.output(document)){BackupIo.write(out,bytes,active);}BackupIo.active(active);MessageDigest digest=MessageDigest.getInstance("SHA-256");int count=0,empty=0;byte[] buffer=new byte[8192];try(InputStream in=provider.input(document)){for(;;){BackupIo.active(active);int n=in.read(buffer);if(n==-1)break;if(n==0){if(++empty>16)throw new IOException("Provider verification stalled.");continue;}empty=0;if(n>BackupIo.LIMIT-count)throw new IOException("Provider copy exceeds 8 MiB.");count+=n;digest.update(buffer,0,n);}}BackupIo.active(active);if(count!=bytes.length||!expected.equals(hex(digest.digest())))throw new IOException("Provider read-back did not match. This version is unverified.");return new Receipt(document,count,expected);
    }
    static String sha(byte[] bytes)throws Exception{return hex(MessageDigest.getInstance("SHA-256").digest(bytes));}
    private static String hex(byte[] bytes){StringBuilder value=new StringBuilder();for(byte b:bytes)value.append(String.format(java.util.Locale.ROOT,"%02x",b&255));return value.toString();}
}
