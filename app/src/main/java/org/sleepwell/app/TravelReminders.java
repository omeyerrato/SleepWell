package org.sleepwell.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import java.time.*;
import java.util.function.LongSupplier;

/** Silent inexact notifications. All Android scheduling/output passes through the injected port. */
final class TravelReminders {
    static final String ACTION="org.sleepwell.app.TRAVEL_LIGHT",CHANNEL="travel_daylight_v1";
    static final int REQUEST=740,NOTIFICATION=740;
    static LongSupplier clock=System::currentTimeMillis;
    static final class Request{final long revision,at;Request(long revision,long at){this.revision=revision;this.at=at;}String token(){return "travel:"+revision+":"+at;}}
    interface Port{void schedule(Context c,Request request);void cancel(Context c,Request request);void dismiss(Context c);boolean allowed(Context c);void show(Context c,TravelPlan plan,Request request);}
    static final Port PLATFORM=new Port(){
        private PendingIntent pending(Context c,Request r,int flags){Intent intent=new Intent(c,TravelReminderReceiver.class).setAction(ACTION).setIdentifier(r.token()).putExtra("revision",r.revision).putExtra("at",r.at);return PendingIntent.getBroadcast(c,REQUEST,intent,flags|PendingIntent.FLAG_IMMUTABLE);}
        public void schedule(Context c,Request r){channel(c);c.getSystemService(AlarmManager.class).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,r.at,pending(c,r,PendingIntent.FLAG_UPDATE_CURRENT));}
        public void cancel(Context c,Request r){if(r==null)return;PendingIntent intent=pending(c,r,PendingIntent.FLAG_NO_CREATE);if(intent!=null){c.getSystemService(AlarmManager.class).cancel(intent);intent.cancel();}}
        public void dismiss(Context c){c.getSystemService(NotificationManager.class).cancel(NOTIFICATION);}
        public boolean allowed(Context c){if(Build.VERSION.SDK_INT>=33&&c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return false;NotificationManager manager=c.getSystemService(NotificationManager.class);NotificationChannel ch=manager.getNotificationChannel(CHANNEL);return manager.areNotificationsEnabled()&&(ch==null||ch.getImportance()!=NotificationManager.IMPORTANCE_NONE);}
        public void show(Context c,TravelPlan p,Request r){channel(c);PendingIntent open=PendingIntent.getActivity(c,REQUEST,new Intent(c,TravelActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);Notification notification=new androidx.core.app.NotificationCompat.Builder(c,CHANNEL).setSmallIcon(Icons.resource("sun")).setContentTitle("Your daylight reminder").setContentText("A time you chose for "+TravelPlan.city(p.destination)+". Open your travel plan.").setContentIntent(open).setSilent(true).setOnlyAlertOnce(true).setAutoCancel(true).build();c.getSystemService(NotificationManager.class).notify(NOTIFICATION,notification);}
    };
    static Port port=PLATFORM;
    static void channel(Context c){NotificationChannel channel=new NotificationChannel(CHANNEL,"Travel daylight reminders",NotificationManager.IMPORTANCE_DEFAULT);channel.setDescription("User-chosen destination-time reminders; no sound, vibration or flashlight.");channel.setSound(null,null);channel.enableVibration(false);c.getSystemService(NotificationManager.class).createNotificationChannel(channel);}
    static void apply(Context c){apply(c,true);}
    private static void apply(Context c,boolean dismiss){synchronized(TravelPlans.LOCK){TravelPlans.Repository repo=TravelPlans.repository(c);TravelPlan p=repo.load(clock.getAsLong());Request previous=repo.pending();
        try{repo.pending(null,"");port.cancel(c,previous);if(dismiss)port.dismiss(c);long next=p.next(clock.getAsLong(),repo.delivered());if(next>0){Request request=new Request(p.revision,next);repo.pending(request,"");port.schedule(c,request);}repo.prefs.edit().remove("travelDeliveryError").apply();}
        catch(Exception error){String message=error.getMessage()==null?"Reminder scheduling is unavailable.":error.getMessage();repo.pending(null,message);throw new IllegalStateException(message,error);}
    }}
    static void receive(Context c,Request incoming){synchronized(TravelPlans.LOCK){TravelPlans.Repository repo=TravelPlans.repository(c);TravelPlan p=repo.load(clock.getAsLong());Request pending=repo.pending();if(pending==null||pending.revision!=p.revision)return;
        TravelReminderPolicy.Decision decision=TravelReminderPolicy.due(p.reminders,p.revision,pending.at,incoming.revision,incoming.at,repo.delivered(),clock.getAsLong());if(decision==TravelReminderPolicy.Decision.IGNORE)return;
        // Consume before output: duplicate broadcasts cannot repost. A process interruption can skip a cue.
        repo.consumed(incoming.at,"Reminder processed");String notice;
        if(decision==TravelReminderPolicy.Decision.SKIP_LATE)notice="A delayed reminder was skipped.";
        else if(!port.allowed(c))notice="A reminder was skipped because notifications were blocked.";
        else try{port.show(c,p,incoming);notice="Your last daylight reminder was shown.";}catch(Exception error){notice="Your last reminder could not be shown.";}
        repo.prefs.edit().putString("travelLastNotice",notice).apply();apply(c,false);
    }}
}
