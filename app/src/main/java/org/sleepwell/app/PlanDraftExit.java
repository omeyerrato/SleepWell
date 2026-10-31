package org.sleepwell.app;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import java.util.function.BooleanSupplier;

/** View-only exit review for the local nap/plan/trip/goal editors. Never owns a save. */
final class PlanDraftExit {
    private final Activity owner;
    private final BooleanSupplier dirty;
    private final BooleanSupplier canSave;
    private final String title,message;
    private final Runnable save,discard;
    private Dialog dialog;
    private boolean requested,foreground,alive=true;

    PlanDraftExit(Activity owner,Bundle state,String title,String message,BooleanSupplier dirty,Runnable save,Runnable discard){
        this(owner,state,title,message,dirty,save,discard,()->true);
    }
    PlanDraftExit(Activity owner,Bundle state,String title,String message,BooleanSupplier dirty,Runnable save,Runnable discard,BooleanSupplier canSave){
        this.owner=owner;this.title=title;this.message=message;this.dirty=dirty;this.save=save;this.discard=discard;
        this.canSave=canSave;
        requested=state!=null&&state.getBoolean("planDraftExit");
    }
    void request(){
        if(!alive||!foreground||owner.isFinishing()||owner.isDestroyed())return;
        if(!dirty.getAsBoolean()){requested=false;discard.run();return;}
        requested=true;show();
    }
    void resume(){foreground=true;restore();}
    void pause(){foreground=false;}
    void restore(){if(requested&&alive&&foreground&&owner.hasWindowFocus()){if(dirty.getAsBoolean())show();else requested=false;}}
    void state(Bundle state){state.putBoolean("planDraftExit",requested);}
    View window(){return dialog!=null&&dialog.isShowing()?dialog.getWindow().getDecorView():null;}
    private void show(){
        if(!alive||!foreground||owner.isFinishing()||owner.isDestroyed()||dialog!=null&&dialog.isShowing())return;
        Dialog shown=new Dialog(owner);dialog=shown;shown.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout panel=Design.col(owner);Design.pad(panel,22,24);panel.setBackground(Design.shape(owner,NightDialTokens.RAISED,24));
        Design.text(panel,title,23,600,NightDialTokens.TEXT);Design.space(panel,16);Design.text(panel,message,15,400,NightDialTokens.SECONDARY);Design.space(panel,16);
        boolean offerSave=save!=null&&canSave.getAsBoolean();
        if(offerSave)Design.button(panel,"Save changes",true,()->act(shown,save));
        Design.button(panel,"Keep editing",!offerSave,shown::dismiss);
        Design.button(panel,"Discard changes",false,()->act(shown,discard));
        ScrollView scroll=new ScrollView(owner);scroll.addView(panel);shown.setContentView(scroll);
        shown.setOnDismissListener(ignored->{if(alive&&dialog==shown){requested=false;dialog=null;}});
        SleepPicker.show(owner,shown,430);
    }
    private void act(Dialog shown,Runnable action){
        if(!alive||!foreground||owner.isFinishing()||owner.isDestroyed()||dialog!=shown||!shown.isShowing()
                ||!shown.getWindow().getDecorView().hasWindowFocus())return;
        if(action==save&&!canSave.getAsBoolean())return;
        shown.dismiss();action.run();
    }
    void close(){alive=false;if(dialog!=null)dialog.dismiss();dialog=null;}
}
