# Local feature native increment — exact source 1c1fe0c1

[Run 41260544579](https://github.com/omeyerrato/SleepWell/actions/runs/41260544579) completed **PASS** at `1c1fe0c16ce35abbf8f64dcb17b29064530e711c`. The original [environment](environment.json), [focused summary](focused-review-summary.json) and eight selected-method logs are copied unchanged. The summary records 8 selected methods, 118 required original images, all `passed:true`, `failures:[]`, `full_app_review:false`. Every log contains `OK (1 test)`.

This is the historical **database 17 / portable backup 14** application. It does not validate newer source or the later interrupted-recovery database 18 increment. [The separate 40A document](PENDING-40A-INCREMENT.md) and [pending plan](PENDING-40A-NATIVE-PLAN.json) keep that distinction explicit. Older failed runs and archives remain unchanged.

## Actual native scope

| Selected method / unchanged log | Reported time | Required originals |
| --- | ---: | ---: |
| [GoalFlowTest#draftSaveCommitmentAndEffectiveProgressPersistWithoutDelivery](GoalFlowTest%23draftSaveCommitmentAndEffectiveProgressPersistWithoutDelivery.txt) | 63.093 s | 18 |
| [NapFlowTest#presetsToneQuietPreviewStartRecreateReopenAndCancelPersist](NapFlowTest%23presetsToneQuietPreviewStartRecreateReopenAndCancelPersist.txt) | 30.55 s | 15 |
| [NapFlowTest#scheduleListAndErrorRetryUseSavedDeadlineWithoutRealDelivery](NapFlowTest%23scheduleListAndErrorRetryUseSavedDeadlineWithoutRealDelivery.txt) | 38.314 s | 21 |
| [TravelPlanFlowTest#savedZonesDstRemindersDeliveryCancelAndFailureAreRealExceptInjectedOutputs](TravelPlanFlowTest%23savedZonesDstRemindersDeliveryCancelAndFailureAreRealExceptInjectedOutputs.txt) | 41.952 s | 13 |
| [RecordAchievementsFlowTest#reportActionPrivateSavedEvidenceEditSearchRecreationAndReopenRecalculate](RecordAchievementsFlowTest%23reportActionPrivateSavedEvidenceEditSearchRecreationAndReopenRecalculate.txt) | 20.095 s | 16 |
| [TagManagementFlowTest#quantityDraftSaveRenameDeleteAndComparisonPersist](TagManagementFlowTest%23quantityDraftSaveRenameDeleteAndComparisonPersist.txt) | 15.176 s | 11 |
| [AlarmSoundModeFlowTest#silentAndPlaylistChoicesCancelRecreateSaveReopenAndReturnSingleWithoutOutputs](AlarmSoundModeFlowTest%23silentAndPlaylistChoicesCancelRecreateSaveReopenAndReturnSingleWithoutOutputs.txt) | 16.243 s | 10 |
| [NightDialAlarmFlowTest#duplicateIsAnOffDraftAndDeleteRequiresTheNamedCurrentAlarm](NightDialAlarmFlowTest%23duplicateIsAnOffDraftAndDeleteRequiresTheNamedCurrentAlarm.txt) | 40.435 s | 14 |

The [machine method manifest](native-methods.json) records the exact method-to-frame mapping and log hashes. Goal covers ordinary plan/draft exits, commitment dates and effective goal-bank progress. The two Nap methods cover presets, guarded Save/Start/recreation, pause/resume/denied resume, retained deadlines, schedule/date error/retry and existing supporting navigation. Travel covers its actual zone/date/reminder/draft-exit/save/error/retry owner with injected reminder delivery. Achievements opens the selected milestone's actual eligible contributing saved periods, including the first of eight and retained error/recreation paths. Tag comparison opens exact with/without contributing groups with unchanged eligibility and source identity. Alarm sound mode retains silent/playlist/radio drafts and rejected-token URL state. Alarm deletion covers named confirmation, stale confirmation, Undo/restoration/off state, reused-ID/stale-review rejection and expiry.

These are native UI/persistence assertions over private fixture state. Clocks, scheduling, capture/playback or delivery boundaries are injected as the selected source methods declare. The artifact's environment explicitly disables host audio output. There is no real alarm delivery, radio playback, recording capture, overnight data, provider exchange or medical/model accuracy claim. Units were not run; no unit campaign or new hosted run was initiated while archiving.

## Bytes and source provenance

The GitHub artifact is `silent-android-review`, ID 13065531255,16,502,191 ZIP bytes. Its published digest and downloaded SHA256 both equal `6a29ed5200a8e78d0f633a3df026dc2853926f6caf310540f7e90160dd7510ca`. [Source manifest](source-manifest.json) pins the exact app/test trees, runner/plan/test-source blobs and copied evidence hashes. The ZIP and full original artifact remain in the ignored `original-main/.android-local/ci-reviews/41260544579/` directory.

[All 118 original PNG hashes/dimensions](original-captures.json) were read from actual bytes, decoded and matched against the exact pinned source runner's required names; no required/extra PNG is missing/unaccounted. All 118 are 1080×2316. Fifteen representative originals,2,287,064 bytes total, are byte-copied here. The directory `.gitattributes` disables text conversion; copied logs/JSON/PNGs keep their original hashes. No screenshot was regenerated, repainted, cropped or replaced for this archive.

## Visual inspection and limits

All 118 frames were inspected through six derived contact sheets as orientation aids; those sheets are not original evidence and are not committed. The15 copied originals were then opened individually: restored plan/goal/travel dirty dialogs; Nap paused/denied-resume/resumed controls and request-results sheet; actual milestone basis/oldest contributor; both tag groups; restored invalid radio entry; stale restored-alarm receipt and recreated successful restoration. The manifest distinguishes individual-original review from contact-sheet-only review. It does not pretend every original was individually inspected at full size or confer all 43-state visual acceptance.

The actual milestone basis shows 4 h from one period plus only 30 min of additional nonoverlapping coverage from another; the source says recording coverage, not confirmed sleep. The tag group preserves the same explicit exclusions and contributing-period count. Its recreated frame retains an honest `Updating saved records…` status, so that frame is not called a completed refresh. Native data assertions, not a decorative count or screenshot alone, carry the selected persistence proof.

[Visual findings](visual-findings.json) record one spacing observation in the original [travel-error.png](travel-error.png): the caption `Silent reminder at cue start` and the error `Reminders need attention: Scheduling is unavailable.` have a tight 7-original-pixel gap. The original is 1080×2316,193,670 bytes, SHA256 `6db702285cf2c5734f91a157d08e9758da4f232a3251696affb77e9826bd5167`. The caption's measured gray ink ends at y1855 and the red error begins y1863. No XML hierarchy is present in this artifact; native View-bound overlap is **not proved**. An initial resized-preview concern about overlap is corrected by the unscaled original-pixel inspection. Source `TravelActivity` lines60 and117–124 places this caption above a footer whose top padding is0. A small footer top inset would be a bounded optional spacing adjustment, but no overlap repair or source mutation is justified from these pixels alone.

This archive records the actual selected native PASS and bounded visual observations at 1c1fe0c1. It is not current-source database 18 validation, a full application test result, an APK release/installation receipt or evidence that unsupported phone sleep-stage/apnea features are implemented. No phone, build, CI dispatch, scientific inference or data download beyond this authorized CI artifact occurred during the archive.
