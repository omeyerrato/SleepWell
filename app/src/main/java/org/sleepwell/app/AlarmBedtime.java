package org.sleepwell.app;

import org.json.*;
import java.util.*;

/** Per-alarm override for the next eligible wake-up used by the bedtime reminder scheduler. */
final class AlarmBedtime {
    static final AlarmBedtime FOLLOW=new AlarmBedtime("follow",0),OFF=new AlarmBedtime("off",0);
    final String mode;final int minutes;
    AlarmBedtime(String mode,int minutes){if(!Set.of("follow","off","custom").contains(mode)||mode.equals("custom")&&(minutes<15||minutes>1080)||!mode.equals("custom")&&minutes!=0)throw new IllegalArgumentException("Choose Follow global, Off, or a lead from 15 minutes to 18 hours.");this.mode=mode;this.minutes=minutes;}
    boolean custom(){return mode.equals("custom");}boolean off(){return mode.equals("off");}String key(){return mode+":"+minutes;}String title(){return custom()?PlanActivity.duration(minutes)+" before next alarm":off()?"Off for this alarm":"Follow global setting";}
    static JSONObject encode(AlarmBedtime value)throws JSONException{return new JSONObject().put("version",1).put("mode",value.mode).put("minutes",value.minutes);}
    static AlarmBedtime decode(Object raw)throws JSONException{if(!(raw instanceof JSONObject j)||j.length()!=3||SensorProvenance.whole(j,"version")!=1)throw new JSONException("Invalid alarm bedtime override");try{return new AlarmBedtime(SensorProvenance.text(j,"mode"),Math.toIntExact(SensorProvenance.whole(j,"minutes")));}catch(IllegalArgumentException|ArithmeticException invalid){throw new JSONException("Invalid alarm bedtime override");}}
    private AlarmBedtime() {throw new AssertionError();}
}
