package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.Build;
import android.view.*;
import android.widget.*;
import java.util.function.*;

final class Ui {
    static final int BG=Design.CREAM,CARD=Design.SOFT,TEXT=Design.INK,MUTED=Design.MUTED,MINT=Design.BROWN,GOLD=NightDialTokens.ACCENT;
    static int dp(Context c,float n) { return (int)(n*c.getResources().getDisplayMetrics().density+.5f); }
    static GradientDrawable shape(int color,int radius) { GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(radius);return g; }
    static LinearLayout column(Context c) { LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.VERTICAL);return l; }
    static LinearLayout page(Activity a,String title,String subtitle) {
        NightDialMotion.configurePage(a);
        a.getWindow().setStatusBarColor(BG); a.getWindow().setNavigationBarColor(BG);
        a.getWindow().setBackgroundDrawable(new ColorDrawable(BG));a.getWindow().setNavigationBarContrastEnforced(false);Design.systemBars(a,true);
        if(Build.VERSION.SDK_INT>=30) a.getWindow().setDecorFitsSystemWindows(false);
        LinearLayout root=column(a); root.setBackgroundColor(BG);
        root.setOnApplyWindowInsetsListener((v,insets)->{
            if(Build.VERSION.SDK_INT>=30) { android.graphics.Insets s=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime());v.setPadding(s.left,s.top,s.right,s.bottom); }
            else v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets;
        });
        ScrollView scroll=new ScrollView(a);scroll.setFillViewport(true);root.addView(scroll,new LinearLayout.LayoutParams(-1,-1));
        LinearLayout content=column(a); int screen=a.getResources().getDisplayMetrics().widthPixels;
        int hpad=Math.max(dp(a,20),(screen-dp(a,720))/2);content.setPadding(hpad,dp(a,20),hpad,dp(a,28));scroll.addView(content);
        a.setContentView(root);root.requestApplyInsets();
        if(title!=null) { heading(content,title,28); if(subtitle!=null) text(content,subtitle); }
        return content;
    }
    static TextView label(LinearLayout parent,String value,int size,int color) {
        TextView t=new TextView(parent.getContext());t.setText(value);t.setTypeface(Design.font(parent.getContext(),400));t.setTextSize(size*Appearance.scale(parent.getContext()));t.setTextColor(Appearance.color(parent.getContext(),color));t.setPadding(0,dp(t.getContext(),6),0,dp(t.getContext(),6));t.setLineSpacing(dp(t.getContext(),3),1);
        parent.addView(t,new LinearLayout.LayoutParams(-1,-2));return t;
    }
    static TextView heading(LinearLayout p,String v,int size) { TextView t=label(p,v,size,TEXT);t.setTypeface(Design.font(p.getContext(),600));return t; }
    static TextView text(LinearLayout p,String v) { return label(p,v,15,MUTED); }
    static LinearLayout card(LinearLayout p) {
        LinearLayout l=column(p.getContext());l.setBackground(shape(CARD,dp(p.getContext(),22)));l.setPadding(dp(p.getContext(),20),dp(p.getContext(),14),dp(p.getContext(),20),dp(p.getContext(),16));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(p.getContext(),16);p.addView(l,lp);return l;
    }
    static Button button(LinearLayout p,String title,Runnable click) { return button(p,title,click,false); }
    static Button button(LinearLayout p,String title,Runnable click,boolean primary) {
        return Design.button(p,title,primary,click);
    }
    static void toggle(LinearLayout p,String title,String help,boolean value,Consumer<Boolean> change) {
        Design.toggle(p,title,help,value,change);
    }
    static EditText input(LinearLayout p,String title,String value,int inputType) {
        label(p,title,14,MUTED);EditText e=new EditText(p.getContext());e.setText(value);e.setTextColor(TEXT);e.setHintTextColor(MUTED);e.setTypeface(Design.font(p.getContext(),400));e.setInputType(inputType);e.setTextSize(16*Appearance.scale(p.getContext()));e.setMinHeight(dp(p.getContext(),52));e.setBackground(Design.outline(p.getContext(),CARD,NightDialTokens.OUTLINE,12));e.setPadding(dp(p.getContext(),16),dp(p.getContext(),12),dp(p.getContext(),16),dp(p.getContext(),12));p.addView(e,new LinearLayout.LayoutParams(-1,-2));return e;
    }
    static void range(LinearLayout p,String title,int value,int min,int max,IntConsumer onChange) {
        TextView t=label(p,title+" · "+value,14,TEXT);SeekBar s=new SeekBar(p.getContext());s.setMax(max-min);s.setProgress(value-min);s.setMinHeight(dp(p.getContext(),48));p.addView(s,new LinearLayout.LayoutParams(-1,-2));
        s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { public void onProgressChanged(SeekBar b,int n,boolean user) { t.setText(title+" · "+(n+min));if(user)onChange.accept(n+min); }public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){} });
    }
    static void choice(LinearLayout p,String title,String current,String[] options,Consumer<String> onChange) {
        Button button=button(p,title+" · "+current,()->{});
        button.setOnClickListener(v->new AlertDialog.Builder(p.getContext()).setTitle(title).setItems(options,(d,w)->{onChange.accept(options[w]);button.setText(title+" · "+options[w]);}).show());
    }
    static void toast(Context c,String s) { Toast.makeText(c,s,Toast.LENGTH_LONG).show(); }
}
