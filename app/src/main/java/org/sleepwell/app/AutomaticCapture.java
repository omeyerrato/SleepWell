package org.sleepwell.app;

/** Per-armed-run consent. A successful visible foreground establishment is required before capture. */
final class AutomaticCapture {
    private final boolean requested,keepClips;
    private boolean established,settled,active,closed,revoked;
    AutomaticCapture(boolean requested,boolean keepClips){this.requested=requested;this.keepClips=requested&&keepClips;}
    boolean requested(){return requested;}
    boolean keepClips(){return keepClips;}
    void foregroundEstablished(boolean microphoneType){
        if(closed||settled)throw new IllegalStateException("Foreground consent is already settled.");
        settled=true;
        if(requested&&!microphoneType)throw new IllegalStateException("Microphone foreground access was not established while arming.");
        established=true;
    }
    void recordingBegan(){if(closed)return;if(!established)throw new IllegalStateException("Foreground access must be established before recording.");active=true;}
    void pause(){active=false;}
    boolean foregroundType(boolean currentPermission){if(!currentPermission)revoked=true;return requested&&!closed&&!revoked&&(!settled||established);}
    boolean allowsCapture(boolean currentPermission){if(!currentPermission)revoked=true;return requested&&established&&active&&!closed&&!revoked;}
    void close(){closed=true;active=false;}
}
