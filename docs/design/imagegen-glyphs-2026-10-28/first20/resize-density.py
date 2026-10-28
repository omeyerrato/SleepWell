"""Reproduce only this set's density PNGs from untouched imagegen originals.

Run from repository root with Python + Pillow. No tracing, drawing, alpha
thresholding, recoloring, cropping, or generation happens in this script.
"""
from pathlib import Path
import json
from PIL import Image

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[3]
DENSITIES = {"mdpi": 24, "hdpi": 36, "xhdpi": 48, "xxhdpi": 72, "xxxhdpi": 96}

for item in json.loads((HERE / "mapping.json").read_text(encoding="utf-8"))["entries"]:
    resource = item["resource"]
    with Image.open(HERE / item["original_png"]) as original:
        assert original.mode == "RGBA" and original.width == original.height
        for density, pixels in DENSITIES.items():
            target = ROOT / f"app/src/main/res/drawable-{density}/{resource}.png"
            target.parent.mkdir(parents=True, exist_ok=True)
            original.resize((pixels, pixels), Image.Resampling.LANCZOS).save(target, optimize=True)
