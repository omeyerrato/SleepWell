# Skip next alarm and Undo

Reference: supplied board02, left Alarms screen. This implements one reviewed exception, not public-holiday rules or a date-range holiday mode.

## Saved behavior

- Every ordinary alarm can skip its chosen next calendar occurrence. A cream confirmation names the date/time and explains whether the schedule stays enabled.
- A repeating alarm stays enabled. Only the chosen local calendar date/time is excluded; the weekday set is preserved. Its next eligible date is scheduled through the existing alarm scheduling boundary.
- A one-time alarm is disabled. Undo can re-enable only that original chosen occurrence, strictly before its selected time; it never rolls an expired one-time alarm to tomorrow.
- The Alarms row displays the skipped date/time and a real Undo button. Scheduling failures persist with a separate Retry action. An unconfirmed scheduling marker is saved before platform effects, so interruption before the request does not silently look complete. No chevron is used to disguise immediate Undo as a settings menu.
- Skip metadata stores version1, canonical UUID, local ISO date, hour/minute, exact weekday set and the original selection zone. The current device zone resolves that same local date/time on reboot and clock/zone reconciliation. No second date is silently chosen. DST gaps/folds use the same Java calendar resolution as existing alarms.
- Ordinary alarm time/day/enable edits explicitly clear the exception. Non-schedule edits retain the currently saved exception, including a newer one created while an old editor remained open. A stale explicit schedule edit or stale Skip/Undo is rejected by saved ownership and enabled/time/day fingerprint. A short SQLite transaction owns each saved-intent check/write, with platform effects outside the transaction and a checked status-only update afterward.
- Naps retain their independent nonce/relative-time scheduler. Nap/skip mixtures and changed/malformed skip signatures are rejected; malformed raw local metadata is preserved for review instead of ringing based on an assumed interpretation.
- Portable backup formats1–4 remain unchanged. The existing validation rejects malformed skip data before replacement; restore removes skip/scheduling-error authority and leaves every imported alarm disabled. No schema change or backup-worker change.

## Delivery ownership

Normal FIRE PendingIntents remain private, explicit and immutable, with an expected occurrence timestamp. A queued older timestamp cannot claim a newer deadline. Legacy deliveries without that extra are accepted only at a saved due deadline and still pass the saved-calendar/skip/enable guard.

Both deadline delivery and smart movement admission re-read saved state under the same occurrence lock as Skip/Undo and reject a skipped date before alarm runtime effects. Smart wake retains its actual configured movement window and its last-claimed occurrence fence. Snooze, backup escalation and wake-up check ownership remain outside this calendar exception; skipping the next scheduled alarm does not cancel a current snooze or wake-up check. The redundant pre-admission sleep-sound stop was removed from the smart caller; AlarmService retains the existing post-admission sound stop.

The shared upcoming-calendar resolver is skip-aware, so Tonight/Wind-down does not advertise the omitted occurrence. Relative naps still use NapPlan deadlines. Exact-alarm permission and protected boot/clock/zone broadcasts remain unchanged.

## Honest boundaries

SQLite, Android alarm scheduling and process lifetime do not share a transaction. There remains a final state-check/platform-call race; a stale PendingIntent may be registered during concurrent replacement, but saved disabled/skip/calendar admission prevents it from sounding. Retry reconciles the saved intent. Status updates never overwrite concurrently replaced alarm contents.

An Android scheduling request is not proof of eventual delivery. Permission changes, force stop, system policy, process death and platform failure can prevent delivery. A saved skip fences an already queued old FIRE, but cannot retroactively stop an occurrence that already won admission. Skip is rejected while an alarm is actively ringing. A one-time skip remains off after expiry; creating another alarm is an explicit action. A clock rollback can make the same chosen date future again; it does not transform the exception into a different date. A passed recurring exception remains visible until a subsequent occurrence completes or the user edits/replaces it.

## Validation and evidence

- Existing project uses native Java Views, JUnit4 and AndroidX/UIAutomator with explicit injectable ports; the installed testing-setup skill was followed within that existing stack, without unrelated framework migration.
- `AlarmSkipTest`: twelve pure cases cover weekdays, midnight/weekly dates, zone changes, DST gap/fold, rollback, malformed identity, smart/claimed admission and queued timestamp ownership.
- `AlarmSkipStorageTest`: eight isolated native SQLite methods cover saved/reopened ownership, one-time expiry, stale editor/Undo, persistent scheduling and Undo failures, strict JSON/restore, explicit toggles/deletion and deterministic concurrent disabled replacement blocking the atomic check/write, and the same real guarded new-editor save protocol. Only a silent scheduling port is used.
- `AlarmSkipFlowTest`: two intended native methods, fifteen original screenshots: `alarm-skip-{default,confirmation,skipped,undo,recreated,reopened,schedule-error,error-reopened,retry,undo-error,undo-error-reopened,undo-retry,one-time-confirmation,one-time-skipped,expired}.png`. Real Main controls, confirmation cancellation, recreation/reopen and saved state are exercised; no AlarmActivity, service, audio, vibration, camera or sensor output is invoked.
- The private fixture rejects every nightly service/scheduling call through its own disabled preferences port, checks that schedule effects only see a durable unconfirmed intent, and preserves all original preferences, the unrelated private alarm and empty history, and rejects any Activity beyond Main. No sample public history is inserted. All native execution and actual screenshot comparisons remain pending until CI.
- The supplied board02 was visually inspected before implementation. No visual acceptance is claimed from compilation.

## Best Practices and Security Alignment Update: private occurrence delivery

Priority: medium. The implementation adds an immutable expected occurrence extra to the existing explicit app-private FIRE PendingIntent; it introduces no exported component or nested Intent forwarding. Receiver timestamp validation and saved-state admission stop stale queued requests from claiming a newly chosen occurrence. The installed android-intent-security skill informed this audit.

Relevant scoped change:

```diff
- setAlarmClock(..., intent(c,id,action));
+ // FIRE uses an explicit AlarmReceiver Intent, FLAG_IMMUTABLE, and expected occurrence.
+ setAlarmClock(..., operation);
+ if (FIRE && !acceptCalendar(delivered, savedDeadline, now)) return;
+ // Scheduler re-reads the saved alarm and checks skip, enabled state and occurrence ownership.
```

Files: Alarm, AlarmSkip/AlarmSkipJson/AlarmSkips/AlarmSkipUi, AlarmReceiver, Scheduler, AlarmScheduling consumers Main/Editor/Tonight/Naps, narrow TrackingActivity/TrackingService guard consumption, and Backup codec validation/restore.

The genuine MIT Phosphor `bell-slash/regular` and `arrow-counter-clockwise/regular` assets were fetched by the existing pinned importer at commit2b75f3ad12b420c9504ef05df8d2564a28f8500e. All64 prior pinned source/license hashes remain identical; all66 current hashes verify. No downloaded code was executed, and no icon path was redrawn.

Primary Android reference: https://developer.android.com/develop/background-work/services/alarms (exact-access requirements, immutable PendingIntents, replacement/cancellation, boot rescheduling, RTC/calendar behavior).

### Local compilation evidence

First scoped Gradle32612 passed in4m50: debug APK, test APK,12 pure tests/0 failures, lint0 errors/87 warnings. The following atomic check/write, durable unconfirmed marker, competing replacement regression and nightly-port isolation refinements were made afterward and still require exact-source compilation. No new native methods or screenshots have executed yet.

### Final source review

Independent strict review is bounded source-clear after two concrete fixes: the ordinary editor now uses the same durable protocol (including new IDs) and the periodic Alarms clock hook cannot dismiss a focused confirmation. Editor scheduling failures keep the saved draft open with truthful status; stale ownership errors get a specific reopen explanation. The native confirmation journey also asserts that the real clock hook leaves its dialog present. Final scoped contract is12 pure tests,8 private storage methods,2 UI methods/15 PNGs. The parent combined build will cover this exact final source; no additional local Gradle job is running.

Exact receiver diff for the security alignment audit:

```diff
diff --git a/app/src/main/java/org/sleepwell/app/AlarmReceiver.java b/app/src/main/java/org/sleepwell/app/AlarmReceiver.java
index 4ff5a28..fcd68de 100644
--- a/app/src/main/java/org/sleepwell/app/AlarmReceiver.java
+++ b/app/src/main/java/org/sleepwell/app/AlarmReceiver.java
@@ -27,8 +27,12 @@ public class AlarmReceiver extends BroadcastReceiver {
             else if(Scheduler.SNOOZE.equals(action)) a=Alarm.read(new JSONObject(Prefs.text(c,"snoozed_"+id,"{}")));
             else if(Scheduler.FIRE.equals(action)) { a=s.alarm(id); if(a==null || !a.enabled) return; if(a.nap!=null||a.napInvalid){if(a.nap!=null)Naps.reconcile(c,a.id);return;} }
             else return;
-            Scheduler.fire(c,a,Scheduler.FIRE.equals(action));
+            synchronized(Scheduler.class){
+                if(Scheduler.FIRE.equals(action)){long deadline=Prefs.of(c).getLong("deadline_"+id,0);long delivered=intent.getLongExtra("occurrence",0);if(!acceptCalendar(delivered,deadline,System.currentTimeMillis()))return;}
+                Scheduler.fire(c,a,Scheduler.FIRE.equals(action));
+            }
         } catch(Exception e) { Prefs.put(c,"lastError","Alarm delivery: "+e.getMessage()); }
     }
+    static boolean acceptCalendar(long delivered,long deadline,long now){return deadline>0&&(delivered==0||delivered==deadline)&&now>=deadline-1000;}
     static boolean backupMatches(boolean ringing,int incoming,int active,String run,String incomingRun){return ringing&&active>0&&incoming==active&&!run.isEmpty()&&run.equals(incomingRun);}
 }
```
