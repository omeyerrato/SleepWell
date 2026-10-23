package org.sleepwell.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.*;
import android.view.Gravity;
import android.widget.*;

/** Board 07 geometry on native progress/seek controls; semantics and keyboard gestures stay native. */
final class SoundSliders {
    static void style(ProgressBar bar){
        Context c=bar.getContext();int height=Design.dp(c,10);
        GradientDrawable base=Design.shape(c,0xff4b3e60,20);base.setSize(Design.dp(c,1),height);
        GradientDrawable fill=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{Design.VIOLET,Design.LILAC});fill.setCornerRadius(Design.dp(c,20));fill.setSize(Design.dp(c,1),height);
        ClipDrawable clip=new ClipDrawable(fill,Gravity.LEFT,ClipDrawable.HORIZONTAL);
        LayerDrawable track=new LayerDrawable(new android.graphics.drawable.Drawable[]{base,clip});track.setId(0,android.R.id.background);track.setId(1,android.R.id.progress);
        bar.setProgressTintList(null);bar.setProgressBackgroundTintList(null);bar.setProgressDrawable(track);bar.setMinHeight(height);bar.setMaxHeight(height);
        if(bar instanceof SeekBar){SeekBar seek=(SeekBar)bar;StateListDrawable thumb=new StateListDrawable();
            GradientDrawable focused=Design.shape(c,0xfffaf5ff,30);focused.setSize(Design.dp(c,24),Design.dp(c,24));focused.setStroke(Design.dp(c,2),Design.VIOLET);
            GradientDrawable normal=Design.shape(c,0xfffaf5ff,30);normal.setSize(Design.dp(c,24),Design.dp(c,24));normal.setStroke(Design.dp(c,1),0x33ffffff);
            thumb.addState(new int[]{android.R.attr.state_pressed},focused);thumb.addState(new int[]{android.R.attr.state_focused},focused);thumb.addState(new int[]{},normal);
            seek.setThumbTintList(null);seek.setThumb(thumb);seek.setThumbOffset(Design.dp(c,12));seek.setSplitTrack(false);seek.setPadding(Design.dp(c,12),0,Design.dp(c,12),0);
        }
    }
}
