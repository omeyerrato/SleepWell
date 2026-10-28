package org.sleepwell.app;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Looper;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONObject;

/** LIVE phone chart only. Exact RecordChart aggregation, with bounded, incremental worker-owned data. */
final class PhoneMotionChartReader {
    static final int PAGE=1024,MAX_SAMPLES=65536,MAX_PENDING=256,MAX_BARS=2048;
    interface Owner {boolean owns(long session,long start,String phase,RecordChart view);}
    static final class Data {
        final long start,end;final int readings,columns;final double high;final List<ChartSeries.Bar> bars;
        Data(long start,long end,int readings,int columns,List<ChartSeries.Bar> bars){this.start=start;this.end=end;this.readings=readings;this.columns=columns;this.bars=List.copyOf(bars);double maximum=.001;for(ChartSeries.Bar bar:bars)maximum=Math.max(maximum,bar.value);high=maximum;}
    }
    private static final class TooDetailed extends IllegalStateException {}
    private static final class Request {
        final long session,start,end,generation;final boolean paused,oversized;final String phase;final RecordChart view;final int columns;final List<ChartSeries.Sample> pending;
        Request(long session,long start,long end,String phase,boolean oversized,List<ChartSeries.Sample> pending,RecordChart view,long generation){this.session=session;this.start=start;this.end=Math.max(start+1,end);this.phase=phase;this.paused=phase.equals("PAUSED");this.oversized=oversized;this.pending=pending;this.view=view;this.generation=generation;columns=view.liveColumns();}
    }
    private final Activity owner;private final Context app;private final TrackingService.StoreFactory factory;private final Owner ownership;
    private final ExecutorService worker=Executors.newSingleThreadExecutor(r->new Thread(r,"SleepWell-phone-chart"));
    private final AtomicLong generation=new AtomicLong();private Request pending,last;private RecordChart target;private boolean running,closed,blocked;private volatile boolean resetCache;private String failureMessage;private long session=Long.MIN_VALUE,start=Long.MIN_VALUE,throughTime,throughId;private final List<ChartSeries.Sample> durable=new ArrayList<>();
    PhoneMotionChartReader(Activity owner,TrackingService.StoreFactory factory,Owner ownership){this.owner=owner;app=owner.getApplicationContext();this.factory=factory;this.ownership=ownership;}
    /** Called on main with the immutable runtime snapshot. JSON is copied into bounded immutable samples. */
    synchronized void request(long id,long from,long now,String phase,List<JSONObject> readings,RecordChart view){
        if(Looper.myLooper()!=Looper.getMainLooper())throw new IllegalStateException("Chart requests belong to the UI thread");
        if(closed||!view.isAttachedToWindow()||view.getWidth()<=0)return;
        if(last==null||last.session!=id||last.start!=from||!last.phase.equals(phase)){generation.incrementAndGet();blocked=false;}
        if(target!=view){generation.incrementAndGet();target=view;}
        boolean paused=phase.equals("PAUSED"),oversized=!paused&&readings.size()>MAX_PENDING;List<ChartSeries.Sample> copy=new ArrayList<>(paused||oversized?0:readings.size());
        if(!paused&&!oversized)for(JSONObject row:readings)copy.add(new ChartSeries.Sample(row.isNull("start")?null:row.optLong("start"),row.optLong("time"),row.isNull("movement")?null:row.optDouble("movement")));
        last=new Request(id,from,now,phase,oversized,List.copyOf(copy),view,generation.get());
        if(blocked){view.liveMessage(failureMessage,this::retry);return;}
        pending=last;if(!running){running=true;worker.execute(this::read);}
    }
    private synchronized void retry(){if(closed||last==null||target!=last.view||!current(last))return;blocked=false;resetCache=true;generation.incrementAndGet();last=new Request(last.session,last.start,last.end,last.phase,last.oversized,last.pending,last.view,generation.get());target.liveMessage("Opening movement history\u2026",null);pending=last;if(!running){running=true;worker.execute(this::read);}}
    synchronized void suspend(){generation.incrementAndGet();pending=null;target=null;}
    synchronized void close(){closed=true;suspend();worker.shutdownNow();}
    void awaitClosed(long millis)throws Exception{if(!worker.awaitTermination(millis,TimeUnit.MILLISECONDS))throw new TimeoutException("Phone chart reader still active");}
    private boolean current(Request r){return !closed&&generation.get()==r.generation&&target==r.view&&!owner.isDestroyed()&&!owner.isFinishing()&&owner.hasWindowFocus()&&r.view.isAttachedToWindow()&&ownership.owns(r.session,r.start,r.phase,r.view);}
    private void check(Request r){if(Thread.currentThread().isInterrupted()||generation.get()!=r.generation)throw new CancellationException();}
    private void read(){while(true){Request r;synchronized(this){r=pending;pending=null;if(closed||r==null){running=false;return;}}
        try{check(r);Data data=load(r);check(r);owner.runOnUiThread(()->{synchronized(this){if(current(r)&&r.columns==r.view.liveColumns())r.view.liveData(data);}});}
        catch(CancellationException cancelled){/* Detached/paused/replaced requests cannot publish. Durable prefix stays reusable. */}
        catch(Exception failure){String message=failure instanceof TooDetailed?"Movement history is too detailed to display. Tap to retry.":"Movement history unavailable. Tap to retry.";synchronized(this){if(generation.get()!=r.generation)continue;blocked=true;failureMessage=message;pending=null;}owner.runOnUiThread(()->{synchronized(this){if(current(r))r.view.liveMessage(message,this::retry);}});}
    }}
    private Data load(Request r){
        if(r.oversized)throw new TooDetailed();
        if(resetCache||session!=r.session||start!=r.start||throughTime>r.end){session=r.session;start=r.start;throughTime=start;throughId=Long.MIN_VALUE;durable.clear();resetCache=false;}
        if(r.session<=0)throw new IllegalStateException("Recording owner unavailable");
        try(Store store=factory.open(app)){
            try(Cursor rows=store.getReadableDatabase().rawQuery("SELECT start FROM sessions WHERE id=?",new String[]{""+r.session})){if(!rows.moveToFirst()||rows.getLong(0)!=r.start)throw new IllegalStateException("Recording owner changed");}
            // (session,time) is indexed; rowid breaks ties so equal timestamps are not dropped between pages.
            while(true){check(r);int count=0;try(Cursor rows=store.getReadableDatabase().rawQuery("SELECT id,start,time,movement FROM epochs WHERE session=? AND (time,id)>(?,?) AND time<=? ORDER BY time,id LIMIT 1024",new String[]{""+r.session,""+throughTime,""+throughId,""+r.end})){
                while(rows.moveToNext()){if((count++&127)==0)check(r);if(durable.size()>=MAX_SAMPLES)throw new TooDetailed();long time=rows.getLong(2);durable.add(new ChartSeries.Sample(rows.isNull(1)?null:rows.getLong(1),time,rows.isNull(3)?null:rows.getDouble(3)));throughTime=time;throughId=rows.getLong(0);}
            }if(count<PAGE)break;}
        }
        check(r);List<ChartSeries.Sample> all=new ArrayList<>(durable);
        // A checkpoint can become durable between snapshot and worker read. Keep its pending tail exactly once.
        for(ChartSeries.Sample sample:r.pending){if(sample.time<=throughTime)continue;Long from=sample.from==null?null:Math.max(throughTime,sample.from);if(all.size()>=MAX_SAMPLES)throw new TooDetailed();all.add(new ChartSeries.Sample(from,sample.time,sample.value));}
        int measured=0;for(ChartSeries.Sample sample:all)if(sample.value!=null&&Double.isFinite(sample.value)&&sample.value>=0&&sample.time>=r.start&&sample.time<=r.end)measured++;
        List<ChartSeries.Bar> bars=ChartSeries.aggregate(all,r.start,r.end,r.columns);check(r);if(bars.size()>MAX_BARS)throw new TooDetailed();return new Data(r.start,r.end,measured,r.columns,bars);
    }
}
