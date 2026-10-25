package org.sleepwell.app;
/** Nonblocking process ownership; retained until provider calls and stream cleanup return. */
final class CloudBackupLease {
 private static Object owner;
 static synchronized boolean acquire(Object next){if(owner!=null)return false;owner=next;return true;}
 static synchronized void release(Object expected){if(owner==expected)owner=null;}
 private CloudBackupLease(){}
}
