package org.sleepwell.app;

import android.app.*;
import android.graphics.Color;
import android.os.*;
import android.view.*;
import android.widget.*;

/** Board 12's local-storage overview; existing reviewed operations keep their own owners. */
final class PrivacyScreen {
    interface Actions{
        void back();void retention();void backup();void exportBackup();void importBackup();void csv();
        void cloud();void exportAudio();void restoreAudio();void research();void connections();void stopPreview();void refresh();
    }
    static void render(Activity activity,LinearLayout body,Actions actions){new Binding(activity,body,actions);}
    private static final class Binding implements View.OnAttachStateChangeListener,ViewTreeObserver.OnWindowFocusChangeListener,Runnable{
        final Activity activity;final LinearLayout body;final Actions actions;final Handler main=new Handler(Looper.getMainLooper());
        final BackupJob backup;final TextView backupStatus;final PrivacyStorageSummary summary;private BackupExportReceipt.Read receipt;private BackupJob.State shown;private boolean attached;private Dialog options;
        Binding(Activity activity,LinearLayout body,Actions actions){
            this.activity=activity;this.body=body;this.actions=actions;Records.Factory records=Records.factory;backup=BackupJobs.get(activity,records);
            LinearLayout navigation=Design.row(activity);navigation.addView(Design.iconButton(activity,"back","Back",actions::back),new LinearLayout.LayoutParams(Design.dp(activity,48),Design.dp(activity,48)));navigation.addView(new View(activity),new LinearLayout.LayoutParams(0,1,1));navigation.addView(Design.iconButton(activity,"more","Privacy options",this::options),new LinearLayout.LayoutParams(Design.dp(activity,48),Design.dp(activity,48)));body.addView(navigation);
            Design.space(body,10);Design.text(body,"Data & privacy",30,700,Design.INK);Design.space(body,6);Design.text(body,"Stored on this phone. Sharing is optional.",14,400,Design.MUTED);Design.space(body,18);
            summary=new PrivacyStorageSummary(body,records,BackupJobs.receipts(activity,records),read->{receipt=read;showBackupStatus();});Design.space(body,6);
            row(body,"file","Audio retention","Automatically remove older recordings.",retentionLabel(activity),actions::retention);Design.line(body);
            favorites(body);Design.line(body);
            Row backupRow=row(body,"upload","Backup","Last export: Loading…",null,actions::backup);backupStatus=backupRow.subtitle;backupStatus.setContentDescription("Backup export status");
            LinearLayout pair=Design.row(activity);button(pair,"upload","Export backup",true,actions::exportBackup);button(pair,"download","Import backup",false,actions::importBackup);body.addView(pair);Design.space(body,12);Design.line(body);
            row(body,"cloud","Cloud backup",cloudStatus(activity),null,actions::cloud);
            Button audio=Design.button(body,"Export audio library",false,actions::exportAudio);audio.setMinHeight(Design.dp(activity,48));audio.setMinimumHeight(Design.dp(activity,48));audio.setCompoundDrawablesRelativeWithIntrinsicBounds(Icons.resource("upload"),0,0,0);audio.setCompoundDrawablePadding(Design.dp(activity,8));tintButtonIcon(audio,false);LinearLayout.LayoutParams audioLayout=(LinearLayout.LayoutParams)audio.getLayoutParams();audioLayout.topMargin=0;audio.setLayoutParams(audioLayout);
            Design.space(body,12);Design.line(body);
            AudioClearUi.add(body,actions::stopPreview,actions::refresh);
            body.addOnAttachStateChangeListener(this);if(body.isAttachedToWindow())onViewAttachedToWindow(body);
        }
        @Override public void onViewAttachedToWindow(View view){if(attached)return;attached=true;HistoryErase.inspect(activity,Records.factory);body.getViewTreeObserver().addOnWindowFocusChangeListener(this);run();}
        @Override public void onViewDetachedFromWindow(View view){attached=false;main.removeCallbacks(this);if(body.getViewTreeObserver().isAlive())body.getViewTreeObserver().removeOnWindowFocusChangeListener(this);if(options!=null){options.setOnDismissListener(null);options.dismiss();options=null;}}
        @Override public void onWindowFocusChanged(boolean focused){main.removeCallbacks(this);if(focused&&attached){summary.refreshReceipt();run();}}
        @Override public void run(){main.removeCallbacks(this);if(!attached)return;BackupJob.State state=backup.state();if(shown!=state){shown=state;summary.refreshReceipt();showBackupStatus();}if(activity.hasWindowFocus())main.postDelayed(this,250);}
        private void showBackupStatus(){
            BackupJob.State state=backup.state();String text;
            if(state.phase==BackupJob.Phase.DONE&&state.receiptStatus==BackupJob.ReceiptStatus.UNAVAILABLE)text=state.receiptWarning;
            else if(state.phase==BackupJob.Phase.DONE&&state.receiptStatus==BackupJob.ReceiptStatus.PENDING)text=backupStatus(state)+" · recording export receipt…";
            else if(state.phase!=BackupJob.Phase.IDLE&&!(state.phase==BackupJob.Phase.DONE&&state.kind==BackupJob.Kind.JSON&&state.receiptStatus==BackupJob.ReceiptStatus.SAVED))text=backupStatus(state);
            else if(receipt==null||state.phase==BackupJob.Phase.DONE&&state.receiptStatus==BackupJob.ReceiptStatus.SAVED&&receipt.status==BackupExportReceipt.Status.NOT_RECORDED)text="Last export: Loading…";
            else text=switch(receipt.status){case NOT_RECORDED->"Last export: No saved export date";case PENDING->"Last export: Recording receipt…";case UNAVAILABLE->"Last export date unavailable · reopen to retry";case RECORDED->"Last export: "+receipt.format(java.time.ZoneId.systemDefault(),java.util.Locale.getDefault());};
            backupStatus.setText(text);
        }
        private void options(){
            if(!attached||options!=null&&options.isShowing())return;
            String[] entries={"Backup contents","Export records as CSV","Restore audio backup","Export research signals","Connected app access","Delete sleep history"};
            options=SleepPicker.choices(activity,"Privacy options",entries,-1,choice->{options=null;switch(choice){case 0:options=SleepPicker.message(activity,"Backup contents","JSON includes alarms, sleep records, notes and sound labels. Audio, app settings and goal commitments are excluded. Imported alarms start off. Audio-library ZIPs include the saved WAV recordings; every restore requires review.");break;case 1:actions.csv();break;case 2:actions.restoreAudio();break;case 3:actions.research();break;case 4:actions.connections();break;default:options=historyDialog(activity,actions);options.setOnDismissListener(d->{options=null;if(attached)actions.refresh();});}});
        }
    }
    private static Dialog historyDialog(Activity activity,Actions actions){
        Dialog dialog=new Dialog(activity);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);LinearLayout content=Design.col(activity);Design.pad(content,22,22);content.setBackground(Design.shape(activity,Design.CREAM,24));Design.text(content,"Sleep history",22,600,Design.INK);Design.space(content,12);HistoryEraseUi.add(content,actions::stopPreview,()->{});Design.button(content,"Close history options",false,dialog::dismiss);ScrollView scroll=new ScrollView(activity);scroll.addView(content);dialog.setContentView(scroll);dialog.show();dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));dialog.getWindow().setLayout(Math.min(activity.getResources().getDisplayMetrics().widthPixels-Design.dp(activity,32),Design.dp(activity,420)),WindowManager.LayoutParams.WRAP_CONTENT);return dialog;
    }
    private static String backupStatus(BackupJob.State state){
        return switch(state.phase){case IDLE->"Manual JSON · audio and settings excluded";case CHOOSING->"Document selection in progress";case READING->"Reading backup…";case WRITING->"Saving document…";case REVIEW->"Backup ready for review";case RESTORING->"Replacing local data…";case ERROR->"Backup needs attention · open to review";case CANCELLED->"Selection or operation cancelled";case DONE->state.kind==BackupJob.Kind.IMPORT?"Restore finished · review its result":state.kind==BackupJob.Kind.CSV?"CSV export finished this session":"JSON export finished this session";};
    }
    private static String cloudStatus(Activity activity){try{CloudBackupSettings.Destination destination=CloudBackupSettings.destination(CloudBackupSettings.factory.preferences(activity));return destination.tree.isEmpty()?"Not connected":"Remembered folder · access checked on open";}catch(RuntimeException unavailable){return "Saved destination unavailable · open to review";}}
    private static String retentionLabel(Activity activity){int days=Prefs.number(activity,"audioRetentionDays",7);return days==0?"Keep all":days+(days==1?" day":" days");}
    private static void favorites(LinearLayout body){
        Activity activity=(Activity)body.getContext();LinearLayout row=Design.row(activity);row.setMinimumHeight(Design.dp(activity,74));icon(row,"star");LinearLayout copy=Design.col(activity);Design.text(copy,"Keep favorites",16,500,Design.INK);Design.space(copy,4);Design.text(copy,"Keep starred audio beyond retention.",12,400,Design.MUTED);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        InteractionFeedback.Toggle toggle=new InteractionFeedback.Toggle(activity);toggle.setContentDescription("Keep favorites");Design.styleSwitch(activity,toggle,Design.VIOLET,0xffbcb5b0);toggle.setChecked(Prefs.of(activity).getBoolean("keepAudioFavorites",true));toggle.setOnCheckedChangeListener((button,keep)->{toggle.userChanged();Prefs.put(activity,"keepAudioFavorites",keep);});LinearLayout.LayoutParams control=new LinearLayout.LayoutParams(-2,-2);control.leftMargin=Design.dp(activity,10);row.addView(toggle,control);body.addView(row);
    }
    private static void icon(LinearLayout row,String name){Design.Icon icon=new Design.Icon(row.getContext(),name,Design.BROWN);icon.setPadding(Design.dp(row.getContext(),7),Design.dp(row.getContext(),7),Design.dp(row.getContext(),7),Design.dp(row.getContext(),7));icon.setBackground(Design.shape(row.getContext(),Design.SOFT,12));LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(Design.dp(row.getContext(),40),Design.dp(row.getContext(),40));size.rightMargin=Design.dp(row.getContext(),16);row.addView(icon,size);}
    private static final class Row{final TextView subtitle;Row(TextView subtitle){this.subtitle=subtitle;}}
    private static Row row(LinearLayout body,String glyph,String title,String subtitle,String value,Runnable action){
        LinearLayout row=Design.row(body.getContext());row.setMinimumHeight(Design.dp(body.getContext(),68));icon(row,glyph);LinearLayout copy=Design.col(body.getContext());Design.text(copy,title,16,500,Design.INK);Design.space(copy,4);TextView detail=Design.text(copy,subtitle,12,400,Design.MUTED);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        if(value!=null){TextView current=Design.text(body.getContext(),value,14,500,Design.INK);LinearLayout.LayoutParams currentSize=new LinearLayout.LayoutParams(-2,-2);currentSize.leftMargin=Design.dp(body.getContext(),10);row.addView(current,currentSize);}
        Design.Icon chevron=new Design.Icon(body.getContext(),"chevron",Design.MUTED);LinearLayout.LayoutParams arrow=new LinearLayout.LayoutParams(Design.dp(body.getContext(),18),Design.dp(body.getContext(),18));arrow.leftMargin=Design.dp(body.getContext(),12);row.addView(chevron,arrow);row.setContentDescription(title);row.setBackground(Design.touch(body.getContext(),Color.TRANSPARENT,10));row.setOnClickListener(view->{InteractionFeedback.action(view);action.run();});body.addView(row);return new Row(detail);
    }
    private static void button(LinearLayout pair,String icon,String title,boolean primary,Runnable action){LinearLayout column=Design.col(pair.getContext());LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(0,-2,1);if(pair.getChildCount()==0)size.rightMargin=Design.dp(pair.getContext(),4);else size.leftMargin=Design.dp(pair.getContext(),4);pair.addView(column,size);Button button=Design.button(column,title,primary,action);button.setTextSize(13*Appearance.scale(pair.getContext()));button.setMinHeight(Design.dp(pair.getContext(),48));button.setMinimumHeight(Design.dp(pair.getContext(),48));button.setPadding(Design.dp(pair.getContext(),10),Design.dp(pair.getContext(),8),Design.dp(pair.getContext(),10),Design.dp(pair.getContext(),8));button.setCompoundDrawablesRelativeWithIntrinsicBounds(Icons.resource(icon),0,0,0);button.setCompoundDrawablePadding(Design.dp(pair.getContext(),6));tintButtonIcon(button,primary);}
    private static void tintButtonIcon(Button button,boolean primary){android.graphics.drawable.Drawable icon=button.getCompoundDrawablesRelative()[0];if(icon!=null){icon=icon.mutate();icon.setTint(Appearance.color(button.getContext(),primary?Design.CREAM:Design.INK));icon.setBounds(0,0,Design.dp(button.getContext(),20),Design.dp(button.getContext(),20));button.setCompoundDrawablesRelative(icon,null,null,null);
        // Native Button keeps its label and accessibility semantics. Symmetric measured
        // padding makes the compound icon and actual label one centered group.
        button.addOnLayoutChangeListener((view,left,top,right,bottom,oldLeft,oldTop,oldRight,oldBottom)->{CharSequence label=button.getText();if(button.getTransformationMethod()!=null)label=button.getTransformationMethod().getTransformation(label,button);int text=(int)Math.ceil(android.text.Layout.getDesiredWidth(label,button.getPaint()));android.graphics.drawable.Drawable glyph=button.getCompoundDrawablesRelative()[0];if(glyph==null)return;int group=text+glyph.getBounds().width()+button.getCompoundDrawablePadding();int padding=Math.max(Design.dp(button.getContext(),10),(button.getWidth()-group)/2);if(button.getPaddingLeft()!=padding||button.getPaddingRight()!=padding)button.setPadding(padding,button.getPaddingTop(),padding,button.getPaddingBottom());});
    }}
}
