# Automatic tracking: draft routes and current-wait meaning

[CI 40735370471](https://github.com/omeyerrato/SleepWell/actions/runs/40735370471)
passed at `0d2c0fb8181ab7c9f76a4f2653994232973133d6`: **83.548 seconds, one
existing method and all 28 required original PNGs**. The unchanged
[transcript](OrganizedSettingsJourneyTest%23organizedMenusPreserveDraftsAndSafeChoicesWithoutOutputs.txt)
reports status 0 and `OK (1 test)`; the [summary](focused-review-summary.json)
has no failures and `full_app_review: false`. [run.json](run.json) records the
successful application/test APK build and authoritative head.

Exact selector:
`OrganizedSettingsJourneyTest#organizedMenusPreserveDraftsAndSafeChoicesWithoutOutputs`.
Scope `organized-settings`; both `native_sounds` and `audio_imports` false.
Disposable API36 x86_64 emulator, 1080×2316 override, density420, font scale1.0,
12-hour clock. The environment reports `system_broadcast_idle: false`; the
script still required three actual MainActivity focus samples, and the native
method's exact preference/runtime preservation assertions completed.

## Visual comparison and all originals

Every final original below was opened individually. Compared with
[Board10 left](../../../design-concepts/2026-10-22-phone-app/10-phone-sensors.png),
the Automatic main now keeps the four distinct radio choices, source and
prerequisite routes above an explicit Save/Arm footer. After settling down
truthfully remains a phone-idle estimate. The current Activity access state is
conditional on the chosen optional Google condition, rather than an invented
Enabled pill. Recurring, missing-night, sound and detail controls remain in a
named supporting page. Normal, error, current-wait and recreated children have
readable labels and full footer actions in these originals.

This is a bounded hierarchy/action review, not pixel equality or all-screen
acceptance. Supporting pages have no direct Board10 mockup. The retained
Tracking setup frames were also checked against
[Board06](../../../design-concepts/2026-10-22-phone-app/06-tracking.png);
the inert Motion child against Board10 middle. Other retained settings are
supplementary evidence, not additional full reference acceptance.

All names below have the prefix `organized-settings-` and suffix `.png`.

| Original | Individually observed state |
| --- | --- |
| [automatic-main](organized-settings-automatic-main.png) | Four choices, At bedtime selected/10:30PM, Source/Conditions/Access routes and complete side-by-side Save/Arm. Core bounds assertions passed. |
| [automatic-source](organized-settings-automatic-source.png) | Recreated saved-bedtime child, real Review sleep plan route, four-hour/Save explanation and full Done. |
| [automatic-options](organized-settings-automatic-options.png) | Recreated supporting page, four named routes, historical stopped-wait result and complete Save/Arm explanation. Title wraps without clipping. |
| [automatic-sounds](organized-settings-automatic-sounds.png) | Recreated private Record sounds/Keep clips draft On, movement-only recurring explanation and full Done. No microphone admission. |
| [automatic-detection](organized-settings-automatic-detection.png) | Recreated Google/rejection drafts On, actual missing-activity-access message, both details links and full Done. |
| [automatic-access](organized-settings-automatic-access.png) | Actual Not allowed grant state, optional-condition explanation, Google settings route, Manage activity access action and full Done. |
| [automatic-active-wait](organized-settings-automatic-active-wait.png) | Modeled ARMED owner, current-session route, restored charging/Google draft, fresh-dispatch error, explicit future-choice Save meaning and disabled Arm. |
| [automatic-saved-reopened](organized-settings-automatic-saved-reopened.png) | Explicitly saved/reopened Stillness/Charging/Google choices with modeled wait still active and full footer. |
| [automatic-conditions](organized-settings-automatic-conditions.png) | Charging On and screen-off Off survive recreation before saving; full Done. |
| [automatic-permission-recovery](organized-settings-automatic-permission-recovery.png) | Synthetic notification denial after request recreation: one rebuilt settings action, complete error and side-by-side Save/Arm. |
| [tracking-automatic-manual](organized-settings-tracking-automatic-manual.png) | Actual Automatic child reached from dark Tracking. Manual selected, all four choices/routes and Save visible. Dark status and gesture glyphs on cream. |
| [sensitivity-categories](organized-settings-sensitivity-categories.png) | Six category choices, Laughter Off, selected-estimates explanation and Save sensitivity. |
| [sensitivity-duration](organized-settings-sensitivity-duration.png) | Owned Snoring duration dialog, Default selected, all three choices and Cancel visible. |
| [sensitivity-reopened](organized-settings-sensitivity-reopened.png) | Reopened five-of-six selection, custom-duration route, Restore defaults and Save. |
| [retention-details](organized-settings-retention-details.png) | Recordings/favorites/history explanation and full retention action; action is not invoked. |
| [snoring-options](organized-settings-snoring-options.png) | Contactless/output child, both contactless cue choices Off, About and Save. No preview/output. |
| [recurring-conditions](organized-settings-recurring-conditions.png) | Recreated Charging On draft, screen-off Required and phone-idle estimate explanation; full Done. |
| [sounds-sources](organized-settings-sounds-sources.png) | Rain selection, all five source routes, brown-noise choice and Back. No source/provider activated. |
| [sounds-playback](organized-settings-sounds-playback.png) | Tracking start/fade Off, details/other-player consent routes and Back. |
| [timer-recreated](organized-settings-timer-recreated.png) | Recreated45min choice with all durations and Use/Cancel. |
| [fade-reopened](organized-settings-fade-reopened.png) | Saved10min fade reopened, all fade choices and Use/Cancel. |
| [tracking-main](organized-settings-tracking-main.png) | Complete inert setup: sensor cards, placement, sounds/light Off, start-mode route and Start footer. Light system glyphs/gesture on plum. |
| [tracking-more](organized-settings-tracking-more.png) | Real schedule/sensor/battery/recording routes and full return action, no clipped core rows. |
| [tracking-sensitivity](organized-settings-tracking-sensitivity.png) | Private fake Active sensor source, intentionally blank/no-readings chart, Medium slider, Test again/Use this sensor. No hardware samples claimed. |
| [tracking-power](organized-settings-tracking-power.png) | Sampling Off, actual unsupported batching disclosure, reserve Off and full Done. |
| [tracking-power-recreated](organized-settings-tracking-power-recreated.png) | Same supporting battery state after recreation. |
| [tracking-recreated](organized-settings-tracking-recreated.png) | Setup light-recording draft On survives recreation; sounds Off, complete core and Start. No capture starts. |
| [tracking-phone-selected](organized-settings-tracking-phone-selected.png) | Phone movement selected in place after a blocked contactless route; light/sounds Off and complete core. |

The CI-only brown verification label has low contrast on dark sound pages; it
is instrumentation text, not product copy. No production label, control or
system-glyph clipping was found in the represented final states. Larger text,
other locales, smaller windows and other OS versions were not exercised here.

## Completed action and ownership checks

The entire existing method passed, including its final continuation. Actual
native input changes all four modes and private child drafts. Framework
recreation preserves source/options/sounds/detection/access pages and choices;
Done/Back returns through the owned page history. The private store remains
empty until explicit Save. Save/reopen retains Stillness, charging, sounds,
clips, Google and awake-start rejection; a subsequent unsaved Manual choice is
discarded by Back. No private recording or delayed admission is created.

Plan, alarm, recurring, missing-night and current Tracking routes dispatch
actual intents into blocking Activity monitors. The app-details action is
matched by action plus exact package URI and blocked before an OS window.
The activity-access display is compared with the actual current grant; this
method performs no OS permission request, grant or revocation.

The permission subsection uses **synthetic callbacks and fake notification
access**. A pending request and its exact identity survive recreation. A grant
callback to the destroyed old owner cannot act, a wrong-permission result
cannot consume the current request, owned denial rebuilds exactly one recovery
action, and owned empty cancellation releases pending ownership. Fake access
recovery clears the recovery action only after the current access check. None
of these operations saves or arms the draft. This does not prove Android's
permission controller, actual grant delivery or callback timing.

The active-wait subsection uses **a private modeled phase port**, not an armed
service. A stale saved `automaticArmed` flag creates no live-wait claim. A
retained Arm callback is invoked synthetically after that port becomes ARMED;
the fresh guard must reject before save/schedule/arm. Actual native Arm remains
forbidden, and schedule/arm callbacks throw if reached. The modeled phase
remains ARMED while native Save stores future choices. Static service inspection
shows real admission pins the relevant wait/capture choices, but this run does
not verify actual admission, active service reconfiguration or cancellation.

The original sensitivity, retention-cancel, snoring-draft, recurring-cancel,
sound-choice, Tracking menu/sensor-selection and safe-parent-return assertions
remain. Sound playback and commands use private/failing ports. Runtime,
microphone-lease and playback absence are checked throughout. Exact original
global preference values and key absence are restored after all owned
Activities and existing workers drain; temporary private stores are removed.

## First passing run exposed a visual gap

[CI40729522971](https://github.com/omeyerrato/SleepWell/actions/runs/40729522971)
passed all assertions in79.935s at `ed65b1ab`, with all28 required originals.
It was **not accepted visually**: its unchanged
[manual-entry original](visual-gap-40729522971/organized-settings-tracking-automatic-manual.png)
has white status glyphs on cream after entering Automatic from dark Tracking.
The raw successful summary, environment, transcript and run metadata are
retained in that subfolder. There was no failed native transcript to relabel.
All original downloaded files, including the other first-run images and full
guest/renderer logs, remain private in ignored `.android-local`.

Source `e0d8b4ae` adds only Automatic's focus-owned call to the existing
`RecordUi.lightBars`. Native `0d2c0fb8` retains all28 originals and actions,
requiring the actual requested OS appearance plus dark rendered glyph/gesture
pixels on cream before capture and in the saved original. Existing Tracking
light-on-plum checks remain. No image painting/cropping, input retry, increased
timeout or fixed sleep determines passage. The final manual-entry original
visibly has readable dark glyphs and the actual gate passed.

## Source and original-byte proof

[Candidate order](candidate-commit-order.json) separates source, native,
documentation and scope commits. [Source audit](source-audit.json) records
Git blobs and production trees for the base and every candidate commit. Only
`AutomaticTrackingActivity.java` changes in production from base4cf8a86f.
`TrackingService` and shared `TrackingOptionsUi` are unchanged. The exact
production delta between the two runs is the local focus-bar callback;
production source is identical from `e0d8b4ae` through final run head0d2c0fb8.
The scope change preserves all39 other scope mappings and every previous frame
array; [inventory](scope-inventory.txt) lists28 unique required names.

[Original hash index](original-sha256.json) checks35 copied artifact files:
28 final PNGs, the prior visual-gap PNG and both runs' exact method transcript,
environment and summary. `.gitattributes` preserves their original bytes.
`SHA256SUMS` covers this compact public archive except itself. Full renderer/
guest logs remain private; no personal data or phone artifact is included.

This establishes the named inert UI/draft/ownership assertions and represented
layout in one disposable configuration. It does not establish microphone,
Google provider, physical sensing, alarm/output, real wait admission/cancel,
whole-app security or full36-screen/reference acceptance. No release, root
workspace, personal device or published APK was changed by this work.
