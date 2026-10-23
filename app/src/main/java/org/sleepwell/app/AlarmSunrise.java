package org.sleepwell.app;

/** Window-only brightness; pause restores the original value and resume keeps occurrence time. */
final class AlarmSunrise {
    interface Port {void brightness(float value);void restore();}
    private final AlarmWakePolicy policy;private final Port port;private boolean visible,closed;
    AlarmSunrise(AlarmWakePolicy policy,Port port){this.policy=policy;this.port=port;}
    void resume(long elapsed){if(closed||!policy.sunrise)return;visible=true;tick(elapsed);}
    void tick(long elapsed){if(visible&&!closed)port.brightness(.02f+.98f*(float)policy.light(elapsed));}
    void pause(){if(!visible)return;visible=false;port.restore();}
    void close(){try{pause();}finally{closed=true;}}
}
