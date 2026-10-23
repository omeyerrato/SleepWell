package org.sleepwell.app;

import java.io.*;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;
import static org.junit.Assert.*;

public class AudioCopyTest {
    private byte[] copy(byte[] input,int limit)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();AtomicLong count=new AtomicLong();assertEquals(input.length,AudioCopy.copy(new ByteArrayInputStream(input),out,limit,()->{},count::set));assertEquals(input.length,count.get());return out.toByteArray();}
    @Test public void exactLimitAndUnknownLengthAreAccepted()throws Exception{assertArrayEquals(new byte[]{1,2,3},copy(new byte[]{1,2,3},3));}
    @Test public void overflowIsNeverWritten()throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();try{AudioCopy.copy(new ByteArrayInputStream(new byte[5]),out,4,()->{},n->{});fail();}catch(IOException expected){assertTrue(out.size()<=4);}}
    @Test public void emptyDocumentIsRejected()throws Exception{try{copy(new byte[0],4);fail();}catch(IOException expected){assertTrue(expected.getMessage().contains("Empty"));}}
    @Test public void cancellationAfterReadPreventsWriting()throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();int[] calls={0};try{AudioCopy.copy(new ByteArrayInputStream(new byte[3]),out,4,()->{if(++calls[0]>1)throw new IOException("cancel");},n->{});fail();}catch(IOException expected){assertEquals(0,out.size());}}
    @Test public void repeatedEmptyReadsCannotSpinForever()throws Exception{InputStream stalled=new InputStream(){public int read(){return 0;}public int read(byte[] b,int off,int len){return 0;}};try{AudioCopy.copy(stalled,new ByteArrayOutputStream(),4,()->{},n->{});fail();}catch(IOException expected){assertTrue(expected.getMessage().contains("responding"));}}
    @Test public void providerFailurePropagates()throws Exception{InputStream broken=new InputStream(){public int read()throws IOException{throw new IOException("provider");}};try{AudioCopy.copy(broken,new ByteArrayOutputStream(),4,()->{},n->{});fail();}catch(IOException expected){assertEquals("provider",expected.getMessage());}}
    @Test public void partialReadsPreserveEveryByte()throws Exception{byte[] input=new byte[80];for(int i=0;i<input.length;i++)input[i]=(byte)i;InputStream shortReads=new ByteArrayInputStream(input){public synchronized int read(byte[] b,int off,int len){return super.read(b,off,Math.min(3,len));}};ByteArrayOutputStream out=new ByteArrayOutputStream();assertEquals(80,AudioCopy.copy(shortReads,out,80,()->{},n->{}));assertArrayEquals(input,out.toByteArray());}
}
