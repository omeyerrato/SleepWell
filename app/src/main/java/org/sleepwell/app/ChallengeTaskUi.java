package org.sleepwell.app;

import android.app.Activity;
import android.content.Context;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.function.*;

/** Task-only controls: adding, selecting a value and opening information have different affordances. */
final class ChallengeTaskUi {
    static void catalog(Activity activity,LinearLayout body,String query,String category,boolean room,Function<String,String> summary,Consumer<String> changed,Consumer<String> categoryChanged,Consumer<String> configure){
        LinearLayout search=Design.row(activity);search.setBackground(Design.shape(activity,Design.SOFT,14));
        EditText input=new EditText(activity);input.setSingleLine(true);input.setHint("Search tasks");input.setContentDescription("Search task types");input.setTextColor(Design.INK);input.setHintTextColor(Design.MUTED);input.setTypeface(Design.font(activity,400));input.setTextSize(16*Appearance.scale(activity));input.setBackgroundColor(android.graphics.Color.TRANSPARENT);input.setPadding(Design.dp(activity,14),Design.dp(activity,10),0,Design.dp(activity,10));input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(80)});input.setText(query);search.addView(input,new LinearLayout.LayoutParams(0,-2,1));
        input.setMinHeight(Design.dp(activity,48));View clear=Design.iconButton(activity,"close","Clear task search",()->input.setText(""));search.addView(clear,new LinearLayout.LayoutParams(Design.dp(activity,48),Design.dp(activity,48)));body.addView(search);Design.space(body,10);
        List<String> categories=List.of("All","Think","Move","Camera","Voice","Codes");
        String[] selected={categories.contains(category)?category:"All"};
        HorizontalScrollView categoryScroll=new HorizontalScrollView(activity);categoryScroll.setHorizontalScrollBarEnabled(false);categoryScroll.setClipToPadding(false);LinearLayout chips=Design.row(activity);categoryScroll.addView(chips);body.addView(categoryScroll,new LinearLayout.LayoutParams(-1,-2));Design.space(body,4);
        LinearLayout results=Design.col(activity);body.addView(results);Map<String,Button> controls=new LinkedHashMap<>();
        Runnable render=()->{
            results.removeAllViews();String text=input.getText().toString().trim().toLowerCase(Locale.ROOT);clear.setVisibility(text.isEmpty()?View.GONE:View.VISIBLE);
            for(Map.Entry<String,Button> entry:controls.entrySet()){boolean active=selected[0].equals(entry.getKey());Button chip=entry.getValue();chip.setTextColor(active?Design.CREAM:Design.INK);chip.setTypeface(Design.font(activity,active?600:400));chip.setBackground(Design.touch(activity,active?Design.BROWN:Design.SOFT,20));SleepPicker.radio(chip,active);}
            int count=0;
            for(String group:List.of("Think & type","Move","Camera & light","Voice & sound","Codes & tags")){
                if(!selected[0].equals("All")&&!category(group).equals(selected[0]))continue;
                List<String> matches=new ArrayList<>();for(String type:ChallengeRoutine.TYPES)if(group.equals(group(type))&&(text.isEmpty()||(type+" "+summary.apply(type)).toLowerCase(Locale.ROOT).contains(text)))matches.add(type);
                if(matches.isEmpty())continue;count+=matches.size();Design.space(results,12);Design.text(results,group,15,600,Design.INK);Design.space(results,4);
                for(String type:matches)ChallengeRoutineUi.task(results,type,summary.apply(type),room,()->configure.accept(type));
            }
            if(count==0){Design.space(results,20);TextView empty=Design.text(results,"No matching tasks",17,600,Design.INK);empty.setContentDescription("No matching task types");Design.space(results,6);Design.text(results,"Try another category or clear the search.",13,400,Design.MUTED);}
        };
        for(String label:categories){Button chip=new Button(activity);chip.setAllCaps(false);chip.setText(label);chip.setTextSize(13*Appearance.scale(activity));chip.setStateListAnimator(null);chip.setElevation(0);chip.setMinHeight(Design.dp(activity,48));chip.setMinimumHeight(Design.dp(activity,48));chip.setMinWidth(0);chip.setMinimumWidth(0);chip.setPadding(Design.dp(activity,16),0,Design.dp(activity,16),0);chip.setContentDescription("Task category "+label);LinearLayout.LayoutParams spacing=new LinearLayout.LayoutParams(-2,Design.dp(activity,48));spacing.rightMargin=Design.dp(activity,6);chips.addView(chip,spacing);controls.put(label,chip);chip.setOnClickListener(v->{if(selected[0].equals(label))return;InteractionFeedback.selection(v);selected[0]=label;categoryChanged.accept(label);render.run();});}
        input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){changed.accept(s.toString());render.run();}public void afterTextChanged(Editable text){}});render.run();
        body.setFocusableInTouchMode(true);body.requestFocus();
    }
    private static String category(String group){return switch(group){case "Think & type"->"Think";case "Camera & light"->"Camera";case "Voice & sound"->"Voice";case "Codes & tags"->"Codes";default->"Move";};}
    private static String group(String type){return switch(type){case "Shake","Walk","Tilt maze","Jumping","Spin around"->"Move";case "Picture match","Object recognition","Smile","Pose","Light","Camera light"->"Camera & light";case "Speak","Laughter","Loud sound","Musical note","Spoken phrase"->"Voice & sound";case "QR code","NFC"->"Codes & tags";default->"Think & type";};}
    static void values(LinearLayout parent,String title,int selected,int[] values,IntFunction<String> visible,IntFunction<String> spoken,IntConsumer choose){
        Context context=parent.getContext();Design.text(parent,title,15,600,Design.INK);Design.space(parent,6);Flow row=new Flow(context);row.setContentDescription(title);int[] current={selected};Map<Integer,Button> buttons=new LinkedHashMap<>();for(int value:values){Button button=new Button(context);button.setAllCaps(false);button.setText(visible.apply(value));button.setTextSize(15*Appearance.scale(context));button.setTypeface(Design.font(context,selected==value?600:400));button.setTextColor(selected==value?Design.CREAM:Design.BROWN);button.setBackground(Design.touch(context,selected==value?Design.BROWN:Design.SOFT,12));button.setStateListAnimator(null);button.setElevation(0);button.setMinWidth(Design.dp(context,48));button.setMinimumWidth(Design.dp(context,48));button.setMinHeight(Design.dp(context,48));button.setMinimumHeight(Design.dp(context,48));button.setPadding(Design.dp(context,12),Design.dp(context,8),Design.dp(context,12),Design.dp(context,8));button.setContentDescription(spoken.apply(value));SleepPicker.radio(button,selected==value);buttons.put(value,button);button.setOnClickListener(v->{if(value==current[0])return;InteractionFeedback.selection(v);choose.accept(value);current[0]=value;for(Map.Entry<Integer,Button> item:buttons.entrySet()){boolean active=item.getKey()==value;Button control=item.getValue();control.setTextColor(active?Design.CREAM:Design.BROWN);control.setTypeface(Design.font(context,active?600:400));control.setBackground(Design.touch(context,active?Design.BROWN:Design.SOFT,12));SleepPicker.radio(control,active);}});row.addView(button,new ViewGroup.LayoutParams(-2,-2));}parent.addView(row,new LinearLayout.LayoutParams(-1,-2));Design.space(parent,10);
    }
    static void footer(LinearLayout body,String title,String detail,String error,Runnable confirm,Runnable cancel){
        Context context=body.getContext();LinearLayout footer=RecordUi.footer(body);footer.setBackgroundColor(Appearance.color(context,Design.CREAM));Design.space(footer,6);TextView note=Design.text(footer,detail,12,400,Design.MUTED);note.setContentDescription("Task draft status");Design.space(footer,6);
        if(error!=null){TextView problem=Design.text(footer,error,12,500,0xffa93919);problem.setContentDescription("Math settings error");problem.setVisibility(error.isEmpty()?View.GONE:View.VISIBLE);problem.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);}
        LinearLayout actions=Design.row(context);Button discard=Design.button(actions,"Cancel",false,cancel);discard.setContentDescription("Cancel task changes");LinearLayout.LayoutParams left=new LinearLayout.LayoutParams(0,-2,1);left.rightMargin=Design.dp(context,8);discard.setLayoutParams(left);Button use=Design.button(actions,title,true,confirm);use.setLayoutParams(new LinearLayout.LayoutParams(0,-2,2));footer.addView(actions);
    }
    /** Wrap whole 48dp radio targets; never shrink hit areas to force a single row. */
    private static final class Flow extends ViewGroup {
        final int gap;Flow(Context context){super(context);gap=Design.dp(context,6);}
        @Override protected void onMeasure(int widthSpec,int heightSpec){int available=MeasureSpec.getSize(widthSpec),x=0,y=0,line=0;for(int n=0;n<getChildCount();n++){View child=getChildAt(n);child.measure(MeasureSpec.makeMeasureSpec(available,MeasureSpec.AT_MOST),MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED));int width=child.getMeasuredWidth(),height=child.getMeasuredHeight();if(x>0&&x+width>available){y+=line+gap;x=0;line=0;}x+=width+gap;line=Math.max(line,height);}setMeasuredDimension(resolveSize(available,widthSpec),resolveSize(y+line,heightSpec));}
        @Override protected void onLayout(boolean changed,int left,int top,int right,int bottom){int available=right-left,x=0,y=0,line=0;boolean rtl=getLayoutDirection()==LAYOUT_DIRECTION_RTL;for(int n=0;n<getChildCount();n++){View child=getChildAt(n);int width=child.getMeasuredWidth(),height=child.getMeasuredHeight();if(x>0&&x+width>available){y+=line+gap;x=0;line=0;}int start=rtl?available-x-width:x;child.layout(start,y,start+width,y+height);x+=width+gap;line=Math.max(line,height);}}
    }
    private ChallengeTaskUi(){}
}
