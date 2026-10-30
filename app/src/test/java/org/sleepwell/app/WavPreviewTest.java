package org.sleepwell.app;
import java.io.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class WavPreviewTest {
    private byte[] wav(int channels,int... samples)throws Exception {ByteArrayOutputStream out=new ByteArrayOutputStream();out.write("RIFF".getBytes("US-ASCII"));number(out,36+samples.length*2,4);out.write("WAVEfmt ".getBytes("US-ASCII"));number(out,16,4);number(out,1,2);number(out,channels,2);number(out,16000,4);number(out,16000*channels*2,4);number(out,channels*2,2);number(out,16,2);out.write("data".getBytes("US-ASCII"));number(out,samples.length*2,4);for(int sample:samples)number(out,sample,2);return out.toByteArray();}
    private void number(OutputStream out,int value,int count)throws Exception {for(int n=0;n<count;n++)out.write((value>>>(n*8))&255);}
    @Test public void realSamplePeaksIncludeNegativeFullScaleAndTrueSilence()throws Exception {float[] peaks=WavPreview.read(new ByteArrayInputStream(wav(1,0,0,-32768,1200,16384,-4000,0,0)),4);assertArrayEquals(new float[]{0,1,.5f,0},peaks,.00001f);}
    @Test public void stereoChannelsShareTheirActualFrameBin()throws Exception {float[] peaks=WavPreview.read(new ByteArrayInputStream(wav(2,0,32767,-16384,0)),2);assertArrayEquals(new float[]{32767/32768f,.5f},peaks,.00001f);}
    @Test public void damagedChunksAndPartialFramesAreNotDrawnAsAudio()throws Exception {byte[] truncated=wav(1,1,2);try{WavPreview.read(new ByteArrayInputStream(java.util.Arrays.copyOf(truncated,truncated.length-1)),2);fail();}catch(IOException expected){}try{WavPreview.read(new ByteArrayInputStream(wav(2,1,2,3)),2);fail();}catch(IOException expected){}}
    @Test public void stalledSourceFailsRatherThanSpinning()throws Exception {try{WavPreview.read(new InputStream(){public int read(){return 0;}public int read(byte[] b,int from,int length){return 0;}},4);fail();}catch(IOException expected){}}
    @Test public void previewUsesRealMiddleExcerptRatherThanWholeLoopOrInventedPeaks()throws Exception {int[] samples=new int[16000];samples[0]=32767;samples[8000]=-16384;float[] peaks=WavPreview.excerpt(new ByteArrayInputStream(wav(1,samples)),128);assertEquals(128,peaks.length);float max=0;int nonzero=0;for(float peak:peaks){max=Math.max(max,peak);if(peak>0)nonzero++;}assertEquals(.5f,max,.00001f);assertEquals(1,nonzero);}
}
