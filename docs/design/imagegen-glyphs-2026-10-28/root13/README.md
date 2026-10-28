# Generated raster control icons

The user explicitly requested imagegen replacements for all SVG/vector artwork.
These thirteen separate built-in imagegen calls produced the final thirteen
alphabetical control glyphs. [Mapping](mapping.json) retains original hashes,
alpha bounds and all five Android density variants. Exact prompts and unchanged
originals are beside it; no SVG geometry was traced or rasterized.

Each original and its actual 24px rendering was inspected in the
[inspection sheet](qa-24px-and-104px.png). Shapes remain legible on cream; native
tint preserves light/dark/selected colors. Alpha has transparent corners and the
full 0–255 range. A first Stop generation incorrectly depicted a sleeping moon;
it was rejected and regenerated as the actual rounded square before packaging.
The accepted Stop original and prompt are the ones recorded here.

Density packaging uses only LANCZOS resizing, preserving generated alpha and
composition. Existing resource names, accessibility labels and actions stay in
place. These are asset checks, not Android screen or whole-app acceptance.
