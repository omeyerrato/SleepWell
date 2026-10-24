package org.sleepwell.app;

import org.junit.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public class BackupIoTest {
    @Test public void exactLimitAndWideUtf8RoundTrip()throws Exception{String wide="界".repeat(10000);assertEquals(wide,BackupIo.read(new ByteArrayInputStream(BackupIo.encode(wide)),()->true));byte[] exact=new byte[BackupIo.LIMIT];assertEquals(BackupIo.LIMIT,BackupIo.read(new ByteArrayInputStream(exact),()->true).length());assertEquals(BackupIo.LIMIT,BackupIo.encode("x".repeat(BackupIo.LIMIT)).length);}
    @Test public void limitIsBytesRatherThanCharacters(){assertThrows(IOException.class,()->BackupIo.encode("界".repeat(BackupIo.LIMIT/3+1)));assertThrows(IOException.class,()->BackupIo.read(new ByteArrayInputStream(new byte[BackupIo.LIMIT+1]),()->true));}
    @Test public void malformedUtf8IsNeverSilentlyReplaced(){assertThrows(IOException.class,()->BackupIo.read(new ByteArrayInputStream(new byte[]{(byte)0xc3,0x28}),()->true));}
    @Test public void cancelledReadCannotReturnAReview(){AtomicInteger checks=new AtomicInteger();assertThrows(InterruptedIOException.class,()->BackupIo.read(new ByteArrayInputStream(new byte[20000]),()->checks.incrementAndGet()<2));assertEquals(2,checks.get());}
    @Test public void partialExportAndCancellationAreHonest()throws Exception{byte[] data=new byte[20000];AtomicInteger checks=new AtomicInteger();ByteArrayOutputStream out=new ByteArrayOutputStream();assertThrows(InterruptedIOException.class,()->BackupIo.write(out,data,()->checks.incrementAndGet()<2));assertEquals(8192,out.size());}
    @Test public void oversizeOutputIsRejectedBeforeFirstByte(){ByteArrayOutputStream out=new ByteArrayOutputStream();assertThrows(IOException.class,()->BackupIo.write(out,new byte[BackupIo.LIMIT+1],()->true));assertEquals(0,out.size());}
    @Test public void writePropagatesProviderFailureAndPreservesItsPrefix(){ByteArrayOutputStream prefix=new ByteArrayOutputStream();OutputStream broken=new OutputStream(){public void write(int b)throws IOException{if(prefix.size()==3)throw new IOException("Provider failed");prefix.write(b);}};assertThrows(IOException.class,()->BackupIo.write(broken,"document".getBytes(StandardCharsets.UTF_8),()->true));assertEquals("doc",prefix.toString());}
    @Test public void boundedWriterCountsMixedBulkAndSingleByteWrites()throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();OutputStream bounded=BackupIo.limited(out);bounded.write(new byte[BackupIo.LIMIT-1]);bounded.write(0);assertThrows(IOException.class,()->bounded.write(0));assertEquals(BackupIo.LIMIT,out.size());}
    @Test public void quotedBracketsAreDataButPathologicalNestingRejects()throws Exception{BackupIo.jsonDepth("{\"note\":\"[[[\\\"[[[\"}");assertThrows(IOException.class,()->BackupIo.jsonDepth("[".repeat(65)+"]".repeat(65)));}
    @Test public void cancelledBeforeOpenCompletesDoesNotReadAnyProviderBytes(){AtomicInteger reads=new AtomicInteger();InputStream input=new InputStream(){public int read(){reads.incrementAndGet();return 0;}};assertThrows(InterruptedIOException.class,()->BackupIo.read(input,()->false));assertEquals(0,reads.get());}
    @Test public void brokenZeroByteProviderCannotSpinForever(){InputStream input=new InputStream(){public int read(){return 0;}public int read(byte[] bytes){return 0;}};assertThrows(IOException.class,()->BackupIo.read(input,()->true));}
}
