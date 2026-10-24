package org.sleepwell.app;

import org.junit.Test;
import java.io.*;
import static org.junit.Assert.*;

public class ToneWaveformTest {
    private byte[] wav(){byte[] data=new byte[44+16000];System.arraycopy(new byte[]{82,73,70,70},0,data,0,4);put(data,4,data.length-8,4);System.arraycopy(new byte[]{87,65,86,69,102,109,116,32},0,data,8,8);put(data,16,16,4);put(data,20,1,2);put(data,22,1,2);put(data,24,8000,4);put(data,28,16000,4);put(data,32,2,2);put(data,34,16,2);System.arraycopy(new byte[]{100,97,116,97},0,data,36,4);put(data,40,16000,4);for(int n=0;n<8000;n++)put(data,44+n*2,n<4000?0:16384,2);return data;}
    private void put(byte[] data,int at,int value,int count){for(int n=0;n<count;n++)data[at+n]=(byte)(value>>n*8);}
    @Test public void actualPcmSilenceAndHalfScaleStayDistinctNotDecorative()throws Exception{ToneWaveform wave=ToneWaveform.read(new ByteArrayInputStream(wav()));assertEquals(1000,wave.durationMs);assertEquals(32,wave.peaks.length);for(int n=0;n<16;n++)assertEquals(0,wave.peaks[n],0);for(int n=16;n<32;n++)assertEquals(.5,wave.peaks[n],0);}
    @Test public void unsupportedAndTruncatedAudioNeverProduceAWaveform()throws Exception{for(int length:new int[]{0,20,44,8000})try{ToneWaveform.read(new ByteArrayInputStream(java.util.Arrays.copyOf(wav(),length)));fail();}catch(IOException expected){}byte[] compressed=wav();put(compressed,20,3,2);try{ToneWaveform.read(new ByteArrayInputStream(compressed));fail();}catch(IOException expected){}}
    @Test public void resourceBoundRejectsOversizedInputBeforeDecode()throws Exception{InputStream input=new InputStream(){int count;public int read(){return ++count<=4*1024*1024+1?0:-1;}};try{ToneWaveform.read(input);fail();}catch(IOException expected){assertTrue(expected.getMessage().contains("bound"));}}
}
