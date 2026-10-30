package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.time.*;
import java.util.function.Consumer;

/** Bedtime-to-alarm plan, not fabricated sensor or sleep-stage data. */
final class NightArc extends View {
    private final Paint p=new Paint(3);private final Design.Icon moon,sun;private final int bedtime,wake;private final long alarmAt;private final NapPlan nap;private final NapPlan.Clock napClock;private final Consumer<NightWindow> changed;private ZoneId zone;private NightWindow window;private android.app.Dialog explanation;
    NightArc(Context c,int bedtime,int wake,long alarmAt,long now,ZoneId zone,Consumer<NightWindow> changed){super(c);moon=new Design.Icon(c,"moon",Design.PLUM);sun=new Design.Icon(c,"sun",Design.BROWN);nap=null;napClock=null;this.bedtime=bedtime;this.wake=wake;this.alarmAt=alarmAt;this.changed=changed;update(now,zone);setFocusable(true);setOnClickListener(v->{InteractionFeedback.action(v);explain();});}
    NightArc(Context c,NapPlan nap,NapPlan.Clock clock,ZoneId zone,Consumer<NightWindow> changed){super(c);moon=new Design.Icon(c,"moon",Design.PLUM);sun=new Design.Icon(c,"sun",Design.BROWN);this.nap=nap;napClock=clock;bedtime=wake=0;alarmAt=nap.projectedDeadline(clock);this.changed=changed;update(clock.wall(),zone);setFocusable(true);setOnClickListener(v->{InteractionFeedback.action(v);explain();});}
    @Override public CharSequence getAccessibilityClassName(){return android.widget.Button.class.getName();}
    boolean needsNewOccurrence(long now,ZoneId currentZone){return !zone.equals(currentZone)||(nap!=null?nap.remaining(napClock)<0:alarmAt>0&&now>alarmAt);}
    boolean explanationShowing(){return explanation!=null&&explanation.isShowing();}
    void restoreExplanation(){if(isAttachedToWindow())explain();}
    private void explain(){
        if(explanation!=null&&explanation.isShowing())return;
        Context context=getContext();Appearance.Palette palette=Appearance.palette(context);android.app.Dialog dialog=new android.app.Dialog(context);dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        android.widget.ScrollView scroll=new android.widget.ScrollView(context);android.widget.LinearLayout body=Design.col(context);Design.pad(body,24,24);body.setBackground(Design.shape(context,palette.dark?palette.panel:Design.CREAM,24));scroll.addView(body);dialog.setContentView(scroll);
        Design.text(body,nap==null?"Your rest window":"Your nap timer",23,600,palette.dark?palette.ink:Design.INK);Design.space(body,16);
        Design.text(body,nap==null?"Bedtime → wake-up":"Nap starts → nap ends",16,600,palette.dark?palette.ink:Design.INK);Design.space(body,10);
        Design.text(body,nap==null?"Amber marks your planned bedtime-to-wake-up window on a 24-hour clock. The small Now marker follows the clock during that window; it does not measure sleep.":"Amber marks your chosen nap window. The Now marker follows the timer while it runs. It does not measure whether you are asleep.",15,400,palette.dark?palette.softInk:Design.MUTED);Design.space(body,16);
        Design.text(body,nap==null?"Uses your next alarm, or your sleep plan when alarms are off. Your measured movement is in Report.":"The timer keeps its remaining duration when the clock or time zone changes on this boot. Smart wake-up may end it early only with active movement tracking.",14,400,palette.dark?palette.softInk:Design.MUTED);Design.space(body,12);
        android.widget.Button close=Design.button(body,"Got it",true,dialog::dismiss);close.setContentDescription("Close rest window explanation");
        explanation=dialog;dialog.setOnDismissListener(v->{if(explanation==dialog)explanation=null;});SleepPicker.show((android.app.Activity)context,dialog,390);
    }
    @Override protected void onDetachedFromWindow(){if(explanation!=null)explanation.dismiss();super.onDetachedFromWindow();}
    void update(long now,ZoneId zone){this.zone=zone;window=nap==null?NightWindow.at(bedtime,wake,alarmAt,now,zone):NightWindow.nap(nap,napClock,zone);String description=window.valid?(nap==null?"Rest window from ":"Nap starts ")+clock(window.start)+(nap==null?" to ":". Nap ends ")+clock(window.end)+". "+duration(window.durationMinutes())+" planned. "+status()+". "+(window.active?Math.round(window.fraction*100)+" percent of clock time elapsed. ":"")+"Tap for an explanation.":"Rest window unavailable. Choose different bedtime and wake-up times. Tap for an explanation.";setContentDescription(description);changed.accept(window);invalidate();}
    private String clock(long instant){java.time.ZonedDateTime local=Instant.ofEpochMilli(instant).atZone(zone);return Design.clock(getContext(),local.getHour()*60+local.getMinute());}
    private String duration(long minutes){return minutes>=60?minutes/60+" h"+(minutes%60>0?" "+minutes%60+" min":""):minutes+" min";}
    private String status(){if(nap!=null)return window.now>window.end?"Nap ended":window.active?duration(window.remainingMinutes())+" until nap ends":"Nap starts in "+duration(window.remainingMinutes());return !window.valid?"Choose a sleep plan":window.active?(window.remainingMinutes()==0?"Wake-up time":duration(window.remainingMinutes())+" until wake-up"):"Bedtime in "+duration(window.remainingMinutes());}
    @Override protected void onMeasure(int widthSpec,int heightSpec){int width=resolveSize(Design.dp(getContext(),340),widthSpec);setMeasuredDimension(width,resolveSize(Math.round(width*.96f),heightSpec));}
    private float angle(long instant){ZonedDateTime time=Instant.ofEpochMilli(instant).atZone(zone);return (time.getHour()*60+time.getMinute())/4f-90;}
    private void endpoint(Canvas canvas,float degrees,Design.Icon icon){double angle=Math.toRadians(degrees);float x=180+130*(float)Math.cos(angle),y=151+130*(float)Math.sin(angle);p.setStyle(Paint.Style.FILL);p.setColor(NightDialTokens.GROUND);canvas.drawCircle(x,y,12,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);p.setColor(NightDialTokens.ACCENT);canvas.drawCircle(x,y,12,p);canvas.save();canvas.translate(x-8,y-8);icon.tint(NightDialTokens.ACCENT);icon.layout(0,0,16,16);icon.draw(canvas);canvas.restore();}
    @Override protected void onDraw(Canvas c){super.onDraw(c);if(getWidth()==0)return;float s=getWidth()/360f,scale=Design.sp(getContext(),12*Appearance.scale(getContext()))/(12*s);c.save();c.scale(s,s);
        RectF ring=new RectF(50,21,310,281);p.setShader(null);p.setStrokeCap(Paint.Cap.ROUND);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setColor(NightDialTokens.RULE);c.drawOval(ring,p);
        // Both endpoints use local clock time. Duration text uses the actual interval, including DST.
        if(window.valid){float start=angle(window.start),end=angle(window.end),sweep=(end-start+360)%360;if(sweep==0&&window.durationMinutes()>0)sweep=360;p.setStrokeWidth(4);p.setColor(NightDialTokens.ACCENT);c.drawArc(ring,start,sweep,false,p);}
        p.setStrokeCap(Paint.Cap.BUTT);for(int n=0;n<96;n++){double angle=Math.toRadians(n*3.75-90);float inner=n%4==0?112:119;float outer=123;p.setColor(n%4==0?NightDialTokens.OUTLINE:NightDialTokens.RULE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(n%4==0?1.1f:.8f);c.drawLine(180+inner*(float)Math.cos(angle),151+inner*(float)Math.sin(angle),180+outer*(float)Math.cos(angle),151+outer*(float)Math.sin(angle),p);}
        p.setStyle(Paint.Style.FILL);p.setTypeface(Design.font(getContext(),400));p.setTextAlign(Paint.Align.CENTER);p.setColor(NightDialTokens.META);p.setTextSize(10*scale);for(int hour=0;hour<24;hour+=2){double a=Math.toRadians(hour*15-90);c.drawText(String.format(java.util.Locale.ROOT,"%02d",hour),180+99*(float)Math.cos(a),151+99*(float)Math.sin(a)-(p.ascent()+p.descent())/2,p);}
        // Endpoint wells sit above ticks; a tick must never slash through the moon/sun glyph.
        if(window.valid){endpoint(c,angle(window.start),moon);endpoint(c,angle(window.end),sun);}
        p.setStyle(Paint.Style.FILL);if(window.active){double now=Math.toRadians(angle(window.now));p.setColor(NightDialTokens.TEXT);c.drawCircle(180+130*(float)Math.cos(now),151+130*(float)Math.sin(now),4,p);}
        String total=window.valid?compactDuration(window.durationMinutes()):"Set a plan";p.setTypeface(Design.font(getContext(),500));p.setTextSize(32*scale);if(p.measureText(total)>166)p.setTextSize(p.getTextSize()*166/p.measureText(total));p.setColor(NightDialTokens.TEXT);c.drawText(total,180,150,p);
        p.setTypeface(Design.font(getContext(),400));p.setTextSize(14*scale);p.setColor(NightDialTokens.SECONDARY);c.drawText("planned",180,174,p);
        p.setTextSize(14*scale);p.setTypeface(Design.font(getContext(),600));p.setColor(NightDialTokens.TEXT);p.setTextAlign(Paint.Align.LEFT);c.drawText(window.valid?clock(window.start):Design.clock(getContext(),bedtime),16,313,p);p.setTextAlign(Paint.Align.RIGHT);c.drawText(window.valid?clock(window.end):Design.clock(getContext(),wake),344,313,p);
        p.setTextSize(12*scale);p.setTypeface(Design.font(getContext(),400));p.setColor(NightDialTokens.SECONDARY);p.setTextAlign(Paint.Align.LEFT);c.drawText(nap==null?"Bedtime":"Nap starts",16,335,p);p.setTextAlign(Paint.Align.RIGHT);c.drawText(nap==null?"Wake-up":"Nap ends",344,335,p);c.restore();
    }
    private static String compactDuration(long minutes){return minutes>=60?minutes/60+"h"+(minutes%60>0?" "+minutes%60+"m":""):minutes+"m";}
}
