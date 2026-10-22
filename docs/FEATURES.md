# Behavioral feature audit and implementation map

For the current phone-only scope and screen/code/test links, use
[FEATURE-MATRIX.md](FEATURE-MATRIX.md). The wearable roadmap below is historical
and excluded from the current build. Existing implementation descriptions are
narrow capability descriptions, not final design/behavior acceptance.

Audit date: 2026-10-22. References: [Sleep as Android manual](https://sleep.urbandroid.org/docs/) and [Challenges Alarm Clock publisher listing](https://play.google.com/store/apps/details?id=com.garageapp.alarmchallenges). [REFERENCE_INDEX.md](REFERENCE_INDEX.md) inventories all 129 linked documentation pages, including device, API and troubleshooting guides.

This is an independent implementation plan. **Implemented** means code exists, not that overnight reliability or medical validity has been established. **Partial** means the narrower behavior described here exists. **Pending** is absent, not a hidden premium feature. **Out of scope** means we will not copy a vendor's assets, hosted service, hardware, or impossible OS guarantees. Every capability we implement will be available without a paid tier.

## Alarms and waking up

| Feature | How the referenced behavior works | SleepWell status / implementation |
| --- | --- | --- |
| Multiple alarms | Independent times, labels and enabled states | Implemented: SQLite alarm configurations, distinct OS PendingIntents. |
| Repeating weekday alarms | Compute the next selected weekday | Implemented: `SleepMath.nextOccurrence`, current time zone, DST normalization, rescheduled protected OS events. |
| Per-alarm configuration | Override wake window, sound, tasks and snoozing | Implemented: each alarm persists its own settings. Global reusable defaults pending. |
| Smart wake-up | Look for activity during a window before the fixed deadline | Partial/experimental: two consecutive movement-active minutes, at least 20 samples, configured window; fixed OS alarm remains independent. No measured sleep stage. |
| Do not wake before duration goal | Suppress early triggering until the goal has elapsed | Implemented for recorded session duration; fixed alarm is unaffected. |
| Movement sensitivity | Tune activity threshold for early wake | Partial: conservative fixed 0.12 m/s² threshold; user calibration pending. |
| Backup alarm | Escalate if the primary alarm is ignored | Implemented: separately scheduled escalation to Pulse at maximum alarm-stream volume after a configurable delay. Quiet test alarms do not escalate. |
| Gentle volume ramp | Start quietly and increase over a chosen duration | Implemented: configurable 0–120 seconds, bounded by device hardware. |
| Extra-loud mode | Override a low alarm-stream setting | Implemented: configurable 10–100% of hardware alarm volume, restoration after stop where applicable. No claim of exceeding hardware limits. |
| Vibration | Haptic alarm pattern | Implemented: repeating phone waveform; per-alarm toggle. Wearable haptics pending. |
| Built-in sounds | Bundled wake sounds | Partial: original synthesized Daybreak and Pulse, not copied commercial sound packs. |
| Device ringtone | Use the system alarm sound | Implemented: Android default alarm URI. Full ringtone library picker pending. |
| Local songs | Use chosen audio via persistent URI | Implemented: document-picker audio grant; original-tone fallback on failure. |
| Playlists / shuffle / silent alarm | Multiple tracks or haptics only | Pending. |
| Internet radio / Spotify | Stream audio with platform fallback | Pending: requires network controls/provider access; subscriptions to third-party music cannot be made free by this project. |
| Sunrise alarm | Gradually brighten screen or connected lights | Pending. |
| Snooze duration and count | Delay the active alarm within configured limits | Implemented: 1–15 minutes, 0–5 snoozes; count persists per occurrence. |
| Decreasing snooze / total snooze cap | Shorten repeated delays or cap accumulated minutes | Pending; current count×duration is bounded. |
| Post-dismiss wake check | Ring again unless the user confirms being awake | Implemented: optional 1–15 minute check; confirmation on Tonight screen. |
| Challenge difficulty / rounds | Require repeated successful tasks | Implemented: 1–6 rounds; shaking uses 5×rounds motion events. |
| Math | Solve arithmetic before dismissal | Implemented: randomized addition, numeric answer validation. More operators/difficulties pending. |
| Retype | Enter a displayed code | Implemented: randomized morning code, exact trimmed comparison. |
| Memory pairs | Find matching cards | Implemented: randomized three-pair board, delayed mismatch concealment. |
| Sequence | Repeat an observed ordered pattern | Implemented: randomized numeric sequence, length increases over rounds. |
| Shake | Require deliberate motion | Implemented: accelerometer magnitude plus debounce. |
| NFC | Walk to and scan an enrolled tag | Implemented: foreground NFC reader; only matching enrolled tag completes the task. |
| QR/barcode sequences | Scan configured codes in order | Pending: camera permission, scanner, and enrollment workflow. |
| Object/picture task | Recognize a chosen object using camera inference | Pending: local model, object constraints, false-match evaluation. |
| Smile task | Require a camera-detected smiling face | Pending: face model, probability threshold, temporal verification. |
| Pose task | Match body landmarks to a target pose | Pending: pose model, confidence and accessibility fallback. |
| Other CAPTCHA pack tasks | Additional counting, movement and game tasks | Pending: independent implementations, not vendor add-on reuse. |
| Prevent leaving the alarm | Accessibility redirects navigation to an active challenge | Partial: voluntary accessibility return and optional Android screen pinning. Calls, Settings and emergencies remain accessible. |
| Prevent uninstall / shutdown | Device-admin or accessibility friction against bypass | Out of scope as an absolute guarantee. Ordinary apps cannot defeat force-stop, hardware shutdown, recovery, or OS controls. No coercive uninstall trap is implemented. |
| Lock-screen/full-screen alarm | A high-priority alarm notification opens a screen over keyguard | Implemented with permission readiness checks. Android may choose heads-up presentation when already unlocked. |
| Nap timer | Ring after a short rest | Partial: duration converted to next wall-clock minute, not a second-accurate monotonic countdown. |
| Power nap | Minimum duration followed by an extended activity-based wake window | Pending. |
| Polyphasic routine | Multiple short sleeps with per-nap reminders/goals | Partial: multiple independent alarms; specialized goal/reminder profiles pending. |
| Voice time entry / alternate pickers | Enter alarm times through several input methods | Partial: accessible Android time picker; speech and relative entry pending. |
| Alarm-only layout / skip occurrence / holiday skipping | Hide tracking or suppress one scheduled instance | Pending. |

## Bedtime and tracking

| Feature | How the referenced behavior works | SleepWell status / implementation |
| --- | --- | --- |
| Bedtime reminder | Notify before rest is due | Implemented: user-chosen local bedtime notification. |
| Smart bedtime | Derive reminder from next alarm, goal and accumulated deficit | Pending; current bedtime is explicit rather than silently inferred. |
| Anti-scrolling guard | Redirect selected apps during a wind-down interval | Implemented: selected packages, cross-midnight window, five-minute pause, optional accessibility grant. This extends the motivating use case. |
| Manual sleep tracking | User starts/stops a foreground recording session | Implemented: ongoing notification with stop action, 12-hour bounded run. |
| Automatic tracking | Detect likely sleep or start before a wake window | Pending: separate activity-recognition/bedtime triggers and modern Android background-microphone handling. No microphone capture from boot. |
| Missing-night estimation | Infer likely sleep from phone inactivity | Pending; missing nights are not fabricated. |
| Accelerometer tracking | Read motion and aggregate into a night graph | Implemented: gravity filtering and one-minute RMS epochs. Placement strongly affects results. |
| Contactless sonar | Emit an ultrasonic signal and process reflected microphone audio | Pending: signal generation, echo analysis, device calibration and external validation. An accelerometer is not a substitute for bedside sonar. |
| PIR tracking | Read movement from a compatible bedside sensor | Pending: device-specific adapter and hardware tests. |
| Sleep-phase estimation | Infer activity-derived rest states | Pending: current UI intentionally shows measured movement rather than fabricated REM/deep/light stages. Clinical stages require independent validation. |
| Awake detection | Combine activity, phone interaction, light and body sensors | Partial: movement and light recording only; a tested wake classifier is pending. |
| Light levels | Log ambient lux | Implemented when the phone has a light sensor. No sensor means no genuine measurement. |
| Breathing rate | Extract respiration periodicity from sonar/PIR/body sensors | Pending. Not inferred from low-rate phone movement. |
| SpO₂ | Read a supported oximeter/wearable | Pending: hardware + consent-scoped adapter. A phone microphone cannot measure oxygen saturation. |
| Heart rate / HRV | Receive pulse and inter-beat data from wearables | Pending: hardware/Health Connect adapters; no invented vital signs. |
| Noise amplitude | Analyze microphone PCM volume | Implemented: sample-weighted epoch RMS amplitude, missing-signal gaps, optional microphone foreground service. It is not calibrated dB. |
| Triggered sound recordings | Save clips only around qualifying events | Implemented: optional 10-second WAV clips at energy threshold; at most 60/session with a 30-second cooldown. Pre-roll pending. |
| Automatic sound labels | CNN recognizes snoring, speech, cough, laughter and baby cries | Pending: properly licensed local classifier and labeled validation corpus. Current clips are labeled only as loud sounds. |
| Noise threshold | Control recording sensitivity | Implemented: relative amplitude threshold; independent per-class sensitivity pending. |
| Noise library | Browse/play clips | Partial: playback within each night; favorites and global filtering pending. |
| Retention / favorite album / alternate formats | Delete old clips; preserve favorites; choose encodings | Partial: seven-day deletion at the next clip-saving session start and WAV format. Favorites, manual audio export and other encodings pending. |
| Anti-snoring cues | Trigger mild audio/haptic/light response after detected snoring | Pending: requires a validated snore classifier first. Generic loud sounds do not trigger snoring treatment. |
| Pair tracking | Compare synchronized movement streams to reduce partner crossover | Pending: peer pairing, clock alignment, filtering evaluation. |
| Sleep sounds / lullabies | Loop relaxing audio, fade/stop by timer or sleep detection | Partial: original rain/brown-noise loops, low player volume, fixed stop timer. Adaptive fade, guided tracks and playlists pending. |
| Binaural beats / meditation | Generate stereo tones or play licensed narration | Pending. |
| Lucid dreaming | Use subtle cues during an inferred dream window | Pending: no REM-trigger claims without a validated estimator. |
| Jet-lag light intervention | Plan timed light exposure by destination/time zone | Pending: evidence-based scheduling, compatible light output, validation. |
| DND / airplane / screen dimming | Adjust the night environment with user controls | Pending: no automatic global device-setting changes. |
| Tracking pause / standby / battery-adaptive sampling | Temporarily suspend or reduce collection | Partial: same-session pause/resume now preserves explicit gaps and excludes paused duration. Standby and battery-adaptive sampling remain pending; see current device evidence in VERIFICATION.md. |
| Sensor diagnostics | Check placement, availability and signal | Partial: movement sample count, latest amplitude, setup diagnostics; full live calibration pending. |

## Records, statistics and presentation

| Feature | How the referenced behavior works | SleepWell status / implementation |
| --- | --- | --- |
| Night graphs | Plot time-stamped sensor and derived metrics | Partial: real movement/noise/light plots; missing samples remain absent. No hypnogram or vital overlays. |
| History | Browse individual nights | Implemented: recent 60 sessions, interrupted-state display, recorded time, detail screen. Pagination pending. |
| Manual journal / tags / ratings | Add context and subjective quality | Implemented: saved notes, removable structured tags, and explicit unrated/0–5 rested rating in the native editor. Tag quantities, bulk taxonomy and automatic context pending. |
| Graph editing / manual records | Correct start/end and missed nights | Implemented: manual periods, tracked trims, merged awake edits, tags, notes, ratings, restore-original and selected-record deletion. Original measurements are immutable; manual periods never receive sensor graphs. |
| Duration goal | Choose target rest duration | Partial: recorded-duration goal, not an assertion of actual sleep duration. |
| Multi-metric sleep score | Combine duration, continuity, stages, regularity and body measures | Pending: no composite score manufactured from unavailable sensors. |
| Statistics | Aggregate nights across a time period | Partial: calendar Week/Month/Year charts, date/search/context filters and configurable day cutoff. Daily coverage merges overlaps, missing dates stay unknown, and naps count toward duration. Advanced statistics and correlations remain pending. |
| Duration/deficit/regularity charts | Aggregate nightly durations and timing differences | Pending; unknown nights must remain unknown rather than counted as zero sleep. |
| Chronotype / social jetlag / SRI | Analyze timing over sufficient workday/free-day history | Pending: requires enough genuine observations, configurable week boundaries and circular-time calculations. |
| Tag impact analysis | Compare groups of nights by habit | Pending: minimum sample counts and observational-language constraints. |
| Personalized advice | Compare recent patterns with a longer baseline | Pending: no medical advice generated from a single short session. |
| Achievements | Recognize personal routine milestones | Pending. |
| Goal bank / wagers | Track target surplus over a commitment period | Pending for a free habit bank; financial wagering is not part of this app. |
| Dashboard / tabs | Organize alarms, recordings and readiness | Partial: five original screens, dark palette, large-text-friendly scrolling, wide-screen width cap. User-arranged cards and themes pending. |
| Global search | Search alarms, records, settings and help | Pending. |
| Home widgets / shortcuts | Start a night or see the next alarm from launcher | Pending. |
| Localization / accessibility | Localized strings, reader support, adaptive layouts | Partial: semantic platform controls and content descriptions. Full localization, large-font/RTL/device testing pending. |

## Devices and external services

| Feature | Expected mechanics / dependencies | SleepWell status |
| --- | --- | --- |
| Wear OS / One UI Watch | Separate watch app, motion/HR permission, data transport, haptics | Pending; a Samsung phone connection alone does not supply watch data. |
| Garmin | Connect IQ companion and vendor constraints | Pending; cannot reuse a proprietary app/API entitlement. |
| Xiaomi / Mi Band / Amazfit | Supported companion bridge, event and sensor transport | Pending. |
| Pebble | Watch companion and supported communication bridge | Pending. |
| Legacy Tizen Gear | Legacy platform/distribution constraints | Pending investigation; deprecated vendor platforms may be unavailable. |
| Bluetooth oximeters / chest sensors | Device protocols, BLE consent and drop-out handling | Pending. |
| Sleep Phaser PIR lamp | Device-specific movement/light protocol and physical hardware | Pending; hardware itself is not replicated by an app. |
| Sleep masks | Wired or Bluetooth light-control adapter | Pending. |
| Hue / IKEA smart lights | User-authorized local bridge API, pairing and sunrise schedule | Pending. |
| Wearable connection test | Signal inspection and actuator test per adapter | Pending. |
| Health Connect | User-selected sleep/vital read/write scopes, provenance and deduplication | Pending. |
| Google Fit / Samsung Health | Platform-supported health data path | Pending; prefer current consent-based Health Connect where available. |
| Local data backup | Export records/configuration and restore transactionally | Partial: versioned JSON alarms/sessions/epochs/pause/edit provenance (format 3, formats 1/2 readable) and CSV export. JSON excludes preferences, NFC IDs and audio. Imported alarms start disabled. |
| Cloud backup / SleepCloud | Authenticated storage, encryption and sync conflict handling | Pending for user-controlled storage. Vendor SleepCloud hosting is not reproduced or included. |
| Calendar | Read consent, holiday/meeting rules, optionally write sessions | Pending. |
| Tasker / Automate / Intent API | Explicit opt-in event/command contracts with sender controls | Pending; current components are private rather than accepting untrusted commands. |
| Custom webhooks | Selected events delivered to a user-configured endpoint | Pending: network/privacy UI, retry limits, secure URL handling. |
| MQTT | Authenticated broker and event topics | Pending. |
| IFTTT | User-owned integration endpoint/account | Pending; third-party platform costs cannot be waived by this app. |
| Assistant voice actions | Supported app shortcuts or alarm intents | Pending. |
| Content provider / CSV compatibility / wearable API | Versioned external contracts with access controls | Partial: documented project JSON/CSV, no public provider. Vendor data-format import pending. |
| Community research sharing | Explicit opt-in dataset donation and hosted analysis | Out of scope for the first release; no telemetry is collected. |

## Delivery priorities

1. **Alarm reliability:** real-device screen-off/Doze/reboot/snooze tests, overlapping alarm policy, missed-alarm diagnostics, accessibility edge cases, and several overnight trials.
2. **Get out of bed:** QR/barcode enrollment, richer math difficulty, camera challenges with local inference and accessibility alternatives.
3. **Better measured nights:** continuous epoch audio summaries, missing-sensor flags, wake estimation, graph edits, tag filters, trends, regularity and goal bank.
4. **Sound understanding:** bundle a suitably licensed audio model, evaluate snore/talk/cough classes, expose uncertainty, then consider anti-snoring cues.
5. **Wearables and health:** choose target hardware, implement companion adapters, Health Connect provenance/deduplication, and test disconnect behavior.
6. **Contactless/integrations:** validated sonar prototype, optional smart lights, and user-controlled automation/cloud adapters.

There is no feature-paywall milestone. The limiting factors are implementation, evidence and hardware access.
