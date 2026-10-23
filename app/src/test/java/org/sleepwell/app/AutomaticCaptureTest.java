package org.sleepwell.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class AutomaticCaptureTest {
    @Test public void defaultNeverCapturesEvenWithExistingManualPermission(){AutomaticCapture c=new AutomaticCapture(false,true);c.foregroundEstablished(false);c.recordingBegan();assertFalse(c.allowsCapture(true));assertFalse(c.keepClips());}
    @Test public void consentAloneNeverStartsTheMicrophone(){AutomaticCapture c=new AutomaticCapture(true,false);assertFalse(c.allowsCapture(true));c.foregroundEstablished(true);assertFalse(c.allowsCapture(true));}
    @Test public void establishedForegroundAndRealRecordingTransitionAreBothRequired(){AutomaticCapture c=new AutomaticCapture(true,false);try{c.recordingBegan();fail();}catch(IllegalStateException expected){}assertFalse(c.allowsCapture(true));c.foregroundEstablished(true);c.recordingBegan();assertTrue(c.allowsCapture(true));}
    @Test public void missingTypeCannotBeSilentlyUpgradedAfterArming(){AutomaticCapture c=new AutomaticCapture(true,true);try{c.foregroundEstablished(false);fail();}catch(IllegalStateException expected){}try{c.foregroundEstablished(true);fail();}catch(IllegalStateException expected){}assertFalse(c.allowsCapture(true));}
    @Test public void currentPermissionRevocationPreventsCapture(){AutomaticCapture c=new AutomaticCapture(true,false);c.foregroundEstablished(true);c.recordingBegan();assertFalse(c.allowsCapture(false));assertFalse(c.allowsCapture(true));assertFalse(c.foregroundType(true));}
    @Test public void pauseAndCloseBlockCaptureAndNeverReestablishConsent(){AutomaticCapture c=new AutomaticCapture(true,true);c.foregroundEstablished(true);c.recordingBegan();assertTrue(c.allowsCapture(true));c.pause();assertFalse(c.allowsCapture(true));c.recordingBegan();assertTrue(c.allowsCapture(true));c.close();c.recordingBegan();assertFalse(c.allowsCapture(true));try{c.foregroundEstablished(true);fail();}catch(IllegalStateException expected){}}
    @Test public void foregroundEstablishmentIsAnInitialOneTimeBoundary(){AutomaticCapture c=new AutomaticCapture(true,false);c.foregroundEstablished(true);try{c.foregroundEstablished(true);fail();}catch(IllegalStateException expected){}assertFalse(c.keepClips());}
    @Test public void foregroundTypeIsChosenBeforeTheWaitAndNeverPromotedAfterRevocation(){AutomaticCapture c=new AutomaticCapture(true,false);assertTrue(c.foregroundType(true));c.foregroundEstablished(true);assertTrue(c.foregroundType(true));assertFalse(c.foregroundType(false));assertFalse(c.foregroundType(true));}
}
