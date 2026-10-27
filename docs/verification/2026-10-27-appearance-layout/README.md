# Appearance: native layout, persistence and complete actions

[CI 40527204264](https://github.com/omeyerrato/SleepWell/actions/runs/40527204264)
passed the existing single Appearance journey in **68.224 seconds** at
`666b20c5490f5a6e9a5cd4429a3f02be0e3d6f27`; its job took **5m47s**. The
[focused summary](focused-review-summary.json) reports ten required original
images and no failures. All ten PNGs below were opened individually against
board 12's Appearance and board 01's Tonight. This closes the earlier full-action
scroll evidence gap at the stated runtime; it is not full 36-screen acceptance.

[The environment](environment.json) pins the disposable API 36 x86_64 emulator at
1080×2316, density 420, normal system font scale. Native sounds and audio imports
were disabled, and host output was disabled. No physical phone, lamp connection,
alarm, recording or playback was exercised. Production sources are unchanged
between `795ab556` and this tested revision.

| Original frame | Observed result |
| --- | --- |
| [Automatic](appearance-automatic.png) | All reference settings, three complete theme compositions, real saved lamp summary and full Save action fit. All three lamp actions are fully visible at 48 dp. Automatic follows the actual local clock; here Midnight is selected. |
| [Midnight selected](appearance-selected-0.png) | Automatic is off, Midnight radio selected, full crescent retained in the compact preview. |
| [Pre-dawn selected](appearance-selected-1.png) | Manual Pre-dawn selection is clear; clouds and landscape remain fully composed. |
| [Dawn selected](appearance-selected-2.png) | Manual Dawn selection is clear; round sun/rays and foreground remain within the preview. |
| [Midnight Tonight](appearance-tonight-0.png) | Saved dark palette, readable clock/plan, illustrated panel, tracking action and four-tab navigation. |
| [Pre-dawn Tonight](appearance-tonight-1.png) | Saved purple palette and selected Tonight dock remain coherent. |
| [Dawn Tonight](appearance-tonight-2.png) | Saved warm palette with clear brown controls and full default-size tracking action. |
| [Large Tonight](appearance-large-contrast-tonight.png) | Larger text, contrasting ink and stacked time rows render; the tracking action extends below the first viewport. |
| [Large Tonight action](appearance-large-contrast-tonight-action.png) | An actual swipe moves the content 154 px. The full 147 px tracking target is exposed above the dock, with readable label and icon. The action was not invoked. |
| [Reopened large Appearance](appearance-large-contrast.png) | Extra large, Reduce motion and High-contrast persist after relaunch. An actual swipe moves content 91 px, fully exposing Connect above Save. Theme compositions and lamp summary remain legible. |

Ten original per-frame layout JSONs are preserved losslessly in
[native-layouts.zip](native-layouts.zip). The complete-target checks use the owned
native View's measured width/height and its global visible rectangle, require at
least 48 dp, and physically scroll until the entire target clears the viewport.

| Native element | Screen Y / complete height |
| --- | --- |
| Default scroll viewport | 131–2048 px |
| Default lamp card | 1520–2006 px |
| Default Sunrise duration | 1611–1737 px / 126 px = 48 dp |
| Default Maximum intensity | 1740–1866 px / 126 px = 48 dp |
| Default Connect light | 1869–1995 px / 126 px = 48 dp |
| Save appearance, default and large | starts at 2074 px |
| Large Connect light after scroll | 1869–1995 px / 126 px = 48 dp; full width 596 px |
| Large Start sleep tracking after scroll | 1795–1942 px / 147 px = 56 dp; full width 1016 px |
| Large Tonight dock | starts at 1989 px |

[The instrumentation result](AppearanceFlowTest.txt) passes draft cancellation,
all three manual-theme saves and warm reopenings, expected native root colors,
the actual 1.3× Tonight title size, reduced-motion rendering, contrast ratios,
and preference persistence after relaunch. Original preference keys and values
are restored in cleanup. The attachment-triggered asynchronous summary read
completed on repeated openings; no static connection state replaced it.

The earlier [CI 40520155550](https://github.com/omeyerrato/SleepWell/actions/runs/40520155550)
passed its weaker scrollIntoView assertion while the large Tonight action had
only a 19 px visible sliver. Visual inspection caught that gap. This follow-up
keeps the same single test method and ten captures, and now checks complete
native bounds for both large-text actions. It changes verification only; the
production UI has not changed since `795ab556`.

This closes default Appearance layout, warm status loading, the stated theme and
preference persistence, and actual full large-action reach at this runtime.
Every picker/More action, real smart-light communication, other display sizes,
physical-phone installation and all 36 reference screens remain outside this
journey. At Extra large, content scrolls and the top navigation can leave the
viewport while the bottom action is exposed; text and targets are not shrunk to
force everything into one frame.
