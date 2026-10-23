package org.sleepwell.app;

/** Best-effort ownership: never claims an already-lit torch or fights an external OFF. */
final class TorchLease {
    interface Port {void set(boolean enabled);}
    private final Port port;private boolean seen,owned,pending,closed;
    TorchLease(Port port){this.port=port;}
    void observed(boolean enabled){
        if(closed)return;
        if(!seen){seen=true;if(enabled)return;pending=true;try{port.set(true);}catch(RuntimeException error){pending=false;throw error;}}
        else if(pending&&enabled){pending=false;owned=true;}
        else if(!pending&&!enabled)owned=false;
    }
    void unavailable(){seen=true;owned=pending=false;}
    void close(){if(closed)return;closed=true;if(owned||pending){owned=pending=false;port.set(false);}}
}
