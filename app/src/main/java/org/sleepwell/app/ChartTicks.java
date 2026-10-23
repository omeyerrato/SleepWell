package org.sleepwell.app;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.zone.ZoneOffsetTransition;
import java.util.*;
import java.util.function.ToDoubleFunction;

/** Clock-aligned labels on the real elapsed-time axis; no sample positions are changed. */
final class ChartTicks {
    static final class Tick {final long time;final String label;final int endpoint;Tick(long time,String label,int endpoint){this.time=time;this.label=label;this.endpoint=endpoint;}}
    static final class Placed {final Tick tick;final double center,left,right;Placed(Tick tick,double center,double width){this.tick=tick;this.center=center;left=center-width/2;right=center+width/2;}}
    private static final long MINUTE=60000,HOUR=60*MINUTE,DAY=24*HOUR;
    static boolean elapsed(long start,long end){return end-start<2*MINUTE;}
    static String duration(long millis){if(millis<=0)return "0 s";if(millis<1000)return millis+" ms";return java.math.BigDecimal.valueOf(millis,3).stripTrailingZeros().toPlainString()+" s";}
    static String clock(long time,long start,long end,ZoneId zone,boolean clock24,Locale locale,boolean compact,boolean multiline){ZonedDateTime local=Instant.ofEpochMilli(time).atZone(zone);String pattern=clock24?"HH:mm":compact&&local.getMinute()==0&&local.getSecond()==0?"h a":"h:mm a";String text=local.format(DateTimeFormatter.ofPattern(pattern,locale));String detail=zone.getRules().getValidOffsets(local.toLocalDateTime()).size()>1?local.getOffset().getId():end-start>=DAY?local.format(DateTimeFormatter.ofPattern("d MMM",locale)):"";return detail.isEmpty()?text:text+(multiline?"\n":" ")+detail;}
    static String tooltip(long time,long start,long end,ZoneId zone,boolean clock24,Locale locale){return elapsed(start,end)?duration(Math.max(0,time-start)):clock(time,start,end,zone,clock24,locale,false,false);}
    private static long step(long duration,int slots,boolean seconds){double wanted=duration/(double)Math.max(1,slots-1);long[] choices=seconds?new long[]{1000,2000,5000,10000,15000,30000,60000}:new long[]{MINUTE,5*MINUTE,15*MINUTE,30*MINUTE,HOUR,2*HOUR,3*HOUR,4*HOUR,6*HOUR,12*HOUR,DAY};for(long value:choices)if(value>=wanted*.85)return value;return Math.max(DAY,(long)Math.ceil(wanted/DAY)*DAY);}
    static List<Tick> candidates(long start,long end,ZoneId zone,boolean clock24,Locale locale,int slots){if(end<=start)return Collections.emptyList();boolean seconds=elapsed(start,end);long interval=step(end-start,Math.max(2,Math.min(8,slots)),seconds);SortedMap<Long,Tick> ticks=new TreeMap<>();ticks.put(start,new Tick(start,seconds?duration(0):clock(start,start,end,zone,clock24,locale,false,true),-1));ticks.put(end,new Tick(end,seconds?duration(end-start):clock(end,start,end,zone,clock24,locale,false,true),1));
        if(seconds){for(long t=start+interval;t<end;t+=interval)ticks.put(t,new Tick(t,duration(t-start),0));return new ArrayList<>(ticks.values());}
        // The first whole hour determines the phase of multi-hour ticks (11 PM, 1 AM,
        // 3 AM...). Process each constant-offset segment separately: gaps never invent
        // a local time, and a repeated hour can produce two distinct real instants.
        LocalDateTime local=Instant.ofEpochMilli(start).atZone(zone).toLocalDateTime();int quantum=(int)Math.min(60,interval/MINUTE);LocalDateTime anchor=local.withSecond(0).withNano(0).withMinute(local.getMinute()/quantum*quantum);if(anchor.isBefore(local))anchor=anchor.plusMinutes(quantum);long phase=anchor.toInstant(ZoneOffset.UTC).toEpochMilli();long cursor=start;int segments=0;
        while(cursor<end&&segments++<512){Instant at=Instant.ofEpochMilli(cursor);long offset=zone.getRules().getOffset(at).getTotalSeconds()*1000L;ZoneOffsetTransition transition=zone.getRules().nextTransition(at);long boundary=transition==null?end:Math.min(end,transition.getInstant().toEpochMilli());if(boundary<=cursor)break;long wall=cursor+offset;long multiple=Math.floorDiv(wall-phase,interval);long first=phase+multiple*interval;if(first<wall)first+=interval;for(long candidate=first-offset;candidate<boundary&&candidate<end;candidate+=interval){if(candidate>start)ticks.put(candidate,new Tick(candidate,clock(candidate,start,end,zone,clock24,locale,true,true),0));}cursor=boundary;}
        return new ArrayList<>(ticks.values());
    }
    /** Keep the exact end label. Start/interior labels are omitted if text cannot fit. */
    static List<Placed> layout(List<Tick> ticks,long start,long end,double left,double right,double gap,ToDoubleFunction<String> measure){if(ticks.isEmpty()||end<=start||right<=left)return Collections.emptyList();Tick last=ticks.get(ticks.size()-1);double endWidth=width(last.label,measure);Placed endLabel=new Placed(last,right-endWidth/2,endWidth);List<Placed> result=new ArrayList<>();double occupied=left-gap;Tick first=ticks.get(0);double startWidth=width(first.label,measure);if(first!=last&&left+startWidth+gap<=endLabel.left){Placed begin=new Placed(first,left+startWidth/2,startWidth);result.add(begin);occupied=begin.right;}
        for(Tick tick:ticks){if(tick.endpoint!=0)continue;double x=left+(right-left)*(tick.time-start)/(double)(end-start),width=width(tick.label,measure);Placed placed=new Placed(tick,x,width);if(placed.left>=Math.max(left,occupied+gap)&&placed.right+gap<=endLabel.left){result.add(placed);occupied=placed.right;}}result.add(endLabel);return Collections.unmodifiableList(result);}
    private static double width(String value,ToDoubleFunction<String> measure){double width=0;for(String line:value.split("\n"))width=Math.max(width,measure.applyAsDouble(line));return width;}
}
