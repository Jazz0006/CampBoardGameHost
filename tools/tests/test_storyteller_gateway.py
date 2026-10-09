"""Offline PROD-1 gateway contract tests: no paid calls or listening socket."""
import importlib.util
import io
import os
from unittest import mock
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
        "strategy": {
            "situationSummary": "首夜厨师与调查员信息容易重叠，需全局考虑邪恶承压。",
            "issues": [{
                "issueId": "issue-1", "priority": 1, "seats": [1, 3, 4],
                "diagnosis": "厨师与调查员可能叠加信息压力",
                "futureEffect": "未来占卜等信息需延续多个可推理世界",
            }],
            "relations": [{
                "fromSeat": 1, "toSeat": 3, "issueId": "issue-1",
                "label": "信息交叉（假设，非已确认登记）",
            }],
            "intentions": [{
                "trigger": "若第一夜信息叠加",
                "approach": "在未来合法裁量时继续审查信息生态",
                "tradeoff": "不可为了保护邪恶凭空捏造事实",
            }],
            "planRevisionNote": "首次分析，尚无已确认的历史战略计划",
        },
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
        assert gateway.parse_openai_response(model_output(), legal, set(range(1, 6)))["primaryCandidateId"] in legal
        for bad in [
            model_output(primary="nonexistent"),
            model_output(alternative="nonexistent"),
            model_output(alternative="setup:drunk-seat:seat-1"),
            {"status": "incomplete", "output": []},
        ]:
            with self.assertRaises(ValueError):
                gateway.parse_openai_response(bad, legal, set(range(1, 6)))

    def test_global_diagnosis_must_precede_and_ground_action(self):
        live = case()
        allowed = gateway.validate_host_request(live)
        payload = gateway.build_openai_request(live, "model")
        assert payload["text"]["format"]["schema"]["required"][0] == "strategy"
        assert "FIRST" in payload["instructions"]
        answer = gateway.parse_openai_response(
            model_output(), allowed, set(range(1, 6))
        )
        assert answer["strategy"]["issues"][0]["seats"] == [1, 3, 4]
        for invalid in ("missing_issue", "wrong_seat", "wrong_relation", "no_intention"):
            bad = model_output()
            body = json.loads(bad["output"][0]["content"][0]["text"])
            if invalid == "missing_issue":
                body["strategy"]["issues"] = []
            elif invalid == "wrong_seat":
                body["strategy"]["issues"][0]["seats"] = [13]
            elif invalid == "wrong_relation":
                body["strategy"]["relations"][0]["issueId"] = "invented-issue"
            else:
                body["strategy"]["intentions"] = []
            bad["output"][0]["content"][0]["text"] = json.dumps(body)
            with self.assertRaises(ValueError):
                gateway.parse_openai_response(bad, allowed, set(range(1, 6)))

    def test_budget_bounds_paid_calls(self):
        budget = gateway.Budget(1)
        assert budget.acquire()
        assert not budget.acquire()

    def _fake_handler(self, access_token="valid-token"):
        handler = object.__new__(gateway.Handler)
        payload = json.dumps(case()).encode("utf-8")
        handler.path = gateway.PATH
        handler.headers = {
            "Authorization": "Bearer " + access_token,
            "Content-Length": str(len(payload)),
        }
        handler.rfile = io.BytesIO(payload)
        output = []
        handler.send_json = lambda status, body: output.append((status, body))
        return handler, output

    def test_gateway_timeout_returns_safe_error_without_leaking_secret(self):
        handler, output = self._fake_handler()
        env = {
            "OPENAI_API_KEY": "never-print-this",
            "OPENAI_MODEL": "model-id",
            "GATEWAY_ACCESS_TOKEN": "valid-token",
        }
        with mock.patch.dict(os.environ, env), \
             mock.patch.object(gateway.BUDGET, "acquire", return_value=True), \
             mock.patch.object(gateway.CONCURRENCY, "acquire", return_value=True), \
             mock.patch.object(gateway.CONCURRENCY, "release"), \
             mock.patch.object(gateway.urllib.request, "urlopen", side_effect=TimeoutError("secret body")):
            handler.do_POST()
        assert output == [(502, {"error": "model_unavailable_or_invalid"})]

    def test_gateway_wrong_token_is_rejected_before_model_call(self):
        handler, output = self._fake_handler(access_token="wrong-token")
        with mock.patch.dict(os.environ, {"GATEWAY_ACCESS_TOKEN": "valid-token"}), \
             mock.patch.object(gateway.urllib.request, "urlopen") as call:
            handler.do_POST()
            call.assert_not_called()
        assert output == [(401, {"error": "unauthorized"})]


if __name__ == "__main__":
    unittest.main()
