package org.sleepwell.app;

import java.util.function.BooleanSupplier;

/** One private scanner launch can complete exactly its owned occurrence/unit, once. */
final class QrProof {
    final String run,nonce,hash,format;final int unit;private boolean consumed;
    QrProof(String run,int unit,String nonce,String hash){this(run,unit,nonce,hash,MorningBarcode.QR);}
    QrProof(String run,int unit,String nonce,MorningCode code){this(run,unit,nonce,code.hash,code.format);}
    QrProof(String run,int unit,String nonce,String hash,String format){if(run==null||run.isEmpty()||unit<0||nonce==null||nonce.isEmpty()||!MorningCode.validHash(hash)||!MorningBarcode.supported(format))throw new IllegalArgumentException("Invalid morning-code challenge request.");this.run=run;this.unit=unit;this.nonce=nonce;this.hash=hash;this.format=format;}
    boolean consume(String responseNonce,String currentRun,int currentUnit,String currentHash,String scannedHash,boolean fallback,BooleanSupplier live){
        return consume(responseNonce,currentRun,currentUnit,MorningBarcode.QR,currentHash,MorningBarcode.QR,scannedHash,fallback,live);
    }
    boolean consume(String responseNonce,String currentRun,int currentUnit,String currentFormat,String currentHash,String scannedFormat,String scannedHash,boolean fallback,BooleanSupplier live){
        if(!format.equals(currentFormat)){consumed=true;return false;}if(!fallback&&!format.equals(scannedFormat))return false;
        if(consumed)return false;if(!live.getAsBoolean()||!run.equals(currentRun)||unit!=currentUnit||!MorningCode.same(hash,currentHash)){consumed=true;return false;}
        if(!nonce.equals(responseNonce)||!fallback&&!MorningCode.same(hash,scannedHash))return false;consumed=true;return true;
    }
}
