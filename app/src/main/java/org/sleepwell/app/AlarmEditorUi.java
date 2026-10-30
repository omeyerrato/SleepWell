package org.sleepwell.app;

import android.content.Context;
import android.graphics.drawable.InsetDrawable;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.*;

/** Alarm-only layout primitives; callers retain draft values and all navigation/save actions. */
final class AlarmEditorUi {
    static Button weekday(Context context,String label,String name,boolean selected,boolean enabled,Runnable choose){
        Button button=new Button(context){@Override protected void onSizeChanged(int width,int height,int oldWidth,int oldHeight){super.onSizeChanged(width,height,oldWidth,oldHeight);int diameter=Math.max(0,Math.min(width,height)-Design.dp(context,4)),horizontal=(width-diameter)/2,vertical=(height-diameter)/2;setBackground(new InsetDrawable(Design.touch(context,selected?Design.BROWN:Design.SOFT,24),horizontal,vertical,horizontal,vertical));}};
        button.setStateListAnimator(null);button.setElevation(0);button.setAllCaps(false);button.setText(label);button.setTextSize(16*Appearance.scale(context));button.setTypeface(Design.font(context,600));button.setIncludeFontPadding(false);button.setPadding(0,0,0,0);button.setMinWidth(0);button.setMinimumWidth(0);button.setMinHeight(0);button.setMinimumHeight(0);button.setTextColor(Appearance.color(context,selected?Design.CREAM:Design.BROWN));button.setSelected(selected);button.setContentDescription(name);button.setEnabled(enabled);button.setAlpha(enabled?1:.45f);
        button.setAccessibilityDelegate(new View.AccessibilityDelegate(){@Override public void onInitializeAccessibilityNodeInfo(View host,AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(host,info);info.setClassName(CheckBox.class.getName());info.setCheckable(true);info.setChecked(selected);}});
        button.setOnClickListener(view->{InteractionFeedback.selection(view);choose.run();});return button;
    }
    static LinearLayout item(LinearLayout body,String icon,String title,String value,Runnable choose){
        Context context=body.getContext();LinearLayout row=Design.row(context);Design.pad(row,0,8);row.setMinimumHeight(Design.dp(context,56));
        if(icon!=null){LinearLayout.LayoutParams glyph=new LinearLayout.LayoutParams(Design.dp(context,26),Design.dp(context,26));glyph.setMarginEnd(Design.dp(context,14));row.addView(new Design.Icon(context,icon,Design.BROWN),glyph);}
        TextView name=Design.text(context,title,16,500,Design.INK);name.setMaxLines(2);row.addView(name,new LinearLayout.LayoutParams(0,-2,icon==null?1:3));
        if(value!=null){TextView detail=Design.text(context,value,icon==null?15:14,400,Design.MUTED);detail.setGravity(Gravity.END);detail.setMaxLines(2);detail.setEllipsize(android.text.TextUtils.TruncateAt.END);row.setTooltipText(title+": "+value);row.addView(detail,new LinearLayout.LayoutParams(0,-2,icon==null?3:2));row.setStateDescription(value);}
        LinearLayout.LayoutParams caret=new LinearLayout.LayoutParams(Design.dp(context,18),Design.dp(context,18));caret.setMarginStart(Design.dp(context,8));row.addView(new Design.Icon(context,"chevron",Design.BROWN),caret);row.setContentDescription(title);row.setFocusable(true);row.setBackground(Design.touch(context,android.graphics.Color.TRANSPARENT,12));row.setAccessibilityDelegate(new View.AccessibilityDelegate(){@Override public void onInitializeAccessibilityNodeInfo(View host,AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(host,info);info.setClassName(Button.class.getName());}});row.setOnClickListener(view->{InteractionFeedback.action(view);choose.run();});body.addView(row,new LinearLayout.LayoutParams(-1,-2));return row;
    }
    static void divider(LinearLayout body){View line=new View(body.getContext());line.setBackgroundColor(Appearance.color(body.getContext(),Design.LINE));LinearLayout.LayoutParams bounds=new LinearLayout.LayoutParams(-1,Design.dp(body.getContext(),1));bounds.setMarginStart(Design.dp(body.getContext(),40));body.addView(line,bounds);}
    static void enabled(LinearLayout parent,String subtitle,boolean checked,java.util.function.Consumer<Boolean> change){
        Context context=parent.getContext();LinearLayout row=Design.row(context);Design.pad(row,14,12);row.setMinimumHeight(Design.dp(context,72));row.setBackground(Design.shape(context,Design.SOFT,16));LinearLayout.LayoutParams glyph=new LinearLayout.LayoutParams(Design.dp(context,26),Design.dp(context,26));glyph.setMarginEnd(Design.dp(context,14));row.addView(new Design.Icon(context,"bell",Design.BROWN),glyph);
        LinearLayout labels=Design.col(context);TextView state=Design.text(labels,checked?"Alarm on":"Alarm off",16,600,Design.INK);Design.space(labels,4);Design.text(labels,subtitle,12,400,Design.MUTED);LinearLayout.LayoutParams copy=new LinearLayout.LayoutParams(0,-2,1);copy.setMarginEnd(Design.dp(context,12));row.addView(labels,copy);
        InteractionFeedback.Toggle toggle=new InteractionFeedback.Toggle(context);toggle.setContentDescription("Alarm on");Design.styleSwitch(context,toggle,Design.BROWN,0xffbcb5b0);toggle.setChecked(checked);toggle.setOnCheckedChangeListener((view,on)->{toggle.userChanged();state.setText(on?"Alarm on":"Alarm off");change.accept(on);if(!InteractionFeedback.motion(context))toggle.jumpDrawablesToCurrentState();});row.addView(toggle);parent.addView(row,new LinearLayout.LayoutParams(-1,-2));
    }
    private AlarmEditorUi(){}
}
