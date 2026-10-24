package org.sleepwell.app;

/** One analysis-worker-owned pipeline. Keeps original capture time; never opens microphone or audio output. */
final class SonarSoundPipeline implements AutoCloseable {
    private final SoundSensitivity sensitivity;private final SoundAnalysisStream.Factory factory;private final SoundAnalysis.Group.Sink events;private final long wall,elapsedNanos;private final Thread owner;
    private SoundAnalysisStream.Model model;private SoundAnalysisStream stream;private SonarDownsampler filter;private long expected=-1;private boolean closed,loading;
    private volatile long frames;private volatile String issue="";
    SonarSoundPipeline(SoundAnalysisStream.Factory factory,SoundAnalysis.Group.Sink events,long wall,long elapsedNanos){this(factory,events,wall,elapsedNanos,SoundSensitivity.DEFAULT);}
    SonarSoundPipeline(SoundAnalysisStream.Factory factory,SoundAnalysis.Group.Sink events,long wall,long elapsedNanos,SoundSensitivity sensitivity){if(sensitivity==null)throw new IllegalArgumentException("Missing sound grouping policy");this.sensitivity=sensitivity;if(factory==null||events==null||wall<=0||elapsedNanos<0)throw new IllegalArgumentException("Shared analysis anchor");this.factory=factory;this.events=events;this.wall=wall;this.elapsedNanos=elapsedNanos;owner=Thread.currentThread();}
    long frames(){return frames;}String issue(){return issue;}
    private void owned(){if(Thread.currentThread()!=owner)throw new IllegalStateException("Sound model must stay on its analysis worker");}
    void accept(short[] pcm,int count,long firstNanos,boolean valid){
        owned();if(closed||!issue.isEmpty())return;
        if(pcm==null||count<1||count>SonarDownsampler.MAX_INPUT||count>pcm.length||firstNanos<elapsedNanos){fail("capture");return;}
        if(!valid){gap();return;}
        if(expected>=0&&Math.abs(firstNanos-expected)>1){gap();if(!issue.isEmpty())return;}
        try{
            if(model==null){loading=true;model=factory.load();loading=false;if(model==null)throw new IllegalStateException("Model unavailable");}
            if(filter==null)filter=new SonarDownsampler();
            if(stream==null)stream=new SoundAnalysisStream(()->new SoundAnalysisStream.Model(){
                public float[] infer(float[] samples){float[] scores=model.infer(samples);SoundAnalysis.classify(scores);if(frames>=SoundAnalysisStatus.MAX_FRAMES)throw new IllegalStateException("Sound inference budget exceeded");frames++;return scores;}
                public void close(){} // The pipeline owns and closes the one real model across contiguous segments.
            },(at,windows,category,score)->{try{events.event(at,windows,category,score);}catch(RuntimeException failure){fail("storage");throw failure;}},()->fail("model"),sensitivity);
            SonarDownsampler.Output output=filter.accept(pcm,count,firstNanos);expected=Math.addExact(firstNanos,Math.round(count*(1e9/SonarDownsampler.INPUT_RATE)));
            if(output!=null){long end=Math.addExact(wall,(output.toNanos-elapsedNanos)/1000000L);stream.accept(output.pcm,output.pcm.length,end);}
        }catch(RuntimeException|LinkageError failure){fail(loading?"model":"capture");loading=false;}
    }
    /** Flush only existing full groups; partial classifier/FIR history is discarded across real gaps. */
    void gap(){owned();try{if(stream!=null){SoundAnalysisStream previous=stream;stream=null;previous.close();}}catch(RuntimeException|LinkageError failure){fail("storage");}finally{filter=null;expected=-1;}}
    void fail(String reason){if(issue.isEmpty())issue=reason;}
    @Override public void close(){owned();if(closed)return;closed=true;try{gap();}catch(RuntimeException failure){fail("storage");}finally{if(model!=null){try{model.close();}catch(RuntimeException|LinkageError failure){fail("cleanup");}finally{model=null;}}}}
}
