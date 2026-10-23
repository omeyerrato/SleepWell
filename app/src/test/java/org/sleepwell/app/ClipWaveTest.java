package org.sleepwell.app;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;

public class ClipWaveTest {
    private byte[] wave(int channels,int frames){ByteBuffer b=ByteBuffer.allocate(44+frames*channels*2).order(ByteOrder.LITTLE_ENDIAN);b.putInt(0x46464952).putInt(b.capacity()-8).putInt(0x45564157).putInt(0x20746d66).putInt(16).putShort((short)1).putShort((short)channels).putInt(16000).putInt(32000*channels).putShort((short)(channels*2)).putShort((short)16).putInt(0x61746164).putInt(frames*channels*2);return b.array();}
    private ClipWave read(byte[] bytes)throws Exception{File file=File.createTempFile("sleepwell-wave-",".wav");try{Files.write(file.toPath(),bytes);return ClipWave.read(file);}finally{assertTrue(file.delete());}}
    private void rejected(byte[] bytes)throws Exception{try{read(bytes);fail("Malformed WAV must reject");}catch(IOException expected){}}
    @Test public void silenceHasActualDurationAndNoInventedPeaks()throws Exception{ClipWave w=read(wave(1,16000));assertEquals(1000,w.durationMs);for(float peak:w.peaks)assertEquals(0,peak,0);}
    @Test public void stereoPeaksUseBothChannelsAndSignedMinimum()throws Exception{byte[] bytes=wave(2,16000);ByteBuffer b=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);b.putShort(44+15999*4+2,Short.MIN_VALUE);ClipWave w=read(bytes);assertEquals(1000,w.durationMs);assertEquals(1,w.peaks[63],0);for(int n=0;n<63;n++)assertEquals(0,w.peaks[n],0);}
    @Test public void truncatedAndOverlargeChunkCannotReadPastFile()throws Exception{byte[] bytes=wave(1,10);rejected(java.util.Arrays.copyOf(bytes,bytes.length-1));ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putInt(40,Integer.MAX_VALUE);rejected(bytes);}
    @Test public void unsupportedFormatAndInconsistentRatesReject()throws Exception{for(int offset:new int[]{20,22,28,32,34}){byte[] bytes=wave(1,10);ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putShort(offset,(short)7);rejected(bytes);}}
    @Test public void emptyAndPartialFramesReject()throws Exception{rejected(wave(1,0));byte[] b=wave(2,10);ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).putInt(40,39);rejected(b);}
    @Test public void unknownEvenAndOddPaddedChunksAreSkipped()throws Exception{byte[] original=wave(1,160);ByteBuffer b=ByteBuffer.allocate(original.length+12).order(ByteOrder.LITTLE_ENDIAN);b.put(original,0,36).putInt(0x4b4e554a).putInt(3).put(new byte[]{1,2,3,0}).put(original,36,original.length-36);b.putInt(4,b.capacity()-8);assertEquals(10,read(b.array()).durationMs);}
    @Test public void intervalPeaksContainOnlyTheActualSelectedPcm()throws Exception{byte[] bytes=wave(1,32000);ByteBuffer b=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);b.putShort(44,(short)16000);b.putShort(44+16000*2,Short.MIN_VALUE);File file=File.createTempFile("sleepwell-slice-",".wav");try{Files.write(file.toPath(),bytes);ClipWave slice=ClipWave.read(file,1000,1500);assertEquals(500,slice.durationMs);assertEquals(1,slice.peaks[0],0);for(int n=1;n<64;n++)assertEquals(0,slice.peaks[n],0);}finally{assertTrue(file.delete());}}
    @Test public void invalidOrPartiallyCoveredIntervalsReject()throws Exception{File file=File.createTempFile("sleepwell-slice-",".wav");try{Files.write(file.toPath(),wave(1,16000));for(long[] interval:new long[][]{{-1,100},{0,1001},{1000,1000},{200,100}})try{ClipWave.read(file,interval[0],interval[1]);fail();}catch(IOException expected){}}finally{assertTrue(file.delete());}}
}
