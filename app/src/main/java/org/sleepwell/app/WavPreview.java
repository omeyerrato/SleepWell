package org.sleepwell.app;

import java.io.*;
import java.nio.charset.StandardCharsets;

/** Bounded PCM16 RIFF preview reader; peak bins contain real sample magnitudes, including silence. */
final class WavPreview {
    private static final int LIMIT=16*1024*1024;
    static float[] read(InputStream input,int bins)throws IOException {
        if(bins<1||bins>1024)throw new IllegalArgumentException("Invalid preview bins");
        ByteArrayOutputStream output=new ByteArrayOutputStream();byte[] block=new byte[8192];int stalled=0;while(output.size()<=LIMIT){int count=input.read(block,0,Math.min(block.length,LIMIT+1-output.size()));if(count<0)break;if(count==0){if(++stalled>8)throw new IOException("Stalled preview source");continue;}stalled=0;output.write(block,0,count);}byte[] bytes=output.toByteArray();if(bytes.length>LIMIT||bytes.length<12||!tag(bytes,0,"RIFF")||!tag(bytes,8,"WAVE"))throw new IOException("Unsupported WAV preview");
        int channels=0,bits=0,format=0,data=-1,length=0;
        for(int cursor=12;cursor+8<=bytes.length;){long size=u32(bytes,cursor+4),end=cursor+8L+size;if(end>bytes.length)throw new IOException("Truncated WAV chunk");if(tag(bytes,cursor,"fmt ")){if(size<16)throw new IOException("Missing PCM format");format=u16(bytes,cursor+8);channels=u16(bytes,cursor+10);bits=u16(bytes,cursor+22);}else if(tag(bytes,cursor,"data")&&data<0){data=cursor+8;length=(int)size;}long next=end+(size&1);if(next>Integer.MAX_VALUE)throw new IOException("Oversized WAV chunk");cursor=(int)next;}
        if(format!=1||channels<1||channels>8||bits!=16||data<0||length==0||length%(channels*2)!=0)throw new IOException("PCM16 frames required");
        int frames=length/(channels*2);float[] result=new float[bins];for(int frame=0;frame<frames;frame++){int bin=(int)((long)frame*bins/frames);for(int channel=0;channel<channels;channel++){int offset=data+(frame*channels+channel)*2;short sample=(short)u16(bytes,offset);result[bin]=Math.max(result[bin],Math.abs((int)sample)/32768f);}}return result;
    }
    private static boolean tag(byte[] bytes,int at,String value){if(at+4>bytes.length)return false;return new String(bytes,at,4,StandardCharsets.US_ASCII).equals(value);}
    private static int u16(byte[] b,int at){return(b[at]&255)|((b[at+1]&255)<<8);}
    private static long u32(byte[] b,int at){return(b[at]&255L)|((b[at+1]&255L)<<8)|((b[at+2]&255L)<<16)|((b[at+3]&255L)<<24);}
    private WavPreview(){}
}
