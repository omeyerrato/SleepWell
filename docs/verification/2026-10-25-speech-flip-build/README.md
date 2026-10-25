# Combined speech, barcode and flip build

Exact source **a3445e7**. Local combined run passes **956 pure tests /117 suites**,
debug/native APK builds and lint in **6m15**. Zero failures, errors or skipped unit
tests; lint zero errors,139 warnings and one hint. Raw XML/logs are retained byte
for byte with SHA256SUMS. The lint8.13.0 workaround still reports LiteRT Kotlin
metadata incompatibility; successful Java analysis does not certify third-party
Kotlin analysis. No native/physical acceptance is inferred from compilation.

Native CI40159540059 on the same source passes the new Speak5 methods/all12 PNGs
and six pinned CPU audio trials, plus flip-control2 methods/all7 PNGs. The whole
24-class/54-method increment run remains **FAIL** on playlist reopening, delay
configuration/storage, barcode input and a flip storage timing assertion. The
[native subset](../2026-10-25-speak-native/README.md) preserves exact limits.
The older independent laughter accuracy failure remains unresolved. No phone or
real alarm/audio/microphone/vibration/torch/DND/provider action was tested.
