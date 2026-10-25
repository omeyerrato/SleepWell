package org.sleepwell.app;

import java.util.concurrent.*;

/** Per-night immutable consent and ordered sound persistence. No microphone, files or cue output. */
final class SonarNightSound {
    interface Port {void event(long at,long duration,String category,double score)throws Exception;void status(SoundAnalysisStatus status)throws Exception;}
    private static final class Event {final long at,duration;final String category;final double score;Event(long at,int windows,String category,double score){this.at=at;duration=windows*975L;this.category=category;this.score=score;}}
    static final class Segment {
        final ArrayBlockingQueue<Event> events;final long start,base;final SonarSoundWorker worker;
        volatile boolean closed;boolean finishing;long capturedTo,active;
        Segment(long start,long base,SonarSoundWorker worker,ArrayBlockingQueue<Event> events){this.start=start;this.base=base;this.worker=worker;this.events=events;capturedTo=start;}
    }
    final boolean requested;private final SoundSensitivity sensitivity;private final SoundAnalysisStream.Factory factory;private final Port port;private final int eventLimit;
    private volatile Segment current;private volatile long completed;private volatile String issue="";private String saved="";private int eventCount;
    SonarNightSound(boolean requested,SoundAnalysisStream.Factory factory,Port port){this(requested,factory,port,3600);}
    SonarNightSound(boolean requested,SoundAnalysisStream.Factory factory,Port port,int eventLimit){this(requested,factory,port,eventLimit,SoundSensitivity.DEFAULT);}
    SonarNightSound(boolean requested,SoundAnalysisStream.Factory factory,Port port,int eventLimit,SoundSensitivity sensitivity){if(sensitivity==null)throw new IllegalArgumentException("Missing sound grouping policy");this.sensitivity=sensitivity;if(requested&&factory==null||port==null||eventLimit<1||eventLimit>3600)throw new IllegalArgumentException("Missing or invalid shared sound owner");this.requested=requested;this.factory=factory;this.port=port;this.eventLimit=eventLimit;if(requested&&!sensitivity.available())issue="capture";}
    Segment begin(long start,long wallAnchor,long elapsedAnchorNanos,Executor storage){
        if(!requested)return null;if(!issue.isEmpty()){if(saved.isEmpty())try{port.status(state());saved="error:0:"+issue;}catch(Exception failure){throw new IllegalStateException("Could not save unavailable sound status",failure);}return null;}
        if(current!=null&&!current.closed)throw new IllegalStateException("Previous sound worker has not acknowledged cleanup");
        ArrayBlockingQueue<Event> events=new ArrayBlockingQueue<>(64);
        SonarSoundWorker worker=new SonarSoundWorker(factory,(at,n,category,score)->{if(!events.offer(new Event(at,n,category,score)))throw new IllegalStateException("Sound persistence could not keep up");},wallAnchor,elapsedAnchorNanos,storage,sensitivity);
        Segment next=new Segment(start,completed,worker,events);
        current=next;return next;
    }
    void captured(Segment owner,long to,long active)throws Exception {if(owner==null)return;owner.capturedTo=to;owner.active=active;flush(owner);}
    void flush(Segment owner)throws Exception {
        if(owner==null)return;if(!owner.worker.issue().isEmpty()&&issue.isEmpty())issue=owner.worker.issue();
        for(int n=0;n<64;n++){Event event=owner.events.peek();if(event==null||event.at+event.duration>owner.capturedTo)break;owner.events.remove();if(event.at<owner.start)throw new IllegalStateException("Sound evidence predates its capture segment");if(eventCount<eventLimit){port.event(event.at,event.duration,event.category,event.score);eventCount++;}else{issue="limit";owner.worker.disable("limit");owner.events.clear();break;}}
        SoundAnalysisStatus actual=state();if(actual.frames*975>owner.active||actual.state.equals("error")&&(!owner.worker.released()||!owner.events.isEmpty()))return;
        String identity=actual.state+":"+actual.frames+":"+actual.issue;if(!identity.equals(saved)){port.status(actual);saved=identity;}
    }
    void finish(Segment owner,long cutoffNanos,Runnable complete,java.util.function.Consumer<Exception> failed){
        if(owner==null){complete.run();return;}if(owner.finishing)throw new IllegalStateException("Sound close already requested");owner.finishing=true;
        owner.worker.finish(cutoffNanos,result->{
            try{completed=owner.base+result.frames;owner.closed=true;if(issue.isEmpty())issue=result.issue;flush(owner);complete.run();}catch(Exception error){failed.accept(error);}
        });
    }
    void storageFailed(){if(requested&&issue.isEmpty())issue="storage";}
    SoundAnalysisStatus state(){
        if(!requested)return SoundAnalysisStatus.initial(false);Segment owner=current;long frames=owner==null||owner.closed?completed:owner.base+owner.worker.frames();String failure=issue;if(failure.isEmpty()&&owner!=null)failure=owner.worker.issue();
        return new SoundAnalysisStatus(true,!failure.isEmpty()?"error":frames>0?"active":"pending",frames,failure);
    }
    SoundAnalysisStatus ended(boolean interrupted){SoundAnalysisStatus actual=state();if(!requested||actual.state.equals("error"))return actual;if(interrupted)return new SoundAnalysisStatus(true,"interrupted",actual.frames,"interrupted");return new SoundAnalysisStatus(true,actual.frames>0?"complete":"insufficient",actual.frames,"");}
}
