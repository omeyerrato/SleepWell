# Third-party notices

## Phosphor Icons

Copyright (c) 2023 Phosphor Icons. MIT license.

SleepWell bundles a selected subset from [phosphor-icons/core](https://github.com/phosphor-icons/core)
at commit `2b75f3ad12b420c9504ef05df8d2564a28f8500e`.
Original SVGs and the full [MIT notice](third_party/phosphor/LICENSE) are retained
under third_party/phosphor; hashes and semantic mappings are in
[manifest.json](third_party/phosphor/manifest.json). The same full license is
packaged in app/src/main/assets/licenses/PHOSPHOR-LICENSE.txt.

The SVG paths and winding are converted unchanged to native Android
VectorDrawable XML with a 256-unit viewport and 24dp nominal size. Icons remain
local, tintable and scalable; no web font, image service or runtime network
request is used. Regular and Fill weights come from the same family. The latter
retains the reference's moon/gear/chart/media silhouettes. Original hand-drawn
paths were removed from the shared Design.Icon renderer.

Regenerate offline with `python scripts/import_phosphor_icons.py`;
`--download` explicitly refetches only this pinned subset and license. No
upstream scripts or package-manager installation run during conversion.

## Google YAMNet mobile audio classifier

Copyright 2019 The TensorFlow Authors. Apache License 2.0.
The unchanged 4.13 MB mobile classification v1 model and its embedded 521 labels
are bundled for offline CPU inference. [Source, exact hashes, tensor contract and model card](third_party/yamnet/manifest.json).
The full license is retained in third_party/yamnet/LICENSE and packaged in
app/src/main/assets/licenses/YAMNET-LICENSE.txt. No runtime download or network request occurs.
The bundled LiteRT CPU runtime uses com.google.ai.edge.litert:litert:2.3.0.

## Public CC0 classifier evaluation audio (test APK only)

Two human-snoring recordings by SortsApostata and pencilgame1, a conversation/crowd recording by ZyryTSounds, and a solo count-down by toam (original voice credited to cognito perceptu) were published under CC0 1.0. [Source pages, preview hashes, decoded PCM hashes and conversion details](third_party/audio-fixtures/manifest.json); [license/provenance](third_party/audio-fixtures/LICENSE.md). They are never bundled in the production APK and are never played by the tests.
