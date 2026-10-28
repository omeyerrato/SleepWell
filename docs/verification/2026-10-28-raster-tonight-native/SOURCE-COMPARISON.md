# Source identities and bounded equivalence

Frozen production source: `ef69408258e900a50274d6f2488e859647138714` (0.1.5/code6). First native attempt: run 40756639924 at that exact head, failed 58.396s. Successful native source: `ecdea35da8956adc120cbf96a50fc08449a374d0`, run 40759551606, passed 50.332s.

Between those two heads, `git diff --name-only` contains exactly `app/src/androidTest/java/org/sleepwell/app/NightArcFlowTest.java`. Their `app/src/main` Git tree is identically `435a3f890533d2a4c4554ab77aa2b5dbdccfb682`. Build metadata, scope script and workflow bytes are unchanged for the native correction. The success therefore exercises the frozen production source/resources with a corrected native-only fit calculation. This statement is restricted to these two exact commits.

The earlier Tonight proof, run 40735685243 at `dea845226d6086794737ad69a338fa32ccd473c6`, passed 52.457s/14 originals before raster replacement. Named-file comparison with frozen source shows `TonightScreen.java`, `NightArc.java`, `NightWindow.java`, `TonightLayout.java`, and `MainActivity.java` byte-equal. `Design.java` changes only its icon documentation comment; `Icons.java` updates its documentation and adds the headphones alias. Resource artwork changes, and unrelated production work is present; no whole-app equivalence to that earlier proof is claimed.

The original 14 capture calls and test body remain equal to that earlier native method after removing only the appended gallery call and its owned-dialog cleanup. The same single `@Test` is retained. The original 14 states are rerun here, not accepted solely from a historical pass. Only the existing `tonight-composition` inventory was extended to 18; the correction changes no selector, inventory, default, optional-output rule or workflow.

`source-comparison.json` retains SHA256 of the named production files at both proof sources, the exact current native/script/workflow blobs, and all 365 density PNGs serving 73 unique resource IDs. `review-inventory.json` records actual decoded native counts/dimensions/alpha ranges and both run identities. Raw run metadata and environments pin the actual GitHub heads; raw summaries retain their actual pass/fail values.

Delivery order after frozen source: native-only `ecdea35da8956adc120cbf96a50fc08449a374d0`, followed by this separate evidence commit. No new production or asset commit was needed. The archive leaves the older Tonight and imagegen provenance folders untouched.
