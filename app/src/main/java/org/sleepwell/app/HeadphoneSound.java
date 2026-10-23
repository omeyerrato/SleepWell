package org.sleepwell.app;

import android.content.Context;
import android.media.*;
import android.os.*;
import java.util.*;

/** Public per-track routing only. No Bluetooth/SCO/stream-volume changes or permission grants. */
final class HeadphoneSound implements HeadphoneCue.Sink {
    private final AudioManager manager;private final Handler handler;private AudioTrack track;private AudioDeviceInfo preferred;private AudioRouting.OnRoutingChangedListener listener;private boolean closed;private long queuedFrames;
    HeadphoneSound(Context context,Handler handler){manager=context.getSystemService(AudioManager.class);this.handler=handler;}
    static HeadphoneCue.Kind kind(int type){if(type==AudioDeviceInfo.TYPE_WIRED_HEADSET||type==AudioDeviceInfo.TYPE_WIRED_HEADPHONES)return HeadphoneCue.Kind.WIRED;if(type==AudioDeviceInfo.TYPE_USB_HEADSET)return HeadphoneCue.Kind.USB_HEADSET;if(Build.VERSION.SDK_INT>=31&&type==AudioDeviceInfo.TYPE_BLE_HEADSET)return HeadphoneCue.Kind.BLE_HEADSET;if(type==AudioDeviceInfo.TYPE_BLUETOOTH_A2DP||type==AudioDeviceInfo.TYPE_BLUETOOTH_SCO)return HeadphoneCue.Kind.BLUETOOTH_MEDIA;return HeadphoneCue.Kind.OTHER;}
    static HeadphoneCue.Endpoint endpoint(AudioDeviceInfo device){return new HeadphoneCue.Endpoint(device.getId(),kind(device.getType()));}
    static List<HeadphoneCue.Endpoint> outputs(Context context){AudioManager manager=context.getSystemService(AudioManager.class);if(manager==null)return List.of();List<HeadphoneCue.Endpoint> result=new ArrayList<>();for(AudioDeviceInfo device:manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS))result.add(endpoint(device));return result;}
    @Override public List<HeadphoneCue.Endpoint> available(){if(manager==null||closed)return List.of();List<HeadphoneCue.Endpoint> result=new ArrayList<>();for(AudioDeviceInfo device:manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS))result.add(endpoint(device));return result;}
    @Override public boolean prefer(HeadphoneCue.Endpoint endpoint){if(manager==null||closed||!endpoint.supported())return false;for(AudioDeviceInfo device:manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS))if(device.getId()==endpoint.id&&kind(device.getType())==endpoint.kind){preferred=device;return true;}return false;}
    @Override public void startSilent(Runnable changed){if(closed||preferred==null)throw new IllegalStateException();int minimum=AudioTrack.getMinBufferSize(16000,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);if(minimum<=0||minimum>32000)throw new IllegalStateException("Unsupported cue buffer");
        track=new AudioTrack.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).setAudioFormat(new AudioFormat.Builder().setSampleRate(16000).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build()).setTransferMode(AudioTrack.MODE_STREAM).setBufferSizeInBytes(Math.max(minimum,6400)).build();
        if(track.getState()!=AudioTrack.STATE_INITIALIZED||track.setVolume(0)!=AudioTrack.SUCCESS||!track.setPreferredDevice(preferred))throw new IllegalStateException("Headphone route unavailable");
        listener=routing->changed.run();track.addOnRoutingChangedListener(listener,handler);short[] silence=new short[Math.max(minimum,6400)/2];int written=track.write(silence,0,silence.length,AudioTrack.WRITE_NON_BLOCKING);if(written<0)throw new IllegalStateException("Silent route setup failed");queuedFrames=written;track.play();
    }
    @Override public List<HeadphoneCue.Endpoint> routed(){if(closed||track==null||track.getPlayState()!=AudioTrack.PLAYSTATE_PLAYING)return List.of();List<HeadphoneCue.Endpoint> result=new ArrayList<>();if(Build.VERSION.SDK_INT>=36){for(AudioDeviceInfo device:track.getRoutedDevices())result.add(endpoint(device));}else{AudioDeviceInfo device=track.getRoutedDevice();if(device!=null)result.add(endpoint(device));}return result;}
    @Override public int chunk(HeadphoneCue.Endpoint expected,int intensity,int frame,int frames){if(closed||track==null||!HeadphoneCue.matches(routed(),expected))return -1;short[] pcm=new short[frames];double gain=new double[]{.10,.20,.35}[Math.max(0,Math.min(2,intensity-1))];for(int n=0;n<frames;n++){int sample=frame+n;double envelope=Math.min(1,Math.min(sample/80.0,(3999-sample)/80.0));pcm[n]=(short)(Math.sin(sample*2*Math.PI*800/16000)*32767*gain*Math.max(0,envelope));}
        if(track.setVolume(1)!=AudioTrack.SUCCESS||!HeadphoneCue.matches(routed(),expected))return -1;int count=track.write(pcm,0,frames,AudioTrack.WRITE_NON_BLOCKING);if(count>0)queuedFrames+=count;if(!HeadphoneCue.matches(routed(),expected)){silence();return -1;}return count;
    }
    @Override public boolean drained(){return track!=null&&Integer.toUnsignedLong(track.getPlaybackHeadPosition())>=queuedFrames;}
    @Override public void silence(){if(track==null)return;try{track.setVolume(0);}catch(RuntimeException ignored){}try{track.pause();}catch(RuntimeException ignored){}try{track.flush();}catch(RuntimeException ignored){}}
    @Override public void close(){if(closed)return;closed=true;silence();if(track!=null){if(listener!=null)try{track.removeOnRoutingChangedListener(listener);}catch(RuntimeException ignored){}try{track.release();}catch(RuntimeException ignored){}track=null;}}
}
