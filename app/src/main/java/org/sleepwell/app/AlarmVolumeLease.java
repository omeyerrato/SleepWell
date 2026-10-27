package org.sleepwell.app;

/** Saved ownership protects the user's baseline across a same-occurrence service restart. */
final class AlarmVolumeLease {
    interface Port {int read();int maximum();void write(int level);}
    interface Journal {State load();boolean save(State state);void clear(String owner);}
    static final class State {
        final String occurrence,owner;final int original,last;final boolean overridden;
        State(String occurrence,String owner,int original,int last,boolean overridden){this.occurrence=occurrence;this.owner=owner;this.original=original;this.last=last;this.overridden=overridden;}
    }
    private final Port port;private final Journal journal;private final String occurrence,owner;
    private boolean acquired,backup,closed;private float scale=1;
    AlarmVolumeLease(Port port,Journal journal,String occurrence,String owner){this.port=port;this.journal=journal;this.occurrence=occurrence;this.owner=owner;}
    synchronized float acquire(AlarmWakePolicy policy,boolean escalate){
        if(closed)return 0;if(acquired&&escalate==backup)return scale;boolean leavingBackup=acquired&&backup&&!escalate;
        float conservative=policy.percent(escalate)/100f;
        // A quiet developer test never raises the global stream. Its permanent
        // digital cap remains safe even if volume keys are pressed during it.
        if(policy.quiet){acquired=true;scale=conservative;return scale;}
        int maximum=port.maximum();if(maximum<=0)return conservative;
        State saved=journal.load();int current=port.read(),original=current;
        if(saved!=null&&saved.occurrence.equals(occurrence)){
            if(saved.last==current&&!saved.overridden)original=saved.original;
            else if(!escalate){journal.save(new State(occurrence,owner,current,current,true));acquired=true;backup=false;scale=leavingBackup&&current>0?Math.min(1,policy.streamLevel(maximum,false)/(float)current):policy.quiet?conservative:1;return scale;}
        }else if(saved!=null){
            if(!saved.overridden&&current==saved.last){port.write(saved.original);current=port.read();}
            original=current;
        }
        int target=policy.streamLevel(maximum,escalate);
        // Persist the restoration baseline before touching the global stream.
        if(!journal.save(new State(occurrence,owner,original,target,false)))return conservative;
        try{port.write(target);}catch(RuntimeException denied){return conservative;}
        // Fixed-volume devices may ignore writes without throwing.
        int actual=port.read();scale=actual<=target?1:Math.min(1,target/(float)actual);acquired=true;backup=escalate;return scale;
    }
    synchronized void release(){
        if(closed)return;closed=true;State saved=journal.load();if(saved==null||!saved.owner.equals(owner))return;
        if(!saved.overridden&&port.read()==saved.last)port.write(saved.original);
        journal.clear(owner);
    }
}
