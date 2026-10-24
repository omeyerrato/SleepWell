# Nap reference corrections after CI 40058852109

The actual 15 Nap flow images and four partial integration images were opened individually, together with board 09's middle Nap reference. The original scene set does not pass the complete visual/behavior gate.

| Actual original files | Observation |
| --- | --- |
| `nap-default`, `nap-preset`, `nap-tracking-required`, `nap-sound` | The Sound/Delete aliases fell back to a question-mark icon. The duration/preset/selection state and real decoded tone waveform remain intact. |
| `nap-pending`, `nap-recreated`, `nap-reopened`, `nap-scheduled`, `nap-retry`, `nap-cancelled` | The same wrong icons persist. Saved selected tone/duration and disabled running controls are visible. These images alone do not verify actual delivery. |
| `nap-permission-error`, `nap-scheduler-error` | The fixed error/action footer leaves the Delete setting's subtitle/switch partly below the visible content viewport. |
| `nap-schedule`, `nap-schedule-error`, `nap-list` | Supporting date/error/list screens have readable controls in this captured size. They are not a substitute for the full reachable integration journey. |
| `nap-integration-future` | Future date, hero, arc and initial start card agree. |
| `nap-integration-clock-shift` | Hero/arc move to 12:20 AM/12:00 AM while the start card remains 9:00 PM. |
| `nap-integration-zone` | Rebuilding for the new zone restores agreement; it did not cover the same-occurrence update defect. |
| `nap-integration-wind-down` | The future date/start/duration route is visible. The subsequent integration assertions fail, so the whole journey is not accepted. |

All names in the table have the `.png` extension and are originals under `captures/ci-40058852109/silent-android-review`; no images are modified or synthesized for this review.

## Source correction

Nap uses the existing pinned `sound` and `trash` Phosphor assets. No icon fallback or made-up waveform is added. The waveform continues to come from the selected bundled tone through `ToneWaveform.read`.

When an error increases the fixed footer, the actual app scroll viewport exposes its bottom setting/status range after layout. Its scroll indicator/fading edge remain available. The native screenshot gate checks the entire Delete row, its switch and its position above the footer in the first committed frame. The harness does not scroll the row into place before checking it. New actual images must establish the resulting composition.

Tonight's start-card value and Nap status subscribe to the same `NightWindow` callback as its hero and curve. Initial binding also uses that frame; later wall-clock projection changes cannot update only the curve. The native integration assertion compares the start card to the model's projected interval before and after a wall shift and after a zone change. Ordinary rest-window behavior remains unchanged; no Main or NightArc change is required.

## Failure diagnosis and test boundary

The old integration method was all on line 34. Its raw result is an `assertFalse` failure after 8.677 seconds and only four captured images. The earlier weekday assertion is excluded by those completed captures. Cancellation and the subsequent inactive-enabling assertions remain candidates; the old log does not uniquely prove which one failed. The source cancellation engine persists synchronously and the same private engine cancellation passes its other fixture, so window/input readiness is an inference to investigate, not a claimed observed engine defect.

The journey is split into explicit statements, and actual taps require the current focused window plus an enabled, fully visible clickable target. The test waits for a real saved CANCELLED/PENDING transition instead of assuming an accessibility-node tap already committed its action. Existing disabled/inactive/nonce/deadline/unrelated-data assertions remain. There are no arbitrary delay steps, no injected View clicks, and no weakened success assertion.

Native contracts remain `NapFlowTest` (3 methods/15 images) and `NapIntegrationFlowTest` (1 method/8 images). All scheduling/playback/clock boundaries stay fake and private. No AlarmActivity, real alarm, phone, sensor or physical output is invoked. Compilation and pure-test results must be reported separately from fresh native execution and visual acceptance.

Local bounded verification: session 70055 completed debug APK, instrumentation APK, 32 focused pure tests (NapEngine16 + NapWindow6 + NightWindow10) and lint (0 errors/87 warnings) in 4m43s. A final same-observer removal/attachment guard was added after this build and is reserved for the parent combined exact-commit build. Native execution of the updated 4-method/23-image contract and new visual acceptance remain pending.
