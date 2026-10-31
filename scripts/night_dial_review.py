"""Native design evidence on a disposable CI emulator. Never addresses a personal device."""
from pathlib import Path
import atexit
import json
import os
import subprocess
import shutil
import xml.etree.ElementTree as ET
import time

if os.environ.get("GITHUB_ACTIONS") != "true":
    raise SystemExit("Restricted to a disposable GitHub Actions emulator.")
root = Path(__file__).resolve().parents[1]
output = root / "captures/night-dial"
output.mkdir(parents=True, exist_ok=True)
unit_results = output / "unit-results"
unit_results.mkdir(exist_ok=True)
unit_totals = {"tests": 0, "failures": 0, "errors": 0}
for report in (root / "app/build/test-results/testDebugUnitTest").glob("TEST-*.xml"):
    shutil.copy2(report, unit_results / report.name)
    suite = ET.parse(report).getroot()
    for key in unit_totals:
        unit_totals[key] += int(suite.attrib.get(key, 0))
sdk = Path(os.environ["ANDROID_HOME"])
adb = [str(sdk / "platform-tools/adb"), "-s", "emulator-5554"]
log = (output / "emulator.log").open("w")
process = subprocess.Popen([str(sdk / "emulator/emulator"), "-avd", "NightDialReview", "-port", "5554", "-no-window", "-no-audio", "-no-boot-anim", "-no-snapshot", "-gpu", "swiftshader", "-feature", "-Vulkan", "-memory", "2048", "-cores", "2", "-timezone", "Asia/Riyadh"], stdout=log, stderr=subprocess.STDOUT)

def command(*arguments, timeout=60, check=True):
    return subprocess.run(adb + list(arguments), capture_output=True, text=True, timeout=timeout, check=check)

def close():
    if process.poll() is None:
        for filename, arguments in (("logcat.txt", ("logcat", "-d", "-t", "2500")), ("windows.txt", ("shell", "dumpsys", "window"))):
            try:
                result = command(*arguments, timeout=15, check=False)
                (output / filename).write_text(result.stdout + result.stderr, encoding="utf-8")
            except Exception as failure:
                (output / filename).write_text(str(failure), encoding="utf-8")
        process.terminate()
        try:
            process.wait(timeout=15)
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait(timeout=5)
    log.close()

atexit.register(close)
deadline = time.monotonic() + 300
while time.monotonic() < deadline:
    if process.poll() is not None:
        raise SystemExit("Renderer exited before boot; inspect emulator.log")
    result = command("shell", "getprop", "sys.boot_completed", timeout=10, check=False)
    if result.returncode == 0 and result.stdout.strip() == "1":
        break
    time.sleep(2)
else:
    raise SystemExit("Renderer boot timed out; no native evidence.")
if command("shell", "getprop", "ro.kernel.qemu").stdout.strip() != "1":
    raise SystemExit("Refusing to configure a physical device.")
command("shell", "wm", "size", "1080x2316")
command("shell", "wm", "density", "420")
command("shell", "settings", "put", "system", "time_12_24", "12")
command("shell", "wm", "dismiss-keyguard")
command("install", "-r", str(root / "app/build/outputs/apk/debug/app-debug.apk"), timeout=120)
command("install", "-r", str(root / "app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"), timeout=120)
command("shell", "am", "start", "-W", "-n", "org.sleepwell.app/.MainActivity", "--ez", "visualReview", "true")
deadline = time.monotonic() + 60
while time.monotonic() < deadline:
    if "org.sleepwell.app/.MainActivity" in command("shell", "dumpsys", "window", timeout=10).stdout:
        break
    time.sleep(1)
else:
    raise SystemExit("The native application never established a window.")
try:
    result = command("shell", "am", "instrument", "-w", "-r", "-e", "class", "org.sleepwell.app.NightDialReviewTest,org.sleepwell.app.NightDialInteractionTest,org.sleepwell.app.NightDialTrackingReviewTest,org.sleepwell.app.NightDialReportFlowTest,org.sleepwell.app.NightDialAlarmFlowTest,org.sleepwell.app.AlarmSkipFlowTest,org.sleepwell.app.NightDialFirstNightTest,org.sleepwell.app.NightDialBreadthTest", "-e", "ciReview", "true", "org.sleepwell.app.test/androidx.test.runner.AndroidJUnitRunner", timeout=1200, check=False)
except subprocess.TimeoutExpired as expired:
    partial = expired.stdout or ""
    if isinstance(partial, bytes):
        partial = partial.decode("utf-8", errors="replace")
    result = subprocess.CompletedProcess([], 124, partial, "Native review exceeded its deadline; partial captures are retained.\n")
(output / "instrumentation.txt").write_text(result.stdout + result.stderr, encoding="utf-8")
print(result.stdout)
if result.stderr:
    print(result.stderr)
command("pull", "/sdcard/Android/data/org.sleepwell.app/files/verification/.", str(output), check=False)
required = ["tonight", "dial-help", "alarms", "settings", "sounds", "time", "time-invalid", "time-restored", "dial-under-finger", "dial-draft", "dial-review-restored", "dial-saved", "dial-conflict", "settle-in", "settle-timer", "settle-microphone-consent", "settle-consent-restored", "settle-sensors", "dial-linked-review", "dial-linked-alarm", "tracking-active", "tracking-end-confirmation", "tracking-end-restored", "tracking-paused", "tracking-paused-restored", "tracking-resumed", "report-night", "report-signal-scrub", "report-signal-restored", "report-history-imported", "report-dirty-back", "report-dirty-restored", "report-restore-review", "report-restore-review-restored", "report-restore-draft", "report-restored-night", "report-trends-week", "report-trends-month", "report-trends-year", "report-trends-empty", "report-trends-empty-actions", "report-trends-restored", "alarm-list", "alarm-editor", "alarm-name-open", "alarm-name-draft", "alarm-name-restored", "alarm-repeat-invalid", "alarm-repeat-restored", "alarm-repeat-interval", "alarm-repeat-parent", "alarm-discard", "alarm-discard-restored", "alarm-routine", "alarm-task-search", "alarm-task-config", "alarm-routine-ordered", "alarm-ramp-picker", "alarm-gentle", "alarm-gentle-restored", "alarm-snooze", "alarm-snooze-restored", "alarm-snooze-off", "alarm-saved", "alarm-skip-review", "alarm-skip-restored", "alarm-skip-applied", "alarm-skip-undone", "alarm-skip-changed", "alarm-skip-changed-restored", "alarm-duplicate-draft", "alarm-duplicate-restored", "alarm-unsaved-actions", "alarm-delete-review", "alarm-delete-restored", "alarm-delete-changed", "alarm-delete-result", "alarm-dirty-saved", "first-welcome", "first-welcome-restored", "first-returning", "first-empty-alarms", "first-new-from-defaults", "first-saved-off", "first-existing-user", "first-readiness", "first-notifications-explanation", "first-notifications-restored", "first-notifications-denied", "first-denied-restored", "first-notifications-returned", "first-regular-readiness", "first-microphone-explanation", "first-microphone-restored", "first-welcome-back", "first-guide-from-settings", "first-guide-parent-return"]
missing = [name for name in required if not (output / f"night-dial-{name}.png").is_file()]
required_alarm_skip = ["default", "confirmation", "skipped", "undo", "recreated", "reopened", "schedule-error", "error-reopened", "retry", "undo-error", "undo-error-reopened", "undo-retry", "one-time-confirmation", "one-time-skipped", "expired"]
missing.extend(f"alarm-skip-{name}" for name in required_alarm_skip if not (output / f"alarm-skip-{name}.png").is_file())
breadth = ["Nap", "GuidedWindDown", "Diary", "Appearance", "Guard", "AutomaticTracking", "RecurringTracking", "SleepRhythm", "GoalBank", "Travel", "CalendarRules", "SmartBedtime", "SmartLight", "HueBridge", "Automation", "HealthConnect", "Backup", "CloudBackup", "SettingsProfile", "Search", "SoundLibrarySettings", "FavoriteSounds", "SleepSounds", "DreamCue", "SnoringCues", "TagManagement", "HomeWidgetSettings", "AlarmProtection", "FeedbackSettings", "AwakeSettings", "SoundSensitivity", "TrackingDelay", "TrackingFlip", "TrackingDnd", "ExternalMediaFade", "BinauralSettings", "SleepCsv", "ResearchExport", "RecordAccess", "AlarmAccess", "IncomingAutomationSettings", "NarrationVoice", "SRI", "NapProfiles", "HabitualTiming", "NapCancelled", "NapRestored", "DiaryDiscard", "DiaryDraft", "DiaryDiscarded"]
tasks = ["Math","Memory","Sequence","Retype","Shake","Walk","NFC","QR code","Visual search","Counting","Tilt maze","Flags","Mirror text","Dream journal","Speak","Laughter","Loud sound","Picture match","Jumping","Smile","Pose","Light","Type code","Camera light","Spin around","Musical note","Object recognition","Spoken phrase"]
missing.extend("breadth-"+name for name in breadth if not (output / f"night-dial-breadth-{name}.png").is_file())
missing.extend("task-"+name for name in tasks if not (output / ("night-dial-breadth-Task-"+name.replace(" ","-")+".png")).is_file())
workspace = ["Math","Memory","Sequence","Retype","Counting","Flags","Mirror text","Dream journal","Visual search","Jumping","Type code"]
missing.extend("workspace-"+name for name in workspace if not (output / ("night-dial-breadth-Workspace-"+name.replace(" ","-")+".png")).is_file())
missing.extend("large-"+name for name in ["Appearance","Nap","GuidedWindDown","Backup","AutomaticTracking"] if not (output / ("night-dial-breadth-Large-"+name+".png")).is_file())
passed = result.returncode == 0 and "OK (20 tests)" in result.stdout and not missing
(output / "review.json").write_text(json.dumps({"commit": subprocess.check_output(["git", "rev-parse", "HEAD"], text=True).strip(), "scope": "45 secondary root destinations, all 28 task configuration entry contracts, 11 actual cognitive workspaces, larger-text/reduced-motion roots, nap draft pointer cancellation/accessibility/recreation and diary dirty Back/recreation/discard; roots, picker validation, real dial gestures/save/cancel/conflict/recreation, silent Settle in and consent cancel; synthetic PCM tracking, pause/recreate/resume/end; recorded-night navigation, signal scrub/recreation, source filters, dirty/restore drafts and canonical week/month/year durations; alarm names/repeat/dirty gate recreation, routine search/config/order, requested output curve and snooze policy drafts with explicit save; skip review/recreation/cancel/undo/stale occurrence, off duplicate draft and named deletion recreation/cancel/stale identity; existing recurring skip, failed-scheduling/retry/reopen and one-time/expired regression journeys; fresh launcher welcome/explore/recreation and existing-user bypass, new alarm defaults/save/discard, actual notification denial/settings return and optional microphone rationale restoration", "unit_results": unit_totals, "full_acceptance": False, "passed": passed, "missing_frames": missing, "output": "No hardware capture, playback or OS alarms; tracking uses injected PCM; host audio disabled"}, indent=2), encoding="utf-8")
if not passed:
    raise SystemExit("Native design review failed; inspect instrumentation and captured frames.")
