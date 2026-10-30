package org.sleepwell.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.function.Consumer;

/** One visible setup owns a draft and reader; only Save changes the existing shared tag. */
final class MorningTagSetup {
    static final String KEY="nfcTag";
    enum Availability {READY,OFF,UNSUPPORTED}
    interface Port {
        SharedPreferences preferences(Activity owner);
        Availability availability(Activity owner);
        NfcEnrollment enrollment(Activity owner,Consumer<String> captured,Consumer<String> notice);
        void settings(Activity owner);
    }
    static Port port=new Port(){
        public SharedPreferences preferences(Activity owner){return Prefs.of(owner);}
        public Availability availability(Activity owner){NfcAdapter adapter=NfcAdapter.getDefaultAdapter(owner);return adapter==null?Availability.UNSUPPORTED:adapter.isEnabled()?Availability.READY:Availability.OFF;}
        public NfcEnrollment enrollment(Activity owner,Consumer<String> captured,Consumer<String> notice){return NfcEnrollment.platform(owner,captured,notice);}
        public void settings(Activity owner){owner.startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));}
    };
    private final Activity owner;
    private final Port access;
    private final Runnable refresh;
    private String base="",saved="",captured="",error="";
    private boolean baseKnown,readFailed,unconfirmed,success,resumed,visible,settingsError;
    private Availability availability=Availability.UNSUPPORTED;
    private NfcEnrollment reader;
    private long attempt,viewGeneration;

    MorningTagSetup(Activity owner,Bundle state,Runnable refresh){
        this.owner=owner;this.access=port;this.refresh=refresh;
        if(state!=null){base=state.getString("base","");captured=state.getString("captured","");baseKnown=state.getBoolean("baseKnown");unconfirmed=state.getBoolean("unconfirmed");success=state.getBoolean("success");error=state.getString("error","");settingsError=state.getBoolean("settingsError");if(!captured.isEmpty()&&!valid(captured)){captured="";error="The captured tag could not be restored. Read it again.";}}
    }
    static String summary(Activity owner,Port access){
        try{String value=access.preferences(owner).getString(KEY,"");return value.isEmpty()?"No bedside tag saved":valid(value)?"Bedside tag saved":"Saved tag needs attention";}
        catch(RuntimeException unavailable){return "Saved tag unavailable";}
    }
    private static boolean valid(String value){return value!=null&&value.length()>=2&&value.length()%2==0&&value.matches("[0-9A-F]+");}
    void resume(){resumed=true;}
    void pause(){resumed=false;closeReader();viewGeneration++;}
    void visible(boolean value){if(visible!=value){visible=value;viewGeneration++;if(!value)closeReader();}}
    void close(){visible=false;pause();}
    Bundle save(){Bundle state=new Bundle();state.putString("base",base);state.putString("captured",captured);state.putBoolean("baseKnown",baseKnown);state.putBoolean("unconfirmed",unconfirmed);state.putBoolean("success",success);state.putString("error",error);state.putBoolean("settingsError",settingsError);return state;}
    private boolean load(){
        try{
            String current=access.preferences(owner).getString(KEY,"");
            Availability currentAccess=java.util.Objects.requireNonNull(access.availability(owner));
            if(!baseKnown||captured.isEmpty()&&!unconfirmed){if(baseKnown&&!base.equals(current))success=false;base=current;baseKnown=true;}
            saved=current;availability=currentAccess;readFailed=false;if(settingsError&&currentAccess==Availability.READY){settingsError=false;error="";}return true;
        }catch(RuntimeException unavailable){readFailed=true;return false;}
    }
    private boolean owned(View action,long generation){return generation==viewGeneration&&visible&&resumed&&!owner.isFinishing()&&!owner.isDestroyed()&&owner.hasWindowFocus()&&action.isAttachedToWindow()&&action.isShown()&&action.isEnabled();}
    private Button action(LinearLayout parent,String label,boolean primary,long generation,Runnable accepted){
        Button button=Design.button(parent,label,primary,()->{});
        button.setOnClickListener(view->{if(!owned(view,generation))return;InteractionFeedback.action(view);accepted.run();});return button;
    }
    void render(LinearLayout body,LinearLayout footer,String exitLabel,Runnable exit){
        visible(true);long generation=++viewGeneration;
        Design.text(body,"Shared by all NFC alarms",16,600,Design.INK);
        Design.space(body,7);Design.text(body,"Save one bedside tag for your NFC challenges. This does not add or change an alarm task.",13,400,Design.MUTED);Design.space(body,16);
        if(!load()){
            Design.text(body,"Tag setup is unavailable",20,600,Design.INK);Design.space(body,10);
            Design.text(body,"The saved tag or NFC state could not be read. Your draft is kept; retry before changing it.",14,400,Design.BROWN);
            action(body,"Retry tag setup",false,generation,()->{error="";refresh.run();});
            action(footer,exitLabel,true,generation,exit);return;
        }
        String stored=unconfirmed?"Last Save is unconfirmed":saved.isEmpty()?"No bedside tag saved":valid(saved)?"Bedside tag saved":"Saved tag needs attention";
        PlanActivity.detail(body,"phone","NFC access",stored,availability==Availability.READY?"On":availability==Availability.OFF?"Off":"Unavailable",null);
        if(availability!=Availability.READY)Design.text(body,availability==Availability.OFF?"Turn on NFC to read a tag.":"This phone has no available NFC reader.",12,400,Design.MUTED);Design.space(body,16);
        LinearLayout panel=Design.col(owner);panel.setGravity(Gravity.CENTER);Design.pad(panel,16,16);panel.setBackground(Design.shape(owner,Design.SOFT,20));panel.setMinimumHeight(Design.dp(owner,208));
        panel.addView(new Design.Icon(owner,"sonar",Design.BROWN),new LinearLayout.LayoutParams(Design.dp(owner,58),Design.dp(owner,58)));Design.space(panel,12);
        Design.text(panel,captured.isEmpty()?"Read a tag away from your bed":"Tag ready to save",14,500,Design.MUTED).setGravity(Gravity.CENTER);
        Button read=action(panel,availability==Availability.OFF?"Open NFC settings":"Read tag",false,generation,availability==Availability.OFF?this::settings:this::read);
        read.setContentDescription(availability==Availability.OFF?"Open NFC settings":"Read bedside NFC tag");read.setEnabled(availability!=Availability.UNSUPPORTED);read.setAlpha(read.isEnabled()?1:.45f);
        body.addView(panel,new LinearLayout.LayoutParams(-1,-2));Design.space(body,12);
        if(!captured.isEmpty()||success){TextView status=Design.text(body,!captured.isEmpty()?"Tag captured · not saved yet":"Tag saved for NFC alarms",14,600,0xff154b19);Design.pad(status,12,10);status.setBackground(Design.shape(owner,0xffe3efd4,12));status.setContentDescription("NFC tag draft status");}
        if(!error.isEmpty()){TextView problem=Design.text(footer,error,13,500,0xffa93919);problem.setContentDescription("NFC setup error");problem.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);}
        Button save=action(footer,"Save bedside tag",true,generation,this::commit);save.setEnabled(valid(captured));save.setAlpha(save.isEnabled()?1:.45f);
        if(exitLabel.equals("Done"))action(footer,exitLabel,false,generation,exit);
    }
    private void settings(){
        closeReader();settingsError=false;error="";try{access.settings(owner);}catch(RuntimeException unavailable){settingsError=true;error="NFC settings could not open. Your draft and saved tag were kept.";refresh.run();}
    }
    private void read(){
        if(!load()||availability!=Availability.READY){error="NFC is not ready. Check access and try again.";refresh.run();return;}
        closeReader();String reviewed=saved;error="";success=false;long ticket=attempt;
        NfcEnrollment[] accepted={null};
        try{
            accepted[0]=access.enrollment(owner,value->{
                if(ticket!=attempt||reader!=accepted[0]||!resumed||!visible||owner.isFinishing()||owner.isDestroyed())return;
                reader=null;attempt++;
                if(!valid(value)){error="The tag identifier could not be read. Try another tag.";refresh.run();return;}
                base=reviewed;baseKnown=true;captured=value;unconfirmed=false;error="";refresh.run();
            },notice->{
                if(ticket!=attempt||!resumed||!visible||owner.isFinishing()||owner.isDestroyed())return;
                closeReader();error=notice;refresh.run();
            });reader=accepted[0];reader.open();
        }catch(RuntimeException unavailable){closeReader();error="NFC reader could not start. Your draft and saved tag were kept.";refresh.run();}
    }
    private void commit(){
        closeReader();if(!valid(captured)){error="Read a tag before saving it.";refresh.run();return;}
        try{
            SharedPreferences preferences=access.preferences(owner);String current=preferences.getString(KEY,"");
            if(!baseKnown||!current.equals(base)){error="The saved bedside tag changed. Read your tag again before replacing it.";refresh.run();return;}
            boolean committed=current.equals(captured)&&!unconfirmed||preferences.edit().putString(KEY,captured).commit();
            if(!committed){base=preferences.getString(KEY,current);unconfirmed=true;error="Saving could not be confirmed. Keep this draft and retry Save bedside tag.";refresh.run();return;}
            saved=captured;base=saved;captured="";unconfirmed=false;success=true;error="";refresh.run();
        }catch(RuntimeException unavailable){error="The bedside tag could not be saved. Your captured draft is kept.";refresh.run();}
    }
    private void closeReader(){attempt++;NfcEnrollment owned=reader;reader=null;if(owned!=null)owned.close();}
}
