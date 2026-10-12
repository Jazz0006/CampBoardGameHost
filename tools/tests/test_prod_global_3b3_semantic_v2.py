"""Offline-only contract evidence for PROD-GLOBAL-3B3 semantic V2.

Examples are synthetic structural witnesses, NOT frozen Luna outputs, strategy gold
answers or independently verified Host legal decisions.
"""
import copy
import json
from pathlib import Path
import unittest

from tools import prod_global_3b2_manual_probe as baseline
from tools import prod_global_3b3_semantic_v2 as v2

DATA = baseline.load_fixture()
CASES = {x["id"]: x for x in DATA["fixtures"]}


def info(seat, role, basis="FUNCTIONING", resolution="DECISION",
         mode="NONE", subjects=None, kind="NUMBER", number=0,
         witness_role=None, boolean=None, waiting=None):
    return {
        "decisionId": "first-night:seat-" + str(seat),
        "sourceSeat": seat, "abilityRoleId": role,
        "effectiveAbilityRoleId": None if basis == "DRUNK_MISINFORMATION" else role,
        "resolution": resolution, "basis": basis,
        "subject": {"mode": mode, "seats": subjects or []},
        "observation": {"kind": kind, "number": number if kind == "NUMBER" else None,
                        "roleId": witness_role if kind == "ROLE" else None,
                        "boolean": boolean if kind == "BOOLEAN" else None},
        "waitingForEvents": waiting or [],
    }


def synthetic(case: str):
    f = CASES[case]
    drunk = 2 if case == "E04" else None
    if f["input"]["hasDrunk"] and case != "E04":
        drunk = 1
    return {
        "schemaVersion": 2, "asOfRevision": f["input"]["asOfRevision"],
        "requestIdentity": v2.input_v2(DATA, f)["requestIdentity"],
        "openingPlan": {
            "drunkSeat": drunk,
            "redHerringSeat": 5 if case == "E04" else 4 if case == "E03"
                               else 3 if case == "E02" else 4,
            "demonBluffRoleIds": ["monk", "soldier", "mayor"] if case != "E04"
                                else ["monk", "ravenkeeper", "soldier"],
            "information": [],
            "deferredActions": [],
        },
        "strategyMemory": {
            "tensions": ["Distinct viewpoints can remain inferable"],
            "intentions": [{"trigger": "an actual event", "approach": "reassess outstanding decisions",
                            "risk": "avoid assuming future actions"}],
            "uncertainties": ["Pending player choices remain unknown"],
        },
        "tradeoffSummary": "Synthetic shape witness only; not strategic approval",
    }


class ExpertSemanticV2Test(unittest.TestCase):
    def test_role_ids_match_host_catalog_convention(self):
        self.assertEqual(v2.role_id("Fortune Teller"), "fortuneteller")
        self.assertEqual(v2.role_id("Scarlet Woman"), "scarletwoman")
        self.assertEqual(len(v2.ROLE_SET), 22)
        self.assertIn("fortuneteller", v2.ROLE_SET)

    def test_v2_input_allowlist_never_sends_grader_or_answer(self):
        for case in CASES.values():
            payload = v2.input_v2(DATA, case)
            serialized = json.dumps(payload)
            self.assertEqual(payload["schemaVersion"], 2)
            self.assertNotIn("graderOnly", serialized)
            self.assertNotIn("legalCandidates", serialized)
            self.assertNotIn("scenarioAim", serialized)
            self.assertEqual(payload["state"]["seats"][0]["experience"],
                             case["input"]["circleSeats"][0]["experience"])
            if case["input"]["hasDrunk"]:
                shown_town = set(payload["roleCatalog"]["Townsfolk"])
                for s in payload["state"]["seats"]:
                    if s["shownRoleId"] in shown_town:
                        self.assertEqual(s["actualRoleId"], "UNCOMMITTED_IF_DRUNK_SELECTED")
            req = v2.make_request_v2(DATA, case, "test-only-system")
            self.assertEqual(req["model"], "gpt-6-luna")
            self.assertFalse(req["store"])
            self.assertTrue(req["text"]["format"]["strict"])
            self.assertNotIn("graderOnly", req["input"])
            self.assertIn("requestIdentity", json.loads(req["input"]))

    def test_number_has_fixed_subject_not_target_choice(self):
        e03 = synthetic("E03")
        e03["openingPlan"]["information"] = [
            info(5, "empath", mode="FIXED_NEIGHBORS", kind="NUMBER", number=1)]
        self.assertEqual(v2.screen_v2(e03, CASES["E03"], DATA), [])
        old = copy.deepcopy(e03)
        old["openingPlan"]["information"][0]["subject"]["seats"] = [4, 6]
        self.assertIn("unexpected_subject_seats",
                      v2.screen_v2(old, CASES["E03"], DATA))

    def test_drunk_shown_role_kept_separate_from_actual_status(self):
        e04 = synthetic("E04")
        e04["openingPlan"]["information"] = [
            info(2, "librarian", basis="DRUNK_MISINFORMATION", mode="EVIDENCE_PAIR",
                 subjects=[5, 6], kind="ROLE", witness_role="recluse")]
        self.assertEqual(v2.screen_v2(e04, CASES["E04"], DATA), [])
        invalid = copy.deepcopy(e04)
        invalid["openingPlan"]["information"][0]["abilityRoleId"] = "librarian (drunk)"
        self.assertTrue(v2.validate_structure(invalid))
        invalid = copy.deepcopy(e04)
        invalid["openingPlan"]["information"][0]["basis"] = "FUNCTIONING"
        self.assertIn("drunk_basis_mismatch", v2.screen_v2(invalid, CASES["E04"], DATA))
        invalid = copy.deepcopy(e04)
        invalid["openingPlan"]["information"][0]["effectiveAbilityRoleId"] = "librarian"
        self.assertIn("effective_ability_mismatch",
                      v2.screen_v2(invalid, CASES["E04"], DATA))

    def test_future_poisoner_action_is_not_invented(self):
        e02 = synthetic("E02")
        e02["openingPlan"]["information"] = [
            info(4, "empath", resolution="DEFERRED", basis="STATUS_DEPENDENT",
                 mode="FIXED_NEIGHBORS", kind="NONE", waiting=["poisoner.target"])]
        e02["openingPlan"]["deferredActions"] = [
            {"decisionId": "event:poisoner.target", "event": "poisoner.target", "actorSeat": 7},
            {"decisionId": "event:fortune_teller.targets", "event": "fortune_teller.targets", "actorSeat": 5}]
        self.assertEqual(v2.screen_v2(e02, CASES["E02"], DATA), [])
        invalid = copy.deepcopy(e02)
        invalid["openingPlan"]["information"][0]["observation"]["kind"] = "NUMBER"
        invalid["openingPlan"]["information"][0]["observation"]["number"] = 0
        self.assertIn("deferred_observation_invented",
                      v2.screen_v2(invalid, CASES["E02"], DATA))

    def test_conditional_global_info_keeps_value_without_premature_publication(self):
        # Models may propose one coordinated as-if-unpoisoned plan, while the
        # Poisoner's actual target remains a causal dependency. No publication.
        e02 = synthetic("E02")
        e02["openingPlan"]["information"] = [
            info(1, "washerwoman", resolution="CONDITIONAL_INTENT",
                 basis="STATUS_DEPENDENT", mode="EVIDENCE_PAIR", subjects=[3, 4],
                 kind="ROLE", witness_role="empath", waiting=["poisoner.target"]),
            info(3, "chef", resolution="CONDITIONAL_INTENT",
                 basis="STATUS_DEPENDENT", kind="NUMBER", number=1,
                 waiting=["poisoner.target"]),
        ]
        e02["openingPlan"]["deferredActions"] = [
            {"decisionId": "event:poisoner.target", "event": "poisoner.target", "actorSeat": 7}]
        self.assertEqual(v2.screen_v2(e02, CASES["E02"], DATA), [])
        missing_dependency = copy.deepcopy(e02)
        missing_dependency["openingPlan"]["information"][1]["waitingForEvents"] = []
        self.assertIn("invalid_conditional_intent",
                      v2.screen_v2(missing_dependency, CASES["E02"], DATA))
        ungrounded_condition = copy.deepcopy(e02)
        ungrounded_condition["openingPlan"]["information"][0]["basis"] = "FUNCTIONING"
        self.assertIn("invalid_conditional_intent",
                      v2.screen_v2(ungrounded_condition, CASES["E02"], DATA))

    def test_host_determined_is_not_model_selected_observation(self):
        e03 = synthetic("E03")
        e03["openingPlan"]["information"] = [
            info(2, "chef", basis="RULE_DETERMINED", resolution="HOST_DETERMINED",
                 kind="NONE")]
        self.assertEqual(v2.screen_v2(e03, CASES["E03"], DATA), [])
        e03["openingPlan"]["information"][0]["observation"]["number"] = 1
        self.assertIn("nonempty_none_payload", v2.screen_v2(e03, CASES["E03"], DATA))

    def test_reject_fields_boolean_number_confusion_and_stale_revision(self):
        case = synthetic("E03")
        case["openingPlan"]["information"].append(
            info(5, "empath", mode="FIXED_NEIGHBORS", number=1))
        case["openingPlan"]["information"][0]["observation"]["boolean"] = True
        self.assertIn("invalid_number_payload", v2.screen_v2(case, CASES["E03"], DATA))
        case["openingPlan"]["information"][0]["observation"]["boolean"] = None
        case["asOfRevision"] = 99
        self.assertIn("stale_revision", v2.screen_v2(case, CASES["E03"], DATA))
        case["asOfRevision"] = 1
        case["openingPlan"]["information"][0]["sourceSeat"] = 8
        self.assertIn("source_ability_identity", v2.screen_v2(case, CASES["E03"], DATA))
        case = synthetic("E03")
        case["requestIdentity"]["gameId"] = "a-different-game"
        self.assertIn("stale_revision", v2.screen_v2(case, CASES["E03"], DATA))

    def test_legacy_structural_problems_reported_without_rewriting(self):
        sample = {"openingPlan": {"informationIntents": [
            {"sourceSeat": 3, "ability": "Empath", "kind": "NUMBER",
             "targetSeats": [2, 4], "value": 1},
            {"sourceSeat": 2, "ability": "Librarian (Drunk misinformation)",
             "kind": "PAIR_CHARACTER", "targetSeats": [5, 6]}]}}
        saved = copy.deepcopy(sample)
        report = v2.legacy_diagnostics(sample, CASES["E04"])
        self.assertEqual(len(report["findings"]), 2)
        self.assertFalse(report["rewritten"])
        self.assertEqual(sample, saved)

    def test_sealed_new_holdouts_without_gold_and_pair_level_contrast(self):
        suite = json.loads((v2.ROOT / "docs/PROD_GLOBAL_3B3_BLIND_HOLDOUTS_V1.json")
                           .read_text(encoding="utf-8"))
        cases = suite["fixtures"]
        self.assertEqual([f["id"] for f in cases], ["N01", "N02", "N03", "N04", "N05"])
        original_configs = {
            tuple(s["shownRole"] for s in case["input"]["circleSeats"])
            for case in DATA["fixtures"]}
        for f in cases:
            raw = json.dumps(f, ensure_ascii=False)
            self.assertNotIn("graderOnly", raw)
            self.assertNotIn("expectedPlan", raw)
            self.assertNotIn("goldAnswer", raw)
            world = f["input"]
            self.assertNotIn(tuple(s["shownRole"] for s in world["circleSeats"]),
                             original_configs)
            self.assertEqual([s["seat"] for s in world["circleSeats"]], list(range(1, 9)))
            role_counts = {group: sum(s["shownRole"] in roles
                                       for s in world["circleSeats"])
                           for group, roles in DATA["roleCatalog"].items()}
            self.assertEqual(role_counts, {
                "Townsfolk": 4 if world["hasDrunk"] else 5,
                "Outsider": 2 if world["hasDrunk"] else 1,
                "Minion": 1, "Demon": 1})
            self.assertEqual(len(v2.input_v2(DATA, f)["state"]["seats"]), 8)
        self.assertEqual(
            [s["shownRole"] for s in cases[1]["input"]["circleSeats"]],
            [s["shownRole"] for s in cases[4]["input"]["circleSeats"]])
        self.assertNotEqual(
            [s["experience"] for s in cases[1]["input"]["circleSeats"]],
            [s["experience"] for s in cases[4]["input"]["circleSeats"]])

    def test_strict_schema_rejects_unexpected_field(self):
        case = synthetic("E03")
        case["openingPlan"]["information"].append(info(5, "empath"))
        case["openingPlan"]["information"][0]["targetSeats"] = [4, 6]
        self.assertTrue(any("extra" in issue for issue in v2.validate_structure(case)))


if __name__ == "__main__":
    unittest.main()
