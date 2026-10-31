package org.sleepwell.app;

import android.app.Activity;
import android.app.Dialog;
import android.os.*;
import android.view.*;
import android.widget.*;

/** Owned review state; rendering/restoring never submits an erase operation. */
final class HistoryEraseUi {
    static final class Controller {
        private static final int CLOSED=0,HISTORY=1,REVIEW=2;
        private final Activity activity;final Records.Factory factory;
        private final Handler main=new Handler(Looper.getMainLooper());
        private Runnable stopPreview=()->{},refresh=()->{},changing=()->{};
        private int stage;private boolean cleanup,active,closed;private String reviewedJob="",notice="";
        private long generation;private Dialog window;private Bundle restoredWindow;
        Controller(Activity activity,Bundle state){this.activity=activity;factory=Records.factory;if(state!=null){stage=state.getInt("stage",CLOSED);if(stage<HISTORY||stage>REVIEW)stage=CLOSED;cleanup=state.getBoolean("cleanup");reviewedJob=state.getString("reviewedJob","");restoredWindow=state.getBundle("window");}}
        Bundle save(){Bundle state=new Bundle();state.putInt("stage",stage);state.putBoolean("cleanup",cleanup);state.putString("reviewedJob",reviewedJob);if(window!=null&&window.isShowing())state.putBundle("window",window.onSaveInstanceState());else if(restoredWindow!=null)state.putBundle("window",restoredWindow);return state;}
        void callbacks(Runnable stopPreview,Runnable refresh,Runnable changing){this.stopPreview=stopPreview;this.refresh=refresh;this.changing=changing;}
        void attach(){if(closed||active)return;active=true;if(stage==REVIEW&&!sameReview()){stage=HISTORY;reviewedJob="";restoredWindow=null;notice="History cleanup changed. Review the current action again.";}if(stage!=REVIEW)HistoryErase.inspect(activity,factory);main.post(this::restore);}
        void detach(){active=false;main.removeCallbacksAndMessages(null);if(window!=null&&window.isShowing())restoredWindow=window.onSaveInstanceState();dismissWindow();}
        void leave(){stage=CLOSED;reviewedJob="";restoredWindow=null;notice="";detach();stopPreview=refresh=changing=()->{};}
        void close(){closed=true;detach();stopPreview=refresh=changing=()->{};}
        void open(){if(!active||closed)return;stage=HISTORY;reviewedJob="";restoredWindow=null;dismissWindow();restore();}
        private void dismissWindow(){generation++;if(window!=null){window.setOnDismissListener(null);window.dismiss();window=null;}}
        private boolean sameReview(){HistoryEraseJobs.Job job=HistoryErase.jobs.get(factory);return job!=null&&job.done&&job.identity.equals(reviewedJob)&&(job.pending>0)==cleanup;}
        void restore(){if(!active||closed||stage==CLOSED||activity.isFinishing()||activity.isDestroyed()||window!=null&&window.isShowing()||!activity.hasWindowFocus())return;if(stage==REVIEW&&!sameReview()){stage=HISTORY;reviewedJob="";restoredWindow=null;notice="History cleanup changed. Review the current action again.";}if(stage==REVIEW)confirmation();else management();}
        private void management(){
            Dialog dialog=new Dialog(activity);window=dialog;long ticket=++generation;dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            LinearLayout content=Design.col(activity);Design.pad(content,22,22);content.setBackground(Design.shape(activity,NightDialTokens.RAISED,24));Design.text(content,"Sleep history",22,600,NightDialTokens.TEXT);Design.space(content,12);
            if(!notice.isEmpty()){Design.text(content,notice,14,400,NightDialTokens.SECONDARY);Design.space(content,12);}
            new Binding(content,this);Design.button(content,"Close history options",false,dialog::dismiss).setContentDescription("Close history options");
            ScrollView scroll=new ScrollView(activity);scroll.addView(content);dialog.setContentView(scroll);
            dialog.setOnDismissListener(d->{if(ticket!=generation||window!=dialog)return;window=null;stage=CLOSED;reviewedJob="";restoredWindow=null;refresh.run();});
            SleepPicker.show(activity,dialog,560);restoreWindow(dialog);
        }
        private void review(HistoryEraseJobs.Job job){if(!active||closed||job==null||!job.done||HistoryErase.jobs.get(factory)!=job)return;cleanup=job.pending>0;reviewedJob=job.identity;stage=REVIEW;notice="";restoredWindow=null;dismissWindow();restore();main.post(this::restore);}
        private void confirmation(){
            long ticket=++generation;Dialog[] owned={null};Dialog dialog=HistoryEraseConfirm.create(activity,cleanup,()->{
                if(!active||closed||ticket!=generation||window!=owned[0]||!owned[0].isShowing()||stage!=REVIEW)return;
                HistoryEraseJobs.Job reviewed=HistoryErase.jobs.get(factory);stage=HISTORY;restoredWindow=null;
                if(!sameReview()){notice="History cleanup changed. Review the current action again.";reviewedJob="";return;}
                try{stopPreview.run();HistoryErase.startReviewed(activity,factory,cleanup?HistoryEraseJobs.Kind.CLEAN:HistoryEraseJobs.Kind.ERASE,reviewed);changing.run();notice="";}
                catch(RuntimeException changed){notice="History cleanup changed or could not start. Review and try again.";main.post(refresh);}
                reviewedJob="";
            });owned[0]=dialog;window=dialog;
            dialog.setOnDismissListener(d->{if(ticket!=generation||window!=dialog)return;window=null;stage=HISTORY;reviewedJob="";restoredWindow=null;main.post(this::restore);});dialog.show();restoreWindow(dialog);
        }
        private void restoreWindow(Dialog dialog){Bundle state=restoredWindow;restoredWindow=null;if(state!=null)dialog.onRestoreInstanceState(state);}
    }
    private static final class Binding implements View.OnAttachStateChangeListener,Runnable {
        private final LinearLayout row;private final TextView status;private final Controller owner;
        private final Handler handler=new Handler(Looper.getMainLooper());private boolean attached;
        Binding(LinearLayout body,Controller owner){this.owner=owner;
            row=PlanActivity.detail(body,"trash","Delete sleep history","Permanently removes records, notes and audio.",null,()->owner.review(HistoryErase.jobs.get(owner.factory)));((Design.Icon)row.getChildAt(0)).tint(NightDialTokens.NEGATIVE);LinearLayout copy=(LinearLayout)row.getChildAt(1);((TextView)copy.getChildAt(0)).setTextColor(NightDialTokens.NEGATIVE);row.setContentDescription("Delete sleep history");
            status=Design.text(body,"",12,400,Design.MUTED);status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);status.setVisibility(View.GONE);row.addOnAttachStateChangeListener(this);if(row.isAttachedToWindow())onViewAttachedToWindow(row);
        }
        @Override public void onViewAttachedToWindow(View view){if(attached)return;attached=true;run();}
        @Override public void onViewDetachedFromWindow(View view){attached=false;handler.removeCallbacks(this);}
        @Override public void run(){handler.removeCallbacks(this);if(!attached||!row.isAttachedToWindow())return;HistoryEraseJobs.Job job=HistoryErase.jobs.get(owner.factory);if(job==null){handler.postDelayed(this,75);return;}
            boolean busy=!job.done;boolean cleanup=job.pending>0&&(job.done||job.kind==HistoryEraseJobs.Kind.CLEAN);String action=cleanup?"Retry audio cleanup":"Delete sleep history";row.setContentDescription(action);LinearLayout copy=(LinearLayout)row.getChildAt(1);((TextView)copy.getChildAt(0)).setText(action);((TextView)copy.getChildAt(2)).setText(cleanup?"Only removes audio already queued for cleanup.":"Permanently removes records, notes and audio.");row.setEnabled(!busy);String message;
            if(busy)message=job.kind==HistoryEraseJobs.Kind.INSPECT?"Checking audio cleanup…":job.kind==HistoryEraseJobs.Kind.CLEAN?"Removing pending audio…":"Deleting history…";
            else if(!job.error.isEmpty())message="History cleanup needs attention: "+job.error;
            else if(job.pending>0)message=(job.erased?"History removed. ":"")+job.pending+(job.pending==1?" audio file still needs cleanup.":" audio files still need cleanup.");
            else message=job.erased?"History cleanup finished. Alarms remain.":"";
            status.setText(message);status.setVisibility(message.isEmpty()?View.GONE:View.VISIBLE);
            if(!busy&&job.refreshNeeded){job.refreshNeeded=false;owner.refresh.run();return;}
            if(busy)handler.postDelayed(this,150);
        }
    }
}
