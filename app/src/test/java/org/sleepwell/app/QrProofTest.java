package org.sleepwell.app;
import org.junit.Test;
import static org.junit.Assert.*;
@org.junit.runner.RunWith(org.junit.runners.JUnit4.class)
public class QrProofTest {
 private final String hash=MorningCode.digest("bathroom");
 @Test public void matchingReplyCompletesOnlyOnceAndCannotSkipNextTask(){QrProof p=new QrProof("run",2,"nonce",hash);assertFalse(p.consume("other","run",2,hash,hash,false,()->true));assertFalse(p.consume("nonce","run",2,hash,MorningCode.digest("other"),false,()->true));assertTrue(p.consume("nonce","run",2,hash,hash,false,()->true));assertFalse(p.consume("nonce","run",2,hash,hash,false,()->true));}
 @Test public void livenessOccurrenceUnitAndEnrolledCodeChangesPermanentlyRevokeRequest(){for(int scenario=0;scenario<4;scenario++){final int n=scenario;QrProof p=new QrProof("run",2,"nonce",hash);assertFalse(p.consume("nonce",n==1?"next":"run",n==2?3:2,n==3?MorningCode.digest("replacement"):hash,hash,false,()->n!=0));assertFalse(p.consume("nonce","run",2,hash,hash,false,()->true));}}
 @Test public void fallbackIsOwnedButDoesNotRequireInventedScanProof(){QrProof p=new QrProof("run",2,"nonce",hash);assertFalse(p.consume("bad","run",2,hash,null,true,()->true));assertTrue(p.consume("nonce","run",2,hash,null,true,()->true));assertFalse(p.consume("nonce","run",2,hash,null,true,()->true));}
}
