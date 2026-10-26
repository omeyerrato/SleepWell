# Confirmed sound automation sources

Four new opt-in source kinds extend the existing rule editor and its Tasker/app,
HTTPS POST and TLS MQTT transports. New rules remain disabled until explicitly
saved and enabled. Opening a screen, selecting a sound, changing settings or
creating a rule never sends a source event. Existing rule names and public
schema 1 remain valid. These are SleepWell contracts, not vendor action aliases.

## Playback acknowledgements

`SLEEP_SOUND_STARTED` follows a real `MediaPlayer.start()` and `isPlaying()`
acknowledgement on the owned decoder worker. One random private occurrence
identity is held for each Play/resume segment. Repeated decoder callbacks,
mixer layers, playlist tracks, ambient rotation, loading and UI recreation do
not create additional starts within that segment. Playback previews outside
SleepSoundService do not qualify. Muted media, zero mixer gain and a silent file
can still qualify: this establishes a player state, not sound heard by a person.

`SLEEP_SOUND_STOPPED` follows a pause/end/error/destroy boundary only after all
acknowledged players and pending start calls in that segment have completed
successful pause or release. Merely receiving STOP, publishing a PAUSED snapshot
or closing an Activity does not establish this. A loading failure that never
played emits neither event. Cleanup failure or process termination can omit the
stop event. Rotation/replacement gaps within a continuing segment do not emit a
new stop/start pair. A resume has a distinct occurrence identity.

## Anti-snore sound output

`ANTI_SNORE_SOUND_STARTED` is emitted at most once for each real production
cue attempt: either `ToneGenerator.startTone()` returns true, or the first
4000-frame headphone pulse is fully drained while the existing supported route
and permission/owner predicates remain valid. Later repeats retain that same
occurrence. A route setup request, silence used to establish the route, failed
output, preview and vibration-only cues do not emit this kind. Android's
vibration API has no physical-actuator success acknowledgement, so this event
intentionally does not represent vibration. It does not prove audible output
or anti-snoring effectiveness. Existing cue budgets and output masking remain.

## Saved recognition estimates

`SOUND_RECOGNIZED` is called only after a successful production `sound_events`
insert returns its saved row ID, for both movement and shared contactless
analysis. Its identity hashes the original recording/row/timestamp/class
occurrence; those private IDs themselves are not transmitted. Selection, raw
PCM windows, failed persistence, model initialization/failure, imports,
reclassification and report recreation do not emit new recognition events.
An earlier genuinely persisted estimate may remain valid even if later analysis
fails. There is no continuous-coverage or measured sleep-stage implication.

Actual recognition payloads add optional primitive `sound_class` to the existing
schema 1 fields (`event_id`, `event`, `occurred_at`, `test`, `schema`).
`occurred_at` is the original saved group's Unix epoch milliseconds, not delivery
time. The class is its original saved estimate, never a later user correction:

| sound_class | Meaning |
| --- | --- |
| snoring | Existing conservative snoring estimate |
| speech | Speech-family estimate; not proof of sleep talking |
| laughter | Existing laughter-family estimate |
| baby_cry | Baby-cry estimate; not general crying or verified infant evidence |
| cough | Existing cough/throat-clearing estimate |

Generic noise is not sent. Existing classifier thresholds, selected categories,
minimum duration and consent remain unchanged. In particular this addition does
not repair or supersede the previously failed independent laughter example.
No audio, score, paths, notes, names, device addresses or record IDs are included.
A manually requested test event is explicitly test=true and has no invented
sound_class or recorded recognition occurrence.

A private durable gate admits at most one production recognition occurrence
per 30 seconds across **all** classes, not one per class. Additional estimates
remain in the local recording but are dropped from automation delivery; they
are not postponed/replayed or merged into fabricated summaries. The bounded
handoff accepts at most eight waiting recognition tasks. Queue saturation,
claim/write failure or process death may drop an occurrence. Backward wall-clock
changes conservatively suppress admission until the previous gate plus 30 seconds
is reached. Retries to multiple enabled destinations may arrive closer together;
receivers must deduplicate event_id. This is a source-rate limit, not a guarantee
about remote arrival spacing or simultaneous rule destinations.

## Storage and limitations

Only the private `automations-v1.db` helper advances version 1→2, adding an empty
sound_class column to existing deliveries. Existing pending events keep their
original IDs and empty optional class. Sleep-record database schema 17, portable
backup format 13 and settings exports are unchanged; automation rules and the
rate gate remain private and are not live authority in portable exports.
Older builds cannot open the upgraded automation database: do not downgrade a
live installation expecting its private rule queue to remain accessible.

The source transition, durable recognition-rate claim and asynchronous outbox
are separate transactions. The gate is claimed before enqueue; a crash can lose
an event rather than replay it. Android may delay/reject jobs or broadcasts.
There are no recipient requests, hardware outputs, device/emulator runs, unit
batches or build claims in this source milestone. Runtime/provider verification
and consented hardware behavior remain open.

The public vendor [Intent API](https://sleep.urbandroid.org/docs/devs/intent_api.html)
was inspected for its lullaby start/stop, anti-snoring and five recognition event
categories and its 30 second source limit. SleepWell does not alias vendor package
names or emit unvalidated REM/deep/light, apnea or medical events.
