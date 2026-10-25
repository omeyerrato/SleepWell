package org.sleepwell.app;

/** One instance per tracked session, shared by every microphone restart. */
final class ClipBudget {
    final SoundSensitivity sensitivity;
    ClipBudget(){this(SoundSensitivity.DEFAULT);}
    ClipBudget(SoundSensitivity sensitivity){if(sensitivity==null)throw new IllegalArgumentException("Missing sound policy");this.sensitivity=sensitivity;}
    final SnoreCuePolicy.Budget snoreCues=new SnoreCuePolicy.Budget();
    private int events;private int saved;private long lastSaved;private boolean pruned;
    boolean analysisAllowed(){return sensitivity.available();}
    boolean permitsClips(boolean requested){return requested&&sensitivity.allIncluded();}
    synchronized boolean pruneOnce(){if(pruned)return false;pruned=true;return true;}
    synchronized boolean canStart(long elapsed){return sensitivity.allIncluded()&&saved<60&&(saved==0||elapsed-lastSaved>=30000);}
    synchronized boolean saveEvent(){if(events>=3600)return false;events++;return true;}
    synchronized void saved(long elapsed){saved++;lastSaved=elapsed;}
}
