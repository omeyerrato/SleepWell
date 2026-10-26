package org.sleepwell.app;

/** Immutable per-night request and capture-worker evidence across actual microphone segments. */
final class MotionSoundAnalysis {
    static final class Segment {final long base;long frames;boolean closed;Segment(long base){this.base=base;}}
    final boolean requested;private Segment current;private long frames;private String issue="";
    MotionSoundAnalysis(boolean requested){this.requested=requested;}
    synchronized Segment begin(){if(!requested||!issue.isEmpty())return null;if(current!=null&&!current.closed)throw new IllegalStateException("Previous sound capture has not closed");current=new Segment(frames);return current;}
    synchronized boolean analyzing(Segment owner){return requested&&owner!=null&&owner==current&&!owner.closed&&issue.isEmpty();}
    synchronized void update(Segment owner,long successful,String failure){
        if(owner==null||owner!=current||owner.closed)return;
        if(successful<owner.frames||successful>SoundAnalysisStatus.MAX_FRAMES-owner.base){fail("limit");return;}
        // A storage stop may arrive during an inference. Its capture worker still acknowledges that
        // last real result before close; new segments/inferences remain disabled after the first issue.
        owner.frames=successful;frames=owner.base+successful;
        if(failure!=null&&!failure.isEmpty())fail(failure);
    }
    synchronized void fail(String reason){if(requested&&issue.isEmpty())issue=SoundAnalysisStatus.ISSUES.contains(reason)?reason:"capture";}
    synchronized void closed(Segment owner,long successful,String failure){update(owner,successful,failure);if(owner!=null&&owner==current)owner.closed=true;}
    synchronized SoundAnalysisStatus state(){return !requested?SoundAnalysisStatus.initial(false):new SoundAnalysisStatus(true,issue.equals("interrupted")?"interrupted":!issue.isEmpty()?"error":frames>0?"active":"pending",frames,issue);}
    /** Live errors are immediate; a terminal durable snapshot waits for the final worker acknowledgment. */
    synchronized SoundAnalysisStatus checkpoint(){SoundAnalysisStatus actual=state();return actual.terminal()&&current!=null&&!current.closed?null:actual;}
    synchronized SoundAnalysisStatus ended(boolean interrupted){if(current!=null&&!current.closed)throw new IllegalStateException("Sound capture has not acknowledged its final results");SoundAnalysisStatus actual=state();if(actual.terminal())return actual;return interrupted?new SoundAnalysisStatus(true,"interrupted",frames,"interrupted"):new SoundAnalysisStatus(true,frames>0?"complete":"insufficient",frames,"");}
}
