package org.sleepwell.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

/** Draft choices plus a retained immutable retention operation. */
public final class SoundLibrarySettingsActivity extends Activity implements SoundRetentionJob.Observer {
    private SoundRetentionJob job;private int days;private boolean favorites;private SoundRetentionJob.State operation;
    private static final int[] DAYS={1,3,7,14,30,0};private static final String[] LABELS={"1 day","3 days","7 days","14 days","30 days","Keep all"};
    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);Object retained=getLastNonConfigurationInstance();job=retained instanceof SoundRetentionJob?(SoundRetentionJob)retained:new SoundRetentionJob(getApplicationContext(),Records.factory);days=saved==null?Prefs.number(this,"audioRetentionDays",7):saved.getInt("days",7);favorites=saved==null?Prefs.of(this).getBoolean("keepAudioFavorites",true):saved.getBoolean("favorites",true);job.observe(this);}
    @Override public Object onRetainNonConfigurationInstance(){return job;}
    @Override public void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putInt("days",days);out.putBoolean("favorites",favorites);}
    @Override public void changed(SoundRetentionJob.State state){if(isDestroyed())return;operation=state;if(state.busy&&state.policy!=null){days=state.policy.days;favorites=state.policy.favorites;}render();}
    private void render(){boolean busy=operation!=null&&operation.busy;LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,"Audio retention",this::finish);Design.space(body,18);int selected=2;for(int n=0;n<DAYS.length;n++)if(days==DAYS[n])selected=n;int index=selected;View choice=Design.item(body,"clock","Keep recordings",LABELS[index],()->SleepPicker.choices(this,"Keep recordings",LABELS,index,n->{days=DAYS[n];render();}));RecordUi.enabled(choice,!busy);Design.line(body);Design.space(body,16);View toggle=Design.toggle(body,"Keep favorites","A favorite estimate keeps its shared recording.",favorites,value->favorites=value);RecordUi.enabled(toggle,!busy);Design.space(body,18);Design.text(body,"Expired audio is permanently deleted. Sleep records and sound estimates stay on this phone.",14,400,Design.MUTED);Design.space(body,12);if(operation!=null&&operation.result!=null&&operation.policy!=null&&operation.policy.days==days&&operation.policy.favorites==favorites)Design.text(body,operation.result.message,14,500,Design.BROWN);View save=Design.button(body,busy?"Applying…":"Save & apply retention",true,()->job.start(new SoundRetentionJob.Policy(days,favorites)));RecordUi.enabled(save,!busy);}
    boolean awaitReaders(long timeout)throws InterruptedException{return job.awaitClosed(timeout);}
    @Override protected void onDestroy(){job.detach(this);if(!isChangingConfigurations())job.close();super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
