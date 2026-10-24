package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

final class RecordUi {
    static String clock(Context c,long time,ZoneId zone){return Instant.ofEpochMilli(time).atZone(zone).format(DateTimeFormatter.ofPattern(android.text.format.DateFormat.is24HourFormat(c)?"HH:mm":"h:mm a",Locale.getDefault()));}
    static android.graphics.drawable.Drawable glyph(Context c,String icon,int color,int size){int px=Design.dp(c,size);android.graphics.Bitmap bitmap=android.graphics.Bitmap.createBitmap(px,px,android.graphics.Bitmap.Config.ARGB_8888);Design.Icon view=new Design.Icon(c,icon,color);view.layout(0,0,px,px);view.draw(new android.graphics.Canvas(bitmap));android.graphics.drawable.BitmapDrawable drawable=new android.graphics.drawable.BitmapDrawable(c.getResources(),bitmap);drawable.setBounds(0,0,px,px);return drawable;}
    static CharSequence withIcon(Context c,String value,String icon,int color){android.text.SpannableString text=new android.text.SpannableString("  "+value);text.setSpan(new android.text.style.ImageSpan(glyph(c,icon,color,18),android.text.style.ImageSpan.ALIGN_BASELINE),0,1,0);return text;}
    static String preciseClock(Context c,long time,ZoneId zone){return Instant.ofEpochMilli(time).atZone(zone).format(DateTimeFormatter.ofPattern(android.text.format.DateFormat.is24HourFormat(c)?"HH:mm:ss":"h:mm:ss a",Locale.getDefault()));}
    static void lightBars(Activity a){Design.focusedSystemBars(a,false);}
    static String date(long time,ZoneId zone){return Instant.ofEpochMilli(time).atZone(zone).format(DateTimeFormatter.ofPattern("EEE d MMM uuuu",Locale.getDefault()));}
    static String duration(long ms){long minutes=Math.max(0,ms/60000);if(ms<60000)return Math.max(0,ms/1000)+" sec";return minutes>=60?minutes/60+" h "+minutes%60+" min":minutes+" min";}
    static CharSequence units(String value){android.text.SpannableString s=new android.text.SpannableString(value);java.util.regex.Matcher m=java.util.regex.Pattern.compile("h|min|sec").matcher(value);while(m.find())s.setSpan(new android.text.style.RelativeSizeSpan(.55f),m.start(),m.end(),0);return s;}
    static void enabled(View view,boolean value){view.setEnabled(value);if(view instanceof android.view.ViewGroup){android.view.ViewGroup group=(android.view.ViewGroup)view;for(int n=0;n<group.getChildCount();n++)enabled(group.getChildAt(n),value);}}
    static Intent intent(Activity a,Class<?> destination){return new Intent(a,destination).putExtra("visualReview",a.getIntent().getBooleanExtra("visualReview",false)).putExtra("verificationLabel",a.getIntent().getStringExtra("verificationLabel"));}
    static void review(Activity a){if(a.getIntent().getBooleanExtra("visualReview",false))a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);}
    static void label(Activity a,LinearLayout body){String label=a.getIntent().getStringExtra("verificationLabel");if(label!=null&&!label.isEmpty()){Design.text(body,label,11,600,Design.BROWN);Design.space(body,8);}}
    static LinearLayout header(LinearLayout body,String title,Runnable back){Context c=body.getContext();LinearLayout row=Design.row(c);if(back!=null)row.addView(Design.iconButton(c,"back","Back",back),new LinearLayout.LayoutParams(Design.dp(c,48),Design.dp(c,48)));row.addView(Design.text(c,title,back==null?28:25,700,Design.INK),new LinearLayout.LayoutParams(0,-2,1));body.addView(row,new LinearLayout.LayoutParams(-1,Design.dp(c,52)));return row;}
    static void headerAction(LinearLayout row,String icon,String description,Runnable action){Context c=row.getContext();row.addView(Design.iconButton(c,icon,description,action),new LinearLayout.LayoutParams(Design.dp(c,48),Design.dp(c,48)));}
    static void heading(LinearLayout body,String text){Design.space(body,20);Design.text(body,text,16,600,Design.INK);Design.space(body,8);}
    static View chip(Context c,String name,boolean selected,Runnable action){FrameLayout hit=new FrameLayout(c);hit.setMinimumHeight(Design.dp(c,48));hit.setMinimumWidth(Design.dp(c,48));TextView text=Design.text(c,name,12,selected?600:400,selected?Design.CREAM:Design.INK);text.setGravity(Gravity.CENTER);text.setPadding(Design.dp(c,8),0,Design.dp(c,8),0);text.setBackground(Design.touch(c,selected?Design.BROWN:Design.SOFT,24));FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-2,Design.dp(c,32),Gravity.CENTER);hit.addView(text,lp);hit.setSelected(selected);hit.setContentDescription(name);hit.setOnClickListener(v->action.run());hit.setFocusable(true);return hit;}
    static LinearLayout segments(LinearLayout body,String[] names,int selected,java.util.function.IntConsumer action){Context c=body.getContext();LinearLayout row=Design.row(c);row.setBackground(Design.shape(c,Design.SOFT,14));for(int n=0;n<names.length;n++){int index=n;FrameLayout hit=new FrameLayout(c);TextView label=Design.text(c,names[n],13,selected==n?600:400,selected==n?Design.INK:Design.MUTED);label.setGravity(Gravity.CENTER);if(selected==n)label.setBackground(Design.shape(c,Design.CREAM,12));FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,Design.dp(c,34),Gravity.CENTER);lp.setMargins(Design.dp(c,3),0,Design.dp(c,3),0);hit.addView(label,lp);hit.setContentDescription(names[n]);hit.setSelected(selected==n);hit.setFocusable(true);hit.setOnClickListener(v->action.accept(index));row.addView(hit,new LinearLayout.LayoutParams(0,Design.dp(c,48),1));}body.addView(row);return row;}
    static LinearLayout footer(LinearLayout body){LinearLayout root=(LinearLayout)body.getParent().getParent(),footer=Design.col(body.getContext());footer.setPadding(Design.dp(body.getContext(),24),0,Design.dp(body.getContext(),24),Design.dp(body.getContext(),10));root.addView(footer,new LinearLayout.LayoutParams(-1,-2));return footer;}
    static Button quietButton(LinearLayout body,String title,Runnable action){Button b=Design.button(body,title,false,action);b.setMinHeight(Design.dp(body.getContext(),48));b.setMinimumHeight(Design.dp(body.getContext(),48));b.setTextSize(14*Appearance.scale(body.getContext()));return b;}
    static void main(Activity a,int destination){a.startActivity(new Intent(a,MainActivity.class).putExtra("destination",destination).putExtra("visualReview",a.getIntent().getBooleanExtra("visualReview",false)).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));a.finish();}
}
