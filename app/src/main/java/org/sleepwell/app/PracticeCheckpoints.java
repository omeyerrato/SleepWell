package org.sleepwell.app;

import android.content.SharedPreferences;
import android.os.*;
import java.util.*;

/** Bounded, memory-only checkpoint port for the same task controllers used by alarms. */
final class PracticeCheckpoints implements SharedPreferences {
    private final Map<String,Object> values=new LinkedHashMap<>();
    private final Set<OnSharedPreferenceChangeListener> listeners=Collections.newSetFromMap(new WeakHashMap<>());
    private final String run;
    private static final int MAX_KEYS=128,MAX_BYTES=65536;

    PracticeCheckpoints(String run,Bundle restored){
        this.run=run;
        if(restored==null)return;
        if(!run.equals(restored.getString("run")))throw new IllegalArgumentException("Preview checkpoints belong to another session.");
        Bundle entries=restored.getBundle("entries");
        if(entries==null||entries.size()>MAX_KEYS)throw new IllegalArgumentException("Preview checkpoints are unavailable.");
        for(String key:entries.keySet()){
            Bundle row=entries.getBundle(key);if(row==null)throw new IllegalArgumentException("Invalid preview checkpoint.");
            Object value=switch(row.getString("type","")){
                case "string"->row.getString("value");case "int"->row.getInt("value");case "long"->row.getLong("value");
                case "float"->row.getFloat("value");case "boolean"->row.getBoolean("value");
                case "set"->{ArrayList<String> items=row.getStringArrayList("value");if(items==null)throw new IllegalArgumentException("Invalid preview checkpoint set.");yield Set.copyOf(items);}
                default->throw new IllegalArgumentException("Unknown preview checkpoint type.");
            };
            if(value==null)throw new IllegalArgumentException("Missing preview checkpoint value.");values.put(key,value);
        }
        if(!bounded(values))throw new IllegalArgumentException("Preview checkpoints are too large.");
    }

    synchronized Bundle save(){
        Bundle state=new Bundle(),entries=new Bundle();state.putString("run",run);
        for(Map.Entry<String,Object> entry:values.entrySet()){
            Bundle row=new Bundle();Object value=entry.getValue();
            if(value instanceof String text){row.putString("type","string");row.putString("value",text);}
            else if(value instanceof Integer number){row.putString("type","int");row.putInt("value",number);}
            else if(value instanceof Long number){row.putString("type","long");row.putLong("value",number);}
            else if(value instanceof Float number){row.putString("type","float");row.putFloat("value",number);}
            else if(value instanceof Boolean flag){row.putString("type","boolean");row.putBoolean("value",flag);}
            else{row.putString("type","set");@SuppressWarnings("unchecked") Set<String> items=(Set<String>)value;row.putStringArrayList("value",new ArrayList<>(items));}
            entries.putBundle(entry.getKey(),row);
        }
        state.putBundle("entries",entries);return state;
    }

    private static Object copy(Object value){if(value instanceof Set<?> items)return Collections.unmodifiableSet(new HashSet<>(items));return value;}
    @Override public synchronized Map<String,?> getAll(){Map<String,Object> result=new LinkedHashMap<>();for(Map.Entry<String,Object> entry:values.entrySet())result.put(entry.getKey(),copy(entry.getValue()));return result;}
    @Override public synchronized String getString(String key,String fallback){return values.containsKey(key)?(String)values.get(key):fallback;}
    @Override @SuppressWarnings("unchecked") public synchronized Set<String> getStringSet(String key,Set<String> fallback){return values.containsKey(key)?(Set<String>)copy(values.get(key)):fallback;}
    @Override public synchronized int getInt(String key,int fallback){return values.containsKey(key)?(Integer)values.get(key):fallback;}
    @Override public synchronized long getLong(String key,long fallback){return values.containsKey(key)?(Long)values.get(key):fallback;}
    @Override public synchronized float getFloat(String key,float fallback){return values.containsKey(key)?(Float)values.get(key):fallback;}
    @Override public synchronized boolean getBoolean(String key,boolean fallback){return values.containsKey(key)?(Boolean)values.get(key):fallback;}
    @Override public synchronized boolean contains(String key){return values.containsKey(key);}
    @Override public Editor edit(){return new Edit();}
    @Override public synchronized void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener){listeners.add(Objects.requireNonNull(listener));}
    @Override public synchronized void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener){listeners.remove(listener);}

    private static boolean bounded(Map<String,Object> entries){
        if(entries.size()>MAX_KEYS)return false;long bytes=0;
        for(Map.Entry<String,Object> entry:entries.entrySet()){
            String key=entry.getKey();if(key==null||key.length()>256)return false;bytes+=key.length()*2L+32;
            Object value=entry.getValue();
            if(value instanceof String text)bytes+=text.length()*2L;
            else if(value instanceof Set<?> items){if(items.size()>128)return false;for(Object item:items){if(!(item instanceof String text))return false;bytes+=text.length()*2L+16;}}
            else if(!(value instanceof Integer||value instanceof Long||value instanceof Float||value instanceof Boolean))return false;
            if(bytes>MAX_BYTES)return false;
        }return true;
    }

    private final class Edit implements Editor {
        final Map<String,Object> updates=new LinkedHashMap<>();boolean clear;
        private Editor put(String key,Object value){updates.put(Objects.requireNonNull(key),copy(value));return this;}
        @Override public Editor putString(String key,String value){return put(key,value);}
        @Override public Editor putStringSet(String key,Set<String> value){return put(key,value==null?null:Set.copyOf(value));}
        @Override public Editor putInt(String key,int value){return put(key,value);}
        @Override public Editor putLong(String key,long value){return put(key,value);}
        @Override public Editor putFloat(String key,float value){return put(key,value);}
        @Override public Editor putBoolean(String key,boolean value){return put(key,value);}
        @Override public Editor remove(String key){return put(key,null);}
        @Override public Editor clear(){clear=true;return this;}
        @Override public boolean commit(){
            List<String> changed;List<OnSharedPreferenceChangeListener> observers;
            synchronized(PracticeCheckpoints.this){
                Map<String,Object> next=clear?new LinkedHashMap<>():new LinkedHashMap<>(values);
                for(Map.Entry<String,Object> update:updates.entrySet())if(update.getValue()==null)next.remove(update.getKey());else next.put(update.getKey(),copy(update.getValue()));
                if(!bounded(next))return false;
                Set<String> keys=new LinkedHashSet<>(values.keySet());keys.addAll(next.keySet());changed=new ArrayList<>();for(String key:keys)if(!Objects.equals(values.get(key),next.get(key)))changed.add(key);
                values.clear();values.putAll(next);observers=new ArrayList<>(listeners);
            }
            Runnable notify=()->{for(String key:changed)for(OnSharedPreferenceChangeListener observer:observers)if(observer!=null)observer.onSharedPreferenceChanged(PracticeCheckpoints.this,key);};
            if(Looper.myLooper()==Looper.getMainLooper())notify.run();else new Handler(Looper.getMainLooper()).post(notify);
            return true;
        }
        @Override public void apply(){if(!commit())throw new IllegalStateException("Preview checkpoint limit reached. Return and start a new preview.");}
    }
}
