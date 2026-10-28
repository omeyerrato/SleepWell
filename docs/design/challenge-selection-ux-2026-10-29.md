# Challenge selection and draft navigation

Future source increment from `3a54f48d8543c62b900398282604b286ced32ada`, isolated from the frozen 0.1.5 APK/source `ef694082`. Production: `55940ba3321a48bae3fa87e59d969282613c167c`. Native preparation: `ad01699e3dc1cd993f32b851e9756fa38c3c9cff`. This is implemented source with compiled native preparation, **not new native screenshot acceptance**.

The question is: **Which challenges will this alarm use, and when do my edits take effect?** The exact supplied [board 04 left](../../design-concepts/2026-10-22-phone-app/04-challenges.png) overrides generic design guidance. Earlier actual [routine originals](../verification/2026-10-28-stale-math-owner/) were inspected, including legacy, ordered, recreated and reopened. Their routine composition helpers match the starting source; those images predate the raster icon replacement and do not certify the new UI.

## Concrete changes

| Before | Implemented behavior |
|---|---|
| Available-task rows used the same chevron as editing an existing task. | Available rows use the board's **plus** affordance and open real configuration before adding. Existing routine-task chevrons still open their real edit page. Both are accessible buttons. A full routine stays disabled with its real eight-task limit and explanation. |
| All 28 task types were in one long list behind More task types. | The same catalog has an actual search field, clear action, meaningful empty result and five named groups. It renders directly from every entry in `ChallengeRoutine.TYPES`; no task is dropped. Query and scroll survive recreation and returning from configuration. Navigation closes the catalog keyboard. |
| Ordinary 1–6 rounds and shake counts required a modal. | The exact original choices are direct, checkable radio controls with at least 48dp targets, wrapping instead of shrinking. A value tap changes the actual task draft while retaining the live controls and keyboard focus. Other advanced-value pickers are retained. |
| Long advanced-task descriptions preceded the controls. | Long descriptions have a concise task summary and a genuine **About this task → How it works** page. The complete original description is retained exactly and its route survives recreation. Existing supporting disclosures, permission/error rows and working preview actions remain. |
| Save routine returned to the alarm draft with no indication that another save was required. | A concise footer says **Save alarm applies this routine**. Task footers say whether Use adds or updates the routine draft. Both levels have a visible, real Cancel action. Save routine still changes only the alarm draft; Save alarm retains the existing durable validation/save path. |
| Cancelling a routine could leave an edited screen-pinning value in the enclosing alarm draft. | The routine-entry pin value is captured and saved across recreation. Routine Cancel/Back restores it along with the unsaved routine. Save routine deliberately retains it. No runtime pinning or preference write is introduced. |

All 28 task types, difficulty/round/step/journal ranges, random-pool behavior, QR/picture/object enrollment, fallbacks, errors and working previews are preserved. The original task-description switch and every task numeric choice array compare exactly with the base. `Alarm`, `ChallengeRoutine`, runtime/service/scheduler implementations, Store, Backup, manifest, resources, assets and build/version metadata are untouched. Only AlarmEditor challenge/catalog/task UI routes and state, `ChallengeRoutineUi`, and the new UI-only `ChallengeTaskUi` change production code.

## Prepared native continuation

Exact existing selector: `org.sleepwell.app.ChallengeRoutineFlowTest#orderedTasksConfigureReorderCancelRecreateAndPersistWithoutOutputs`.

The same method keeps its private UUID store, disabled alarms, fake scheduling, original preference checks, unrelated-alarm preservation, malformed-backup checks, real drag/reorder and only-Save-alarm durability assertions. It now uses the real direct radio controls and prepares search/empty/recreation, About/Back, task Cancel and routine/pinning Cancel assertions. Captures wait for the focused, attached, laid-out owned window and a committed hardware frame; full visible dimensions and minimum 48dp bounds are asserted for relevant radio/footer/search controls. It does not open a physical sensor, microphone, camera, actual alarm owner or output service.

Exact **18 literal PNGs** for that one method, in generation order:

```text
routine-legacy.png
routine-catalog-empty.png
routine-catalog-search.png
routine-task-about.png
routine-task-values.png
routine-ordered.png
routine-reordered.png
routine-recreated.png
routine-removed.png
routine-reopened.png
challenge-math-initial.png
challenge-math-selected.png
challenge-math-wrong.png
challenge-math-recreated.png
challenge-math-completed.png
challenge-math-typed-wrong.png
challenge-math-typed-recreated.png
challenge-math-typed-completed.png
```

The eight Math-practice frames and existing owner/focus/lifecycle fences are retained unchanged through `WakeMathPracticeFixture.verify`. They remain private Practice proof, not actual AlarmActivity admission, advancement, dismissal or output verification.

Authorized minimal maintenance also updates the existing `GradedMathConfigurationTest`, `AlarmMenusFlowTest` and `TypedWakeFlowTest` selectors/actions from obsolete round-modal assumptions to direct checked choices and the actual More-task catalog/Back route. Their existing save/cancel/recreation/backup assertions and capture names remain. No `@Test` is added, and these three maintained methods have **not been executed** in this increment. Non-round/shake advanced pickers retain their existing modal routes.

Production and instrumentation compilation passed in **5m53s**, 25 tasks / 3 executed / 22 up to date, after the native maintenance. The final production-only incremental compile for the last catalog keyboard-hide calls also **passed in 1m57s**, 15 tasks / 1 executed / 14 up to date. No test execution, CI dispatch, lint campaign, phone/device operation, install, release or frozen Root mutation was performed.

## Still open

Fresh native visual/action/persistence evidence is required before accepting the changed layout, all row/footer bounds, search interaction and Cancel behavior. The larger routine catalog and all 28 detailed task paths are not claimed as independently exercised by the one method. Device/font-scale/RTL/TalkBack and actual alarm/hardware boundaries remain open.

**Board 04's whole-routine Preview remains a functional gap.** Existing individual previews are retained; no placeholder routine-preview button is introduced. This requires a separate safe runtime scope. Screen 10 and all 36 reference screens are not marked complete by this source increment.
