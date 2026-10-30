# Failed report review, 5eb55a6e

This is historical failure evidence, not an accepted build. Run 41066938357, job 124100696629, artifact 12982941719. The full result and missing frame list are in review.json; one of five native methods failed when it expected an offscreen empty-state heading without scrolling. Four methods, including the complete report/edit/restore journey, passed.

Native screenshot inspection also exposed near-zero-width history duration and trend coverage columns, plus poorly wrapped supporting signal routes. The follow-up c1d4a75f uses explicit weighted widths, stacked destination copy and visible-bounds assertions. No model assertion was removed. These PNGs intentionally retain the defects for comparison.
