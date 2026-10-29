package org.sleepwell.app;

import android.app.*;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.*;

/** Board 12: a draft with an explicit Save action; Back cancels every unsaved change. */
public final class AppearanceActivity extends Activity {
    private boolean automatic,motion,contrast;private int theme,size;
    private LinearLayout[] choices;private ThemeMark[] radios;private TextView themeHelp,sizeValue;private Dialog picker,options;private String optionsPage="menu";private Bundle restoredPicker;private AppearanceLightCard lightCard;
    @Override public void onCreate(Bundle saved){super.onCreate(saved);
        if(getIntent().getBooleanExtra("visualReview",false))getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        automatic=saved==null?Appearance.automatic(this):saved.getBoolean("auto");
        theme=saved==null?Prefs.number(this,Appearance.THEME,0):saved.getInt("theme");theme=Math.max(0,Math.min(2,theme));
        size=saved==null?Prefs.number(this,Appearance.SIZE,0):saved.getInt("size");size=Math.max(0,Math.min(2,size));
        motion=saved==null?Appearance.reduced(this):saved.getBoolean("motion");
        contrast=saved==null?Appearance.contrast(this):saved.getBoolean("contrast");restoredPicker=saved==null?null:saved.getBundle("appearancePicker");render();if(restoredPicker!=null){textSize();picker.onRestoreInstanceState(restoredPicker);restoredPicker=null;}else if(saved!=null&&saved.getBoolean("appearanceOptions")){if("schedule".equals(saved.getString("appearanceOptionsPage")))colorSchedule();else appearanceOptions();}
    }
    @Override public void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b);b.putBoolean("auto",automatic);b.putInt("theme",theme);b.putInt("size",size);b.putBoolean("motion",motion);b.putBoolean("contrast",contrast);if(picker instanceof SleepPicker.StateDialog&&picker.isShowing())b.putBundle("appearancePicker",picker.onSaveInstanceState());b.putBoolean("appearanceOptions",options!=null&&options.isShowing());b.putString("appearanceOptionsPage",optionsPage);}
    private void render(){
        LinearLayout body=Design.page(this,false,null,n->{});Design.pad(body,24,8);body.setLayoutParams(new ScrollView.LayoutParams(-1,-2));
        LinearLayout navigation=Design.row(this);navigation.addView(Design.iconButton(this,"back","Back",this::finish),new LinearLayout.LayoutParams(Design.dp(this,48),Design.dp(this,48)));navigation.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));navigation.addView(Design.iconButton(this,"more","More appearance options",this::appearanceOptions),new LinearLayout.LayoutParams(Design.dp(this,48),Design.dp(this,48)));body.addView(navigation);
        Design.text(body,"Appearance",30,700,Design.INK);Design.space(body,4);
        Design.text(body,"Customize how SleepWell looks and feels.",14,400,Design.MUTED);Design.space(body,6);
        preference(body,"sun","Time of day","Colors follow your local clock.",automatic,on->{automatic=on;updateSelection();});Design.line(body);
        LinearLayout intro=Design.row(this);intro.setMinimumHeight(Design.dp(this,40));LinearLayout.LayoutParams paletteSpace=new LinearLayout.LayoutParams(Design.dp(this,26),Design.dp(this,26));paletteSpace.rightMargin=Design.dp(this,18);intro.addView(new Design.Icon(this,"palette",Design.INK),paletteSpace);LinearLayout themeCopy=Design.col(this);Design.text(themeCopy,"App theme",15,500,Design.INK);Design.space(themeCopy,4);themeHelp=Design.text(themeCopy,"Preview the look for different times.",11,400,Design.MUTED);intro.addView(themeCopy,new LinearLayout.LayoutParams(0,-2,1));body.addView(intro);Design.space(body,6);
        choices=new LinearLayout[3];radios=new ThemeMark[3];LinearLayout tiles=Design.row(this);tiles.setGravity(Gravity.TOP);body.addView(tiles);
        for(int n=0;n<3;n++){final int index=n;LinearLayout tile=Design.col(this);tile.setGravity(Gravity.CENTER);choices[n]=tile;
            ThemeScene scene=new ThemeScene(this,n);scene.setBackground(Design.shape(this,Design.SOFT,12));scene.setClipToOutline(true);tile.addView(scene,new LinearLayout.LayoutParams(-1,Design.dp(this,92)));Design.space(tile,4);
            TextView label=Design.text(this,AppearancePolicy.THEMES[n],12,500,Design.INK);label.setGravity(Gravity.CENTER);tile.addView(label);
            ThemeMark radio=new ThemeMark(this);radios[n]=radio;tile.addView(radio,new LinearLayout.LayoutParams(Design.dp(this,32),Design.dp(this,28)));
            tile.setContentDescription(AppearancePolicy.THEMES[n]+" theme");tile.setFocusable(true);tile.setClickable(true);tile.setAccessibilityDelegate(new View.AccessibilityDelegate(){@Override public void onInitializeAccessibilityNodeInfo(View v,AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(v,info);info.setClassName(RadioButton.class.getName());info.setCheckable(true);info.setChecked(selectedTheme()==index);}});
            tile.setOnClickListener(v->{InteractionFeedback.selection(v);theme=index;automatic=false;autoSwitch.setChecked(false);updateSelection();});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);if(n>0)lp.leftMargin=Design.dp(this,10);tiles.addView(tile,lp);
        }
        updateSelection();Design.space(body,6);Design.line(body);
        LinearLayout textSize=Design.row(this);textSize.setMinimumHeight(Design.dp(this,48));textSize.setBackground(Design.touch(this,android.graphics.Color.TRANSPARENT,12));textSize.setContentDescription("Text size");textSize.setFocusable(true);textSize.setOnClickListener(v->{InteractionFeedback.action(v);textSize();});
        TextView aa=Design.text(this,"Aa",18,500,Design.INK);aa.setSingleLine(true);aa.setTextScaleX(.85f);LinearLayout.LayoutParams aaLp=new LinearLayout.LayoutParams(Design.dp(this,26),-2);aaLp.rightMargin=Design.dp(this,18);textSize.addView(aa,aaLp);
        LinearLayout sizeCopy=Design.col(this);Design.text(sizeCopy,"Text size",15,500,Design.INK);Design.space(sizeCopy,5);Design.text(sizeCopy,"Use a comfortable size.",11,400,Design.MUTED);textSize.addView(sizeCopy,new LinearLayout.LayoutParams(0,-2,1));sizeValue=Design.text(this,AppearancePolicy.SIZES[size],13,400,Design.MUTED);LinearLayout.LayoutParams valueSpace=new LinearLayout.LayoutParams(-2,-2);valueSpace.leftMargin=Design.dp(this,8);valueSpace.rightMargin=Design.dp(this,8);textSize.addView(sizeValue,valueSpace);textSize.addView(new Design.Icon(this,"chevron",Design.INK),new LinearLayout.LayoutParams(Design.dp(this,18),Design.dp(this,24)));body.addView(textSize);Design.line(body);
        preference(body,"motion","Reduce motion","Remove animated press effects.",motion,on->motion=on);Design.line(body);
        preference(body,"contrast","High-contrast mode","Increase text and control contrast.",contrast,on->contrast=on);Design.line(body);
        Design.space(body,4);lightCard=new AppearanceLightCard(this,body,()->startActivity(RecordUi.intent(this,SmartLightActivity.class)));
        Design.space(body,8);Design.fixedAction(body,"Save appearance  →",()->{Appearance.save(this,automatic,theme,size,motion,contrast);finish();if(motion)overridePendingTransition(0,0);});
        fitThemePreviews(body,tiles);
    }
    /** Give decorative previews the remaining viewport; larger text keeps its natural height and scrolls. */
    private void fitThemePreviews(LinearLayout body,LinearLayout tiles){
        ScrollView viewport=(ScrollView)body.getParent();
        body.getViewTreeObserver().addOnPreDrawListener(()->{
            if(viewport.getHeight()==0||tiles.getChildCount()==0)return true;
            LinearLayout first=(LinearLayout)tiles.getChildAt(0);View preview=first.getChildAt(0);
            int fixed=body.getPaddingTop()+body.getPaddingBottom()+tiles.getMeasuredHeight()-preview.getMeasuredHeight();
            for(int n=0;n<body.getChildCount();n++){View child=body.getChildAt(n);if(child==tiles)continue;LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)child.getLayoutParams();fixed+=child.getMeasuredHeight()+lp.topMargin+lp.bottomMargin;}
            int available=viewport.getHeight()-viewport.getPaddingTop()-viewport.getPaddingBottom()-fixed;
            int height=Math.max(Design.dp(this,64),Math.min(Design.dp(this,100),available));
            if(preview.getLayoutParams().height==height)return true;
            for(int n=0;n<tiles.getChildCount();n++){View scene=((LinearLayout)tiles.getChildAt(n)).getChildAt(0);android.view.ViewGroup.LayoutParams lp=scene.getLayoutParams();lp.height=height;scene.setLayoutParams(lp);}
            return false;
        });
    }
    private Switch autoSwitch;
    private void preference(LinearLayout body,String icon,String title,String description,boolean value,java.util.function.Consumer<Boolean> action){
        LinearLayout row=Design.row(this);row.setMinimumHeight(Design.dp(this,48));Design.Icon glyph=new Design.Icon(this,icon,Design.INK);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(Design.dp(this,26),Design.dp(this,26));ip.rightMargin=Design.dp(this,18);row.addView(glyph,ip);
        LinearLayout text=Design.col(this);Design.text(text,title,15,500,Design.INK);Design.space(text,5);Design.text(text,description,11,400,Design.MUTED);row.addView(text,new LinearLayout.LayoutParams(0,-2,1));
        Switch toggle=new InteractionFeedback.Toggle(this);toggle.setContentDescription(title);Design.styleSwitch(this,toggle,Design.VIOLET,0xffc8c2be);toggle.setChecked(value);toggle.setOnCheckedChangeListener((v,on)->{action.accept(on);if(motion)toggle.jumpDrawablesToCurrentState();});row.addView(toggle);body.addView(row);if(title.equals("Time of day"))autoSwitch=toggle;
    }
    private int selectedTheme(){java.time.LocalTime now=java.time.LocalTime.now();return AppearancePolicy.phase(automatic,theme,now.getHour()*60+now.getMinute());}
    private void updateSelection(){if(choices==null)return;for(int n=0;n<3;n++){boolean selected=selectedTheme()==n;choices[n].setSelected(selected);radios[n].setChecked(selected);}
        themeHelp.setText("Preview the look for different times.");
    }
    /** Decorative ring only; the whole theme tile retains its existing radio semantics and action. */
    private static final class ThemeMark extends View {
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private boolean checked;
        ThemeMark(android.content.Context context){super(context);setClickable(false);setFocusable(false);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        void setChecked(boolean value){if(checked!=value){checked=value;invalidate();}}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);float centerX=getWidth()/2f,centerY=getHeight()/2f,radius=Math.min(Design.dp(getContext(),9),Math.min(getWidth(),getHeight())*.45f);
            if(checked){paint.setStyle(Paint.Style.FILL);paint.setColor(Appearance.contrast(getContext())?Design.BROWN:Design.VIOLET);canvas.drawCircle(centerX,centerY,radius,paint);paint.setColor(Design.CREAM);canvas.drawCircle(centerX,centerY,radius*.43f,paint);}
            else{paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(getResources().getDisplayMetrics().density*1.5f);paint.setColor(Appearance.color(getContext(),Design.MUTED));canvas.drawCircle(centerX,centerY,radius-.75f*getResources().getDisplayMetrics().density,paint);}
        }
    }
    private void textSize(){if(picker!=null&&picker.isShowing())picker.dismiss();picker=SleepPicker.choices(this,"Text size",AppearancePolicy.SIZES,size,which->{size=which;sizeValue.setText(AppearancePolicy.SIZES[size]);});}
    private void appearanceOptions(){
        if(options!=null&&options.isShowing())options.dismiss();optionsPage="menu";Dialog dialog=new Dialog(this);options=dialog;dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);LinearLayout content=Design.col(this);Design.pad(content,22,20);content.setBackground(Design.shape(this,Design.CREAM,24));Design.text(content,"Appearance options",22,600,Design.INK);Design.space(content,12);
        PlanActivity.detail(content,"home","Tonight layout","Choose your home screen sections",null,()->{dialog.dismiss();startActivity(RecordUi.intent(this,TonightLayoutActivity.class));});Design.line(content);
        PlanActivity.detail(content,"clock","Color schedule","See when the theme changes",null,()->{dialog.dismiss();colorSchedule();});Design.button(content,"Done",false,dialog::dismiss);ScrollView scroll=new ScrollView(this);scroll.addView(content);dialog.setContentView(scroll);SleepPicker.show(this,dialog,420);
    }
    private void colorSchedule(){if(options!=null&&options.isShowing())options.dismiss();optionsPage="schedule";options=SleepPicker.message(this,"Colors through the day","Midnight: 6 PM–4 AM. Pre-dawn: 4–6 AM. Dawn: 6 AM–6 PM. These follow your local clock. Choosing a theme turns Time of day off.");}
    @Override protected void onResume(){super.onResume();if(lightCard!=null)lightCard.refresh();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
    @Override protected void onDestroy(){if(picker!=null)picker.dismiss();if(options!=null)options.dismiss();if(lightCard!=null)lightCard.close();super.onDestroy();}
}
