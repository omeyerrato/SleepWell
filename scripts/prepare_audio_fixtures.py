"""Verify CC0 test audio offline; --download explicitly rebuilds from public previews."""
import argparse
import hashlib
import json
from pathlib import Path
import shutil
import subprocess
import tempfile
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
manifest = json.loads((ROOT / "third_party/audio-fixtures/manifest.json").read_text())
args = argparse.ArgumentParser()
args.add_argument("--download", action="store_true")
options = args.parse_args()
for item in manifest["fixtures"]:
    assert item["license"] == "CC0-1.0"
    destination = ROOT / "app/src/androidTest/assets/classifier-fixtures" / (item["name"] + ".pcm")
    if options.download:
        assert item["preview"].startswith("https://cdn.freesound.org/previews/")
        ffmpeg = shutil.which("ffmpeg")
        if not ffmpeg:
            raise SystemExit("FFmpeg is required only when rebuilding fixtures")
        with tempfile.TemporaryDirectory(prefix="sleepwell-cc0-") as folder:
            source = Path(folder) / "source.mp3"
            urllib.request.urlretrieve(item["preview"], source)
            assert hashlib.sha256(source.read_bytes()).hexdigest() == item["preview_sha256"], "Upstream preview changed"
            subprocess.run([ffmpeg, "-hide_banner", "-loglevel", "error", "-y", "-i", str(source), "-ac", "1", "-ar", "16000", "-c:a", "pcm_s16le", "-f", "s16le", str(destination)], check=True)
    data = destination.read_bytes()
    assert len(data) == item["samples"] * 2
    assert hashlib.sha256(data).hexdigest() == item["pcm_sha256"], "Fixture bytes differ from the pinned conversion"
    print(f"{item['name']}: {len(data)} verified bytes, {item['license']}")
