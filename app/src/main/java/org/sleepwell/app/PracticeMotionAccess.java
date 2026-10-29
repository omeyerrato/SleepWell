package org.sleepwell.app;

import android.app.Activity;
import android.content.*;

/** Composition of existing foreground ports, with no registration or access request in construction. */
final class PracticeMotionAccess {
    interface Ports {
        boolean busy();VisibleTaskSensors.Port sensor(VisibleTaskSensors.Kind kind);
        TiltSensors.Port tilt();WalkSession.Port walk();PracticeNfcAccess.Port nfc();
        WalkPermission.Status walkAccess();void requestWalk();void nfcSettings();String enrolledTag();
    }
    interface Factory {Ports create(Activity owner);}
    static Factory factory=Platform::new;
    private static final class Platform implements Ports {
        final Activity owner;
        Platform(Activity owner){this.owner=owner;}
        public boolean busy(){
            // Reading fails closed. This owner never stops an alarm, tracking session or other capture.
            try{return AlarmService.active||TrackingService.running||Prefs.flag(owner,"ringing");}
            catch(RuntimeException unavailable){return true;}
        }
        public VisibleTaskSensors.Port sensor(VisibleTaskSensors.Kind kind){return VisibleTaskSensors.create(owner,kind);}
        public TiltSensors.Port tilt(){return TiltSensors.factory.create(owner);}
        public WalkSession.Port walk(){return WalkPlatform.factory.create(owner);}
        public PracticeNfcAccess.Port nfc(){return PracticeNfcAccess.create(owner);}
        public WalkPermission.Status walkAccess(){return WalkPermission.port.status(owner);}
        public void requestWalk(){WalkPermission.port.request(owner);}
        public void nfcSettings(){owner.startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));}
        public String enrolledTag(){return Prefs.text(owner,"nfcTag","");}
    }
    private PracticeMotionAccess(){}
}
