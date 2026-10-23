# Bounded alarm wake settings evidence — 23 October 2026

Source: `69298aae20d63e4d9d3b751b44a4d1df0a0d30a9`.
[Silent Android run 39967552077](https://github.com/omeyerrato/SleepWell/actions/runs/39967552077)
passed five native flows. This folder preserves 39 original emulator PNGs,
five raw test logs and environment metadata; `sha256.json` hashes those 45 files.

The strict reviewer independently inspected all 39 originals. Accepted scope:
new settings controls, planned curves, picker and draft/recreation/save/reopen/
cancel behavior. Full Alarm sound board03 and the full app remained incomplete.
Offscreen helper copy and large-text wake controls were unverified. Later menu
and feature commits require their own acceptance; these images do not certify
changed source.

No alarm, playback, microphone, torch, vibration, power-menu or screen-off action
was exercised. Fake scheduling and isolated databases were used. Pure output
models do not certify native effects. No personal phone backup is published here.

A separate physical read-only root review passed, but preservation comparison
found three appearance preference changes during the interval. Alarm/record rows,
other preferences and preexisting user files were preserved. The origin of those
appearance changes was not established; they were left untouched. This folder
contains only emulator evidence.
