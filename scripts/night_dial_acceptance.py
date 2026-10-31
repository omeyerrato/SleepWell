"""Check/generate the finite Night Dial state register; never run app/tests/devices.

This is opt-in evidence accounting, not an implementation/release approval gate.
An unfinished classification is valid. Only structural/provenance errors fail.
"""
import argparse
from collections import Counter
from functools import lru_cache
import hashlib
import json
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
REGISTER = ROOT / "docs/night-dial/STATE-ACCEPTANCE.json"
SHORT = {
    "implemented_functional_current_evidence": "Pinned functional subset",
    "visual_only": "Entry/appearance only",
    "missing_local_behavior": "Missing local action",
    "external_physical_prerequisite": "External prerequisite",
    "unsupported_model_or_contract": "Unavailable contract",
}


@lru_cache(maxsize=None)
def tracked(path):
    local = ROOT / path
    if local.is_file():
        return local.read_bytes()
    return subprocess.check_output(["git", "show", "HEAD:" + path], cwd=ROOT)


@lru_cache(maxsize=None)
def source_tree(revision):
    tree = subprocess.check_output(["git", "ls-tree", "-r", revision, "app/src/main/java"], cwd=ROOT, text=True)
    return {line.split("\t", 1)[1]: line.split()[2] for line in tree.splitlines()}


def contracts(flows):
    return [{"id": f["id"], "title": f["title"], "states": [
        {"id": s["id"], "contract": s["contract"]} for s in f["states"]
    ]} for f in flows]


def registry_from_design(text):
    flows = []
    pattern = r"^### Flow (\d{2}): ([^\n]+)\n(.*?)(?=^### Flow |^## Feature coverage)"
    for match in re.finditer(pattern, text, re.M | re.S):
        states = re.findall(r"^\| (\d{2}[A-D]) \| (.*?) \|\s*$", match[3], re.M)
        flows.append({"id": match[1], "title": match[2].strip(), "states": [
            {"id": key, "contract": value.strip()} for key, value in states
        ]})
    return flows


def check(design=None):
    data = json.loads(REGISTER.read_text(encoding="utf-8"))
    expected = [f"{n:02d}{letter}" for n in range(1, 44) for letter in "ABCD"]
    states = [s for f in data["flows"] for s in f["states"]]
    assert [f["id"] for f in data["flows"]] == [f"{n:02d}" for n in range(1, 44)]
    assert [s["id"] for s in states] == expected, "Missing, duplicated or reordered registry state"
    assert data["flow_count"] == 43 and data["state_count"] == 172
    digest = hashlib.sha256(json.dumps(contracts(data["flows"]), ensure_ascii=False,
                                      separators=(",", ":")).encode()).hexdigest()
    assert digest == data["contract_source"]["registry_sha256"], "Contract snapshot changed"
    if design:
        text = Path(design).read_text(encoding="utf-8")
        assert registry_from_design(text) == contracts(data["flows"]), "Design registry differs"
        assert hashlib.sha256(text.encode()).hexdigest() == data["contract_source"]["full_file_sha256"]
    source = data["implementation_revision"]
    blobs = source_tree(source)
    for flow in data["flows"]:
        assert all(owner in blobs for owner in flow["implementation_family_owners"])
        for state in flow["states"]:
            impl = state["implementation"]
            assert impl["source_revision"] == source
            assert blobs[impl["owner"]] == impl["owner_blob"], state["id"] + " owner pin mismatch"
            assert all(path in blobs for path in impl.get("related_owners", []))
            assert state["classification"] in SHORT
            # Later acceptance needs an explicit exact-source state receipt, never a root render.
            if state["accepted_on_current_source"]:
                assert "pending" not in state
                accepted = [ref for ref in state["evidence"] if ref.get("state_action_acceptance") is True]
                assert accepted and all(data["evidence_catalog"][ref["catalog"]]["source_revision"] == source
                                        for ref in accepted), state["id"] + " lacks exact-source acceptance"
            for ref in state["evidence"]:
                evidence = data["evidence_catalog"][ref["catalog"]]
                pinned = source_tree(evidence["source_revision"]).get(impl["owner"])
                assert ref["implementation_owner_blob_at_evidence"] == pinned
                assert ref["owner_blob_unchanged"] == (pinned == impl["owner_blob"])
                rows = json.loads(tracked(evidence["png_manifest"]))
                index = {(row["file"], row["source"], row["run"]): row for row in rows}
                for png in ref["pngs"]:
                    row = index[(png, evidence["source_revision"], evidence["run"])]
                    assert re.fullmatch(r"[0-9a-f]{64}", row["sha256"])
            if "pending" in state:
                assert state["pending"]["status"] == "not_run"
    for key, evidence in data["evidence_catalog"].items():
        cls, method = evidence["method"].split("#", 1)
        log = tracked(evidence["native_log"]).decode("utf-8-sig")
        assert "class=" + cls in log and "test=" + method in log, key + " method absent from actual log"
        receipt = json.loads(tracked(evidence["receipt"]))
        assert receipt.get("passed") is True or (
            receipt.get("class_results") and all(item["passed"] for item in receipt["class_results"])
            and not receipt.get("failures")), key + " receipt is not passing"
        assert evidence["result"] == "pass" and evidence["units"] == "not_run"
    counts = Counter(s["classification"] for s in states)
    assert dict(counts) == data["classification_counts"]
    pending = json.loads(tracked(data["pending_report_plan"]["path"]))
    assert pending["native_status"] == "not_run_pending_integration"
    frames = [png for method in pending["methods"] for png in method["required_originals"]]
    assert len(pending["methods"]) == 3 and len(frames) == len(set(frames)) == 37
    assert [m["method"] for m in pending["methods"]] == data["pending_report_plan"]["methods"]
    accepted = sum(state["accepted_on_current_source"] for state in states)
    assert data["accepted_on_current_source_count"] == accepted
    assert not data["full_acceptance"] or accepted == 172
    return data


def write_summary(data):
    lines = ["# Finite Night Dial state register", "",
             "Exactly **43 flows / 172 unique state IDs**, copied from the accepted design interaction registry. "
             "This bounds implementation and verification work; it is not a design approval gate. "
             "The 172 state IDs are distinct from the separate F001–F172 feature inventory.", "",
             f"Implementation inspected at `{data['implementation_revision']}`. Latest pinned native evidence remains "
             "source351/run41212354914 plus its Appearance/recording-only runs, and source538/run41232644704. "
             "The new Report cohort is **pending: 3 existing methods / 37 original PNGs**. "
             "No source66 native pass, full-state checkbox or public release is inferred.", "",
             "## First local implementation assignments", ""]
    for item in data["prioritized_local_findings"]:
        lines += [f"- **{item['state']} — {item['owner']}.{item['symbol']} (line {item['source_line']})**: {item['finding']} {item['next_action']}"]
    lines += ["", "## How to read the finite classes", "",
              "| Class | States | Meaning |", "| --- | ---: | --- |"]
    for key, label in SHORT.items():
        lines += [f"| {label} | {data['classification_counts'][key]} | {data['classification_definitions'][key]} |"]
    lines += ["", "Functional evidence is a named **pinned subset**, never proof of every action in a state. "
              "Entry/appearance includes family roots or task configuration/workspace rendering and can have no "
              "state-specific image. Missing evidence does not mean missing implementation. External targets are "
              "finite: the named supported path plus admission, stop/cancel and stated unavailable alternative; "
              "no arbitrary future device combinations are added. External classification identifies a prerequisite; "
              "it does not certify every local branch in that state. An explicitly unsupported service/model is complete "
              "when its truthful unavailable/insufficient-data and supported alternative contract is satisfied; "
              "this register does not require invented sleep stages, clinical chronotype or external-player fade.", "",
              "[JSON register](STATE-ACCEPTANCE.json) carries verbatim contracts, source/blob owner pins, exact "
              "native method/log/run/revision, actual manifest-backed PNG names, narrow covered scope and remaining action. "
              "Owner-blob comparisons disclose file changes since evidence; matching one owner file does not prove "
              "all dependencies or current runtime behavior unchanged. "
              "Old cream screenshots and source-only proof do not establish Night Dial appearance. "
              "[351 evidence](verification/integrated-351b78bf/README.md), "
              "[538 evidence](verification/recovery-5387587a/README.md), "
              "[Report pending plan](REPORT-STATE-NATIVE-PLAN.json).", "",
              "## All registered states", "",
              "| State | Exact required action contract | Class | Implementation owner | Evidence scope |", "| --- | --- | --- | --- | --- |"]
    for flow in data["flows"]:
        for state in flow["states"]:
            owner = state["implementation"]["owner"]
            name = Path(owner).stem
            evidence = ", ".join(dict.fromkeys(ref["catalog"] for ref in state["evidence"]))
            if "pending" in state:
                evidence += "; Report37 pending"
            contract = state["contract"].replace("|", "\\|")
            lines += [f"| {state['id']} | {contract} | {SHORT[state['classification']]} | "
                      f"[{name}](../../{owner}) | {evidence} |"]
    lines += ["", "Check explicitly with `python scripts/night_dial_acceptance.py --check` or "
              "`python scripts/night_dial_inventory.py --check-states`. Add `--design PATH` to compare the "
              "accepted design registry/digest. Regenerate this summary with `--write-summary`. "
              "These commands only read repository evidence and write this summary when requested; "
              "they never run builds, unit/native tests, models, downloads or a device.", ""]
    (REGISTER.parent / "STATE-ACCEPTANCE.md").write_bytes("\n".join(lines).encode("utf-8"))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Validate structural and pinned evidence references only")
    parser.add_argument("--design", help="Optional accepted design.md to compare exactly")
    parser.add_argument("--write-summary", action="store_true", help="Regenerate the Markdown register from curated JSON")
    args = parser.parse_args()
    register = check(args.design)
    if args.write_summary:
        write_summary(register)
    print("43 flows / 172 unique states; pinned evidence and pending Report37 references valid. No app/test/device run.")
