# QR configuration and reviewed backup: scoped native evidence

Two existing focused methods **passed** at
`bfc27151a5e3c0e191589ca0b9fd296aff0fcc56` in
[CI 40546119570](https://github.com/omeyerrato/SleepWell/actions/runs/40546119570):

| Focused method | Result |
| --- | --- |
| [QrCodeConfigurationTest#enrollmentPermissionFallbackRecreationPersistenceAndStrictRestoreRemainInert](QrCodeConfigurationTest%23enrollmentPermissionFallbackRecreationPersistenceAndStrictRestoreRemainInert.txt) | One method passed in **40.108 seconds**, thirteen original frames. |
| [BackupDocumentFlowTest#explicitReviewedReplacementPersistsAcrossRotationAndReopen](BackupDocumentFlowTest%23explicitReviewedReplacementPersistsAcrossRotationAndReopen.txt) | One method passed in **12.502 seconds**, eight original frames. |

**The overall CI job failed.** Gentle wake settings, Health Connect and Privacy
history failed separately. The unmodified [combined focused summary](focused-review-summary.json)
retains those failures and missing images. This archive does not turn that job
into a passing full-app review.

[The original environment](environment.json) pins an API 36 x86_64 disposable
emulator at 1080×2316 override and density 420. These frames use default text size.
Host audio output and audio imports are disabled; no native playback is
allowlisted. The QR fixture uses fake camera permission/session and NFC-reader
ports; the backup fixture uses fake document transport and scheduling effects
with private SQLite. No real camera, NFC reader, document provider, personal data,
physical phone or alarm output was exercised by these two methods.

All twenty-one original PNGs were opened individually. QR frames were compared
with board 04's middle Add a morning code frame (the routine frame also supplies
context); backup frames were compared with board 12's middle Data & privacy
palette, typography, rows and backup actions. The supplied boards do not contain
the backup review/confirmation/progress screens, so these supporting states are
not claimed as exact board matches.

| QR original | Observed result |
| --- | --- |
| [Permission](qr-code-permission.png) | The printable-code title and supporting copy now stack in one column, with a clear icon/chevron lane (`db47f62e`). Camera-off explanation, location field, fallback row and Save fit. |
| [Invalid](qr-code-invalid.png) | Empty location validation is legible above the full Save code button. |
| [Denied](qr-code-denied.png) | Camera-denial explanation is visible and the draft remains editable. |
| [Access error](qr-code-access-error.png) | Settings-access failure is shown without losing the field, fallback or Save action. |
| [Scanning](qr-code-scanning.png) | Scan framing is visible over the intentionally empty fake camera surface. A copy defect remains: the hint reads `Point the camera at your QR code code.` |
| [Camera error](qr-code-camera-error.png) | The camera-unavailable error and Scan code retry action are clear. |
| [Captured](qr-code-captured.png) | Code-ready and green Code captured states appear without a fabricated camera image. At Medium fallback, the subtitle wraps and its last line reaches below the fixed Save footer boundary; the full row requires body scrolling. |
| [Fallback](qr-code-fallback.png) | Hard, Division and Type answer are selected. Operator cards have complete aligned borders; Use fallback is visible. |
| [Fallback error](qr-code-fallback-error.png) | Attempting to clear the last operator leaves Division selected and shows its validation above Use fallback. |
| [Recreated](qr-code-recreated.png) | Bathroom, captured code and Hard fallback survive recreation. Its shorter Hard value allows the subtitle to fit on one line above Save. |
| [Routine](qr-code-routine.png) | The second QR task appears with Bathroom beneath its title, alongside the original Math task and complete Save routine action. |
| [Reopened](qr-code-reopened.png) | The committed Bathroom code and Hard fallback survive alarm save/reopen. |
| [Unavailable](qr-code-unavailable.png) | Existing code remains captured and Save is available while scanning is disabled with No supported camera is available. The extra status pushes part of the fallback row into the scroll boundary. |

The passed QR journey checks real owned UI navigation through More task types,
location and scan validation, denied/failed permission requests, cancellation of
a pending request while in fallback, stale versus replacement camera callbacks,
fallback draft recreation, routine/alarm persistence and strict backup validation.
It decodes a synthetic QR image, persists its digest rather than raw payload,
preserves another alarm and app preferences, and leaves alarms/tracking inactive.
Its fake NFC-reader checks reject a cancelled queued tag and an old reader callback,
then accept the current reader only after the correct owner regains focus. This
is configuration evidence, not real camera/NFC interoperability or challenge
runtime verification. Printable generation/printing and Barcode selection are
visible features outside this selected journey.

| Backup original | Observed result |
| --- | --- |
| [Review](backup-document-review.png) | JSON scope, excluded audio/settings and actual fixture counts are clear; both Review replacement and Keep local data fit. |
| [Review recreated](backup-document-review-recreated.png) | The same review survives recreation without applying replacement. |
| [Review reopened](backup-document-review-reopened.png) | Review state and both actions survive closing and reopening. |
| [Confirmation](backup-document-confirmation.png) | Replace local data? explains replacement, queued audio deletion and disabled imported alarms. Both explicit Replace and Keep actions are complete. |
| [Restoring](backup-document-restoring.png) | Replacing local data and the non-cancellable progress explanation are legible without a misleading cancel action. |
| [Restoring recreated](backup-document-restoring-recreated.png) | The same active replacement state survives recreation. |
| [Restored](backup-document-restored.png) | Completion copy says to review and enable alarms. Portable settings, sleep CSV entry and all three export/import footer actions fit. |
| [Result reopened](backup-document-result-reopened.png) | Completion state and navigation/actions remain visible after reopening. |

The backup method uses a real owned confirmation action. Cancelling preserves
review and a deliberately stale cancelled-button callback cannot commit it.
Recreation/reopening preserves the pending review. A later explicit confirmation
performs one private-database replacement, survives recreation while held in
progress, retains sound labels, disables/cancels the imported nap and clears the
fixture's retired audio reference/file. Completion survives reopening. The
fixture verifies production database, app preferences and files outside its owned
fixture remain unchanged. It does not certify real storage providers, export/CSV
callbacks, malformed/stale imports, or the parent Data & privacy screen; other
backup methods cover separate work and were not selected here.

The stacked printable-code row is verified at this runtime. The duplicated QR
hint and fallback-row overflow above remain open visual issues, reported for
coordination; no source changes were made in this archive. Functional passes do
not close full screen-11 reference fidelity, full backup behavior, large text,
other locales, hardware/provider integration, or all 36 reference screens.

All thirteen QR PNGs, eight backup PNGs, two exact instrumentation logs,
environment and original combined summary were copied directly from this run's
artifact. [The SHA-256 manifest](original-sha256.json) records all twenty-five
byte-identical originals. Folder-local Git attributes preserve their original
bytes without newline normalization.
