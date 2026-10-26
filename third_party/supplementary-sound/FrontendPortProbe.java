package org.sleepwell.app;
import java.nio.*;import java.nio.file.*;import java.io.*;
final class FrontendPortProbe {
 static float[] floats(Path p)throws Exception{ByteBuffer b=ByteBuffer.wrap(Files.readAllBytes(p)).order(ByteOrder.LITTLE_ENDIAN);float[] f=new float[b.remaining()/4];b.asFloatBuffer().get(f);return f;}
 static void write(Path p,float[] f)throws Exception{ByteBuffer b=ByteBuffer.allocate(f.length*4).order(ByteOrder.LITTLE_ENDIAN);b.asFloatBuffer().put(f);Files.write(p,b.array());}
 public static void main(String[] args)throws Exception{
  Path root=Path.of(args[0]),asset=root.resolve("app/src/main/assets/recorded-sound");
  RecordedSoundFrontend front=new RecordedSoundFrontend(floats(asset.resolve("resample.f32")),floats(asset.resolve("pann-window.f32")),floats(asset.resolve("efficient-window.f32")),floats(asset.resolve("pann-mel.f32")),floats(asset.resolve("efficient-mel.f32")));
  for(String name:new String[]{"laugh-frenkfurth","guitar-qubodup"}){
   ByteBuffer b=ByteBuffer.wrap(Files.readAllBytes(root.resolve(".android-local/"+name+".input.pcm"))).order(ByteOrder.LITTLE_ENDIAN);short[] pcm=new short[b.remaining()/2];b.asShortBuffer().get(pcm);
   float[] x=front.resample(pcm,0,pcm.length,()->false);write(root.resolve(".android-local/"+name+".java-upsampled.f32"),x);
   write(root.resolve(".android-local/"+name+".java-pann.f32"),front.pann(x,()->false));write(root.resolve(".android-local/"+name+".java-efficient.f32"),front.efficient(x,()->false));
  }
 }
}
