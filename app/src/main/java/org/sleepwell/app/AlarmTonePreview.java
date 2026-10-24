package org.sleepwell.app;

/** Explicit foreground preview only. Digital gain is independent of the alarm-volume draft. */
final class AlarmTonePreview implements AutoCloseable {
    enum Phase {STOPPED,LOADING,PLAYING,PAUSED,ERROR}
    interface Events {void ready();void playing();void paused();void ended();void failed(String message);}
    interface Session extends AutoCloseable {void play();void pause();void gain(float gain);void close();}
    interface Port extends AutoCloseable {Session open(String tone,float gain,Events events);void close();}
    interface Observer {void changed();}
    private final Port port;private final Observer observer;private Session session;private boolean visible,closed;private int generation,volume=25;private String tone="",error="";private Phase phase=Phase.STOPPED;
    AlarmTonePreview(Port port,Observer observer){this.port=port;this.observer=observer;}
    Phase phase(){return phase;}String tone(){return tone;}String error(){return error;}int volume(){return volume;}
    void volume(int percent){volume=Math.max(0,Math.min(25,percent));if(session!=null)session.gain(volume/100f);observer.changed();}
    void visible(boolean value){visible=value;if(!value)stop();}
    void toggle(String value){if(closed||!visible)return;if(value.equals(tone)&&session!=null){if(phase==Phase.PLAYING||phase==Phase.LOADING){if(phase==Phase.LOADING)stop();else session.pause();}else if(phase==Phase.PAUSED)session.play();return;}stop();tone=value;error="";phase=Phase.LOADING;int request=++generation;observer.changed();boolean[] ready={false};try{Session opened=port.open(value,volume/100f,new Events(){boolean live(){return !closed&&visible&&request==generation;}public void ready(){if(live()){ready[0]=true;if(session!=null)session.play();}}public void playing(){if(live()){phase=Phase.PLAYING;observer.changed();}}public void paused(){if(live()){phase=Phase.PAUSED;observer.changed();}}public void ended(){if(live())stop();}public void failed(String message){if(live()){dispose();phase=Phase.ERROR;error=message==null?"Preview unavailable. Try another sound.":message;observer.changed();}}});if(request==generation){session=opened;if(ready[0])session.play();}else opened.close();}catch(RuntimeException failure){dispose();phase=Phase.ERROR;error="Preview unavailable. Try another sound.";observer.changed();}}
    private void dispose(){generation++;Session prior=session;session=null;if(prior!=null)prior.close();}
    void stop(){dispose();phase=Phase.STOPPED;error="";observer.changed();}
    @Override public void close(){if(closed)return;closed=true;stop();port.close();}
}
