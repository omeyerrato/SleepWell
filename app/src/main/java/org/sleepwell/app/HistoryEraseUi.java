package org.sleepwell.app;

import android.app.Dialog;
import android.os.*;
import android.view.View;
import android.widget.*;

/** A privacy-row binding; detached views stop observing while the application worker continues. */
final class HistoryEraseUi {
    static void add(LinearLayout body,Runnable stopPreview,Runnable refresh){new Binding(body,stopPreview,refresh);}
    private static final class Binding implements View.OnAttachStateChangeListener,Runnable {
        private final LinearLayout body,row;private final TextView status;private final Records.Factory factory;
        private final Runnable stopPreview,refresh;private final Handler handler=new Handler(Looper.getMainLooper());
        private boolean confirming,attached;private Dialog dialog;
        Binding(LinearLayout body,Runnable stopPreview,Runnable refresh){this.body=body;this.stopPreview=stopPreview;this.refresh=refresh;factory=Records.factory;
            row=PlanActivity.detail(body,"trash","Delete sleep history","Permanently removes records, notes and audio.",null,this::confirm);((Design.Icon)row.getChildAt(0)).tint(0xffd22b20);LinearLayout copy=(LinearLayout)row.getChildAt(1);((TextView)copy.getChildAt(0)).setTextColor(0xffd22b20);row.setContentDescription("Delete sleep history");
            status=Design.text(body,"",12,400,Design.MUTED);status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);status.setVisibility(View.GONE);row.addOnAttachStateChangeListener(this);if(row.isAttachedToWindow())onViewAttachedToWindow(row);
        }
        @Override public void onViewAttachedToWindow(View view){if(attached)return;attached=true;HistoryErase.inspect(body.getContext(),factory);run();}
        @Override public void onViewDetachedFromWindow(View view){attached=false;handler.removeCallbacks(this);if(dialog!=null){dialog.setOnDismissListener(null);dialog.dismiss();dialog=null;}}
        @Override public void run(){handler.removeCallbacks(this);if(!attached||!row.isAttachedToWindow())return;HistoryEraseJobs.Job job=HistoryErase.jobs.get(factory);if(job==null){HistoryErase.inspect(body.getContext(),factory);handler.postDelayed(this,75);return;}
            boolean busy=!job.done;String action=job.done&&job.pending>0?"Retry audio cleanup":"Delete sleep history";row.setContentDescription(action);((TextView)((LinearLayout)row.getChildAt(1)).getChildAt(0)).setText(action);row.setEnabled(!busy&&!confirming);row.setAlpha(busy?.55f:1f);String message;
            if(busy)message=job.kind==HistoryEraseJobs.Kind.INSPECT?"Checking audio cleanup…":job.kind==HistoryEraseJobs.Kind.CLEAN?"Removing pending audio…":"Deleting history…";
            else if(!job.error.isEmpty())message="History cleanup needs attention: "+job.error;
            else if(job.pending>0)message=(job.erased?"History removed. ":"")+job.pending+(job.pending==1?" audio file still needs cleanup.":" audio files still need cleanup.");
            else message=job.erased?"History and its recordings were removed. Alarms remain.":"";
            status.setText(message);status.setVisibility(message.isEmpty()?View.GONE:View.VISIBLE);
            if(job.done&&job.refreshNeeded){job.refreshNeeded=false;refresh.run();return;}
            if(!job.done)handler.postDelayed(this,150);
        }
        private void confirm(){HistoryEraseJobs.Job job=HistoryErase.jobs.get(factory);if(confirming||job!=null&&!job.done)return;confirming=true;row.setEnabled(false);
            boolean cleanup=job!=null&&job.pending>0;dialog=HistoryEraseConfirm.create(body.getContext(),cleanup,()->{stopPreview.run();HistoryErase.start(body.getContext(),factory,cleanup?HistoryEraseJobs.Kind.CLEAN:HistoryEraseJobs.Kind.ERASE);handler.removeCallbacks(this);handler.post(this);});dialog.setOnDismissListener(d->{confirming=false;handler.post(this);});dialog.show();
        }
    }
}
