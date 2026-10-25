package org.sleepwell.app;

/** One fresh final on-device text result for exactly the still-current phrase round. */
final class PhraseProof {
    final String run,nonce;final int unit,rounds;final long issued;private boolean consumed;
    PhraseProof(String run,int unit,String nonce,long issued,int rounds){PhraseRule.rounds(rounds);PhraseRule.target(run,unit);if(nonce==null||!java.util.UUID.fromString(nonce).toString().equals(nonce)||issued<0)throw new IllegalArgumentException("Invalid phrase request.");this.run=run;this.unit=unit;this.nonce=nonce;this.issued=issued;this.rounds=rounds;}
    boolean consume(String responseRun,int responseUnit,String responseNonce,int responseRounds,long captured,long now,String currentRun,int currentUnit,int currentRounds,boolean current,boolean fallback){if(consumed)return false;if(!current||!run.equals(currentRun)||unit!=currentUnit||rounds!=currentRounds){consumed=true;return false;}if(!run.equals(responseRun)||unit!=responseUnit||!nonce.equals(responseNonce)||responseRounds!=rounds||!fallback&&(captured<issued||captured>now||now-captured>10000))return false;consumed=true;return true;}
}
