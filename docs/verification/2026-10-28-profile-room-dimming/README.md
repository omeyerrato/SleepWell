# Room dimming profile: bounded native API evidence

[CI40615701815](https://github.com/omeyerrato/SleepWell/actions/runs/40615701815), source `bc9bc9c28568e441b1fb44b656c77e8915d8698f`: **BackupDocumentFlowTest#explicitReviewedReplacementPersistsAcrossRotationAndReopen passed in 15.905 seconds, one existing method, eight original Backup PNGs.** The exact transcript reports status0 and `OK (1 test)`; the unchanged focused summary has no failures and `full_app_review: false`. The class's other two methods were not selected.

API36 disposable x86_64 emulator, 1080×2316 override, density420; host output disabled and optional playback/import tests off. All eight PNGs were opened individually. The images, method log, environment and summary are byte-for-byte copies; [original-sha256.json](original-sha256.json) records all 11 originals. Full guest/renderer logs remain private. Local attributes and SHA256SUMS preserve and identify the archived bytes.

## Existing records-backup UI, individually inspected

| Original | Visible result |
| --- | --- |
| [Review](backup-document-review.png) | Private fixture summary: one alarm, one sleep record, two sound estimates and one user label; app settings/audio excluded; full Review replacement and Keep local data. |
| [Confirmation](backup-document-confirmation.png) | Focused replacement dialog, complete consequences, separate full Replace local data and Keep local data actions. |
| [Review recreated](backup-document-review-recreated.png) | Review returns after cancellation and Activity recreation with the same summary and actions. |
| [Review reopened](backup-document-review-reopened.png) | Retained review and full actions after closing/reopening Backup. |
| [Restoring](backup-document-restoring.png) | Replacing local data status, progress indicator and uncancellable-step explanation; no replacement/cancel action offered. |
| [Restoring recreated](backup-document-restoring-recreated.png) | Same in-progress state after recreation; original bytes equal the preceding image. |
| [Restored](backup-document-restored.png) | Completed-data message, Portable settings and CSV routes, and full Export/Import/CSV footer actions. |
| [Result reopened](backup-document-result-reopened.png) | Same completed state after reopening; original bytes equal the preceding image. |

The actual native flow cancels its first replacement dialog, deliberately invokes the detached confirmation callback, and verifies no replacement dispatch. Review survives recreation and reopen. A second explicit native confirmation performs one replacement of private SQLite data; a held fake cleanup keeps the in-progress phase visible through recreation. Assertions require the imported alarm off, pending nap cancelled, user sound label retained, clip reference cleared, fixture audio file removed and one cleanup call. These images prove this records-backup scope, **not the separate Settings profile Activity or new dimming import UI**.

## New private JSON/settings API continuation that completed

After the eighth screenshot, the selected method unconditionally calls `roomDimmingProfileRoundTrip()` before fixture cleanup. The passing method status therefore includes the continuation and its assertions. The log is a whole-method result, not a separate per-assertion trace or subcase timing.

The continuation uses the real typed SettingsProfile/JSON/Persistence/RoomDimming-save APIs, Android SharedPreferences and AtomicFile. A ContextWrapper maps exactly the three supported preference files to UUID-owned names, returns a private directory and returns itself from `getApplicationContext()`. It creates and reads an actual private `profile.json`; no content provider, Activity test port or global preference dump is substituted.

The executed assertions establish:

- Appearance capture with Room dimming on emits profile **version2** and a typed `roomDimming` Boolean. The source `roomMicrophoneAsked` marker is absent from the JSON.
- UTF-8 file decode and review name the incoming on choice. Destination dimming remains off until explicit Persistence.apply; the acknowledged saved value survives reread and capture. Destination permission history stays false and no recovery journal remains.
- A legacy **version1** document without the new field decodes/encodes as version1, reviews dimming as unchanged, and preserves both current on and current off choices through real apply calls.
- A newer dimming choice changes the supported-settings fingerprint; applying a stale review throws before writing, preserves that newer choice and leaves no journal.
- A deliberately seeded, fixture-owned version1 AtomicFile recovery journal records Room's prior **explicit absent/null** value and planned true value. Real rollback removes the key, restores default off, preserves the destination permission marker and removes the journal. This is a recovery-record fixture, not a simulated crash or forced process interruption.

The continuation deletes its exact private preference names and private file directory. Outer fixture cleanup drains workers, closes owned Activities, restores factories, requires exact original app preferences and production backup data, checks no unexpected file changes, and leaves alarm/tracking inactive. No RoomDimming.apply or window operation occurs.

## Compatibility and limits

Settings profiles export version2 only when Appearance includes the new field; profiles without it retain version1. Older version1-only apps reject version2. Legacy imports leave dimming untouched. Permission-request markers, credentials, provider identities and grants remain excluded. Records/alarms Backup14 and database schema17 are unchanged; their local restore preserves existing preferences instead of transferring them.

Old recovery journals that omit Room's key are **static compatibility evidence only**: the decoder checks supplied keys against the expanded allowlist; rollback compares and writes only journal-provided keys. Omission therefore leaves the current Room choice untouched. It differs from the explicitly absent/null key exercised above, which removes the key. No runtime old-journal case is claimed.

[source-equivalence.json](source-equivalence.json) pins 22 production/native fixture files byte-identical between captured source and integrated release source `d4415a2b5194abd50c36921a5c7c317ef1f18c51`; whole workflow/script differences are retained because release has additional scopes. This is listed-source equivalence, not a whole-APK runtime certificate. No Settings profile Activity navigation/Back, Android document picker/provider, OS permission/grant, window dimming, physical output, process death, full-app security or all36-screen acceptance is established here. Creating this archive ran no new tests, CI, build or device operation.
