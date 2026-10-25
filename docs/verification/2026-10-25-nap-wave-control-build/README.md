# Nap control arrival correction

Source **0a821eb** passes debug/native APK builds and lint in **1m36**, with zero
lint errors/fatals and142 warnings. This incremental build changes the Nap
waveform host and test helpers. A subsequent exact-source pure run passes all1063
methods/129 suites in41s; raw XML and its log are included. Quiet nap regression
CI40170785588 passes4classes/8methods/all27 originals; expanded increment40170787840
remains pending. No physical output or phone installation ran.

Earlier52bcf99 failed native-test compilation because its added diagnostic used
Files.writeString, unavailable in Android's API stubs. The corrected helper uses
Files.write with UTF-8 bytes. Both failure and corrected successful logs, plus
the resulting lint XML, are preserved byte for byte with SHA256SUMS. No assertion
or native count was removed. The unsigned release APK remains the priorba2d478
build; it is not a signed current production release.
