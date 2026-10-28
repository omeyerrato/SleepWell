# Source equivalence and delivery order

Published baseline: 4cf8a86fcf035686979052b95fbd5f08b6c385b1. Successful native head: dea845226d6086794737ad69a338fa32ccd473c6.

Apply these commits in order (production, native, verification metadata, bounded production repair):

- 61e309bf703f3ebab20af74b5bbcb501b5269cde — Anchor Tonight actions above dock and clarify empty alarm state
- b5564a0366c77cbf1f95e8a5ed01a0704bd768b1 — Extend existing Tonight journey for fixed actions and saved alarm states
- 886cbb7a088ae1c2021a8edb23a0f9ea5d6c4206 — Scope isolated Tonight composition native verification [skip ci]
- dea845226d6086794737ad69a338fa32ccd473c6 — Measure dock height from rounded child and padding pixels [skip ci]

The evidence commit follows these four commits and changes only this archive. The first failed run uses the first three commits; the successful run adds only the Design rounding repair. No native gate was relaxed between those runs.

## Named Git blob SHA-256 at the successful native head

| Path | SHA-256 |
|---|---|
| app/src/main/java/org/sleepwell/app/TonightScreen.java | 2b0218cff544c1618d35400b9d43a5f040f0668352523421ee1d0005ce11b515 |
| app/src/main/java/org/sleepwell/app/Design.java | 760f786a205066baec021af53362ef385901ec8ee006f3ab2a487966237f4523 |
| app/src/androidTest/java/org/sleepwell/app/NightArcFlowTest.java | 9f9d843b0e6e3d2a73142cfcb5754e27fdd3072ef46e6f589a624ee80987cc40 |
| scripts/ci_silent_ui.py | ae1d078b09105208c357300c9cc90c01087a8f9c48380e945414f403cbdb586f |
| .github/workflows/silent-ui-review.yml | 0fd9ad8b19bffef7d55d24ddabc6c45120730607abe787092317a67612da22e0 |

The complete app/src/main diff against baseline contains exactly TonightScreen.java and Design.java. NightArc.java, NightWindow.java, TonightLayout.java and MainActivity.java are byte-identical; every asset/resource, runtime engine, scheduler implementation and model/schema file is unchanged. The existing default AlarmScheduling.port delegates to Scheduler.allowed; using that existing port for Tonight admission preserves the default behavior while allowing the native flow to avoid OS scheduling.

## Focused CI inventory audit

The new tonight-composition scope selects exactly NightArcFlowTest#clockProgressAndExplanationWithoutAnyAlarmDelivery with the 14 literal PNG names listed in README. There remains exactly one @Test in that class. Both optional flags were explicitly false on dispatch.

An AST comparison removing only the new scope choice, exact selector branch and frame dictionary entry equals the baseline script AST. Removing only the new workflow choice line equals the baseline workflow bytes. Thus all pre-existing selectors, frame inventories, optional defaults and successful unrelated scopes are preserved. This is a structural comparison, not a claim that the whole script files are unchanged.

## Evidence provenance

Raw method logs, environment, summaries and image bytes match the downloaded artifacts. Run metadata was fetched from GitHub for each named handle; its head/conclusion matches the environment and native result. The archive copies only this method and its diagnostic failure, without unrelated guest logs or old screenshots. SHA256SUMS covers every archive file except itself.
