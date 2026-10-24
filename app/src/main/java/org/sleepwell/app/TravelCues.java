package org.sleepwell.app;
import android.content.Context;

/** Visible arming boundary; CI substitutes a private controller that cannot construct hardware. */
final class TravelCues {
 static final class Manual {final long session;final String token;Manual(long session,String token){this.session=session;this.token=token;}}
 interface Control {Manual manual();void arm(Context c,Manual expected,long revision);void stop(String nonce);TravelCueSession.State status(Context c);boolean live(String nonce);}
 static Control control=new Control(){public Manual manual(){return TrackingService.travelManual();}public void arm(Context c,Manual expected,long revision){TrackingService.armTravel(expected,revision);}public void stop(String nonce){TrackingService.stopTravel(nonce,"You stopped light cues.");}public boolean live(String nonce){return TrackingService.travelLive(nonce);}public TravelCueSession.State status(Context c){TravelCueSession.State live=TrackingService.travelStatus();if(live!=null)return live;TravelCueSession.State saved=new TravelCueStore(TravelPlans.factory.open(c)).last();return saved!=null&&saved.busy()?new TravelCueSession.State(saved.nonce,TravelCueSession.Phase.INTERRUPTED,saved.start,saved.end,saved.requests,saved.skipped,"The previous session stopped. No pulses were replayed."):saved;}};
 static void changed(){TrackingService.stopTravel(null,"Light-cue settings changed. Arm the reviewed window again.");}
 private TravelCues(){}
}
