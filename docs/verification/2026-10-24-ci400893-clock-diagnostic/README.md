# Exact synthetic clock failure

[CI40089335942](https://github.com/omeyerrato/SleepWell/actions/runs/40089335942), source4646560: FAIL two of five methods pass; eight of fourteen required originals exist (plus a diagnostic image). Both storage cases end before an initial valid interval; one visible Night method also fails. Report still passes. All original diagnostic artifacts are retained.

The new diagnostics establish the late-evidence failure: CLOSED with 'Capture arrived after its interval was checkpointed'. One run has2277ms active time but only9120samples of generated capture; another1585ms has21120samples. The fake port's historical PCM ages against host elapsed CPU time during cold DSP work. No real hardware capture is involved. No production deadline, quality gate or late-evidence policy is weakened.

Source2477cbe corrects only the fixture clock/admission pairing and retains an explicit1.5second processing stall. Its separate CI40091982679 and fourteen original captures pass; do not relabel this original run green. The2dp real-marker source fix remains intact, and no missing samples are fabricated.

SHA256SUMS verifies every unmodified raw copy. No phone effects occurred.
