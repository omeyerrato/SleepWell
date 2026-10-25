package org.sleepwell.app;
/** Standard timer request data, kept separate from actual saved nap authority. */
final class TimerRequest {
 final Integer seconds;final String message;final boolean skipRequested;
 TimerRequest(Integer seconds,String message,boolean skip){if(seconds!=null&&(seconds<1||seconds>86400))throw new IllegalArgumentException("Requested timer length must be 1 second to 24 hours.");this.seconds=seconds;this.message=clean(message);skipRequested=skip;}
 int exactMinutes(){return seconds!=null&&seconds%60==0&&seconds<=36000?seconds/60:0;}
 int suggestedMinutes(){return seconds!=null&&seconds<36000?(seconds+59)/60:0;}
 static String clean(String raw){if(raw==null)return "";if(raw.length()>512)throw new IllegalArgumentException("Timer message is too long.");String text=raw.replaceAll("[\\p{Cntrl}\\p{Cf}\\p{Zl}\\p{Zp}]"," ").strip();if(text.codePointCount(0,text.length())>80)throw new IllegalArgumentException("Use a timer message of at most 80 characters.");return text;}
}
