package org.sleepwell.app;

import java.util.Set;

/** Historical per-run evidence, never permission or authority to capture. Counts are successful model calls. */
final class SoundAnalysisStatus {
    static final long MAX_FRAMES=36L*3600000/975;
    static final Set<String> STATES=Set.of("off","pending","active","complete","insufficient","interrupted","error");
    static final Set<String> ISSUES=Set.of("model","capture","queue","storage","cleanup","interrupted");
    final boolean requested;final String state,issue;final long frames;
    SoundAnalysisStatus(boolean requested,String state,long frames,String issue){
        if(state==null||issue==null||!STATES.contains(state)||frames<0||frames>MAX_FRAMES
                ||!requested&&(!state.equals("off")||frames!=0||!issue.isEmpty())
                ||requested&&state.equals("off")
                ||Set.of("pending","insufficient").contains(state)&&frames!=0
                ||Set.of("active","complete").contains(state)&&frames==0
                ||Set.of("error","interrupted").contains(state)&&!ISSUES.contains(issue)
                ||!Set.of("error","interrupted").contains(state)&&!issue.isEmpty())throw new IllegalArgumentException("Invalid sound analysis evidence");
        this.requested=requested;this.state=state;this.frames=frames;this.issue=issue;
    }
    static SoundAnalysisStatus initial(boolean requested){return new SoundAnalysisStatus(requested,requested?"pending":"off",0,"");}
    boolean terminal(){return !state.equals("pending")&&!state.equals("active");}
    SoundAnalysisStatus interrupted(){return terminal()?this:new SoundAnalysisStatus(true,"interrupted",frames,"interrupted");}
    void follows(SoundAnalysisStatus previous){
        if(previous==null||requested!=previous.requested||frames<previous.frames
                ||previous.terminal()&&(!state.equals(previous.state)||frames!=previous.frames||!issue.equals(previous.issue))
                ||previous.state.equals("active")&&state.equals("pending"))throw new IllegalArgumentException("Sound analysis evidence changed ownership or moved backwards");
    }
    String summary(){switch(state){case "off":return "Sound analysis was off";case "pending":return "Sound analysis was requested";case "active":return "Sound analysis is running";case "complete":return "Sound analysis completed";case "insufficient":return "Not enough audio for sound analysis";case "interrupted":return "Sound analysis was interrupted";default:return "Sound analysis needs attention";}}
}
