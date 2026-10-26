package org.sleepwell.app;
import android.content.Context;
import java.time.format.DateTimeFormatter;
import java.util.*;
final class SearchData {
    static List<SearchIndex.Entry> settings(){return SearchSettings.entries();}
    static List<SearchIndex.Entry> read(Context context){List<SearchIndex.Entry> rows=new ArrayList<>();try(Store store=Records.open(context)){
        for(Alarm a:store.alarms())rows.add(new SearchIndex.Entry(SearchIndex.Kind.ALARM,a.id,a.label.isBlank()?"Alarm":a.label,a.time()+" · "+(a.enabled?"On":"Off"),"alarm "+a.challenges().summary()+" "+a.days+" "+(a.date==null?"":a.date)+" "+a.tone));
        for(RecordEntry r:Records.all(store)){String date=r.day(0).format(DateTimeFormatter.ofPattern("EEE d MMM uuuu",Locale.getDefault()));StringBuilder terms=new StringBuilder(r.edit.note).append(' ').append(r.provenance()).append(' ').append(r.day(0));for(String tag:r.edit.tags){terms.append(' ').append(tag);TagQuantity quantity=r.edit.quantities.get(tag);if(quantity!=null)terms.append(' ').append(quantity.display());}rows.add(new SearchIndex.Entry(SearchIndex.Kind.RECORD,r.id,date,RecordUi.duration(r.duration())+" · "+r.provenance(),terms.toString()));}
    }rows.addAll(settings());return List.copyOf(rows);}
    private SearchData(){}
}
