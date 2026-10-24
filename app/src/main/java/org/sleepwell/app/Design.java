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
        on.setSize(dp(c,52),dp(c,28));off.setSize(dp(c,52),dp(c,28));track.addState(new int[]{android.R.attr.state_checked},on);track.addState(new int[]{},off);toggle.setTrackDrawable(track);track.setState(toggle.getDrawableState());
        GradientDrawable thumb=shape(c,Color.WHITE,30);thumb.setSize(dp(c,24),dp(c,24));thumb.setStroke(dp(c,1),0x11808080);toggle.setThumbDrawable(thumb);toggle.setSplitTrack(false);toggle.setThumbTextPadding(dp(c,2));toggle.setSwitchMinWidth(dp(c,52));toggle.setMinHeight(dp(c,48));toggle.setShowText(false);
    }
    static LinearLayout col(Context c){return Ui.column(c);}
    private static void applySystemBars(Activity a,boolean dark){androidx.core.view.WindowInsetsControllerCompat controller=androidx.core.view.WindowCompat.getInsetsController(a.getWindow(),a.getWindow().getDecorView());controller.setAppearanceLightStatusBars(!dark);controller.setAppearanceLightNavigationBars(!dark);}
    static void systemBars(Activity a,boolean dark){a.getWindow().getDecorView().setTag(R.id.system_bar_dark_surface,dark);applySystemBars(a,dark);}
    static boolean darkSystemBarSurface(Activity a){View decor=a.getWindow().getDecorView();Object declared=decor.getTag(R.id.system_bar_dark_surface);if(declared instanceof Boolean)return(Boolean)declared;int appearance=a.getWindow().getInsetsController().getSystemBarsAppearance();return(appearance&WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)==0&&(decor.getSystemUiVisibility()&View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)==0;}
    static void focusedSystemBars(Activity a,boolean dark){View decor=a.getWindow().getDecorView();systemBars(a,dark);Runnable apply=()->{if(!a.isDestroyed()&&a.hasWindowFocus()&&Boolean.valueOf(dark).equals(decor.getTag(R.id.system_bar_dark_surface)))applySystemBars(a,dark);};decor.post(apply);decor.postDelayed(apply,200);}
    static LinearLayout row(Context c){LinearLayout r=new LinearLayout(c);r.setGravity(Gravity.CENTER_VERTICAL);return r;}
    static void space(LinearLayout p,int height){p.addView(new View(p.getContext()),new LinearLayout.LayoutParams(1,dp(p.getContext(),height)));}
    static void line(LinearLayout p){View v=new View(p.getContext());v.setBackgroundColor(LINE);p.addView(v,new LinearLayout.LayoutParams(-1,dp(p.getContext(),1)));}
    static GradientDrawable shape(Context c,int color,int radius){GradientDrawable shape=Ui.shape(color,dp(c,radius));if(Appearance.contrast(c)&&color==SOFT)shape.setStroke(dp(c,1),BROWN);return shape;}
    static android.graphics.drawable.Drawable touch(Context c,int color,int radius){
        if(!Appearance.reduced(c))return new RippleDrawable(ColorStateList.valueOf(0x224a2615),shape(c,color,radius),null);
        StateListDrawable states=new StateListDrawable();states.addState(new int[]{android.R.attr.state_pressed},shape(c,ColorUtilsOverlay(color),radius));states.addState(new int[]{},shape(c,color,radius));return states;
    }
    private static int ColorUtilsOverlay(int color){return androidx.core.graphics.ColorUtils.compositeColors(0x334a2615,color);}
    static android.graphics.drawable.Drawable outline(Context c,int color,int line,int radius){GradientDrawable background=shape(c,color,radius);background.setStroke(dp(c,1),line);return Appearance.reduced(c)?background:new RippleDrawable(ColorStateList.valueOf(0x22d9c4fc),background,null);}
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
        // API35+ also derives three-button icon contrast from the Window background,
        // independently of the content View and requested light/dark appearance.
        a.getWindow().setBackgroundDrawable(new ColorDrawable(night?palette.bg:CREAM));
        a.getWindow().getDecorView(); // Create decor before querying the controller during onCreate.
        a.getWindow().setNavigationBarContrastEnforced(false);
        a.getWindow().setStatusBarContrastEnforced(false);
        systemBars(a,dark);
        LinearLayout root=col(a);root.setBackgroundColor(night?palette.bg:CREAM);root.setContentDescription(night&&destination!=null?"Tonight · "+AppearancePolicy.THEMES[palette.phase]:null);
        root.setOnApplyWindowInsetsListener((v,i)->{Insets bars=i.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return i;});
        ScrollView scroll=new ScrollView(a);scroll.setDefaultFocusHighlightEnabled(false);scroll.setFillViewport(true);scroll.setClipToPadding(false);scroll.setVerticalScrollBarEnabled(false);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
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
        a.setContentView(root);root.requestApplyInsets();root.post(()->{ViewGroup content=a.findViewById(android.R.id.content);if(!a.isDestroyed()&&root.isAttachedToWindow()&&content.getChildCount()>0&&content.getChildAt(0)==root)systemBars(a,dark);});return body;
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
    /** Native vectors from the pinned Phosphor family; geometry and licenses are vendored. */
    static class Icon extends View {
        private final Drawable glyph;
        Icon(Context c,String name,int color){super(c);glyph=c.getDrawable(Icons.resource(name)).mutate();glyph.setTint(color);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        void tint(int color){glyph.setTint(color);invalidate();}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);int width=getWidth()-getPaddingLeft()-getPaddingRight(),height=getHeight()-getPaddingTop()-getPaddingBottom();int size=Math.min(width,height);if(size<=0)return;
            int left=getPaddingLeft()+(width-size)/2,top=getPaddingTop()+(height-size)/2;glyph.setBounds(left,top,left+size,top+size);glyph.draw(canvas);
        }
    }
}
