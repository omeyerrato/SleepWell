package org.sleepwell.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalTime;

/** Shared rendering preferences. Saving is atomic; editing never changes alarms or records. */
final class Appearance {
    static final String AUTO="appearanceAuto",THEME="appearanceTheme",SIZE="appearanceTextSize",
        MOTION="appearanceReduceMotion",CONTRAST="appearanceHighContrast";
    static boolean automatic(Context c){return Prefs.of(c).getBoolean(AUTO,true);}
    static boolean reduced(Context c){return Prefs.flag(c,MOTION);}
    static boolean contrast(Context c){return Prefs.flag(c,CONTRAST);}
    static float scale(Context c){return AppearancePolicy.textScale(Prefs.number(c,SIZE,0));}
    static int color(Context c,int color){return contrast(c)&&color==Design.MUTED?Design.INK:color;}
    static Palette palette(Context c){LocalTime now=LocalTime.now();return new Palette(AppearancePolicy.phase(automatic(c),Prefs.number(c,THEME,0),now.getHour()*60+now.getMinute()),contrast(c));}
    static void save(Context c,boolean auto,int theme,int size,boolean motion,boolean contrast){
        SharedPreferences.Editor edit=Prefs.of(c).edit();edit.putBoolean(AUTO,auto).putInt(THEME,theme)
            .putInt(SIZE,size).putBoolean(MOTION,motion).putBoolean(CONTRAST,contrast).apply();
    }
    static final class Palette {
        final int phase,bg,panel,dock,selected,ink,softInk,accent,buttonInk,line,quiet;
        final boolean dark;
        Palette(int phase,boolean contrast){
            // Keep saved phase values readable for integration; no phase may illuminate the room.
            this.phase=phase;dark=true;
            bg=NightDialTokens.GROUND;panel=NightDialTokens.SURFACE;dock=NightDialTokens.GROUND;
            selected=NightDialTokens.RAISED;ink=NightDialTokens.TEXT;
            softInk=contrast?NightDialTokens.TEXT:NightDialTokens.SECONDARY;
            accent=NightDialTokens.ACCENT;buttonInk=NightDialTokens.ACCENT_INK;
            line=contrast?NightDialTokens.OUTLINE:NightDialTokens.RULE;
            quiet=contrast?NightDialTokens.SECONDARY:NightDialTokens.META;
        }
    }
}
