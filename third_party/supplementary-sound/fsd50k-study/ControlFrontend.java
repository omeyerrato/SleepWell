package org.sleepwell.app;

import java.nio.*;
import java.nio.file.*;

/** Retained PCM16/16k control adapter; calls unchanged shipping frontend, no capture or playback. */
final class ControlFrontend {
    public static void main(String[] args) throws Exception {
        Path asset=Path.of(args[0]),input=Path.of(args[1]),output=Path.of(args[2]);
        ByteBuffer bytes=ByteBuffer.wrap(Files.readAllBytes(input)).order(ByteOrder.LITTLE_ENDIAN);
        short[] pcm=new short[bytes.remaining()/2];bytes.asShortBuffer().get(pcm);
        RecordedSoundFrontend frontend=new RecordedSoundFrontend(StudyFrontend.floats(asset.resolve("resample.f32")),
                StudyFrontend.floats(asset.resolve("pann-window.f32")),StudyFrontend.floats(asset.resolve("efficient-window.f32")),
                StudyFrontend.floats(asset.resolve("pann-mel.f32")),StudyFrontend.floats(asset.resolve("efficient-mel.f32")));
        for(int start=0;start<pcm.length;start+=160000) {
            int count=Math.min(160000,pcm.length-start);if(count<32000)break;
            float[] samples=frontend.resample(pcm,start,count,()->false);
            StudyFrontend.write(output.resolve(start+".pann.f32"),frontend.pann(samples,()->false));
            StudyFrontend.write(output.resolve(start+".efficient.f32"),frontend.efficient(samples,()->false));
        }
    }
}
