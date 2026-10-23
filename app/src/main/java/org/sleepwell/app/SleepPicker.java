package org.sleepwell.app;

import android.app.*;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.util.Locale;
import java.util.function.IntConsumer;

/** SleepWell-owned choice/time controls. Opening and cancelling never apply a value. */
final class SleepPicker {
    private SleepPicker(){}
    static Dialog choices(Activity a,String title,String[] labels,int selected,IntConsumer choose){
        Dialog dialog=new Dialog(a);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout root=Design.col(a);Design.pad(root,20,20);root.setBackground(Design.shape(a,Design.CREAM,24));
        Design.text(root,title,22,600,Design.INK);Design.space(root,16);
        ScrollView scroll=new ScrollView(a);scroll.setVerticalScrollBarEnabled(false);LinearLayout options=Design.col(a);scroll.addView(options);
        for(int index=0;index<labels.length;index++){
            int n=index;LinearLayout row=Design.row(a);Design.pad(row,12,10);row.setMinimumHeight(Design.dp(a,52));
            row.setBackground(Design.touch(a,index==selected?Design.SOFT:Color.TRANSPARENT,12));
            row.addView(new Mark(a,index==selected),new LinearLayout.LayoutParams(Design.dp(a,24),Design.dp(a,24)));
            TextView label=Design.text(a,labels[index],16,index==selected?600:400,Design.INK);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.leftMargin=Design.dp(a,14);row.addView(label,lp);
            row.setSelected(index==selected);row.setFocusable(true);row.setContentDescription(labels[index]);
            row.setOnClickListener(v->{dialog.dismiss();choose.accept(n);});options.addView(row,new LinearLayout.LayoutParams(-1,-2));
        }
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));Design.button(root,"Cancel",false,dialog::dismiss);
        dialog.setContentView(root);show(a,dialog,Math.min(620,150+labels.length*58));return dialog;
    }
    static Dialog time(Activity a,String title,int minuteOfDay,IntConsumer choose){
        Dialog dialog=new Dialog(a);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout root=Design.col(a);Design.pad(root,22,22);root.setBackground(Design.shape(a,Design.CREAM,24));Design.text(root,title,24,600,Design.INK);Design.space(root,20);
        boolean fullDay=android.text.format.DateFormat.is24HourFormat(a);int[] period={minuteOfDay>=720?1:0};
        LinearLayout time=Design.row(a);time.setGravity(Gravity.CENTER);EditText hour=number(a,"Hour",fullDay?minuteOfDay/60:(minuteOfDay/60+11)%12+1),minute=number(a,"Minute",minuteOfDay%60);
        column(time,hour,"hour",fullDay?0:1,fullDay?23:12);TextView colon=Design.text(a,":",44,600,Design.INK);colon.setGravity(Gravity.CENTER);time.addView(colon,new LinearLayout.LayoutParams(Design.dp(a,26),-2));column(time,minute,"minute",0,59);root.addView(time);
        LinearLayout periods=Design.col(a);root.addView(periods);Runnable[] refresh={null};refresh[0]=()->{periods.removeAllViews();if(!fullDay){Design.space(periods,12);RecordUi.segments(periods,new String[]{"AM","PM"},period[0],n->{period[0]=n;refresh[0].run();});}};refresh[0].run();
        Design.space(root,12);TextView error=Design.text(root,"",13,400,Design.MUTED);error.setVisibility(View.GONE);
        Design.button(root,"Use time",true,()->{Integer h=read(hour),m=read(minute);if(h==null||m==null||h<(fullDay?0:1)||h>(fullDay?23:12)||m<0||m>59){error.setText(fullDay?"Choose hour 0–23 and minute 0–59.":"Choose hour 1–12 and minute 0–59.");error.setVisibility(View.VISIBLE);return;}dialog.dismiss();choose.accept((fullDay?h:h%12+period[0]*12)*60+m);});
        Design.button(root,"Cancel",false,dialog::dismiss);ScrollView scroll=new ScrollView(a);scroll.setVerticalScrollBarEnabled(false);scroll.addView(root);dialog.setContentView(scroll);show(a,dialog,fullDay?470:530);dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);return dialog;
    }
    private static EditText number(Context c,String label,int value){EditText edit=new EditText(c);edit.setText(String.format(Locale.ROOT,"%02d",value));edit.setContentDescription(label);edit.setGravity(Gravity.CENTER);edit.setTextSize(42*Appearance.scale(c));edit.setTypeface(Design.font(c,600));edit.setTextColor(Design.INK);edit.setSelectAllOnFocus(true);edit.setSingleLine(true);edit.setInputType(InputType.TYPE_CLASS_NUMBER);edit.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(2)});edit.setBackground(Design.outline(c,Design.SOFT,Design.LINE,16));edit.setPadding(0,Design.dp(c,10),0,Design.dp(c,10));return edit;}
    private static void column(LinearLayout row,EditText edit,String name,int min,int max){Context c=row.getContext();LinearLayout col=Design.col(c);col.setGravity(Gravity.CENTER);for(boolean up:new boolean[]{true,false}){View arrow=Design.iconButton(c,up?"up":"down",(up?"Increase ":"Decrease ")+name,()->{Integer value=read(edit);int next=value==null?min:Math.floorMod(value-min+(up?1:-1),max-min+1)+min;edit.setText(String.format(Locale.ROOT,"%02d",next));});if(up){col.addView(arrow,new LinearLayout.LayoutParams(Design.dp(c,64),Design.dp(c,48)));col.addView(edit,new LinearLayout.LayoutParams(-1,Design.dp(c,84)));}else col.addView(arrow,new LinearLayout.LayoutParams(Design.dp(c,64),Design.dp(c,48)));}LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);row.addView(col,lp);}
    private static Integer read(EditText value){try{return Integer.valueOf(value.getText().toString());}catch(NumberFormatException invalid){return null;}}
    private static void show(Activity a,Dialog dialog,int heightDp){dialog.show();Window window=dialog.getWindow();window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));int width=Math.min(a.getResources().getDisplayMetrics().widthPixels-Design.dp(a,32),Design.dp(a,440));int available=a.getWindow().getDecorView().getHeight();if(available<=0)available=a.getResources().getDisplayMetrics().heightPixels;window.setLayout(width,Math.min(Design.dp(a,heightDp*Math.max(1,Design.fontScale(a))),available-Design.dp(a,64)));}
    /** Radio semantics without inheriting a platform theme's color/geometry. */
    static final class Mark extends View {
        private final Paint paint=new Paint(3);private final boolean selected;
        Mark(Context c,boolean selected){super(c);this.selected=selected;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas canvas){float radius=Math.min(getWidth(),getHeight())*.40f,x=getWidth()/2f,y=getHeight()/2f;paint.setColor(Design.BROWN);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Design.dp(getContext(),2));canvas.drawCircle(x,y,radius,paint);if(selected){paint.setStyle(Paint.Style.FILL);canvas.drawCircle(x,y,radius*.57f,paint);}}
    }
}
