package org.sleepwell.app;
/** Frozen conditions for one explicitly armed wait; never a recording stop policy. */
final class ArmedWaitConditions {static final ArmedWaitConditions NONE=new ArmedWaitConditions(false,false);final boolean charging,screenOff;ArmedWaitConditions(boolean charging,boolean screenOff){this.charging=charging;this.screenOff=screenOff;}String blocked(boolean isCharging,boolean interactive){return charging&&!isCharging?"WAITING_CHARGING":screenOff&&interactive?"WAITING_SCREEN_OFF":"";}}
