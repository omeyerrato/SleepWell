package org.sleepwell.app;

import android.app.*;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.*;
import org.json.JSONObject;
import java.util.*;
import java.util.concurrent.*;

/** Supporting measured-signal view; manual entries never receive fabricated plots. */
public final class RecordSignalsActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();private MediaPlayer player;private RecordEntry record;private List<JSONObject> epochs=List.of(),clips=List.of();private boolean original;
    @Override public void onCreate(Bundle state){super.onCreate(state);RecordUi.review(this);long id=getIntent().getLongExtra("record",0);LinearLayout body=Design.page(this,false,null,n->{});RecordUi.header(body,"Recorded signals",this::finish);Design.text(body,"Loading signals…",15,400,Design.MUTED);
        worker.execute(()->{try(Store store=Records.open(this)){RecordEntry entry=Records.get(store,id);List<JSONObject> data=store.rows("SELECT * FROM epochs WHERE session=? ORDER BY time",""+id),sounds=store.rows("SELECT * FROM clips WHERE session=? ORDER BY time",""+id);runOnUiThread(()->{if(isDestroyed())return;record=entry;epochs=data;clips=sounds;render();});}catch(Exception e){runOnUiThread(()->{if(isDestroyed())return;LinearLayout page=Design.page(this,false,null,n->{});RecordUi.header(page,"Recorded signals",this::finish);Design.text(page,"Signals unavailable. "+e.getMessage(),15,400,Design.MUTED);});}});
    }
    private void render(){LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,"Recorded signals",this::finish);Design.space(body,8);Design.text(body,record.provenance(),13,500,Design.MUTED);if(record.manual()){Design.space(body,32);Design.text(body,"No sensor measurements",22,600,Design.INK);Design.space(body,10);Design.text(body,"This period was entered manually. It has no movement, microphone or light measurements.",15,400,Design.MUTED);return;}
        if(record.edited){Design.space(body,12);RecordUi.segments(body,new String[]{"Edited period","Original capture"},original?1:0,index->{original=index==1;render();});}
        long start=original?record.rawStart:record.edit.start,end=original?record.rawEnd:record.edit.end;Design.space(body,12);Design.text(body,RecordUi.clock(this,start,record.zone())+" – "+RecordUi.clock(this,end,record.zone()),16,600,Design.INK);Design.space(body,8);Design.text(body,"Original minute averages. Editing changes the visible period; stored measurements remain intact.",12,400,Design.MUTED);
        String[] names={"Movement","Room noise","Room light"},metrics={"movement","noise","light"};for(int n=0;n<3;n++){RecordUi.heading(body,names[n]);body.addView(new RecordChart(this,original?epochs:Records.window(epochs,start,end),metrics[n],start,end,false,record.zone()),new LinearLayout.LayoutParams(-1,Design.dp(this,160)));if(n==1)Design.text(body,"Relative microphone amplitude, not calibrated decibels.",11,400,Design.MUTED);}
        RecordUi.heading(body,"Saved sound clips");int shown=0;for(JSONObject clip:clips){long time=clip.optLong("time");if(time<start||time>end)continue;shown++;Design.button(body,"Play recorded sound · "+RecordUi.clock(this,time,record.zone()),false,()->play(clip.optString("path")));}if(shown==0)Design.text(body,"No sound clips in this period.",14,400,Design.MUTED);Design.space(body,16);
    }
    private void play(String name){stop();try{player=new MediaPlayer();player.setDataSource(FileSafety.clip(this,name).getAbsolutePath());player.prepare();player.setOnCompletionListener(p->stop());player.start();}catch(Exception e){stop();new AlertDialog.Builder(this).setTitle("Audio unavailable").setMessage("This sound clip could not be opened.").setPositiveButton("OK",null).show();}}
    private void stop(){if(player!=null){player.release();player=null;}}
    @Override protected void onPause(){stop();super.onPause();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
    @Override protected void onDestroy(){stop();worker.shutdownNow();super.onDestroy();}
}
