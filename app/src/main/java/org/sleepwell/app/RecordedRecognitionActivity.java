package org.sleepwell.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.*;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import org.json.*;
import java.util.Locale;

/** Private, explicit saved-audio analysis. Navigation never prepares or analyzes audio. */
public final class RecordedRecognitionActivity extends Activity implements RecordedRecognitionJob.Observer {
    private static final int DOCUMENT=864;
    private enum Page {MAIN,SOURCES,SAVED,ABOUT,SOURCE,DETAILS,LICENSES,NOTICE}
    private RecordedRecognitionJob job;
    private Dialog clearDialog;
    private Page page=Page.MAIN,sourceParent=Page.MAIN;
    private Bundle positions=new Bundle();
    private ScrollView scroll;

    static Intent clip(Activity owner,long record,long clip,long event,long updated){return RecordUi.intent(owner,RecordedRecognitionActivity.class).putExtra("record",record).putExtra("clip",clip).putExtra("event",event).putExtra("updated",updated);}
    private static Page restored(String value){try{return Page.valueOf(value);}catch(IllegalArgumentException|NullPointerException invalid){return Page.MAIN;}}
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);RecordUi.review(this);
        if(saved!=null){page=restored(saved.getString("page"));sourceParent=restored(saved.getString("sourceParent"));Bundle stored=saved.getBundle("positions");if(stored!=null)positions=new Bundle(stored);}
        if(android.os.Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::back);
        Object retained=getLastNonConfigurationInstance();job=retained instanceof RecordedRecognitionJob?(RecordedRecognitionJob)retained:new RecordedRecognitionJob(getApplicationContext(),Records.factory,getIntent().getLongExtra("record",0),getIntent().getLongExtra("clip",0),getIntent().getLongExtra("event",0),getIntent().getLongExtra("updated",-1));
        if(saved!=null&&retained==null){page=Page.MAIN;job.message="Audio copies and unfinished analysis are not restored after process loss. Select and review the source again.";}
        job.observe(this);
    }
    private void remember(){if(scroll!=null)positions.putInt(page.name(),scroll.getScrollY());}
    @Override public void onSaveInstanceState(Bundle out){remember();super.onSaveInstanceState(out);out.putString("page",page.name());out.putString("sourceParent",sourceParent.name());out.putBundle("positions",positions);}
    @Override public Object onRetainNonConfigurationInstance(){return job;}
    private void go(Page next){remember();page=next;scroll=null;changed();}
    private void sourceInfo(){sourceParent=page;go(Page.SOURCE);}
    private void back(){switch(page){case MAIN:finish();break;case SOURCE:go(sourceParent);break;case NOTICE:go(Page.LICENSES);break;case LICENSES:go(Page.ABOUT);break;default:go(Page.MAIN);}}
    @android.annotation.SuppressLint("GestureBackNavigation") @Override public void onBackPressed(){back();}
    private String title(){switch(page){case SOURCES:return "Sources";case SAVED:return "Saved results";case ABOUT:return "About analysis";case SOURCE:return "Source info";case DETAILS:return "Result details";case LICENSES:return "Model credits";case NOTICE:return "Model notice";default:return "Recording analysis";}}
    private boolean working(){return job.phase==RecordedRecognitionJob.Phase.READING||job.phase==RecordedRecognitionJob.Phase.ANALYZING;}
    @Override public void changed(){
        if(isDestroyed())return;remember();LinearLayout body=Design.page(this,false,null,n->{});scroll=(ScrollView)body.getParent();ScrollView rendered=scroll;int top=positions.getInt(page.name(),0);
        RecordUi.label(this,body);RecordUi.header(body,title(),this::back);Design.space(body,12);
        if(page==Page.MAIN){Design.text(body,"Offline · experimental",13,600,Design.BROWN);Design.space(body,6);Design.text(body,"Sound estimates can miss or confuse sounds. Uncertain does not mean absent.",13,400,Design.MUTED);Design.space(body,16);}
        if(working()){Design.text(body,job.progress,15,600,Design.INK);RecordUi.quietButton(body,"Cancel analysis",job::cancel);Design.space(body,12);}
        message(body);
        switch(page){case SOURCES:sources(body);break;case SAVED:saved(body);break;case ABOUT:about(body);break;case SOURCE:sourceDetails(body);break;case DETAILS:details(body);break;case LICENSES:licenses(body);break;case NOTICE:notice(body);break;default:main(body);}
        Design.space(body,20);rendered.post(()->{if(scroll==rendered)rendered.scrollTo(0,top);});
    }
    private void message(LinearLayout body){
        String text=job.message;
        if(text.isEmpty()||text.equals("Review this copy, then explicitly start analysis.")||text.equals("Saved on this phone. These are experimental estimates, not confirmed events."))return;
        Design.text(body,text,13,500,Design.BROWN);Design.space(body,12);
    }
    private void main(LinearLayout body){
        if(!working()){
            if(job.phase==RecordedRecognitionJob.Phase.READY&&job.input!=null){
                source(body,job.input.source,job.input.durationMs);navigation(body,"info","Source info","Format and provenance",this::sourceInfo);Design.space(body,6);
                Design.button(body,"Analyze this copy",true,()->{positions.putInt(Page.MAIN.name(),0);scroll=null;job.analyze();});
                RecordUi.quietButton(body,"Discard copy",job::cancel);
            }else if(job.result!=null)result(body,job.result);
            else{Design.text(body,"Choose a recording",22,600,Design.INK);Design.space(body,6);Design.text(body,"Look for laughter and instrument sounds in a saved clip or audio file.",14,400,Design.MUTED);Design.button(body,"Sources",true,()->go(Page.SOURCES));}
        }
        Design.space(body,12);if(working()||job.input!=null||job.result!=null){navigation(body,"file","Sources","Saved recordings or an audio file",()->go(Page.SOURCES));Design.line(body);}
        navigation(body,"wave","Saved results",job.saved.isEmpty()?"No analyses saved":job.saved.size()+" saved on this phone",()->go(Page.SAVED));Design.line(body);
        navigation(body,"info","About analysis","Limits, format and model credits",()->go(Page.ABOUT));
    }
    private void navigation(LinearLayout body,String icon,String title,String subtitle,Runnable action){PlanActivity.detail(body,icon,title,subtitle,null,action);}
    private void sources(LinearLayout body){
        Design.text(body,"Select audio to review before analysis.",14,400,Design.MUTED);Design.space(body,10);
        if(job.record>0&&job.clip>0)RecordUi.enabled(Design.button(body,"Prepare selected saved clip",true,()->{go(Page.MAIN);job.retained();}),!job.busy());
        if(job.record>0&&job.clip<=0){
            RecordUi.heading(body,"This night’s recordings");Design.text(body,"Ending times use the current device zone.",12,400,Design.MUTED);
            if(job.clips.isEmpty())Design.text(body,"No saved recordings on this page.",14,400,Design.MUTED);
            for(RecordedRecognitionJob.ClipChoice choice:job.clips){String time=RecordUi.preciseClock(this,choice.time,java.time.ZoneId.systemDefault());LinearLayout row=PlanActivity.detail(body,"wave","Recording ending "+time,choice.pending?"Pending deletion":choice.duration>0?RecordUi.duration(choice.duration):"Duration checked when opened",null,()->{go(Page.MAIN);job.retained(choice.id);});RecordUi.enabled(row,!choice.pending&&!job.busy());}
            if(job.previousClips())RecordUi.enabled(RecordUi.quietButton(body,"Previous recordings",()->job.pageClips(false)),!job.busy());
            if(job.moreClips)RecordUi.enabled(RecordUi.quietButton(body,"More recordings",()->job.pageClips(true)),!job.busy());
            Design.space(body,12);
        }
        RecordUi.enabled(Design.button(body,"Choose an audio file",job.record<=0,this::choose),!job.busy());
        Design.space(body,8);Design.text(body,"AAC, MP3, Ogg, FLAC or PCM16 WAV · up to 2 min",12,400,Design.MUTED);
        navigation(body,"info","Audio requirements","Supported files and source checks",this::sourceInfo);
        if(job.input!=null||job.result!=null)RecordUi.quietButton(body,"Back to current analysis",()->go(Page.MAIN));
    }
    private void saved(LinearLayout body){
        Design.text(body,job.saved.size()+" of 50 · summaries only",13,500,Design.MUTED);Design.space(body,12);
        if(job.saved.isEmpty()){Design.text(body,"No saved results yet",21,600,Design.INK);Design.space(body,6);Design.text(body,"Completed analyses appear here. Original sleep events and recordings stay separate.",14,400,Design.MUTED);}
        for(RecordedRecognitionStore.Entry entry:job.saved){String date=RecordUi.date(entry.created,java.time.ZoneId.systemDefault());LinearLayout row=Design.row(this);LinearLayout link=Design.col(this);navigation(link,"wave",date,sourceName(entry.source)+" · "+RecordUi.duration(entry.source.optLong("durationMs")),()->{go(Page.MAIN);job.open(entry);});row.addView(link,new LinearLayout.LayoutParams(0,-2,1));row.addView(Design.iconButton(this,"trash","Delete saved analysis "+date,()->job.delete(entry)),new LinearLayout.LayoutParams(Design.dp(this,48),Design.dp(this,48)));RecordUi.enabled(row,!job.busy());body.addView(row);Design.line(body);}
        if(!job.saved.isEmpty())RecordUi.enabled(RecordUi.quietButton(body,"Clear saved analyses",this::clear),!job.busy());
    }
    private static String sourceName(JSONObject source){return source.optString("kind").equals("saved_clip")?"Saved recording":source.optString("kind").equals("selected_wav")?"Selected WAV":"Selected audio";}
    private void source(LinearLayout body,JSONObject source,long duration){LinearLayout card=Design.col(this);Design.pad(card,16,16);card.setBackground(Design.shape(this,Design.SOFT,18));Design.text(card,sourceName(source),14,500,Design.MUTED);Design.space(card,6);Design.text(card,RecordUi.duration(duration),30,700,Design.INK);String provenance=source.optString("provenance","");if(!provenance.isEmpty()){Design.space(card,5);Design.text(card,provenance,12,400,Design.MUTED);}body.addView(card);}
    private static String clock(long ms){long seconds=ms/1000;return seconds/60+":"+String.format(Locale.ROOT,"%02d",seconds%60);}
    private void result(LinearLayout body,RecordedRecognitionStore.Entry entry){
        source(body,entry.source,entry.source.optLong("durationMs"));
        try{JSONObject data=entry.result;RecordUi.heading(body,"Sound estimates");Design.text(body,"Analyzed "+RecordUi.duration(data.getLong("analyzedMs"))+" · omitted "+RecordUi.duration(data.getLong("omittedMs")),12,500,Design.MUTED);Design.space(body,10);windows(body,data,false);}
        catch(JSONException invalid){Design.text(body,"This saved result is unreadable. Open Result details to remove it and analyze the source again.",14,500,Design.BROWN);}
        navigation(body,"info","Result details","Model, scores and saved result options",()->go(Page.DETAILS));
    }
    private void windows(LinearLayout body,JSONObject data,boolean technical)throws JSONException{
        JSONArray windows=data.getJSONArray("windows");
        if(windows.length()==0)Design.text(body,"No window of at least two seconds was available. Result is unknown.",14,500,Design.MUTED);
        for(int n=0;n<windows.length();n++){JSONObject window=windows.getJSONObject(n);String candidate=window.getString("candidate"),label=candidate.equals("laughter")?"Laughter candidate":candidate.equals("instrument")?"Instrument candidate":"Uncertain";
            LinearLayout card=Design.col(this);Design.pad(card,14,12);card.setBackground(Design.shape(this,Design.SOFT,14));Design.text(card,clock(window.getLong("startMs"))+"–"+clock(window.getLong("startMs")+window.getLong("durationMs")),12,500,Design.MUTED);Design.space(card,4);Design.text(card,label,16,600,Design.INK);
            if(candidate.equals("instrument")&&!window.optString("instrumentLabel").isEmpty())Design.text(card,window.getString("instrumentLabel")+" · experimental class",13,400,Design.MUTED);
            if(technical){estimate(card,"Laughter",window.getJSONObject("laughter"));estimate(card,"Instrument",window.getJSONObject("instrument"));
                if(window.has("instrumentModel")){
                    Design.text(card,window.getString("instrumentModel").equals(RecordedSoundPolicy.PANN)?"Instrument estimate uses PANNs fallback":"Instrument estimate uses EfficientAT",12,400,Design.MUTED);
                    estimate(card,"EfficientAT instrument",window.getJSONObject("efficientInstrument"));estimate(card,"PANNs instrument",window.getJSONObject("pannInstrument"));
                }
            }
            body.addView(card);Design.space(body,8);
        }
    }
    private void estimate(LinearLayout body,String label,JSONObject estimate)throws JSONException{Design.space(body,6);Design.text(body,String.format(Locale.ROOT,"%s · score %.3f · competing score %.3f",label,estimate.getDouble("score"),estimate.getDouble("competitor")),12,400,Design.MUTED);Design.text(body,estimate.getBoolean("qualifies")?"Experimental gate met":"Experimental gate not met",12,400,Design.MUTED);}
    private void details(LinearLayout body){
        RecordedRecognitionStore.Entry entry=job.result;if(entry==null){Design.text(body,"No current result. Open a saved analysis first.",14,400,Design.MUTED);navigation(body,"wave","Saved results",null,()->go(Page.SAVED));return;}
        Design.text(body,"Window positions are relative to the selected audio. Scores are uncalibrated outputs, not confidence percentages or exact event boundaries.",14,400,Design.MUTED);
        try{Design.space(body,14);Design.text(body,"Model version",13,600,Design.INK);Design.text(body,entry.result.getString("modelVersion"),12,400,Design.MUTED).setTextIsSelectable(true);Design.space(body,12);Design.text(body,"Analyzed "+RecordUi.duration(entry.result.getLong("analyzedMs"))+" · omitted "+RecordUi.duration(entry.result.getLong("omittedMs")),13,500,Design.INK);Design.space(body,12);windows(body,entry.result,true);}
        catch(JSONException invalid){Design.text(body,"This saved result is unreadable. You can delete the summary and analyze the source again.",14,500,Design.BROWN);}
        navigation(body,"file","Source info","Provenance and file fingerprints",this::sourceInfo);navigation(body,"info","About analysis","Limitations and model credits",()->go(Page.ABOUT));
        Design.space(body,10);RecordUi.enabled(RecordUi.quietButton(body,"Delete this analysis",()->{go(Page.SAVED);job.delete(entry);}),!job.busy());Design.text(body,"Deletes only this summary. Original audio stays unchanged.",12,400,Design.MUTED);
    }
    private JSONObject currentSource(){if(job.input!=null)try{return job.input.provenance();}catch(JSONException unavailable){return job.input.source;}return job.result!=null?job.result.source:null;}
    private void sourceDetails(LinearLayout body){
        JSONObject source=currentSource();if(source!=null){source(body,source,source.optLong("durationMs"));RecordUi.heading(body,"Source provenance");try{Design.text(body,source.toString(2),12,400,Design.MUTED).setTextIsSelectable(true);}catch(JSONException invalid){Design.text(body,"Source metadata is unavailable.",14,400,Design.MUTED);}}
        RecordUi.heading(body,"Audio requirements");Design.text(body,"One audio file, up to 120 seconds and 16 MiB. Supported phone decoders can prepare AAC/M4A, MP3, Ogg and FLAC; PCM16 WAV is also supported. Use mono or stereo audio at 8–96 kHz. Stereo channels are averaged and supported audio is converted to mono 16 kHz. Filter edges are omitted rather than padded; no peak normalization is applied. Less than two seconds after preparation produces no analyzed window.",14,400,Design.MUTED);Design.space(body,12);
        Design.text(body,"Saved recordings are checked against their current file and effective record interval. Missing audio, pending deletion, gaps, reviewed awake periods and pauses may prevent analysis. Source edits or deletion invalidate saved results.",14,400,Design.MUTED);Design.space(body,12);
        Design.text(body,"The selected audio is copied into private memory only. No audio is saved by this tool and no document access is retained. Its provider may need a connection to supply the file; analysis stays on this phone. Unfinished copies are discarded after process loss.",14,400,Design.MUTED);
    }
    private void about(LinearLayout body){
        Design.text(body,"Experimental, offline estimates",21,600,Design.INK);Design.space(body,10);Design.text(body,"This tool looks for laughter and instrument candidates in audio you explicitly select. It can miss or confuse sounds, including laughter and instruments in independent recorded-clip evaluations. Uncertain results never prove a sound was absent.",14,400,Design.MUTED);Design.space(body,12);
        Design.text(body,"Actual audio is analyzed in separate 10-second windows, including a final tail of at least two seconds. Shorter tails are omitted. These are coarse estimates, not exact event boundaries, identity checks, medical conclusions or alarm challenge proofs.",14,400,Design.MUTED);Design.space(body,12);
        Design.text(body,"Bundled models run on this phone without downloads or audio uploads. Original snoring, speech events and your corrected labels are unchanged. Native model work may finish its current step before cancellation discards the private copy; incomplete results are not saved.",14,400,Design.MUTED);Design.space(body,12);
        navigation(body,"file","Audio requirements","Format, privacy and source checks",this::sourceInfo);navigation(body,"info","Model credits and licenses","Attribution, changes and full licenses",()->go(Page.LICENSES));
    }
    private void licenses(LinearLayout body){String[] names={"NOTICE.txt","PANN-WEIGHTS-CC-BY-4.0.txt","PANN-CODE-MIT.txt","EFFICIENTAT-MIT.txt","ONNX-RUNTIME-MIT.txt"};String[] labels={"Model credits and changes","PANN weights · CC BY 4.0","PANN code · MIT","EfficientAT · MIT","ONNX Runtime · MIT"};for(int n=0;n<names.length;n++){String name=names[n];LinearLayout row=PlanActivity.detail(body,"file",labels[n],null,null,()->{go(Page.NOTICE);job.notice(name);});RecordUi.enabled(row,!job.busy());}}
    private void notice(LinearLayout body){if(job.notice.isEmpty()){if(!working())Design.text(body,"No notice is open. Choose a model credit or license.",14,400,Design.MUTED);}else Design.text(body,job.notice,12,400,Design.MUTED).setTextIsSelectable(true);RecordUi.quietButton(body,"Close model notice",()->{go(Page.LICENSES);job.hideNotice();});}
    private void clear(){Dialog dialog=new Dialog(this);clearDialog=dialog;dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);LinearLayout panel=Design.col(this);Design.pad(panel,22,22);panel.setBackground(Design.shape(this,Design.CREAM,22));Design.text(panel,"Clear saved analyses?",22,600,Design.INK);Design.space(panel,12);Design.text(panel,"Remove these experimental summaries. Original audio, sleep records and corrected labels stay unchanged.",14,400,Design.MUTED);Design.button(panel,"Clear summaries",true,()->{dialog.dismiss();job.clearSaved();});Design.button(panel,"Cancel",false,dialog::dismiss);dialog.setContentView(panel);dialog.show();dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);dialog.getWindow().setLayout(Math.min(getResources().getDisplayMetrics().widthPixels-Design.dp(this,32),Design.dp(this,420)),-2);}
    private void choose(){if(!hasWindowFocus()||!job.choosing())return;try{startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("audio/*"),DOCUMENT);}catch(ActivityNotFoundException|SecurityException unavailable){job.choiceCancelled("A document picker is unavailable. No audio was read.");}}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=DOCUMENT)return;if(result!=RESULT_OK||data==null){job.choiceCancelled(null);return;}try{if(data.getClipData()!=null&&data.getClipData().getItemCount()!=1){job.choiceCancelled("Select one audio document.");return;}go(Page.MAIN);job.selected(data.getData());}catch(RuntimeException invalid){job.choiceCancelled("This file selection could not be read.");}}
    @Override protected void onStop(){if(!isChangingConfigurations()&&(job.phase==RecordedRecognitionJob.Phase.ANALYZING||job.phase==RecordedRecognitionJob.Phase.READING))job.cancel();super.onStop();}
    @Override protected void onDestroy(){if(clearDialog!=null)clearDialog.dismiss();job.detach(this);if(!isChangingConfigurations())job.close();super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
