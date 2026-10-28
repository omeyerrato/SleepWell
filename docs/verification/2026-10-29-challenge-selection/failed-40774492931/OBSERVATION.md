# First focused attempt

[Run 40774492931](https://github.com/omeyerrato/SleepWell/actions/runs/40774492931) at `b318d57069fa2b447c31c53a14693a17d483fc07` failed the single existing method after **16.862 seconds**. Three named originals were reached: legacy, catalog-empty and catalog-search. They and the diagnostic failure PNG/XML were individually inspected and are preserved unmodified here. The diagnostic is not an acceptance frame.

The failure is the new fixture's `By.desc("About this task")` selector. The actual focused destination is Spoken phrase configuration; its visible About this task text and chevron appear in both the original PNG and XML. The common `Design.item` exposes the label as a child text node within a clickable parent, rather than assigning that row a content description. The new fixture used the wrong selector and failed before tapping the row. This attempt therefore does not prove the information-page action, later Cancel/persistence assertions or the whole flow.

The bounded native correction waits for the real visible text label, checks the actual clickable ancestor's full measured/visible width and height and minimum 48dp bounds, then taps the visible child label. Other controls retain their existing full-bounds gates; no production route, source flag, text or behavior is changed to satisfy this selector. No unchanged rerun is used.
