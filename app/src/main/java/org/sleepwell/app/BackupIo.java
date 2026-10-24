package org.sleepwell.app;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.function.BooleanSupplier;

/** The portable document boundary is a byte limit, including multibyte UTF-8. */
final class BackupIo {
    static final int LIMIT=8*1024*1024;
    static void active(BooleanSupplier active)throws IOException {if(!active.getAsBoolean())throw new InterruptedIOException("Operation cancelled");}
    static String read(InputStream input,BooleanSupplier active)throws IOException {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] block=new byte[8192];
        int empty=0;while(true){active(active);int count=input.read(block);if(count==-1)break;if(count==0){if(++empty>16)throw new IOException("Document provider returned no data. Try again.");continue;}empty=0;if(count>LIMIT-bytes.size())throw new IOException("Backup exceeds 8 MiB.");bytes.write(block,0,count);}active(active);
        try{return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes.toByteArray())).toString();}
        catch(CharacterCodingException invalid){throw new IOException("Choose a UTF-8 JSON backup.",invalid);}
    }
    static byte[] encode(String text)throws IOException {ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(Writer writer=new OutputStreamWriter(limited(bytes),StandardCharsets.UTF_8)){writer.write(text);}return bytes.toByteArray();}
    static void jsonDepth(String text)throws IOException{int depth=0;boolean quoted=false,escape=false;for(int at=0;at<text.length();at++){char value=text.charAt(at);if(quoted){if(escape)escape=false;else if(value=='\\')escape=true;else if(value=='"')quoted=false;}else if(value=='"')quoted=true;else if(value=='{'||value=='['){if(++depth>64)throw new IOException("Backup nesting exceeds the safe limit.");}else if(value=='}'||value==']')depth--;} }
    static OutputStream limited(OutputStream output){return new FilterOutputStream(output){int count;
        @Override public void write(int value)throws IOException{if(count==LIMIT)throw new IOException("Export exceeds 8 MiB.");out.write(value);count++;}
        @Override public void write(byte[] bytes,int from,int length)throws IOException{if(length>LIMIT-count)throw new IOException("Export exceeds 8 MiB.");out.write(bytes,from,length);count+=length;}
    };}
    static void write(OutputStream output,byte[] bytes,BooleanSupplier active)throws IOException {if(bytes.length>LIMIT)throw new IOException("Export exceeds 8 MiB.");for(int at=0;at<bytes.length;at+=8192){active(active);output.write(bytes,at,Math.min(8192,bytes.length-at));}active(active);output.flush();}
    private BackupIo(){}
}
