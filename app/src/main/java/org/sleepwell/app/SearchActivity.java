package org.sleepwell.app;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

/** Local search and explicit alarm selection. Opening a result never starts an output. */
public final class SearchActivity extends Activity {
    public static final String EXTRA_MODE="searchMode",MODE_SETTINGS="settings";
    public static Intent settingsIntent(Activity owner){return RecordUi.intent(owner,SearchActivity.class).putExtra(EXTRA_MODE,MODE_SETTINGS);}
    private final ExecutorService reader=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    private String query="",error="",category="",defaultsNotice="";
    private int generation,searchPosition,choicePosition;
    private long choice=-1;
    private boolean loaded,settingsOnly,opening;
    private List<SearchIndex.Entry> index=List.of();
    private List<Alarm> alarms=List.of();
    private LinearLayout results,filters;private TextView status;private EditText input;private ScrollView scroll;
    private final Runnable searchLater=this::search;

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);RecordUi.review(this);settingsOnly=MODE_SETTINGS.equals(getIntent().getStringExtra(EXTRA_MODE));
        if(saved!=null){query=saved.getString("query","");settingsOnly=saved.getBoolean("settingsOnly",settingsOnly);category=saved.getString("category","");choice=saved.getLong("alarmChoice",-1);searchPosition=Math.max(0,saved.getInt("searchPosition"));choicePosition=Math.max(0,saved.getInt("choicePosition"));}
        if(query.length()>160)query=query.substring(0,160);if(!SearchSettings.category(category))category="";if(SearchSettings.alarmKey(choice).isEmpty())choice=-1;
        if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::back);
        render();
    }
    @Override protected void onResume(){super.onResume();opening=false;load();}
    @Override protected void onPause(){rememberPosition();super.onPause();}
    private boolean choosing(){return choice>=0;}
    private void rememberPosition(){if(scroll!=null&&scroll.isAttachedToWindow()){if(choosing())choicePosition=scroll.getScrollY();else searchPosition=scroll.getScrollY();}}
    private void restorePosition(int position){ScrollView owner=scroll;int request=generation;owner.post(()->{if(!isDestroyed()&&scroll==owner&&request==generation&&owner.isAttachedToWindow())owner.scrollTo(0,position);});}
    private void back(){if(choosing()){generation++;opening=false;choice=-1;error="";render();load();}else finish();}
    @android.annotation.SuppressLint("GestureBackNavigation") @Override public void onBackPressed(){back();}
    private void load(){
        opening=false;if(choosing()){loadAlarms();return;}
        int request=++generation;boolean settings=settingsOnly;loaded=false;status.setText(settings?"Opening settings…":"Opening local search…");
        reader.execute(()->{try{List<SearchIndex.Entry> snapshot=settings?SearchData.settings():SearchData.read(getApplicationContext());runOnUiThread(()->{if(isDestroyed()||request!=generation||choosing())return;index=snapshot;loaded=true;error="";search();});}catch(RuntimeException failed){runOnUiThread(()->{if(isDestroyed()||request!=generation||choosing())return;error=settings?"Settings search could not open.":"Search could not open your saved data.";status.setText(error);results.removeAllViews();Design.button(results,"Try again",true,this::load);});}});
    }
    private void loadAlarms(){
        int request=++generation;long selected=choice;error="";renderChooser(true);
        reader.execute(()->{try{
            List<Alarm> found=new ArrayList<>();try(Store store=Records.open(getApplicationContext())){for(Alarm alarm:store.alarms())if(alarm.nap==null&&!alarm.napInvalid)found.add(alarm);}
            AlarmDefaultsSettings.Read defaults=AlarmDefaultsSettings.load(getApplicationContext());
            runOnUiThread(()->{if(isDestroyed()||request!=generation||selected!=choice)return;alarms=List.copyOf(found);defaultsNotice=defaults.error;renderChooser(false);restorePosition(choicePosition);});
        }catch(RuntimeException failure){runOnUiThread(()->{if(isDestroyed()||request!=generation||selected!=choice)return;error="Saved alarms could not open. Try again.";renderChooser(false);});}});
    }
    private void render(){if(choosing()){renderChooser(true);return;}renderSearch();}
    private LinearLayout page(){LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);scroll=(ScrollView)body.getParent();return body;}
    private void renderChooser(boolean loading){input=null;filters=null;LinearLayout body=page();SearchAlarmChooser.render(this,body,SearchSettings.entry(choice),alarms,loading,error,defaultsNotice,this::back,this::loadAlarms,()->openChosen(-1),this::openChosen);}
    private void renderSearch(){
        LinearLayout body=page();RecordUi.header(body,settingsOnly?"Search settings":"Search SleepWell",this::back);Design.space(body,12);
        RecordUi.segments(body,new String[]{"Search everything","Settings only"},settingsOnly?1:0,n->{boolean next=n==1;if(next==settingsOnly)return;if(input!=null)query=input.getText().toString();settingsOnly=next;searchPosition=0;main.removeCallbacks(searchLater);render();load();});Design.space(body,12);
        input=new EditText(this);input.setSingleLine();input.setTextSize(16*Appearance.scale(this));input.setTypeface(Design.font(this,400));input.setTextColor(Design.INK);input.setHintTextColor(Design.MUTED);input.setHint(settingsOnly?"Search settings":"Alarms, notes or settings");input.setContentDescription(settingsOnly?"Search settings query":"Search query");input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(160)});input.setImeOptions(EditorInfo.IME_ACTION_SEARCH);input.setBackground(Design.shape(this,Design.SOFT,14));Design.pad(input,14,12);input.setText(query);body.addView(input,new LinearLayout.LayoutParams(-1,Design.dp(this,56)));
        input.setOnEditorActionListener((v,action,event)->{if(action!=EditorInfo.IME_ACTION_SEARCH)return false;search();return true;});input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){query=s.toString();searchPosition=0;if(opening){opening=false;generation++;}main.removeCallbacks(searchLater);main.postDelayed(searchLater,180);}public void afterTextChanged(Editable e){}});
        if(settingsOnly){Design.space(body,8);HorizontalScrollView strip=new HorizontalScrollView(this);strip.setHorizontalScrollBarEnabled(false);filters=Design.row(this);strip.addView(filters);body.addView(strip,new LinearLayout.LayoutParams(-1,Design.dp(this,48)));renderFilters();}
        Design.space(body,12);status=Design.text(body,"",13,400,Design.MUTED);status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);Design.space(body,12);results=Design.col(this);body.addView(results);
    }
    private void renderFilters(){
        if(filters==null)return;filters.removeAllViews();List<String> options=new ArrayList<>();options.add("");options.addAll(SearchSettings.CATEGORIES);
        for(String value:options){String name=value.isEmpty()?"All settings":value;View chip=RecordUi.chip(this,name,category.equals(value),()->{category=value;searchPosition=0;renderFilters();search();});chip.setContentDescription("Filter settings: "+name);LinearLayout.LayoutParams gap=new LinearLayout.LayoutParams(-2,Design.dp(this,48));gap.rightMargin=Design.dp(this,4);filters.addView(chip,gap);}
    }
    private void search(){
        if(!loaded||choosing()||isDestroyed())return;opening=false;int request=++generation;String searched=query,chosenCategory=category;boolean settings=settingsOnly;List<SearchIndex.Entry> source=index;
        reader.execute(()->{List<SearchIndex.Entry> found=settings?SearchIndex.findSettings(source,searched,chosenCategory):SearchIndex.find(source,searched);runOnUiThread(()->{
            if(isDestroyed()||request!=generation||choosing())return;results.removeAllViews();boolean blank=SearchIndex.normalize(searched).isEmpty();
            if(blank&&settings&&chosenCategory.isEmpty()){status.setText("Jump to a setting or browse a category.");popularSettings();RecordUi.heading(results,"Browse all categories");for(String group:SearchSettings.CATEGORIES){PlanActivity.detail(results,"settings",group,null,null,()->{category=group;searchPosition=0;renderFilters();search();});Design.line(results);}restorePosition(searchPosition);return;}
            if(blank&&!settings){status.setText("Search your alarms, sleep records and settings.");popularSettings();restorePosition(searchPosition);return;}
            String count=found.size()+(found.size()==100?" results · first 100 shown":found.size()==1?" result":" results");status.setText(found.isEmpty()?settings?"No matching settings · try another term or category":"No matching results":settings?(chosenCategory.isEmpty()?"All settings":chosenCategory)+" · "+count:count);
            for(SearchIndex.Entry entry:found){LinearLayout row=PlanActivity.detail(results,entry.kind==SearchIndex.Kind.ALARM?"alarm":entry.kind==SearchIndex.Kind.RECORD?"report":"settings",entry.title,entry.subtitle,null,()->open(entry));row.setMinimumHeight(Design.dp(this,64));row.setContentDescription(entry.title+". "+entry.subtitle.replace('\n',' ')+". Open");Design.line(results);}restorePosition(searchPosition);
        });});
    }
    private void popularSettings(){
        Design.text(results,"Popular settings",17,600,Design.INK);Design.space(results,10);List<SearchSettings.Shortcut> shortcuts=SearchSettings.popular();LinearLayout row=null;
        for(int n=0;n<shortcuts.size();n++){
            if(n%2==0){row=Design.row(this);row.setBaselineAligned(false);LinearLayout.LayoutParams line=new LinearLayout.LayoutParams(-1,-2);line.bottomMargin=Design.dp(this,8);results.addView(row,line);}
            SearchSettings.Shortcut shortcut=shortcuts.get(n);LinearLayout tile=Design.row(this);Design.pad(tile,12,10);tile.setMinimumHeight(Design.dp(this,58));tile.setBackground(Design.touch(this,Design.SOFT,14));LinearLayout.LayoutParams glyph=new LinearLayout.LayoutParams(Design.dp(this,22),Design.dp(this,22));glyph.rightMargin=Design.dp(this,8);tile.addView(new Design.Icon(this,shortcut.icon,Design.BROWN),glyph);tile.addView(Design.text(this,shortcut.label,13,600,Design.INK),new LinearLayout.LayoutParams(0,-2,1));
            tile.setContentDescription(shortcut.label+(SearchSettings.alarmKey(shortcut.entry.id).isEmpty()?"":". Choose an alarm"));tile.setFocusable(true);SoundEventUi.button(tile);tile.setOnClickListener(view->{InteractionFeedback.action(view);open(shortcut.entry);});LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(0,-2,1);if(n%2==1)size.leftMargin=Design.dp(this,8);row.addView(tile,size);
        }
        if(shortcuts.size()%2==1){LinearLayout.LayoutParams space=new LinearLayout.LayoutParams(0,1,1);space.leftMargin=Design.dp(this,8);row.addView(new View(this),space);}
    }
    private void open(SearchIndex.Entry entry){
        if(isDestroyed()||isFinishing()||!hasWindowFocus()||opening)return;rememberPosition();
        if(entry.kind==SearchIndex.Kind.SETTING){
            if(!SearchSettings.alarmKey(entry.id).isEmpty()){main.removeCallbacks(searchLater);AlarmEditorSearchUi.hideKeyboard(this);generation++;choice=entry.id;choicePosition=0;alarms=List.of();defaultsNotice="";loadAlarms();return;}
            try{startActivity(SearchSettings.intent(this,entry.id));}catch(ActivityNotFoundException|IllegalArgumentException unavailable){status.setText("This setting could not be opened. Refresh the search.");}return;
        }
        if(entry.kind==SearchIndex.Kind.RECORD){startActivity(RecordUi.intent(this,RecordReportActivity.class).putExtra("record",entry.id));return;}
        openAlarm(entry.id,false);
    }
    private void openChosen(int id){if(!choosing()||isDestroyed()||isFinishing()||!hasWindowFocus()||opening)return;rememberPosition();openAlarm(id,id<0);}
    private void openAlarm(long id,boolean fresh){
        int request=++generation;long selected=choice;opening=true;String key=SearchSettings.alarmKey(selected);
        reader.execute(()->{
            String saved=null,notice="";
            try(Store store=Records.open(getApplicationContext())){
                Alarm alarm;
                if(fresh){int next=Math.addExact(store.alarms().stream().mapToInt(a->a.id).max().orElse(0),1);AlarmDefaultsSettings.Read defaults=AlarmDefaultsSettings.load(getApplicationContext());alarm=defaults.create(next);notice=defaults.error;}
                else alarm=store.alarm(Math.toIntExact(id));
                if(alarm!=null&&(key.isEmpty()||alarm.nap==null&&!alarm.napInvalid))saved=alarm.json().toString();
            }catch(RuntimeException failure){/* Never select a different alarm after a failed lookup. */}
            String draft=saved,explanation=notice;runOnUiThread(()->{
                if(isDestroyed()||request!=generation)return;opening=false;if(isFinishing()||!hasWindowFocus()||selected!=choice)return;
                if(draft==null){if(choosing()){error=fresh?"A new alarm draft could not open. Try again.":"That alarm changed or is no longer available. Refresh the list and choose again.";renderChooser(false);}else{status.setText("This alarm changed. Refresh the search.");load();}return;}
                Intent intent=RecordUi.intent(this,AlarmEditorActivity.class).putExtra("alarm",draft).putExtra("newAlarm",fresh);if(!key.isEmpty())intent.putExtra(SearchSettings.ALARM_SETTING,key).putExtra("alarmDefaultsNotice",explanation);startActivity(intent);
            });
        });
    }
    @Override protected void onSaveInstanceState(Bundle state){rememberPosition();super.onSaveInstanceState(state);state.putString("query",query);state.putBoolean("settingsOnly",settingsOnly);state.putString("category",category);state.putLong("alarmChoice",choice);state.putInt("searchPosition",searchPosition);state.putInt("choicePosition",choicePosition);}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
    @Override protected void onDestroy(){generation++;main.removeCallbacksAndMessages(null);reader.shutdownNow();super.onDestroy();}
    boolean awaitReaders(long millis)throws InterruptedException{return reader.awaitTermination(millis,TimeUnit.MILLISECONDS);}
}
