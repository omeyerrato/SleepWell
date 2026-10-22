package org.sleepwell.app;

import android.app.Activity;
import android.view.*;
import android.widget.*;
import org.json.JSONObject;
import java.util.*;

final class ReportScreen {
    interface Actions{void history();void tonight();void detail(long id);}
    static final class Snapshot {
        final JSONObject session;final List<JSONObject> epochs;final int clipCount,pauseCount;final long recordedMillis;
        Snapshot(JSONObject session,List<JSONObject> epochs,int clipCount){this(session,epochs,clipCount,session.isNull("active_ms")?session.optLong("end")-session.optLong("start"):session.optLong("active_ms"),0);}
        Snapshot(JSONObject session,List<JSONObject> epochs,int clipCount,long recordedMillis,int pauseCount){this.session=session;this.epochs=epochs;this.clipCount=clipCount;this.recordedMillis=recordedMillis;this.pauseCount=pauseCount;}
    }
    static void render(Activity a,LinearLayout body,Store store,Actions actions){
        RecordEntry record=Records.latest(store);Snapshot snapshot=null;
        if(record!=null){String id=""+record.id;List<JSONObject> epochs=store.rows("SELECT * FROM epochs WHERE session=? ORDER BY time",id);if(record.edited)epochs=Records.window(epochs,record.edit.start,record.edit.end);snapshot=new Snapshot(Records.display(record),epochs,store.rows("SELECT id FROM clips WHERE session=? AND time>=? AND time<=?",id,""+record.edit.start,""+record.edit.end).size(),record.duration(),record.pauses.size());}
        render(a,body,snapshot,actions);
    }
    static void render(Activity a,LinearLayout body,Snapshot snapshot,Actions actions){
        LinearLayout header=Design.row(a);TextView title=Design.text(a,"Sleep report",29,700,Design.INK);header.addView(title,new LinearLayout.LayoutParams(0,-2,1));header.addView(Design.iconButton(a,"calendar","Sleep history",actions::history),new LinearLayout.LayoutParams(Design.dp(a,48),Design.dp(a,48)));body.addView(header);Design.space(body,8);
        if(snapshot==null){
            Design.text(body,"No nights recorded",16,400,Design.MUTED);Design.space(body,64);
            LinearLayout moon=Design.col(a);moon.setGravity(Gravity.CENTER);Design.Icon icon=new Design.Icon(a,"moon",Design.VIOLET);moon.addView(icon,new LinearLayout.LayoutParams(Design.dp(a,64),Design.dp(a,64)));body.addView(moon);Design.space(body,24);
            Design.text(body,"Your first night starts here",23,600,Design.INK).setGravity(Gravity.CENTER);Design.space(body,12);Design.text(body,"Track a night to see your movement, room light and optional sound recordings.",15,400,Design.MUTED).setGravity(Gravity.CENTER);Design.space(body,32);Design.button(body,"Go to Tonight",true,actions::tonight);return;
        }
        JSONObject session=snapshot.session;long id=session.optLong("id"),start=session.optLong("start"),end=session.optLong("end");List<JSONObject> epochs=snapshot.epochs;
        java.time.ZoneId zone=session.optString("zone","").isEmpty()?java.time.ZoneId.systemDefault():java.time.ZoneId.of(session.optString("zone"));boolean manual=session.optString("source","").equals("manual"),edited=session.optBoolean("edited",false);
        String date=java.time.Instant.ofEpochMilli(end).atZone(zone).format(java.time.format.DateTimeFormatter.ofPattern("EEE, d MMM",Locale.getDefault()));Design.text(body,date,16,500,Design.MUTED);Design.space(body,14);if(manual||edited){Design.text(body,session.optString("provenance",manual?"User-reported period":"Edited period"),12,500,Design.MUTED);Design.space(body,8);}
        long seconds=Math.max(0,snapshot.recordedMillis/1000);String duration=seconds>=3600?seconds/3600+" h "+seconds/60%60+" min":seconds/60+" min "+seconds%60+" sec";
        TextView value=Design.text(body,duration,56,700,Design.INK);android.text.SpannableString durationText=new android.text.SpannableString(duration);java.util.regex.Matcher units=java.util.regex.Pattern.compile("h|min|sec").matcher(duration);while(units.find())durationText.setSpan(new android.text.style.RelativeSizeSpan(.45f),units.start(),units.end(),0);value.setText(durationText);Design.space(body,4);Design.text(body,manual?"Reported time":"Recorded time",16,500,Design.MUTED);Design.space(body,22);
        LinearLayout times=Design.row(a);LinearLayout bed=Design.col(a),wake=Design.col(a);Design.text(bed,manual||edited?"Period starts":"Recording started",12,400,Design.MUTED);Design.space(bed,6);Design.text(bed,RecordUi.clock(a,start,zone),19,600,Design.INK);Design.text(wake,manual||edited?"Period ends":"End of recording",12,400,Design.MUTED);Design.space(wake,6);Design.text(wake,RecordUi.clock(a,end,zone),19,600,Design.INK);LinearLayout startGroup=Design.row(a),endGroup=Design.row(a);Design.Icon bedIcon=new Design.Icon(a,"bed",Design.BROWN),sunIcon=new Design.Icon(a,"sun",Design.BROWN);LinearLayout.LayoutParams iconSpace=new LinearLayout.LayoutParams(Design.dp(a,24),Design.dp(a,24));iconSpace.rightMargin=Design.dp(a,12);startGroup.addView(bedIcon,iconSpace);startGroup.addView(bed,new LinearLayout.LayoutParams(0,-2,1));LinearLayout.LayoutParams endIconSpace=new LinearLayout.LayoutParams(Design.dp(a,24),Design.dp(a,24));endIconSpace.rightMargin=Design.dp(a,12);endGroup.addView(sunIcon,endIconSpace);endGroup.addView(wake,new LinearLayout.LayoutParams(0,-2,1));times.addView(startGroup,new LinearLayout.LayoutParams(0,-2,1));View divider=new View(a);divider.setBackgroundColor(Design.LINE);LinearLayout.LayoutParams dividerSpace=new LinearLayout.LayoutParams(Design.dp(a,1),Design.dp(a,44));dividerSpace.setMargins(Design.dp(a,10),0,Design.dp(a,16),0);times.addView(divider,dividerSpace);times.addView(endGroup,new LinearLayout.LayoutParams(0,-2,1));body.addView(times);Design.space(body,24);
        int startHour=java.time.Instant.ofEpochMilli(start).atZone(zone).getHour();String movementTitle=end-start>=4*3600000L&&(startHour>=18||startHour<5)?"Overnight movement":"Recorded movement";
        Design.text(body,movementTitle,16,600,Design.INK);if(manual){Design.space(body,10);Design.text(body,"No sensor measurements were made for this manual entry.",14,400,Design.MUTED);Design.space(body,20);}else body.addView(new RecordChart(a,epochs,"movement",start,end,false,zone),new LinearLayout.LayoutParams(-1,Design.dp(a,180)));Design.space(body,14);
        Design.text(body,"Sleep stages",16,600,Design.INK);Design.space(body,8);Design.text(body,manual?"No measured or estimated stages for a manual entry.":"Unavailable · phone motion doesn't measure sleep stages. Recorded time includes time awake unless you mark it.",13,400,Design.MUTED);if(session.optBoolean("duration_limited",false)){Design.space(body,8);Design.text(body,"Duration was limited to the original record interval because its saved timing metadata is inconsistent.",13,500,Design.BROWN);}if(edited&&!manual){Design.space(body,8);Design.text(body,"Trimmed display uses original minute averages. Awake edits change duration, not sensor measurements.",12,400,Design.MUTED);}if(session.optInt("clock_changed")==1){Design.space(body,8);Design.text(body,"Phone clock changed. The timeline retains its original clock reference.",13,400,Design.MUTED);}if(snapshot.pauseCount>0){Design.space(body,8);Design.text(body,"Paused time is excluded from recorded duration.",13,400,Design.MUTED);}if(session.optInt("interrupted")==1){Design.space(body,8);Design.text(body,"This recording was interrupted. Some intervals may be missing.",13,500,Design.BROWN);}Design.space(body,24);
        Design.text(body,"Sound events",16,600,Design.INK);Design.space(body,8);Design.text(body,snapshot.clipCount==0?"No sound clips saved for this night.":snapshot.clipCount+" saved sound clips",14,400,Design.MUTED);
        Design.item(body,"wave","View recorded signals & notes","",()->actions.detail(id));
    }
}
