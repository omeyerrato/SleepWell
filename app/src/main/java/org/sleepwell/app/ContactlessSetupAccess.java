package org.sleepwell.app;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.media.*;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import java.util.*;

/** Read-only device observation and explicit Android access screens; never creates audio or changes routing. */
interface ContactlessSetupAccess {
    default boolean rationale(Activity owner){return owner.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO);}
    default void request(Activity owner){owner.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},701);}
    default void settings(Activity owner,boolean microphone){owner.startActivity(microphone?new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+owner.getPackageName())):new Intent(Settings.ACTION_SOUND_SETTINGS));}
    interface Watch {void close();}
    default Watch watch(Context context,Handler main,Runnable changed){
        AudioManager audio=context.getSystemService(AudioManager.class);if(audio==null)throw new IllegalStateException("Audio devices unavailable");
        // The first added callback contains the already-present devices. It is not a route change.
        Set<Integer> initial=new HashSet<>();for(AudioDeviceInfo device:audio.getDevices(AudioManager.GET_DEVICES_INPUTS|AudioManager.GET_DEVICES_OUTPUTS))initial.add(device.getId());
        boolean[] first={true},closed={false};
        AudioDeviceCallback callback=new AudioDeviceCallback(){
            @Override public void onAudioDevicesAdded(AudioDeviceInfo[] devices){boolean baseline=first[0];first[0]=false;for(AudioDeviceInfo device:devices)if(!initial.contains(device.getId()))baseline=false;if(!closed[0]&&!baseline)changed.run();}
            @Override public void onAudioDevicesRemoved(AudioDeviceInfo[] devices){first[0]=false;if(!closed[0])changed.run();}
        };
        try{audio.registerAudioDeviceCallback(callback,main);}catch(RuntimeException unavailable){closed[0]=true;try{audio.unregisterAudioDeviceCallback(callback);}catch(RuntimeException cleanup){unavailable.addSuppressed(cleanup);}throw unavailable;}
        return ()->{if(!closed[0]){closed[0]=true;audio.unregisterAudioDeviceCallback(callback);}};
    }
    ContactlessSetupAccess PLATFORM=new ContactlessSetupAccess(){};
}
