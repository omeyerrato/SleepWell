# Movement-night sound analysis and inline Report audio

This note describes the current source batch through **714fc4cb**. The combined
production build is **running/pending at the time of writing**. No new tests,
screenshots, device, audio, microphone, model, provider or network actions were
performed for this note. The implementation does not establish bedroom recognition
accuracy, complete design fidelity, sleep stages or clinical measurements.

## Pinned request and actual analysis outcomes

At movement-recording admission, the service freezes whether sound was requested
for that night. A new `MotionSoundAnalysis` owns that request and starts the saved
record with explicit `off` or `pending` evidence. An off night cannot become a
successful analyzed night merely because sound preferences are enabled later.
Permission and current capture authorization still apply; historical evidence
grants no permission to start a microphone. Batched movement remains sound-off.

Each actual microphone segment contributes successful **15,600-sample / 16 kHz**
model/classification windows: **975 ms per frame**. The total includes real
successful windows even when no category was accepted. It is not an event count,
a recording-file count or evidence that every moment was analyzed. Partial windows,
paused time and masked gaps are not filled with invented frames. Pause closes the
old segment; resume adds a new segment to the same night's count only after the
prior capture worker has closed. A stale segment cannot replace the current one.

The first issue is retained, including model, capture, queue, storage, cleanup,
interruption and limit outcomes. A model or storage failure does not get hidden by
an empty event list or a stale global preference diagnostic. The capture worker's
last real result can still be acknowledged during shutdown; a live terminal error
is not checkpointed as immutable final evidence while that worker is still open.
Final outcomes distinguish `complete`, `insufficient`, `error`, `interrupted` and
`off`, rather than treating all zero-event nights as quiet successful analysis.

Source: [pinned movement evidence](../app/src/main/java/org/sleepwell/app/MotionSoundAnalysis.java),
[capture segment updates/cleanup](../app/src/main/java/org/sleepwell/app/NoiseRecorder.java),
[successful-frame accounting](../app/src/main/java/org/sleepwell/app/SoundAnalysisStream.java),
[service admission/pause/end](../app/src/main/java/org/sleepwell/app/TrackingService.java).

## Durable end, recovery and portable compatibility

Analysis checkpoints must follow the saved request, never decrease frame counts,
and never move an already terminal outcome forward. Frame time cannot exceed the
durable active capture time. `complete`/`insufficient` are final-end outcomes,
not premature open-session checkpoints. The final analysis value and actual session
end are written in one database transaction, so either both commit or both roll
back. A failure is reported instead of claiming successful finalization.

False-start rejection passes the actual final analysis outcome into its existing
owned transaction. Only an eligible untouched automatic-start row can be discarded;
uncertain/edited records remain interrupted for review. Pending audio deletion uses
the existing journal. False starts do not become successful-night backup/calendar
triggers. Recovery turns unfinished saved analysis into interrupted evidence while
preserving known counts; it does not infer missing model results.

The existing `sound_analysis` field now accepts this evidence on tracked movement
as well as SONAR records. Empty legacy evidence stays **unknown**, not off or
complete. Metadata backup/restore preserves and validates the request, state,
frame count and issue; restored unfinished evidence is recovered as interrupted.
**Database schema 17 and backup format 13 are unchanged. This does not make new
movement evidence readable by every older build:** older readers that accepted
`sound_analysis` only for SONAR may reject a new movement-night backup. Do not
describe this extension as backward-compatible with those SONAR-only validators.

Source: [transactional checkpoints/end and recovery](../app/src/main/java/org/sleepwell/app/Store.java),
[state invariants](../app/src/main/java/org/sleepwell/app/SoundAnalysisStatus.java),
[portable evidence codec](../app/src/main/java/org/sleepwell/app/SoundAnalysisProvenance.java),
[false-start transaction](../app/src/main/java/org/sleepwell/app/AutomaticFalseStartStore.java),
[backup restore](../app/src/main/java/org/sleepwell/app/Backup.java).

## Live, event-list, signals and Report wording

Live tracking reads the current run's analysis snapshot for Off, Preparing,
Analysis only/Analysis + clips and Needs attention. The event list, recorded-signals
detail and Report share historical copy derived from the saved record:

- Explicit off is different from a completed analysis with no matching events.
- Insufficient audio is different from model/capture/storage/cleanup failure.
- Legacy absent status is unavailable/unknown; malformed status needs attention.
- Manual entries have no microphone analysis.

Successful analyzed duration is `frames × 975 ms`, not the full recording duration.
When a record has been trimmed, that outcome is labeled **Original recording**;
it is not recomputed or falsely restricted to the edited window. Event counts and
playable selection still use the edited interval. User corrections remain user
labels, with other events described as estimates. No stage graph or awake-duration
projection is created from these sound outcomes.

Source: [shared historical copy](../app/src/main/java/org/sleepwell/app/SoundAnalysisCopy.java),
[live display](../app/src/main/java/org/sleepwell/app/TrackingActivity.java),
[sound events](../app/src/main/java/org/sleepwell/app/SoundEventsActivity.java),
[recorded signals](../app/src/main/java/org/sleepwell/app/RecordSignalsActivity.java),
[Report](../app/src/main/java/org/sleepwell/app/ReportScreen.java).

## Actual inline audio in Report

Report's Snoring, Speech and Other rows count events using the effective category
(`user_category` when corrected, otherwise the original estimate) overlapping the
current edited period. Empty categories are omitted. Preview loading checks that
the record and counts still match, then tries **at most four chronologically ordered
linked candidates per category**. It selects one actually readable, fully contained
event/clip interval after applying the edited-window bounds. The row is one retained
event, not a concatenated category soundtrack or a sample fabricated from its label.

The waveform is decoded from that actual PCM interval. Analysis-only events have
no invented waveform or Play control. Missing, deleting, invalid or stale audio
remains nonplayable with an unavailable notice. A category can have valid events
but no usable preview among its four bounded candidates; the event list remains
the route to fuller inspection.

Playback requires explicit Play; Pause is a real player action. Admission rechecks
the record/edit bounds, label revision, clip/event identity and file size/mtime,
then uses the shared guarded player. While playing, periodic checks can stop stale
content. Calls, alarms, tracking and active/loading sleep sounds block admission.
Loss of the visible focused owner, noisy-route broadcast, Activity pause/destruction
or row detachment stops/disposes preview work; callbacks are generation-fenced.
These checks are point-in-time guards, not a claim that Android/provider races or
physical output have been validated.

Each row's **three-dot menu** actually opens **View sound events** or **Audio
retention settings**. It suspends playback before opening the selector or navigating;
it is not a decorative affordance or an immediate category-selection arrow.

Source: [bounded real candidate selection](../app/src/main/java/org/sleepwell/app/ReportAudioData.java),
[inline rows, menu and lifecycle](../app/src/main/java/org/sleepwell/app/ReportAudioRows.java),
[shared target validation](../app/src/main/java/org/sleepwell/app/ClipReviewAccess.java),
[shared player](../app/src/main/java/org/sleepwell/app/ClipReviewPlayer.java).

## Parent microphone-task Math fallback

The parent alarm screen now checks cleanup before changing a Speak, Laughter or
Loud sound task to Math. Only matching run/unit pending and owned attempts are
cancelled. Math is selected only after those matching attempts report released;
the current occurrence and actual task are checked again before saving the fallback.
With no matching attempt, Math remains immediately selectable. An unrelated owner
is neither cancelled nor treated as released.

If the matching attempt is CLOSING or BLOCKED, the parent keeps the microphone task
and directs the user to reopen it. The child can reattach the owned attempt and
offer **Retry microphone cleanup**. This does not fabricate a release, advance a
proof or restore the matching alarm's quiet lease while capture cleanup is still
unconfirmed.

Source: [matching cleanup helper](../app/src/main/java/org/sleepwell/app/SpeakAccess.java),
[parent fallback](../app/src/main/java/org/sleepwell/app/AlarmActivity.java),
[child cleanup UI](../app/src/main/java/org/sleepwell/app/SpeakChallengeActivity.java).
