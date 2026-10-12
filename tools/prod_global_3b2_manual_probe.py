#!/usr/bin/env python3
"""Manual 3B2 Luna probe; offline by default, paid calls only via explicit --live."""
from __future__ import annotations
import argparse
import hashlib
import json
import os
from pathlib import Path
import stat
import sys
import time
import urllib.error
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
FIXTURE = ROOT / "docs/PROD_GLOBAL_3B2_EXPERT_PROBE_FIXTURES_V1.json"
DESIGN = ROOT / "docs/PROD_GLOBAL_3B2_EXPERT_PROBE_DESIGN_V1_2026-10-12.md"
MODEL = "gpt-5.6-luna"
API_URL = "https://api.openai.com/v1/responses"
MAX_OUTPUT_TOKENS = 8192

def obj(properties):
    return {"type": "object", "properties": properties, "required": list(properties),
            "additionalProperties": False}

SCHEMA = obj({
    "asOfRevision": {"type": "integer"},
    "openingPlan": obj({
        "drunkSeat": {"type": ["integer", "null"]},
        "redHerringSeat": {"type": ["integer", "null"]},
        "demonBluffRoles": {"type": "array", "items": {"type": "string"}},
        "informationIntents": {"type": "array", "items": obj({
            "sourceSeat": {"type": "integer"},
            "ability": {"type": "string"},
            "kind": {"type": "string", "enum": ["PAIR_CHARACTER", "NUMBER"]},
            "targetSeats": {"type": "array", "items": {"type": "integer"}},
            "character": {"type": ["string", "null"]},
            "value": {"type": ["integer", "null"]},
            "condition": {"type": "string", "enum": [
                "CURRENT_UNAFFECTED", "DRUNK_MISINFORMATION", "PENDING_POISONER"]},
        })},
        "hostDetermined": {"type": "array", "items": {"type": "string"}},
        "deferred": {"type": "array", "items": obj({
            "event": {"type": "string"},
            "sourceSeat": {"type": ["integer", "null"]},
            "reason": {"type": "string"},
        })},
    }),
    "strategyMemory": obj({
        "tensions": {"type": "array", "items": {"type": "string"}},
        "intentions": {"type": "array", "items": {"type": "string"}},
        "uncertainties": {"type": "array", "items": {"type": "string"}},
    }),
    "choiceSummary": {"type": "string"},
})

def load_fixture():
    data = json.loads(FIXTURE.read_text(encoding="utf-8"))
    if data["schemaId"] != "botc.expert-storyteller-probe-fixtures" or data["metadata"]["model"] != MODEL:
        raise ValueError("Unexpected fixture/model")
    role_catalog = data["roleCatalog"]
    all_roles = sum(role_catalog.values(), [])
    if len(set(all_roles)) != len(all_roles) or set(all_roles) != set(data["roleEffects"]):
        raise ValueError("Duplicate/missing role reference")
    if [f["id"] for f in data["fixtures"]] != ["E01", "E02", "E03", "E04", "E05"]:
        raise ValueError("Unexpected test IDs")
    for f in data["fixtures"]:
        w = f["input"]
        seats = w["circleSeats"]
        if [s["seat"] for s in seats] != list(range(1, 9)):
            raise ValueError("Incorrect seat order")
        shown = [s["shownRole"] for s in seats]
        if len(set(shown)) != 8:
            raise ValueError("Duplicate shown role")
        counts = {team: sum(s in roles for s in shown) for team, roles in role_catalog.items()}
        expected = {"Townsfolk": 4 if w["hasDrunk"] else 5,
                    "Outsider": 2 if w["hasDrunk"] else 1, "Minion": 1, "Demon": 1}
        if counts != expected:
            raise ValueError("Incorrect TB setup")
        if w["drunkAssignment"] != ("UNCOMMITTED" if w["hasDrunk"] else "NOT_APPLICABLE"):
            raise ValueError("Invalid Drunk state")
    return data

def instructions():
    content = DESIGN.read_text(encoding="utf-8")
    section = content.split("## 2. V1 System/Developer 提示词", 1)[1]
    fence = chr(96) * 3
    prompt = section.split(fence + "text\n", 1)[1].split("\n" + fence, 1)[0].strip()
    if len(prompt) < 500:
        raise ValueError("Unexpected short system prompt")
    return prompt

def make_input(data, fixture):
    # Strict allowlist, excludes graderOnly, prior answer, and all candidate menus.
    body = {"schemaVersion": 1, "roleCatalog": data["roleCatalog"],
            "roleEffects": data["roleEffects"], "rules": data["rules"],
            "gameWorld": fixture["input"]}
    prompt = json.dumps(body, ensure_ascii=False, sort_keys=True, separators=(",", ":"))
    if any(s in prompt for s in ("graderOnly", "scenarioAim", "legalCandidates")):
        raise ValueError("Evaluation data leaked into model input")
    return prompt

def make_request(system, prompt):
    return {"model": MODEL, "instructions": system, "input": prompt,
            "store": False, "max_output_tokens": MAX_OUTPUT_TOKENS,
            "text": {"format": {"type": "json_schema",
                                "name": "botc_3b2_expert_probe_v1",
                                "strict": True, "schema": SCHEMA}}}

def parse_response(upstream):
    if upstream.get("status") != "completed":
        raise ValueError("Incomplete response")
    pieces = []
    for item in upstream.get("output", []):
        if item.get("type") == "message":
            for block in item.get("content", []):
                if block.get("type") == "refusal":
                    raise ValueError("Model refused")
                if block.get("type") == "output_text":
                    pieces.append(block["text"])
    if not pieces:
        raise ValueError("No output_text returned")
    result = json.loads("".join(pieces))
    if not isinstance(result, dict):
        raise ValueError("Non-object response")
    return result

def basic_checks(result, fixture, data):
    """Small, generic screening only; NOT Host's complete legal-domain validator."""
    issues = []
    w = fixture["input"]
    seats = w["circleSeats"]
    by_seat = {x["seat"]: x["shownRole"] for x in seats}
    townsfolk = set(data["roleCatalog"]["Townsfolk"])
    good = townsfolk | set(data["roleCatalog"]["Outsider"])
    try:
        if result["asOfRevision"] != w["asOfRevision"]:
            issues.append("revision_mismatch")
        plan = result["openingPlan"]
        drunk = plan["drunkSeat"]
        if w["hasDrunk"]:
            if drunk not in by_seat or by_seat[drunk] not in townsfolk:
                issues.append("invalid_drunk_seat")
        elif drunk is not None:
            issues.append("unexpected_drunk")
        actual_roles = {s["shownRole"] for s in seats if s["seat"] != drunk}
        bluffs = plan["demonBluffRoles"]
        if len(bluffs) != 3 or len(set(bluffs)) != 3:
            issues.append("invalid_bluff_trio")
        if any(x not in good or x in actual_roles for x in bluffs):
            issues.append("non_offboard_good_bluff")
        ft = next((s["seat"] for s in seats if s["shownRole"] == "Fortune Teller"
                   and s["seat"] != drunk), None)
        red = plan["redHerringSeat"]
        if ft is None and red is not None:
            issues.append("red_herring_without_ft")
        if ft is not None and (red not in by_seat or by_seat[red] not in good):
            issues.append("missing_or_evil_red_herring")
        events = [x["event"] for x in plan["deferred"]]
        if len(events) != len(set(events)):
            issues.append("duplicate_deferred")
        if ft is not None and "fortune_teller.targets" not in events:
            issues.append("missing_ft_query_dependency")
        if "Poisoner" in actual_roles and "poisoner.target" not in events:
            issues.append("missing_poisoner_target_dependency")
        for x in plan["informationIntents"]:
            if by_seat.get(x["sourceSeat"]) != x["ability"]:
                issues.append("info_actor_mismatch")
            if x["kind"] == "PAIR_CHARACTER":
                if (len(x["targetSeats"]) != 2 or len(set(x["targetSeats"])) != 2
                    or x["character"] is None or x["value"] is not None):
                    issues.append("malformed_pair")
            if x["kind"] == "NUMBER":
                if (x["targetSeats"] or x["character"] is not None
                    or type(x["value"]) is not int):
                    issues.append("malformed_number")
        for key in ("tensions", "intentions", "uncertainties"):
            if len(result["strategyMemory"][key]) > 3:
                issues.append("long_memory_" + key)
    except (KeyError, TypeError, ValueError):
        issues.append("response_shape_mismatch")
    return list(dict.fromkeys(issues))

def validate_output_dir(name):
    path = Path(name).expanduser()
    if not path.is_absolute() or path.is_symlink():
        raise ValueError("Output directory must be an absolute nonsymlink path")
    path = path.resolve()
    if path == ROOT or ROOT in path.parents:
        raise ValueError("Results must be outside git repository")
    if path.exists():
        if not path.is_dir() or any(path.iterdir()):
            raise ValueError("Output directory already contains files")
        if path.stat().st_mode & (stat.S_IRWXG | stat.S_IRWXO):
            raise ValueError("Existing result directory must be mode 0700")
    return path

def private_json(path, obj):
    raw = (json.dumps(obj, ensure_ascii=False, indent=2) + "\n").encode("utf-8")
    fd = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW, 0o600)
    with os.fdopen(fd, "wb") as stream:
        stream.write(raw)

def call_responses(payload, timeout):
    body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request(
        API_URL, data=body, method="POST",
        headers={"Authorization": "Bearer " + os.environ["OPENAI_API_KEY"],
                 "Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=timeout) as response:
            raw = response.read(1_000_001)
            if len(raw) > 1_000_000:
                raise ValueError("Upstream response exceeds 1MB cap")
            return json.loads(raw)
    except urllib.error.HTTPError as exc:
        # Never log HTTP response body or credential-bearing headers.
        raise RuntimeError("HTTP_" + str(exc.code)) from None

def main(argv=None):
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--case", choices=["all", "E01", "E02", "E03", "E04", "E05"], default="all")
    p.add_argument("--repeat", type=int, choices=[1, 2], default=1)
    p.add_argument("--live", action="store_true")
    p.add_argument("--max-calls", type=int, default=0)
    p.add_argument("--timeout", type=int, default=180)
    p.add_argument("--output-dir")
    args = p.parse_args(argv)
    data = load_fixture()
    system = instructions()
    # "all" intentionally preserves frozen E01-E04 baseline; optional E05 is opt-in.
    fixtures = [f for f in data["fixtures"] if
                (f["id"] in ("E01", "E02", "E03", "E04") if args.case == "all"
                 else f["id"] == args.case)]
    prepared = []
    for rep in range(1, args.repeat + 1):
        for f in fixtures:
            prompt = make_input(data, f)
            request = make_request(system, prompt)
            stamp = hashlib.sha256(json.dumps(request, ensure_ascii=False, sort_keys=True).encode()).hexdigest()
            prepared.append((f, rep, request, stamp))
    if not args.live:
        if args.max_calls or args.output_dir:
            p.error("--max-calls and --output-dir require --live")
        print(json.dumps({"mode": "OFFLINE_PREFLIGHT", "model": MODEL, "networkCalls": 0,
                          "plan": [{"case": f["id"], "repeat": rep, "requestSha256": digest}
                                   for f, rep, _, digest in prepared]}, indent=2))
        return 0
    if not 1 <= args.max_calls <= 8:
        p.error("--live requires --max-calls 1..8")
    if not args.output_dir:
        p.error("--live requires --output-dir")
    if not 30 <= args.timeout <= 300:
        p.error("--timeout must be 30..300 seconds")
    if not os.environ.get("OPENAI_API_KEY"):
        p.error("Set OPENAI_API_KEY securely in your developer shell")
    if os.environ.get("OPENAI_MODEL") not in (None, "", MODEL):
        p.error("OPENAI_MODEL conflicts with fixed gpt-5.6-luna")
    try:
        out = validate_output_dir(args.output_dir)
    except ValueError as exc:
        p.error(str(exc))
    out.mkdir(parents=True, exist_ok=True, mode=0o700)
    os.chmod(out, 0o700)
    summary = []
    failures = 0
    for f, rep, request, digest in prepared[:args.max_calls]:
        name = f["id"] + "_R" + str(rep)
        info = {"sample": name, "requestSha256": digest, "model": MODEL,
                "status": "error", "hostSemanticValidation": "NOT_PERFORMED"}
        private_json(out / (name + ".request.json"), request)
        start = time.perf_counter()
        try:
            response = call_responses(request, args.timeout)
            private_json(out / (name + ".raw.json"), response)
            info["responseId"] = response.get("id")
            info["responseStatus"] = response.get("status")
            info["usage"] = response.get("usage")
            result = parse_response(response)
            private_json(out / (name + ".plan.json"), result)
            info["basicCheckIssues"] = basic_checks(result, f, data)
            info["status"] = "basic_checks_ok" if not info["basicCheckIssues"] else "basic_checks_issues"
            if info["basicCheckIssues"]:
                failures += 1
        except (RuntimeError, ValueError, TypeError, KeyError, urllib.error.URLError,
                TimeoutError, OSError) as exc:
            failures += 1
            info["errorType"] = type(exc).__name__
            if isinstance(exc, RuntimeError) and str(exc).startswith("HTTP_"):
                info["httpStatus"] = str(exc)
        info["latencySeconds"] = round(time.perf_counter() - start, 3)
        private_json(out / (name + ".metrics.json"), info)
        summary.append(info)
        print(name + ": " + info["status"] + ", " + str(info["latencySeconds"]) + "s, tokens="
              + str((info.get("usage") or {}).get("total_tokens")), flush=True)
        if info["status"] == "error":
            # Technical/API/schema failure: stop this batch instead of billing more calls.
            # Strategy/basic-check issues remain recorded without silently patching choices.
            print("Stopping batch after technical/model-response failure.", flush=True)
            break
    private_json(out / "summary.json", {"experiment": "PROD-GLOBAL-3B2-EXPERT-PROBE",
        "model": MODEL, "attempted": len(summary), "basicPassed":
        sum(x["status"] == "basic_checks_ok" for x in summary),
        "independentHostRulesAndStrategyReview": "REQUIRED", "samples": summary})
    print("Private results:", out)
    print("Mechanical checks are NOT Host semantic or strategic acceptance.")
    return 1 if failures else 0

if __name__ == "__main__":
    sys.exit(main())
