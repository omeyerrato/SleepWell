package org.sleepwell.app;

/** The same deliberate-shake threshold and spacing used by the admitted alarm owner. */
final class WakeShakeRule {
    static boolean accepts(double magnitude,long now,long previous){return magnitude>15&&now-previous>500;}
    private WakeShakeRule(){}
}
