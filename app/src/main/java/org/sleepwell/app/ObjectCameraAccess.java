package org.sleepwell.app;
import android.app.Activity;import android.content.*;import android.content.pm.PackageManager;import android.net.Uri;import android.provider.Settings;
final class ObjectCameraAccess {
 static final int REQUEST=817;enum Status{READY,NEEDED,SETTINGS,UNSUPPORTED}
 static Status status(Activity a){if(!a.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))return Status.UNSUPPORTED;if(a.checkSelfPermission(android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)return Status.READY;return !Prefs.flag(a,"objectCameraAsked")||a.shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)?Status.NEEDED:Status.SETTINGS;}
 static void request(Activity a){Status state=status(a);if(state==Status.READY)return;if(state==Status.UNSUPPORTED)throw new IllegalStateException("A rear camera is required");if(state==Status.SETTINGS){a.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+a.getPackageName())));return;}Prefs.put(a,"objectCameraAsked",true);a.requestPermissions(new String[]{android.Manifest.permission.CAMERA},REQUEST);}
 private ObjectCameraAccess(){}
}
