# Automatic tracking choices and draft ownership

Source: `69b15445aeee5002f573521f07e2e83fba3eb04e`.
Native extension: `4bd1be77776286d083898799b32a07f94621fdd0`.
Focus contrast repair: `e0d8b4ae`; native rendered-bar gate: `0d2c0fb8`.
Base: `4cf8a86f`. This work is separate from the frozen 0.1.4 release.

The [Board 10 reference](../design-concepts/2026-10-22-phone-app/10-phone-sensors.png)
puts four start choices above source and prerequisite rows. The earlier
[permission-recovery original](verification/2026-10-28-tracking-light-glyphs/organized-settings-automatic-permission-recovery.png)
shows tall choices, secondary setup sections and a large stacked footer.
The earlier [manual original](verification/2026-10-28-tracking-light-glyphs/organized-settings-tracking-automatic-manual.png)
also puts recurring and missing-night routes on the core page. Neither image
is evidence for this new layout.

## Bounded change

Only `AutomaticTrackingActivity` changes in production. Four actual radio
choices remain: Manual only, At bedtime, Before smart window and After
settling down. The latter remains a stillness estimate, not measured sleep.
Source schedule, Start conditions and Activity access are real child routes.
The source child retains explicit links to the saved sleep plan or alarms.
Activity access displays the actual platform permission and has an explicit
app-details action; viewing it requests no permission.

A named Automatic tracking options page holds sound recording, optional Google
sleep confirmation, awake-start protection, recurring starts, missing-night
review and the last result. Back/Done use a saved page history. Draft values,
per-page scroll positions, errors and request-specific pending permission
ownership survive recreation. Opening, navigating or canceling does not save.
Callbacks from destroyed owners, unrelated requests or the wrong permission
cannot arm the current draft. A granted result still requires actual access.

Save settings and Arm tonight share one row with separate 56dp buttons when
arming is applicable. Save keeps future choices. Arm saves and dispatches an
explicit bounded wait through the existing port. A live read-only service
snapshot supplies current phase; the saved `automaticArmed` marker is only a
historical result hint. A current session has a real Tracking viewer route.
The phase is refreshed only while this Activity is resumed, and checked again
at Arm dispatch. Unavailable phase fails closed. No new cancellation protocol
or engine guard is introduced.

Static inspection of `TrackingService.arm` shows mode, wait conditions, Google
subscription choice and awake-start rejection pinned at admission; sound/clip
choices belong to the admitted capture owner. Saving these future choices
neither edits nor cancels the already armed wait. The service, scheduler,
sensor/audio engines, permission defaults and storage contracts are unchanged.

## Existing native journey extension

The same existing method is extended:
`OrganizedSettingsJourneyTest#organizedMenusPreserveDraftsAndSafeChoicesWithoutOutputs`.
No test method is added. Its original 20 frames and checks remain. It now plans
28 originals, adding these exact eight names:

- `organized-settings-automatic-main.png`
- `organized-settings-automatic-source.png`
- `organized-settings-automatic-options.png`
- `organized-settings-automatic-sounds.png`
- `organized-settings-automatic-detection.png`
- `organized-settings-automatic-access.png`
- `organized-settings-automatic-active-wait.png`
- `organized-settings-automatic-saved-reopened.png`

The extension measures the four choices, three main routes, More action and
Save/Arm buttons for complete visibility and 48dp bounds; footer buttons must
not overlap. It uses native input for child choices, Back, Done and Save,
framework recreation, saved/reopened settings and cancel-without-save.
Blocking Activity monitors inspect actual plan/alarm/recurring/missing-night/
Tracking intents and the exact package app-details action without launching
those destinations. The activity permission display is compared with the
current platform grant, without changing it.

Permission-denial/cancel/grant-shape callbacks are synthetic and notification
access is fake. An old owner and a wrong-permission result must not acquire
the restored pending request. Sound changes are permitted only on the private
Automatic draft owner and sound child; capture admission and native Arm remain
forbidden. A deliberately retained Arm callback is invoked synthetically after
the private phase port changes to ARMED, proving the fresh dispatch check
precedes saving/scheduling. Schedule/arm ports throw if reached. A modeled
current wait stays ARMED while native Save stores future choices, reopening
restores them, and Back discards a later draft change.

Private fixtures and exact global preference absence/values are restored only
after owned Activities and existing workers drain. Existing no-runtime,
no-microphone, no-playback and no-created-record assertions remain.

## Verification status and limits

[CI40735370471](https://github.com/omeyerrato/SleepWell/actions/runs/40735370471)
passed the exact existing method in83.548s with all28 required originals at
`0d2c0fb8181ab7c9f76a4f2653994232973133d6`. The
[archive](verification/2026-10-28-automatic-tracking/README.md) contains all28
individually inspected originals, exact transcripts/environments/summaries,
source blobs and hash proof. The first passing run79.935s exposed white glyphs
on cream in its manual-entry frame; that original remains explicit historical
evidence. A local focus-bar repair and actual contrasting rendered-bar gates
passed in the final run. Both APKs built in CI. Scope inventory is now28 for
the same named method; all39 other scope mappings and prior arrays are kept.

The main core, restorable children, error/recovery and modeled active-wait
states are readable in the represented normal-text configuration. This is
bounded evidence, not all-device or pixel-equality acceptance. Synthetic
permission results and a private phase port do not establish actual OS denial,
service admission, wait cancellation, Google subscription, microphone,
physical sensing or alarm behavior. Blocking intent monitors prove dispatch
shape, not destination behavior. This is not whole-app security or all-screen
acceptance. No personal device, root workspace or released package was changed.
