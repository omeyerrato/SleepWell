package org.sleepwell.app;

import android.content.*;
import org.json.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

final class HomeWidgetData {
    static HomeWidgetSummary load(Context context,Records.Factory factory,SharedPreferences prefs,long now,ZoneId zone,boolean alarmAccess){
        String title="No alarm enabled",time="Add an alarm",detail="Open alarms to set your next wake-up",report="No sleep records",duration="View history",date="Record a night or add a sleep entry";boolean attention=false;long id=0;
        try(Store store=factory.open(context)){try{List<JSONObject> rows=store.rows("SELECT data FROM alarms ORDER BY id LIMIT 129");if(rows.size()>128)throw new IllegalStateException("Alarm limit");long nearest=Long.MAX_VALUE;boolean enabled=false,invalid=false;for(JSONObject row:rows){Alarm alarm=Alarm.read(new JSONObject(row.getString("data")));if(!alarm.enabled)continue;enabled=true;if(alarm.napInvalid||alarm.skipInvalid||alarm.routineInvalid||alarm.dateInvalid){invalid=true;continue;}long due=prefs.getLong("deadline_"+alarm.id,0);if(alarm.nap!=null){if(alarm.nap.phase!=NapPlan.Phase.PENDING||!alarm.nap.error.isEmpty())continue;String device=prefs.getString("napDeviceId","");long elapsed=android.os.SystemClock.elapsedRealtime(),boot=android.provider.Settings.Global.getInt(context.getContentResolver(),android.provider.Settings.Global.BOOT_COUNT,0);NapPlan.Clock clock=new NapPlan.Clock(){public long wall(){return now;}public long elapsed(){return elapsed;}public long boot(){return boot;}public String device(){return device;}};due=alarm.nap.projectedDeadline(clock);}if(due>now&&due<nearest)nearest=due;}
            if(prefs.getBoolean("ringing",false)){title="Alarm active";time="Open alarms";detail="Complete the current wake-up in SleepWell";}
            else if(enabled&&!alarmAccess){title="Alarm access needed";time="Check reliability";detail="Saved alarms are kept";attention=true;}
            else if(nearest<Long.MAX_VALUE){title="Next saved alarm";time=RecordUi.clock(context,nearest,zone);detail=Instant.ofEpochMilli(nearest).atZone(zone).format(DateTimeFormatter.ofPattern("EEE d MMM",Locale.getDefault()));if(invalid)detail+=" · another alarm needs review";}
            else if(enabled){title="Alarm needs review";time="Open alarms";detail="No future saved deadline is available";attention=false;}
        }catch(Exception failure){title="Alarms unavailable";time="Open alarms";detail="Refresh or check saved alarms";attention=false;}
        try{List<JSONObject> newest=store.rows("SELECT s.id FROM sessions s LEFT JOIN record_edits e ON e.session=s.id WHERE s.end IS NOT NULL ORDER BY CASE WHEN e.reverted=0 THEN e.start ELSE s.start END DESC LIMIT 1");if(!newest.isEmpty()){long candidate=newest.get(0).getLong("id");List<JSONObject> pauses=store.rows("SELECT COUNT(*) AS n FROM tracking_pauses WHERE session=?",""+candidate);if(pauses.get(0).getLong("n")>2048)throw new IllegalStateException("Record too large");RecordEntry entry=Records.get(store,candidate);id=entry.id;report="Last sleep record";duration=HomeWidgetSummary.duration(entry.duration());date=Instant.ofEpochMilli(entry.edit.end).atZone(entry.zone()).format(DateTimeFormatter.ofPattern("EEE d MMM",Locale.getDefault()))+(entry.interrupted!=0?" · interrupted":entry.manual()?" · manual":"");}}
        catch(Exception failure){report="Report unavailable";duration="Open history";date="Refresh or check saved records";id=0;}
        }catch(Exception failure){return HomeWidgetSummary.error();}return new HomeWidgetSummary(title,time,detail,attention,report,duration,date,id);
    }
}
