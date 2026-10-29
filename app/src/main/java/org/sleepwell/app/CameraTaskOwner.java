package org.sleepwell.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/** Private in-process continuation for an exact request, never an alarm admission proof.
 * Saved state contains only an opaque lookup token. A lost process cannot recreate earned work.
 */
final class CameraTaskOwner {
 static final class Pending {
  final int result;final long captured;final Intent data;
  Pending(int result,long captured,Intent data){this.result=result;this.captured=captured;this.data=data==null?null:new Intent(data);}
 }
 private static final Map<String,CameraTaskOwner> RETAINED=new HashMap<>();
 private final String token=UUID.randomUUID().toString(),identity;
 private WeakReference<Activity> attached;
 private Runnable close;
 private BooleanSupplier released;
 private Pending pending;private boolean closing;
 private CameraTaskOwner(String identity,Activity activity){this.identity=identity;attached=new WeakReference<>(activity);}
 static CameraTaskOwner attach(Activity activity,String identity,Bundle saved){
  RETAINED.values().removeIf(owner->owner.attached.get()==null&&owner.clean()&&owner.pending==null);
  String token=saved==null?null:saved.getString("cameraCleanupOwner");
  CameraTaskOwner owner=RETAINED.get(token);
  if(owner==null||!owner.identity.equals(identity))owner=new CameraTaskOwner(identity,activity);
  owner.attached=new WeakReference<>(activity);RETAINED.put(owner.token,owner);return owner;
 }
 void save(Activity activity,Bundle state){if(owned(activity))state.putString("cameraCleanupOwner",token);}
 boolean owned(Activity activity){return attached.get()==activity;}
 void bind(Activity activity,Runnable close,BooleanSupplier released){
  if(!owned(activity)||!clean()||pending!=null)throw new IllegalStateException("Previous camera release or result is still pending");
  this.close=close;this.released=released;closing=false;
 }
 boolean stop(Activity activity){
  if(!owned(activity))return false;
  closing=true;if(close!=null)try{close.run();}catch(RuntimeException|LinkageError failure){return false;}
  boolean done=clean();if(done)closing=false;return done;
 }
 boolean clean(){
  if(released==null)return true;if(!closing)return false;
  try{if(!released.getAsBoolean())return false;}catch(RuntimeException|LinkageError unavailable){return false;}
  close=null;released=null;closing=false;return true;
 }
 boolean pending(Activity activity){return owned(activity)&&pending!=null;}
 boolean closing(Activity activity){return owned(activity)&&closing;}
 void earn(Activity activity,int result,long captured,Intent data){
  if(!owned(activity))return;if(pending==null)pending=new Pending(result,captured,data);
 }
 Pending take(Activity activity){if(!owned(activity)||!clean())return null;Pending earned=pending;pending=null;return earned;}
 void cancel(Activity activity){if(!owned(activity))return;pending=null;stop(activity);}
 void detach(Activity activity,boolean changing){
  if(!owned(activity))return;
  if(!changing){pending=null;stop(activity);attached.clear();if(clean())RETAINED.remove(token);}
 }
}
