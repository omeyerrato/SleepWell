# Category appearance correction: rejected first attempt

[CI40093853208](https://github.com/omeyerrato/SleepWell/actions/runs/40093853208), source68b5570: FAIL. SoundCategoryFlowTest fails actual compositor/saved-original bar checks; FavoriteSoundsFlowTest hits a stale accessibility object before its first capture. Other functional classes retain their own passing results. Missing required images remain in the raw summary, not counted as accepted.

The owned category dialog shows white system glyphs/handle on a dimmed cream page. Reading the owner's current native appearance bits before showing it does not establish the page's intended policy during transitions. A subsequent source change declares the current per-window policy in a resource-keyed decor tag and uses that for owned dialogs, while fencing delayed callbacks against newer page policy. It still requires fresh native proof; current originals remain rejected. The stale Favorites interaction is investigated separately using the fully loaded owned view and one measured input, without action retries.

Original logs, diagnostics and available screenshots remain unchanged. No screenshot pixels or source evidence are rewritten. SHA256SUMS covers each raw copy. No phone or real audio effects occurred.
