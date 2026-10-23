package org.sleepwell.app;

interface ClipPlaybackPort extends ClipReviewPlayer.Factory,AutoCloseable {
    void close();
    boolean awaitClosed(long timeout)throws InterruptedException;
}
