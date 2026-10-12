"""Offline contract tests for manual 3B2 probe; never call OpenAI."""
import contextlib
import io
import json
import os
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[2]))
from tools import prod_global_3b2_manual_probe as probe


class ManualExpertProbeTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.data = probe.load_fixture()
        cls.system = probe.instructions()

    def test_four_distinct_valid_fixture_shapes(self):
        self.assertEqual([f["id"] for f in self.data["fixtures"]],
                         ["E01", "E02", "E03", "E04"])
        self.assertIn("联合策略", self.system)
        for f in self.data["fixtures"]:
            self.assertEqual(len(f["input"]["circleSeats"]), 8)
            prompt = json.loads(probe.make_input(self.data, f))
            self.assertEqual(prompt["gameWorld"], f["input"])
            self.assertNotIn("graderOnly", prompt)
            self.assertNotIn("metadata", prompt)
            self.assertNotIn("legalCandidates", prompt)

    def test_request_has_one_model_no_storage_strict_output(self):
        f = self.data["fixtures"][0]
        req = probe.make_request(self.system, probe.make_input(self.data, f))
        self.assertEqual(req["model"], "gpt-5.6-luna")
        self.assertIs(req["store"], False)
        self.assertEqual(req["text"]["format"]["type"], "json_schema")
        self.assertIs(req["text"]["format"]["strict"], True)
        self.assertEqual(req["text"]["format"]["schema"]["required"],
                         ["asOfRevision", "openingPlan", "strategyMemory", "choiceSummary"])

    def test_default_preflight_makes_no_network_calls(self):
        output = io.StringIO()
        with patch.object(probe, "call_responses", side_effect=AssertionError("network")), \
             contextlib.redirect_stdout(output):
            self.assertEqual(probe.main([]), 0)
        manifest = json.loads(output.getvalue())
        self.assertEqual(manifest["networkCalls"], 0)
        self.assertEqual([x["case"] for x in manifest["plan"]],
                         ["E01", "E02", "E03", "E04"])

    def test_incomplete_and_refused_response_rejected(self):
        with self.assertRaisesRegex(ValueError, "Incomplete"):
            probe.parse_response({"status": "incomplete", "output": []})
        with self.assertRaisesRegex(ValueError, "refused"):
            probe.parse_response({"status": "completed", "output": [
                {"type": "message", "content": [{"type": "refusal"}]}]})

    def test_basic_check_does_not_pretend_host_legality(self):
        f = self.data["fixtures"][1]
        fake = {
            "asOfRevision": 1,
            "openingPlan": {
                "drunkSeat": None,
                "redHerringSeat": 2,
                "demonBluffRoles": ["Monk", "Mayor", "Soldier"],
                "informationIntents": [],
                "hostDetermined": [],
                "deferred": [
                    {"event": "poisoner.target", "sourceSeat": 7, "reason": "unknown"},
                    {"event": "fortune_teller.targets", "sourceSeat": 5, "reason": "unknown"},
                ],
            },
            "strategyMemory": {"tensions": [], "intentions": [], "uncertainties": []},
            "choiceSummary": "Synthetic shape check only",
        }
        self.assertEqual(probe.basic_checks(fake, f, self.data), [])
        fake["openingPlan"]["deferred"] = []
        self.assertIn("missing_poisoner_target_dependency",
                      probe.basic_checks(fake, f, self.data))
        fake["openingPlan"]["drunkSeat"] = 3
        self.assertIn("unexpected_drunk", probe.basic_checks(fake, f, self.data))

    def test_http_error_stops_paid_batch_without_retry(self):
        with tempfile.TemporaryDirectory() as folder:
            result_dir = str(Path(folder) / "private-result")
            with patch.dict(os.environ, {"OPENAI_API_KEY": "test-only-dummy",
                                      "OPENAI_MODEL": "gpt-5.6-luna"}), \
                 patch.object(probe, "call_responses", side_effect=RuntimeError("HTTP_400")) as calls, \
                 contextlib.redirect_stdout(io.StringIO()):
                code = probe.main(["--live", "--case", "all", "--max-calls", "4",
                                   "--output-dir", result_dir])
            self.assertEqual(code, 1)
            self.assertEqual(calls.call_count, 1)
            summary = json.loads((Path(result_dir) / "summary.json").read_text())
            self.assertEqual(summary["attempted"], 1)
            self.assertEqual(summary["samples"][0]["httpStatus"], "HTTP_400")

    def test_private_output_only_outside_repo(self):
        with self.assertRaisesRegex(ValueError, "outside"):
            probe.validate_output_dir(str(probe.ROOT / "private-test"))
        with tempfile.TemporaryDirectory() as folder:
            new_dir = Path(folder) / "new"
            self.assertEqual(probe.validate_output_dir(str(new_dir)), new_dir.resolve())
            new_dir.mkdir(mode=0o700)
            probe.private_json(new_dir / "result.json", {"a": 1})
            self.assertEqual(json.loads((new_dir / "result.json").read_text()), {"a": 1})
            with self.assertRaises(ValueError):
                probe.validate_output_dir(str(new_dir))

    def test_live_requires_explicit_max_calls_and_key(self):
        with patch.dict(os.environ, {"OPENAI_API_KEY": ""}):
            with contextlib.redirect_stderr(io.StringIO()):
                with self.assertRaises(SystemExit):
                    probe.main(["--live", "--case", "E01", "--max-calls", "1",
                                "--output-dir", "/tmp/explicit-private-test-3b2"])
        with contextlib.redirect_stderr(io.StringIO()):
            with self.assertRaises(SystemExit):
                probe.main(["--live", "--case", "E01"])


if __name__ == "__main__":
    unittest.main()
