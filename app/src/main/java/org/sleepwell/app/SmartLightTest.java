package org.sleepwell.app;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/** Explicit confirmed low-level test, bounded to three seconds and preempted by a real alarm. */
final class SmartLightTest {
    private static SmartLightTest current;private final AtomicBoolean alive=new AtomicBoolean(true);private SmartLightRun run;
    static synchronized boolean start(SmartLightConfig saved,Consumer<String> status){if(SmartLightAlarm.active())return false;stop();SmartLightTest owner=new SmartLightTest();current=owner;SmartLightConfig quiet=new SmartLightConfig(saved.endpoint,saved.token,saved.entity,true,1,Math.min(5,saved.maximum),true,saved.revision);SmartLightAlarm.worker.execute(()->{if(!owner.alive.get()||SmartLightAlarm.active())return;owner.run=new SmartLightRun(quiet,new HomeAssistantLight(quiet),()->owner.alive.get()&&!SmartLightAlarm.active(),status);owner.run.tick(60000);SmartLightAlarm.worker.schedule(owner::close,3,TimeUnit.SECONDS);});return true;}
    private void close(){if(!alive.compareAndSet(true,false))return;SmartLightAlarm.worker.execute(()->{if(run!=null)run.close();});synchronized(SmartLightTest.class){if(current==this)current=null;}}
    static synchronized void stop(){if(current!=null)current.close();}
}
