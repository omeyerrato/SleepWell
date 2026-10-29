package org.sleepwell.app;

import android.view.View;
import androidx.activity.ComponentActivity;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

/** The six real camera validators, composed without enrollment, alarm admission or outputs. */
final class PracticeCameraTasks {
 enum State {IDLE,RUNNING,PAUSED,ERROR,RELEASING,DONE}
 private static final Map<String,PracticeCameraTasks> RETAINED=new HashMap<>();
 final PracticeChildTicket.Ticket ticket;
 private WeakReference<ComponentActivity> owner;
 private PracticeCameraAccess.Ports access;
 private BooleanSupplier visible;
 private Runnable changed;
 private Runnable close;
 private BooleanSupplier released;
 private View preview;
 private int generation;
 int attempt=-1;
 private boolean active,closeFailed,earned;
 private long started;
 private State state=State.IDLE;
 private String status="Camera stays off until Start",issue="";
 private final PictureHold picture;
 private final SmileHold smile=new SmileHold();
 private final PoseHold pose=new PoseHold();
 private final ObjectHold object;
 private final CameraLightHold light;
 static PracticeCameraTasks attach(ComponentActivity activity,PracticeChildTicket.Ticket ticket,PracticeCameraAccess.Ports access,BooleanSupplier visible,Runnable changed){
  RETAINED.values().removeIf(old->!old.ticket.belongs(old.ticket.task.type)&&old.released());
  PracticeCameraTasks task=RETAINED.get(ticket.nonce);
  if(task==null||task.ticket!=ticket){task=new PracticeCameraTasks(ticket);RETAINED.put(ticket.nonce,task);}
  task.owner=new WeakReference<>(activity);task.access=access;task.visible=visible;task.changed=changed;return task;
 }
 private PracticeCameraTasks(PracticeChildTicket.Ticket ticket){
  this.ticket=ticket;
  picture=ticket.task.picture==null?null:new PictureHold(ticket.task.picture.signature);
  object=ticket.task.objectTarget==null?null:new ObjectHold(ticket.task.objectTarget);
  light=ticket.task.type.equals("Camera light")?new CameraLightHold(ticket.task.rounds):null;
 }
 State state(){return !active&&!released()?State.RELEASING:state;}String status(){return state()==State.RELEASING?(earned?"Check earned · releasing camera":"Releasing this camera attempt"):status;}String issue(){return issue;}
 boolean active(){return active;}
 boolean boundTo(ComponentActivity activity){return activity()==activity;}
 void pause(ComponentActivity activity){if(!boundTo(activity))return;close();}
 void detach(ComponentActivity activity){if(!boundTo(activity))return;close();owner.clear();visible=null;changed=null;}
 View preview(){return active?preview:null;}
 boolean done(){return earned&&ticket.earned(attempt);}
 private ComponentActivity activity(){return owner==null?null:owner.get();}
 private boolean current(){
  try{return active&&visible!=null&&visible.getAsBoolean()&&ticket.captureCurrent(attempt)&&!access.busy()&&access.permission(ticket.task.type)==PracticeCameraAccess.Permission.READY;}
  catch(RuntimeException unreadable){return false;}
 }
 private boolean current(int expected){return expected==generation&&current();}
 void start(){
  ComponentActivity activity=activity();if(activity==null||visible==null||!visible.getAsBoolean())return;
  if(earned){status="This earned result is waiting for its own camera cleanup.";notifyChanged();return;}
  if(active||!released()){issue="Release this camera attempt before starting another.";notifyChanged();return;}
  if(!ticket.clean()){issue="Previous practice capture is still releasing.";notifyChanged();return;}
  String unavailable=access.unavailable(ticket.task);if(!unavailable.isEmpty()){issue=unavailable;state=State.ERROR;notifyChanged();return;}
  attempt=ticket.beginCapture(activity,true);if(attempt<0){issue="Another practice owner is active or still releasing.";notifyChanged();return;}
  active=true;closeFailed=false;state=State.RUNNING;issue="";started=access.now();int expected=++generation;
  if(picture!=null)picture.reset();smile.reset();pose.reset();if(object!=null)object.reset();if(light!=null)light.reset();
  try{
   switch(ticket.task.type){
    case "QR code"->{
     if(ticket.task.code==null)throw new IllegalArgumentException("Enroll a morning code first");
     QrCamera.Port port=access.code(ticket.task.code.format);bind(activity,port::close,port::released);preview=port.preview();
     status="Scan the enrolled code at "+ticket.task.code.label;
     port.start(new QrScanSession.Listener(){public void captured(String hash){onMain(()->{if(!current(expected))return;if(MorningCode.validHash(hash)&&MorningCode.same(ticket.task.code.hash,hash))solved(expected);else{status="That is not the enrolled morning code.";notifyChanged();}});}public void error(String issue){onMain(()->failed(expected,issue));}});
    }
    case "Picture match"->{
     if(picture==null)throw new IllegalArgumentException("Capture a picture reference first");
     PictureCamera.Port port=access.picture();bind(activity,port::close,port::released);preview=port.preview();status="Match the saved framing and lighting.";
     port.start(new PictureCamera.Listener(){public void frame(PictureSignature signature,long captured,String issue){onMain(()->{if(!current(expected))return;if(picture.frame(signature,captured,access.now()))solved(expected);else{status=signature==null?message(issue,"Use a detailed, well-lit view."):"Hold steady · "+picture.progress()+" of 3 frames";notifyChanged();}});}public void error(String issue){onMain(()->failed(expected,issue));}});
    }
    case "Smile"->{
     SmileCamera.Port port=access.smile();bind(activity,port::close,port::released);preview=port.preview();status="Face the camera, smile naturally and hold steady.";
     port.start(new SmileCamera.Listener(){public void frame(SmileHold.Sample sample,String issue){onMain(()->{if(!current(expected))return;if(smile.frame(sample,access.now()))solved(expected);else{status=sample!=null&&sample.framed()?"Hold your smile · "+smile.progress()+"%":message(issue,"Center one face in good light.");notifyChanged();}});}public void error(String issue){onMain(()->failed(expected,issue));}});
    }
    case "Pose"->{
     PoseCamera.Port port=access.pose();bind(activity,port::close,port::released);preview=port.preview();status="Keep your head, arms and hips visible. Raise both hands.";
     port.start(new PoseCamera.Listener(){public void frame(PoseHold.Sample sample,String issue){onMain(()->{if(!current(expected))return;if(pose.frame(sample,access.now()))solved(expected);else{status=sample!=null&&sample.raised()?"Hold both hands up · "+pose.progress()+"%":message(issue,"Keep your upper body in view.");notifyChanged();}});}public void error(String issue){onMain(()->failed(expected,issue));}});
    }
    case "Object recognition"->{
     if(object==null)throw new IllegalArgumentException("Choose an object target first");
     ObjectCamera.Port port=access.object();bind(activity,port::close,port::released);preview=port.preview();status="Keep "+ticket.task.objectTarget.label()+" centered.";
     port.start(new ObjectCamera.Listener(){public void frame(float[] scores,long captured){float[] snapshot=scores==null?null:scores.clone();onMain(()->{if(!current(expected))return;if(object.frame(snapshot,captured,access.now()))solved(expected);else{status="Hold the selected object steady · "+object.progress()+"%";notifyChanged();}});}public void error(String issue){onMain(()->failed(expected,issue));}});
    }
    case "Camera light"->{
     CameraLightCamera.Port port=access.light();bind(activity,port::close,port::released);preview=port.preview();status="Keep a room light centered.";
     port.start(new CameraLightCamera.Listener(){public void frame(double brightness,long captured,String issue){onMain(()->{if(!current(expected))return;if(light.frame(brightness,captured,access.now()))solved(expected);else{status=Double.isFinite(brightness)?Math.round(brightness*100/255)+"% brightness · hold "+light.progress()+"%":message(issue,"Waiting for a fresh frame.");notifyChanged();}});}public void error(String issue){onMain(()->failed(expected,issue));}});
    }
    default->throw new IllegalArgumentException("This camera adapter is not available");
   }
  }catch(RuntimeException|LinkageError failure){close();state=State.ERROR;issue="Camera could not start safely. Retry after cleanup or choose the configured alternative.";}
  notifyChanged();
 }
 private void bind(ComponentActivity activity,Runnable close,BooleanSupplier released){
  this.close=close;this.released=released;
  ticket.bindCleanup(activity,this::close,this::released,this::retry);
 }
 private void solved(int expected){
  ComponentActivity activity=activity();
  if(!current(expected)||activity==null||!ticket.markEarned(activity,true,attempt))return;
  earned=true;status="Real check completed · releasing camera";close();state=State.DONE;notifyChanged();
 }
 private void failed(int expected,String issue){if(!current(expected))return;this.issue=message(issue,"Camera is unavailable. Retry or choose the configured alternative.");close();state=State.ERROR;notifyChanged();}
 private static String message(String issue,String fallback){return issue==null||issue.isBlank()?fallback:issue;}
 void tick(){
  if(active&&!current()){close();state=State.PAUSED;status="Camera paused after capture ownership changed.";notifyChanged();return;}
  if(active&&access.now()-started>=(ticket.task.type.equals("Object recognition")?30000:60000)){close();state=State.PAUSED;status="Attempt ended. Start a new attempt when ready.";notifyChanged();}
 }
 boolean released(){
  if(active||closeFailed)return false;
  if(released==null)return true;
  try{return released.getAsBoolean();}catch(RuntimeException|LinkageError uncertain){return false;}
 }
 void close(){
  active=false;generation++;
  try{if(close!=null)close.run();closeFailed=false;}catch(RuntimeException|LinkageError failure){closeFailed=true;issue="This camera attempt has not confirmed cleanup.";}
  if(!earned&&state==State.RUNNING){state=State.PAUSED;status="Camera paused. Tap Start for a new attempt.";}
 }
 void retry(){close();}
 void forget(){if(released())RETAINED.remove(ticket.nonce);}
 private static void onMain(Runnable callback){if(android.os.Looper.myLooper()==android.os.Looper.getMainLooper())callback.run();else new android.os.Handler(android.os.Looper.getMainLooper()).post(callback);}
 private void notifyChanged(){if(changed!=null&&visible!=null&&visible.getAsBoolean())changed.run();}
}
