package org.sleepwell.app;

/** Original bounded listening rules. Scores and amplitude are estimates, never identity or dB. */
enum AudioChallengeMode {
 SPEECH("Speak","Speak challenge","Speak for about 2 seconds","speech"),
 LAUGHTER("Laughter","Laughter estimate","Laugh naturally after tapping Listen","laughter"),
 LOUD("Loud sound","Sound-level challenge","Make a comfortable sound above the room baseline","sound");
 final String type,title,instruction,noun;
 AudioChallengeMode(String type,String title,String instruction,String noun){this.type=type;this.title=title;this.instruction=instruction;this.noun=noun;}
 static boolean supported(String type){for(AudioChallengeMode m:values())if(m.type.equals(type))return true;return false;}
 static AudioChallengeMode from(String type){for(AudioChallengeMode m:values())if(m.type.equals(type))return m;return SPEECH;}
 boolean model(){return this!=LOUD;}
 String detail(){return switch(this){case SPEECH->"Detects speech sounds, not words or who is speaking. No recording is saved.";case LAUGHTER->"Experimental on-device laughter-family estimate, not emotion, identity or liveness. Bedroom accuracy is unverified; Math is always available. No recording is saved.";case LOUD->"First stay quiet for about 3 seconds, then make a comfortable sound. Measures relative microphone amplitude, not calibrated decibels or an instrument. Do not shout into the phone or use dangerously loud sounds. No recording is saved.";};}
 static final class BaselineFailure extends IllegalStateException {BaselineFailure(){super("The baseline is already loud or clipped. Try a quieter room or use Math.");}}
 static final class Rule {
  final AudioChallengeMode mode;private int calibration,accepted;private double baseline;private boolean done;private final SpeakRule speech=new SpeakRule();
  Rule(AudioChallengeMode mode){this.mode=mode;}
  boolean accept(float[] samples,float[] scores){if(done)return false;if(mode==SPEECH){done=speech.accept(scores);accepted=speech.accepted();return done;}
   if(mode==LAUGHTER){SoundAnalysis.Result value=SoundAnalysis.classify(scores);accepted=value!=null&&value.category.equals("laughter")?accepted+1:0;return done=accepted>=2;}
   double sum=0;int clipped=0;for(float value:samples){if(!Float.isFinite(value)||value< -1||value>1)throw new IllegalArgumentException("Microphone samples are invalid.");sum+=value*value;if(Math.abs(value)>=.98)clipped++;}double rms=Math.sqrt(sum/Math.max(1,samples.length));
   if(calibration<3){if(clipped>0||rms>.08)throw new BaselineFailure();baseline=Math.max(baseline,rms);calibration++;return false;}
   // Fixed floor avoids a silent/near-zero denominator; two complete ~0.975s windows.
   double threshold=Math.max(.025,baseline*3.1622776602);accepted=clipped<=samples.length/100&&rms>=threshold?accepted+1:0;return done=accepted>=2;
  }
  int accepted(){return accepted;}
  String progress(){return mode==LOUD&&calibration<3?"Stay quiet · baseline "+calibration+" of 3 windows":accepted+" of 2 "+mode.noun+" windows";}
 }
}
