package org.sleepwell.app;
import android.content.Context;import android.content.SharedPreferences;
/** Saved synthetic-speech choices. Loading and saving never initialize or play speech. */
final class NarrationSettings {
 static final int[] RATES={75,90,100,115},PITCHES={85,100,115};
 static final String[] RATE_LABELS={"Slower · 0.75×","Relaxed · 0.90×","Normal · 1.00×","Faster · 1.15×"},PITCH_LABELS={"Lower · 0.85×","Natural · 1.00×","Higher · 1.15×"};
 static final NarrationSettings DEFAULT=new NarrationSettings(90,100);
 final int rate,pitch;
 NarrationSettings(int rate,int pitch){if(index(RATES,rate)<0||index(PITCHES,pitch)<0)throw new IllegalArgumentException("Choose one of the saved narration presets.");this.rate=rate;this.pitch=pitch;}
 private static SharedPreferences prefs(Context context){return context.getSharedPreferences("guided_narration_v1",Context.MODE_PRIVATE);}
 static NarrationSettings load(Context context){SharedPreferences saved=prefs(context);return new NarrationSettings(saved.getInt("rate",90),saved.getInt("pitch",100));}
 void save(Context context){if(!prefs(context).edit().putInt("rate",rate).putInt("pitch",pitch).commit())throw new IllegalStateException("Narration settings could not be saved. Choose them again before the next practice.");}
 static int index(int[] values,int value){for(int n=0;n<values.length;n++)if(values[n]==value)return n;return -1;}
 float speechRate(){return rate/100f;}float speechPitch(){return pitch/100f;}
 String rateLabel(){return RATE_LABELS[index(RATES,rate)];}String pitchLabel(){return PITCH_LABELS[index(PITCHES,pitch)];}
}
