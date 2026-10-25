package org.sleepwell.app;

/** Behavior template only. Identity, time, weekdays and pending occurrence authority never transfer. */
final class AlarmDefaults {
    private final Alarm value;
    AlarmDefaults(Alarm source) {
        if(source==null||source.repeatInvalid||source.snoozeTotalInvalid||source.solarInvalid||source.routineInvalid||source.soundModeInvalid||source.timingInvalid||source.inputsInvalid||source.napInvalid||source.nap!=null)throw new IllegalArgumentException("Choose valid ordinary alarm settings first.");
        if(source.volume<10||source.volume>100||source.smartWindow<0||source.smartWindow>60||source.rampSeconds<0||source.rampSeconds>1440||source.soundDelaySeconds<0||source.soundDelaySeconds>1440||source.vibrationDelaySeconds<0||source.vibrationDelaySeconds>1440||source.snoozeMinutes<1||source.snoozeMinutes>60||source.snoozeLimit<0||source.snoozeLimit>5||source.snoozeTotalMinutes<0||source.snoozeTotalMinutes>300||source.awakeCheck<0||source.awakeCheck>15||source.backupMinutes<0||source.backupMinutes>15)throw new IllegalArgumentException("Alarm settings are outside supported ranges.");
        if(source.tone==null||!(source.tone.equals("daybreak")||source.tone.equals("pulse")||source.tone.equals("chime")||source.tone.equals("system")||source.tone.startsWith("content://")))throw new IllegalArgumentException("Choose a supported alarm sound.");
        value=copy(source,0);
    }
    Alarm create(int id){if(id<=0)throw new IllegalArgumentException("Alarm identity must be positive");return copy(value,id);}
    private static Alarm copy(Alarm source,int id){Alarm target=new Alarm(id);target.smartWindow=source.smartWindow;target.volume=source.volume;target.rampSeconds=source.rampSeconds;target.snoozeMinutes=source.snoozeMinutes;target.snoozeLimit=source.snoozeLimit;target.snoozeTotalMinutes=source.snoozeTotalMinutes;target.awakeCheck=source.awakeCheck;target.backupMinutes=source.backupMinutes;target.vibrate=source.vibrate;target.pin=source.pin;target.extraLoud=source.extraLoud;target.sunrise=source.sunrise;target.flashlight=source.flashlight;target.shortenSnoozes=source.shortenSnoozes;target.soundDelaySeconds=source.soundDelaySeconds;target.vibrationDelaySeconds=source.vibrationDelaySeconds;target.tone=source.tone;target.toneDisplayName=source.toneDisplayName;target.setRoutine(source.challenges());target.soundMode=source.soundMode;target.inputs=source.inputs;target.timing=source.timing;return target;}
    boolean references(String uri){return uri.equals(value.tone)||value.soundMode.references(uri);}
    String tone(){return value.tone;}
}
