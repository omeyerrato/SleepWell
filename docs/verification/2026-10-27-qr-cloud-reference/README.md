# QR fit and cloud destination: bounded native evidence

Two existing focused methods **passed** at
`170b0e44785c52429f7b4c089e234ca9d18ce102` in
[CI 40557496255](https://github.com/omeyerrato/SleepWell/actions/runs/40557496255):

| Focused method | Native result |
| --- | --- |
| [QrCodeConfigurationTest#enrollmentPermissionFallbackRecreationPersistenceAndStrictRestoreRemainInert](QrCodeConfigurationTest%23enrollmentPermissionFallbackRecreationPersistenceAndStrictRestoreRemainInert.txt) | One method passed in **30.523 seconds**, thirteen original frames. |
| [CloudBackupFlowTest#chooseSaveReopenAndReviewKeepLocalDataWithNoUpload](CloudBackupFlowTest%23chooseSaveReopenAndReviewKeepLocalDataWithNoUpload.txt) | One method passed in **7.107 seconds**, five original frames. |

**The overall CI job failed** in PrivacyHistoryFlowTest; its pending and done
frames are missing. The other four selected flows passed. The unaltered
[combined focused summary](focused-review-summary.json) records all five results
and the failure. This archive does not claim an overall passing job.

[The original environment](environment.json) pins the API 36 x86_64 disposable
emulator at 1080×2316 override and density 420. These frames use default text size.
Host audio and audio imports are disabled; no native playback is allowlisted.
QR uses injected camera/access and fake NFC-reader ports. Cloud uses injected
folder selection/validation, an in-memory document provider and private SQLite.
No real hardware, OS permission controller, storage provider, remote upload,
physical phone or alarm output was exercised.

All eighteen original PNGs were opened individually and compared with board 04's
middle Add a morning code frame and board 12's middle Data & privacy styling and
cloud entry. Cloud destination/history/review screens are supporting app states
not directly pictured on that board; no exact board parity is claimed for them.

| QR original | Observed result |
| --- | --- |
| [Permission](qr-code-permission.png) | Generate printable code is now a direct text action beside Location name. The camera-off panel contains the Allow camera action. Name, fallback and Save remain legible. |
| [Invalid](qr-code-invalid.png) | Location validation sits above the complete Save code action without hiding the fallback subtitle. |
| [Denied](qr-code-denied.png) | Denial copy remains visible and the draft is editable; the access action stays in the inactive camera panel. |
| [Access error](qr-code-access-error.png) | Settings-access error and retry access action are clear without a separate action row displacing fallback. |
| [Scanning](qr-code-scanning.png) | The full scan area and reticle remain unobscured by buttons while scanning. The hint now reads `Point the camera at your QR code.` The blank preview is the fake camera, not a fabricated bathroom photograph. |
| [Camera error](qr-code-camera-error.png) | Scan code retry appears inside the inactive panel and the error stays above Save. |
| [Captured](qr-code-captured.png) | The green capture status, entire Medium fallback subtitle and full Save code fit together. The new native geometry gate passes before this screenshot. |
| [Fallback](qr-code-fallback.png) | Hard, Division and Type answer are selected, with complete operator card borders and Use fallback. |
| [Fallback error](qr-code-fallback-error.png) | Last-operator validation remains visible; Division stays selected. |
| [Recreated](qr-code-recreated.png) | Bathroom, capture and Hard fallback survive recreation with the compact print/scan layout intact. |
| [Routine](qr-code-routine.png) | The real QR task appears as the second task with Bathroom, beside the retained Math task. Save routine remains available. |
| [Reopened](qr-code-reopened.png) | The saved Bathroom code and Hard fallback survive save/reopen. |
| [Unavailable](qr-code-unavailable.png) | The disabled Scan code, unavailable-camera explanation, complete fallback row and Save all fit. The full native geometry gate passes despite the added status copy. |

The tested QR change (`170b0e44`, original source `e0f8b3fe`) preserves the
**228dp camera panel**, moves the scan/access action into its inactive area and
retains direct printable-code navigation. Before captured and unavailable PNGs,
the existing method asserts the full measured fallback row and Save button are
visible, the fallback bottom is above Save, and print/scan/fallback/Save targets
each retain complete dimensions of at least **48dp**. Scanning shows no action
overlay. These bounded checks close the duplicate hint and default-size fallback
overflow recorded in the earlier QR archive.

The same journey passes permission/access errors, pending-request cancellation,
late-grant suppression, stale and current camera callbacks, fallback recreation,
routine/alarm persistence and strict malformed restore validation. Synthetic QR
payload is decoded then stored as a digest, another alarm and shared preferences
are preserved, and alarms/tracking remain inactive. Fake NFC callbacks remain
bound to their current dialog/owner. Printable generation/printing, Barcode
selection, real camera/NFC operation and active challenge runtime are outside
this selected method; visibility does not certify those callbacks.

| Cloud original | Observed result |
| --- | --- |
| [Folder busy](cloud-backup-folder-busy.png) | Checking folder access survives recreation; destination remains unselected. Native assertions show Choose destination and Save are disabled while validation is held, even though their button surfaces retain the usual colors. |
| [Destination](cloud-backup-destination.png) | The private test folder is remembered and automatic backup remains a separate, off-by-default option. All three fixed actions fit; some lower explanatory text needs ordinary body scrolling. |
| [Saved](cloud-backup-saved.png) | One read-back-verified fixture version appears with Review. Actual version time/size remain data, and the complete Save/Change/Disconnect footer is visible. |
| [Reopened](cloud-backup-reopened.png) | The destination, verified version, Review and controls survive closing/reopening without repeating folder selection or creation. |
| [Restore review](cloud-backup-restore-review.png) | Backup review shows one alarm and zero sleep/sound records, with complete Review replacement and Keep local data actions. No replacement occurs. |

The Cloud method holds injected folder validation, confirms it owns application
context rather than a destroyed Activity, recreates the screen and checks both
actions remain disabled until completion. Folder persistence and selection each
occur once. One explicitly requested private JSON version is created and verified
by reading it back through the in-memory provider. Reopening does not repeat
creation. Review opens the owned Backup screen, Keep local data cancels it,
restore count stays zero and the private database plus real app preferences are
unchanged. Workers finish before fixture cleanup. Automatic delivery, real
provider account/permission behavior, OS document picker UI, remote upload and
audio-folder backup are not verified by this method.

No introduced core layout regression was observed at this runtime. Error/large
text content and lower Cloud explanatory copy may require ordinary scrolling.
Later Barcode and printable-generation features intentionally add controls to
the illustrative QR board, while the fake preview preserves honest state. This
is bounded changed-flow verification, not full QR/cloud coverage or all 36
reference-screen fidelity. No source or test was changed for this archive.

The eighteen PNGs, both exact logs, environment and original combined summary
were copied from the run artifact and SHA-256 compared. [The manifest](original-sha256.json)
records all twenty-two byte-identical originals; folder-local Git attributes
preserve original bytes without newline normalization.
