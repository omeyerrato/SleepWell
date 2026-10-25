package org.sleepwell.app;

import android.content.*;

final class SoundPreferences {
    interface Factory {SharedPreferences open(Context c);}
    static Factory factory=Prefs::of;
    static String error(Context c){return factory.open(c).getString("soundError","");}
    static void error(Context c,String message){factory.open(c).edit().putString("soundError",message).apply();}
    static SoundMix load(Context c){return load(factory.open(c));}
    static SoundMix load(SharedPreferences p){
        if(!p.contains("mixVersion"))return SoundMix.legacy(p.getString("lullaby","Off"),p.getInt("lullabyMinutes",30));
        SoundMix m=new SoundMix();m.primary=p.getString("mixPrimary","Rain");m.uri=p.getString("mixUri","");m.title=p.getString("mixTitle","My audio");
        m.binauralCarrier=p.getInt("mixBinauralCarrier",200);m.binauralBeat=p.getInt("mixBinauralBeat",6);
        m.primaryGain=p.getInt("mixPrimaryGain",70);m.brownGain=p.getInt("mixBrownGain",20);m.timerMinutes=p.getInt("mixMinutes",30);m.fadeMinutes=p.getInt("mixFade",5);m.withTracking=p.getBoolean("mixWithTracking",false);m.validate();return m;
    }
    static void save(Context c,SoundMix mix){save(factory.open(c),mix);}
    static synchronized long revision(SharedPreferences p){return p.getLong("mixRevision",0);}
    static synchronized boolean saveIfRevision(SharedPreferences p,long expected,SoundMix m){if(revision(p)!=expected)return false;save(p,m);return true;}
    static synchronized void save(SharedPreferences p,SoundMix m){
        m.validate();
        // Keep legacy consumers readable, while the new engine uses the entire atomic profile.
        p.edit().putLong("mixRevision",revision(p)+1).putInt("mixVersion",1).putString("mixPrimary",m.primary).putString("mixUri",m.uri).putString("mixTitle",m.title)
            .putInt("mixBinauralCarrier",m.binauralCarrier).putInt("mixBinauralBeat",m.binauralBeat).putInt("mixPrimaryGain",m.primaryGain).putInt("mixBrownGain",m.brownGain).putInt("mixMinutes",m.timerMinutes).putInt("mixFade",m.fadeMinutes).putBoolean("mixWithTracking",m.withTracking)
            .putString("lullaby",m.withTracking?m.label():"Off").putInt("lullabyMinutes",m.timerMinutes).apply();
    }
}
