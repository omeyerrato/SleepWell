package org.sleepwell.app;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import java.util.concurrent.*;

/** Exported only to let launchers open allowlisted screens; never forwards incoming intents. */
public final class HomeLaunchActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    @Override public void onCreate(Bundle state){super.onCreate(state);HomeLaunch.Screen screen=HomeLaunch.read(getIntent());if(screen==null){finish();return;}if(screen==HomeLaunch.Screen.ADD_ALARM)addAlarm();else open(HomeLaunch.target(this,screen,getIntent().getLongExtra("record",0)));}

    private void addAlarm(){Records.Factory pinned=Records.factory;worker.execute(()->{try(Store store=pinned.open(getApplicationContext())){Alarm draft=HomeLaunch.draft(this,store);runOnUiThread(()->{if(!isDestroyed())open(new Intent(this,AlarmEditorActivity.class).putExtra("alarm",draft.json().toString()));});}catch(Exception failure){runOnUiThread(()->{if(!isDestroyed())open(new Intent(this,MainActivity.class).putExtra("destination",1));});}});}
    private void open(Intent intent){startActivity(intent);finish();}
    @Override protected void onDestroy(){worker.shutdown();super.onDestroy();}
}
