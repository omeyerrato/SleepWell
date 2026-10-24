"""Adversarial evidence checks only; no model, network, Android or audio outputs."""
import copy
import unittest
from evaluate_laughter_holdout import compare_android


class EvidenceIntegrityTest(unittest.TestCase):
    def fixture(self):
        windows = [{"start_ms": n * 975, "top8": [{"index": i, "score": .9 - i * .05} for i in range(8)]} for n in range(2)]
        host = {"one": {"frames": 2, "pcm_sha256": "same", "counts": {"baseline": {"noise": 1}}, "windows": windows}}
        archived = {"fixtures": [{"fixture": "one", "frames": 2, "pcm_sha256": "same", "counts": {"noise": 1}, "windows": copy.deepcopy(windows)}]}
        return host, archived

    def test_identical_complete_evidence_matches(self):
        host, archived = self.fixture()
        self.assertTrue(compare_android(host, archived, ["one"])["compatible"])

    def test_truncated_archived_windows_cannot_match_unchanged_declared_frames(self):
        host, archived = self.fixture()
        archived["fixtures"][0]["windows"].pop()
        self.assertFalse(compare_android(host, archived, ["one"])["compatible"])

    def test_truncated_host_windows_cannot_match(self):
        host, archived = self.fixture()
        host["one"]["windows"].pop()
        self.assertFalse(compare_android(host, archived, ["one"])["compatible"])

    def test_repeated_window_start_cannot_match_identical_scores(self):
        host, archived = self.fixture()
        archived["fixtures"][0]["windows"][1]["start_ms"] = 0
        self.assertFalse(compare_android(host, archived, ["one"])["compatible"])

    def test_duplicate_source_cannot_stand_in_for_missing_source(self):
        host, archived = self.fixture()
        archived["fixtures"].append(copy.deepcopy(archived["fixtures"][0]))
        self.assertFalse(compare_android(host, archived, ["one", "two"])["compatible"])

    def test_score_difference_is_retained_and_cannot_claim_parity(self):
        host, archived = self.fixture()
        archived["fixtures"][0]["windows"][0]["top8"][0]["score"] -= .0625
        result = compare_android(host, archived, ["one"])
        self.assertFalse(result["compatible"])
        self.assertEqual("top8_score", result["differences"][0]["reason"])

    def test_empty_top_ranking_cannot_vacuously_claim_score_parity(self):
        host, archived = self.fixture()
        archived["fixtures"][0]["windows"][0]["top8"] = []
        self.assertFalse(compare_android(host, archived, ["one"])["compatible"])

    def test_nan_and_boolean_scores_cannot_claim_parity(self):
        for invalid in (float("nan"), True):
            host, archived = self.fixture()
            archived["fixtures"][0]["windows"][0]["top8"][0]["score"] = invalid
            self.assertFalse(compare_android(host, archived, ["one"])["compatible"])

    def test_duplicate_label_cannot_hide_a_missing_ranked_score(self):
        host, archived = self.fixture()
        archived["fixtures"][0]["windows"][0]["top8"][1]["index"] = 0
        self.assertFalse(compare_android(host, archived, ["one"])["compatible"])


if __name__ == "__main__":
    unittest.main()
