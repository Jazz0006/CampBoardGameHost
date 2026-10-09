"""Offline PROD-1 gateway contract tests: no paid calls or listening socket."""
import importlib.util
import json
from pathlib import Path
import unittest


MODULE = Path(__file__).resolve().parents[1] / "storyteller_gateway.py"
SPEC = importlib.util.spec_from_file_location("storyteller_gateway", MODULE)
gateway = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(gateway)


def case():
    roles = ("chef", "empath", "investigator", "poisoner", "imp")
    return {
        "schemaId": "botc.storyteller-provider-request",
        "schemaVersion": 1,
        "identity": {
            "gameId": "test-live-game",
            "scriptId": "trouble_brewing",
            "decisionTypeId": "drunk-assignment",
            "decisionId": "setup:drunk-seat:test-live-game",
        },
        "sourceRevision": {"gameStateRevision": 0, "playerInputRevision": 0},
        "state": {
            "stage": "SETUP_PRECOMMIT",
            "hasDrunk": True,
            "drunkAssignmentSeat": "UNCOMMITTED",
            "seats": [
                {
                    "seat": i,
                    "shownRoleId": role,
                    "actualRoleId": "UNCOMMITTED" if i <= 3 else role,
                    "alive": True,
                    "poisoned": False,
                }
                for i, role in enumerate(roles, 1)
            ],
        },
        "legalCandidates": [
            {"candidateId": "setup:drunk-seat:seat-1", "seat": 1, "shownRoleId": "chef"},
            {"candidateId": "setup:drunk-seat:seat-2", "seat": 2, "shownRoleId": "empath"},
        ],
        "playerContext": [
            {"seat": i, "experienceLevel": "NORMAL", "claimedRoleIds": [], "pressureLevel": None}
            for i in range(1, 6)
        ],
        "historyCoverage": "NOT_AVAILABLE_AT_SETUP_PRECOMMIT",
        "coordinationHorizon": "CURRENT_DECISION_ONLY",
    }


def model_output(primary="setup:drunk-seat:seat-1", alternative="setup:drunk-seat:seat-2"):
    answer = {
        "primaryCandidateId": primary,
        "rationale": "Consider the interaction of seats 1, 4 and 5.",
        "alternatives": [{"candidateId": alternative, "rationale": "Different clue economy"}],
        "uncertainty": ["No current social claims"],
    }
    return {
        "status": "completed",
        "output": [{"type": "message", "content": [
            {"type": "output_text", "text": json.dumps(answer)}
        ]}],
    }


class GatewayContractTests(unittest.TestCase):
    def test_full_precommit_roster_and_exact_candidates(self):
        live = case()
        assert gateway.validate_host_request(live) == {
            "setup:drunk-seat:seat-1", "setup:drunk-seat:seat-2"
        }
        encoded = gateway.build_openai_request(live, "configurable-model-id")
        assert encoded["model"] == "configurable-model-id"
        assert encoded["store"] is False
        prompt = json.loads(encoded["input"])
        assert len(prompt["state"]["seats"]) == 5
        assert prompt["state"]["seats"][2]["actualRoleId"] == "UNCOMMITTED"

    def test_reject_incomplete_shown_role_or_candidate_collision(self):
        live = case()
        live["state"]["seats"][2]["shownRoleId"] = "UNCOMMITTED"
        with self.assertRaises(ValueError):
            gateway.validate_host_request(live)
        live = case()
        live["legalCandidates"][1]["candidateId"] = live["legalCandidates"][0]["candidateId"]
        with self.assertRaises(ValueError):
            gateway.validate_host_request(live)

    def test_model_ids_checked_before_host_reply(self):
        legal = gateway.validate_host_request(case())
        assert gateway.parse_openai_response(model_output(), legal)["primaryCandidateId"] in legal
        for bad in [
            model_output(primary="nonexistent"),
            model_output(alternative="nonexistent"),
            model_output(alternative="setup:drunk-seat:seat-1"),
            {"status": "incomplete", "output": []},
        ]:
            with self.assertRaises(ValueError):
                gateway.parse_openai_response(bad, legal)

    def test_budget_bounds_paid_calls(self):
        budget = gateway.Budget(1)
        assert budget.acquire()
        assert not budget.acquire()


if __name__ == "__main__":
    unittest.main()
