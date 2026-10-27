package org.sleepwell.app;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

/** Event-list chrome only; categories and playback remain owned by their existing controllers. */
final class SoundEventUi {
    static void categories(LinearLayout body,String selected,ToIntFunction<String> count,Consumer<String> choose){
        Context context=body.getContext();LinearLayout group=Design.row(context);
        group.setContentDescription("Sound categories");
        for(String key:List.of("all","snoring","speech")){
            String label=SoundBrowseFilter.name(key)+" ("+count.applyAsInt(key)+")";boolean active=key.equals(selected);
            TextView chip=Design.text(context,label,13,active?600:400,active?Design.CREAM:Design.INK);
            chip.setGravity(Gravity.CENTER);chip.setMinimumHeight(Design.dp(context,48));Design.pad(chip,6,8);
            chip.setBackground(Design.touch(context,active?Design.BROWN:Design.SOFT,12));chip.setContentDescription(label);chip.setFocusable(true);SleepPicker.radio(chip,active);
            chip.setOnClickListener(v->{if(!active)InteractionFeedback.selection(v);choose.accept(key);});
            LinearLayout.LayoutParams layout=new LinearLayout.LayoutParams(0,-1,1);if(group.getChildCount()>0)layout.leftMargin=Design.dp(context,8);group.addView(chip,layout);
        }
        body.addView(group,new LinearLayout.LayoutParams(-1,-2));
    }
    static void button(View view){view.setAccessibilityDelegate(new View.AccessibilityDelegate(){@Override public void onInitializeAccessibilityNodeInfo(View host,AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(host,info);info.setClassName(Button.class.getName());}});}
    private SoundEventUi(){}
}
