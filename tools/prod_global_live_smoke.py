#!/usr/bin/env python3
"""Explicitly opt-in, paid PROD-GLOBAL-1C smoke via the deployed local Gateway.

Use on the Oracle developer machine:
  python3 tools/prod_global_live_smoke.py                # offline validate only
  python3 tools/prod_global_live_smoke.py --live         # at most three paid calls

All rosters/history are SYNTHETIC 8-player TB evaluation fixtures, not a claim
that Android generated the decisions. The Android UI-to-Gateway pass is separate.
Never put access tokens, OpenAI keys, or request headers into results.
"""
import argparse
import copy
import importlib.util
import json
import os
from pathlib import Path
import stat
import sys
import time
import urllib.error
import urllib.request

GATEWAY_MODULE = Path(__file__).with_name("storyteller_gateway.py")
spec = importlib.util.spec_from_file_location("botc_gateway_smoke_contract", GATEWAY_MODULE)
gateway = importlib.util.module_from_spec(spec)
spec.loader.exec_module(gateway)

ROLES = ("Chef", "Empath", "Investigator", "Fortune Teller",
         "Slayer", "Recluse", "Poisoner", "Imp")
DEFAULT_REPORT = Path.home() / ".local/share/botc-evaluation/prod-global-1c-llm-smoke-report.json"
ENDPOINT = "http://127.0.0.1:8765/v1/storyteller/recommend"
COVERAGE = {
    "MECHANICAL": {"state": "PARTIAL", "reasonCode": "SYNTHETIC_PREFIX"},
    "SOCIAL": {"state": "UNKNOWN", "reasonCode": "NOT_ENTERED"},
}


def roster(poisoned_seat):
    return [
        {"seat": i, "actualRoleId": role, "shownRoleId": role,
         "alive": True, "poisoned": i == poisoned_seat}
        for i, role in enumerate(ROLES, 1)
    ]


def action(sequence, target):
    return {
        "eventId": f"test-action-{sequence}", "kind": "action",
        "phase": "FIRST_NIGHT", "round": 1, "localSequence": sequence + 1,
        "globalSequence": sequence, "epistemicClass": "HOST_CONFIRMED_ACTION",
        "action": {"type": "POISON", "sourceSeat": 7,
                   "targetSeat": target, "visibility": "HOST_PRIVATE"},
    }


def observation(sequence, seat, role, proposition):
    return {
        "eventId": f"test-observation-{sequence}", "kind": "observation",
        "phase": "FIRST_NIGHT", "round": 1, "localSequence": sequence + 1,
        "globalSequence": sequence,
        "epistemicClass": "PLAYER_RECEIVED_OR_PUBLIC_INFORMATION",
        "sourceSeat": seat, "sourceAbility": role, "visibility": "PRIVATE",
        "recipientSeats": [seat], "receivedReliabilityLabel": "RECEIVED_AS_FUNCTIONING",
        "proposition": proposition,
    }


def base_case(game_id, poison_target, events, revision, prior=None):
    return {
        "schemaId": "botc.storyteller-provider-request", "schemaVersion": 1,
        "identity": {
            "gameId": game_id, "scriptId": "trouble_brewing",
            "decisionTypeId": "scalar-information", "decisionId": "",
        },
        "sourceRevision": {"gameStateRevision": revision, "playerInputRevision": 0},
        "state": {
            "stage": "RUNTIME", "phase": "FIRST_NIGHT", "round": 1,
            "hasDrunk": False, "drunkAssignmentSeat": "NOT_APPLICABLE",
            "seats": roster(poison_target),
        },
        "decisionContext": {},
        "legalCandidates": [],
        "playerContext": [
            {"seat": i, "experienceLevel": "NORMAL", "claimedRoleIds": [],
             "pressureLevel": None} for i in range(1, 9)
        ],
        "causalHistory": {
            "historyMode": "GLOBAL_V1", "cutoffSource": "LIVE_CAPTURED",
            "exclusiveGlobalSequence": len(events),
            "coverage": copy.deepcopy(COVERAGE),
            "events": copy.deepcopy(events),
        },
        "priorStrategy": copy.deepcopy(prior),
        "strategicPlanningScope": "GLOBAL_EVENT_DRIVEN_CONTINUATION",
        "coordinationHorizon": "CURRENT_DECISION_ONLY",
    }


def chef_case(events, prior=None):
    case = base_case("synthetic-A", 1, events, len(events) + 1, prior)
    case["identity"]["decisionId"] = "synthetic-A:chef:1"
    case["decisionContext"] = {
        "sourceSeat": 1, "abilityRoleId": "Chef", "reliability": "POISONED",
        "resultKind": "NUMBER", "metric": "ADJACENT_EVIL_PAIRS",
        "subjectSeats": list(range(1, 9)),
    }
    case["legalCandidates"] = [
        {"candidateId": f"chef-{n}", "resultValue": str(n)} for n in (0, 1)
    ]
    return case


def investigator_case(events, prior=None):
    case = base_case("synthetic-A", 1, events, len(events) + 1, prior)
    case["identity"].update(
        decisionTypeId="first-night-pair-information",
        decisionId="synthetic-A:investigator:1",
    )
    case["decisionContext"] = {
        "sourceSeat": 3, "abilityRoleId": "Investigator",
        "reliability": "RELIABLE",
    }
    case["legalCandidates"] = [
        {"candidateId": "investigator-6-7", "shownRoleId": "Poisoner",
         "candidateSeats": [6, 7], "semanticTruth": "TRUE",
         "registrationWitnessesArePossibilities": True},
        {"candidateId": "investigator-5-7", "shownRoleId": "Poisoner",
         "candidateSeats": [5, 7], "semanticTruth": "TRUE",
         "registrationWitnessesArePossibilities": True},
    ]
    return case


def fortune_teller_case():
    # Different game B: poison Fortune Teller (4), not Chef (1).
    # Confirmed information precedes this question, independent of model previews.
    events = [
        action(0, 4),
        observation(1, 1, "Chef", {"type": "NUMERIC_RESULT", "value": 1}),
        observation(2, 2, "Empath", {"type": "NUMERIC_RESULT", "value": 0}),
        observation(3, 3, "Investigator", {
            "type": "PAIR_INFORMATION", "shownRoleId": "Poisoner",
            "candidateSeats": [6, 7],
        }),
    ]
    case = base_case("synthetic-B", 4, events, 5)
    case["identity"]["decisionId"] = "synthetic-B:fortune-teller:5-6"
    case["decisionContext"] = {
        "sourceSeat": 4, "abilityRoleId": "Fortune Teller",
        "reliability": "POISONED", "resultKind": "BOOLEAN",
        "metric": "DEMON_OR_RED_HERRING_PRESENT", "subjectSeats": [5, 6],
    }
    case["legalCandidates"] = [
        {"candidateId": "fortune-yes", "resultValue": "true"},
        {"candidateId": "fortune-no", "resultValue": "false"},
    ]
    return case


def check_case(case):
    legal = gateway.validate_host_request(case)
    assert legal == {c["candidateId"] for c in case["legalCandidates"]}
    assert [e["globalSequence"] for e in case["causalHistory"]["events"]] == list(
        range(case["causalHistory"]["exclusiveGlobalSequence"])
    )
    return legal


def authenticate():
    token = os.environ.get("GATEWAY_ACCESS_TOKEN", "").strip()
    if not token:
        token_path = Path.home() / ".config" / "botc-gateway" / "token"
        if not token_path.exists():
            raise RuntimeError("Gateway token absent from environment and local token file")
        if token_path.stat().st_mode & (stat.S_IRWXG | stat.S_IRWXO):
            raise RuntimeError("Refusing world/group-accessible Gateway token file")
        token = token_path.read_text(encoding="utf-8").strip()
    if len(token) < 32:
        raise RuntimeError("Gateway token unexpectedly short")
    return token


def call_live(case, token):
    payload = json.dumps(case, ensure_ascii=False).encode("utf-8")
    request = urllib.request.Request(
        ENDPOINT, data=payload, method="POST",
        headers={"Content-Type": "application/json",
                 "Authorization": "Bearer " + token},
    )
    started = time.monotonic()
    try:
        with urllib.request.urlopen(request, timeout=110) as response:
            body = json.load(response)
    except urllib.error.HTTPError as exc:
        # Only the gateway's public error code, never the request or authentication.
        try:
            error = json.loads(exc.read(4096)).get("error", "unknown")
        except (json.JSONDecodeError, UnicodeError):
            error = "unknown"
        raise RuntimeError(f"Gateway HTTP {exc.code}: {error}") from None
    except (urllib.error.URLError, TimeoutError) as exc:
        raise RuntimeError(f"Gateway unavailable: {type(exc).__name__}") from None
    elapsed = round(time.monotonic() - started, 2)
    if body.get("schemaId") != "botc.storyteller-provider-response" or (
        body.get("decisionId") != case["identity"]["decisionId"]
    ) or body.get("sourceRevision") != case["sourceRevision"]:
        raise RuntimeError("Mismatched Gateway response identity/revision")
    legal = check_case(case)
    primary = body.get("primaryCandidateId")
    alternatives = body.get("alternatives", [])
    ids = [primary] + [x.get("candidateId") for x in alternatives]
    if not ids or set(ids) - legal or len(ids) != len(set(ids)):
        raise RuntimeError("Illegal or duplicate LLM legal candidate IDs")
    if len(legal) > 1 and len(ids) < 2:
        raise RuntimeError("Model omitted a distinct legal alternative")
    strategy = body.get("strategy", {})
    if not strategy.get("situationSummary") or not strategy.get("issues"):
        raise RuntimeError("Missing global situation diagnosis")
    return {"latencySeconds": elapsed, "response": body}


def emit_report(report, path):
    path.parent.mkdir(parents=True, exist_ok=True, mode=0o700)
    if path.parent.stat().st_mode & (stat.S_IRWXG | stat.S_IRWXO):
        raise RuntimeError("Report directory is accessible to other users")
    # Create only private, user-owned files. No keys/tokens/headers in report.
    fd = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_TRUNC | os.O_NOFOLLOW, 0o600)
    os.fchmod(fd, 0o600)
    with os.fdopen(fd, "w", encoding="utf-8") as stream:
        json.dump(report, stream, ensure_ascii=False, indent=2)
        stream.write("\n")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--live", action="store_true",
                        help="Explicit consent: send at most 3 paid model calls")
    parser.add_argument("--report", type=Path, default=DEFAULT_REPORT)
    args = parser.parse_args()
    first = chef_case([action(0, 1)])
    # For offline verification, verify the two follow-up contracts as well.
    test_observed = observation(
        1, 1, "Chef", {"type": "NUMERIC_RESULT", "value": 0}
    )
    for case in (first, investigator_case([action(0, 1), test_observed]),
                 fortune_teller_case()):
        check_case(case)
    if not args.live:
        print("OFFLINE_PASS: 3 sequential/scalar/pair/Boolean fixtures validate; no network or paid calls.")
        print("Run --live on the Oracle developer machine for actual model responses.")
        return 0

    report = {"schemaId": "botc.prod-global-1c-live-smoke",
              "schemaVersion": 1, "mode": "REAL_LLM_SYNTHETIC_HOST_FIXTURES",
              "note": "Not proof of Android UI end-to-end or original Host-emitted candidate IDs.",
              "modelLabel": os.environ.get("OPENAI_MODEL", "server-configured-unknown"),
              "usageTokens": "NOT_EXPOSED_BY_GATEWAY", "steps": []}
    token = authenticate()
    steps = [("A1 poisoned Chef candidate decision", first)]
    try:
        first_result = call_live(first, token)
        report["steps"].append({"name": steps[0][0], "request": first,
                                **first_result})
        print("A1 PASS, global plan:", first_result["response"]["primaryCandidateId"])
        selected_override = next(
            c["resultValue"] for c in first["legalCandidates"]
            if c["candidateId"] != first_result["response"]["primaryCandidateId"]
        )
        # A2 MUST use a confirmed MANUAL override, not the AI's proposed value.
        confirmed = observation(1, 1, "Chef", {
            "type": "NUMERIC_RESULT", "value": int(selected_override)
        })
        second = investigator_case(
            [action(0, 1), confirmed], first_result["response"]["strategy"]
        )
        second_result = call_live(second, token)
        report["steps"].append({
            "name": "A2 Investigator after confirmed manual override",
            "manualOverrideChefValue": int(selected_override), "request": second,
            **second_result,
        })
        print("A2 PASS, after manual Chef override to", selected_override,
              ":", second_result["response"]["primaryCandidateId"])
        third = fortune_teller_case()
        third_result = call_live(third, token)
        report["steps"].append({
            "name": "B1 FT Boolean after committed Chef/Empath/Investigator",
            "request": third, **third_result,
        })
        print("B1 PASS, global plan:", third_result["response"]["primaryCandidateId"])
        report["result"] = "LIVE_COMPLETED_3_REQUESTS"
        exit_code = 0
    except (RuntimeError, ValueError, KeyError, TypeError) as exc:
        report["result"] = "LIVE_INCOMPLETE"
        report["error"] = str(exc)[:350]
        print("INCOMPLETE:", report["error"], file=sys.stderr)
        exit_code = 1
    emit_report(report, args.report)
    print("Sanitized model report:", args.report)
    print("No API keys, Gateway tokens or HTTP Authorization headers in report.")
    return exit_code


if __name__ == "__main__":
    raise SystemExit(main())
