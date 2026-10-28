# Contactless setup: actionable route and microphone status

[CI 40638613782](https://github.com/omeyerrato/SleepWell/actions/runs/40638613782) **passed in 40.403s**, head `175f34227571347b44220849c73d97add5f513e6`: exactly one existing method and eleven original PNGs. Scope `contactless-status`; selector `org.sleepwell.app.ContactlessSetupFlowTest#explicitRealDspFakePcmQualificationSaveAndReopenNoAutostart`. Both optional output flags were false, host audio disabled and `full_app_review` false. No real microphone/speaker was opened.

All eleven passing originals and both first-attempt failure PNGs were opened individually against [board 06 middle](../../../design-concepts/2026-10-22-phone-app/06-tracking.png) and the [previous setup evidence](../2026-10-28-sensing-reference/README.md). Original bytes, raw transcripts, environment and summaries are unchanged. **Content/status/footer fit is verified; complete visual acceptance is not:** the reopened original has mixed dark clock/cellular/battery glyphs on plum. The recreated original is light. This visible limitation is retained below.

## Delivered source and exact boundary

Separate commits: pinned icon `8ecb9bf8`, production `1e5b6dc0`, native continuation `4b51db05`, verification scope `58c5f4e7`, observed layout repair `175f3422`. The isolated branch starts at frozen `08d3911e`; this archive does not claim installation, publication or inclusion in release 0.1.4.

The new Headphones row uses the same real SonarCapabilities.external output enumeration as admission. It reports Not connected, External audio connected or Not checked, with supporting copy explaining that all external audio must be disconnected. This is availability, not proof of the active capture route. Microphone Allowed describes RECORD_AUDIO permission only, not a verified microphone or calibration. Each real button/keyboard-accessible row opens the corresponding sound/app settings or permission action. No decorative/inert chevron was added.

The setup access adapter only observes device inventory and opens explicit permission/settings UI; it cannot allocate audio or change routing, volume, Bluetooth or a permission. The existing SonarAudio still verifies actual built-in speaker/microphone before emission and monitors capture routing. New resumed device observation invalidates a completed calibration and cancels an owned test when devices change. Initial enumeration is distinguished from a change. Watchers close on pause/destroy; generation and live-owner checks reject stale callbacks. Observer failure blocks admission. Permission requests deduplicate and retain pending/asked draft state across recreation. Permission result and Settings return refresh status while leaving Use disabled until a new explicit Test.

The same first native method replaces PCM I/O, capability facts, device watches and permission/settings requests with private fakes. It uses real DSP, SonarRuntime, Activity, View actions and accessibility gain update. The settings fake records which real row dispatched it, then opens a real same-task ContactlessSetupActivity child; this proves actual parent pause/resume and watcher cleanup without launching Android Settings. The permission fake records requests and supplies disclosed denied/granted capability facts through the real Activity result handler; it does not execute PermissionController or change an OS permission.

Before the first test, pending-request duplicate input, recreation, a stale old-owner result, denial, denied-status settings return, allowed-status settings and Headphones settings all leave fake PCM opens at zero and private preferences empty. The settings adapter receives two microphone visits and one sound visit. Unknown route is rendered Not checked. The completed first DSP test qualifies Use; a fake external-device event disables it, and a blocked Test cannot reopen PCM. Disconnecting does not restore qualification. Recreation rejects a retained old watch. Existing explicit retest, restored More/gain dialog, actual gain invalidation, retest, Use/save, reopen and stopped recreation remain. Exactly three fake PCM opens occur, matching the three explicit tests. The private saved profile retains gain 7%; reopening/recreation never starts capture or remains qualified.

All captures require full status-row, Back/More and footer bounds with 48dp action targets, a focused laid-out owner and committed frame. Existing aggregate light-system-bar checks remain, including a saved-PNG check; their mixed-glyph limitation is explicit below. Every owned Activity, fake device watcher, runtime and save worker closes/drains before factories restore or private preferences are deleted. Global preferences remain unchanged and no alarm/tracking service runs. No new @Test was added; the other two method bodies remain unchanged, while shared fixture/capture safety helpers gained the fake access port and bounds checks.

## Each passing original inspected

All names below have the `sonar-setup-` prefix.

| Original suffix | Actual observation |
| --- | --- |
| `initial.png` | Empty real signal frame, correct headphones/microphone glyphs and statuses, disabled Use; both rows clear of full footer. |
| `access-denied.png` | Microphone Not allowed, review message, no bars/qualification; both actions fit. Denial is injected. |
| `access-requesting.png` | Requesting access and disabled access rows/Test; explanation wraps without clipping the rows or footer. The secondary caption Microphone settings is tight but visible at this size. |
| `access-returned.png` | Permission-only Allowed after the inert settings child returns, empty plot and disabled Use; explicit Test instruction remains. |
| `ready.png` | Real DSP output from generated PCM, green check, qualified state and enabled Use; full status rows/footer. |
| `route-blocked.png` | External audio connected, disconnect instruction, no check/qualification and disabled Use. Previous measured bars remain labelled Not testing. |
| `route-returned.png` | Not connected again, explicit Test again message, prior measured bars and disabled Use. Disconnecting does not manufacture a new qualification. |
| `gain-changed.png` | Real gain update requires retest; empty plot and disabled Use, full rows/footer. |
| `retested.png` | Real fake-PCM DSP restores qualified bars/check/enabled Use at the changed gain. |
| `reopened.png` | Saved gain/profile reopen stopped, empty plot and disabled Use. **Clock, cellular and battery are dark on plum; Wi-Fi and navigation are light.** |
| `recreated.png` | Recreated owner remains stopped/unqualified with full rows/footer; status glyphs and navigation are light. |

The correct pinned Phosphor headphones glyph comes from the already-used official commit `2b75f3ad12b420c9504ef05df8d2564a28f8500e`, SVG SHA256 `992d04ff6759f1f12d40259298a92589c18556bf5b2ea6f82bc23e74d9c48185`. Its original path geometry converts exactly to the Android vector; existing aliases/source hashes and packaged MIT license remain unchanged.

## First actual failure and production correction

[CI 40637691414](https://github.com/omeyerrato/SleepWell/actions/runs/40637691414), head `58c5f4e7`, **failed in 7.776s**, before any required acceptance PNG or PCM test. Its method-specific and generic diagnostic PNGs, XML aliases, diagnostic/raw method logs, environment, unmodified failed summary and run metadata are retained under `failed-attempt/`.

The first native bounds assertion observed a 126px Microphone row with only 105px visible. The XML places its visible row at y1949-2054, above the fixed footer beginning y2080; the 21px loss was a real body-viewport clip. The original plot was 140dp. Source-only `175f3422` reduces it to 112dp, closer to the reference's chart proportions, and reduces the intro gap from 16 to 12dp. It changes no plot data, 48dp targets, footer, image, engine or selection semantics. Compile passed in 34s. The corrected run retained every assertion and all eleven names; it was not an unchanged retry.

## Remaining differences and limits

The reopened image is **not accepted for complete system-bar fidelity**. The aggregate brightBars gate requires only 24 bright pixels across each bar region; light Wi-Fi/notification pixels can satisfy it while clock/cellular/battery remain dark. The Activity's requested appearance flags and that aggregate saved-PNG gate passed. These originals establish the visual mismatch, without proving whether an unsettled SystemUI frame or a production appearance bug caused it. No pixels were corrected, no assertion relaxed and no unrelated shared Design change was made.

Art crop, spacing/type, truthful echo-change/relative-signal labels, real generated-PCM plot shape, warning/access copy, current footer captions and available supporting options still differ from the illustrative board. Existing measured signal stays displayed after a route change while Not testing/Use disabled prevent it from being presented as newly qualified. Larger fonts, every display size and physical placement/sensitivity remain open.

Native methods for interrupted/busy/failed saves and the wider prerequisite-error matrix were not run in this one-method scope. Actual Android permission/settings screens, real device callbacks, connected Bluetooth/wired/USB route behavior, hardware support, microphone privacy toggles, charging interruption during capture, audio focus and audible output were not exercised. Source inspection preserves existing engine admission; it is not a hardware result. Full screen 17, overnight runtime and all 36 screens are not declared complete.

`source-equivalence.json` pins ten named source/native/asset files at the verified head and separate commits, and six unchanged engine/schema/setup-chart files against frozen 08d. Reversing only the new scope restores the entire prior Python AST and workflow bytes, preserving all existing selectors, manifests and defaults. SHA256SUMS covers every accompanying file, including the failed evidence. The earlier archive remains untouched.
