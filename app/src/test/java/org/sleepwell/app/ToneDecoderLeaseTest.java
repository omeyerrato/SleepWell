package org.sleepwell.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class ToneDecoderLeaseTest {
    @Test public void aBlockedOldOwnerPreventsRecreatedOwnersFromAllocating(){ToneDecoderLease old=ToneDecoderLease.acquire();try{for(int n=0;n<20;n++)try{ToneDecoderLease.acquire();fail("Process gate bypassed");}catch(IllegalStateException expected){}}finally{old.close();}try(ToneDecoderLease next=ToneDecoderLease.acquire()){assertNotNull(next);}}
    @Test public void staleDuplicateCleanupCannotReleaseANewerDecoder(){ToneDecoderLease first=ToneDecoderLease.acquire();first.close();ToneDecoderLease second=ToneDecoderLease.acquire();try{first.close();try{ToneDecoderLease.acquire();fail("Stale cleanup released active owner");}catch(IllegalStateException expected){}}finally{second.close();}}
    @Test public void cleanupAfterFailureAllowsExplicitRetry(){ToneDecoderLease failed=ToneDecoderLease.acquire();try{throw new IllegalStateException("Synthetic descriptor failed");}catch(IllegalStateException expected){}finally{failed.close();}try(ToneDecoderLease retry=ToneDecoderLease.acquire()){assertNotNull(retry);}}
}
