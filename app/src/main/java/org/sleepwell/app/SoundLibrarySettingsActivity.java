package org.sleepwell.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import java.util.concurrent.*;

/** Explicit retention policy, applied to ended recordings on a worker. */
public final class SoundLibrarySettingsActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();private Records.Factory source;private int days;private boolean favorites,busy;private String result="";
    private static final int[] DAYS={1,3,7,14,30,0};private static final String[] LABELS={"1 day","3 days","7 days","14 days","30 days","Keep all"};
    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);source=Records.factory;days=saved==null?Prefs.number(this,"audioRetentionDays",7):saved.getInt("days",7);favorites=saved==null?Prefs.of(this).getBoolean("keepAudioFavorites",true):saved.getBoolean("favorites",true);render();}
    @Override public void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putInt("days",days);out.putBoolean("favorites",favorites);}
    private void render(){LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,"Audio retention",this::finish);Design.space(body,18);int selected=2;for(int n=0;n<DAYS.length;n++)if(days==DAYS[n])selected=n;int index=selected;Design.item(body,"clock","Keep recordings",LABELS[index],()->SleepPicker.choices(this,"Keep recordings",LABELS,index,n->{days=DAYS[n];render();}));Design.line(body);Design.space(body,16);Design.toggle(body,"Keep favorites","A favorite estimate keeps its shared recording.",favorites,value->favorites=value);Design.space(body,18);Design.text(body,"Expired audio is permanently deleted. Sleep records and sound estimates stay on this phone.",14,400,Design.MUTED);Design.space(body,12);if(!result.isEmpty())Design.text(body,result,14,500,Design.BROWN);View save=Design.button(body,busy?"Applying…":"Save & apply retention",true,this::save);RecordUi.enabled(save,!busy);}
    private void save(){if(busy)return;busy=true;int selectedDays=days;boolean keep=favorites;render();worker.execute(()->{String message;try(Store store=source.open(getApplicationContext())){Prefs.put(getApplicationContext(),"audioRetentionDays",selectedDays);Prefs.put(getApplicationContext(),"keepAudioFavorites",keep);Prefs.put(getApplicationContext(),"audioRetentionError","");SoundClips.prune(getApplicationContext(),store);message=store.rows("SELECT clip FROM pending_clip_deletes").isEmpty()?"Retention saved.":"Retention saved. Some recordings could not be deleted; apply again to retry.";}catch(Exception failure){message="Retention could not be applied. Try again.";}String done=message;runOnUiThread(()->{if(isDestroyed())return;busy=false;result=done;render();});});}
    boolean awaitReaders(long timeout)throws InterruptedException{return worker.awaitTermination(timeout,TimeUnit.MILLISECONDS);}
    @Override protected void onDestroy(){worker.shutdown();super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
