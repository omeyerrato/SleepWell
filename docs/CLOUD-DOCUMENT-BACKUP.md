# Remembered cloud or local document folder

F137 now has an explicit **manual** backup flow using Android's Storage Access Framework. It works with an installed storage app that exposes a writable document tree; that provider may represent a cloud account or a local folder. No proprietary cloud login, paid service or account token is added to SleepWell. Provider account setup, connectivity and remote upload are external prerequisites.

## User flow

Data & privacy → Cloud backup → Choose destination opens Android's folder selector. A successful user result must grant persistent read and write access. The app checks a writable folder, remembers the selected tree and label, and only then retains newly granted permission. Cancelled or failed selection keeps the previous destination. Change chooses another folder; Disconnect removes the remembered destination and releases only grants acquired by this flow where possible. No provider file is deleted by either action.

**Save a new backup** is the only export trigger. Opening/reopening, selecting a folder or connecting an account does not schedule a backup. The app uses the existing `BackupDocuments` JSON writer and its actual portable format/8 MiB limit, snapshots private SQLite in a worker and creates a unique timestamp/UUID JSON document. It checks that the provider returned a new child document before opening it for writing. Existing documents are never intentionally opened for export, overwritten or deleted. Use a dedicated folder with fewer than 200 child documents so the new-document check is bounded.

After writing, the app reads the provider's document back, compares exact size and SHA-256 with the prepared export, and only then marks the version verified. This proves provider read-back, **not a completed cloud upload**. Storage providers can cache locally or upload asynchronously. Inspect the provider's own sync status for remote availability.

Local history remembers the last 20 versions created by this installation, including unverified/partial attempts. Earlier provider files remain untouched. History is filtered to the active folder and survives reopening. Disconnect keeps this local catalog but prevents access until the folder is chosen again. Other existing provider documents can be imported through the ordinary document chooser; this catalog does not scan an entire remote account.

## Restore review and conflict handling

Only a locally verified entry offers **Review**. It hands the saved document URI and recorded SHA-256 into the existing BackupActivity/BackupJob read flow. Existing strict JSON/format validation runs. Before presenting review, the immutable document bytes must still match the verified-copy hash; changed files require explicit manual selection/review instead of silently trusting old history.

The existing replacement confirmation, runtime idle/admission checks, current-local-data fingerprint, transactional replacement and imported-alarms-off behavior remain authoritative. Reading or reviewing never restores. The final “Replace local data” confirmation is still required. Current tracking/alarm admission or local data changes invalidate a stale review. Portable JSON excludes audio, app settings, integration credentials and goal commitments, matching the existing backup flow.

## Cancellation and failures

The retained worker state survives Activity recreation in the same process. Cancel fences source/stream/verification steps and requests provider descriptor/cancellation-signal cleanup off the UI thread. The operation remains busy until the provider returns. A provider can ignore cancellation or block; document creation has no cancellation-signal API. Cancelling or losing the process can leave an empty, partial or complete new file. The app does not silently delete that file. History records it as unverified until read-back succeeds; a persisted in-progress flag warns after process restart.

No automatic retry or scheduling runs in this increment. Retry creates another unique version. A missing/revoked grant, unavailable folder, full/slow provider, write denial, oversized snapshot, changed/read-back-mismatched file or corrupt local catalog produces an error instead of a verified success. If the provider wrote a valid file but local history persistence fails, an unverified or uncatalogued version may remain; review it manually before relying on it. Remote encryption and provider retention policies are controlled by the user and storage app; SleepWell writes the normal readable JSON format.

## Verification contract

- `CloudBackupCopyTest`: 8 pure cases for exact read-back/hash/size, before-create size/cancellation admission, post-create cancellation, denied output, mismatch/stall and provider URI rejection.
- `CloudBackupJobTest`: 4 pure cases for explicit single admission, cancellation retirement, recoverable failure/explicit retry and rejected worker.
- `CloudBackupReviewTest`: 2 pure cases for matching-hash review requiring confirmation and rejecting changed/malformed hashes.
- Existing `BackupJobTest`: all 12 existing pure cases retained.
- `CloudBackupStorageTest`: 5 native private-preference/SQLite cases for destination/revision persistence, disconnect/history preservation, bounded/upgraded receipts, corrupt-history preservation and the actual JSON snapshot without source mutation.
- `CloudBackupFlowTest`: 1 native method with fake folder choice/provider, actual private JSON snapshot, explicit Save, reopen, verified history, handoff to actual BackupActivity review and Keep local data. Four original screenshots: `cloud-backup-destination.png`, `cloud-backup-saved.png`, `cloud-backup-reopened.png`, `cloud-backup-restore-review.png`.

Direct JDK/JUnit execution passes **26 pure cases** (14 new plus 12 existing). Android compilation/native execution and screenshot production are pending the root combined build/CI. No real cloud account, upload, provider folder dialog, physical phone, alarm or audio is used by these tests. No visual completion or actual cloud interoperability is inferred.

Primary Android references: [user-selected document trees and persistent access](https://developer.android.com/training/data-storage/shared/documents-files), [DocumentsContract creation/query APIs](https://developer.android.com/reference/android/provider/DocumentsContract).
