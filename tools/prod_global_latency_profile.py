#!/usr/bin/env python3
"""PROD-GLOBAL-1D-0: opt-in, paid, whole-game latency/quality comparison.

Offline by default. --live explicitly permits up to nine OpenAI API calls.
The benchmark uses ONE legal eight-seat Trouble Brewing setup. Actual wake
sequence: Poisoner -> Investigator -> Chef (and then later information).
No real player data, credentials, HTTP headers or raw upstream errors are saved.

This diagnostic calls Responses API directly to vary per-request parameters.
Production Android still uses the authenticated Gateway, measured separately.
"""
from __future__ import annotations

import argparse
import importlib.util
import json
import os
from pathlib import Path
import stat
import time
import urllib.error
import urllib.request

HERE = Path(__file__).resolve().parent
SPEC = importlib.util.spec_from_file_location("profile_gateway", HERE / "storyteller_gateway.py")
gateway = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(gateway)
SMOKE_SPEC = importlib.util.spec_from_file_location("profile_fixtures", HERE / "prod_global_live_smoke.py")
smoke = importlib.util.module_from_spec(SMOKE_SPEC)
SMOKE_SPEC.loader.exec_module(smoke)
REPORT = HERE / ".private-botc-evaluation" / "prod_global_1d0_report.json"
ARMS = (
    ("default-full", None, False),
    ("low-full", "low", False),
    ("low-compact", "low", True),
)


def initial_case():
    """Committed setup; all eight shown and actual roles known; no invented night actions."""
    state = {
        "stage": "SETUP_COMMITTED", "hasDrunk": False,
        "drunkAssignmentSeat": "NOT_APPLICABLE", "seats": smoke.roster(None),
    }
    return {
        "schemaId": gateway.ANALYSIS_SCHEMA_ID, "schemaVersion": 1,
        "analysisIdentity": {
            "gameId": "profile-TB8", "scriptId": "trouble_brewing",
            "analysisId": "profile-TB8:setup",
        },
        "sourceRevision": {"gameStateRevision": 1, "playerInputRevision": 0},
        "state": state,
        "playerContext": [
            {"seat": seat, "experienceLevel": "NORMAL",
             "claimedRoleIds": [], "pressureLevel": None}
            for seat in range(1, 9)
        ],
        "historyCoverage": "SETUP_ONLY_NO_FUTURE_OBSERVATIONS",
        "priorStrategy": None,
    }


def investigator_after_poison(prior_strategy):
    # Poisoner acts first; no good information has been revealed yet.
    case = smoke.investigator_case([smoke.action(0, 1)], prior_strategy)
    case["identity"]["gameId"] = "profile-TB8"
    case["identity"]["decisionId"] = "profile-TB8:investigator"
    return case


def chef_after_manual_override(investigator_response):
    # A deliberately different Investigator pair was REALLY shown before Chef.
    legal = investigator_response["legalCandidates"]
    answer = investigator_response["_modelResult"]
    overridden = next(c for c in legal if c["candidateId"] != answer["primaryCandidateId"])
    pair_seen = smoke.observation(
        1, 3, "Investigator",
        {"type": "PAIR_INFORMATION", "shownRoleId": overridden["shownRoleId"],
         "candidateSeats": overridden["candidateSeats"]},
    )
    case = smoke.chef_case(
        [smoke.action(0, 1), pair_seen],
        answer["strategy"],
    )
    case["identity"]["gameId"] = "profile-TB8"
    case["identity"]["decisionId"] = "profile-TB8:chef"
    return case, overridden["candidateId"]


def validate_fixture(case):
    analysis = case["schemaId"] == gateway.ANALYSIS_SCHEMA_ID
    if analysis:
        gateway.validate_host_analysis_request(case)
        return
    gateway.validate_host_request(case)
    events = case["causalHistory"]["events"]
    source = case["decisionContext"]["abilityRoleId"]
    assert case["identity"]["gameId"] == "profile-TB8"
    assert events and events[0]["action"]["type"] == "POISON"
    if source == "Investigator":
        assert len(events) == 1
    elif source == "Chef":
        assert len(events) == 2
        assert events[1]["sourceAbility"] == "Investigator"
        assert events[1]["proposition"]["type"] == "PAIR_INFORMATION"
    else:
        raise ValueError("Unsupported benchmark stage")


def validate_output(case, upstream):
    analysis = case["schemaId"] == gateway.ANALYSIS_SCHEMA_ID
    allowed_seats = set(range(1, len(case["state"]["seats"]) + 1))
    if analysis:
        allowed = set()
    else:
        allowed = gateway.validate_host_request(case)
    parsed = gateway.parse_openai_response(upstream, allowed, allowed_seats, analysis)
    strategy = parsed["strategy"]
    # These are mechanical checks, NOT a subjective verdict on fairness/quality.
    linked = {seat for issue in strategy["issues"] for seat in issue["seats"]}
    return parsed, {
        "validStrictSchema": True,
        "legalAndDistinctCandidates": True if not analysis else "NOT_APPLICABLE",
        "issueCount": len(strategy["issues"]),
        "multiSeatIssueCount": sum(len(issue["seats"]) > 1 for issue in strategy["issues"]),
        "uniqueIssueSeats": len(linked),
        "relationCount": len(strategy["relations"]),
        "conditionalIntentions": len(strategy["intentions"]),
        "humanCoherenceReview": "NOT_YET_REVIEWED",
    }


def api_request(case, effort, compact, stream):
    """One paid request, no retry. All durations are measured at developer machine."""
    model = os.environ["OPENAI_MODEL"]
    api_key = os.environ["OPENAI_API_KEY"]
    payload = gateway.build_openai_request(
        case, model, reasoning_effort=effort, compact=compact
    )
    if stream:
        payload["stream"] = True
    encoded = json.dumps(payload, ensure_ascii=False).encode("utf-8")
    request = urllib.request.Request(
        gateway.API_URL, data=encoded, method="POST",
        headers={"Authorization": "Bearer " + api_key,
                 "Content-Type": "application/json"},
    )
    started = time.perf_counter()
    with urllib.request.urlopen(request, timeout=110) as response:
        got_headers = time.perf_counter()
        if stream:
            upstream, received_bytes, first_delta = read_sse(response, started)
        else:
            data = response.read(256_001)
            if len(data) > 256_000:
                raise ValueError("Upstream response size exceeds Gateway cap")
            received_bytes = len(data)
            first_delta = None
            upstream = json.loads(data)
    got_body = time.perf_counter()
    parsed, quality = validate_output(case, upstream)
    parsed_at = time.perf_counter()
    usage = upstream.get("usage") or {}
    metrics = {
        "totalMs": round((parsed_at - started) * 1000),
        "toHeadersMs": round((got_headers - started) * 1000),
        "bodyReadMs": round((got_body - got_headers) * 1000),
        "parseValidateMs": round((parsed_at - got_body) * 1000),
        "firstOutputDeltaMs": None if first_delta is None else round((first_delta - started) * 1000),
        "requestBytes": len(encoded),
        "responseBytes": received_bytes,
        "usage": {
            "inputTokens": usage.get("input_tokens"),
            "cachedInputTokens": (usage.get("input_tokens_details") or {}).get("cached_tokens"),
            "outputTokens": usage.get("output_tokens"),
            "reasoningTokens": (usage.get("output_tokens_details") or {}).get("reasoning_tokens"),
            "totalTokens": usage.get("total_tokens"),
        },
    }
    return parsed, metrics, quality


def read_sse(response, started):
    """Optional first-output-token diagnostic; SSE event data is bounded."""
    event_name, event_data = None, []
    total_bytes, first_delta = 0, None
    completed = None
    for line in response:
        total_bytes += len(line)
        if total_bytes > 1_000_000:
            raise ValueError("Streaming response too large")
        line = line.decode("utf-8").rstrip("\r\n")
        if line.startswith("event:"):
            event_name = line[6:].strip()
        elif line.startswith("data:"):
            event_data.append(line[5:].strip())
        elif not line:
            if event_name == "response.output_text.delta" and first_delta is None:
                first_delta = time.perf_counter()
            if event_name == "response.completed" and event_data:
                completed = json.loads("\n".join(event_data))["response"]
            if event_name in ("response.failed", "response.incomplete"):
                raise ValueError("Model streaming completion unsuccessful")
            event_name, event_data = None, []
    if completed is None:
        raise ValueError("Missing completed streaming response")
    return completed, total_bytes, first_delta


def private_report(path, report):
    # A private synthetic grimoire is not a public artifact and must not enter git.
    path.parent.mkdir(parents=True, exist_ok=True, mode=0o700)
    if path.parent.stat().st_mode & (stat.S_IRWXG | stat.S_IRWXO):
        raise ValueError("Report parent is not private")
    fd = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_TRUNC | os.O_NOFOLLOW, 0o600)
    os.fchmod(fd, 0o600)
    with os.fdopen(fd, "w", encoding="utf-8") as out:
        json.dump(report, out, ensure_ascii=False, indent=2)
        out.write("\n")


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--live", action="store_true", help="Opt in to up to N paid calls, no retries")
    p.add_argument("--max-calls", type=int, default=9, help="Hard cap 1-9, defaults to 9")
    p.add_argument("--stream", action="store_true", help="SSE first-output diagnostic; not identical to buffered Gateway")
    p.add_argument("--report", type=Path, default=REPORT)
    args = p.parse_args()
    if not 1 <= args.max_calls <= 9:
        p.error("--max-calls must be 1..9")
    initial = initial_case()
    validate_fixture(initial)
    # The continuation fixtures are valid before paying for any model response.
    pair = investigator_after_poison(None)
    validate_fixture(pair)
    mock_pair = {"legalCandidates": pair["legalCandidates"], "_modelResult": {
        "primaryCandidateId": pair["legalCandidates"][0]["candidateId"],
        "strategy": {"situationSummary": "offline fixture"},
    }}
    chef, _ = chef_after_manual_override(mock_pair)
    validate_fixture(chef)
    if not args.live:
        print("OFFLINE_PASS: same valid eight-player TB setup, Poisoner -> Investigator -> Chef,")
        print("Host-legal pair/numeric cases, strict response parser and three comparison arms.")
        print("No network or paid model call. Use --live --max-calls 9 to measure.")
        return 0
    if not os.environ.get("OPENAI_API_KEY") or not os.environ.get("OPENAI_MODEL"):
        p.error("Server-only OPENAI_API_KEY and OPENAI_MODEL are required")
    report = {
        "schemaId": "botc.prod-global-1d0-latency-quality",
        "schemaVersion": 1,
        "mode": "REAL_MODEL_SYNTHETIC_LEGAL_CHRONOLOGY",
        "model": os.environ["OPENAI_MODEL"],
        "transport": "streaming-diagnostic" if args.stream else "buffered-api",
        "notProductE2E": True,
        "qualityScope": "Mechanical gates automated; fairness/coherence require review",
        "calls": [],
    }
    used = 0
    for arm_name, effort, compact in ARMS:
        strategy = None
        investigator = None
        for step_name in ("initial-global", "post-poison-investigator", "post-override-chef"):
            if used >= args.max_calls:
                break
            if step_name == "initial-global":
                case = initial_case()
            elif step_name == "post-poison-investigator":
                case = investigator_after_poison(strategy)
            else:
                case, overridden = chef_after_manual_override(investigator)
            validate_fixture(case)
            # Count an attempted paid call before the network request (fail closed).
            used += 1
            item = {
                "arm": arm_name, "step": step_name, "modelEffort": effort or "API_DEFAULT",
                "outputProfile": "compact" if compact else "full",
                "callNumber": used, "status": "ATTEMPTED",
            }
            if step_name == "post-override-chef":
                item["confirmedManualInvestigatorOverride"] = overridden
            report["calls"].append(item)
            private_report(args.report, report)
            try:
                result, timings, quality = api_request(case, effort, compact, args.stream)
                item.update(status="VALID", timing=timings, quality=quality,
                            primaryCandidateId=result.get("primaryCandidateId"),
                            rationale=result.get("rationale"),
                            uncertainty=result.get("uncertainty"),
                            strategy=result["strategy"],
                            alternatives=result.get("alternatives"))
                if step_name == "initial-global":
                    strategy = result["strategy"]
                elif step_name == "post-poison-investigator":
                    investigator = {
                        "legalCandidates": case["legalCandidates"], "_modelResult": result
                    }
            except urllib.error.HTTPError as ex:
                item.update(status="API_HTTP_ERROR", httpStatus=ex.code)
                private_report(args.report, report)
                print(arm_name, step_name, "HTTP", ex.code)
                return 1
            except Exception as ex:
                item.update(status="ERROR", errorType=type(ex).__name__)
                private_report(args.report, report)
                print(arm_name, step_name, type(ex).__name__)
                return 1
            private_report(args.report, report)
            print(arm_name, step_name, timings["totalMs"], "ms",
                  timings["usage"]["outputTokens"], "output tokens")
        if used >= args.max_calls:
            break
    print("Completed", used, "attempted paid requests. Private report:", args.report)
    print("No secrets, raw headers or real player data in this report.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
