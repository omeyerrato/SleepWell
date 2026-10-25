package org.sleepwell.app;
/** Actual measured interval/quality. Missing evidence is null, never zero motion. */
final class SonarFrame {
 enum Quality {CALIBRATING,NEEDS_MOVEMENT,VALID,NO_ECHO,CLIPPED,TIMING_LOST,ROUTE_LOST,SILENCED,CHARGING_LOST,UNAVAILABLE,CUE_ACTIVE}
 final long from,to;final Double movement;final Quality quality;final double snr,correlation;
 SonarFrame(long from,long to,Double movement,Quality quality,double snr,double correlation){if(from<0||to<=from||quality==null||!Double.isFinite(snr)||!Double.isFinite(correlation)||correlation<0||correlation>1||movement!=null&&(!Double.isFinite(movement)||movement<0||quality!=Quality.VALID)||quality==Quality.VALID&&movement==null)throw new IllegalArgumentException("Invalid sonar evidence");this.from=from;this.to=to;this.movement=movement;this.quality=quality;this.snr=snr;this.correlation=correlation;}
}
