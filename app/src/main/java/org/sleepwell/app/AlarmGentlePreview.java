package org.sleepwell.app;

import java.util.function.LongSupplier;
import java.util.function.Supplier;

/** Explicit bounded media sample of the real ramp, never an alarm occurrence or delivery owner. */
final class AlarmGentlePreview implements AutoCloseable {
    enum Phase {STOPPED,LOADING,PLAYING,ERROR}
    static final long RAMP_SAMPLE=8000,STEADY_SAMPLE=3000,LOAD_TIMEOUT=8000;
    static final class Sample {
        final AlarmWakePolicy policy;final String source,label,issue;final long duration;
        Sample(Alarm alarm){policy=new AlarmWakePolicy(alarm);duration=policy.rampMillis==0?STEADY_SAMPLE:RAMP_SAMPLE;
            if(alarm.soundModeInvalid){source="";label="";issue="Review sound playback mode before previewing.";}
            else if(alarm.soundMode.silent()){source="";label="";issue="Silent mode has no audio preview.";}
            else{issue="";source=alarm.soundMode.radio()?"daybreak":alarm.soundMode.playlist()?alarm.soundMode.tracks.get(0):alarm.tone;label=alarm.soundMode.radio()?"Daybreak fallback":alarm.soundMode.playlist()?alarm.soundMode.name(source)+" (first listed track)":source.startsWith("content:")&&!alarm.toneDisplayName.isEmpty()?alarm.toneDisplayName:AlarmTones.name(source);}
        }
        float gain(long elapsed){long bounded=Math.max(0,Math.min(duration,elapsed)),virtual=policy.rampMillis==0?0:(long)(policy.rampMillis*(bounded/(double)duration));return clamp((float)(.25*policy.targetPercent/100d*policy.gain(policy.delayMillis+virtual,false)));}
        String disclosure(){String suffix=" (first listed track)",name=label,kind="";if(name.endsWith(suffix)){name=name.substring(0,name.length()-suffix.length());kind=" \u00b7 first listed track";}if(name.codePointCount(0,name.length())>18)name=name.substring(0,name.offsetByCodePoints(0,17))+"\u2026";return name+kind+" \u00b7 "+(policy.rampMillis==0?"3-second steady sample":"8-second sample")+" \u00b7 delay skipped";}
    }
    private final Supplier<AlarmTonePreview.Port> factory;private final LongSupplier clock;private final Runnable changed;private AlarmTonePreview.Port port;private AlarmTonePreview.Session session;private Sample sample;private Phase phase=Phase.STOPPED;private String message="";private boolean visible,closed;private int generation;private long openedAt,startedAt;
    AlarmGentlePreview(Supplier<AlarmTonePreview.Port> factory,LongSupplier clock,Runnable changed,String restored,boolean error){this.factory=factory;this.clock=clock;this.changed=changed;message=restored==null?"":restored;phase=error?Phase.ERROR:Phase.STOPPED;}
    Phase phase(){return phase;}String message(){return message;}boolean active(){return phase==Phase.LOADING||phase==Phase.PLAYING;}
    void visible(boolean value){visible=value;if(!value&&active())stop("Preview stopped. Tap to start again.");}
    void start(Alarm alarm){if(closed||!visible)return;if(active()){stop("Preview stopped.");return;}dispose();sample=new Sample(alarm);if(!sample.issue.isEmpty()){phase=Phase.ERROR;message=sample.issue;changed.run();return;}phase=Phase.LOADING;message="";openedAt=clock.getAsLong();int request=++generation;changed.run();boolean[] ready={false};
        try{if(port==null)port=factory.get();AlarmTonePreview.Session opened=port.openLoop(sample.source,0,new AlarmTonePreview.Events(){boolean live(){return !closed&&visible&&request==generation;}
            public void ready(){if(live()){ready[0]=true;if(session!=null)play();}}
            public void playing(){if(live()&&phase!=Phase.PLAYING){phase=Phase.PLAYING;startedAt=clock.getAsLong();changed.run();}}
            public void paused(){if(live())stop("Preview interrupted. Tap to start again.");}
            public void ended(){if(live())stop("Preview ended.");}
            public void failed(String reason){if(live())fail(reason);}
        });if(request==generation){session=opened;if(ready[0])play();}else opened.close();}catch(RuntimeException failure){fail("Preview unavailable. Try again.");}
    }
    private void play(){try{session.gain(0);session.play();}catch(RuntimeException failure){fail("Preview could not start. Try again.");}}
    void tick(){if(!active()||closed)return;long now=clock.getAsLong();if(phase==Phase.LOADING){if(now-openedAt>=LOAD_TIMEOUT)fail("Preview took too long to open. Try again.");return;}long elapsed=Math.max(0,now-startedAt);try{session.gain(sample.gain(elapsed));if(elapsed>=sample.duration)stop("Preview complete.");}catch(RuntimeException failure){fail("Preview volume unavailable. Try again.");}}
    void stop(String reason){dispose();phase=Phase.STOPPED;message=reason==null?"":reason;changed.run();}
    private void fail(String reason){dispose();phase=Phase.ERROR;message=reason==null||reason.isBlank()?"Preview unavailable. Try again.":reason;changed.run();}
    private void dispose(){generation++;AlarmTonePreview.Session prior=session;session=null;if(prior!=null)try{prior.close();}catch(RuntimeException ignored){/* Port retains its decoder lease if native disposal is uncertain. */}}
    static float clamp(float gain){return Float.isFinite(gain)?Math.max(0,Math.min(.25f,gain)):0;}
    @Override public void close(){if(closed)return;closed=true;dispose();phase=Phase.STOPPED;if(port!=null)try{port.close();}catch(RuntimeException ignored){}changed.run();}
}
