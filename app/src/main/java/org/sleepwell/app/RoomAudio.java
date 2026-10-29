package org.sleepwell.app;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.media.*;
import java.util.function.BooleanSupplier;

/** Foreground-only, nonblocking PCM adapter. Samples never leave the worker's short scratch buffer. */
final class RoomAudio implements RoomSoundLevel.Capture {
    private final Context app;private final BooleanSupplier current;
    private AudioRecord input;private MicrophoneLease lease;
    private RoomAudio(Context app,BooleanSupplier current){this.app=app;this.current=current;}
    static RoomSoundLevel.Capture open(Context app,BooleanSupplier current)throws Exception{
        RoomAudio capture=new RoomAudio(app,current);
        try{capture.allocate();return capture;}catch(Exception|LinkageError failure){try{capture.close();}catch(Exception|LinkageError cleanup){failure.addSuppressed(cleanup);throw new RoomSoundLevel.OpenFailure(capture,failure);}if(failure instanceof Exception exception)throw exception;throw (LinkageError)failure;}
    }
    private void check(){
        if(!current.getAsBoolean()||app.checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)throw new SecurityException("Microphone admission lost");
        AudioManager audio=app.getSystemService(AudioManager.class);
        if(audio==null||audio.getMode()!=AudioManager.MODE_NORMAL||TrackingService.running||AlarmService.active||Prefs.flag(app,"ringing"))throw new IllegalStateException("Microphone occupied");
        if(lease!=null&&!lease.current())throw new IllegalStateException("Microphone ownership lost");
    }
    private void allocate(){
        check();lease=MicrophoneLease.acquire("room-level");if(lease==null)throw new IllegalStateException("Microphone occupied");check();
        int minimum=AudioRecord.getMinBufferSize(16000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
        if(minimum<=0||minimum>64000)throw new IllegalStateException("Microphone format unavailable");
        // Recheck at allocation as access may change after the admission/lease checks.
        if(app.checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)throw new SecurityException("Microphone admission lost");
        input=new AudioRecord.Builder().setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION).setAudioFormat(new AudioFormat.Builder().setSampleRate(16000).setChannelMask(AudioFormat.CHANNEL_IN_MONO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build()).setBufferSizeInBytes(Math.max(4096,minimum)).setPrivacySensitive(true).build();
        if(input.getState()!=AudioRecord.STATE_INITIALIZED||input.getSampleRate()!=16000)throw new IllegalStateException("Microphone unavailable");
    }
    public void start(){check();input.startRecording();if(input.getRecordingState()!=AudioRecord.RECORDSTATE_RECORDING)throw new IllegalStateException("Microphone did not start");}
    public int read(short[] values){
        check();if(input.getRecordingState()!=AudioRecord.RECORDSTATE_RECORDING)throw new IllegalStateException("Microphone interrupted");
        AudioRecordingConfiguration configuration=input.getActiveRecordingConfiguration();if(configuration!=null&&configuration.isClientSilenced())throw new IllegalStateException("Microphone silenced");
        return input.read(values,0,values.length,AudioRecord.READ_NON_BLOCKING);
    }
    public void close(){
        if(input!=null){try{if(input.getRecordingState()==AudioRecord.RECORDSTATE_RECORDING)input.stop();}catch(RuntimeException ignored){/* Release still must succeed before ownership is released. */}input.release();input=null;}
        if(lease!=null){lease.released();lease=null;}
    }
}
