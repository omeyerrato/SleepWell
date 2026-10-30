package org.sleepwell.app;

import android.view.Gravity;
import android.widget.*;
import org.json.JSONException;

/** Read-only presentation of the typed profile; it never loads or applies local preferences. */
final class SettingsProfileReviewUi {
    static void add(LinearLayout parent,SettingsProfile profile)throws JSONException {
        if(profile.appearance!=null){
            LinearLayout card=card(parent,"Appearance");boolean automatic=SettingsProfile.flag(profile.appearance,"automatic");String theme=AppearancePolicy.THEMES[SettingsProfile.number(profile.appearance,"theme",0,2)];
            value(card,"Theme",automatic?"Time of day":theme);if(automatic)value(card,"Manual fallback",theme);
            value(card,"Text size",AppearancePolicy.SIZES[SettingsProfile.number(profile.appearance,"textSize",0,2)]);value(card,"Reduce motion",on(SettingsProfile.flag(profile.appearance,"reduceMotion")));value(card,"High contrast",on(SettingsProfile.flag(profile.appearance,"highContrast")));
            value(card,"Room & Wind-down dimming",profile.appearance.has("roomDimming")?on(SettingsProfile.flag(profile.appearance,"roomDimming")):"Unchanged");value(card,"Touch feedback",profile.hasFeedback()?on(SettingsProfile.flag(profile.appearance,SettingsProfile.FEEDBACK_ENABLED)):"Unchanged");value(card,"Feedback strength",profile.hasFeedback()?profile.feedbackStrengthLabel():"Unchanged");
        }
        if(profile.plan!=null){
            SleepPlan plan=profile.sleepPlan();LinearLayout card=card(parent,"Sleep plan");value(card,"Sleep goal",PlanActivity.duration(plan.goal));value(card,"Bedtime",Design.clock(parent.getContext(),plan.bedtime));value(card,"Wake-up",Design.clock(parent.getContext(),plan.wake));value(card,"Wind-down",plan.windDown+" min before bed");value(card,"Reminder advance",plan.reminderAdvance+" min");value(card,"Reminder repeat",plan.smartRepeatMinutes==0?"Off":plan.smartRepeatMinutes+" min");value(card,"Repeat limit",Integer.toString(plan.smartRepeatLimit));
            value(card,"Reminder in file",on(SettingsProfile.flag(profile.plan,"reminder")));value(card,"Smart Bedtime in file",on(SettingsProfile.flag(profile.plan,"smartBedtime")));value(card,"No-alarm reminder in file",on(SettingsProfile.flag(profile.plan,"noAlarmBedtime")));
        }
        if(profile.sounds!=null){
            SoundMix sound=profile.soundMix();LinearLayout card=card(parent,"Bundled sounds");value(card,"Sound",sound.label());value(card,"Primary level",sound.primaryGain+"%");value(card,"Brown noise level",sound.brownGain+"%");value(card,"Timer",sound.timerMinutes+" min");value(card,"Fade out",sound.fadeMinutes==0?"Off":sound.fadeMinutes+" min");value(card,"Stored rotation",String.join(" → ",sound.rotation.sounds));value(card,"Rotation interval",sound.rotation.minutes+" min");value(card,"Stored stereo carrier",sound.binauralCarrier+" Hz");value(card,"Stored stereo beat",sound.binauralBeat+" Hz");value(card,"Play with tracking in file",on(SettingsProfile.flag(profile.sounds,"withTracking")));
        }
        if(profile.narration!=null){NarrationSettings narration=profile.narrationSettings();LinearLayout card=card(parent,"Narration");value(card,"Speed",narration.rateLabel());value(card,"Pitch",narration.pitchLabel());Design.space(card,6);Design.text(card,"Voice selection is not included. Nothing will be spoken.",12,400,Design.MUTED);}
        if(profile.tonight!=null){TonightLayout.Config layout=profile.tonightLayout();LinearLayout card=card(parent,"Tonight layout");int order=1;for(String id:layout.order)value(card,(order++)+". "+TonightLayout.title(id),layout.visible.contains(id)?"Shown":"Hidden");value(card,"Bedside illustration",layout.illustration?"Shown":"Hidden");Design.space(card,6);Design.text(card,"Alarm, tracking and nap safety controls stay available.",12,400,Design.MUTED);}
    }
    private static String on(boolean value){return value?"On":"Off";}
    private static LinearLayout card(LinearLayout parent,String title){Design.space(parent,12);LinearLayout card=Design.col(parent.getContext());Design.pad(card,16,14);card.setBackground(Design.shape(parent.getContext(),Design.SOFT,18));Design.text(card,title,18,600,Design.INK);Design.space(card,10);parent.addView(card,new LinearLayout.LayoutParams(-1,-2));return card;}
    private static void value(LinearLayout card,String label,String value){
        if(value.length()>36||value.contains("→")){Design.space(card,6);Design.text(card,label,12,400,Design.MUTED);Design.space(card,4);Design.text(card,value,14,500,Design.INK);Design.space(card,8);return;}
        LinearLayout row=Design.row(card.getContext());row.setGravity(Gravity.TOP);row.setMinimumHeight(Design.dp(card.getContext(),36));Design.pad(row,0,6);row.addView(Design.text(card.getContext(),label,13,400,Design.MUTED),new LinearLayout.LayoutParams(0,-2,1));TextView text=Design.text(card.getContext(),value,14,500,Design.INK);text.setGravity(Gravity.END);LinearLayout.LayoutParams right=new LinearLayout.LayoutParams(0,-2,1);right.leftMargin=Design.dp(card.getContext(),12);row.addView(text,right);card.addView(row,new LinearLayout.LayoutParams(-1,-2));
    }
    private SettingsProfileReviewUi(){}
}
