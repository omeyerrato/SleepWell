"""Native design evidence on a disposable CI emulator. Never addresses a personal device."""
from pathlib import Path
import atexit
import json
import os
import subprocess
import time

if os.environ.get("GITHUB_ACTIONS") != "true":
    raise SystemExit("Restricted to a disposable GitHub Actions emulator.")
root = Path(__file__).resolve().parents[1]
output = root / "captures/night-dial"
output.mkdir(parents=True, exist_ok=True)
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
result = command("shell", "am", "instrument", "-w", "-r", "-e", "class", "org.sleepwell.app.NightDialReviewTest,org.sleepwell.app.NightDialInteractionTest", "-e", "ciReview", "true", "org.sleepwell.app.test/androidx.test.runner.AndroidJUnitRunner", timeout=420, check=False)
(output / "instrumentation.txt").write_text(result.stdout + result.stderr, encoding="utf-8")
print(result.stdout)
if result.stderr:
    print(result.stderr)
command("pull", "/sdcard/Android/data/org.sleepwell.app/files/verification/.", str(output), check=False)
required = ["tonight", "dial-help", "alarms", "settings", "sounds", "time", "time-invalid", "time-restored", "dial-draft", "dial-review-restored", "dial-saved", "dial-conflict", "settle-in", "settle-timer", "settle-microphone-consent", "settle-sensors"]
missing = [name for name in required if not (output / f"night-dial-{name}.png").is_file()]
passed = result.returncode == 0 and "OK (2 tests)" in result.stdout and not missing
(output / "review.json").write_text(json.dumps({"commit": subprocess.check_output(["git", "rev-parse", "HEAD"], text=True).strip(), "scope": "roots, picker validation, real dial gestures/save/cancel/conflict/recreation, silent Settle in and consent cancel", "full_acceptance": False, "passed": passed, "missing_frames": missing, "output": "No alarms, audio or sensors started; host audio disabled"}, indent=2), encoding="utf-8")
if not passed:
    raise SystemExit("Native design review failed; inspect instrumentation and captured frames.")
