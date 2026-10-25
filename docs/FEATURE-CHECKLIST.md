# SleepWell feature checklist —26 October 2026

Not fully done. Checked items have actual production implementation and build
success; they are not claims of current phone/overnight validation. Unchecked
acceptance items below remain required. Latest application sourceb212a67 builds
in15s after the38s combined build; its published APK retains the installed signer
and matching SHA256.

## Implemented feature paths

- [x] Best-effort prevent-leave and recognized power-menu handling during alarms.
- [x] Extra-loud toggle: maximum alarm stream plus optional owned-session gain.
- [x] Gentle volume increase, sound delay and independent vibration timing.
- [x] Screen sunrise and flashlight wake-up.
- [x] On-device snoring/speech estimates, triggered clips, playback, favorites,
  correction, retention and export.
- [x] Automatic tracking from schedules/phone stillness, recurring starts and
  optional Google Sleep confidence input.
- [x] Optional guarded rejection of likely false automatic starts.
- [x] Anti-snoring sound/vibration cues, with separate consent and output bounds.
- [x] Travel time-zone planning, dated sleep shifts, reminders and wake-alarm drafts.
- [x] Real recorded-history graphs/trends, record edits, tags/notes and goal bank.
- [x] Per-alarm detail settings and custom time/duration selectors.
- [x] Weekday, odd/even ISO-week and anchored day-interval repeats, skips,
  excluded dates and sunrise schedules.
- [x] Reviewed one-occurrence rescheduling, Skip/Undo and ordinary recurrence restoration.
- [x] Optional per-alarm orientation lock with saved input settings.
- [x] Configurable challenge routines, previews/fallbacks, QR/barcode/NFC,
  text/memory/Math/walking and additional motion/camera/object tasks.
- [x] Separate explicitly started on-device spoken-phrase challenge and Math fallback.
- [x] Generated morning QR codes, PNG/PDF export and Android printing.
- [x] Naps, Power nap, weekly nap profiles and exact-second timers.
- [x] Bedtime reminders, app guard and temporary pause.
- [x] Battery-aware movement/light sampling and separate low-battery reserve stop.
- [x] Optional manual wake-up sensor FIFO batching on supported hardware.
- [x] Snooze-aware home widgets and screen-only shortcuts.
- [x] Local ambient mixer, imported audio, timed fade, optional narration/binaural tools.
- [x] Saved local-document playlists with ordered track completion, pause/stop and one timer.
- [x] Optional tracking-owned fade after sustained measured phone stillness.
- [x] Metadata/audio backup and restore, including optional user-selected folder versions.
- [x] Integration paths for sleep-only Health Connect, calendar, smart lights,
  Tasker/webhooks/MQTT, subject to their external services/permissions.
- [x] Published development APK; original signer and uploaded checksum verified.

## Still missing or narrower than full parity

- [ ] Validated phone sleep-stage/REM and breathing/apnea analysis.
- [ ] Reliable laughter/instrument recognition and additional recognition scope.
- [ ] Built-in rotating playlists and external-music player control.
- [ ] Dedicated Sleep Phaser hardware protocol.
- [ ] Direct legacy Google Fit/Samsung Health SDK clients; Health Connect is the
  existing route.
- [ ] Hosted SleepCloud/research-community/Spotify account-service parity.
- [ ] Established chronotype, jet-lag-prevention or lucid-dream efficacy.

## Acceptance and deferred work

- [ ] Physical alarm delivery, leave/power-menu handling, extra loud, volume restore,
  ramp, sunrise, flashlight, snooze and challenge behavior on the actual phone.
- [ ] Overnight automatic tracking, noise accuracy, battery and anti-snoring behavior.
- [ ] New repeat/DST/calendar behavior, false-start data cleanup, printing, widget
  hosting, one-occurrence overrides, FIFO/Doze capture and real provider exchange.
- [ ] On-device phrase recognition, microphone cleanup and orientation behavior.
- [ ] Local playlist provider/decoder lifecycle and stillness-fade sensor/output behavior.
- [ ] Full36-screen reference design fidelity, especially Report/charts and menus.
- [ ] Remaining interaction bugs and security review, deferred by user instruction.
- [ ] Back up/install the latest APK on the phone and verify preserved data.
- [ ] Full completion verification; current builds alone do not establish it.

Forced hardware power-off cannot be prevented by an ordinary Android app. The
personal phone remains untouched on571018b/schema7. No new unit/native/lint/
screenshot/device batches ran in this feature window. Details and bounds are in
[FEATURE-MATRIX.md](FEATURE-MATRIX.md) and [REMAINING-WORK.md](REMAINING-WORK.md).
