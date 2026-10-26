package org.sleepwell.app;

import android.content.*;
import org.json.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

/** Only optional Tonight content is configurable; all alarm/run controls remain mandatory. */
final class TonightLayout {
 static final String BEDTIME="bedtime",WIND_DOWN="wind_down",GOAL_BANK="goal_bank",LATEST_NIGHT="latest_night";
 static final List<String> ROWS=List.of(BEDTIME,WIND_DOWN,GOAL_BANK,LATEST_NIGHT);
 static final String FILE="tonight_layout_v1",KEY="layout";
 static final class Config {
  final List<String> order;final Set<String> visible;final boolean illustration;final long revision;final String fingerprint,issue;
  Config(List<String> order,Set<String> visible,boolean illustration,long revision,String fingerprint,String issue){this.order=List.copyOf(order);this.visible=Set.copyOf(visible);this.illustration=illustration;this.revision=revision;this.fingerprint=fingerprint;this.issue=issue;}
  List<String> shown(){List<String> out=new ArrayList<>();for(String id:order)if(visible.contains(id))out.add(id);return out;}
 }
 static String title(String id){return switch(id){case BEDTIME->"Bedtime goal";case WIND_DOWN->"Wind-down";case GOAL_BANK->"Sleep goal bank";case LATEST_NIGHT->"Latest saved night";default->"Unknown row";};}
 static String detail(String id){return switch(id){case BEDTIME->"Your saved planned bedtime";case WIND_DOWN->"Your saved wind-down time";case GOAL_BANK->"Recorded balance against a dated duration goal";case LATEST_NIGHT->"Latest non-nap saved period and its report";default->"";};}
 static Config defaults(){return new Config(ROWS,Set.of(BEDTIME,WIND_DOWN),true,0,"","");}
 private static SharedPreferences prefs(Context c){return c.getApplicationContext().getSharedPreferences(FILE,Context.MODE_PRIVATE);}
 private static String fingerprint(Object raw){return digest(raw==null?"absent":raw.getClass().getName()+":"+raw);}
 static Config load(Context c){SharedPreferences p=prefs(c);Object raw=p.getAll().get(KEY);String fingerprint=fingerprint(raw);if(raw==null){Config d=defaults();return new Config(d.order,d.visible,true,0,fingerprint,"");}try{
  if(!(raw instanceof String text)||text.length()>8192)throw new IllegalArgumentException();JSONObject value=new JSONObject(text);Object version=value.get("version"),revision=value.get("revision"),art=value.get("illustration");if(!(version instanceof Integer||version instanceof Long)||((Number)version).longValue()!=1||!(revision instanceof Integer||revision instanceof Long)||((Number)revision).longValue()<0||!(art instanceof Boolean))throw new IllegalArgumentException();
  JSONArray order=value.getJSONArray("order"),visible=value.getJSONArray("visible");if(order.length()>16||visible.length()>16)throw new IllegalArgumentException();List<String> normalized=new ArrayList<>();Set<String> shown=new LinkedHashSet<>();boolean repaired=false;
  for(int n=0;n<order.length();n++){Object id=order.get(n);if(!(id instanceof String))throw new IllegalArgumentException();if(ROWS.contains(id)&&!normalized.contains(id))normalized.add((String)id);else repaired=true;}for(String id:ROWS)if(!normalized.contains(id))normalized.add(id);
  for(int n=0;n<visible.length();n++){Object id=visible.get(n);if(!(id instanceof String))throw new IllegalArgumentException();if(ROWS.contains(id)){if(!shown.add((String)id))repaired=true;}else repaired=true;}
  return new Config(normalized,shown,(Boolean)art,((Number)revision).longValue(),fingerprint,repaired?"Unsupported or duplicate rows were removed from this draft. Save to keep this layout.":"");
 }catch(JSONException|RuntimeException invalid){Config d=defaults();return new Config(d.order,d.visible,true,0,fingerprint,"Saved layout needs review. The default layout is being used; Save explicitly to replace it.");}}
 static synchronized void save(Context c,List<String> order,Set<String> visible,boolean illustration,String expected,String request){if(request==null||request.isEmpty())throw new IllegalArgumentException("Missing layout save request.");write(c,order,visible,illustration,expected,request);}
 /** A portable profile changes only visual configuration; operation receipts are never imported. */
 static synchronized void savePortable(Context c,Config config,String expected){write(c,config.order,config.visible,config.illustration,expected,null);}
 private static void write(Context c,List<String> order,Set<String> visible,boolean illustration,String expected,String request){SharedPreferences p=prefs(c);if(request!=null&&request.equals(p.getString("lastRequest","")))return;Config previous=load(c);if(!previous.fingerprint.equals(expected))throw new IllegalStateException("Tonight layout changed while this screen was open. Reopen before saving.");if(previous.revision==Long.MAX_VALUE)throw new IllegalStateException("Layout revision is unavailable.");if(order.size()!=ROWS.size()||new HashSet<>(order).size()!=ROWS.size()||!order.containsAll(ROWS)||!ROWS.containsAll(visible))throw new IllegalArgumentException("Choose only the supported optional rows.");try{JSONArray ordered=new JSONArray(),shown=new JSONArray();for(String id:order){ordered.put(id);if(visible.contains(id))shown.put(id);}String value=new JSONObject().put("version",1).put("revision",previous.revision+1).put("order",ordered).put("visible",shown).put("illustration",illustration).toString();SharedPreferences.Editor edit=p.edit().putString(KEY,value);if(request!=null)edit.putString("lastRequest",request);if(!edit.commit())throw new IllegalStateException("Tonight layout could not be saved.");}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
 private static String digest(String value){try{StringBuilder out=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)))out.append(String.format(Locale.ROOT,"%02x",b&255));return out.toString();}catch(NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}}
 private TonightLayout(){}
}