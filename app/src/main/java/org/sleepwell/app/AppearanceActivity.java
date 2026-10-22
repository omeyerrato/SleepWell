package org.sleepwell.app;

import android.app.*;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.*;

/** Board 12: a draft with an explicit Save action; Back cancels every unsaved change. */
public final class AppearanceActivity extends Activity {
    private boolean automatic,motion,contrast;private int theme,size;
    private LinearLayout[] choices;private RadioButton[] radios;private TextView themeHelp,sizeValue;
    @Override public void onCreate(Bundle saved){super.onCreate(saved);
        if(getIntent().getBooleanExtra("visualReview",false))getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        automatic=saved==null?Appearance.automatic(this):saved.getBoolean("auto");
        theme=saved==null?Prefs.number(this,Appearance.THEME,0):saved.getInt("theme");theme=Math.max(0,Math.min(2,theme));
        size=saved==null?Prefs.number(this,Appearance.SIZE,0):saved.getInt("size");size=Math.max(0,Math.min(2,size));
        motion=saved==null?Appearance.reduced(this):saved.getBoolean("motion");
        contrast=saved==null?Appearance.contrast(this):saved.getBoolean("contrast");render();
    }
    @Override public void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b);b.putBoolean("auto",automatic);b.putInt("theme",theme);b.putInt("size",size);b.putBoolean("motion",motion);b.putBoolean("contrast",contrast);}
    private void render(){
        LinearLayout body=Design.page(this,false,null,n->{});Design.pad(body,24,8);
        body.addView(Design.iconButton(this,"back","Back",this::finish),new LinearLayout.LayoutParams(Design.dp(this,48),Design.dp(this,48)));
        Design.space(body,6);Design.text(body,"Appearance",30,700,Design.INK);Design.space(body,5);
        Design.text(body,"Customize how SleepWell looks and feels.",14,400,Design.MUTED);Design.space(body,10);
        preference(body,"sun","Time of day","Colors follow your local clock.",automatic,on->{automatic=on;updateSelection();});Design.line(body);
        LinearLayout intro=Design.item(body,"palette","App theme",null,null);intro.setMinimumHeight(Design.dp(this,48));intro.addView(Design.iconButton(this,"info","Automatic color schedule",()->new AlertDialog.Builder(this).setTitle("Colors through the day").setMessage("Colors follow your local clock: Midnight from 6 PM to 4 AM, Pre-dawn from 4 to 6 AM, and Dawn from 6 AM to 6 PM. These are clock-based colors, not calculated sunrise times. Choosing a tile turns Time of day off and keeps that color theme.").setPositiveButton("Got it",null).show()),new LinearLayout.LayoutParams(Design.dp(this,48),Design.dp(this,48)));
        themeHelp=Design.text(body,"",12,400,Design.MUTED);Design.space(body,12);
        choices=new LinearLayout[3];radios=new RadioButton[3];LinearLayout tiles=Design.row(this);tiles.setGravity(Gravity.TOP);body.addView(tiles);
        for(int n=0;n<3;n++){final int index=n;LinearLayout tile=Design.col(this);tile.setGravity(Gravity.CENTER);choices[n]=tile;
            ThemeScene scene=new ThemeScene(this,n);scene.setBackground(Design.shape(this,Design.SOFT,12));scene.setClipToOutline(true);tile.addView(scene,new LinearLayout.LayoutParams(-1,Design.dp(this,92)));Design.space(tile,7);
            TextView label=Design.text(this,AppearancePolicy.THEMES[n],12,500,Design.INK);label.setGravity(Gravity.CENTER);tile.addView(label);
            RadioButton radio=new RadioButton(this);radio.setButtonTintList(ColorStateList.valueOf(Appearance.contrast(this)?Design.BROWN:Design.VIOLET));radio.setGravity(Gravity.CENTER);radio.setMinHeight(Design.dp(this,36));radio.setClickable(false);radio.setFocusable(false);radio.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);radios[n]=radio;tile.addView(radio,new LinearLayout.LayoutParams(Design.dp(this,32),Design.dp(this,36)));
            tile.setContentDescription(AppearancePolicy.THEMES[n]+" theme");tile.setFocusable(true);tile.setClickable(true);tile.setAccessibilityDelegate(new View.AccessibilityDelegate(){@Override public void onInitializeAccessibilityNodeInfo(View v,AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(v,info);info.setClassName(RadioButton.class.getName());info.setCheckable(true);info.setChecked(selectedTheme()==index);}});
            tile.setOnClickListener(v->{theme=index;automatic=false;autoSwitch.setChecked(false);updateSelection();});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);if(n>0)lp.leftMargin=Design.dp(this,10);tiles.addView(tile,lp);
        }
        updateSelection();Design.space(body,10);Design.line(body);
        LinearLayout textSize=Design.item(body,null,"Text size",null,()->new AlertDialog.Builder(this).setTitle("Text size").setSingleChoiceItems(AppearancePolicy.SIZES,size,(dialog,which)->{size=which;sizeValue.setText(AppearancePolicy.SIZES[size]);dialog.dismiss();}).setNegativeButton("Cancel",null).show());
        TextView aa=Design.text(this,"Aa",18,500,Design.INK);aa.setSingleLine(true);aa.setTextScaleX(.85f);LinearLayout.LayoutParams aaLp=new LinearLayout.LayoutParams(Design.dp(this,32),-2);aaLp.rightMargin=Design.dp(this,12);textSize.addView(aa,0,aaLp);
        sizeValue=Design.text(this,AppearancePolicy.SIZES[size],13,400,Design.MUTED);textSize.addView(sizeValue,textSize.getChildCount()-1);Design.line(body);
        preference(body,"motion","Reduce motion","Remove animated press effects.",motion,on->motion=on);Design.line(body);
        preference(body,"contrast","High-contrast mode","Increase text and control contrast.",contrast,on->contrast=on);Design.line(body);
        Design.space(body,18);LinearLayout light=Design.row(this);Design.Icon bulb=new Design.Icon(this,"bulb",Design.INK);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(Design.dp(this,26),Design.dp(this,26));bp.rightMargin=Design.dp(this,18);light.addView(bulb,bp);
        LinearLayout lightText=Design.col(this);Design.text(lightText,"Smart lights",16,500,Design.INK);Design.space(lightText,5);Design.text(lightText,"No light connection available yet.",12,400,Design.MUTED);light.addView(lightText,new LinearLayout.LayoutParams(0,-2,1));body.addView(light);
        Design.space(body,12);Design.fixedAction(body,"Save appearance  →",()->{Appearance.save(this,automatic,theme,size,motion,contrast);finish();if(motion)overridePendingTransition(0,0);});
    }
    private Switch autoSwitch;
    private void preference(LinearLayout body,String icon,String title,String description,boolean value,java.util.function.Consumer<Boolean> action){
        LinearLayout row=Design.row(this);row.setMinimumHeight(Design.dp(this,64));Design.Icon glyph=new Design.Icon(this,icon,Design.INK);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(Design.dp(this,26),Design.dp(this,26));ip.rightMargin=Design.dp(this,18);row.addView(glyph,ip);
        LinearLayout text=Design.col(this);Design.text(text,title,15,500,Design.INK);Design.space(text,5);Design.text(text,description,11,400,Design.MUTED);row.addView(text,new LinearLayout.LayoutParams(0,-2,1));
        Switch toggle=new Switch(this);toggle.setContentDescription(title);Design.styleSwitch(this,toggle,Design.VIOLET,0xffc8c2be);toggle.setChecked(value);toggle.setOnCheckedChangeListener((v,on)->{action.accept(on);if(motion)toggle.jumpDrawablesToCurrentState();});row.addView(toggle);body.addView(row);if(title.equals("Time of day"))autoSwitch=toggle;
    }
    private int selectedTheme(){java.time.LocalTime now=java.time.LocalTime.now();return AppearancePolicy.phase(automatic,theme,now.getHour()*60+now.getMinute());}
    private void updateSelection(){if(choices==null)return;for(int n=0;n<3;n++){boolean selected=selectedTheme()==n;choices[n].setSelected(selected);radios[n].setChecked(selected);}
        themeHelp.setText(automatic?"Choose a tile to use a fixed theme.":"Use one color theme for Tonight.");
    }
}
