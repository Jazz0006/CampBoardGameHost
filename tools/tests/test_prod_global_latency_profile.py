"""PROD-GLOBAL-1D-0 offline, charge-free profiling/chronology contract tests."""
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest
from unittest import mock

SCRIPT = Path(__file__).resolve().parents[1] / "prod_global_latency_profile.py"
SPEC = importlib.util.spec_from_file_location("botc_global_latency_profile", SCRIPT)
profile = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(profile)


class LatencyProfileTests(unittest.TestCase):
    def test_eight_seat_setup_and_official_first_night_prefix(self):
        setup = profile.initial_case()
        profile.validate_fixture(setup)
        self.assertEqual([s["seat"] for s in setup["state"]["seats"]], list(range(1, 9)))
        self.assertEqual(
            [s["actualRoleId"] for s in setup["state"]["seats"]],
            list(profile.smoke.ROLES),
        )
        pair = profile.investigator_after_poison(None)
        profile.validate_fixture(pair)
        self.assertEqual(len(pair["causalHistory"]["events"]), 1)
        self.assertEqual(pair["causalHistory"]["events"][0]["action"]["targetSeat"], 1)
        answer = {
            "legalCandidates": pair["legalCandidates"],
            "_modelResult": {
                "primaryCandidateId": "investigator-5-7",
                "strategy": {"situationSummary": "Conditional multi-seat strategy"},
            },
        }
        chef, override = profile.chef_after_manual_override(answer)
        profile.validate_fixture(chef)
        self.assertEqual(override, "investigator-6-7")
        self.assertEqual(
            [event.get("sourceAbility") for event in chef["causalHistory"]["events"]],
            [None, "Investigator"],
        )
        self.assertEqual(chef["decisionContext"]["reliability"], "POISONED")
        self.assertEqual(chef["priorStrategy"], answer["_modelResult"]["strategy"])
        invalid = json.loads(json.dumps(chef))
        invalid["causalHistory"]["events"][1]["sourceAbility"] = "Chef"
        with self.assertRaises(AssertionError):
            profile.validate_fixture(invalid)

    def test_real_model_profiles_keep_full_global_contract(self):
        live = profile.investigator_after_poison(None)
        baseline = profile.gateway.build_openai_request(live, "configured-model")
        self.assertNotIn("reasoning", baseline)
        self.assertIn("strict", baseline["text"]["format"])
        low = profile.gateway.build_openai_request(
            live, "configured-model", reasoning_effort="low",
        )
        lean = profile.gateway.build_openai_request(
            live, "configured-model", reasoning_effort="low", compact=True,
        )
        self.assertEqual(low["reasoning"]["effort"], "low")
        self.assertIn("BENCHMARK COMPACT REPORT", lean["instructions"])
        self.assertEqual(
            baseline["text"]["format"]["schema"],
            lean["text"]["format"]["schema"],
        )
        self.assertEqual(json.loads(lean["input"]), live)
        self.assertFalse(lean["store"])
        self.assertIn("priorStrategy", lean["instructions"])
        with self.assertRaises(ValueError):
            profile.gateway.build_openai_request(live, "configured-model", reasoning_effort="ultra")
        with self.assertRaises(ValueError):
            profile.gateway.build_openai_request(live, "configured-model", compact="true")

    def test_offline_default_never_opens_network(self):
        with mock.patch("sys.argv", ["prod_global_latency_profile.py"]), \
             mock.patch.object(profile.urllib.request, "urlopen", side_effect=AssertionError("network")):
            self.assertEqual(profile.main(), 0)

    def test_private_report_permissions_and_no_header_dump(self):
        with tempfile.TemporaryDirectory() as d:
            parent = Path(d) / "private"
            report = parent / "summary.json"
            profile.private_report(report, {"schemaId": "sanitized", "calls": []})
            self.assertEqual(report.stat().st_mode & 0o777, 0o600)
            self.assertEqual(parent.stat().st_mode & 0o777, 0o700)
            self.assertEqual(json.loads(report.read_text())["calls"], [])


if __name__ == "__main__":
    unittest.main()
