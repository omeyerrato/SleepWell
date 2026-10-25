package org.sleepwell.app;
/** Narrow AudioSet Piano/Electric piano compatibility policy; not instrument identity or authenticity. */
final class PianoRule {
    private int consecutive;private boolean done;
    static boolean window(float[] scores){
        if(scores==null||scores.length!=521)throw new IllegalArgumentException("Expected 521 sound scores");
        float positive=0,competitor=0;
        for(int n=0;n<scores.length;n++){
            float value=scores[n];if(!Float.isFinite(value)||value<0||value>1)throw new IllegalArgumentException("Invalid sound score");
            if(n==148||n==149)positive=Math.max(positive,value);
            else if(n!=132&&n!=133&&n!=147)competitor=Math.max(competitor,value);
        }
        return positive>=.65f&&positive-competitor>=.15f;
    }
    boolean accept(float[] scores){if(done)return false;consecutive=window(scores)?consecutive+1:0;if(consecutive>=2){done=true;return true;}return false;}
    int accepted(){return consecutive;}
}
