package org.sleepwell.app;

/** One instance per tracked session, shared by every microphone restart. */
final class ClipBudget {
    final SnoreCuePolicy.Budget snoreCues=new SnoreCuePolicy.Budget();
    private int events;private int saved;private long lastSaved;
    synchronized boolean canStart(long elapsed){return saved<60&&(saved==0||elapsed-lastSaved>=30000);}
    synchronized boolean saveEvent(){if(events>=3600)return false;events++;return true;}
    synchronized void saved(long elapsed){saved++;lastSaved=elapsed;}
}
