"""Offline tests for API-0 runner. No network, credentials or Android dependencies."""
import json
import sys
import unittest
from pathlib import Path
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[2]))
from tools import gsp_api0_responses as api  # noqa: E402


class Api0OfflineTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.fixture = api.load_fixture(api.DEFAULT_FIXTURE)

    def test_canonical_same_for_all_arms(self):
        for checkpoint in self.fixture["checkpoints"]:
            prompts = [json.loads(api.prompt_case(self.fixture, checkpoint, arm)) for arm in api.ARMS]
            for part in ("canonical", "legal_candidates", "question", "phase", "script"):
                self.assertEqual(prompts[0][part], prompts[1][part])
                self.assertEqual(prompts[1][part], prompts[2][part])

    def test_no_future_identity_leak_into_d0(self):
        d0 = self.fixture["checkpoints"][0]
        for arm in api.ARMS:
            prompt = api.prompt_case(self.fixture, d0, arm)
            self.assertNotIn("postcommit_actual_role", prompt)
            self.assertNotIn("After seat 1 is chosen", prompt)
            self.assertNotIn("DrunkSelectionConfirmed", prompt)
            self.assertNotIn("strategic_notes_INTENT_NOT_FACT", prompt) if arm != "C" else None

    def test_arm_specific_facts_and_intent(self):
        d2 = self.fixture["checkpoints"][2]
        a = json.loads(api.prompt_case(self.fixture, d2, "A"))
        b = json.loads(api.prompt_case(self.fixture, d2, "B"))
        c = json.loads(api.prompt_case(self.fixture, d2, "C"))
        self.assertNotIn("prior_confirmed_facts", a)
        self.assertNotIn("strategic_notes_INTENT_NOT_FACT", a)
        self.assertIn("prior_confirmed_facts", b)
        self.assertNotIn("strategic_notes_INTENT_NOT_FACT", b)
        self.assertIn("prior_confirmed_facts", c)
        self.assertIn("strategic_notes_INTENT_NOT_FACT", c)
        self.assertTrue(all(
            item["kind"] == "INTENT_NOT_FACT" for item in c["strategic_notes_INTENT_NOT_FACT"]
        ))

    def test_nine_randomized_reproducible_cases(self):
        first = api.cases(self.fixture, 123)
        self.assertEqual(first, api.cases(self.fixture, 123))
        self.assertEqual(len(first), 9)
        self.assertEqual({(a["arm"], a["checkpoint"]) for a in first}, {
            (arm, d) for arm in api.ARMS for d in ("D0", "D1", "D2")
        })
        self.assertEqual(len({a["prompt_sha256"] for a in first}), 9)  # A/B/C have distinct input contracts

    def sample(self, candidate: str) -> dict:
        return {
            "primary_candidate_id": candidate,
            "alternatives": [
                {"candidate_id": "red-herring:3", "why_distinct": "Different downstream world"}
            ],
            "reasoning_summary": "Balanced alternatives",
            "impact_on_future_choices": "Reassess claims after day one",
            "memory_used": [],
            "uncertainties": [],
            "would_revise_prior_intent": "not_applicable",
            "revision_reason": "",
        }

    def test_response_parse_and_legal_validation(self):
        result = self.sample("red-herring:1")
        response = {
            "status": "completed",
            "output": [{"type": "message", "content": [
                {"type": "output_text", "text": json.dumps(result)}
            ]}],
        }
        parsed = api.parse_response(response)
        api.validate_recommendation(parsed, ["red-herring:1", "red-herring:3"])
        parsed["primary_candidate_id"] = "red-herring:8"
        with self.assertRaisesRegex(ValueError, "Primary candidate"):
            api.validate_recommendation(parsed, ["red-herring:1", "red-herring:3"])

    def test_disallow_duplicate_and_unattributed_memory(self):
        s = self.sample("red-herring:1")
        s["alternatives"][0]["candidate_id"] = "red-herring:1"
        with self.assertRaises(ValueError):
            api.validate_recommendation(s, ["red-herring:1", "red-herring:3"])
        s = self.sample("red-herring:1")
        s["memory_used"] = [{"source": "fabricated", "text": "misattributed"}]
        with self.assertRaises(ValueError):
            api.validate_recommendation(s, ["red-herring:1", "red-herring:3"])

    def test_refusal_and_incomplete_rejected(self):
        with self.assertRaisesRegex(ValueError, "Incomplete"):
            api.parse_response({"status": "incomplete", "output": []})
        with self.assertRaisesRegex(ValueError, "refused"):
            api.parse_response({"status": "completed", "output": [
                {"type": "message", "content": [{"type": "refusal", "refusal": "No"}]}
            ]})

    def test_no_network_by_default(self):
        with patch.object(api, "call_responses", side_effect=AssertionError("unexpected network")):
            self.assertEqual(api.main(["--fixture", str(api.DEFAULT_FIXTURE)]), 0)

    def test_request_is_nonpersistent(self):
        req = api.build_request("test-model", "{}")
        self.assertFalse(req["store"])
        self.assertEqual(req["text"]["format"]["type"], "json_schema")
        self.assertTrue(req["text"]["format"]["strict"])

    def test_output_cannot_enter_repository(self):
        with self.assertRaises(ValueError):
            api.output_path(str(api.ROOT / "docs" / "benchmarks"))


if __name__ == "__main__":
    unittest.main()
