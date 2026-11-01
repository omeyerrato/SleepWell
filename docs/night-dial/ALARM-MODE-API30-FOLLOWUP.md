# Alarm mode selector and API 30 lint follow-up

Source base: `ec2a1647`. This change does not claim a new native or lint pass.

## Silent native failure

Run `41254733103`, method
`AlarmSoundModeFlowTest#silentAndPlaylistChoicesCancelRecreateSaveReopenAndReturnSingleWithoutOutputs`,
stopped at `single owned input Shuffle order`.

The original `alarm-mode-failure.png` and XML show the correct three-track playlist
and enabled, unchecked native switch. Its accessible description is now
`Shuffle order. One shuffled order for each alarm occurrence`, which includes the
explanation supplied by the shared `Design.toggle`. The fixture's old exact
description was absent. The test now targets the full current description. It
retains the exact focused decor, attached owner, full visible 48dp bounds, rendered
frame, one real input, persistence, recreation, cancel, and scheduler-fixture
assertions. The production accessibility description remains unchanged.

## Four lint errors

The integrated debug lint report named four errors:

- `ChallengeActionRows`: `Collection.toArray(IntFunction)` needs API 33 without
  desugaring. Use the older `toArray(new String[0])` overload for API 30. The
  neighboring `Stream.toArray(IntFunction)` is a different, supported API.
- `styles.xml`: move `android:windowSplashScreenBackground` to `values-v31`.
  Both theme variants inherit the same `AppThemeBase`, retaining the complete
  font, colors, window, dialog and input settings. API 30 continues to use the
  existing black `windowBackground`; API 31+ retains the black system splash.
- `AlarmEditorUi.weekday` and `NightDialArt`: narrow `AppCompatCustomView`
  annotations document the intentional platform widgets. SleepWell uses
  `android:style/Theme.Material.NoActionBar` and owns weekday styling and bitmap
  decoding; these widgets do not rely on AppCompat tint or resource loading.
  Adopting AppCompat widgets would require a different theme contract. No NewApi
  suppression, global lint suppression, dependency or framework change is added.

## Consolidated verification owner

Root will rerun debug lint/build and the existing native alarm sound-mode method
on a silent emulator. Its existing screenshots (including configured/recreated/
reopened/single and radio draft/error recovery) remain the required evidence;
there are no new UI states or screenshot names. No build, unit campaign, emulator
or physical phone operation was run in this worktree. Confirm API 30 theme loading
and API 31+ splash resource selection in the consolidated checks if those API
targets are available. Physical alarm, sound and haptic behavior are outside this
change.
