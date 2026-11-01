# Travel occurrence native dialog tap repair — scoped rerun pending

2026-11-01. Run `41260549703` at source `1c1fe0c1` failed
`TripOccurrenceFlowTest#savedTripOccurrenceReviewKeepStaleAndSavePreserveRecurringAlarm`
after 13.073s, in `enterReview` while awaiting the saved alarm label. The complete
method log, diagnostic text/XML, actual chooser-restored and failure originals
were read. The chooser original shows the real recurring fixture and Keep current
sheet; failure shows the ordinary trip plan with no chooser. Keep/recreated-choice
frames existed, but proposal/draft/final-save frames did not. No successful travel
mutation or new 31C acceptance is inferred from this earlier failed receipt.

The same fixture coordinate defect identified in Emergency was present in
TripOccurrence.click: `getGlobalVisibleRect` is relative to the root view, while
UiDevice injects screen coordinates. The bottom Dialog has a different screen
origin. Clicking its locally computed target can hit outside and dismiss it,
without invoking the intended Keep/row action. The helper now retains focused-root,
actual enabled/clickable target, full visible width/height and 48dp checks, then
uses `getLocationOnScreen` to compute the real touch rectangle. It still injects
an actual UiDevice gesture; no performClick substitution, arbitrary delay, disabled
animation or weakened proposal assertion was added.

Only this native tap helper/proof field changes in this increment. The separate
31C admission/profile source correction remains `3d5b7155`. All existing method
assertions and the updated 16-original admission plan remain required, including
linked profile exclusion, stale plan and whole alarm rejection, recreation,
explicit ordinary Save, real SQLite commit/competing-plan lock assertions and
unchanged recurrence. The actual rerun must establish the diagnosis and outcomes.

Retained original artifact identities:

| Actual artifact | SHA-256 |
| --- | --- |
| `TripOccurrenceFlowTest#savedTripOccurrenceReviewKeepStaleAndSavePreserveRecurringAlarm.txt` | `8582cdc361ca3cc50fa4ff62318c78cb7d81a3cc7a93d0601b8e7e71f129f546` |
| `trip-occurrence-failure.txt` | `1e5b688761bbab2fc74704610b813ddbb8bb4d207f393483e6064fa89785e8c4` |
| `trip-occurrence-failure.xml` | `33d536d0ab337093b213615a27b7005808bde94928900ec2745149e27a7e7b8d` |
| `trip-occurrence-choices-restored.png` | `b50d1475cd988cb3a9230810011f8ebd40080125049918a0e78cbc415375dd5b` |
| `trip-occurrence-failure.png` | `a3e5e6f87ea78b4168a085be3f2874c5c6d0f69998c18a3359274894a827a102` |

Diff and unchanged finite capture names checked locally. No build, emulator,
hosted CI, unit campaign, phone, alarm, audio or model action ran here. Root owns
the one scoped method rerun and original-PNG inspection. Failed receipts remain
unchanged.
