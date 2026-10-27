package org.sleepwell.app;

import android.content.Context;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Private memory copy. Retained WAV is exact; explicit imports decode/resample with provenance. No playback or audio storage. */
final class RecordedRecognitionInput {
    static final int MAX_BYTES=4*1024*1024, MAX_SAMPLES=1_920_000;
    final short[] pcm;final String hash,wavHash;final JSONObject source;final long durationMs;
    private RecordedRecognitionInput(short[] pcm,String wavHash,JSONObject source)throws Exception {
        this.pcm=pcm;this.wavHash=wavHash;this.source=source;durationMs=pcm.length*1000L/16000;
        MessageDigest digest=MessageDigest.getInstance("SHA-256");for(short sample:pcm){digest.update((byte)sample);digest.update((byte)(sample>>8));}hash=hex(digest.digest());
    }
    static byte[] copy(InputStream in,BooleanSupplier active)throws IOException {
        return copy(in,active,MAX_BYTES);
    }
    static byte[] copySelected(InputStream in,BooleanSupplier active)throws IOException {
        return copy(in,active,RecordedAudioDecoder.MAX_BYTES);
    }
    private static byte[] copy(InputStream in,BooleanSupplier active,int maximum)throws IOException {
        ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[16384];int total=0;
        for(int n;(n=in.read(buffer))!=-1;){check(active);if(n==0)continue;total+=n;if(total>maximum)throw new IOException("Choose audio no larger than "+maximum/1024/1024+" MiB.");out.write(buffer,0,n);}check(active);return out.toByteArray();
    }
    static RecordedRecognitionInput selected(byte[] bytes,BooleanSupplier active)throws Exception {
        RecordedAudioDecoder.Result decoded=null;RecordedPcmResampler.Result resampled=null;
        try{
            decoded=RecordedAudioDecoder.decode(bytes,active);resampled=RecordedPcmResampler.resample(decoded.mono,decoded.rate,active);
            String kind=decoded.decoder.equals("PCM16 WAV")?"selected_wav":"selected_audio";
            JSONObject source=new JSONObject().put("kind",kind).put("provenance","Explicitly selected audio; original recording context unverified")
                    .put("inputMime",decoded.mime).put("inputRate",decoded.rate).put("inputChannels",decoded.channels).put("decodedFrames",decoded.mono.length)
                    .put("firstPresentationUs",decoded.firstPresentationUs).put("decoder",decoded.decoder).put("firstOutputFrame",resampled.firstOutputFrame)
                    .put("omittedStartMs",resampled.omittedStartMs).put("omittedEndMs",resampled.omittedEndMs)
                    .put("downmix",decoded.channels==1?"none":"arithmetic-mean").put("resampling",decoded.rate==16000?"none":"blackman193-v1");
            check(active);RecordedRecognitionInput result=new RecordedRecognitionInput(resampled.pcm,hex(MessageDigest.getInstance("SHA-256").digest(bytes)),source);
            check(active);resampled=null;return result;
        }finally{if(decoded!=null)Arrays.fill(decoded.mono,(short)0);if(resampled!=null)Arrays.fill(resampled.pcm,(short)0);}
    }
    private static int u16(byte[] bytes,int at){return (bytes[at]&255)|((bytes[at+1]&255)<<8);}
    private static long u32(byte[] bytes,int at){return Integer.toUnsignedLong(u16(bytes,at)|(u16(bytes,at+2)<<16));}
    private static String tag(byte[] b,int at){return new String(b,at,4,StandardCharsets.US_ASCII);}
    static RecordedRecognitionInput wav(byte[] bytes,JSONObject source,long fromMs,long lengthMs,BooleanSupplier active)throws Exception {
        check(active);if(bytes.length<44||bytes.length>MAX_BYTES||!tag(bytes,0).equals("RIFF")||!tag(bytes,8).equals("WAVE")||u32(bytes,4)+8!=bytes.length)throw new IOException("Choose a complete RIFF/WAVE PCM file.");
        boolean format=false;int data=-1,size=0,chunks=0;
        for(int at=12;at<bytes.length;){check(active);if(++chunks>128||at>bytes.length-8)throw new IOException("Invalid WAV chunk layout.");long amount=u32(bytes,at+4),end=at+8L+amount,next=end+(amount&1);if(next>bytes.length)throw new IOException("The WAV file is truncated.");String type=tag(bytes,at);int begin=at+8;
            if(type.equals("fmt ")){if(format||amount!=16&&amount!=18)throw new IOException("Unsupported WAV format header.");format=true;if(u16(bytes,begin)!=1||u16(bytes,begin+2)!=1||u32(bytes,begin+4)!=16000||u32(bytes,begin+8)!=32000||u16(bytes,begin+12)!=2||u16(bytes,begin+14)!=16||amount==18&&u16(bytes,begin+16)!=0)throw new IOException("Only mono 16 kHz PCM16 WAV is supported. No resampling is performed.");}
            if(type.equals("data")){if(data>=0||amount==0||(amount&1)!=0||amount/2>MAX_SAMPLES)throw new IOException("Choose one complete audio chunk of at most two minutes.");data=begin;size=(int)amount;}at=(int)next;
        }
        if(!format||data<0)throw new IOException("This WAV has no supported PCM audio.");int first=0,last=size/2;long whole=last*1000L/16000;
        if(fromMs!=0||lengthMs!=Long.MAX_VALUE){if(fromMs<0||lengthMs<=0||fromMs+lengthMs>whole)throw new IOException("The selected recording interval changed.");first=Math.toIntExact(fromMs*16);last=Math.toIntExact((fromMs+lengthMs)*16);}
        if(last<=first||last-first>MAX_SAMPLES)throw new IOException("This interval has no complete supported audio.");short[] pcm=new short[last-first];for(int n=0;n<pcm.length;n++){if((n&8191)==0)check(active);pcm[n]=(short)u16(bytes,data+(n+first)*2);}return new RecordedRecognitionInput(pcm,hex(MessageDigest.getInstance("SHA-256").digest(bytes)),source);
    }
    static void check(BooleanSupplier active)throws IOException{if(!active.getAsBoolean()||Thread.currentThread().isInterrupted())throw new InterruptedIOException("Analysis request cancelled or expired.");}
    static String hex(byte[] digest){StringBuilder out=new StringBuilder();for(byte b:digest)out.append(String.format(Locale.ROOT,"%02x",b&255));return out.toString();}
    static String fingerprint(RecordEntry r,JSONObject clip,JSONObject event)throws Exception {
        JSONArray coverage=new JSONArray();for(RecordIntervals.Span span:r.coverage())coverage.put(new JSONArray().put(span.start).put(span.end));
        JSONObject value=new JSONObject().put("record",r.id).put("rawStart",r.rawStart).put("rawEnd",r.rawEnd).put("updated",r.updated).put("source",r.source).put("origin",r.origin).put("sensor",r.sensor).put("coverage",coverage).put("clip",clip);
        if(event!=null)value.put("event",event);return hex(MessageDigest.getInstance("SHA-256").digest(value.toString().getBytes(StandardCharsets.UTF_8)));
    }
    static RecordedRecognitionInput retained(Context app,Records.Factory factory,long recordId,long clipId,long eventId,long expectedUpdated,JSONObject expected,BooleanSupplier active)throws Exception {
        return retained(app,factory,recordId,clipId,eventId,expectedUpdated,expected,active,null);
    }
    interface Validated {void accept(RecordedRecognitionInput input)throws Exception;}
    static RecordedRecognitionInput retained(Context app,Records.Factory factory,long recordId,long clipId,long eventId,long expectedUpdated,JSONObject expected,BooleanSupplier active,Validated validated)throws Exception {
        synchronized(SoundClips.LOCK){try(Store store=factory.open(app)){
            android.database.sqlite.SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try {
                check(active);RecordEntry night=Records.get(store,recordId);if(night.manual()||expectedUpdated>=0&&night.updated!=expectedUpdated)throw new IOException("The saved night changed. Reopen sound events.");
                JSONObject clip=SoundClips.get(store,recordId,clipId),event=eventId>0?SoundClips.event(store,recordId,eventId):null;if(SoundClips.pending(store,clipId))throw new IOException("This recording is pending deletion.");
                if(event!=null&&(event.isNull("clip")||event.getLong("clip")!=clipId))throw new IOException("The event no longer refers to this recording.");
                byte[] bytes;try(InputStream in=new FileInputStream(FileSafety.clip(app,clip.getString("path")))){bytes=copy(in,active);}
                // Read the actual header, not the optional historical duration column.
                RecordedRecognitionInput whole=wav(bytes,new JSONObject(),0,Long.MAX_VALUE,active);long wholeDuration=whole.durationMs;whole.erase();long end=clip.getLong("time"),start=Math.subtractExact(end,wholeDuration);
                long selectedStart=Math.max(start,night.edit.start),selectedEnd=Math.min(end,night.edit.end);if(event!=null){selectedStart=Math.max(selectedStart,event.getLong("time"));selectedEnd=Math.min(selectedEnd,Math.addExact(event.getLong("time"),event.getLong("duration_ms")));}
                List<RecordIntervals.Span> spans=RecordIntervals.intersect(night.coverage(),selectedStart,selectedEnd);
                if(spans.size()!=1||spans.get(0).start!=selectedStart||spans.get(0).end!=selectedEnd||selectedEnd<=selectedStart)throw new IOException("This recording crosses an excluded or missing period. Select a WAV excerpt instead; gaps are never joined.");
                String fingerprint=fingerprint(night,clip,event);JSONObject source=new JSONObject().put("kind","saved_clip").put("record",recordId).put("clip",clipId).put("event",eventId).put("updated",night.updated).put("fingerprint",fingerprint).put("fromMs",selectedStart-start).put("durationMs",selectedEnd-selectedStart).put("startWall",selectedStart).put("provenance",night.provenance());
                RecordedRecognitionInput input;try{input=wav(bytes,source,selectedStart-start,selectedEnd-selectedStart,active);}finally{Arrays.fill(bytes,(byte)0);}
                if(expected!=null&&(!fingerprint.equals(expected.getString("fingerprint"))||!input.hash.equals(expected.getString("pcmSha256"))||!input.wavHash.equals(expected.getString("wavSha256"))))throw new IOException("The saved recording or effective period changed. Prepare a new analysis.");
                if(validated!=null)validated.accept(input);db.setTransactionSuccessful();return input;
            }finally{db.endTransaction();}
        }}
    }
    JSONObject provenance()throws JSONException {JSONObject value=new JSONObject(source.toString()).put("pcmSha256",hash).put("sourceSha256",wavHash).put("sampleRate",16000).put("samples",pcm.length).put("durationMs",durationMs);return value.put(source.optString("kind").equals("selected_audio")?"fileSha256":"wavSha256",wavHash);}
    void erase(){Arrays.fill(pcm,(short)0);}
}
