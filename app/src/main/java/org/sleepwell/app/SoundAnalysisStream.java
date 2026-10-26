package org.sleepwell.app;

/** Bounded PCM framing and failure isolation. Invoked exclusively by the capture worker. */
final class SoundAnalysisStream implements AutoCloseable {
    interface Model extends AutoCloseable {float[] infer(float[] samples);void close();}
    interface Factory {Model load();}
    private static final int SAMPLES=15600;
    private final float[] frame=new float[SAMPLES];private final SoundAnalysis.Group groups;
    private final Runnable failed;private Model model;private int count;private long start;private boolean closed;private volatile long frames;private volatile String issue="";
    SoundAnalysisStream(Factory factory,SoundAnalysis.Group.Sink sink,Runnable failed){this(factory,sink,failed,SoundSensitivity.DEFAULT);}
    SoundAnalysisStream(Factory factory,SoundAnalysis.Group.Sink sink,Runnable failed,SoundSensitivity sensitivity){this.failed=failed;this.groups=new SoundAnalysis.Group((at,windows,category,score)->{try{sink.event(at,windows,category,score);}catch(RuntimeException|LinkageError failure){fail("storage");throw failure;}},sensitivity);if(!sensitivity.available()){fail("capture");return;}try{model=factory.load();if(model==null)fail("model");}catch(RuntimeException|LinkageError error){fail("model");}}
    long frames(){return frames;}String issue(){return issue;}
    private void fail(String reason){if(!issue.isEmpty())return;issue=reason;try{failed.run();}catch(RuntimeException ignored){/* Evidence does not depend on a preference/UI observer succeeding. */}}
    void accept(short[] pcm,int length,long end){
        if(closed||model==null||!issue.isEmpty())return;if(length<0||length>pcm.length)throw new IllegalArgumentException("Invalid PCM count");
        for(int n=0;n<length;n++){
            if(count==0)start=Math.max(1,end-(length-n)*1000L/16000);
            frame[count++]=pcm[n]/32768f;
            if(count==SAMPLES){try{if(frames>=SoundAnalysisStatus.MAX_FRAMES){fail("limit");release();return;}SoundAnalysis.Result result=SoundAnalysis.classify(model.infer(frame));frames++;groups.add(start,result);}catch(RuntimeException|LinkageError error){fail("model");try{groups.flush();}finally{release();}return;}count=0;}
        }
    }
    void gap(){if(closed)return;count=0;groups.flush();}
    private void release(){if(model!=null){try{model.close();model=null;}catch(RuntimeException|LinkageError cleanup){fail("cleanup");}}}
    @Override public void close(){if(closed)return;closed=true;try{groups.flush();}finally{release();}}
}
