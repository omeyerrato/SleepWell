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
        row.update(status="partial", screens="07–08 + supporting flashlight state", implementation=["Alarm.java", "AlarmWakePolicy.java", "AlarmPlayback.java", "AlarmAudioSink.java", "AlarmVolumeLease.java", "AlarmService.java", "AlarmSunrise.java", "AlarmTorch.java", "TorchLease.java", "AlarmActivity.java", "AlarmEditorActivity.java", "AlarmRampChart.java", "Scheduler.java"], validation="Candidate: AlarmWakePolicyTest 6, AlarmPlaybackTest 15, AlarmVolumeLeaseTest 10, AlarmLightTest 9 pure passes. Native configuration/screenshots and strict source review pending. No actual alarm/effects test authorized.", notes="Native runtime candidate: max-device-volume opt-in, shared continuous planned ramp, sound delay, window-only sunrise and optional steady flashlight with ownership cleanup. No speaker boost, smart-light control or independent vibration delay. Occurrence-fenced async activation/backup, durable stream baseline and permanent quiet digital cap. See ALARM-WAKE-UP.md for exact limitations; device output is not certified.")
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
            notes="Bundled Apache-2.0 offline YAMNet estimates snoring/speech. Candidate policy respects compatible label hierarchy while retaining unrelated-class margin gates; uncertain sounds stay Other. Analysis-only intervals persist without WAV. Cough/baby/laughter categories, user correction and broader validation remain missing. See SOUND-ANALYSIS.md.")
    elif title == "Decreasing snooze / total snooze cap":
        row.update(status="partial", screens="09",
            implementation=["SnoozePolicy.java", "Alarm.java", "AlarmEditorActivity.java", "AlarmActivity.java"],
            validation="SnoozePolicyTest three pure passes; AlarmMenusFlowTest native configuration/recreation/save/reopen/cancel passes at e00ff55/CI40002966787 with sixteen original screenshots, including long selected choices. No physical alarm/snooze execution.",
            notes="Optional per-alarm decreasing delay shares display/schedule policy with a one-minute floor; legacy/import defaults preserve fixed snoozes. Separate total-minute cap remains missing.")
    elif title == "Noise library":
        row.update(status="partial", screens="20",
            implementation=["SoundEventsActivity.java", "SoundEventRows.java", "SoundClips.java", "ClipReviewPlayer.java", "ClipPlayback.java", "ClipWave.java", "ClipSeekView.java", "Store.java"],
            validation="SoundEventsUiTest passes at e00ff55/CI40002966787 for the older category/recreation UI. New SoundLibraryStorageTest and SoundLibraryUiTest require fresh native acceptance: schema5 migration, per-event/shared-WAV favorites, actual PCM interval waveforms, seek, fake playback, deletion and retention. No physical playback/microphone hardware actions.",
            notes="New per-night inline PCM waveform/player, persisted per-event or legacy-recording favorites and explicit estimate-versus-shared-recording deletion are integrated candidates. Classification-only and missing audio never invent a player. Configurable retention preserves audio linked to any favorite. Actual native player output, global album, export and full board07 fidelity remain open.")
    elif title == "Retention / favorite album / alternate formats":
        row.update(status="partial", screens="20,32",
            implementation=["SoundClips.java", "SoundLibrarySettingsActivity.java", "SoundEventsActivity.java", "NoiseRecorder.java", "HistoryErase.java", "HistoryEraseJobs.java", "HistoryEraseUi.java", "Store.java"],
            validation="SoundLibraryStorageTest three actual SQLite/file fixtures and SoundLibraryUiTest two native fake-output flows await execution. PrivacyHistoryFlowTest covers retained background erase, real failed unlink and separate retry preserving newer nights; its source is not a passing-run claim.",
            notes="Additive schema6 preserves existing rows/WAVs and adds favorites plus identity-checked pending deletion journal. Retention can keep1/3/7/14/30days or all, with favorite protection. Background per-run cleanup and explicit policy apply replace hardcoded main-thread pruning. Global album, portable audio export and alternate formats remain incomplete.")
    elif title == "Dream and travel light cues":
        row.update(status="partial", screens="27",
            implementation=["TravelActivity.java", "TravelPlan.java", "TravelPlans.java", "TravelClock.java", "TravelReminders.java", "TravelReminderPolicy.java", "TravelReminderReceiver.java", "TravelBootReceiver.java"],
            validation="TravelPlanTest and TravelReminderPolicyTest eighteen pure passes. TravelPlanFlowTest source covers actual zone/date/time Save/recreation/reopen, fake scheduler/delivery, stale/duplicate/rollback/failure/retry/disable and seven native captures; execution and visual acceptance pending.",
            notes="Saved actual IANA zones, DST-aware clocks/date differences and finite user-chosen destination-time silent daylight reminders. No clinical jet-lag prevention algorithm, experimental flashlight therapy, dream/REM cue output or smart-light integration. Real OS delivery/reboot/permission behavior unverified. See TRAVEL-PLAN.md.")
    elif title in ("Anti-snoring cues", "Headphone-only cues / intensity / repeat"):
        row.update(status="partial", screens="21",
            implementation=["SnoreCuePolicy.java", "SnoreCueOutput.java", "SnoringCuesActivity.java", "NoiseRecorder.java", "ClipBudget.java"],
            validation="SnoreCuePolicyTest pure fake-output tests pass. SnoreCueSettingsNativeTest passes at 8fd7dd9/CI39997627697: saved options, recreation/reopen and four original screenshots with fake output. Real positive sound fixture gate, hardware cue operation and effectiveness unverified.",
            notes="Default-off cues after confident snoring estimates, selectable sound/vibration/both, intensity/repeats/sensitivity, two-minute cooldown and thirty-per-session cap. No global-volume writes; alarm/disable/close guards. Headphone-only routing and preview remain missing. No medical effectiveness claim.")

test_paths = {path.stem: path.relative_to(ROOT).as_posix()
              for source_set in ("test", "androidTest")
              for path in (ROOT / "app/src" / source_set / "java/org/sleepwell/app").glob("*Test.java")}

for n, row in enumerate(rows, 1):
    row["id"] = f"F{n:03}"
    row["tests"] = [{"name": name, "path": test_paths[name]}
                    for name in sorted(set(re.findall(r"\b\w+Test\b", row["validation"])))
                    if name in test_paths]

header = """# Phone feature matrix

Reviewed 2026-10-23 against the supplied brief, all 12 visually inspected boards,
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

| ID / feature and manual | Screen(s) | Code | Validation | Status and remaining work |
| --- | --- | --- | --- | --- |
"""
for row in rows:
    code = ", ".join(f"[{name[:-5]}](../app/src/main/java/org/sleepwell/app/{name})" for name in row["implementation"]) or "—"
    notes = row["notes"].replace("|", "/")
    validation = re.sub(r"\b\w+Test\b", lambda match: f"[{match[0]}](../{test_paths[match[0]]})" if match[0] in test_paths else match[0], row["validation"])
    header += f"| {row['id']} [{row['feature']}]({row['source']}) | {row['screens']} | {code} | {validation} | **{row['status']}** — {notes} |\n"
(ROOT / "docs/FEATURE-MATRIX.md").write_text(header, encoding="utf-8")
(ROOT / "docs/feature-matrix.json").write_text(json.dumps(dict(reviewed="2026-10-23", features=rows), indent=2, ensure_ascii=False)+"\n", encoding="utf-8")
print(f"Persisted {len(rows)} phone-capable feature entries")
