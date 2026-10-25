package org.sleepwell.app;
/** Frozen explicit per-night recording choice. Never reads or changes consent preferences. */
final class SonarClipPolicy {
 static final SonarClipPolicy OFF=new SonarClipPolicy(false,0,null);
 final boolean requested,enabled;final double threshold;final ClipBudget budget;private boolean pending;
 private SonarClipPolicy(boolean requested,double threshold,ClipBudget budget){this.requested=requested;this.threshold=threshold;this.budget=budget;enabled=requested&&budget!=null&&budget.analysisAllowed()&&budget.permitsClips(true);}
 static SonarClipPolicy create(boolean requested,double threshold,ClipBudget budget){if(!Double.isFinite(threshold)||threshold<0||threshold>1||budget==null)throw new IllegalArgumentException("Invalid contactless recording policy");return new SonarClipPolicy(requested,threshold,budget);}
 synchronized boolean claim(long elapsed){if(!enabled||pending||!budget.canStart(elapsed))return false;pending=true;return true;}
 synchronized void abandon(){pending=false;}
 synchronized void saved(long elapsed){if(!enabled||!pending)throw new IllegalStateException("Recording lost its per-night consent or budget");budget.saved(elapsed);pending=false;}
}
