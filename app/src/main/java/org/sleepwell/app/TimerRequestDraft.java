package org.sleepwell.app;
import android.app.Activity;import android.content.Intent;
/** Fresh private draft handoff. Starting a timer remains the Nap screen's explicit action. */
final class TimerRequestDraft {
 final int minutes;final String message;
 TimerRequestDraft(int minutes,String message){if(minutes<1||minutes>600)throw new IllegalArgumentException("Choose 1 to 600 whole minutes.");this.minutes=minutes;this.message=TimerRequest.clean(message);}
 static Intent open(Activity owner,int minutes,String message){TimerRequestDraft selected=new TimerRequestDraft(minutes,message);return RecordUi.intent(owner,NapActivity.class).putExtra("id",0).putExtra("timerDraft",true).putExtra("timerMinutes",selected.minutes).putExtra("timerMessage",selected.message);}
 static TimerRequestDraft read(Intent intent){if(intent==null||!intent.getBooleanExtra("timerDraft",false))return null;if(intent.getIntExtra("id",0)!=0)throw new IllegalArgumentException("Timer requests always create a new draft.");return new TimerRequestDraft(intent.getIntExtra("timerMinutes",-1),intent.getStringExtra("timerMessage"));}
}
