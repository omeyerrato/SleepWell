# Combined armed-wait, smart-deadline and snooze build

Exact application/test source **6ec80fb** (later8e60fe9 changes documentation only).
Local combined verification passes **1046 pure tests/127 suites**, both APKs and
lint in **5m37**. Unit failures/errors/skips:zero. Lint errors/fatals:zero;
warnings:142. Raw XML/lint/logs are copied byte for byte with SHA256SUMS.

Adds explicit armed-wait charging/screen-off conditions, actual projected
smart-window lead-in and optional exact-clock snooze to the prior combined
picture/inclusion/backup/goal candidate. Quiet native CI40167823818 remains
pending. No actual alarm, microphone, camera, DND, charging, provider account or
overnight behavior was exercised locally. Existing third-party LiteRT Kotlin
metadata incompatibility is diagnostic; Java lint does not certify that metadata.
