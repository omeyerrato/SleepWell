# Final travel list assertion follows the later saved plan — rerun pending

2026-11-01. Actual run `41265653342`, source
`e481138ac069f1dc38ca3ae34f569a36237c7426`, failed the existing Trip method after
23.403s at its final no-eligible-list expectation. Full instrumentation text,
diagnostic text/XML and original failure PNG were read. The XML/PNG show the
still-open15-minute Trip draft. The saved frame exists, and execution reached the
final line after the race completion, committed override/source/following recurrence,
whole ordinary alarm and linked-profile JSON assertions. The overall method still
failed and its final admission receipt was not written; this is retained partial
evidence, not full method acceptance.

The native fixture deliberately lets a competing plan writer save a new30-minute
plan after the actual alarm commit. It proves that writer was blocked until the
override committed, then completes its own later revision. The Trip Activity below
the editor correctly retains the earlier reviewed15-minute draft. Its next Apply
once must reject that stale plan before listing alarms. The fixture incorrectly
expected the no-eligible list immediately, skipping this legitimate rejection.

The corrected journey explicitly awaits and captures the existing changed-plan
message. The committed alarm JSON and exactly one fake scheduling call must stay
unchanged. It then uses the actual header Back to close the stale draft, launches
a fresh actual Trip owner, and verifies its complete draft JSON equals the later
saved plan. Only then does it exercise the unchanged no-eligible list assertion.
The source exclusion/recurrence policy is not weakened to satisfy that assertion.

Activity recreation is deliberately not used as a refresh/reset: it restores the
current draft, including a stale draft. The new owner is opened through the same
real Activity route already used by this isolated native journey; no private-field
mutation, synthetic plan replacement or production flag is introduced. The later
plan edit never schedules a second alarm. Existing save/race/profile/retired-owner
checks and all16 originals stay required. The
[updated finite plan](TRAVEL-OCCURRENCE-PLAN-REFRESH-NATIVE-PLAN.json) adds only
`trip-occurrence-post-commit-plan-changed.png` and two final proof fields; there is
still one existing native method and the final no-eligible frame remains mandatory.

Retained failed-run identities:

| Actual artifact | SHA-256 |
| --- | --- |
| `TripOccurrenceFlowTest#savedTripOccurrenceReviewKeepStaleAndSavePreserveRecurringAlarm.txt` | `7a4a8357bf8bfa32484ef522d1a7430072d1f8797fd528193c055e488282c2f7` |
| `trip-occurrence-saved.png` | `e99835c35997fb7406ca6ac69afd8ca12da030afb75a0504dfa98ffc471b6114` |
| `trip-occurrence-failure.png` | `e99835c35997fb7406ca6ac69afd8ca12da030afb75a0504dfa98ffc471b6114` |
| `trip-occurrence-failure.xml` | `5de31cdad8a051f87931f899fe42424925439140e7d16679be8fadb7a91eb918` |

Validation here: diff, execution-order/source inspection and exact17 frame-plan
consistency. Production files are unchanged. No build, emulator, native run,
hosted CI, unit campaign, phone, alarm, audio or model action ran. Root owns the
focused rerun. Rhythm remains with its assigned owner; prior receipts are preserved.
