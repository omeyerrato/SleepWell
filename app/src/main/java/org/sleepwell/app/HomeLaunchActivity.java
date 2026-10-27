package org.sleepwell.app;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import java.util.concurrent.*;

/** Exported allowlisted navigation only. Deferred routes belong to this visible request. */
public final class HomeLaunchActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private HomeLaunch.Screen screen;private Intent target;private Alarm draft;private int generation;
    private boolean resumed,pending,opening;private String error="";
    @Override public void onCreate(Bundle state){super.onCreate(state);if(android.os.Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::cancel);route(getIntent(),state);}
    private void route(Intent input,Bundle state){
        int request=++generation;target=null;draft=null;pending=false;opening=false;error="";screen=HomeLaunch.read(input);
        if(screen==null){error="This shortcut is unavailable. Close it and open SleepWell again.";render();return;}
        if(screen!=HomeLaunch.Screen.ADD_ALARM){try{target=HomeLaunch.target(this,screen,Math.max(0,input.getLongExtra("record",0)));pending=true;}catch(RuntimeException invalid){error="This shortcut could not be read. Open the screen from SleepWell.";}render();openIfVisible();return;}
        if(state!=null&&state.containsKey("draft")){try{draft=Alarm.read(new org.json.JSONObject(state.getString("draft")));target=new Intent(this,AlarmEditorActivity.class).putExtra("alarm",draft.json().toString());pending=true;render();openIfVisible();return;}catch(Exception invalid){error="The interrupted alarm draft could not be restored. Close this shortcut and add an alarm again.";render();return;}}
        render();Records.Factory pinned=Records.factory;
        worker.execute(()->{try(Store store=pinned.open(getApplicationContext())){Alarm prepared=HomeLaunch.draft(getApplicationContext(),store);runOnUiThread(()->{if(!current(request))return;draft=prepared;target=new Intent(this,AlarmEditorActivity.class).putExtra("alarm",prepared.json().toString());pending=true;render();openIfVisible();});}catch(Exception failure){runOnUiThread(()->{if(!current(request))return;error="A new alarm draft could not be opened. Existing alarms were not changed.";render();});}});
    }
    private boolean current(int request){return request==generation&&!isDestroyed()&&!isFinishing();}
    private void openIfVisible(){if(!pending||opening||target==null||!resumed||!hasWindowFocus()||isFinishing()||isDestroyed())return;opening=true;pending=false;try{startActivity(target);generation++;finish();}catch(RuntimeException unavailable){opening=false;error="The requested screen could not open. Try again or close this shortcut.";render();}}
    private void render(){if(isDestroyed()||isFinishing())return;android.widget.LinearLayout body=Design.page(this,false,null,n->{});RecordUi.header(body,"Open SleepWell",this::cancel);Design.space(body,18);Design.text(body,error.isEmpty()?"Opening your screen…":error,16,500,error.isEmpty()?Design.INK:Design.BROWN);android.widget.LinearLayout footer=RecordUi.footer(body);if(!error.isEmpty()&&target!=null)Design.button(footer,"Try opening again",true,()->{error="";pending=true;openIfVisible();});if(!error.isEmpty()&&screen==HomeLaunch.Screen.ADD_ALARM&&target==null)Design.button(footer,"Open alarms",true,()->{target=HomeLaunch.target(this,HomeLaunch.Screen.ALARMS,0);pending=true;openIfVisible();});Design.button(footer,"Cancel",false,this::cancel);}
    private void cancel(){generation++;pending=false;target=null;finish();}
    @Override protected void onNewIntent(Intent input){super.onNewIntent(input);setIntent(input);route(input,null);}
    @Override protected void onResume(){super.onResume();resumed=true;openIfVisible();}
    @Override protected void onPause(){resumed=false;super.onPause();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused){RecordUi.lightBars(this);openIfVisible();}}
    @android.annotation.SuppressLint("GestureBackNavigation") @Override public void onBackPressed(){cancel();}
    @Override public void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);if(pending&&draft!=null)state.putString("draft",draft.json().toString());}
    @Override protected void onDestroy(){generation++;worker.shutdown();super.onDestroy();}
}
