package org.sleepwell.app;

import java.util.*;

/** Single-threaded playback state machine; Android sinks post their callbacks to its owner thread. */
final class SoundPlayback {
    interface Clock {long now();}
    interface Ready {void ready();void failed();}
    interface Layer {void prepare(Ready callback);void gain(float value);void start();void close();default void finite(Runnable completed){}default void pause(){close();}}
    interface Sink {Layer create(String source);}
    enum State {STOPPED,LOADING,PLAYING,PAUSED,FINISHED,ERROR}
    private final Clock clock;private final Sink sink;
    private final Map<String,Layer> layers=new LinkedHashMap<>();private final Set<String> prepared=new HashSet<>();
    private SoundMix mix;private long elapsed,anchor,loadingAt;private int generation,track;private float duck=1,attenuation=1;private String endedReason="";private String primaryKey="";
    private boolean loadingContinues,retainedPause,queueEnded,pendingCompletion;
    State state=State.STOPPED;String error="";
    SoundPlayback(Clock clock,Sink sink){this.clock=clock;this.sink=sink;}
    long elapsed(){return elapsed+(state==State.PLAYING||state==State.LOADING&&loadingContinues?Math.max(0,clock.now()-anchor):0);}
    SoundMix mix(){return mix==null?null:mix.copy();}
    boolean playlist(){return mix!=null&&mix.primary.equals(SoundPlaylist.NAME);}
    int trackNumber(){return playlist()?Math.min(track+1,mix.playlist.tracks.size()):0;}
    int trackCount(){return playlist()?mix.playlist.tracks.size():0;}
    String trackTitle(){return playlist()&&!mix.playlist.tracks.isEmpty()?mix.playlist.tracks.get(Math.min(track,mix.playlist.tracks.size()-1)).title:"";}
    String finishedMessage(){return !endedReason.isEmpty()?endedReason:queueEnded?"Playlist ended · tap Play to restart":"Timer finished · tap Play to start again";}
    private String source(){return playlist()?mix.playlist.tracks.get(track).uri:mix.source();}
    void play(SoundMix choice){play(choice,0);}
    void play(SoundMix choice,long offset){stop();mix=choice.copy();mix.validate();track=0;elapsed=Math.max(0,offset);error="";duck=1;queueEnded=false;if(expired()){state=State.FINISHED;return;}load();}
    void resume(){if(state!=State.PAUSED)return;if(expired()){finish();return;}if(playlist()&&retainedPause&&!layers.isEmpty()){retainedPause=false;if(pendingCompletion){pendingCompletion=false;anchor=clock.now();state=State.PLAYING;Layer brown=layers.get("Brown noise");if(brown!=null)brown.start();completed(generation,track,layers.get(primaryKey));}else begin();}else load();}
    private void load(){
        release();loadingContinues=false;state=State.LOADING;loadingAt=clock.now();int token=generation,index=track;
        try{primaryKey=source();Layer primary=sink.create(primaryKey);layers.put(primaryKey,primary);if(playlist())primary.finite(()->completed(token,index,primary));
            if(!mix.primary.equals("Brown noise"))layers.put("Brown noise",sink.create("Brown noise"));
            for(Map.Entry<String,Layer> entry:new ArrayList<>(layers.entrySet())){if(token!=generation)break;prepare(entry.getKey(),entry.getValue(),token,index);}
        }catch(RuntimeException e){fail();}
    }
    private void prepare(String key,Layer layer,int token,int index){
        layer.prepare(new Ready(){public void ready(){if(token!=generation||layers.get(key)!=layer){layer.close();return;}if(state!=State.LOADING)return;prepared.add(key);if(prepared.size()==layers.size())begin();}
            public void failed(){if(token==generation&&layers.get(key)==layer)fail();else layer.close();}});
    }
    private void begin(){
        try{elapsed=elapsed();anchor=clock.now();state=State.PLAYING;loadingContinues=false;retainedPause=false;apply();for(Layer layer:layers.values())layer.start();}
        catch(RuntimeException e){fail();}
    }
    private void completed(int token,int index,Layer owner){
        if(token!=generation||!playlist()||index!=track||layers.get(primaryKey)!=owner)return;if(state==State.PAUSED&&retainedPause){pendingCompletion=true;return;}if(state!=State.PLAYING)return;
        elapsed=elapsed();anchor=clock.now();layers.remove(primaryKey);prepared.remove(primaryKey);owner.close();track++;
        if(track>=mix.playlist.tracks.size()){queueEnded=true;release();state=State.FINISHED;return;}
        if(expired()){finish();return;}
        // Brown noise keeps playing. Its time and fade continue while the next document prepares.
        state=State.LOADING;loadingContinues=true;loadingAt=clock.now();
        try{primaryKey=source();Layer next=sink.create(primaryKey);layers.put(primaryKey,next);int selected=track;next.finite(()->completed(token,selected,next));prepare(primaryKey,next,token,selected);apply();}
        catch(RuntimeException failure){fail();}
    }
    void update(SoundMix choice){
        choice.validate();boolean replace=mix!=null&&!mix.source().equals(choice.source());long at=elapsed();State before=state;mix=choice.copy();elapsed=at;anchor=clock.now();
        if((before==State.PLAYING||before==State.LOADING||before==State.PAUSED)&&expired()){finish();return;}
        if(replace){track=0;retainedPause=false;queueEnded=false;if(before==State.PLAYING||before==State.LOADING)load();else if(before==State.PAUSED)release();}
        else if(before==State.PLAYING||before==State.LOADING&&loadingContinues)try{apply();}catch(RuntimeException e){fail();}
    }
    void tick(){if(state==State.LOADING&&clock.now()-loadingAt>=30000){fail();return;}if(state==State.PLAYING||state==State.LOADING&&loadingContinues){if(expired())finish();else try{apply();}catch(RuntimeException e){fail();}}}
    void pause(){if(state!=State.LOADING&&state!=State.PLAYING)return;elapsed=elapsed();if(playlist()&&state==State.PLAYING){try{for(Layer layer:layers.values())layer.pause();retainedPause=true;}catch(RuntimeException failure){release();retainedPause=false;}}else{release();retainedPause=false;}loadingContinues=false;state=State.PAUSED;duck=1;}
    void stop(){release();elapsed=0;track=0;queueEnded=false;state=State.STOPPED;duck=attenuation=1;endedReason="";error="";}
    void duck(boolean value){duck=value?.2f:1;if(state==State.PLAYING||state==State.LOADING&&loadingContinues)try{apply();}catch(RuntimeException e){fail();}}
    float attenuation(){return attenuation;}
    void attenuate(float gain){if(!Float.isFinite(gain)||gain<0||gain>1)return;attenuation=Math.min(attenuation,gain);if(state==State.PLAYING||state==State.LOADING&&loadingContinues){if(attenuation==0){elapsed=elapsed();endedReason="Movement-estimate fade ended · tap Play to restart";release();state=State.FINISHED;}else try{apply();}catch(RuntimeException failure){fail();}}}
    void fail(){elapsed=elapsed();release();state=State.ERROR;error=playlist()?"Playlist stopped. A file is unavailable or unreadable; no track was skipped. Re-select it before playing again.":"This sound could not be played. Re-select it or import another audio file.";}
    private void finish(){elapsed=mix.timerMinutes*60000L;queueEnded=false;release();state=State.FINISHED;}
    private boolean expired(){return mix!=null&&elapsed()>=mix.timerMinutes*60000L;}
    float fade(){if(mix==null||mix.fadeMinutes==0)return 1;return Math.max(0,Math.min(1,(mix.timerMinutes*60000L-elapsed())/(float)(mix.fadeMinutes*60000L)));}
    private void apply(){int brown=mix.brown();float normalization=Math.max(100,mix.primaryGain+brown);float scale=fade()*duck*attenuation/normalization;for(Map.Entry<String,Layer> entry:layers.entrySet())entry.getValue().gain((entry.getKey().equals(primaryKey)?mix.primaryGain:brown)*scale);}
    private void release(){generation++;pendingCompletion=false;for(Layer layer:layers.values())layer.close();layers.clear();prepared.clear();retainedPause=false;loadingContinues=false;}
}
