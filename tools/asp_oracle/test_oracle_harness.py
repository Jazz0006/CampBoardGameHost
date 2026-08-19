import json
import tempfile
import unittest
from pathlib import Path

from oracle_harness import FixtureError, RunResult, classify, load_catalog, render_scenario, sha256_json


HERE = Path(__file__).parent
FIXTURES = HERE / "scenarios" / "trouble_brewing_a2.json"


class OracleHarnessTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.catalog = load_catalog(FIXTURES)

    def test_priority_catalog_is_machine_readable_and_broad(self):
        self.assertGreaterEqual(len(self.catalog["scenarios"]), 45)
        self.assertLessEqual(len(self.catalog["scenarios"]), 60)
        ids = {item["scenarioId"] for item in self.catalog["scenarios"]}
        self.assertIn("TB-WW-01", ids)
        self.assertIn("TB-LIB-03", ids)
        self.assertIn("TB-INV-03", ids)
        self.assertIn("TB-CHEF-02", ids)
        self.assertIn("TB-FT-03", ids)
        self.assertIn("TB-MAL-01", ids)
        self.assertIn("TB-MAL-05", ids)
        self.assertIn("TB-MAL-08", ids)
        self.assertIn("TB-SETUP-02", ids)
        self.assertIn("TB-FT-04", ids)
        self.assertIn("TB-FT-05", ids)
        self.assertIn("TB-IMP-03", ids)
        self.assertIn("TB-KNOW-04", ids)

    def test_all_a0_scenarios_are_executable_contracts(self):
        required = {
            "TB-SETUP-01", "TB-SETUP-02", "TB-SETUP-03", "TB-SETUP-04", "TB-SETUP-05",
            "TB-WW-01", "TB-WW-02", "TB-LIB-01", "TB-LIB-02", "TB-LIB-03",
            "TB-INV-01", "TB-INV-02", "TB-INV-03", "TB-SPY-01", "TB-CHEF-01",
            "TB-CHEF-02", "TB-EMPATH-01", "TB-EMPATH-02", "TB-FT-01", "TB-FT-02",
            "TB-FT-03", "TB-MAL-01", "TB-MAL-02", "TB-IMP-01", "TB-IMP-02",
            "TB-IMP-03", "TB-SW-01", "TB-UT-01", "TB-RK-01", "TB-SLAYER-01",
            "TB-VIRGIN-01", "TB-SAINT-01", "TB-MAYOR-01",
        }
        actual = {item["scenarioId"] for item in self.catalog["scenarios"]}
        self.assertTrue(required <= actual)

    def test_all_scenarios_render_without_android_or_third_party_code(self):
        for scenario in self.catalog["scenarios"]:
            if scenario.get("oracleMode", "RUN") == "NOT_APPLICABLE":
                with self.assertRaises(FixtureError):
                    render_scenario(self.catalog, scenario)
                continue
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

    def test_authority_order_and_official_contract_are_explicit(self):
        self.assertEqual(
            ["OFFICIAL", "PROJECT_GOLDEN", "EXTERNAL_ORACLE"],
            self.catalog["authorityOrder"],
        )
        for scenario in self.catalog["scenarios"]:
            self.assertTrue(scenario["officialBasis"])
            self.assertTrue(scenario["officialAssertions"])
            self.assertIn(scenario["hypothesisMode"], {"ACTUAL_ONLY", "PLAYER_PERSPECTIVE"})

    def test_oracle_not_applicable_is_distinct_from_not_run(self):
        not_applicable = [
            scenario for scenario in self.catalog["scenarios"]
            if scenario.get("oracleMode", "RUN") == "NOT_APPLICABLE"
        ]
        self.assertTrue(not_applicable)
        self.assertTrue(all(scenario.get("oracleLimitation") for scenario in not_applicable))

    def test_red_herring_authority_boundary_is_frozen(self):
        scenarios = {item["scenarioId"]: item for item in self.catalog["scenarios"]}
        spy = scenarios["TB-FT-04"]
        recluse = scenarios["TB-FT-05"]
        self.assertEqual("UNSAT", spy["expectedStatus"])
        self.assertEqual("KNOWN_ORACLE_VARIANCE", spy["mismatchDisposition"])
        self.assertEqual("SAT", recluse["expectedStatus"])

    def test_poisoned_registration_authority_boundary_is_frozen(self):
        scenarios = {item["scenarioId"]: item for item in self.catalog["scenarios"]}
        for scenario_id in ("TB-MAL-05", "TB-MAL-06", "TB-MAL-07", "TB-MAL-08"):
            scenario = scenarios[scenario_id]
            self.assertEqual("UNSAT", scenario["expectedStatus"])
            self.assertEqual("KNOWN_ORACLE_VARIANCE", scenario["mismatchDisposition"])
            self.assertEqual("ACTUAL_ONLY", scenario["hypothesisMode"])
            self.assertEqual("numeric-info", scenario["query"]["kind"])


if __name__ == "__main__":
    unittest.main()
