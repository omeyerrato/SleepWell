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
            this.phase=phase;dark=phase!=2;
            if(phase==2){bg=0xffffd291;panel=0xffffe5bc;dock=0xfff6c88e;selected=0xffffedd2;ink=Design.INK;
                softInk=contrast?Design.INK:0xff66391d;accent=Design.BROWN;buttonInk=Design.CREAM;line=contrast?0xff845b34:0xffcfaa7b;quiet=contrast?0xff845b34:0xffb69577;
            }else if(phase==1){bg=0xff483768;panel=0xff5a467a;dock=0xff42315e;selected=0xff715495;ink=0xfffff9ff;
                softInk=contrast?0xffffffff:0xffe8d7ff;accent=0xffe1c9ff;buttonInk=Design.PLUM;line=contrast?0xffd9c4fc:0xff8a72a3;quiet=contrast?0xffd9c4fc:0xffa68eb7;
            }else{bg=Design.PLUM;panel=0xff3a3046;dock=0xff332c3e;selected=0xff50415f;ink=Design.CREAM;
                softInk=contrast?0xfffaf4ff:Design.LILAC;accent=Design.LILAC;buttonInk=Design.PLUM;line=contrast?0xffd9c4fc:0xff62566d;quiet=contrast?0xffd9c4fc:0xff756286;}
        }
    }
}
