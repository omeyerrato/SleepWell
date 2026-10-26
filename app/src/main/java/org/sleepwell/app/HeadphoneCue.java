package org.sleepwell.app;

import java.util.*;
import java.util.function.*;

/** Sound-only route gate. A connected endpoint is never proof of an active route. */
final class HeadphoneCue implements AutoCloseable {
    enum Kind {WIRED,USB_HEADSET,BLE_HEADSET,BLUETOOTH_MEDIA,OTHER}
    static final class Endpoint {final int id;final Kind kind;Endpoint(int id,Kind kind){this.id=id;this.kind=kind;}boolean supported(){return kind==Kind.WIRED||kind==Kind.USB_HEADSET||kind==Kind.BLE_HEADSET;}}
    interface Sink extends AutoCloseable {
        List<Endpoint> available();boolean prefer(Endpoint endpoint);void startSilent(Runnable routeChanged);List<Endpoint> routed();
        /** Must recheck the actual route immediately before queueing any audible frames. */
        int chunk(Endpoint expected,int intensity,int frame,int frames);default boolean drained(){return true;}void silence();void close();
    }
    interface Scheduler {void later(Runnable task,long delay);long now();}
    private final Sink sink;private final Scheduler scheduler;private final BooleanSupplier allowed;private final Consumer<String> status;private final Runnable outputConfirmed;private boolean outputReported;
    private Endpoint selected;private SnoreCuePolicy.Settings settings;private long deadline,pulseStart;private int pulse,frame;private boolean closed,started,confirmed,queued;
    HeadphoneCue(Sink sink,Scheduler scheduler,BooleanSupplier allowed,Consumer<String> status){this(sink,scheduler,allowed,status,()->{});}
    HeadphoneCue(Sink sink,Scheduler scheduler,BooleanSupplier allowed,Consumer<String> status,Runnable outputConfirmed){this.sink=sink;this.scheduler=scheduler;this.allowed=allowed;this.status=status;this.outputConfirmed=outputConfirmed;}
    static boolean matches(List<Endpoint> routes,Endpoint selected){return selected!=null&&routes!=null&&routes.size()==1&&routes.get(0).id==selected.id&&routes.get(0).kind==selected.kind&&routes.get(0).supported();}
    static Endpoint select(List<Endpoint> available){if(available==null)return null;for(Kind kind:new Kind[]{Kind.WIRED,Kind.USB_HEADSET,Kind.BLE_HEADSET})for(Endpoint endpoint:available)if(endpoint.kind==kind)return endpoint;return null;}
    void start(SnoreCuePolicy.Settings settings){if(closed||started)return;started=true;this.settings=settings;if(!allowed.getAsBoolean()){close();return;}try{
        selected=select(sink.available());if(selected==null){fail("No supported headphone route. Snoring sounds skipped.");return;}if(!sink.prefer(selected)){fail("Headphone route could not be requested. Snoring sounds skipped.");return;}
        deadline=scheduler.now()+1500;sink.startSilent(this::changed);schedule(20);
    }catch(RuntimeException error){fail("Headphone sound is unavailable. Snoring sounds skipped.");}}
    private void changed(){if(closed)return;try{if(!allowed.getAsBoolean()){close();return;}if(confirmed&&!safeRoute()){fail("Headphone route lost. Snoring sounds stopped.");return;}if(!confirmed)schedule(0);}catch(RuntimeException error){fail("Headphone route is unavailable. Snoring sounds stopped.");}}
    private void schedule(long delay){if(closed||queued)return;queued=true;scheduler.later(()->{queued=false;check();},delay);}
    private boolean safeRoute(){return matches(sink.routed(),selected);}
    private void check(){if(closed)return;try{
        if(!allowed.getAsBoolean()){close();return;}
        if(!safeRoute()){if(confirmed||scheduler.now()>=deadline){fail(confirmed?"Headphone route lost. Snoring sounds stopped.":"Headphone route could not be verified. Snoring sounds skipped.");return;}schedule(20);return;}
        if(!confirmed){confirmed=true;pulseStart=scheduler.now();deadline=pulseStart+3500;status.accept("");}
        long target=pulseStart+pulse*900L;if(scheduler.now()<target){schedule(Math.min(20,target-scheduler.now()));return;}
        if(frame>=4000){if(scheduler.now()>deadline){fail("Headphone sound timed out. Snoring sounds stopped.");return;}if(!sink.drained()){schedule(20);return;}if(!outputReported){outputReported=true;try{outputConfirmed.run();}catch(RuntimeException ignored){}}frame=0;if(++pulse>=settings.repeats){close();return;}schedule(20);return;}
        int requested=Math.min(320,4000-frame),written=sink.chunk(selected,settings.intensity,frame,requested);if(written<0||written>requested||scheduler.now()>deadline){fail("Headphone route changed or sound could not play. Snoring sounds stopped.");return;}frame+=written;schedule(20);
    }catch(RuntimeException error){fail("Headphone sound is unavailable. Snoring sounds stopped.");}}
    private void fail(String message){if(closed)return;try{status.accept(message);}finally{close();}}
    @Override public void close(){if(closed)return;closed=true;boolean failed=false;try{sink.silence();}catch(RuntimeException error){failed=true;}try{sink.close();}catch(RuntimeException error){failed=true;}if(failed)status.accept("Headphone sound could not be stopped cleanly.");}
}
