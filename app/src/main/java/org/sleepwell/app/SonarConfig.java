package org.sleepwell.app;
/** Immutable engineering configuration. No values are clinical or physical-SPL calibration. */
final class SonarConfig {
 static final int RATE=48000, CHIRP=1920;static final String ALGORITHM="fmcw_echo_change_v1";
 final double low=18500,high=20000;final float gain;
 SonarConfig(float gain){if(!Float.isFinite(gain)||gain<.01f||gain>.10f)throw new IllegalArgumentException("Sonar gain must be between 1% and 10%.");this.gain=gain;}
 static SonarConfig defaults(){return new SonarConfig(.05f);}
}
