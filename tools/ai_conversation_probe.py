#!/usr/bin/env python3
"""P1 gpt-6-luna Responses probe: offline preparation, manually gated one-call execution."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import time
import urllib.error
import urllib.request

OPENING = "botc.initial-joint-opening-request-v2"
CONTINUATION = "botc.whole-game-strategy-continuation-v2"
API = "https://api.openai.com/v1/responses"


def read_json(path):
    obj = json.loads(Path(path).read_text("utf-8"))
    if not isinstance(obj, dict):
        raise ValueError("Expected a JSON object")
    return obj


def check_app_request(req):
    if req.get("model") != "gpt-6-luna":
        raise ValueError("Probe is pinned to production gpt-6-luna")
    if req.get("store") is not False or "previous_response_id" in req:
        raise ValueError("Input must be the unchanged APP store:false request")
    if not isinstance(req.get("instructions"), str) or not req["instructions"].strip():
        raise ValueError("Missing APP strategy instructions")
    inp = req.get("input")
    if not isinstance(inp, str):
        raise ValueError("APP input must be serialized Host JSON")
    schema_id = json.loads(inp).get("schemaId")
    if schema_id not in (OPENING, CONTINUATION):
        raise ValueError("Only whole-game V2 schemas allowed")
    fmt = req.get("text", {}).get("format", {})
    if fmt.get("type") != "json_schema" or fmt.get("strict") is not True:
        raise ValueError("Missing strict APP Responses schema")
    if not isinstance(fmt.get("schema"), dict):
        raise ValueError("Missing schema body")
    return schema_id


def prepare(app, mode, previous=None, host_verified=False, reasoning="omit", delta=None):
    schema_id = check_app_request(app)
    if mode == "opening" and schema_id != OPENING:
        raise ValueError("Opening mode requires V2 opening")
    if mode in ("chain", "baseline") and schema_id != CONTINUATION:
        raise ValueError("Continuation mode requires V2 continuation")
    req = dict(app)
    req["store"] = mode != "baseline"
    if mode == "chain":
        if not host_verified or not isinstance(previous, str) or not re.fullmatch(
            r"resp_[A-Za-z0-9_-]{8,}", previous
        ):
            raise ValueError("Host acceptance and a completed response ID are required")
        req["previous_response_id"] = previous
    if delta is not None:
        if mode != "chain" or not isinstance(delta.get("schemaId"), str):
            raise ValueError("Experimental Host-derived delta allowed only for chained request")
        req["input"] = json.dumps(delta, ensure_ascii=False, separators=(",", ":"))
    if reasoning != "omit":
        if mode != "chain" or reasoning not in ("auto", "current_turn", "all_turns"):
            raise ValueError("Reasoning continuity is only tested in a chain")
        req["reasoning"] = {"context": reasoning}
    return req


def write_private(path, contents):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True, mode=0o700)
    os.chmod(path.parent, 0o700)
    fd = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
    try:
        with os.fdopen(fd, "w", encoding="utf-8") as target:
            target.write(contents)
    except BaseException:
        path.unlink(missing_ok=True)
        raise


def cmd_prepare(a):
    previous = None
    if a.mode == "chain":
        if not a.accepted_response:
            raise ValueError("A Host-accepted raw response is needed")
        raw = read_json(a.accepted_response)
        if raw.get("status") != "completed":
            raise ValueError("Cannot chain an incomplete response")
        previous = raw.get("id")
    delta = read_json(a.delta) if a.delta else None
    req = prepare(read_json(a.app_request), a.mode, previous,
                  a.host_verified, a.reasoning, delta)
    body = json.dumps(req, ensure_ascii=False, indent=2)
    write_private(a.output, body + "\n")
    print(json.dumps({
        "prepared": str(a.output), "mode": a.mode,
        "sha256": hashlib.sha256(body.encode("utf-8")).hexdigest(),
        "model": req["model"], "store": req["store"],
        "chained": "previous_response_id" in req, "delta_experimental": delta is not None
    }))


def cmd_send(a):
    if not a.confirm_one_paid_call:
        raise ValueError("Paid calls require --confirm-one-paid-call")
    key = os.environ.get("OPENAI_API_KEY", "")
    if not key.startswith("sk-"):
        raise ValueError("Missing OPENAI_API_KEY")
    if a.raw_output.exists() or a.metrics_output.exists():
        raise ValueError("Refusing to overwrite private experiment artifacts")
    req = read_json(a.prepared)
    if req.get("model") != "gpt-6-luna":
        raise ValueError("Prepared request model changed")
    body = json.dumps(req, ensure_ascii=False, separators=(",", ":")).encode("utf-8")
    start = time.monotonic()
    http = urllib.request.Request(API, data=body, method="POST", headers={
        "Content-Type": "application/json", "Authorization": "Bearer " + key.strip()
    })
    try:
        with urllib.request.urlopen(http, timeout=150) as response:
            raw = response.read(2_000_001)
            if len(raw) > 2_000_000:
                raise ValueError("Provider output exceeded safe read limit")
    except urllib.error.HTTPError as exc:
        print(json.dumps({"http_status": exc.code, "outcome": "provider-rejected"}))
        raise SystemExit(2)
    parsed = json.loads(raw)
    if not isinstance(parsed, dict):
        raise ValueError("Invalid provider response")
    write_private(a.raw_output, json.dumps(parsed, ensure_ascii=False, indent=2))
    usage = parsed.get("usage") or {}
    result = {
        "id": parsed.get("id"), "status": parsed.get("status"),
        "model": parsed.get("model"), "request_sha256": hashlib.sha256(body).hexdigest(),
        "stored": req.get("store"),
        "previous_response_id": req.get("previous_response_id"),
        "requested_reasoning_context": (req.get("reasoning") or {}).get("context"),
        "effective_reasoning_context": (parsed.get("reasoning") or {}).get("context"),
        "elapsed_ms": round(1000 * (time.monotonic() - start)),
        "input_tokens": usage.get("input_tokens"),
        "cached_input_tokens": (usage.get("input_tokens_details") or {}).get("cached_tokens"),
        "output_tokens": usage.get("output_tokens"),
        "reasoning_tokens": (usage.get("output_tokens_details") or {}).get("reasoning_tokens"),
        "total_tokens": usage.get("total_tokens")
    }
    write_private(a.metrics_output, json.dumps(result, indent=2))
    print(json.dumps(result))


def cmd_compare(a):
    rows = [read_json(path) for path in a.metrics]
    print(json.dumps(rows, ensure_ascii=False, indent=2))


def main():
    cli = argparse.ArgumentParser(description=__doc__)
    sub = cli.add_subparsers(dest="command", required=True)
    p = sub.add_parser("prepare", help="Offline only; zero paid calls")
    p.add_argument("--app-request", type=Path, required=True)
    p.add_argument("--mode", choices=("opening", "chain", "baseline"), required=True)
    p.add_argument("--accepted-response", type=Path)
    p.add_argument("--host-verified", action="store_true",
                   help="Operator assertion only; Python cannot replace Host semantic gate")
    p.add_argument("--delta", type=Path, help="Experimental delta, NOT P2 production contract")
    p.add_argument("--reasoning", choices=("omit", "auto", "current_turn", "all_turns"),
                   default="omit")
    p.add_argument("--output", type=Path, required=True)
    p.set_defaults(handler=cmd_prepare)
    s = sub.add_parser("send", help="Exactly one paid request, without retry")
    s.add_argument("--prepared", type=Path, required=True)
    s.add_argument("--raw-output", type=Path, required=True)
    s.add_argument("--metrics-output", type=Path, required=True)
    s.add_argument("--confirm-one-paid-call", action="store_true")
    s.set_defaults(handler=cmd_send)
    c = sub.add_parser("compare", help="Compare metric files offline")
    c.add_argument("metrics", nargs="+", type=Path)
    c.set_defaults(handler=cmd_compare)
    args = cli.parse_args()
    args.handler(args)


if __name__ == "__main__":
    main()
