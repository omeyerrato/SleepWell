"""Run allowlisted silent UI verification without Gradle's device uninstaller.

Build with assembleDebug/assembleDebugAndroidTest first. This script never runs
the alarm test, uses the microphone, changes screen power,
uninstalls packages or clears app data. The tracking flow records only movement,
asserts all existing alarms are already disabled, and restores its test preferences
and removes only its own short session. Backups remain private and gitignored.
"""
from pathlib import Path
import argparse
import datetime
import subprocess
import sys

parser = argparse.ArgumentParser()
parser.add_argument("--adb", required=True)
parser.add_argument("--serial", required=True)
parser.add_argument("--flow", choices=("roots", "appearance", "charts", "tracking", "history", "recreation"), default="roots")
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
flows = {
    "roots": ("RootVisualTest", "root", ("tonight", "report", "settings", "report-selected")),
    "appearance": ("AppearanceFlowTest", "appearance", ("automatic", "selected-0", "selected-1", "selected-2", "tonight-0", "tonight-1", "tonight-2", "large-contrast-tonight", "large-contrast-tonight-action", "large-contrast")),
    "charts": ("ChartVisualTest", "chart", ("dense", "selected", "empty")),
    "recreation": ("RecordRecreationTest", "recreation", ()),
    "history": ("HistoryFlowTest", "history", ("empty", "editor", "reopened", "manual-signals", "history", "search", "filter", "trends-week", "trends-month", "trends-year", "tracked-editor", "tracked-saved", "short-editor")),
    "tracking": ("TrackingFlowTest", "tracking", ("setup", "collecting", "live", "paused", "resumed", "report", "reopened")),
}
try:
    command("install", "-r", str(root/"app/build/outputs/apk/debug/app-debug.apk"))
    command("install", "-r", str(root/"app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"))
    test_class, prefix, names = flows[args.flow]
    capture = root / "captures" / args.flow
    capture.mkdir(parents=True, exist_ok=True)
    for name in names:
        (capture/f"{name}.png").unlink(missing_ok=True)
    if names: command("shell", "rm", "-f", *[f"/sdcard/Android/data/org.sleepwell.app/files/verification/{prefix}-{name}.png" for name in names])
    result = command("shell", "am", "instrument", "-w", "-r", "-e", "class",
                     "org.sleepwell.app."+test_class,
                     "org.sleepwell.app.test/androidx.test.runner.AndroidJUnitRunner",
                     capture_output=True, text=True, encoding="utf-8", errors="replace")
    (backup/(args.flow+"-test-result.txt")).write_text(result.stdout+result.stderr, encoding="utf-8")
    print(result.stdout)
    missing = []
    for name in names:
        destination = capture/f"{name}.png"
        destination.unlink(missing_ok=True)  # Never leave a stale image looking like fresh evidence.
        pulled = subprocess.run(adb+["pull", f"/sdcard/Android/data/org.sleepwell.app/files/verification/{prefix}-{name}.png", str(destination)], check=False)
        if pulled.returncode and not (args.flow=="roots" and name=="report-selected"):
            missing.append(name)
    if "OK (" not in result.stdout:
        sys.exit(args.flow+" test failed; inspect its result, do not claim acceptance.")
    if missing:
        raise RuntimeError("Required captures missing: "+", ".join(missing))
finally:
    # The test runner finishes activities. Restore the review window even on failure.
    subprocess.run(adb+["shell", "am", "start", "-n", "org.sleepwell.app/.MainActivity", "--ez", "visualReview", "true"], check=False)
