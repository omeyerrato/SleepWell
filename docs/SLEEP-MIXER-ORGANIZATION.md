# Sleep mixer organization

The listening page follows board 07: existing artwork, explicit Play/Pause and playing-time progress, primary and brown-noise levels, Timer, Fade out, and four bundled sound tiles. Stop remains a 48 dp header action. The existing stream, file, generated-tone and audio-ownership engines are unchanged.

`More sound settings` opens three real destinations:

- **Sources & playlists:** My audio, ambient rotation, local playlist, internet radio, binaural tones, and standalone brown noise. Selected sources show their actual saved state. Arrows open detail/editor screens; direct source choices are buttons or selected tiles.
- **Playback & tracking:** tracking auto-play, measured-stillness fade and the separate other-player fade consent screen. The original eligibility, manual-only source restrictions and consent explanations remain.
- **Guided wind-down:** the existing timed practice/narration activity.

My audio has a detail page with explicit Use, Import and Remove-from-mix actions. Existing URI validation/import progress/cancellation are retained. Radio, playlist and rotation keep their original independent drafts, revision checks and Save/Cancel behavior; returning restores their source-menu parent.

Timer and Fade out now use dark, Inter choice screens. Selecting a row changes only the draft; **Use** applies it, while header Back, system Back and Cancel discard it. The draft and expected saved-mix revision survive recreation. A stale revision is rejected; arbitrary previously saved valid minute values remain visible. Shortening the timer shows and applies the existing fade clamp. Applying to an already playing mix uses the existing update path; merely opening/restoring never starts audio.

The current menu, parent and per-menu scroll positions survive recreation. Import status/errors remain visible on supporting menus. No feature is removed, no account/provider support is invented, and source selection continues to require manual Play.

## Private routing

`SleepSoundsActivity.EXTRA_PAGE` is `sleepSoundPage`. Allowlisted launch values are `main` (default), `more`, `sources`, `playback`, `timer`, and `fade`; all other values fall back to the listening page. Saved state takes precedence on recreation. The existing private Activity declaration suffices; no permission or manifest change is required.

## Verification boundary

This increment is source-only. Board 07 was visually inspected, but the resulting Android geometry, route/recreation behavior and audio interaction have not been exercised by this author. No phone/emulator actions, playback, capture, builds, lint or test batches were run. Root performs the combined build; actual screenshot comparison remains separate.
