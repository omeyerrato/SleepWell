package org.sleepwell.app;

import org.json.JSONObject;
import java.util.*;

/** Derived summaries of measured echo windows. No interpolation or physiological classification. */
final class EchoRhythm {
    static final class Minute {
        final long start,end;
        final Double rate,change;
        final double correlation;
        final String quality;
        Minute(long start,long end,Double rate,double correlation,String quality,Double change){
            this.start=start;this.end=end;this.rate=rate;this.correlation=correlation;this.quality=quality;this.change=change;
        }
        String reason(){return switch(quality){
            case "PERIODIC" -> "Repeating echo";
            case "AMBIGUOUS" -> "Conflicting periods";
            case "NO_STABLE_PERIOD" -> "No stable period";
            default -> "Insufficient signal";
        };}
    }
    final List<Minute> minutes;
    final int qualified,adjacentPairs,insufficient,unstable,ambiguous;
    final Double median,low,high,variability;

    EchoRhythm(List<JSONObject> rows){
        List<Minute> result=new ArrayList<>();List<Double> rates=new ArrayList<>();
        Minute previous=null;double sumSquares=0;int pairs=0,weak=0,unsteady=0,conflicting=0;
        for(JSONObject row:rows){
            long start=row.optLong("start"),end=row.optLong("time");
            String quality=row.optString("quality");double correlation=row.optDouble("periodicity",Double.NaN);
            Double rate=row.isNull("rate")?null:row.optDouble("rate",Double.NaN);
            if(start<=0||end-start!=EchoPeriodicity.WINDOW||!Double.isFinite(correlation)||correlation<0||correlation>1
                    ||!Set.of("PERIODIC","INSUFFICIENT_SIGNAL","NO_STABLE_PERIOD","AMBIGUOUS").contains(quality)
                    ||quality.equals("PERIODIC")!=(rate!=null)||rate!=null&&(!Double.isFinite(rate)||rate<5||rate>40)
                    ||previous!=null&&start<previous.end)throw new IllegalArgumentException("Invalid echo interval");
            Double change=rate!=null&&previous!=null&&previous.rate!=null&&previous.end==start?rate-previous.rate:null;
            Minute minute=new Minute(start,end,rate,correlation,quality,change);result.add(minute);previous=minute;
            if(rate!=null)rates.add(rate);
            if(change!=null){sumSquares+=change*change;pairs++;}
            switch(quality){case "INSUFFICIENT_SIGNAL" -> weak++;case "NO_STABLE_PERIOD" -> unsteady++;case "AMBIGUOUS" -> conflicting++;}
        }
        Collections.sort(rates);minutes=List.copyOf(result);qualified=rates.size();adjacentPairs=pairs;
        insufficient=weak;unstable=unsteady;ambiguous=conflicting;
        median=rates.isEmpty()?null:rates.size()%2==1?rates.get(rates.size()/2):(rates.get(rates.size()/2-1)+rates.get(rates.size()/2))/2;
        low=rates.isEmpty()?null:rates.get(0);high=rates.isEmpty()?null:rates.get(rates.size()-1);
        variability=pairs==0?null:Math.sqrt(sumSquares/pairs);
    }

    List<Minute> filtered(int filter){
        if(filter==0)return minutes;
        List<Minute> result=new ArrayList<>();for(Minute minute:minutes)if(filter==1?minute.rate!=null:minute.rate==null)result.add(minute);
        return List.copyOf(result);
    }
    Minute at(long start){for(Minute minute:minutes)if(minute.start==start)return minute;return null;}
}
