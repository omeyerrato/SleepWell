package org.sleepwell.app;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Looper;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Switch;

/** Short foreground input acknowledgements, never output alarms or async success signals. */
final class InteractionFeedback {
    static final String ENABLED="interactionHapticsEnabled";
    static final String STRENGTH="interactionHapticsStrength";
    static final int LIGHT=0,STANDARD=1,FIRM=2;
    private static long lastFeedback;
    static boolean enabled(Context context){try{return Prefs.of(context).getBoolean(ENABLED,true);}catch(RuntimeException unreadable){return false;}}
    static boolean setEnabled(Context context,boolean enabled){try{return Prefs.of(context).edit().putBoolean(ENABLED,enabled).commit();}catch(RuntimeException unavailable){return false;}}
    static int strength(Context context){try{int saved=Prefs.of(context).getInt(STRENGTH,STANDARD);return saved>=LIGHT&&saved<=FIRM?saved:STANDARD;}catch(RuntimeException unavailable){return STANDARD;}}
    static boolean setStrength(Context context,int strength){if(strength<LIGHT||strength>FIRM)return false;try{return Prefs.of(context).edit().putInt(STRENGTH,strength).commit();}catch(RuntimeException unavailable){return false;}}
    // AOSP maps these requests to texture tick, click and heavy click respectively.
    // OEMs still control the sensation; these are effect choices, not an amplitude override.
    static int effectForStrength(int strength){return switch(strength){case LIGHT->HapticFeedbackConstants.CLOCK_TICK;case FIRM->HapticFeedbackConstants.LONG_PRESS;default->HapticFeedbackConstants.VIRTUAL_KEY;};}
    /** No extra animation when reduced motion, system animation settings or touch exploration require quiet chrome. */
    static boolean motion(Context context){
        try{AccessibilityManager access=context.getSystemService(AccessibilityManager.class);return !Appearance.reduced(context)&&android.animation.ValueAnimator.areAnimatorsEnabled()&&(access==null||!access.isTouchExplorationEnabled());}catch(RuntimeException unavailable){return false;}
    }
    // Standard retains the previous platform click until the user saves another strength.
    static void selection(View source){acknowledge(source);}
    static void action(View source){acknowledge(source);}
    private static Activity activity(Context context){for(int n=0;n<12;n++){if(context instanceof Activity)return(Activity)context;if(!(context instanceof ContextWrapper))return null;Context base=((ContextWrapper)context).getBaseContext();if(base==context)return null;context=base;}return null;}
    private static boolean allowed(View source){
        if(source==null||Looper.myLooper()!=Looper.getMainLooper()||!source.isEnabled()||!source.isShown()||!source.isAttachedToWindow()||!source.hasWindowFocus()||!source.isHapticFeedbackEnabled())return false;
        Context context=source.getContext();if(!enabled(context))return false;
        Activity activity=activity(context);if(activity==null||activity.isFinishing()||activity.isDestroyed())return false;
        try{
            if(activity.getIntent()!=null&&(activity.getIntent().getBooleanExtra("visualReview",false)||activity.getIntent().getBooleanExtra("quietInteractionFeedback",false)))return false;
            if(Prefs.flag(context,"ringing")||AlarmService.active||TrackingService.running||MicrophoneLease.occupied())return false;
            String screen=activity.getClass().getSimpleName();
            if(screen.equals("AlarmActivity")||screen.equals("TrackingActivity")||screen.equals("MotionSensorActivity")||screen.equals("SpeakChallengeActivity")||screen.equals("PhraseChallengeActivity")||screen.equals("AudioChallengePreviewActivity")||screen.equals("NoteChallengeActivity"))return false;
            AccessibilityManager access=context.getSystemService(AccessibilityManager.class);return access==null||!access.isTouchExplorationEnabled();
        }catch(RuntimeException unavailable){return false;}
    }
    private static void acknowledge(View source){
        if(!allowed(source))return;
        int effect=effectForStrength(strength(source.getContext()));
        long now=SystemClock.uptimeMillis();if(lastFeedback>0&&now-lastFeedback<80)return;
        // Flags are deliberately zero: Android and the View retain final user-setting control.
        try{if(source.performHapticFeedback(effect,0))lastFeedback=now;}catch(RuntimeException unavailable){/* An optional acknowledgement never changes the action. */}
    }
    /** Native Switch gestures only. setChecked/restore/render never produce this feedback. */
    static final class Toggle extends Switch {
        private boolean feedbackDispatch;private int userDispatch;
        Toggle(Context context){super(context);}
        @Override public boolean performClick(){userDispatch++;try{return super.performClick();}finally{userDispatch--;}}
        @Override public boolean onTouchEvent(MotionEvent event){userDispatch++;try{return super.onTouchEvent(event);}finally{userDispatch--;}}
        // Native Switch versions may already request haptics. One owned dispatch avoids doubling them.
        @Override public boolean performHapticFeedback(int effect,int flags){return feedbackDispatch&&super.performHapticFeedback(effect,flags);}
        void userChanged(){if(userDispatch<=0)return;feedbackDispatch=true;try{selection(this);}finally{feedbackDispatch=false;}}
    }
    private InteractionFeedback(){}
}
