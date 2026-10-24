package org.sleepwell.app;

/** Conservative score policy, not calibrated probabilities or a medical detector. */
final class SoundAnalysis {
    static final int SPEECH=0,LAUGHTER=13,CRYING=19,BABY_CRY=20,BREATHING=36,SNORING=38,COUGH=42,SILENCE=494,CLASSES=521;
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
        // The exported class map contains Speech and five of its ontology children
        // (Child speech, Conversation, Narration, Babbling, Speech synthesizer).
        // Max aggregation supports the broad estimate without adding correlated scores.
        // https://research.google.com/audioset/ontology/speech.html
        float speech=0,speechCompetitor=0;for(int n=0;n<scores.length;n++)if(n<=5)speech=Math.max(speech,scores[n]);else speechCompetitor=Math.max(speechCompetitor,scores[n]);
        if(speech>=.65f&&speech-speechCompetitor>=.15f)return new Result("speech",speech);
        // Exported AudioSet ontology families, not a sum of correlated scores.
        // Laughter13 includes Baby laughter/Giggle/Snicker/Belly laugh/Chuckle14–18.
        Result laughter=family(scores,"laughter",13,18,-1);if(laughter!=null)return laughter;
        // Crying19 is compatible with its Baby cry20 child, but cannot supply infant evidence.
        Result baby=family(scores,"baby_cry",20,20,19);if(baby!=null)return baby;
        // AudioSet Cough42 includes Throat clearing43: UI explicitly names both.
        Result cough=family(scores,"cough",42,43,-1);if(cough!=null)return cough;
        if(scores[top]<.2f)return null;return new Result("noise",scores[top]);
    }
    private static Result family(float[] scores,String name,int first,int last,int ancestor){float positive=0,competitor=0;for(int n=0;n<scores.length;n++)if(n>=first&&n<=last)positive=Math.max(positive,scores[n]);else if(n!=ancestor)competitor=Math.max(competitor,scores[n]);return positive>=.65f&&positive-competitor>=.15f?new Result(name,positive):null;}
    /** Immutable per-class duration (default two windows); bounded events, no invented pause/stop samples. */
    static final class Group {
        interface Sink {void event(long start,int windows,String category,double score);}
        private final Sink sink;private final SoundSensitivity sensitivity;private String category;private int windows;private long start;private double sum;
        Group(Sink sink){this(sink,SoundSensitivity.DEFAULT);}
        Group(Sink sink,SoundSensitivity sensitivity){if(sink==null||sensitivity==null)throw new IllegalArgumentException("Missing sound grouping policy");this.sink=sink;this.sensitivity=sensitivity;}
        void add(long at,Result value){if(value==null||!value.category.equals(category))flush();if(value==null)return;if(windows==0){start=at;category=value.category;}windows++;sum+=value.score;if(windows==10)flush();}
        void flush(){int count=windows;long at=start;String type=category;double score=count==0?0:sum/count;category=null;windows=0;sum=0;if(count>0&&count>=sensitivity.windows(type))sink.event(at,count,type,score);}
    }
}
