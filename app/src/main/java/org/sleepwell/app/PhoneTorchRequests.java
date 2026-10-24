package org.sleepwell.app;

/** Reservation is taken before enqueueing, so idle shutdown cannot strand a new client. */
final class PhoneTorchRequests {
 private int count;private boolean closed;
 synchronized boolean reserve(){if(closed)return false;if(count==Integer.MAX_VALUE)throw new IllegalStateException("Too many flashlight requests.");count++;return true;}
 synchronized void release(){if(count<=0)throw new IllegalStateException("Unowned flashlight request.");count--;}
 synchronized boolean closeIfIdle(){if(closed||count!=0)return false;closed=true;return true;}
 synchronized boolean closed(){return closed;}
 synchronized int count(){return count;}
}
