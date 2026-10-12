#!/usr/bin/env python3
"""PROD-GLOBAL-3B3 experimental semantic boundary. No network, no Host commit.

The schema is NOT wired to the current postcommit production request. Structural
screening is intentionally separate from the authoritative Host rules validator.
"""
from __future__ import annotations

import json
from pathlib import Path

from tools.prod_global_3b2_manual_probe import load_fixture

ROOT = Path(__file__).resolve().parents[1]
ROLE_SET = frozenset(
    "".join(c for c in name.lower() if c.isalnum())
    for roles in load_fixture()["roleCatalog"].values() for name in roles
)


def obj(properties: dict) -> dict:
    return {"type": "object", "properties": properties,
            "required": list(properties), "additionalProperties": False}


def role_ref(nullable: bool = False) -> dict:
    return {"type": ["string", "null"], "enum": sorted(ROLE_SET) + [None]} if nullable else {
        "type": "string", "enum": sorted(ROLE_SET)}


SCHEMA = obj({
    "schemaVersion": {"type": "integer", "enum": [2]},
    "asOfRevision": {"type": "integer"},
    "openingPlan": obj({
        "drunkSeat": {"type": ["integer", "null"]},
        "redHerringSeat": {"type": ["integer", "null"]},
        "demonBluffRoleIds": {"type": "array", "items": role_ref()},
        "information": {"type": "array", "items": obj({
            "sourceSeat": {"type": "integer"},
            "abilityRoleId": role_ref(),
            "effectiveAbilityRoleId": role_ref(nullable=True),
            "resolution": {"type": "string", "enum": [
                "DECISION", "HOST_DETERMINED", "DEFERRED"]},
            "basis": {"type": "string", "enum": [
                "FUNCTIONING", "DRUNK_MISINFORMATION",
                "STATUS_DEPENDENT", "RULE_DETERMINED"]},
            "subject": obj({
                "mode": {"type": "string", "enum": [
                    "NONE", "EVIDENCE_PAIR", "FIXED_NEIGHBORS",
                    "PLAYER_SELECTED_TARGETS", "PLAYER_QUERY_PENDING"]},
                "seats": {"type": "array", "items": {"type": "integer"}},
            }),
            "observation": obj({
                "kind": {"type": "string", "enum": [
                    "NONE", "NUMBER", "ROLE", "BOOLEAN"]},
                "number": {"type": ["integer", "null"]},
                "roleId": role_ref(nullable=True),
                "boolean": {"type": ["boolean", "null"]},
            }),
            "waitingForEvents": {"type": "array", "items": {"type": "string"}},
        })},
        "deferredActions": {"type": "array", "items": obj({
            "event": {"type": "string"},
            "actorSeat": {"type": ["integer", "null"]},
        })},
    }),
    "strategyMemory": obj({
        "tensions": {"type": "array", "items": {"type": "string"}},
        "intentions": {"type": "array", "items": obj({
            "trigger": {"type": "string"}, "approach": {"type": "string"},
            "risk": {"type": "string"},
        })},
        "uncertainties": {"type": "array", "items": {"type": "string"}},
    }),
    "tradeoffSummary": {"type": "string"},
})


def role_id(name: str) -> str:
    """Mirrors legacy Host catalog externalIdFor: lowercase, alphanumeric."""
    return "".join(c for c in name.lower() if c.isalnum())


def input_v2(data: dict, fixture: dict) -> dict:
    """Strict input allowlist; no graderOnly and no exhaustive legal candidates."""
    world = fixture["input"]
    roles = data["roleCatalog"]
    return {
        "schemaVersion": 2,
        "scriptId": "troublebrewing",
        "roleCatalog": {category: [role_id(n) for n in names]
                        for category, names in roles.items()},
        "roleKnowledge": {
            role_id(name): {"name": name, "effect": effect}
            for name, effect in data["roleEffects"].items()},
        "rules": data["rules"],
        "state": {
            "asOfRevision": world["asOfRevision"],
            "phase": world["phase"],
            "hasDrunk": world["hasDrunk"],
            "drunkAssignment": world["drunkAssignment"],
            "seats": [{
                "seat": s["seat"], "shownRoleId": role_id(s["shownRole"]),
                "actualRoleId": ("UNCOMMITTED_IF_DRUNK_SELECTED"
                                 if world["hasDrunk"] and s["shownRole"] in roles["Townsfolk"]
                                 else role_id(s["shownRole"])),
                "experience": s["experience"],
            } for s in world["circleSeats"]],
            "confirmedEvents": world["confirmedEvents"],
            "unknownEvents": world["unknownEvents"],
            "recentGames": world["recentGames"],
        },
    }


def make_request_v2(data: dict, fixture: dict, instructions: str) -> dict:
    """Offline request assembly only. Explicitly opt in to any later paid runner."""
    return {
        "model": "gpt-6-luna", "instructions": instructions,
        "input": json.dumps(input_v2(data, fixture), ensure_ascii=False,
                            separators=(",", ":"), sort_keys=True),
        "store": False,
        "max_output_tokens": 8192,
        "text": {"format": {"type": "json_schema", "name": "botc_global_expert_v2",
                            "strict": True, "schema": SCHEMA}},
    }


def validate_structure(value: dict, schema: dict = SCHEMA, path: str = "$") -> list[str]:
    """Strict local JSON shape/enum/types; not gameplay legality."""
    problems = []
    expected = schema.get("type", "object")
    expected = expected if isinstance(expected, list) else [expected]
    actual_type = ("null" if value is None else "boolean" if type(value) is bool
                   else "integer" if type(value) is int else "string" if type(value) is str
                   else "array" if type(value) is list else "object" if type(value) is dict
                   else "other")
    if actual_type not in expected:
        return [path + ":type"]
    if "enum" in schema and value not in schema["enum"]:
        problems.append(path + ":enum")
    if actual_type == "object":
        allowed = schema.get("properties", {})
        if set(value) != set(schema.get("required", [])):
            problems.append(path + ":fields")
        if set(value) - set(allowed):
            problems.append(path + ":extra")
        for field in set(value) & set(allowed):
            problems += validate_structure(value[field], allowed[field], path + "." + field)
    if actual_type == "array":
        for i, v in enumerate(value):
            problems += validate_structure(v, schema["items"], path + "[" + str(i) + "]")
    return problems


def screen_v2(result: dict, fixture: dict, data: dict) -> list[str]:
    """Contract integrity only: no invented actions, no Host rules acceptance."""
    issues = validate_structure(result)
    if issues:
        return sorted(set(issues))
    state = input_v2(data, fixture)["state"]
    shown = {s["seat"]: s["shownRoleId"] for s in state["seats"]}
    catalog = input_v2(data, fixture)["roleCatalog"]
    townsfolk = set(catalog["Townsfolk"])
    good = townsfolk | set(catalog["Outsider"])
    plan = result["openingPlan"]
    drunk = plan["drunkSeat"]
    if result["asOfRevision"] != state["asOfRevision"]:
        issues.append("stale_revision")
    if state["hasDrunk"]:
        if shown.get(drunk) not in townsfolk:
            issues.append("invalid_drunk_seat")
    elif drunk is not None:
        issues.append("unexpected_drunk")
    active = {role for seat, role in shown.items() if seat != drunk}
    bluffs = plan["demonBluffRoleIds"]
    if len(bluffs) != 3 or len(set(bluffs)) != 3 or any(
            role not in good or role in active for role in bluffs):
        issues.append("bluff_structure_or_presence")
    red = plan["redHerringSeat"]
    if "fortuneteller" not in active:
        if red is not None:
            issues.append("unexpected_red_herring")
    elif red not in shown or shown[red] not in good or red == drunk:
        issues.append("invalid_red_herring")
    for info in plan["information"]:
        seat = info["sourceSeat"]
        ability = info["abilityRoleId"]
        effective = info["effectiveAbilityRoleId"]
        resolution = info["resolution"]
        basis = info["basis"]
        subject = info["subject"]
        obs = info["observation"]
        waiting = info["waitingForEvents"]
        if shown.get(seat) != ability:
            issues.append("source_ability_identity")
        if (seat == drunk and effective is not None) or (seat != drunk and effective != ability):
            issues.append("effective_ability_mismatch")
        if (seat == drunk) != (basis == "DRUNK_MISINFORMATION"):
            issues.append("drunk_basis_mismatch")
        if resolution == "DECISION":
            if obs["kind"] == "NONE" or waiting:
                issues.append("unresolved_decision")
        elif resolution == "HOST_DETERMINED":
            if obs["kind"] != "NONE" or basis != "RULE_DETERMINED" or waiting:
                issues.append("host_determined_not_authoritative")
        elif obs["kind"] != "NONE" or not waiting:
            issues.append("deferred_observation_invented")
        if basis == "RULE_DETERMINED" and resolution != "HOST_DETERMINED":
            issues.append("host_determined_status")
        if len(waiting) != len(set(waiting)):
            issues.append("duplicate_dependencies")
        mode, targets = subject["mode"], subject["seats"]
        if mode == "EVIDENCE_PAIR":
            if len(targets) != 2 or len(set(targets)) != 2 or any(x not in shown for x in targets):
                issues.append("invalid_evidence_pair")
        elif mode == "PLAYER_SELECTED_TARGETS":
            if not targets or len(targets) != len(set(targets)) or any(x not in shown for x in targets):
                issues.append("invalid_player_target_seats")
        elif targets:
            issues.append("unexpected_subject_seats")
        k = obs["kind"]
        if (k == "NUMBER" and
            (type(obs["number"]) is not int or obs["roleId"] is not None
             or obs["boolean"] is not None)):
            issues.append("invalid_number_payload")
        if k == "ROLE" and (obs["roleId"] is None or obs["number"] is not None
                            or obs["boolean"] is not None or mode not in (
                                "EVIDENCE_PAIR", "PLAYER_SELECTED_TARGETS")):
            issues.append("invalid_role_payload")
        if k == "BOOLEAN" and (type(obs["boolean"]) is not bool
                               or obs["number"] is not None or obs["roleId"] is not None):
            issues.append("invalid_boolean_payload")
        if k == "NONE" and any(obs[v] is not None for v in ("number", "roleId", "boolean")):
            issues.append("nonempty_none_payload")
    events = [x["event"] for x in plan["deferredActions"]]
    if len(events) != len(set(events)):
        issues.append("duplicate_deferred_action")
    for d in plan["deferredActions"]:
        if d["actorSeat"] is not None and d["actorSeat"] not in shown:
            issues.append("invalid_deferred_actor")
    if len(result["strategyMemory"]["intentions"]) > 3:
        issues.append("overlong_intentions")
    return sorted(set(issues))


def legacy_diagnostics(v1: dict, fixture: dict) -> dict:
    """Read-only classification of frozen V1; no rewriting or silent repair."""
    shown = {s["seat"]: s["shownRole"] for s in fixture["input"]["circleSeats"]}
    findings = []
    for index, item in enumerate(v1.get("openingPlan", {}).get("informationIntents", [])):
        ability = item.get("ability")
        if ability != shown.get(item.get("sourceSeat")):
            findings.append({"index": index, "issue": "annotated_or_mismatched_ability"})
        if item.get("kind") == "NUMBER" and item.get("targetSeats"):
            findings.append({"index": index, "issue": "numeric_subject_mixed_into_targets"})
    return {"source": "FROZEN_V1_DIAGNOSTICS_ONLY", "rewritten": False,
            "independentHostValidation": "NOT_PERFORMED", "findings": findings}
