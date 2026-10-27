package org.sleepwell.app;

import android.content.Context;
import android.text.InputType;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import java.util.function.IntConsumer;

/** Shared production Math widgets and validator; completion belongs solely to the caller. */
final class WakeMathCard {
    static final class Binding {EditText input;TextView feedback;}
    static Binding render(LinearLayout card,MathTask task,Integer selected,String entry,boolean practice,IntConsumer choose,Runnable correct){
        Context context=card.getContext();Binding binding=new Binding();Design.text(card,practice?"Math practice":"Solve to dismiss",22,600,Design.INK).setGravity(Gravity.CENTER);Design.space(card,10);
        TextView question=Design.text(card,task.question(),64,700,Design.INK);question.setGravity(Gravity.CENTER);question.setSingleLine(true);question.setHorizontallyScrolling(false);question.setAutoSizeTextTypeUniformWithConfiguration(Math.max(24,Math.round(28*Appearance.scale(context))),Math.round(64*Appearance.scale(context)),2,TypedValue.COMPLEX_UNIT_SP);question.setContentDescription(task.spoken());Design.space(card,16);
        if(task.settings.typed){EditText input=new EditText(context);binding.input=input;input.setContentDescription(practice?"Practice answer":"Math answer");input.setHint("Your answer");input.setSingleLine(true);input.setInputType(InputType.TYPE_CLASS_NUMBER);input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(5)});input.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);input.setGravity(Gravity.CENTER);input.setTextSize(34*Appearance.scale(context));input.setTypeface(Design.font(context,600));input.setTextColor(Design.INK);input.setHintTextColor(Design.MUTED);input.setBackground(Design.outline(context,Design.SOFT,Design.LINE,20));Design.pad(input,16,18);input.setText(entry);card.addView(input,new LinearLayout.LayoutParams(-1,-2));}
        else for(int row=0;row<2;row++){LinearLayout choices=Design.row(context);for(int col=0;col<2;col++){int option=task.options.get(row*2+col);Button button=new Button(context);button.setStateListAnimator(null);button.setElevation(0);button.setText(Integer.toString(option));button.setTextSize(42*Appearance.scale(context));button.setSingleLine(true);button.setHorizontallyScrolling(false);button.setAutoSizeTextTypeUniformWithConfiguration(Math.round(24*Appearance.scale(context)),Math.round(42*Appearance.scale(context)),2,TypedValue.COMPLEX_UNIT_SP);button.setTypeface(Design.font(context,600));boolean active=selected!=null&&selected==option;button.setTextColor(active?Design.CREAM:Design.INK);button.setSelected(active);button.setContentDescription("Answer "+option);button.setBackground(Design.touch(context,active?Design.BROWN:Design.SOFT,22));LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(0,Design.dp(context,84*Math.max(1,Design.fontScale(context))),1);size.setMargins(Design.dp(context,col==0?0:4),Design.dp(context,4),Design.dp(context,col==0?4:0),Design.dp(context,4));choices.addView(button,size);button.setOnClickListener(view->choose.accept(option));}card.addView(choices);}
        binding.feedback=Design.text(card,task.settings.typed?"Type a whole-number answer":"Select an answer",13,400,Design.MUTED);binding.feedback.setGravity(Gravity.CENTER);Design.space(card,6);
        Runnable check=()->{if(task.settings.typed){String typed=binding.input.getText().toString();if(typed.trim().isEmpty()){binding.feedback.setText("Enter an answer first");return;}if(task.acceptsTyped(typed)){correct.run();return;}}else{if(selected==null){binding.feedback.setText("Choose an answer first");return;}if(task.accepts(selected)){correct.run();return;}}binding.feedback.setText(practice?"Try again":"Try again · alarm is still ringing");binding.feedback.setTextColor(0xffb33120);binding.feedback.announceForAccessibility("Try again");};
        Design.button(card,practice?"Check practice answer":"Check answer",true,check);
        if(binding.input!=null)binding.input.setOnEditorActionListener((view,action,event)->{if(action==android.view.inputmethod.EditorInfo.IME_ACTION_DONE){check.run();return true;}return false;});return binding;
    }
    private WakeMathCard(){}
}
