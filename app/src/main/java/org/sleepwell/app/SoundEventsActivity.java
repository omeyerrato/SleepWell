package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

/** Actual classified intervals and recorded PCM, with foreground-only explicit playback. */
public final class SoundEventsActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();private final List<Binding> bindings=new ArrayList<>();private final Map<String,Long> positions=new HashMap<>();
    private SharedPreferences browse;private Dialog categoryDialog;private Records.Factory source;private ClipPlaybackPort port;private ClipReviewPlayer player;private ClipReviewPlayer.State state;private List<SoundEventRows.Row> metadata=List.of(),rows=List.of();private RecordEntry record;private String category="all",error="",selected="",modal="",restoredModal="";private int limit=50,generation;private boolean loaded,busy,resumed;
    private final BroadcastReceiver noisy=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){if(player!=null)player.stop();}};
    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);source=Records.factory;browse=SoundBrowseState.factory.open(getApplicationContext());category=SoundBrowseState.read(browse,false);port=ClipPlayback.provider.create(getApplicationContext(),source);player=new ClipReviewPlayer(port,this::playbackState);if(saved!=null){restoredModal=saved.getString("modal","");category=SoundBrowseFilter.filter(saved.getString("category","all"),true);selected=saved.getString("selected","");limit=saved.getInt("limit",50);ArrayList<String> keys=saved.getStringArrayList("positionKeys");long[] values=saved.getLongArray("positions");if(keys!=null&&values!=null)for(int n=0;n<Math.min(keys.size(),values.length);n++)positions.put(keys.get(n),values[n]);}androidx.core.content.ContextCompat.registerReceiver(this,noisy,new IntentFilter(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY),androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);load();}
    @Override protected void onResume(){super.onResume();player.visible(true);if(resumed)load();resumed=true;}
    @Override protected void onPause(){player.visible(false);super.onPause();}
    @Override public void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("modal",categoryDialog!=null&&categoryDialog.isShowing()?modal:restoredModal);out.putString("category",category);out.putString("selected",selected);out.putInt("limit",limit);ArrayList<String> keys=new ArrayList<>(positions.keySet());long[] values=new long[keys.size()];for(int n=0;n<keys.size();n++)values[n]=positions.get(keys.get(n));out.putStringArrayList("positionKeys",keys);out.putLongArray("positions",values);}
    private void load(){loaded=false;error="";int request=++generation;render();long id=getIntent().getLongExtra("record",0);String filter=category;int count=limit;worker.execute(()->{try(Store store=source.open(getApplicationContext())){SoundClips.recover(getApplicationContext(),store);RecordEntry night=Records.get(store,id);List<SoundEventRows.Row> all=SoundEventRows.metadata(getApplicationContext(),store,night),visible=SoundEventRows.prepare(getApplicationContext(),store,all,filter,count);runOnUiThread(()->{if(isDestroyed()||request!=generation)return;record=night;metadata=all;rows=visible;loaded=true;busy=false;render();restoreDialog();});}catch(Exception failure){runOnUiThread(()->{if(isDestroyed()||request!=generation)return;error="Sound events could not be loaded.";loaded=true;busy=false;render();});}});}
    private int count(String filter){int count=0;for(SoundEventRows.Row row:metadata)if(SoundEventRows.matches(row,filter))count++;return count;}
    private void filter(String value){player.stop();category=SoundBrowseFilter.filter(value,true);SoundBrowseState.save(browse,false,category,0);selected="";positions.clear();limit=50;load();}
    private void categories(){if(!loaded||!error.isEmpty())return;player.stop();modal="categories";categoryDialog=SoundCategoryPicker.show(this,category,true,this::count,this::filter);}
    private void menu(){
        player.stop();modal="options";Dialog dialog=new Dialog(this);categoryDialog=dialog;dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);LinearLayout panel=Design.col(this);Design.pad(panel,22,22);panel.setBackground(Design.shape(this,Design.CREAM,22));Design.text(panel,"Sound event options",22,600,Design.INK);Design.space(panel,12);
        RecordUi.heading(panel,"Browse");String[] filters={"all","noise","favorites"},labels={"All events","Other sounds","Favorites"};for(int n=0;n<filters.length;n++){String key=filters[n];filterOption(panel,labels[n],key,()->{dialog.dismiss();filter(key);});}
        Design.item(panel,"fade","All sound categories",null,()->{dialog.dismiss();categories();});Design.item(panel,"heartFilled","Favorite sounds","Across all nights",()->{dialog.dismiss();startActivity(RecordUi.intent(this,FavoriteSoundsActivity.class));});
        if(record!=null){RecordUi.heading(panel,"This recording");Design.item(panel,"info","Analysis details",null,()->{dialog.dismiss();analysisDetails();});Design.item(panel,"wave","Analyze a recording",null,()->{dialog.dismiss();startActivity(RecordUi.intent(this,RecordedRecognitionActivity.class).putExtra("record",record.id).putExtra("updated",record.updated));});Design.item(panel,"wave","Recorded signals",null,()->{dialog.dismiss();startActivity(RecordUi.intent(this,RecordSignalsActivity.class).putExtra("record",record.id));});RecordUi.heading(panel,"Export audio");Design.item(panel,"file","Export M4A intervals","Select sections to save",()->{dialog.dismiss();startActivity(AacExportActivity.night(this,record.id));});Design.item(panel,"upload","Export recordings","Save this night’s audio library",()->{dialog.dismiss();startActivity(RecordUi.intent(this,SoundArchiveActivity.class).putExtra("record",record.id).putExtra("updated",record.updated));});}
        RecordUi.heading(panel,"Recording settings");Design.item(panel,"clock","Audio retention",null,()->{dialog.dismiss();startActivity(RecordUi.intent(this,SoundLibrarySettingsActivity.class));});Design.item(panel,"settings","Snoring cues",null,()->{dialog.dismiss();startActivity(RecordUi.intent(this,SnoringCuesActivity.class));});Design.button(panel,"Done",false,dialog::dismiss);ScrollView scroll=new ScrollView(this);scroll.addView(panel);dialog.setContentView(scroll);SleepPicker.show(this,dialog,680);
    }
    private void filterOption(LinearLayout panel,String label,String key,Runnable choose){boolean active=category.equals(key);LinearLayout row=Design.row(this);row.setMinimumHeight(Design.dp(this,52));Design.pad(row,12,8);row.setBackground(Design.touch(this,active?Design.SOFT:Color.TRANSPARENT,12));row.addView(new SleepPicker.Mark(this,active),new LinearLayout.LayoutParams(Design.dp(this,24),Design.dp(this,24)));TextView text=Design.text(this,label,15,active?600:400,Design.INK);LinearLayout.LayoutParams copy=new LinearLayout.LayoutParams(0,-2,1);copy.leftMargin=Design.dp(this,14);row.addView(text,copy);SleepPicker.radio(row,active);row.setContentDescription(label);row.setFocusable(true);row.setOnClickListener(view->{if(!active)InteractionFeedback.selection(view);choose.run();});panel.addView(row);}
    private void render(){
        bindings.clear();LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);LinearLayout header=RecordUi.header(body,"Sound events",this::finish);RecordUi.menuAction(header,"Sound event options",this::menu,false);
        if(!loaded){Design.space(body,20);Design.text(body,"Loading recordings…",15,400,Design.MUTED);return;}if(!error.isEmpty()){Design.space(body,20);Design.text(body,error,16,400,Design.MUTED);Design.button(body,"Try again",true,this::load);return;}
        TextView date=Design.text(body,RecordUi.date(record.edit.end,record.zone()),14,400,Design.MUTED);date.setPadding(Design.dp(this,48),0,0,0);Design.space(body,12);
        SoundAnalysisCopy.Info analysis=SoundAnalysisCopy.of(record);
        if(analysis.attention){PlanActivity.detail(body,"attention",analysis.title,(record.edited?"Original recording · ":"")+analysis.detail,null,this::analysisDetails);Design.space(body,8);}
        SoundEventUi.categories(body,category,this::count,this::filter);Design.space(body,12);
        SoundCategoryPicker.selected(body,category,count(category),this::categories);
        if(rows.isEmpty()){Design.space(body,32);Design.text(body,metadata.isEmpty()?"No sound events":"No events in this category",22,600,Design.INK);Design.space(body,8);Design.text(body,metadata.isEmpty()?analysis.empty():category.equals("favorites")?"Tap a heart to keep a favorite.":"Try another category.",14,400,Design.MUTED);}
        for(SoundEventRows.Row row:rows)addRow(body,row);
        if(count(category)>rows.size())Design.button(body,"Load more",false,()->{player.stop();limit+=50;load();});
        Design.space(body,20);
    }
    private void analysisDetails(){
        if(record==null)return;player.stop();SoundAnalysisCopy.Info analysis=SoundAnalysisCopy.of(record);
        String detail=analysis.detail.isEmpty()?analysis.title:analysis.title+"\n\n"+analysis.detail;
        if(record.edited)detail+="\n\nAnalysis describes the original recording. This list shows only events inside the edited sleep period.";
        detail+="\n\nSound labels are estimates. Tap an event name to correct its label. Waveforms appear only when recorded audio is still available.";
        modal="analysis";categoryDialog=SleepPicker.message(this,"Analysis details",detail);
    }
    private void restoreDialog(){if(restoredModal.isEmpty()||!loaded||!error.isEmpty()||!hasWindowFocus()||isFinishing()||isDestroyed())return;String saved=restoredModal;restoredModal="";if(saved.equals("categories"))categories();else if(saved.equals("options"))menu();else if(saved.equals("analysis"))analysisDetails();}
    private Drawable rowBackground(SoundEventRows.Row row){
        LayerDrawable layers=new LayerDrawable(new Drawable[]{Design.shape(this,Color.TRANSPARENT,16),Design.shape(this,row.category.equals("speech")?0xffad82ef:0xffaa6538,2)});
        layers.setLayerSize(1,Design.dp(this,2),Design.dp(this,36));layers.setLayerGravity(1,Gravity.LEFT|Gravity.TOP);layers.setLayerInset(1,0,Design.dp(this,12),0,0);return layers;
    }
    private void addRow(LinearLayout body,SoundEventRows.Row row){
        LinearLayout card=Design.col(this);Design.pad(card,8,10);card.setBackground(rowBackground(row));
        LinearLayout head=Design.row(this);head.setGravity(Gravity.TOP);int clockColumn=Design.dp(this,80*Math.max(1,Design.fontScale(this)));
        TextView time=Design.text(this,RecordUi.clock(this,row.start,record.zone()).toUpperCase(Locale.getDefault()),13,400,Design.MUTED);
        time.setPadding(0,Design.dp(this,5),Design.dp(this,8),0);head.addView(time,new LinearLayout.LayoutParams(clockColumn,-2));
        LinearLayout copy=Design.col(this);copy.setMinimumHeight(Design.dp(this,48));copy.setGravity(Gravity.CENTER_VERTICAL);
        Design.text(copy,row.title,15,600,Design.INK);Design.space(copy,4);
        Design.text(copy,row.duration>0?RecordUi.duration(row.duration):"Duration unavailable",13,400,Design.MUTED);
        if(row.event!=null){
            copy.setBackground(Design.touch(this,Color.TRANSPARENT,8));copy.setFocusable(true);copy.setContentDescription("Correct label "+row.key);SoundEventUi.button(copy);
            copy.setOnClickListener(v->{InteractionFeedback.action(v);player.stop();startActivity(SoundLabelActivity.intent(this,row,record));});
        }
        head.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        View favorite=Design.iconButton(this,row.favorite?"heartFilled":"heart",(row.favorite?"Unfavorite ":"Favorite ")+row.key,()->mutate(()->{try(Store store=source.open(getApplicationContext())){if(row.event!=null)SoundClips.favoriteEvent(store,row.event,!row.favorite);else SoundClips.favorite(store,row.clip,!row.favorite);}}));
        favorite.setSelected(row.favorite);head.addView(favorite,new LinearLayout.LayoutParams(Design.dp(this,48),Design.dp(this,48)));
        head.addView(Design.iconButton(this,"trash","Delete "+row.key,()->confirmDelete(row)),new LinearLayout.LayoutParams(Design.dp(this,48),Design.dp(this,48)));card.addView(head);
        if(row.target==null){
            TextView noAudio=Design.text(card,row.audioError.isEmpty()?"Audio not recorded":row.audioError,12,400,Design.MUTED);noAudio.setPadding(clockColumn,Design.dp(this,4),0,0);
        }else{
            Design.space(card,8);LinearLayout audioPanel=Design.col(this);Design.pad(audioPanel,8,6);card.addView(audioPanel,new LinearLayout.LayoutParams(-1,-2));
            LinearLayout audio=Design.row(this);FrameLayout play=new FrameLayout(this);play.setFocusable(true);SoundEventUi.button(play);
            play.setOnClickListener(v->{InteractionFeedback.action(v);selected=row.key;player.toggle(row.target,positions.getOrDefault(row.key,0L));});audio.addView(play,new LinearLayout.LayoutParams(Design.dp(this,48),Design.dp(this,48)));
            ClipWaveView wave=new ClipWaveView(this,row.peaks,row.category.equals("speech")?0xff9262df:Design.BROWN,row.category.equals("speech")?0xffc3a4f4:0xffcfb69f);
            LinearLayout.LayoutParams wp=new LinearLayout.LayoutParams(0,Design.dp(this,42),1);wp.leftMargin=clockColumn-Design.dp(this,56);audio.addView(wave,wp);audioPanel.addView(audio);
            LinearLayout progress=Design.row(this);TextView elapsed=Design.text(this,"0:00",11,400,Design.MUTED),total=Design.text(this,clock(row.target.duration()),11,400,Design.MUTED);progress.addView(elapsed);
            ClipSeekView seek=new ClipSeekView(this,row.target.duration(),position->{selected=row.key;positions.put(row.key,position);if(state!=null&&state.key.equals(row.key))player.seek(position);else{player.stop();selected=row.key;positions.put(row.key,position);for(Binding other:bindings)update(other);}});
            LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(0,Design.dp(this,48),1);sp.leftMargin=Design.dp(this,6);sp.rightMargin=Design.dp(this,6);progress.addView(seek,sp);progress.addView(total);
            LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,-2);pp.leftMargin=clockColumn-Design.dp(this,8);audioPanel.addView(progress,pp);
            TextView playbackError=Design.text(audioPanel,"",12,400,Design.BROWN);playbackError.setVisibility(View.GONE);
            Binding binding=new Binding(row,card,audioPanel,play,wave,seek,elapsed,playbackError,progress);bindings.add(binding);update(binding);
        }
        body.addView(card,new LinearLayout.LayoutParams(-1,-2));Design.line(body);
    }
    private static String clock(long millis){long seconds=Math.max(0,millis/1000);return seconds/60+":"+String.format(Locale.ROOT,"%02d",seconds%60);}
    private void playbackState(ClipReviewPlayer.State value){state=value;if(!value.key.isEmpty()){positions.put(value.key,value.position);selected=value.key;}for(Binding binding:bindings)update(binding);}
    private void update(Binding binding){boolean current=state!=null&&state.key.equals(binding.row.key),active=current&&(state.phase==ClipReviewPlayer.Phase.PLAYING||state.phase==ClipReviewPlayer.Phase.LOADING||state.phase==ClipReviewPlayer.Phase.READY);long position=current?state.position:positions.getOrDefault(binding.row.key,0L);String icon=active?"pause":"play";if(!icon.equals(binding.icon)){binding.icon=icon;binding.play.removeAllViews();Design.Icon glyph=new Design.Icon(this,icon,active?Design.CREAM:Design.BROWN);binding.play.addView(glyph,new FrameLayout.LayoutParams(Design.dp(this,22),Design.dp(this,22),Gravity.CENTER));binding.play.setBackground(active?Design.touch(this,Design.BROWN,26):Appearance.contrast(this)?Design.outline(this,Design.CREAM,Design.BROWN,26):Design.touch(this,Design.SOFT,26));}binding.play.setContentDescription((active?"Pause ":"Play ")+binding.row.key);binding.card.setBackground(rowBackground(binding.row));binding.audioPanel.setBackground(Design.shape(this,binding.row.key.equals(selected)?Design.SOFT:Color.TRANSPARENT,16));binding.wave.progress(position/(float)binding.row.target.duration());binding.progress.setVisibility(binding.row.key.equals(selected)?View.VISIBLE:View.GONE);binding.seek.position(position);binding.elapsed.setText(clock(position));boolean failed=current&&state.phase==ClipReviewPlayer.Phase.ERROR;binding.error.setVisibility(failed?View.VISIBLE:View.GONE);binding.error.setText(failed?state.error:"");}
    private interface Mutation {void run()throws Exception;}
    private void mutate(Mutation action){if(busy)return;busy=true;player.stop();worker.execute(()->{try{action.run();runOnUiThread(()->{if(!isDestroyed())load();});}catch(Exception failure){runOnUiThread(()->{if(isDestroyed())return;busy=false;SleepPicker.message(this,"Could not save",failure.getMessage()==null?"Try again.":failure.getMessage());});}});}
    private void confirmDelete(SoundEventRows.Row row){player.stop();Dialog dialog=new Dialog(this);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);LinearLayout panel=Design.col(this);Design.pad(panel,22,22);panel.setBackground(Design.shape(this,Design.CREAM,22));Design.text(panel,"Delete sound event?",22,600,Design.INK);Design.space(panel,14);Design.text(panel,row.clip==null?"Remove this estimate from the night.":row.event==null?"Permanently remove this audio recording.":"Remove only this estimate or permanently delete its shared audio. Other estimates keep their labels.",14,400,Design.MUTED);if(row.event!=null)Design.button(panel,"Delete estimate",true,()->{dialog.dismiss();mutate(()->{try(Store store=source.open(getApplicationContext())){SoundClips.deleteEvent(store,row.event);}});});if(row.clip!=null)Design.button(panel,"Delete recording",row.event==null,()->{dialog.dismiss();mutate(()->{try(Store store=source.open(getApplicationContext())){SoundClips.delete(getApplicationContext(),store,row.clip);}});});Design.button(panel,"Cancel",false,dialog::dismiss);dialog.setContentView(panel);dialog.show();dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);dialog.getWindow().setLayout(Math.min(getResources().getDisplayMetrics().widthPixels-Design.dp(this,32),Design.dp(this,420)),-2);}
    boolean awaitReaders(long timeoutMs)throws InterruptedException{long start=System.currentTimeMillis();boolean readers=worker.awaitTermination(timeoutMs,TimeUnit.MILLISECONDS);return readers&&port.awaitClosed(Math.max(1,timeoutMs-(System.currentTimeMillis()-start)));}
    @Override protected void onDestroy(){generation++;if(categoryDialog!=null)categoryDialog.dismiss();unregisterReceiver(noisy);player.close();port.close();worker.shutdown();super.onDestroy();}
    @Override protected void onPostResume(){super.onPostResume();Design.systemBars(this,false);RecordUi.lightBars(this);}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused){RecordUi.lightBars(this);restoreDialog();}}
    private static final class Binding {final SoundEventRows.Row row;final LinearLayout card,audioPanel;final FrameLayout play;final ClipWaveView wave;final ClipSeekView seek;final TextView elapsed,error;final View progress;String icon="";Binding(SoundEventRows.Row row,LinearLayout card,LinearLayout audioPanel,FrameLayout play,ClipWaveView wave,ClipSeekView seek,TextView elapsed,TextView error,View progress){this.progress=progress;this.row=row;this.card=card;this.audioPanel=audioPanel;this.play=play;this.wave=wave;this.seek=seek;this.elapsed=elapsed;this.error=error;}}
}
