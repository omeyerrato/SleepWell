package org.sleepwell.app;

/** Voice activity only: exactly the unchanged production Speech score/margin and two full windows. */
final class SpeakRule {
    private int consecutive;private boolean done;
    boolean accept(float[] scores){if(done)return false;SoundAnalysis.Result result=SoundAnalysis.classify(scores);consecutive=result!=null&&result.category.equals("speech")?consecutive+1:0;if(consecutive>=2){done=true;return true;}return false;}
    int accepted(){return consecutive;}
}
