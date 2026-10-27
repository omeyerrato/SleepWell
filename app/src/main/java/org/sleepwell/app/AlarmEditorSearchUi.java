package org.sleepwell.app;

import android.app.Activity;
import android.os.Bundle;
import android.text.*;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.util.*;
import java.util.function.Consumer;

/** A local catalog of this editor's real pages. Results never execute a setting action. */
final class AlarmEditorSearchUi {
    static final class State {
        String query="";int position;private ScrollView scroll;
        State(Bundle saved){if(saved!=null){String text=saved.getString("query","");query=text!=null&&text.length()<=160?text:"";position=Math.max(0,saved.getInt("position"));}}
        void remember(){if(scroll!=null&&scroll.isAttachedToWindow())position=scroll.getScrollY();}
        Bundle save(){remember();Bundle saved=new Bundle();saved.putString("query",query);saved.putInt("position",position);return saved;}
    }
    static final class Entry {
        final String title,route,anchor,path,terms;
        Entry(String title,String route,String anchor,String path,String terms){this.title=title;this.route=route;this.anchor=anchor;this.path="Edit alarm › "+path;this.terms=SearchIndex.normalize(title+" "+path+" "+terms);}
    }
    private static Entry item(String title,String route,String anchor,String path,String terms){return new Entry(title,route,anchor,path,terms);}
    private static final List<Entry> CATALOG=List.of(
        item("Alarm time & weekdays","edit","","Time and weekdays","clock hour minute morning day name label repeat"),
        item("Name","edit","Name","Name","label title nickname"),
        item("Alarm on","edit","Alarm on","Alarm on","enable disable enabled active off"),
        item("Smart wake-up","smart","","Smart wake-up","early wake window movement sleep tracking"),
        item("Movement sensitivity","smart","Movement sensitivity","Smart wake-up","motion low medium high calibration sensor"),
        item("Respect sleep goal","smart","Respect sleep goal","Smart wake-up","recorded duration wait goal minimum sleep"),
        item("Challenges","challenge","","Challenges","tasks math memory qr barcode nfc walk shake spin jumping smile pose picture object captcha routine"),
        item("Random challenge selection","challenge","Routine mode","Challenges › Routine mode","random tasks pool shuffle sequence"),
        item("Sound","sound","","Sound","tone melody ringtone daybreak pulse chime import file audio"),
        item("Alarm volume","sound","Alarm volume","Sound","loud quiet volume level"),
        item("Extra loud","sound","Alarm sound options","Sound › More","extra loud louder maximum boost prevent quiet"),
        item("Playback mode","soundmodes","Playback mode","Sound › Sound playback","playlist random songs radio internet stream shuffled sound"),
        item("Gentle wake-up","gentle","","Sound › Gentle wake-up","gradual gently increase ramp fade volume"),
        item("Volume ramp","gentle","Volume ramp","Sound › Gentle wake-up","gentle increase crescendo fade duration"),
        item("Sound delay","gentle","Sound delay","Sound › Gentle wake-up","wait silence seconds audio start"),
        item("Vibration","gentle","Vibration","Sound › Gentle wake-up","vibrate haptic delay start"),
        item("Screen sunrise","gentle","Screen sunrise","Sound › Gentle wake-up","sunrise brighten screen light dawn display"),
        item("Flashlight wake-up","gentle","Flashlight wake-up","Sound › Gentle wake-up","torch flash rear light sunrise wake"),
        item("Snooze","snooze","","Snooze","duration minutes sleep again"),
        item("Choose duration each time","snooze","Choose duration each time","Snooze","until clock exact snooze choose duration"),
        item("Maximum snoozes","snooze","Maximum snoozes","Snooze","limit count repeats"),
        item("Total snooze time","snooze","Total snooze time","Snooze","total minutes budget cap limit"),
        item("Shorten each snooze","snooze","Shorten each snooze","Snooze","decrease duration shorter"),
        item("Backup alarm","backup","Backup alarm","Snooze › Backup & wake-up check","escalate loud repeat backup bursts"),
        item("Wake-up check","backup","Wake-up check","Snooze › Backup & wake-up check","awake confirmation after dismissal missed"),
        item("Stop ringing after","backup","Stop ringing after","Snooze › Backup & wake-up check","timeout automatic stop ringing duration"),
        item("Schedule & dates","schedule","","Schedule & dates","calendar dates time basis"),
        item("Repeat rule","schedule","Repeat rule","Schedule & dates","weekly interval every days odd even iso week recurrence"),
        item("Skip selected dates","schedule","Skip selected dates","Schedule & dates","exclude exception holiday calendar skip"),
        item("Next alarm only","schedule","Next alarm only","Schedule & dates","reschedule move next occurrence date override"),
        item("Run on","schedule","Run on","Schedule & dates","one time fixed date calendar"),
        item("Relative time / speech","schedule","Relative time / speech","Schedule & dates","in minutes hours voice spoken time"),
        item("Use sleep plan","schedule","Use sleep plan","Schedule & dates","goal bedtime wake time copy plan"),
        item("Time basis","schedule","Time basis","Schedule & dates","sunrise astronomical solar latitude longitude clock"),
        item("Bedtime reminder","schedule","Bedtime reminder","Schedule & dates","wind down notification before bed"),
        item("More alarm settings","options","","More alarm settings","advanced options"),
        item("Alarm protection","options","Alarm protection","More alarm settings","prevent leave preventleave power off poweroff exit app pinning block protect shutdown"),
        item("New alarm defaults","options","New alarm defaults","More alarm settings","template default reuse copy behavior"),
        item("Buttons & orientation","controls","","More alarm settings › Buttons & orientation","input controls"),
        item("Lock alarm orientation","controls","Lock alarm orientation","More alarm settings › Buttons & orientation","rotation rotate screen landscape portrait"),
        item("Flip to snooze","controls","Flip to snooze","More alarm settings › Buttons & orientation","turn face down flip"),
        item("Volume keys","controls","Volume keys","More alarm settings › Buttons & orientation","buttons volume hardware ignore snooze")
    );
    private static final Set<String> BROWSE=Set.of("Alarm time & weekdays","Smart wake-up","Challenges","Sound","Gentle wake-up","Snooze","Schedule & dates","More alarm settings","Buttons & orientation");
    static boolean route(String value){return value!=null&&Set.of("edit","smart","challenge","sound","soundmodes","gentle","snooze","backup","schedule","options","controls").contains(value);}
    static void hideKeyboard(Activity activity){View focus=activity.getCurrentFocus();InputMethodManager keyboard=activity.getSystemService(InputMethodManager.class);if(focus!=null&&keyboard!=null)keyboard.hideSoftInputFromWindow(focus.getWindowToken(),0);}
    static void render(Activity activity,LinearLayout body,State state,Alarm alarm,Consumer<Entry> navigate,Runnable back){
        RecordUi.header(body,"Search alarm settings",back);Design.space(body,8);Design.text(body,"This draft only · Save alarm keeps your changes",12,400,Design.MUTED);Design.space(body,14);
        EditText input=new EditText(activity);input.setSingleLine();input.setTextSize(16*Appearance.scale(activity));input.setTypeface(Design.font(activity,400));input.setTextColor(Design.INK);input.setHintTextColor(Design.MUTED);input.setHint("Try flashlight, snooze or repeat");input.setContentDescription("Search alarm query");input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(160)});input.setImeOptions(EditorInfo.IME_ACTION_SEARCH);input.setBackground(Design.shape(activity,Design.SOFT,14));Design.pad(input,14,12);input.setText(state.query);body.addView(input,new LinearLayout.LayoutParams(-1,Design.dp(activity,56)));Design.space(body,12);
        TextView status=Design.text(body,"",13,500,Design.MUTED);status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);Design.space(body,8);LinearLayout results=Design.col(activity);body.addView(results);
        state.scroll=(ScrollView)body.getParent();ScrollView owner=state.scroll;
        Runnable update=()->{
            results.removeAllViews();String clean=SearchIndex.normalize(state.query);String[] words=clean.isEmpty()?new String[0]:clean.split(" ");int count=0;
            for(Entry entry:CATALOG){if(clean.isEmpty()&&!BROWSE.contains(entry.title))continue;boolean match=true;for(String word:words)if(!entry.terms.contains(word)){match=false;break;}if(!match)continue;count++;
                String detail=entry.path;String availability=availability(alarm,entry);if(!availability.isEmpty())detail+="\n"+availability;
                LinearLayout row=PlanActivity.detail(results,"settings",entry.title,detail,null,()->{state.remember();hideKeyboard(activity);navigate.accept(entry);});row.setContentDescription("Open alarm setting "+entry.title);Design.line(results);
            }
            status.setText(clean.isEmpty()?"Choose a setting or search by name.":count==0?"No matching alarm settings. Try another word.":count+(count==1?" setting":" settings"));
            if(count==0)RecordUi.quietButton(results,"Clear search",()->input.setText(""));
        };
        input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void afterTextChanged(Editable value){}public void onTextChanged(CharSequence value,int start,int before,int count){state.query=value.toString();state.position=0;update.run();owner.post(()->{if(owner.isAttachedToWindow())owner.scrollTo(0,0);});}});
        input.setOnEditorActionListener((view,action,event)->{if(action!=EditorInfo.IME_ACTION_SEARCH)return false;hideKeyboard(activity);return true;});
        update.run();int position=state.position;owner.post(()->{if(owner.isAttachedToWindow())owner.scrollTo(0,position);});body.setFocusableInTouchMode(true);body.requestFocus();
    }
    private static String availability(Alarm alarm,Entry entry){
        if(entry.title.equals("Extra loud"))return "Open Sound, then More · no sound starts";
        if(entry.title.equals("Alarm protection"))return "Opens all-alarm protection options";
        if(entry.title.equals("New alarm defaults"))return "Review this draft as a reusable template";
        if(entry.title.equals("Relative time / speech")&&!RelativeAlarmTime.allowed(alarm))return "One-time clock alarms only";
        if(entry.title.equals("Run on")&&(alarm.repeatInvalid||alarm.repeatRule.custom()))return "Choose Weekly / one-time first";
        if(entry.title.equals("Smart wake-up")&&alarm.smartWakeInvalid)return "Saved settings need review";
        if(entry.route.equals("sound")&&alarm.soundMode.radio())return "Current sound uses Internet radio";
        return "";
    }
    private AlarmEditorSearchUi(){}
}
