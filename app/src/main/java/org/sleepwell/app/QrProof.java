package org.sleepwell.app;

import java.util.function.BooleanSupplier;

/** One private scanner launch can complete exactly its owned occurrence/unit, once. */
final class QrProof {
    final String run,nonce,hash;final int unit;private boolean consumed;
    QrProof(String run,int unit,String nonce,String hash){if(run==null||run.isEmpty()||unit<0||nonce==null||nonce.isEmpty()||!MorningCode.validHash(hash))throw new IllegalArgumentException("Invalid QR challenge request.");this.run=run;this.unit=unit;this.nonce=nonce;this.hash=hash;}
    boolean consume(String responseNonce,String currentRun,int currentUnit,String currentHash,String scannedHash,boolean fallback,BooleanSupplier live){
        if(consumed)return false;if(!live.getAsBoolean()||!run.equals(currentRun)||unit!=currentUnit||!MorningCode.same(hash,currentHash)){consumed=true;return false;}
        if(!nonce.equals(responseNonce)||!fallback&&!MorningCode.same(hash,scannedHash))return false;consumed=true;return true;
    }
}
