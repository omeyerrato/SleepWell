package org.sleepwell.app;
import android.app.*;import android.widget.*;import java.util.*;import java.util.function.*;

/** Draft-only configuration. No output, grant acquisition or scheduler work. */
final class AlarmSoundModesUi {
    static void render(Activity activity,LinearLayout body,Alarm alarm,Runnable redraw,Runnable back,Consumer<Supplier<Dialog>> open){
        Design.header(body,"Sound playback",back);Design.space(body,16);
        Design.item(body,"sound","Playback mode",alarm.soundModeInvalid?"Review sound mode":alarm.soundMode.title(),()->open.accept(()->SleepPicker.choices(activity,"Playback mode",new String[]{"Single sound","Silent","Playlist"},alarm.soundMode.kind.ordinal(),n->{alarm.soundMode=n==0?AlarmSoundMode.SINGLE:n==1?AlarmSoundMode.SILENT:alarm.soundMode.playlist()?alarm.soundMode:new AlarmSoundMode(AlarmSoundMode.Kind.PLAYLIST,List.of("daybreak","pulse"),false);alarm.soundModeInvalid=false;redraw.run();}))).setContentDescription("Playback mode");Design.line(body);
        if(alarm.soundMode.silent()){Design.space(body,18);Design.text(body,"No alarm sound, including backup sound. Vibration, lights and challenges keep their own settings.",14,400,Design.MUTED);}
        else if(alarm.soundMode.playlist()){
            Design.toggle(body,"Shuffle order","One shuffled order for each alarm occurrence",alarm.soundMode.shuffle,on->{alarm.soundMode=alarm.soundMode.changed(alarm.soundMode.tracks,on);redraw.run();});Design.line(body);Design.space(body,12);
            for(int n=0;n<alarm.soundMode.tracks.size();n++){int index=n;String source=alarm.soundMode.tracks.get(n);LinearLayout row=Design.row(activity);row.addView(Design.text(activity,(n+1)+". "+alarm.soundMode.name(source),16,500,Design.INK),new LinearLayout.LayoutParams(0,-2,1));if(n>0)row.addView(Design.iconButton(activity,"up","Move sound "+(n+1)+" up",()->{List<String> tracks=new ArrayList<>(alarm.soundMode.tracks);Collections.swap(tracks,index,index-1);alarm.soundMode=alarm.soundMode.changed(tracks,alarm.soundMode.shuffle);redraw.run();}),new LinearLayout.LayoutParams(Design.dp(activity,48),Design.dp(activity,48)));if(alarm.soundMode.tracks.size()>2)row.addView(Design.iconButton(activity,"trash","Remove sound "+(n+1),()->{List<String> tracks=new ArrayList<>(alarm.soundMode.tracks);tracks.remove(index);alarm.soundMode=alarm.soundMode.changed(tracks,alarm.soundMode.shuffle);redraw.run();}),new LinearLayout.LayoutParams(Design.dp(activity,48),Design.dp(activity,48)));body.addView(row);Design.line(body);}
            List<String> choices=new ArrayList<>(List.of("daybreak","pulse","chime","system"));if(AlarmSoundMode.source(alarm.tone)&&alarm.tone.startsWith("content:"))choices.add(alarm.tone);choices.removeAll(alarm.soundMode.tracks);
            if(alarm.soundMode.tracks.size()<8&&!choices.isEmpty())Design.item(body,"plus","Add sound","Built-in or your selected imported audio",()->open.accept(()->SleepPicker.choices(activity,"Add sound",choices.stream().map(AlarmTones::name).toArray(String[]::new),-1,n->{alarm.soundMode=alarm.soundMode.add(choices.get(n),alarm.toneDisplayName);redraw.run();})));
            Design.space(body,12);Design.text(body,"Each sound finishes before the next. The list repeats. To add a file, import it in Alarm sound, then add it here.",13,400,Design.MUTED);
        }else{Design.space(body,18);Design.text(body,"Repeats your selected alarm sound.",14,400,Design.MUTED);}
        if(alarm.soundModeInvalid)Design.text(body,"Saved playback settings could not be read. Choose a mode before saving.",13,500,0xffa93919);
        Design.space(body,16);Design.fixedAction(body,"Use playback settings",back);
    }
}
