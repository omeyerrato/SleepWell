# Browsing saved sound categories

Sound events and global Favorites retain the three quick chips from board 07. The soft circular faders action opens an owned radio selector for All, Snoring, Speech, Cough, Baby crying, Laughter and Other sounds; the per-night selector also offers Favorites. Selecting a row applies the filter once. Cancel leaves the filter unchanged. An extended selection remains visible as a selected brown chip instead of leaving an unexplained unselected navigation row.

Counts, paging and rows use the effective user label when present. The original model category, score, version, timing and WAV remain unchanged. A user correction is still shown as **Your label**, while an automatic event still says **estimate**. Other sounds now means the actual Other/legacy category; it no longer silently includes cough, crying and laughter. Invalid old overlays fall back to the original category consistently in SQL and row presentation.

Category and Favorites date choices are remembered in a separate optional browsing namespace. Playback positions are not resumed automatically. Existing foreground-only guarded playback, missing-WAV behavior, independent stars and analysis-only rows are retained. Remembering a view does not modify sleep records, labels, retention rules or sound settings, and is not part of a portable sleep-data backup.

This expands browsing of saved observations and user corrections. It does not change the classifier or demonstrate broader automatic recognition accuracy; the cough/cry/laughter recognition qualification remains a separate, partial feature.

`SoundBrowseFilterTest` contains four pure tests. `SoundCategoryStorageTest` has two isolated SQLite methods covering six distinct categories, manual overlays, exact Other, date counts, category paging, invalid legacy overlays and unchanged original evidence/WAV bytes. `SoundCategoryFlowTest` has two native methods with fake playback ports and private database/view-state fixtures. It exercises real category/date controls, cancel, selected state, quiet explicit fake playback, recreation and reopening.

The fourteen required actual originals are `sound-category-{night-all,night-selector,night-cough,night-playing,night-recreated,night-reopened,night-other-empty,night-baby,album-selector,album-laughter,album-week,album-recreated,album-reopened,album-speech}.png`. Native execution and comparison with board 07 remain required; compilation and pure tests alone do not accept those screens.
