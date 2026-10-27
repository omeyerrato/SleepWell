package org.sleepwell.app;

/** Shared monotonic envelope for real alarm output and its settings chart. No Android hardware calls. */
final class AlarmWakePolicy {
    final int targetPercent,backupPercent;final long rampMillis,delayMillis,sunriseMillis,vibrationDelayMillis;final boolean sunrise,flashlight,quiet,vibration,extraLoud;
    AlarmWakePolicy(Alarm alarm){
        quiet=alarm.id<=0;extraLoud=!quiet&&alarm.extraLoud;targetPercent=quiet?Math.min(25,Math.max(10,alarm.volume)):alarm.extraLoud?100:Math.max(10,Math.min(100,alarm.volume));
        backupPercent=quiet?targetPercent:alarm.backupSoundInvalid||alarm.backupSound==null?100:alarm.backupSound.volume;
        rampMillis=quiet?0:Math.max(0,Math.min(24*60,alarm.rampSeconds))*1000L;
        delayMillis=quiet?0:Math.max(0,Math.min(24*60,alarm.soundDelaySeconds))*1000L;
        vibration=!quiet&&alarm.vibrate;vibrationDelayMillis=quiet?0:Math.max(0,Math.min(24*60,alarm.vibrationDelaySeconds))*1000L;
        sunrise=!quiet&&alarm.sunrise;flashlight=!quiet&&alarm.flashlight;
        sunriseMillis=delayMillis>0?delayMillis:rampMillis>0?rampMillis:120000;
    }
    double gain(long elapsed,boolean backup){if(backup&&!quiet)return 1;if(elapsed<delayMillis)return 0;if(rampMillis==0)return 1;double fraction=Math.max(0,Math.min(1,(elapsed-delayMillis)/(double)rampMillis));return fraction*fraction;}
    double light(long elapsed){return Math.max(0,Math.min(1,elapsed/(double)sunriseMillis));}
    boolean audible(long elapsed,boolean backup){return backup&&!quiet||elapsed>=delayMillis;}
    int percent(boolean backup){return backup&&!quiet?backupPercent:targetPercent;}
    boolean boost(boolean backup){return extraLoud&&!quiet&&(!backup||backupPercent==100);}
    int streamLevel(int maximum,boolean backup){if(maximum<=0)return 0;double level=maximum*percent(backup)/100d;return Math.min(maximum,quiet?(int)Math.floor(level):Math.max(1,(int)Math.round(level)));}
}
