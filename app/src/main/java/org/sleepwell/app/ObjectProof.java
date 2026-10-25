package org.sleepwell.app;
/** Current occurrence, routine position, model/target policy and private request all must match. */
final class ObjectProof {
 final String run,nonce,key;final int unit;final long issued;private boolean consumed;
 ObjectProof(String run,int unit,String nonce,String key,long issued){if(run==null||run.isBlank()||run.length()>128||unit<0||nonce==null||!java.util.UUID.fromString(nonce).toString().equals(nonce)||key==null||key.length()>128||issued<0)throw new IllegalArgumentException("Invalid object recognition request");this.run=run;this.unit=unit;this.nonce=nonce;this.key=key;this.issued=issued;}
 boolean consume(String responseRun,int responseUnit,String responseNonce,String responseKey,long captured,long now,String currentRun,int currentUnit,String currentKey,boolean live,boolean fallback){if(consumed)return false;if(!live||!run.equals(currentRun)||unit!=currentUnit||!key.equals(currentKey)){consumed=true;return false;}if(!run.equals(responseRun)||unit!=responseUnit||!nonce.equals(responseNonce)||!key.equals(responseKey)||!fallback&&(captured<issued||captured>now||now-captured>10000))return false;consumed=true;return true;}
}
