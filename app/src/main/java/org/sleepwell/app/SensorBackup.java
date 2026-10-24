package org.sleepwell.app;

import android.content.ContentValues;
import org.json.*;
import java.util.*;

/** Portable format-five provenance. All older-format behavior remains in Backup. */
final class SensorBackup {
 static void validateExport(JSONObject root)throws JSONException {Map<Long,JSONObject> sessions=new HashMap<>();JSONArray rows=root.getJSONArray("sessions");for(int n=0;n<rows.length();n++){JSONObject row=rows.getJSONObject(n);sessions.put(SensorProvenance.whole(row,"id"),row);}validate(root,sessions);}
 static void validate(JSONObject root,Map<Long,JSONObject> sessions)throws JSONException {
  try{SoundEventBackup.checkPortableSize(root);}catch(IllegalArgumentException tooLarge){throw new JSONException(tooLarge.getMessage());}
  if((SensorProvenance.whole(root,"format")<5||SensorProvenance.whole(root,"format")>6))throw new JSONException("Invalid sensor backup format");
  Map<Long,Long> recovered=new HashMap<>();for(Map.Entry<Long,JSONObject> entry:sessions.entrySet()){JSONObject session=entry.getValue();recovered.put(entry.getKey(),session.isNull("end")?SensorProvenance.whole(session,"start"):SensorProvenance.whole(session,"end"));}
  JSONArray rawEpochs=root.getJSONArray("epochs"),rawPauseRows=root.getJSONArray("pauses");for(int n=0;n<rawEpochs.length();n++){JSONObject row=rawEpochs.getJSONObject(n);long id=SensorProvenance.whole(row,"session");JSONObject session=sessions.get(id);if(session!=null&&session.isNull("end"))recovered.put(id,Math.max(recovered.get(id),SensorProvenance.whole(row,"time")));}for(int n=0;n<rawPauseRows.length();n++){JSONObject row=rawPauseRows.getJSONObject(n);long id=SensorProvenance.whole(row,"session");JSONObject session=sessions.get(id);if(session!=null&&session.isNull("end"))recovered.put(id,Math.max(recovered.get(id),SensorProvenance.whole(row,row.isNull("end")?"start":"end")));}
  for(JSONObject session:sessions.values()){
   SensorProvenance.whole(session,"id");SensorProvenance.whole(session,"start");if(!session.isNull("end"))SensorProvenance.whole(session,"end");if(!session.isNull("active_ms"))SensorProvenance.whole(session,"active_ms");
   String sensor=SensorProvenance.text(session,"sensor"),config=SensorProvenance.text(session,"sensor_config"),source=SensorProvenance.text(session,"source");
   try{SensorProvenance.validate(sensor,config);}catch(IllegalArgumentException invalid){throw new JSONException(invalid.getMessage());}
   if((sensor.equals(SensorProvenance.SONAR)||sensor.equals(SensorProvenance.MOTION))&&!source.equals("tracked")||sensor.equals(SensorProvenance.NONE)&&!source.equals("manual"))throw new JSONException("Sensor and record provenance disagree");
   if(sensor.equals(SensorProvenance.SONAR)){long start=SensorProvenance.whole(session,"start");if(!session.isNull("active_ms")){long active=SensorProvenance.whole(session,"active_ms");if(active<0||active>SensorProvenance.MAX_SESSION||active>recovered.get(SensorProvenance.whole(session,"id"))-start)throw new JSONException("Invalid contactless active time");}}
  }
  Set<Long> epochIds=new HashSet<>();Map<Long,List<JSONObject>> sonar=new HashMap<>();JSONArray epochs=root.getJSONArray("epochs");
  for(int n=0;n<epochs.length();n++){
   JSONObject epoch=epochs.getJSONObject(n);long id=SensorProvenance.whole(epoch,"id"),sessionId=SensorProvenance.whole(epoch,"session");if(id<=0||!epochIds.add(id)||!epoch.has("signal_quality"))throw new JSONException("Invalid sensor epoch identity");JSONObject session=sessions.get(sessionId);if(session==null)throw new JSONException("Missing sensor record");
   SensorProvenance.whole(epoch,"time");if(!epoch.isNull("start"))SensorProvenance.whole(epoch,"start");if(!epoch.isNull("samples"))SensorProvenance.whole(epoch,"samples");for(String key:List.of("movement","noise","light"))if(!epoch.isNull(key))SensorProvenance.number(epoch,key);
   if(!SensorProvenance.SONAR.equals(session.getString("sensor"))){if(!epoch.isNull("signal_quality"))throw new JSONException("Unexpected contactless quality on another sensor");continue;}
   long from=SensorProvenance.whole(epoch,"start"),to=SensorProvenance.whole(epoch,"time"),start=SensorProvenance.whole(session,"start");
   if(from<start||to<=from||to-from>5000||to-start>SensorProvenance.MAX_SESSION||!session.isNull("end")&&to>SensorProvenance.whole(session,"end")||!epoch.isNull("noise")||!epoch.isNull("light"))throw new JSONException("Invalid contactless interval");
   try{SensorProvenance.evidence(epoch.isNull("movement")?null:SensorProvenance.number(epoch,"movement"),SensorProvenance.text(epoch,"signal_quality"),Math.toIntExact(SensorProvenance.whole(epoch,"samples")));}catch(IllegalArgumentException|ArithmeticException invalid){throw new JSONException("Invalid contactless signal quality");}
   sonar.computeIfAbsent(sessionId,k->new ArrayList<>()).add(epoch);
  }
  Map<Long,List<JSONObject>> pauses=new HashMap<>();JSONArray rawPauses=root.getJSONArray("pauses");for(int n=0;n<rawPauses.length();n++){JSONObject row=rawPauses.getJSONObject(n);pauses.computeIfAbsent(SensorProvenance.whole(row,"session"),k->new ArrayList<>()).add(row);}
  for(Map.Entry<Long,List<JSONObject>> entry:sonar.entrySet()){
   long previous=sessions.get(entry.getKey()).getLong("start");List<JSONObject> measured=entry.getValue(),gaps=new ArrayList<>(pauses.getOrDefault(entry.getKey(),List.of()));measured.sort(Comparator.comparingLong(e->e.optLong("start")));gaps.sort(Comparator.comparingLong(p->p.optLong("start")));int pauseIndex=0;
   for(JSONObject epoch:measured){long from=epoch.getLong("start"),to=epoch.getLong("time");if(from<previous)throw new JSONException("Contactless intervals overlap");long covered=previous;while(pauseIndex<gaps.size()){JSONObject pause=gaps.get(pauseIndex);long a=pause.getLong("start"),b=pause.isNull("end")?Long.MAX_VALUE:pause.getLong("end");if(b>from)break;if(a<=covered&&b>covered)covered=b;pauseIndex++;}if(pauseIndex<gaps.size()){JSONObject pause=gaps.get(pauseIndex);if(pause.getLong("start")<to&&(pause.isNull("end")||pause.getLong("end")>from))throw new JSONException("Contactless interval crosses a pause");}if(from>previous&&covered<from)throw new JSONException("Contactless signal gap is missing");previous=to;}
  }
 }
 static void restoreEpoch(Store store,JSONObject epoch)throws JSONException {ContentValues values=new ContentValues();for(String key:List.of("id","session","time"))values.put(key,epoch.getLong(key));for(String key:List.of("start","samples")){if(epoch.isNull(key))values.putNull(key);else values.put(key,epoch.getLong(key));}for(String key:List.of("movement","noise","light")){if(epoch.isNull(key))values.putNull(key);else values.put(key,epoch.getDouble(key));}if(epoch.isNull("signal_quality"))values.putNull("signal_quality");else values.put("signal_quality",epoch.getString("signal_quality"));store.getWritableDatabase().insertOrThrow("epochs",null,values);}
 private SensorBackup(){}
}
