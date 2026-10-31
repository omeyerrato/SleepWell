# SleepWell Night Dial — handoff to the feature agent

## Integration target

Integrate **origin/codex/night-dial-design** as the implemented visual baseline. PR: https://github.com/omeyerrato/SleepWell/pull/7 . Implementation/evidence checkpoint: **ef32be8624faf2ba977ca4d57dfdacaeb0bdaee4**. Native-reviewed application revision: **c5d558af529cd0928763be2756cb7b013b856748**. Both have application tree **9f8874f0ffbafea53cc460e6e7c52798c0373ab9** at app/src/main. Main **cc4871eefa5833266f79d6f1b319d5561c1bf285** is already integrated. Later handoff-only commits do not alter the application tree.

Local design checkout: `C:/Users/omeye/.codex/worktrees/night-dial-design/SleepWell`.

Preserve your current feature work and uncommitted changes. Fetch origin and merge the design branch into an integration branch/worktree based on your current feature state. Do not cherry-pick only Activities: helpers, themes, resources, motion and lifecycle adapters are interdependent. Resolve conflicts by retaining current feature/storage/scheduling/permission authority alongside the new presentation. Do not replace newer feature files wholesale or discard later main commits.

## Read first

1. `design.md`: authoritative UI/UX, typography, motion, flows and state contract.
2. `docs/night-dial/INTEGRATION.md`: source adapters, ownership and conflict guidance; latest delivery notes are at the end.
3. `docs/night-dial/SCREEN-COVERAGE.md` and `.json`: 127 manifest Activities mapped to 43 flow families.
4. `docs/night-dial/PROGRESS.md`: implemented coverage and limits.
5. `docs/night-dial/verification/c5d558af/README.md` and `build-receipt.json`: verified source, captures, tests and APK.

## Preserve the accepted design

Dark at every hour. Ground #080808, surface #171717, raised #242424, primary text #F3F3EF, secondary text #B8B8B3, accent #F2BF6D. Reuse NightDialTokens rather than inventing another palette. White primary actions, amber selection/focus, restrained semantic red/green, Inter typography and existing icon assets. Preserve natural text height, minimum 48dp targets, scalable headers/footers and stacked long descriptions.

Give each experience its own composition: turnable time dials, genuine measured report graphs, actual PCM source previews, original sound/diary photographs, paced breathing contours, woven diary surfaces, embossed game cards, task instruction diagrams and actual saved-data widget previews. Keep necessary configuration forms clear. Avoid turning every feature into repeated full-width buttons, toggle stacks or decorative cards. Do not fabricate sleep stages, measurements or progress to fill empty space.

Opening a screen remains silent. Illustrations/configured curves must not masquerade as measurements. Motion follows reduced-motion/system-animation preferences; existing quiet haptic guards remain. Preserve press/cancel/focus feedback, dark native dialogs, natural sheet sizing and explicit destructive/dirty-draft actions. Review popups, selectors, IME and return paths as carefully as root screens.

## Source anchors

- Shared: NightDialTokens, NightDialSurface, NightDialMotion, Design, Ui, RecordUi, SleepPicker, ReportDialogs and res/values/styles.xml.
- Tonight/plan/tracking: TonightScreen, NightArc, NightDialRange, PlanActivity, NightDialMixerPanel, NightDialWaveform and TrackingActivity.
- Other experiences: NightDialNapWheel, NightDialDetails, NightDialArt, NightDialPlanMark, NightDialStereoPreview, NightDialTaskGuide, NightDialMemoryBack and NightDialWidgetPreview.
- Assets: docs/night-dial/RAIN-ASSET.md, JOURNAL-ASSET.md and SOUND-PHOTOGRAPHS.md. Preserve original assets/provenance; keep labels and controls native.

Legacy Design.CREAM/PLUM/LILAC names are compatibility aliases. Review each foreground/background use individually and use semantic tokens for new code.

## Preserve interaction and feature authority

Dial gestures edit a draft. ACTION_CANCEL restores it; release does not bypass review, explicit save or stale-write guards. Linked alarms have a separate review. Keep exact accessibility entry, focus and recreation. Nap edits update in place and do not schedule until the existing explicit action.

Keep existing services, permission requests, model/analysis thresholds, provenance, providers, storage and scheduling ports. Artwork/motion must not start audio/capture or become task-completion evidence. Empty/unavailable/error states remain honest. Diary legacy/predictive Back share the dirty-draft gate. Named delete/skip/copy confirmations keep identity checks. Retain the narrow AlarmRequest nullable-minute repair for time-less external requests.

Keep native Java Views. No Compose/Kotlin/framework/navigation/database/Gradle migration is required to use this design.

## Validate after integration

Read applicable AGENTS.md/skills first. Build assembleDebug and assembleDebugAndroidTest. Check the destination inventory with `python scripts/night_dial_inventory.py`. Preserve meaningful state assertions rather than only changing tests to pass.

The Night Dial workflow runs scripts/night_dial_review.py: **20 native methods, 206 required original screenshots and 84 focused unit methods** passed at c5d558af. The separate appearance scope in silent-ui-review.yml passed one full method and 11 original frames at eb4ced67, with identical application source. Recheck the actual merged revision; those captures cannot certify later changes. Cover save/cancel, rotation, dirty Back, permission/settings return, stale state, loading/empty/error, large text, reduced motion, keyboard and accessibility paths.

Known limits: full local suite **1,078 tests / 10 inherited failures** in QR cleanup, sonar and tracking-delay behavior. Exact names/XML/logs are in docs/night-dial/verification/eb4ced67/. The earlier full native feature run timed out and is not a passing regression. Do not hide these failures or claim all state/hardware acceptance. Physical camera/NFC/audio/sensors/providers, haptic feel, landscape/TalkBack combinations and overnight delivery need appropriate evidence.

## Delivery and user feedback

The user chose **APK only**. Do not install, clear or reconnect the phone unless that choice explicitly changes. Current APK: `C:/Users/omeye/Downloads/SleepWell-Night-Dial-c5d558af.apk`. Version 0.1.25/code 26, minimum API 30; previous preview signer retained. SHA256: `d5c72d236e18959edb4b2e91dc411a8beb92ba289d6d075d9dc309fe45ef15da`. Preserve old pinned APKs and issue a new source/signature/checksum receipt for subsequent builds.

Full actual gallery: `C:/Users/omeye/Downloads/SleepWell-Night-Dial-c5d558af-review/index.html`.

Numbered feedback pack: `C:/Users/omeye/Downloads/SleepWell-Design-Feedback/`. Numbers 01–16 identify full-resolution originals. Preserve the accepted direction while refining the particular layout/control/flow named by the user.

## Integrated feature delivery —31 October 2026

The feature integration branch `codex/night-dial-integration-20261031` now contains both current feature4a20dc56 and this complete6202a00e checkpoint. Built/reviewed app source is `351b78bf0436412d8edd889d7eb234be0bf4f8b3`; [receipt and current evidence](verification/integrated-351b78bf/README.md). The delivered0.1.26/code27 arm64 APK retains the previous signer.22 native methods and226 original captures pass across three identified runs; units were not run. No phone operation or release publication. Do not substitute the older c5 APK for this merged build or infer full app/physical acceptance.
