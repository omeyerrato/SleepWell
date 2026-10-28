package org.sleepwell.app;

import android.app.Activity;
import android.widget.LinearLayout;
import java.util.List;
import java.util.function.IntConsumer;

/** Search navigation only. A row identifies an actual alarm; this UI never edits it. */
final class SearchAlarmChooser {
    static void render(Activity owner,LinearLayout body,SearchIndex.Entry feature,List<Alarm> alarms,
            boolean loading,String error,String defaultsNotice,Runnable back,Runnable reload,
            Runnable create,IntConsumer choose){
        RecordUi.header(body,"Choose an alarm",back);
        Design.space(body,8);
        Design.text(body,feature.title,20,600,Design.INK);
        Design.space(body,6);
        Design.text(body,"This alarm only · changes stay in its draft until Save alarm.",13,400,Design.MUTED);
        Design.space(body,16);
        if(loading){Design.text(body,"Opening saved alarms…",14,400,Design.MUTED);return;}
        if(!error.isEmpty()){
            Design.text(body,error,14,500,0xffa93919);
            Design.button(body,"Try again",false,reload);return;
        }
        Design.button(body,"New alarm draft",false,create);
        Design.text(body,"Starts from your new-alarm defaults. Nothing is saved or scheduled yet.",12,400,Design.MUTED);
        if(!defaultsNotice.isEmpty()){Design.space(body,6);Design.text(body,defaultsNotice,12,500,0xffa93919);}
        Design.space(body,20);
        Design.text(body,alarms.isEmpty()?"No saved alarms":"Saved alarms",14,600,Design.INK);
        if(alarms.isEmpty()){Design.space(body,8);Design.text(body,"Create a draft above to set "+feature.title.toLowerCase(java.util.Locale.ROOT)+".",14,400,Design.MUTED);return;}
        Design.space(body,8);
        for(Alarm alarm:alarms){
            String title=AlarmEditorActivity.displayTime(owner,alarm);
            if(!alarm.label.isBlank())title+=" · "+alarm.label;
            String summary=(alarm.enabled?"On":"Off")+" · "+days(alarm);
            LinearLayout row=PlanActivity.detail(body,"alarm",title,summary,null,()->choose.accept(alarm.id));
            row.setContentDescription("Choose alarm "+title+". "+summary);
            Design.line(body);
        }
    }
    static String identity(Activity owner,Alarm alarm,boolean fresh){
        String identity=(fresh?"New alarm draft":"Alarm draft")+" · "+AlarmEditorActivity.displayTime(owner,alarm);
        return alarm.label.isBlank()?identity:identity+" · "+alarm.label;
    }
    private static String days(Alarm alarm){
        if(alarm.dateInvalid)return "Saved date needs review";
        if(alarm.repeatInvalid)return "Saved repeat rule needs review";
        if(alarm.date!=null)return alarm.date.toString();
        if(alarm.repeatRule.custom())return alarm.repeatRule.label();
        if(alarm.days.isEmpty())return "One time";
        String[] names={"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};StringBuilder value=new StringBuilder();
        for(int day=1;day<=7;day++)if(alarm.days.contains(day)){if(value.length()>0)value.append(" ");value.append(names[day-1]);}
        return value.toString();
    }
    private SearchAlarmChooser(){}
}
