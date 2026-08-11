import json
import tempfile
import unittest
from pathlib import Path

from oracle_harness import RunResult, classify, load_catalog, render_scenario, sha256_json


HERE = Path(__file__).parent
FIXTURES = HERE / "scenarios" / "trouble_brewing_a2.json"


class OracleHarnessTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.catalog = load_catalog(FIXTURES)

    def test_priority_catalog_is_machine_readable_and_broad(self):
        self.assertGreaterEqual(len(self.catalog["scenarios"]), 17)
        ids = {item["scenarioId"] for item in self.catalog["scenarios"]}
        self.assertIn("TB-WW-01", ids)
        self.assertIn("TB-LIB-03", ids)
        self.assertIn("TB-INV-03", ids)
        self.assertIn("TB-CHEF-02", ids)
        self.assertIn("TB-FT-03", ids)
        self.assertIn("TB-MAL-01", ids)
        self.assertIn("TB-SETUP-02", ids)

    def test_all_scenarios_render_without_android_or_third_party_code(self):
        for scenario in self.catalog["scenarios"]:
            rendered = render_scenario(self.catalog, scenario)
            self.assertIn(f"% generated scenario: {scenario['scenarioId']}", rendered)
            self.assertIn("#show oracle_output/1.", rendered)
            self.assertNotIn("com.codex", rendered)

    def test_render_is_deterministic(self):
        scenario = self.catalog["scenarios"][0]
        self.assertEqual(render_scenario(self.catalog, scenario), render_scenario(self.catalog, scenario))
        self.assertEqual(64, len(sha256_json(scenario)))

    def test_comparison_detects_agreement_and_unexplained_mismatch(self):
        scenario = {
            "expectedStatus": "SAT",
            "outputAssertions": [{"relation": "CONTAINS", "atoms": ["oracle_output(yes)"]}],
        }
        agree = RunResult("SAT", frozenset({"oracle_output(yes)"}), ("clingo",), 1)
        mismatch = RunResult("UNSAT", frozenset(), ("clingo",), 1)
        self.assertEqual("AGREE", classify(scenario, agree)[0])
        self.assertEqual("UNEXPLAINED_MISMATCH", classify(scenario, mismatch)[0])

    def test_not_run_is_never_translated_to_unsat(self):
        scenario = {"expectedStatus": "UNSAT"}
        result = RunResult("NOT_RUN", frozenset(), ("clingo",), 0, "timeout")
        comparison, details = classify(scenario, result)
        self.assertEqual("NOT_RUN", comparison)
        self.assertEqual(["timeout"], details)

    def test_catalog_round_trips_as_canonical_json(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "fixtures.json"
            path.write_text(json.dumps(self.catalog, ensure_ascii=False), encoding="utf-8")
            self.assertEqual(self.catalog, load_catalog(path))

    def test_ruleset_fields_match_a1_domain_constraints(self):
        for state in self.catalog["formalStates"].values():
            ruleset = state["rulesetRef"]
            self.assertRegex(ruleset["scriptContentHash"], r"^[0-9a-f]{32}$")
            self.assertIn(ruleset["coverage"], {"VERIFIED", "PARTIAL", "UNVERIFIED"})


if __name__ == "__main__":
    unittest.main()
