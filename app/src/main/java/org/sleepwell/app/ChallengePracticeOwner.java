package org.sleepwell.app;

import android.app.Activity;
import android.os.Looper;
import android.view.View;
import java.util.function.BooleanSupplier;

/** Preview-only input admission. No ringing marker, alarm proof or service can be minted here. */
final class ChallengePracticeOwner {
    private final Activity activity;
    private final View card;
    private final ChallengePracticeSession session;
    private final ChallengePracticeSession.Ticket ticket;
    private final BooleanSupplier resumed;

    ChallengePracticeOwner(Activity activity,View card,ChallengePracticeSession session,
                           ChallengePracticeSession.Ticket ticket,BooleanSupplier resumed){
        this.activity=activity;this.card=card;this.session=session;this.ticket=ticket;this.resumed=resumed;
    }
    boolean current(){
        if(Looper.myLooper()!=Looper.getMainLooper())return false;
        return resumed.getAsBoolean()&&!activity.isFinishing()&&!activity.isDestroyed()
                &&activity.hasWindowFocus()&&card.isAttachedToWindow()&&card.isShown()
                &&card.getRootView()==activity.getWindow().getDecorView()&&session.owns(ticket);
    }
    boolean submit(Runnable mutation){if(!current())return false;mutation.run();return true;}
}
