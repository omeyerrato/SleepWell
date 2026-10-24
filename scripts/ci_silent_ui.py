"""Ephemeral CI emulator only. No alarm, microphone or tracking tests.

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
parser.add_argument("--native-sounds", action="store_true", help="Real media service tests on this no-host-audio CI emulator only")
parser.add_argument("--audio-imports", action="store_true", help="Actual document picker and imported playback on this isolated emulator only")
args = parser.parse_args()
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
command("shell", "input", "keyevent", "82")  # The fresh emulator has no credential lock.
command("install", "-r", str(root / "app/build/outputs/apk/debug/app-debug.apk"))
command("install", "-r", str(root / "app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"))
# Establish first-run Android/boot initialization before the strict preservation baseline.
command("shell", "am", "start", "-W", "-n", "org.sleepwell.app/.MainActivity", "--ez", "visualReview", "true")
try:
    broadcast_idle = command("shell", "am", "wait-for-broadcast-idle", check=False, timeout=10).returncode == 0
except subprocess.TimeoutExpired:
    broadcast_idle = False  # System queues can stay busy; the app's exact preservation assertion still applies.
metadata = {"commit": subprocess.check_output(["git", "rev-parse", "HEAD"], text=True).strip(), "emulator": True, "audio_imports": args.audio_imports, "import_process_restart": False, "audio": "host output disabled; native sound review allowlisted" if (args.native_sounds or args.audio_imports) else "host output disabled; no native playback output allowlisted; library uses fake playback", "serial": args.serial, "system_broadcast_idle": broadcast_idle}
for prop in ("ro.build.version.sdk", "ro.product.model"):
    metadata[prop] = command("shell", "getprop", prop).stdout.strip()
metadata["size"] = command("shell", "wm", "size").stdout.strip()
metadata["density"] = command("shell", "wm", "density").stdout.strip()
(output / "environment.json").write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")
failures = []
tests = ["SoundSettingsFlowTest", "RootVisualTest", "NightArcFlowTest", "AlarmProtectionFlowTest", "AlarmWakeSettingsTest", "AlarmMenusFlowTest", "AutomaticTrackingFlowTest", "SoundAnalysisNativeTest", "SoundClassifierFixtureEvaluationTest", "SoundEventsUiTest", "SnoreCueSettingsNativeTest", "ReportReferenceFlowTest", "StorageMigrationTest", "SoundLibraryStorageTest", "TravelPlanFlowTest", "PrivacyHistoryFlowTest", "PlanPickerFlowTest", "SoundLibraryUiTest", "SoundRetentionLifecycleTest", "SleepRhythmFlowTest", "NightAudioArchiveTest", "SoundArchiveUiTest", "MotionConfigurationFlowTest", "AudioArchiveConcurrencyTest", "SoundArchiveJobTest", "RecordCalendarFlowTest", "ChallengeRoutineFlowTest"]
if args.native_sounds:
    tests.append("NativeSoundFlowTest")
tests += ["HeadphoneCueOutputTest", "SnoreSoundRouteFlowTest", "WalkConfigurationFlowTest", "ChartClockFlowTest", "MissedNightFlowTest", "FavoriteSoundsStorageTest", "FavoriteSoundsFlowTest", "ClipReviewAccessTest", "SoundCategoryFixtureEvaluationTest", "GradedMathConfigurationTest", "QrCodeConfigurationTest", "AlarmToneAssetsTest", "AlarmToneGrantTest", "AlarmToneFlowTest", "WakeCheckFlowTest"]
if args.audio_imports:
    tests += ["SoundImportPickerTest", "SoundImportReopenTest"]
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
    expected_count = {"SoundAnalysisNativeTest": 3, "StorageMigrationTest": 4, "SoundLibraryStorageTest": 3, "SoundLibraryUiTest": 2, "NightAudioArchiveTest": 3, "AudioArchiveConcurrencyTest": 3, "SoundArchiveJobTest": 2, "HeadphoneCueOutputTest": 2, "MissedNightFlowTest": 2, "FavoriteSoundsStorageTest": 3, "WakeCheckFlowTest": 2}.get(test, 1)
    passed = bool(re.search(r"(?m)^OK \(" + str(expected_count) + r" tests?\)\s*$", result.stdout)) and "FAILURES!!!" not in result.stdout
    print(test + ": " + ("PASS" if passed else "FAIL"), flush=True)
    if result.returncode or not passed:
        failures.append(test)
command("pull", "/sdcard/Android/data/org.sleepwell.app/files/verification/.", str(output), check=False)
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
required += ["wake-settings-" + name + ".png" for name in ("sound", "loud-curve", "delay-options", "vibration-options", "lights", "recreated", "reopened", "normal-curve", "edited-curve", "immediate")]
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
required += ["alarm-tone-" + name + ".png" for name in ("builtins", "playing", "paused", "preview-error", "options", "selected", "import-checking", "recreated", "import-cancelled", "decoder-busy", "decoder-recovered", "import-empty", "import-error", "imported", "reopened", "cancelled")]
required += ["wake-check-" + name + ".png" for name in ("waiting", "prompt", "countdown", "recreated", "confirmed", "reopened", "storage-error", "access-error", "delivery-error", "retry", "expired", "stale")]
if args.native_sounds:
    required += ["sounds-native-" + name + ".png" for name in ("playing", "paused", "error", "recovered")]
if args.audio_imports:
    required += ["sounds-import-" + name + ".png" for name in ("checking", "controls", "rejected", "rotated", "unknown-size", "selected", "reopened", "playing", "removed")]
failures += [name for name in required if not (output / name).is_file()]
if failures:
    raise SystemExit("UI verification incomplete: " + ", ".join(failures))
