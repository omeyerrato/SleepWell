package org.sleepwell.app;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.*;
import android.os.*;
import android.view.*;
import org.json.JSONObject;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/** A recording's real wall-clock span. Unmeasured and paused time is never zero-filled. */
final class LiveMovementTimeline extends View {
    static final int MAX_COLUMNS=240;
    static final class Bar {
        final long from,to;final Double value;
        Bar(long from,long to,Double value){this.from=from;this.to=to;this.value=value;}
    }
    static final class Data {
        final long start,end,measuredMillis;final ZoneId zone;final List<Bar> bars;
        Data(long start,long end,long measured,ZoneId zone,List<Bar> bars){this.start=start;this.end=end;measuredMillis=measured;this.zone=zone;this.bars=List.copyOf(bars);}
        Double valueAt(long at){for(Bar bar:bars)if(at>=bar.from&&at<bar.to)return bar.value;return null;}
    }
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private Data data;private String message="Opening movement history…";private boolean failed;private Runnable retry;
    LiveMovementTimeline(Context c){super(c);setFocusable(true);setDefaultFocusHighlightEnabled(false);setContentDescription(message);}
    Data rendered(){return data;}
    private int columns(){return Math.max(1,Math.min(MAX_COLUMNS,(int)((getWidth()-Design.dp(getContext(),36))/(float)Math.max(1,Design.dp(getContext(),5)))));}
    private void show(Data value,String error,Runnable retry){
        data=value;failed=error!=null;message=failed?"Movement history unavailable. Tap to retry.":value==null?"Opening movement history…":"";this.retry=retry;setClickable(failed);
        long count=value==null?0:value.bars.stream().filter(bar->bar.value!=null).count();
        setContentDescription(failed?message:value==null?message:"Whole-night movement timeline. "+count+" measured columns from "+clock(value.start,value)+" to now. Pauses and missing signal are blank. Dense columns average measured time; columns containing a gap stay blank.");invalidate();
    }
    private String clock(long at,Data value){return ChartTicks.clock(at,value.start,value.end,value.zone,android.text.format.DateFormat.is24HourFormat(getContext()),Locale.getDefault(),false,false);}
    @Override public boolean performClick(){super.performClick();if(failed&&retry!=null){retry.run();return true;}return false;}
    @Override public boolean onTouchEvent(android.view.MotionEvent event){if(!failed)return super.onTouchEvent(event);if(event.getAction()==android.view.MotionEvent.ACTION_UP)performClick();return true;}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);float d=getResources().getDisplayMetrics().density,left=30*d,right=getWidth()-6*d,top=8*d,bottom=getHeight()-30*d;
        paint.setTypeface(Design.font(getContext(),400));paint.setTextSize(Design.sp(getContext(),10*Appearance.scale(getContext())));paint.setStyle(Paint.Style.FILL);paint.setTextAlign(Paint.Align.RIGHT);paint.setColor(NightDialTokens.SECONDARY);canvas.drawText("High",left-5*d,top+8*d,paint);canvas.drawText("Low",left-5*d,bottom,paint);
        paint.setColor(NightDialTokens.RULE);paint.setStrokeWidth(.7f*d);canvas.drawLine(left,bottom,right,bottom,paint);
        Data value=data;
        if(value!=null){
            List<ChartTicks.Tick> ticks=new ArrayList<>();ticks.add(new ChartTicks.Tick(value.start,clock(value.start,value),-1));
            if(value.end-value.start>=120000)for(ChartTicks.Tick tick:ChartTicks.candidates(value.start,value.end,value.zone,android.text.format.DateFormat.is24HourFormat(getContext()),Locale.getDefault(),5))if(tick.endpoint==0)ticks.add(tick);
            ticks.add(new ChartTicks.Tick(value.end,"Now",1));List<ChartTicks.Placed> placed=ChartTicks.layout(ticks,value.start,value.end,left,right,8*d,paint::measureText);
            for(ChartTicks.Placed tick:placed)if(tick.tick.endpoint==0){float x=left+(right-left)*(tick.tick.time-value.start)/(float)(value.end-value.start);canvas.drawLine(x,top,x,bottom,paint);}
            double high=.001;for(Bar bar:value.bars)if(bar.value!=null)high=Math.max(high,bar.value);
            for(Bar bar:value.bars)if(bar.value!=null){float x=left+(right-left)*(bar.from-value.start)/(float)(value.end-value.start),x2=left+(right-left)*(bar.to-value.start)/(float)(value.end-value.start),width=Math.max(d,(x2-x)*.75f),center=(x+x2)/2,y=bottom-(float)(bar.value/high)*(bottom-top);y=Math.min(y,bottom-1.5f*d);paint.setColor(0xffc5a367);canvas.drawRoundRect(center-width/2,y,center+width/2,bottom,3*d,3*d,paint);paint.setShader(null);}
            paint.setColor(NightDialTokens.SECONDARY);paint.setTextAlign(Paint.Align.CENTER);Paint.FontMetrics font=paint.getFontMetrics();float height=font.descent-font.ascent+2*d;
            for(ChartTicks.Placed tick:placed){String[] lines=tick.tick.label.split("\n");for(int n=0;n<lines.length;n++)canvas.drawText(lines[n],(float)tick.center,getHeight()-5*d-(lines.length-1-n)*height,paint);}
        }
        if(!message.isEmpty()||value!=null&&value.bars.stream().noneMatch(bar->bar.value!=null)){paint.setColor(NightDialTokens.SECONDARY);paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(Design.sp(getContext(),11*Appearance.scale(getContext())));String text=message.isEmpty()?"No measured movement yet":message;canvas.drawText(android.text.TextUtils.ellipsize(text,new android.text.TextPaint(paint),Math.max(1,right-left-8*d),android.text.TextUtils.TruncateAt.END).toString(),(left+right)/2,(top+bottom)/2,paint);}
    }

    /** One reader per Activity, with at most one queued replacement snapshot. Never opens the UI Store. */
    static final class Reader {
        private final Activity owner;private final Context app;private final TrackingService.StoreFactory factory;
        private final ExecutorService worker=Executors.newSingleThreadExecutor(r->new Thread(r,"SleepWell-live-timeline"));
        private final AtomicLong generation=new AtomicLong();private Request pending,last;private LiveMovementTimeline target;private boolean running,closed,blocked;private Cache cache;
        Reader(Activity owner,TrackingService.StoreFactory factory){this.owner=owner;app=owner.getApplicationContext();this.factory=factory;}
        synchronized void request(TrackingService.Snapshot snapshot,LiveMovementTimeline view){
            if(closed||!view.isAttachedToWindow()||view.getWidth()<=0)return;if(target!=view){generation.incrementAndGet();target=view;}
            if(last!=null&&(last.snapshot.id!=snapshot.id||last.snapshot.start!=snapshot.start))blocked=false;last=new Request(snapshot,view,view.columns(),generation.get());if(blocked){view.show(null,"unavailable",this::retry);return;}pending=last;if(!running){running=true;worker.execute(this::read);}
        }
        private synchronized void retry(){if(closed||last==null||target==null||target!=last.view||!target.isAttachedToWindow()||!owner.hasWindowFocus())return;blocked=false;cache=null;target.show(null,null,this::retry);pending=last;if(!running){running=true;worker.execute(this::read);}}
        synchronized void suspend(){generation.incrementAndGet();target=null;pending=null;}
        synchronized void close(){closed=true;suspend();worker.shutdownNow();}
        void awaitClosed(long timeout)throws Exception{if(!worker.awaitTermination(timeout,TimeUnit.MILLISECONDS))throw new TimeoutException("Live movement reader still active");}
        private void check(Request request){if(Thread.currentThread().isInterrupted()||generation.get()!=request.generation)throw new CancellationException();}
        private void read(){
            while(true){Request request;synchronized(this){request=pending;pending=null;if(closed||request==null){running=false;return;}}
                try{check(request);Data result=load(request);check(request);owner.runOnUiThread(()->{synchronized(Reader.this){if(closed||generation.get()!=request.generation||target!=request.view||!request.view.isAttachedToWindow()||!owner.hasWindowFocus())return;request.view.show(result,null,this::retry);}});}
                catch(CancellationException cancelled){/* Ownership changed; the next request keeps the same bounded durable cache. */}
                catch(Exception failure){synchronized(this){if(generation.get()!=request.generation)continue;blocked=true;pending=null;}owner.runOnUiThread(()->{synchronized(Reader.this){if(!closed&&generation.get()==request.generation&&target==request.view&&request.view.isAttachedToWindow())request.view.show(null,"unavailable",this::retry);}});}
            }
        }
        private Data load(Request request){
            TrackingService.Snapshot s=request.snapshot;long end=Math.max(s.start+1,s.now);if(cache==null||cache.id!=s.id||cache.start!=s.start||cache.sonar!=s.sonar)cache=new Cache(s.id,s.start,s.sonar);cache.fit(end);
            if(s.id<=0)return cache.display(end,request.columns);
            try(Store store=factory.open(app)){
                try(Cursor row=store.getReadableDatabase().rawQuery("SELECT start,zone FROM sessions WHERE id=?",new String[]{""+s.id})){if(!row.moveToFirst()||row.getLong(0)!=s.start)throw new IllegalStateException("Recording owner changed");String zone=row.getString(1);if(zone!=null&&!zone.isEmpty())cache.zone=ZoneId.of(zone);}
                // Bounded pages release the read cursor between batches so capture writes can continue.
                while(true){check(request);int count=0;try(Cursor rows=store.getReadableDatabase().rawQuery("SELECT start,time,movement,samples,signal_quality FROM epochs WHERE session=? AND time>? AND time<=? ORDER BY time LIMIT 2048",new String[]{""+s.id,""+cache.through,""+end})){
                    while(rows.moveToNext()){if((count++&127)==0)check(request);long to=rows.getLong(1),from=rows.isNull(0)?Math.max(s.start,to-60000):rows.getLong(0);Double value=rows.isNull(2)?null:rows.getDouble(2);boolean valid=value!=null&&Double.isFinite(value)&&value>=0&&(!s.sonar||value<=4&&"VALID".equals(rows.getString(4))&&rows.getInt(3)>0);cache.add(Math.max(cache.through,from),to,valid?value:null);cache.through=to;}
                }if(count<2048)break;}
                check(request);
                // Pause intervals are streamed into a bounded mask, rather than retained as a growing list.
                boolean[] mask=new boolean[cache.coverage.length];try(Cursor rows=store.getReadableDatabase().rawQuery("SELECT start,end FROM tracking_pauses WHERE session=? AND start<? ORDER BY start",new String[]{""+s.id,""+end})){while(rows.moveToNext()){check(request);long from=Math.max(s.start,rows.getLong(0)),to=rows.isNull(1)?end:Math.min(end,rows.getLong(1));cache.mask(mask,from,to);}}
                Cache visible=cache.copy();long after=cache.through;
                if(!s.paused()){
                    if(s.sonar){for(SonarFrame frame:s.sonarReadings){long from=Math.addExact(s.start,frame.from),to=Math.min(end,Math.addExact(s.start,frame.to));visible.add(Math.max(after,from),to,frame.quality==SonarFrame.Quality.VALID?frame.movement:null);after=Math.max(after,to);}}
                    else for(JSONObject frame:s.readings){long to=Math.min(end,frame.optLong("time")),from=frame.isNull("start")?to-60000:frame.optLong("start");Double value=frame.isNull("movement")?null:frame.optDouble("movement");visible.add(Math.max(after,from),to,value!=null&&Double.isFinite(value)&&value>=0?value:null);after=Math.max(after,to);}
                }
                for(int n=0;n<mask.length;n++)if(mask[n])visible.coverage[n]=0;return visible.display(end,request.columns);
            }
        }
        private static final class Request {final TrackingService.Snapshot snapshot;final LiveMovementTimeline view;final int columns;final long generation;Request(TrackingService.Snapshot s,LiveMovementTimeline view,int columns,long generation){snapshot=s;this.view=view;this.columns=columns;this.generation=generation;}}
    }
    /** Fixed memory. Cells containing any unmeasured time remain blank when displayed. */
    private static final class Cache {
        static final int CELLS=65536;final long id,start;final boolean sonar;long through,cell=1000;ZoneId zone=ZoneId.systemDefault();double[] mean=new double[CELLS];long[] coverage=new long[CELLS];
        Cache(long id,long start,boolean sonar){this.id=id;this.start=start;this.sonar=sonar;through=start;}
        void fit(long end){while((end-start-1)/cell>=CELLS){for(int n=0;n<CELLS/2;n++){long a=coverage[2*n],b=coverage[2*n+1];mean[n]=average(mean[2*n],a,mean[2*n+1],b);coverage[n]=a+b;}Arrays.fill(coverage,CELLS/2,CELLS,0);Arrays.fill(mean,CELLS/2,CELLS,0);cell*=2;}}
        Cache copy(){Cache copy=new Cache(id,start,sonar);copy.cell=cell;copy.through=through;copy.zone=zone;System.arraycopy(mean,0,copy.mean,0,mean.length);System.arraycopy(coverage,0,copy.coverage,0,coverage.length);return copy;}
        static double average(double a,long aDuration,double b,long bDuration){long duration=aDuration+bDuration;return duration==0?0:aDuration==0?b:bDuration==0?a:a+(b-a)*(bDuration/(double)duration);}
        void add(long from,long to,Double value){if(value==null||!Double.isFinite(value)||value<0||to<=from)return;from=Math.max(start,from);while(from<to){int n=(int)((from-start)/cell);if(n<0||n>=CELLS)throw new IllegalArgumentException("Movement interval outside chart bounds");long cut=Math.min(to,start+(n+1)*cell),duration=cut-from;mean[n]=average(mean[n],coverage[n],value,duration);coverage[n]+=duration;from=cut;}}
        void mask(boolean[] mask,long from,long to){if(to<=from)return;int first=(int)Math.max(0,(from-start)/cell),last=(int)Math.min(CELLS-1,(to-start-1)/cell);for(int n=first;n<=last;n++)mask[n]=true;}
        Data display(long end,int columns){
            int count=Math.max(1,Math.min(MAX_COLUMNS,columns));long step=Math.max(cell,(long)Math.ceil((end-start)/(double)count));int size=(int)((end-start-1)/step)+1;double[] values=new double[size];long[] covered=new long[size];long measured=0;
            int limit=(int)Math.min(CELLS,(end-start-1)/cell+1);for(int n=0;n<limit;n++){long from=start+n*cell,to=Math.min(end,from+cell);measured+=coverage[n];if(coverage[n]!=to-from)continue;while(from<to){int column=(int)((from-start)/step);long cut=Math.min(to,start+(column+1)*step),duration=cut-from;values[column]=average(values[column],covered[column],mean[n],duration);covered[column]+=duration;from=cut;}}
            List<Bar> bars=new ArrayList<>(size);for(int n=0;n<size;n++){long from=start+n*step,to=Math.min(end,from+step);bars.add(new Bar(from,to,covered[n]==to-from?values[n]:null));}return new Data(start,end,measured,zone,bars);
        }
    }
}
