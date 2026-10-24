# Wake-up check dawn illustration

## Reference and intent

The third screen of supplied board 04 is the primary visual reference. The third screen of board 01 supplies the related dunes/sun/foreground-plant direction. Both were visually inspected alongside all 12 actual WakeCheck originals from CI 40054511743 before this change. The previous code-native bottom strip was visibly too simple: hard repeated cloud circles, two undifferentiated hills, sparse straight leaves and a small sun largely obscured by the footer.

The new original illustration is decorative only: warm golden sky, pale edge clouds, shaded dunes, a low sun and overlapping dark olive leaves. The central sky is quiet for separately rendered native data and controls. No board, phone frame, text, time, chart, timer ring, button, icon or status information is baked into the asset.

## Asset provenance

- File: `app/src/main/res/drawable-nodpi/wake_dawn_landscape.png`.
- Built-in image generation tool, generated 2026-10-24; no API/CLI fallback.
- Requested portrait 1024×2048; the returned original is 887×1774 RGB PNG, 1,482,160 bytes. It is copied into the repository unchanged, with no resize, recolor, crop or screenshot compositing of the saved file.
- SHA-256: `69e73f759b6bb0f2fe96fce996a4123de92934be436c608c3d1da8363827f6cc`.
- Original generated-image filename: `exec-d673df33-6ce8-4205-93bb-c5a01c7d81d9.png`.
- The supplied boards are composition/style references, not extraction sources. No third-party application artwork was extracted. The asset is distributed with this project's MIT-licensed assets under the repository LICENSE; retain that notice when redistributing the project.

## Final generation prompt

Use case: illustration-story. Asset type: original full-bleed portrait illustration, 1024×2048, for a native Android wake-up screen. The two supplied design-board images are STYLE AND COMPOSITION REFERENCES ONLY: use the golden dawn background behind the THIRD phone in board 04, and the warm layered dunes/plants behind the THIRD phone in board 01. Do not reproduce a board or any UI. Create one beautiful standalone dawn landscape with a calm warm golden sky, soft pale cream clouds entering only at the extreme left/right edges, softly shaded layered ochre/caramel sand dunes in the bottom quarter, a small pale sun partly concealed at the horizon, and lush broad dark olive/charcoal foreground leaves growing upward from the two bottom corners. Match the reference's cozy polished flat 2D editorial illustration, broad smooth shaded planes, graceful organic silhouettes, restrained depth, very subtle grain; not photorealistic, not primitive circles/triangles. Composition: tall portrait. Sky fills the top 70% and must be quiet and readable. Keep the central 65% of the image entirely quiet clear sky from y=5% through y=68%, reserved for separately rendered live native text and a countdown. Cream clouds at the far edges around y=20%, 38%, 52%, 62%, with no cloud crossing the central reading area. Dune horizon rises from the sides at about y=70% and layers descend toward y=84%; sun is small and low near y=74%, mostly hidden by dunes. Foreground plants should occupy only the outermost 15% on each side, rising to y=66%, with a few overlapping leaves and warm shaded tips. Bottom center can be simple warm sand because a separate native button panel will cover it. Palette: amber #ffc573 at the top, honey/apricot #ffcf86 and #ffdb9d lower sky, cream #ffeac4 clouds, warm ochre/gold/caramel dunes, dark desaturated olive leaves. No text, no lettering, no numbers, no UI, no buttons, no rings, no timer, no diagrams, no borders, no phone frame, no logo, no watermark. Do not crop or collage a screenshot; this is a new original illustration asset.

## Native rendering and scope

`WakeDawnBackground` draws the immutable resource bitmap as the root View's background, so root padding for status/navigation insets does not create a separate background band. Scaling preserves aspect ratio and center-crops overflow; it never stretches the portrait into a short landscape strip. A caller's bounds are clipped. Android Resources owns/caches the bitmap, and repeated state renders share its immutable pixels rather than decoding an image every countdown tick. The asset's decoded ARGB pixel budget is approximately 6 MiB.

Only the WakeCheck background assignment changes. Native countdown geometry, live ring progress, foreground sun/check glyphs, labels, buttons, permissions, saved state and confirmation/deadline behavior remain separate. `DawnScene` and `AlarmActivity` are unchanged; no alarm runtime has been launched or tested for this artwork task. The new scene can inform a later separately reviewed alarm-art pass without changing that prohibited runtime here.

## Verification boundary

The original RGB image was visually inspected for composition, no baked UI/text and quiet reading space. Dimensions, byte count and SHA-256 were read from the actual saved file; no pixels were modified for verification. Source review and build checks do not establish visual acceptance. Fresh actual Android WakeCheck screenshots across all parent-owned states are required before accepting placement, text contrast, safe-area coverage or parity with board 04. The previous failed-save Toast is a separate parent-owned feedback/capture issue and is not edited or hidden by this artwork change.

The supplied art direction takes precedence over a randomly dealt app-screen style. The installed design skill is applied here through reference inspection, original product-specific artwork and fresh native verification, without redesigning the screen or replacing its live data.
