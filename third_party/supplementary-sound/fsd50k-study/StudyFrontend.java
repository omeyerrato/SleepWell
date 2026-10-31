package org.sleepwell.app;

import java.nio.*;
import java.nio.file.*;

/** Host-only oracle over unchanged shipping DSP. Never captures or plays audio. */
final class StudyFrontend {
    static float[] floats(Path path)throws Exception {
        ByteBuffer bytes=ByteBuffer.wrap(Files.readAllBytes(path)).order(ByteOrder.LITTLE_ENDIAN);
        float[] result=new float[bytes.remaining()/4];bytes.asFloatBuffer().get(result);return result;
    }
    static void write(Path path,float[] values)throws Exception {
        ByteBuffer bytes=ByteBuffer.allocate(values.length*4).order(ByteOrder.LITTLE_ENDIAN);
        bytes.asFloatBuffer().put(values);Files.write(path,bytes.array());
    }
    public static void main(String[] args)throws Exception {
        Path asset=Path.of(args[0]),input=Path.of(args[1]),output=Path.of(args[2]);
        ByteBuffer bytes=ByteBuffer.wrap(Files.readAllBytes(input)).order(ByteOrder.LITTLE_ENDIAN);
        short[] source=new short[bytes.remaining()/2];bytes.asShortBuffer().get(source);
        RecordedPcmResampler.Result converted=RecordedPcmResampler.resample(source,44100,()->true);
        ByteBuffer pcm=ByteBuffer.allocate(converted.pcm.length*2).order(ByteOrder.LITTLE_ENDIAN);
        pcm.asShortBuffer().put(converted.pcm);Files.write(output.resolve("input16.pcm"),pcm.array());
        Files.writeString(output.resolve("origin.txt"),converted.firstOutputFrame+"\n"+converted.pcm.length+"\n");
        RecordedSoundFrontend frontend=new RecordedSoundFrontend(floats(asset.resolve("resample.f32")),
                floats(asset.resolve("pann-window.f32")),floats(asset.resolve("efficient-window.f32")),
                floats(asset.resolve("pann-mel.f32")),floats(asset.resolve("efficient-mel.f32")));
        for(int start=0;start<converted.pcm.length;start+=160000) {
            int count=Math.min(160000,converted.pcm.length-start);if(count<32000)break;
            float[] samples=frontend.resample(converted.pcm,start,count,()->false);
            write(output.resolve(start+".pann.f32"),frontend.pann(samples,()->false));
            write(output.resolve(start+".efficient.f32"),frontend.efficient(samples,()->false));
        }
    }
}
