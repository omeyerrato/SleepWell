package org.sleepwell.app;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.hardware.*;
import android.net.Uri;

/** Configuration-only consent. The ringing activity never calls request(). */
final class WalkPermission {
    static final int REQUEST=420;
    enum Status { READY, NEEDED, SETTINGS, UNSUPPORTED }
    interface Port {Status status(Activity activity);void request(Activity activity);}
    static Port port=new Port(){
        public Status status(Activity a){SensorManager manager=a.getSystemService(SensorManager.class);if(manager==null||manager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)==null&&manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)==null)return Status.UNSUPPORTED;if(a.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)==PackageManager.PERMISSION_GRANTED)return Status.READY;return Prefs.flag(a,"walkPermissionAsked")&&!a.shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION)?Status.SETTINGS:Status.NEEDED;}
        public void request(Activity a){if(status(a)==Status.SETTINGS){a.startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+a.getPackageName())));return;}if(status(a)==Status.NEEDED){Prefs.put(a,"walkPermissionAsked",true);a.requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION},REQUEST);}}
    };
}
