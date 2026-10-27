package org.sleepwell.app;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.Toast;

/** Authorization-review touch boundary, including partial occlusion on API29+.
 * https://developer.android.com/privacy-and-security/risks/tapjacking
 * Does not disable screenshots or legitimate unobscured accessibility actions. */
final class SensitiveReviewTouches {
    interface Dispatch {boolean send(MotionEvent event);}
    private boolean blocked;
    boolean dispatch(Context context,MotionEvent event,Dispatch normal){
        if(event.getActionMasked()==MotionEvent.ACTION_DOWN)blocked=false;
        int obscured=MotionEvent.FLAG_WINDOW_IS_OBSCURED|MotionEvent.FLAG_WINDOW_IS_PARTIALLY_OBSCURED;
        if((event.getFlags()&obscured)!=0&&!blocked){
            blocked=true;
            // A control may already have received DOWN before an overlay appeared.
            // Cancel its pressed/gesture state; never deliver a later clean UP as consent.
            MotionEvent cancel=MotionEvent.obtain(event.getDownTime(),event.getEventTime(),MotionEvent.ACTION_CANCEL,event.getX(),event.getY(),event.getMetaState());
            try{normal.send(cancel);}finally{cancel.recycle();}
            Toast.makeText(context,"Close other app overlays, then try again.",Toast.LENGTH_SHORT).show();
        }
        return !blocked&&normal.send(event);
    }
}
