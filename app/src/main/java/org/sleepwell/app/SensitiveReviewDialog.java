package org.sleepwell.app;

import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;

/** Destructive consent belongs to this native window, including when partially obscured. */
final class SensitiveReviewDialog extends Dialog {
    private final SensitiveReviewTouches touches = new SensitiveReviewTouches();

    SensitiveReviewDialog(Context context) { super(context); }

    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        return touches.dispatch(getContext(), event, super::dispatchTouchEvent);
    }
}
