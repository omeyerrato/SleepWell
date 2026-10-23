# Active parallel implementation — 23 October 2026

The full phone-app goal and all reference boards remain in scope. Completion is
not established. Each worker has an isolated managed Git worktree and branch.

| Worker | Branch / worktree | Assignment | Acceptance |
| --- | --- | --- | --- |
| Automatic sleep / travel | work/automatic-start / automatic-sleep; work/travel-plan / travel-plan | Automatic-start configuration regression, then board09 saved travel plan, actual zones/DST and opt-in silent reminders | Automatic source/pure tests integrated; current native reopen regression under repair. Travel source in progress; actual Service/device/overnight acceptance pending |
| Sound analysis / library | work/local-noise-analysis / sleep-analysis; work/sound-library / sound-library | Offline PCM inference, persisted events, real human fixture evaluation; board07 WAV waveforms/player, favorites and retention | Synthetic CPU, events UI and cue settings pass run39997627697. Real positive fixtures integrated for next CI; hardware capture and cue output unverified. Library in separate worktree |
| Report | work/report-reference / report-reference | Board08 history, trends and editing plus board01 per-night detail; inspect every original screenshot and correct deviations | Source integrated. Seven native originals produced; final reopen capture focus check failed. Agent correcting synchronization and visual differences; no completed-screen claim |
| Primary | main / existing checkout | Task navigation, themed pickers, shortening snoozes, integration and CI | Nine of twelve native classes pass run39997627697; selected-duration visibility test under repair. Full screen parity open |

No worker may execute alarms, microphone capture, tracking, vibration, flashlight,
power-menu or screen-off tests on the personal phone during the sleep restriction.
CI uses an isolated emulator with host audio disabled. Fixtures are not production
sample graphs. Source/test existence is not a passing-run claim.

The shared main branch receives reviewed commits through cherry-picks. Agents
must commit only their assigned files and leave shared integration files to the
primary worker. CI failures, missing screenshots and reference mismatches remain
open acceptance gates rather than being described as implemented features.
