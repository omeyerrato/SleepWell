package org.sleepwell.app;

import android.widget.*;

/** Listening screens share the same action hierarchy as the rest of Night Dial. */
final class SleepMixerUi {
    static Button button(LinearLayout parent,String label,boolean primary,Runnable action){return Design.button(parent,label,primary,action);}
    private SleepMixerUi(){}
}
