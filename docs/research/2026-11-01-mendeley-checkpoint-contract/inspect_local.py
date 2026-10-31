"""Read already acquired primary ZIP/XML/JSON artifacts; no network or model runtime.

Usage: python inspect_local.py MENDELEY_DIR PINNED_GITHUB_DIR CHECKPOINT OUTPUT_JSON
Only local metadata is parsed. No upstream code, pickle, HDF5, formula or macro runs.
"""
from pathlib import Path
import hashlib
import json
import sys
import xml.etree.ElementTree as ET
import zipfile

MAIN = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
REL = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
NS = {"m": MAIN}


def receipt(path):
    body = path.read_bytes()
    return {"name": path.name, "bytes": len(body), "sha256": hashlib.sha256(body).hexdigest()}


def workbook(path):
    with zipfile.ZipFile(path) as archive:
        strings = []
        if "xl/sharedStrings.xml" in archive.namelist():
            strings = ["".join(item.itertext()) for item in ET.fromstring(archive.read("xl/sharedStrings.xml"))]
        book = ET.fromstring(archive.read("xl/workbook.xml"))
        targets = {item.attrib["Id"]: item.attrib["Target"] for item in ET.fromstring(archive.read("xl/_rels/workbook.xml.rels"))}
        sheets = []
        for sheet in book.find("m:sheets", NS):
            target = targets[sheet.attrib["{" + REL + "}id"]]
            target = target.lstrip("/") if target.startswith("/") else "xl/" + target
            element = ET.fromstring(archive.read(target))
            rows = element.findall("m:sheetData/m:row", NS)
            cells = []
            for row in rows:
                for cell in row:
                    value = cell.find("m:v", NS)
                    inline = cell.find("m:is", NS)
                    text = value.text if value is not None else "".join(inline.itertext()) if inline is not None else ""
                    if cell.attrib.get("t") == "s":
                        text = strings[int(text)]
                    cells.append({"cell": cell.attrib["r"], "value": text})
            dimension = element.find("m:dimension", NS)
            sheets.append({"name": sheet.attrib["name"], "dimension": dimension.attrib.get("ref") if dimension is not None else None,
                           "row_count": len(rows), "nonempty_cells": sum(bool(cell["value"]) for cell in cells),
                           "first_rows": [cell for cell in cells if int("".join(filter(str.isdigit, cell["cell"]))) <= 6]})
        media = [{"name": name, "bytes": len(archive.read(name)), "sha256": hashlib.sha256(archive.read(name)).hexdigest()}
                 for name in archive.namelist() if name.startswith("xl/media/")]
        return {"file": receipt(path), "sheets": sheets, "embedded_media": media}


def inspect(mendeley, github, checkpoint):
    inventory = json.loads((mendeley / "root-files.json").read_bytes())
    for item in inventory:
        pinned = item["content_details"]
        actual = receipt(mendeley / item["filename"])
        assert actual["bytes"] == pinned["size"]
        assert actual["sha256"] == pinned["sha256_hash"]
    tree_path = github / "Sleep-Apnea-Detection-Model-tree.json"
    tree = json.loads(tree_path.read_bytes())
    assert not tree["truncated"]
    notebook_path = github / "Sleep-Apnea-Detection-Model-Model_Sleep Apnea.ipynb"
    notebook = json.loads(notebook_path.read_bytes())
    code = [{"cell": index, "code_sha256": hashlib.sha256("".join(cell["source"]).encode()).hexdigest()}
            for index, cell in enumerate(notebook["cells"]) if cell["cell_type"] == "code"]
    body = checkpoint.read_bytes()
    blob = hashlib.sha1(b"blob " + str(len(body)).encode() + b"\0" + body).hexdigest()
    assert len(body) == 1090253 and blob == "df6ce773ae289d444c1ac42392717d4b17a165d1"
    with zipfile.ZipFile(checkpoint) as archive:
        metadata = json.loads(archive.read("metadata.json"))
        config = json.loads(archive.read("config.json"))
        members = [{"name": item.filename, "bytes": item.file_size, "sha256": hashlib.sha256(archive.read(item.filename)).hexdigest()}
                   for item in archive.infolist()]
    return {"schema": 1, "date": "2026-11-01", "actions": "Local ZIP/XML/JSON parsing only; no network, HDF5/runtime load, upstream execution, fit or inference",
            "mendeley_files": [receipt(path) for path in sorted(mendeley.iterdir()) if path.is_file()],
            "publisher_inventory": inventory, "publisher_folders": json.loads((mendeley / "folders.json").read_bytes()),
            "workbooks": [workbook(mendeley / name) for name in ["apnea_model_results.xlsx", "Raw_Data_All_Figures.xlsx"]],
            "github": {"repository": "SebasTS15/Sleep-Apnea-Detection-Model", "commit": "ad979ff72de4cb27a308ed03d56c3729a4396352",
                       "tree_receipt": receipt(tree_path), "complete_tree": tree, "notebook_receipt": receipt(notebook_path),
                       "code_cells": code, "license": receipt(github / "Sleep-Apnea-Detection-Model-LICENSE")},
            "checkpoint": {**receipt(checkpoint), "git_blob": blob, "metadata": metadata, "members": members, "config": config}}


if __name__ == "__main__":
    mendeley, github, checkpoint, output = map(Path, sys.argv[1:])
    output.write_text(json.dumps(inspect(mendeley, github, checkpoint), indent=2) + "\n", encoding="utf8")
