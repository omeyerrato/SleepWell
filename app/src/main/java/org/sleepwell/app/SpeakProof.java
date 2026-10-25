package org.sleepwell.app;

/** A private microphone result advances exactly its still-live Speak unit once. */
final class SpeakProof {
 final String run,nonce;final int unit;private boolean used;
 SpeakProof(String run,int unit,String nonce){if(run==null||run.isEmpty()||unit<0||nonce==null||nonce.isEmpty())throw new IllegalArgumentException("Invalid Speak request");this.run=run;this.unit=unit;this.nonce=nonce;}
 boolean consume(String responseRun,int responseUnit,String responseNonce,String currentRun,int currentUnit,boolean speak,boolean live){if(used)return false;if(!speak||!live||!run.equals(currentRun)||unit!=currentUnit){used=true;return false;}if(!run.equals(responseRun)||unit!=responseUnit||!nonce.equals(responseNonce))return false;used=true;return true;}
}
