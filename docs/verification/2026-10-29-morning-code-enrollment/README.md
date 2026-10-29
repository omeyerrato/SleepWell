# Morning code and shared NFC enrollment

UI11, Board04 middle (`04-challenges.png`), with Board01/12 typography, spacing and local-data context. This bounded change exposes Code and NFC tabs in the existing morning-code enrollment screen, retains QR and supported barcode choices, and gives the existing shared NFC tag an explicit Read / Save flow. This archive concerns configuration only; it does not certify the complete 36-screen app or physical camera/NFC operation.

## Source and boundaries

Base `697d123202cf53c62b14b09eab4a6527bb13b1c9`. Production commits, in order:

- `2b87ce0ff4b90142de87d8069b5bdede6199cbb0`: retained Code/NFC drafts, tabs and enrollment scroll; dedicated NFC setup from the NFC task draft; shared-tag setup with explicit persistence.
- `e60209a3ac72e232891e841249c1a13f2929b2fd`: live-view enrollment ownership and NFC settings recovery.
- `585d8005b5dd73cd23367bad3f5674c4f4e62ed1`: compact NFC access/capture layout above the fixed Save action.

Only `QrCodeActivity`, new `MorningTagSetup`, and the NFC setup portion of `AlarmEditorActivity` change in production. The shared `nfcTag` preference remains shared by all NFC alarms. Read stages a draft; only the explicit Save commits it. Tab changes, permission results, recreation and stale reader/view callbacks do not enroll a tag. A changed shared tag rejects an older captured draft. Dedicated NFC setup returns to the same NFC task draft without converting it into a QR task.

`source-preflight.json` pins production tree `7c38b4ff1dfa254cbcf74dc93cd53e2b8b8382bb`, identical across all native attempts, and 13 unchanged model/codec/camera/session/NFC/runtime/preference guard files. Existing code identity, formats, hashing, Math fallback and strict Backup validation remain unchanged. Platform ports remain real by default. The native method injects private preferences, fake reader/settings/camera access and a UUID-private database; it never starts an alarm, tracking, physical camera, NFC settings or audio output.

The existing `QrCodeConfigurationTest#enrollmentPermissionFallbackRecreationPersistenceAndStrictRestoreRemainInert` remains one method. `scope-inventory.json` contains the exact 22 required originals (13 existing plus nine new), with both optional output flags false. The method timeout remains 180 seconds. Removing only the enrollment scope additions restores the workflow and script text to `a19499d2^` exactly; prior scope maps/defaults remain intact. `7c3c3477` is an inventory bridge for this isolated branch and can be skipped where those mappings already exist.

Local compilation used only `:app:compileDebugJavaWithJavac :app:compileDebugAndroidTestJavaWithJavac --offline --max-workers=2 --no-daemon`. Both compiles succeeded; the final initial-route fixture correction took 1m39s (one executed task, 24 up-to-date). No unit batch, release build, install or publication was performed.

## Failed attempts retained

All downloaded artifact files are preserved byte-for-byte in each attempt directory and compared with the private download before archiving. The failed attempts remain failed; their partial/diagnostic images do not count as 22-image acceptance.

1. [40829340570](https://github.com/omeyerrato/SleepWell/actions/runs/40829340570), `a19499d22df9551feface08734610a1df39c2377`, failed in 22.954s. The fixture used a root-local rectangle as screen input in the owned Code type dialog, selecting EAN-8 instead of Code 39. `d8f9b441` converts local visible rectangles with `getLocationOnScreen` while preserving full bounds, focus, actual frame and single-tap gates. One required image plus diagnostic PNG retained.
2. [40831542713](https://github.com/omeyerrato/SleepWell/actions/runs/40831542713), `d8f9b441092ed1e18f35a9399afa72214a5ecc34`, failed in 32.173s. Code 39 selection and the recreated chooser succeeded. After QR cancel, the catalog correctly retained its scrolled position; the fixture incorrectly required its offscreen heading to be visible. `05e7369a` checks the same focused editor, mounted catalog, exact captured query/scroll, then reopens the actual QR row. Three required images plus diagnostic PNG retained.
3. [40834521546](https://github.com/omeyerrato/SleepWell/actions/runs/40834521546), `05e7369a1e3306f3cb3ce55994707b85c0558a7d`, failed in 9.822s before any required image. The new initial owned tap used the four-scroll helper intended for the shorter enrollment menus and stopped before the QR catalog row. `db13b5da` restores the initial existing `UiScrollable.scrollIntoView` navigation, then applies the unchanged full-visible 48dp / focused window / committed frame / one actual tap gates. No swipe-loop or timeout increase, forced focus, position reset or retap was added. Diagnostic PNG retained.

All seven failed-attempt PNGs were individually opened. They show the permission form, EAN-8 wrong selection, restored Code 39 chooser, invalid-name error, retained catalog at Codes & tags, and the too-short initial scroll at Camera & light. The second attempt's permission screenshot has faint platform status icons; this is recorded as a SystemUI limitation, separate from app-content bounds.

## Final focused run

[40835499904](https://github.com/omeyerrato/SleepWell/actions/runs/40835499904), tested head `db13b5dae2630896991b5695e16ed2060a3dc4cd`, **passed in 58.924s**, status 0 / `OK (1 test)`. Both APK builds passed. Its exact summary reports one class, one method, 22 required originals and no failures. `originals/` preserves all 32 artifact files byte-for-byte, including the exact method output, environment, summary and 22 PNGs. `run.json` pins the terminal run identity. This final attempt changes no production source.

All 22 originals were individually opened against Board04 middle and the Board01/12 support boards. `inspection.json` records per-image SHA256, dimensions and distinct observations. Code/NFC tabs, location, format selector, captured status, complete fallback text and fixed Save fit. The scrolled Code 39 chooser retains its selected row and complete Cancel after recreation. Math operator borders, selected Division/typed answer, the last-operator warning and Use fit. Every NFC state keeps its actual access/read or recovery action and fixed Save/return action visible without overlap. The NFC draft and success state survive recreation; the dedicated setup and returned NFC task draft remain readable.

The same successful method completed the actual action/persistence assertions: canceled and stale NFC readers remain inert; detached Save is rejected; tab/recreation/permission navigation starts no capture; a changed shared tag rejects an old draft; explicit Save commits `313233` to the private shared-tag preference, and dedicated NFC setup commits `7071`. Dedicated setup returns to the same parent with a refreshed saved-tag summary and does not change the saved alarm or add a QR task. QR catalog cancel retains its exact owner/search/scroll. The code label/hash/Hard Division typed fallback survive explicit routine/alarm Save and reopening; strict malformed imports are rejected, raw code payload stays out of Backup, and the unrelated alarm remains byte-identical. Personal preferences, ringing, alarm/tracking state and all injected ports are checked/restored through cleanup. The scheduling port receives only the expected disabled alarm; no real scheduling occurs.

These checks use actual owned Android windows, committed hardware frames and fully visible 48dp action bounds before the new coordinate actions. They do not simulate a successful click on an offscreen/covered target. The private preference commit is real, while NFC bytes and camera/error callbacks are injected. The static reference's bathroom photo is intentionally absent from these inert-camera captures, and the Code tab includes the retained supported barcode choices.

The final permission image has a faint white platform navigation pill on cream; later captures show a darker pill. The earlier faint status icons and this navigation contrast remain an explicit broader SystemUI limitation. No app-content clipping or action overlap was observed in these 22 captures. Physical camera/NFC devices, Android settings behavior, font/theme/device variants, alarm execution and all 36 reference screens remain outside this result.

The unrelated push build `40835469969` was canceled; `incidental-push-build-canceled.json` preserves its terminal state. The older incidental push build `40834503293` had already completed before cancellation was requested. The final archive commit uses `[skip ci]`; no extra review is dispatched for documentation.

`SHA256SUMS` covers every archived file except itself. All 72 original artifact files across the four attempts were byte-compared with their private downloads; all 29 PNGs (22 required final originals and seven partial/diagnostic originals) were individually opened. The failed attempts are retained as failures.
