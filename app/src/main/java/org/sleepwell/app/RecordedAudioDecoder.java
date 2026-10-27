package org.sleepwell.app;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaDataSource;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.SystemClock;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.function.BooleanSupplier;

/** Worker-only, bounded file decoding. Never opens a URI, plays audio or writes a file.
 * Platform contracts: developer.android.com/reference/android/media/MediaCodec
 * and developer.android.com/reference/android/media/MediaDataSource . */
final class RecordedAudioDecoder {
    static final int MAX_BYTES=16*1024*1024,MAX_SECONDS=120;
    private static final Set<String> TYPES=Set.of("audio/mp4a-latm","audio/mpeg","audio/opus","audio/vorbis","audio/flac","audio/3gpp","audio/amr-wb");
    static final class Result {
        final short[] mono;final int rate,channels;final long firstPresentationUs;final String mime,decoder;
        Result(short[] pcm,int rate,int channels,long first,String mime,String decoder){mono=pcm;this.rate=rate;this.channels=channels;firstPresentationUs=first;this.mime=mime;this.decoder=decoder;}
    }
    private static void check(BooleanSupplier active)throws IOException{RecordedRecognitionInput.check(active);}
    static Result decode(byte[] bytes,BooleanSupplier active)throws Exception {
        check(active);if(bytes==null||bytes.length<12||bytes.length>MAX_BYTES)throw new IOException("Choose a complete audio file up to 16 MiB and two minutes.");
        if(tag(bytes,0).equals("RIFF")&&tag(bytes,8).equals("WAVE"))return wave(bytes,active);
        MediaExtractor extractor=new MediaExtractor();MediaCodec codec=null;Mono samples=null;boolean started=false;Throwable original=null;MemorySource source=new MemorySource(bytes,active);
        try{
            extractor.setDataSource(source);check(active);
            if(extractor.getDrmInitData()!=null)throw new IOException("Protected audio cannot be analyzed. Choose an unprotected recording.");
            int track=-1;MediaFormat format=null;
            if(extractor.getTrackCount()>16)throw new IOException("Choose a file with one audio track.");
            for(int i=0;i<extractor.getTrackCount();i++){
                MediaFormat next=extractor.getTrackFormat(i);String type=next.getString(MediaFormat.KEY_MIME);
                if(type!=null&&type.startsWith("audio/")){if(track>=0)throw new IOException("Choose a file with one audio track.");track=i;format=next;}
                else throw new IOException("Choose an audio recording without video or other tracks.");
            }
            if(track<0||format==null)throw new IOException("This file has no supported audio track.");
            String mime=format.getString(MediaFormat.KEY_MIME);if(!TYPES.contains(mime))throw new IOException("This audio format is unsupported. Choose AAC, MP3, FLAC, Ogg or PCM16 WAV.");
            int rate=format.getInteger(MediaFormat.KEY_SAMPLE_RATE),channels=format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);validate(rate,channels);
            if(format.containsKey(MediaFormat.KEY_DURATION)&&format.getLong(MediaFormat.KEY_DURATION)>MAX_SECONDS*1000000L)throw new IOException("Choose a complete recording no longer than two minutes.");
            format.setInteger(MediaFormat.KEY_PCM_ENCODING,AudioFormat.ENCODING_PCM_16BIT);
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,262144);
            extractor.selectTrack(track);codec=MediaCodec.createDecoderByType(mime);codec.configure(format,null,null,0);codec.start();started=true;
            String decoder=codec.getName();samples=new Mono(rate);MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();
            StringBuilder packetTiming=new StringBuilder(),outputTiming=new StringBuilder();
            boolean inputEnd=false,outputEnd=false,outputFormat=false;long first=Long.MIN_VALUE,began=SystemClock.elapsedRealtime(),lastProgress=began,lastPts=Long.MIN_VALUE;
            while(!outputEnd){
                check(active);long now=SystemClock.elapsedRealtime();if(now-began>30000||now-lastProgress>10000)throw new IOException("Audio decoding timed out. Try a shorter recording.");boolean progress=false;
                if(!inputEnd){int index=codec.dequeueInputBuffer(10000);if(index>=0){
                    ByteBuffer target=codec.getInputBuffer(index);if(target==null)throw new IOException("Audio decoder input is unavailable.");target.clear();
                    long size=extractor.getSampleSize();if(size<0){codec.queueInputBuffer(index,0,0,0,MediaCodec.BUFFER_FLAG_END_OF_STREAM);inputEnd=true;}
                    else{
                        int flags=extractor.getSampleFlags();if((flags&(MediaExtractor.SAMPLE_FLAG_ENCRYPTED|MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME))!=0)throw new IOException("Protected or fragmented audio packets are unsupported.");
                        if(size>target.remaining()||size>262144)throw new IOException("This recording uses an unsupported audio packet size.");
                        long pts=extractor.getSampleTime();int count=extractor.readSampleData(target,0);if(count<0||count!=size)throw new IOException("This audio packet is incomplete.");
                        if(packetTiming.length()<512)packetTiming.append(pts).append('/').append(count).append(';');
                        codec.queueInputBuffer(index,0,count,pts,0);extractor.advance();
                    }progress=true;
                }}
                int index=codec.dequeueOutputBuffer(info,10000);
                if(index==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                    MediaFormat decoded=codec.getOutputFormat();int newRate=decoded.getInteger(MediaFormat.KEY_SAMPLE_RATE),newChannels=decoded.getInteger(MediaFormat.KEY_CHANNEL_COUNT);validate(newRate,newChannels);
                    if(decoded.getInteger(MediaFormat.KEY_PCM_ENCODING,AudioFormat.ENCODING_PCM_16BIT)!=AudioFormat.ENCODING_PCM_16BIT)throw new IOException("This phone's decoder did not provide PCM16 audio.");
                    if(samples.size>0&&(newRate!=rate||newChannels!=channels))throw new IOException("The audio format changed during this recording.");
                    rate=newRate;channels=newChannels;if(samples.size==0)samples=new Mono(rate);outputFormat=true;progress=true;
                }else if(index>=0){try{
                    if(info.size>0&&(info.flags&MediaCodec.BUFFER_FLAG_CODEC_CONFIG)==0){
                        if(!outputFormat)throw new IOException("The decoded audio format is unavailable.");
                        ByteBuffer buffer=codec.getOutputBuffer(index);int align=channels*2;
                        if(buffer==null||info.offset<0||info.size<0||info.offset>buffer.capacity()-info.size||info.size%align!=0)throw new IOException("The decoded audio is incomplete.");
                        if(first==Long.MIN_VALUE)first=info.presentationTimeUs;
                        if(outputTiming.length()<512)outputTiming.append(info.presentationTimeUs).append('/').append(info.size/align).append('/').append(info.flags).append(';');
                        long difference;try{long expected=Math.addExact(first,samples.size*1000000L/rate);difference=Math.subtractExact(info.presentationTimeUs,expected);}catch(ArithmeticException invalid){throw new IOException("This file has invalid audio timestamps.",invalid);}
                        if(info.presentationTimeUs<lastPts||difference< -2000||difference>2000){
                            // Bounded format/timing metadata only: never audio, source URI or a user filename.
                            android.util.Log.w("SleepWellDecode","timestamp-discontinuity codec="+decoder+" mime="+mime+" rate="+rate+" channels="+channels
                                    +" firstUs="+first+" previousUs="+lastPts+" actualUs="+info.presentationTimeUs+" accumulatedFrames="+samples.size
                                    +" deltaUs="+difference+" bufferFrames="+(info.size/align)+" flags="+info.flags+" inputPts/bytes="+packetTiming+" outputPts/frames/flags="+outputTiming);
                            throw new IOException("This recording has a timestamp gap. Choose a continuous excerpt.");
                        }
                        lastPts=info.presentationTimeUs;buffer.position(info.offset);buffer.limit(info.offset+info.size);buffer.order(ByteOrder.nativeOrder());int frames=info.size/align;samples.reserve(frames);
                        for(int n=0;n<frames;n++){if((n&4095)==0)check(active);int left=buffer.getShort();samples.add(channels==1?(short)left:(short)Math.round((left+buffer.getShort())/2d));}
                    }
                    outputEnd=(info.flags&MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0;progress=true;
                }finally{codec.releaseOutputBuffer(index,false);}}
                if(progress)lastProgress=SystemClock.elapsedRealtime();
            }
            check(active);if(samples.size==0||!inputEnd)throw new IOException("The complete recording could not be decoded.");
            return new Result(samples.finish(),rate,channels,first,mime,decoder);
        }catch(Exception|LinkageError failure){original=failure;throw failure;}
        finally{
            if(samples!=null)samples.erase();
            Exception cleanup=null;
            if(codec!=null){if(started)try{codec.stop();}catch(Exception error){cleanup=error;}try{codec.release();}catch(Exception error){if(cleanup==null)cleanup=error;else cleanup.addSuppressed(error);}}
            try{extractor.release();}catch(Exception error){if(cleanup==null)cleanup=error;else cleanup.addSuppressed(error);}source.close();
            if(cleanup!=null){if(original!=null)original.addSuppressed(cleanup);else throw new IOException("Audio decoding could not close cleanly.",cleanup);}
        }
    }
    private static void validate(int rate,int channels)throws IOException{if(rate<8000||rate>96000||channels<1||channels>2)throw new IOException("Choose mono or stereo audio between 8 and 96 kHz.");}
    private static String tag(byte[] bytes,int at){return new String(bytes,at,4,StandardCharsets.US_ASCII);}
    private static int u16(byte[] b,int p){return (b[p]&255)|((b[p+1]&255)<<8);}
    private static long u32(byte[] b,int p){return Integer.toUnsignedLong(u16(b,p)|(u16(b,p+2)<<16));}
    private static Result wave(byte[] b,BooleanSupplier active)throws IOException{
        if(b.length<44||u32(b,4)+8!=b.length)throw new IOException("Choose a complete RIFF/WAVE recording.");
        int rate=0,channels=0,data=-1,count=0,chunks=0;boolean format=false;
        for(int at=12;at<b.length;){check(active);if(++chunks>128||at>b.length-8)throw new IOException("Invalid WAV chunks.");long amount=u32(b,at+4),end=at+8L+amount,next=end+(amount&1);if(next>b.length)throw new IOException("The WAV recording is truncated.");String type=tag(b,at);int begin=at+8;
            if(type.equals("fmt ")){if(format||amount!=16&&amount!=18)throw new IOException("Unsupported WAV format header.");format=true;channels=u16(b,begin+2);long hz=u32(b,begin+4);if(hz>96000)throw new IOException("Unsupported WAV sample rate.");rate=(int)hz;validate(rate,channels);if(u16(b,begin)!=1||u16(b,begin+12)!=channels*2||u16(b,begin+14)!=16||u32(b,begin+8)!=rate*channels*2L||amount==18&&u16(b,begin+16)!=0)throw new IOException("Choose a mono or stereo PCM16 WAV recording.");}
            if(type.equals("data")){if(data>=0||amount==0)throw new IOException("Choose one complete WAV audio chunk.");data=begin;count=(int)amount;}at=(int)next;
        }
        if(!format||data<0||count%(channels*2)!=0)throw new IOException("This WAV has no complete PCM16 audio.");int frames=count/(channels*2);if(frames>rate*MAX_SECONDS)throw new IOException("Choose a complete recording no longer than two minutes.");short[] pcm=new short[frames];
        try{for(int i=0,at=data;i<frames;i++,at+=channels*2){if((i&4095)==0)check(active);int left=(short)u16(b,at);pcm[i]=channels==1?(short)left:(short)Math.round((left+(short)u16(b,at+2))/2d);}
            return new Result(pcm,rate,channels,0,"audio/raw","PCM16 WAV");
        }catch(IOException|RuntimeException failure){Arrays.fill(pcm,(short)0);throw failure;}
    }
    private static final class Mono {
        short[] values;int size;final int max;
        Mono(int rate){max=rate*MAX_SECONDS;values=new short[Math.min(max,rate*2)];}
        void reserve(int count)throws IOException{if(count<0||size>max-count)throw new IOException("Decoded audio exceeds two minutes. No excerpt was silently selected.");int needed=size+count;if(needed>values.length)values=Arrays.copyOf(values,Math.min(max,Math.max(needed,values.length*2)));}
        void add(short value){values[size++]=value;}
        short[] finish(){short[] result=Arrays.copyOf(values,size);erase();return result;}
        void erase(){Arrays.fill(values,(short)0);}
    }
    private static final class MemorySource extends MediaDataSource {
        private final byte[] bytes;private final BooleanSupplier active;private volatile boolean closed;
        MemorySource(byte[] bytes,BooleanSupplier active){this.bytes=bytes;this.active=active;}
        @Override public int readAt(long position,byte[] buffer,int offset,int size)throws IOException{check(active);if(closed||position<0||offset<0||size<0||offset>buffer.length-size)throw new IOException("Audio source is unavailable.");if(size==0)return 0;if(position>=bytes.length)return -1;int amount=(int)Math.min(size,bytes.length-position);System.arraycopy(bytes,(int)position,buffer,offset,amount);return amount;}
        @Override public long getSize()throws IOException{check(active);if(closed)throw new IOException("Audio source is closed.");return bytes.length;}
        @Override public void close(){closed=true;}
    }
    private RecordedAudioDecoder(){}
}
