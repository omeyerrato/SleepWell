# Contactless recording provenance and portable format 5

This is a storage/model change, following the approved [contactless proposal](CONTACTLESS-SONAR-PROPOSAL.md). It does not start tracking, open microphone/speaker routes, infer sleep stages or validate a real phone's acoustic performance. Runtime, permission/foreground ownership and report rendering are separate integration work.

## Additive database migration

Schema 8 adds only `sessions.sensor TEXT NOT NULL DEFAULT 'unspecified'`, `sessions.sensor_config TEXT NOT NULL DEFAULT ''`, and nullable `epochs.signal_quality TEXT`. Existing session/source/origin/time/measurement/edit/audio-label rows are not rewritten. Old data remains honestly unspecified; migration does not label historical readings as sonar or assume an old sensor. History deletion already covers the sessions/epochs columns; no new hidden table is introduced.

New ordinary `Store.begin()` retains wall-time creation/recovery behavior and records `accelerometer`. New user-reported entries record `none`. `Store.begin(String sensor,String sensorConfig)` accepts explicit accelerometer/empty config or `sonar_fmcw_v1`/strict immutable metadata, validates before recovery/mutation, and uses the actual creation wall time. It cannot set a fabricated historical start. Invalid admission does not close an existing recording.

`SensorProvenance.sonar(SonarConfig,String actualCapture)` emits version 1, algorithm `fmcw_echo_change_v1`, 48000 Hz, 18500–20000 Hz, 1920-sample chirp, finite requested digital gain .01–.10 and actual `UNPROCESSED` or `VOICE_RECOGNITION` capture source. Unknown/extra fields, numeric strings, fractional fixed integers and invalid gains fail. This is small bounded metadata, never a path, URL, output command, measured dose or clinical claim.

## Ordered checkpoint contract

`Store.sonarCheckpoint(session,from,to,movement,quality,samples,active)` is called on the runtime's ordered storage worker. `from/to` are paired-clock wall-epoch bounds, not relative DSP indices. Each interval is positive and at most five seconds; `quality` is an exact `SonarFrame.Quality.name()`. A VALID measured value is finite 0–4 with at least one actual sample. Every other quality requires movement NULL, including calibration, silence/clipping/no echo and route/timing/charging loss. A genuine valid stationary value may be zero.

The same transaction checks that the tracked sonar session is still open, validates its immutable config, checks raw bounds and monotonic active progress, rejects overlap/crossing a pause, and inserts the actual quality interval plus active checkpoint atomically. The first interval starts at session creation. Subsequent intervals are contiguous, except for a fully recorded closed pause; missing signal must be an explicit null-quality interval rather than an omitted span or fabricated zero. Active time cannot exceed the recording's anchored wall interval or 36-hour raw limit. Samples are bounded 0–10000 per interval.

Legacy `checkpoint`/`epoch` behavior is unchanged. Recovery already uses the last epoch endpoint even when movement is NULL, so a final timing-loss/null interval retains the actual observed recording endpoint. Quality is not a sleep stage or a successful acoustic-calibration assertion.

## Portable data and model

Format 5 validates/exports/restores sensor/config and nullable epoch quality, preserving raw epoch IDs, intervals, measured zeros, null gaps and edit overlays. Configuration, numeric types/ranges, session linkage, quality/metric agreement, contiguous signal coverage and pause boundaries are checked before replacement. The exact whole-document 8 MiB UTF-8 limit remains; oversized evidence fails rather than being dropped. Existing file tombstones and guard checks stay in the replacement transaction, and no WAV bytes are unlinked by `restoreData`.

Formats 1–4 keep their old acceptance/restoration behavior. They cannot smuggle newer metadata into an active sensor: new fields are ignored, tracked/legacy sensor defaults to unspecified, and known manual records default to none. Format 5 imports remain closed historical recordings with imported origin and disabled alarms; no import starts capture or output. Sound-label/favorite metadata remains analysis-only under the existing format-4 contract.

`RecordEntry.sensor`, `sensorConfig` and `sonar()` make provenance available to actual report consumers. Editing a projection preserves its snapshot. Records' display JSON and appended CSV sensor/config columns retain it; existing constructors remain compatible. Graph thresholds and stages must not reuse accelerometer inference for the relative echo metric. Consumers making their own `RecordEntry` copies must pass the sensor fields too.

## Evidence at this commit

- Four pure `RecordSensorTest` projection/copy/default tests pass through direct javac/JUnit and Gradle, 0 failures/errors.
- `SonarProvenanceStorageTest` contains twelve isolated native methods: real schema-7 fixture migration with every old table's typed values compared, new provenance, failed/checkpoint rollback, wrong/stale owner/gap/quality rejection, pause/resume, format-5 roundtrip/edits, formats 1–4/defaults, invalid-metadata replacement rejection, late guarded restore rollback/WAV preservation, invalid admission, unfinished-import/re-export stability and final-null recovery. No hardware/scheduling/service call is present.
- Existing migration/backup fixture assertions only advance exact schema/format numbers; their preservation checks remain intact.
- Scoped Gradle session 46175 passes (5m32s): debug/native-test APKs, four pure tests and compatible lint (0 errors / 89 warnings; no warning points to the owned storage/model/codec changes). Strict source review cleared the additive schema/format scope after an unfinished-input recovery-end validation fix; a dedicated native regression covers reject-without-mutation and accepted→restore→re-export stability.
- Native execution of all twelve new SQLite methods and runtime integration remain pending. This source does not prove physical echo sensing, overnight reliability, UI appearance or medical accuracy. No phone/device outputs ran.
