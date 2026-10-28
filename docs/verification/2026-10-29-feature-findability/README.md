# Feature findability: explicit alarm and night selection

Candidate source for future integration; no release, install or phone action. The existing Settings organization and all settings remain available.

## Scope and source

Base `cff2778ca6baaed3abeaa9c39a660b7bb932a869` retains the frozen 0.1.5 production tree. The initial three bounded source commits follow it:

1. `cae7d66f2a5e9cbc954dd9a3e4c1ae504fbceb00`: user-facing feature names/aliases, explicit saved-alarm or new-draft chooser, allowlisted editor arrivals, actual alarm identity and draft scope, Report editing through History.
2. `ee6fc0c32f82f0d7bc6d51a71f916a3519b96e4c`: prefer visible title matches over hidden keywords; compact sleep-noise-analysis alias.
3. `e568a3a8f69ce202c43e8cf7b76fccbef6a64fa9`: Flashlight result opens its actual owned options dialog after resumed/focused readiness; externally opened Gentle footer says **Keep changes in draft**, with **Save alarm** scope.

Exactly seven production files change: `SearchSettings`, `SearchIndex`, `SearchActivity`, new package-local `SearchAlarmChooser`, `AlarmEditorSearchUi`, `AlarmEditorActivity` and `AlarmToneScreen`. No manifest export, engine, scheduler, database schema, global design, icon, release or version change. Named editor keys are a closed allowlist; there is no caller-supplied Activity/URI route. New drafts derive from actual saved new-alarm defaults and are neither saved nor scheduled on arrival. Nap records are excluded from this chooser; failed/deleted lookups never select a different alarm.

Native extension `5eb8292a1bd0f1625b352720727e2187352a5b8c` changes the same existing `SearchFlowTest#queryResultsRecreationAndSavedRecordOpenUseRealControls`. Scope `8c701162e4e91101a91985ca45d23c6909e995b0` adds only **feature-search**, one method / 18 required originals. The previous **room-settings-search** five-image inventory and all other scope mappings/defaults remain unchanged. Native correction `4b0949e57ed8d5576c92cd1877e0721b4532b25c` binds readiness to the exact Activity decor or its actual owned dialog, and returns on the accepted predicate once. It changes no production source, action, timeout or capture inventory.

`source-equivalence.json` pins every changed production path and both native hashes. The first two attempts use `app/src/main` tree `56d344a63592779ab458c0b40824cd59c7021b4c`; the earlier 0.1.5 base tree is `435a3f890533d2a4c4554ab77aa2b5dbdccfb682`. This proof does not reuse the historical five-image Search pass for the new runtime.

## Actual runs

| Run | Source | Result |
| --- | --- | --- |
| [40781016190](https://github.com/omeyerrato/SleepWell/actions/runs/40781016190) | `8c701162` | **FAIL**, 14.782 s, one method; five actual PNGs including failure diagnostic, not 18-frame acceptance. |
| [40782999524](https://github.com/omeyerrato/SleepWell/actions/runs/40782999524) | `4b0949e5` | **FAIL**, 40.748 s, one method; ten actual PNGs including failure, not 18-frame acceptance. |
| [40784711149](https://github.com/omeyerrato/SleepWell/actions/runs/40784711149) | `19f6ac9e` | **PASS**, 44.557 s, status 0 / OK (1 test), all 18 required originals, no failures; full_app_review=false. |

The failed method could match the chooser's **Extra loud** heading while the editor was launching because its readiness helper searched any focused app window. Its generic wait then evaluated the predicate again. Raw guest lifecycle shows the actual editor resumed and its options dialog acquired focus; no app crash or ANR is recorded. The correction requires this exact owner's current root and actual control, without retapping or weakening gates. `failed-40781016190/` retains all 17 raw artifact files byte-for-byte, including its failed summary, stack, hierarchy and five original images. All five were individually opened.

The second failed run reached the deliberately saved second alarm, reopened it, restored the query/category and opened the new Flashlight draft's actual options. Its post-recreation Sound options were lost because the pending boolean was interpreted from the original Flashlight feature. This was an actual production navigation/restoration defect. `failed-40782999524/` retains all 22 raw files byte-for-byte; all ten actual PNGs were individually opened. The method remains failed and its earlier reached assertions are partial evidence, not a pass.

Source-only correction `19f6ac9e121c0767d5b63d8aab93c9c40c895835` changes only `AlarmEditorActivity`: pending **sound/gentle/none** now records the actual options destination independently of the original feature, requires the current parent page/resumed/focused owner, consumes once, and clears on explicit Back/navigation or parent mismatch. The new `app/src/main` tree is `7d0b39312318829464aae68027fe8cf1c9ed45a9`; the other six production files and native source are byte-identical to the second attempt. The exact native method and 18-image scope are unchanged. Source/native compile passed in 46 s, 25 tasks (two executed, 23 up-to-date).

The corrected native source type-check passed in 38 s, 25 tasks (one executed, 24 up-to-date). Earlier final source/native type-checks passed; no unit batches, new `@Test`, physical output checks or local APK build were run for this change.

## Verified native boundaries

The existing method retains its original five images and Room navigation checks. The extension uses a UUID-private SQLite database, two actual disabled alarm records, the existing inert WakeChecks fixture, a fake scheduling port and a strict preview port that fails if audio is opened. It checks explicit selection of the second alarm, unsaved toggle/recreation, deliberate **Save alarm**, reopen, first-alarm identity, new-draft recreation, **Keep editing** / **Discard**, query/category restoration, actual History selection and the selected record's editor/Back path. It also checks feature aliases, explicit local destinations, global protection/defaults scope, unchanged original record fingerprint and unchanged preferences.

Both optional output flags are false. Auto-detect sleep and protection images show their real search results; they do not claim automatic tracking admission or platform power-menu enforcement. Loading/error/empty chooser branches are honest source paths, not native states covered by this two-alarm fixture. The Gentle draft footer is checked for presence and is visible behind the options dialog; its callback is unchanged and this journey returns using Back. Other named feature targets are checked as actual local catalog/Intent destinations, not opened platform behavior. All original record-table fingerprint and preference assertions completed. Only the explicitly saved second disabled fixture alarm reached the fake scheduling port; the first alarm was unchanged, and Keep editing/Discard left no new alarm row.

## Reference and limits

Board 11 left guides cream/brown typography, ruled rows, labeled destinations and clear scope. It does not contain a Search or alarm-chooser mockup, so these are functional supporting routes rather than invented completion of another reference screen. The Settings home organization is unchanged. The per-alarm sound/options and Report History/editor routes retain their existing designs and real data.

All 18 successful-run originals were individually opened against Board 11 and supporting Boards 03/08. `passed-inspection.json` names each observation. Search/chooser scope and actions, all full options dialogs, actual toggles, unsaved warning and History row are readable; no new core clipping was observed. The long unique-record query uses its existing single-line input scrolling. The History fixture has one actual Manual period, so it correctly has a plain coverage ribbon rather than invented measured variations. The system AlertDialog discard prompt remains platform white, and per-alarm identity/draft warnings add intentional context absent from the boards. The existing global SystemUI contrast item and alternate fonts/themes/devices remain outside this bounded acceptance. No claim of all 36 screens, all features, physical alarm/volume/torch/microphone behavior, OS power-off prevention, accessibility-service enforcement, phone persistence, provider/hardware acceptance or a release/install is made.
