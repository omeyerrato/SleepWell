"""Vendor a pinned Phosphor subset and convert its unchanged paths to Android vectors.

Default is offline/reproducible. --download fetches only these selected SVGs and
LICENSE from the pinned upstream commit. No downloaded code is executed.
"""
from pathlib import Path
import argparse
import concurrent.futures
import hashlib
import json
import urllib.request
import xml.etree.ElementTree as ET
from xml.sax.saxutils import quoteattr

ROOT = Path(__file__).resolve().parents[1]
PIN = "2b75f3ad12b420c9504ef05df8d2564a28f8500e"
REPO = "https://github.com/phosphor-icons/core"
VENDOR = ROOT / "third_party/phosphor"
# Filled silhouettes retain the reference's moon/gear/report/media vocabulary;
# the other controls use the matching Regular weight from the same icon family.
ICONS = {
    "back": ("arrow-left", "regular"), "up": ("caret-up", "regular"),
    "down": ("caret-down", "regular"), "meal": ("fork-knife", "regular"),
    "exercise": ("person-simple-run", "regular"), "tag": ("tag", "regular"),
    "chevron": ("caret-right", "regular"), "minus": ("minus", "regular"),
    "attention": ("exclamation-mark", "regular"), "plus": ("plus", "regular"),
    "moon": ("moon", "fill"), "report": ("chart-bar", "fill"),
    "settings": ("gear-six", "fill"), "sun": ("sun", "regular"),
    "sound": ("speaker-high", "regular"), "tasks": ("list-checks", "regular"),
    "bed": ("bed", "regular"), "battery": ("battery-vertical-medium", "regular"),
    "bell": ("bell", "regular"), "lock": ("lock-key", "regular"),
    "shield": ("shield-check", "regular"), "wave": ("waveform", "regular"),
    "fade": ("faders-horizontal", "regular"), "plug": ("plug", "regular"),
    "calendar": ("calendar-blank", "regular"), "github": ("github-logo", "fill"),
    "cloud": ("cloud", "regular"), "rain": ("cloud-rain", "regular"),
    "clock": ("clock", "regular"), "upload": ("upload-simple", "regular"),
    "file": ("file-text", "regular"), "palette": ("palette", "regular"),
    "motion": ("wave-sine", "regular"), "contrast": ("circle-half", "regular"),
    "bulb": ("lightbulb", "regular"), "info": ("info", "regular"),
    "phone": ("device-mobile", "regular"), "mic": ("microphone", "regular"),
    "speech": ("chats", "regular"),
    "pause": ("pause", "fill"), "play": ("play", "fill"),
    "tracking": ("spinner-gap", "regular"), "sonar": ("broadcast", "regular"),
    "stop": ("stop", "fill"), "check": ("check", "regular"),
    "search": ("magnifying-glass", "regular"), "close": ("x", "regular"),
    "more": ("dots-three-vertical", "regular"), "trash": ("trash", "regular"),
    "external": ("arrow-square-out", "regular"), "alarm": ("alarm", "regular"),
    "help": ("question", "regular"),
    "heart": ("heart", "regular"), "heartFilled": ("heart", "fill"),
    "vibrate": ("vibrate", "regular"), "flashlight": ("flashlight", "regular"),
}


def upstream_path(name, weight):
    return f"assets/{weight}/{name}{'-' + weight if weight != 'regular' else ''}.svg"


def resource(name, weight):
    return "ic_phosphor_" + name.replace("-", "_") + "_" + weight


def convert(data):
    # Phosphor ships the Regular/Fill silhouettes as filled paths. Retain their
    # exact geometry/winding; do not approximate curves or redraw icons.
    assert b"<!DOCTYPE" not in data and b"<!ENTITY" not in data
    root = ET.fromstring(data)
    assert root.tag == "{http://www.w3.org/2000/svg}svg"
    assert root.attrib == {"viewBox": "0 0 256 256", "fill": "currentColor"}, root.attrib
    lines = ['<!-- Phosphor Icons, MIT; see assets/licenses/PHOSPHOR-LICENSE.txt. Generated; do not redraw. -->',
             '<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp" android:height="24dp" android:viewportWidth="256" android:viewportHeight="256">']
    assert len(root) > 0
    for child in root:
        assert child.tag == "{http://www.w3.org/2000/svg}path", child.tag
        assert set(child.attrib) <= {"d", "fill-rule", "clip-rule"}, child.attrib
        assert child.get("d")
        winding = "evenOdd" if child.get("fill-rule") == "evenodd" else "nonZero"
        lines.append('    <path android:fillColor="#FF000000" android:fillType="' + winding + '" android:pathData=' + quoteattr(child.get("d")) + ' />')
    return "\n".join(lines + ["</vector>", ""])


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--download", action="store_true")
    args = parser.parse_args()
    paths = ["LICENSE"] + sorted({upstream_path(*value) for value in ICONS.values()})
    if args.download:
        def fetch(path):
            url = "https://raw.githubusercontent.com/phosphor-icons/core/" + PIN + "/" + path
            request = urllib.request.Request(url, headers={"User-Agent": "SleepWell-icon-vendor"})
            data = urllib.request.urlopen(request, timeout=30).read(65537)
            assert 0 < len(data) <= 65536, path
            target = VENDOR / path
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(data)
        with concurrent.futures.ThreadPoolExecutor(max_workers=6) as executor:
            pending = [(path, executor.submit(fetch, path)) for path in paths]
            errors = []
            for path, future in pending:
                try:
                    future.result()
                except Exception as error:
                    errors.append(path + ": " + str(error))
            if errors:
                raise RuntimeError("Failed selected assets: " + "; ".join(errors))
    hashes = {path: hashlib.sha256((VENDOR / path).read_bytes()).hexdigest() for path in paths}
    manifest = {"repository": REPO, "commit": PIN, "sha256": hashes,
                "aliases": {key: {"name": name, "weight": weight} for key, (name, weight) in ICONS.items()}}
    lock = VENDOR / "manifest.json"
    if not args.download:
        assert json.loads(lock.read_text(encoding="utf-8")) == manifest, "Vendored source or mapping differs from pinned manifest"
    drawables = ROOT / "app/src/main/res/drawable"
    drawables.mkdir(exist_ok=True)
    for name, weight in sorted(set(ICONS.values())):
        xml = convert((VENDOR / upstream_path(name, weight)).read_bytes())
        (drawables / (resource(name, weight) + ".xml")).write_text(xml, encoding="utf-8")
    lines = ["package org.sleepwell.app;", "", "/** Pinned Phosphor assets. Generated by scripts/import_phosphor_icons.py. */", "final class Icons {",
             "    static int resource(String name){", "        if(name==null)return R.drawable." + resource(*ICONS["help"]) + ";", "        return switch(name){"]
    for key, value in ICONS.items():
        lines.append('            case "' + key + '" -> R.drawable.' + resource(*value) + ';')
    lines += ["            default -> R.drawable." + resource(*ICONS["help"]) + ";", "        };", "    }", "}", ""]
    (ROOT / "app/src/main/java/org/sleepwell/app/Icons.java").write_text("\n".join(lines), encoding="utf-8")
    license_dir = ROOT / "app/src/main/assets/licenses"
    license_dir.mkdir(exist_ok=True)
    (license_dir / "PHOSPHOR-LICENSE.txt").write_bytes((VENDOR / "LICENSE").read_bytes())
    lock.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    print(f"Converted {len(set(ICONS.values()))} pinned icons for {len(ICONS)} semantic names; license packaged in APK.")


if __name__ == "__main__":
    main()
