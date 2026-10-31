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
        LinearLayout body=Design.page(this,false,null,n->{});Design.pad(body,24,0);body.setLayoutParams(new ScrollView.LayoutParams(-1,-2));
        LinearLayout navigation=Design.row(this);navigation.addView(Design.iconButton(this,"back","Back",this::finish),new LinearLayout.LayoutParams(Design.dp(this,48),Design.dp(this,48)));navigation.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));RecordUi.menuAction(navigation,"More appearance options",this::appearanceOptions,false);body.addView(navigation);
        Design.text(body,"Appearance",28,700,Design.INK);Design.space(body,4);
        Design.text(body,"Customize how SleepWell looks and feels.",14,400,Design.MUTED);Design.space(body,6);
        LinearLayout preview=Design.row(this);Design.pad(preview,12,12);preview.setBackground(Design.shape(this,Design.SOFT,20));SleepPlan plan=SleepPlans.load(this);NightArc dial=new NightArc(this,plan.bedtime,plan.wake,0,System.currentTimeMillis(),java.time.ZoneId.systemDefault(),window->{});dial.reviewFace();dial.setOnClickListener(null);dial.setClickable(false);dial.setFocusable(false);dial.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);preview.addView(dial,new LinearLayout.LayoutParams(Design.dp(this,154),-2));LinearLayout copy=Design.col(this);Design.text(copy,"Night dial",20,500,Design.INK);Design.space(copy,8);Design.text(copy,"Black at every hour. Warm amber where it matters.",14,400,Design.MUTED);preview.addView(copy,new LinearLayout.LayoutParams(0,-2,1));body.addView(preview);Design.space(body,18);
        Design.text(body,"Make it comfortable",16,600,Design.INK);Design.space(body,6);
        LinearLayout textSize=Design.row(this);textSize.setMinimumHeight(Design.dp(this,48));textSize.setBackground(Design.touch(this,android.graphics.Color.TRANSPARENT,12));textSize.setContentDescription("Text size");textSize.setFocusable(true);textSize.setOnClickListener(v->{InteractionFeedback.action(v);textSize();});
        TextView aa=Design.text(this,"Aa",20,500,Design.INK);aa.setSingleLine(true);LinearLayout.LayoutParams aaLp=new LinearLayout.LayoutParams(Design.dp(this,28),-2);aaLp.rightMargin=Design.dp(this,16);textSize.addView(aa,aaLp);
        LinearLayout sizeCopy=Design.col(this);Design.text(sizeCopy,"Text size",15,500,Design.INK);Design.space(sizeCopy,5);Design.text(sizeCopy,"Use a comfortable size.",12,400,Design.MUTED);textSize.addView(sizeCopy,new LinearLayout.LayoutParams(0,-2,1));sizeValue=Design.text(this,AppearancePolicy.SIZES[size],13,400,Design.MUTED);LinearLayout.LayoutParams valueSpace=new LinearLayout.LayoutParams(-2,-2);valueSpace.leftMargin=Design.dp(this,8);valueSpace.rightMargin=Design.dp(this,8);textSize.addView(sizeValue,valueSpace);textSize.addView(new Design.Icon(this,"chevron",Design.INK),new LinearLayout.LayoutParams(Design.dp(this,18),Design.dp(this,24)));body.addView(textSize);Design.line(body);
        preference(body,"motion","Reduce motion","Keep controls and breathing cues still.",motion,on->motion=on);Design.line(body);
        preference(body,"contrast","High-contrast mode","Increase text and control contrast.",contrast,on->contrast=on);Design.line(body);
        Design.space(body,4);lightCard=new AppearanceLightCard(this,body,()->startActivity(RecordUi.intent(this,SmartLightActivity.class)));
        Design.space(body,8);LinearLayout footer=RecordUi.footer(body);footer.setPadding(Design.dp(this,24),Design.dp(this,6),Design.dp(this,24),Design.dp(this,8));Button save=Design.button(footer,"Save appearance",true,()->{Appearance.save(this,automatic,theme,size,motion,contrast);finish();if(motion)overridePendingTransition(0,0);});LinearLayout.LayoutParams saveSize=(LinearLayout.LayoutParams)save.getLayoutParams();saveSize.topMargin=0;save.setLayoutParams(saveSize);android.text.SpannableString caption=new android.text.SpannableString("Save appearance   \ufffc");caption.setSpan(new android.text.style.ImageSpan(RecordUi.glyph(this,"chevron",NightDialTokens.ACTION_INK,18),android.text.style.ImageSpan.ALIGN_CENTER),caption.length()-1,caption.length(),0);save.setText(caption);save.setContentDescription("Save appearance");

    }
    private Switch autoSwitch;
    private void preference(LinearLayout body,String icon,String title,String description,boolean value,java.util.function.Consumer<Boolean> action){
        LinearLayout row=Design.row(this);row.setMinimumHeight(Design.dp(this,48));Design.Icon glyph=new Design.Icon(this,icon,Design.INK);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(Design.dp(this,26),Design.dp(this,26));ip.rightMargin=Design.dp(this,18);row.addView(glyph,ip);
        LinearLayout text=Design.col(this);Design.text(text,title,15,500,Design.INK);Design.space(text,5);Design.text(text,description,12,400,Design.MUTED);row.addView(text,new LinearLayout.LayoutParams(0,-2,1));
        InteractionFeedback.Toggle toggle=new InteractionFeedback.Toggle(this);toggle.setContentDescription(title);Design.styleSwitch(this,toggle,Design.VIOLET,0xffc8c2be);toggle.setChecked(value);toggle.setOnCheckedChangeListener((v,on)->{toggle.userChanged();action.accept(on);if(motion)toggle.jumpDrawablesToCurrentState();});row.addView(toggle);body.addView(row);if(title.equals("Time of day"))autoSwitch=toggle;
    }
    private int selectedTheme(){java.time.LocalTime now=java.time.LocalTime.now();return AppearancePolicy.phase(automatic,theme,now.getHour()*60+now.getMinute());}
    private void updateSelection(){if(choices==null)return;for(int n=0;n<3;n++){boolean selected=selectedTheme()==n;choices[n].setSelected(selected);radios[n].setChecked(selected);}
        themeHelp.setText(automatic?"Choose a theme to turn Time of day off.":"Your selected theme stays on all day.");
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
    private void colorSchedule(){if(options!=null&&options.isShowing())options.dismiss();optionsPage="schedule";options=SleepPicker.message(this,"Dark at every hour","Night dial stays dark through the night and morning. Sunrise alarm output is a separate choice in your alarm settings.");}
    @Override protected void onResume(){super.onResume();if(lightCard!=null)lightCard.refresh();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
    @Override protected void onDestroy(){if(picker!=null)picker.dismiss();if(options!=null)options.dismiss();if(lightCard!=null)lightCard.close();super.onDestroy();}
}
