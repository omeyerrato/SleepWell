package org.sleepwell.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Per-alarm draft controls. Reading this page never starts tracking or writes preferences. */
final class AlarmSmartWakeUi {
    private static final int[] WINDOWS={0,5,10,15,20,30,45,60};
    private static final int[] STOPS={5,10,15,20,30,45,60};

    static void render(Activity activity,LinearLayout body,Alarm alarm,Runnable redraw,
                       Runnable back,Consumer<Supplier<Dialog>> open){
        Runnable refresh=()->{
            int scroll=body.getParent() instanceof ScrollView parent?parent.getScrollY():0;
            redraw.run();
            ViewGroup content=activity.findViewById(android.R.id.content);
            if(content.getChildCount()>0&&content.getChildAt(0) instanceof LinearLayout root
                    &&root.getChildCount()>0&&root.getChildAt(0) instanceof ScrollView replacement)
                replacement.post(()->replacement.scrollTo(0,scroll));
        };
        LinearLayout header=RecordUi.header(body,"Smart wake-up",back);
        RecordUi.headerAction(header,"more","Smart wake-up options",()->open.accept(()->options(activity,alarm,refresh,open)));
        Design.space(body,10);
        Design.toggle(body,"Smart wake-up","Use movement within your wake window",alarm.smartWindow>0,
                on->{alarm.smartWindow=on?30:0;refresh.run();});
        Design.space(body,14);
        Design.text(body,"Wake window",16,600,Design.INK);
        Design.space(body,6);
        long deadline=deadline(activity,alarm);
        TextView range=Design.text(body,"",27,700,Design.INK);
        Design.space(body,6);
        TextView caption=Design.text(body,"",13,400,Design.MUTED);
        WindowTimeline timeline=new WindowTimeline(activity,alarm,deadline);
        body.addView(timeline,new LinearLayout.LayoutParams(-1,Design.dp(activity,92*Math.max(1,Design.fontScale(activity)))));
        Design.space(body,8);

        LinearLayout length=Design.row(activity);
        length.setMinimumHeight(Design.dp(activity,48));
        length.addView(Design.text(activity,"Window length",16,600,Design.INK),new LinearLayout.LayoutParams(0,-2,1));
        TextView value=Design.text(activity,"",15,600,Design.BROWN);
        length.addView(value);
        length.addView(new Design.Icon(activity,"down",Design.BROWN),new LinearLayout.LayoutParams(Design.dp(activity,22),Design.dp(activity,22)));
        length.setContentDescription("Window length");
        length.setFocusable(true);
        length.setBackground(Design.touch(activity,android.graphics.Color.TRANSPARENT,10));
        length.setOnClickListener(v->{InteractionFeedback.action(v);open.accept(()->{
            int selected=-1;for(int n=0;n<WINDOWS.length;n++)if(WINDOWS[n]==alarm.smartWindow)selected=n;
            return SleepPicker.choices(activity,"Window length",Arrays.stream(WINDOWS).mapToObj(AlarmSmartWakeUi::windowLabel).toArray(String[]::new),selected,
                    n->{alarm.smartWindow=WINDOWS[n];refresh.run();});
        });});
        body.addView(length);
        SeekBar slider=new SeekBar(activity);
        slider.setMax(STOPS.length-1);
        int selected=-1;for(int n=0;n<STOPS.length;n++)if(STOPS[n]==alarm.smartWindow)selected=n;
        // An unrecognised saved length remains visible in the menu, never a fabricated slider selection.
        slider.setProgress(Math.max(0,selected));
        slider.setEnabled(selected>=0);
        if(selected<0&&alarm.smartWindow>0)slider.setVisibility(View.GONE);
        AlarmSliders.style(slider);
        slider.setContentDescription("Smart wake-up window length");
        body.addView(slider,new LinearLayout.LayoutParams(-1,Design.dp(activity,48)));
        LinearLayout ticks=Design.row(activity);
        String[] marks={"5 min","15 min","30 min","60 min"};
        for(int n=0;n<marks.length;n++){
            TextView tick=Design.text(activity,marks[n],11,400,Design.MUTED);
            tick.setGravity(n==0?Gravity.START:n==marks.length-1?Gravity.END:Gravity.CENTER);
            ticks.addView(tick,new LinearLayout.LayoutParams(0,-2,1));
        }
        body.addView(ticks);
        Runnable updateWindow=()->{
            value.setText(windowLabel(alarm.smartWindow));
            length.setStateDescription(windowLabel(alarm.smartWindow));
            range.setText(alarm.smartWindow<=0?"Off":deadline<=0?"Wake time unavailable":
                    clock(activity,deadline-alarm.smartWindow*60000L)+" – "+clock(activity,deadline));
            caption.setText(alarm.smartWindow<=0?"Your fixed alarm time stays scheduled.":deadline<=0?
                    "Review this alarm’s schedule to show its next window.":
                    alarm.smartWindow+" min before your alarm at "+clock(activity,deadline));
            slider.setStateDescription(windowLabel(alarm.smartWindow));
            timeline.changed();
        };
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override public void onProgressChanged(SeekBar bar,int progress,boolean fromUser){
                if(fromUser){alarm.smartWindow=STOPS[progress];updateWindow.run();}
            }
            @Override public void onStartTrackingTouch(SeekBar bar){}
            @Override public void onStopTrackingTouch(SeekBar bar){InteractionFeedback.selection(bar);}
        });
        updateWindow.run();
        Design.space(body,12);Design.line(body);Design.space(body,12);

        if(alarm.smartWakeInvalid){
            Design.text(body,"Smart wake-up settings need review",16,600,Design.INK);
            Design.space(body,8);
            Design.text(body,"Saved sensitivity and goal settings could not be read. Reset them before saving this alarm.",13,400,Design.MUTED);
            action(body,"Reset to following settings",()->{alarm.setSmartWake(AlarmSmartWake.DEFAULT);refresh.run();});
        }else{
            sensitivity(activity,body,alarm,refresh);
            Design.space(body,12);Design.line(body);Design.space(body,12);
            goal(activity,body,alarm,refresh);
        }
        Design.space(body,12);
        Design.fixedAction(body,"Save settings",back);
    }

    private static void sensitivity(Activity activity,LinearLayout body,Alarm alarm,Runnable redraw){
        Design.text(body,"Movement sensitivity",16,600,Design.INK);
        if(alarm.smartWake.sensitivity<0){
            String label;
            try{label="Following tracking · "+MotionSettings.load(activity).label();}
            catch(RuntimeException unreadable){label="Following tracking · profile unavailable. Review motion sensor settings or choose a level.";}
            Design.space(body,6);Design.text(body,label,12,400,Design.MUTED);
        }
        Design.space(body,10);
        LinearLayout options=Design.row(activity);
        options.setBackground(Design.shape(activity,Design.SOFT,12));
        String[] names={"Low","Medium","High"};
        for(int n=0;n<names.length;n++){
            int level=n;boolean chosen=alarm.smartWake.sensitivity==n;
            TextView button=Design.text(activity,names[n],13,chosen?600:400,chosen?Design.CREAM:Design.INK);
            button.setGravity(Gravity.CENTER);button.setMinimumHeight(Design.dp(activity,48));
            button.setBackground(Design.touch(activity,chosen?Design.BROWN:android.graphics.Color.TRANSPARENT,12));
            button.setContentDescription("Movement sensitivity "+names[n]);button.setFocusable(true);SleepPicker.radio(button,chosen);
            button.setOnClickListener(v->{if(!chosen)InteractionFeedback.selection(v);alarm.setSmartWake(new AlarmSmartWake(level,alarm.smartWake.goal));redraw.run();});
            options.addView(button,new LinearLayout.LayoutParams(0,-2,1));
        }
        body.addView(options);
    }

    private static void goal(Activity activity,LinearLayout body,Alarm alarm,Runnable redraw){
        Boolean global=null;String target;
        try{global=Prefs.flag(activity,"smartNeedsGoal");}catch(RuntimeException unreadable){/* Show unknown instead of inventing inherited Off. */}
        try{int minutes=SleepPlans.load(activity).goal;if(minutes<20||minutes>810)throw new IllegalArgumentException();target=RecordUi.duration(minutes*60000L)+" recorded before early waking";}
        catch(RuntimeException unreadable){target="Duration goal unavailable · review Sleep goals";}
        if(alarm.smartWake.goal<0&&global==null){
            Design.text(body,"Respect sleep goal",16,600,Design.INK);Design.space(body,6);
            Design.text(body,"Following global goal · unavailable. Choose a value for this alarm or review Sleep goals.",12,400,Design.MUTED);
            Design.space(body,8);
            RecordUi.segments(body,new String[]{"Off","On"},-1,n->{alarm.setSmartWake(new AlarmSmartWake(alarm.smartWake.sensitivity,n));redraw.run();});
        }else{
            boolean effective=alarm.smartWake.requiresGoal(Boolean.TRUE.equals(global));
            String following=alarm.smartWake.goal<0?"Following global goal · "+(effective?"On":"Off")+"\n":"";
            Design.toggle(body,"Respect sleep goal",following+target,effective,
                    on->{alarm.setSmartWake(new AlarmSmartWake(alarm.smartWake.sensitivity,on?1:0));redraw.run();});
        }
    }

    private static Dialog options(Activity activity,Alarm alarm,Runnable refresh,Consumer<Supplier<Dialog>> open){
        SleepPicker.StateDialog dialog=new SleepPicker.StateDialog(activity,"actions","Smart wake-up options");
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout panel=Design.col(activity);Design.pad(panel,20,20);panel.setBackground(Design.shape(activity,Design.CREAM,22));
        Design.text(panel,"Smart wake-up options",22,600,Design.INK);Design.space(panel,12);
        if(alarm.smartWakeInvalid){
            Design.button(panel,"Reset to following settings",false,()->{dialog.dismiss();alarm.setSmartWake(AlarmSmartWake.DEFAULT);refresh.run();});
        }else{
            Design.button(panel,"Follow tracking sensitivity",false,()->{dialog.dismiss();alarm.setSmartWake(new AlarmSmartWake(-1,alarm.smartWake.goal));refresh.run();});
            Design.button(panel,"Follow global duration goal",false,()->{dialog.dismiss();alarm.setSmartWake(new AlarmSmartWake(alarm.smartWake.sensitivity,-1));refresh.run();});
        }
        Design.button(panel,"About smart wake-up",false,()->{dialog.dismiss();activity.getWindow().getDecorView().post(()->{if(activity.isFinishing()||activity.isDestroyed())return;open.accept(()->about(activity));});});
        Design.button(panel,"Cancel",false,dialog::dismiss);
        ScrollView scroll=new ScrollView(activity);dialog.scroll=scroll;scroll.addView(panel);dialog.setContentView(scroll);SleepPicker.show(activity,dialog,560);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(Math.min(activity.getResources().getDisplayMetrics().widthPixels-Design.dp(activity,32),Design.dp(activity,400)),-2);
        return dialog;
    }

    static Dialog about(Activity activity){
        SleepPicker.StateDialog dialog=new SleepPicker.StateDialog(activity,"message","About smart wake-up");dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout panel=Design.col(activity);Design.pad(panel,22,22);panel.setBackground(Design.shape(activity,Design.CREAM,24));
        Design.text(panel,"About smart wake-up",22,600,Design.INK);Design.space(panel,16);
        Design.text(panel,"Requires active phone-movement tracking. Sustained movement can trigger this alarm within its configured window; its fixed deadline stays scheduled. The landscape illustrates your configured schedule, not measured sleep stages.\n\nSensitivity can follow your tracking profile, including surface calibration, or use this alarm’s Low, Medium or High override. Follow tracking sensitivity and Follow global duration goal restore inheritance for this draft.\n\nThe goal counts active recorded duration, excluding pauses, not confirmed sleep. Missing or stale movement cannot trigger early waking. Changes stay in the draft until Save alarm.",15,400,Design.MUTED);
        Design.space(panel,16);Design.button(panel,"Got it",true,dialog::dismiss);
        ScrollView scroll=new ScrollView(activity);dialog.scroll=scroll;scroll.addView(panel);dialog.setContentView(scroll);SleepPicker.show(activity,dialog,560);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(Math.min(activity.getResources().getDisplayMetrics().widthPixels-Design.dp(activity,32),Design.dp(activity,400)),-2);
        return dialog;
    }

    private static void action(LinearLayout body,String title,Runnable action){
        Context context=body.getContext();TextView button=Design.text(context,title,13,600,Design.BROWN);
        button.setGravity(Gravity.CENTER);button.setMinimumHeight(Design.dp(context,48));
        button.setBackground(Design.touch(context,android.graphics.Color.TRANSPARENT,10));button.setFocusable(true);
        button.setAccessibilityDelegate(new View.AccessibilityDelegate(){@Override public void onInitializeAccessibilityNodeInfo(View host,android.view.accessibility.AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(host,info);info.setClassName(android.widget.Button.class.getName());}});
        button.setOnClickListener(v->{InteractionFeedback.action(v);action.run();});body.addView(button,new LinearLayout.LayoutParams(-1,-2));
    }

    private static String windowLabel(int minutes){return minutes==0?"Off":minutes+" min";}
    private static String clock(Context context,long time){return RecordUi.clock(context,time,ZoneId.systemDefault());}
    private static long deadline(Context context,Alarm alarm){
        if(!AlarmSkips.usable(alarm))return 0;
        try{return AlarmSkips.next(context,alarm);}catch(RuntimeException unavailable){return 0;}
    }

    /** A configured time interval only: never a synthetic movement/stage trace. */
    private static final class WindowTimeline extends View {
        private final Alarm alarm;private final long deadline;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Drawable sun;private final Bitmap landscape;private final float density,scale;
        WindowTimeline(Context context,Alarm alarm,long deadline){super(context);this.alarm=alarm;this.deadline=deadline;density=getResources().getDisplayMetrics().density;scale=Design.fontScale(context);sun=context.getDrawable(Icons.resource("sun")).mutate();sun.setTint(Appearance.color(context,0xffd87d13));landscape=((BitmapDrawable)context.getDrawable(R.drawable.wake_dawn_landscape)).getBitmap();paint.setTypeface(Design.font(context,400));setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);}
        void changed(){setContentDescription(alarm.smartWindow<=0?"Smart wake-up is off":deadline<=0?"Scheduled wake window unavailable":
                "Scheduled wake window "+clock(getContext(),deadline-alarm.smartWindow*60000L)+" to "+clock(getContext(),deadline)+". No sleep-stage measurements shown.");invalidate();}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);float left=12*density,right=getWidth()-12*density,top=28*density,bottom=top+30*density;
            paint.setColor(Appearance.color(getContext(),Design.SOFT));canvas.drawRoundRect(new RectF(left,top,right,bottom),4*density,4*density,paint);
            if(deadline>0&&alarm.smartWindow>0){
                float start=right-(right-left)*alarm.smartWindow/Math.max(60f,alarm.smartWindow);
                // A decorative crop only; the window boundary is computed from real scheduled times.
                RectF strip=new RectF(left,top,right,bottom);Path clip=new Path();clip.addRoundRect(strip,4*density,4*density,Path.Direction.CW);canvas.save();canvas.clipPath(clip);
                int cropHeight=Math.max(1,Math.min(landscape.getHeight(),Math.round(landscape.getWidth()*(bottom-top)/Math.max(1,right-left))));
                int cropTop=Math.max(0,Math.min(landscape.getHeight()-cropHeight,Math.round(landscape.getHeight()*.72f)-cropHeight/2));
                paint.setFilterBitmap(true);canvas.drawBitmap(landscape,new Rect(0,cropTop,landscape.getWidth(),cropTop+cropHeight),strip,paint);
                paint.setColor(Appearance.color(getContext(),Design.LILAC));paint.setAlpha(205);canvas.drawRect(left,top,start,bottom,paint);paint.setAlpha(255);canvas.restore();
                paint.setColor(Appearance.color(getContext(),Design.BROWN));paint.setStrokeWidth(density);
                canvas.drawLine(start,top-3*density,start,bottom+4*density,paint);canvas.drawLine(right,top-8*density,right,bottom+4*density,paint);
                int size=(int)(30*density);sun.setBounds((int)right-size,(int)top-size-3*(int)density,(int)right,(int)top-3*(int)density);sun.draw(canvas);
            }
            paint.setColor(Appearance.color(getContext(),Design.MUTED));paint.setTextSize(11*density*scale);
            if(deadline<=0){paint.setTextAlign(Paint.Align.CENTER);canvas.drawText("Scheduled window unavailable",getWidth()/2f,bottom+22*density*scale,paint);return;}
            float y=bottom+22*density*scale;
            long horizon=Math.max(60,alarm.smartWindow)*60000L;
            String first=clock(getContext(),deadline-horizon),middle=clock(getContext(),deadline-horizon/2),last=clock(getContext(),deadline);
            paint.setTextAlign(Paint.Align.LEFT);canvas.drawText(first,left,y,paint);
            float half=(right-left)/2;
            if(paint.measureText(first)+paint.measureText(middle)/2+8*density<half&&paint.measureText(last)+paint.measureText(middle)/2+8*density<half){paint.setTextAlign(Paint.Align.CENTER);canvas.drawText(middle,(left+right)/2,y,paint);}
            paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText(last,right,y,paint);
        }
    }
}
