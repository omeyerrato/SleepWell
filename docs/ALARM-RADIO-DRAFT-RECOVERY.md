# Alarm radio source draft recovery — 1 November 2026

Base: `66c9fd4a`, native Night Dial feature integration. The direct-HTTPS alarm
radio source already has an actual asynchronous playback owner, bounded stall
watchdog and Daybreak fallback. This change does not invent a Spotify connector,
connect to a station or change playback/model thresholds.

## Completed source behavior

The radio form used an ordinary Dialog while `AlarmEditorActivity` retained only
shared `SleepPicker.StateDialog` windows. Consequently, entering a radio URL or
getting a validation error and then recreating the Activity discarded the open
form and its input.

Radio now uses the shared stateful dialog, with its own `alarm-radio-source`
identity. Recreation restores the URL, cursor/selection range, inline validation
message/visibility and scroll position. The parent recognizes that identity only
on the sound-playback route. It does not apply the URL while restoring.

Cancel retains the preceding playback mode. Use radio source validates and edits
only the in-memory alarm draft; the normal final Save alarm remains the commit
and scheduling owner. Invalid query/credential/web-player links still fail in
`AlarmRadioUrl`; no automatic network probe or preview is added. Daybreak fallback,
backup escalation and existing playlist/shuffle behavior remain unchanged.

The shared dialog's optional message field is inactive for every other existing
picker. No text, layout, colors, permissions, database/backup schema, model or
dependency changes are introduced.

## Native action/persistence verification

The existing
`AlarmSoundModeFlowTest#silentAndPlaylistChoicesCancelRecreateSaveReopenAndReturnSingleWithoutOutputs`
is extended, preserving its Silent/playlist/shuffle/Single checks and private
scheduler fixture. It now also:

- Types an invalid direct-stream URL and requires a visible inline error.
- Recreates the real Activity with its actual dialog open and checks exact URL,
  cursor selection and error restoration.
- Cancels and verifies that the saved playlist and scheduling count are unchanged.
- Uses a valid `https://example.test/...` URL without any playback/network request,
  verifies that only the draft changed, then uses the normal Save/reopen path.
- Returns the saved alarm to Single mode, and asserts no alarm or sleep-sound owner
  became active.

Required new actual originals are `alarm-mode-radio-error.png`,
`alarm-mode-radio-recreated.png` and `alarm-mode-radio-draft.png`, in addition to
the existing seven originals. Root owns APK compilation and native execution;
this source commit has only diff/static inspection evidence. These fixtures do
not establish real network station or phone-output compatibility.

## Inspected adjacent paths and limits

Alarm playlist/shuffle already has ordered decoder completion, checkpointed
per-occurrence ordering and bundled fallback behavior. Sleep sounds retains local
document queues, direct HTTPS station-library drafts, explicit Play, timer/fade,
provider errors and separate owned/external-media fade admission. The present
repair closes the concrete alarm-radio interruption loss; it does not label every
provider, hardware or hosted-service path accepted. The station-library and local
playlist source remain explicit user choices, not implicit sign-in/provider
access.
