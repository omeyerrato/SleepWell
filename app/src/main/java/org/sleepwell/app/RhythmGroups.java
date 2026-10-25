package org.sleepwell.app;

import java.time.*;
import java.util.*;

/** Descriptive circular grouping of actual recorded period midpoints. Unknown dates add no points. */
final class RhythmGroups {
    static final int DAYS=90,MAX_NIGHTS=30;
    static final class Group {
        final List<RhythmStats.Night> nights;final double midpoint,spread;final long duration;
        Group(List<RhythmStats.Night> input){nights=List.copyOf(input);List<Double> values=new ArrayList<>();long total=0;for(RhythmStats.Night n:input){values.add(n.midpoint);total+=n.record.duration();}Double mean=RhythmStats.mean(values);midpoint=mean==null?values.get(0):mean;double sum=0;for(double v:values)sum+=RhythmStats.distance(v,midpoint);spread=sum/values.size();duration=total/values.size();}
    }
    final List<Group> groups;final List<RhythmStats.Night> nights;final int excluded,manual,missing;final LocalDate first,last;final ZoneId zone;
    RhythmGroups(List<RhythmStats.Night> input,long now,ZoneId zone){
        this.zone=zone;last=Instant.ofEpochMilli(now).atZone(zone).toLocalDate();first=last.minusDays(DAYS-1);List<RhythmStats.Night> selected=new ArrayList<>();int skipped=0,reported=0;
        for(RhythmStats.Night n:input){if(n.day.isBefore(first)||n.day.isAfter(last))continue;if(n.record.interrupted!=0||n.record.clockChanged!=0||!knownZone(n.record.zone,zone)||!Double.isFinite(n.midpoint)){skipped++;continue;}selected.add(n);}
        selected.sort(Comparator.comparingLong(n->n.end));if(selected.size()>MAX_NIGHTS)selected=new ArrayList<>(selected.subList(selected.size()-MAX_NIGHTS,selected.size()));for(RhythmStats.Night n:selected)if(n.record.manual())reported++;
        nights=List.copyOf(selected);excluded=skipped;manual=reported;Set<LocalDate> dates=new HashSet<>();for(RhythmStats.Night n:selected)dates.add(n.day);missing=DAYS-dates.size();groups=List.copyOf(cluster(selected));
    }
    private static List<Group> cluster(List<RhythmStats.Night> nights){
        if(nights.isEmpty())return List.of();List<Group> best=List.of(new Group(nights));if(nights.size()<6)return best;
        double oldError=error(best);
        // Farthest-point starts are deterministic; a new group needs three nights and a
        // full hour reduction in mean within-group distance, rather than always splitting.
        for(int k=2;k<=3&&nights.size()>=3*k;k++){
            List<Group> candidate=null;double candidateError=Double.POSITIVE_INFINITY;
            for(int start=0;start<nights.size();start++){
                double[] centers=new double[k];centers[0]=nights.get(start).midpoint;
                for(int j=1;j<k;j++){double far=-1;for(RhythmStats.Night n:nights){double near=1440;for(int a=0;a<j;a++)near=Math.min(near,RhythmStats.distance(n.midpoint,centers[a]));if(near>far){far=near;centers[j]=n.midpoint;}}}
                List<List<RhythmStats.Night>> buckets=null;boolean valid=true;
                for(int iteration=0;iteration<48;iteration++){
                    buckets=new ArrayList<>();for(int j=0;j<k;j++)buckets.add(new ArrayList<>());
                    for(RhythmStats.Night n:nights){int nearest=0;for(int j=1;j<k;j++)if(RhythmStats.distance(n.midpoint,centers[j])<RhythmStats.distance(n.midpoint,centers[nearest]))nearest=j;buckets.get(nearest).add(n);}
                    boolean moved=false;for(int j=0;j<k;j++){List<RhythmStats.Night> bucket=buckets.get(j);if(bucket.size()<3){valid=false;break;}List<Double> values=new ArrayList<>();for(RhythmStats.Night n:bucket)values.add(n.midpoint);Double mean=RhythmStats.mean(values);if(mean==null){valid=false;break;}if(RhythmStats.distance(centers[j],mean)>.01)moved=true;centers[j]=mean;}
                    if(!valid||!moved)break;
                }
                if(!valid)continue;List<Group> result=new ArrayList<>();for(List<RhythmStats.Night> bucket:buckets)result.add(new Group(bucket));double value=error(result);if(value<candidateError){candidateError=value;candidate=result;}
            }
            if(candidate==null||oldError-candidateError<60)break;best=candidate;oldError=candidateError;
        }
        best=new ArrayList<>(best);best.sort(Comparator.comparingDouble(g->g.midpoint));return best;
    }
    private static boolean knownZone(String recorded,ZoneId current){try{return recorded!=null&&!recorded.isEmpty()&&ZoneId.of(recorded).equals(current);}catch(DateTimeException invalid){return false;}}
    private static double error(List<Group> groups){double sum=0;int count=0;for(Group g:groups){sum+=g.spread*g.nights.size();count+=g.nights.size();}return count==0?0:sum/count;}
}
