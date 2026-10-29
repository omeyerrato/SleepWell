package org.sleepwell.app;

import android.app.*;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.util.Locale;
import java.util.function.IntConsumer;

/** SleepWell-owned exact duration review; dismiss/cancel never applies typed changes. */
final class ExactTimerPicker extends Dialog {
    private final EditText hours,minutes,seconds;
    ExactTimerPicker(Activity owner,int value,Bundle restored,IntConsumer use){
        super(owner);requestWindowFeature(Window.FEATURE_NO_TITLE);int selected=Math.max(1,Math.min(86400,value));
        LinearLayout body=Design.col(owner);Design.pad(body,20,20);body.setBackground(Design.shape(owner,Design.CREAM,22));
        Design.text(body,"Timer duration",23,600,Design.INK);Design.space(body,12);Design.text(body,"1 second to 24 hours",13,400,Design.MUTED);Design.space(body,14);
        LinearLayout row=Design.row(owner);hours=field(owner,row,"Hours",selected/3600);minutes=field(owner,row,"Minutes",selected/60%60);seconds=field(owner,row,"Seconds",selected%60);body.addView(row);
        if(restored!=null){hours.setText(restored.getString("hours",hours.getText().toString()));minutes.setText(restored.getString("minutes",minutes.getText().toString()));seconds.setText(restored.getString("seconds",seconds.getText().toString()));}
        Design.space(body,12);TextView error=Design.text(body,"",13,500,Design.BROWN);error.setVisibility(View.GONE);error.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        SleepPicker.presetChoices(body,new int[]{60,300,600,1200,1800,5400},n->n/60+" min",this::selectedDuration,preset->{hours.setText(String.format(Locale.ROOT,"%02d",preset/3600));minutes.setText(String.format(Locale.ROOT,"%02d",preset/60%60));seconds.setText(String.format(Locale.ROOT,"%02d",preset%60));error.setVisibility(View.GONE);},hours,minutes,seconds);
        Design.button(body,"Use exact duration",true,()->{
            try{int h=Integer.parseInt(hours.getText().toString()),m=Integer.parseInt(minutes.getText().toString()),s=Integer.parseInt(seconds.getText().toString());long total=h*3600L+m*60L+s;
                if(h<0||h>24||m<0||m>59||s<0||s>59||total<1||total>86400)throw new IllegalArgumentException();dismiss();use.accept((int)total);
            }catch(IllegalArgumentException invalid){error.setText("Choose hours 0–24, minutes and seconds 0–59, totaling 1 second to 24 hours.");error.setVisibility(View.VISIBLE);}
        });
        Design.button(body,"Cancel",false,this::dismiss);ScrollView scroll=new ScrollView(owner);scroll.addView(body);setContentView(scroll);show();getWindow().setBackgroundDrawableResource(android.R.color.transparent);getWindow().setLayout(Math.min(owner.getResources().getDisplayMetrics().widthPixels-Design.dp(owner,32),Design.dp(owner,440)),-2);getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }
    private int selectedDuration(){try{int h=Integer.parseInt(hours.getText().toString()),m=Integer.parseInt(minutes.getText().toString()),s=Integer.parseInt(seconds.getText().toString());long total=h*3600L+m*60L+s;return h>=0&&h<=24&&m>=0&&m<=59&&s>=0&&s<=59&&total>=1&&total<=86400?(int)total:-1;}catch(NumberFormatException invalid){return -1;}}
    private EditText field(Activity owner,LinearLayout row,String name,int value){
        LinearLayout column=Design.col(owner);Design.pad(column,4,0);Design.text(column,name,13,500,Design.MUTED).setGravity(Gravity.CENTER);Design.space(column,8);
        EditText edit=new EditText(owner);edit.setText(String.format(Locale.ROOT,"%02d",value));edit.setContentDescription("Timer duration "+name.toLowerCase(Locale.ROOT));edit.setSingleLine(true);edit.setSelectAllOnFocus(true);edit.setGravity(Gravity.CENTER);edit.setInputType(InputType.TYPE_CLASS_NUMBER);edit.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(2)});edit.setTextSize(30*Appearance.scale(owner));edit.setTypeface(Design.font(owner,600));edit.setTextColor(Design.INK);Design.pad(edit,6,14);edit.setBackground(Design.outline(owner,Design.SOFT,Design.LINE,12));column.addView(edit,new LinearLayout.LayoutParams(-1,-2));row.addView(column,new LinearLayout.LayoutParams(0,-2,1));return edit;
    }
    Bundle draft(){Bundle out=new Bundle();out.putString("hours",hours.getText().toString());out.putString("minutes",minutes.getText().toString());out.putString("seconds",seconds.getText().toString());return out;}
}
