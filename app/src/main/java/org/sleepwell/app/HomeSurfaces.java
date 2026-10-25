package org.sleepwell.app;

import android.appwidget.AppWidgetManager;
import android.content.*;
import android.widget.RemoteViews;
import java.time.ZoneId;
import java.util.concurrent.*;
import java.util.*;

/** At most one reading task and one coalesced follow-up. No timers/exact alarms or capture services. */
final class HomeSurfaces {
    interface Host {int[] ids(Context context);void update(Context context,int id,RemoteViews views);}
    interface Reader {HomeWidgetSummary load(Context context,Records.Factory records);}
    static Reader reader=(context,records)->{boolean access;try{access=Scheduler.allowed(context);}catch(RuntimeException denied){access=false;}return HomeWidgetData.load(context,records,Prefs.of(context),System.currentTimeMillis(),ZoneId.systemDefault(),access);};
    private static final Host PLATFORM=new Host(){public int[] ids(Context context){return AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context,HomeWidgetProvider.class));}public void update(Context context,int id,RemoteViews views){AppWidgetManager.getInstance(context).updateAppWidget(id,views);}};
    static Host host=PLATFORM;
    private static final ScheduledExecutorService worker=Executors.newSingleThreadScheduledExecutor(r->new Thread(r,"SleepWell home summary"));
    private static boolean queued,dirty;private static Context latest;private static Records.Factory records;private static Host output;private static Reader source;private static final List<Runnable> completions=new ArrayList<>();
    static void changed(Context context){SRIReviews.changed(context);SmartBedtimeRuntime.changed(context);request(context,null);}
    static void request(Context context,Runnable complete){try{synchronized(HomeSurfaces.class){latest=context.getApplicationContext();records=Records.factory;output=host;source=reader;dirty=true;if(complete!=null){if(completions.size()<16)completions.add(complete);else complete.run();}if(!queued){queued=true;worker.schedule(HomeSurfaces::read,250,TimeUnit.MILLISECONDS);}}}catch(RuntimeException failure){if(complete!=null)complete.run();}}
    private static void read(){Context context;Records.Factory pinned;Host host;Reader reader;List<Runnable> done;synchronized(HomeSurfaces.class){context=latest;pinned=records;host=output;reader=source;dirty=false;done=new ArrayList<>(completions);completions.clear();}
        try{if(host==PLATFORM&&pinned!=Records.PRODUCTION)return;int[] ids=host.ids(context);if(ids.length>0){HomeWidgetSummary data=reader.load(context,pinned);for(int id:ids)host.update(context,id,HomeWidgetViews.render(context,data));}}catch(RuntimeException ignored){}finally{for(Runnable completion:done)try{completion.run();}catch(RuntimeException ignored){}synchronized(HomeSurfaces.class){if(dirty)worker.schedule(HomeSurfaces::read,250,TimeUnit.MILLISECONDS);else queued=false;}}
    }
    static void drain(long timeout)throws Exception{long end=System.nanoTime()+TimeUnit.MILLISECONDS.toNanos(timeout);for(;;){synchronized(HomeSurfaces.class){if(!queued)return;}if(System.nanoTime()>=end)throw new TimeoutException("Home summary worker still busy");Thread.sleep(10);}}
}
