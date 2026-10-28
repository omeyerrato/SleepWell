# First focused attempt

Run [40729220884](https://github.com/omeyerrato/SleepWell/actions/runs/40729220884), head `886cbb7a088ae1c2021a8edb23a0f9ea5d6c4206`, failed the single existing method after 4.58 seconds. No acceptance PNG was reached; `arc-failure.png` and its XML are diagnostics, not acceptance frames.

The first full-bounds check found the existing Tonight dock item measured 163 pixels high with 162 pixels visible. At density 420, the shared dock requests `dp(74) = 194` pixels while its separately rounded child and padding require `dp(62) + 2 * dp(6) = 163 + 32 = 195` pixels. This is a real one-pixel clipping discrepancy in the existing shared dock measurement. The native assertion is preserved rather than tolerated. The diagnostic screenshot shows the Tonight-specific fixed Start footer above the four-tab dock, with a continuous plum background; it does not prove the rest of the method.

Raw method log, environment, failed summary, diagnostic PNG/XML and run metadata are retained without rewriting their original bytes.
