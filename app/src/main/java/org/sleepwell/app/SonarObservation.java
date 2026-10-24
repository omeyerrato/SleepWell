package org.sleepwell.app;
/** Hardware capture BOOTTIME coordinates, distinct from the DSP's relative frame indices. */
final class SonarObservation {
 final long fromNanos,toNanos;final SonarFrame frame;
 SonarObservation(long from,long to,SonarFrame frame){if(from<0||to<=from||to-from!=40000000L||frame==null)throw new IllegalArgumentException("Capture interval");fromNanos=from;toNanos=to;this.frame=frame;}
 long from(){return fromNanos/1000000L;}long to(){return toNanos/1000000L;}
}
