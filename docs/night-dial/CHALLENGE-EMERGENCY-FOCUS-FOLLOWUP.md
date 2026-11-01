# Tilt focus boundary follow-up — native acceptance pending

2026-11-01, reviewed source `98b633bdf42a5f91ab6f88fcd45c30ba549132aa`.
Independent source review found the active alarm Tilt supplier did not require
window focus. An owned options modal does not pause its Activity, so samples
could still move/checkpoint the maze behind the modal. This was a source-proven
admission defect; no physical alarm execution or failed native result is claimed.

`AlarmActivity.tiltMaze` now requires focus. Focus loss explicitly stops input,
retains only the already admitted maze position for the matching live occurrence
and unit, and removes the watchdog. The controller remains usable; returning
requires an explicit Calibrate action and fresh calibration. The watchdog's lost
owner path pauses without saving or permanently closing the controller. Failed
sensor cleanup remains blocked until cleanup succeeds; no extra listener is
acquired over uncertain cleanup.

The private Practice Tilt path already rejected input without focus. It now also
checkpoints the last admitted position before invalidating its capture lease and
closing the child controller. The new ticket checkpoint admission requires the
live matching child/run/unit and grants neither input nor completion authority.
The recreated controller returns without a listener and requires Calibrate.

The [updated bounded native plan](CHALLENGE-EMERGENCY-FOCUS-NATIVE-PLAN.json)
retains the six original emergency assertions/frames and adds two actual private
Tilt modal/return frames. It invokes the same existing method with
`routineSlice=emergency`. Stale sensor/watchdog input must leave the maze,
checkpoint and completed count unchanged; return must restore the position
without silently starting a sensor. No production alarm or phone app is launched.
The earlier six-frame plan remains as history. No build, hosted run, unit
campaign, phone action or output ran here; runtime and visual acceptance are
pending integration.
