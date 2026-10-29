package org.sleepwell.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.os.SystemClock;
import androidx.activity.ComponentActivity;

/** Preview-only, read-only readiness; constructors never request or start a camera. */
final class PracticeCameraAccess {
 static final int REQUEST=849;
 enum Permission {READY,NEEDED,SETTINGS,UNSUPPORTED}
 interface Ports {
  Permission permission(String type);void request(String type);boolean busy();long now();
  String unavailable(ChallengeRoutine.Task task);
  QrCamera.Port code(String format);PictureCamera.Port picture();SmileCamera.Port smile();
  PoseCamera.Port pose();ObjectCamera.Port object();CameraLightCamera.Port light();
 }
 interface Factory {Ports create(Activity owner);}
 static Factory factory=Native::new;
 private static final class Native implements Ports {
  private final java.lang.ref.WeakReference<Activity> owner;
  private final android.content.Context app;
  Native(Activity activity){owner=new java.lang.ref.WeakReference<>(activity);app=activity.getApplicationContext();}
  private Activity activity(){Activity value=owner.get();if(value==null||value.isDestroyed())throw new IllegalStateException("Camera preview owner closed");return value;}
  public long now(){return SystemClock.elapsedRealtime();}
  public boolean busy(){try{return AlarmService.active||TrackingService.running||Prefs.flag(app,"ringing");}catch(RuntimeException unreadable){return true;}}
  public Permission permission(String type){
   Activity a=activity();String feature=switch(type){
    case "Smile","Pose"->PackageManager.FEATURE_CAMERA_FRONT;
    case "Object recognition"->PackageManager.FEATURE_CAMERA;
    case "QR code","Picture match","Camera light"->PackageManager.FEATURE_CAMERA_ANY;
    default->throw new IllegalArgumentException("Unknown camera task");
   };
   if(!a.getPackageManager().hasSystemFeature(feature))return Permission.UNSUPPORTED;
   if(a.checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)return Permission.READY;
   boolean asked=a instanceof RoutineCameraPracticeActivity practice&&practice.cameraAsked();
   return !asked||a.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)?Permission.NEEDED:Permission.SETTINGS;
  }
  public void request(String type){
   Activity a=activity();Permission permission=permission(type);
   if(permission==Permission.UNSUPPORTED)throw new IllegalStateException("Required camera unavailable");
   if(permission==Permission.READY)return;
   if(permission==Permission.SETTINGS)a.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+a.getPackageName())));
   else a.requestPermissions(new String[]{Manifest.permission.CAMERA},REQUEST);
  }
  private ComponentActivity component(){Activity a=activity();if(!(a instanceof ComponentActivity component))throw new IllegalStateException("Explicit private camera owner required");return component;}
  public String unavailable(ChallengeRoutine.Task task){
   try{
    if(busy())return "Finish the active alarm or sleep tracking before using this preview.";
    if(!CameraCaptureLease.idle())return "Another camera owner is active or still releasing.";
    if(task.type.equals("QR code")&&task.code==null)return "Enroll a morning code before previewing this task.";
    if(task.type.equals("Picture match")&&task.picture==null)return "Capture a picture reference before previewing this task.";
    if(task.type.equals("Object recognition")&&task.objectTarget==null)return "Choose an object target before previewing this task.";
    Permission permission=permission(task.type);
    if(permission==Permission.UNSUPPORTED)return "This device does not have the required camera. Use the configured alternative.";
    if(permission!=Permission.READY)return "Camera access is off. Allow access, then tap Start.";
    return "";
   }catch(RuntimeException unreadable){return "Camera readiness could not be checked. Retry or use the configured alternative.";}
  }
  public QrCamera.Port code(String format){return QrCamera.factory.create(component(),format);}
  public PictureCamera.Port picture(){return PictureCamera.factory.create(component());}
  public SmileCamera.Port smile(){return SmileCamera.factory.create(component());}
  public PoseCamera.Port pose(){return PoseCamera.factory.create(component());}
  public ObjectCamera.Port object(){return ObjectCamera.factory.create(component());}
  public CameraLightCamera.Port light(){return CameraLightCamera.factory.create(component());}
 }
 private PracticeCameraAccess(){}
}
