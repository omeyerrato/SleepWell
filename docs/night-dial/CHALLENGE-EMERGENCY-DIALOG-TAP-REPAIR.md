# Emergency options native tap repair — scoped rerun pending

2026-11-01. Failed run `41260557207`, source
`1c1fe0c16ce35abbf8f64dcb17b29064530e711c`, scope `draft-challenge-reference`.
The complete per-method instrumentation text reports one failure after 6.374s:
`RoutinePracticeFixture.java:318` dereferenced a null Dialog while waiting for the
changed-default-dialer rejection. Only the initial and recreated Emergency frames
were produced. The actual failure PNG/XML show ordinary undimmed Practice with
no Emergency sheet. This is an earlier failed receipt, not current acceptance.
Wake-check Practice passed its separate method; Random's failure has another owner.

The fixture's `emergencyTap` verified whole target bounds, then passed the rectangle
from `getGlobalVisibleRect` directly to `UiDevice.click`. Those coordinates are in
the root view's space. A bottom Dialog root has a nonzero origin on the screen;
its local action center can hit the underlying screen/outside the sheet. That
explains the dismissal and subsequent null lookup without establishing a bypass
of the production changed-recipient guard.

The native helper retains the same owned, focused, attached, laid-out sheet and
complete 48dp visibility checks, then obtains the action's screen origin with
`getLocationOnScreen` before the real touch. It does not performClick the action,
change the dialer port, bypass focus, disable animation, add sleeps, or relax the
recipient assertion. The changed-recipient case now also requires the exact same
reviewed sheet to stay open with its error and no injected handoff. Unexpected
closure produces a meaningful assertion instead of an incidental NPE. All six
Emergency and two Tilt original frames and all intent/fallback/focus assertions
remain required by `CHALLENGE-EMERGENCY-FOCUS-NATIVE-PLAN.json`.

Both initial and recreated originals were visually read. A misleading tool preview
made the second look black; direct original pixel reads showed the same opaque
Night action/sheet colors, and SHA-256 proves the two PNGs are byte-identical.
There is no evidence here for a black production render, and this repair takes no
credit for a visual change. Production Emergency/dialog/dialer code is unchanged.
No claim of runtime repair completion is made before the focused rerun.

Original artifact identities (retained locally under the failed run):

| Actual artifact | SHA-256 |
| --- | --- |
| `ChallengeRoutineFlowTest#orderedTasksConfigureReorderCancelRecreateAndPersistWithoutOutputs.txt` | `374774bcb656a09b4ea32cea29d1be8406af4349b948cb1ae20bb460a62083c3` |
| `focused-review-summary.json` | `b1a586e4b41fa21ca920f0f078c6013ff647547ae14be85392bd8a0808b8390e` |
| `routine-preview-emergency-open.png` | `3a76b94a60fbc86adeafe04b818f72b5be4a6ac5637480a23cc6fce265fbcfff` |
| `routine-preview-emergency-recreated.png` | `3a76b94a60fbc86adeafe04b818f72b5be4a6ac5637480a23cc6fce265fbcfff` |
| `routine-preview-failure.png` | `a94e60cbf77740010e63de57829c44680d131d9e0ba43edd69fd3f1ef76f6005` |
| `routine-preview-failure.xml` | `fc35bbb75bef9dacbd8fe5b637d1449d3c2bf9f86551c8cf727542c972efae63` |

Local validation: diff and unchanged finite capture/proof names only. No build,
emulator, hosted CI, unit campaign, phone, dialer, alarm, audio or model action ran
here. Root owns the same existing method with `routineSlice=emergency`, actual
originals and proof inspection. Historical failures are preserved.
