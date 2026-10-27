# Appearance: native layout and persistence evidence

[CI 40520155550](https://github.com/omeyerrato/SleepWell/actions/runs/40520155550)
passed the existing single Appearance journey in **53.315 seconds** at
`1ba182dba3f18c2bf57bd4d8ce1461b8230f0ef2`; its job took **5m7s**. The
[focused summary](focused-review-summary.json) reports ten required originals and
no failures. All ten PNGs below were opened individually against board 12's
Appearance screen and board 01's Tonight screen. The visual review found a
remaining large-text action-evidence gap, described below; this is not full
36-screen acceptance.

[The original environment](environment.json) pins the disposable API 36 x86_64
emulator at 1080×2316, density 420, normal system font scale. Native sounds and
audio imports were disabled, and host output was disabled. No physical phone,
lamp connection, alarm, recording or playback was exercised. Production sources
are unchanged between `795ab556` and this tested revision.

| Original frame | Observed result |
| --- | --- |
| [Automatic](appearance-automatic.png) | All reference settings, three complete theme compositions, real saved lamp summary and full Save action fit. All three lamp actions are fully visible at 48 dp. |
| [Midnight selected](appearance-selected-0.png) | Automatic is off, Midnight radio selected, full crescent retained in the compact preview. |
| [Pre-dawn selected](appearance-selected-1.png) | Manual Pre-dawn selection is clear; clouds and landscape remain fully composed. |
| [Dawn selected](appearance-selected-2.png) | Manual Dawn selection is clear; round sun/rays and foreground remain within the preview. |
| [Midnight Tonight](appearance-tonight-0.png) | Saved dark palette, readable clock/plan, illustrated panel, tracking action and four-tab navigation. |
| [Pre-dawn Tonight](appearance-tonight-1.png) | Saved purple palette and selected Tonight dock remain coherent. |
| [Dawn Tonight](appearance-tonight-2.png) | Saved warm palette with clear brown controls and full default-size tracking action. |
| [Large Tonight](appearance-large-contrast-tonight.png) | Larger text, contrasting ink and responsive stacked time rows render; the tracking action extends below the first viewport. |
| [Large Tonight action](appearance-large-contrast-tonight-action.png) | This is identical to the preceding frame: the old scrollIntoView check accepted a 19 px visible sliver. It does not prove full action reach. |
| [Reopened large Appearance](appearance-large-contrast.png) | Extra large, Reduce motion and High-contrast persist after relaunch. Theme artwork remains complete. Connect extends below this initial viewport and needs scrolling. |

The ten original per-frame layout JSONs are preserved losslessly in
[native-layouts.zip](native-layouts.zip). The default frame measures:

| Native element | Screen Y / height |
| --- | --- |
| Scroll viewport | 131–2048 px |
| Lamp card | 1520–2006 px |
| Sunrise duration | 1611–1737 px / 126 px = 48 dp |
| Maximum intensity | 1740–1866 px / 126 px = 48 dp |
| Connect light | 1869–1995 px / 126 px = 48 dp |
| Save appearance | starts at 2074 px |

[The instrumentation result](AppearanceFlowTest.txt) passes draft cancellation,
all three manual-theme saves and warm reopenings, expected native root colors,
the actual 1.3× Tonight title size, reduced-motion rendering, contrast ratios,
and preference persistence after relaunch. Original preference keys and values
are restored in cleanup. The attachment-triggered asynchronous summary read
completed on repeated openings; no static connection state replaced it.

**Remaining scope:** the old large-text scroll assertion was weaker than the
visual requirement. Native data shows Start sleep tracking at Y1949 with height
147 px but only 19 px visible; large Appearance Connect has only 88 of 126 px
visible. Both bodies have additional scroll range. The follow-up test-only change
`643cca2c` requires the whole owned native target (at least 48 dp) to be visible
before these same captures. Its execution is pending. This run closes default
Appearance layout and the stated persistence behavior, not full large-action
reach, every picker/More action, real smart-light communication, other display
sizes, physical-phone installation, or all 36 reference screens.
