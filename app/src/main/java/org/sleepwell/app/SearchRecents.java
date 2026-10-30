package org.sleepwell.app;

import android.content.Context;
import org.json.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/** Private quick-access order. Only known setting metadata is stored, never a search or alarm ID. */
final class SearchRecents {
    private static final String FILE="settings_search_recents_v1",KEY="recent_settings";
    private static final int LIMIT=5;
    private static final ExecutorService STORAGE=Executors.newSingleThreadExecutor(task->new Thread(task,"SleepWell setting shortcuts"));
    static final class Snapshot {
        final List<SearchIndex.Entry> entries;final String problem;
        Snapshot(List<SearchIndex.Entry> entries,String problem){this.entries=List.copyOf(entries);this.problem=problem;}
    }
    static final class Result {
        final Snapshot snapshot;final boolean saved;
        Result(Snapshot snapshot,boolean saved){this.snapshot=snapshot;this.saved=saved;}
    }
    /** Called by SearchActivity's existing reader, so preferences never load on the UI thread. */
    static Snapshot load(Context context,List<SearchIndex.Entry> known){
        Context app=context.getApplicationContext();
        try{return STORAGE.submit(()->read(app,known)).get();}
        catch(InterruptedException interrupted){Thread.currentThread().interrupt();return unavailable();}
        catch(ExecutionException|RuntimeException unavailable){return unavailable();}
    }
    private static Snapshot unavailable(){return new Snapshot(List.of(),"Recent shortcuts are unavailable. Default shortcuts still work.");}
    private static Snapshot read(Context app,List<SearchIndex.Entry> known){
        try{
            String encoded=app.getSharedPreferences(FILE,Context.MODE_PRIVATE).getString(KEY,"[]");if(encoded==null||encoded.length()>8192)return unavailable();JSONArray stored=new JSONArray(encoded);List<SearchIndex.Entry> entries=new ArrayList<>();Set<Long> seen=new HashSet<>();
            for(int n=0;n<Math.min(stored.length(),LIMIT);n++){JSONObject value=stored.optJSONObject(n);if(value==null)continue;long id=value.optLong("id",-1);SearchIndex.Entry match=known(known,id,value.optString("title",""));if(match!=null&&seen.add(id))entries.add(match);}
            return new Snapshot(entries,"");
        }catch(JSONException|RuntimeException unavailable){return unavailable();}
    }
    private static SearchIndex.Entry known(List<SearchIndex.Entry> entries,long id,String title){for(SearchIndex.Entry entry:entries)if(entry.kind==SearchIndex.Kind.SETTING&&entry.id==id&&entry.title.equals(title))return entry;return null;}
    static List<SearchIndex.Entry> promote(List<SearchIndex.Entry> previous,SearchIndex.Entry entry){
        if(entry.kind!=SearchIndex.Kind.SETTING)return List.copyOf(previous);List<SearchIndex.Entry> rows=new ArrayList<>();rows.add(entry);for(SearchIndex.Entry older:previous)if(rows.size()<LIMIT&&older.kind==SearchIndex.Kind.SETTING&&older.id!=entry.id)rows.add(older);return List.copyOf(rows);
    }
    /** Serial writes finish independently of the Activity's reader cancellation or destination opening. */
    static void remember(Context context,SearchIndex.Entry entry,List<SearchIndex.Entry> known,Consumer<Result> done){
        Context app=context.getApplicationContext();STORAGE.execute(()->{
            Snapshot before=read(app,known);SearchIndex.Entry setting=entry.kind==SearchIndex.Kind.SETTING?known(known,entry.id,entry.title):null;
            if(setting==null){done.accept(new Result(before,false));return;}List<SearchIndex.Entry> order=promote(before.entries,setting);boolean saved=false;
            try{JSONArray rows=new JSONArray();for(SearchIndex.Entry value:order)rows.put(new JSONObject().put("id",value.id).put("title",value.title));saved=app.getSharedPreferences(FILE,Context.MODE_PRIVATE).edit().putString(KEY,rows.toString()).commit();}catch(JSONException|RuntimeException unavailable){/* Optional order storage never blocks real navigation. */}
            done.accept(new Result(new Snapshot(saved?order:before.entries,saved?"":"Shortcut order could not be saved. Settings still open normally."),saved));
        });
    }
    static void reset(Context context,List<SearchIndex.Entry> known,Consumer<Result> done){
        Context app=context.getApplicationContext();STORAGE.execute(()->{boolean saved=false;try{saved=app.getSharedPreferences(FILE,Context.MODE_PRIVATE).edit().remove(KEY).commit();}catch(RuntimeException unavailable){/* Keep routes usable. */}Snapshot current=read(app,known);done.accept(new Result(new Snapshot(current.entries,saved?"":"Shortcuts could not be reset. Try again."),saved));});
    }
    static List<SearchSettings.Shortcut> quickAccess(List<SearchIndex.Entry> recent,List<SearchSettings.Shortcut> defaults){
        LinkedHashMap<Long,SearchSettings.Shortcut> slots=new LinkedHashMap<>();for(SearchIndex.Entry entry:recent){if(slots.size()==LIMIT)break;if(entry.kind!=SearchIndex.Kind.SETTING)continue;SearchSettings.Shortcut shortcut=null;for(SearchSettings.Shortcut candidate:defaults)if(candidate.entry.id==entry.id){shortcut=candidate;break;}if(shortcut==null)shortcut=new SearchSettings.Shortcut(entry,entry.title,SearchSettings.icon(entry));slots.putIfAbsent(entry.id,shortcut);}for(SearchSettings.Shortcut shortcut:defaults){if(slots.size()==LIMIT)break;slots.putIfAbsent(shortcut.entry.id,shortcut);}return List.copyOf(slots.values());
    }
    private SearchRecents(){}
}
