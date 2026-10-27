package org.sleepwell.app;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.view.WindowManager;

/** Saved opt-in for this app's visible room / Wind-down window only. Never changes Android settings. */
final class RoomDimming {
    static final String ENABLED="roomBedtimeDimming";
    private final Activity owner;
    private Float original;

    RoomDimming(Activity owner){this.owner=owner;}
    static boolean enabled(Context context){try{return Prefs.of(context).getBoolean(ENABLED,false);}catch(RuntimeException unavailable){return false;}}
    static boolean save(Context context,boolean enabled){try{return Prefs.of(context).edit().putBoolean(ENABLED,enabled).commit();}catch(RuntimeException unavailable){return false;}}
    void apply(boolean active){
        if(Looper.myLooper()!=Looper.getMainLooper())return;
        if(!active||!enabled(owner)||owner.isFinishing()||owner.isDestroyed()||!owner.hasWindowFocus()){restore();return;}
        WindowManager.LayoutParams attributes=owner.getWindow().getAttributes();
        if(original==null)original=attributes.screenBrightness;
        // Respect an already-dimmer window. The system still controls the actual display output.
        attributes.screenBrightness=original>=0?Math.min(original,.05f):.05f;
        owner.getWindow().setAttributes(attributes);
    }
    void restore(){
        if(original==null||Looper.myLooper()!=Looper.getMainLooper())return;
        WindowManager.LayoutParams attributes=owner.getWindow().getAttributes();
        attributes.screenBrightness=original;owner.getWindow().setAttributes(attributes);original=null;
    }
}
