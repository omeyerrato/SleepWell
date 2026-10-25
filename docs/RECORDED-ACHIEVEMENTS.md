# Recorded achievements (F079)

Report options → Achievements opens a real, read-only ledger for first, seven and thirty qualifying recorded days. Empty Report also exposes the route. Nothing is awarded or stored separately: editing, deleting or restoring records recalculates progress. No current goal is applied retroactively, and this increment does not invent goal/streak/clinical achievements.

## Counting contract

A qualifying recorded day needs at least **three elapsed hours of valid saved sensor coverage**. This is an explicit application recording-coverage criterion, not a medical sleep minimum or proof of sleep. Records must be closed, tracked, have known validated motion/sonar provenance, known recorded zone, finite actual readings and positive sample counts, and be noninterrupted/clock-stable. Manual/provider/unknown records, explicitly `nap`-tagged periods, future/open periods and missing measurements do not qualify. An untagged nap cannot be identified retrospectively; the screen does not claim to classify naps or sleep.

Motion evidence uses explicit saved epoch bounds (at most two minutes per measurement interval), and sonar uses actual ≤5-second `VALID` epochs. Zero is a real valid reading; NULL, invalid quality, missing sample count or long unmeasured spans are not filled. Existing effective coverage removes pauses, marked-awake intervals and trims, respecting active elapsed time. Physical coverage is unioned across records before grouping so duplicate/restored/time-zone-shifted copies cannot double-count it. The recorded zone and a fixed local noon boundary label the day; DST changes labels without changing elapsed coverage. One day can qualify once; multiple nonoverlapping pieces can meet the threshold together. Restored tracked evidence remains usable; importing does not manufacture new awards.

The reader uses the existing `Records.factory` (production defaults to the production database; private test factories stay private), a pinned current-time snapshot and a single SQLite read transaction. It compacts actual epochs and calculates off the UI thread. Histories beyond 5,000 sessions or 2,000,000 epochs show a bounded read error and retain the underlying data. No schema, backup format, preferences, scheduler, tracking, hardware, or stored awards change.

## Verification

- `RecordAchievementsTest`: **9 host JUnit tests pass**, covering count thresholds, missing data, edits/pauses/awake periods, interrupted/unknown/manual/nap rejection, overlap/restored time-zone duplicates, DST, actual measured epoch quality and valid zero readings.
- Direct `javac` check passes for the new production model/reader/Activity plus modified ReportScreen. Native fixture sources also compile against cached Android/test APIs and the existing focused-input helper interface. This is not a Gradle APK or Android execution claim; root performs the integrated build.
- `RecordAchievementsStorageTest`: **2 native methods**, private motion and sonar databases. Actual begin/checkpoint/end APIs, reader nonmutation fingerprint, raw-evidence preservation, trim/reset/delete recalculation, valid zero sonar coverage and null/quality gaps. Not locally executed.
- `RecordAchievementsFlowTest`: **1 native method**, emulator/configuration guard, private database and injected clock. Actual Report options route, empty/data/recreation, History search, actual editor note Save, trim→refresh/reopen, reader error→Retry, original preference/raw epoch preservation and no runtime service admission. Not locally executed.
- Expected **6 PNGs**: `record-achievements-empty.png`, `record-achievements-first.png`, `record-achievements-recreated.png`, `record-achievements-search.png`, `record-achievements-edited.png`, `record-achievements-error.png`.

No device, recording, alarm, microphone, torch, cloud, notification or actual playback actions were run. Native results and composed visuals remain pending CI.
