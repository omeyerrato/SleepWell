package org.sleepwell.app;

/** Opt-in, bounded responses to estimates. No diagnosis, no alarm-volume changes. */
final class SnoreCuePolicy implements AutoCloseable {
    enum Mode {SOUND,VIBRATION,BOTH}
    static final class Settings {
        final boolean enabled;final Mode mode;final int intensity,repeats,sensitivity;
        Settings(boolean enabled,Mode mode,int intensity,int repeats,int sensitivity){this.enabled=enabled;this.mode=mode==null?Mode.VIBRATION:mode;this.intensity=Math.max(1,Math.min(3,intensity));this.repeats=Math.max(1,Math.min(3,repeats));this.sensitivity=Math.max(0,Math.min(2,sensitivity));}
        double threshold(){return sensitivity==0?.85:sensitivity==1?.75:.60;}
    }
    static final class Budget {
        private int count;private long last=-1;
        synchronized boolean claim(long elapsed){if(elapsed<0||count>=30||last>=0&&elapsed-last<120000)return false;last=elapsed;count++;return true;}
    }
    interface Device extends AutoCloseable {void play(Settings settings);void close();}
    interface Factory {Device create();}
    private final Budget budget;private final Factory factory;private final Runnable failure;private Device device;private boolean closed;
    SnoreCuePolicy(Budget budget,Factory factory,Runnable failure){this.budget=budget;this.factory=factory;this.failure=failure;}
    boolean event(String category,double score,long elapsed,Settings settings){
        if(closed||!settings.enabled||!"snoring".equals(category)||!Double.isFinite(score)||score<settings.threshold()||score>1||!budget.claim(elapsed))return false;
        try{if(device==null)device=factory.create();device.play(settings);return true;}catch(RuntimeException error){failure.run();return false;}
    }
    @Override public void close(){if(closed)return;closed=true;if(device!=null)try{device.close();}catch(RuntimeException error){failure.run();}}
}
