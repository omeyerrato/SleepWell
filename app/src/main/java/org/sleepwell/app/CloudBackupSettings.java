package org.sleepwell.app;

import android.content.*;
import org.json.*;
import java.util.*;

final class CloudBackupSettings {
    interface Factory {SharedPreferences preferences(Context context);}
    static Factory factory=c->c.getSharedPreferences("cloud_backup_tree_v1",Context.MODE_PRIVATE);
    static final class Destination {final String tree,label;final int owned;final long revision;Destination(String tree,String label,int owned,long revision){this.tree=tree;this.label=label;this.owned=owned;this.revision=revision;}}
    static final class Entry {final String tree,uri,name,sha;final long time,size;final boolean verified;Entry(String tree,String uri,String name,long time,long size,String sha,boolean verified){this.tree=tree;this.uri=uri;this.name=name;this.time=time;this.size=size;this.sha=sha;this.verified=verified;}}
    static Destination destination(SharedPreferences prefs){return new Destination(prefs.getString("tree",""),prefs.getString("label","No destination"),prefs.getInt("owned",0),prefs.getLong("revision",0));}
    static synchronized Destination remember(SharedPreferences prefs,String tree,String label,int owned){Destination old=destination(prefs);if(!prefs.edit().putString("tree",tree).putString("label",label).putInt("owned",owned).putLong("revision",old.revision+1).commit())throw new IllegalStateException("Destination could not be saved.");return old;}
    static synchronized Destination disconnect(SharedPreferences prefs){Destination old=destination(prefs);if(!prefs.edit().remove("tree").remove("label").remove("owned").putLong("revision",old.revision+1).commit())throw new IllegalStateException("Destination could not be disconnected.");return old;}
    static boolean current(SharedPreferences prefs,Destination destination){Destination now=destination(prefs);return now.revision==destination.revision&&now.tree.equals(destination.tree);}
    static List<Entry> history(SharedPreferences prefs)throws JSONException{JSONArray rows=new JSONArray(prefs.getString("history","[]"));List<Entry> entries=new ArrayList<>();if(rows.length()>20)throw new JSONException("History too large");for(int n=0;n<rows.length();n++){JSONObject row=rows.getJSONObject(n);entries.add(new Entry(row.getString("tree"),row.getString("uri"),row.getString("name"),row.getLong("time"),row.getLong("size"),row.getString("sha"),row.getBoolean("verified")));}return entries;}
    static synchronized void record(SharedPreferences prefs,Entry entry)throws JSONException{List<Entry> previous=history(prefs);JSONArray rows=new JSONArray();rows.put(json(entry));for(Entry old:previous)if(!old.uri.equals(entry.uri)&&rows.length()<20)rows.put(json(old));if(!prefs.edit().putString("history",rows.toString()).commit())throw new IllegalStateException("Backup history could not be saved.");}
    private static JSONObject json(Entry entry)throws JSONException{return new JSONObject().put("tree",entry.tree).put("uri",entry.uri).put("name",entry.name).put("time",entry.time).put("size",entry.size).put("sha",entry.sha).put("verified",entry.verified);}
}
