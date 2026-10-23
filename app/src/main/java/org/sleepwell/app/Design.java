package org.sleepwell.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import java.util.function.Consumer;

/** Native view tokens taken from the supplied Changing light boards. */
final class Design {
    static final int CREAM=0xfffcf7ef, INK=0xff381707, BROWN=0xff572a15,
        SOFT=0xfff2e7dc, MUTED=0xff666568, LINE=0xffe6d9ce,
        PLUM=0xff241e30, LILAC=0xffd9c4fc, VIOLET=0xffac83ed;
    private static final java.util.Map<Integer,Typeface> FONTS=new java.util.HashMap<>();
    static Typeface font(Context c,int weight) {
        return FONTS.computeIfAbsent(weight,w->new Typeface.Builder(c.getAssets(),"fonts/Inter.ttf")
            .setFontVariationSettings("'wght' "+w).build());
    }
    static int dp(Context c,float value){return Ui.dp(c,value);}
    static float sp(Context c,float value){return android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_SP,value,c.getResources().getDisplayMetrics());}
    static float fontScale(Context c){return c.getResources().getConfiguration().fontScale*Appearance.scale(c);}
    static void styleSwitch(Context c,Switch toggle,int active,int inactive){
        if(Appearance.contrast(c)){active=BROWN;inactive=0xff77706b;}
        toggle.setTrackTintList(null);toggle.setThumbTintList(null);StateListDrawable track=new StateListDrawable();GradientDrawable on=shape(c,active,30),off=shape(c,inactive,30);
        on.setSize(dp(c,52),dp(c,28));off.setSize(dp(c,52),dp(c,28));track.addState(new int[]{android.R.attr.state_checked},on);track.addState(new int[]{},off);toggle.setTrackDrawable(track);
        GradientDrawable thumb=shape(c,Color.WHITE,30);thumb.setSize(dp(c,24),dp(c,24));thumb.setStroke(dp(c,1),0x11808080);toggle.setThumbDrawable(thumb);toggle.setSplitTrack(false);toggle.setThumbTextPadding(dp(c,2));toggle.setSwitchMinWidth(dp(c,52));toggle.setMinHeight(dp(c,48));toggle.setShowText(false);
    }
    static LinearLayout col(Context c){return Ui.column(c);}
    static LinearLayout row(Context c){LinearLayout r=new LinearLayout(c);r.setGravity(Gravity.CENTER_VERTICAL);return r;}
    static void space(LinearLayout p,int height){p.addView(new View(p.getContext()),new LinearLayout.LayoutParams(1,dp(p.getContext(),height)));}
    static void line(LinearLayout p){View v=new View(p.getContext());v.setBackgroundColor(LINE);p.addView(v,new LinearLayout.LayoutParams(-1,dp(p.getContext(),1)));}
    static GradientDrawable shape(Context c,int color,int radius){GradientDrawable shape=Ui.shape(color,dp(c,radius));if(Appearance.contrast(c)&&color==SOFT)shape.setStroke(dp(c,1),BROWN);return shape;}
    static android.graphics.drawable.Drawable touch(Context c,int color,int radius){
        if(!Appearance.reduced(c))return new RippleDrawable(ColorStateList.valueOf(0x224a2615),shape(c,color,radius),null);
        StateListDrawable states=new StateListDrawable();states.addState(new int[]{android.R.attr.state_pressed},shape(c,ColorUtilsOverlay(color),radius));states.addState(new int[]{},shape(c,color,radius));return states;
    }
    private static int ColorUtilsOverlay(int color){return androidx.core.graphics.ColorUtils.compositeColors(0x334a2615,color);}
    static TextView text(Context c,String value,int sp,int weight,int color){TextView t=new TextView(c);t.setText(value);t.setTextSize(sp*Appearance.scale(c));t.setTypeface(font(c,weight));t.setTextColor(Appearance.color(c,color));t.setIncludeFontPadding(false);t.setFontFeatureSettings("tnum");return t;}
    static TextView text(LinearLayout p,String value,int sp,int weight,int color){TextView t=text(p.getContext(),value,sp,weight,color);p.addView(t,new LinearLayout.LayoutParams(-1,-2));return t;}
    static String clock(Context c,int minute){return java.time.LocalTime.of(Math.floorMod(minute,1440)/60,Math.floorMod(minute,1440)%60).format(java.time.format.DateTimeFormatter.ofPattern(android.text.format.DateFormat.is24HourFormat(c)?"HH:mm":"h:mm a",java.util.Locale.getDefault())).toUpperCase(java.util.Locale.getDefault());}
    static CharSequence timeText(String value){android.text.SpannableString span=new android.text.SpannableString(value);int suffix=value.lastIndexOf(' ');if(suffix>=0)span.setSpan(new android.text.style.RelativeSizeSpan(.55f),suffix,value.length(),0);return span;}
    static void pad(View v,int h,int vertical){v.setPadding(dp(v.getContext(),h),dp(v.getContext(),vertical),dp(v.getContext(),h),dp(v.getContext(),vertical));}
    static LinearLayout page(Activity a,boolean night,Integer destination,java.util.function.IntConsumer navigate){
        return page(a,night,destination,navigate,Appearance.palette(a));
    }
    static LinearLayout darkPage(Activity a){return page(a,true,null,n->{},new Appearance.Palette(0,Appearance.contrast(a)));}
    private static LinearLayout page(Activity a,boolean night,Integer destination,java.util.function.IntConsumer navigate,Appearance.Palette palette){
        boolean dark=night&&palette.dark;
        a.getWindow().setDecorFitsSystemWindows(false);
        a.getWindow().getDecorView(); // Create decor before querying the controller during onCreate.
        a.getWindow().setNavigationBarContrastEnforced(false);
        a.getWindow().setStatusBarContrastEnforced(false);
        a.getWindow().getInsetsController().setSystemBarsAppearance(dark?0:WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
        LinearLayout root=col(a);root.setBackgroundColor(night?palette.bg:CREAM);root.setContentDescription(night&&destination!=null?"Tonight · "+AppearancePolicy.THEMES[palette.phase]:null);
        root.setOnApplyWindowInsetsListener((v,i)->{Insets bars=i.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return i;});
        ScrollView scroll=new ScrollView(a);scroll.setFillViewport(true);scroll.setClipToPadding(false);scroll.setVerticalScrollBarEnabled(false);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout body=col(a);pad(body,24,16);scroll.addView(body,new ScrollView.LayoutParams(-1,-1));
        if(destination!=null){LinearLayout dock=row(a);dock.setPadding(dp(a,8),dp(a,6),dp(a,8),dp(a,6));dock.setBackground(shape(a,night?palette.dock:SOFT,40));
            String[] names={"Tonight","Alarms","Report","Settings"},icons={"moon","alarm","report","settings"};
            for(int n=0;n<names.length;n++){final int index=n;LinearLayout item=col(a);item.setGravity(Gravity.CENTER);item.setBackground(touch(a,destination==n?(night?palette.selected:CREAM):Color.TRANSPARENT,32));
                int color=Appearance.color(a,night?palette.softInk:(destination==n?INK:MUTED));Icon icon=new Icon(a,icons[n],color);item.addView(icon,new LinearLayout.LayoutParams(dp(a,26),dp(a,26)));space(item,4);TextView label=text(a,names[n],13,destination==n?600:400,color);label.setGravity(Gravity.CENTER);item.addView(label);
                if(Appearance.contrast(a)&&destination==n){GradientDrawable selection=shape(a,night?palette.selected:CREAM,32);selection.setStroke(dp(a,2),night?palette.softInk:BROWN);item.setBackground(selection);}
                item.setContentDescription(names[n]);item.setSelected(destination==n);item.setOnClickListener(v->navigate.accept(index));dock.addView(item,new LinearLayout.LayoutParams(0,dp(a,62*Math.max(1,fontScale(a))),1));}
            GradientDrawable dockSurface=shape(a,night?palette.dock:SOFT,40);if(night)dockSurface.setStroke(dp(a,1),palette.line);dock.setBackground(dockSurface);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(a,12+62*Math.max(1,fontScale(a))));lp.setMargins(dp(a,12),dp(a,8),dp(a,12),dp(a,8));root.addView(dock,lp);
        }
        a.setContentView(root);root.requestApplyInsets();root.post(()->a.getWindow().getInsetsController().setSystemBarsAppearance(dark?0:WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS));return body;
    }
    static void header(LinearLayout p,String title,Runnable back){LinearLayout r=row(p.getContext());if(back!=null)r.addView(iconButton(p.getContext(),"back","Back",back),new LinearLayout.LayoutParams(dp(p.getContext(),48),dp(p.getContext(),48)));
        TextView t=text(p.getContext(),title,back==null?30:24,700,INK);r.addView(t,new LinearLayout.LayoutParams(0,-2,1));p.addView(r,new LinearLayout.LayoutParams(-1,dp(p.getContext(),56)));space(p,12);}
    static View iconButton(Context c,String icon,String label,Runnable click){Icon v=new Icon(c,icon,INK);v.setPadding(dp(c,11),dp(c,11),dp(c,11),dp(c,11));v.setContentDescription(label);v.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);v.setBackground(touch(c,Color.TRANSPARENT,24));v.setOnClickListener(w->click.run());return v;}
    static Button button(LinearLayout p,String title,boolean primary,Runnable click){Context c=p.getContext();Button b=new Button(c);b.setStateListAnimator(null);b.setElevation(0);b.setText(title);b.setAllCaps(false);b.setTextSize(16*Appearance.scale(c));b.setTypeface(font(c,600));b.setTextColor(primary?CREAM:INK);b.setMinHeight(dp(c,56));b.setMinimumHeight(dp(c,56));b.setBackground(touch(c,primary?BROWN:SOFT,16));b.setOnClickListener(v->click.run());
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(c,10);p.addView(b,lp);return b;}
    static void fixedAction(LinearLayout body,String title,Runnable click){LinearLayout root=(LinearLayout)body.getParent().getParent();LinearLayout footer=col(body.getContext());footer.setPadding(dp(body.getContext(),24),0,dp(body.getContext(),24),dp(body.getContext(),12));button(footer,title,true,click);root.addView(footer,new LinearLayout.LayoutParams(-1,-2));}
    static LinearLayout item(LinearLayout p,String icon,String title,String value,Runnable click){Context c=p.getContext();LinearLayout r=row(c);r.setMinimumHeight(dp(c,66));
        if(icon!=null){Icon i=new Icon(c,icon,INK);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(c,26),dp(c,26));ip.rightMargin=dp(c,18);r.addView(i,ip);}
        TextView name=text(c,title,16,500,INK);r.addView(name,new LinearLayout.LayoutParams(0,-2,1));
        if(value!=null){TextView val=text(c,value,14,400,MUTED);val.setGravity(Gravity.END);r.addView(val);}
        if(click!=null){Icon chevron=new Icon(c,"chevron",INK);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(c,22),dp(c,22));cp.leftMargin=dp(c,8);r.addView(chevron,cp);r.setBackground(touch(c,Color.TRANSPARENT,8));r.setOnClickListener(v->click.run());}
        p.addView(r,new LinearLayout.LayoutParams(-1,-2));return r;
    }
    static Switch toggle(LinearLayout p,String title,String subtitle,boolean checked,Consumer<Boolean> change){Context c=p.getContext();LinearLayout r=row(c);pad(r,14,12);r.setBackground(shape(c,SOFT,16));LinearLayout copy=col(c);text(copy,title,16,600,INK);if(subtitle!=null){space(copy,5);text(copy,subtitle,12,400,MUTED);}r.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        Switch s=new Switch(c);s.setContentDescription(title);styleSwitch(c,s,BROWN,0xffbcb5b0);s.setChecked(checked);s.setOnCheckedChangeListener((b,on)->{change.accept(on);if(Appearance.reduced(c))s.jumpDrawablesToCurrentState();});r.addView(s);p.addView(r,new LinearLayout.LayoutParams(-1,-2));return s;
    }
    /** Original, consistent 24-unit stroked vector icons. No emoji glyph dependency. */
    static class Icon extends View {
        private final String name;private final Paint paint=new Paint(3);
        Icon(Context c,String name,int color){super(c);this.name=name;paint.setColor(color);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.8f);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        void tint(int color){paint.setColor(color);invalidate();}
        @Override protected void onDraw(Canvas original){super.onDraw(original);Canvas c=original;c.save();c.translate(getPaddingLeft(),getPaddingTop());c.scale((getWidth()-getPaddingLeft()-getPaddingRight())/24f,(getHeight()-getPaddingTop()-getPaddingBottom())/24f);Path p=new Path();
            switch(name){
                case "back":c.drawLine(21,12,3,12,paint);c.drawLine(3,12,10,5,paint);c.drawLine(3,12,10,19,paint);break;
                case "up":c.drawLine(5,15,12,8,paint);c.drawLine(12,8,19,15,paint);break;
                case "down":c.drawLine(5,9,12,16,paint);c.drawLine(12,16,19,9,paint);break;
                case "meal":c.drawLine(5,2,5,10,paint);c.drawLine(2,2,2,7,paint);c.drawLine(8,2,8,7,paint);c.drawLine(2,7,8,7,paint);c.drawLine(5,10,5,22,paint);c.drawLine(18,2,18,22,paint);p.moveTo(18,2);p.quadTo(11,8,18,12);c.drawPath(p,paint);break;
                case "exercise":c.drawCircle(16,3,2,paint);p.moveTo(4,13);p.lineTo(9,7);p.lineTo(14,10);p.lineTo(20,10);p.moveTo(13,9);p.lineTo(10,15);p.lineTo(15,21);p.moveTo(10,15);p.lineTo(5,22);c.drawPath(p,paint);break;
                case "tag":p.moveTo(3,3);p.lineTo(12,3);p.lineTo(22,13);p.lineTo(13,22);p.lineTo(3,12);p.close();c.drawPath(p,paint);c.drawCircle(8,8,1.5f,paint);break;
                case "chevron":c.drawLine(9,5,16,12,paint);c.drawLine(16,12,9,19,paint);break;
                case "minus":c.drawLine(4,12,20,12,paint);break;
                case "attention":c.drawLine(12,3,12,14,paint);c.drawCircle(12,20,.8f,paint);break;
                case "plus":c.drawLine(12,3,12,21,paint);c.drawLine(3,12,21,12,paint);break;
                case "moon":p.moveTo(15,2);p.cubicTo(-3,0,-1,23,15,22);p.cubicTo(20,21,22,17,22,14);p.cubicTo(10,19,6,7,15,2);paint.setStyle(Paint.Style.FILL);c.drawPath(p,paint);paint.setStyle(Paint.Style.STROKE);break;
                case "report":for(int i=0;i<3;i++){paint.setStrokeWidth(4);c.drawLine(5+i*7,20,5+i*7,10-(i%2)*7,paint);}break;
                case "settings":for(int i=0;i<32;i++){double angle=i*Math.PI/16;float radius=(i%4==1||i%4==2)?11:8;float x=12+(float)Math.cos(angle)*radius,y=12+(float)Math.sin(angle)*radius;if(i==0)p.moveTo(x,y);else p.lineTo(x,y);}p.close();Path hole=new Path();hole.addCircle(12,12,3.6f,Path.Direction.CCW);p.addPath(hole);p.setFillType(Path.FillType.EVEN_ODD);paint.setStyle(Paint.Style.FILL);c.drawPath(p,paint);paint.setStyle(Paint.Style.STROKE);break;
                case "sun":c.drawCircle(12,12,4,paint);for(int i=0;i<8;i++){c.save();c.rotate(i*45,12,12);c.drawLine(12,1,12,4,paint);c.restore();}break;
                case "sound":p.moveTo(3,10);p.lineTo(7,10);p.lineTo(12,5);p.lineTo(12,19);p.lineTo(7,14);p.lineTo(3,14);p.close();c.drawPath(p,paint);c.drawArc(11,4,22,20,-55,110,false,paint);break;
                case "tasks":for(int i=0;i<3;i++){c.drawCircle(3,5+i*7,1,paint);c.drawLine(8,5+i*7,21,5+i*7,paint);}break;
                case "bed":c.drawLine(2,7,2,21,paint);c.drawLine(22,11,22,21,paint);c.drawRect(2,11,22,17,paint);c.drawRect(5,7,11,11,paint);break;
                case "battery":c.drawRoundRect(6,4,18,23,2,2,paint);c.drawRect(10,1,14,4,paint);c.drawLine(9,8,15,8,paint);break;
                case "bell":p.moveTo(5,17);p.lineTo(7,14);p.lineTo(7,9);p.cubicTo(7,2,17,2,17,9);p.lineTo(17,14);p.lineTo(19,17);p.close();c.drawPath(p,paint);c.drawArc(9,17,15,22,0,180,false,paint);c.drawLine(12,2,12,3,paint);break;
                case "lock":c.drawRoundRect(5,10,19,22,2,2,paint);c.drawArc(8,1,16,15,180,180,false,paint);c.drawCircle(12,15,1,paint);c.drawLine(12,16,12,18,paint);break;
                case "shield":p.moveTo(12,2);p.lineTo(21,6);p.lineTo(20,14);p.quadTo(18,20,12,23);p.quadTo(6,20,4,14);p.lineTo(3,6);p.close();c.drawPath(p,paint);c.drawLine(8,12,11,15,paint);c.drawLine(11,15,17,9,paint);break;
                case "wave":for(int i=0;i<5;i++)c.drawLine(3+i*4,12-(i%2==0?4:8),3+i*4,12+(i%2==0?4:8),paint);break;
                case "plug":c.drawLine(8,1,8,7,paint);c.drawLine(16,1,16,7,paint);c.drawRoundRect(5,7,19,15,3,3,paint);c.drawLine(12,15,12,23,paint);break;
                case "calendar":c.drawRoundRect(3,5,21,22,2,2,paint);c.drawLine(3,10,21,10,paint);c.drawLine(8,2,8,7,paint);c.drawLine(16,2,16,7,paint);break;
                case "github":p.moveTo(5,9);p.lineTo(4,3);p.lineTo(9,5);p.quadTo(12,4,15,5);p.lineTo(20,3);p.lineTo(19,9);p.cubicTo(24,16,17,19,14,18);p.lineTo(14,23);p.moveTo(10,23);p.lineTo(10,18);p.cubicTo(4,19,0,15,5,9);c.drawPath(p,paint);p.reset();p.moveTo(10,21);p.cubicTo(4,23,5,18,2,18);c.drawPath(p,paint);break;
                case "cloud":p.moveTo(6,20);p.cubicTo(-2,20,0,10,6,10);p.cubicTo(5,0,21,0,20,11);p.cubicTo(27,14,23,20,20,20);p.close();c.drawPath(p,paint);break;
                case "rain":p.moveTo(6,14);p.cubicTo(-2,14,0,6,6,6);p.cubicTo(7,-1,19,0,19,7);p.cubicTo(25,7,25,14,19,14);c.drawPath(p,paint);for(int i=0;i<3;i++){c.drawLine(7+i*5,16,6+i*5,19,paint);c.drawLine(7+i*5,22,6+i*5,24,paint);}break;
                case "clock":c.drawCircle(12,12,10,paint);c.drawLine(12,5,12,12,paint);c.drawLine(12,12,17,14,paint);break;
                case "upload":c.drawLine(12,17,12,2,paint);c.drawLine(6,8,12,2,paint);c.drawLine(12,2,18,8,paint);p.moveTo(3,14);p.lineTo(3,22);p.lineTo(21,22);p.lineTo(21,14);c.drawPath(p,paint);break;
                case "file":p.moveTo(4,2);p.lineTo(14,2);p.lineTo(21,9);p.lineTo(21,22);p.lineTo(4,22);p.close();c.drawPath(p,paint);c.drawLine(14,2,14,9,paint);c.drawLine(14,9,21,9,paint);break;
                case "palette":p.moveTo(20,15);p.cubicTo(27,2,4,-4,2,10);p.cubicTo(0,20,12,26,14,21);p.cubicTo(17,17,14,15,20,15);p.close();c.drawPath(p,paint);for(int i=0;i<3;i++)c.drawCircle(7+i*5,7+(i==1?-2:1),1,paint);break;
                case "motion":p.moveTo(1,12);p.cubicTo(5,12,4,3,8,3);p.cubicTo(12,3,10,21,15,21);p.cubicTo(19,21,18,8,23,10);c.drawPath(p,paint);break;
                case "contrast":c.drawCircle(12,12,10,paint);paint.setStyle(Paint.Style.FILL);c.drawArc(2,2,22,22,90,180,false,paint);paint.setStyle(Paint.Style.STROKE);break;
                case "bulb":c.drawCircle(12,9,7,paint);c.drawLine(7,14,9,20,paint);c.drawLine(17,14,15,20,paint);c.drawLine(9,20,15,20,paint);c.drawLine(10,23,14,23,paint);break;
                case "info":c.drawCircle(12,12,10,paint);c.drawLine(12,10,12,18,paint);c.drawCircle(12,6,0.7f,paint);break;
                case "phone":c.drawRoundRect(7,2,17,22,2,2,paint);c.drawLine(11,19,13,19,paint);c.drawArc(1,5,8,19,120,120,false,paint);c.drawArc(16,5,23,19,-60,120,false,paint);break;
                case "mic":c.drawRoundRect(9,1,15,16,3,3,paint);c.drawArc(5,6,19,21,0,180,false,paint);c.drawLine(12,20,12,23,paint);break;
                case "pause":paint.setStrokeWidth(4);c.drawLine(7,4,7,20,paint);c.drawLine(17,4,17,20,paint);break;
                case "play":p.moveTo(6,3);p.lineTo(21,12);p.lineTo(6,21);p.close();paint.setStyle(Paint.Style.FILL);c.drawPath(p,paint);paint.setStyle(Paint.Style.STROKE);break;
                case "tracking":for(int i=0;i<10;i++){double angle=i*Math.PI/5;paint.setStyle(Paint.Style.FILL);c.drawCircle(12+(float)Math.cos(angle)*9,12+(float)Math.sin(angle)*9,i<3?1.4f:1,paint);}paint.setStyle(Paint.Style.STROKE);c.drawArc(3,3,21,21,100,48,false,paint);break;
                case "sonar":c.drawCircle(4,12,1,paint);c.drawArc(0,6,12,18,-65,130,false,paint);c.drawArc(-3,2,20,22,-65,130,false,paint);c.drawArc(-6,-2,27,26,-65,130,false,paint);break;
                case "stop":paint.setStyle(Paint.Style.FILL);c.drawRoundRect(4,4,20,20,2,2,paint);paint.setStyle(Paint.Style.STROKE);break;
                case "check":c.drawLine(4,12,10,18,paint);c.drawLine(10,18,21,5,paint);break;
                case "search":c.drawCircle(10,10,7,paint);c.drawLine(15,15,22,22,paint);break;
                case "close":c.drawLine(5,5,19,19,paint);c.drawLine(19,5,5,19,paint);break;
                case "more":paint.setStyle(Paint.Style.FILL);for(int i=0;i<3;i++)c.drawCircle(12,5+i*7,1.8f,paint);paint.setStyle(Paint.Style.STROKE);break;
                case "trash":c.drawLine(3,6,21,6,paint);c.drawRect(6,6,18,22,paint);c.drawRect(9,2,15,6,paint);c.drawLine(10,10,10,18,paint);c.drawLine(14,10,14,18,paint);break;
                case "external":p.moveTo(10,4);p.lineTo(3,4);p.lineTo(3,21);p.lineTo(20,21);p.lineTo(20,14);c.drawPath(p,paint);c.drawLine(11,13,22,2,paint);c.drawLine(15,2,22,2,paint);c.drawLine(22,2,22,9,paint);break;
                default:c.drawCircle(12,13,8,paint);c.drawLine(12,8,12,13,paint);c.drawLine(12,13,16,15,paint);c.drawLine(3,3,6,1,paint);c.drawLine(18,1,21,3,paint);
            }c.restore();
        }
    }
}
