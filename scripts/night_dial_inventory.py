"""Account for every manifest Activity in the Night Dial flow contract.

This is a route inventory, not a claim that a screenshot or a passing root test
accepts a complete flow. New destinations must be assigned deliberately.
"""
from pathlib import Path
import json
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
FLOWS = [
    (1, "First night", "Main"),
    (2, "Plan tonight", "Main Plan SmartBedtime"),
    (3, "Wind down and guard", "Main Guard Focus GuidedWindDown"),
    (4, "Create and manage alarms", "Main AlarmEditor AlarmDefaults"),
    (5, "Dates and smart waking", "AlarmRepeat AlarmExcludedDates NextAlarmOverride RelativeAlarmTime GoalAlarmTime SunriseTime CalendarRules"),
    (6, "Tone and gentle wake", "AlarmEditor"),
    (7, "Snooze, backup and input", "AlarmEditor WakeCheck"),
    (8, "Build a wake routine", "AlarmEditor RandomChallenge RandomChallengePreview ChallengePractice"),
    (9, "Enroll morning codes", "Main QrCode GeneratedMorningQr"),
    (10, "Screen challenges I", "Alarm ChallengePractice RandomChallengePreview"),
    (11, "Screen challenges II", "ChallengePackPreview MoonSearchPreview ChallengePractice Alarm"),
    (12, "Writing and light tasks", "TypedWakePreview ChallengePractice ChallengePackPreview"),
    (13, "Movement challenges", "MotionChallengePractice ChallengePackPreview ChallengePractice Alarm"),
    (14, "Camera challenges", "ObjectRecognition PictureMatch Pose Smile RoutineCameraPractice"),
    (15, "Sound challenges", "SpeakChallenge PhraseChallenge NoteChallenge AudioChallengePreview RoutineAudioPractice"),
    (16, "Notes, camera light and fallback", "TypedWakePreview CameraLight Alarm ChallengePractice"),
    (17, "Alarm rings to dismissal", "Alarm WakeCheck"),
    (18, "Choose sounds and settle in", "SleepSounds Tracking GuidedWindDown NarrationVoice"),
    (19, "Advanced audio sources", "SleepSounds BinauralSettings ExternalMediaFade"),
    (20, "Start, pause and finish tracking", "Tracking"),
    (21, "Automatic tracking", "AutomaticTracking RecurringTracking TrackingDelay TrackingFlip TrackingDnd"),
    (22, "Sensors and calibration", "MotionSensor MotionCalibration PairMotion PairMotionSaved ContactlessSetup EchoSettings EchoPeriods"),
    (23, "Room, awake and experimental signals", "RoomConditions AwakeSettings AwakeReview PirSettings PirReport"),
    (24, "Sound analysis and recordings", "SoundEvents FavoriteSounds SoundLabel SoundArchive RecordedRecognition SoundSensitivity SoundLibrarySettings SnoreSoundRoute"),
    (25, "Gentle cues and dream tools", "DreamCue DreamCueHistory SnoringCues"),
    (26, "Review a recorded night", "RecordReport RecordEditor RecordSignals RecordInsights RecordedNightScore HealthSleepSource SleepCsvSource AwakeReview"),
    (27, "History, manual records and import", "History SleepCsv DiaryImport MissedNight RecordEditor Diary"),
    (28, "Trends, rhythm and context", "RecordReport SleepRhythm RhythmGroups HabitualTiming SRI TripSleep TagManagement"),
    (29, "Sleep plan, bank and milestones", "GoalBank RecordAchievements Plan"),
    (30, "Naps and timers", "Nap NapProfiles NapProfilesDocuments TimerRequest"),
    (31, "Travel and calendar schedules", "Travel TravelCueReview CalendarRules AlarmBedtime TripSleep"),
    (32, "Find and personalize", "Appearance FeedbackSettings TonightLayout Search"),
    (33, "Reliability and alarm protection", "Main AlarmProtection UninstallProtection"),
    (34, "Health and calendar exchange", "HealthConnect HealthSleepSource HealthSleepPrivacy CalendarEventExport CalendarEventDirect AutoCalendar"),
    (35, "Lights and automation", "SmartLight HueBridge Automation IncomingAutomationSettings IncomingAutomation"),
    (36, "Local data, export and restore", "Backup ResearchExport LibraryAudioImport LibraryAudioExport AacExport SoundArchive DiaryImport"),
    (37, "Cloud versions, profiles and conflicts", "CloudBackup CloudAudio SettingsProfile"),
    (38, "Imported signals, sharing and access", "RecordAccess AlarmAccess SleepSummary AlarmRequest HealthSleepPrivacy"),
    (39, "Home shortcuts and system handoffs", "HomeLaunch HomeWidgetSettings TimerRequest AlarmRequest"),
    (40, "Recovery and details across flows", "Main AlarmEditor Tracking Backup"),
    (41, "Tags, confirmation and custom values", "TagManagement RecordEditor Diary"),
    (42, "Physical challenge and permission branches", "RoutineCameraPractice RoutineAudioPractice MotionChallengePractice QrCode ObjectRecognition PictureMatch Pose Smile CameraLight NoteChallenge SpeakChallenge PhraseChallenge Main"),
    (43, "Empty, search and loading states", "Main Search History RecordReport Diary SoundEvents FavoriteSounds GoalBank"),
]

def inventory():
    ns = "{http://schemas.android.com/apk/res/android}"
    manifest = ET.parse(ROOT / "app/src/main/AndroidManifest.xml")
    names = sorted(node.get(ns + "name").rsplit(".", 1)[-1] for node in manifest.findall(".//activity"))
    flows = [{"board": n, "flow": title, "activities": [name + "Activity" for name in members.split()]} for n, title, members in FLOWS]
    routes = [{"activity": name, "boards": [flow["board"] for flow in flows if name in flow["activities"]]} for name in names]
    missing = [route["activity"] for route in routes if not route["boards"]]
    if missing:
        raise SystemExit("Unassigned native destinations: " + ", ".join(missing))
    return {"activity_count": len(routes), "flow_count": len(flows), "full_acceptance": False, "meaning": "Route accounting only; see revision-specific native evidence and PROGRESS.md for acceptance.", "flows": flows, "routes": routes}

if __name__ == "__main__":
    data = inventory()
    out = ROOT / "docs/night-dial/SCREEN-COVERAGE.json"
    out.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
    lines = ["# Native destination coverage", "", f"All {data['activity_count']} manifest Activities are assigned to the {data['flow_count']} flow families. This accounts for routes; it does not establish complete visual, accessibility or hardware acceptance. Native compositions, shared sheets and helper-owned task surfaces are tracked in PROGRESS.md and revision-specific evidence.", "", "Regenerate with `python scripts/night_dial_inventory.py`. The command fails when a new manifest destination has no assigned flow.", "", "| Native destination | Flow boards |", "| --- | --- |"]
    for route in data["routes"]:
        name = route["activity"]
        lines.append(f"| [{name}](../../app/src/main/java/org/sleepwell/app/{name}.java) | " + ", ".join(f"{n:02d}" for n in route["boards"]) + " |")
    (out.parent / "SCREEN-COVERAGE.md").write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Accounted for {data['activity_count']} native destinations in {data['flow_count']} flow families.")
