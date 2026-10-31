package org.sleepwell.app;

import android.app.Activity;
import android.os.*;
import android.text.InputType;
import android.widget.*;
import org.json.*;
import java.util.*;

/** Quiet selection/practice only. It never owns or advances an alarm occurrence. */
public final class RandomChallengePreviewActivity extends Activity {
    private ChallengeRoutine selected,configured;private String notice="";private long seed;private boolean resumed;private final Handler handler=new Handler(Looper.getMainLooper());private int practiceGeneration;
    private MathTask practiceMath;private Integer mathChoice;private String mathEntry="";private boolean mathDone;private EditText mathInput;
    static void task(Activity owner,ChallengeRoutine.Task task){owner.startActivity(RecordUi.intent(owner,RandomChallengePreviewActivity.class).putExtra("task",ChallengeRoutineJson.encode(new ChallengeRoutine(List.of(task))).toString()));}
    @Override public void onCreate(Bundle state){super.onCreate(state);RecordUi.review(this);try{
        seed=state==null?new java.security.SecureRandom().nextLong():state.getLong("seed");
        if(getIntent().hasExtra("task")){selected=ChallengeRoutineJson.decode(new JSONArray(getIntent().getStringExtra("task")));if(selected.randomCount>0||selected.tasks.size()!=1)throw new IllegalArgumentException();}
        else{configured=RandomChallenges.decode(new JSONArray(getIntent().getStringExtra("routine")));if(state==null)choose();else{selected=ChallengeRoutineJson.decode(new JSONArray(state.getString("selected")));notice=state.getString("notice","");if(selected.randomCount>0)throw new IllegalArgumentException();}}
        if(state!=null){if(state.containsKey("mathChoice"))mathChoice=state.getInt("mathChoice");mathEntry=state.getString("mathEntry","");mathDone=state.getBoolean("mathDone");String encoded=state.getString("practiceMath","");if(!encoded.isEmpty())try{practiceMath=MathTask.decode(encoded);}catch(RuntimeException invalid){practiceMath=null;mathChoice=null;mathDone=false;}}
        render();
    }catch(Exception invalid){finish();}}
    private void choose(){RandomChallenges.Selection result=RandomChallenges.select(this,configured);selected=result.routine;notice=result.notice;}
    private void render(){practiceGeneration++;handler.removeCallbacksAndMessages(null);if(configured==null&&selected.tasks.get(0).type.equals("Math")){math();return;}LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,configured==null?"Task preview":"Selection preview",this::finish);Design.space(body,14);
        if(configured!=null){Design.text(body,"Example selection",23,600,Design.INK);Design.text(body,"This does not change the next alarm. Its selection is made only when that occurrence is admitted.",13,400,Design.MUTED);Design.space(body,12);for(ChallengeRoutine.Task task:selected.tasks)PlanActivity.detail(body,"tasks",task.type,task.detail(),"Preview",()->task(this,task));Design.space(body,10);Design.text(body,notice,12,400,Design.MUTED);Design.button(body,"Try another selection",true,()->{choose();render();});return;}
        ChallengeRoutine.Task task=selected.tasks.get(0);Design.text(body,task.type,23,600,Design.INK);Design.text(body,task.detail(),14,500,Design.BROWN);Design.space(body,14);String unavailable=RandomChallenges.unavailable(this,task);if(!unavailable.isEmpty())Design.text(body,"Excluded now: "+unavailable,13,500,Design.BROWN);String readiness=RandomChallenges.readinessNote(task);if(!readiness.isEmpty())Design.text(body,readiness,12,400,Design.MUTED);
        if(task.type.equals("Memory"))memory(body);
        else if(task.type.equals("Sequence"))sequence(body);
        else if(task.type.equals("Retype")){String phrase="I am awake and ready for today";Design.text(body,phrase,22,600,Design.INK);answer(body,text->phrase.equals(text.trim()));}
        else if(List.of("Counting","Flags","Tilt maze","Jumping","Light","Type code","Spin around").contains(task.type))Design.button(body,"Open quiet practice",true,()->startActivity(RecordUi.intent(this,ChallengePackPreviewActivity.class).putExtra("type",task.type).putExtra("round",task.rounds-1)));
        else if(task.type.equals("Mirror text")||task.type.equals("Dream journal")){if(task.type.equals("Dream journal"))Design.text(body,"Explicit practice Save writes an entry in the private diary.",13,400,Design.MUTED);Design.button(body,"Open quiet practice",true,()->startActivity(RecordUi.intent(this,TypedWakePreviewActivity.class).putExtra("type",task.type).putExtra("minimum",task.journalMinimum)));}
        else if(task.type.equals("Visual search"))Design.button(body,"Open quiet practice",true,()->startActivity(RecordUi.intent(this,MoonSearchPreviewActivity.class).putExtra("round",task.rounds-1)));
        else if(AudioChallengeMode.supported(task.type))Design.button(body,"Open listening preview",true,()->startActivity(AudioChallengePreviewActivity.intent(this,task.type)));
        else if(task.type.equals(PhraseRule.TYPE))Design.button(body,"Open spoken phrase preview",true,()->startActivity(PhraseChallengeActivity.preview(this,task.rounds)));
        else if(task.type.equals("Musical note"))Design.button(body,"Open pitch preview",true,()->startActivity(NoteChallengeActivity.preview(this,task.rounds)));
        else if(task.type.equals("Smile"))Design.button(body,"Open smile preview",true,()->startActivity(SmileActivity.preview(this)));
        else if(task.type.equals("Pose"))Design.button(body,"Open pose preview",true,()->startActivity(PoseActivity.preview(this)));
        else if(task.type.equals("Camera light"))Design.button(body,"Open camera-light preview",true,()->startActivity(CameraLightActivity.preview(this,task.rounds)));
        else if(task.type.equals("Picture match"))Design.button(body,"Try saved picture reference",true,()->startActivity(PictureMatchActivity.enroll(this,task.picture,UUID.randomUUID().toString()).putExtra("mode","preview")));
        else if(task.type.equals("Object recognition")){Design.text(body,"Opens the saved target’s existing camera setup/practice. Changes there do not alter this pool.",13,400,Design.MUTED);Design.button(body,"Try saved object target",true,()->startActivity(ObjectRecognitionActivity.configure(this,task.objectTarget,UUID.randomUUID().toString())));}
        else Design.text(body,"Configuration preview only. This screen does not start step, shake, NFC or camera capture. At the actual alarm, the selected task keeps its existing Start, permission and Math-fallback behavior.",13,400,Design.MUTED);
        Design.space(body,16);Design.text(body,"Practice does not complete any current alarm task.",12,400,Design.MUTED);
    }
    private void math(){
        ChallengeRoutine.Task task=selected.tasks.get(0);if(practiceMath==null||!practiceMath.matches(task.math)){practiceMath=MathTask.create(new Random(seed),task.math);mathChoice=null;mathEntry="";mathDone=false;}
        if(mathInput!=null)mathEntry=mathInput.getText().toString();mathInput=null;
        java.time.LocalTime now=java.time.LocalTime.now();String clock=Design.clock(this,now.getHour()*60+now.getMinute());LinearLayout card=WakeChallengeUi.card(this,clock,"Task preview","Task 1 of "+selected.tasks.size(),0,selected.tasks.size(),this::finish);
        int generation=practiceGeneration;WakeMathCard.Binding[] binding={null};binding[0]=WakeMathCard.render(card,practiceMath,mathChoice,mathEntry,true,option->{if(!resumed||!hasWindowFocus()||generation!=practiceGeneration)return;mathChoice=option;mathDone=false;render();},()->{if(!resumed||!hasWindowFocus()||generation!=practiceGeneration)return;mathDone=true;binding[0].feedback.setText("Practice completed");});mathInput=binding[0].input;if(mathDone)binding[0].feedback.setText("Practice completed");Design.space(card,8);Design.text(card,"Practice does not complete any current alarm task.",11,400,Design.MUTED).setGravity(android.view.Gravity.CENTER);Design.button(card,"Done",false,this::finish);
    }
    private interface Accept {boolean check(String text);}
    private void answer(LinearLayout body,Accept check){EditText input=new EditText(this);input.setSingleLine(true);input.setContentDescription("Practice answer");input.setInputType(InputType.TYPE_CLASS_TEXT);input.setTypeface(Design.font(this,500));input.setTextColor(Design.INK);input.setBackground(Design.outline(this,Design.SOFT,Design.LINE,14));Design.pad(input,14,14);body.addView(input);TextView feedback=Design.text(body,"",14,500,Design.BROWN);int generation=practiceGeneration;Design.button(body,"Check practice answer",true,()->{if(!resumed||generation!=practiceGeneration)return;feedback.setText(check.check(input.getText().toString())?"Practice completed":"Try again");});}
    private void memory(LinearLayout body){List<String> values=new ArrayList<>(List.of("☀","☀","☽","☽","✿","✿"));Collections.shuffle(values,new Random(seed));GridLayout grid=new GridLayout(this);grid.setColumnCount(3);body.addView(grid);Button[] first={null};String[] value={null};int[] matched={0};boolean[] waiting={false};TextView feedback=Design.text(body,"Match all three pairs",14,500,Design.BROWN);int generation=practiceGeneration;
        for(String symbol:values){Button button=new Button(this);button.setText("?");button.setTextColor(Design.BROWN);button.setBackground(Design.shape(this,Design.SOFT,14));GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=Design.dp(this,80);lp.height=Design.dp(this,64);lp.setMargins(4,4,4,4);grid.addView(button,lp);button.setOnClickListener(v->{if(!resumed||generation!=practiceGeneration||waiting[0]||first[0]==button||!button.isEnabled())return;button.setText(symbol);if(first[0]==null){first[0]=button;value[0]=symbol;}else if(symbol.equals(value[0])){first[0].setEnabled(false);button.setEnabled(false);first[0]=null;if(++matched[0]==3)feedback.setText("Practice completed");}else{Button previous=first[0];first[0]=null;waiting[0]=true;handler.postDelayed(()->{if(generation!=practiceGeneration)return;previous.setText("?");button.setText("?");waiting[0]=false;},700);}});}}
    private void sequence(LinearLayout body){Random random=new Random(seed);StringBuilder digits=new StringBuilder();for(int i=0;i<3;i++)digits.append(1+random.nextInt(4));String expected=digits.toString();TextView prompt=Design.text(body,expected,32,600,Design.INK);int generation=practiceGeneration;handler.postDelayed(()->{if(generation==practiceGeneration)prompt.setText("Repeat the sequence");},2500);answer(body,expected::equals);}
    @Override protected void onResume(){super.onResume();resumed=true;}
    @Override protected void onPause(){resumed=false;super.onPause();}
    @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);if(selected==null)return;state.putLong("seed",seed);state.putString("selected",ChallengeRoutineJson.encode(selected).toString());state.putString("notice",notice);if(mathInput!=null)mathEntry=mathInput.getText().toString();state.putString("mathEntry",mathEntry);state.putBoolean("mathDone",mathDone);if(mathChoice!=null)state.putInt("mathChoice",mathChoice);if(practiceMath!=null)state.putString("practiceMath",practiceMath.encode());}
    @Override protected void onDestroy(){practiceGeneration++;handler.removeCallbacksAndMessages(null);super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)RecordUi.lightBars(this);}
}
