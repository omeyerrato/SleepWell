package org.sleepwell.app;
import android.graphics.drawable.*;
import android.view.Gravity;
import android.widget.SeekBar;
/** Board 03's warm thick track, retaining native range/keyboard accessibility. */
final class AlarmSliders {
    static void style(SeekBar bar){style(bar,0xffe6d4c5,Design.BROWN,Design.CREAM,Design.BROWN);}
    static void style(SeekBar bar,int background,int foreground,int thumbColor,int outline){android.content.Context c=bar.getContext();int height=Design.dp(c,10);GradientDrawable base=Design.shape(c,background,20),fill=Design.shape(c,foreground,20);base.setSize(1,height);fill.setSize(1,height);LayerDrawable track=new LayerDrawable(new android.graphics.drawable.Drawable[]{base,new ClipDrawable(fill,Gravity.LEFT,ClipDrawable.HORIZONTAL)});track.setId(0,android.R.id.background);track.setId(1,android.R.id.progress);bar.setProgressTintList(null);bar.setProgressBackgroundTintList(null);bar.setProgressDrawable(track);bar.setMinHeight(height);bar.setMaxHeight(height);GradientDrawable thumb=Design.shape(c,thumbColor,20);thumb.setSize(Design.dp(c,24),Design.dp(c,24));thumb.setStroke(Design.dp(c,2),outline);bar.setThumbTintList(null);bar.setThumb(thumb);bar.setThumbOffset(Design.dp(c,12));bar.setSplitTrack(false);bar.setPadding(Design.dp(c,12),0,Design.dp(c,12),0);}
}
