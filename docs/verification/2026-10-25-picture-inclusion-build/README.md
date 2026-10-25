# Combined picture, inclusion, backup and sleep-plan build

Exact source **8027f83**. The combined local run passes **1016 pure tests/125 suites**,
debug APK, native test APK and lint in **6m50**. Unit failures/errors/skips:zero.
Lint reports zero errors/fatals and142 warnings. Raw XML/lint/logs are byte-preserved
with SHA256SUMS. The existing LiteRT Kotlin2.4/2.2 metadata diagnostic remains;
successful Java lint does not certify analysis of third-party Kotlin metadata.

Includes picture matching, category inclusion/fail-closed settings, automatic
metadata backup, local sunrise and reviewed sleep-plan alarm-time copy. Native
execution for the newest flows is pending CI40166582977 at27cdf83, which adds a
test-only backup-dialog screen-coordinate correction. No device output, camera,
microphone, actual provider account, overnight reliability or design is certified.
