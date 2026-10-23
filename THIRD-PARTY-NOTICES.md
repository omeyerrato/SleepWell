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
