package org.sleepwell.app;

import android.media.*;
import android.os.SystemClock;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.function.BooleanSupplier;

/** Worker-only PCM16 -> AAC-LC/MPEG-4. No playback, resampling or synthesized gap filling.
 * Platform contracts: https://developer.android.com/reference/android/media/MediaCodec
 * and https://developer.android.com/reference/android/media/MediaMuxer . */
final class AacPcmEncoder {
    static final class Pcm {
        final int rate,channels,align;final long offset,frames;
        Pcm(int rate,int channels,long offset,long frames){this.rate=rate;this.channels=channels;align=channels*2;this.offset=offset;this.frames=frames;}
        long duration(){return frames*1000/rate;}
    }
    private static long u32(RandomAccessFile in)throws IOException{return Integer.toUnsignedLong(Integer.reverseBytes(in.readInt()));}
    private static int u16(RandomAccessFile in)throws IOException{return Short.toUnsignedInt(Short.reverseBytes(in.readShort()));}
    private static void c2LcFrameContract(MediaFormat format,int rate,int channels)throws IOException {
        ByteBuffer config=format.getByteBuffer("csd-0");if(config==null||config.remaining()<2)throw new IOException("AAC frame timing is unavailable. Use WAV/ZIP export.");
        ByteBuffer bits=config.duplicate();int header=((bits.get()&255)<<8)|(bits.get()&255),frequency=(header>>7)&15;
        int[] rates={96000,88200,64000,48000,44100,32000,24000,22050,16000,12000,11025,8000,7350};
        if((header>>11)!=MediaCodecInfo.CodecProfileLevel.AACObjectLC||frequency>=rates.length||rates[frequency]!=rate
                ||((header>>3)&15)!=channels||(header&4)!=0)throw new IOException("Unsupported AAC frame timing. Use WAV/ZIP export.");
    }
    static Pcm inspect(RandomAccessFile in)throws IOException {
        in.seek(0);long length=in.length();if(length<44||length>16L*1024*1024||in.readInt()!=0x52494646)throw new IOException("Unsupported WAV recording.");
        long limit=u32(in)+8;if(limit!=length||in.readInt()!=0x57415645)throw new IOException("Truncated or unsupported WAV recording.");
        int channels=0,rate=0;long data=-1,bytes=0;boolean format=false;
        while(in.getFilePointer()+8<=limit){int tag=in.readInt();long size=u32(in),at=in.getFilePointer();if(size>limit-at)throw new IOException("Truncated WAV recording.");
            if(tag==0x666d7420){if(format||size<16||u16(in)!=1)throw new IOException("Export requires a PCM16 WAV recording.");format=true;channels=u16(in);long hz=u32(in),byteRate=u32(in);int align=u16(in),bits=u16(in);if(channels<1||channels>2||hz<8000||hz>96000||bits!=16||align!=channels*2||byteRate!=hz*align)throw new IOException("Unsupported PCM audio format.");rate=(int)hz;}
            else if(tag==0x64617461){if(data>=0)throw new IOException("Multiple PCM chunks are unsupported.");data=at;bytes=size;}
            long next=at+size+(size&1);if(next>limit)throw new IOException("Truncated WAV padding.");in.seek(next);
        }
        if(!format||data<0||bytes<=0||bytes%(channels*2)!=0||bytes/(channels*2)*1000/rate>300000)throw new IOException("Unsupported recording duration.");
        return new Pcm(rate,channels,data,bytes/(channels*2));
    }
    static void encode(File wav,File destination,long first,long end,BooleanSupplier active)throws Exception {
        MediaCodec codec=null;MediaMuxer muxer=null;boolean started=false,muxed=false,completed=false;Exception original=null;
        try(RandomAccessFile input=new RandomAccessFile(wav,"r")){
            Pcm pcm=inspect(input);if(first<0||end<=first||end>pcm.frames)throw new IOException("The selected PCM interval is unavailable.");
            MediaFormat format=MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,pcm.rate,pcm.channels);
            format.setInteger(MediaFormat.KEY_AAC_PROFILE,MediaCodecInfo.CodecProfileLevel.AACObjectLC);format.setInteger(MediaFormat.KEY_BIT_RATE,pcm.channels==1?64000:96000);format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,16384);
            try{codec=MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);codec.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);codec.start();started=true;}catch(Exception unavailable){throw new IOException("AAC encoding is unavailable for this recording on this phone. Use the original WAV/ZIP export.",unavailable);}
            boolean c2SampleClock=codec.getName().equals("c2.android.aac.encoder");
            muxer=new MediaMuxer(destination.getAbsolutePath(),MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();
            input.seek(pcm.offset+first*pcm.align);long frames=0,total=end-first,lastProgress=SystemClock.elapsedRealtime(),began=lastProgress;boolean sentEnd=false,receivedEnd=false;int track=-1,samples=0;byte[] buffer=new byte[16384];
            while(!receivedEnd){NightAudioArchive.active(active);long now=SystemClock.elapsedRealtime();if(now-lastProgress>15000||now-began>120000)throw new IOException("AAC encoding timed out. Use WAV/ZIP or export fewer recordings.");boolean progress=false;
                if(!sentEnd){int index=codec.dequeueInputBuffer(10000);if(index>=0){ByteBuffer target=codec.getInputBuffer(index);if(target==null)throw new IOException("AAC input buffer unavailable.");target.clear();int count=(int)Math.min(total-frames,Math.min(buffer.length,target.remaining())/pcm.align);if(count<=0)throw new IOException("AAC encoder has an invalid input buffer.");long pts=frames*1000000L/pcm.rate;
                    input.readFully(buffer,0,count*pcm.align);target.put(buffer,0,count*pcm.align);frames+=count;sentEnd=frames==total;
                    // Signal completion with the final real PCM; no source frames are padded or dropped.
                    codec.queueInputBuffer(index,0,count*pcm.align,pts,sentEnd?MediaCodec.BUFFER_FLAG_END_OF_STREAM:0);progress=true;}}
                int index=codec.dequeueOutputBuffer(info,10000);
                if(index==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){if(muxed)throw new IOException("AAC output format changed unexpectedly.");MediaFormat actual=codec.getOutputFormat();if(c2SampleClock)c2LcFrameContract(actual,pcm.rate,pcm.channels);track=muxer.addTrack(actual);muxer.start();muxed=true;progress=true;}
                else if(index>=0){try{ByteBuffer output=codec.getOutputBuffer(index);if((info.flags&MediaCodec.BUFFER_FLAG_CODEC_CONFIG)==0&&info.size>0){if(!muxed||output==null)throw new IOException("AAC output format unavailable.");output.position(info.offset);output.limit(info.offset+info.size);
                    if(c2SampleClock){if((info.flags&MediaCodec.BUFFER_FLAG_PARTIAL_FRAME)!=0)throw new IOException("Partial AAC frames are unsupported. Use WAV/ZIP export.");
                        // This AOSP encoder emits one AU per output; verified LC frameLengthFlag=0
                        // means 1024 PCM frames. Its EOS work can shift delayed-tail timestamps.
                        // Clock only our contiguous source export by its encoded sample count;
                        // never apply this correction to imported files or arbitrary vendor codecs.
                        info.presentationTimeUs=samples*1024L*1000000L/pcm.rate;
                    }
                    muxer.writeSampleData(track,output,info);samples++;}receivedEnd=(info.flags&MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0;progress=true;}finally{codec.releaseOutputBuffer(index,false);}}
                if(progress)lastProgress=SystemClock.elapsedRealtime();if(destination.length()>8L*1024*1024)throw new IOException("Encoded recording exceeds the export limit.");
            }
            if(!muxed||samples==0||frames!=total)throw new IOException("AAC encoding did not produce complete audio.");NightAudioArchive.active(active);muxed=false;muxer.stop();completed=true;
        }catch(Exception failure){original=failure;throw failure;}
        finally{
            Exception cleanup=null;if(codec!=null){if(started)try{codec.stop();}catch(Exception error){cleanup=error;}try{codec.release();}catch(Exception error){if(cleanup==null)cleanup=error;else cleanup.addSuppressed(error);}}
            if(muxer!=null){if(muxed)try{muxer.stop();}catch(Exception error){if(cleanup==null)cleanup=error;else cleanup.addSuppressed(error);}try{muxer.release();}catch(Exception error){if(cleanup==null)cleanup=error;else cleanup.addSuppressed(error);}}
            if(!completed||cleanup!=null)destination.delete();if(cleanup!=null){if(original!=null)original.addSuppressed(cleanup);else throw new IOException("Encoded audio could not be finalized.",cleanup);}
        }
        if(!destination.isFile()||destination.length()<32)throw new IOException("Encoded audio is empty.");
        try(FileOutputStream sync=new FileOutputStream(destination,true)){sync.getFD().sync();}
    }
    private AacPcmEncoder(){}
}
