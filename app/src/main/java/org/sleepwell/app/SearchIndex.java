package org.sleepwell.app;
import java.text.Normalizer;
import java.util.*;
/** Immutable local search; no network, database writes or fabricated entries. */
final class SearchIndex {
    enum Kind {ALARM,RECORD,SETTING}
    static final class Entry {
        final Kind kind;final long id;final String title,subtitle,category;private final String hay,titleKey;
        Entry(Kind kind,long id,String title,String subtitle,String terms){this(kind,id,title,subtitle,terms,"");}
        Entry(Kind kind,long id,String title,String subtitle,String terms,String category){if(kind==null||title==null||subtitle==null||terms==null||category==null)throw new IllegalArgumentException("Invalid search entry");this.kind=kind;this.id=id;this.title=title;this.subtitle=subtitle;this.category=category;titleKey=normalize(title);hay=normalize(title+" "+subtitle+" "+terms);}
    }
    static String normalize(String text){return Normalizer.normalize(text,Normalizer.Form.NFKD).replaceAll("\\p{M}+","").toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+"," ").trim().replaceAll("\\s+"," ");}
    static List<Entry> find(List<Entry> source,String query){if(query==null||query.length()>160)throw new IllegalArgumentException("Use up to 160 characters.");String clean=normalize(query);if(clean.isEmpty())return List.of();String[] words=clean.split(" ");List<Entry> hits=new ArrayList<>();for(Entry entry:source){boolean match=true;for(String word:words)if(!entry.hay.contains(word)){match=false;break;}if(match)hits.add(entry);}hits.sort(Comparator.comparingInt((Entry e)->e.titleKey.equals(clean)?0:e.titleKey.startsWith(clean)?1:2).thenComparingInt(e->e.kind.ordinal()));return List.copyOf(hits.subList(0,Math.min(100,hits.size())));}
    static List<Entry> findSettings(List<Entry> source,String query,String category){if(query==null||query.length()>160)throw new IllegalArgumentException("Use up to 160 characters.");List<Entry> settings=new ArrayList<>();for(Entry entry:source)if(entry.kind==Kind.SETTING&&(category.isEmpty()||category.equals(entry.category)))settings.add(entry);return normalize(query).isEmpty()?List.copyOf(settings.subList(0,Math.min(100,settings.size()))):find(settings,query);}
    private SearchIndex(){}
}
