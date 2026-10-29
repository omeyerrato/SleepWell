package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.time.*;
import java.util.function.Consumer;

/** Bedtime-to-alarm plan, not fabricated sensor or sleep-stage data. */
final class NightArc extends View {
    private final Paint p=new Paint(3);private final Design.Icon moon,sun;private final RectF labelBounds=new RectF(),tickBounds=new RectF();private final int bedtime,wake;private final long alarmAt;private final NapPlan nap;private final NapPlan.Clock napClock;private final Consumer<NightWindow> changed;private ZoneId zone;private NightWindow window;private android.app.Dialog explanation;
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
        Design.text(body,nap==null?"Empty before bedtime. Fills from bedtime to wake-up as time passes. The Now marker follows the clock, not your sleep.":"The curve shows only your chosen nap duration. It stays empty before the scheduled start and fills as the timer runs. It does not measure whether you are asleep.",15,400,palette.dark?palette.softInk:Design.MUTED);Design.space(body,16);
        Design.text(body,nap==null?"Uses your next alarm, or your sleep plan when alarms are off. Your measured movement is in Report.":"The timer keeps its remaining duration when the clock or time zone changes on this boot. Smart wake-up may end it early only with active movement tracking.",14,400,palette.dark?palette.softInk:Design.MUTED);Design.space(body,12);
        android.widget.Button close=Design.button(body,"Got it",true,dialog::dismiss);close.setContentDescription("Close rest window explanation");close.setBackground(Design.touch(context,palette.accent,16));close.setTextColor(palette.buttonInk);
        explanation=dialog;dialog.setOnDismissListener(v->{if(explanation==dialog)explanation=null;});dialog.show();android.view.Window window=dialog.getWindow();window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));window.setLayout(context.getResources().getDisplayMetrics().widthPixels-Design.dp(context,32),android.view.ViewGroup.LayoutParams.WRAP_CONTENT);window.setNavigationBarContrastEnforced(false);androidx.core.view.WindowInsetsControllerCompat bars=androidx.core.view.WindowCompat.getInsetsController(window,window.getDecorView());bars.setAppearanceLightStatusBars(!palette.dark);bars.setAppearanceLightNavigationBars(!palette.dark);
    }
    @Override protected void onDetachedFromWindow(){if(explanation!=null)explanation.dismiss();super.onDetachedFromWindow();}
    void update(long now,ZoneId zone){this.zone=zone;window=nap==null?NightWindow.at(bedtime,wake,alarmAt,now,zone):NightWindow.nap(nap,napClock,zone);String description=window.valid?(nap==null?"Rest window from ":"Nap starts ")+clock(window.start)+(nap==null?" to ":". Nap ends ")+clock(window.end)+". "+duration(window.durationMinutes())+" planned. "+status()+". "+(window.active?Math.round(window.fraction*100)+" percent of clock time elapsed. ":"")+"Tap for an explanation.":"Rest window unavailable. Choose different bedtime and wake-up times. Tap for an explanation.";setContentDescription(description);changed.accept(window);invalidate();}
    private String clock(long instant){java.time.ZonedDateTime local=Instant.ofEpochMilli(instant).atZone(zone);return Design.clock(getContext(),local.getHour()*60+local.getMinute());}
    private String duration(long minutes){return minutes>=60?minutes/60+" h"+(minutes%60>0?" "+minutes%60+" min":""):minutes+" min";}
    private String status(){if(nap!=null)return window.now>window.end?"Nap ended":window.active?duration(window.remainingMinutes())+" until nap ends":"Nap starts in "+duration(window.remainingMinutes());return !window.valid?"Choose a sleep plan":window.active?(window.remainingMinutes()==0?"Wake-up time":duration(window.remainingMinutes())+" until wake-up"):"Bedtime in "+duration(window.remainingMinutes());}
    private void badge(Canvas canvas,float x,float y,int background,int ink,Design.Icon icon){p.setStyle(Paint.Style.FILL);p.setColor(background);canvas.drawCircle(x,y,17,p);canvas.save();canvas.translate(x-12,y-12);icon.tint(ink);icon.layout(0,0,24,24);icon.draw(canvas);canvas.restore();}
    @Override protected void onDraw(Canvas c){super.onDraw(c);Appearance.Palette palette=Appearance.palette(getContext());float w=getWidth(),s=w/360f,scale=Design.sp(getContext(),13*Appearance.scale(getContext()))/(13*s),extra=Math.max(0,scale-1),offset=6+32*extra;c.save();c.scale(s,s);
        // The plan is one continuous curve. Its accent is a trimmed portion of that
        // exact path, so stroke edges and tangents cannot separate at the join.
        Path arc=new Path();arc.moveTo(30,72+offset);arc.quadTo(180,-22+offset,330,72+offset);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(20);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(palette.line);c.drawPath(arc,p);
        PathMeasure measure=new PathMeasure(arc,false);if((window.active||nap!=null)&&window.fraction>0){Path active=new Path();measure.getSegment(0,(float)(measure.getLength()*window.fraction),active,true);p.setShader(new LinearGradient(30,0,330,0,new int[]{palette.dark?Design.VIOLET:palette.accent,palette.accent},null,Shader.TileMode.CLAMP));c.drawPath(active,p);p.setShader(null);}
        p.setTypeface(Design.font(getContext(),500));p.setTextSize(13*scale);String planned=window.valid?duration(window.durationMinutes())+" planned":"Rest window";float half=p.measureText(planned)/2+5,baseline=72+30*extra;labelBounds.set(180-half,baseline+p.ascent()-4,180+half,baseline+p.descent()+4);
        p.setStyle(Paint.Style.FILL);p.setColor(palette.softInk);float[] position=new float[2];for(int i=1;i<=9;i++){measure.getPosTan(measure.getLength()*i/10f,position,null);float y=position[1]+17;tickBounds.set(position[0]-2,y-2,position[0]+2,y+2);if(!RectF.intersects(labelBounds,tickBounds))c.drawCircle(position[0],y,1.7f,p);}
        badge(c,30,72+offset,palette.softInk,palette.bg,moon);
        badge(c,330,72+offset,0xfff7ce83,Design.BROWN,sun);
        if(window.active){measure.getPosTan((float)(measure.getLength()*window.fraction),position,null);if(Math.hypot(position[0]-30,position[1]-72-offset)>23&&Math.hypot(position[0]-330,position[1]-72-offset)>23){p.setColor(palette.ink);c.drawCircle(position[0],position[1],5,p);p.setColor(palette.bg);c.drawCircle(position[0],position[1],2.4f,p);}p.setTypeface(Design.font(getContext(),600));p.setTextSize(11*scale);p.setColor(palette.ink);p.setTextAlign(Paint.Align.CENTER);c.drawText("Now",Math.max(36*scale,Math.min(360-36*scale,position[0])),position[1]-18*scale,p);}
        p.setTypeface(Design.font(getContext(),500));p.setTextSize(13*scale);p.setColor(palette.ink);p.setTextAlign(Paint.Align.CENTER);c.drawText(window.valid?duration(window.durationMinutes())+" planned":"Rest window",180,72+30*extra,p);
        p.setTypeface(Design.font(getContext(),400));p.setTextSize(11*scale);p.setColor(palette.softInk);String clockStatus="Clock time · "+status();c.drawText(android.text.TextUtils.ellipsize(clockStatus,new android.text.TextPaint(p),340,android.text.TextUtils.TruncateAt.END).toString(),180,93+46*extra,p);
        p.setTypeface(Design.font(getContext(),600));p.setTextSize(15*scale);p.setColor(palette.softInk);p.setTextAlign(Paint.Align.LEFT);c.drawText(window.valid?clock(window.start):Design.clock(getContext(),bedtime),0,115+62*extra,p);p.setTextAlign(Paint.Align.RIGHT);c.drawText(window.valid?clock(window.end):Design.clock(getContext(),wake),360,115+62*extra,p);
        p.setTypeface(Design.font(getContext(),400));p.setTextSize(12*scale);p.setTextAlign(Paint.Align.LEFT);c.drawText(nap==null?"Bedtime":"Nap starts",0,134+80*extra,p);p.setTextAlign(Paint.Align.RIGHT);c.drawText(nap==null?"Wake-up":"Nap ends",360,134+80*extra,p);c.restore();
    }
}
