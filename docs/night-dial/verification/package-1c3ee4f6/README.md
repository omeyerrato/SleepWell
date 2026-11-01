# Delivered and installed0.1.29

Source1c3ee4f64120083ff3695e734c98e1e0a3e3ade4, app/src/main tree
c30bd120909facc1f01f230c501d60bec724dd23. Native Java Views and the integrated
6202a00e Night Dial design are retained. Both APK builds and lint pass; unit tests
were not executed. The package is arm64-only, minAPI30/target36, code30, and uses
the existing preview signer.

The [package audit](package-audit.json) independently checks size/hash, ABI,
version, signer and the ten bundled models. [Installation and readback](phone-installation.md)
confirm the authorized in-place phone update. No app/output test ran on the phone.

Current [task/settings follow-up](../tasks-1c3ee4f6/) covers two selected native
methods and64 originals. Its corrected task states, Guard action and empty-access
copy follow the earlier [237-original baseline review](../baseline-10e8d74c/core-README.md).
That earlier workflow's stale20-method summary failure remains a failure; all21
actual native methods passed. The fixed runner counter does not erase the raw
failed receipt. Every172 design state, physical or overnight behavior is not
accepted by these bounded reviews.

The [additional-sound UI/migration journey](../additional-ui-10e8d74c/README.md)
and [frozen operational benchmark](../additional-cee13e91/README.md) remain
separately pinned to their actual source/application trees. The supplementary
worker/models/thresholds were not changed by the later task and settings fixes.
The feature is default-off and experimental; model quality, phone Deep/REM/apnea,
external prerequisites and remaining physical acceptance are still unfinished in
the [current checklist](../../../QUICK-CHECKLIST.md).
