"""Run allowlisted silent UI verification without Gradle's device uninstaller.

Build with assembleDebug/assembleDebugAndroidTest first. This script never runs
the alarm test, starts tracking, uses the microphone, changes screen power,
uninstalls packages or clears app data. Backups remain private and gitignored.
"""
from pathlib import Path
import argparse
import datetime
import subprocess
import sys

parser = argparse.ArgumentParser()
parser.add_argument("--adb", required=True)
parser.add_argument("--serial", required=True)
parser.add_argument("--flow", choices=("roots", "appearance"), default="roots")
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
adb = [args.adb, "-s", args.serial]


def command(*parts, **kwargs):
    return subprocess.run(adb+list(parts), check=True, **kwargs)


backup = root / ".device-backups" / datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
backup.mkdir(parents=True, exist_ok=False)
with (backup / "private-app-data.tar").open("wb") as output:
    command("exec-out", "run-as", "org.sleepwell.app", "tar", "-cf", "-", ".", stdout=output)
print("Private app data backed up locally; never add .device-backups to Git.")
command("install", "-r", str(root/"app/build/outputs/apk/debug/app-debug.apk"))
command("install", "-r", str(root/"app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"))
test_class = "RootVisualTest" if args.flow == "roots" else "AppearanceFlowTest"
result = command("shell", "am", "instrument", "-w", "-r", "-e", "class",
                 "org.sleepwell.app."+test_class,
                 "org.sleepwell.app.test/androidx.test.runner.AndroidJUnitRunner",
                 capture_output=True, text=True, encoding="utf-8", errors="replace")
(backup/(args.flow+"-test-result.txt")).write_text(result.stdout+result.stderr, encoding="utf-8")
print(result.stdout)
capture = root / "captures" / args.flow
capture.mkdir(parents=True, exist_ok=True)
names = ("automatic", "selected-0", "selected-1", "selected-2", "tonight-0", "tonight-1", "tonight-2", "large-contrast-tonight", "large-contrast-tonight-action", "large-contrast") if args.flow=="appearance" else ("tonight", "report", "settings", "report-selected")
for name in names:
    prefix="appearance" if args.flow=="appearance" else "root"
    subprocess.run(adb+["pull", f"/sdcard/Android/data/org.sleepwell.app/files/verification/{prefix}-{name}.png", str(capture/f"{name}.png")], check=False)
# Launch outside the instrumentation lifecycle so its teardown cannot finish the
# review window and drop KEEP_SCREEN_ON while the user is unavailable.
command("shell", "am", "start", "-n", "org.sleepwell.app/.MainActivity", "--ez", "visualReview", "true")
if "OK (" not in result.stdout:
    sys.exit(args.flow+" test failed; inspect its result, do not claim acceptance.")
