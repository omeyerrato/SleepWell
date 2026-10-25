package org.sleepwell.app;
import java.util.concurrent.atomic.*;

/** Process capture ownership. Only confirmed native cleanup releases the token; no timeout release. */
final class MicrophoneLease {
    private static final AtomicReference<MicrophoneLease> OWNER=new AtomicReference<>();
    final String purpose;private MicrophoneLease(String purpose){this.purpose=purpose;}
    static MicrophoneLease acquire(String purpose){MicrophoneLease token=new MicrophoneLease(purpose);return OWNER.compareAndSet(null,token)?token:null;}
    static boolean occupied(){return OWNER.get()!=null;}
    boolean current(){return OWNER.get()==this;}
    void released(){OWNER.compareAndSet(this,null);}
}
