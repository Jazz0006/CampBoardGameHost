#!/usr/bin/env python3
"""PROD-1 authenticated private gateway for real Host Storyteller recommendations.

Bind to loopback only; publish externally ONLY behind an authenticated TLS reverse proxy.
OPENAI_API_KEY never leaves this server. Never log game payloads or API responses.
"""
from __future__ import annotations

import hmac
import json
import os
import threading
import time
import urllib.error
import urllib.request
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

API_URL = "https://api.openai.com/v1/responses"
PATH = "/v1/storyteller/recommend"
MAX_BODY = 64 * 1024
ANALYSIS_SCHEMA_ID = "botc.storyteller-global-analysis-request"
ANALYSIS_RESPONSE_SCHEMA_ID = "botc.storyteller-global-analysis-response"
COMPACT_MEMO_PROFILE = "COMPACT_MEMO_V1"
# The model must diagnose and coordinate the entire board BEFORE choosing the pending action.
# This is a provider-neutral strategic artifact; it never changes Host rules or GameState.
ISSUE_SCHEMA = {
    "type": "object",
    "properties": {
        "issueId": {"type": "string"},
        "priority": {"type": "integer"},
        "seats": {"type": "array", "items": {"type": "integer"}},
        "diagnosis": {"type": "string"},
        "futureEffect": {"type": "string"},
    },
    "required": ["issueId", "priority", "seats", "diagnosis", "futureEffect"],
    "additionalProperties": False,
}
RELATION_SCHEMA = {
    "type": "object",
    "properties": {
        "fromSeat": {"type": "integer"},
        "toSeat": {"type": "integer"},
        "label": {"type": "string"},
        "issueId": {"type": "string"},
    },
    "required": ["fromSeat", "toSeat", "label", "issueId"],
    "additionalProperties": False,
}
INTENTION_SCHEMA = {
    "type": "object",
    "properties": {
        "trigger": {"type": "string"},
        "approach": {"type": "string"},
        "tradeoff": {"type": "string"},
    },
    "required": ["trigger", "approach", "tradeoff"],
    "additionalProperties": False,
}
STRATEGY_SCHEMA = {
    "type": "object",
    "properties": {
        "situationSummary": {"type": "string"},
        "issues": {"type": "array", "items": ISSUE_SCHEMA},
        "relations": {"type": "array", "items": RELATION_SCHEMA},
        "intentions": {"type": "array", "items": INTENTION_SCHEMA},
        "planRevisionNote": {"type": "string"},
    },
    "required": ["situationSummary", "issues", "relations", "intentions", "planRevisionNote"],
    "additionalProperties": False,
}
SCHEMA = {
    "type": "object",
    "properties": {
        "strategy": STRATEGY_SCHEMA,
        "primaryCandidateId": {"type": "string"},
        "rationale": {"type": "string"},
        "alternatives": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "candidateId": {"type": "string"},
                    "rationale": {"type": "string"},
                },
                "required": ["candidateId", "rationale"],
                "additionalProperties": False,
            },
        },
        "uncertainty": {"type": "array", "items": {"type": "string"}},
    },
    "required": ["strategy", "primaryCandidateId", "rationale", "alternatives", "uncertainty"],
    "additionalProperties": False,
}
INSTRUCTIONS = (
    "You are an expert Blood on the Clocktower (Trouble Brewing) Storyteller STRATEGIST, "
    "not a standalone Drunk selector. You receive a complete PRECOMMIT board with ordered seats, "
    "shown and actual roles (some Townsfolk actual roles UNCOMMITTED), experience and exact legal choices. "
    "FIRST produce a concise whole-game situation diagnosis: identify the most important setup tension, "
    "evil-player pressure, combinations of Good information (Chef, Investigator, Empath, Fortune Teller etc. "
    "ONLY if actually present), alternate plausible worlds, and chain reactions across the coming night. "
    "THEN identify 1-4 high-impact, NON-DUPLICATE issues with stable issueId, seat references and the "
    "likely effects on future decisions. Map 0-4 crucial player-to-player strategic relations; each line "
    "MUST cite an existing issueId and actual seat numbers. These are HYPOTHESES, not registration facts. "
    "THEN propose 1-4 CONDITIONAL intentions: trigger, future approach and tradeoff. Do NOT promise "
    "future choices are already legal or predetermined. "
    "ONLY AFTER this global plan select the exact legal primaryCandidateId; tie its rationale explicitly "
    "to the diagnosed issues and conditional future effects, with a genuinely distinct legal alternative "
    "when there are multiple candidates. Aim for interesting, fair, solvable games, not a Good/Evil win. "
    "Do not rank roles independently, do not fall back to memorized named-role rules or a fixed score. "
    "NEVER treat UNCOMMITTED as known, invent claims/history/future events, or claim a unique Spy/Recluse "
    "registration witness when a displayed result has multiple legal explanations. "
    "History marked unavailable is UNKNOWN, not proof that no previous events occurred. "
    "Use concise natural Chinese, concrete seat-linked reasons, and admit uncertainty. "
    "Return all strategy fields in the required structured JSON. "
    "Never reveal private chain-of-thought; present a brief auditable decision summary instead."
)


def validate_host_request(case: dict) -> set[str]:
    if case.get("schemaId") != "botc.storyteller-provider-request" or case.get("schemaVersion") != 1:
        raise ValueError("Unsupported request version")
    ident = case["identity"]
    if ident["scriptId"] != "trouble_brewing" or ident["decisionTypeId"] not in (
        "drunk-assignment", "first-night-pair-information", "scalar-information", "mayor-redirect",
    ):
        raise ValueError("Unsupported decision family")
    if ident["decisionTypeId"] in ("first-night-pair-information", "scalar-information", "mayor-redirect"):
        return validate_live_global_decision_request(case)
    if not isinstance(ident["gameId"], str) or not ident["gameId"]:
        raise ValueError("Missing game identity")
    if not isinstance(ident["decisionId"], str) or not ident["decisionId"]:
        raise ValueError("Missing decision identity")
    revision = case["sourceRevision"]
    if not all(type(revision[k]) is int and revision[k] >= 0 for k in ("gameStateRevision", "playerInputRevision")):
        raise ValueError("Invalid source revision")
    state = case["state"]
    if state["stage"] != "SETUP_PRECOMMIT" or state["hasDrunk"] is not True:
        raise ValueError("Not a pending Drunk selection")
    seats = state["seats"]
    if not isinstance(seats, list) or len(seats) not in range(5, 16):
        raise ValueError("Incomplete roster")
    if [s["seat"] for s in seats] != list(range(1, len(seats) + 1)):
        raise ValueError("Noncanonical seat order")
    if any(not isinstance(s["shownRoleId"], str) or
           s["shownRoleId"] in ("UNKNOWN", "UNCOMMITTED", "NOT_APPLICABLE")
           for s in seats):
        raise ValueError("Missing shown role")
    legal = case["legalCandidates"]
    ids = [c["candidateId"] for c in legal]
    if not ids or len(ids) != len(set(ids)):
        raise ValueError("Empty or duplicate candidate IDs")
    for candidate in legal:
        seat = candidate["seat"]
        if type(seat) is not int or not 1 <= seat <= len(seats):
            raise ValueError("Out-of-roster candidate")
        if candidate["shownRoleId"] != seats[seat - 1]["shownRoleId"]:
            raise ValueError("Candidate display role mismatch")
        if seats[seat - 1]["actualRoleId"] != "UNCOMMITTED":
            raise ValueError("Candidate actual role must remain uncommitted")
    players = case["playerContext"]
    if [p["seat"] for p in players] != list(range(1, len(seats) + 1)):
        raise ValueError("Missing player context")
    return set(ids)


def validate_live_global_decision_request(case: dict) -> set[str]:
    """One engine-legal live decision, with the existing ordered, partial Host event prefix.

    No candidate ranking or role-specific replanning: every event remains a typed fact
    or observation, while social consequences and plans remain explicit uncertainty.
    """
    identity = case["identity"]
    revision = case["sourceRevision"]
    if not isinstance(identity.get("gameId"), str) or not identity["gameId"] or not isinstance(
        identity.get("decisionId"), str
    ) or not identity["decisionId"]:
        raise ValueError("Missing decision identity")
    if not all(type(revision.get(key)) is int and revision[key] >= 0
               for key in ("gameStateRevision", "playerInputRevision")):
        raise ValueError("Invalid source revision")
    if case.get("strategicPlanningScope") != "GLOBAL_EVENT_DRIVEN_CONTINUATION":
        raise ValueError("Missing global continuation scope")
    state = case["state"]
    if state.get("stage") != "RUNTIME" or type(state.get("hasDrunk")) is not bool:
        raise ValueError("Expected live runtime state")
    seats = state["seats"]
    if not isinstance(seats, list) or not 5 <= len(seats) <= 15 or [
        s.get("seat") for s in seats
    ] != list(range(1, len(seats) + 1)):
        raise ValueError("Incomplete ordered roster")
    for seat in seats:
        if any(not isinstance(seat.get(k), str) or seat[k] in
               ("UNCOMMITTED", "UNKNOWN", "NOT_APPLICABLE")
               for k in ("shownRoleId", "actualRoleId")):
            raise ValueError("Unknown role in committed live roster")
        if type(seat.get("alive")) is not bool or type(seat.get("poisoned")) is not bool:
            raise ValueError("Missing mechanical status")
    players = case["playerContext"]
    if not isinstance(players, list) or [p.get("seat") for p in players] != [
        s["seat"] for s in seats
    ]:
        raise ValueError("Incomplete player context")
    context = case["decisionContext"]
    if type(context.get("sourceSeat")) is not int or not 1 <= context["sourceSeat"] <= len(seats):
        raise ValueError("Invalid decision source seat")
    if not isinstance(context.get("abilityRoleId"), str) or not context["abilityRoleId"]:
        raise ValueError("Missing decision ability")
    if context.get("reliability") not in ("RELIABLE", "DRUNK", "POISONED", "NOT_APPLICABLE"):
        raise ValueError("Invalid reliability")
    is_target_decision = identity["decisionTypeId"] == "mayor-redirect"
    is_scalar_decision = identity["decisionTypeId"] == "scalar-information"
    if is_target_decision != (context["reliability"] == "NOT_APPLICABLE"):
        raise ValueError("Invalid decision reliability kind")
    if is_scalar_decision:
        if context.get("resultKind") not in ("NUMBER", "BOOLEAN") or not isinstance(
            context.get("metric"), str
        ) or not context["metric"]:
            raise ValueError("Invalid scalar information context")
        subjects = context.get("subjectSeats")
        if not isinstance(subjects, list) or any(type(s) is not int or s not in
            range(1, len(seats) + 1) for s in subjects) or len(subjects) != len(set(subjects)):
            raise ValueError("Invalid scalar information subjects")
    legal = case["legalCandidates"]
    if not isinstance(legal, list) or not legal:
        raise ValueError("Missing legal candidates")
    ids = []
    for item in legal:
        if not isinstance(item.get("candidateId"), str) or not item["candidateId"]:
            raise ValueError("Invalid legal ID")
        ids.append(item["candidateId"])
        if is_scalar_decision:
            value = item.get("resultValue")
            if not isinstance(value, str) or not value:
                raise ValueError("Missing scalar legal result")
            if context["resultKind"] == "BOOLEAN" and value not in ("true", "false"):
                raise ValueError("Illegal Boolean representation")
            if context["resultKind"] == "NUMBER" and (
                not value.lstrip("-").isdigit() or str(int(value)) != value
            ):
                raise ValueError("Illegal numeric representation")
            if any(key in item for key in ("targetSeat", "candidateSeats", "semanticTruth")):
                raise ValueError("Scalar result must not masquerade as another decision")
        elif is_target_decision:
            if type(item.get("targetSeat")) is not int or item["targetSeat"] not in range(1, len(seats) + 1):
                raise ValueError("Invalid target decision seat")
            if "candidateSeats" in item or "semanticTruth" in item:
                raise ValueError("Target choice cannot impersonate a displayed information result")
        else:
            if not isinstance(item.get("candidateSeats"), list) or any(
                type(s) is not int or s not in range(1, len(seats) + 1)
                for s in item["candidateSeats"]
            ):
                raise ValueError("Invalid candidate seats")
            if item.get("registrationWitnessesArePossibilities") is not True:
                raise ValueError("Registration witnesses are not verified registration facts")
            if item.get("semanticTruth") not in ("TRUE", "FALSE", "PARTIALLY_TRUE", "NOT_APPLICABLE"):
                raise ValueError("Invalid candidate truth marker")
    if len(ids) != len(set(ids)):
        raise ValueError("Duplicate candidate IDs")
    history = case["causalHistory"]
    if history.get("cutoffSource") != "LIVE_CAPTURED" or history.get("historyMode") != "GLOBAL_V1":
        raise ValueError("Unknown causal prefix")
    cutoff = history.get("exclusiveGlobalSequence")
    if type(cutoff) is not int or cutoff < 0:
        raise ValueError("Invalid causal cutoff")
    if not isinstance(history.get("coverage"), dict) or not isinstance(history.get("events"), list):
        raise ValueError("Missing coverage/chronology")
    previous = -1
    for event in history["events"]:
        seq = event.get("globalSequence")
        if type(seq) is not int or not previous < seq < cutoff:
            raise ValueError("Out-of-order or future causal event")
        previous = seq
        if event.get("kind") == "action":
            if event.get("epistemicClass") != "HOST_CONFIRMED_ACTION" or not isinstance(
                event.get("action"), dict
            ):
                raise ValueError("Unverified action")
        elif event.get("kind") == "observation":
            if event.get("epistemicClass") != "PLAYER_RECEIVED_OR_PUBLIC_INFORMATION":
                raise ValueError("Unverified observation")
            if event.get("visibility") not in ("PUBLIC", "PRIVATE") or not isinstance(
                event.get("proposition"), dict
            ):
                raise ValueError("Invalid player-visible observation")
        else:
            raise ValueError("Unknown event class")
    prior = case.get("priorStrategy")
    if prior is not None and (not isinstance(prior, dict) or
                              len(json.dumps(prior)) > 16000):
        raise ValueError("Invalid advisory strategy")
    profile = case.get("responseProfile")
    if profile is not None and (
        profile != COMPACT_MEMO_PROFILE
        or case.get("strategicPlanningScope") != "GLOBAL_EVENT_DRIVEN_CONTINUATION"
    ):
        raise ValueError("Unrecognized or non-live response profile")
    memo = case.get("priorCompactMemo")
    if memo is not None and (profile != COMPACT_MEMO_PROFILE
                             or type(memo) is not str or len(memo) > 200):
        raise ValueError("Invalid advisory memo")
    return set(ids)


def validate_host_analysis_request(case: dict) -> set[int]:
    """Informational only: no invented pending Host decision."""
    if case.get("schemaId") != ANALYSIS_SCHEMA_ID or case.get("schemaVersion") != 1:
        raise ValueError("Unsupported analysis contract")
    ident = case["analysisIdentity"]
    if ident.get("scriptId") != "trouble_brewing" or not isinstance(ident.get("gameId"), str) or not ident["gameId"]:
        raise ValueError("Invalid game identity")
    if not isinstance(ident.get("analysisId"), str) or not ident["analysisId"]:
        raise ValueError("Invalid analysis identity")
    revision = case["sourceRevision"]
    if not all(type(revision.get(key)) is int and revision[key] >= 0
               for key in ("gameStateRevision", "playerInputRevision")):
        raise ValueError("Invalid source revision")
    state = case["state"]
    if state.get("stage") != "SETUP_COMMITTED" or type(state.get("hasDrunk")) is not bool:
        raise ValueError("Analysis requires confirmed setup")
    seats = state["seats"]
    if not isinstance(seats, list) or not 5 <= len(seats) <= 15:
        raise ValueError("Incomplete roster")
    if [seat.get("seat") for seat in seats] != list(range(1, len(seats) + 1)):
        raise ValueError("Noncanonical seat order")
    for seat in seats:
        if not isinstance(seat.get("shownRoleId"), str) or not isinstance(seat.get("actualRoleId"), str):
            raise ValueError("Incomplete committed identities")
        if seat["shownRoleId"] in ("UNCOMMITTED", "UNKNOWN", "NOT_APPLICABLE") or seat["actualRoleId"] in ("UNCOMMITTED", "UNKNOWN", "NOT_APPLICABLE"):
            raise ValueError("Uncommitted identity after setup commit")
    actual_drunk = next((seat["seat"] for seat in seats if seat["actualRoleId"] == "drunk"), None)
    if (actual_drunk is not None) != state["hasDrunk"]:
        raise ValueError("Drunk state contradicts committed roster")
    if state.get("drunkAssignmentSeat") != (actual_drunk if actual_drunk is not None else "NOT_APPLICABLE"):
        raise ValueError("Drunk seat contradiction")
    if "legalCandidates" in case or "decisionId" in ident:
        raise ValueError("Analysis cannot impersonate a pending Host decision")
    if case.get("historyCoverage") != "SETUP_ONLY_NO_FUTURE_OBSERVATIONS":
        raise ValueError("Missing prefix coverage")
    prior = case.get("priorStrategy")
    if prior is not None:
        if not isinstance(prior, dict) or prior.get("sourceGameId") != ident["gameId"]:
            raise ValueError("Cross-game prior strategy")
        if type(prior.get("gameStateRevision")) is not int or prior["gameStateRevision"] > revision["gameStateRevision"]:
            raise ValueError("Future previous strategy")
        if not isinstance(prior.get("strategy"), dict) or len(json.dumps(prior["strategy"])) > 16000:
            raise ValueError("Malformed previous strategy")
    return set(range(1, len(seats) + 1))


def build_openai_request(
    case: dict, model: str, *, reasoning_effort: str | None = None,
    compact: bool = False,
) -> dict:
    """The default production payload is unchanged; optional arms are test-only."""
    if reasoning_effort is not None and reasoning_effort not in ("low", "medium", "high"):
        raise ValueError("Unsupported reasoning effort")
    if type(compact) is not bool:
        raise ValueError("Invalid output profile")
    # No raw player names or OAuth/client credentials in this request.
    analysis_only = case.get("schemaId") == ANALYSIS_SCHEMA_ID
    instructions = INSTRUCTIONS
    if case.get("strategicPlanningScope") == "GLOBAL_EVENT_DRIVEN_CONTINUATION":
        instructions = (
            "You are a whole-game Blood on the Clocktower Storyteller strategist. "
            "FIRST examine the FULL current actual/shown roster and causalHistory in its exact as-of order. "
            "Host-confirmed actions and player-facing observations are different: private actions can "
            "affect real ability functioning without being known to players, and public failed or fake "
            "actions can change players' plausible worlds without mechanically succeeding. "
            "The causalHistory coverage is PARTIAL, so unrecorded social speech is UNKNOWN. "
            "Compare priorStrategy with all committed facts, keep/revise/retire contingent intentions "
            "in planRevisionNote, and diagnose multi-seat information ecology, trust, pressure and fairness. "
            "Only THEN propose the next currently legal candidate ID and a genuinely different legal "
            "alternative if available, referencing how the combined plan affects future information. "
            "Do NOT treat option previews as published, retroactively change observations, declare a "
            "Spy/Recluse registration witness definite from an ambiguous result, or invent claims. "
            "The Host will validate every candidate and remains sole game-state authority. "
            "No role-specific ranking or hardcoded action-trigger policy. "
            "Return the same required structured JSON with concise auditable Chinese explanations."
        )
    # Explicit, live-only compact output: retain the entire Host history and
    # full-board reasoning duty while removing expensive narrative fields.
    # The memo is advisory and is NOT a substitute for canonical events.
    if case.get("responseProfile") == COMPACT_MEMO_PROFILE:
        if analysis_only or case.get("strategicPlanningScope") != "GLOBAL_EVENT_DRIVEN_CONTINUATION":
            raise ValueError("Compact memo allowed only for live global decisions")
        instructions = (
            "You are a whole-game Blood on the Clocktower Storyteller strategist. "
            "Evaluate the COMPLETE actual/shown seat roster, player levels, evil pressure, "
            "all Good information and its interactions, and the FULL ordered causalHistory "
            "as-of the pending Host decision. Compare priorStrategy against confirmed facts; "
            "confirmed actions and player-received observations outrank any prior plan. "
            "priorCompactMemo, if supplied, is a fallible earlier recommendation, "
            "NOT evidence that its recommended action was accepted or that a future "
            "conditional event occurred. Prefer the CURRENT confirmed causalHistory. "
            "Poisoning and registrations may change reliability but don't create player "
            "knowledge; Spy/Recluse registration ambiguity is a hypothesis, NOT a fact. "
            "Never invent player claims, retroactively change a previous observation, "
            "or rank one role in isolation. Consider multiple plausible worlds, "
            "future conditional information and fair pressure, not either side winning. "
            "FIRST complete the global strategic assessment, THEN choose exactly ONE "
            "candidateId from legalCandidates. Return only candidateId and a compact "
            "Chinese planMemo (one sentence, no more than 80 Chinese characters) "
            "linking the key confirmed event, cross-seat tradeoff, and future caveat. "
            "Do not output hidden reasoning, alternatives or a full strategy."
        )
    if analysis_only:
        instructions = (
            "You are an expert Blood on the Clocktower Trouble Brewing whole-game Storyteller strategist. "
            "This is an ANALYSIS-ONLY checkpoint AFTER setup commit: every actual and shown role "
            "in state.seats is confirmed Host truth. No pending Host decision is present. "
            "FIRST identify the most important global tensions, interactions across multiple seats "
            "and roles, Good information ecology, evil pressure and plausible alternate worlds; "
            "do not make isolated-role recommendations or optimize for either side to win. "
            "THEN provide 1-4 nonduplicate issue diagnoses, 0-4 strategic relationship hypotheses "
            "grounded in actual seat numbers, and 1-4 conditional future intentions. "
            "Use priorStrategy ONLY as a fallible earlier strategic plan, not game truth: "
            "explicitly KEEP, REVISE or RETIRE its intentions in planRevisionNote after comparing "
            "to the now-confirmed actual Drunk assignment (or the confirmed absence of Drunk). "
            "If priorStrategy is missing, perform an independent full-board assessment. "
            "Future player actions, night observations, claims and registrations have not happened "
            "or have not been provided; do not invent them. Spy/Recluse witness ambiguity remains "
            "a hypothesis, never an automatic confirmed registration. "
            "Return ONLY the structured strategy object; do not select a candidate or propose "
            "illegal future action. Use concise Chinese for actionable strategic conclusions, "
            "not hidden reasoning steps."
        )
    if compact:
        # Preserve ALL strategic dimensions and strict schema; shorten narration only.
        # Never turn a whole-game diagnosis into independent role-only advice.
        instructions += (
            " BENCHMARK COMPACT REPORT: Keep the same whole-game causal analysis, "
            "legal alternatives and auditable tradeoffs. Return 1-2 prioritized "
            "cross-seat issues, 0-2 high-value relations and 1-2 conditional "
            "intentions. Each explanation should be one concise Chinese sentence "
            "without repeating the roster; preserve uncertainty and factual "
            "versus hypothetical distinctions. Do not omit required fields."
        )
    if case.get("responseProfile") == COMPACT_MEMO_PROFILE:
        payload = {
            "model": model, "instructions": instructions,
            "input": json.dumps(case, ensure_ascii=False, separators=(",", ":")),
            "store": False,
            "text": {"format": {
                "type": "json_schema", "name": "botc_global_compact_memo_v1",
                "strict": True,
                "schema": {
                    "type": "object",
                    "properties": {
                        "candidateId": {"type": "string"},
                        "planMemo": {"type": "string"},
                    },
                    "required": ["candidateId", "planMemo"],
                    "additionalProperties": False,
                },
            }},
        }
        if reasoning_effort is not None:
            payload["reasoning"] = {"effort": reasoning_effort}
        return payload
    payload = {
        "model": model,
        "instructions": instructions,
        "input": json.dumps(case, ensure_ascii=False, separators=(",", ":")),
        "store": False,
        "text": {"format": {
            "type": "json_schema",
            "name": "botc_global_analysis_v1" if analysis_only else "botc_global_storyteller_v1",
            "strict": True,
            "schema": {
                "type": "object", "properties": {"strategy": STRATEGY_SCHEMA},
                "required": ["strategy"], "additionalProperties": False,
            } if analysis_only else SCHEMA,
        }},
    }
    if reasoning_effort is not None:
        payload["reasoning"] = {"effort": reasoning_effort}
    return payload


def parse_openai_response(response: dict, allowed: set[str], allowed_seats: set[int] | None = None, analysis_only: bool = False) -> dict:
    if allowed_seats is None:
        raise ValueError("Validated seat domain is required")
    if response.get("status") != "completed":
        raise ValueError("Model did not complete")
    texts = [
        block["text"]
        for item in response.get("output", [])
        if item.get("type") == "message"
        for block in item.get("content", [])
        if block.get("type") == "output_text" and isinstance(block.get("text"), str)
    ]
    if not texts:
        raise ValueError("Missing structured response")
    result = json.loads("".join(texts))
    required = {"strategy"} if analysis_only else set(SCHEMA["required"])
    if not isinstance(result, dict) or set(result) != required:
        raise ValueError("Invalid response shape")
    if not analysis_only:
        ids = [result["primaryCandidateId"]]
        if ids[0] not in allowed or not isinstance(result["rationale"], str) or not result["rationale"].strip():
            raise ValueError("Invalid primary recommendation")
        if not isinstance(result["alternatives"], list) or not isinstance(result["uncertainty"], list):
            raise ValueError("Invalid output lists")
        for alt in result["alternatives"]:
            if not isinstance(alt, dict) or set(alt) != {"candidateId", "rationale"}:
                raise ValueError("Malformed alternative")
            ids.append(alt["candidateId"])
            if not isinstance(alt["rationale"], str) or not alt["rationale"].strip():
                raise ValueError("Missing alternative rationale")
        if len(ids) != len(set(ids)) or set(ids) - allowed:
            raise ValueError("Illegal or duplicate recommendation")
        if len(allowed) > 1 and len(ids) == 1:
            raise ValueError("No distinct alternative")
        if not all(isinstance(x, str) and x.strip() for x in result["uncertainty"]):
            raise ValueError("Invalid uncertainty")
    strategy = result["strategy"]
    if not isinstance(strategy, dict) or set(strategy) != set(STRATEGY_SCHEMA["required"]):
        raise ValueError("Missing whole-game diagnosis")
    summary = strategy["situationSummary"]
    if not isinstance(summary, str) or not summary.strip() or len(summary) > 2400:
        raise ValueError("Invalid situation summary")
    issues, relations, intentions = (
        strategy["issues"], strategy["relations"], strategy["intentions"]
    )
    if not isinstance(issues, list) or not 1 <= len(issues) <= 4:
        raise ValueError("Global issue diagnosis is required")
    if not isinstance(relations, list) or len(relations) > 4:
        raise ValueError("Too many relation hypotheses")
    if not isinstance(intentions, list) or not 1 <= len(intentions) <= 4:
        raise ValueError("Conditional whole-game intentions are required")
    issue_ids = []
    # Only the Host can establish facts. All model-supplied graph links remain hypotheses.
    # Seat domain was validated on the input side before any paid request was sent.
    for item in issues:
        if not isinstance(item, dict) or set(item) != set(ISSUE_SCHEMA["required"]):
            raise ValueError("Malformed issue")
        if not isinstance(item["issueId"], str) or not item["issueId"].strip():
            raise ValueError("Missing issue ID")
        issue_ids.append(item["issueId"])
        if type(item["priority"]) is not int or item["priority"] not in range(1, 5):
            raise ValueError("Invalid issue priority")
        if not isinstance(item["seats"], list) or not item["seats"]:
            raise ValueError("No linked seats")
        if any(type(x) is not int or x not in range(1, len(allowed_seats) + 1)
               for x in item["seats"]):
            raise ValueError("Issue references nonexistent seat")
        for field in ("diagnosis", "futureEffect"):
            if not isinstance(item[field], str) or not item[field].strip() or len(item[field]) > 1600:
                raise ValueError("Invalid issue narrative")
    if len(issue_ids) != len(set(issue_ids)):
        raise ValueError("Duplicate issue IDs")
    for relation in relations:
        if not isinstance(relation, dict) or set(relation) != set(RELATION_SCHEMA["required"]):
            raise ValueError("Malformed relation")
        if relation["issueId"] not in issue_ids:
            raise ValueError("Relation lacks a known issue")
        if any(type(relation[k]) is not int or relation[k] not in allowed_seats
               for k in ("fromSeat", "toSeat")) or relation["fromSeat"] == relation["toSeat"]:
            raise ValueError("Relation references nonexistent or identical seats")
        if not isinstance(relation["label"], str) or not relation["label"].strip() or len(relation["label"]) > 500:
            raise ValueError("Invalid relation narrative")
    for intention in intentions:
        if not isinstance(intention, dict) or set(intention) != set(INTENTION_SCHEMA["required"]):
            raise ValueError("Malformed intention")
        for field in ("trigger", "approach", "tradeoff"):
            if not isinstance(intention[field], str) or not intention[field].strip() or len(intention[field]) > 900:
                raise ValueError("Invalid strategic intention")
    if not isinstance(strategy["planRevisionNote"], str) or len(strategy["planRevisionNote"]) > 900:
        raise ValueError("Invalid plan revision note")
    return result


def parse_compact_memo_response(response: dict, legal_ids: set[str]) -> dict:
    """Model output is advisory; only exact Host-legal IDs can cross the gateway."""
    if response.get("status") != "completed":
        raise ValueError("Model did not complete")
    texts = [
        block["text"]
        for item in response.get("output", [])
        if item.get("type") == "message"
        for block in item.get("content", [])
        if block.get("type") == "output_text" and isinstance(block.get("text"), str)
    ]
    if not texts:
        raise ValueError("Missing compact output")
    obj = json.loads("".join(texts))
    if not isinstance(obj, dict) or set(obj) != {"candidateId", "planMemo"}:
        raise ValueError("Unexpected compact output structure")
    candidate = obj["candidateId"]
    memo = obj["planMemo"]
    if type(candidate) is not str or candidate not in legal_ids:
        raise ValueError("Non-legal compact candidate")
    if type(memo) is not str or not memo.strip() or len(memo) > 200:
        raise ValueError("Invalid compact strategic memo")
    return {
        "responseProfile": COMPACT_MEMO_PROFILE,
        "primaryCandidateId": candidate,
        "rationale": memo,
        "alternatives": [],
        "uncertainty": [],
    }


class Budget:
    def __init__(self, max_per_hour: int):
        self.limit = max_per_hour
        self.started = time.monotonic()
        self.calls = 0
        self.lock = threading.Lock()

    def acquire(self) -> bool:
        with self.lock:
            now = time.monotonic()
            if now - self.started >= 3600:
                self.started, self.calls = now, 0
            if self.calls >= self.limit:
                return False
            self.calls += 1  # includes failed calls: conservative quota
            return True


BUDGET = Budget(int(os.environ.get("GATEWAY_MAX_CALLS_PER_HOUR", "10")))
CONCURRENCY = threading.BoundedSemaphore(1)


class Handler(BaseHTTPRequestHandler):
    def log_message(self, format, *args):  # noqa: A002
        pass  # Do not persist secret-bearing headers, private prompts, or URLs.

    def send_json(self, status: int, body: dict, profile: dict | None = None) -> None:
        data = json.dumps(body, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Cache-Control", "no-store")
        self.send_header("Content-Length", str(len(data)))
        # Opt-in, authenticated metrics only. Never expose prompts, IDs, credentials
        # or model text. No private content is written to process logs.
        if profile and os.environ.get("GATEWAY_PROFILE_HEADERS") == "1":
            for field, value in profile.items():
                if type(value) is int and value >= 0:
                    self.send_header("X-Botc-Profile-" + field, str(value))
        self.end_headers()
        self.wfile.write(data)

    def do_POST(self):  # noqa: N802
        if self.path != PATH:
            return self.send_json(404, {"error": "not_found"})
        supplied = self.headers.get("Authorization", "")
        expected = "Bearer " + os.environ["GATEWAY_ACCESS_TOKEN"]
        if not hmac.compare_digest(supplied, expected):
            return self.send_json(401, {"error": "unauthorized"})
        try:
            length = int(self.headers.get("Content-Length", "-1"))
            if length < 1 or length > MAX_BODY:
                return self.send_json(413, {"error": "invalid_size"})
            case = json.loads(self.rfile.read(length))
            analysis_only = case.get("schemaId") == ANALYSIS_SCHEMA_ID
            if analysis_only:
                allowed_seats = validate_host_analysis_request(case)
                allowed = set()
            else:
                allowed = validate_host_request(case)
                allowed_seats = set(range(1, len(case["state"]["seats"]) + 1))
        except (ValueError, TypeError, KeyError, IndexError, json.JSONDecodeError):
            return self.send_json(400, {"error": "invalid_request"})
        if not BUDGET.acquire():
            return self.send_json(429, {"error": "budget_exceeded"})
        if not CONCURRENCY.acquire(blocking=False):
            return self.send_json(429, {"error": "busy"})
        try:
            upstream_payload = json.dumps(
                build_openai_request(case, os.environ["OPENAI_MODEL"])
            ).encode("utf-8")
            request = urllib.request.Request(
                API_URL,
                data=upstream_payload,
                method="POST",
                headers={
                    "Authorization": "Bearer " + os.environ["OPENAI_API_KEY"],
                    "Content-Type": "application/json",
                },
            )
            upstream_started = time.perf_counter()
            with urllib.request.urlopen(request, timeout=75) as stream:
                headers_received = time.perf_counter()
                data = stream.read(256_001)
                body_received = time.perf_counter()
                if len(data) > 256_000:
                    raise ValueError("Model response too large")
                upstream_response = json.loads(data)
                if case.get("responseProfile") == COMPACT_MEMO_PROFILE:
                    result = parse_compact_memo_response(upstream_response, allowed)
                else:
                    result = parse_openai_response(
                        upstream_response, allowed, allowed_seats, analysis_only=analysis_only,
                    )
            validated = time.perf_counter()
            # The buffered HTTP response cannot provide time-to-first-model-token.
            # Time-to-headers contains upstream model generation plus connection.
            profile = {
                "To-Headers-Ms": round((headers_received - upstream_started) * 1000),
                "Body-Read-Ms": round((body_received - headers_received) * 1000),
                "Validate-Ms": round((validated - body_received) * 1000),
                "Upstream-Ms": round((validated - upstream_started) * 1000),
                "Request-Bytes": len(upstream_payload),
                "Response-Bytes": len(data),
            }
            usage = upstream_response.get("usage") or {}
            token_fields = {
                "Input-Tokens": usage.get("input_tokens"),
                "Output-Tokens": usage.get("output_tokens"),
                "Total-Tokens": usage.get("total_tokens"),
                "Cached-Tokens": (usage.get("input_tokens_details") or {}).get("cached_tokens"),
                "Reasoning-Tokens": (usage.get("output_tokens_details") or {}).get("reasoning_tokens"),
            }
            profile.update(token_fields)
            if analysis_only:
                self.send_json(200, {
                    "schemaId": ANALYSIS_RESPONSE_SCHEMA_ID,
                    "schemaVersion": 1,
                    "analysisId": case["analysisIdentity"]["analysisId"],
                    "sourceRevision": case["sourceRevision"],
                    **result,
                }, profile)
            else:
                self.send_json(200, {
                    "schemaId": "botc.storyteller-provider-response",
                    "schemaVersion": 1,
                    "decisionId": case["identity"]["decisionId"],
                    "sourceRevision": case["sourceRevision"],
                    **result,
                }, profile)
        except (urllib.error.URLError, TimeoutError, ConnectionError, ValueError, KeyError, TypeError):
            self.send_json(502, {"error": "model_unavailable_or_invalid"})
        finally:
            CONCURRENCY.release()


def main() -> None:
    for key in ("OPENAI_API_KEY", "OPENAI_MODEL", "GATEWAY_ACCESS_TOKEN"):
        if not os.environ.get(key):
            raise SystemExit(f"Missing server-only environment setting: {key}")
    # Explicit loopback bind. Terminate TLS/restrict access at the authenticated reverse proxy.
    server = ThreadingHTTPServer(("127.0.0.1", int(os.environ.get("GATEWAY_PORT", "8765"))), Handler)
    server.serve_forever()


if __name__ == "__main__":
    main()
