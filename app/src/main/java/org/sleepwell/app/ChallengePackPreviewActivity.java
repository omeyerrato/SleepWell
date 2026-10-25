package org.sleepwell.app;

import android.app.Activity;import android.content.*;import android.os.Bundle;import android.widget.*;import java.util.UUID;

/** Local challenge preview. It never starts an alarm, sound, camera or tracking service. */
public final class ChallengePackPreviewActivity extends Activity {
    interface Preferences {SharedPreferences open(Context context,String name);}
    static Preferences preferences=(context,name)->context.getSharedPreferences(name,0);
    private String run,type,name;private int round;private boolean solved,resumed;private SharedPreferences prefs;private TiltMazeTask tilt;private JumpTask jumping;private EditText counting;private Bundle answer;
    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);type=getIntent().getStringExtra("type");if(type==null||!java.util.List.of("Counting","Flags","Tilt maze","Jumping").contains(type)){finish();return;}round=Math.max(0,Math.min(5,getIntent().getIntExtra("round",0)));run=saved==null?UUID.randomUUID().toString():saved.getString("run");name="challenge-preview-"+run;prefs=preferences.open(this,name);solved=saved!=null&&saved.getBoolean("solved");answer=saved;render();}
    private boolean current(){return resumed&&!isDestroyed()&&!isFinishing()&&!solved;}
    private boolean currentJump(){return current()&&hasWindowFocus();}
    private void complete(){if(!current())return;solved=true;render();}
    private void render(){closeJump();closeTilt();LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,type+" preview",this::finish);Design.space(body,18);if(solved){Design.text(body,"Challenge completed",23,600,Design.INK);Design.button(body,"Try another puzzle",true,()->{run=UUID.randomUUID().toString();solved=false;answer=null;render();});return;}if(type.equals("Jumping")){try{if(JumpCheckpoint.math(prefs,run,0,round)){WakePuzzleView.math(body,run,0,round,this::current,this::complete);return;}jumping=new JumpTask(body,prefs,run,0,round,this::currentJump,this::complete,this::render);}catch(RuntimeException unavailable){Design.text(body,"Game unavailable · solve Math instead",14,500,Design.BROWN);WakePuzzleView.math(body,run,0,round,this::current,this::complete);}return;}if(type.equals("Counting"))counting=WakePuzzleView.counting(body,run,0,round,answer,this::current,this::complete);else if(type.equals("Flags"))WakePuzzleView.flags(body,run,0,round,this::current,this::complete);else{if(tilt!=null){Design.text(body,"Tilt sensor could not stop. Retry before opening another puzzle.",14,500,Design.BROWN);Design.button(body,"Retry cleanup",false,this::render);return;}try{if(ChallengePackState.fallback(prefs,run,0,round)){WakePuzzleView.math(body,run,0,round,this::current,this::complete);return;}tilt=new TiltMazeTask(body,prefs,run,0,round,this::current,this::complete,this::render);}catch(RuntimeException failure){Design.text(body,"Maze unavailable. Close this preview and try again.",14,500,Design.BROWN);}}}
    private void closeJump(){if(jumping!=null){jumping.close();jumping=null;}}
    private void closeTilt(){if(tilt!=null){tilt.close();if(tilt.released())tilt=null;}}
    @Override protected void onPause(){if(counting!=null){answer=new Bundle();answer.putString("countKey",ChallengePackState.key(run,0,round));answer.putString("countAnswer",counting.getText().toString());}closeJump();closeTilt();resumed=false;super.onPause();}
    @Override protected void onResume(){super.onResume();resumed=true;if(type!=null&&(type.equals("Tilt maze")||type.equals("Jumping")))render();}
    @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);closeJump();closeTilt();state.putString("run",run);state.putBoolean("solved",solved);if(counting!=null){state.putString("countKey",ChallengePackState.key(run,0,round));state.putString("countAnswer",counting.getText().toString());}}
    @Override protected void onDestroy(){closeJump();closeTilt();if(isFinishing()&&name!=null)getApplicationContext().deleteSharedPreferences(name);super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
