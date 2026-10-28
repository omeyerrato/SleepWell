package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

/** Global saved choices, separate from accessibility access and a real ringing alarm. */
public final class AlarmProtectionActivity extends Activity {
    private Dialog disclosure;
    private ScrollView informationScroll,mainScroll;
    private String information="",notice="";
    private int informationPosition,mainPosition;
    private boolean resumed,readFailed;
    private Snapshot shown;
    private record Snapshot(boolean leaving,boolean power,boolean access) {}

    static boolean access(Context context){String services=Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);ComponentName own=new ComponentName(context,BedtimeGuard.class);if(services!=null)for(String service:services.split(":"))if(own.equals(ComponentName.unflattenFromString(service)))return true;return false;}
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);RecordUi.review(this);
        if(saved!=null){String page=saved.getString("information","");if(page.equals("about")||page.equals("disclosure"))information=page;informationPosition=saved.getInt("informationPosition");mainPosition=saved.getInt("mainPosition");notice=saved.getString("notice","");}
    }
    private Snapshot read(){
        // Complete every read before adding either interactive choice. Failure never resets a key.
        boolean leaving=AlarmProtections.enabled(this,AlarmProtections.LEAVING);
        boolean power=AlarmProtections.enabled(this,AlarmProtections.POWER);
        boolean allowed=access(this);
        return new Snapshot(leaving,power,allowed);
    }
    private boolean visible(){return resumed&&hasWindowFocus()&&!isFinishing()&&!isDestroyed()&&disclosure==null;}
    private void render(){
        if(isFinishing()||isDestroyed())return;
        LinearLayout body=Design.page(this,false,null,n->{});mainScroll=(ScrollView)body.getParent();RecordUi.label(this,body);
        LinearLayout header=RecordUi.header(body,"Alarm protection",this::finish);RecordUi.headerAction(header,"info","About alarm protection",()->information("about"));
        Design.space(body,10);Design.text(body,"Applies to all alarms",18,600,Design.INK);Design.space(body,6);Design.text(body,"Selected controls apply only while a real alarm rings.",13,400,Design.MUTED);Design.space(body,16);
        shown=null;readFailed=false;
        try{shown=read();}catch(RuntimeException unavailable){readFailed=true;}
        if(readFailed){
            Design.text(body,"Protection settings could not be read.",18,600,Design.INK);Design.space(body,8);Design.text(body,"Your saved choices have not been reset. Retry to load both controls and Android access together.",14,400,Design.MUTED);
        }else{
            Snapshot state=shown;boolean selected=state.leaving||state.power;
            String status=!selected?"Off":!state.access?"Setup needed":"Access enabled";
            String detail=!selected?"Neither protection is selected. Accessibility access is "+(state.access?"enabled.":"off."):!state.access?"Choices saved. Selected controls cannot run without accessibility access.":"Selected controls are available during ringing alarms, subject to device limits.";
            PlanActivity.detail(body,"shield",status,detail,null,null);Design.space(body,14);
            protection(body,AlarmProtections.LEAVING,"Prevent leaving app","Return to the ringing challenge.",state.leaving);Design.space(body,10);
            protection(body,AlarmProtections.POWER,"Prevent power off","Dismiss recognised power menus. Best effort.",state.power);
            Design.space(body,16);PlanActivity.detail(body,"shield","Uninstall protection","Optional Android administrator step",null,()->{if(visible())try{startActivity(RecordUi.intent(this,UninstallProtectionActivity.class));}catch(RuntimeException blocked){notice="Uninstall protection could not open. Your saved choices are unchanged.";render();}});Design.line(body);
            Design.space(body,10);Design.text(body,"Forced hardware restart remains possible. About explains privacy and device limits.",12,400,Design.MUTED);
        }
        LinearLayout footer=RecordUi.footer(body);
        if(!notice.isEmpty()){Design.text(footer,notice,13,500,Design.BROWN);Design.space(footer,6);}
        if(readFailed)Design.button(footer,"Retry protection settings",true,()->{if(visible()){notice="";render();showInformation();}});
        else Design.button(footer,shown.access?"Review accessibility access":"Set up accessibility access",true,()->information("disclosure"));
        Design.button(footer,"Done",false,()->{if(visible())finish();});
        ScrollView owner=mainScroll;owner.post(()->{if(mainScroll==owner&&owner.isAttachedToWindow())owner.scrollTo(0,mainPosition);});
    }
    private void protection(LinearLayout body,String key,String title,String description,boolean selected){
        Switch[] choice={null};choice[0]=Design.toggle(body,title,description,selected,value->{
            Switch source=choice[0];if(!visible()||readFailed||source==null||!source.isAttachedToWindow()||source.getRootView()!=getWindow().getDecorView())return;
            rememberMain();
            try{read();AlarmProtections.set(this,key,value);notice="";render();if(value&&shown!=null&&!shown.access)information("disclosure");}
            catch(RuntimeException unavailable){notice="The change could not be confirmed. Review the saved choices before trying again.";render();}
        });
    }
    private void rememberMain(){if(mainScroll!=null)mainPosition=mainScroll.getScrollY();}
    private void information(String page){
        if(!visible())return;rememberMain();information=page;informationPosition=0;showInformation();
    }
    private boolean owns(Dialog dialog,String page){return disclosure==dialog&&information.equals(page)&&resumed&&!isFinishing()&&!isDestroyed()&&dialog.isShowing()&&dialog.getWindow().getDecorView().hasWindowFocus();}
    private void showInformation(){
        if(information.isEmpty()||disclosure!=null||!resumed||!hasWindowFocus()||isFinishing()||isDestroyed()||readFailed&&information.equals("disclosure"))return;
        String page=information;boolean about=page.equals("about");Dialog dialog=new Dialog(this);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);disclosure=dialog;
        LinearLayout panel=Design.col(this);Design.pad(panel,22,20);panel.setBackground(Design.shape(this,Design.CREAM,24));
        Design.text(panel,about?"About alarm protection":shown!=null&&shown.access?"Review optional guard access":"Enable the optional guard?",22,600,Design.INK);Design.space(panel,12);
        ScrollView scroll=new ScrollView(this);informationScroll=scroll;scroll.setContentDescription(about?"Alarm protection information":"Optional guard disclosure");LinearLayout copy=Design.col(this);scroll.addView(copy);panel.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        Design.text(copy,"Optional access",16,600,Design.INK);Design.space(copy,8);
        // Preserve the complete mandatory Android disclosure, with readable paragraph breaks.
        for(String sentence:getString(R.string.guard_description).split("(?<=\\.) ")){Design.text(copy,sentence,14,400,Design.MUTED);Design.space(copy,8);}
        Design.space(copy,6);Design.text(copy,"Device limits",16,600,Design.INK);Design.space(copy,8);Design.text(copy,"Power-menu protection is best effort. It cannot stop a forced hardware restart, work while the phone is off, or cover every manufacturer's menu.",14,400,Design.MUTED);Design.space(copy,12);
        boolean[] accepted={false};
        if(!about)Design.button(panel,"Open settings",true,()->{
            if(accepted[0]||!owns(dialog,page))return;accepted[0]=true;information="";dialog.dismiss();
            try{startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));}
            catch(RuntimeException blocked){notice="Android accessibility settings could not open. Find SleepWell under Accessibility in your phone’s Settings app.";render();}
        });
        Design.button(panel,about?"Done":"Later",about,()->{if(owns(dialog,page))dialog.dismiss();});
        dialog.setContentView(panel);dialog.setOnDismissListener(ignored->{if(disclosure==dialog){disclosure=null;informationScroll=null;information="";informationPosition=0;}});
        try{SleepPicker.show(this,dialog,620);scroll.post(()->{if(disclosure==dialog&&informationScroll==scroll)scroll.scrollTo(0,informationPosition);});}
        catch(RuntimeException blocked){if(disclosure==dialog){disclosure=null;informationScroll=null;information="";}notice="Protection information could not open. Your saved choices have not been reset.";render();}
    }
    @Override protected void onResume(){super.onResume();resumed=true;render();showInformation();}
    @Override protected void onPause(){resumed=false;rememberMain();super.onPause();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused){RecordUi.lightBars(this);showInformation();}}
    @Override public void onSaveInstanceState(Bundle saved){rememberMain();if(informationScroll!=null)informationPosition=informationScroll.getScrollY();super.onSaveInstanceState(saved);saved.putString("information",information);saved.putInt("informationPosition",informationPosition);saved.putInt("mainPosition",mainPosition);saved.putString("notice",notice);}
    @Override protected void onDestroy(){Dialog owned=disclosure;disclosure=null;if(owned!=null){owned.setOnDismissListener(null);owned.dismiss();}super.onDestroy();}
}
