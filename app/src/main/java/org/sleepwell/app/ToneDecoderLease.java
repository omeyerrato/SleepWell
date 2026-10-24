package org.sleepwell.app;

import java.util.concurrent.atomic.AtomicBoolean;

/** Process-wide: UI closure never proves a provider/native decoder stopped. */
final class ToneDecoderLease implements AutoCloseable {
    private static final AtomicBoolean BUSY=new AtomicBoolean();private final AtomicBoolean released=new AtomicBoolean();
    static ToneDecoderLease acquire(){if(!BUSY.compareAndSet(false,true))throw new IllegalStateException("Previous preview is still closing");return new ToneDecoderLease();}
    private ToneDecoderLease(){}
    public void close(){if(released.compareAndSet(false,true))BUSY.set(false);}
}
