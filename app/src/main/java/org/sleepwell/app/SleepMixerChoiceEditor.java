package org.sleepwell.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.util.*;

/** One silent draft. Row taps select; only Use changes the current mix and timer. */
final class SleepMixerChoiceEditor {
    private static final int INK=0xfff7efff,MUTED=0xffd0bce9,PANEL=0xff4b3e60;
    private final Activity activity;private final Runnable finished;private final SharedPreferences preferences;
    private SoundMix previous;private String kind,error="";private long revision;private int value;
    SleepMixerChoiceEditor(Activity activity,String kind,Bundle state,Runnable finished){this.activity=activity;this.finished=finished;preferences=SoundPreferences.factory.open(activity);this.kind=kind;try{previous=SoundPreferences.load(preferences).copy();revision=SoundPreferences.revision(preferences);if(!Set.of("timer","fade").contains(kind))throw new IllegalArgumentException();value=kind.equals("timer")?previous.timerMinutes:previous.fadeMinutes;if(state!=null){this.kind=state.getString("kind",kind);revision=state.getLong("revision",revision);value=state.getInt("value",value);error=state.getString("error","");if(!this.kind.equals(kind)||value<(kind.equals("timer")?1:0)||value>(kind.equals("timer")?240:previous.timerMinutes))throw new IllegalArgumentException();}}catch(RuntimeException invalid){previous=null;error="The mix changed or could not be read. Cancel and reopen this setting.";}}
    Bundle state(){Bundle out=new Bundle();out.putString("kind",kind);out.putInt("value",value);out.putLong("revision",revision);out.putString("error",error);return out;}
    void render(){LinearLayout body=Design.darkPage(activity);RecordUi.label(activity,body);LinearLayout header=Design.row(activity);Design.Icon back=(Design.Icon)Design.iconButton(activity,"back","Cancel "+kind,finished);back.tint(INK);header.addView(back,new LinearLayout.LayoutParams(dp(48),dp(48)));header.addView(Design.text(activity,kind.equals("timer")?"Sound timer":"Fade out",25,600,INK),new LinearLayout.LayoutParams(0,-2,1));body.addView(header);Design.space(body,16);
        Design.text(body,kind.equals("timer")?"Stop after this much playing time. Pause freezes the timer.":"Lower the mix level during the final part of its timer.",14,400,MUTED);Design.space(body,16);
        if(previous==null){Design.text(body,error,14,500,Design.LILAC);SleepMixerUi.button(body,"Cancel",false,finished);return;}
        SortedSet<Integer> values=new TreeSet<>();for(int n:kind.equals("timer")?new int[]{15,30,45,60,90,120,240}:new int[]{0,1,2,5,10,15})if(kind.equals("timer")||n<=previous.timerMinutes)values.add(n);values.add(kind.equals("timer")?previous.timerMinutes:previous.fadeMinutes);values.add(value);
        for(int n:values){boolean selected=value==n;LinearLayout row=Design.row(activity);Design.pad(row,14,8);row.setMinimumHeight(dp(56));row.setBackground(Design.touch(activity,selected?PANEL:android.graphics.Color.TRANSPARENT,14));Design.Icon indicator=new Design.Icon(activity,"check",Design.LILAC);indicator.setVisibility(selected?View.VISIBLE:View.INVISIBLE);row.addView(indicator,new LinearLayout.LayoutParams(dp(24),dp(24)));TextView label=Design.text(activity,n==0?"Off":n+" min",16,selected?600:400,INK);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.leftMargin=dp(14);row.addView(label,lp);SleepPicker.radio(row,selected);row.setFocusable(true);row.setContentDescription(n==0?"Off":n+" minutes");row.setOnClickListener(v->{if(value!=n)InteractionFeedback.selection(v);value=n;error="";render();});body.addView(row);Design.space(body,4);}
        Design.space(body,12);if(kind.equals("timer")&&previous.fadeMinutes>value)Design.text(body,"The existing fade will shorten to "+value+" min.",13,500,MUTED);Design.text(body,"Use applies this choice to the saved mix and any current playback. No sound starts here.",12,400,MUTED);if(!error.isEmpty()){Design.space(body,10);Design.text(body,error,13,500,Design.LILAC);}Design.space(body,16);SleepMixerUi.button(body,kind.equals("timer")?"Use "+value+" min timer":value==0?"Use no fade":"Use "+value+" min fade",true,this::save);SleepMixerUi.button(body,"Cancel",false,finished);
    }
    private int dp(int n){return Design.dp(activity,n);}
    private void save(){if(previous==null)return;try{SoundMix next=previous.copy();if(kind.equals("timer")){next.timerMinutes=value;next.fadeMinutes=Math.min(next.fadeMinutes,value);}else next.fadeMinutes=value;next.validate();if(!SoundPreferences.saveIfRevision(preferences,revision,next))throw new IllegalStateException("The mix changed while this choice was open. Cancel and reopen it before saving.");SleepSoundService.update(activity);finished.run();}catch(RuntimeException failure){error=failure.getMessage()==null?"This setting could not be saved.":failure.getMessage();render();}}
}
