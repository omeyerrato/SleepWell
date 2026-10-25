"""Rebuild the scoped feature inventory from the prior audit plus missing manual subfeatures.

Statuses are implementation assessments; test source never implies a passing result.
"""
from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[1]
SOURCE = "https://sleep.urbandroid.org/docs/"
GROUPS = {
    "Alarms": ("04–12,26", "alarms/alarm_settings.html", ["Alarm.java", "Scheduler.java", "AlarmService.java", "AlarmActivity.java", "AlarmEditorActivity.java"]),
    "Bedtime": ("13–21,27–30", "sleep/sleep_tracking.html", ["TrackingService.java", "NoiseRecorder.java", "BedtimeGuard.java", "MainActivity.java"]),
    "Records": ("02,22–25", "sleep/statistics.html", ["Store.java", "ReportScreen.java", "RecordChart.java", "ChartSeries.java", "GraphView.java", "MainActivity.java"]),
    "Devices": ("32,33,35,36", "services/automation.html", ["Backup.java", "FileSafety.java", "MainActivity.java"]),
}
EXCLUDED = ("Wear OS /", "Garmin", "Xiaomi /", "Pebble", "Legacy Tizen", "Bluetooth oximeters", "SpO₂", "Heart rate / HRV", "Wearable connection", "Sleep masks")

# Additional phone-capable subfeatures from the manual, not just its chapter titles.
EXTRA = [
    ("Reusable alarm defaults", "04,05,31", "alarms/alarm_settings.html", "missing", "Constructor defaults only; persisted global defaults absent."),
    ("Separate sound/vibration delay", "08", "alarms/alarm_settings.html", "missing", "Independent start offsets absent."),
    ("Flip snooze / hardware-key controls", "03", "alarms/alarm_settings.html", "missing", "Needs opt-in sensor/key behavior and accessibility alternatives."),
    ("Backup duration / repeat / timeout", "09", "alarms/backup.html", "partial", "Separate escalation exists; configurable duration, repeats and timeout do not."),
    ("Date / sunrise / goal-derived alarm", "05", "alarms/create_alarm.html", "missing", "Weekday/local time only; date, location-derived sunrise and goal time absent."),
    ("Skip and restore next occurrence", "04", "alarms/create_alarm.html", "missing", "Must persist skipped deadline across reboot and expose undo."),
    ("Dynamic snooze choices / until deadline", "03,09", "alarms/snooze.html", "missing", "Only fixed duration and count presently."),
    ("Wake-check prompt / confirmation deadline / retries", "12", "alarms/captcha.html", "partial", "Current delay rings unless confirmed earlier on Tonight; designed notification-then-deadline sequence absent."),
    ("Multi-task routine / reorder / per-task difficulty", "10", "alarms/captcha.html", "missing", "One task type with rounds is not a heterogeneous routine."),
    ("Lost code fallback / travel bypass", "11", "alarms/captcha.html", "partial", "Emergency dismissal exists; configured fallback and travel inference absent."),
    ("Typed arithmetic / operators / complexity", "03,10", "alarms/captcha.html", "partial", "Stable multiple-choice addition exists; graded complexity and operators absent."),
    ("Sleeping-target visual search", "10", "alarms/captcha.html", "missing", "Original search task and accessible alternative needed."),
    ("Dream diary / mirror text task", "10", "alarms/captcha.html", "missing", "Needs minimum-length checks, journal persistence and mirrored rendering."),
    ("Laughter / instrument microphone task", "10", "alarms/captcha.html", "missing", "Requires microphone consent and detection evaluated beyond raw amplitude."),
    ("Tilt maze / jumping / flags / counting tasks", "10", "alarms/captcha.html", "missing", "Additional phone-only categories; original content needed."),
    ("Walking task", "10", "alarms/captcha.html", "missing", "Step sensor or measured motion required; shaking is not walking."),
    ("Pause/resume within one tracked session", "16,18,02", "sleep/sleep_tracking.html", "partial", "Movement-only setup/start/pause/recreation/resume/stop/reopened-report device flow passes. Same session, persisted pause gaps and monotonic active duration; microphone restart, overnight and broader device evidence remain unverified."),
    ("Charging condition / standby mode", "16,28", "sleep/automatic_sleep_tracking.html", "missing", "Needs real conditions and compatible background service start handling."),
    ("After-fall-asleep / false-start rejection", "28", "sleep/automatic_sleep_tracking.html", "missing", "Needs opt-in activity data and measured false-start behavior."),
    ("Missed-night estimate review / provenance", "28,24", "sleep/sleep_time_estimation.html", "missing", "Duration only; never fabricate sensor/audio graphs for untracked nights."),
    ("Awake triggers from interaction/light/snooze", "18,24,30", "sleep/awake_detection.html", "missing", "Recorded signals exist; tested awake classification absent."),
    ("Flip pause / delayed tracking start", "16,18", "sleep/awake_detection.html", "missing", "Needs gap provenance and monotonic timers."),
    ("Phone breathing / disturbance estimates", "17,02", "sleep/breath_rate.html", "missing", "Requires functioning sonar and independent accuracy evidence; no diagnosis claim."),
    ("Sonar calibration / signal quality / fallback", "17", "sleep/sensors.html", "missing", "Echo processing, speaker/mic compatibility, power budget and device evidence required."),
    ("Per-class sound sensitivity", "20,21", "sleep/sound_recognition.html", "missing", "Snore/talk/cough/baby/laugh requires licensed on-device model and evaluated labels."),
    ("Correct sound labels / favorites / export", "20,35", "sleep/sound_recognition.html", "missing", "Raw clips only; favorites and corrected labels absent."),
    ("Headphone-only cues / intensity / repeat", "21", "sleep/anti_snoring.html", "missing", "Cannot implement cues before snore detection; loud noise is not snoring."),
    ("Sound mixing / gains / fade / local playlist", "19", "sleep/lullaby.html", "partial", "One selected loop with timer exists; concurrent mix/gain/fade absent."),
    ("Meditation / stereo binaural generator", "19", "sleep/lullaby.html", "missing", "Original/licensed audio and actual stereo output needed."),
    ("Dream and travel light cues", "27", "sleep/jetlag.html", "missing", "Needs real scheduling and light output, with estimate disclosures."),
    ("Sleep-day cutoff / naps / date filtering", "22,23", "sleep/statistics.html", "implemented", "Configurable local sleep-day cutoff, date/search/context filters, merged overlapping periods and naps, calendar Week/Month/Year summaries. Missing days excluded; Year shows monthly averages of recorded days. Advanced statistics remain separate work."),
    ("Tag quantities / rename / delete / impact", "22–25", "sleep/tags.html", "partial", "Native per-record add/remove tags and notes persist and feed search. Quantities, global rename/delete and impact analysis remain absent."),
    ("Awake edits / trim / manual record / provenance", "24", "sleep/graph_edit.html", "implemented", "Native draft editor saves tracked trims, awake intervals, manual periods, tags/notes/rating and reset-original. Source, original bounds, zone and raw sensor rows remain distinct. Atomic version checks and persistent save tokens prevent stale saves and duplicate creates."),
    ("Dated goal bank", "15,23", "sleep/goals.html", "missing", "Goal currently affects early wake; no commitment periods or surplus/deficit bank."),
    ("SRI / circular midpoint clustering", "25", "sleep/chrono_jetlag.html", "missing", "Needs coverage thresholds and missing intervals, not treating unknown days as no sleep."),
    ("Own Intent and data-provider contracts", "33", "devs/intent_api.html", "missing", "Versioned contracts and explicit sender access controls required."),
    ("Calendar holiday skip / meeting shift / record write", "33 + calendar setup", "services/google_calendar.html", "missing", "Needs calendar consent, provider availability and deterministic conflicts."),
    ("Health Connect sleep provenance / dedup / deletion", "32", "services/health_connect.html", "missing", "Sleep-only consent and read/write behavior; hardware vitals excluded."),
    ("User-controlled cloud / conflict / audio backup", "35", "services/backup_data.html", "missing", "No cloud transport; external user-owned storage/provider prerequisite."),
    ("Tasker events / webhook retries / MQTT transport", "33", "services/automation.html", "missing", "Needs opted-in transport, credentials, event schemas and actual failure tests."),
    ("Appearance options alter behavior", "36", "ux/personalize.html", "implemented", "Saved local-clock/manual Midnight/Pre-dawn/Dawn themes affect Tonight, dock and system bars. Text size, high-contrast indicators and static press effects are real. This narrower entry excludes smart lights and other personalization features; full screen 36 remains partial."),
    ("Phone flashlight wake-up", "08 + supporting flashlight setting", "alarms/alarm_settings.html", "partial", "Native optional torch source and fake ownership tests; hardware operation untested by request."),
    ("Recorded rhythm / social jetlag", "25", "sleep/chrono_jetlag.html", "partial", "Descriptive saved-night statistics; native execution pending."),
    ("Chronotype estimation", "25", "sleep/chrono_jetlag.html", "missing", "Recording progress is not a chronotype estimator. Alarm-free, recovery and sleep-onset evidence remain missing."),
    ("Motion sensitivity configuration", "16,29", "sleep/sensors.html", "partial", "Saved sensitivity affects next run heuristic inputs; native config verification pending."),
    ("Retained record date calendars", "23,24", "sleep/graph_edit.html", "partial", "Themed retained supporting controls; native evidence pending."),
]

rows = []
group = "Alarms"
for line in (ROOT / "docs/FEATURES.md").read_text(encoding="utf-8").splitlines():
    if line.startswith("## "):
        for key in GROUPS:
            if line[3:].startswith(key):
                group = key
    if not line.startswith("| ") or line.startswith(("| ---", "| Feature")):
        continue
    parts = [c.strip() for c in line.strip().strip("|").split("|")]
    if len(parts) != 3:
        continue
    title, mechanics, notes = parts
    notes = notes.replace(" Wearable haptics pending.", "").replace("five original screens, dark palette", "four root destinations with supporting details, Changing light palette")
    if title.startswith(EXCLUDED):
        continue
    if title == "Community research sharing":
        notes = "Pending: optional user-controlled research export; no vendor endpoint or automatic telemetry is reused."
    title = title.replace(" / wearable API", "")
    screens, source, implementation = GROUPS[group]
    status = "partial" if notes.startswith("Partial") else "missing" if notes.startswith("Pending") else "blocked" if notes.startswith("Out of scope") else "implemented"
    tests = "Not verified for this feature"
    if title == "Repeating weekday alarms":
        tests = "SleepMathTest: midnight, repeat, DST cases"
    elif title == "Noise amplitude":
        tests = "SignalWindowTest + SleepMathTest"
    elif title == "Math":
        tests = "MathTaskTest; AlarmFlowTest device run pending"
    rows.append(dict(feature=title, screens=screens, source=SOURCE+source, implementation=implementation if status in ("partial", "implemented") else [], validation=tests, status=status, notes=notes))

for title, screens, source, status, notes in EXTRA:
    rows.append(dict(feature=title, screens=screens, source=SOURCE+source, implementation=[], validation="Not yet verified", status=status, notes=notes))

for row in rows:
    if row["feature"] == "Appearance options alter behavior":
        row["implementation"]=["AppearanceActivity.java", "Appearance.java", "AppearancePolicy.java", "Design.java", "TonightScreen.java", "NightArc.java"]
        row["validation"]="AppearancePolicyTest (3 passing); AppearanceFlowTest silent device pass 2026-10-22; narrow visual review in DESIGN-REVIEW.md"
    elif row["feature"] == "Dashboard / tabs":
        row["notes"]="Partial: native Tonight/Alarms/Report/Settings roots and real theme settings. User-arranged cards and other home configurations absent; no all-screen acceptance claim."
    elif row["feature"] == "Accelerometer tracking":
        row["implementation"]=["TrackingActivity.java", "TrackingService.java", "TrackingClock.java", "Store.java", "RecordChart.java", "ChartSeries.java"]
        row["screens"]="16,18,02,29"
        row["validation"]="TrackingFlowTest 2 passing on S23 2026-10-23 (movement only, live selection/focus, pause/recreate/resume/reopen); ChartSeriesTest 7 passing; full-night and broader-device coverage incomplete"
        row["notes"]="Measured gravity-filtered RMS: real 1-second live previews, minute saved epochs and partial final windows. Explicit interval starts preserve pauses; unavailable samples stay null. No stage estimator or clinical validation. Narrow movement flow accepted; full screens remain partial."
    elif row["feature"] in ("Manual sleep tracking", "Pause/resume within one tracked session", "Tracking pause / standby / battery-adaptive sampling"):
        row["implementation"]=["TrackingActivity.java", "TrackingService.java", "TrackingClock.java", "SessionTiming.java", "Store.java", "ReportScreen.java"]
        row["screens"]="16,18,02"
        row["validation"]="TrackingFlowTest 2 device passes; TrackingClockTest 5 and SessionTimingTest 3 local passes; StorageMigrationTest 4 device passes. Microphone lifecycle and overnight reliability unverified."
        if row["feature"] == "Manual sleep tracking":
            row["notes"]="Native draft setup, actual sensor availability, Start, live status and saved Report. Back cancels setup drafts. Real movement-only journey verified; audio permission/denial and long-session checks still pending."
        elif row["feature"] == "Tracking pause / standby / battery-adaptive sampling":
            row["notes"]="Partial: real same-session Pause/Resume stops sensors/audio and freezes active duration. Persisted pauses and interrupted recovery exist. Standby/battery-adaptive sampling absent; mic restart hardware evidence pending."
    elif row["feature"] in ("Graph editing / manual records", "Awake edits / trim / manual record / provenance", "Sleep-day cutoff / naps / date filtering", "Statistics", "Manual journal / tags / ratings", "Tag quantities / rename / delete / impact"):
        row["implementation"]=["Records.java", "RecordEntry.java", "RecordDraft.java", "RecordIntervals.java", "RecordTrends.java", "HistoryActivity.java", "RecordEditorActivity.java", "RecordSaves.java", "PeriodTimeline.java", "TrendsChart.java", "RecordSignalsActivity.java"]
        row["screens"]="22–24,02"
        row["validation"]="RecordModelTest 17 local passes; isolated StorageMigration/RecordStorage/RecordReplay/RecordClock 17 device passes; HistoryFlowTest and pending-save RecordRecreationTest silent passes. Strict visual status and exact limits in DESIGN-REVIEW.md."
    elif row["feature"] == "Local data backup":
        row["implementation"]=["Backup.java", "Store.java", "SessionTiming.java", "MainActivity.java"]
        row["validation"]="17 isolated device passes across migration/records/replay/clock suites, including formats 1/2/3, original-row preservation, malformed edit/manual rejection and atomic rollback. Actual document-picker restore pending"
        row["notes"]="Partial: format 3 includes manual/recorded provenance, zones and edit overlays as well as active duration, epoch starts and pauses; formats 1/2 stay readable. Schema 4 adds local create-request tokens; these are not exported. Transactional database restore disables imported alarms; audio, preferences and external URI grants excluded. Full user-facing backup flow is not yet accepted."
    elif row["feature"] == "Retention / favorite album / alternate formats":
        row["notes"]="Partial: seven-day pruning when a new clip-saving session starts, WAV only. Analysis-only and movement-only sessions do not delete clips. Favorites, manual audio export, configurable retention and other formats remain absent."
    elif row["feature"] == "Triggered sound recordings":
        row["implementation"]=["NoiseRecorder.java", "ClipBudget.java", "TrackingService.java", "Store.java"]
        row["validation"]="ClipBudgetTest 2 local passes; microphone/clip lifecycle not exercised under user's sleep-time restriction"
        row["notes"]="Code saves 10-second threshold-triggered WAV clips, shared maximum 60 and 30-second cooldown across pauses. Session clock anchors event timestamps. No classified labels; recording/restart hardware evidence still pending."

# Verified 2026-10-23 plan/bank and four-tab UI milestone. Device delivery remains separate.
PLAN_CODE = ["SleepPlan.java", "SleepPlans.java", "PlanActivity.java", "PlanOperations.java", "SleepDay.java"]
BANK_CODE = PLAN_CODE + ["GoalCommitment.java", "GoalProgress.java", "GoalChart.java", "GoalBankActivity.java"]
PLAN_TESTS = "GoalModelTest 22 local passes; GoalStorageTest 10 isolated device passes; GoalFlowTest silent pass (34.526 s), fake reminder only; actual reminder delivery not tested"
for row in rows:
    title = row["feature"]
    if title == "Do not wake before duration goal":
        row["implementation"] = PLAN_CODE + ["TrackingService.java"]
        row["validation"] = PLAN_TESTS
        row["notes"] = "Saved native plan changes the recorded-duration threshold used for early smart waking. The independent fixed alarm is unaffected; no real alarm was exercised."
    elif title == "Prevent leaving the alarm":
        row.update(status="partial", screens="04–12, Alarm protection", implementation=["BedtimeGuard.java", "AlarmProtections.java", "AlarmProtectionActivity.java", "AlarmService.java"], validation="ab01ba5 source review and AlarmProtectionFlowTest 15.794 s pass: independent global switches, recreation, both entry points, exact personal preferences and unchanged fixture alarm rows. CI39954661465 actual captures retained. No active-alarm enforcement test authorized.", notes="Global opt-in returns from other apps only while a real positive-id alarm service is active. Calls, Settings, emergency and system dialogs remain available; voluntary screen pinning stays separate. No uninstall trap.")
    elif title in ("Gentle volume ramp", "Extra-loud mode", "Sunrise alarm", "Separate sound/vibration delay", "Phone flashlight wake-up"):
        row.update(status="partial", screens="07–08 + supporting flashlight state", implementation=["Alarm.java", "AlarmWakePolicy.java", "AlarmVibration.java", "AlarmPlayback.java", "AlarmAudioSink.java", "AlarmVolumeLease.java", "AlarmService.java", "AlarmSunrise.java", "AlarmTorch.java", "TorchLease.java", "AlarmActivity.java", "AlarmEditorActivity.java", "AlarmRampChart.java", "Scheduler.java"], validation="Candidate: AlarmWakePolicyTest 6, AlarmPlaybackTest 15, AlarmVolumeLeaseTest 10, AlarmLightTest 9 pure passes. Earlier native configuration passes at e00ff55/CI40002966787. New independent vibration timing adds AlarmVibrationTest ten pure cases and requires fresh configuration capture. No actual alarm/effects test authorized.", notes="Native runtime candidate: max-device-volume opt-in, shared continuous planned ramp, sound delay, window-only sunrise and optional steady flashlight with ownership cleanup. Independent vibration delay now shares occurrence time with backup/liveness cleanup; native effects remain unverified. No speaker boost or smart-light control. Occurrence-fenced async activation/backup, durable stream baseline and permanent quiet digital cap. See ALARM-WAKE-UP.md for exact limitations; device output is not certified.")
    elif title == "Prevent uninstall / shutdown":
        row.update(status="partial", screens="Alarm protection", implementation=["PowerMenuGuard.java", "BedtimeGuard.java", "AlarmProtections.java", "AlarmProtectionActivity.java", "AlarmService.java"], validation="PowerMenuGuardTest 11 pure metadata/timing/liveness passes, including unknown window IDs; ab01ba5 source review bounded pass. AlarmProtectionFlowTest 15.794 s configuration-only pass, CI39954661465 with eight actual captures. Samsung menu operation deliberately untested.", notes="Best-effort global Prevent power off opt-in dismisses exact recognised AOSP/Samsung power-menu classes during a live real alarm. Delayed BACK is fenced by last observed window/focus events, not authoritative current input focus. Forced hardware restart, force-stop, powered-off delivery and unknown OEM menus remain possible; uninstall prevention is not implemented. No managed-device provisioning or screen-content retrieval.")
    elif title == "Alarm-only layout / skip occurrence / holiday skipping":
        row.update(status="partial", screens="04, root Alarms", implementation=["MainActivity.java", "AlarmEditorActivity.java", "Design.java", "Icons.java"], validation="ab01ba5: RootVisualTest four selected dock destinations/API36 CI23.342 s and S23 19.215 s. AlarmProtectionFlowTest15.794 s opens fixture time/name editor, verifies checked48dp Alarm on above Save and both protection links. Actual images in verification/2026-10-23-alarm-protection; no scheduling/alarm behavior test.", notes="Dedicated Alarms root shows time, enable switch, name and days; per-alarm settings open inside the editor. Global optional protection is linked from each alarm. Compact list and supporting configuration reviewed separately from full editor/reference parity. Skip/restore occurrence and holiday rules remain missing.")
    elif title == "Dashboard / tabs":
        row["implementation"] = row["implementation"] + ["TonightScreen.java", "NightArc.java", "NightWindow.java", "Icons.java"]
        row["validation"] = "NightWindowTest 10 pure clock/boundary/DST passes; NightArcFlowTest observes the real 30-second foreground tick in manual appearance, actual 0/50/100/next/help/weekly/app-large states and reachable large-text action. Final run and 22 actual captures: CI39940420018 at cfb5e68; exact scope in verification/2026-10-23-rest-window/README.md. Physical RootVisualTest 19.215 s, 10 private captures; SoundSettingsFlowTest 10.929 s, 4 stopped captures with actual light-on-plum system-bar pixel checks. Exact personal preferences/database rows preserved. Phosphor source/geometry/license audit and prior icon rendering pass remain separately recorded. No alarm/physical audio or all-screen acceptance."
        row["notes"] += " Tonight's prior fixed 80% decoration is replaced by a live clock-based rest window with a labeled Now marker, countdown, same-snapshot endpoints/hero, DST resolution, weekly date and themed explanation. It does not measure sleep; refresh neither schedules alarms nor writes preferences. Full screen parity remains partial."
    elif title == "Bedtime reminder":
        row.update(status="partial", screens="13,15", implementation=PLAN_CODE+["Scheduler.java", "AlarmReceiver.java"], validation=PLAN_TESTS, notes="Native linked bedtime/wake/duration plan, wind-down and configurable advance; atomic revision-checked save, legacy minute precision, DST-aware reminder times, permission/error states. Actual OS notification delivery, reboot and denied-permission hardware flows remain unverified during the sleep-time restriction.")
    elif title == "Anti-scrolling guard":
        row.update(status="partial", screens="13,14,15", implementation=PLAN_CODE+["GuardActivity.java", "BedtimeGuard.java", "FocusActivity.java", "MainActivity.java"], validation="GuardFlowTest: draft/recreation/cancel/save/reopen/pause persist with identical personal rows/preferences; GoalModelTest guard-window cases. Accessibility enforcement not exercised.", notes="Dark native per-app draft editor saves selected installed packages and schedule; persistent five-minute pause/Resume state. Existing service consumes saved choices and wind-down-to-wake plan. Device access is absent and clearly disclosed; real redirect/call-exclusion tests remain pending.")
    elif title in ("Goal bank / wagers", "Dated goal bank"):
        row.update(status="implemented", screens="15,23 + goal supporting states", implementation=BANK_CODE, validation=PLAN_TESTS, notes="Free 14/30-day duration or bedtime commitments, immutable targets/time zone/cutoff, dated observed surplus/deficit and cumulative chart, motivation, stop/reopen. Current/future/missing days remain unknown; merged edited/manual durations and non-nap main bedtimes drive results. Financial wagers are not offered. Current portable backups exclude plans/commitments; no clinical sleep-debt or measured-onset claim.")
    elif title == "Lost code fallback / travel bypass":
        row.update(status="partial", implementation=["AlarmActivity.java", "AlarmEditorActivity.java", "MainActivity.java"], validation="Source review and debug/release compilation only; user prohibited alarm testing", notes="Unavailable accelerometer or unavailable/unconfigured NFC falls back to Math. Developer stop is conditional on the debug flag and hidden in release. NFC enrollment remains reachable in Alarms → Alarm options. Configurable multi-task/QR fallback and travel bypass remain missing; new fallback/stop behavior deliberately untested.")

# Native screen 19 source/model work, not a hardware playback or visual acceptance claim.
SOUND_CODE = ["SoundMix.java", "SoundPreferences.java", "AudioCopy.java", "SoundImports.java", "SoundPlayback.java", "SoundOwnership.java", "SleepSoundService.java", "SleepSoundsActivity.java", "SoundSliders.java", "TrackingService.java"]
for row in rows:
    if row["feature"] in ("Sleep sounds / lullabies", "Sound mixing / gains / fade / local playlist"):
        row.update(status="partial", screens="19,13 + import/error states", implementation=SOUND_CODE,
            validation="SoundPlaybackTest 24 and AudioCopyTest 7 local passes. SoundSettingsFlowTest API36 CI18.146s and S23 9.079s: choices/recreation/reopen, no Play, exact personal preference preservation. NativeSoundFlowTest CI25.664s. SoundImportPickerTest CI108.542s: actual picker, invalid/known+unknown oversize/denied/cancel, stale choice and portrait Activity recreation; SoundImportReopenTest CI13.883s: saved read grant after actual process restart, explicit native Play/Pause/Stop/removal with original bytes unchanged. All five pass at cdf17d5 in no-host-audio CI39927399518;26 actual captures. The later More-only icon correction at efa9cae has fresh two-flow CI39928589826 and13 actual captures, not a same-head five-flow claim. No physical audio/import, listening/full-fade or full-screen parity acceptance.",
            notes="Native standalone two-layer player source: Rain/Ocean/Fireplace/Wind or local document plus Brown noise, per-layer gain, normalized output, playing-time timer/fade, notification controls and tracking ownership. Opening/selection does not start a stopped mix. Legacy Rain/Brown gain/timer preserved. Default-size API36 configuration composition has a bounded visual pass; error/recovery layouts also pass. Import uses retained validation, actual100MiB bounds, visible cancel, revision fencing and neutral art; narrower picker/read-grant/restart/playback/removal flow now has native CI evidence. Physical configuration/navigation only verified; Samsung Maximum power saving initially paused app, temporarily unpaused without changing global settings. Physical audio/import, actual tracking ownership and audible loop quality remain unverified. Paused timeout, pending-import process death, missing persistable grants and disk-failure reporting remain open. Timer completion uses only the last three seconds via a 57-second offset. Playlists, guided tracks, sleep-detected fade and external music remain absent. See SOUND-PLAYBACK.md for limits.")

# Current parallel feature work. Keep incomplete native/device gates explicit.
for row in rows:
    title = row["feature"]
    if title in ("Automatic tracking", "After-fall-asleep / false-start rejection"):
        row.update(status="partial", screens="16,18,28",
            implementation=["AutomaticSleep.java", "AutomaticStart.java", "AutomaticTransition.java", "AutomaticCapture.java", "AutomaticTrackingActivity.java", "TrackingService.java", "TrackingActivity.java"],
            validation="AutomaticSleepTest, AutomaticStartTest, AutomaticTransitionTest and AutomaticCaptureTest pure passes. AutomaticTrackingFlowTest passes at e00ff55/CI40002966787 (11.617 s), including separate Save versus fake Arm, optional sound consent, recreation/reopen/error. Its five screenshots still expose first-frame Save clipping; visual correction and fresh run required. Actual Service, sensors, deferred microphone and overnight lifecycle unverified.",
            notes="Explicit four-hour foreground wait: bedtime, smart-window lead-in or 15-minute screen-off low movement. Missing/stale evidence resets; exactly-once recording boundary. Optional sound/clip consent establishes microphone foreground type while visible and defers capture. No boot start, no certainty that phone inactivity means sleep. See AUTOMATIC-TRACKING.md.")
    elif title == "Automatic sound labels":
        row.update(status="partial", screens="18,20,30",
            implementation=["YamnetClassifier.java", "SoundAnalysis.java", "SoundAnalysisStream.java", "CloseResources.java", "NoiseRecorder.java", "Store.java", "SoundEventsActivity.java"],
            validation="SoundAnalysisTest, SoundAnalysisStreamTest and CloseResourcesTest pure passes. SoundAnalysisNativeTest three methods and SoundEventsUiTest pass at e00ff55/CI40002966787. The real CC0 positive PCM-to-production-policy/group/store/reopen gate FAILED: high snoring scores but no saved positive events. Candidate ancestor/speech-family corrections keep thresholds/grouping and require a fresh four-fixture run; microphone capture unverified.",
            notes="Bundled Apache-2.0 offline YAMNet estimates snoring/speech. Policy respects compatible label hierarchy while retaining unrelated-class margin gates; uncertain sounds stay Other. Analysis-only intervals persist without WAV. Cough/baby/laughter categories, user correction and broader validation remain missing. See SOUND-ANALYSIS.md.")
    elif title == "Decreasing snooze / total snooze cap":
        row.update(status="partial", screens="09",
            implementation=["SnoozePolicy.java", "Alarm.java", "AlarmEditorActivity.java", "AlarmActivity.java"],
            validation="SnoozePolicyTest three pure passes; AlarmMenusFlowTest native configuration/recreation/save/reopen/cancel passes at e00ff55/CI40002966787 with sixteen original screenshots, including long selected choices. No physical alarm/snooze execution.",
            notes="Optional per-alarm decreasing delay shares display/schedule policy with a one-minute floor; legacy/import defaults preserve fixed snoozes. Separate total-minute cap remains missing.")
    elif title == "Noise library":
        row.update(status="partial", screens="20",
            implementation=["SoundEventsActivity.java", "SoundEventRows.java", "SoundClips.java", "ClipReviewPlayer.java", "ClipPlayback.java", "ClipWave.java", "ClipSeekView.java", "Store.java"],
            validation="SoundEventsUiTest passes at e00ff55/CI40002966787 for the older category/recreation UI. SoundLibraryStorageTest three methods and the SoundLibraryUiTest playback/favorites/delete method pass at f471c4d/CI40016966655 with five actual PNGs. Retention flow fails on a transition lookup and requires a fresh pass. Schema5 migration, shared-WAV favorites, exact interval/seek and deletion verified only in isolated fixtures. No physical playback/microphone hardware actions.",
            notes="New per-night inline PCM waveform/player, persisted per-event or legacy-recording favorites and explicit estimate-versus-shared-recording deletion are integrated candidates. Classification-only and missing audio never invent a player. Configurable retention preserves audio linked to any favorite. Actual native player output, global album, export and full board07 fidelity remain open.")
    elif title == "Retention / favorite album / alternate formats":
        row.update(status="partial", screens="20,32",
            implementation=["SoundClips.java", "SoundLibrarySettingsActivity.java", "SoundRetentionJob.java", "SoundEventsActivity.java", "NoiseRecorder.java", "HistoryErase.java", "HistoryEraseJobs.java", "HistoryEraseUi.java", "Store.java"],
            validation="SoundLibraryStorageTest three actual SQLite/file methods pass at f471c4d/CI40016966655. Retention UI/lifecycle methods fail during choice-dialog transition and remain open. SoundRetentionLifecycleTest covers one retained immutable operation, recreation/error/retry and disabled controls; fresh native execution pending. PrivacyHistoryFlowTest covers retained background erase, real failed unlink and separate retry preserving newer nights; its source is not a passing-run claim.",
            notes="Additive schema6 preserves existing rows/WAVs and adds favorites plus identity-checked pending deletion journal. Retention can keep1/3/7/14/30days or all, with favorite protection. Background per-run cleanup and explicit policy apply replace hardcoded main-thread pruning. Global album, portable audio export and alternate formats remain incomplete.")
    elif title == "Motion sensitivity configuration":
        row.update(implementation=["MotionSensorActivity.java", "MotionProfile.java", "MotionFilter.java", "MotionPort.java", "MotionPreview.java", "MotionSettings.java", "MotionSignalChart.java", "TrackingService.java", "TrackingActivity.java"], validation="MotionConfigurationTest eight pure cases pass in integrated309-test build. MotionConfigurationFlowTest seven captured states with injected sensor, saved/reopened heuristic consumption and conflict/unavailable/errors await native execution.", notes="Foreground-only real accelerometer preview, actual RMS bins with missing data blank, saved Low/Medium/High gain consumed by smart-wake/automatic-stillness heuristics on the next run. Raw epochs remain unchanged. Medium preserves default; gains are not clinically calibrated. Real sensor and overnight behavior unverified.")
    elif title == "Recorded rhythm / social jetlag":
        row.update(implementation=["SleepRhythmActivity.java", "RhythmStats.java", "RhythmChart.java", "RhythmSettings.java"], validation="RhythmStatsTest fourteen pure passes in integrated build. SleepRhythmFlowTest passes at f471c4d/CI40016966655: seven actual fixture nights, workday edits/recreation/save/reopen/empty and five originals. Strict reference review remains separate; no overnight or clinical claim.", notes="Actual recorded main-night bedtime range, circular workday/free-day midpoint difference and editable wake-day work schedule. Naps excluded, missing dates stay absent, longest main night per sleep day. Thirty-night history progress is descriptive; no clinical chronotype or prevention claim.")
    elif title == "Dream and travel light cues":
        row.update(status="partial", screens="27",
            implementation=["TravelActivity.java", "TravelPlan.java", "TravelPlans.java", "TravelClock.java", "TravelReminders.java", "TravelReminderPolicy.java", "TravelReminderReceiver.java", "TravelBootReceiver.java"],
            validation="TravelPlanTest and TravelReminderPolicyTest eighteen pure passes. TravelPlanFlowTest source covers actual zone/date/time Save/recreation/reopen, fake scheduler/delivery, stale/duplicate/rollback/failure/retry/disable and seven native captures; execution and visual acceptance pending.",
            notes="Saved actual IANA zones, DST-aware clocks/date differences and finite user-chosen destination-time silent daylight reminders. No clinical jet-lag prevention algorithm, experimental flashlight therapy, dream/REM cue output or smart-light integration. Real OS delivery/reboot/permission behavior unverified. See TRAVEL-PLAN.md.")
    elif title in ("Anti-snoring cues", "Headphone-only cues / intensity / repeat"):
        row.update(status="partial", screens="21",
            implementation=["SnoreCuePolicy.java", "SnoreCueOutput.java", "SnoringCuesActivity.java", "NoiseRecorder.java", "ClipBudget.java"],
            validation="SnoreCuePolicyTest pure fake-output tests pass. SnoreCueSettingsNativeTest passes at 8fd7dd9/CI39997627697: saved options, recreation/reopen and four original screenshots with fake output. Positive fixture gate passes at f471c4d/CI40016966655, but this does not verify hardware cues. New preview8pure tests and native configuration compilation pass; five-image native flow pending. Hardware output/effectiveness unverified.",
            notes="Default-off cues after confident snoring estimates, selectable sound/vibration/both, intensity/repeats/sensitivity, two-minute cooldown and thirty-per-session cap. No global-volume writes; alarm/disable/close guards. Headphone-only routing and preview is an explicit low/single lifecycle-bound candidate with fake-output tests; headphone-only routing remains missing. No medical effectiveness claim.")

# Latest bounded native evidence; later source candidates still need their own run.
for row in rows:
    feature = row["feature"]
    if feature in ("Automatic tracking", "After-fall-asleep / false-start rejection"):
        row["validation"] = "AutomaticTrackingFlowTest passes at7d02819/CI40021145034: five originals, Save versus fake Arm, consent, recreation/reopen/error. Strict per-file review accepts complete first-frame Save/Arm in all states. Actual Service, sensors, deferred microphone and overnight lifecycle unverified."
    elif feature == "Automatic sound labels":
        row["validation"] = "SoundAnalysisNativeTest3, SoundEventsUiTest and SoundClassifierFixtureEvaluationTest pass at7d02819/CI40021145034. Actual Android CPU plus production framing/policy/grouping/reopened SQLite: sorts9 and pencil3 saved snoring intervals, countdown1 speech; both speech fixtures0snoring, crowd remains Other. Four CC0 recordings establish compatibility only, not bedroom accuracy or microphone operation. Raw scores committed in verification/2026-10-24-native-configuration."
    elif feature == "Noise library":
        row["validation"] = "SoundLibraryStorageTest3 and SoundLibraryUiTest2 pass at7d02819/CI40021145034. Actual PCM interval waveforms, seek with fake playback, favorites/recreation, shared-WAV deletion and real retention tested; five library and two policy originals. New accent/menu refinement requires fresh captures. No real playback/microphone output."
        row["notes"] = "Actual saved WAV intervals and classified events with independent favorites, no-audio states, explicit estimate/shared-recording deletion and retained background retention. Per-night ZIP export separately verified with fake provider. Global favorite album, alternate formats and real player output remain incomplete. Full board07 acceptance remains partial."
    elif feature == "Retention / favorite album / alternate formats":
        row["implementation"] += ["NightAudioArchive.java", "SoundArchiveJob.java", "SoundArchiveActivity.java", "AudioArchiveWorkspace.java"]
        row["validation"] = "SoundLibraryStorageTest3, SoundLibraryUiTest2, SoundRetentionLifecycleTest1, NightAudioArchiveTest3 and SoundArchiveUiTest1 pass at7d02819/CI40021145034; fourteen originals reviewed. Actual DB/WAV cleanup, retained busy/error/retry and fake-provider ZIP with exact WAV/hash manifest. Hardened AudioArchiveConcurrencyTest3 and SoundArchiveJobTest2 source require new native run. PrivacyHistoryFlowTest remains failed on route/inspection timing."
        row["notes"] = "Additive schema6 with favorites and identity-checked deletion journal; keep1/3/7/14/30days or all, optional favorite protection. Explicit per-night complete-WAV/estimate ZIP through Android Files (provider prerequisite), no restore/import claim. New bounded immutable snapshots and cancellation source await native verification. Global album, alternate formats and real provider behavior remain open."
    elif feature == "Motion sensitivity configuration":
        row["validation"] = "MotionConfigurationTest eight pure cases; MotionConfigurationFlowTest passes9.745s at7d02819/CI40021145034 with seven injected-sensor originals. Saved/reopened profile consumption, conflict/unsupported/error and listener cleanup verified. Tilted phone artwork differs; later helper/green-feedback changes require fresh capture. No hardware or overnight evidence."
    elif feature == "Dream and travel light cues":
        row["validation"] = "TravelPlanTest and TravelReminderPolicyTest eighteen pure passes. TravelPlanFlowTest passes23.825s at7d02819/CI40021145034: actual zones/date/DST Save/recreation/reopen plus fake scheduler/delivery/error/retry/disable, seven originals. Strict review rejects fallback home icon and hidden error/retry; later source fixes require fresh images. OS delivery/reboot unverified."
    elif feature in ("Anti-snoring cues", "Headphone-only cues / intensity / repeat"):
        row["validation"] = "SnoreCuePolicyTest eight pure passes; SnoreCueSettingsNativeTest passes at7d02819/CI40021145034, five focused drawn originals including fake low/single preview and saved/recreated/reopened settings. Blank initial screenshot fixed and individually verified. Real sound fixture gate also passes, but actual cue output/effectiveness unverified."
        row["notes"] = "Default-off confident-estimate cues: sound/vibration/both, intensity/repeats/sensitivity, two-minute cooldown and thirty-per-session cap. No global-volume writes. Explicit preview is forced low/single with separate process budget and lifecycle/alarm guards. Headphone-only routing remains missing in this integrated source; no clinical effectiveness claim."
    elif feature == "Multi-task routine / reorder / per-task difficulty":
        row.update(status="partial", screens="10,03", implementation=["ChallengeRoutine.java", "ChallengeRoutineJson.java", "Alarm.java", "AlarmEditorActivity.java", "AlarmActivity.java", "Backup.java"], validation="ChallengeRoutineTest ten pure passes on agent branch. ChallengeRoutineFlowTest6captured states and updated AlarmMenusFlowTest are compiled candidates awaiting native execution. No actual alarm/challenge output executed by user request.", notes="Additive ordered1–8task routine, add/configure/reorder/remove and per-task rounds or shake target. Runtime cursor requires every task/round, fences stale occurrence/unit callbacks and preserves legacy scalar alarms. Supported Math/Memory/Sequence/Retype/Shake/NFC; QR/Walking/new task types and graded mathematical complexity remain missing. Screen beauty and actual runtime verification remain open.")
    elif feature == "Retained record date calendars":
        row.update(status="partial", screens="23,24", implementation=["CalendarMonth.java", "RecordCalendar.java", "HistoryActivity.java", "RecordEditorActivity.java"], validation="CalendarMonthTest4 and RecordModelTest17 pure passes plus native compilation. RecordCalendarFlowTest seven originals, pending/recreate/cancel/filter/edit/save with DST offset/seconds/milliseconds verification await execution.", notes="Cream/Inter date selection replaces stock History filter/trend and record-editor calendars. Pending date/month and surrounding time-form draft retained across recreation; Use/Cancel/All dates explicit. No premature persistence; RecordDraft.instant still rejects invalid DST gaps and ambiguous offsets. Native evidence and visual acceptance pending.")

# Current source additions remain candidates until their exact-head native run.
for row in rows:
    feature = row["feature"]
    if feature == "Walking task":
        row.update(status="partial", implementation=["WalkSteps.java", "WalkSession.java", "WalkPlatform.java", "WalkPermission.java", "AlarmEditorActivity.java", "AlarmActivity.java", "ChallengeRoutineJson.java"],
            validation="WalkSessionTest thirteen cases pass in the integrated358-test build, including ownership-loss release and unregister retry. WalkConfigurationFlowTest passes13.954s at64ae8d2/CI40033681541 with six individually reviewed permission/target/recreate/save/reopen originals and real private SQLite/JSON. No actual alarm, steps, sensor or permission dialog executed.",
            notes="Actual Android step detector/counter adapter; contextual editor permission, per-task5–100 steps, fresh counter baseline, stale occurrence/unit/listener fences and fallback for missing permission/sensor. Walk consumes one routine unit and never uses shake magnitude. Original Phosphor footprints icon bundled. Real hardware recognition and full alarm runtime remain unverified.")
    elif feature in ("Anti-snoring cues", "Headphone-only cues / intensity / repeat"):
        row["implementation"] += ["HeadphoneCue.java", "HeadphoneSound.java", "SnoreSoundRouteActivity.java"]
        row["validation"] = "HeadphoneCueTest eleven cases pass in integrated358-test build. At64ae8d2/CI40033681541, HeadphoneCueOutputTest2 passes0.142s and SnoreSoundRouteFlowTest1 passes11.571s with five individually reviewed originals. Fake worker/provider only, exact preference restoration; no AudioTrack/vibration/hardware route output tested. SnoreCueSettingsNativeTest also passes; its later Preview glyph styling needs new originals."
        row["notes"] = "Default-off confident-estimate cues retain sound/vibration/both, intensity/repeats/sensitivity, budget/cooldown and low/single preview. Optional headphone sound requires an accepted wired/USB headset or BLE headset and checks actual single route during bounded chunks. Ambiguous A2DP/SCO routes visibly excluded. OS rerouting can still cause a transient speaker leak; vibration is independent. No medical effectiveness claim."
    elif feature == "Multi-task routine / reorder / per-task difficulty":
        row["validation"] = "ChallengeRoutineFlowTest passes at d6389dc/CI40027277488: six individually reviewed originals, real JSON/privateDB/reorder/cancel/recreate/save/reopen, malformed backup rejection. New Walk source extends this candidate; native configuration still needs fresh run. Actual ringing/challenge execution remains unverified."
        row["notes"] = "Additive ordered1–8task routine and per-task rounds/targets, fenced runtime progression preserving legacy scalar alarms. Math/Memory/Sequence/Retype/Shake/NFC plus new step-sensor Walk candidate. QR, silent interactive routine preview and graded mathematical complexity remain missing."
    elif feature == "Retained record date calendars":
        row["validation"] = "RecordCalendarFlowTest passes at d6389dc/CI40027277488 with seven individually reviewed originals: pending date/recreate/cancel/filter/edit/save, actual DST choice and unchanged seconds/milliseconds. Supporting time form has remaining visual spacing/plain-seconds input differences."
    elif feature == "Retention / favorite album / alternate formats":
        row["validation"] = "Storage/library/retention/export pass at64ae8d2/CI40033681541, including AudioArchiveConcurrencyTest3 and SoundArchiveJobTest2 with actual SQLite/WAV/ZIP and fake provider. PrivacyHistoryFlowTest now passes26.046s, including late pending-journal reentry/new-night preservation. Five lower-scroll originals reviewed; checked gray favorites is a later style candidate."
        row["implementation"] += ["FavoriteSoundRows.java", "FavoriteSoundsActivity.java"]
        row["validation"] += " New FavoriteSoundsStorageTest3 and FavoriteSoundsFlowTest1/fourteen originals are compiled candidates awaiting native execution."
        row["notes"] = "Additive schema6 favorite-protected retention, identity-checked delete journal, retained background cleanup and explicit complete-raw-clip ZIP with size/hash manifest. New nights survive audio-only Retry. New global favorite album uses existing stars with actual date/category filters,50-row pages, explicit waveform/interval playback and missing/edited states; unstar never deletes originals. Alternate formats, portable favorite backup and real player/provider remain open. Personal-phone schema4→6 update preserved original rows/preferences."
    elif feature == "Motion sensitivity configuration":
        row["validation"] = "MotionConfigurationFlowTest passes at d6389dc/CI40027277488 with seven individually reviewed originals; saved profile, conflict/unsupported/error and cleanup verified using fake sensors. Helper/error/footer visibility now passes scoped review. Flat phone glyph still differs from the reference illustration; real hardware/overnight evidence absent."
    elif feature == "Dream and travel light cues":
        row["validation"] = "TravelPlanFlowTest passes at d6389dc/CI40027277488 with seven individually reviewed originals: saved zones/DST/date and fake reminder delivery/error/retry/disable. Actual error/Retry and genuine house icon now visible. Real OS delivery/reboot and light output remain unverified."
    elif feature == "Accelerometer tracking":
        row["implementation"] += ["ChartTicks.java"]
        row["validation"] += "; ChartTicksTest ten integrated passes for aligned clock/12–24h/DST/fractional seconds/no-overlap labels. ChartClockFlowTest FAILS initial fixture focus at64ae8d2/CI40033681541 before all four images; correction/new native originals required."
    elif feature == "Missed-night estimate review / provenance":
        row.update(status="partial", screens="28 + supporting review", implementation=["MissedNight.java", "MissedNightUsage.java", "MissedNightRecords.java", "MissedNightActivity.java", "MissedNightChart.java"],
            validation="MissedNightTest ten pure cases, scoped debug/test APK and lint pass on agent branch. MissedNightFlowTest two private-DB/fake-Usage-Access methods and eleven review/save/recreate/error originals are compiled candidates; native execution and real Android usage query unverified.",
            notes="Explicit Usage Access; on-demand actual paired screen-off/on events for one of the last three nights. User reviews/edits before manual-record insertion; rechecks original evidence, grant, overlap/open records and idempotence. No background capture, inferred stages, movement or audio. Phone inactivity does not establish sleep. Primary hook in Automatic tracking; permission/empty/incomplete/error/conflict and saved states present.")

# The latest real native evidence and newer, explicitly unaccepted source.
for row in rows:
    feature = row["feature"]
    if feature in ("Typed arithmetic / operators / complexity", "Math"):
        row.update(status="partial", screens="03,10", implementation=["MathSettings.java", "MathSettingsJson.java", "MathTask.java", "ChallengeRoutine.java", "ChallengeRoutineJson.java", "AlarmEditorActivity.java", "AlarmActivity.java", "Backup.java"],
            validation="GradedMathTest ten focused cases plus all378 agent-branch pure tests pass, debug/test APK and lint pass. GradedMathConfigurationTest1/seven native originals await execution. Legacy seeded outputs/cache validated, no AlarmActivity or real challenge execution by request.",
            notes="Per-task grade, chosen +/minus/multiply/divide operators, choices or typed whole-number answer, and independent rounds. Actual runtime consumes the settings; division is whole-number and subtraction nonnegative. Omitted default JSON preserves legacy Medium/addition/choice behavior. Strict cross-task/invalid JSON backup validation. Full runtime/device and new visual acceptance remain open.")
    elif feature == "Automatic sound labels":
        row["implementation"] += ["SoundCategories.java"]
        row["validation"] += " The old four-fixture gate also passes atbb582cf/CI40037791812. New SoundCategoryPolicyTest7 and30 focused branch cases pass; SoundCategoryFixtureEvaluationTest1/eight licensed real PCM files require fresh Android CPU/group/reopened-Store evidence."
        row["notes"] = "Bundled offline YAMNet snore/speech estimates remain. New conservative cough/throat-clear, baby-cry and laughter source is a candidate; existing thresholds/two-window grouping preserved. Named positive and cross-negative evidence is required before UI/filter integration or acceptance. No broader bedroom/clinical accuracy or microphone proof. User correction/per-class calibration remain missing."
    elif feature == "Noise library":
        row["implementation"] += ["FavoriteSoundRows.java", "FavoriteSoundsActivity.java", "ClipReviewAccess.java"]
        row["validation"] += " FavoriteSoundsStorageTest3 and FavoriteSoundsFlowTest1 pass atbb582cf/CI40037791812 (0.549s/20.487s), fourteen individually reviewed originals. New ClipReviewAccessTest1 for stale saved trims awaits native execution; real player output remains unverified."
        row["notes"] = "Real per-night events and global independent stars with date/category filters, bounded paging, PCM wave/seek, unstar/save/reopen and absent/outside-edited audio states. Fake playback only in native tests. New shared guard rechecks current saved period at open/Play/Resume; not atomic against later concurrent edits while already playing. Alternate formats and physical playback remain open."
    elif feature == "Retention / favorite album / alternate formats":
        row["validation"] = "Library/retention/export/privacy pass atbb582cf/CI40037791812; new FavoriteSoundsStorageTest3/FlowTest1 pass with real SQLite/WAV and fake playback,14 originals individually reviewed. ClipReviewAccessTest1 stale-trim regression is a later compiled-source candidate awaiting native execution."
    elif feature == "Correct sound labels / favorites / export":
        row.update(status="partial", implementation=["SoundClips.java", "FavoriteSoundRows.java", "FavoriteSoundsActivity.java", "NightAudioArchive.java", "SoundArchiveJob.java"],
            validation="FavoriteSoundsStorageTest3/FlowTest1, SoundLibraryStorageTest3 and actual synthetic WAV/ZIP native tests pass atbb582cf/CI40037791812. Fake playback/provider only; real external output unverified.",
            notes="Persisted independent event/whole-recording favorites, global filtered album, and explicit complete-raw-WAV ZIP with interval/hash metadata exist. User label correction and portable favorite restore remain missing; shared WAV export is explicitly disclosed.")
    elif feature == "Missed-night estimate review / provenance":
        row["validation"] = "MissedNightTest10 pure passes; MissedNightFlowTest2 passes21.739s atbb582cf/CI40037791812: reviewed/edited/recreated save, no-write-before-review, idempotence/tombstones/overlap/permission/evidence failures,11 originals. Ten supporting states pass narrowly; denied image rejected for a default focus slab. Later touch-mode/viewport focus fixes need new images. Actual Android Usage Access queries still unverified."
    elif feature == "Retained record date calendars":
        row["validation"] = "Prior seven-frame native journey passes atd6389dc. Latest RecordCalendarFlowTest FAILS19.911s atbb582cf/CI40037791812, five images only, after recreated nested date dialog; no exact failing-assertion diagnostic. New owned-window touch, outer-date/focus wait and diagnostic preservation await rerun; all DST/millisecond/save assertions retained."
    elif feature == "Accelerometer tracking":
        row["validation"] += " ChartClockFlowTest now passes4.366s atbb582cf/CI40037791812 with four actual images and retained selection/focus; visual review rejects default focus slabs and long fixture title. Deliberate focus/input-mode cleanup requires new captures."

# Exact-head native results supersede earlier candidate observations above.
for row in rows:
    feature = row["feature"]
    if feature in ("Typed arithmetic / operators / complexity", "Math"):
        row["validation"] = "GradedMathTest12 and integrated387 pure tests pass. GradedMathConfigurationTest1 PASS23.318s ata8362a1/CI40047180910: all7 original settings/recreate/save/reopen/error frames, private SQLite/JSON and bad-import preservation. Actual card geometry assertions pass; strict review clears clipped borders in all6 detail frames. Actual ringing/keyboard execution remains unverified."
    elif feature == "Automatic sound labels":
        row["validation"] = "SoundAnalysisNativeTest3 and old SoundClassifierFixtureEvaluationTest1 pass at12c8c0f/CI40042183746. New SoundCategoryPolicyTest7 passes pure rules, but SoundCategoryFixtureEvaluationTest1 FAILS actual laughter positive:0 events in initial30s. Cough1, baby-cry2 (also laughter2), retained snore9/3 and solo speech1; zero new classes on4 old fixtures. Full54.61s laughter preview also FAILS atCI40047180910:0 laughter, maximum family score0.26171875, unchanged thresholds/grouping. Independent solo-source evaluation remains pending. Raw all-window CPU scores/events preserved in verification/2026-10-24-math-chart-calendar."
        row["notes"] = "Offline snore/speech estimates exist. Additional cough/throat-clear, baby-cry and laughter remain unaccepted candidates; no broad category UI/filter integration until actual PCM positives/cross-negatives pass. Mixed infant classification and uploader descriptions cannot prove accuracy. User label correction, per-class calibration and bedroom recognition remain unverified or missing."
    elif feature == "Noise library":
        row["validation"] = "FavoriteSoundsStorageTest3 PASS0.604s and FavoriteSoundsFlowTest1 PASS21.562s at12c8c0f/CI40042183746; fourteen prior individually reviewed originals, fake playback only. ClipReviewAccessTest1 PASS0.364s with actual private SQLite/WAV before/after saved start/end trims, no player allocation. AtCI40047180910 FavoriteSoundsFlowTest1 passes22.302s; strict review clears paused/retry Play contrast in2 fresh originals. Real player output remains unverified."
    elif feature == "Retention / favorite album / alternate formats":
        row["validation"] = "At12c8c0f/CI40042183746, library/favorite storage and UI, SoundRetentionLifecycleTest1, NightAudioArchiveTest3 and SoundArchiveJobTest2 PASS. Latest PrivacyHistoryFlowTest FAILS20.799s atCI40047180910 after Keep/error recreation: full168px row remains Checking because fixture storage hold can block recreated INSPECT. Later gate-after-confirmation and real-error-text waits need native proof; no erase/retry acceptance from this run. ClipReviewAccessTest1 stale-trim guard PASS0.364s."
    elif feature == "Missed-night estimate review / provenance":
        row["validation"] = "MissedNightTest10 pure pass; MissedNightFlowTest2 PASS21.570s at12c8c0f/CI40042183746,11 originals, real private SQLite/fake Usage Access, reviewed save/idempotence/conflict/evidence/permission failures. Strict review clears denied-state gray focus body; prior ten supporting-state findings retained. Actual Android Usage Access queries remain unverified."
    elif feature == "Retained record date calendars":
        row["validation"] = "RecordCalendarFlowTest1 PASS33.085s at12c8c0f/CI40042183746 with all7 individually reviewed original calendars: owned-dialog cancellation, recreation, DST/zone offsets, exact seconds/milliseconds and save/reopen preserved. Narrow calendar visual gate passes. No generic picker or full36-screen acceptance inferred."
    elif feature == "Accelerometer tracking":
        row["validation"] += " Latest ChartClockFlowTest1 PASS4.590s at12c8c0f/CI40042183746 with4 originals; strict review clears gray focus slabs and fold-title clipping while retaining actual selection/clock/DST/short-second labels. This supersedes earlier rejected chart styling only; hardware tracking accuracy remains unverified."
    elif feature == "Walking task":
        row["validation"] = "WalkSessionTest13 pure cases pass. WalkConfigurationFlowTest1 PASS13.106s at12c8c0f/CI40042183746 with6 individually reviewed originals; obsolete permission error is cleared in ready/unavailable/reopened states. Actual private SQLite/JSON persists per-task targets. No actual alarm, step sensor or physical permission dialog execution."

# Offline morning-code source is integrated; inert native acceptance is still pending.
for row in rows:
    feature = row["feature"]
    if feature == "QR/barcode sequences":
        row.update(status="partial", screens="10,11,03", implementation=["MorningCode.java", "MorningCodeJson.java", "QrDecoder.java", "QrLuma.java", "QrCamera.java", "QrCameraAccess.java", "QrScanSession.java", "QrProof.java", "QrCodeActivity.java", "ChallengeRoutine.java", "AlarmEditorActivity.java", "AlarmActivity.java"],
            validation="MorningCodeTest, QrDecoderTest, QrLumaTest, QrProofTest and QrScanSessionTest:14 new pure passes on401-test branch, debug/native APK and lint pass. Actual generated UTF-8/rotated/inverted QR bytes decode. QrCodeConfigurationTest1 PASS24.619s ata8362a1/CI40047180910 with12 individually reviewed inert fake-camera/access originals. Draft/save/reopen/private backup invariants pass; actual camera/ringing transition and full reference-screen fidelity remain unverified.",
            notes="Explicit camera enrollment stores only location label and SHA256, never raw QR text or URLs. Multiple ordered distinct QR tasks can each own1 routine unit; nonce/run/unit/hash and current occurrence fence completion. Saved one-question graded Math fallback; no permission dialog while ringing. Optional CameraX/ZXing offline dependencies, missing permission/camera/error/cancel states and strict backup JSON. General barcode formats, generated printable codes and actual CameraX compatibility remain missing or unverified.")
    elif feature == "Lost code fallback / travel bypass":
        row.update(status="partial", implementation=["MorningCode.java", "QrCodeActivity.java", "QrProof.java", "AlarmActivity.java", "AlarmEditorActivity.java"],
            validation="QR14 pure tests and401-test branch build pass. Configured fallback JSON and one-unit ownership tested in pure source; QrCodeConfigurationTest1 PASS24.619s ata8362a1 with12 inert enrollment/error/fallback/save/reopen originals. No actual alarm/ringing/camera test under user restriction.",
            notes="Configured QR fallback selects grade/operators/typed or choices and consumes exactly1 question. Missing access avoids permission prompts while ringing. Existing unavailable step/NFC/sensor Math fallback retained; release hides debug developer stop. Travel inference/bypass and full live behavior remain unverified or missing.")
    elif feature == "Multi-task routine / reorder / per-task difficulty":
        row["validation"] += " Later gradedMath1 native7frame configuration passes at12c8c0f; new QR task metadata/decoder/ownership14 pure cases and12frame inert configuration gate pass ata8362a1. All6 routine originals reviewed with cream icon wells/action divider aligned; actual ordered runtime remains unverified."
        row["notes"] = "Additive ordered1–8task routine, per-task rounds/targets/graded Math and newly enrolled QR code, step-sensor Walk, Math/Memory/Sequence/Retype/Shake/NFC. Runtime callbacks fence occurrence/unit; legacy scalar behavior remains. QR actual camera/live challenge, silent interactive routine preview and full visual fidelity remain unverified or missing."

# Board03 tone flow: integrated source, real asset data; output/UI acceptance is separate.
for row in rows:
    feature = row["feature"]
    if feature in ("Built-in sounds", "Device ringtone"):
        row.update(status="partial", screens="07", implementation=["AlarmTones.java", "ToneWaveform.java", "AlarmTonePreview.java", "ToneDecoderLease.java", "AlarmToneAudio.java", "AlarmToneImport.java", "AlarmToneScreen.java", "AlarmAudioSink.java", "AlarmEditorActivity.java"],
            validation="AlarmTonePreviewTest8, ToneWaveformTest3 and ToneDecoderLeaseTest3 pass on agent branch; debug/native APK and lint pass. Original Daybreak/Pulse SHA256 bytes preserved. AlarmToneAssetsTest1/AlarmToneGrantTest1/AlarmToneFlowTest1 and16 original UI states await native execution. Peer decoder/grant lifecycle findings corrected; actual MediaPlayer/SAF/OEM behavior unverified.",
            notes="Actual Daybreak/Pulse/original Chime PCM and32 measured peak bins; no manufactured external/default waves. Separate0–25 percent digital preview, explicit Play/Pause/Stop, process decoder lease,8s prepare and20s wall bounds, focus/noisy/lifecycle/alarm-occupancy guards without global volume writes. Validated explicit persisted content imports, source cancellation/rotation fencing and shared grant preservation. Device alarm remains under My audio. Full screen fidelity, actual output/provider compatibility and broad tone catalogue remain open.")

# Two-stage wake checks are integrated, with native action/visual proof pending.
for row in rows:
    if row["feature"] in ("Post-dismiss wake check", "Wake-check prompt / confirmation deadline / retries"):
        row.update(status="partial", screens="12,09", implementation=["WakeCheckState.java", "WakeCheckEngine.java", "WakeCheckStorage.java", "WakeChecks.java", "WakeCheckReceiver.java", "WakeCheckActivity.java", "AlarmActivity.java", "Scheduler.java", "AlarmReceiver.java", "BootReceiver.java", "MainActivity.java", "TonightScreen.java"],
            validation="WakeCheckEngineTest16 focused fake-clock/scheduler/notification/re-ring cases and integrated431-test full build pass; debug/native/release APK and lint pass. Source peer review clear; post-save cancellation and expired confirmation fixes compiled. WakeCheckFlowTest2/12 private-state originals with fake scheduling/delivery/re-ring and actual Editor save/failure preservation await native execution. No real alarm, notification, clock, power or hardware tests.",
            notes="Saved check delay now leads to a silent prompt and one persisted2-minute confirmation deadline. Same-boot elapsed time, reboot wall reconciliation, nonce/stage immutable pending intents, stale/duplicate/Off/deletion/supersession guards, explicit confirm and durable at-most-once expiry claim. Miss restarts frozen saved alarm through existing runtime. Off cancellation occurs only after successful persisted edit. Notification/exact errors and retry preserve deadline; silent notifications may not peek, full-screen visibility is OS-controlled. Old checks lack original nonce/due time, and process death after expiry claim can prevent re-ring; these limitations remain documented. Real delivery and board04 fidelity remain unverified.")
    elif row["feature"] == "Automatic sound labels":
        row["validation"] += " New10-fixture source retains all8 prior recordings and adds whole62.621s solo laughter/72.906s independent infant PCM selected before inference; test-only3MiB/80s bounds, all hashes/counts and521 label indices verified. Mandatory new positives/cross-negatives await actual CPU execution; model/threshold/grouping unchanged."

# Shared fallback controls are a later UI candidate, outside the frozen native run.
for row in rows:
    if row["feature"] in ("Typed arithmetic / operators / complexity", "Math", "QR/barcode sequences"):
        row["implementation"] += ["MathOperatorGrid.java"]
        row["validation"] += " Later14c4d5a shares the Math/QR operator cards; scoped debug/native APK and lint pass1m23s. QR last-operator error plus card geometry require13 original frames in a future run; current frozen9188e78 still requires12."

# CI40051144096 is terminal: retain its failures separately from later fixes.
for row in rows:
    feature = row["feature"]
    if feature in ("Built-in sounds", "Device ringtone"):
        row["validation"] = "Integrated431 pure tests/53 suites and debug/native/release/lint pass at cce7a60. At9188e78/CI40051144096 AlarmToneGrantTest1 PASS0.087s, but AlarmToneAssetsTest1 fails its incorrect blanket4s expectation (original Daybreak/Pulse8s, Chime4s) and AlarmToneFlowTest1 stops after3 of16 expected frames. Strict review rejects preview controls clipped behind footer. Later d4c08c7 compacts real controls, keeps48dp targets, reveals errors, checks complete geometry, and corrects exact per-asset duration without changing PCM. Fresh native proof required; actual MediaPlayer/SAF/OEM output remains unverified."
    elif feature in ("Post-dismiss wake check", "Wake-check prompt / confirmation deadline / retries"):
        row["validation"] = "WakeCheckEngineTest16 and integrated431-test build pass. WakeCheckFlowTest2 PASS21.435s at9188e78/CI40051144096 with12 originals: private durable states, fake scheduling/delivery/re-ring, and failed/successful Editor save preservation. Strict visual review FAILS left-aligned countdown and button glyph grouping plus stale fixture Toast. Later4d07378 centers actual timer/button and waits Toast dismissal; fresh capture required. Actual notification/alarm delivery remains unverified."
    elif feature == "Automatic tracking":
        row["validation"] += " Latest AutomaticTrackingFlowTest1 FAIL at9188e78/CI40051144096 on initial footer visibility; diagnostic original shows full Save, indicating assertion ran before layout. Later d4c08c7 waits actual current layout then retains complete bounds and persistence assertions; fresh native pass still required."
    elif feature == "Gentle volume ramp":
        row["validation"] += " Latest AlarmWakeSettingsTest1 FAIL at9188e78/CI40051144096 while finding Extra loud during options-dialog transition, before ramp assertions. Later d4c08c7 waits fresh dialog descendants. No actual effects test or new configuration pass claimed."
    elif feature in ("QR/barcode sequences", "Lost code fallback / travel bypass"):
        row["validation"] += " Later9188e78/CI40051144096 QR flow FAILS on stale native control during fallback rerender; prior a836 pass remains narrower evidence. d4c08c7 re-finds controls after each action and retries only pre-click StaleObjectException; shared14c4d5a grid and13-frame geometry/error contract remain pending."
    elif feature == "Automatic sound labels":
        row["validation"] += " Actual10-input CPU evaluation at9188e78/CI40051144096 FAILS independent solo laughter positivity: whole62.621s yields0 persisted laughter, max family0.5859375 below unchanged0.65 gate. Independent72.906s infant source persists5 babycry/0 laughter/cough/snore. Original crowd0, ambiguous bonehead2cry+2laugh, cough1 and snore9/3 observations retained. No model, threshold or grouping relaxation; laughter recognition remains unaccepted."
    elif feature == "Retention / favorite album / alternate formats":
        row["validation"] += " PrivacyHistoryFlowTest1 now PASS35.701s at9188e78/CI40051144096; all5 originals reviewed: actual storage error, busy erase, pending file cleanup, done and keep/retry preservation. This supersedes the earlier rejected inspection-race images only; other stated limitations remain."

# Narrow local build for the latest geometry candidate does not imply native acceptance.
for row in rows:
    if row["feature"] in ("Built-in sounds", "Device ringtone", "Post-dismiss wake check", "Wake-check prompt / confirmation deadline / retries", "Automatic tracking", "Gentle volume ramp", "QR/barcode sequences", "Lost code fallback / travel bypass"):
        row["validation"] += " Combinedbed8aba debug/native APK and lint PASS1m26s (0errors/86warnings); no fresh native run or domain-test rerun inferred."

# Latest exact-head results and two newly integrated vertical flows.
for row in rows:
    feature = row["feature"]
    if feature == "Nap timer":
        row.update(status="partial", screens="26 (board09 middle) + Tonight/Alarms/Wind-down", implementation=["NapPlan.java", "NapPlanJson.java", "NapEngine.java", "Naps.java", "NapReceiver.java", "NapActivity.java", "Alarm.java", "AlarmActivity.java", "Scheduler.java", "AlarmReceiver.java", "WakeChecks.java", "TrackingService.java", "NightWindow.java", "NightArc.java", "TonightScreen.java", "MainActivity.java", "AlarmEditorActivity.java", "Backup.java"],
            validation="NapEngineTest16 and NapWindowTest6 plus existing NightWindowTest10 pass in the combined453-test build atfeb38ec. Debug/native/release APKs and lint pass (0errors/87warnings). NapFlowTest3 and NapIntegrationFlowTest1 require23 originals and private fake-port persistence/cancel/restart/smart/migration checks; compiled, not yet executed. No actual delivery/hardware proof.",
            notes="Saved exact relative timer and scheduled start,10/20/30/90 presets, owned cancel/restart/list, real shared sound settings and deferred delete after dismiss or matching wake confirmation. Main/Tonight/Wind-down use projected relative deadlines and a separately labelled nap arc; inactive restored timers require explicit fresh Start. Smart wake uses already-active phone movement, never starts sensors/microphone here and is not stage detection. Strict optional Alarm JSON, immutable nonce job, durable delivery claim and disabled portable restore. Crash after claim, overlapping alarm, force-stop/OEM and permission limits remain. Distinct minimum-rest Power Nap remains missing; see NAPS.md.")
    elif feature == "Correct sound labels / favorites / export":
        row.update(status="partial", screens="20 (board07 middle), favorites album + supporting label editor", implementation=["SoundLabels.java", "SoundLabelActivity.java", "SoundLabelJob.java", "SoundEventBackup.java", "Store.java", "Backup.java", "SoundEventRows.java", "FavoriteSoundRows.java", "ReportSounds.java", "ReportScreen.java", "NightAudioArchive.java"],
            validation="Integrated935c773 source peer-reviewed including interrupted-night backup repair. Combined453 pure tests/55suites, debug/native/release APKs and lint pass atfeb38ec; SoundLabelStorageTest9 and SoundLabelFlowTest3/18 originals compiled but not yet executed. No real playback/microphone/phone actions.",
            notes="Schema7 nullable user_category and increment-only label_revision preserve original category/score/model/interval/clip/WAV; checked save/reset, retained worker/recreate/error/retry, stale/trim/deletion/ABA fences. Effective library/favorites/report counts clearly distinguish user choice from model estimates. Strict format4 analysis-only metadata and original/user provenance in ZIP; no portable WAV link. Old formats1–3 accepted. Recovery uses actual durable sound endpoints without invented active_ms; already recovered interrupted tails have explicit bounded provenance, preserving original times/end. Export self-validates and fits the existing8MiB pretty-UTF8 importer; portable legacy clip favorites and alternate formats remain incomplete. See SOUND-LABEL-CORRECTIONS.md.")
    elif feature == "Local data backup":
        row["implementation"] += ["SoundEventBackup.java", "NapPlanJson.java"]
        row["validation"] += " Format4/schema7 label metadata and inactive Nap JSON restore are integrated atfeb38ec; complete453-test build passes. New actual SQLite SoundLabelStorageTest9 and NapFlowTest3 portable roundtrips await native execution."
        row["notes"] += " New portable event metadata preserves manual corrections and original estimates without clip links;8MiB pretty-UTF8 export bound matches importer. Nap imports are inactive and need explicit fresh Start. Provider IO/lifecycle and broad backup completeness still need their own verification."
    elif feature in ("QR/barcode sequences", "Lost code fallback / travel bypass"):
        row["validation"] += " Latest QrCodeConfigurationTest1 PASS26.218s atc48fedd/CI40054511743: all13 originals individually reviewed, shared card geometry/last-operator error/save/reopen proven with fake camera/access. Actual scanner/ringing and full board04 segmented fidelity remain open."
    elif feature == "Automatic tracking":
        row["validation"] += " Latest AutomaticTrackingFlowTest1 PASS15.459s atc48fedd/CI40054511743: all5 named originals individually reviewed, complete Save/Arm/error geometry and private preferences/fake arming. This supersedes initial-layout failure only; recurring behavior and actual OS delivery are not covered."
    elif feature in ("Built-in sounds", "Device ringtone"):
        row["validation"] += " Atc48fedd/CI40054511743 assets1 PASS0.401s and grants1 PASS0.147s; ToneFlow fails new helper null-label lookup before named captures. Diagnostic shows complete preview/footer but full flow remains rejected. Later1971b2b null-safe helper andfeb38ec icon/gap polish compile; fresh native run required."
    elif feature == "Gentle volume ramp":
        row["validation"] += " c48fedd WakeSettings reaches7 named states then stale-node failure after reopen. Later1971b2b uses lazy native selectors and stronger48dp complete bounds; combined build passes, fresh full configuration proof required."
    elif feature in ("Post-dismiss wake check", "Wake-check prompt / confirmation deadline / retries"):
        row["validation"] += " Atc48fedd WakeCheckFlowTest2 passes again and12 originals were reviewed: centered timer/button and error-copy correction accepted narrowly; waiting/prompt remain rejected for stale injected-save Toast. Laterfeb38ec uses a persistent Editor inline save error and adds13th supporting error capture, with failed/successful save ownership preserved. Compiled, fresh original images required."

# Recurring starts are opt-in and deliberately movement-only; native delivery is not inferred.
for row in rows:
    if row["feature"] == "Automatic tracking":
        row["screens"] += ", supporting Repeat nightly settings"
        row["implementation"] += ["RecurringNight.java", "RecurringTracking.java", "RecurringTrackingActivity.java", "RecurringTrackingReceiver.java", "RecurringTrackingBootReceiver.java", "MainActivity.java"]
        row["validation"] += " Recurring0150652 source passed independent ownership/security review and its scoped debug/native APK,9 RecurringNightTest cases and lint passed (0errors/86warnings). Integratedc9582a2; RecurringTrackingTest12 and RecurringTrackingFlowTest2/13 originals are compiled but not yet executed. Main app reopening now reconciles an enabled schedule, future-only. The full CI contract includes these isolated preference/fake-launch tests; actual AlarmManager/FGS delivery remains unverified."
        row["notes"] += " Separate Repeat nightly opt-in saves local calendar time, optional stillness and due-time charging condition; single-use persisted token/revision fences stale/duplicate/ABA starts. Disable affects only the owned run; enabled edits preserve admitted tonight capture. Recurring starts force microphone/clips/light/sleep sounds off, never open an Activity, never backfill missed starts, and retain4h/12h limits. Protected reconciliation and visible reopening schedule future occurrences only. Force-stop/OEM/permission restrictions and background specialUse distribution review remain; see RECURRING-MOVEMENT.md."
    elif row["feature"] in ("Built-in sounds", "Device ringtone"):
        row["validation"] += " FocusedCI40058849301 at e0a78b4: assets1 PASS1.883s, grants1 PASS0.284s; ToneFlow fails because its helper checks a51px label instead of the126px tappable row. Strict review of12 named originals plus diagnostic accepts compact preview geometry narrowly; final4 import/reopen/cancel originals absent, full flow rejected."
    elif row["feature"] == "Gentle volume ramp":
        row["validation"] += " FocusedCI40058849301 WakeSettings still fails: Flashlight node is below viewport but helper waits for existence before scrolling. Its loud-curve original is the wrong Alarm sound page; strict review rejects that named frame. Six later originals absent. Source fixes await a fresh native run; no effects claim."
    elif row["feature"] in ("Post-dismiss wake check", "Wake-check prompt / confirmation deadline / retries"):
        row["validation"] += " FocusedCI40058849301 at e0a78b4 WakeCheckFlowTest2 PASS42.064s. All13 originals individually reviewed: centered timer/glyph, original layered dawn background, clear terminal/error states and no stale Toast; bounded visual gate passes. Supporting failed-save Editor error persists inline. Actual notification/alarm delivery and other stated runtime limitations remain unverified."

# First combined native run keeps newly discovered defects explicit.
for row in rows:
    if row["feature"] == "Correct sound labels / favorites / export":
        row["validation"] += " FullCI40058852109 at e0a78b4: Storage9 has5 failures and UI3 allfail because linked clip ID Double-versus-integer JSON comparison rejects unchanged evidence. A strict null-aware exact numeric comparison and tenth storage regression are added; fresh native proof required. Schema6-to7 migration and analysis-only cases pass narrowly; no general label flow acceptance."
    elif row["feature"] == "Nap timer":
        row["validation"] += " FullCI40058852109 NapFlowTest3 PASS27.884s, but NapIntegrationFlowTest1 fails after4 of8 originals. Strict review of all19 available originals rejects question-mark sound/delete icons, clipped Delete row under error footer and stale card start time after same-occurrence clock refresh. Full visual/integration gate remains failed."

# Retained document workers preserve explicit review/confirmation and never imply provider acceptance.
for row in rows:
    if row["feature"] == "Local data backup":
        row["implementation"] += ["BackupActivity.java", "BackupDocuments.java", "BackupIo.java", "BackupJob.java", "BackupJobs.java", "BackupFingerprint.java", "BackupRuntime.java"]
        row["validation"] += " Backup49ad607 source passed independent review and author debug/native APK,26 focused tests (BackupIoTest11, BackupJobTest12, BackupRuntimeTest3) and lint0errors87warnings. Integratedce19d14 with admission hook after accepted recurring normalization. BackupStorageTest6 and BackupDocumentFlowTest3/19 originals are compiled but not executed. Source does not certify an actual document provider, destructive restore on a phone, or absence of all ANRs."
        row["notes"] += " Private Backup screen uses retained view-free background IO and explicit review/replace confirmation, pinned application-only factory,8MiB strict UTF8/depth bounds, off-main best-effort cancellation, typed full-table fingerprint and within-transaction runtime/data checks. Imported alarms/naps stay off, original/user sound evidence stays portable, no audio/settings. Postcommit cleanup/count/close failure explicitly says data was restored. No WAL change; other main DB users and final-check/commit/system-scheduling intervals remain documented limitations. See BACKUP-DOCUMENT-IO.md."

# Latest focused output supersedes the configuration failure, not missing actual output proof.
for row in rows:
    if row["feature"] == "Gentle volume ramp":
        row["validation"] += " Fresh focusedCI40062368995 at4bd5317 WakeSettings1 PASS43.522s and all10 originals individually reviewed. Exact full-chart loud/reopened100%@2min, normal80%@2min, edited80%@1min and Off80% horizontal states pass; real saved/reopened/cancelled settings preserved via fake scheduling. No speaker/brightness/torch behavior certification."
    elif row["feature"] in ("Built-in sounds", "Device ringtone"):
        row["validation"] += " Fresh focusedCI40062368995 Tone fails initial read-only StaleObjectException before any16 named originals. Later244e4bc gates initial measured waveform readiness and retries only stale selector/geometry reads max4, never caller actions. Fresh native proof still required."
    elif row["feature"] == "Nap timer":
        row["validation"] += " Source92c3f19 integrated6f1db41 fixes aliases, error viewport reveal with full geometry assertions and same-window projected start display, with focused32 domain tests/lint pass. Final observer cleanup and integrated app await combined build/native23 originals; no runtime engine semantics changed."

# Native storage success is distinguished from still-failing UI or actual hardware delivery.
for row in rows:
    if row["feature"] == "Correct sound labels / favorites / export":
        row["validation"] += " Latest244e4bc/fullCI40063157145: SoundLabelStorageTest10 PASS1.106s and SoundLabelFlowTest3 PASS26.038s with all18 originals. Typed clip comparison now proven on real Android SQLite/JSON, including migration/overlay/revision/WAV/backup invariants. Per-file visual review pending; automatic category accuracy and real capture/playback remain separate."
    elif row["feature"] == "Automatic tracking":
        row["validation"] += " Latest244e4bc/fullCI40063157145 RecurringTrackingTest12 PASS0.258s. SettingsFlow2 has1 failure on stale click after cancel/reopen,9 of13 named originals available, so complete UI acceptance remains rejected. Native preferences/fake admission are proven narrowly; actual OS/sensor delivery unverified."
    elif row["feature"] == "Local data backup":
        row["validation"] += " Latest244e4bc/fullCI40063157145 BackupStorageTest6 PASS0.433s. BackupDocumentFlowTest3 has2 failures (posted picker callback not awaited and confirmation capture checks unfocused Activity instead of owned Dialog),6/19 originals. Root adds exact chooser-state and owned-window/current-render gates; native full flow/provider acceptance remains pending."
    elif row["feature"] == "Nap timer":
        row["validation"] += " Latest244e4bc/fullCI40063157145 NapFlowTest3 PASS20.594s; integration reaches6/8 originals then rejects its Remove dialog because the helper checks Activity focus. A read-only owned-dialog capture hook corrects this harness scope; unchanged cancellation/delivery assertions stay required. Fresh complete UI/design proof pending."
    elif row["feature"] in ("Built-in sounds", "Device ringtone"):
        row["validation"] += " Focused40063164364 and full40063157145 at244e4bc reach12 named Tone states then fail an action-only monitor that cannot match audio/*+OPENABLE; DocumentsUI escape contaminates later Editor launches. Correct typed monitor/exact-one callback wait and guaranteed cleanup are added, without changing production picker. Fresh complete flow still required."
    if any(name in row["implementation"] for name in ("ReportScreen.java", "PeriodTimeline.java", "RecordRibbon.java", "TrendsChart.java", "HistoryActivity.java", "RecordEditorActivity.java")):
        row["validation"] += " Report8c9faca source integrates as1df91fe: compact actions/soft affordances, calmer measured ribbons, truthful tracked-only timeline shading with real gaps, Notes/footer geometry. Author debug/native APK plus24 ChartSeriesTest/RecordModelTest cases pass; final native APK/lint0errors87warnings pass39s. New ReportRibbonNativeTest2 and stronger ReportReferenceFlowTest1/eight originals await fresh native/strict review; no visual completion inferred."

# Saved occurrence exceptions have source coverage, not yet a fresh native acceptance result.
for row in rows:
    if row["feature"] == "Skip and restore next occurrence":
        row.update(status="partial", screens="04, root Alarms, supporting Skip confirmation/Undo/retry", implementation=["AlarmSkip.java", "AlarmSkipJson.java", "AlarmSkips.java", "AlarmSkipUi.java", "Alarm.java", "Scheduler.java", "AlarmReceiver.java", "AlarmEditorActivity.java", "MainActivity.java"], validation="Author first build: AlarmSkipTest12 pass, debug/native APKs and lint0errors87warnings. Final guarded Editor/new-ID/atomic replacement and confirmation-clock refinements passed independent source review and integrate asf2cd55f. Exact combined613e44b build passes debug/native/release APK, all500 pure cases/60 suites, and lint0errors87warnings in5m30. Phone install-r returned Success after private backup; app launch was suspended and USB disconnected before postupdate verification. AlarmSkipStorageTest8 and AlarmSkipFlowTest2/15 original captures require fresh native execution; CI contract53classes/112methods/314images includes both classes.", notes="Durable local-calendar skip and visible Undo/retry preserve recurring enable state; one-time skip disables and Undo expires at its original local deadline. Strict metadata/ownership fences queued deliveries, stale editors and concurrent replacement; non-schedule edits preserve current skip. Restore clears skip authority and imports disabled. A saved unconfirmed scheduling marker survives interruption; platform scheduling/delivery remains unverified and the final-check/platform-call race is documented in SKIP-NEXT-ALARM.md. Holiday rules remain separate and missing.")
    elif row["feature"] == "Alarm-only layout / skip occurrence / holiday skipping":
        row["notes"] = row["notes"].replace("Skip/restore occurrence and holiday rules remain missing.", "Skip/restore occurrence is now source-integrated with pending native/visual verification; holiday rules remain missing.")
    elif row["feature"] == "Automatic tracking":
        row["validation"] += " Final recurring UI fix9a3ca04 refreshes permission summaries after failed Save without losing draft/revision/error state, and resolves fresh native actions once; independent source review clear. Same2 methods/13 originals require rerun."
    elif row["feature"] == "Correct sound labels / favorites / export":
        row["validation"] += " Strict reviewer individually inspected all18 latest label originals: bounded selector/provenance/error corrections pass. Broader automatic classification, alternate formats and actual capture/playback remain incomplete."
    elif row["feature"] == "Nap timer":
        row["validation"] += " Strict reviewer individually inspected21 latest Nap originals: icon/error-viewport/clock-projection corrections pass narrowly; removed/invalid integration frames remain absent and are not accepted."

# Exact fresh native results remain separate from later source-only visual corrections.
for row in rows:
    if row["feature"] == "Skip and restore next occurrence":
        row["validation"] += " Fresh d43f480/fullCI40068184362: AlarmSkipFlowTest2 PASS20.891s/all15 originals; Storage8 has2 fixture failures (fixed ID1 assumption for new ID3 and incorrect SQL/platform atomicity assumption). Strict review accepts14 supporting frames but rejects oversized expired status; bca0d81 gives its text explicit wrap width and adds compact/full-action geometry assertions. Source correction and transactional-vs-platform fixture fix require new native execution."
    elif row["feature"] == "Automatic tracking":
        row["validation"] += " Fresh d43f480/fullCI40068184362: backend12 PASS0.272s; first settings flow now passes, second fails because real error is below viewport. All8 named originals/diagnostic reviewed;3fda351 moves actual error into pinned footer above Save/Cancel. Same2methods/13states require rerun."
    elif row["feature"] == "Local data backup":
        row["validation"] += " Fresh d43f480/fullCI40068184362: BackupStorageTest6 PASS0.522s and BackupDocumentFlowTest3 PASS28.984s/all19 original states. Strict individually reviews all19: supporting operations/confirmation/current-error contents pass, stale-state white navigation handle rejected. bca0d81 corrects actual owned-dialog/render bar appearance and checks current window bits; fresh capture required. Actual external provider/destructive phone restore remain unverified."
    elif row["feature"] == "Nap timer":
        row["validation"] += " Fresh d43f480/fullCI40068184362 NapIntegrationFlowTest1 PASS17.369s/all8 original states individually reviewed and bounded visual pass. Main NapFlowTest3 has2 failures on immediate post-click new-row/retry reads;3fda351 waits actual private persisted state without retrying actions. Complete source/clock/quiet fake behavior does not prove actual delivery."
    elif row["feature"] in ("Built-in sounds", "Device ringtone", "Gentle volume ramp", "Post-dismiss wake check", "Wake-check prompt / confirmation deadline / retries"):
        row["validation"] += " Fresh d43f480/focusedCI40068188579 PASS5classes/6methods/all39 required originals, individually reviewed. Assets1.497s,Grant0.611s,ToneFlow40.346s,WakeSettings44.902s,WakeCheck2/39.15s. Native configuration/private persistence/fake preview pass narrowly; full run's WakeSettings reopen fails prior-editor lifecycle timing, addressed3fda351. Filename-on-reopen and loading chevron polish remain, and actual outputs/provider grants/OS delivery are not certified."
    if any(name in row["implementation"] for name in ("ReportScreen.java", "PeriodTimeline.java", "RecordRibbon.java", "TrendsChart.java", "HistoryActivity.java", "RecordEditorActivity.java")):
        row["validation"] += " Fresh d43f480/focusedCI40068186520 PASS4classes/5methods/13required originals: ReportRibbonNativeTest2 PASS1.212s,ReportReferenceFlowTest1 PASS33.744s. All13 originals individually inspected: report hierarchy/editor geometry pass, hard color-bin stripes and touching chips rejected. b4deeb3 blends only visual colors within contiguous measured spans (unchanged metrics/exact gaps), adds missing-signal Canvas assertions and4dp chip gutters; fresh raster/visual run required. Missing stage estimator remains explicit."

# Latest verified milestones supersede earlier pending/rejected snapshots, which remain historical.
for row in rows:
    if row["feature"] == "Skip and restore next occurrence":
        row["validation"] += " Fresh dc30736/fullCI40072555253: Storage8 PASS0.571s,UI2 PASS18.45s/all15 frames; strict expired compact-card/action gate now clears. Actual platform delivery remains unverified."
    elif row["feature"] == "Automatic tracking":
        row["validation"] += " Fresh dc30736/fullCI40072555253 runtime12 PASS0.312s; UI2 has1 Got it acknowledgment failure, diagnostic proves dismissal. Actual pinned error now visible; taller-state clock glyph still clipped (P2). b7e5233 injects once at owned dialog bounds then awaits exact detach/focus/error/preferences, fresh proof pending."
    elif row["feature"] == "Local data backup":
        row["validation"] += " Fresh dc30736/fullCI40072555253 Storage6 PASS0.412s,Document3 PASS26.612s/all19; strict stale-state cream/dark system-bar mismatch clears. External provider/physical restore remains unverified."
    elif row["feature"] == "Nap timer":
        row["validation"] += " Fresh dc30736/fullCI40072555253 NapFlow3 PASS20.604s +Integration1 PASS16.83s/all23 originals. Actual alarm delivery remains untested."
    elif row["feature"] in ("Built-in sounds", "Device ringtone"):
        row["validation"] += " Fresh dc30736/fullCI40072555253 Tone fails error-message synchronization after3 originals; diagnostic already shows full error/footer.29fdf58 awaits published native text, keeps exact error geometry and full JSON cancel invariance, and caches bounded imported filename for fresh reopen; native rerun pending."
    if any(name in row["implementation"] for name in ("ReportScreen.java", "PeriodTimeline.java", "RecordRibbon.java", "TrendsChart.java", "HistoryActivity.java", "RecordEditorActivity.java")):
        row["validation"] += " Fresh dc30736/focusedCI40072560126 PASS4classes/5methods/all13 required originals, individually reviewed against boards01/08. Ribbon color blending/exact gaps, filter gutters, seven-row history, Week/Month/Year and complete editor Notes/Save/Delete pass this bounded correction. Stage estimator/full app parity remain incomplete."
    if row["feature"] in ("Contactless sonar", "Sonar calibration / signal quality / fallback"):
        row.update(status="partial",implementation=["ContactlessSetupActivity.java","SonarRuntime.java","SonarAudio.java","SonarPump.java","SonarDsp.java","SonarAlignment.java","SonarSettings.java","SonarSignalChart.java"],validation="Original source24f815a integrated asaba7d5a. Author20 pure cases pass; debug/native APK/lint0errors87warnings pass. Strict source-clear;4 native methods/15 required originals execution-pending. Combined native contract now57classes/126methods/345required originals. No physical effects tested.",notes="Explicit foreground Test emits original18.5–20kHz FMCW and measures real echo change with dechirp/FFT/quality, route/timing/charging/focus checks and bounded cleanup. Save stores profile without audio output. Missing/invalid evidence stays absent. Overnight persistence/report integration is in progress; initial setup is not a completed night flow. Real hardware support, actual echo accuracy and power budget unverified; no breathing/stage/medical claim. See CONTACTLESS-SONAR-IMPLEMENTATION.md.")
    elif row["feature"] in ("Jet-lag light intervention", "Dream and travel light cues"):
        row.update(status="partial",implementation=["TravelActivity.java","TravelCueReviewActivity.java","TravelCuePlan.java","TravelCueSession.java","TravelCueRuntime.java","TravelCueStore.java","TravelCues.java","PhoneTorchCoordinator.java","PhoneTorches.java","TrackingService.java"],validation="Sourcecd35c21 integrated as5d11405.47 focused pure tests pass; debug/native APKs and lint0errors87warnings pass; strict source-clear. TravelCueStorageTest8 and TravelCueFlowTest2/16 originals execution-pending. No hardware output or actual clinical effect verified.",notes="Saved zones/date/daylight reminders plus separate default-off experimental phone-light windows. Explicit Arm only during current manual active tracking; separate Stop/journal/error/interruption, requested2ms/15s finite session, no automatic/recurring/boot/replay arming. Serial app torch ownership gives real alarms priority; timing/dose not measured. No evidence-based phase scheduling, jet-lag prevention, dream/REM or smart-light claim. See TRAVEL-PHONE-LIGHT-CUES.md.")

# Schema8 and the exact latest native gates: configuration is distinct from actual hardware.
for row in rows:
    if row["feature"] in ("Contactless sonar", "Sonar calibration / signal quality / fallback"):
        row["implementation"] += ["SensorProvenance.java","SensorBackup.java","Store.java","Records.java"]
        row["validation"] += " Main05cd205 local573pure/69suites0fail +debug/nativeAPKs/lint0errors89warnings PASS7m29. FocusedCI40079038257 all26methods/31originals PASS: SonarProvenanceStorageTest12 PASS1.861s,SonarSettingsNativeTest1/.333s,ContactlessSetupFlowTest3/52.64s. All15setup originals individually reviewed: initialplum/lightbars,largeractualchart,scene,slider,error/gain/save/reopen pass bounded supporting-state gate. Reference ready/status rows and recorded-night integration remain open."
        row["notes"] += " Additive schema8 and portableformat5 preserve sensor/config,nullable5squality,rawepochs/edits and legacy unknown provenance. Real native migration/roundtrip/rollback/recovery tests pass. Shared microphone noise analysis is not connected to sonar; overnight source is in progress."
    elif row["feature"] in ("Jet-lag light intervention", "Dream and travel light cues"):
        row["validation"] += " Main05cd205/focusedCI40079038257 TravelCueStorageTest8 PASS.158s andTravelCueFlowTest2 PASS30.764s/all16originals. All16 individually inspected: disabledArm visiblymuted,windowCancel/recreate/save/reopen,active/interrupted/error gates pass bounded config/fake-light scope. Wholeboard09stillpartial: lower controls/continuation and supporting prose need polishing. Hardwaredose/OSdelivery/clinicalprevention are unproven."
    elif row["feature"] == "Local data backup":
        row["validation"] += " Main05cd205/fullCI40079043221 BackupStorage6/Document3 eachhas1CSVfailure: streamedbytes omit new sensor/config whileRecords.csv includes them. da3b11a fixes actual16-column header+closed/openrows,retains8MiB/cancellation,and adds exactstreamedbyte equality/config regression. Native rerun pending; no green claim."
        row["notes"] += " Latestformat5 adds strict original sensor/config/quality evidence;formats1–4stayreadable. Newschema8metadata participates in existing fulltyped stale-restore fingerprint."
    elif row["feature"] == "Correct sound labels / favorites / export":
        row["validation"] += " FullCI40079043221 onelegacyv6fixture failsduplicate sensorcolumn because it copiedv8schema then claimedv6. da3b11a constructs truepre-label/pre-sensor SQL+columns witholdrowassertionsretained;productionmigration unchanged. Actualv7→8preservation already passesnew12-case suite."
    if any(name in row["implementation"] for name in ("ReportScreen.java", "PeriodTimeline.java", "RecordRibbon.java", "TrendsChart.java", "HistoryActivity.java", "RecordEditorActivity.java")):
        row["validation"] += " ba1ae61/0dcbb44 use exactpersistedSensorProvenance.SONAR for contactless graph/accessibility labels, preserve actualsamplegaps, and retain sensor/config throughgoalzonecopies. Newprivate saved-sonar navigation originals remain pending."

# Current bounded evidence supersedes the earlier source-only and rejected captures.
for row in rows:
    if row["feature"] in ("Contactless sonar", "Sonar calibration / signal quality / fallback"):
        row["implementation"] += ["SonarEntry.java", "SonarNight.java", "SonarEpochs.java", "SonarNightStore.java", "TrackingService.java", "TrackingActivity.java", "RecordSignalsActivity.java"]
        row["implementation"] += ["SonarDownsampler.java", "SonarSoundPipeline.java", "SonarSoundWorker.java", "SoundAnalysisStatus.java", "SoundAnalysisProvenance.java"]
        row["validation"] = "At2477cbe/CI40091982679, SonarNightStorageTest2, SonarNightFlowTest2 and SonarReportFlowTest1 pass all5methods/14originals. Independent visual review passes only these supporting-flow states: measured marks, exact gaps/zero, retained errors, pause/recreate/resume/stop and saved report reopening. Prior rejected and diagnostic runs remain archived. Actual production DSP/coordinator/private SQLite run with generated PCM and paired capture time, not hardware. Local01406b4 debug/native APKs,593pure/73suites and lint pass. Schema9/format6 shared-analysis foundation is committed; SharedSoundProvenanceStorageTest12 and expanded six-frame Report contract await fresh native execution. No phone output or overnight certification."
        row["notes"] = "Explicit setup and manual Start/Pause/Resume/Stop measure relative echo change through original18.5–20kHz FMCW. Bounded ordered persistence retains sensor/config, nullable5squality, validzero and real gaps. Saved report shows relative echo change, quality duration and unrecorded time without fabricating stages. Schema9/format6 separately stores immutable analysis consent/outcome; legacy unknown stays unknown. Shared microphone analysis runtime is being integrated, not yet verified. No automatic sonar, raw sonar audio, breathing or medical claim. Full board06 composition, real hardware accuracy, foreground-service delivery and overnight battery remain open. See sonar-night-runtime.md and SONAR-REPORT-FLOW.md."
    elif row["feature"] == "Automatic sound labels":
        row["implementation"] += ["SoundBrowseFilter.java", "SoundBrowseState.java", "SoundAnalysisStatus.java", "SoundAnalysisProvenance.java"]
        row["validation"] += " Frozen laughter0.30 candidate rejected:0/3 new independent positives, production unchanged;9 host evidence-integrity tests pass. CI40093856492 still fails solo-laughter positive. See SOUND-CLASSIFIER-HOLDOUT-RESULT.md. Saved-category browsing CI40090631946 passes13methods/68originals, but strict appearance review rejects mixed system glyphs. First correction CI40093853208 fails; declared-window-policy and owned-input fixes await fresh native proof."
        row["notes"] = "Offline snore/speech estimates exist. Cough, baby-cry and laughter recognition remain unaccepted candidates; reliable bedroom recognition and per-class calibration are incomplete. All six saved effective categories can be browsed independently, with user corrections distinguished from original estimates. Category browsing does not establish classifier accuracy. Same-capture contactless/noise runtime remains integration-pending."
    elif row["feature"] == "Correct sound labels / favorites / export":
        row["implementation"] += ["SoundBrowseFilter.java", "SoundBrowseState.java"]
        row["validation"] += " Saved-category browsing CI40090631946 passes8classes/13methods/all68originals; new14 content states pass, strict system-bar appearance rejects. CI40093856492 reveals old SoundLabelStorageTest catch-all noise expectation conflicts with new exact Other category; test now explicitly asserts cough1/noise0 and both row matches while retaining report aggregate/reset/WAV invariants, native rerun pending."
        row["notes"] += " All six effective categories now have exact independent filters and optional persisted browsing state; Other excludes named cough/cry/laughter. This is saved metadata navigation, not new recognition accuracy."
    elif row["feature"] == "Local data backup":
        row["implementation"] += ["SoundAnalysisProvenance.java"]
        row["validation"] += " Later04bdb4a full native backup/migration regressions pass. Current schema9/format6 adds strict immutable shared-analysis status to portable JSON and both17-column CSV implementations. Twelve new private storage cases are authored/compiled; actual schema9 migration/rollback/recovery execution remains pending."
        row["notes"] += " Format6 preserves explicit analysis outcomes; formats1–5 keep unknown legacy status rather than inventing off. New format6 native acceptance remains open."

# Feature-first sprint: current bounded results supersede pending descriptions above.
for row in rows:
    title = row["feature"]
    if title == "Reusable alarm defaults":
        row.update(status="partial", implementation=["AlarmDefaults.java", "AlarmDefaultsJson.java", "AlarmDefaultsSettings.java", "AlarmDefaultsSave.java", "AlarmDefaultsActivity.java", "AlarmEditorActivity.java", "MainActivity.java"], validation="311d170: AlarmDefaultsTest4 pure PASS; debug/native APKs and lint PASS. AlarmDefaultsStorageTest3 and AlarmDefaultsFlowTest2/nine frames compiled; native execution pending CI40133514540.", notes="Saved reusable behavior templates apply only to future new alarms; schedule/time/enabled/skip/nap authority is excluded. Existing alarms remain independent. Native save/reopen and output verification remain open. See alarm-defaults.md.")
    elif title == "Per-class sound sensitivity":
        row.update(status="implemented", implementation=["SoundSensitivity.java", "SoundSensitivitySettings.java", "SoundSensitivityActivity.java", "SoundClassifier.java", "ClipBudget.java", "SonarSoundWorker.java", "NoiseRecorder.java"], validation="496a0c2/3463834 CI40124391172 PASS5classes/10methods/all14required originals: SoundSensitivityStorageTest3, SoundSensitivityFlowTest2, SoundAnalysisNativeTest3, SoundClassifierFixtureEvaluationTest1 and SoundEventsUiTest1. Private save/recreate/reopen/error and actual shared analysis policy tested; no acoustic accuracy claim.", notes="Explicit per-class minimum accepted duration (one/two/three inference windows) is frozen per admitted night for motion and shared sonar capture; default two windows retains previous policy. This controls event acceptance, not model score calibration or reliable cough/cry/laughter recognition.")
    elif title in ("Tasker / Automate / Intent API", "Tasker events / webhook retries / MQTT transport"):
        row.update(status="partial", implementation=["AutomationActivity.java", "AutomationRule.java", "AutomationEvent.java", "AutomationStore.java", "AutomationSecrets.java", "Automations.java", "AutomationDispatch.java", "AutomationDelivery.java", "AutomationTransports.java", "AutomationTracking.java", "AutomationJobService.java", "WebhookPost.java", "MqttPublish.java"], validation="985f4ef: AutomationPolicyTest6, MqttPublishTest6 and WebhookPostTest4 pure PASS; debug/native APKs and lint PASS. AutomationStorageTest10 private fake-transport methods compiled; native execution pending CI40133514540. No real external request.", notes="Default-off rules emit real tracking-start/end and successfully completed positive-ID alarm-dismiss events. Package-targeted Tasker broadcast, HTTPS POST and TLS MQTT QoS1 have encrypted private credentials, durable bounded queue/revisions and six bounded retries within24h. Actual recipient/broker/account configuration and exchange remain unverified; no inbound generic Intent/data-provider API. See AUTOMATIONS.md.")
    elif title == "Calendar holiday skip / meeting shift / record write":
        row.update(status="partial", implementation=["CalendarPolicy.java", "CalendarSettings.java", "CalendarAccess.java", "CalendarAlarms.java", "CalendarSync.java", "CalendarSyncJob.java", "CalendarRulesActivity.java", "Scheduler.java", "AlarmSkips.java", "Naps.java"], validation="0b7f46d: CalendarPolicyTest12 pure PASS. CalendarRulesStorageTest4 and CalendarRulesFlowTest1/four frames authored; combined native build/execution pending CI40133514540. No actual phone calendar or alarm access.", notes="Read-only selected-calendar all-day skip and bounded earlier-meeting rules apply to repeating alarms, with ordinary-time fallback, worker provider reads, background refresh, occurrence receipt, actual next-alarm preview and shifted Skip/Undo expiry. Default off; phone calendar provider/READ_CALENDAR and alarm readiness required. Calendar record writing remains absent. See CALENDAR-RULES.md.")
    elif title in ("Contactless sonar", "Sonar calibration / signal quality / fallback"):
        row["validation"] = "f9f7acb CI40120286873 PASS6classes/20methods/all25required originals: SharedSoundProvenanceStorageTest12, SonarNightStorageTest2, SonarNightFlowTest2, SonarReportFlowTest1, SonarSharedSoundFlowTest2 and SonarSharedClassifierStorageTest1. Actual FIR/model/private SQLite replay covers three pinned PCM fixtures and correct distinct snore/speech events. No hardware/overnight evidence."
        row["notes"] = "Manual contactless setup/start/pause/resume/stop/report now optionally shares one48k capture with ordered sound analysis and durable outcome/coverage. Legacy unknown remains unknown, exact gaps/zero preserved. Shared sonar sound analysis excludes saved clips/cues and automatic starts. Real hardware calibration, bedroom accuracy, battery and background delivery remain unverified; no breathing/stage/medical claim."
    elif title == "Correct sound labels / favorites / export":
        row["validation"] += " f9f7acb CI40120293302 PASS8classes/13methods/all68required originals for saved category navigation, favorites, labels and export. Full3463834 legacy SoundLabelStorageTest now passes; broader visual review deferred by user."
    elif title in ("Goal bank / wagers", "Dated goal bank"):
        row["validation"] += " 6808373 adds dated historical targets and worker ledger.3463834 fullCI40124416810: GoalStorageTest10, GoalFlowTest1 pass; DatedGoalBankFlowTest second historical-link method passes, first target/date method fails readiness. No complete new-flow pass claimed."

# Integrated da0f571 native77-method gate; this is functional evidence, not design review.
sprint_titles = {
    "Health Connect": ("partial", ["HealthConnectActivity.java", "HealthSleepAndroid.java", "HealthSleepSync.java", "HealthSleepStore.java", "HealthSleepSourceActivity.java"], "HealthSleepStorageTest12 and HealthConnectFlowTest2 pass", "Manual sleep-only permissions/import/export, stable IDs/versions, provider changes/deletions, conflict review and historical provenance are real. Schema10/format7 retains source stages and clears live ownership on restore. Actual Health Connect provider exchange remains unverified; no hardware vitals. See HEALTH-CONNECT-SLEEP.md."),
    "Health Connect sleep provenance / dedup / deletion": ("partial", ["HealthSleep.java", "HealthSleepCodec.java", "HealthSleepTables.java", "HealthSleepStore.java", "HealthSleepSync.java"], "HealthSleepStorageTest12 and HealthConnectFlowTest2 pass", "Native private-provider import/dedup/update/delete/conflict/export/restore cases pass. Imported stages remain source-reported, not a SleepWell stage estimate. Real provider availability/consent/exchange required. See HEALTH-CONNECT-SLEEP.md."),
    "Tag impact analysis": ("implemented", ["TagImpact.java", "TagRecords.java", "TagManagementActivity.java"], "TagStorageTest8 and TagManagementFlowTest1 pass", "Descriptive tagged/untagged recorded-duration averages and per-unit quantity correlation show actual sample and excluded/unknown coverage counts. No causal, diagnostic or personalized-treatment claim. See TAG-MANAGEMENT.md."),
    "Tag quantities / rename / delete / impact": ("implemented", ["TagQuantity.java", "TagCodec.java", "TagQuantityDialog.java", "TagRecords.java", "TagJobs.java", "TagManagementActivity.java", "RecordDraft.java"], "TagStorageTest8 and TagManagementFlowTest1 pass", "Quantities persist with legacy tags, JSON/CSV and HC provenance. Global rename/delete is transactional and stale-fenced with collision rejection; native draft/recreation/save/rename/delete/comparison passes. Main schema10/portableformat7 unchanged. See TAG-MANAGEMENT.md."),
    "Hue / IKEA smart lights": ("partial", ["SmartLightActivity.java", "SmartLightSettings.java", "SmartLightRun.java", "SmartLightAlarm.java", "HomeAssistantLight.java", "HomeAssistantHttp.java"], "SmartLightStorageTest6 and HomeAssistantProtocolTest4 pass", "Optional Home Assistant HTTPS dimmable-light entity ramps from ringing, with bounded network work/manual override and conditional restore. User-owned server, trusted TLS, token/entity and compatible lamp required. No direct vendor bridge, pre-alarm lamp claim or physical light test. See SMART-BEDROOM-LIGHT.md."),
    "Cloud backup / SleepCloud": ("partial", ["CloudBackupActivity.java", "CloudBackupProvider.java", "CloudBackupCopy.java", "CloudBackupWork.java", "CloudBackupSettings.java"], "CloudBackupStorageTest5 and CloudBackupFlowTest1 pass", "Explicit remembered SAF cloud/local folder supports unique JSON versions, readback SHA256, bounded local receipts and immutable reviewed restore. Provider/account prerequisite; no proprietary SleepCloud service, automatic scheduling or audio inclusion. See CLOUD-DOCUMENT-BACKUP.md."),
    "User-controlled cloud / conflict / audio backup": ("partial", ["CloudBackupActivity.java", "CloudBackupCopy.java", "CloudBackupJob.java", "CloudBackupJobs.java", "CloudBackupSnapshot.java", "BackupJob.java", "BackupActivity.java"], "CloudBackupStorageTest5 and CloudBackupFlowTest1 pass", "Manual create-only verified versions and recorded-hash restore review preserve existing admission/fingerprint/confirmation. Fake provider/private storage/native reopen passes. No real upload, scheduled backup or cloud audio bundle. See CLOUD-DOCUMENT-BACKUP.md."),
    "Calendar": ("partial", ["CalendarRulesActivity.java", "CalendarAccess.java", "CalendarAlarms.java", "CalendarSync.java"], "CalendarRulesStorageTest4 and CalendarRulesFlowTest1 pass", "Read-only opt-in all-day skips and bounded earlier-meeting rules for repeating alarms; original occurrence consumption and actual preview/Skip/Undo persisted. Calendar provider/read permission/job scheduling required; no calendar event writes or actual provider/OS-delivery test. See CALENDAR-RULES.md."),
    "Flip snooze / hardware-key controls": ("partial", ["AlarmInputs.java", "AlarmInputsJson.java", "AlarmFlipSensor.java", "FlipSnooze.java", "AlarmActivity.java"], "AlarmInputsStorageTest3 and AlarmInputsFlowTest1 pass", "Per-alarm flip and volume-key Phone volume/Snooze/No action choices, fresh capture/occurrence/count fences and independent step/shake listener ownership. Native configuration/persistence and pure policy pass; real sensor/key/AlarmActivity output remains unverified. See alarm-input-actions.md."),
    "Date / sunrise / goal-derived alarm": ("partial", ["AlarmDate.java", "Alarm.java", "AlarmEditorActivity.java", "AlarmSkips.java", "Scheduler.java"], "AlarmDateStorageTest3 and AlarmDateFlowTest1 pass", "Explicit local one-time dates persist/recreate/reopen and expire without rolling into tomorrow; date/repeat changes are explicit. Native private scheduling/portable cases pass. Location-derived sunrise and goal-derived alarm time remain absent. See dated-alarms.md."),
    "Dynamic snooze choices / until deadline": ("partial", ["SnoozeOffer.java", "SnoozePolicy.java", "AlarmInputs.java", "AlarmActivity.java", "AlarmEditorActivity.java"], "AdvancedSnoozeStorageTest2 and AdvancedSnoozeFlowTest1 pass", "Optional per-press bounded chooser and configured1–60min durations retain defaults, shorten/count rules and occurrence ownership. Cancel/pause/stale offers do not snooze. Native configuration passes; live scheduling/output and arbitrary until-deadline snooze remain unverified/absent. See advanced-snooze.md."),
    "Sleeping-target visual search": ("partial", ["MoonSearch.java", "MoonSearchState.java", "MoonSearchView.java", "MoonSearchPreviewActivity.java", "AlarmActivity.java", "ChallengeRoutine.java"], "MoonSearchStorageTest3 and MoonSearchFlowTest1 pass", "Original3×3/4×4/5×5 sleeping-moon search persists occurrence/unit board and explicit Math fallback. Wrong choices never advance; silent shared preview and native routine/default roundtrip pass. Live alarm dispatch is untested. See VISUAL-SEARCH-CHALLENGE.md."),
}
for row in rows:
    if row["feature"] in sprint_titles:
        state, code, checks, remaining = sprint_titles[row["feature"]]
        row.update(status=state, implementation=code, validation="da0f571 / CI40141612460: " + checks + "; focused23classes/77methods/all68originals PASS. Combined778pure/100suites, debug/nativeAPKs and lint0errors98warnings PASS7m03. No real external or phone effects.", notes=remaining)
    elif row["feature"] == "Reusable alarm defaults":
        row["status"] = "implemented"
        row["validation"] = "da0f571 CI40141612460 AlarmDefaultsStorageTest3 and AlarmDefaultsFlowTest2/allnine originals PASS; combined778pure/debug/native/lint PASS. Native save/recreate/reopen/reset/error tested with private preferences, without changing existing alarm schedules."
        row["notes"] = "Saved behavior templates apply only to future new alarms. Identity/time/date/repeat/enabled/skip/nap authority is excluded; existing alarms stay independent. Imported-tone grant references retained. Physical downstream alarm output remains separately unverified. See alarm-defaults.md."
    elif row["feature"] in ("Tasker / Automate / Intent API", "Tasker events / webhook retries / MQTT transport", "Custom webhooks", "MQTT", "IFTTT"):
        row["status"] = "partial"
        row["implementation"] = ["AutomationActivity.java", "Automations.java", "AutomationStore.java", "AutomationDispatch.java", "AutomationTransports.java", "WebhookPost.java", "MqttPublish.java"]
        row["validation"] = "da0f571 CI40141612460 AutomationStorageTest10 PASS.16 pure transport/policy cases and combined778pure/debug/native/lint PASS. No real recipient/broker/IFTTT request."
        row["notes"] = "Opted-in real tracking/alarm-dismiss events use targeted Tasker broadcasts, HTTPS POST and TLS MQTT QoS1, encrypted private credentials and bounded durable retries. HTTPS can target a user-configured webhook service; actual account/endpoint/broker exchange and inbound Intent/data-provider contracts remain unverified/absent. See AUTOMATIONS.md."
    elif row["feature"] in ("Calendar holiday skip / meeting shift / record write", "Goal bank / wagers", "Dated goal bank"):
        row["validation"] += " Superseding da0f571 CI40141612460 CalendarRulesStorageTest4/FlowTest1 and DatedGoalBankFlowTest2 allPASS, with complete originals/save/reopen. Earlier UUID/owned-dialog failures fixed; no real provider/hardware claim."

expanded = {
    "Global search": (["SearchActivity.java", "SearchData.java", "SearchIndex.java"], "SearchStorageTest1 and SearchFlowTest1/all4 originals", "Actual saved-record/settings search opens the selected destination and retains query across recreation; no network or invented results."),
    "Home widgets / shortcuts": (["HomeWidgetProvider.java", "HomeWidgetData.java", "HomeLaunch.java", "HomeLaunchActivity.java", "HomeWidgetSettingsActivity.java"], "HomeWidgetNativeTest7", "Next saved alarm/latest real record RemoteViews and add-alarm/tracking/history shortcuts use reconstructed screen-only routes. Fake host and private records pass; actual host pinning/sizing and snooze deadline projection remain unverified/absent."),
    "Dream diary / mirror text task": (["TypedWakeTasks.java", "DiaryStore.java", "DiaryActivity.java", "MirrorText.java", "JournalRule.java", "AlarmActivity.java"], "DiaryStorageTest10 and TypedWakeFlowTest2/all11 originals", "Stable mirrored phrases and explicit bounded journal engagement rules use shared native challenge controls, with private idempotent diary persistence and create/edit/delete/export. Main portable backup excludes separate diary data. No physical AlarmActivity delivery or semantic dream validation claim."),
    "Backup duration / repeat / timeout": (["AlarmTiming.java", "AlarmLifetime.java", "AlarmService.java", "AlarmAudioSink.java", "AlarmEditorActivity.java"], "AlarmTimingStorageTest3 and AlarmTimingFlowTest1/all7 originals", "Optional bounded backup-sound bursts/repeats/gaps and occurrence-owned auto-silence preserve continuous/Never defaults. Primary sound continues between backup bursts; timeout is not successful challenge completion. Configuration and pure ownership policy pass; real scheduling/output remains unverified."),
}
for row in rows:
    if row["feature"] in expanded:
        code, checks, notes = expanded[row["feature"]]
        row.update(status="partial", implementation=code, validation="4b7d017 / CI40147599727: " + checks + " PASS within30classes/102methods/90originals. Private fixtures/fake providers only; no new combined local count or physical output inferred.", notes=notes)
    elif row["feature"] == "Tilt maze / jumping / flags / counting tasks":
        row.update(status="partial", implementation=["CountingPuzzle.java", "FlagPuzzle.java", "TiltMaze.java", "TiltMotion.java", "TiltMazeTask.java", "ChallengePackPreviewActivity.java", "AlarmActivity.java"], validation="Author a0d2bed: ChallengeCountingFlagsTest, TiltMazeTest, TiltMotionTest and existing ChallengeRoutineTest:24pure cases PASS, debug/native APKs and lint PASS. f5e80ab CI40150902142 ChallengePackNativeTest8 PASS; whole focused run remains FAIL on DND permission callback and goal dialog recreation (targeted fixes pending rerun).", notes="Original Counting and eight-country Flags plus calibrated fresh-sensor collision maze are wired into saved routines and shared silent previews. Persisted Math fallback and lifecycle/run fences included. Actual sensor/alarm behavior remains unverified; jumping missing. See COUNTING-TILT-FLAGS.md.")
    elif row["feature"] in ("Assistant voice actions", "Own Intent and data-provider contracts"):
        row.update(status="partial", implementation=["AlarmRequest.java", "AlarmRequestInput.java", "AlarmRequestActivity.java", "HomeLaunch.java"], validation="f5e80ab CI40150902142 AlarmRequestStorageTest3 and AlarmRequestFlowTest1/all3 originals PASS. AlarmRequestTest4 authored; combined local run pending. Whole focused run FAIL on separate DND/goal synchronization assertions. No assistant or phone action.", notes="Permission-protected standard SET_ALARM/SHOW_ALARMS entry reconstructs bounded scalar fields into a fresh ordinary editor draft. Explicit Save required even when skip-UI requested. No incoming identity/nested Intent authority. Custom/silent ringtone requests explicitly unsupported; actual assistant routing, dismiss/snooze/timer and inbound data-provider contracts absent/unverified. See STANDARD-ALARM-REQUESTS.md.")

for row in rows:
    if row["feature"] == "Playlists / shuffle / silent alarm":
        row.update(status="partial", implementation=["AlarmSoundMode.java", "AlarmSoundModeJson.java", "AlarmSoundSequence.java", "AlarmSoundModesUi.java", "AlarmPlayback.java", "AlarmAudioSink.java", "AlarmPlaylistJournal.java", "AlarmService.java"], validation="Author e95a4c0: AlarmSoundModeTest plus existing AlarmPlaybackTest/AlarmLifetimeTest/AlarmVolumeLeaseTest/AlarmDefaultsTest:57focused pure/debug/native APKs/lint PASS before final bounded failure-circuit delta; final32direct host cases PASS. Superseding d346441 CI40154059415 AlarmSoundModeStorageTest4/AlarmSoundModeFlowTest1/all7 originals PASS with fake output/grant ports.", notes="Default Single preserves legacy. Silent suppresses audible playback/fallback/backup and silences the alarm notification while configured vibration/light/tasks continue. Ordered/shuffled2–8-track playlists advance on real decoder completion, checkpoint per-run position before start, retain imported names/grants and bound source failures. Physical output, real providers and restart delivery remain unverified. See alarm-sound-modes.md.")
    elif row["feature"] == "DND / airplane / screen dimming":
        row.update(status="partial", implementation=["TrackingDndOwner.java", "TrackingDndRuntime.java", "TrackingDndAndroid.java", "TrackingDndSettings.java", "TrackingDndActivity.java", "TrackingService.java"], validation="Author cf32b1e: TrackingDndOwnerTest21 and debug/native APKs/lint PASS. f5e80ab CI40150902142 TrackingDndStorageTest4 + cleanup UI case PASS; first permission UI callback wait failed.95e23e1 fixes exact fake callback/checked-state synchronization. Superseding d346441 CI40154059415 Storage4/Flow2/all7 originals PASS, no actual DND changes.", notes="Default-off ACTIVE-tracking app-owned priority rule with own nonce/deadline,15-minute expiry/renewal, pause/end/boot/process cleanup and explicit prerequisite/error states. This rule allows calls/alarms; other OS policies still apply. Real DND and forced-stop/revoked-access cleanup remain unverified. Airplane control absent; app brightness dimming is a separate existing behavior. See TRACKING-DND.md.")
    elif row["feature"] in ("Statistics", "Duration/deficit/regularity charts", "Personalized advice"):
        row.update(status="partial", implementation=["RecordInsights.java", "RecordInsightsActivity.java", "RecordInsightsChart.java", "RecordInsightSettings.java", "RecordTrends.java", "RhythmStats.java", "TagImpact.java"], validation="Author e51f43f: RecordInsightsTest12 + RhythmStatsTest15 + TagImpactTest14 =41pure PASS; debug/native APKs/lint PASS. Superseding d346441 CI40154059415 RecordInsightsNativeTest3 and RecordInsightsFlowTest1/all4 originals PASS.", notes="Saved7/30/90-day views use actual merged coverage, observed/missing dates, duration distribution, circular recorded main-period start range, original dated-goal targets and sufficiently sampled tag associations. Missing days stay unknown. Descriptive comparisons support reflection; no personalized clinical score, predictive advice, measured sleep onset/stages or chronotype claim. See RECORDED-INSIGHTS.md.")
    elif row["feature"] == "Flip pause / delayed tracking start":
        row.update(status="partial", implementation=["TrackingDelay.java", "TrackingDelayEntry.java", "TrackingDelayRuntime.java", "TrackingDelaySettings.java", "TrackingDelayActivity.java", "TrackingService.java"], validation="Author6cd72db: TrackingDelayTest16 and TrackingDelayEntryTest6, plus8AutomaticCaptureTest and21TrackingDndOwnerTest=51pure/debug/native APKs/lint PASS. TrackingDelayStorageTest4 + TrackingDelayFlowTest2/nine originals compiled, native execution pending.", notes="DefaultOff optional1/5/10/15/30-minute manual delay holds original visible admission, frozen audio/light intent, monotonic deadline and nonce-fenced Cancel/Start now. No sensor/mic/sonar/DND/session row during waiting. Durable claim precedes actual capture; lost process/boot never auto-restarts. Real OEM behavior unverified; flip pause remains assigned. See MANUAL-TRACKING-DELAY.md.")
    elif row["feature"] == "Calendar holiday skip / meeting shift / record write":
        row["implementation"] += ["CalendarEventDraft.java", "CalendarEventExport.java", "CalendarEventExportActivity.java"]
        row["validation"] += " CalendarEventDraftTest3 PASS in combined900-test1275be1 build. d346441 CI40154059415 CalendarEventExportTest1 PASS; FlowTest1 fails before note toggle because its helper selected a label instead of Switch.2732a6f fixes exact native control lookup; rerun pending."
        row["notes"] += " Explicit saved-period event draft handoff opens the calendar app only after review, with notes excluded by default; user saves externally. No WRITE_CALENDAR, automatic write/update/delete sync or save acknowledgment. See CALENDAR-EVENT-EXPORT.md."

# Later evidence is intentionally narrow: the combined native run below failed.
for row in rows:
    title = row["feature"]
    if title == "Date / sunrise / goal-derived alarm":
        row["implementation"] += ["SunriseMath.java", "SunriseSchedule.java", "SunriseScheduleJson.java", "SunriseTimeActivity.java"]
        row["validation"] += " Solar author e4f4f20 integrated6b8099a:38 focused pure/debug/native APKs/lint PASS. SunriseStorageTest6 and SunriseTimeFlowTest1/four originals execution pending CI40164774515."
        row["implementation"] += ["GoalAlarmTime.java", "GoalAlarmPlan.java", "GoalAlarmTimeActivity.java"]
        row["validation"] += " CI40164774515 SunriseStorageTest6/SunriseTimeFlowTest1/all4 originals PASS.9396f81 adds explicit fixed-time copy from saved sleep plan:GoalAlarmTimeTest11 host PASS; GoalAlarmPlanStorageTest3/GoalAlarmTimeFlowTest2/seven originals native pending."
        row["notes"] = "Explicit dates plus optional manual-coordinate local sunrise schedules exist. Original solar calculation uses the current phone time zone, weekday/date rules and bounded offsets; polar/unsupported/no-crossing dates use an explicitly acknowledged ordinary-time fallback. Existing skip/calendar receipts retain the actual occurrence. Use sleep plan reviews a saved bedtime plus goal and copies a fixed local clock time, or sunrise fallback, into the editor; Save remains required and later plan edits do not auto-follow. Date/repeats preserved, DST elapsed differences disclosed. No GPS/network, actual alarm delivery or sunrise accuracy certification. See SUNRISE-ALARM-TIME.md and GOAL-DERIVED-ALARM.md."
    elif title in ("Cloud backup / SleepCloud", "User-controlled cloud / conflict / audio backup"):
        row["implementation"] += ["AutoCloudBackups.java", "AutoCloudBackupEngine.java", "AutoCloudBackupStore.java", "AutoCloudBackupPolicy.java", "AutoCloudBackupService.java", "CloudBackupLease.java"]
        row["validation"] += " ce800dc integratedbc6c01e adds default-off deferred backup:21 focused pure/debug/native APKs/lint PASS before final durable-Off refinement; final source combined build pending. AutoCloudBackupStorageTest12 and AutoCloudBackupFlowTest2/eight originals execution pending CI40164774515."
        row["validation"] += " Superseding CI40164774515 AutoCloudBackupStorageTest12 and first save/reopen UI method PASS; second uncertain-write confirmation method FAIL, two of eight required frames absent.6fbc212 fixes native dialog screen coordinates, fresh rerun pending."
        row["notes"] = "Manual verified create-only metadata versions and reviewed restore remain. Optional automatic backup queues only successfully ended production tracked nights into a separate durable journal, requires an explicitly selected writable SAF destination, bounds retries/size/queue and preserves uncertain provider outcomes without blind duplicate replay. Turning Off persists independently of blocked provider I/O; lease ownership remains until cleanup. Actual provider/account/job delivery unverified. Audio inclusion and proprietary SleepCloud service absent. See AUTOMATIC-METADATA-BACKUP.md."
    elif title == "Flip pause / delayed tracking start":
        row["implementation"] += ["TrackingFlip.java", "TrackingFlipRuntime.java", "TrackingFlipSettings.java", "TrackingFlipActivity.java"]
        row["validation"] += " a3445e7 combined956pure/117suites/debug/native APKs/lint PASS. CI40159540059 TrackingFlipFlowTest2/all7 originals PASS; delay storage/config and flip callback-clock assertion FAIL. aa5c72e fixes cold-boot fixture baseline, complete delay preset geometry and exact pause callback time; fresh native run pending."
        row["validation"] += " Superseding CI40164774515 TrackingDelayStorageTest4/TrackingDelayFlowTest2/all9 originals and TrackingFlipStorageTest3/TrackingFlipFlowTest2/all7 originals PASS."
        row["notes"] = "Default-off manual delay plus default-off movement-only flip pause are wired to real tracking. Delay allocates no capture/session/DND until durable due claim. Flip requires fresh calibrated orientation, closes capture before one same-session five-minute gap, and exposes keep-paused/resume states. Sonar requires fresh visible admission and does not support flip pause. Private/fake-sensor UI passes narrowly; actual capture/OEM/overnight behavior unverified. See MANUAL-TRACKING-DELAY.md and TRACKING-FLIP-PAUSE.md."
    elif title == "Playlists / shuffle / silent alarm":
        row["implementation"] += ["AlarmToneScreen.java"]
        row["validation"] += " Later CI40159540059 fails playlist reopening on asynchronous decoded-waveform page replacement.49a9a80 preserves actual footer/control identity when waveforms arrive; ToneWaveArrivalTest1 plus original flow enrolled for fresh native execution."
        row["validation"] += " Superseding CI40164774515 original mode Storage4/Flow1/all7 and ToneWaveArrivalTest1 PASS."
    elif title == "Object/picture task":
        row.update(status="partial", implementation=["PictureSignature.java", "PictureReference.java", "PictureReferenceJson.java", "PictureHold.java", "PictureProof.java", "PictureCamera.java", "PictureMatchActivity.java", "AlarmEditorActivity.java", "AlarmActivity.java"], validation="d705352 integrateddf17b50:PictureSignatureTest/PictureHoldTest10 host actual-pixel cases PASS under fixed declared policy. PictureNativeTest3/PictureMatchFlowTest1/five required originals compiled/execution pending combined build/CI.", notes="Explicit local reference enrollment and offline compact similarity signature, no retained image or upload. Matching requires quality gates and three fresh stable frames, current reference/run/unit proof and Math fallback. Permission/error/cancel/recreation and strict portable rules are wired; actual camera/device accuracy unverified. This is picture similarity, not semantic object recognition or liveness. See PICTURE-MATCH-CHALLENGE.md.")
        row["validation"] += " CI40166582977 PictureNativeTest3 PASS, PictureMatchFlowTest1 FAIL on offscreen Challenges title after successful cancellation.48b0917 integrated99d678a reveals the owned header before the unchanged observation; fresh native run pending."
    elif title == "Smile task":
        row["notes"] = "No accepted Smile task. Fixed-policy original OpenCV/Haar experiment stopped when the pinned positive failed the declared framing gate; smile classification was never reached. No fixture recrop, threshold tuning, model or dependency shipped. See SMILE-DETECTOR-ASSESSMENT.md."
    elif title == "Per-class sound sensitivity":
        row["implementation"] += ["SoundSensitivityJson.java", "SonarNightSound.java"]
        row["validation"] += "643c895 integrated92878b7 adds immutable inclusion profile:59 focused pure/debug/native APKs/lint0errors PASS; final tiny editor-port delta covered by combined build pending. SoundInclusionStorageTest3/SoundInclusionFlowTest3/eight originals native execution pending."
        row["notes"] += " Optional category inclusion rejects a disabled winning label before grouping/storage/cues without relabeling it Other. Any custom selection suppresses all new clips (not category-private clip recognition); saved consent/old audio stay unchanged. Corrupt or unavailable settings block new analysis/clips until explicit review and Save. Absent/v1 retain exact all-included defaults. See sound-analysis-inclusion.md."
        row["validation"] += " CI40166582977 SoundInclusionStorageTest3/SoundInclusionFlowTest3/all8 originals PASS; full run fails separately on Picture configuration."
    elif title == "Charging condition / standby mode":
        row.update(status="partial", implementation=["ArmedWaitConditions.java", "ArmedWaitSettings.java", "AutomaticSleep.java", "AutomaticTransition.java", "AutomaticTrackingActivity.java", "TrackingService.java", "TrackingActivity.java", "RecurringTracking.java"], validation="7b3e42e integrated6cec933:ArmedWaitTest8 plus AutomaticSleepTest13/AutomaticTransitionTest7=28 host PASS. ArmedWaitNativeTest3/ArmedWaitFlowTest1/three originals compile/native pending combined candidate.", notes="Default-off Arm tonight conditions wait for charging and/or screen off within the existing four-hour admission window. Conditions are frozen per arm, blocked stillness resets and final recording admission rechecks them; no record/capture while blocked. Already active capture does not stop on unplug. Recurring due-time charging skip remains independent. Actual OS/charging/sensor/FGS behavior unverified; this is not battery-adaptive standby sampling. See ARMED-WAIT-CONDITIONS.md.")
    elif title in ("Automatic tracking", "Scheduled tracking"):
        row["implementation"] += ["ArmedWaitConditions.java", "ArmedWaitSettings.java", "AutomaticStart.java"]
        row["validation"] += " 264c2bb fixes smart-window projection:AutomaticStartTest18 host PASS; combined/native pending.6cec933 adds charging/screen-off conditions with28 focused host PASS."
        row["notes"] += " Smart-window lead-in now uses the actual projected dated/sunrise/skipped/calendar-adjusted unconsumed deadline; expired or unsupported nap occurrences cannot admit capture. Optional charging/screen-off gates apply only to explicit Arm tonight, not background recurring tokens."
    elif title == "Dynamic snooze choices / until deadline":
        row["implementation"] += ["SnoozeUntil.java", "SnoozeUntilDialog.java", "SleepPicker.java", "AlarmActivity.java"]
        row["validation"] += " 9c72096 integrated3f3fa6d adds SnoozeUntilTest12 plus8 existing snooze host tests PASS. SnoozeUntilFlowTest2/seven originals compile/native pending; AlarmActivity/Service are forbidden in this quiet harness."
        row["notes"] = "Optional per-press duration chooser and configured1–60-minute choices retain defaults, shorten/count rules and occurrence ownership. When Choose snooze duration is enabled, Until… accepts an exact same-day local clock target within the current per-press cap. Current occurrence/count/settings/focus/zone/wall-and-elapsed timing are rechecked before exact deadline scheduling. Past/equal/out-of-range/DST gap/ambiguous targets reject visibly. Today-only; relative choices remain for midnight crossings. No actual snooze output tested. See snooze-until-time.md."
    elif title == "Calendar holiday skip / meeting shift / record write":
        row["validation"] += " Superseding CI40159540059 CalendarEventExportTest1 and CalendarEventExportFlowTest1/all4 originals PASS with a replaced calendar handoff port. No real event was saved."
    elif title in ("Goal bank / wagers", "Dated goal bank"):
        row["validation"] += " Superseding CI40159540059 DatedGoalBankFlowTest2/all10 originals PASS after owned-activity recreation handling; this does not make the full native run pass."
    elif title == "Dream diary / mirror text task":
        row["implementation"] += ["DiaryJobs.java"]
        row["validation"] += " CI40159540059 TypedWakeFlowTest2/all11 originals and DiaryTerminalStateTest1 PASS after4888864 consumes save/delete terminal results even when the worker finishes before first render."
    elif title == "Laughter / instrument microphone task":
        row.update(status="missing", validation="Independent full native laughter positive remains FAIL. Fixed-policy piano experiment accepted0/2 pinned CC0 positives; full scores retained without production policy changes. See verification/2026-10-25-piano-challenge.", notes="No accepted laughter/instrument task. Neither thresholds nor fixtures were changed to force acceptance. Separate Speak task detects broad speech only and does not fulfill this feature.")

rows.append(dict(feature="Speak microphone challenge", screens="03,10 + microphone permission/cleanup/fallback", source=SOURCE+"alarms/captcha.html", implementation=["SpeakChallengeActivity.java", "SpeakRuntime.java", "SpeakAudio.java", "SpeakCaptureService.java", "SpeakAdmission.java", "MicrophoneLease.java", "AlarmService.java"], validation="a3445e7 combined956pure/117suites/debug/native APKs/lint PASS. CI40159540059 SpeakStorageTest1, SpeakConfigurationFlowTest1, SpeakCaptureFlowTest2 and SpeakFixtureEvaluationTest1/all12 originals PASS. Six pinned whole-source CPU trials:two broad-speech positives accepted,four snore/music/cry distractors rejected. Whole24class/54method native run remains FAIL separately; raw bounded evidence in verification/2026-10-25-speak-native.", status="partial", notes="Explicit Listen, one-use visible microphone service admission, exclusive capture lease, matching-alarm quiet/cleanup barriers, two unchanged accepted speech windows and Math fallback are wired. No audio/transcript/event saved. Permission, cleanup, recreation and persistence tested with fake capture/output ports; actual microphone/FGS/alarm output and bedroom accuracy unverified. No speech transcription, speaker identity, laughter or instrument detection. See SPEAK-CHALLENGE.md."))

passing_increment = {
    "Object/picture task": "PictureNativeTest3/PictureMatchFlowTest1/all5 originals",
    "Date / sunrise / goal-derived alarm": "SunriseStorageTest6/SunriseTimeFlowTest1/all4 and GoalAlarmPlanStorageTest3/GoalAlarmTimeFlowTest2/all7 originals",
    "Charging condition / standby mode": "ArmedWaitNativeTest3/ArmedWaitFlowTest1/all3 originals",
    "Dynamic snooze choices / until deadline": "SnoozeUntilFlowTest2/all7 originals",
    "Per-class sound sensitivity": "SoundInclusionStorageTest3/SoundInclusionFlowTest3/all8 originals",
    "Cloud backup / SleepCloud": "AutoCloudBackupStorageTest12/AutoCloudBackupFlowTest2/all8 originals",
    "User-controlled cloud / conflict / audio backup": "AutoCloudBackupStorageTest12/AutoCloudBackupFlowTest2/all8 originals",
    "Flip pause / delayed tracking start": "TrackingDelayStorageTest4/TrackingDelayFlowTest2/all9 and TrackingFlipStorageTest3/TrackingFlipFlowTest2/all7 originals",
    "Playlists / shuffle / silent alarm": "AlarmSoundModeStorageTest4/AlarmSoundModeFlowTest1/all7 originals and ToneWaveArrivalTest1",
    "Speak microphone challenge": "SpeakStorageTest1/SpeakConfigurationFlowTest1/SpeakCaptureFlowTest2/SpeakFixtureEvaluationTest1/all12 originals and six pinned CPU trials",
}
for row in rows:
    if row["feature"] in passing_increment:
        row["validation"] += " Superseding6ec80fb CI40167823818 PASS38classes/97methods/all121 originals: " + passing_increment[row["feature"]] + ". Exact same source combined1046pure/127suites/debug/native APKs/lint0errors142warnings PASS5m37. This is private/fake-port evidence, not physical/design/full-app acceptance."
    elif row["feature"] == "Achievements":
        row.update(status="partial", implementation=["RecordAchievements.java", "RecordAchievementsReader.java", "RecordAchievementsActivity.java", "ReportScreen.java"], validation="f8c125a integrated0d51760:RecordAchievementsTest9 host PASS and production/native direct Java compilation PASS. RecordAchievementsStorageTest2/RecordAchievementsFlowTest1/six required originals compiled/execution pending CI40168584743. Combined Gradle pending.", notes="Report exposes derived first/7/30 qualifying recorded days from known valid motion/sonar coverage, excluding incomplete/unknown/manual/HC evidence and deduplicating physical overlaps. Three-hour recording coverage is a documented criterion, not measured sleep or clinical advice. Progress recalculates from edits/deletions; no stored fake awards, schema change, retrospective current-goal comparisons or rewards. Untagged naps cannot be inferred. See RECORDED-ACHIEVEMENTS.md.")
        row["validation"] += " Supersedinge26e1ca CI40168584743 PASS40classes/100methods/all127 originals, including RecordAchievementsStorageTest2/RecordAchievementsFlowTest1/all6 originals."
    elif row["feature"] == "Power nap":
        row.update(status="partial", implementation=["NapPlan.java", "NapPlanJson.java", "NapEngine.java", "NapActivity.java", "Naps.java"], validation="24fd667 integrated1d9da36:PowerNapTest8 plus NapEngineTest16=24 host cases PASS. PowerNapNativeTest3/PowerNapFlowTest1/four originals and unchanged NapFlowTest3/NapIntegrationFlowTest1/23 originals registered for CI40169243915; combined build/native pending.", notes="Explicit protected minimum rest plus reviewed later-wake interval reuse the actual elapsed relative timer and fixed latest deadline. NapEngine rejects smart claims before the minimum. Optional smart movement uses only already-active tracking; otherwise visible timer-only fallback, with no sensor/microphone start. Strict optional portable metadata retains byte-identical ordinary naps and imports inactive. Save/cancel/recreate/reopen, actual delivery/OEM and physical motion still need separate evidence. No stage or measured sleep-onset claim. See NAPS.md.")
    elif row["feature"] == "Nap timer":
        row["notes"] = row["notes"].replace("Distinct minimum-rest Power Nap remains missing;", "Distinct minimum-rest Power Nap is source-integrated with pending combined/native checks;")

# Parent inventory entries share the narrower component's actual evidence, not a
# contradictory stale 'missing' label. Unimplemented subfeatures remain explicit.
by_title = {row["feature"]: row for row in rows}
for parent, component in {
    "Other CAPTCHA pack tasks": "Tilt maze / jumping / flags / counting tasks",
    "Missing-night estimation": "Missed-night estimate review / provenance",
    "Chronotype / social jetlag / SRI": "Recorded rhythm / social jetlag",
}.items():
    target, detail = by_title[parent], by_title[component]
    for key in ("status", "implementation", "validation", "notes"):
        target[key] = list(detail[key]) if isinstance(detail[key], list) else detail[key]
    if parent == "Chronotype / social jetlag / SRI":
        target["notes"] += " SRI and clinical chronotype remain missing; recorded midpoint comparison does not establish either."

for row in rows:
    if row["feature"] in set(passing_increment) | {"Power nap", "Nap timer", "Achievements"}:
        row["validation"] += " Final0a821eb CI40170787840 PASS44classes/108methods/all154 originals; exact source1063pure/129suites PASS41s, debug/native APKs+lint0errors142warnings PASS1m36. Raw evidence:verification/2026-10-25-final-feature-batch. Prior failed runs remain historical, not relabelled green."
    if row["feature"] == "Power nap":
        row["notes"] = row["notes"].replace("Save/cancel/recreate/reopen, actual delivery/OEM and physical motion still need separate evidence.", "Private save/cancel/recreate/start/reopen and unchanged ordinary-nap regression pass. Actual timer delivery/OEM and physical motion remain unverified.")
    elif row["feature"] == "Nap timer":
        row["notes"] = row["notes"].replace("Distinct minimum-rest Power Nap is source-integrated with pending combined/native checks;", "Distinct minimum-rest Power Nap and ordinary native regression now pass in the stated quiet scope;")
    elif row["feature"] == "Achievements":
        row["status"] = "implemented"

test_paths = {path.stem: path.relative_to(ROOT).as_posix()
              for source_set in ("test", "androidTest")
              for path in (ROOT / "app/src" / source_set / "java/org/sleepwell/app").glob("*Test.java")}

for n, row in enumerate(rows, 1):
    row["id"] = f"F{n:03}"
    row["validation"] = re.sub(r"(\b\w+Test)(\d+)", r"\1 \2", row["validation"])
    row["tests"] = [{"name": name, "path": test_paths[name]}
                    for name in sorted(set(re.findall(r"\b\w+Test\b", row["validation"])))
                    if name in test_paths]

header = """# Phone feature matrix

Reviewed 2026-10-25 against the supplied brief, all 12 visually inspected boards,
SCREEN-MAP.md, current code and official Sleep as Android manual. Screen numbers
are intended reference destinations, not a claim of completed screens.

**Implemented**: narrower stated behavior exists; validation is separately listed.
**Partial**: subfeatures still missing. **Missing**: no working implementation.
**Blocked**: stated OS or external prerequisite. Passing compilation, a screenshot,
or a test source file alone is not evidence of functional completion.

Watch companions, fitness bands, oximeters and hardware vital measurements are
excluded. Sonar, optional smart lights and sleep-only Health Connect remain in scope.
Vendor proprietary assets/algorithms and hosted services are not copied.
This matrix supersedes the wearable roadmap in the older FEATURES.md.

Baseline unit/lint/build passed before UI work. New device results and visual
comparisons are recorded in DESIGN-REVIEW.md and VERIFICATION.md. Existing code links
show the relevant group of implementation classes; exact narrower behavior and gaps
are stated in the last column. Named tests link to committed source; those links
are not passing-run claims. Most features still need device-level verification.

Latest phone installation: source571018b, same application source asdc30736,
schema7 retained, all original rows/preferences and installed APK hash verified.
No alarm/sound/vibration/flashlight/tracking test ran on the phone.
Latest full native run3463834/CI40124416810 remains FAIL: Walk task hit height,
one dated-goal target/date flow and the existing independent laughter positive.
Other passing classes do not make that full run green. Focused f9f7acb shared Night
CI40120286873 passes20methods/25originals; category browsing CI40120293302 passes
13methods/68originals.3463834 sound sensitivity CI40124391172 passes10methods/14originals.
Superseding da0f571 CI40141612460 passes23classes/77methods/all68originals for the
feature sprint, including corrected calendar/defaults/HC/date/goals/tag/cloud flows.
Local combined778pure/100suites, debug/native APKs and lint0errors98warnings pass7m03.
See verification/2026-10-25-feature-sprint/README.md for original bounded evidence.
Expanded4b7d017 CI40147599727 passes30classes/102methods/all90originals, adding
search, diary, shared typed challenges, fake-host widget contracts and backup timing.
See verification/2026-10-25-feature-sprint-expanded/README.md. Newer request/challenge
increments remain pending native execution; no physical acceptance is inferred.
Later a3445e7 combined956pure/117suites/debug/native APKs/lint0errors139warnings
passes; exact raw build evidence is in verification/2026-10-25-speech-flip-build.
CI40159540059 remains FAIL overall, with narrow Speak5methods/12originals,
flip UI2methods/7originals and corrected dated-goal/calendar/diary flows passing.
CI40164774515 fails only automatic-backup confirmation; corrected CI40166582977
passes backup14methods/all8 originals, inclusion6/all8 and sleep-plan5/all7.
That35class/91method run still fails Picture configuration; its owned-title
observation fix and new armed-wait/snooze flows await CI40167823818.
Combined8027f83 passes1016pure/125suites, both APKs and lint0errors142warnings;
raw evidence:verification/2026-10-25-picture-inclusion-build.
Superseding6ec80fb CI40167823818 PASS38classes/97methods/all121 originals,
including corrected Picture, armed conditions and exact-clock snooze. Same source
passes1046pure/127suites, both APKs and lint0errors142warnings in5m37. Raw evidence:
verification/2026-10-25-feature-increment-pass and2026-10-25-armed-snooze-build.
Final0a821eb CI40170787840 PASS44classes/108methods/all154 originals, including
achievements, protected Power Nap and unchanged ordinary-nap regression. Exact
source1063pure/129suites PASS41s; debug/native APKs+lint0errors142warnings PASS1m36.
The predecessorba2d478 also built an unsigned release APK; the current UI correction
has not been rebuilt as a signed production release. Raw evidence:
verification/2026-10-25-final-feature-batch. These are scoped functional results,
not full-app/design/physical/overnight/provider-account acceptance.
At the user's request, the dedicated design reviewer is stopped and broad design,
bug and security audits are deferred while agents implement features. Real acoustic
accuracy, overnight reliability and full36screen parity remain uncertified.


| ID / feature and manual | Screen(s) | Code | Validation | Status and remaining work |
| --- | --- | --- | --- | --- |
"""
for row in rows:
    code = ", ".join(f"[{name[:-5]}](../app/src/main/java/org/sleepwell/app/{name})" for name in row["implementation"]) or "—"
    notes = row["notes"].replace("|", "/")
    validation = re.sub(r"\b\w+Test\b", lambda match: f"[{match[0]}](../{test_paths[match[0]]})" if match[0] in test_paths else match[0], row["validation"])
    header += f"| {row['id']} [{row['feature']}]({row['source']}) | {row['screens']} | {code} | {validation} | **{row['status']}** — {notes} |\n"
(ROOT / "docs/FEATURE-MATRIX.md").write_text(header, encoding="utf-8")
(ROOT / "docs/feature-matrix.json").write_text(json.dumps(dict(reviewed="2026-10-25", features=rows), indent=2, ensure_ascii=False)+"\n", encoding="utf-8")
print(f"Persisted {len(rows)} phone-capable feature entries")
