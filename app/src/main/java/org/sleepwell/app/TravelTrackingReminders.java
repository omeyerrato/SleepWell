package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.*;
import java.time.*;
import java.util.concurrent.atomic.AtomicLong;
import org.json.*;

/** Independent silent opt-in. Never starts recording or calls the flashlight controller. */
final class TravelTrackingReminders {
    static final String ACTION="org.sleepwell.app.TRAVEL_TRACKING_REMINDER";
    static final String PENDING="travelTrackingPending",CONSUMED="travelTrackingConsumed",ERROR="travelTrackingReminderError",NOTICE="travelTrackingLastNotice";
    static final int REQUEST=748,NOTIFICATION=748;
    static final long MAX_LATE=60000;
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private static final AtomicLong generation=new AtomicLong();
    interface Recording {TravelCues.Manual active();}
    static Recording recording=()->TravelCues.control.manual();
    static final class Request {
        final long trip,cue,start,end;
        Request(long trip,long cue,long start,long end){if(trip<=0||cue<=0||start<=0||end<=start||end-start>3600000)throw new IllegalArgumentException("Invalid tracking reminder identity");this.trip=trip;this.cue=cue;this.start=start;this.end=end;}
        String token(){return "travel-tracking:"+trip+":"+cue+":"+start+":"+end;}
        String json(){try{return new JSONObject().put("version",1).put("trip",trip).put("cue",cue).put("start",start).put("end",end).toString();}catch(JSONException e){throw new IllegalStateException(e);}}
        static Request parse(String raw){if(raw==null||raw.length()>512)throw new IllegalArgumentException("Invalid tracking reminder");try{JSONObject j=new JSONObject(raw);if(j.getInt("version")!=1)throw new JSONException("Unknown identity version");return new Request(number(j,"trip"),number(j,"cue"),number(j,"start"),number(j,"end"));}catch(JSONException e){throw new IllegalArgumentException("Invalid tracking reminder",e);}}
        private static long number(JSONObject j,String key)throws JSONException{Object v=j.get(key);if(!(v instanceof Number))throw new JSONException("Expected integer");double d=((Number)v).doubleValue();long n=((Number)v).longValue();if(!Double.isFinite(d)||d!=n||n<=0||n>9007199254740991L)throw new JSONException("Invalid identity integer");return n;}
    }
    interface Port {void schedule(Context c,Request r);void cancel(Context c,Request r);void dismiss(Context c);boolean allowed(Context c);void show(Context c,Request r);}
    static final Port PLATFORM=new Port(){
        private PendingIntent pending(Context c,Request r,int flags){return PendingIntent.getBroadcast(c,REQUEST,new Intent(c,TravelTrackingReminderReceiver.class).setAction(ACTION).setIdentifier(r.token()).putExtra("request",r.json()),flags|PendingIntent.FLAG_IMMUTABLE);}
        public void schedule(Context c,Request r){TravelReminders.channel(c);c.getSystemService(AlarmManager.class).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,r.start,pending(c,r,PendingIntent.FLAG_UPDATE_CURRENT));}
        public void cancel(Context c,Request r){if(r==null)return;PendingIntent p=pending(c,r,PendingIntent.FLAG_NO_CREATE);if(p!=null){c.getSystemService(AlarmManager.class).cancel(p);p.cancel();}}
        public void dismiss(Context c){c.getSystemService(NotificationManager.class).cancel(NOTIFICATION);}
        public boolean allowed(Context c){return TravelReminders.port.allowed(c);}
        public void show(Context c,Request r){TravelReminders.channel(c);PendingIntent open=PendingIntent.getActivity(c,REQUEST,new Intent(c,TravelActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);Notification n=new androidx.core.app.NotificationCompat.Builder(c,TravelReminders.CHANNEL).setSmallIcon(Icons.resource("sun")).setContentTitle("Your saved light-cue window").setContentText("Your manual recording is active. Open Travel to review light cues; the flashlight is not armed by this reminder.").setContentIntent(open).setSilent(true).setOnlyAlertOnce(true).setAutoCancel(true).setTimeoutAfter(Math.max(1,r.end-TravelReminders.clock.getAsLong())).build();c.getSystemService(NotificationManager.class).notify(NOTIFICATION,n);}
    };
    static Port port=PLATFORM;
    static void validate(TravelPlan trip,TravelCuePlan cues,long now){if(!trip.trackingReminders)return;if(!cues.enabled||!cues.reviewed)throw new IllegalArgumentException("Enable and review a light-cue window, or turn tracking reminders off.");if(future(cues,now)==null)throw new IllegalArgumentException("Choose a future light-cue window, or turn tracking reminders off.");}
    private static TravelCuePlan.Window future(TravelCuePlan cues,long now){for(int day=0;day<cues.nights;day++){java.time.LocalDate date=cues.first.plusDays(day);long start=date.atTime(cues.minute/60,cues.minute%60).atZone(ZoneId.of(cues.home)).toInstant().toEpochMilli();if(start>now)return new TravelCuePlan.Window(start,Math.addExact(start,cues.minutes*60000L),date);}return null;}
    static Request next(TravelPlan trip,TravelCuePlan cues,long now){if(!trip.trackingReminders||!cues.enabled||!cues.reviewed||cues.tripRevision!=trip.revision)return null;cues.validateTrip(trip);TravelCuePlan.Window window=future(cues,now);return window==null?null:new Request(trip.revision,cues.revision,window.start,window.end);}
    static Request pending(TravelPlans.Repository repo){String raw=repo.prefs.getString(PENDING,"");return raw.isEmpty()?null:Request.parse(raw);}
    private static void commit(SharedPreferences.Editor edit){if(!edit.commit())throw new IllegalStateException("Tracking reminder state could not be stored.");}
    static void apply(Context c){generation.incrementAndGet();MAIN.post(()->dismiss(c));synchronized(TravelPlans.LOCK){TravelPlans.Repository repo=TravelPlans.repository(c);try{Request old=pending(repo);commit(repo.prefs.edit().remove(PENDING).remove(ERROR));if(old!=null)port.cancel(c,old);scheduleNext(c,repo);}catch(RuntimeException error){repo.prefs.edit().putString(ERROR,"Tracking reminders need attention: "+message(error)).apply();throw error;}}}
    private static void scheduleNext(Context c,TravelPlans.Repository repo){TravelPlan trip=repo.load(TravelReminders.clock.getAsLong());if(!trip.trackingReminders)return;TravelCuePlan cues=new TravelCueStore(repo.prefs).load(trip,TravelReminders.clock.getAsLong());long after=TravelReminders.clock.getAsLong();String consumed=repo.prefs.getString(CONSUMED,"");if(!consumed.isEmpty()){String[] identity=consumed.split(":");if(identity.length==5)try{if(Long.parseLong(identity[1])==trip.revision&&Long.parseLong(identity[2])==cues.revision)after=Math.max(after,Long.parseLong(identity[3]));}catch(NumberFormatException invalid){throw new IllegalStateException("The tracking reminder receipt could not be read.");}}Request next=next(trip,cues,after);if(next==null)return;if(!port.allowed(c))throw new IllegalStateException("Allow Travel reminder notifications in Android settings.");commit(repo.prefs.edit().putString(PENDING,next.json()));try{port.schedule(c,next);}catch(RuntimeException error){commit(repo.prefs.edit().remove(PENDING));throw error;}}
    // Called before the recording phase changes, so even an asynchronously pausing sonar run is fenced.
    static void recordingEnded(Context c){generation.incrementAndGet();if(Looper.myLooper()==Looper.getMainLooper())dismiss(c);else MAIN.post(()->dismiss(c));}
    private static void dismiss(Context c){try{port.dismiss(c);}catch(RuntimeException error){String notice="Your previous tracking reminder could not be dismissed. Clear it from Android notifications.";try{TravelPlans.repository(c).prefs.edit().putString(ERROR,notice).putString(NOTICE,notice).apply();}catch(RuntimeException unavailable){android.util.Log.e("SleepWell","Could not store tracking reminder cleanup error",unavailable);}}}
    static void receive(Context c,Request incoming,Runnable finished){Context app=c.getApplicationContext();MAIN.post(()->{long fence=generation.get();TravelCues.Manual active;try{active=recording.active();}catch(RuntimeException unavailable){active=null;}TravelCues.Manual owner=active;new Thread(()->{boolean publish=false;try{synchronized(TravelPlans.LOCK){TravelPlans.Repository repo=TravelPlans.repository(app);Request pending=pending(repo);TravelPlan trip=repo.load(TravelReminders.clock.getAsLong());TravelCuePlan cues=new TravelCueStore(repo.prefs).load(trip,TravelReminders.clock.getAsLong());long now=TravelReminders.clock.getAsLong();if(incoming.token().equals(repo.prefs.getString(CONSUMED,""))||pending==null||!pending.token().equals(incoming.token())||!matches(trip,cues,incoming)||now<incoming.start)return;
                    // Durable consumption precedes every output attempt; process death can skip, never replay.
                    commit(repo.prefs.edit().remove(PENDING).putString(CONSUMED,incoming.token()).putString(NOTICE,"The saved window was processed; no reminder was shown."));
                    publish=owner!=null&&now-incoming.start<=MAX_LATE&&now<incoming.end;
                    try{scheduleNext(app,repo);}catch(RuntimeException error){repo.prefs.edit().putString(ERROR,"The next tracking reminder could not be scheduled: "+message(error)).apply();}
                }}catch(RuntimeException error){TravelPlans.repository(app).prefs.edit().putString(ERROR,"Tracking reminder delivery needs attention: "+message(error)).apply();}finally{boolean eligible=publish;MAIN.post(()->{try{if(eligible)publish(app,incoming,owner,fence);}catch(RuntimeException error){TravelPlans.repository(app).prefs.edit().putString(ERROR,"Tracking reminder delivery needs attention: "+message(error)).apply();}finally{finished.run();}});}},"SleepWell-tracking-reminder").start();});}
    private static void publish(Context c,Request r,TravelCues.Manual owner,long fence){synchronized(TravelPlans.LOCK){TravelPlans.Repository repo=TravelPlans.repository(c);TravelPlan trip=repo.load(TravelReminders.clock.getAsLong());TravelCuePlan cues=new TravelCueStore(repo.prefs).load(trip,TravelReminders.clock.getAsLong());TravelCues.Manual current=recording.active();long now=TravelReminders.clock.getAsLong();if(fence!=generation.get()||current==null||owner.session!=current.session||!owner.token.equals(current.token)||!matches(trip,cues,r)||!r.token().equals(repo.prefs.getString(CONSUMED,""))||now<r.start||now-r.start>MAX_LATE||now>=r.end||!port.allowed(c))return;
            try{port.show(c,r);repo.prefs.edit().putString(NOTICE,"Your last tracking reminder was shown silently.").apply();}catch(RuntimeException error){repo.prefs.edit().putString(ERROR,"Your tracking reminder could not be shown.").apply();}
        }}
    private static boolean matches(TravelPlan trip,TravelCuePlan cues,Request r){if(!trip.trackingReminders||!cues.enabled||!cues.reviewed||trip.revision!=r.trip||cues.revision!=r.cue||cues.tripRevision!=r.trip)return false;try{cues.validateTrip(trip);for(int i=0;i<cues.nights;i++){long start=cues.first.plusDays(i).atTime(cues.minute/60,cues.minute%60).atZone(ZoneId.of(cues.home)).toInstant().toEpochMilli();if(start==r.start&&Math.addExact(start,cues.minutes*60000L)==r.end)return true;}return false;}catch(RuntimeException invalid){return false;}}
    private static String message(Exception e){return e.getMessage()==null?"Unavailable.":e.getMessage();}
    private TravelTrackingReminders(){}
}
