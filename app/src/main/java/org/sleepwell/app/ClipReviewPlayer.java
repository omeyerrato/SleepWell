package org.sleepwell.app;

/** Serialized foreground review state. Native callbacks cannot revive a stopped/hidden player. */
final class ClipReviewPlayer implements AutoCloseable {
    interface Target {String key();long duration();}
    interface Events {void ready();void playing();void position(long value);void paused();void ended();void failed(String message);}
    interface Session extends AutoCloseable {void play(long position);void pause();void seek(long position);void close();}
    interface Factory {Session open(Target target,Events events);}
    interface Listener {void state(State value);}
    enum Phase {IDLE,LOADING,READY,PLAYING,PAUSED,ERROR}
    static final class State {final String key,error;final long position,duration;final Phase phase;State(String key,long position,long duration,Phase phase,String error){this.key=key;this.position=position;this.duration=duration;this.phase=phase;this.error=error;}}
    private final Factory factory;private final Listener listener;private Target target;private Session session;private long position;private int generation;private boolean visible,closed,ready,wantsPlay,starting;private Phase phase=Phase.IDLE;private String error="";
    ClipReviewPlayer(Factory factory,Listener listener){this.factory=factory;this.listener=listener;}
    void visible(boolean value){if(closed)return;visible=value;if(!value)stop();}
    private long bounded(long value){return Math.max(0,Math.min(target==null?0:target.duration(),value));}
    void toggle(Target next,long resume){
        if(closed||!visible)return;
        if(next==null||next.duration()<=0){failure("Recording duration is unavailable.");return;}
        if(target!=null&&target.key().equals(next.key())&&session!=null){if(wantsPlay){wantsPlay=false;starting=false;session.pause();phase=Phase.PAUSED;publish();}else{wantsPlay=true;start();}return;}
        dispose();target=next;position=bounded(resume);phase=Phase.LOADING;error="";wantsPlay=true;ready=false;starting=false;int request=++generation;publish();
        Events events=new Events(){private boolean current(){return !closed&&request==generation;}
            public void ready(){if(current()&&!ready){ready=true;phase=wantsPlay?Phase.READY:Phase.PAUSED;start();publish();}}
            public void playing(){if(current()){starting=false;if(!visible||!wantsPlay){if(session!=null)session.pause();phase=Phase.PAUSED;}else phase=Phase.PLAYING;publish();}}
            public void position(long value){if(current()){position=bounded(value);publish();}}
            public void paused(){if(current()){wantsPlay=false;starting=false;phase=Phase.PAUSED;publish();}}
            public void ended(){if(current()){position=target.duration();wantsPlay=false;starting=false;phase=Phase.PAUSED;publish();}}
            public void failed(String message){if(current())failure(message);}
        };
        try{Session opened=factory.open(next,events);if(request!=generation||closed){opened.close();return;}session=opened;start();}catch(RuntimeException problem){if(request==generation)failure("This recording could not be opened.");}
    }
    private void start(){if(!ready||session==null||!wantsPlay||!visible||closed||starting||phase==Phase.PLAYING)return;starting=true;try{session.play(position>=target.duration()?0:position);}catch(RuntimeException problem){failure("This recording could not play.");}}
    void seek(long value){if(closed||target==null)return;position=bounded(value);if(session!=null&&ready)try{session.seek(position);}catch(RuntimeException problem){failure("This recording could not seek.");return;}publish();}
    void stop(){dispose();phase=Phase.PAUSED;publish();}
    private void dispose(){generation++;wantsPlay=false;starting=false;ready=false;Session old=session;session=null;if(old!=null)try{old.close();}catch(RuntimeException ignored){}}
    private void failure(String message){dispose();phase=Phase.ERROR;error=message==null||message.isEmpty()?"Recording unavailable.":message;publish();}
    private void publish(){listener.state(new State(target==null?"":target.key(),position,target==null?0:target.duration(),phase,error));}
    @Override public void close(){if(closed)return;closed=true;dispose();}
}
