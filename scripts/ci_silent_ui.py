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
metadata = {"commit": subprocess.check_output(["git", "rev-parse", "HEAD"], text=True).strip(), "emulator": True, "audio_imports": args.audio_imports, "import_process_restart": False, "audio": "host output disabled; native sound review allowlisted" if (args.native_sounds or args.audio_imports) else "host output disabled; no playback actions allowlisted", "serial": args.serial, "system_broadcast_idle": broadcast_idle}
for prop in ("ro.build.version.sdk", "ro.product.model"):
    metadata[prop] = command("shell", "getprop", prop).stdout.strip()
metadata["size"] = command("shell", "wm", "size").stdout.strip()
metadata["density"] = command("shell", "wm", "density").stdout.strip()
(output / "environment.json").write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")
failures = []
tests = ["SoundSettingsFlowTest", "RootVisualTest", "NightArcFlowTest", "AlarmProtectionFlowTest", "AlarmWakeSettingsTest", "AlarmMenusFlowTest"]
if args.native_sounds:
    tests.append("NativeSoundFlowTest")
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
    passed = bool(re.search(r"(?m)^OK \(1 test\)\s*$", result.stdout)) and "FAILURES!!!" not in result.stdout
    print(test + ": " + ("PASS" if passed else "FAIL"), flush=True)
    if result.returncode or not passed:
        failures.append(test)
command("pull", "/sdcard/Android/data/org.sleepwell.app/files/verification/.", str(output), check=False)
required = ["sounds-settings-" + name + ".png" for name in ("rain", "ocean", "recreated", "reopened")]
required += ["root-" + name + ".png" for name in ("tonight", "alarms", "report", "settings", "readiness", "sound-analysis", "bedtime-guard", "privacy", "wind-down")]
required += ["arc-" + name + ".png" for name in ("upcoming", "help", "start", "middle", "end", "next", "large", "large-action", "weekly")]
required += ["protection-" + name + ".png" for name in ("alarms", "editor", "off", "disclosure", "on", "recreated", "global", "power-only")]
required += ["wake-settings-" + name + ".png" for name in ("sound", "loud-curve", "delay-options", "lights", "recreated", "reopened", "normal-curve", "edited-curve", "immediate")]
required += ["menus-" + name + ".png" for name in ("editor", "time", "time-recreated", "time-24", "time-invalid", "large-snooze", "duration-recreated", "tasks", "task-settings", "task-chosen", "snooze", "duration", "snooze-changed", "recreated", "sound", "reopened")]
if args.native_sounds:
    required += ["sounds-native-" + name + ".png" for name in ("playing", "paused", "error", "recovered")]
if args.audio_imports:
    required += ["sounds-import-" + name + ".png" for name in ("checking", "controls", "rejected", "rotated", "unknown-size", "selected", "reopened", "playing", "removed")]
failures += [name for name in required if not (output / name).is_file()]
if failures:
    raise SystemExit("UI verification incomplete: " + ", ".join(failures))
