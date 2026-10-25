package org.sleepwell.app;
/** One-use admission expiry; no capture or Android work. */
final class SpeakAdmission {
    final long until;private boolean claimed;
    SpeakAdmission(long now){until=now+5000;}
    synchronized boolean claim(long now){if(claimed||now>=until)return false;claimed=true;return true;}
    synchronized boolean expired(long now){return !claimed&&now>=until;}
}
