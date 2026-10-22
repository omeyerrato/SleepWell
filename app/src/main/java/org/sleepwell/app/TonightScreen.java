package org.sleepwell.app;

import android.app.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Tonight composition from board 01. All displayed plan values come from stored settings. */
final class TonightScreen {
    interface Actions {void alarms();void bedtime();void windDown();void settings();void start();void activeAlarm();void confirmCheck();void update(Alarm alarm);}
    static void render(Activity a,LinearLayout body,Store store,Actions actions){
        Design.pad(body,12,10);int width=a.getResources().getDisplayMetrics().widthPixels;
        LinearLayout header=Design.row(a);header.setPadding(Design.dp(a,10),0,Design.dp(a,4),0);TextView title=Design.text(a,"Tonight",26,600,Design.CREAM);header.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        View settings=Design.iconButton(a,"settings","Settings",actions::settings);((Design.Icon)settings).tint(Design.CREAM);header.addView(settings,new LinearLayout.LayoutParams(Design.dp(a,48),Design.dp(a,48)));body.addView(header);
        Alarm chosen=null;long nearest=Long.MAX_VALUE;for(Alarm alarm:store.alarms())if(alarm.enabled){long deadline=Prefs.of(a).getLong("deadline_"+alarm.id,alarm.next(System.currentTimeMillis()));if(deadline<nearest){nearest=deadline;chosen=alarm;}}
        if(chosen==null&&!store.alarms().isEmpty())chosen=store.alarms().get(0);final Alarm alarm=chosen;
        TextView next=Design.text(body,alarm!=null&&alarm.enabled?"NEXT ALARM":"WAKE-UP PLAN",12,500,Design.LILAC);next.setGravity(Gravity.CENTER);Design.space(body,6);
        TextView time=Design.text(body,alarm==null?"Set an alarm":AlarmEditorActivity.displayTime(alarm),alarm==null?40:58,700,Design.LILAC);if(alarm!=null)time.setText(Design.timeText(AlarmEditorActivity.displayTime(alarm)));time.setGravity(Gravity.CENTER);time.setMinimumHeight(Design.dp(a,68));time.setContentDescription("Alarms & challenges");time.setOnClickListener(v->actions.alarms());
        int bed=Prefs.number(a,"bedtime",1350),wake=alarm==null?420:alarm.hour*60+alarm.minute;
        LinearLayout arcBox=Design.col(a);arcBox.setPadding(Design.dp(a,8),0,Design.dp(a,8),0);NightArc arc=new NightArc(a,bed,wake,alarm!=null&&alarm.enabled);arcBox.addView(arc,new LinearLayout.LayoutParams(-1,Design.dp(a,130)));body.addView(arcBox);Design.space(body,10);
        LinearLayout panel=Design.col(a);panel.setBackground(Design.shape(a,0xff3a3046,22));panel.setClipToOutline(true);body.addView(panel,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout options=Design.col(a);options.setPadding(Design.dp(a,16),Design.dp(a,8),Design.dp(a,16),0);panel.addView(options);
        LinearLayout enabledRow=Design.row(a);Design.Icon icon=new Design.Icon(a,"alarm",Design.CREAM);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(Design.dp(a,26),Design.dp(a,26));ip.rightMargin=Design.dp(a,20);enabledRow.addView(icon,ip);
        TextView label=Design.text(a,alarm!=null&&alarm.enabled?"Alarm on":"Alarm off",17,500,Design.CREAM);enabledRow.addView(label,new LinearLayout.LayoutParams(0,-2,1));
        Switch enabled=new Switch(a);enabled.setContentDescription("Tonight alarm enabled");enabled.setMinHeight(Design.dp(a,48));enabled.setChecked(alarm!=null&&alarm.enabled);enabled.setThumbTintList(ColorStateList.valueOf(0xfffcf9ff));enabled.setTrackTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{Design.VIOLET,0xff6a6076}));enabledRow.addView(enabled);options.addView(enabledRow);
        enabled.setOnCheckedChangeListener((v,on)->{if(alarm==null){actions.alarms();return;}if(on&&!Scheduler.allowed(a)){enabled.setChecked(false);actions.settings();return;}alarm.enabled=on;actions.update(alarm);});
        if(alarm!=null){LinearLayout weekdays=Design.row(a);String[] letters={"M","T","W","T","F","S","S"},names={"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
            for(int n=1;n<=7;n++){final int day=n;Button b=new Button(a);b.setStateListAnimator(null);b.setElevation(0);b.setText(letters[n-1]);b.setTextSize(12);b.setTypeface(Design.font(a,600));b.setTextColor(alarm.days.contains(n)?Design.PLUM:Design.LILAC);b.setMinWidth(0);b.setMinimumWidth(0);b.setPadding(0,0,0,0);b.setContentDescription(names[n-1]);b.setSelected(alarm.days.contains(n));
                GradientDrawable circle=Design.shape(a,alarm.days.contains(n)?Design.VIOLET:0xff302839,50);InsetDrawable inset=new InsetDrawable(circle,Design.dp(a,10));b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff),inset,null));b.setOnClickListener(v->{if(alarm.days.contains(day))alarm.days.remove(day);else alarm.days.add(day);actions.update(alarm);});weekdays.addView(b,new LinearLayout.LayoutParams(0,Design.dp(a,48),1));}options.addView(weekdays);}
        divider(options);nightRow(options,"bed","Bedtime goal",clock(bed),null,actions::bedtime);divider(options);
        nightRow(options,"moon","Wind-down at",clock((bed-30+1440)%1440),"Put your phone down before bed.",actions::windDown);
        ImageView illustration=new ImageView(a);illustration.setImageResource(R.drawable.bedside_tonight);illustration.setScaleType(ImageView.ScaleType.CENTER_CROP);illustration.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);FrameLayout art=new FrameLayout(a);art.addView(illustration,new FrameLayout.LayoutParams(-1,-1));View blend=new View(a);blend.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xff3a3046,0x003a3046}));art.addView(blend,new FrameLayout.LayoutParams(-1,Design.dp(a,28),Gravity.TOP));panel.addView(art,new LinearLayout.LayoutParams(-1,Design.dp(a,122)));
        Design.space(body,2);LinearLayout actionRow=Design.row(a);actionRow.setGravity(Gravity.CENTER);actionRow.setMinimumHeight(Design.dp(a,56));actionRow.setBackground(Design.touch(a,Design.LILAC,18));LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,-2);ap.topMargin=Design.dp(a,10);body.addView(actionRow,ap);Design.Icon bars=new Design.Icon(a,"report",Design.PLUM);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(Design.dp(a,23),Design.dp(a,23));bp.rightMargin=Design.dp(a,14);actionRow.addView(bars,bp);TextView start=Design.text(a,TrackingService.running?"End night & review":"Start sleep tracking",16,600,Design.PLUM);actionRow.addView(start);actionRow.setOnClickListener(v->actions.start());actionRow.setContentDescription(start.getText());
        if(Prefs.flag(a,"ringing"))Design.button(body,"Return to active alarm",false,actions::activeAlarm);
        if(Prefs.number(a,"checkPending",0)!=0)Design.button(body,"I'm awake · confirm wake-up check",false,actions::confirmCheck);
    }
    private static String clock(int minutes){Alarm a=new Alarm(0);a.hour=minutes/60;a.minute=minutes%60;return AlarmEditorActivity.displayTime(a);}
    private static void divider(LinearLayout p){View line=new View(p.getContext());line.setBackgroundColor(0xff62566d);p.addView(line,new LinearLayout.LayoutParams(-1,Design.dp(p.getContext(),1)));}
    private static void nightRow(LinearLayout p,String icon,String title,String value,String subtitle,Runnable click){android.content.Context c=p.getContext();LinearLayout row=Design.row(c);row.setMinimumHeight(Design.dp(c,subtitle==null?48:64));Design.Icon i=new Design.Icon(c,icon,Design.LILAC);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(Design.dp(c,24),Design.dp(c,24));ip.rightMargin=Design.dp(c,20);row.addView(i,ip);LinearLayout text=Design.col(c);LinearLayout head=Design.row(c);head.addView(Design.text(c,title,14,400,Design.LILAC),new LinearLayout.LayoutParams(0,-2,1));head.addView(Design.text(c,value,19,600,Design.LILAC));text.addView(head);if(subtitle!=null){Design.space(text,4);Design.text(text,subtitle,12,400,Design.LILAC);}row.addView(text,new LinearLayout.LayoutParams(0,-2,1));Design.Icon arrow=new Design.Icon(c,"chevron",Design.CREAM);LinearLayout.LayoutParams ar=new LinearLayout.LayoutParams(Design.dp(c,18),Design.dp(c,18));ar.leftMargin=Design.dp(c,10);row.addView(arrow,ar);row.setBackground(Design.touch(c,Color.TRANSPARENT,8));row.setOnClickListener(v->click.run());p.addView(row);}
}
