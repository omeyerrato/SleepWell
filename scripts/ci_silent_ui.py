"""Ephemeral CI emulator only. No alarm, playback, tracking or permission-grant tests.

The Android test runner is invoked directly with two explicit classes. Never use
connectedCheck here, because the suite also contains non-allowlisted tests.
Only isolated emulator fixtures/screenshots are published, never phone backups.
"""
from pathlib import Path
import argparse
import json
import os
import subprocess
import time

parser = argparse.ArgumentParser()
parser.add_argument("--serial", required=True, choices=("emulator-5554",))
args = parser.parse_args()
if os.environ.get("GITHUB_ACTIONS") != "true":
    raise SystemExit("This script is restricted to an ephemeral GitHub Actions emulator.")
root = Path(__file__).resolve().parents[1]
output = root / "captures/ci"
output.mkdir(parents=True, exist_ok=True)
adb = [str(Path(os.environ["ANDROID_HOME"]) / "platform-tools/adb"), "-s", args.serial]


def command(*parts, check=True, timeout=120):
    return subprocess.run(adb + list(parts), check=check, capture_output=True, text=True, timeout=timeout)


deadline = time.monotonic() + 300
while time.monotonic() < deadline:
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
metadata = {"commit": subprocess.check_output(["git", "rev-parse", "HEAD"], text=True).strip(), "emulator": True, "audio": "host output disabled; no playback actions allowlisted", "serial": args.serial}
for prop in ("ro.build.version.sdk", "ro.product.model"):
    metadata[prop] = command("shell", "getprop", prop).stdout.strip()
metadata["size"] = command("shell", "wm", "size").stdout.strip()
metadata["density"] = command("shell", "wm", "density").stdout.strip()
(output / "environment.json").write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")
failures = []
for test in ("SoundSettingsFlowTest", "RootVisualTest"):
    result = command("shell", "am", "instrument", "-w", "-r", "-e", "class", "org.sleepwell.app." + test, "org.sleepwell.app.test/androidx.test.runner.AndroidJUnitRunner", check=False, timeout=180)
    (output / (test + ".txt")).write_text(result.stdout + result.stderr, encoding="utf-8")
    print(test + ": " + ("PASS" if "OK (" in result.stdout else "FAIL"), flush=True)
    if result.returncode or "OK (" not in result.stdout:
        failures.append(test)
command("pull", "/sdcard/Android/data/org.sleepwell.app/files/verification/.", str(output), check=False)
required = ["sounds-settings-" + name + ".png" for name in ("rain", "ocean", "recreated", "reopened")]
failures += [name for name in required if not (output / name).is_file()]
if failures:
    raise SystemExit("UI verification incomplete: " + ", ".join(failures))
