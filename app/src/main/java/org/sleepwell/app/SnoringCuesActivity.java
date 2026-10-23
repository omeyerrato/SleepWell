package org.sleepwell.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.*;

/** Board 07 opt-in controls. Saving never emits a preview or starts the microphone. */
public final class SnoringCuesActivity extends Activity {
    private boolean enabled,analysis;private int mode,intensity,repeats,sensitivity;private String error="";
    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);enabled=saved==null?Prefs.flag(this,"snoreCues"):saved.getBoolean("enabled");analysis=saved==null?Prefs.flag(this,"noiseEnabled"):saved.getBoolean("analysis");mode=saved==null?Prefs.number(this,"snoreCueMode",1):saved.getInt("mode");intensity=saved==null?Prefs.number(this,"snoreCueIntensity",1):saved.getInt("intensity");repeats=saved==null?Prefs.number(this,"snoreCueRepeats",1):saved.getInt("repeats");sensitivity=saved==null?Prefs.number(this,"snoreCueSensitivity",1):saved.getInt("sensitivity");mode=Math.max(0,Math.min(2,mode));intensity=Math.max(1,Math.min(3,intensity));repeats=Math.max(1,Math.min(3,repeats));sensitivity=Math.max(0,Math.min(2,sensitivity));render();}
    @Override public void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putBoolean("enabled",enabled);out.putBoolean("analysis",analysis);out.putInt("mode",mode);out.putInt("intensity",intensity);out.putInt("repeats",repeats);out.putInt("sensitivity",sensitivity);}
    private void render(){LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,"Snoring cues",this::finish);
        Design.toggle(body,"Respond to snoring estimates","A short cue while tracking may help you change position.",enabled,on->enabled=on);Design.space(body,12);
        section(body,"Alert type");RecordUi.segments(body,new String[]{"Sound","Vibration","Both"},mode,n->{mode=n;render();}).setContentDescription("Alert type options");
        section(body,"Intensity");RecordUi.segments(body,new String[]{"Low","Medium","High"},intensity-1,n->{intensity=n+1;render();}).setContentDescription("Intensity options");
        section(body,"Repeats");RecordUi.segments(body,new String[]{"1","2","3"},repeats-1,n->{repeats=n+1;render();}).setContentDescription("Repeats options");
        section(body,"Detection sensitivity");RecordUi.segments(body,new String[]{"Low","Medium","High"},sensitivity,n->{sensitivity=n;render();}).setContentDescription("Detection sensitivity options");Design.space(body,16);
        Design.toggle(body,"Microphone analysis","Applies to your next recording.",analysis,on->{if(on&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){analysis=false;requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},104);}else analysis=on;});
        Design.space(body,14);Design.text(body,"Sound uses media volume. Cues pause for 2 minutes between estimates.",12,400,Design.MUTED);
        if(!error.isEmpty()){Design.space(body,10);Design.text(body,error,13,500,Design.BROWN);}String failure=Prefs.text(this,"snoreCueError","");if(!failure.isEmpty()){Design.space(body,8);Design.text(body,failure,13,500,Design.BROWN);}
        Design.fixedAction(body,"Save settings",this::save);
    }
    private void section(LinearLayout body,String title){Design.space(body,16);Design.text(body,title,17,600,Design.INK);Design.space(body,8);}
    private void save(){if(enabled&&(!analysis||checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)){error="Enable microphone analysis to use cues.";render();return;}Prefs.of(this).edit().putBoolean("snoreCues",enabled).putBoolean("noiseEnabled",analysis).putInt("snoreCueMode",mode).putInt("snoreCueIntensity",intensity).putInt("snoreCueRepeats",repeats).putInt("snoreCueSensitivity",sensitivity).apply();finish();}
    @Override public void onRequestPermissionsResult(int request,String[] names,int[] grants){super.onRequestPermissionsResult(request,names,grants);if(request==104){analysis=grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED;if(!analysis)error="Microphone access is off. You can enable it in Android settings.";render();}}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
