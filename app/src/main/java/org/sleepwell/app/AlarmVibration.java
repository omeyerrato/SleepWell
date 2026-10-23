package org.sleepwell.app;

import java.util.function.*;

/** One lazy vibration attempt per output owner; elapsed time belongs to the alarm occurrence. */
final class AlarmVibration implements AutoCloseable {
    interface Port {boolean start();void cancel();}
    private final long delay;private final LongSupplier elapsed;private final BooleanSupplier alive;
    private final Supplier<Port> factory;private final Runnable failed;
    private Port port;private boolean attempted,closed;
    AlarmVibration(long delay,LongSupplier elapsed,BooleanSupplier alive,Supplier<Port> factory,Runnable failed){this.delay=Math.max(0,delay);this.elapsed=elapsed;this.alive=alive;this.factory=factory;this.failed=failed;}
    void tick(boolean backup){
        if(closed)return;if(!alive.getAsBoolean()){close();return;}if(attempted||!backup&&Math.max(0,elapsed.getAsLong())<delay)return;
        attempted=true;
        try{port=factory.get();if(!alive.getAsBoolean()){close();return;}if(port==null||!port.start()){failure();return;}if(!alive.getAsBoolean())close();}
        catch(RuntimeException unavailable){failure();}
    }
    private void failure(){close();try{failed.run();}catch(RuntimeException ignored){/* Output failure must not terminate the alarm lifetime. */}}
    @Override public void close(){if(closed)return;closed=true;Port owned=port;port=null;if(owned!=null)try{owned.cancel();}catch(RuntimeException ignored){/* Never retry a failed hardware release. */}}
}
