package org.sleepwell.app;

/** Sample-weighted RMS for one epoch. Missing input remains absent rather than silent. */
final class SignalWindow {
    private double sumSquares=0;private long samples=0;
    synchronized void add(double rms,int count) {if(count<=0)return;sumSquares+=rms*rms*count;samples+=count;}
    synchronized Double take() {if(samples==0)return null;double value=Math.sqrt(sumSquares/samples);sumSquares=0;samples=0;return value;}
}
