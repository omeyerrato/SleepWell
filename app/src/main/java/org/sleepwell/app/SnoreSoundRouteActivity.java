package org.sleepwell.app;

import android.app.*;
import android.content.Context;
import android.media.*;
import android.os.*;
import android.widget.LinearLayout;
import java.util.*;

/** Supporting sound-only constraint. Listing devices never starts audio or verifies a route. */
public final class SnoreSoundRouteActivity extends Activity {
    interface Source extends AutoCloseable {List<HeadphoneCue.Endpoint> outputs();void watch(Runnable changed);void close();}
    interface Factory {Source create(Context context);}
    static Factory factory=context->new Source(){final AudioManager manager=context.getSystemService(AudioManager.class);AudioDeviceCallback callback;Runnable target;public List<HeadphoneCue.Endpoint> outputs(){return HeadphoneSound.outputs(context);}public void watch(Runnable changed){if(manager==null)return;target=changed;callback=new AudioDeviceCallback(){public void onAudioDevicesAdded(AudioDeviceInfo[] devices){if(target!=null)target.run();}public void onAudioDevicesRemoved(AudioDeviceInfo[] devices){if(target!=null)target.run();}};manager.registerAudioDeviceCallback(callback,new Handler(Looper.getMainLooper()));}public void close(){target=null;AudioDeviceCallback previous=callback;callback=null;if(manager!=null&&previous!=null)manager.unregisterAudioDeviceCallback(previous);}};
    private Factory provider;private Source source;private boolean headphone,visible;private Dialog about;
    @Override public void onCreate(Bundle state){super.onCreate(state);RecordUi.review(this);provider=factory;headphone=state==null?Prefs.flag(this,"snoreHeadphoneSound"):state.getBoolean("headphone");render();}
    @Override protected void onStart(){super.onStart();visible=true;try{source=provider.create(getApplicationContext());source.watch(()->{if(visible&&!isDestroyed())render();});}catch(RuntimeException ignored){if(source!=null)try{source.close();}catch(RuntimeException cleanup){}source=null;}render();}
    @Override public void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);state.putBoolean("headphone",headphone);}
    private String availability(){try{List<HeadphoneCue.Endpoint> outputs=source==null?null:source.outputs();if(outputs==null)return "Headphone availability could not be checked.";if(HeadphoneCue.select(outputs)!=null)return "Supported headphones available. Actual routing is checked at each cue.";for(HeadphoneCue.Endpoint device:outputs)if(device.kind==HeadphoneCue.Kind.BLUETOOTH_MEDIA)return "Bluetooth media routes cannot be verified as headphones. Use wired, USB or BLE headphones.";return "No supported headphones connected. Headphone-only sound will be skipped.";}catch(RuntimeException unavailable){return "Headphone availability could not be checked.";}}
    private void render(){LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.headerAction(RecordUi.header(body,"Sound output",this::finish),"info","About headphone routing",()->about=SleepPicker.message(this,"Headphone routing","Only wired headphones, USB headsets and BLE headsets are supported. Ordinary Bluetooth A2DP media, call routes, generic USB audio and speakers are skipped because their type does not prove headphones. Android routing is checked before each short audio chunk; a route change can still cause a brief speaker transient before the system callback arrives. Vibration is unaffected. No Bluetooth permission or pairing change is required."));Design.space(body,18);
        Design.toggle(body,"Headphone sound only","Skip sound without a verified route.",headphone,on->{headphone=on;render();});Design.space(body,20);
        LinearLayout status=Design.col(this);Design.pad(status,18,18);status.setBackground(Design.shape(this,Design.SOFT,18));Design.text(status,"Headphone availability",16,600,Design.INK);Design.space(status,8);Design.text(status,availability(),14,400,Design.MUTED);body.addView(status);Design.space(body,18);
        Design.item(body,"vibrate","Vibration", "Unchanged",null);Design.text(body,"Vibration still follows the selected alert type, even when headphone sound is skipped.",14,400,Design.MUTED);Design.space(body,18);if(!headphone)Design.text(body,"Sound uses the current media route. This may be the phone speaker.",14,400,Design.MUTED);String last=Prefs.text(this,"snoreCueRouteError","");if(!last.isEmpty()){Design.space(body,12);Design.text(body,"Last sound cue: "+last,13,500,Design.BROWN);}Design.fixedAction(body,"Save sound output",()->{Prefs.of(this).edit().putBoolean("snoreHeadphoneSound",headphone).apply();finish();});}
    @Override protected void onStop(){visible=false;if(source!=null){try{source.close();}catch(RuntimeException ignored){}source=null;}super.onStop();}
    @Override protected void onDestroy(){if(about!=null)about.dismiss();super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
