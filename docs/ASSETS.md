# Asset provenance

## Contactless placement illustration

`app/src/main/res/drawable-nodpi/sonar_bedside.png` is original artwork generated
with the built-in image tool on 24 October 2026, using board06's middle-screen
illustration as a composition/style reference. The selected output is copied
unchanged into the repository. It contains no UI or measurement data. Native
controls and the live graph remain functional Android views.

Prompt: Standalone wide bedroom illustration, no screen, labels, controls, or
logos. Match the middle reference's cozy flat painted/paper-cut plum and lavender
room: amber lamp and dark plant at left, upright phone on a stable wooden bedside
table, purple bed and resting adult at right. Small lilac dots illustrate sound
reflections from phone toward torso. Keep the phone off the bed and pillow; use
soft restrained gradients, edge-to-edge landscape composition, and no medical
monitoring claim.

## Inter

`app/src/main/assets/fonts/Inter.ttf` is Inter from Google Fonts,
<https://github.com/google/fonts/tree/main/ofl/inter>, downloaded 2026-10-22.
Its SIL Open Font License is bundled beside the font as `OFL.txt`.

Font SHA-256: `29160a80ff49ddcab2c97711247e08b1fab27a484a329ce8b813d820dc559031`.
License SHA-256: `5b9321a4298cfeb6b34354164a1c3afc3db114569984c502b9b35d988fd58c57`.
The app uses variable weight settings and tabular time numerals.

## Bedside illustration

`app/src/main/res/drawable-nodpi/bedside_tonight.png` is a standalone original
image generated with the built-in image tool for this project. The initial
painting was rejected as too realistic; the selected second version follows
the supplied reference's flatter illustrated direction. It contains no text,
controls, charts or full-screen UI. All interactive elements remain native.

Final prompt: edit the original wide bedside strip into flat stylized 2D vector
editorial art, with crisp simple shapes and broad cel-shaded planes. Keep a
plum leafy plant at left, simple golden lamp center-left, phone on wooden table,
small peach hand entering from right in violet pajamas. Use deep plum upper
negative space to blend into the native panel. No realistic texture, fine
fabric detail, UI, text, labels, border or rounded frame. The Tonight board was
provided as a style reference, not flattened into an application screen.

The app loads the selected generated PNG unchanged. A native gradient over the
top edge blends it into the panel. Earlier variants remain outside the repo in
Codex's generated-images folder.

`bedside_predawn.png` and `bedside_dawn.png` beside that file are built-in
image-tool edits of the original, inspected and copied unchanged into the
project. They preserve the bedside composition instead of stretching a theme
thumbnail into the wide panel. Both contain only standalone decorative art.

Shared final prompt: preserve the exact wide composition and flat illustration
style: plant left, lamp left-middle, wooden bedside table below, hand setting a
black-screen phone down right-middle, sleeve/bed right. Preserve silhouettes,
positions, framing and pose; clean flat shading; sparse upper background; no
text, UI, symbols, logos, borders, realism or extra objects. Pre-dawn variant:
muted violet/lilac room, soft violet-pink ambient light, mauve plant and lavender
sleeves, upper background #5a467a. Dawn variant: cream/amber room, soft golden
ambient light, olive plant, taupe/cream sleeves, upper background #ffe5bc.

## Icons and landscapes

`Design.Icon`, `NightArc`, `DawnScene` and the three `ThemeScene` thumbnail color
studies contain original code-native paths.
They do not depend on emoji fonts or external icon CDNs. The plan arc displays
stored bedtime/alarm values, not sensor measurements. Existing WAV assets were
synthesized by `scripts/generate_audio.py`; no vendor sound packs are copied.

## Design boards

The 12 supplied boards are implementation references. Their fictional graph
values are never imported into production records. Actual phone screenshots
remain local under ignored `captures/` because they may contain private records
and system status metadata. They are not evidence of final acceptance until
the separate behavior and strict visual gates both pass.

## Tracking placement illustration

`app/src/main/res/drawable-nodpi/tracking_placement.png` is an original standalone
built-in image-tool illustration, visually inspected and copied unchanged. It
shows an uncovered phone on a plum/lilac mattress beside a warm lamp and plant.
Prompt direction: wide 2.8:1 flat vector editorial bedroom strip, simplified
cel-shaded planes, phone clearly resting on the bed, lilac movement marks,
plum negative space and amber lamp; no text, UI, border, logo or controls.
No third-party application asset was extracted. The supplied tracking board
provided composition/style guidance; its graph and UI were not imported.

SHA-256: `4f36867bceb626d9099456a4594d70f52cb67c648eaa4f58a66e683ff5d481ac`.

## Wake-up check dawn landscape

`drawable-nodpi/wake_dawn_landscape.png` is an original standalone built-in image-tool illustration, copied unchanged and used only as WakeCheck root background. It contains no UI or text. Full prompt, reference mapping, actual dimensions, SHA-256 and verification limits are recorded in [WAKE-DAWN-ART.md](WAKE-DAWN-ART.md). Native controls remain live over the illustration; the shared alarm `DawnScene` has not been replaced.

The native home widget uses the same unchanged Inter font bytes in
`app/src/main/res/font/inter_widget.ttf`, so Android RemoteViews can load them
through a font resource without changing the existing in-app asset loader.
The Inter source and OFL license above apply to both copies.

Counting shapes, maze geometry and the eight-country flag recognition illustrations in WakePuzzleView/TiltMazeView are original Canvas drawings shipped as source under this repository's MIT license. No external flag/image/emoji assets were downloaded; illustrations are recognition patterns, not legal flag construction specifications.

## Bundled semantic object classifier

Object recognition uses TensorFlow's EfficientNet-Lite0 INT8 mobile model, whose
embedded metadata identifies TensorFlow and Apache2.0. The exact model source,
input/output tensors, normalization, quantization, labels and SHA256 pins are in
[object_model_PROVENANCE.json](../app/src/main/assets/object_model_PROVENANCE.json).
The [Apache2.0 license](../app/src/main/assets/object_model_LICENSE.txt) and unchanged
metadata-extracted labels ship beside the model. This is independent of the
existing picture-similarity task and Google ML Kit Smile/Pose models.

Original challenge policy requires the chosen actual model label, score/margin
and a fresh multi-frame hold. Scores are not calibrated confidence. Dim-room,
household-object and camera accuracy remain unverified; photos/screens may pass.
The app retains no camera image or face/body template for this task.
