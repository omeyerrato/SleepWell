# S23 update — 28 October 2026

The authorized S23 Ultra update completed with `adb install -r` reporting
`Success`. The installed base APK SHA256 is exactly
`3b6c698fbfc1d71a21fa2e9b5420b7cf08ad39632608648e226482d11709a024`,
matching published **0.1.4 / code5 / source08d3911e** and the original signer.

Before replacement, the existing APK and full private app data were backed up
locally. No active alarm, tracking or unfinished night was present. The app was
not launched, cleared, uninstalled or granted additional permissions. No alarm,
sound, haptic, light or protection test ran on the phone.

After replacement, SQLite quick_check passed and schema17 remained unchanged.
Every original table's columns and rows matched, including both recorded nights,
426 epochs and49 sound-event records. User settings and private user files matched.

The raw first comparison flagged exactly two private API revision counters and
AndroidX's24-byte profile cache. Those differences remain recorded privately.
Classification checked that each empty-grant revision incremented by exactly one,
with grants/status/all other preferences unchanged; the ProfileVerifier format
and result stayed fixed while package timestamp/profile size changed. These are
expected package-replacement bookkeeping, not lost user data. No broad preference
or file exception was used. The original private backup is retained locally.

Later Contactless, Report gesture, Tonight, Automatic Tracking and imagegen work
is not part of this frozen APK. The newly generated raster icon is not installed
by this update; this package contains the previous icon and firmer tap preset.
