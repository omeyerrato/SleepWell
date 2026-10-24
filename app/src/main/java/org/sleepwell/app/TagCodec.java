package org.sleepwell.app;
import org.json.*;import java.util.*;
/** Backward compatible strings plus explicit typed quantity entries in the existing JSON column. */
final class TagCodec {
 static JSONArray encode(RecordDraft draft){JSONArray out=new JSONArray();for(String name:draft.tags){TagQuantity q=draft.quantities.get(name);if(q==null)out.put(name);else try{out.put(new JSONObject().put("name",name).put("quantity",q.value).put("unit",q.unit));}catch(JSONException e){throw new IllegalArgumentException(e);}}return out;}
 static void read(JSONArray values,RecordDraft draft)throws JSONException{if(values.length()>24)throw new JSONException("Too many tags");for(int n=0;n<values.length();n++){Object raw=values.get(n);if(raw instanceof String){draft.tags.add((String)raw);continue;}if(!(raw instanceof JSONObject))throw new JSONException("Invalid tag");JSONObject item=(JSONObject)raw;if(item.length()!=3||!(item.get("name") instanceof String)||!(item.get("unit") instanceof String)||!(item.get("quantity") instanceof Number))throw new JSONException("Invalid quantity tag");double value=((Number)item.get("quantity")).doubleValue();try{TagQuantity quantity=new TagQuantity(value,item.getString("unit"));String name=item.getString("name");if(!quantity.unit.equals(item.getString("unit"))||draft.quantities.put(name,quantity)!=null)throw new IllegalArgumentException("Invalid duplicate quantity");draft.tags.add(name);}catch(IllegalArgumentException e){throw new JSONException(e.getMessage());}}}
 static String display(RecordDraft draft,String tag){TagQuantity quantity=draft.quantities.get(tag);return quantity==null?tag:tag+" · "+quantity.display();}
 private TagCodec(){}
}
