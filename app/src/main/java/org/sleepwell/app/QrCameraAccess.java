package org.sleepwell.app;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

/** Enrollment-only permission action. The ringing scan route never invokes request(). */
final class QrCameraAccess {
    static final int REQUEST=341;
    enum Status {READY,NEEDED,SETTINGS,UNSUPPORTED}
    interface Port {Status status(Activity activity);void request(Activity activity);}
    static Port port=new Port(){
        public Status status(Activity a){if(!a.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))return Status.UNSUPPORTED;if(a.checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)return Status.READY;return !Prefs.flag(a,"qrCameraAsked")||a.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)?Status.NEEDED:Status.SETTINGS;}
        public void request(Activity a){Status state=status(a);if(state==Status.READY)return;if(state==Status.UNSUPPORTED)throw new IllegalStateException("No supported camera is available.");if(state==Status.SETTINGS){a.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+a.getPackageName())));return;}boolean asked=Prefs.flag(a,"qrCameraAsked");Prefs.put(a,"qrCameraAsked",true);try{a.requestPermissions(new String[]{Manifest.permission.CAMERA},REQUEST);}catch(RuntimeException unavailable){Prefs.put(a,"qrCameraAsked",asked);throw unavailable;}}
    };
    private QrCameraAccess(){}
}
