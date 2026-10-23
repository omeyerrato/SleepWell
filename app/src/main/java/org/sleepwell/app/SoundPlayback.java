package org.sleepwell.app;

import java.util.*;

/** Single-threaded playback state machine; the Android sink is replaceable with a silent fake. */
final class SoundPlayback {
    interface Clock {long now();}
    interface Ready {void ready();void failed();}
    interface Layer {void prepare(Ready callback);void gain(float value);void start();void close();}
    interface Sink {Layer create(String source);}
    enum State {STOPPED,LOADING,PLAYING,PAUSED,FINISHED,ERROR}
    private final Clock clock;private final Sink sink;
    private final Map<String,Layer> layers=new LinkedHashMap<>();private final Set<String> prepared=new HashSet<>();
    private SoundMix mix;private long elapsed,anchor,loadingAt;private int generation;private float duck=1;
    State state=State.STOPPED;String error="";
    SoundPlayback(Clock clock,Sink sink){this.clock=clock;this.sink=sink;}
    long elapsed(){return elapsed+(state==State.PLAYING?Math.max(0,clock.now()-anchor):0);}
    SoundMix mix(){return mix==null?null:mix.copy();}
    void play(SoundMix choice){play(choice,0);}
    void play(SoundMix choice,long offset){stop();mix=choice.copy();mix.validate();elapsed=Math.max(0,offset);error="";duck=1;if(expired()){state=State.FINISHED;return;}load();}
    void resume(){if(state!=State.PAUSED)return;if(expired()){finish();return;}load();}
    private void load(){
        release();state=State.LOADING;loadingAt=clock.now();int token=generation;
        try{
            layers.put(mix.source(),sink.create(mix.source()));
            if(!mix.primary.equals("Brown noise"))layers.put("Brown noise",sink.create("Brown noise"));
            for(Map.Entry<String,Layer> entry:new ArrayList<>(layers.entrySet())){
                if(token!=generation)break;String key=entry.getKey();Layer layer=entry.getValue();
                layer.prepare(new Ready(){public void ready(){if(token!=generation||state!=State.LOADING){if(token!=generation)layer.close();return;}prepared.add(key);if(prepared.size()==layers.size())begin();}
                    public void failed(){if(token==generation)fail();else layer.close();}});
            }
        }catch(RuntimeException e){fail();}
    }
    private void begin(){
        try{anchor=clock.now();state=State.PLAYING;apply();for(Layer layer:layers.values())layer.start();}
        catch(RuntimeException e){fail();}
    }
    void update(SoundMix choice){
        choice.validate();boolean replace=mix!=null&&!mix.source().equals(choice.source());
        long at=elapsed();State before=state;mix=choice.copy();elapsed=at;anchor=clock.now();
        if((before==State.PLAYING||before==State.LOADING||before==State.PAUSED)&&expired()){finish();return;}
        if(replace&&(before==State.PLAYING||before==State.LOADING))load();else if(before==State.PLAYING)try{apply();}catch(RuntimeException e){fail();}
    }
    void tick(){if(state==State.LOADING&&clock.now()-loadingAt>=30000){fail();return;}if(state==State.PLAYING){if(expired())finish();else try{apply();}catch(RuntimeException e){fail();}}}
    void pause(){if(state!=State.LOADING&&state!=State.PLAYING)return;elapsed=elapsed();release();state=State.PAUSED;duck=1;}
    void stop(){release();elapsed=0;state=State.STOPPED;duck=1;error="";}
    void duck(boolean value){duck=value?.2f:1;if(state==State.PLAYING)try{apply();}catch(RuntimeException e){fail();}}
    void fail(){elapsed=elapsed();release();state=State.ERROR;error="This sound could not be played. Re-select it or import another audio file.";}
    private void finish(){elapsed=mix.timerMinutes*60000L;release();state=State.FINISHED;}
    private boolean expired(){return mix!=null&&elapsed()>=mix.timerMinutes*60000L;}
    float fade(){if(mix==null||mix.fadeMinutes==0)return 1;return Math.max(0,Math.min(1,(mix.timerMinutes*60000L-elapsed())/(float)(mix.fadeMinutes*60000L)));}
    private void apply(){
        int brown=mix.brown();float normalization=Math.max(100,mix.primaryGain+brown);float scale=fade()*duck/normalization;
        for(Map.Entry<String,Layer> entry:layers.entrySet())entry.getValue().gain((entry.getKey().equals(mix.source())?mix.primaryGain:brown)*scale);
    }
    private void release(){generation++;for(Layer layer:layers.values())layer.close();layers.clear();prepared.clear();}
}
