# Alternatives native evidence at 569a6991

Archived 2026-11-01 from [run 41266940619, attempt 2](https://github.com/omeyerrato/SleepWell/actions/runs/41266940619/attempts/2). **Four methods passed; the cohort failed on Contactless.** This is bounded native evidence, not full-app or all-state acceptance.

| Actual method | Result | Seconds | Required PNGs present |
| --- | --- | ---: | ---: |
| NightDialBreadthTest#secondaryDestinationsRenderWithDarkChromeAndQuietOwners | PASS |72.378|47/47|
| CalendarEventExportFlowTest#reviewNoteChoiceRecreationMissingCalendarAndRetryDoNotModifySavedNight | PASS |6.116|8/8|
| TripOccurrenceFlowTest#savedTripOccurrenceReviewKeepStaleAndSavePreserveRecurringAlarm | PASS |17.177|17/17|
| SleepRhythmFlowTest#actualSevenNightsWorkdaysPersistRecreateAndEmptyWithoutOutputs | PASS |32.931|15/15|
| ContactlessSetupFlowTest#cancelRotationAndPrerequisiteErrorsNeverOpenNativeAudio | FAIL |14.026|7/11|

Source is `569a6991a7063b289d0f2011dc46189e2650359e`, app/src/main Git tree `57441705e7c1bee7e96d82ded25d59058faa2ac2`. The original environment and focused summary remain unchanged. The separate [verification receipt](verification-receipt.json) does not rewrite the earlier source plans' pending/native_review_run:null metadata.

The correct artifact is ID **13067898375**, `attempt-2-original.zip`, 21,869,234 bytes, SHA-256 `63b28a3659b4158544b45ad4fae5d5bb9f7341996cd5648f6df22c25ed292609`. All 163 ZIP members were checked against the extraction. Selection by artifact name had returned attempt 1's tiny artifact instead: its three original files are retained under [attempt-1-infrastructure](attempt-1-infrastructure/emulator.log), including the explicit insufficient-disk fatal error before instrumentation. They provide no native result.

[method-frames.json](method-frames.json) records all five exact methods and frame lists, derived from the pinned runner's three plans. [original-captures.json](original-captures.json) records SHA-256, byte count and 1080×2316 dimensions for all 96 actual PNGs: 94 of 98 required originals and two diagnostic failure PNGs. All 87 originals required by the four passing methods exist. Eight passing representative originals and one raw failure original are copied here without modification; the verified ZIP holds the rest.

The complete five instrumentation logs and both proof JSONs are preserved. Trip's receipt confirms linked-profile exclusion, retained/detached-owner rejection, no scheduling before ordinary Save, the lock-ordered actual SQLite admission race, recurrence preservation, post-commit stale-plan rejection and fresh-plan no-eligible review. Its single scheduling-port call is injected, with no physical outputs. Rhythm's receipt confirms 21 actual private fixture nights, exact source navigation, real nonzero contributor scrolling across return/recreation and older/newer pagination; insufficient history remains unknown.

[visual-review.json](visual-review.json) maps 33 distinct originals opened for visual inspection to 43 actual files by exact hash equivalence: all 17 Trip, 15 Rhythm, 8 Calendar and 3 voice frames. No material text/action overlap or clipping issue was found in this bounded review. The other 44 Breadth frames have native pass and hash/dimension evidence, not a new personal visual acceptance claim. Voice-error and Calendar launch behavior use injected ports; no audible voice quality, installed-engine download or physical calendar save is certified.

Contactless failed on the unchanged-preferences assertion: expected `{}`, observed `{trackingPaused=false, tracking=false}`. The complete failure method log and original diagnostic TXT/XML/PNG are retained in [contactless-failure](contactless-failure/sonar-setup-cancelRotationAndPrerequisiteErrorsNeverOpenNativeAudio-failure.txt). The four absent originals are movement-before-stop, movement-returned, movement-stopped-returned and movement-restored; they are explicitly missing, not accepted. Root's separate Contactless-only follow-up 41268504688 at source 4422 is outside this receipt; no outcome from that run is claimed here.

This archive performed file/hash checks and visual inspection only. No build, hosted run, unit campaign, emulator launch, phone action, alarm, capture, audio playback or model evaluation ran. Full-app/overnight, physical-device, security and reliable automatic-class acceptance remain separate unfinished work.
