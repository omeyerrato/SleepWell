# Best Practices and Security Alignment Update

Baseline: origin/main5dca898b0c46a9002de355866412a6b54271e78d. Scope is bounded interaction/IPC fixes; no alarm, mic, torch, phone or broad-test execution.

| ID | Area / priority | Concrete issue and impact | Status / evidence |
|---|---|---|---|
| IS03 | Optional calendar permission interaction / medium interaction | Repeated taps requested READ_CALENDAR after permanent denial, without rationale or Settings recovery; returning from Android Settings did not refresh permission/source state. | Fixed source: initial/rationale/in-flight/Settings states, actual-request history with canceled/failed-request rollback, refresh on resume, and rejection of stale source results. Unsaved rule selections remain intact. Focused native fixture added; execution pending. |
| IS02 | Sensitive external authorization / medium | Sleep-summary Share and installed-app alarm/record Allow pages accepted fully/partially obscured touch sequences. Visibility/focus alone does not reject partial-overlay touches. | Fixed source: shared per-gesture obscured/partially-obscured touch rejection, synthetic CANCEL clears an already pressed control, entire sequence remains rejected until a fresh DOWN. User feedback appears only for a blocked gesture. No new permissions, screenshot restriction or grant-scope changes. Native flagged-input verification pending. |
| IS01 | Deferred exported navigation / medium interaction, low security | HomeLaunchActivity ADD_ALARM worker accepted callbacks while Activity was finishing or paused; only isDestroyed was checked. Cancellation and a later request did not own/invalidate the pending route. A cancelled/background shortcut could open Alarm editor later. Navigation failure had no recoverable UI. | Fixed source: request-generation token; resumed/focused/notfinishing gate for all routes; modern+legacy Back cancellation; same validated onNewIntent path; pending draft recreation; explicit error/Retry/Open alarms actions. No alarms saved or scheduled. Native race verification pending. |

## Scope and implementation

Production scope is HomeLaunchActivity, the sensitive-review touch helper and its three authorization screens, and CalendarRulesActivity. Internal shortcut targets are still fresh explicit intents constructed by the existing HomeLaunch allowlist; incoming selectors, nested intents, ClipData or URI grant flags are never forwarded. The lifecycle gate does not enable capture, output or Save. No dependency/manifest changes.

Reviewed boundaries, without a new exploit claim: AlarmContentProvider and RecordContentProvider have signature read/write permissions, scoped shared/v1 grants and per-calling-UID app grant revalidation; unsupported SQL/RPC/file surfaces are rejected. Main exported extras inspected route only to pages. Protected boot action allowlists and permission-bound platform services remain necessary. This is not an exhaustive security certification.

Existing HomeWidgetNativeTest checks allowlisted targets and unsaved-draft behavior; those methods do not yet reproduce the delayed callback cancellation race. Source diff checking passed. No unit/device/CI batch was run. Root owns bounded native follow-up and any manifest/CI integration.

## Exact implementation diff

```diff
diff --git a/app/src/main/java/org/sleepwell/app/HomeLaunchActivity.java b/app/src/main/java/org/sleepwell/app/HomeLaunchActivity.java
index 4998b2f9..02ac75db 100644
--- a/app/src/main/java/org/sleepwell/app/HomeLaunchActivity.java
+++ b/app/src/main/java/org/sleepwell/app/HomeLaunchActivity.java
@@ -8 +8 @@ import java.util.concurrent.*;
-/** Exported only to let launchers open allowlisted screens; never forwards incoming intents. */
+/** Exported allowlisted navigation only. Deferred routes belong to this visible request. */
@@ -11,5 +11,22 @@ public final class HomeLaunchActivity extends Activity {
-    @Override public void onCreate(Bundle state){super.onCreate(state);HomeLaunch.Screen screen=HomeLaunch.read(getIntent());if(screen==null){finish();return;}if(screen==HomeLaunch.Screen.ADD_ALARM)addAlarm();else open(HomeLaunch.target(this,screen,getIntent().getLongExtra("record",0)));}
-
-    private void addAlarm(){Records.Factory pinned=Records.factory;worker.execute(()->{try(Store store=pinned.open(getApplicationContext())){Alarm draft=HomeLaunch.draft(this,store);runOnUiThread(()->{if(!isDestroyed())open(new Intent(this,AlarmEditorActivity.class).putExtra("alarm",draft.json().toString()));});}catch(Exception failure){runOnUiThread(()->{if(!isDestroyed())open(new Intent(this,MainActivity.class).putExtra("destination",1));});}});}
-    private void open(Intent intent){startActivity(intent);finish();}
-    @Override protected void onDestroy(){worker.shutdown();super.onDestroy();}
+    private HomeLaunch.Screen screen;private Intent target;private Alarm draft;private int generation;
+    private boolean resumed,pending,opening;private String error="";
+    @Override public void onCreate(Bundle state){super.onCreate(state);if(android.os.Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::cancel);route(getIntent(),state);}
+    private void route(Intent input,Bundle state){
+        int request=++generation;target=null;draft=null;pending=false;opening=false;error="";screen=HomeLaunch.read(input);
+        if(screen==null){error="This shortcut is unavailable. Close it and open SleepWell again.";render();return;}
+        if(screen!=HomeLaunch.Screen.ADD_ALARM){try{target=HomeLaunch.target(this,screen,Math.max(0,input.getLongExtra("record",0)));pending=true;}catch(RuntimeException invalid){error="This shortcut could not be read. Open the screen from SleepWell.";}render();openIfVisible();return;}
+        if(state!=null&&state.containsKey("draft")){try{draft=Alarm.read(new org.json.JSONObject(state.getString("draft")));target=new Intent(this,AlarmEditorActivity.class).putExtra("alarm",draft.json().toString());pending=true;render();openIfVisible();return;}catch(Exception invalid){error="The interrupted alarm draft could not be restored. Close this shortcut and add an alarm again.";render();return;}}
+        render();Records.Factory pinned=Records.factory;
+        worker.execute(()->{try(Store store=pinned.open(getApplicationContext())){Alarm prepared=HomeLaunch.draft(getApplicationContext(),store);runOnUiThread(()->{if(!current(request))return;draft=prepared;target=new Intent(this,AlarmEditorActivity.class).putExtra("alarm",prepared.json().toString());pending=true;render();openIfVisible();});}catch(Exception failure){runOnUiThread(()->{if(!current(request))return;error="A new alarm draft could not be opened. Existing alarms were not changed.";render();});}});
+    }
+    private boolean current(int request){return request==generation&&!isDestroyed()&&!isFinishing();}
+    private void openIfVisible(){if(!pending||opening||target==null||!resumed||!hasWindowFocus()||isFinishing()||isDestroyed())return;opening=true;pending=false;try{startActivity(target);generation++;finish();}catch(RuntimeException unavailable){opening=false;error="The requested screen could not open. Try again or close this shortcut.";render();}}
+    private void render(){if(isDestroyed()||isFinishing())return;android.widget.LinearLayout body=Design.page(this,false,null,n->{});RecordUi.header(body,"Open SleepWell",this::cancel);Design.space(body,18);Design.text(body,error.isEmpty()?"Opening your screen…":error,16,500,error.isEmpty()?Design.INK:Design.BROWN);android.widget.LinearLayout footer=RecordUi.footer(body);if(!error.isEmpty()&&target!=null)Design.button(footer,"Try opening again",true,()->{error="";pending=true;openIfVisible();});if(!error.isEmpty()&&screen==HomeLaunch.Screen.ADD_ALARM&&target==null)Design.button(footer,"Open alarms",true,()->{target=HomeLaunch.target(this,HomeLaunch.Screen.ALARMS,0);pending=true;openIfVisible();});Design.button(footer,"Cancel",false,this::cancel);}
+    private void cancel(){generation++;pending=false;target=null;finish();}
+    @Override protected void onNewIntent(Intent input){super.onNewIntent(input);setIntent(input);route(input,null);}
+    @Override protected void onResume(){super.onResume();resumed=true;openIfVisible();}
+    @Override protected void onPause(){resumed=false;super.onPause();}
+    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused){RecordUi.lightBars(this);openIfVisible();}}
+    @android.annotation.SuppressLint("GestureBackNavigation") @Override public void onBackPressed(){cancel();}
+    @Override public void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);if(pending&&draft!=null)state.putString("draft",draft.json().toString());}
+    @Override protected void onDestroy(){generation++;worker.shutdown();super.onDestroy();}
```

## Sensitive authorization touch alignment

[Android primary tapjacking guidance](https://developer.android.com/privacy-and-security/risks/tapjacking) distinguishes full and partial occlusion; partial-occlusion touches lack default protection. The applied boundary handles the actual MotionEvent obscured flags, not package-name claims. A fresh clean gesture still works. It does not claim to block a fully privileged accessibility service or all possible deceptive UI. Benign overlays can also trigger the flags; the actual-block feedback tells the user how to proceed. Existing UID/signer/consent/grant verification is unchanged.

Scope: new SensitiveReviewTouches.java; SleepSummaryActivity.java; AlarmAccessActivity.java; RecordAccessActivity.java. No dependencies/manifest changes. Source diff checking passed; no attack/device/CI execution was performed. Root must exercise flagged DOWN and mid-gesture obscured UP (both must not invoke grant/Share) followed by a clean gesture before declaring verified.

```diff
diff --git a/app/src/main/java/org/sleepwell/app/AlarmAccessActivity.java b/app/src/main/java/org/sleepwell/app/AlarmAccessActivity.java
index b55ce0de..893f23e4 100644
--- a/app/src/main/java/org/sleepwell/app/AlarmAccessActivity.java
+++ b/app/src/main/java/org/sleepwell/app/AlarmAccessActivity.java
@@ -16,0 +17,3 @@ public final class AlarmAccessActivity extends Activity {
+ private final SensitiveReviewTouches reviewTouches=new SensitiveReviewTouches();
+ @Override public boolean dispatchTouchEvent(android.view.MotionEvent event){return reviewTouches.dispatch(this,event,super::dispatchTouchEvent);}
+
diff --git a/app/src/main/java/org/sleepwell/app/RecordAccessActivity.java b/app/src/main/java/org/sleepwell/app/RecordAccessActivity.java
index 87f14b5d..fb41bf45 100644
--- a/app/src/main/java/org/sleepwell/app/RecordAccessActivity.java
+++ b/app/src/main/java/org/sleepwell/app/RecordAccessActivity.java
@@ -16,0 +17,3 @@ public final class RecordAccessActivity extends Activity {
+ private final SensitiveReviewTouches reviewTouches=new SensitiveReviewTouches();
+ @Override public boolean dispatchTouchEvent(android.view.MotionEvent event){return reviewTouches.dispatch(this,event,super::dispatchTouchEvent);}
+
diff --git a/app/src/main/java/org/sleepwell/app/SensitiveReviewTouches.java b/app/src/main/java/org/sleepwell/app/SensitiveReviewTouches.java
new file mode 100644
index 00000000..9394a6ec
--- /dev/null
+++ b/app/src/main/java/org/sleepwell/app/SensitiveReviewTouches.java
@@ -0,0 +1,26 @@
+package org.sleepwell.app;
+
+import android.content.Context;
+import android.view.MotionEvent;
+import android.widget.Toast;
+
+/** Authorization-review touch boundary, including partial occlusion on API29+.
+ * https://developer.android.com/privacy-and-security/risks/tapjacking
+ * Does not disable screenshots or legitimate unobscured accessibility actions. */
+final class SensitiveReviewTouches {
+    interface Dispatch {boolean send(MotionEvent event);}
+    private boolean blocked;
+    boolean dispatch(Context context,MotionEvent event,Dispatch normal){
+        if(event.getActionMasked()==MotionEvent.ACTION_DOWN)blocked=false;
+        int obscured=MotionEvent.FLAG_WINDOW_IS_OBSCURED|MotionEvent.FLAG_WINDOW_IS_PARTIALLY_OBSCURED;
+        if((event.getFlags()&obscured)!=0&&!blocked){
+            blocked=true;
+            // A control may already have received DOWN before an overlay appeared.
+            // Cancel its pressed/gesture state; never deliver a later clean UP as consent.
+            MotionEvent cancel=MotionEvent.obtain(event.getDownTime(),event.getEventTime(),MotionEvent.ACTION_CANCEL,event.getX(),event.getY(),event.getMetaState());
+            try{normal.send(cancel);}finally{cancel.recycle();}
+            Toast.makeText(context,"Close other app overlays, then try again.",Toast.LENGTH_SHORT).show();
+        }
+        return !blocked&&normal.send(event);
+    }
+}
diff --git a/app/src/main/java/org/sleepwell/app/SleepSummaryActivity.java b/app/src/main/java/org/sleepwell/app/SleepSummaryActivity.java
index aaf24e8e..b50f1381 100644
--- a/app/src/main/java/org/sleepwell/app/SleepSummaryActivity.java
+++ b/app/src/main/java/org/sleepwell/app/SleepSummaryActivity.java
@@ -4,0 +5,3 @@ public final class SleepSummaryActivity extends Activity implements SleepSummary
+ private final SensitiveReviewTouches reviewTouches=new SensitiveReviewTouches();
+ @Override public boolean dispatchTouchEvent(android.view.MotionEvent event){return reviewTouches.dispatch(this,event,super::dispatchTouchEvent);}
+
```

## Calendar permission recovery alignment (IS03)

Security alignment area: runtime permission interaction and revocation handling. Priority: medium interaction correctness; no new IPC exploit claim.

CalendarRulesActivity checks current access for every operation and source publication. It distinguishes the first request, Android's rationale state, an in-flight prompt, and requested/no-rationale Settings recovery. Only an actual prompt attempt records request history; a thrown launch or empty canceled result restores its previous value. Settings launch failures have recoverable feedback. Returning to the Activity refreshes calendar sources without saving or applying the draft. Revocation hides source controls while preserving calendar selections and timing options. A request-generation check discards stale source reads. No permission, dependency, provider or scheduling scope changed.

A pinned PermissionActions seam lets the existing CalendarRulesFlowTest exercise native controls with private preferences, fake calendar sources and fake request/Settings actions. The focused method checks failed/canceled prompts, rationale cancellation/continuation, permanent denial, Settings return and revocation while asserting that no rules are saved and no sync is applied. These are controlled permission-state fixtures, not Android's permission controller.

## One bounded native verification proposal / added implementation

Run only InteractionSecurityNativeTest's two methods and CalendarRulesFlowTest's focused permission method in the CI emulator (`ciReview=true`, qemu required), alongside the already selected build validation. Root owns CI dispatch; no local device, ADB, alarms, audio, capture, haptics, torch or personal data access.

1. `delayedShortcutDraftCannotRouteAfterCancelOrWhilePaused`: reuse the existing HomeWidgetNativeTest private Store/Records.Factory strategy and block draft preparation with latches. Cancel an actual HomeLaunchActivity before releasing its worker; assert no editor launch. For a second request, invoke Android lifecycle pause/resume callbacks, release/drain the worker, assert no route while paused and one route on resume. A blocking Instrumentation monitor intercepts AlarmEditorActivity, so no editor, Save or scheduler is executed. Private alarms/sessions/defaults remain empty. Factories restore only after both Activity workers drain.
2. `obscuredNativeGesturesCancelAndRequireAFreshCleanDown`: feed flagged MotionEvents through SensitiveReviewTouches into an attached inert Android Button. For full and partial obscuration, flagged DOWN and mid-gesture flagged UP produce zero clicks and a delivered CANCEL; a later clean UP remains rejected. A fresh clean DOWN/UP clicks once. No real Share or app Allow action runs.

Evidence at implementation handoff: `git diff --check` passed. No local build, unit batch, instrumentation, phone install or runtime test was run; root coordinates the single bounded CI run. Remaining limits: manual lifecycle callbacks and synthetic flagged input do not reproduce real overlays or platform background-task timing; the three Activity dispatch hooks are source-reviewed, not a real attack demonstration. Physical-device and OS permission-controller behavior remain unverified. No exhaustive security certification is claimed.

### Calendar implementation diff

```diff
diff --git a/app/src/main/java/org/sleepwell/app/CalendarRulesActivity.java b/app/src/main/java/org/sleepwell/app/CalendarRulesActivity.java
index 17d72976..3c149ec6 100644
--- a/app/src/main/java/org/sleepwell/app/CalendarRulesActivity.java
+++ b/app/src/main/java/org/sleepwell/app/CalendarRulesActivity.java
@@ -3,7 +3,9 @@ package org.sleepwell.app;
 import android.Manifest;
 import android.app.*;
 import android.content.*;
+import android.net.Uri;
 import android.os.*;
+import android.provider.Settings;
 import android.widget.*;
 import java.time.*;
 import java.util.*;
@@ -11,17 +13,29 @@ import java.util.concurrent.*;

 /** Optional calendar selection. Only Save/Refresh changes future repeating schedules. */
 public final class CalendarRulesActivity extends Activity {
+    private static final int CALENDAR_PERMISSION=813;
+    private static final String PERMISSION_REQUESTED="readCalendarRequested";
+    interface PermissionActions {boolean rationale(Activity activity);void request(Activity activity);void settings(Activity activity);}
+    static PermissionActions permissions=new PermissionActions(){
+        public boolean rationale(Activity activity){return activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_CALENDAR);}
+        public void request(Activity activity){activity.requestPermissions(new String[]{Manifest.permission.READ_CALENDAR},CALENDAR_PERMISSION);}
+        public void settings(Activity activity){activity.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.fromParts("package",activity.getPackageName(),null)));}
+    };
     private CalendarPolicy.Rules draft;private List<CalendarAccess.Source> sources=List.of();
-    private CalendarAccess.Port access;private String error="",status="";private boolean loading,busy;
+    private CalendarAccess.Port access;private PermissionActions permissionActions;private String error="",status="",permissionMessage="";private boolean loading,busy,requesting,showRationale,askedBeforeRequest;private int loadGeneration;
     private final ExecutorService worker=Executors.newSingleThreadExecutor();private final CancellationSignal cancel=new CancellationSignal();
-    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);access=CalendarAccess.factory.open(getApplicationContext());try{draft=saved==null?CalendarSettings.load(this):CalendarSettings.decode(saved.getString("draft"));}catch(RuntimeException invalid){draft=CalendarPolicy.Rules.off();error="Saved calendar rules need review. Choose your calendars again.";}load();}
-    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("draft",CalendarSettings.encode(draft));}
-    private void load(){loading=true;render();worker.execute(()->{List<CalendarAccess.Source> result=List.of();String failure="";try{if(access.allowed())result=access.sources(cancel);}catch(RuntimeException unavailable){failure="Calendars could not be read. Check access and try again.";}List<CalendarAccess.Source> found=result;String message=failure;runOnUiThread(()->{if(isDestroyed())return;sources=found;loading=false;if(!message.isEmpty())error=message;render();});});}
+    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);access=CalendarAccess.factory.open(getApplicationContext());permissionActions=permissions;if(saved!=null){requesting=saved.getBoolean("requesting");showRationale=saved.getBoolean("rationale");askedBeforeRequest=saved.getBoolean("askedBeforeRequest");}try{draft=saved==null?CalendarSettings.load(this):CalendarSettings.decode(saved.getString("draft"));}catch(RuntimeException invalid){draft=CalendarPolicy.Rules.off();error="Saved calendar rules need review. Choose your calendars again.";}render();}
+    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("draft",CalendarSettings.encode(draft));out.putBoolean("requesting",requesting);out.putBoolean("rationale",showRationale);out.putBoolean("askedBeforeRequest",askedBeforeRequest);}
+    @Override protected void onResume(){super.onResume();load();}
+    private void load(){int generation=++loadGeneration;sources=List.of();loading=access.allowed();if(!loading){render();return;}permissionMessage="";showRationale=false;render();worker.execute(()->{List<CalendarAccess.Source> result=List.of();String failure="";try{if(access.allowed())result=access.sources(cancel);}catch(RuntimeException unavailable){failure="Calendars could not be read. Check access and try again.";}List<CalendarAccess.Source> found=result;String message=failure;runOnUiThread(()->{if(isDestroyed()||isFinishing()||generation!=loadGeneration)return;sources=access.allowed()?found:List.of();loading=false;if(!message.isEmpty())error=message;render();});});}
+    private boolean permissionSettings(){return CalendarSettings.prefs(this).getBoolean(PERMISSION_REQUESTED,false)&&!permissionActions.rationale(this);}
+    private void permission(){if(busy||requesting)return;if(access.allowed()){load();return;}permissionMessage="";if(permissionSettings()){showRationale=false;try{permissionActions.settings(this);}catch(RuntimeException missing){permissionMessage="Open SleepWell's permissions from Android Settings to allow calendar access.";}render();return;}if(permissionActions.rationale(this)){showRationale=true;render();return;}askPermission();}
+    private void askPermission(){if(busy||requesting)return;if(access.allowed()){showRationale=false;load();return;}if(permissionSettings()){permission();return;}askedBeforeRequest=CalendarSettings.prefs(this).getBoolean(PERMISSION_REQUESTED,false);requesting=true;showRationale=false;permissionMessage="";CalendarSettings.prefs(this).edit().putBoolean(PERMISSION_REQUESTED,true).apply();render();try{permissionActions.request(this);}catch(RuntimeException unavailable){requesting=false;CalendarSettings.prefs(this).edit().putBoolean(PERMISSION_REQUESTED,askedBeforeRequest).apply();permissionMessage="Calendar access could not be requested. Try again or open Android Settings.";render();}}
     private void change(boolean enabled,Set<Long> holidays,Set<Long> meetings,int preparation,int advance){draft=new CalendarPolicy.Rules(enabled,holidays,meetings,preparation,advance,draft.revision);status="";}
     private void select(long id,boolean holiday,boolean checked){Set<Long> h=new HashSet<>(draft.holidays),m=new HashSet<>(draft.meetings),chosen=holiday?h:m;if(checked)chosen.add(id);else chosen.remove(id);change(draft.enabled,h,m,draft.preparation,draft.advance);}
-    private void render(){LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,"Calendar alarms",this::finish);Design.space(body,12);Design.text(body,"Optional rules for repeating alarms.",15,400,Design.MUTED);Design.space(body,16);
+    private void render(){LinearLayout body=Design.page(this,false,null,n->{});Button permissionButton=null;RecordUi.label(this,body);RecordUi.header(body,"Calendar alarms",this::finish);Design.space(body,12);Design.text(body,"Optional rules for repeating alarms.",15,400,Design.MUTED);Design.space(body,16);
         Design.toggle(body,"Use calendar rules","One-time alarms and naps keep their own times",draft.enabled,on->change(on,draft.holidays,draft.meetings,draft.preparation,draft.advance));Design.space(body,16);
-        if(!access.allowed()){Design.text(body,"Calendar access is not allowed.",15,500,Design.INK);Design.button(body,"Allow calendar access",false,()->requestPermissions(new String[]{Manifest.permission.READ_CALENDAR},813));}
+        if(!access.allowed()){Design.text(body,"Calendar access is not allowed. Your selections are kept.",15,500,Design.INK);if(!permissionMessage.isEmpty())Design.text(body,permissionMessage,13,500,Design.BROWN);if(showRationale&&!requesting){Design.text(body,"Read access lists your calendars and checks selected dates for optional alarm rules. Granting access does not save rules or change alarms.",14,400,Design.MUTED);Design.button(body,"Continue to calendar access",false,this::askPermission);Design.button(body,"Not now",false,()->{showRationale=false;render();});}else permissionButton=Design.button(body,requesting?"Waiting for Android…":permissionSettings()?"Open app permission settings":"Allow calendar access",false,this::permission);}
         else if(loading)Design.text(body,"Reading calendars…",14,400,Design.MUTED);
         else if(sources.isEmpty()){Design.text(body,"No visible calendars on this phone.",15,500,Design.INK);Design.text(body,"Add or show a calendar in your calendar app, then refresh.",13,400,Design.MUTED);}
         else for(CalendarAccess.Source source:sources){RecordUi.heading(body,source.name);Design.toggle(body,"Skip all-day dates",null,draft.holidays.contains(source.id),on->select(source.id,true,on));Design.space(body,6);Design.toggle(body,"Earlier for meetings",null,draft.meetings.contains(source.id),on->select(source.id,false,on));Design.space(body,10);}
@@ -29,11 +43,11 @@ public final class CalendarRulesActivity extends Activity {
         Design.item(body,"sun","Maximum advance",draft.advance+" min",()->SleepPicker.choices(this,"Maximum advance",new String[]{"30 min","60 min","120 min","180 min","360 min"},index(new int[]{30,60,120,180,360},draft.advance),n->{change(draft.enabled,draft.holidays,draft.meetings,draft.preparation,new int[]{30,60,120,180,360}[n]);render();}));Design.space(body,10);
         Design.text(body,"Meetings can move an alarm earlier on the same day. Missing calendar data keeps its ordinary time.",12,400,Design.MUTED);long refreshed=CalendarSettings.prefs(this).getLong("refreshed",0);Design.space(body,10);Design.text(body,refreshed==0?"Last refreshed: Never":"Last refreshed: "+RecordUi.clock(this,refreshed,ZoneId.systemDefault()),12,400,Design.MUTED);
         String syncError=CalendarSettings.prefs(this).getString("error","");if(!syncError.isEmpty()){Design.space(body,6);Design.text(body,syncError,13,500,Design.BROWN);}Design.button(body,"Reload calendar list",false,()->{error="";load();});
-        Design.space(body,16);PlanActivity.detail(body,"calendar","Save future nights","Separate automatic calendar export",null,()->startActivity(RecordUi.intent(this,AutoCalendarActivity.class)));LinearLayout footer=RecordUi.footer(body);if(!error.isEmpty())Design.text(footer,error,13,500,0xffa93919);if(!status.isEmpty())Design.text(footer,status,13,500,Design.BROWN);Design.button(footer,busy?"Saving…":"Save calendar rules",true,this::save).setEnabled(!busy);RecordUi.enabled(body,!busy);
+        Design.space(body,16);PlanActivity.detail(body,"calendar","Save future nights","Separate automatic calendar export",null,()->startActivity(RecordUi.intent(this,AutoCalendarActivity.class)));LinearLayout footer=RecordUi.footer(body);if(!error.isEmpty())Design.text(footer,error,13,500,0xffa93919);if(!status.isEmpty())Design.text(footer,status,13,500,Design.BROWN);Design.button(footer,busy?"Saving…":"Save calendar rules",true,this::save).setEnabled(!busy);RecordUi.enabled(body,!busy);if(permissionButton!=null)permissionButton.setEnabled(!busy&&!requesting);
     }
     private int index(int[] values,int selected){for(int n=0;n<values.length;n++)if(values[n]==selected)return n;return -1;}
     private void save(){if(busy)return;if(draft.enabled&&(!access.allowed()||draft.holidays.isEmpty()&&draft.meetings.isEmpty())){error="Allow access and select at least one calendar.";render();return;}CalendarPolicy.Rules value=new CalendarPolicy.Rules(draft.enabled,draft.holidays,draft.meetings,draft.preparation,draft.advance,UUID.randomUUID().toString());busy=true;error="";render();worker.execute(()->{String result="";try{CalendarSettings.save(getApplicationContext(),value);CalendarSync.port.apply(getApplicationContext());}catch(RuntimeException failed){result="Rules could not be applied. Check access and try again.";}String failure=result;runOnUiThread(()->{if(isDestroyed())return;busy=false;draft=value;error=failure;status=failure.isEmpty()?"Calendar rules saved.":"";render();});});}
-    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] grants){super.onRequestPermissionsResult(request,permissions,grants);if(request==813)load();}
+    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] grants){super.onRequestPermissionsResult(request,permissions,grants);if(request==CALENDAR_PERMISSION){requesting=false;showRationale=false;if(grants.length==0)CalendarSettings.prefs(this).edit().putBoolean(PERMISSION_REQUESTED,askedBeforeRequest).apply();permissionMessage=access.allowed()?"":grants.length==0?"Calendar access request was canceled. Your selections are kept.":"Calendar access was not allowed. Your selections are kept.";load();}}
     @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
     @Override protected void onDestroy(){cancel.cancel();worker.shutdown();super.onDestroy();}
     boolean awaitWorkers(long millis)throws InterruptedException{return worker.awaitTermination(millis,TimeUnit.MILLISECONDS);}
```
