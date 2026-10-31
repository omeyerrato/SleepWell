# Private Report share admission across process loss

Reviewed base: `27796d0c`. The private Report share feature already separates its
default-off notes preview, exact-byte verification and explicit Android chooser
from the public authenticated summary picker. Those public picker contracts,
caller checks and data schema remain unchanged.

## Finding and completed source behavior

The original private provider required a process-local `ADMITTED` Snapshot map.
A recipient could retain Android's temporary URI permission while SleepWell's
provider process was reclaimed. Its later legitimate read then failed because
the map no longer existed, even within the advertised cache lifetime. This was
a fail-closed availability defect, not evidence of an unauthorized file read.

The private provider now resolves a bounded durable admission index:

- An AtomicFile in the dedicated cache directory holds at most 16 entries and
  8 KiB of JSON. Each entry contains only a random report filename, SHA-256,
  byte length and file modification time. The index has a schema version and
  generation token. It stores no report text, notes, record ID, model data,
  provider/device identity or user credentials.
- A separate private preference stores the durable generation token. Explicit
  Share's existing verification worker creates the admission after checking the
  current record, pinned bytes, digest and active generation. Merely opening or
  preparing a preview does not persist an admission or grant access.
- The visible callback uses the prepared admission and opens Android's chooser
  with the same read-only URI/ClipData grant. It does not write admission metadata
  or preferences on the UI dispatcher. Raw ClipData is sufficient because the
  MIME type is already explicitly `text/plain`; no provider query is needed to
  construct it.
- Reads after process-memory loss use the private index and current durable
  generation. They require the exact content authority/path, no query/fragment,
  canonical regular cache file, expected time/length and SHA-256. Unknown,
  unadmitted, expired, corrupt or changed files fail closed. Writes remain
  rejected even from the owning UID. All file reads and JSON are bounded.
- A first Share cancelled while losing focus retires its prepared admission in
  the background, retaining the private preview for another explicit attempt.
  Previously shared snapshots retain their already-authorized lifetime. A failed
  chooser queues cleanup on the same background owner.

The lifetime remains the original **up to ten minutes from preview staging**,
subject to Android cache eviction, clock/mtime checks and explicit history
invalidation. A recipient may keep a copy it already received; no mechanism can
revoke that separate copy. No receiver is selected or contacted automatically.

## History invalidation and lock order

Private report cleanup rotates and commits the durable generation **before**
revoking grants or deleting cache/index files. An old cache/index reappearing
after process-memory loss cannot restore old admission under the new generation.
If rotation fails, cleanup still attempts its owned file/grant scopes but returns
failure. The existing `SleepSummaryFiles.historyChanging` pipeline then also
attempts its prior public-summary cleanup and reports failure before history
erasure/replacement proceeds. It must not claim successful durable invalidation
after a failed rotation.

Pending Share workers remain fenced by the existing volatile history epoch as
well as their request generation. Report-file code reads that epoch without
acquiring the summary monitor. It does not introduce the inverse
ReportShareFiles-lock → SleepSummaryFiles-lock order. Metadata parsing/writing
and hashing run on verification/Binder workers; the ordinary visible admit
callback performs no admission disk write.

## Existing native journey extension

`ReportShareAdmissionChecks` contains no new test methods or unit campaign. The
existing isolated native Share journey can call:

1. `persistedAdmissionSurvivesMemoryLoss(context, sharedSnapshot)` after its
   explicit Share and exact-byte read. It clears the in-memory generation cache,
   reads the identical bytes through the actual provider, verifies that metadata
   has only the bounded allowlisted fields, makes the disk index malformed and
   requires denial, restores the exact metadata, then requires exact bytes again.
   Unexpected URI query data is also denied.
2. `retiredGenerationRejectsReappearingCache(context, sharedSnapshot)` after the
   chooser and Share Activity close, before final cleanup. It rotates private
   report admission through history cleanup, recreates the old report bytes and
   old index with the original file time, clears process memory, and requires the
   actual provider to reject them under the new generation. Owned test cache is
   then removed.

These are controlled cache-loss and old-cache-reappearance checks inside the
existing fixture. They do not claim an actual package force-stop, a selected
external recipient, confirmed delivery or a malicious DocumentsProvider test.
No additional screenshots are required because the UI composition is unchanged.
Root owns method integration, build/native execution and merged evidence. This
source increment alone does not establish a passing runtime result.
