package org.sleepwell.app;

import android.app.*;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.util.Locale;
import java.util.function.IntConsumer;

/** SleepWell-owned choice/time controls. Opening and cancelling never apply a value. */
final class SleepPicker {
    private SleepPicker(){}
    static Dialog message(Activity a,String title,String message){Dialog dialog=new Dialog(a);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);LinearLayout root=Design.col(a);Design.pad(root,22,22);root.setBackground(Design.shape(a,Design.CREAM,24));Design.text(root,title,22,600,Design.INK);Design.space(root,18);Design.text(root,message,15,400,Design.MUTED);Design.space(root,16);Design.button(root,"Got it",true,dialog::dismiss);ScrollView scroll=new ScrollView(a);scroll.addView(root);dialog.setContentView(scroll);show(a,dialog,380);return dialog;}
    static Dialog name(Activity a,String current,java.util.function.Consumer<String> choose){Dialog dialog=new Dialog(a);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);LinearLayout root=Design.col(a);Design.pad(root,22,22);root.setBackground(Design.shape(a,Design.CREAM,24));Design.text(root,"Alarm name",22,600,Design.INK);Design.space(root,18);EditText edit=new EditText(a);edit.setText(current);edit.setSingleLine(true);edit.setContentDescription("Alarm name");edit.setTextSize(18*Appearance.scale(a));edit.setTypeface(Design.font(a,400));edit.setTextColor(Design.INK);edit.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(80)});Design.pad(edit,14,14);edit.setBackground(Design.outline(a,Design.SOFT,Design.LINE,12));root.addView(edit,new LinearLayout.LayoutParams(-1,-2));Design.space(root,10);Design.button(root,"Use name",true,()->{String value=edit.getText().toString().trim();if(value.isEmpty()){edit.setError("Enter an alarm name");return;}dialog.dismiss();choose.accept(value);});Design.button(root,"Cancel",false,dialog::dismiss);ScrollView scroll=new ScrollView(a);scroll.addView(root);dialog.setContentView(scroll);show(a,dialog,330);return dialog;}
    static Dialog choices(Activity a,String title,String[] labels,int selected,IntConsumer choose){
        StateDialog dialog=new StateDialog(a,"choice",title);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout root=Design.col(a);Design.pad(root,20,20);root.setBackground(Design.shape(a,Design.CREAM,24));
        Design.text(root,title,22,600,Design.INK);Design.space(root,16);
        ScrollView scroll=new ScrollView(a);dialog.scroll=scroll;scroll.setContentDescription("Options for "+title);scroll.setVerticalScrollBarEnabled(true);scroll.setScrollbarFadingEnabled(false);scroll.setScrollBarSize(Design.dp(a,3));scroll.setVerticalScrollbarThumbDrawable(Design.shape(a,Design.LINE,2));LinearLayout options=Design.col(a);scroll.addView(options);
        View[] selectedRow={null};for(int index=0;index<labels.length;index++){
            int n=index;LinearLayout row=Design.row(a);Design.pad(row,12,10);row.setMinimumHeight(Design.dp(a,52));
            row.setBackground(Design.touch(a,index==selected?Design.SOFT:Color.TRANSPARENT,12));
            row.addView(new Mark(a,index==selected),new LinearLayout.LayoutParams(Design.dp(a,24),Design.dp(a,24)));
            TextView label=Design.text(a,labels[index],16,index==selected?600:400,Design.INK);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.leftMargin=Design.dp(a,14);row.addView(label,lp);
            radio(row,index==selected);if(index==selected)selectedRow[0]=row;row.setFocusable(true);row.setContentDescription(labels[index]);
            row.setOnClickListener(v->{if(n!=selected)InteractionFeedback.selection(v);dialog.dismiss();choose.accept(n);});options.addView(row,new LinearLayout.LayoutParams(-1,-2));
        }
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));Design.button(root,"Cancel",false,dialog::dismiss);
        scroll.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener(){public void onGlobalLayout(){scroll.getViewTreeObserver().removeOnGlobalLayoutListener(this);int centered=selectedRow[0]==null?0:selectedRow[0].getTop()-(scroll.getHeight()-selectedRow[0].getHeight())/2;scroll.scrollTo(0,dialog.restoredScroll!=null?dialog.restoredScroll:Math.max(0,centered));}});
        dialog.setContentView(root);show(a,dialog,Math.min(620,150+labels.length*58));return dialog;
    }
    static StateDialog actions(Activity a,String title,String[] labels,String[] icons,IntConsumer choose){
        StateDialog dialog=new StateDialog(a,"choice",title);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout root=Design.col(a);Design.pad(root,20,20);root.setBackground(Design.shape(a,Design.CREAM,24));Design.text(root,title,22,600,Design.INK);Design.space(root,12);
        ScrollView scroll=new ScrollView(a);dialog.scroll=scroll;scroll.setContentDescription("Actions for "+title);scroll.setVerticalScrollBarEnabled(false);LinearLayout options=Design.col(a);scroll.addView(options);
        for(int index=0;index<labels.length;index++){
            int selected=index;LinearLayout row=Design.row(a);Design.pad(row,12,12);row.setMinimumHeight(Design.dp(a,56));row.setBackground(Design.touch(a,Color.TRANSPARENT,12));
            if(icons!=null&&index<icons.length&&icons[index]!=null){LinearLayout.LayoutParams glyph=new LinearLayout.LayoutParams(Design.dp(a,23),Design.dp(a,23));glyph.rightMargin=Design.dp(a,14);row.addView(new Design.Icon(a,icons[index],Design.BROWN),glyph);}
            row.addView(Design.text(a,labels[index],16,500,Design.INK),new LinearLayout.LayoutParams(0,-2,1));row.setContentDescription(labels[index]);row.setFocusable(true);
            row.setAccessibilityDelegate(new View.AccessibilityDelegate(){@Override public void onInitializeAccessibilityNodeInfo(View host,android.view.accessibility.AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(host,info);info.setClassName(Button.class.getName());}});
            row.setOnClickListener(v->{InteractionFeedback.action(v);dialog.dismiss();choose.accept(selected);});options.addView(row,new LinearLayout.LayoutParams(-1,-2));if(index+1<labels.length)Design.line(options);
        }
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));Design.button(root,"Cancel",false,dialog::dismiss);dialog.setContentView(root);show(a,dialog,Math.min(620,150+labels.length*57));return dialog;
    }
    static Dialog time(Activity a,String title,int minuteOfDay,IntConsumer choose){return time(a,title,minuteOfDay,choose,null,null,"Use time");}
    static Dialog time(Activity a,String title,int minuteOfDay,IntConsumer choose,java.util.function.IntFunction<String> validate,String detail,String confirm){
        StateDialog dialog=new StateDialog(a,"time",title);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout root=Design.col(a);Design.pad(root,22,22);root.setBackground(Design.shape(a,Design.CREAM,24));Design.text(root,title,24,600,Design.INK);if(detail!=null){Design.space(root,8);Design.text(root,detail,13,400,Design.MUTED);}Design.space(root,20);
        boolean fullDay=android.text.format.DateFormat.is24HourFormat(a);int[] period={minuteOfDay>=720?1:0};
        LinearLayout time=Design.row(a);time.setGravity(Gravity.CENTER);EditText hour=number(a,"Hour",fullDay?minuteOfDay/60:(minuteOfDay/60+11)%12+1),minute=number(a,"Minute",minuteOfDay%60);
        column(time,hour,"hour",fullDay?0:1,fullDay?23:12);TextView colon=Design.text(a,":",44,600,Design.INK);colon.setGravity(Gravity.CENTER);time.addView(colon,new LinearLayout.LayoutParams(Design.dp(a,26),-2));column(time,minute,"minute",0,59);root.addView(time);
        LinearLayout periods=Design.col(a);root.addView(periods);Runnable[] refresh={null};refresh[0]=()->{periods.removeAllViews();if(!fullDay){Design.space(periods,12);RecordUi.segments(periods,new String[]{"AM","PM"},period[0],n->{period[0]=n;refresh[0].run();});}};refresh[0].run();dialog.hour=hour;dialog.minute=minute;dialog.period=period;dialog.refresh=refresh[0];
        Design.space(root,12);TextView error=Design.text(root,"",13,400,Design.MUTED);error.setVisibility(View.GONE);
        Design.button(root,confirm,true,()->{Integer h=read(hour),m=read(minute);if(h==null||m==null||h<(fullDay?0:1)||h>(fullDay?23:12)||m<0||m>59){error.setText(fullDay?"Choose hour 0–23 and minute 0–59.":"Choose hour 1–12 and minute 0–59.");error.setVisibility(View.VISIBLE);return;}int selected=(fullDay?h:h%12+period[0]*12)*60+m;String problem=validate==null?null:validate.apply(selected);if(problem!=null){error.setText(problem);error.setVisibility(View.VISIBLE);error.post(()->error.requestRectangleOnScreen(new android.graphics.Rect(0,0,error.getWidth(),error.getHeight()),true));return;}dialog.dismiss();choose.accept(selected);});
        Design.button(root,"Cancel",false,dialog::dismiss);root.setFocusableInTouchMode(true);root.requestFocus();ScrollView scroll=new ScrollView(a);scroll.setVerticalScrollBarEnabled(false);scroll.addView(root);dialog.setContentView(scroll);show(a,dialog,(fullDay?470:530)+(detail==null?0:50));dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);return dialog;
    }
    static Dialog duration(Activity a,String title,int value,int minimum,int maximum,IntConsumer choose){
        StateDialog dialog=new StateDialog(a,"duration",title);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout root=Design.col(a);Design.pad(root,22,22);root.setBackground(Design.shape(a,Design.CREAM,24));Design.text(root,title,24,600,Design.INK);Design.space(root,16);
        LinearLayout units=Design.row(a);for(String label:new String[]{"Hours","Minutes"}){TextView text=Design.text(a,label,14,500,Design.MUTED);text.setGravity(Gravity.CENTER);units.addView(text,new LinearLayout.LayoutParams(0,-2,1));}root.addView(units);
        LinearLayout values=Design.row(a);EditText hours=number(a,"Duration hours",value/60),minutes=number(a,"Duration minutes",value%60);column(values,hours,"duration hour",0,maximum/60);values.addView(new View(a),new LinearLayout.LayoutParams(Design.dp(a,12),1));column(values,minutes,"duration minute",0,59);root.addView(values);dialog.hour=hours;dialog.minute=minutes;
        TextView error=Design.text(root,"",13,400,Design.BROWN);error.setVisibility(View.GONE);Design.space(root,12);
        Design.button(root,"Use duration",true,()->{Integer h=read(hours),m=read(minutes);long total=h==null||m==null?-1:h*60L+m;if(h==null||m==null||h<0||m<0||m>59||total<minimum||total>maximum){error.setText("Choose "+length(minimum)+" to "+length(maximum)+".");error.setVisibility(View.VISIBLE);return;}dialog.dismiss();choose.accept((int)total);});Design.button(root,"Cancel",false,dialog::dismiss);
        root.setFocusableInTouchMode(true);root.requestFocus();ScrollView scroll=new ScrollView(a);scroll.addView(root);dialog.setContentView(scroll);show(a,dialog,470);dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);return dialog;
    }
    private static String length(int minutes){return minutes<60?minutes+" min":minutes/60+" h"+(minutes%60==0?"":" "+minutes%60+" min");}
    private static EditText number(Context c,String label,int value){EditText edit=new EditText(c);edit.setText(String.format(Locale.ROOT,"%02d",value));edit.setContentDescription(label);edit.setGravity(Gravity.CENTER);edit.setTextSize(42*Appearance.scale(c));edit.setTypeface(Design.font(c,600));edit.setTextColor(Design.INK);edit.setSelectAllOnFocus(true);edit.setSingleLine(true);edit.setInputType(InputType.TYPE_CLASS_NUMBER);edit.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(2)});edit.setBackground(Design.outline(c,Design.SOFT,Design.LINE,16));edit.setPadding(0,Design.dp(c,10),0,Design.dp(c,10));return edit;}
    private static void column(LinearLayout row,EditText edit,String name,int min,int max){Context c=row.getContext();LinearLayout col=Design.col(c);col.setGravity(Gravity.CENTER);for(boolean up:new boolean[]{true,false}){View arrow=Design.iconButton(c,up?"up":"down",(up?"Increase ":"Decrease ")+name,()->{Integer value=read(edit);int next=value==null?min:Math.floorMod(value-min+(up?1:-1),max-min+1)+min;edit.setText(String.format(Locale.ROOT,"%02d",next));});if(up){col.addView(arrow,new LinearLayout.LayoutParams(Design.dp(c,64),Design.dp(c,48)));col.addView(edit,new LinearLayout.LayoutParams(-1,Design.dp(c,84*Math.max(1,Design.fontScale(c)))));}else col.addView(arrow,new LinearLayout.LayoutParams(Design.dp(c,64),Design.dp(c,48)));}LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);row.addView(col,lp);}
    private static Integer read(EditText value){try{return Integer.valueOf(value.getText().toString());}catch(NumberFormatException invalid){return null;}}
    static void show(Activity a,Dialog dialog,int heightDp){
        boolean dark=Design.darkSystemBarSurface(a);
        // Floating dialogs do not inherit the Activity's system-background drawing flags.
        Window owned=dialog.getWindow();owned.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);owned.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS|WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION|WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        // Native floating-window dimming forces white system glyphs. A light, content-only
        // backdrop preserves the owner's cream/plum bars and never intercepts input.
        Backdrop backdrop=new Backdrop(a.getWindow().getDecorView(),owned.getDecorView());
        try{dialog.show();owned.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));int width=Math.min(a.getResources().getDisplayMetrics().widthPixels-Design.dp(a,32),Design.dp(a,440));int available=a.getWindow().getDecorView().getHeight();if(available<=0)available=a.getResources().getDisplayMetrics().heightPixels;owned.setLayout(width,Math.min(Design.dp(a,heightDp*Math.max(1,Design.fontScale(a))),available-Design.dp(a,64)));backdrop.attach();}catch(RuntimeException|Error failure){backdrop.close();if(dialog.isShowing())dialog.dismiss();throw failure;}
        Window window=owned;
        View decor=window.getDecorView();Runnable bars=()->{if(dialog.isShowing()&&!a.isDestroyed()&&decor.isAttachedToWindow()){androidx.core.view.WindowInsetsControllerCompat controller=androidx.core.view.WindowCompat.getInsetsController(window,decor);controller.setAppearanceLightStatusBars(!dark);controller.setAppearanceLightNavigationBars(!dark);}};
        bars.run();decor.getViewTreeObserver().addOnWindowFocusChangeListener(focused->{if(focused)decor.post(bars);});
    }
    private static final class Backdrop implements View.OnAttachStateChangeListener,View.OnLayoutChangeListener {
        final View owner,dialog;final ColorDrawable shade=new ColorDrawable(0x26000000);boolean closed,attached;
        Backdrop(View owner,View dialog){this.owner=owner;this.dialog=dialog;}
        void attach(){owner.addOnAttachStateChangeListener(this);dialog.addOnAttachStateChangeListener(this);owner.addOnLayoutChangeListener(this);if(!owner.isAttachedToWindow()){close();return;}if(dialog.isAttachedToWindow())show();}
        void show(){if(closed||attached)return;shade.setBounds(0,0,owner.getWidth(),owner.getHeight());owner.getOverlay().add(shade);attached=true;}
        void close(){if(closed)return;closed=true;owner.getOverlay().remove(shade);owner.removeOnLayoutChangeListener(this);owner.removeOnAttachStateChangeListener(this);dialog.removeOnAttachStateChangeListener(this);}
        public void onViewAttachedToWindow(View view){if(view==dialog&&owner.isAttachedToWindow())show();}
        public void onViewDetachedFromWindow(View view){close();}
        public void onLayoutChange(View view,int left,int top,int right,int bottom,int oldLeft,int oldTop,int oldRight,int oldBottom){if(!closed)shade.setBounds(0,0,owner.getWidth(),owner.getHeight());}
    }
    static void radio(View row,boolean checked){row.setSelected(checked);row.setAccessibilityDelegate(new View.AccessibilityDelegate(){@Override public void onInitializeAccessibilityNodeInfo(View host,android.view.accessibility.AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(host,info);info.setClassName(RadioButton.class.getName());info.setCheckable(true);info.setChecked(checked);}});}
    static final class StateDialog extends Dialog {
        final String kind,title;EditText hour,minute;int[] period;Runnable refresh;ScrollView scroll;Integer restoredScroll;
        StateDialog(Activity a,String kind,String title){super(a);this.kind=kind;this.title=title;}
        @Override public Bundle onSaveInstanceState(){Bundle out=super.onSaveInstanceState();out.putString("sleepPickerKind",kind);out.putString("sleepPickerTitle",title);if(hour!=null){out.putString("hour",hour.getText().toString());out.putString("minute",minute.getText().toString());if(period!=null)out.putInt("period",period[0]);}if(scroll!=null)out.putInt("scroll",scroll.getScrollY());return out;}
        @Override public void onRestoreInstanceState(Bundle state){super.onRestoreInstanceState(state);if(hour!=null){hour.setText(state.getString("hour",""));minute.setText(state.getString("minute",""));if(period!=null)period[0]=state.getInt("period");if(refresh!=null)refresh.run();}if(scroll!=null){restoredScroll=state.getInt("scroll");scroll.post(()->scroll.scrollTo(0,restoredScroll));}}
    }
    /** Radio semantics without inheriting a platform theme's color/geometry. */
    static final class Mark extends View {
        private final Paint paint=new Paint(3);private final boolean selected;private final int color;
        Mark(Context c,boolean selected){this(c,selected,Design.BROWN);}
        Mark(Context c,boolean selected,int color){super(c);this.selected=selected;this.color=color;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas canvas){float radius=Math.min(getWidth(),getHeight())*.40f,x=getWidth()/2f,y=getHeight()/2f;paint.setColor(color);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Design.dp(getContext(),2));canvas.drawCircle(x,y,radius,paint);if(selected){paint.setStyle(Paint.Style.FILL);canvas.drawCircle(x,y,radius*.57f,paint);}}
    }
}
