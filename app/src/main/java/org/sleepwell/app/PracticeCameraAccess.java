package org.sleepwell.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import androidx.activity.ComponentActivity;

/** Preview-only, read-only readiness; constructors never request or start a camera. */
final class PracticeCameraAccess {
 enum Permission {READY,NEEDED,SETTINGS,UNSUPPORTED}
 interface Ports {
  Permission permission(String type);void request(String type);boolean busy();long now();
  String unavailable(ChallengeRoutine.Task task);
  QrCamera.Port code(String format);PictureCamera.Port picture();SmileCamera.Port smile();
  PoseCamera.Port pose();ObjectCamera.Port object();CameraLightCamera.Port light();
 }
 interface Factory {Ports create(ComponentActivity owner);}
 static Factory factory=Native::new;
 private static final class Native implements Ports {
  private final java.lang.ref.WeakReference<ComponentActivity> owner;
  private final android.content.Context app;
  Native(ComponentActivity activity){owner=new java.lang.ref.WeakReference<>(activity);app=activity.getApplicationContext();}
  private ComponentActivity activity(){ComponentActivity value=owner.get();if(value==null||value.isDestroyed())throw new IllegalStateException("Camera preview owner closed");return value;}
  public long now(){return SystemClock.elapsedRealtime();}
  public boolean busy(){try{return AlarmService.active||TrackingService.running||Prefs.flag(app,"ringing");}catch(RuntimeException unreadable){return true;}}
  public Permission permission(String type){
   ComponentActivity a=activity();return switch(type){
    case "QR code"->Permission.valueOf(QrCameraAccess.port.status(a).name());
    case "Picture match"->Permission.valueOf(PictureCameraAccess.port.status(a).name());
    case "Smile"->Permission.valueOf(SmileCameraAccess.status(a).name());
    case "Pose"->Permission.valueOf(PoseCameraAccess.status(a).name());
    case "Object recognition"->Permission.valueOf(ObjectCameraAccess.status(a).name());
    case "Camera light"->Permission.valueOf(CameraLightCameraAccess.status(a).name());
    default->throw new IllegalArgumentException("Unknown camera task");
   };
  }
  public void request(String type){ComponentActivity a=activity();switch(type){
   case "QR code"->QrCameraAccess.port.request(a);case "Picture match"->PictureCameraAccess.port.request(a);
   case "Smile"->SmileCameraAccess.request(a);case "Pose"->PoseCameraAccess.request(a);
   case "Object recognition"->ObjectCameraAccess.request(a);case "Camera light"->CameraLightCameraAccess.request(a);
   default->throw new IllegalArgumentException("Unknown camera task");
  }}
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
  public QrCamera.Port code(String format){return QrCamera.factory.create(activity(),format);}
  public PictureCamera.Port picture(){return PictureCamera.factory.create(activity());}
  public SmileCamera.Port smile(){return SmileCamera.factory.create(activity());}
  public PoseCamera.Port pose(){return PoseCamera.factory.create(activity());}
  public ObjectCamera.Port object(){return ObjectCamera.factory.create(activity());}
  public CameraLightCamera.Port light(){return CameraLightCamera.factory.create(activity());}
 }
 private PracticeCameraAccess(){}
}
