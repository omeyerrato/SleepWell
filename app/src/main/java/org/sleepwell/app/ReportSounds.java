package org.sleepwell.app;

import android.content.Context;
import org.json.JSONObject;
import java.util.*;

/** Saved classifier intervals in the visible record window, never inferred from WAV names. */
final class ReportSounds {
    static final ReportSounds EMPTY=new ReportSounds(0,0,0,0,0,0);
    final int snoring,speech,other,total,analysisOnly,availableClips,userLabels;
    ReportSounds(int snoring,int speech,int other,int total,int analysisOnly,int availableClips){this(snoring,speech,other,total,analysisOnly,availableClips,0);}
    ReportSounds(int snoring,int speech,int other,int total,int analysisOnly,int availableClips,int userLabels){this.userLabels=userLabels;this.snoring=snoring;this.speech=speech;this.other=other;this.total=total;this.analysisOnly=analysisOnly;this.availableClips=availableClips;}
    static ReportSounds read(Context context,Store store,RecordEntry record){
        if(record.manual())return EMPTY;String id=""+record.id,start=""+record.edit.start,end=""+record.edit.end;
        // sound_events.time is START; clips.time is END. Arithmetic expressions need
        // explicit numeric parameters because rawQuery binds its arguments as text.
        List<JSONObject> events=store.rows("SELECT COALESCE(user_category,category) AS category,user_category,clip FROM sound_events WHERE session=? AND duration_ms>0 AND time+duration_ms>CAST(? AS INTEGER) AND time<CAST(? AS INTEGER)",id,start,end);
        int snoring=0,speech=0,other=0,analysisOnly=0,userLabels=0;
        for(JSONObject event:events){if(SoundLabels.manual(event))userLabels++;switch(event.optString("category")){case "snoring":snoring++;break;case "speech":speech++;break;default:other++;}if(event.isNull("clip"))analysisOnly++;}
        int available=0;for(JSONObject clip:store.rows("SELECT path FROM clips WHERE session=? AND duration_ms>0 AND time>CAST(? AS INTEGER) AND time-duration_ms<CAST(? AS INTEGER)",id,start,end))try{java.io.File file=FileSafety.clip(context,clip.optString("path"));if(file.isFile()&&file.length()>44)available++;}catch(java.io.IOException invalid){/* Missing or invalid audio is not advertised as saved audio. */}
        return new ReportSounds(snoring,speech,other,events.size(),analysisOnly,available,userLabels);
    }
}
