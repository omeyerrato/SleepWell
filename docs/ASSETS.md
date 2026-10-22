# Asset provenance

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

## Icons and landscapes

`Design.Icon`, `NightArc` and `DawnScene` contain original code-native paths.
They do not depend on emoji fonts or external icon CDNs. The plan arc displays
stored bedtime/alarm values, not sensor measurements. Existing WAV assets were
synthesized by `scripts/generate_audio.py`; no vendor sound packs are copied.

## Design boards

The 12 supplied boards are implementation references. Their fictional graph
values are never imported into production records. Actual phone screenshots
remain local under ignored `captures/` because they may contain private records
and system status metadata. They are not evidence of final acceptance until
the separate behavior and strict visual gates both pass.
