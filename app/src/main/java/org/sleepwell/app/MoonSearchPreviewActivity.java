package org.sleepwell.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import java.util.Random;

/** Entirely local preview: never enters an alarm, schedules one or starts output. */
public final class MoonSearchPreviewActivity extends Activity {
    private MoonSearch board;private boolean solved;private int round;
    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);round=Math.max(0,Math.min(5,getIntent().getIntExtra("round",0)));try{board=saved==null?MoonSearch.create(new Random(),round):MoonSearch.decode(saved.getString("board"));}catch(RuntimeException invalid){board=MoonSearch.create(new Random(),round);}solved=saved!=null&&saved.getBoolean("solved");render();}
    void render(){LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,"Visual search preview",this::finish);Design.space(body,18);if(solved){Design.text(body,"Challenge completed",22,600,Design.INK);Design.space(body,16);Design.button(body,"Try another board",true,()->{board=MoonSearch.create(new Random(),round);solved=false;render();});}else MoonSearchView.render(body,board,()->!solved&&!isDestroyed(),()->{solved=true;render();});}
    @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);state.putString("board",board.encode());state.putBoolean("solved",solved);}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
