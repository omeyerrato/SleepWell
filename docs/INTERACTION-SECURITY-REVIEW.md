# Best Practices and Security Alignment Update

Baseline: origin/main5dca898b0c46a9002de355866412a6b54271e78d. Scope is bounded interaction/IPC fixes; no alarm, mic, torch, phone or broad-test execution.

| ID | Area / priority | Concrete issue and impact | Status / evidence |
|---|---|---|---|
| IS02 | Sensitive external authorization / medium | Sleep-summary Share and installed-app alarm/record Allow pages accepted fully/partially obscured touch sequences. Visibility/focus alone does not reject partial-overlay touches. | Fixed source: shared per-gesture obscured/partially-obscured touch rejection, synthetic CANCEL clears an already pressed control, entire sequence remains rejected until a fresh DOWN. User feedback appears only for a blocked gesture. No new permissions, screenshot restriction or grant-scope changes. Native flagged-input verification pending. |
| IS01 | Deferred exported navigation / medium interaction, low security | HomeLaunchActivity ADD_ALARM worker accepted callbacks while Activity was finishing or paused; only isDestroyed was checked. Cancellation and a later request did not own/invalidate the pending route. A cancelled/background shortcut could open Alarm editor later. Navigation failure had no recoverable UI. | Fixed source: request-generation token; resumed/focused/notfinishing gate for all routes; modern+legacy Back cancellation; same validated onNewIntent path; pending draft recreation; explicit error/Retry/Open alarms actions. No alarms saved or scheduled. Native race verification pending. |

## Scope and implementation

Only HomeLaunchActivity.java changes production behavior. Internal targets are still fresh explicit intents constructed by the existing HomeLaunch allowlist; incoming selectors, nested intents, ClipData or URI grant flags are never forwarded. The new lifecycle gate does not enable capture, output or Save. No dependency/manifest changes.

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
