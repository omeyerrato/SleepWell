"""Ephemeral CI emulator only. No alarm output or hardware capture tests.

Default: configuration/rendering only. Optional --native-sounds exercises real
MediaPlayer/notification controls with host audio disabled, never physical hardware.
The Android test runner is invoked directly with explicit classes. Never use
connectedCheck here, because the suite also contains non-allowlisted tests.
Only isolated emulator fixtures/screenshots are published, never phone backups.
"""
from pathlib import Path
import argparse
import atexit
import json
import hashlib
import zipfile
import os
import re
import subprocess
import time

parser = argparse.ArgumentParser()
parser.add_argument("--serial", required=True, choices=("emulator-5554",))
parser.add_argument("--scope", choices=("full", "tone-wake", "report", "report-flow", "organization", "organized-settings", "recorded-analysis", "challenge-routine", "tonight", "appearance", "event-library", "goal-plan", "interaction-security", "priority-menus", "priority-current", "priority-repair", "reference-menus", "privacy-recovery", "setup-readiness", "readiness-recovery", "sensing-reference", "sensing-travel", "travel-ui", "sonar-night", "sound-categories", "sound-sensitivity", "phone-features", "feature-increment", "nap-features"), default="full", help="A named narrow scope is never a full app acceptance run")
parser.add_argument("--native-sounds", action="store_true", help="Real media service tests on this no-host-audio CI emulator only")
parser.add_argument("--audio-imports", action="store_true", help="Actual document picker and imported playback on this isolated emulator only")
args = parser.parse_args()
if args.scope != "full" and (args.native_sounds or args.audio_imports):
    raise SystemExit("Focused review does not allow optional native output/import tests.")
TONE_FRAMES = ["alarm-tone-" + name + ".png" for name in ("builtins", "playing", "paused", "preview-error", "options", "selected", "import-checking", "recreated", "import-cancelled", "decoder-busy", "decoder-recovered", "import-empty", "import-error", "imported", "reopened", "cancelled")]
WAKE_SETTING_FRAMES = ["wake-settings-" + name + ".png" for name in ("sound", "loud-curve", "delay-options", "vibration-options", "lights", "recreated", "reopened", "normal-curve", "edited-curve", "immediate")]
WAKE_CHECK_FRAMES = ["wake-check-" + name + ".png" for name in ("waiting", "prompt", "countdown", "recreated", "confirmed", "reopened", "storage-error", "access-error", "delivery-error", "retry", "expired", "stale", "editor-save-error")]
FOCUSED_TESTS = ("AlarmToneAssetsTest", "AlarmToneGrantTest", "AlarmToneFlowTest", "AlarmWakeSettingsTest", "WakeCheckFlowTest")
ORGANIZED_SETTINGS_FRAMES = ["organized-settings-" + name + ".png" for name in ("sensitivity-categories", "sensitivity-duration", "sensitivity-reopened", "retention-details", "snoring-options", "automatic-conditions", "recurring-conditions", "sounds-sources", "sounds-playback", "timer-recreated", "fade-reopened")]
SETUP_READINESS_FRAMES = ORGANIZED_SETTINGS_FRAMES + ["organized-settings-tracking-"+name+".png" for name in ("main","more","sensitivity","power","power-recreated","recreated","automatic-manual","phone-selected")]
SETUP_READINESS_FRAMES += ["protection-"+name+".png" for name in ("alarms","editor","off","disclosure","on","recreated","global","power-only","readiness","readiness-rationale","readiness-rationale-cancel","readiness-settings-recovery","readiness-returned")]
RECORDED_ANALYSIS_FRAMES = ["recorded-analysis-" + name + ".png" for name in ("prepared", "result", "details", "import-prepared", "import-result")]
ROUTINE_FRAMES = ["routine-" + name + ".png" for name in ("legacy", "ordered", "reordered", "recreated", "removed", "reopened")]
EVENT_LIBRARY_FRAMES = ["library-"+name+".png" for name in ("recordings","playback","favorite-recreated","speech","recording-deleted","options","options-recreated","analysis-details","analysis-details-recreated")]
GOAL_PLAN_FRAMES = ['goal-plan.png', 'goal-options.png', 'goal-options-recreated.png', 'goal-options-winddown-recreated.png', 'goal-options-updated.png', 'goal-tonight.png', 'goal-saved-plan.png', 'goal-empty-bank.png', 'goal-create.png', 'goal-bank.png', 'goal-reopened-bank.png', 'goal-bank-details.png', 'goal-stopped.png', 'goal-trends.png']
INTERACTION_SECURITY_FRAMES = ["calendar-rules-permission-"+name+".png" for name in ("settings","granted-draft","revoked-draft")]
PRIORITY_MENU_FRAMES = ["advanced-snooze-"+name+".png" for name in ("initial","options","options-recreated","draft","recreated","reopened","sound-picker","sound-picker-recreated","invalid","repaired-draft")]
PRIORITY_MENU_FRAMES += ["qr-code-"+name+".png" for name in ("permission","invalid","denied","access-error","scanning","camera-error","captured","fallback","fallback-error","recreated","routine","reopened","unavailable")]
PRIORITY_CURRENT_FRAMES = ["qr-code-"+name+".png" for name in ("permission","invalid","denied","access-error","scanning","camera-error","captured","fallback","fallback-error","recreated","routine","reopened","unavailable")]
PRIORITY_CURRENT_FRAMES += WAKE_SETTING_FRAMES + ["wake-settings-"+name+".png" for name in ("options","options-recreated","preview-loading","preview-playing","preview-recreated","preview-error","preview-complete")]
PRIORITY_CURRENT_FRAMES += ["rhythm-"+name+".png" for name in ("seven-nights","workdays","recreated","reopened","empty")]
PRIORITY_CURRENT_FRAMES += ["nap-"+name+".png" for name in ("default","preset","tracking-required","sound","pending","recreated","reopened","cancelled")]
PRIORITY_CURRENT_FRAMES += ["health-connect-"+name+".png" for name in ("default","configured","synced","source","conflict","kept","unavailable","permissions")]
PRIORITY_CURRENT_FRAMES += ["privacy-history-"+name+".png" for name in ("ready","error","busy","pending","done")]
PRIORITY_CURRENT_FRAMES += ["backup-document-"+name+".png" for name in ("review","confirmation","review-recreated","review-reopened","restoring","restoring-recreated","restored","result-reopened")]
PRIORITY_REPAIR_FRAMES = [name for name in PRIORITY_CURRENT_FRAMES if name.startswith(("wake-settings-","health-connect-","privacy-history-"))]
PRIORITY_REPAIR_FRAMES += [name for name in PRIORITY_MENU_FRAMES if name.startswith("advanced-snooze-")]
REFERENCE_MENU_FRAMES = [name for name in PRIORITY_CURRENT_FRAMES if name.startswith(("wake-settings-","privacy-history-","qr-code-"))] + TONE_FRAMES
REFERENCE_MENU_FRAMES += ["cloud-backup-"+name+".png" for name in ("folder-busy","destination","saved","reopened","restore-review")]
APPEARANCE_FRAMES = ["appearance-"+name+".png" for name in ("automatic","selected-0","selected-1","selected-2","tonight-0","tonight-1","tonight-2","large-contrast-tonight","large-contrast-tonight-action","large-contrast")]
TONIGHT_FRAMES = ["arc-" + name + ".png" for name in ("upcoming", "help", "start", "middle", "end", "next", "large", "large-action", "weekly")]
SONAR_REPORT_FRAMES = ["sonar-report-" + name + ".png" for name in ("night", "signals", "zero", "reopened", "legacy-unknown", "analysis-error")]
GOAL_FRAMES = ["goal-" + name + ".png" for name in ("plan", "tonight", "saved-plan", "empty-bank", "create", "bank", "reopened-bank", "bank-details", "stopped", "trends")]
DATED_GOAL_FRAMES = ["dated-goal-" + name + ".png" for name in ("plan", "history", "trends", "linked", "empty", "target", "calendar", "saved", "error", "error-recreated")]
REPORT_TESTS = ("RootVisualTest", "ChartClockFlowTest", "ReportRibbonNativeTest", "ReportReferenceFlowTest", "SonarReportFlowTest", "GoalStorageTest", "GoalFlowTest", "DatedGoalBankFlowTest")
REPORT_FRAMES = ["report-reference-" + name + ".png" for name in ("history", "week", "month", "year", "night", "editor", "tags-notes", "reopened")] + ["root-report.png"] + ["chart-clock-" + name + ".png" for name in ("overnight", "night", "short", "fold")]
REPORT_FRAMES += SONAR_REPORT_FRAMES
REPORT_FRAMES += GOAL_FRAMES + DATED_GOAL_FRAMES
SHARED_SOUND_TESTS = ("SonarSharedSoundFlowTest", "SonarSharedClassifierStorageTest")
SHARED_SOUND_FRAMES = ["sonar-shared-opt-in-" + name + ".png" for name in ("default-off", "opt-in-recreated", "analysis-live", "analysis-paused", "stop-confirmation")]
SHARED_SOUND_FRAMES += ["sonar-shared-error-" + name + ".png" for name in ("model-error", "model-error-recreated", "model-error-resumed", "stop-confirmation")]
NIGHT_TESTS = ("SharedSoundProvenanceStorageTest", "SonarNightStorageTest", "SonarNightFlowTest", "SonarReportFlowTest") + SHARED_SOUND_TESTS
NIGHT_FRAMES = ["sonar-night-" + name + ".png" for name in ("setup", "measuring", "paused", "paused-recreated", "resumed", "missing", "stop-confirmation", "needs-setup", "charging-error", "charging-recreated")] + SONAR_REPORT_FRAMES
NIGHT_FRAMES += SHARED_SOUND_FRAMES
SENSING_TESTS = ("SharedSoundProvenanceStorageTest", "SonarProvenanceStorageTest", "SonarSettingsNativeTest", "ContactlessSetupFlowTest", "SonarNightStorageTest", "SonarNightFlowTest", "SonarReportFlowTest", "TravelCueStorageTest", "TravelCueFlowTest") + SHARED_SOUND_TESTS
SENSING_FRAMES = ["sonar-setup-" + name + ".png" for name in ("initial", "ready", "gain-changed", "retested", "reopened", "recreated", "testing", "cancelled", "cancel-recreated", "charging-error", "unsupported", "permission", "saving", "saving-recreated", "save-error")]
SENSING_FRAMES += ["travel-cues-" + name + ".png" for name in ("settings-off", "window", "date", "time", "window-recreated", "settings-enabled", "settings-reopened", "invalid-window", "no-manual", "ready", "active", "active-recreated", "stopped", "stopped-reopened", "output-error", "interrupted")]
SENSING_FRAMES += ["sonar-night-" + name + ".png" for name in ("setup", "measuring", "paused", "paused-recreated", "resumed", "missing", "stop-confirmation", "needs-setup", "charging-error", "charging-recreated")]
SENSING_FRAMES += SONAR_REPORT_FRAMES
SENSING_FRAMES += SHARED_SOUND_FRAMES
TRAVEL_UI_TESTS = ("TravelPlanFlowTest", "TravelCueFlowTest")
TRAVEL_UI_FRAMES = ["travel-" + name + ".png" for name in ("plan", "zones", "date", "reminders", "reopened", "error", "disabled", "schedules", "reminders-recreated", "reminders-reopened", "disabled-main")]
TRAVEL_UI_FRAMES += ["travel-cues-" + name + ".png" for name in ("settings-off", "window", "date", "time", "window-recreated", "settings-enabled", "settings-reopened", "invalid-window", "no-manual", "ready", "active", "active-recreated", "stopped", "stopped-reopened", "output-error", "interrupted", "light-settings", "light-settings-recreated", "review-from-menu", "about-recreated")]
CATEGORY_TESTS = ("SoundCategoryStorageTest", "SoundCategoryFlowTest", "SoundEventsUiTest", "FavoriteSoundsFlowTest", "ReportReferenceFlowTest", "SoundArchiveUiTest", "SoundLabelFlowTest", "SoundLibraryUiTest")
CATEGORY_NEW_FRAMES = ["sound-category-" + name + ".png" for name in ("night-all", "night-selector", "night-cough", "night-playing", "night-recreated", "night-reopened", "night-other-empty", "night-baby", "album-selector", "album-laughter", "album-week", "album-recreated", "album-reopened", "album-speech")]
CATEGORY_FRAMES = CATEGORY_NEW_FRAMES + ["analysis-events-" + name + ".png" for name in ("all", "speech", "recreated")]
CATEGORY_FRAMES += ["favorites-" + name + ".png" for name in ("all", "playback", "recreated", "week", "filter-empty", "speech", "playback-error", "unstarred", "reopened", "deleted-audio", "missing-audio", "empty", "error", "retry")]
CATEGORY_FRAMES += ["report-reference-" + name + ".png" for name in ("history", "week", "month", "year", "night", "editor", "tags-notes", "reopened")]
CATEGORY_FRAMES += ["library-export-" + name + ".png" for name in ("ready", "complete", "recreated", "failed")]
CATEGORY_FRAMES += ["sound-label-" + name + ".png" for name in ("source", "selector", "draft", "draft-recreated", "cancelled", "corrected", "reopened", "reset", "album", "busy", "busy-recreated", "error", "error-recreated", "retried", "trim-error", "trim-error-recreated", "deleted-error", "deleted-retry")]
CATEGORY_FRAMES += ["library-" + name + ".png" for name in ("recordings", "playback", "favorite-recreated", "speech", "recording-deleted", "retention-applied", "retention-recreated")]
SENSITIVITY_TESTS = ("SoundSensitivityStorageTest", "SoundSensitivityFlowTest", "SoundAnalysisNativeTest", "SoundClassifierFixtureEvaluationTest", "SoundEventsUiTest")
SENSITIVITY_NEW_FRAMES = ["sound-sensitivity-" + name + ".png" for name in ("default", "selector", "draft", "recreated", "saved", "reopened", "invalid", "saving", "saving-recreated", "save-error", "retry")]
SENSITIVITY_FRAMES = SENSITIVITY_NEW_FRAMES + ["analysis-events-" + name + ".png" for name in ("all", "speech", "recreated")]
PHONE_FEATURE_TESTS = ("CalendarRulesStorageTest", "CalendarRulesFlowTest", "AlarmDefaultsStorageTest", "AlarmDefaultsFlowTest", "AutomationStorageTest")
PHONE_FEATURE_TESTS += ("HealthSleepStorageTest", "HealthConnectFlowTest", "AlarmDateStorageTest", "AlarmDateFlowTest", "WalkConfigurationFlowTest", "DatedGoalBankFlowTest")
PHONE_FEATURE_TESTS += ("SmartLightStorageTest", "HomeAssistantProtocolTest", "AlarmInputsStorageTest", "AlarmInputsFlowTest", "MoonSearchStorageTest", "MoonSearchFlowTest", "TagStorageTest", "TagManagementFlowTest", "CloudBackupStorageTest", "CloudBackupFlowTest")
PHONE_FEATURE_TESTS += ("AdvancedSnoozeStorageTest", "AdvancedSnoozeFlowTest")
PHONE_FEATURE_TESTS += ("SearchStorageTest", "SearchFlowTest")
PHONE_FEATURE_TESTS += ("DiaryStorageTest", "TypedWakeFlowTest", "HomeWidgetNativeTest", "AlarmTimingStorageTest", "AlarmTimingFlowTest")
PHONE_FEATURE_TESTS += ("AlarmRequestStorageTest", "AlarmRequestFlowTest", "ChallengePackNativeTest")
PHONE_FEATURE_TESTS += ("TrackingDndStorageTest", "TrackingDndFlowTest")
PHONE_FEATURE_TESTS += ("AlarmSoundModeStorageTest", "AlarmSoundModeFlowTest", "CalendarEventExportTest", "CalendarEventExportFlowTest", "RecordInsightsNativeTest", "RecordInsightsFlowTest")
PHONE_FEATURE_TESTS += ("TrackingDelayStorageTest", "TrackingDelayFlowTest")
PHONE_FEATURE_TESTS += ("BarcodeCodeStorageTest", "BarcodeCodeFlowTest", "DiaryTerminalStateTest")
PHONE_FEATURE_TESTS += ("SpeakStorageTest", "SpeakConfigurationFlowTest", "SpeakCaptureFlowTest", "SpeakFixtureEvaluationTest", "TrackingFlipStorageTest", "TrackingFlipFlowTest")
PHONE_FEATURE_TESTS += ("SunriseStorageTest", "SunriseTimeFlowTest", "ToneWaveArrivalTest", "AutoCloudBackupStorageTest", "AutoCloudBackupFlowTest")
PHONE_FEATURE_TESTS += ("SoundInclusionStorageTest", "SoundInclusionFlowTest", "PictureNativeTest", "PictureMatchFlowTest", "GoalAlarmPlanStorageTest", "GoalAlarmTimeFlowTest")
PHONE_FEATURE_TESTS += ("ArmedWaitNativeTest", "ArmedWaitFlowTest")
PHONE_FEATURE_TESTS += ("SnoozeUntilFlowTest",)
PHONE_FEATURE_TESTS += ("RecordAchievementsStorageTest", "RecordAchievementsFlowTest")
PHONE_FEATURE_TESTS += ("PowerNapNativeTest", "PowerNapFlowTest", "NapFlowTest", "NapIntegrationFlowTest")
PHONE_FEATURE_FRAMES = ["calendar-rules-" + name + ".png" for name in ("initial", "draft", "saved", "reopened")]
PHONE_FEATURE_FRAMES += ["alarm-defaults-" + name + ".png" for name in ("initial", "recreated", "saved", "reopened", "reset", "saving", "saving-recreated", "save-error", "retry")]
PHONE_FEATURE_FRAMES += ["health-connect-" + name + ".png" for name in ("default", "configured", "synced", "source", "conflict", "kept", "unavailable", "permissions")]
PHONE_FEATURE_FRAMES += ["alarm-date-" + name + ".png" for name in ("initial", "selector", "draft", "recreated", "reopened")]
PHONE_FEATURE_FRAMES += ["walk-" + name + ".png" for name in ("permission", "ready", "settings", "error", "unavailable", "reopened")] + DATED_GOAL_FRAMES
PHONE_FEATURE_FRAMES += ["alarm-inputs-" + name + ".png" for name in ("initial", "selector", "draft", "recreated", "reopened")]
PHONE_FEATURE_FRAMES += ["moon-search-" + name + ".png" for name in ("initial", "wrong", "recreated", "complete", "new-board")]
PHONE_FEATURE_FRAMES += ["tags-" + name + ".png" for name in ("quantity-draft", "quantity-recreated", "editor", "comparison", "rename", "renamed", "delete-confirm", "deleted")]
PHONE_FEATURE_FRAMES += ["cloud-backup-" + name + ".png" for name in ("destination", "saved", "reopened", "restore-review")]
PHONE_FEATURE_FRAMES += ["advanced-snooze-" + name + ".png" for name in ("initial", "draft", "recreated", "reopened")]
PHONE_FEATURE_FRAMES += ["search-" + name + ".png" for name in ("empty", "no-results", "record", "recreated")]
PHONE_FEATURE_FRAMES += ["typed-wake-" + name + ".png" for name in ("config-mirror", "config-journal", "config-reopened", "mirror", "mirror-error", "journal-short", "journal-saved", "diary-list", "diary-edited", "diary-delete", "diary-empty")]
PHONE_FEATURE_FRAMES += ["alarm-timing-" + name + ".png" for name in ("initial", "selector", "configured", "recreated", "reopened", "continuous", "never")]
PHONE_FEATURE_FRAMES += ["alarm-request-" + name + ".png" for name in ("draft", "recreated", "reopened")]
PHONE_FEATURE_FRAMES += ["tracking-dnd-" + name + ".png" for name in ("default", "prerequisites", "saved", "ready", "owned", "cleanup-error", "cleaned")]
PHONE_FEATURE_FRAMES += ["alarm-mode-" + name + ".png" for name in ("initial", "silent", "playlist", "configured", "recreated", "reopened", "single")]
PHONE_FEATURE_FRAMES += ["calendar-event-" + name + ".png" for name in ("default", "recreated", "unavailable", "opened")]
PHONE_FEATURE_FRAMES += ["record-insights-" + name + ".png" for name in ("week", "goals", "reopened", "empty")]
PHONE_FEATURE_FRAMES += ["tracking-delay-" + name + ".png" for name in ("default", "draft-recreated", "saved", "reopened", "countdown", "countdown-recreated", "cancelled", "second-wait", "started")]
PHONE_FEATURE_FRAMES += ["barcode-code-" + name + ".png" for name in ("mode", "captured", "recreated", "routine", "reopened")]
PHONE_FEATURE_FRAMES += ["speak-config-" + name + ".png" for name in ("task", "recreated", "routine", "reopened")]
PHONE_FEATURE_FRAMES += ["speak-capture-" + name + ".png" for name in ("permission", "ready", "quiet", "listening", "closing", "recreated", "cleanup", "error")]
PHONE_FEATURE_FRAMES += ["tracking-flip-" + name + ".png" for name in ("default", "draft-recreated", "saved-reopened", "preview", "detected", "unsupported", "preview-stopped")]
PHONE_FEATURE_FRAMES += ["sunrise-time-" + name + ".png" for name in ("invalid", "configured", "recreated", "reopened")]
PHONE_FEATURE_FRAMES += ["automatic-backup-" + name + ".png" for name in ("off", "enabled", "recreated", "queued", "verified", "uncertain", "retried", "disabled")]
PHONE_FEATURE_FRAMES += ["sound-inclusion-" + name + ".png" for name in ("default", "cancelled", "draft-recreated", "reopened", "restored", "last-required", "saving-recreated", "speech-only")]
PHONE_FEATURE_FRAMES += ["picture-reference-" + name + ".png" for name in ("invalid", "captured", "recreated", "matched", "reopened")]
PHONE_FEATURE_FRAMES += ["goal-alarm-" + name + ".png" for name in ("missing", "invalid", "review", "recreated", "copied", "dated", "sunrise-fallback")]
PHONE_FEATURE_FRAMES += ["armed-wait-" + name + ".png" for name in ("settings", "recreated", "reopened")]
PHONE_FEATURE_FRAMES += ["snooze-until-" + name + ".png" for name in ("initial", "past", "too-late", "confirmed", "cancelled", "recreated", "stale")]
PHONE_FEATURE_FRAMES += ["record-achievements-" + name + ".png" for name in ("empty", "first", "recreated", "search", "edited", "error")]
PHONE_FEATURE_FRAMES += ["power-nap-" + name + ".png" for name in ("settings", "recreated", "pending", "reopened")]
PHONE_FEATURE_FRAMES += ["nap-" + name + ".png" for name in ("default", "preset", "tracking-required", "sound", "pending", "recreated", "reopened", "cancelled", "schedule", "schedule-error", "scheduled", "list", "permission-error", "scheduler-error", "retry")]
PHONE_FEATURE_FRAMES += ["nap-integration-" + name + ".png" for name in ("future", "clock-shift", "zone", "wind-down", "inactive", "started", "removed", "invalid")]
# Source4b7d017 already passed the first30 classes/102methods. This shorter scope
# exercises every later feature plus the two earlier flows with recent failures.
# It never replaces phone-features or the independent full-app classifier gate.
FEATURE_INCREMENT_TESTS = ("DatedGoalBankFlowTest", "TypedWakeFlowTest") + PHONE_FEATURE_TESTS[PHONE_FEATURE_TESTS.index("AlarmRequestStorageTest"):]
FEATURE_INCREMENT_PREFIXES = ("dated-goal-", "typed-wake-", "alarm-request-", "tracking-dnd-", "alarm-mode-", "calendar-event-", "record-insights-", "tracking-delay-", "barcode-code-", "speak-config-", "speak-capture-", "tracking-flip-", "sunrise-time-", "automatic-backup-", "sound-inclusion-", "picture-reference-", "goal-alarm-", "armed-wait-", "snooze-until-", "record-achievements-", "power-nap-", "nap-")
FEATURE_INCREMENT_FRAMES = [name for name in PHONE_FEATURE_FRAMES if name.startswith(FEATURE_INCREMENT_PREFIXES)]
NAP_FEATURE_TESTS = ("PowerNapNativeTest", "PowerNapFlowTest", "NapFlowTest", "NapIntegrationFlowTest")
NAP_FEATURE_FRAMES = [name for name in PHONE_FEATURE_FRAMES if name.startswith(("power-nap-", "nap-"))]
if os.environ.get("GITHUB_ACTIONS") != "true":
    raise SystemExit("This script is restricted to an ephemeral GitHub Actions emulator.")
root = Path(__file__).resolve().parents[1]
output = root / "captures/ci"
output.mkdir(parents=True, exist_ok=True)
adb = [str(Path(os.environ["ANDROID_HOME"]) / "platform-tools/adb"), "-s", args.serial]
emulator = str(Path(os.environ["ANDROID_HOME"]) / "emulator/emulator")
avds = subprocess.run([emulator, "-list-avds"], capture_output=True, text=True, timeout=20)
(output / "avds.txt").write_text(avds.stdout + avds.stderr, encoding="utf-8")
if "SleepWellSilent" not in avds.stdout.splitlines():
    raise SystemExit("The configured isolated AVD is missing; see avds.txt")
with (output / "acceleration.txt").open("w") as report:
    subprocess.run([emulator, "-accel-check"], stdout=report, stderr=subprocess.STDOUT, check=False, timeout=20)
log = (output / "emulator.log").open("w")
process = subprocess.Popen([emulator, "-avd", "SleepWellSilent", "-port", "5554", "-no-window", "-no-audio", "-no-boot-anim", "-no-snapshot", "-gpu", "swiftshader", "-feature", "-Vulkan", "-accel", "on", "-memory", "2048", "-cores", "2", "-timezone", "Asia/Riyadh"], stdout=log, stderr=subprocess.STDOUT)


def close_emulator():
    if process.poll() is None:
        # Keep guest evidence for startup/system ANRs as well as app failures.
        # This serial was admitted as an ephemeral qemu guest before any test.
        for name, parts in (("guest-logcat.txt", ["logcat", "-d", "-b", "all", "-t", "3000"]),
                            ("guest-last-anr.txt", ["shell", "dumpsys", "activity", "lastanr"]),
                            ("guest-windows.txt", ["shell", "dumpsys", "window", "windows"])):
            try:
                result = subprocess.run(adb + parts, capture_output=True, text=True, timeout=15)
                (output / name).write_text(result.stdout + result.stderr, encoding="utf-8")
            except Exception as error:
                (output / name).write_text("Guest diagnostic unavailable: " + str(error), encoding="utf-8")
        process.terminate()
        try:
            process.wait(timeout=15)
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait(timeout=5)
    log.close()


atexit.register(close_emulator)


def command(*parts, check=True, timeout=120):
    return subprocess.run(adb + list(parts), check=check, capture_output=True, text=True, timeout=timeout)


deadline = time.monotonic() + 300
while time.monotonic() < deadline:
    if process.poll() is not None:
        raise SystemExit("Android emulator exited before boot: " + str(process.returncode) + "; see emulator.log")
    status = command("shell", "getprop", "sys.boot_completed", check=False, timeout=15)
    if status.returncode == 0 and status.stdout.strip() == "1":
        break
    time.sleep(3)
else:
    raise SystemExit("Android emulator did not boot; no screenshot acceptance.")
if command("shell", "getprop", "ro.kernel.qemu").stdout.strip() != "1":
    raise SystemExit("Refusing to configure a physical device.")
command("shell", "wm", "size", "1080x2316")
command("shell", "wm", "density", "420")
command("shell", "settings", "put", "system", "font_scale", "1.0")
command("shell", "settings", "put", "system", "time_12_24", "12")
for key in ("window_animation_scale", "transition_animation_scale", "animator_duration_scale"):
    command("shell", "settings", "put", "global", key, "0")
# Use the WindowManager operation instead of injecting a key into a cold launcher.
# AOSP WindowManagerShellCommand documents dismiss-keyguard; this fresh guest has no credential.
command("shell", "wm", "dismiss-keyguard")
command("install", "-r", str(root / "app/build/outputs/apk/debug/app-debug.apk"))
command("install", "-r", str(root / "app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"))
# Establish first-run Android/boot initialization before the strict preservation baseline.
command("shell", "am", "start", "-W", "-n", "org.sleepwell.app/.MainActivity", "--ez", "visualReview", "true")
try:
    broadcast_idle = command("shell", "am", "wait-for-broadcast-idle", check=False, timeout=10).returncode == 0
except subprocess.TimeoutExpired:
    broadcast_idle = False  # System queues can stay busy; the app's exact preservation assertion still applies.
# sys.boot_completed does not prove that a rendered application owns focus.
# Require actual stable MainActivity focus before starting the unchanged journey;
# never dismiss an ANR or relax an app assertion to make startup look successful.
startup_deadline = time.monotonic() + 60
focused_samples = 0
startup_windows = []
while time.monotonic() < startup_deadline:
    windows = command("shell", "dumpsys", "window", timeout=10).stdout
    focus = next((line.strip() for line in windows.splitlines() if "mCurrentFocus=" in line), "No focused window")
    startup_windows.append(focus)
    owned = "org.sleepwell.app" in focus and ".MainActivity" in focus
    focused_samples = focused_samples + 1 if owned else 0
    if focused_samples >= 3:
        break
    time.sleep(1)
else:
    (output / "startup-focus.txt").write_text("\n".join(startup_windows) + "\n", encoding="utf-8")
    raise SystemExit("The CI guest never established stable application focus; no journey was started. See guest/startup diagnostics.")
(output / "startup-focus.txt").write_text("\n".join(startup_windows) + "\n", encoding="utf-8")
metadata = {"commit": subprocess.check_output(["git", "rev-parse", "HEAD"], text=True).strip(), "emulator": True, "scope": args.scope, "audio_imports": args.audio_imports, "import_process_restart": False, "audio": "host output disabled; native sound review allowlisted" if (args.native_sounds or args.audio_imports) else "host output disabled; no native playback output allowlisted; library uses fake playback", "serial": args.serial, "system_broadcast_idle": broadcast_idle}
metadata["startup_focused_samples"] = focused_samples
for prop in ("ro.build.version.sdk", "ro.product.model"):
    metadata[prop] = command("shell", "getprop", prop).stdout.strip()
metadata["size"] = command("shell", "wm", "size").stdout.strip()
metadata["density"] = command("shell", "wm", "density").stdout.strip()
(output / "environment.json").write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")
failures = []
tests = ["SoundSettingsFlowTest", "RootVisualTest", "NightArcFlowTest", "AlarmProtectionFlowTest", "AlarmWakeSettingsTest", "AlarmMenusFlowTest", "AutomaticTrackingFlowTest", "SoundAnalysisNativeTest", "SoundClassifierFixtureEvaluationTest", "SoundEventsUiTest", "SnoreCueSettingsNativeTest", "ReportReferenceFlowTest", "StorageMigrationTest", "SoundLibraryStorageTest", "TravelPlanFlowTest", "PrivacyHistoryFlowTest", "PlanPickerFlowTest", "SoundLibraryUiTest", "SoundRetentionLifecycleTest", "SleepRhythmFlowTest", "NightAudioArchiveTest", "SoundArchiveUiTest", "MotionConfigurationFlowTest", "AudioArchiveConcurrencyTest", "SoundArchiveJobTest", "RecordCalendarFlowTest", "ChallengeRoutineFlowTest"]
if args.native_sounds:
    tests.append("NativeSoundFlowTest")
tests += ["HeadphoneCueOutputTest", "SnoreSoundRouteFlowTest", "WalkConfigurationFlowTest", "WalkFallbackStorageTest", "ChartClockFlowTest", "MissedNightFlowTest", "FavoriteSoundsStorageTest", "FavoriteSoundsFlowTest", "ClipReviewAccessTest", "SoundCategoryFixtureEvaluationTest", "GradedMathConfigurationTest", "QrCodeConfigurationTest", "AlarmToneAssetsTest", "AlarmToneGrantTest", "AlarmToneFlowTest", "WakeCheckFlowTest", "SoundLabelStorageTest", "SoundLabelFlowTest", "NapFlowTest", "NapIntegrationFlowTest", "RecurringTrackingTest", "RecurringTrackingFlowTest", "BackupStorageTest", "BackupDocumentFlowTest", "ReportRibbonNativeTest", "AlarmSkipStorageTest", "AlarmSkipFlowTest"]
tests += list(SENSING_TESTS)
tests += ["SoundCategoryStorageTest", "SoundCategoryFlowTest"]
tests += ["SoundSensitivityStorageTest", "SoundSensitivityFlowTest", "GoalStorageTest", "GoalFlowTest", "DatedGoalBankFlowTest"]
tests += [test for test in PHONE_FEATURE_TESTS if test not in tests]
if args.audio_imports:
    tests += ["SoundImportPickerTest", "SoundImportReopenTest"]
if args.scope == "tone-wake":
    tests = list(FOCUSED_TESTS)
elif args.scope == "report":
    tests = list(REPORT_TESTS)
elif args.scope == "report-flow":
    tests = ["ReportReferenceFlowTest"]
elif args.scope == "organization":
    tests = ["SettingsOrganizationFlowTest"]
elif args.scope == "organized-settings":
    tests = ["OrganizedSettingsJourneyTest"]
elif args.scope == "recorded-analysis":
    tests = ["RecordedAnalysisJourneyTest"]
elif args.scope == "challenge-routine":
    tests = ["ChallengeRoutineFlowTest"]
elif args.scope == "tonight":
    tests = ["NightArcFlowTest"]
elif args.scope == "appearance":
    tests = ["AppearanceFlowTest"]
elif args.scope == "event-library":
    tests = ["SoundLibraryUiTest#recordedIntervalsFavoritesAndDeletionPersistWithFakePlaybackOnly"]
elif args.scope == "goal-plan":
    tests = ["GoalFlowTest"]
elif args.scope == "interaction-security":
    tests = ["InteractionSecurityNativeTest", "CalendarRulesFlowTest#permissionDenialSettingsReturnAndRevocationKeepTheUnsavedDraft"]
elif args.scope == "priority-menus":
    tests = ["AdvancedSnoozeFlowTest#longMaximumAndPerPressChoicePersistCancelAndRecreateWithoutRuntime", "QrCodeConfigurationTest#enrollmentPermissionFallbackRecreationPersistenceAndStrictRestoreRemainInert"]
elif args.scope == "priority-current":
    tests = ["QrCodeConfigurationTest#enrollmentPermissionFallbackRecreationPersistenceAndStrictRestoreRemainInert", "AlarmWakeSettingsTest#actualDraftRecreationSaveReopenAndCancelWithoutOutputs", "SleepRhythmFlowTest#actualSevenNightsWorkdaysPersistRecreateAndEmptyWithoutOutputs", "NapFlowTest#presetsToneQuietPreviewStartRecreateReopenAndCancelPersist", "HealthConnectFlowTest", "PrivacyHistoryFlowTest#confirmedEraseIsRetainedDbFirstAndPendingAudioRetriesWithoutDeletingNewHistory", "BackupDocumentFlowTest#explicitReviewedReplacementPersistsAcrossRotationAndReopen"]
elif args.scope == "priority-repair":
    tests = ["AlarmWakeSettingsTest#actualDraftRecreationSaveReopenAndCancelWithoutOutputs", "AdvancedSnoozeFlowTest#longMaximumAndPerPressChoicePersistCancelAndRecreateWithoutRuntime", "HealthConnectFlowTest", "PrivacyHistoryFlowTest#confirmedEraseIsRetainedDbFirstAndPendingAudioRetriesWithoutDeletingNewHistory"]
elif args.scope == "reference-menus":
    tests = ["AlarmWakeSettingsTest#actualDraftRecreationSaveReopenAndCancelWithoutOutputs", "PrivacyHistoryFlowTest#confirmedEraseIsRetainedDbFirstAndPendingAudioRetriesWithoutDeletingNewHistory", "AlarmToneFlowTest#toneSelectionQuietPreviewImportCancelRecreateAndSavePersistWithoutOutput", "CloudBackupFlowTest#chooseSaveReopenAndReviewKeepLocalDataWithNoUpload", "QrCodeConfigurationTest#enrollmentPermissionFallbackRecreationPersistenceAndStrictRestoreRemainInert"]
elif args.scope == "privacy-recovery":
    tests = ["PrivacyHistoryFlowTest#confirmedEraseIsRetainedDbFirstAndPendingAudioRetriesWithoutDeletingNewHistory"]
elif args.scope == "setup-readiness":
    tests = ["OrganizedSettingsJourneyTest#organizedMenusPreserveDraftsAndSafeChoicesWithoutOutputs", "AlarmProtectionFlowTest#compactListAndGlobalProtectionsPersistWithoutAlarmExecution"]
elif args.scope == "readiness-recovery":
    tests = ["AlarmProtectionFlowTest#compactListAndGlobalProtectionsPersistWithoutAlarmExecution"]
elif args.scope == "sensing-reference":
    tests = ["ContactlessSetupFlowTest#explicitRealDspFakePcmQualificationSaveAndReopenNoAutostart", "SonarNightFlowTest#visibleStartPauseRecreateResumeAndExplicitStopSaveActualNight", "SnoreCueSettingsNativeTest#savedSettingsSurviveRecreationAndControlFakeCueBehavior"]
elif args.scope == "sensing-travel":
    tests = list(SENSING_TESTS)
elif args.scope == "travel-ui":
    tests = list(TRAVEL_UI_TESTS)
elif args.scope == "sonar-night":
    tests = list(NIGHT_TESTS)
elif args.scope == "sound-categories":
    tests = list(CATEGORY_TESTS)
elif args.scope == "sound-sensitivity":
    tests = list(SENSITIVITY_TESTS)
elif args.scope == "phone-features":
    tests = list(PHONE_FEATURE_TESTS)
elif args.scope == "feature-increment":
    tests = list(FEATURE_INCREMENT_TESTS)
elif args.scope == "nap-features":
    tests = list(NAP_FEATURE_TESTS)
metadata["test_classes"] = tests
(output / "environment.json").write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")
method_counts = {"SoundAnalysisNativeTest": 3, "StorageMigrationTest": 4, "SoundLibraryStorageTest": 3, "SoundLibraryUiTest": 2, "NightAudioArchiveTest": 3, "AudioArchiveConcurrencyTest": 3, "SoundArchiveJobTest": 2, "HeadphoneCueOutputTest": 2, "MissedNightFlowTest": 2, "FavoriteSoundsStorageTest": 3, "WakeCheckFlowTest": 2, "SoundLabelStorageTest": 10, "SoundLabelFlowTest": 3, "NapFlowTest": 3, "RecurringTrackingTest": 12, "RecurringTrackingFlowTest": 2, "BackupStorageTest": 6, "BackupDocumentFlowTest": 3, "ReportRibbonNativeTest": 2, "AlarmSkipStorageTest": 8, "AlarmSkipFlowTest": 2, "ContactlessSetupFlowTest": 3, "TravelCueStorageTest": 8, "TravelCueFlowTest": 2, "SonarProvenanceStorageTest": 12}
class_results = []
method_counts.update({"SharedSoundProvenanceStorageTest": 12, "SonarNightStorageTest": 2, "SonarNightFlowTest": 2, "SonarSharedSoundFlowTest": 2, "WalkFallbackStorageTest": 2, "SoundCategoryStorageTest": 2, "SoundCategoryFlowTest": 2, "SoundSensitivityStorageTest": 3, "SoundSensitivityFlowTest": 2, "GoalStorageTest": 10, "DatedGoalBankFlowTest": 2})
method_counts.update({"CalendarRulesStorageTest": 4, "AlarmDefaultsStorageTest": 3, "AlarmDefaultsFlowTest": 2, "AutomationStorageTest": 10})
method_counts.update({"HealthSleepStorageTest": 12, "HealthConnectFlowTest": 2, "AlarmDateStorageTest": 3})
method_counts.update({"SmartLightStorageTest": 6, "HomeAssistantProtocolTest": 4, "AlarmInputsStorageTest": 3, "MoonSearchStorageTest": 3, "TagStorageTest": 8, "CloudBackupStorageTest": 5})
method_counts.update({"AdvancedSnoozeStorageTest": 2})
method_counts.update({"HealthConnectFlowTest": 2})
method_counts.update({"DiaryStorageTest": 10, "TypedWakeFlowTest": 2, "HomeWidgetNativeTest": 7, "AlarmTimingStorageTest": 3})
method_counts.update({"AlarmRequestStorageTest": 3, "ChallengePackNativeTest": 8})
method_counts.update({"TrackingDndStorageTest": 4, "TrackingDndFlowTest": 2})
method_counts.update({"AlarmSoundModeStorageTest": 4, "RecordInsightsNativeTest": 3})
method_counts.update({"TrackingDelayStorageTest": 4, "TrackingDelayFlowTest": 2})
method_counts.update({"BarcodeCodeStorageTest": 3})
method_counts.update({"SpeakCaptureFlowTest": 2, "TrackingFlipStorageTest": 3, "TrackingFlipFlowTest": 2})
method_counts.update({"SunriseStorageTest": 6, "AutoCloudBackupStorageTest": 12, "AutoCloudBackupFlowTest": 2})
method_counts.update({"SoundInclusionStorageTest": 3, "SoundInclusionFlowTest": 3, "PictureNativeTest": 3, "GoalAlarmPlanStorageTest": 3, "GoalAlarmTimeFlowTest": 2})
method_counts.update({"ArmedWaitNativeTest": 3})
method_counts.update({"SnoozeUntilFlowTest": 2})
method_counts.update({"RecordAchievementsStorageTest": 2})
method_counts.update({"PowerNapNativeTest": 3})
method_counts["InteractionSecurityNativeTest"] = 2
for test in tests:
    extra = []
    if test == "SoundImportReopenTest":
        if "SoundImportPickerTest" in failures:
            failures.append(test + " skipped: picker prerequisite failed")
            continue
        command("shell", "am", "force-stop", "org.sleepwell.app")  # This guarded disposable emulator only.
        metadata["import_process_restart"] = True
        (output / "environment.json").write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")
    if test.startswith("SoundImport"):
        extra += ["-e", "ciAudioImports", "true"]
        command("shell", "pm", "grant", "org.sleepwell.app", "android.permission.POST_NOTIFICATIONS")
    if test == "NativeSoundFlowTest":
        command("shell", "pm", "grant", "org.sleepwell.app", "android.permission.POST_NOTIFICATIONS")
        extra = ["-e", "ciSilentPlayback", "true"]
    result = command("shell", "am", "instrument", "-w", "-r", "-e", "class", "org.sleepwell.app." + test, "-e", "ciReview", "true", *extra, "org.sleepwell.app.test/androidx.test.runner.AndroidJUnitRunner", check=False, timeout=180)
    (output / (test + ".txt")).write_text(result.stdout + result.stderr, encoding="utf-8")
    expected_count = method_counts.get(test, 1)
    passed = bool(re.search(r"(?m)^OK \(" + str(expected_count) + r" tests?\)\s*$", result.stdout)) and "FAILURES!!!" not in result.stdout
    class_results.append({"class": test, "expected_methods": expected_count, "passed": passed and not result.returncode})
    print(test + ": " + ("PASS" if passed else "FAIL"), flush=True)
    if result.returncode or not passed:
        failures.append(test)
command("pull", "/sdcard/Android/data/org.sleepwell.app/files/verification/.", str(output), check=False)
if "SonarSharedClassifierStorageTest" in tests:
    try:
        shared = json.loads((output / "sonar-shared-classifier-replay.json").read_text(encoding="utf-8"))
        shared_rows = shared["fixtures"]
        shared_by_name = {row["fixture"]: row for row in shared_rows}
        pinned_shared = {row["name"]: row for row in json.loads((root / "third_party/audio-fixtures/manifest.json").read_text(encoding="utf-8"))["fixtures"]}
        shared_expected = {"snoring-sorts", "snoring-pencil", "speech-countdown"}
        if shared["model"] != "yamnet-mobile-v1-10c95ea3" or len(shared_rows) != 3 or set(shared_by_name) != shared_expected:
            raise ValueError("Shared analysis replay identities differ from pinned whole sources")
        for name, row in shared_by_name.items():
            pcm = (root / "app/src/androidTest/assets/classifier-fixtures" / (name + ".pcm")).read_bytes()
            digest = hashlib.sha256(pcm).hexdigest()
            if len(pcm) % 2 or len(pcm) > 80 * 32000 or row["pcm_sha256"] != digest or pinned_shared[name]["pcm_sha256"] != digest:
                raise ValueError("Shared analysis whole-source hash/bounds mismatch: " + name)
            actual_frames = ((len(pcm) // 2 * 3 - 193) // 3 + 1) // 15600
            if type(row["inferences"]) is not int or row["inferences"] != actual_frames or actual_frames < 1:
                raise ValueError("Shared analysis capture framing mismatch: " + name)
            if any(type(row[key]) is not int or row[key] < 0 for key in ("snoring_events", "speech_events")):
                raise ValueError("Invalid shared persisted category count: " + name)
        if (shared_by_name["speech-countdown"]["snoring_events"] != 0 or shared_by_name["speech-countdown"]["speech_events"] < 1
                or sum(shared_by_name[name]["snoring_events"] for name in ("snoring-sorts", "snoring-pencil")) < 1):
            raise ValueError("Shared real-model replay positive/cross-negative gate failed")
    except (OSError, KeyError, ValueError, TypeError) as error:
        failures.append("Shared PCM replay evidence incomplete: " + str(error))
if args.scope != "full":
    focused_frames = {"readiness-recovery": ["protection-"+name+".png" for name in ("alarms", "editor", "off", "disclosure", "on", "recreated", "global", "power-only", "readiness", "readiness-rationale", "readiness-rationale-cancel", "readiness-settings-recovery", "readiness-returned")], "sensing-reference": ["sonar-setup-"+name+".png" for name in ("initial", "ready", "gain-changed", "retested", "reopened", "recreated")]+["sonar-night-"+name+".png" for name in ("setup", "measuring", "paused", "paused-recreated", "resumed", "missing", "stop-confirmation")]+["snore-settings-"+name+".png" for name in ("default", "preview", "selected", "recreated", "reopened")], "setup-readiness": SETUP_READINESS_FRAMES, "privacy-recovery": ["privacy-history-"+name+".png" for name in ("ready","error","busy","pending","done")], "reference-menus": REFERENCE_MENU_FRAMES, "priority-repair": PRIORITY_REPAIR_FRAMES, "priority-current": PRIORITY_CURRENT_FRAMES, "priority-menus": PRIORITY_MENU_FRAMES, "event-library": EVENT_LIBRARY_FRAMES, "goal-plan": GOAL_PLAN_FRAMES, "interaction-security": INTERACTION_SECURITY_FRAMES, "appearance": APPEARANCE_FRAMES, "tonight": TONIGHT_FRAMES, "challenge-routine": ROUTINE_FRAMES, "travel-ui": TRAVEL_UI_FRAMES, "recorded-analysis": RECORDED_ANALYSIS_FRAMES, "organized-settings": ORGANIZED_SETTINGS_FRAMES, "organization": ['organization-settings-root.png', 'organization-settings-sleep.png', 'organization-search-flashlight.png', 'organization-feedback-off.png', 'organization-alarm-editor.png', 'organization-alarm-schedule.png', 'organization-alarm-options.png', 'organization-controls-recreated.png', 'organization-alarm-backup.png', 'organization-alarm-reopened.png', 'organization-smart-following.png', 'organization-smart-configured.png', 'organization-smart-reopened.png', 'organization-alarm-search.png'], "report-flow": ["report-reference-" + name + ".png" for name in ("history", "week", "week-calendar", "week-selected", "month", "year", "night", "editor", "tags-notes", "reopened", "chart-gap")], "nap-features": NAP_FEATURE_FRAMES, "feature-increment": FEATURE_INCREMENT_FRAMES, "phone-features": PHONE_FEATURE_FRAMES, "sound-categories": CATEGORY_FRAMES, "sound-sensitivity": SENSITIVITY_FRAMES, "sonar-night": NIGHT_FRAMES, "report": REPORT_FRAMES, "sensing-travel": SENSING_FRAMES, "tone-wake": TONE_FRAMES + WAKE_SETTING_FRAMES + WAKE_CHECK_FRAMES}[args.scope]
    failures += [name for name in focused_frames if not (output / name).is_file()]
    summary = {"scope": args.scope, "full_app_review": False, "expected_classes": len(tests), "expected_methods": sum(method_counts.get(test, 1) for test in tests), "class_results": class_results, "required_original_images": len(focused_frames), "failures": failures}
    (output / "focused-review-summary.json").write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")
    if failures:
        raise SystemExit("Focused " + args.scope + " review incomplete (full review not run): " + ", ".join(failures))
    print(f"Focused {args.scope} review passed:{len(tests)} classes/{sum(method_counts.get(test, 1) for test in tests)} methods/{len(focused_frames)} images. Full app review NOT executed.", flush=True)
    raise SystemExit(0)
fixture_report = output / "analysis-human-fixture-evaluation.json"
if not fixture_report.is_file():
    failures.append("Missing real human sound fixture evaluation")
else:
    try:
        fixture_data = json.loads(fixture_report.read_text(encoding="utf-8"))["fixtures"]
        fixtures = {item["fixture"]: item for item in fixture_data}
        expected = {"snoring-sorts": "snoring", "snoring-pencil": "snoring", "speech-crowd": "speech", "speech-countdown": "speech"}
        if len(fixture_data) != len(expected) or set(fixtures) != set(expected) or any(fixtures[name]["expected_broad_class"] != label for name, label in expected.items()):
            failures.append("Real sound fixture identities or expected classes do not match the pinned set")
        elif (sum(fixtures[name]["persisted_snoring_count"] for name in ("snoring-sorts", "snoring-pencil")) < 1
              or fixtures["speech-countdown"]["persisted_speech_count"] < 1
              or any(fixtures[name]["persisted_snoring_count"] != 0 for name in ("speech-crowd", "speech-countdown"))):
            failures.append("Human fixtures did not persist both snoring and speech through the production policy")
    except (KeyError, ValueError, TypeError) as error:
        failures.append("Invalid human sound fixture evaluation: " + str(error))
category_report = output / "analysis-category-fixture-evaluation.json"
try:
    category_rows = json.loads(category_report.read_text(encoding="utf-8"))["fixtures"]
    expected_categories = {"cough-fabrizio": "cough", "cough-crowd": "cough", "babycry-bonehead": "baby_cry", "laughter-crowd": "laughter", "laughter-solo": "laughter", "babycry-ciccarelli": "baby_cry", "snoring-sorts": "snoring", "snoring-pencil": "snoring", "speech-countdown": "speech", "speech-crowd": "speech"}
    by_fixture = {row["fixture"]: row for row in category_rows}
    pinned_rows = json.loads((root / "third_party/audio-fixtures/manifest.json").read_text(encoding="utf-8"))["fixtures"]
    pinned = {row["name"]: row for row in pinned_rows}
    if len(category_rows) != 10 or set(by_fixture) != set(expected_categories) or len(pinned_rows) != 10 or set(pinned) != set(expected_categories):
        raise ValueError("Category fixture identities do not match the pinned ten")
    categories = {"snoring", "speech", "cough", "baby_cry", "laughter", "noise"}
    for name, row in by_fixture.items():
        if row["expected_broad_class"] != expected_categories[name] or row["frames"] <= 0 or row["frames"] != len(row["windows"]):
            raise ValueError("Invalid category fixture framing: " + name)
        source_pcm = (root / "app/src/androidTest/assets/classifier-fixtures" / (name + ".pcm")).read_bytes()
        samples = len(source_pcm) // 2
        digest = hashlib.sha256(source_pcm).hexdigest()
        if (len(source_pcm) % 2 or len(source_pcm) > 3 * 1024 * 1024 or samples > 80 * 16000
                or row["pcm_sha256"] != digest or pinned[name]["pcm_sha256"] != digest
                or row["samples"] != samples or pinned[name]["samples"] != samples
                or row["duration_ms"] != samples * 1000 // 16000 or pinned[name]["duration_ms"] != row["duration_ms"]
                or row["frames"] != samples // 15600):
            raise ValueError("Category PCM hash, bounds or framing mismatch: " + name)
        counts = dict.fromkeys(categories, 0)
        for event in row["persisted_events"]:
            if event["category"] not in categories or event["duration_ms"] < 1950 or event.get("clip") is not None:
                raise ValueError("Invalid analysis-only grouped event: " + name)
            counts[event["category"]] += 1
        if row["counts"] != counts:
            raise ValueError("Stored category counts disagree with reopened events: " + name)
    count = lambda name, category: by_fixture[name]["counts"][category]
    if (count("cough-fabrizio", "cough") + count("cough-crowd", "cough") < 1 or count("babycry-bonehead", "baby_cry") < 1 or count("laughter-solo", "laughter") < 1 or count("babycry-ciccarelli", "baby_cry") < 1
            or count("snoring-sorts", "snoring") + count("snoring-pencil", "snoring") < 1 or count("speech-countdown", "speech") < 1):
        raise ValueError("Production category positives were not all persisted")
    if (any(count("laughter-solo", category) != 0 for category in ("baby_cry", "cough"))
            or any(count("babycry-ciccarelli", category) != 0 for category in ("laughter", "cough"))):
        raise ValueError("Independent solo-source cross-category gate failed")
    if any(count(name, category) != 0 for name in ("snoring-sorts", "snoring-pencil", "speech-countdown", "speech-crowd") for category in ("cough", "baby_cry", "laughter")) or any(count(name, "snoring") != 0 for name in ("cough-fabrizio", "cough-crowd", "babycry-bonehead", "laughter-crowd", "laughter-solo", "babycry-ciccarelli")):
        raise ValueError("Category cross-class negative gate failed")
except (OSError, KeyError, ValueError, TypeError) as error:
    failures.append("Category PCM evidence incomplete: " + str(error))
# Independent consumer check of actual native-exported bytes, never media playback.
archive = output / "library-export-fixture.zip"
try:
    if not archive.is_file() or archive.stat().st_size > 1024 * 1024:
        raise ValueError("Missing or oversized synthetic export fixture")
    with zipfile.ZipFile(archive) as exported:
        manifest_info = exported.getinfo("manifest.json")
        if manifest_info.file_size > 64 * 1024:
            raise ValueError("Oversized fixture manifest")
        manifest = json.loads(exported.read("manifest.json"))
        recordings = manifest["recordings"]
        if (manifest.get("format") != "sleepwell-sound-archive" or manifest.get("version") != 1
                or manifest.get("scope") != "complete_raw_clips" or len(recordings) != 1
                or len(manifest["sound_events"]) != 2):
            raise ValueError("Unexpected synthetic archive metadata")
        names = ["manifest.json"] + [recording["file"] for recording in recordings]
        if set(exported.namelist()) != set(names) or len(exported.namelist()) != len(names):
            raise ValueError("Unexpected or duplicate ZIP entries")
        for recording in recordings:
            if not re.fullmatch(r"recordings/[0-9]{3}\.wav", recording["file"]):
                raise ValueError("Unsafe recording name")
            info = exported.getinfo(recording["file"])
            if info.file_size > 512 * 1024 or info.file_size != recording["bytes"]:
                raise ValueError("Unexpected fixture recording size")
            pcm_wave = exported.read(recording["file"])
            if hashlib.sha256(pcm_wave).hexdigest() != recording["sha256"]:
                raise ValueError("Exported recording hash mismatch")
except (OSError, ValueError, KeyError, TypeError, zipfile.BadZipFile) as error:
    failures.append("Native audio export consumer check: " + str(error))
required = ["sounds-settings-" + name + ".png" for name in ("rain", "ocean", "recreated", "reopened")]
required += ["root-" + name + ".png" for name in ("tonight", "alarms", "report", "settings", "readiness", "sound-analysis", "audio-retention", "bedtime-guard", "travel", "privacy", "wind-down")]
required += ["arc-" + name + ".png" for name in ("upcoming", "help", "start", "middle", "end", "next", "large", "large-action", "weekly")]
required += ["protection-" + name + ".png" for name in ("alarms", "editor", "off", "disclosure", "on", "recreated", "global", "power-only")]
required += SENSING_FRAMES
required += CATEGORY_NEW_FRAMES
required += GOAL_FRAMES + DATED_GOAL_FRAMES + SENSITIVITY_NEW_FRAMES
required += [name for name in PHONE_FEATURE_FRAMES if name not in required and not name.startswith("walk-")]
required += WAKE_SETTING_FRAMES
required += ["menus-" + name + ".png" for name in ("editor", "time", "time-recreated", "time-24", "time-invalid", "large-snooze", "duration-recreated", "tasks", "task-settings", "task-chosen", "snooze", "duration", "snooze-changed", "recreated", "sound", "reopened")]
required += ["automatic-" + name + ".png" for name in ("manual", "stillness", "sounds", "reopened", "error")]
required += ["analysis-events-" + name + ".png" for name in ("all", "speech", "recreated")]
required += ["snore-settings-" + name + ".png" for name in ("default", "selected", "recreated", "reopened", "preview")]
required += ["report-reference-" + name + ".png" for name in ("history", "week", "month", "year", "night", "editor", "tags-notes", "reopened")]
required += ["travel-" + name + ".png" for name in ("plan", "zones", "date", "reminders", "reopened", "error", "disabled")]
required += ["privacy-history-" + name + ".png" for name in ("ready", "busy", "error", "pending", "done")]
required += ["plan-picker-" + name + ".png" for name in ("time", "time-recreated", "duration", "duration-invalid", "duration-recreated", "reminder", "reopened")]
required += ["library-" + name + ".png" for name in ("recordings", "playback", "favorite-recreated", "speech", "recording-deleted", "retention-applied", "retention-recreated")]
required += ["library-retention-" + name + ".png" for name in ("busy-recreated", "error-recreated", "retry")]
required += ["rhythm-" + name + ".png" for name in ("seven-nights", "workdays", "recreated", "reopened", "empty")]
required += ["library-export-" + name + ".png" for name in ("ready", "complete", "recreated", "failed")]
required += ["motion-settings-" + name + ".png" for name in ("ready", "changed", "recreated", "reopened", "conflict", "unavailable", "error")]
required += ["calendar-" + name + ".png" for name in ("history", "history-recreated", "trend", "editor", "editor-recreated", "editor-selected", "reopened")]
required += ["routine-" + name + ".png" for name in ("legacy", "ordered", "reordered", "recreated", "removed", "reopened")]
required += ["snore-route-" + name + ".png" for name in ("unavailable", "bluetooth-unsupported", "supported", "recreated", "reopened")]
required += ["walk-" + name + ".png" for name in ("permission", "ready", "settings", "error", "unavailable", "reopened")]
required += ["chart-clock-" + name + ".png" for name in ("overnight", "night", "short", "fold")]
required += ["missed-night-" + name + ".png" for name in ("denied", "review", "edited", "recreated", "busy", "saved", "covered", "incomplete", "empty", "error", "conflict")]
required += ["favorites-" + name + ".png" for name in ("all", "playback", "recreated", "week", "filter-empty", "speech", "playback-error", "unstarred", "reopened", "deleted-audio", "missing-audio", "empty", "error", "retry")]
required += ["math-config-" + name + ".png" for name in ("default", "operators", "typed", "recreated", "ordered", "reopened", "error")]
required += ["qr-code-" + name + ".png" for name in ("permission", "invalid", "denied", "access-error", "scanning", "camera-error", "captured", "fallback", "fallback-error", "recreated", "routine", "reopened", "unavailable")]
required += TONE_FRAMES
required += WAKE_CHECK_FRAMES
required += ["sound-label-" + name + ".png" for name in ("source", "selector", "draft", "draft-recreated", "cancelled", "corrected", "reopened", "reset", "album", "busy", "busy-recreated", "error", "error-recreated", "retried", "trim-error", "trim-error-recreated", "deleted-error", "deleted-retry")]
required += ["nap-" + name + ".png" for name in ("default", "preset", "tracking-required", "sound", "pending", "recreated", "reopened", "cancelled", "schedule", "schedule-error", "scheduled", "list", "permission-error", "scheduler-error", "retry")]
required += ["nap-integration-" + name + ".png" for name in ("future", "clock-shift", "zone", "wind-down", "inactive", "started", "removed", "invalid")]
required += ["recurring-" + name + ".png" for name in ("off", "draft", "draft-recreated", "time-recreated", "saved", "saved-recreated", "disabled", "permission", "permission-error", "permission-recreated", "notifications-error", "limits", "schedule-error")]
required += ["backup-document-" + name + ".png" for name in ("chooser", "selection-cancelled", "writing", "writing-recreated", "cancelled", "csv-done", "review", "confirmation", "review-recreated", "review-reopened", "restoring", "restoring-recreated", "restored", "result-reopened", "denied", "oversized", "malformed", "stale", "runtime-admission")]
required += ["alarm-skip-" + name + ".png" for name in ("default", "confirmation", "skipped", "undo", "recreated", "reopened", "schedule-error", "error-reopened", "retry", "undo-error", "undo-error-reopened", "undo-retry", "one-time-confirmation", "one-time-skipped", "expired")]
if args.native_sounds:
    required += ["sounds-native-" + name + ".png" for name in ("playing", "paused", "error", "recovered")]
if args.audio_imports:
    required += ["sounds-import-" + name + ".png" for name in ("checking", "controls", "rejected", "rotated", "unknown-size", "selected", "reopened", "playing", "removed")]
failures += [name for name in required if not (output / name).is_file()]
summary = {"scope": args.scope, "full_app_review": True, "expected_classes": len(tests), "expected_methods": sum(method_counts.get(test, 1) for test in tests), "class_results": class_results, "required_original_images": len(required), "available_required_images": sum((output / name).is_file() for name in required), "failures": failures}
(output / "full-review-summary.json").write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")
if failures:
    raise SystemExit("UI verification incomplete: " + ", ".join(failures))
