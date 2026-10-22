package org.sleepwell.app;

/** One instance per tracked session, shared by every microphone restart. */
final class ClipBudget {
    private int saved;private long lastSaved;
    synchronized boolean canStart(long elapsed){return saved<60&&(saved==0||elapsed-lastSaved>=30000);}
    synchronized void saved(long elapsed){saved++;lastSaved=elapsed;}
}
