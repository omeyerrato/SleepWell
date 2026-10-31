# Automatic sound recognition audit and saved-source review

Audited the native Java source at integrated `9843587f` on 1 November 2026.
This increment improves explicit saved-source review. **Reliable automatic
laughter/instrument recognition remains unfinished.** It does not promote the
experimental classifiers into capture, change a gate, add an instrument event
category, or reinterpret a stored result.

## Actual automatic path

`NoiseRecorder` passes real mono PCM16 16 kHz microphone samples to
`SoundAnalysisStream`. Its YAMNet input is 15,600 actual samples per complete
frame, normalized by 32768; gaps clear incomplete analysis. The Sonar capture
path passes its downsampled real audio through `SonarSoundWorker` to the same
stream. That worker has a bounded 64-block queue and reports overload instead
of inventing continuous coverage. Capture gaps, pauses and cue-output masking
remain effective.

`SoundAnalysis` admits snoring at score 0.60 and unrelated-label margin 0.15,
excluding the compatible breathing parent. Speech, laughter, baby crying and
cough families require 0.65 and margin 0.15, with their existing parent/family
handling. `SoundSensitivity` controls grouped duration, not model confidence.
`SoundCategories` persists only `snoring`, `speech`, `cough`, `baby_cry`,
`laughter`, and `noise`. These categories are shared with correction, filters,
backup validation and sound-triggered actions. No automatic instrument category
or supplemental-model execution is currently present.

The existing bundled PANN and EfficientAT models run only after an explicit
recorded-analysis action: sequential single-CPU sessions, actual nonoverlapping
10-second windows and a real tail of at least 2 seconds, with source hashes,
model/policy version and omitted duration. [Current offline contract](RECORDED-SOUND-ANALYSIS.md).

## Why automatic admission is still unsupported

The frozen source/uploader-separated FSD50K development/evaluation study has
already been consumed. Untouched evaluation accepted 6/8 instrument sources
after the development-selected fallback, versus 1/8 before; laughter stayed
3/8. It added no accepted distractor/wrong-family sources or lost positives in
that small set. These are generic original-clip results, not independently
annotated native-bedroom interval accuracy. The five laughter and two instrument
misses remain misses. [Frozen study and controls](FSD50K-RECOGNITION-CALIBRATION.md).

Still missing are source/speaker/device-separated development and new untouched
evaluation with actual native microphone interval annotations: quiet/solo/crowd
laughter, isolated and mixed instruments, and speech/snore/cough/cry/moan/media/
room-noise negatives across microphone distances, devices and rooms. Rejection,
false events per hour, missed intervals, temporal grouping and coexistence with
the retained classes must be established on those recordings. Original-source
licensing, PCM hashes and interval provenance must be retained. Existing failed
or consumed holdouts cannot select another gate, framing policy or model.

Sustained on-device latency, peak memory, battery cost, thermal behavior and
capture-deadline/queue behavior for the approximately 36 MB supplemental assets
also remain unmeasured. Successful foreground CPU inference does not establish
that budget for an overnight live worker. A new persisted category would also
need justified admission and consistent correction, backup and action semantics.
No supported complete automatic recognizer follows from the current evidence.

## Functioning supporting increment

Saved retained-clip result details now offer **Prepare saved source again** and
**Open source sound events**, including when reached from the global saved-result
list. Both revalidate the original record, clip, optional event, effective
coverage, revision/fingerprint and exact PCM/WAV hashes on the existing worker.
Pending deletion, missing/changed source, excluded intervals, cancellation and
active tracking/alarm reject the operation. A failed expected-source check now
also erases its temporary PCM copy.

Preparation reaches the existing review screen; **Analyze this copy** remains a
separate explicit action. Discard erases that private copy. The previous summary
stays intact. Source-event navigation opens the existing event/correction owner
without starting playback or copying experimental scores into the sound ledger.
The temporary validation copy is erased. Imported summaries explain that the
file must be selected again; no document access or audio copy is retained.

## Bounded verification handoff

Extend the existing API36 CI-only
`RecordedAnalysisJourneyTest#savedClipRunsBundledModelsPersistsAndDeletesSummary`;
do not introduce another test method or accuracy replay. Its original saved-WAV
and resolver-import inference calls and assertions remain. The added segment
reopens a global saved result, checks exact-source preparation without inference,
discards and checks PCM zeroing, changes one byte of the owned fixture WAV to
require stale-source rejection, restores that owned fixture, then opens source
events and returns with no allocated playback session. It checks the saved
summary, source rows, audio bytes and preferences remain unchanged and closes
the owned readers/workers before fixture cleanup.

The existing `recorded-analysis` and `data-recovery-reference` CI scopes must
require all seven actual original PNGs: the previous five plus
`recorded-analysis-source-prepared-again.png` and
`recorded-analysis-source-events.png`. Inspect the original images against Night
Dial, including details scrolling, complete action labels, 48 dp controls and
status/navigation-bar clearance. Presence and passing instrumentation alone do
not establish visual acceptance or recognition accuracy.

This branch has not run Gradle, native CI, model inference, a unit campaign,
downloads, a personal phone, capture, playback or alarm output. Integration owns
the focused native execution and evidence. No existing frozen evaluation or
model assets were changed.
