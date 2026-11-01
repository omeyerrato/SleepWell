# Retire occurrence-review callbacks before returning a result

2026-11-01. Source parent `9d707ddd`, addressing actual run `41262356510` at
`71181142772adb551904519382bb0828408cac36`. The complete Trip method log reports
8.673s and failure at the reviewed-restored capture after the retained old
`Use next time` callback was invoked. Actual reviewed/failure originals were read;
the later failure XML contains the parent `Edit alarm` / `Save alarm` screen.
This is an earlier failed receipt, not acceptance. No visual-layout correction is
claimed from these frames.

This is a production retired-owner hole, rather than grounds to remove the native
callback assertion. NextAlarmOverrideActivity.use/undo previously admitted solely
by hasWindowFocus. A destroyed Activity can retain stale window state, and its
finish/result token still addresses the recreated ActivityRecord. A callback held
by the retired positive could return a result and finish the replacement owner.
The real saved alarm remained protected by ordinary Save, but the current review
and its parent draft were not protected from that callback.

The Activity now admits interaction only on the main thread while resumed, not
retired/returning/destroyed/finishing/changing configuration, with an attached
current page and the current Activity window. Every page action captures that
page's render epoch and requires actual window focus. Review, Use, Undo, Cancel,
header Back/options and date/time entry follow the same owner boundary. Teardown
retires epochs before dismissing child pickers. Picker result callbacks also
retain their opening page/epoch and request; they cannot update a later render or
another Activity. Their deliberate result is allowed after their own modal
Dismiss without pretending the covered Activity currently owns window focus.

Use captures the immutable reviewed proposal that its own render displayed and
requires it still to be the current reviewed object. Fresh admission compares to
that same value before preparing a result, then marks the owner returning before
setResult/finish. Old views cannot read a newer class-level reviewed value and
approve it. Current valid review/save, trip/alarm/zone/occurrence checks, travel
lock ordering, profile exclusion and the ordinary final Save authority stay intact.
No alarm is scheduled by occurrence review itself.

The [focused native plan](TRAVEL-OCCURRENCE-RETIRED-OWNER-NATIVE-PLAN.json) retains
the exact existing retired positive invocation after Activity recreation and all
16 originals. It strengthens the same one method with detached Review/Use callback
checks after real rerenders of the same Activity. The current positive must remain
attached, enabled and focused; its exact reviewed key and the complete parent
alarm draft must stay unchanged. The saved database/no-scheduling proof remains.
The existing actual gestures still exercise Keep current, stale proposals and
ordinary final Save; no retired assertion is removed or softened. The completion
proof records these added callback boundaries only after the whole method passes.

Retained failed-run artifact identities:

| Actual artifact | SHA-256 |
| --- | --- |
| `TripOccurrenceFlowTest#savedTripOccurrenceReviewKeepStaleAndSavePreserveRecurringAlarm.txt` | `0a19df6feac134be0b847f589f43f365cba429385932ac6e717060f6aac09866` |
| `trip-occurrence-reviewed.png` | `64c6285289c1d7ddfdf4e502364923752aefe2d893491c7f70564f6a4d39ec94` |
| `trip-occurrence-failure.png` | `6f265eefb61f0b49f04d4304e85d3e8957820f141107f99004162efcd1bb6f7b` |
| `trip-occurrence-failure.xml` | `e119733abfd3fa3f486979cf21d173fc3df7f114c85b7d25c2ef8c3e8589371b` |

Validation here is source/lifecycle inspection, diff and exact16 capture-plan
consistency. No build, emulator, native run, hosted CI, unit campaign, phone,
alarm, audio or model action ran. Root owns the new app build and the single
focused native rerun. Rhythm is handled separately by its assigned owner.
Historical failures and acceptance claims are unchanged.
