package org.sleepwell.app;
import android.app.Activity;import android.content.Intent;
/** Fresh private draft handoff. Starting remains the Nap screen's explicit action. */
final class TimerRequestDraft {
 final int minutes,seconds;final String message;
 TimerRequestDraft(int minutes,String message){this(minutes,message,0);}
 private TimerRequestDraft(int minutes,String message,int seconds){if(seconds<0||seconds>86400||seconds==0&&(minutes<1||minutes>600)||seconds>0&&minutes!=(seconds+59)/60)throw new IllegalArgumentException("Choose a valid timer duration.");this.minutes=minutes;this.seconds=seconds;this.message=TimerRequest.clean(message);}
 static Intent open(Activity owner,int minutes,String message){TimerRequestDraft selected=new TimerRequestDraft(minutes,message);return draft(owner,selected);}
 static Intent openSeconds(Activity owner,int seconds,String message){if(seconds<1||seconds>86400)throw new IllegalArgumentException("Choose 1 second to 24 hours.");return draft(owner,new TimerRequestDraft((seconds+59)/60,message,seconds));}
 private static Intent draft(Activity owner,TimerRequestDraft selected){Intent result=RecordUi.intent(owner,NapActivity.class).putExtra("id",0).putExtra("timerDraft",true).putExtra("timerMinutes",selected.minutes).putExtra("timerMessage",selected.message);if(selected.seconds>0)result.putExtra("timerSeconds",selected.seconds);return result;}
 static TimerRequestDraft read(Intent intent){if(intent==null||!intent.getBooleanExtra("timerDraft",false))return null;if(intent.getIntExtra("id",0)!=0)throw new IllegalArgumentException("Timer requests always create a new draft.");int seconds=intent.hasExtra("timerSeconds")?intent.getIntExtra("timerSeconds",-1):0;if(intent.hasExtra("timerSeconds")&&seconds<1)throw new IllegalArgumentException("Invalid exact timer duration.");return new TimerRequestDraft(intent.getIntExtra("timerMinutes",-1),intent.getStringExtra("timerMessage"),seconds);}
}
