package org.sleepwell.app;

import android.app.*;
import android.app.usage.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import java.util.*;

/** Explicit Usage Access, queried on demand; package/app names are never copied or stored. */
final class MissedNightUsage implements MissedNightUsagePort {
    interface Factory {MissedNightUsagePort create(Context context);}
    static Factory factory=MissedNightUsage::new;
    private final Context app;
    MissedNightUsage(Context context){app=context.getApplicationContext();}
    public boolean allowed(){AppOpsManager ops=app.getSystemService(AppOpsManager.class);return ops!=null&&ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(),app.getPackageName())==AppOpsManager.MODE_ALLOWED;}
    public List<MissedNight.Event> query(long begin,long end){
        if(!allowed())throw new SecurityException("Usage Access is off. Allow it to review phone activity.");
        UserManager user=app.getSystemService(UserManager.class);if(user==null||!user.isUserUnlocked())throw new IllegalStateException("Unlock this phone before checking its activity.");
        UsageStatsManager manager=app.getSystemService(UsageStatsManager.class);if(manager==null)throw new IllegalStateException("Phone activity is unavailable on this device.");
        UsageEvents events=manager.queryEvents(begin,end);if(events==null)throw new IllegalStateException("Phone activity is unavailable. Unlock this phone and try again.");
        List<MissedNight.Event> out=new ArrayList<>();UsageEvents.Event event=new UsageEvents.Event();int count=0;
        while(events.hasNextEvent()){
            if(++count>200000)throw new IllegalStateException("This activity window is too large to review safely.");
            events.getNextEvent(event);MissedNight.Kind kind=switch(event.getEventType()){case UsageEvents.Event.SCREEN_NON_INTERACTIVE->MissedNight.Kind.OFF;case UsageEvents.Event.SCREEN_INTERACTIVE->MissedNight.Kind.ON;case UsageEvents.Event.DEVICE_SHUTDOWN,UsageEvents.Event.DEVICE_STARTUP->MissedNight.Kind.RESET;default->null;};
            if(kind!=null)out.add(new MissedNight.Event(event.getTimeStamp(),kind));
        }
        if(!allowed())throw new SecurityException("Usage Access changed. Check access and try again.");return out;
    }
    public void settings(Activity activity){activity.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS,Uri.parse("package:"+app.getPackageName())));}
}

interface MissedNightUsagePort {
    boolean allowed();
    List<MissedNight.Event> query(long begin,long end);
    void settings(Activity activity);
}
