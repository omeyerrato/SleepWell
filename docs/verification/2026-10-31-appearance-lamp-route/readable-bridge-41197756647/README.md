# Corrected bridge navigation: follow-up actual frames

Run **41197756647**, frozen source **b19836918a17d2277c0e75cba618453d4e298b63**. Same existing AppearanceFlowTest method: **OK (1 test), 54.815 seconds**, 11 required originals, no summary failures. All 11 full native screenshots were individually opened. Original PNGs, actual layout JSON, native log, environment and summary are preserved unchanged; `originals.json` records their byte sizes and SHA-256 hashes.

## Observed correction

The actual Bedroom light destination now shows **Direct Philips Hue bridge** on a single readable line, with **Pair one light over your private LAN** directly underneath and a real chevron. The complete navigation target is x63–1017, y352–520: 954×168 physical pixels (64 dp high at density420). Title and subtitle share an actual 797px-wide column, x173–970. Their y ranges are 389–436 and 447–482. This replaces the prior four-line squeezed title. The same callback still opens the existing Hue route; this journey does not press that route or contact any lamp.

The native journey asserts the title is one line, its text region is at least220dp wide, the subtitle shares its column and sits below it, and the entire row is a visible target of at least48dp. It opens this actual configuration screen from the Appearance lamp header, then Back verifies the unsaved Dawn/manual draft remains and the saved preferences map is unchanged.

## Retained Appearance and Tonight layout

Automatic and all three saved manual themes retain board12's hierarchy, illustration selectors, selected rings, settings, lamp art and full fixed Save. Current normal and1.3 app-text layout positions are unchanged from the parent archive: all four lamp targets remain501×126px (48dp), with full visible rectangles; normal y1558–2062 and large y1533–2037. Fixed Save remains x63–1017,y2085–2232; Connect clears it by23px normal /48px large. Large text stacks lamp duration/intensity labels above values. The normal card clips4px of noninteractive bottom padding, without clipping a control. Theme-colored Tonight frames retain aligned four-tab navigation and full Start action, with a truthful no-alarm state. The large Tonight body remains scrollable above the fixed action/dock.

## Scope

The same method saves/reopens all three themes, recreates MainActivity and asserts real Tonight colors; it saves/reopens extra-large text, reduced motion and high contrast. Tracking is not started. Config body content continues below its viewport, including informational text cut at the scroll edge; the fixed Save is complete. We do not claim that every configuration control is visible or exercised in this one frame. There is no new large-text SmartLightActivity config capture; the captured1.3 state is Appearance/Tonight.

No physical phone, alarm, audio, haptic, flashlight, lamp request, connection credentials, external-service availability or restoration was exercised. Broader device/model gaps and full36-screen matching remain open. This archive records the bounded local correction and preserves the earlier defect archive for traceability.
