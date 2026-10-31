package org.sleepwell.app;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;

import android.app.*;
import android.content.Intent;
import android.os.*;
import android.widget.*;
import org.json.*;
import java.util.*;

/** Edits a portable pool; Use returns a draft. Only the parent alarm Save persists it. */
public final class RandomChallengeActivity extends ComponentActivity {
    static final int REQUEST=835;
    private final List<ChallengeRoutine.Task> pool=new ArrayList<>(),templates=new ArrayList<>();
    private String request,error="";private int count=1,editing=-1;private Dialog picker;
    static Intent edit(Activity owner,ChallengeRoutine routine,String nonce){return RecordUi.intent(owner,RandomChallengeActivity.class).putExtra("routine",ChallengeRoutineJson.encode(routine).toString()).putExtra("request",nonce);}
    @Override public void onCreate(Bundle state){super.onCreate(state);RecordUi.review(this);try{
        request=getIntent().getStringExtra("request");if(request==null||!UUID.fromString(request).toString().equals(request))throw new IllegalArgumentException();
        ChallengeRoutine source=ChallengeRoutineJson.decode(new JSONArray(getIntent().getStringExtra("routine")));templates.addAll(source.tasks);
        if(state!=null){ChallengeRoutine restored=ChallengeRoutineJson.decode(new JSONArray(state.getString("draft")));pool.addAll(restored.tasks);count=restored.randomCount;editing=state.getInt("editing",-1);error=state.getString("error","");}
        else{for(ChallengeRoutine.Task task:source.tasks)if(RandomChallenges.supported(task.type))addInitial(task);if(source.randomCount>0)count=source.randomCount;else{for(String type:List.of("Math","Memory","Sequence","Retype","Counting"))if(pool.size()<5)addInitial(new ChallengeRoutine.Task(type,1));count=Math.min(5,pool.size());}}
        getOnBackPressedDispatcher().addCallback(this,new OnBackPressedCallback(true){@Override public void handleOnBackPressed(){back();}});
        render();
    }catch(Exception invalid){finish();}}
    private static String key(ChallengeRoutine.Task task){return ChallengeRoutineJson.encode(new ChallengeRoutine(List.of(task))).toString();}
    private void addInitial(ChallengeRoutine.Task task){if(pool.size()<8&&pool.stream().noneMatch(t->key(t).equals(key(task))))pool.add(task);}
    private void back(){if(editing>=0){editing=-1;render();}else finish();}
    private void render(){if(editing>=pool.size())editing=-1;LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,editing<0?"Random tasks":pool.get(editing).type,this::back);Design.space(body,14);
        if(editing>=0){editTask(body);return;}
        Design.text(body,"A fresh selection at each alarm",22,600,Design.INK);Design.space(body,8);Design.text(body,"Choose distinct pool entries without replacement. Their saved settings stay intact. Snoozing or reopening keeps the current selection.",13,400,Design.MUTED);Design.space(body,12);
        PlanActivity.detail(body,"tasks","Tasks per occurrence",null,count+" of "+pool.size(),this::chooseCount);
        for(int i=0;i<pool.size();i++){int index=i;ChallengeRoutine.Task task=pool.get(i);String unavailable=RandomChallenges.unavailable(this,task);PlanActivity.detail(body,"tasks",(i+1)+". "+task.type,task.detail()+(unavailable.isEmpty()?"":"\nExcluded now: "+unavailable),"Edit",()->{editing=index;render();});}
        Design.space(body,12);Design.button(body,"Preview a selection",false,()->{if(valid())startActivity(RecordUi.intent(this,RandomChallengePreviewActivity.class).putExtra("routine",ChallengeRoutineJson.encode(draft()).toString()));});
        RecordUi.heading(body,"Add to pool");
        List<ChallengeRoutine.Task> available=new ArrayList<>();for(ChallengeRoutine.Task task:templates)if(available.stream().noneMatch(t->key(t).equals(key(task))))available.add(task);
        for(String type:ChallengeRoutine.TYPES){if(!RandomChallenges.supported(type)||RandomChallenges.needsTemplate(type))continue;ChallengeRoutine.Task task=new ChallengeRoutine.Task(type,1);if(available.stream().noneMatch(t->key(t).equals(key(task))))available.add(task);}
        for(ChallengeRoutine.Task task:available){boolean selected=pool.stream().anyMatch(t->key(t).equals(key(task))),supported=RandomChallenges.supported(task.type),room=pool.size()<8;String unavailable=RandomChallenges.unavailable(this,task);String reason=selected?"Already in pool":!supported?"This category is not supported in random mode yet.":!room?"Pool full · remove an entry first":"";if(!unavailable.isEmpty())reason+=(reason.isEmpty()?"":" · ")+"Excluded now: "+unavailable;ChallengeActionRows.add(body,ChallengeActionRows.icon(task.type),task.type,task.detail(),selected,supported&&room,reason,()->{if(pool.size()>=8){error="Use at most eight pool entries. Remove one first.";render();return;}if(!RandomChallenges.supported(task.type)){error="This category is not supported in random mode yet.";render();return;}if(pool.stream().anyMatch(t->key(t).equals(key(task))))return;pool.add(task);error="";render();});}
        for(String type:List.of("QR code","Picture match","Object recognition"))if(templates.stream().noneMatch(t->t.type.equals(type)))PlanActivity.detail(body,"tasks",type,"No configured template. Add this task to a fixed routine and save its code, reference or target first.","Not enrolled",null);
        Design.space(body,12);Design.text(body,"All current task categories are supported. Missing permissions, enrollment or observable prerequisites exclude an entry when the alarm starts; Math fills missing selections to preserve the count. No camera or microphone starts from this screen.",12,400,Design.MUTED);
        LinearLayout footer=RecordUi.footer(body);feedback(footer);Design.button(footer,"Use random routine",true,()->{if(!valid())return;setResult(RESULT_OK,new Intent().putExtra("request",request).putExtra("routine",ChallengeRoutineJson.encode(draft()).toString()));finish();});Design.button(footer,"Cancel",false,this::finish);
    }
    private ChallengeRoutine draft(){return ChallengeRoutine.random(pool,count);}
    private boolean valid(){try{RandomChallenges.decode(ChallengeRoutineJson.encode(draft()));error="";return true;}catch(Exception invalid){error=invalid.getMessage()==null?"Choose a valid pool and task count.":invalid.getMessage();render();return false;}}
    private void chooseCount(){if(pool.isEmpty())return;String[] values=new String[pool.size()];for(int i=0;i<values.length;i++)values[i]=(i+1)+(i==0?" task":" tasks");closePicker();picker=SleepPicker.choices(this,"Tasks per occurrence",values,count-1,n->{count=n+1;error="";render();});}
    private void editTask(LinearLayout body){ChallengeRoutine.Task task=pool.get(editing);Design.text(body,task.detail(),15,500,Design.BROWN);String unavailable=RandomChallenges.unavailable(this,task);if(!unavailable.isEmpty())Design.text(body,"Excluded now: "+unavailable+". Eligibility is rechecked when the alarm starts.",13,500,Design.BROWN);String readiness=RandomChallenges.readinessNote(task);if(!readiness.isEmpty()){Design.space(body,8);Design.text(body,readiness,12,400,Design.MUTED);}Design.space(body,18);
        if(task.type.equals("Math")){Design.text(body,"Complexity",15,600,Design.INK);RecordUi.segments(body,new String[]{"Easy","Medium","Hard"},task.math.level.ordinal(),n->replace(new ChallengeRoutine.Task("Math",task.rounds,0,new MathSettings(MathSettings.Level.values()[n],task.math.operators,task.math.typed))));Design.space(body,12);MathOperatorGrid.add(body,task.math,value->replace(new ChallengeRoutine.Task("Math",task.rounds,0,value)),()->{error="Keep at least one operator.";render();});RecordUi.segments(body,new String[]{"Choices","Type answer"},task.math.typed?1:0,n->replace(new ChallengeRoutine.Task("Math",task.rounds,0,new MathSettings(task.math.level,task.math.operators,n==1))));}
        if(task.type.equals("Walk"))choice(body,"Target steps",new int[]{5,10,15,20,30,50,75,100},task.targetSteps,n->new ChallengeRoutine.Task("Walk",1,n));
        else if(task.type.equals("Dream journal"))choice(body,"Minimum letters / digits",new int[]{40,80,120,160,240},task.journalMinimum,n->new ChallengeRoutine.Task("Dream journal",1,0,MathSettings.LEGACY,null,n));
        else if(task.type.equals("Light")||task.type.equals("Type code")||task.type.equals("Camera light")||task.type.equals("Musical note"))level(body,task);
        else if(task.type.equals(PhraseRule.TYPE))choice(body,"Phrase rounds",new int[]{1,2,3},task.rounds,n->new ChallengeRoutine.Task(task.type,n));
        else if(!List.of("QR code","NFC","Spin around","Picture match","Object recognition","Smile","Pose").contains(task.type)&&!AudioChallengeMode.supported(task.type))choice(body,task.type.equals("Shake")?"Shakes (sets of five)":"Rounds",new int[]{1,2,3,4,5,6},task.rounds,n->new ChallengeRoutine.Task(task.type,n,task.targetSteps,task.math));
        if(task.type.equals("QR code"))Design.text(body,"Location, code digest, format and Math fallback are copied from the enrolled task. Switch to a fixed routine to enroll a different code.",13,400,Design.MUTED);
        if(task.type.equals("Picture match"))Design.text(body,"The named visual reference and signature stay exactly as enrolled. Switch to a fixed routine to capture a different reference.",13,400,Design.MUTED);
        if(task.type.equals("Object recognition"))Design.text(body,"The explicitly chosen model target is retained. Switch to a fixed routine to change which object is required.",13,400,Design.MUTED);
        if(AudioChallengeMode.supported(task.type))Design.text(body,AudioChallengeMode.from(task.type).detail(),13,400,Design.MUTED);
        Design.space(body,14);Design.button(body,"Preview task",false,()->RandomChallengePreviewActivity.task(this,task));
        Design.button(body,"Remove from pool",false,()->{if(pool.size()==1){error="Keep at least one pool entry.";render();return;}pool.remove(editing);count=Math.min(count,pool.size());editing=-1;error="";render();});LinearLayout footer=RecordUi.footer(body);feedback(footer);Design.button(footer,"Back to pool",true,()->{editing=-1;render();});
    }
    private void replace(ChallengeRoutine.Task task){String encoded=key(task);for(int i=0;i<pool.size();i++)if(i!=editing&&key(pool.get(i)).equals(encoded)){error="That exact task configuration is already in the pool.";render();return;}pool.set(editing,task);error="";render();}
    private interface Make {ChallengeRoutine.Task value(int n);}
    private static String levelLabel(String type,int n){return switch(type){case "Light"->LightHold.target(n)+" lx";case "Camera light"->CameraLightHold.percent(n)+"% image brightness";case "Musical note"->"±"+NotePitch.tolerance(n)+" cents";default->TypeCodePuzzle.length(n)+" characters";};}
    private void level(LinearLayout body,ChallengeRoutine.Task task){String title=task.type.equals("Musical note")?"Pitch tolerance":task.type.equals("Type code")?"Code length":"Brightness target";PlanActivity.detail(body,"tasks",title,null,levelLabel(task.type,task.rounds),()->{String[] labels=new String[3];for(int i=0;i<3;i++)labels[i]=levelLabel(task.type,i+1);closePicker();picker=SleepPicker.choices(this,title,labels,task.rounds-1,n->replace(new ChallengeRoutine.Task(task.type,n+1)));});}
    private void choice(LinearLayout body,String title,int[] values,int current,Make make){PlanActivity.detail(body,"tasks",title,null,""+current,()->{String[] labels=new String[values.length];int selected=0;for(int i=0;i<values.length;i++){labels[i]=""+values[i];if(values[i]==current)selected=i;}closePicker();picker=SleepPicker.choices(this,title,labels,selected,n->replace(make.value(values[n])));});}
    private void feedback(LinearLayout footer){if(!error.isEmpty())Design.text(footer,error,13,500,NightDialTokens.NEGATIVE);Design.text(footer,"Draft only. Save alarm in the parent editor to apply it.",12,400,Design.MUTED);}
    private void closePicker(){if(picker!=null)picker.dismiss();picker=null;}
    @Override protected void onResume(){super.onResume();if(!pool.isEmpty())render();}
    @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);if(pool.isEmpty())return;state.putString("draft",ChallengeRoutineJson.encode(draft()).toString());state.putInt("editing",editing);state.putString("error",error);}
    @Override protected void onDestroy(){closePicker();super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)RecordUi.lightBars(this);}
}
