package org.sleepwell.app;

/** Bounded PCM framing and failure isolation. Invoked exclusively by the capture worker. */
final class SoundAnalysisStream implements AutoCloseable {
    interface Model extends AutoCloseable {float[] infer(float[] samples);void close();}
    interface Factory {Model load();}
    private static final int SAMPLES=15600;
    private final float[] frame=new float[SAMPLES];private final SoundAnalysis.Group groups;
    private final Runnable failed;private Model model;private int count;private long start;private boolean closed;
    SoundAnalysisStream(Factory factory,SoundAnalysis.Group.Sink sink,Runnable failed){this(factory,sink,failed,SoundSensitivity.DEFAULT);}
    SoundAnalysisStream(Factory factory,SoundAnalysis.Group.Sink sink,Runnable failed,SoundSensitivity sensitivity){this.groups=new SoundAnalysis.Group(sink,sensitivity);this.failed=failed;try{model=factory.load();}catch(RuntimeException|LinkageError error){failed.run();}}
    void accept(short[] pcm,int length,long end){
        if(closed||model==null)return;if(length<0||length>pcm.length)throw new IllegalArgumentException("Invalid PCM count");
        for(int n=0;n<length;n++){
            if(count==0)start=Math.max(1,end-(length-n)*1000L/16000);
            frame[count++]=pcm[n]/32768f;
            if(count==SAMPLES){try{groups.add(start,SoundAnalysis.classify(model.infer(frame)));}catch(RuntimeException|LinkageError error){try{groups.flush();}finally{release();failed.run();}return;}count=0;}
        }
    }
    private void release(){if(model!=null){try{model.close();}catch(RuntimeException ignored){}model=null;}}
    @Override public void close(){if(closed)return;closed=true;try{groups.flush();}finally{release();}}
}
