# Contactless setup: settled individual SystemUI glyphs

[CI 40641070813](https://github.com/omeyerrato/SleepWell/actions/runs/40641070813) **passed in 58.348s**, head `bf513ca4c864305eeb62677a3994fa0206ca2d75`: one existing ContactlessSetup method and the same eleven original names. Scope `contactless-status`; selector `org.sleepwell.app.ContactlessSetupFlowTest#explicitRealDspFakePcmQualificationSaveAndReopenNoAutostart`. Both optional flags were false, host output disabled, `full_app_review` false and every capture/access/route port remained private/fake.

All eleven originals were opened individually against [board 06 middle](../../../design-concepts/2026-10-22-phone-app/06-tracking.png). They show full status rows/footer and light clock, Wi-Fi, cellular, battery, visible notification and navigation, including reopened/recreated. Original PNG, glyph report, method transcript, environment and summary bytes are unchanged. The [prior functional archive](../2026-10-28-contactless-status/README.md), including its original mixed-glyph reopened image and first real layout failure, remains unchanged. No old capture is relabelled or duplicated here.

## Native-only correction and actual evidence

Only native capture/observation helpers changed in `bf513ca4`; the entire production tree, workflow/script, all three native method bodies and eleven literal names equal the prior functional head `175f3422`. Production code, appearance flags and UI actions are not modified by the gate. Compilation passed in 33s.

The earlier aggregate brightness check could accept white Wi-Fi while clock/mobile/battery were dark. The replacement collects the actual visible SystemUI bounds for clock, wifi_signal, mobile_signal, battery and any notification ImageViews. Each region must have at least 24 pixels with all channels above180 and more than twice as many bright pixels as near-black pixels (all channels20 or less). Plum background cannot supply bright pixels. Two consecutive complete compositor samples must also retain the same focused live Activity with light-glyph appearance flags; existing plum/navigation and full control bounds checks remain. This is a bounded state wait, with no fixed sleep, replayed action, pixel editing, source flag manipulation or hidden window dismissal. The one subsequently saved original is independently checked against every measured glyph region; a bad saved frame fails and is preserved.

The raw `sonar-setup-glyphs-...txt` has eleven settled and eleven saved lines. Every capture completed after two samples. The native windows report focused=true, destroyed=false, finishing=false, appearance=0 and legacyUi=0. Every saved glyph region reports zero near-black pixels. Reopened specifically records clock576, Wi-Fi680, mobile527, battery541 and notification303 bright pixels, then independently records those same light counts in its saved original. Metrics identify each actual region and owner; no isolated Wi-Fi pixel can qualify another glyph.

The stronger gate and new originals support a settled capture/handoff explanation and reveal no persistent wrong production flags in this run. They **do not conclusively establish the earlier mismatch's cause**: this run's two samples were already complete, so it did not observe a mixed-to-light transition. No production repair is justified by these results alone. This closes the bounded settled capture gap for these eleven originals, not every future transition or every device.

## Each original inspected

All names below have the `sonar-setup-` prefix; all have light SystemUI glyphs/navigation and complete footer/status rows.

| Suffix | Visible state |
| --- | --- |
| `initial.png` | Not testing, blank measured-signal frame, Not connected/Allowed and disabled Use. |
| `access-denied.png` | Injected Not allowed with required-access message; no qualification. |
| `access-requesting.png` | Requesting access, disabled controls and full explanation. Microphone settings caption remains tight but visible. |
| `access-returned.png` | Allowed permission only, explicit Test instruction, empty plot and disabled Use. |
| `ready.png` | Real generated-PCM DSP bars, green qualification and enabled Use. |
| `route-blocked.png` | External audio connected, disconnect message and disabled Use; previous measured bars labelled Not testing. |
| `route-returned.png` | Not connected again with explicit retest instruction; no automatic qualification. |
| `gain-changed.png` | Real gain invalidation, blank graph and disabled Use. |
| `retested.png` | Actual DSP retest qualifies Use at the changed gain. |
| `reopened.png` | Stopped, empty plot, disabled Use, complete light clock/cellular/battery. |
| `recreated.png` | Still stopped/unqualified with light SystemUI and unchanged layout. |

The unchanged method retains the exact private save/gain7%/reopen assertions, zero capture on permission/settings-surrogate return, one pending request across recreation, stale-owner/result/watch rejection, route invalidation and three explicit fake PCM tests. It executes real Activity pause/resume through a same-task inert Contactless child. Android Settings and PermissionController themselves are not opened; no actual grant, wired/Bluetooth/USB device, microphone/speaker, Service or physical output is tested. All runtimes/watchers/save workers and owned Activities close before private cleanup/factory restoration; global preferences remain unchanged.

Art crop/type/spacing, warning copy, relative echo-change labels, generated signal shape, footer captions and every font/display size still differ from or remain outside the illustrative reference. No overnight/hardware accuracy, full screen17 or all36 completion is claimed. Root/release0.1.4 remain outside this isolated branch. `source-equivalence.json` pins exact source/native/asset bytes and unchanged production/metadata/method boundaries; SHA256SUMS covers all accompanying files.
