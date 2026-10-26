package org.sleepwell.app;

import android.widget.*;

/** Board 07 dark buttons; retains the shared Inter, touch feedback and minimum target size. */
final class SleepMixerUi {
    static Button button(LinearLayout parent,String label,boolean primary,Runnable action){Button button=Design.button(parent,label,primary,action);button.setTextColor(primary?Design.PLUM:0xfff7efff);button.setBackground(Design.touch(parent.getContext(),primary?Design.LILAC:0xff4b3e60,16));return button;}
    private SleepMixerUi(){}
}
