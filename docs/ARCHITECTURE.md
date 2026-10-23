# Architecture

SleepWell is a native Android Java application with a small AndroidX Core dependency. Android framework views keep the initial build inspectable and avoid a web wrapper. Components are deliberately separated by responsibility.

| Area | Implementation | Contract |
| --- | --- | --- |
| Alarms | `Alarm`, `Scheduler`, `AlarmReceiver` | Per-alarm immutable explicit PendingIntents; wake-up time scheduled with AlarmManager independently of tracking. |
| Recovery | `BootReceiver` | Handles protected OS reboot/time/time-zone/package events, reschedules enabled alarms, marks interrupted sessions. Never starts the microphone at boot. |
| Ringing | `AlarmService`, `AlarmActivity` | Foreground media playback, alarm audio attributes, volume ramp, vibration, challenge UI, and optional wake-up check. |
| Night capture | `TrackingActivity`, `TrackingService`, `TrackingClock` | Draft setup and same-session live/pause/resume. Measured 1-second live previews, atomic minute epochs and active duration, persisted gaps. Monotonic total lifetime and CPU wake lock bounded to 12 hours. |
| Audio events | `NoiseRecorder`, `FileSafety` | PCM energy threshold, optional 10-second WAV clips, capped storage, private-file path validation. No ML classification. |
| Behavior rules | `SleepMath` | Pure scheduling, bedtime windows, digital RMS, and smart-window predicates with unit tests. |
| Storage | `Store`, `Backup` | SQLite schema 4 sessions/epochs/clips/alarms/pauses/edit overlays/create tokens, private preferences, nondestructive migration, format 1/2/3 validation and atomic database replacement. |
| Bedtime friction | `BedtimeGuard`, `FocusActivity` | Optional accessibility service receives window-state package names only. No window-content capability. Redirects chosen apps; Settings, calls and emergencies exempted. |
| Screens | `MainActivity`, `Ui`, `GraphView` | Tonight, alarms, history, bedtime, setup. Width capped on large displays; system bar/IME insets handled. Empty graphs are empty rather than fabricated. |

## Measurements and limits

Movement is gravity-filtered accelerometer RMS. This is **phone movement**, not body movement in all placements and not EEG staging. The experimental early-wake predicate needs two consecutive active one-minute buckets, at least 20 sensor samples, and a time inside the configured window. It cannot promise improved alertness.

Noise is normalized digital amplitude; the device microphone is uncalibrated, so the UI does not label it decibels. Noise epochs use sample-weighted RMS across all captured frames. Missing microphone, light or movement samples are stored as nulls and graph gaps, rather than measured zeroes. Audio retention is time- and count-bounded. No speculative snore/cough/REM labels are stored.

Alarms use one-shot OS scheduling even for repeating weekdays. Next occurrences are computed in the current time zone with `java.time`, including DST normalization. The main alarm and snooze/check intents have distinct actions and identities. A single active ringing session owns playback at a time.

## Component security

Activities other than the launcher, the alarm receiver, and services are private. The accessibility service is exported only with Android's `BIND_ACCESSIBILITY_SERVICE` permission. The exported boot receiver accepts only allowlisted protected OS actions. No arbitrary nested intents, public mutation API, or shared database provider exist. PendingIntents are explicit and immutable. File selections use Storage Access Framework URI grants; no broad storage permission is requested.

## Integration boundaries

Future sleep-only Health Connect, ML sound recognition, sonar, smart lights, and external automation belong in separate adapters. They must preserve provenance and missing-data states, use consent-scoped permissions, and never replace the independent fixed alarm with a tracking-only timer.

## Pause and clock provenance

A pause stops sensor listeners, sleep audio and microphone capture; microphone
closing occurs on a worker before storage finalization. Resume closes the same
session's pause interval and starts fresh sensor windows. Active time uses elapsed
realtime; graph timestamps retain the original wall-clock anchor. Actual wall time
still determines alarm calendar windows. Clock changes are flagged in the report.
Process recovery ends an unfinished session at the latest persisted epoch or pause
endpoint and labels it interrupted, rather than pretending the app recorded until
relaunch. Live graph updates preserve the View, selected time and accessibility
focus; saved minute averages can have lower resolution than the live preview.

## Effective records

Captured session/epoch/pause rows remain immutable during editing. `Records`
projects a separate `record_edits` overlay through one interval model shared by
History, Report, CSV and Trends. Tracked edits stay inside original bounds;
manual periods may change dates but carry no measurements. Awake intervals merge
before subtraction, and monotonic active duration bounds available tracked
coverage. An inconsistent legacy duration is disclosed rather than expanded.

Daily totals assign periods by their ending local date and configurable cutoff,
merge overlap (including naps), and exclude unknown dates from means. An overlap
across assigned dates belongs to the earliest ending sleep day. The main bedtime
plot uses the longest non-Nap period of at least three hours and a noon-based
clock axis. Year bars are monthly means of observed daily totals. No stages,
clinical score, SRI or chronotype are inferred by these charts.

Editor saves use optimistic revisions, atomic SQLite writes, persistent create
request tokens and Activity-independent operation ownership. Deleted tokens are
tombstoned so SQLite ID reuse cannot replay another record. Reset-original also
advances the edit revision. Format 3 backups carry edit/source/zone provenance;
local operation tokens are not portable backup data. Restore validates before
replacement, runs transactionally and disables imported alarms.


## Sleep plan and dated goals

`SleepPlan` is the single linked duration/bedtime/wake/wind-down model. Loading
keeps legacy minute precision and mismatched saved times; an explicit adjustment
links the values. The editor discloses a legacy mismatch instead of silently
rewriting it. `SleepPlans.Repository` captures one concrete preferences file,
checks the plan revision, and commits all plan fields together. Reminder failures
are stored separately for retry. `PlanOperations` owns pending writes across
Activity recreation without retaining an Activity. Reminder time calculation
resolves each local bedtime first, then subtracts the advance in elapsed minutes
so DST gaps/folds have explicit, tested semantics.

Commitments freeze metric, target, local dates, 14/30-day length, time zone and
sleep-day cutoff. `SleepDay` resolves cutoff instants consistently through DST.
`GoalProgress` derives closed-day balances from effective records in the frozen
zone: merged duration includes naps; bedtime uses the longest non-nap period of
at least three hours. Missing/current/future days remain unknown. Stopping keeps
history and excludes open days; a future cancellation has an empty counted range.
No financial wagering or clinical debt/onset inference is present. Plans and
commitments are currently local preferences and excluded from portable backups.

`GuardActivity` keeps app selections and schedule enabled state in a cancellable
recreation-safe draft. Save binds preferences before queueing; pause/resume is a
separate change to the active guard's deadline. Accessibility access remains a
real prerequisite and is shown as missing on the test phone. Configuration
persistence tests do not certify package-redirection behavior.

Root navigation now has Tonight, Alarms, Report and Settings per the user's
latest instruction. It supersedes the original three-tab boards. Detail routes
return through both app and system Back callbacks; the newest system-Back fix
still needs connected-device verification after USB loss. The Tonight plan band
uses a single path and an exact trimmed segment, rather than two divergent curves.
Developer stop is gated by the Android debuggable flag; release and debug build
configuration differ, but the alarm control was deliberately not executed.
