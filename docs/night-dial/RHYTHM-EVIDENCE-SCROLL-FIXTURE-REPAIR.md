# Rhythm evidence scroll fixture repair — scoped rerun pending

2026-11-01. Run `41260549703`, source `1c1fe0c1`, failed the existing
SleepRhythm method after 9.68s at the assertion that reviewing an older contributor
requires a real nonzero list scroll. The full method log, actual evidence/failure
original PNGs and failure XML were read. This is an earlier failed receipt, not
new acceptance or a production layout regression.

The private fixture had only seven eligible nights. The object lookup returned an
already partly visible seventh row, so it never performed its fallback scroll.
The XML records viewport `[0,131][1080,2227]`; the seventh contributor occupied
`[53,2082][1027,2227]`. The ScrollView was scrollable, but presence was satisfied
at position zero. The assertion was useful: this fixture had not actually
exercised the promised scrolled return.

The fixture now adds14 earlier eligible closed phone-recorded nights. There are21
history observations and the same latest seven chart points; existing chart timing
and workday calculations still use those seven. The count honestly changes to
21of30 and chronotype remains unknown. Review now selects the twentieth actual
source on the first20-row page, preserving exact-ID Report navigation and the
unchanged `evidencePosition>0` demand. That position still must survive Report
return and recreation. No synthetic scroll setter or relaxed bound replaces a
native gesture.

The same journey uses Older to show the remaining oldest observation, recreates
that page and verifies21–21of21, then uses Newer to return. The
[updated finite plan](RHYTHM-EVIDENCE-SCROLL-NATIVE-PLAN.json) keeps all14 originals
and adds `rhythm-evidence-older-page.png` plus a completion proof with actual fixture
source IDs, scroll/page behavior, unchanged rows and unknown/empty truth. This is
still one existing native method, with no unit campaign or production test flag.
Production Rhythm/model/capture code is unchanged.

Retained earlier failure identities:

| Actual artifact | SHA-256 |
| --- | --- |
| `SleepRhythmFlowTest#actualSevenNightsWorkdaysPersistRecreateAndEmptyWithoutOutputs.txt` | `66ccbe439bb75542b123ea6e5345ef901e19586e81e3eff512782ee1b7b2acda` |
| `rhythm-evidence.png` | `dbcd10c6cf4bae1f329f33ce53279d98a35e1049ac9b083a28dddcd0e447c957` |
| `rhythm-failure.png` | `dbcd10c6cf4bae1f329f33ce53279d98a35e1049ac9b083a28dddcd0e447c957` |
| `rhythm-failure.xml` | `cf335a1b1ad2b65fe0782dfc029de77b7a14bea849ade4a6e8d7b4fd3f36e59a` |

Diff and exact15 capture-plan names checked locally. No build, emulator, hosted
CI, native execution, phone, alarm, audio output, unit campaign or model study ran
here. Root owns the scoped rerun and actual original-PNG inspection. The failed
run and historical acceptance register remain unchanged.

## Current recreated-owner readiness failure

Run `41262356510` at `71181142` reached the real older contributor, opened its
exact Report and returned to the saved nonzero evidence position. It then failed
inside `recreate("Timing evidence")`. The actual failure PNG/XML shows the
correctly restored contributor list, including the visible Older observed nights
button; the title is above that viewport. The helper required that offscreen title
to be visible to UiAutomator. This is a fixture readiness error, not evidence that
the app lost its route or scroll.

Recreation readiness now requires destruction of the old owner, the distinct new
owner's attached, laid out, hardware-accelerated focused decor, completed loaded
and resumed state, the expected presentation in its actual view tree, and a
committed frame. It does not scroll, request a rectangle, replace a gesture or
reset the viewport to expose the title. The existing exact nonzero scroll,
immutable source-ID, Older/Newer page, unchanged library and no-output assertions
remain. All15 original names and the proof receipt remain required.

No production code changed. This failure remains failed until the consolidated
rerun passes; no separate build, CI dispatch, native run or phone action was
performed for this correction.
