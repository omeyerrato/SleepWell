package org.sleepwell.app;

import java.util.*;
import java.util.function.LongSupplier;

/** Main-loop orchestration, with replaceable clock/outputs for silent lifecycle tests. */
final class AlarmPlayback {
    interface Ready {void ready();void failed();}
    interface Layer {void prepare(Ready callback);void gain(float gain);void start(Ready activated);void close();default void completion(Runnable done){}}
    interface Sink {Layer create(String tone);default Layer create(String tone,boolean loop,String key,int position){return create(tone);}default String occurrence(){return "playback";}default int position(String key){return 0;}void volume(Layer layer,AlarmWakePolicy policy,boolean backup);void stopped();default void stopped(Runnable quiet){stopped();quiet.run();}default void failed(){}default void radioFallback(){}}
    private final AlarmWakePolicy policy;private final Sink sink;private final LongSupplier elapsed,clock;
    private final Set<Layer> allocated=new LinkedHashSet<>();private final String tone;private final boolean silent;private final AlarmSoundSequence sequence;private final Set<String> failedSources=new HashSet<>();private boolean completedCurrent;
    private Layer current,pending;private boolean ready,activating,closed,backup,fallback,started;private long loadingAt,connectionTimeout=4000;private int generation,pendingPosition=-1,currentPosition=-1;
    AlarmPlayback(Alarm alarm,Sink sink,LongSupplier elapsed,LongSupplier clock){policy=new AlarmWakePolicy(alarm);tone=!policy.quiet&&!alarm.soundModeInvalid&&alarm.soundMode.radio()?"radio:"+alarm.soundMode.radioUrl:alarm.tone;silent=!alarm.soundModeInvalid&&alarm.soundMode.silent();AlarmSoundSequence initial=!alarm.soundModeInvalid&&alarm.soundMode.playlist()?new AlarmSoundSequence(alarm.soundMode,sink.occurrence(),0):null;sequence=initial==null?null:new AlarmSoundSequence(alarm.soundMode,sink.occurrence(),sink.position(initial.key));this.sink=sink;this.elapsed=elapsed;this.clock=clock;}
    void start(){start(false);}
    void start(boolean escalation){if(started||closed)return;started=true;backup=escalation&&!policy.quiet;if(silent)return;if(backup)load("pulse",false);else primary(false);}
    private void primary(boolean replacement){if(sequence!=null){int checked=0;while(checked<sequence.order.size()&&failedSources.contains(sequence.current())){sequence.next();checked++;}if(checked==sequence.order.size()){fallback=true;load("daybreak",replacement);return;}}load(sequence==null?tone:sequence.current(),replacement,sequence==null?-1:sequence.position());}
    private void load(String source,boolean replacement){load(source,replacement,-1);}
    private void load(String source,boolean replacement,int position){
        int request=++generation;pendingPosition=position;ready=false;activating=false;loadingAt=clock.getAsLong();connectionTimeout=AlarmRadioUrl.source(source)?AlarmRadioUrl.TIMEOUT:4000;
        try{Layer layer=AlarmRadioUrl.source(source)?sink.create(source,false,"",-1):position<0?sink.create(source):sink.create(source,false,sequence.key,position);allocated.add(layer);pending=layer;if(position>=0)layer.completion(()->completed(layer));else if(AlarmRadioUrl.source(source))layer.completion(()->{if(!closed&&(layer==current||layer==pending))failure(layer,false);});layer.prepare(new Ready(){
            public void ready(){if(closed||request!=generation){safeClose(layer);return;}ready=true;tick();}
            public void failed(){if(closed){safeClose(layer);return;}if(layer==current){if(pending!=null&&pending!=layer){safeClose(layer);current=null;return;}failure(layer,false);return;}if(request==generation)failure(layer,replacement);else safeClose(layer);}
        });}catch(RuntimeException error){failure(pending,replacement);}
    }
    private void completed(Layer layer){if(closed||layer!=current)return;if(pending!=null){completedCurrent=true;return;}safeClose(layer);current=null;completedCurrent=false;sequence.next();primary(false);}
    private void failure(Layer failed,boolean replacement){
        boolean sequenceAttempt=failed==current?currentPosition>=0:pendingPosition>=0;generation++;if(failed!=null)safeClose(failed);if(current==failed)current=null;pending=null;ready=false;activating=false;
        if(sequenceAttempt&&sequence!=null&&!fallback){failedSources.add(sequence.current());if(failedSources.size()<sequence.order.size()){sequence.next();primary(current!=null);return;}}
        if(replacement&&current!=null){try{sink.volume(current,policy,backup);}catch(RuntimeException ignored){}apply();if(completedCurrent&&current!=null)completed(current);return;}
        if(!fallback){if(!backup&&AlarmRadioUrl.source(tone))try{sink.radioFallback();}catch(RuntimeException ignored){}fallback=true;load("daybreak",false);}else sink.failed();
    }
    void tick(){
        if(closed||silent)return;
        if(pending!=null&&(!ready||activating)&&clock.getAsLong()-loadingAt>=connectionTimeout){failure(pending,current!=null);return;}
        if(current!=null)apply();
        if(pending==null||!ready||!policy.audible(elapsed.getAsLong(),backup))return;
        if(activating){try{pending.gain((float)policy.gain(elapsed.getAsLong(),backup));}catch(RuntimeException error){failure(pending,current!=null);}return;}
        Layer next=pending;Layer previous=current;
        try{
            next.gain((float)policy.gain(elapsed.getAsLong(),backup));
            sink.volume(next,policy,backup);
            activating=true;loadingAt=clock.getAsLong();int request=generation;
            next.start(new Ready(){public void ready(){if(closed||request!=generation||pending!=next){safeClose(next);return;}current=next;completedCurrent=false;currentPosition=pendingPosition;pending=null;pendingPosition=-1;activating=false;if(previous!=null&&previous!=next)safeClose(previous);}public void failed(){if(!closed&&request==generation)failure(next,previous!=null);else safeClose(next);}});
        }catch(RuntimeException error){failure(next,previous!=null);}
    }
    private void apply(){try{current.gain((float)policy.gain(elapsed.getAsLong(),backup));}catch(RuntimeException error){failure(current,false);}}
    void backup(){backup(true);}
    void backup(boolean enabled){if(closed||silent||backup==enabled||policy.quiet)return;backup=enabled;if(pending!=null){safeClose(pending);pending=null;}if(!enabled&&current!=null)try{sink.volume(current,policy,false);apply();}catch(RuntimeException error){failure(current,false);return;}if(enabled)load("pulse",current!=null);else{fallback=false;primary(current!=null);}}
    void stop(){stop(()->{});}
    void stop(Runnable quiet){if(closed)return;closed=true;generation++;for(Layer layer:new ArrayList<>(allocated))safeClose(layer);current=pending=null;try{sink.stopped(quiet);}catch(RuntimeException ignored){}}
    private void safeClose(Layer layer){try{layer.close();}catch(RuntimeException ignored){}finally{allocated.remove(layer);}}
}
