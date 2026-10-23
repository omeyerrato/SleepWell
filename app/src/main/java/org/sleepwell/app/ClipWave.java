package org.sleepwell.app;

import java.io.*;

/** Bounded reader for the PCM16 WAV files saved by NoiseRecorder. No invented waveform. */
final class ClipWave {
    final float[] peaks; final long durationMs;
    private ClipWave(float[] peaks,long durationMs){this.peaks=peaks;this.durationMs=durationMs;}
    private static long u32(RandomAccessFile in)throws IOException{return Integer.toUnsignedLong(Integer.reverseBytes(in.readInt()));}
    private static int u16(RandomAccessFile in)throws IOException{return Short.toUnsignedInt(Short.reverseBytes(in.readShort()));}
    static ClipWave read(File file)throws IOException{return read(file,0,Long.MAX_VALUE);}
    static ClipWave read(File file,long fromMs,long toMs)throws IOException{
        try(RandomAccessFile in=new RandomAccessFile(file,"r")){
            long length=in.length();if(length<44||length>16*1024*1024)throw new IOException("Unsupported recording size");
            if(in.readInt()!=0x52494646)throw new IOException("Not a WAV recording");long limit=u32(in)+8;
            if(limit>length||limit<44||in.readInt()!=0x57415645)throw new IOException("Truncated WAV recording");
            int channels=0,rate=0,align=0;long data=-1,bytes=0;
            while(in.getFilePointer()+8<=limit){
                if(Thread.currentThread().isInterrupted())throw new InterruptedIOException();
                int type=in.readInt();long size=u32(in),at=in.getFilePointer();if(size>limit-at)throw new IOException("Truncated audio chunk");
                if(type==0x666d7420){if(size<16)throw new IOException("Invalid WAV format");int format=u16(in);channels=u16(in);long hz=u32(in),byteRate=u32(in);align=u16(in);int bits=u16(in);
                    if(format!=1||channels<1||channels>2||bits!=16||hz<8000||hz>192000||align!=channels*2||byteRate!=hz*align)throw new IOException("Only PCM16 recordings are supported");rate=(int)hz;
                }else if(type==0x64617461){if(data!=-1)throw new IOException("Multiple audio chunks are unsupported");data=at;bytes=size;}
                long next=at+size+(size&1);if(next>limit)throw new IOException("Truncated audio padding");in.seek(next);
            }
            if(data<0||rate==0||bytes==0||bytes%align!=0)throw new IOException("No complete PCM frames");
            long frames=bytes/align,duration=frames*1000/rate;if(duration>5*60000L)throw new IOException("Recording exceeds five minutes");
            if(fromMs<0||fromMs>=duration||toMs!=Long.MAX_VALUE&&(toMs<=fromMs||toMs>duration))throw new IOException("Recording does not contain the requested interval");
            long first=fromMs*rate/1000,last=toMs==Long.MAX_VALUE?frames:toMs*rate/1000;frames=last-first;duration=frames*1000/rate;bytes=frames*align;
            float[] peaks=new float[64];in.seek(data+first*align);byte[] buffer=new byte[8192];long index=0,remaining=bytes;
            while(remaining>0){if(Thread.currentThread().isInterrupted())throw new InterruptedIOException();int count=(int)Math.min(remaining,buffer.length);in.readFully(buffer,0,count);for(int offset=0;offset<count;offset+=align){int bin=(int)Math.min(63,index*64/frames);for(int channel=0;channel<channels;channel++){int n=offset+channel*2;short sample=(short)((buffer[n]&255)|(buffer[n+1]<<8));peaks[bin]=Math.max(peaks[bin],Math.abs((int)sample)/32768f);}index++;}remaining-=count;}
            return new ClipWave(peaks,duration);
        }
    }
}
