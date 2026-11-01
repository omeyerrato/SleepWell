# Experimental automatic additional sounds

Source implementation, 1 November 2026. Runtime acceptance remains pending the
combined Android build and the separately owned silent native journey. This is
an operational experimental feature, not the reliable-recognition checklist gate.

`AdditionalSoundSettings` adds one default-off, device-local future-night option.
Saving it never enables the microphone, recording, base sound categories, cues,
or automations. The next actual night enrolls a separate derived-analysis receipt.
When the existing microphone/sound-analysis owner is off or unavailable, the
receipt reports unavailable; the extra option does not request another capture.

The motion path copies already-admitted 16 kHz PCM before the existing YAMNet
call. The contactless path uses the existing causal 48-to-16 kHz FIR output; it
does not open another microphone or reconfigure the speaker. Cue-masked samples
and real gaps do not become supplementary context. Existing YAMNet categories,
scores, grouping, sound events, snoring cues, and automation dispatch are unchanged.

The additional worker uses the existing bundled PANNs/EfficientAT weights, exact
Java frontends, notices, frozen family maps and `RecordedSoundPolicy`. It never
downloads weights, creates raw audio files, uploads audio, retrains, or changes
thresholds. Full nonoverlapping ten-second real PCM windows are analyzed. Partial
tails are not padded or analyzed. Candidates and uncertainty are stored separately
from `sound_events` and cannot trigger any output or automation.

There is one background worker, at most one executing ten-second window and one
queued window. A partial builder can hold a third ten-second buffer: the PCM
ceiling is 960,000 bytes plus small metadata, not a total model/process-memory
claim. Enqueuing copies existing capture samples; it never waits for inference,
SQLite, or native cleanup. Queue overflow disables further supplementary inference
for that night, retires queued/in-flight context, and reports captured-but-not-
analyzed coverage. Base capture continues. A ten-second-or-longer inference also
disables further supplementary work. No p95 or Android RAM acceptance is claimed
from this guard or from source inspection.

Once inference is disabled, that same worker immediately closes and releases both
models and their shared lease after any native call returns. It continues only
bounded coverage/status accounting until the recording ends. Failure of the
already-admitted contactless PCM pipeline likewise disables extra inference.

Each night pins its storage factory identity, canonical database path, process
history epoch, raw session start, and random durable receipt token. Each capture
segment has its own nonce and atomic revision. Gap, pause, stop and capture failure
invalidate unfinished windows without waiting for SQLite/native work. The atomic
revision check is the acceptance point for a completed result: an already-accepted
historical result may finish its durable write after a pause, but a result whose
inference completes after retirement is discarded. Durable writes recheck the
exact receipt and raw session inside the transaction. Deleting/replacing a session
removes the receipt, so late work cannot attach to a reused ID even if its raw
timestamps match. Erase/restore also changes the existing process history epoch.
The existing `RecordedSoundAnalyzer` lease remains held until native work returns
and both sessions close, including cancellation. A new saved-analysis request
cannot overlap that cleanup; its existing error/retry route remains in charge.

Store version 19 adds only `additional_sound_nights`,
`additional_sound_windows`, and `additional_sound_session_deleted`. Versions
17 and 18 upgrade through their existing additive migrations before these objects
are created. Existing records, alarms, clips, annotations, and interrupted recovery
receipts are not rewritten. The DELETE trigger clears only these derived rows.
There are at most 4,320 retained result windows per night.

Portable backup remains version 14. The new derived tables are **not exported**,
and the new device-local consent is absent from the settings-profile allowlist.
A restored/imported recording therefore truthfully reports that local derived
results are unavailable; no raw audio or user annotations are lost by that choice.
The new results route is read-only. Ordinary session deletion/history replacement
clears the derived data through the session trigger.

`AdditionalSoundStore.read` returns consent, status, issue, source, model/policy,
coverage and candidate counts. `page` accepts a pinned Snapshot and returns up to
20 immutable-window metadata objects in order, with the original family estimates.
`capturedMs` is offered real PCM, `analyzedMs` is committed full windows, and
`skippedMs` is their difference. While active/paused the last quantity includes
waiting/partial windows; after termination it is captured but not analyzed.
An unfinished receipt on an ended night is reported as interrupted. No receipt
means not requested or unavailable after portable restore, never zero detections.

Window diagnostics retain inference wall milliseconds and Java used heap sampled
after inference. That heap value is neither native memory nor the model's added
RAM. Actual Android one-thread p95 latency and process-memory measurements must
come from the existing licensed fixture in the new native journey. Synthetic
late-return control must be distinguished from actual model inference. No old
FSD50K outcomes are new accuracy evidence. The consumed evaluation remains six
of eight instrument positives and three of eight laughter positives; reliable
automatic recognition is unfinished.
