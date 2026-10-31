# Travel reference pictograms

Generated30October2026 with built-in imagegen from supplied board09, inspected as complete outputs, and copied unchanged with alpha transparency into the app. These are decorative location/connection assets; native clock faces, timezone offsets, civil dates, DST differences and saved settings still come from the actual selected zones. The London landmark is used only for Europe/London. Other destinations keep the existing clock pictogram.

Assets:

- `app/src/main/res/drawable-nodpi/travel_london_landmark.png` —1280×1280; generated original `exec-ce7bfd2e-cd1c-460c-a7f4-e63f0097710c.png`.
- `app/src/main/res/drawable-nodpi/travel_clock_connector.png` —2172×724; generated original `exec-96a881c1-7636-446a-945c-981424eaafce.png`.

The originals remain in the session's `.codex/generated_images` directory. Project assets are not dependent on that directory. No SVG substitution or physical-device operation was used. Asset inspection is not Android screen acceptance; the existing focused Travel journey must capture the integrated layout and exercise its persisted choices.

## London landmark prompt

> Use case: background-extraction. Edit target/reference: the supplied SleepWell board09 PNG. Extract/recreate ONLY the small Big Ben / Elizabeth Tower pictogram inside the Target time zone London card in the RIGHT phone. Deliver ONE production raster icon on a truly transparent background, no cream disc (the Android app draws the disc). A clean, simple, upright flat dark chocolate brown (#54260f) silhouette with restrained cream cutouts for its two clock faces and tall windows, a narrow pointed spire, compact broad lower tower. Match the reference's recognisable modest hand-designed London landmark; crisp, restrained geometric forms readable at 26dp. Center the tower, occupy about 78% of a square canvas height, 10% transparent safe margin top/bottom. No gradients, shadows, lighting, 3D, texture, realism, extra buildings, outlines around canvas, letters, numerals, watermark, UI, phone border or other icons. Keep genuine alpha transparency. Not a logo redesign; this is the existing reference's London location marker.

## Connector prompt

> Use case: background-extraction. Reference/edit target: supplied SleepWell board09. Deliver ONLY the small decorative airplane-and-dotted-connection illustration between the two analog clocks in the RIGHT phone's Time difference section. A very wide 3:1 transparent canvas. Dark muted navy (#25445a) simple flat passenger-airplane silhouette, points right, centered horizontally in the upper half, modest size about one sixth of total canvas width. Under it, one perfectly straight horizontal row of small evenly-spaced round navy dots, from 8% to92% of canvas width, at75% canvas height. The airplane must be recognizable at 14dp and the dotted connection quiet. Match the reference's clean flat pictogram, no gradient, no shadows or textures, no clocks, no numbers or letters, no UI, no arrows, no dashed flight arc, no clouds, no cream disc, no phone border. Keep all background pixels genuinely transparent. The native Android app supplies live clock faces and actual numeric timezone difference; this asset supplies only the decorative plane and dotted connector. Fill the wide canvas with this arrangement, little empty outer padding.
