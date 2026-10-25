package org.sleepwell.app;
/** Total scheduled snooze duration, counted in milliseconds including exact-clock choices. */
final class SnoozeBudget {
 static int read(Object value){if(!(value instanceof Number n)||!Double.isFinite(n.doubleValue())||n.doubleValue()!=Math.rint(n.doubleValue())||n.doubleValue()<0||n.doubleValue()>300)throw new IllegalArgumentException("Choose a total snooze budget from 1 to 300 minutes, or Off");return n.intValue();}
 static long remaining(int minutes,long spent){if(minutes<0||minutes>300||spent<0)return 0;return minutes==0?Long.MAX_VALUE:Math.max(0,minutes*60000L-spent);}
 static String label(long millis){if(millis==Long.MAX_VALUE)return "No time cap";long seconds=Math.max(0,millis)/1000;return seconds%60==0?seconds/60+" min":seconds/60+" min "+seconds%60+" sec";}
 static boolean clockAvailable(long wall,java.time.ZoneId zone,long maximum){if(maximum<=0)return false;java.time.ZonedDateTime now=java.time.Instant.ofEpochMilli(wall).atZone(zone),next=now.withSecond(0).withNano(0).plusMinutes(1);for(int n=0;n<61;n++,next=next.plusMinutes(1)){if(!next.toLocalDate().equals(now.toLocalDate())||next.toInstant().toEpochMilli()-wall>maximum)return false;if(next.toInstant().toEpochMilli()>wall&&zone.getRules().getValidOffsets(next.toLocalDateTime()).size()==1)return true;}return false;}
 private SnoozeBudget(){}
}
