# Experimental phone echo rhythm review

## Functioning flow

Enable **Breathing rhythm → Analyze periodic echoes**, save, and explicitly start a contactless night. The existing `EchoPeriodicity` analyzer stores one-minute results in `echo_periods`. Open that night's **Recorded signals → Breathing rhythm** entry to review the results. This change retains that existing route and storage format; it introduces no microphone, capture, service, manifest, dependency or backup changes.

`EchoPeriodsActivity` now offers:

- Median qualified periodic rate and the measured minimum–maximum range.
- Adjacent variation: RMS of rate differences across touching, qualified one-minute windows. No comparison spans a missing, rejected, paused, trimmed or excluded minute.
- Separate numerical charts for periodic rate and signed minute-to-minute change, using actual window timestamps. Each bar represents one actual minute; there is no interpolated curve or gap filling.
- Qualified/analyzed duration and a quality breakdown distinguishing insufficient signal, no stable period and conflicting periods. Missing signal is never called a breathing pause.
- All, qualified and unresolved filters, with 20-row pages that expose every eligible minute.
- Tappable chart intervals and list rows opening a real minute detail view: exact local times and recording time zone, original/reviewed context, quality reason, rate, preceding difference when eligible, and autocorrelation score. Previous/next recorded-minute actions retain the actual timeline, including gaps.
- Recreation state for original/reviewed mode, filter, chart mode, page, detail minute and scroll position. Back returns to the overview, then the calling report.
- Actual off/no-analysis, unsupported-source/algorithm, no-qualified-signal, empty-filter, missing-minute and load-error states.

The setting still uses the original save-only draft, revision conflict guard and asynchronous operation token. Information rows now open concise explanations rather than suggesting a nonexistent results action.

## Source and quality contract

Only `sonar_fmcw_v1` records with `echo_iq_periodicity_v1` runs are summarized. The derived helper validates ordered nonoverlapping 60-second windows, the persisted quality enum, finite correlation in [0,1], and qualified rates in [5,40] cycles/minute. It rejects malformed input rather than substituting numbers.

The existing estimator is unchanged: 23 reflection bins, 300 samples per minute after averaging five 40-ms frames, principal-axis projection, detrending, autocorrelation and full/half-window agreement. An estimate describes periodic reflections; breathing is only one possible source. Fans, other movement and other people can contaminate it. No clinical reference validation has been demonstrated.

Reviewed mode retains the existing full-window inclusion rule: a minute must lie wholly within the edited period and effective coverage after pauses/awake exclusions. Original mode uses original capture bounds. Consequently even a partial-minute trim removes that complete analysis minute; the screen never silently clips its measurement.

**Not implemented by this feature:** individual breath timing, breath-to-breath variability, apnea/hypopnea detection, oxygen saturation, EEG staging, or light/deep/REM labels. Adjacent variation is a descriptive echo-rate statistic, not a physiological or diagnostic threshold. There is no background start, new continuous audio archive or model download.

## Verification scope

Source and whitespace review performed in the feature worktree. No new unit-test campaign, device recording, alarm, sound playback, emulator session or screenshot run was performed by this agent. Integration compilation and any later quiet UI verification belong to the main task; visual parity and measured breathing accuracy are not claimed here.

## Deferred phone stage candidate

F049 remains unfulfilled. The privately inspected Apache-2.0 Nidra checkpoint at `devangk003/Nidra@2c4f17077f5e2a9c9f3cb1a0f50b3952f57253c7` was faithfully reconstructed, including the original EfficientNet ImageNet rescaling, then converted without changing trained weights. The checkpoint SHA-256 is `affbc5445f40d31c2109a4118f34f7fee177a4249bf403da0de10d8dfe2be873`.

Six predetermined short PSG-Audio excerpts all predicted Light, including expert-labelled Wake, N3 and REM intervals. Full and split converted graphs agreed closely, so this did not establish useful stage discrimination. Replay uses excerpt-global rather than whole-night normalization; training-subject exclusion is unknown, so these are neither held-out accuracy results nor proof that every use must fail. The model remains private and unaccepted: no weights, raw research audio, stage-capture scaffolding or predicted hypnogram is shipped. A usable phone-stage method still needs meaningful real-input behavior and a coherent trained preprocessing/time contract before integration.
