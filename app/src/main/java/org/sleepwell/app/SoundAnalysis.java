package org.sleepwell.app;

/** Conservative score policy, not calibrated probabilities or a medical detector. */
final class SoundAnalysis {
    static final int SPEECH=0,BREATHING=36,SNORING=38,SILENCE=494,CLASSES=521;
    static final class Result {final String category;final double score;Result(String category,double score){this.category=category;this.score=score;}}
    static Result classify(float[] scores){
        if(scores==null||scores.length!=CLASSES)throw new IllegalArgumentException("Expected 521 sound scores");
        int top=0;for(int n=0;n<scores.length;n++){if(!Float.isFinite(scores[n])||scores[n]<0||scores[n]>1)throw new IllegalArgumentException("Invalid sound score");if(scores[n]>scores[top])top=n;}
        if(top==SILENCE&&scores[SILENCE]>=.5f)return null;
        // YAMNet emits independent multilabel scores. Breathing is Snoring's parent,
        // not contradictory evidence: https://research.google.com/audioset/ontology/snoring.html
        // Keep the margin against every unrelated label AND respiratory siblings.
        float snoreCompetitor=0;for(int n=0;n<scores.length;n++)if(n!=SNORING&&n!=BREATHING)snoreCompetitor=Math.max(snoreCompetitor,scores[n]);
        if(scores[SNORING]>=.6f&&scores[SNORING]-snoreCompetitor>=.15f)return new Result("snoring",scores[SNORING]);
        float runner=0;for(int n=0;n<scores.length;n++)if(n!=top)runner=Math.max(runner,scores[n]);
        if(top==SPEECH&&scores[top]>=.65f&&scores[top]-runner>=.15f)return new Result("speech",scores[top]);
        if(scores[top]<.2f)return null;return new Result("noise",scores[top]);
    }
    /** Two consecutive windows, bounded event lengths; flush at pause/stop without inventing samples. */
    static final class Group {
        interface Sink {void event(long start,int windows,String category,double score);}
        private final Sink sink;private String category;private int windows;private long start;private double sum;
        Group(Sink sink){this.sink=sink;}
        void add(long at,Result value){if(value==null||!value.category.equals(category))flush();if(value==null)return;if(windows==0){start=at;category=value.category;}windows++;sum+=value.score;if(windows==10)flush();}
        void flush(){int count=windows;long at=start;String type=category;double score=count==0?0:sum/count;category=null;windows=0;sum=0;if(count>=2)sink.event(at,count,type,score);}
    }
}
