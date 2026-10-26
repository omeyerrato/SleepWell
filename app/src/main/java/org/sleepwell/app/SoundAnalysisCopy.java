package org.sleepwell.app;

/** Historical analysis wording shared by Report and its event list; current preferences are not evidence. */
final class SoundAnalysisCopy {
    static final class Info {
        final SoundAnalysisStatus status;final String title,detail;final boolean attention;
        Info(SoundAnalysisStatus status,String title,String detail,boolean attention){this.status=status;this.title=title;this.detail=detail;this.attention=attention;}
        String empty(){
            if(status==null)return "No saved events. "+detail;
            return switch(status.state){case "off"->"Sound analysis was off for this night.";case "complete"->"No matching sound events were saved.";case "insufficient"->"Not enough audio was analyzed to produce estimates.";case "error","interrupted"->"No saved events. "+detail;default->"No saved events yet.";};
        }
    }
    static Info of(RecordEntry record){return of(record.manual(),record.soundAnalysis,record.rawEnd>0);}
    static Info of(boolean manual,String encoded,boolean closed){
        if(manual)return new Info(null,"No sound analysis","This entry has no microphone analysis.",false);
        try{
            SoundAnalysisStatus status=SoundAnalysisProvenance.read(encoded);
            if(status==null)return new Info(null,"Analysis status unavailable","No historical analysis status was saved.",false);
            if(closed&&!status.terminal())throw new IllegalArgumentException("Closed recording has unfinished analysis");
            String amount=status.frames==0?"":RecordUi.duration(status.frames*975)+" of audio analyzed.";
            String title=switch(status.state){case "off"->"Sound analysis was off";case "pending"->"Waiting for audio analysis";case "active"->"Sound analysis is running";case "complete"->"Sound analysis finished";case "insufficient"->"Not enough audio to analyze";case "interrupted"->"Sound analysis was interrupted";default->"Sound analysis needs attention";};
            String issue=switch(status.issue){case "model"->"The sound detector was unavailable.";case "capture"->"Microphone capture was unavailable.";case "queue"->"Audio processing could not keep up.";case "storage"->"Sound estimates could not all be saved.";case "cleanup"->"Audio cleanup could not be confirmed.";case "limit"->"The sound-analysis limit was reached.";case "interrupted"->"The recording ended unexpectedly.";default->"";};
            return new Info(status,title,issue.isEmpty()?amount:issue+(amount.isEmpty()?"":" "+amount),status.state.equals("error")||status.state.equals("interrupted"));
        }catch(IllegalArgumentException invalid){return new Info(null,"Saved analysis status needs attention","Historical analysis details could not be read.",true);}
    }
    private SoundAnalysisCopy(){}
}
