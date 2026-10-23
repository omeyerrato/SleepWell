package org.sleepwell.app;

import android.content.Context;
import com.google.ai.edge.litert.*;
import java.util.*;

/** Fixed mobile YAMNet contract. All handles belong to the creating capture worker. */
final class YamnetClassifier implements SoundAnalysisStream.Model {
    static final int SAMPLES=15600,CLASSES=521;
    static final String MODEL_ID="yamnet-mobile-v1-10c95ea3";
    private final Thread owner=Thread.currentThread();
    private Environment environment;private CompiledModel model;
    private List<TensorBuffer> inputs=List.of(),outputs=List.of();
    YamnetClassifier(Context context){
        try{
            environment=Environment.create();
            model=CompiledModel.create(context.getAssets(),"yamnet.tflite",new CompiledModel.Options(Accelerator.CPU),environment);
            inputs=model.createInputBuffers();outputs=model.createOutputBuffers();
            if(inputs.size()!=1||outputs.size()!=1)throw new IllegalStateException("Unsupported sound model contract");
            infer(new float[SAMPLES]);
        }catch(Exception|LinkageError failure){try{close();}catch(RuntimeException|LinkageError cleanup){failure.addSuppressed(cleanup);}throw new IllegalStateException("Sound model unavailable",failure);}
    }
    public float[] infer(float[] samples){
        owned();if(model==null||samples.length!=SAMPLES)throw new IllegalArgumentException("Expected 15600 mono samples");
        try{inputs.get(0).writeFloat(samples);model.run(inputs,outputs);float[] scores=outputs.get(0).readFloat();
        if(scores.length!=CLASSES)throw new IllegalStateException("Expected 521 sound scores");return scores;}catch(LiteRtException failure){throw new IllegalStateException("Sound inference failed",failure);}
    }
    private void owned(){if(Thread.currentThread()!=owner)throw new IllegalStateException("Sound model used outside its worker");}
    @Override public void close(){owned();List<AutoCloseable> handles=new ArrayList<>();handles.addAll(inputs);handles.addAll(outputs);handles.add(model);handles.add(environment);inputs=List.of();outputs=List.of();model=null;environment=null;CloseResources.close(handles);}
}
