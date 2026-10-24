package org.sleepwell.app;

import org.json.*;
import java.util.*;

/** Strict portable evidence codec. Empty means unknown for pre-existing or uninstrumented recordings. */
final class SoundAnalysisProvenance {
    static String encode(SoundAnalysisStatus status){try{return new JSONObject().put("version",1).put("requested",status.requested).put("state",status.state).put("frames",status.frames).put("issue",status.issue).toString();}catch(JSONException impossible){throw new IllegalArgumentException("Invalid sound analysis evidence",impossible);}}
    static SoundAnalysisStatus read(String encoded){
        if(encoded==null)throw new IllegalArgumentException("Missing sound analysis provenance");if(encoded.isEmpty())return null;
        if(encoded.length()>512)throw new IllegalArgumentException("Sound analysis evidence is too large");
        try{JSONObject row=new JSONObject(encoded);Set<String> fields=Set.of("version","requested","state","frames","issue");if(row.length()!=fields.size())throw new JSONException("Invalid sound analysis fields");for(Iterator<String> it=row.keys();it.hasNext();)if(!fields.contains(it.next()))throw new JSONException("Unknown sound analysis field");
            if(SensorProvenance.whole(row,"version")!=1||!(row.get("requested") instanceof Boolean))throw new JSONException("Invalid sound analysis version or consent");
            return new SoundAnalysisStatus((Boolean)row.get("requested"),SensorProvenance.text(row,"state"),SensorProvenance.whole(row,"frames"),SensorProvenance.text(row,"issue"));
        }catch(JSONException invalid){throw new IllegalArgumentException("Invalid sound analysis provenance",invalid);}
    }
    static String recovered(String encoded){SoundAnalysisStatus status=read(encoded);return status==null?"":encode(status.interrupted());}
    static void session(JSONObject session)throws JSONException {
        String encoded=SensorProvenance.text(session,"sound_analysis");SoundAnalysisStatus status;
        try{status=read(encoded);}catch(IllegalArgumentException invalid){throw new JSONException(invalid.getMessage());}
        if(status==null)return;
        if(!SensorProvenance.SONAR.equals(SensorProvenance.text(session,"sensor"))||!"tracked".equals(SensorProvenance.text(session,"source")))throw new JSONException("Unexpected shared sound analysis provenance");
        if(!session.isNull("end")&&!status.terminal())throw new JSONException("Closed recording has unfinished sound analysis");
        if(status.frames>0&&(session.isNull("active_ms")||status.frames*975>SensorProvenance.whole(session,"active_ms")))throw new JSONException("Sound analysis exceeds captured time");
    }
    static void validateExport(JSONObject root)throws JSONException{JSONArray rows=root.getJSONArray("sessions");for(int n=0;n<rows.length();n++)session(rows.getJSONObject(n));}
    private SoundAnalysisProvenance(){}
}
