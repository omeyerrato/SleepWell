package org.sleepwell.app;
import java.time.ZoneId;
/** Original timing calculator; published arithmetic, not a reproduction of the MCTQ instrument.
 * Corrected formula: https://doi.org/10.1016/j.cub.2013.04.011 */
final class HabitualTiming {
 static final String METHOD="https://doi.org/10.1016/j.cub.2013.04.011",DETAILS="https://doi.org/10.1177/0748730412475041";
 // -1: unreported clock/count. For choices: 0 unknown, 1 eligible answer, 2 ineligible answer.
 int workOnset=-1,workEnd=-1,freeOnset=-1,freeEnd=-1,workdays=-1,regular=0,freeAlarm=0,unrestricted=0;
 String zone=ZoneId.systemDefault().getId();long revision;
 HabitualTiming copy(){HabitualTiming t=new HabitualTiming();t.workOnset=workOnset;t.workEnd=workEnd;t.freeOnset=freeOnset;t.freeEnd=freeEnd;t.workdays=workdays;t.regular=regular;t.freeAlarm=freeAlarm;t.unrestricted=unrestricted;t.zone=zone;t.revision=revision;return t;}
 void validate(){for(int value:new int[]{workOnset,workEnd,freeOnset,freeEnd})if(value< -1||value>=1440)throw new IllegalArgumentException("A reported clock time is invalid.");if(workdays< -1||workdays>7||regular<0||regular>2||freeAlarm<0||freeAlarm>2||unrestricted<0||unrestricted>2||revision<0)throw new IllegalArgumentException("A reported timing choice is invalid.");if(zone==null||zone.isEmpty())throw new IllegalArgumentException("The reported clock zone is missing.");try{ZoneId.of(zone);}catch(java.time.DateTimeException invalid){throw new IllegalArgumentException("The reported clock zone is invalid.");}}
 static int duration(int onset,int end){return Math.floorMod(end-onset,1440);}
 static final class Result {final boolean available;final String reason;final int workDuration,freeDuration;final double weeklyMinutes,freeMidpoint,correctedMidpoint,correction;
  Result(String reason){available=false;this.reason=reason;workDuration=freeDuration=0;weeklyMinutes=freeMidpoint=correctedMidpoint=correction=Double.NaN;}
  Result(int work,int free,double weekly,double middle,double correction){available=true;reason="";workDuration=work;freeDuration=free;weeklyMinutes=weekly;freeMidpoint=wrap(middle);this.correction=correction;correctedMidpoint=wrap(middle-correction);}
 }
 Result calculate(){validate();if(workOnset<0||workEnd<0||freeOnset<0||freeEnd<0)return new Result("Enter sleep onset and final waking for both day types.");if(workdays<0)return new Result("Enter the number of workdays in a usual seven-day week.");if(workdays==0||workdays==7)return new Result("This calculation needs both workdays and free days in the same usual week.");if(regular==0)return new Result("Confirm whether these times describe a regular day schedule.");if(regular!=1)return new Result("This simple calculation is unavailable for rotating, night or irregular schedules.");if(freeAlarm==0)return new Result("Report whether an alarm wakes you on free days.");if(freeAlarm!=1)return new Result("An alarm-constrained free-day wake time does not meet this method’s prerequisite.");if(unrestricted==0)return new Result("Confirm whether you can choose your free-day waking time.");if(unrestricted!=1)return new Result("A constrained free-day schedule does not meet this method’s prerequisite.");int work=duration(workOnset,workEnd),free=duration(freeOnset,freeEnd);if(work==0||free==0)return new Result("Equal onset and waking times are ambiguous. Enter a sleep period shorter than 24 hours.");double weekly=(work*(double)workdays+free*(7-workdays))/7d,middle=freeOnset+free/2d,correction=free>work?(free-weekly)/2d:0;return new Result(work,free,weekly,middle,correction);}
 static double wrap(double minute){double result=minute%1440;return result<0?result+1440:result;}
 static int displayMinute(double minute){return Math.floorMod((int)Math.round(minute),1440);}
}
