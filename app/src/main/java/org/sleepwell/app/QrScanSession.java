package org.sleepwell.app;

/** Camera callbacks lose ownership on pause, retry or occurrence loss. Earned reads await release. */
final class QrScanSession implements AutoCloseable {
 interface Listener {void captured(String hash);void error(String reason);}
 interface Port {void start(Listener listener);void close();default boolean released(){return true;}}
 interface Effects {boolean current();void captured(String hash);void error(String reason);default void earned(String hash){}}
 enum State {CLOSED,SCANNING,CAPTURED,ERROR}
 private final Port port;private final Effects effects;
 private long generation;private boolean active,released=true,closeFailed;private State state=State.CLOSED;
 private String pending="";
 QrScanSession(Port port,Effects effects){this.port=port;this.effects=effects;}
 State state(){return state;}
 boolean released(){if(!active&&!released&&!closeFailed)try{released=port.released();}catch(RuntimeException failure){return false;}return released;}
 boolean pending(){return !pending.isEmpty();}
 void start(){
  close();
  if(!released()||pending()){state=State.ERROR;effects.error("Release the previous camera attempt before scanning again.");return;}
  if(!effects.current())return;long ticket=++generation;active=true;released=false;state=State.SCANNING;
  try{port.start(new Listener(){
   public void captured(String hash){
    if(!owned(ticket))return;if(!MorningCode.validHash(hash)){error("This code could not be read.");return;}
    pending=hash;effects.earned(hash);stop(State.CAPTURED);
    if(!complete())effects.error("Camera cleanup is pending. Retry cleanup before continuing.");
   }
   public void error(String reason){if(!owned(ticket))return;stop(State.ERROR);effects.error(reason==null||reason.isBlank()?"Camera is unavailable.":reason);}
  });}catch(RuntimeException unavailable){if(active&&ticket==generation){stop(State.ERROR);effects.error("Camera could not start. Check access and retry.");}}
 }
 /** Called only by the current UI; cleanup itself does not need UI admission or focus. */
 boolean complete(){if(pending.isEmpty()||!released()||!effects.current())return false;String hash=pending;pending="";state=State.CAPTURED;effects.captured(hash);return true;}
 void cancel(){pending="";close();}
 private boolean owned(long ticket){if(!active||ticket!=generation)return false;if(!effects.current()){close();return false;}return true;}
 private void stop(State next){close();state=next;}
 @Override public void close(){active=false;generation++;try{port.close();closeFailed=false;released=port.released();}catch(RuntimeException unavailable){closeFailed=true;released=false;}state=State.CLOSED;}
}
