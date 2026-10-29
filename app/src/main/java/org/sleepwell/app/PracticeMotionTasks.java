package org.sleepwell.app;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.*;
import android.widget.*;
import java.util.function.*;

/** Real foreground task engines; only their explicit Start actions acquire a practice lease. */
final class PracticeMotionTasks implements AutoCloseable {
    final Activity owner;final PracticeChildTicket.Ticket ticket;final PracticeMotionAccess.Ports access;
    final PracticeCheckpoints prefs;final BooleanSupplier visible;final Runnable complete,fallback;
    LightTask light;SpinTask spin;TiltMazeTask tilt;WalkSession walk;Button shakeMath,nfcMath;
    VisibleTaskSensors.Port shake;PracticeNfcAccess.Port nfc;
    boolean closed,released=true,shakeUnavailable,nfcUnavailable;int attempt=-1,shakes;long armed,lastShake;String tag="";
    PracticeMotionTasks(Activity owner,LinearLayout card,PracticeChildTicket.Ticket ticket,PracticeMotionAccess.Ports access,PracticeCheckpoints prefs,BooleanSupplier visible,Runnable complete,Runnable fallback){
        this.owner=owner;this.ticket=ticket;this.access=access;this.prefs=prefs;this.visible=visible;this.complete=complete;this.fallback=fallback;
        ticket.bindCleanup(owner,this::close,this::released);
        switch(ticket.task.type){
            case "Light"->light=new LightTask(card,prefs,ticket.run,ticket.unit,ticket.task.rounds,this::current,complete,fallback,sensor(access.sensor(VisibleTaskSensors.Kind.LIGHT)));
            case "Spin around"->spin=new SpinTask(card,prefs,ticket.run,ticket.unit,this::current,complete,fallback,sensor(access.sensor(VisibleTaskSensors.Kind.ROTATION)));
            case "Tilt maze"->{tilt=new TiltMazeTask(card,prefs,ticket.run,ticket.unit,ticket.round,this::current,complete,fallback,tilt(access.tilt()));LinearLayout.LayoutParams size=(LinearLayout.LayoutParams)tilt.view.getLayoutParams();size.width=Math.max(1,Math.min(Design.dp(owner,260),owner.getWindowManager().getCurrentWindowMetrics().getBounds().width()-Design.dp(owner,48)));size.gravity=android.view.Gravity.CENTER_HORIZONTAL;tilt.view.setLayoutParams(size);}
            case "Walk"->walk(card);
            case "Shake"->shake(card);
            case "NFC"->nfc(card);
            default->throw new IllegalArgumentException("This motion adapter is unfinished.");
        }
    }
    boolean current(){if(Looper.myLooper()!=Looper.getMainLooper()||closed||!visible.getAsBoolean())return false;try{return !access.busy();}catch(RuntimeException failure){return false;}}
    private int begin(){if(!current())return -1;attempt=ticket.beginCapture(owner,true);return attempt;}
    private boolean listening(int lease){return current()&&ticket.captureCurrent(lease);}
    private VisibleTaskSensors.Port sensor(VisibleTaskSensors.Port delegate){return new VisibleTaskSensors.Port(){
        public boolean available(){return delegate.available();}public float range(){return delegate.range();}public int reportingMode(){return delegate.reportingMode();}public long now(){return delegate.now();}
        public boolean start(VisibleTaskSensors.Sink sink){int lease=begin();if(lease<0)return false;released=false;return delegate.start(new VisibleTaskSensors.Sink(){public void sample(long time,float[] values,int accuracy){if(listening(lease))sink.sample(time,values,accuracy);}public void accuracy(int value){if(listening(lease))sink.accuracy(value);}});}
        public boolean stop(){boolean stopped=delegate.stop();released=stopped;return stopped;}
    };}
    private TiltSensors.Port tilt(TiltSensors.Port delegate){if(delegate==null)return null;return new TiltSensors.Port(){
        public long now(){return delegate.now();}public int rotation(){return delegate.rotation();}
        public boolean start(TiltSensors.Listener listener){int lease=begin();if(lease<0)return false;released=false;return delegate.start((time,x,y,z)->{if(listening(lease))listener.sample(time,x,y,z);});}
        public boolean stop(){boolean stopped=delegate.stop();released=stopped;return stopped;}
    };}
    private void walk(LinearLayout card){
        Design.text(card,"Walk "+ticket.task.targetSteps+" steps",23,600,Design.INK);
        Design.text(card,"Only the phone's real step detector or counter is used. Start when you can walk safely.",14,400,Design.MUTED);
        int earned=Math.max(0,Math.min(ticket.task.targetSteps,prefs.getInt("walkEarned",0)));
        WalkPermission.Status accessStatus=access.walkAccess();
        TextView status=Design.text(card,accessStatus==WalkPermission.Status.READY?earned+" / "+ticket.task.targetSteps+" steps · sensor off until Start":accessStatus==WalkPermission.Status.UNSUPPORTED?"A real step detector or counter is unavailable. Use Math instead.":"Step access is required. Allow access when ready; checks remain off.",14,500,Design.BROWN);
        WalkSession.Port delegate=access.walk();
        WalkSession.Port guarded=new WalkSession.Port(){
            public WalkSteps.Mode mode(){return delegate.mode();}public boolean allowed(){return delegate.allowed();}public long now(){return delegate.now();}
            public boolean begin(WalkSession.Sink sink){int lease=beginLease();if(lease<0)return false;released=false;return delegate.begin((time,value)->{if(listening(lease))sink.sample(time,value);});}
            private int beginLease(){return PracticeMotionTasks.this.begin();}
            public void close(){delegate.close();released=true;}
        };
        walk=new WalkSession(guarded,new WalkSession.Effects(){
            public boolean current(String run,int unit){return PracticeMotionTasks.this.current();}
            public void progress(String run,int unit,int value){if(!prefs.edit().putInt("walkEarned",value).commit())throw new IllegalStateException("Practice steps could not be retained");status.setText(value+" / "+ticket.task.targetSteps+" steps");}
            public void solved(String run,int unit){if(current(run,unit))complete.run();}
            public void unavailable(String run,int unit,WalkSession.State state){status.setText("Steps unavailable. Start again or choose Math.");}
        },ticket.run,ticket.unit,ticket.task.targetSteps,earned);
        Button start=Design.button(card,"Start step check",true,()->{if(!current())return;try{if(access.walkAccess()!=WalkPermission.Status.READY){status.setText("Step access is unavailable. Use Allow step access or Math.");return;}if(begin()<0){status.setText("Another practice capture is closing. Retry later.");return;}walk.resume();if(current())status.setText(walk.state()==WalkSession.State.TRACKING?walk.earned()+" / "+ticket.task.targetSteps+" steps":"Step readings unavailable. Retry or choose Math.");}catch(RuntimeException failure){status.setText("Step readings unavailable. Retry or choose Math.");}});
        start.setEnabled(accessStatus==WalkPermission.Status.READY);
        Button allow=Design.button(card,"Allow step access",false,()->{if(current())try{access.requestWalk();}catch(RuntimeException unavailable){status.setText("Step access could not open. Retry or choose Math.");}});allow.setVisibility(accessStatus==WalkPermission.Status.READY||accessStatus==WalkPermission.Status.UNSUPPORTED?android.view.View.GONE:android.view.View.VISIBLE);
        Design.button(card,"Use Math instead",false,()->{if(current())fallback.run();});
    }
    private void shake(LinearLayout card){
        int target=ticket.task.rounds*5;shakes=Math.max(0,Math.min(target,prefs.getInt("shakes",0)));
        Design.text(card,"Shake deliberately",23,600,Design.INK);TextView status=Design.text(card,shakes+" / "+target+" shakes",18,600,Design.BROWN);
        shake=access.sensor(VisibleTaskSensors.Kind.ACCELERATION);
        Design.button(card,"Start shake check",true,()->{if(!current())return;try{if(!shake.stop()){status.setText("Sensor cleanup failed. Retry before continuing.");return;}released=true;if(!shake.available()){status.setText("Shake sensor unavailable. Use Math instead.");return;}int lease=begin();if(lease<0){status.setText("Another practice capture is closing. Retry later.");return;}if(shakes>=target){complete.run();return;}armed=shake.now();lastShake=0;released=false;
            if(!shake.start(new VisibleTaskSensors.Sink(){public void sample(long time,float[] values,int accuracy){if(!listening(lease)||values.length<3||time<armed||time>shake.now())return;double magnitude=Math.sqrt(values[0]*values[0]+values[1]*values[1]+values[2]*values[2]);long now=shake.now()/1_000_000L;if(WakeShakeRule.accepts(magnitude,now,lastShake)){lastShake=now;shakes++;if(!prefs.edit().putInt("shakes",shakes).commit()){status.setText("Practice progress could not be retained. Return and retry.");return;}status.setText(shakes+" / "+target+" shakes");if(shakes>=target)complete.run();}}public void accuracy(int value){}})){released=shake.stop();shakeUnavailable=true;shakeMath.setVisibility(android.view.View.VISIBLE);status.setText("Shake readings unavailable. Retry or choose Math.");}
        }catch(RuntimeException unavailable){shakeUnavailable=true;shakeMath.setVisibility(android.view.View.VISIBLE);status.setText("Shake readings unavailable. Retry or choose Math.");}});
        shakeMath=Design.button(card,"Use Math instead",false,()->{if(current())try{if(!shake.available()||shakeUnavailable)fallback.run();else status.setText("Shake is available. Complete the real check, or Cancel preview.");}catch(RuntimeException unavailable){status.setText("Sensor readiness could not be read. Return and retry.");}});shakeMath.setVisibility(shake.available()?android.view.View.GONE:android.view.View.VISIBLE);
    }
    private void nfc(LinearLayout card){
        nfc=access.nfc();tag=access.enrolledTag();Design.text(card,"Tap your enrolled morning tag",23,600,Design.INK);
        TextView status=Design.text(card,"NFC reader stays off until Start",14,500,Design.BROWN);
        Design.button(card,"Start tag check",true,()->{if(!current())return;try{if(!nfc.stop()){status.setText("Reader cleanup failed. Retry before continuing.");return;}released=true;if(!nfc.available()||!nfc.enabled()||tag.isEmpty()){status.setText("An enabled NFC reader and enrolled tag are required. Use NFC settings or Math.");return;}int lease=begin();if(lease<0){status.setText("Another practice capture is closing. Retry later.");return;}released=false;if(!nfc.start(id->{if(!listening(lease))return;String enrolled;try{enrolled=access.enrolledTag();}catch(RuntimeException unreadable){status.setText("The enrolled tag could not be read. Return and retry.");return;}if(!tag.equals(enrolled)){status.setText("The enrolled tag changed. Cancel and reopen this preview.");return;}if(tag.equals(id))complete.run();else status.setText("That is not your enrolled tag.");})){released=nfc.stop();nfcUnavailable=true;nfcMath.setVisibility(android.view.View.VISIBLE);status.setText("Reader could not start. Retry or choose Math.");}else status.setText("Ready for your enrolled tag");}catch(RuntimeException failure){nfcUnavailable=true;nfcMath.setVisibility(android.view.View.VISIBLE);status.setText("NFC reader unavailable. Retry or choose Math.");}});
        Design.button(card,"NFC settings",false,()->{if(current())try{access.nfcSettings();}catch(RuntimeException unavailable){status.setText("NFC settings could not open. Return and check access.");}});
        nfcMath=Design.button(card,"Use Math instead",false,()->{if(current())try{if(!nfc.available()||!nfc.enabled()||tag.isEmpty()||nfcUnavailable)fallback.run();else status.setText("NFC is available. Tap the enrolled tag, or Cancel preview.");}catch(RuntimeException unavailable){status.setText("NFC readiness could not be read. Return and retry.");}});nfcMath.setVisibility(!nfc.available()||!nfc.enabled()||tag.isEmpty()?android.view.View.VISIBLE:android.view.View.GONE);
    }
    boolean released(){return released&&(light==null||light.released())&&(spin==null||spin.released())&&(tilt==null||tilt.released());}
    public void close(){boolean saved=true;try{if(tilt!=null&&!tilt.solved)saved=ChallengePackState.save(prefs,tilt.key,tilt.maze);}finally{try{if(light!=null)light.close();if(spin!=null)spin.close();if(tilt!=null)tilt.close();if(walk!=null)released=walk.stopForFallback();if(shake!=null)released=shake.stop();if(nfc!=null)released=nfc.stop();}finally{closed=true;}}if(!saved)throw new IllegalStateException("Practice maze checkpoint could not be retained");}
}
