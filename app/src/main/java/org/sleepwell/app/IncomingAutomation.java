package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.os.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

/** SleepWell v1 foreground-reviewed requests. No vendor-action aliases or unattended control. */
final class IncomingAutomation {
 static final String ACTION="org.sleepwell.app.action.TRACKING_REQUEST",DISPATCH="incomingAutomationDispatch",START_NOTICE="incomingAutomationStartReview";
 static final long REVIEW_TTL=120000,DISPATCH_TTL=10000;
 static final String PROCESS=UUID.randomUUID().toString();
 static final Set<String> COMMANDS=Set.of("START","PAUSE","RESUME","STOP");
 static final ExecutorService worker=Executors.newSingleThreadExecutor(r->new Thread(r,"Incoming tracking requests"));
 static final class Request {final String id,command;Request(String id,String command){this.id=id;this.command=command;}}
 static Request parse(Context c,Intent intent){try{
  if(intent==null||!ACTION.equals(intent.getAction()))throw new IllegalArgumentException("Unsupported action. Use the SleepWell tracking-request action.");
  int allowedFlags=Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT|Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY;
  if((intent.getFlags()&~allowedFlags)!=0||intent.getData()!=null||intent.getType()!=null||intent.getClipData()!=null||intent.getSelector()!=null)throw new IllegalArgumentException("This request includes unsupported data, flags or a nested intent.");
  if(intent.getPackage()!=null&&!c.getPackageName().equals(intent.getPackage()))throw new IllegalArgumentException("The request targets another package.");
  ComponentName component=intent.getComponent();if(component!=null&&(!component.getPackageName().equals(c.getPackageName())||!component.getClassName().equals(IncomingAutomationActivity.class.getName())))throw new IllegalArgumentException("Unsupported request component.");
  Set<String> categories=intent.getCategories();if(categories!=null&&categories.stream().anyMatch(category->!Intent.CATEGORY_DEFAULT.equals(category)))throw new IllegalArgumentException("Unsupported request category.");
  Bundle values=intent.getExtras();if(values==null||values.size()!=3||!values.keySet().equals(Set.of("version","command","request_id")))throw new IllegalArgumentException("Use only version, command and request_id.");
  Object version=values.get("version"),command=values.get("command"),id=values.get("request_id");if(!(version instanceof Integer)||((Integer)version)!=1)throw new IllegalArgumentException("Only integer version 1 is supported.");
  if(!(command instanceof String)||!COMMANDS.contains(command)||!(id instanceof String)||((String)id).length()!=36||!UUID.fromString((String)id).toString().equals(id))throw new IllegalArgumentException("Use START, PAUSE, RESUME or STOP and a canonical UUID request_id.");
  return new Request((String)id,(String)command);
 }catch(android.os.BadParcelableException|ClassCastException bad){throw new IllegalArgumentException("The request contains unsupported parcelled data.");}}
 static final class Settings {final boolean enabled;final long revision;final String issue;Settings(boolean enabled,long revision,String issue){this.enabled=enabled;this.revision=revision;this.issue=issue;}}
 private static SharedPreferences prefs(Context c){return c.getApplicationContext().getSharedPreferences("incoming_automation_v1",Context.MODE_PRIVATE);}
 static Settings settings(Context c){try{SharedPreferences p=prefs(c);long r=p.getLong("revision",0);if(r<0)throw new IllegalStateException();return new Settings(p.getBoolean("enabled",false),r,"");}catch(RuntimeException invalid){return new Settings(false,-1,"Incoming request settings are unreadable; requests are disabled.");}}
 static synchronized void saveSettings(Context c,boolean enabled,long expected,String token){SharedPreferences p=prefs(c);if(token.equals(p.getString("savedRequest","")))return;Settings s=settings(c);if(s.revision<0||s.revision!=expected||s.revision==Long.MAX_VALUE)throw new IllegalStateException("Settings changed. Reopen before saving.");if(!p.edit().putBoolean("enabled",enabled).putLong("revision",s.revision+1).putString("savedRequest",token).commit())throw new IllegalStateException("Incoming request settings could not be saved.");}
 static boolean unlocked(Activity activity){KeyguardManager keyguard=activity.getSystemService(KeyguardManager.class);PowerManager power=activity.getSystemService(PowerManager.class);return keyguard!=null&&!keyguard.isKeyguardLocked()&&!keyguard.isDeviceLocked()&&power!=null&&power.isInteractive();}
 static boolean alarmBusy(Context c){return AlarmService.active||Prefs.flag(c,"ringing");}
 static final class Identity {
  final long id,start,admission;final boolean sonar;final String phase,delay;
  Identity(TrackingService.Snapshot s){this(s.id,s.start,s.sonar,s.phase,s.delayOwner,BackupRuntime.generation());}
  Identity(long id,long start,boolean sonar,String phase,String delay,long admission){this.id=id;this.start=start;this.sonar=sonar;this.phase=phase;this.delay=delay;this.admission=admission;}
  boolean same(TrackingService.Snapshot s,boolean requirePhase){return s!=null&&admission!=Long.MAX_VALUE&&admission==BackupRuntime.generation()&&s.id==id&&s.start==start&&s.sonar==sonar&&Objects.equals(delay,s.delayOwner)&&(!requirePhase||phase.equals(s.phase));}
 }
 static final class Grant {
  final Request request;final Identity identity;final String token=UUID.randomUUID().toString(),owner;final long issued,revision;final BooleanSupplier visible;boolean accepted,revoked;String issue="";
  Grant(Request request,Identity identity,String owner,long revision,BooleanSupplier visible){this.request=request;this.identity=identity;this.owner=owner;this.revision=revision;this.visible=visible;issued=SystemClock.elapsedRealtime();}
 }
 private static final Map<String,Grant> grants=new HashMap<>();
 static synchronized Grant grant(Context c,Request request,Identity identity,String owner,long revision,BooleanSupplier visible){grants.values().removeIf(g->g.revoked||SystemClock.elapsedRealtime()-g.issued>DISPATCH_TTL);if(grants.size()>=32)throw new IllegalStateException("Too many pending requests. Try again later.");Settings s=settings(c);if(!s.enabled||s.revision!=revision)throw new IllegalStateException("Incoming request access changed.");Grant grant=new Grant(request,identity,owner,revision,visible);grants.put(grant.token,grant);return grant;}
 static synchronized void revoke(Grant grant){if(grant!=null&&!grant.accepted){grant.revoked=true;grants.remove(grant.token);}}
 /** Root hooks this at TrackingService entry before STOP/PAUSE/RESUME branches, on its main thread. */
 static synchronized boolean accept(Context context,Intent command){Object raw;try{raw=command.getExtras()==null?null:command.getExtras().get(DISPATCH);}catch(RuntimeException malformed){return false;}if(!(raw instanceof String))return false;Grant g=grants.get((String)raw);if(g==null||g.accepted||g.revoked)return false;grants.remove(g.token);g.revoked=true;
  try{String action=g.request.command;long now=SystemClock.elapsedRealtime();Settings settings=settings(context);TrackingService.Snapshot current=TrackingService.snapshot();KeyguardManager keyguard=context.getSystemService(KeyguardManager.class);PowerManager power=context.getSystemService(PowerManager.class);
  boolean valid=g.visible.getAsBoolean()&&action.equals(command.getAction())&&!action.equals("START")&&now>=g.issued&&now-g.issued<=DISPATCH_TTL&&settings.enabled&&settings.revision==g.revision&&g.identity!=null&&g.identity.same(current,true)&&TrackingService.running&&!alarmBusy(context)&&keyguard!=null&&!keyguard.isKeyguardLocked()&&!keyguard.isDeviceLocked()&&power!=null&&power.isInteractive();
  if(!valid){g.issue="The confirmed recording, access or unlocked state changed before delivery.";return false;}g.accepted=true;g.revoked=false;return true;}catch(RuntimeException unavailable){g.issue="The visible recording request could not be validated at service delivery.";return false;}
 }
 static boolean accept(Intent command,Context context){return accept(context,command);}
 static final class Work {volatile boolean done,created;volatile String state="",error="";}
 static Work inspect(Context c,Request request){Work w=new Work();Context app=c.getApplicationContext();worker.execute(()->{try(Ledger ledger=new Ledger(app)){w.state=ledger.inspect(request);}catch(RuntimeException failure){w.error="The private request journal could not be read.";}finally{w.done=true;}});return w;}
 static Work prepare(Context c,Request request,String owner){Work w=new Work();Context app=c.getApplicationContext();worker.execute(()->{try(Ledger ledger=new Ledger(app)){w.state=ledger.inspect(request);if(w.state.isEmpty()){ledger.prepare(request,owner);w.created=true;w.state="prepared";}}catch(RuntimeException failure){w.error="The request could not be recorded safely; no command was sent.";}finally{w.done=true;}});return w;}
 static void record(Context c,Request request,String owner,String state){Context app=c.getApplicationContext();worker.execute(()->{try(Ledger ledger=new Ledger(app)){ledger.finish(request,owner,state);}catch(RuntimeException ignored){/* PREPARED remains non-replayable if final receipt persistence fails. */}});}
 private static final class Ledger extends SQLiteOpenHelper {
  Ledger(Context c){super(c,"incoming_automation_requests_v1.db",null,1);}
  @Override public void onCreate(SQLiteDatabase db){db.execSQL("CREATE TABLE requests(id TEXT PRIMARY KEY,command TEXT NOT NULL,owner TEXT NOT NULL,state TEXT NOT NULL,created INTEGER NOT NULL,updated INTEGER NOT NULL)");}
  @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){throw new IllegalStateException("Request journal version is unavailable.");}
  String inspect(Request request){try(Cursor c=getReadableDatabase().query("requests",new String[]{"command","state"},"id=?",new String[]{request.id},null,null,null)){if(!c.moveToFirst())return "";return request.command.equals(c.getString(0))?c.getString(1):"request_id_conflict";}}
  void prepare(Request request,String owner){SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{if(!inspect(request).isEmpty())throw new IllegalStateException("The request was already prepared.");try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM requests",null)){if(!c.moveToFirst()||c.getLong(0)>=4096)throw new IllegalStateException("Request history is full.");}ContentValues v=new ContentValues();v.put("id",request.id);v.put("command",request.command);v.put("owner",owner);v.put("state","prepared");v.put("created",System.currentTimeMillis());v.put("updated",System.currentTimeMillis());db.insertOrThrow("requests",null,v);db.setTransactionSuccessful();}finally{db.endTransaction();}}
  void finish(Request request,String owner,String state){ContentValues v=new ContentValues();v.put("state",state);v.put("updated",System.currentTimeMillis());getWritableDatabase().update("requests",v,"id=? AND command=? AND owner=?",new String[]{request.id,request.command,owner});}
 }
 static Intent result(Request request,String status){Intent result=new Intent().putExtra("version",1).putExtra("status",status);if(request!=null)result.putExtra("request_id",request.id).putExtra("command",request.command);return result;}
 static String verifiedCaller(Activity activity){String pkg=null;try{if(Build.VERSION.SDK_INT>=35){ComponentCaller caller=activity.getInitialCaller();if(caller.getUid()>=0&&caller.getPackage()!=null&&activity.getPackageManager().getPackageUid(caller.getPackage(),0)==caller.getUid())pkg=caller.getPackage();}if(pkg==null)pkg=activity.getCallingPackage();}catch(android.content.pm.PackageManager.NameNotFoundException|RuntimeException unavailable){pkg=null;}return pkg==null?"Android did not provide a verified source package":pkg+" · source package verified by Android";}
 private IncomingAutomation(){}
}