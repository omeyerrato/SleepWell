package org.sleepwell.app;

import java.util.Locale;
import java.util.function.*;

/** Metadata-only, best-effort global-actions guard. It never treats arbitrary System UI dialogs as power menus. */
final class PowerMenuGuard {
    interface Queue {void after(long milliseconds,Runnable callback);}
    private final Queue queue;private final BooleanSupplier allowed,dismiss;private final Runnable returnToAlarm;
    private final LongSupplier clock;private long generation,lastAttempt=-1;private int observedWindow=-1;private String observedPackage;private boolean closed;
    PowerMenuGuard(Queue queue,BooleanSupplier allowed,BooleanSupplier dismiss,Runnable returnToAlarm,LongSupplier clock){this.queue=queue;this.allowed=allowed;this.dismiss=dismiss;this.returnToAlarm=returnToAlarm;this.clock=clock;}
    static boolean systemPackage(String name){return "android".equals(name)||"com.android.systemui".equals(name)||"com.samsung.android.systemui".equals(name);}
    static boolean activeAlarm(boolean serviceActive,boolean ringing,int id){return serviceActive&&ringing&&id>0;}
    static boolean powerWindow(String packageName,String className){
        if(packageName==null||className==null)return false;
        if(!systemPackage(packageName))return false;
        String name=className.toLowerCase(Locale.ROOT);
        if(name.contains("emergency")||name.contains("sos")||name.contains("incall")||name.contains("dialer"))return false;
        return name.equals("com.android.systemui.globalactions.globalactionsdialoglite$actionsdialog")
            ||name.equals("com.android.systemui.globalactions.globalactionsdialog$actionsdialog")
            ||name.equals("com.android.systemui.globalactions.globalactionsdialog")
            ||name.equals("com.android.internal.globalactions.globalactionsdialog")
            ||name.equals("com.android.server.policy.globalactions")
            ||name.equals("com.android.internal.policy.impl.globalactions")
            ||name.equals("com.samsung.android.globalactions.presentation.view.samsungglobalactionsdialogbase$actionsdialog");
    }
    void window(String packageName,String className,int windowId){
        observedWindow=windowId;observedPackage=packageName;long request=++generation;if(closed||!powerWindow(packageName,className)||!allowed.getAsBoolean())return;
        long now=clock.getAsLong();if(lastAttempt>=0&&now-lastAttempt<500)return;
        // Give a newly reported menu time to take focus. Metadata cannot prove current input focus.
        queue.after(150,()->{if(closed||request!=generation||windowId>=0&&observedWindow>=0&&observedWindow!=windowId||!allowed.getAsBoolean())return;lastAttempt=clock.getAsLong();if(dismiss.getAsBoolean()&&allowed.getAsBoolean())returnToAlarm.run();});
    }
    void focus(String packageName,int windowId){if(!java.util.Objects.equals(packageName,observedPackage)||windowId>=0&&observedWindow>=0&&windowId!=observedWindow)cancel();observedPackage=packageName;observedWindow=windowId;}
    void cancel(){generation++;}
    void close(){closed=true;cancel();}
}
