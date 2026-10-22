"""Rebuild the scoped feature inventory from the prior audit plus missing manual subfeatures.

Statuses are implementation assessments; test source never implies a passing result.
"""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
SOURCE = "https://sleep.urbandroid.org/docs/"
GROUPS = {
    "Alarms": ("04–12,26", "alarms/alarm_settings.html", ["Alarm.java", "Scheduler.java", "AlarmService.java", "AlarmActivity.java", "AlarmEditorActivity.java"]),
    "Bedtime": ("13–21,27–30", "sleep/sleep_tracking.html", ["TrackingService.java", "NoiseRecorder.java", "BedtimeGuard.java", "MainActivity.java"]),
    "Records": ("02,22–25", "sleep/statistics.html", ["Store.java", "GraphView.java", "MainActivity.java"]),
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
    ("Pause/resume within one tracked session", "18", "sleep/sleep_tracking.html", "missing", "Must preserve explicit gaps; stopping/starting separate sessions is insufficient."),
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
    ("Sleep-day cutoff / naps / date filtering", "22,23", "sleep/statistics.html", "missing", "Recent-session list and mean do not provide complete local-day aggregation."),
    ("Tag quantities / rename / delete / impact", "22–25", "sleep/tags.html", "partial", "Free-form hashtags exist; structured quantities, edits and minimum-sample analysis absent."),
    ("Awake edits / trim / manual record / provenance", "24", "sleep/graph_edit.html", "missing", "Original measurements must remain distinguishable from edited periods."),
    ("Dated goal bank", "15,23", "sleep/goals.html", "missing", "Goal currently affects early wake; no commitment periods or surplus/deficit bank."),
    ("SRI / circular midpoint clustering", "25", "sleep/chrono_jetlag.html", "missing", "Needs coverage thresholds and missing intervals, not treating unknown days as no sleep."),
    ("Own Intent and data-provider contracts", "33", "devs/intent_api.html", "missing", "Versioned contracts and explicit sender access controls required."),
    ("Calendar holiday skip / meeting shift / record write", "33 + calendar setup", "services/google_calendar.html", "missing", "Needs calendar consent, provider availability and deterministic conflicts."),
    ("Health Connect sleep provenance / dedup / deletion", "32", "services/health_connect.html", "missing", "Sleep-only consent and read/write behavior; hardware vitals excluded."),
    ("User-controlled cloud / conflict / audio backup", "35", "services/backup_data.html", "missing", "No cloud transport; external user-owned storage/provider prerequisite."),
    ("Tasker events / webhook retries / MQTT transport", "33", "services/automation.html", "missing", "Needs opted-in transport, credentials, event schemas and actual failure tests."),
    ("Appearance options alter behavior", "36", "ux/personalize.html", "missing", "Inter/plum/cream/dawn components exist; saved text-size, contrast and time-of-day controls absent."),
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
    notes = notes.replace(" Wearable haptics pending.", "").replace("five original screens, dark palette", "three root destinations with supporting details, Changing light palette")
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

for n, row in enumerate(rows, 1):
    row["id"] = f"F{n:03}"

header = """# Phone feature matrix

Reviewed 2026-10-22 against the supplied brief, all 12 visually inspected boards,
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
are stated in the last column. Most features still need device-level verification.

| ID / feature and manual | Screen(s) | Code | Validation | Status and remaining work |
| --- | --- | --- | --- | --- |
"""
for row in rows:
    code = ", ".join(f"[{name[:-5]}](../app/src/main/java/org/sleepwell/app/{name})" for name in row["implementation"]) or "—"
    notes = row["notes"].replace("|", "/")
    header += f"| {row['id']} [{row['feature']}]({row['source']}) | {row['screens']} | {code} | {row['validation']} | **{row['status']}** — {notes} |\n"
(ROOT / "docs/FEATURE-MATRIX.md").write_text(header, encoding="utf-8")
(ROOT / "docs/feature-matrix.json").write_text(json.dumps(dict(reviewed="2026-10-22", features=rows), indent=2, ensure_ascii=False)+"\n", encoding="utf-8")
print(f"Persisted {len(rows)} phone-capable feature entries")
