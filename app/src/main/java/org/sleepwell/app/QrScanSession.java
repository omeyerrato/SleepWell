package org.sleepwell.app;

/** Camera callbacks lose ownership on pause, retry or occurrence loss; no callback survives a new generation. */
final class QrScanSession implements AutoCloseable {
    interface Listener {void captured(String hash);void error(String reason);}
    interface Port {void start(Listener listener);void close();}
    interface Effects {boolean current();void captured(String hash);void error(String reason);}
    enum State {CLOSED,SCANNING,CAPTURED,ERROR}
    private final Port port;private final Effects effects;private long generation;private boolean active,released=true;private State state=State.CLOSED;
    QrScanSession(Port port,Effects effects){this.port=port;this.effects=effects;}
    State state(){return state;}boolean released(){return released;}
    void start(){close();if(!released){state=State.ERROR;effects.error("Camera could not close. Reopen this screen before scanning again.");return;}if(!effects.current())return;long ticket=++generation;active=true;released=false;state=State.SCANNING;
        try{port.start(new Listener(){public void captured(String hash){if(!owned(ticket))return;if(!MorningCode.validHash(hash)){error("This QR code could not be read.");return;}stop(State.CAPTURED);if(released)effects.captured(hash);else effects.error("Camera could not close. Reopen this screen before continuing.");}public void error(String reason){if(!owned(ticket))return;stop(State.ERROR);effects.error(reason==null||reason.isBlank()?"Camera is unavailable.":reason);}});}catch(RuntimeException unavailable){if(active&&ticket==generation){stop(State.ERROR);effects.error("Camera could not start. Check permission and try again.");}}
    }
    private boolean owned(long ticket){if(!active||ticket!=generation)return false;if(!effects.current()){close();return false;}return true;}
    private void stop(State next){close();state=next;}
    @Override public void close(){active=false;generation++;try{port.close();released=true;}catch(RuntimeException unavailable){released=false;}state=State.CLOSED;}
}
