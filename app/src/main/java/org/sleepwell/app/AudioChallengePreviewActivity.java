package org.sleepwell.app;

import android.app.*;import android.content.*;import android.os.*;import android.view.*;import android.widget.*;
import java.lang.ref.WeakReference;import java.util.concurrent.*;

/** Explicit listening preview. A retained cleanup owner never grants alarm completion. */
public final class AudioChallengePreviewActivity extends Activity {
 private static Session owned;
 private final Handler main=new Handler(Looper.getMainLooper());
 private final SpeakAccess.Native permissions=new SpeakAccess.Native();
 private AudioChallengeMode mode;private Session session;private boolean resumed,visible;private String issue="";
 @Override public void onCreate(Bundle b){super.onCreate(b);RecordUi.review(this);String chosen=getIntent().getStringExtra("type");if(!AudioChallengeMode.supported(chosen)){finish();return;}mode=AudioChallengeMode.from(chosen);if(owned!=null&&!owned.released()){session=owned;session.view=new WeakReference<>(this);issue="An earlier listening attempt is closing. Finish cleanup before another preview.";}if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::finish);render();}
 static Intent intent(Activity a,String type){return RecordUi.intent(a,AudioChallengePreviewActivity.class).putExtra("type",type);}
 private boolean current(){return resumed&&visible&&!isFinishing()&&!isDestroyed()&&!AlarmService.active&&!TrackingService.running&&!Prefs.flag(this,"ringing");}
 private void render(){if(isDestroyed()||mode==null)return;SpeakRuntime attempt=session==null?null:session.runtime;LinearLayout body=Design.page(this,false,null,n->{});RecordUi.header(body,mode.title+" preview",this::finish);Design.space(body,22);Design.text(body,mode.instruction,23,600,Design.INK);Design.space(body,14);Design.text(body,mode.detail(),14,400,Design.MUTED);Design.space(body,16);SpeakRuntime.State state=attempt==null?SpeakRuntime.State.IDLE:attempt.state;boolean busy=attempt!=null&&!attempt.released();String text=switch(state){case IDLE->"Ready · microphone is off";case QUIETING->"Preparing a visible preview…";case LISTENING->attempt.progress;case CLOSING->"Stopping microphone…";case DONE->"Preview accepted · no alarm was changed";case BLOCKED->"Microphone cleanup needs attention";case ERROR,CANCELLED->attempt.error.isEmpty()?"Preview stopped":attempt.error;};Design.text(body,text,17,600,Design.BROWN);if(!issue.isEmpty()){Design.space(body,10);Design.text(body,issue,13,500,Design.BROWN);}SpeakAccess.Permission permission=permissions.permission(this);if(permission!=SpeakAccess.Permission.ALLOWED)Design.button(body,permission==SpeakAccess.Permission.SETTINGS?"Open microphone settings":"Allow microphone",false,()->permissions.request(this));
  LinearLayout footer=RecordUi.footer(body);if(state==SpeakRuntime.State.BLOCKED)Design.button(footer,"Retry microphone cleanup",true,attempt::retryCleanup);else if(busy)Design.button(footer,"Stop preview",false,attempt::cancel);else Design.button(footer,"Listen for "+mode.noun,true,this::start).setEnabled(permission==SpeakAccess.Permission.ALLOWED);Design.button(footer,"Close",false,this::finish);Design.space(body,14);Design.text(body,"Up to 15 seconds per attempt. Leaving or losing focus stops listening. No recording, transcript, sound event or alarm task result is saved.",12,400,Design.MUTED);
 }
 private void start(){if(!current()||owned!=null&&!owned.released())return;String unavailable=permissions.unavailable(this);if(!unavailable.isEmpty()){issue=unavailable;render();return;}issue="";owned=session=new Session(getApplicationContext(),mode,this);session.runtime.start();render();}
 private static final class Session implements SpeakRuntime.Port {
  final Context app;final ExecutorService worker=Executors.newSingleThreadExecutor();final Handler main=new Handler(Looper.getMainLooper());final SpeakRuntime runtime;volatile WeakReference<AudioChallengePreviewActivity> view;
  Session(Context app,AudioChallengeMode mode,AudioChallengePreviewActivity activity){this.app=app;view=new WeakReference<>(activity);runtime=new SpeakRuntime(this,mode);}
  boolean released(){return runtime.released();}
  public long now(){return SystemClock.elapsedRealtime();}
  public boolean current(){AudioChallengePreviewActivity a=view.get();return a!=null&&a.session==this&&a.current();}
  public void check(){if(!current()||app.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)!=android.content.pm.PackageManager.PERMISSION_GRANTED)throw new SecurityException();}
  public SpeakRuntime.Quiet quiet(Runnable ready){ready.run();return ()->{};}
  public SpeakRuntime.Capture open()throws Exception{return SpeakAudio.open(app,this::current);}
  public SoundAnalysisStream.Model model(){return new YamnetClassifier(app);}
  public void execute(Runnable work){worker.execute(work);}
  public void idle()throws Exception{Thread.sleep(10);}
  public void changed(){main.post(()->{AudioChallengePreviewActivity a=view.get();if(a!=null&&!a.isDestroyed())a.render();if(runtime.released()){worker.shutdown();if(owned==this)owned=null;}});}
 }
 private final Runnable tick=new Runnable(){public void run(){if(!resumed)return;if(session!=null)session.runtime.tick();main.postDelayed(this,150);}};
 @Override protected void onResume(){super.onResume();resumed=true;visible=hasWindowFocus();main.post(tick);render();}
 @Override protected void onPause(){visible=false;resumed=false;main.removeCallbacks(tick);if(session!=null&&!session.released())session.runtime.cancel();super.onPause();}
 @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);visible=resumed&&focus;if(!focus&&session!=null&&!session.released())session.runtime.cancel();if(focus){RecordUi.lightBars(this);render();}}
 @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] grants){super.onRequestPermissionsResult(code,permissions,grants);if(code==SpeakAccess.PERMISSION)render();}
 @Override protected void onDestroy(){main.removeCallbacks(tick);if(session!=null){if(!session.released())session.runtime.cancel();if(session.view.get()==this)session.view.clear();}super.onDestroy();}
}
