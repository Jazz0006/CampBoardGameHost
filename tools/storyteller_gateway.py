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
SCHEMA = {
    "type": "object",
    "properties": {
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
    "required": ["primaryCandidateId", "rationale", "alternatives", "uncertainty"],
    "additionalProperties": False,
}
INSTRUCTIONS = (
    "You are advising a Blood on the Clocktower Trouble Brewing Storyteller. "
    "Consider the entire seat topology and all roles, all displayed roles, legal candidates, "
    "ability interactions, future information and multiple viable worlds jointly. "
    "Aim for fair, interesting and solvable games, NOT either team's victory. "
    "Every legal candidate is a possible choice. Choose the best and explain concrete "
    "cross-role tradeoffs; include a meaningfully distinct alternative when multiple options exist. "
    "Only choose exact legal candidateId values. Never claim an UNCOMMITTED role is an observed "
    "actual identity. Never invent player claims, past facts, or registration witnesses. "
    "History marked unavailable is not evidence that nothing happened. "
    "Use concise Chinese prose for rationale, and explicitly state important uncertainty."
)


def validate_host_request(case: dict) -> set[str]:
    if case.get("schemaId") != "botc.storyteller-provider-request" or case.get("schemaVersion") != 1:
        raise ValueError("Unsupported request version")
    ident = case["identity"]
    if ident["scriptId"] != "trouble_brewing" or ident["decisionTypeId"] != "drunk-assignment":
        raise ValueError("Unsupported decision family")
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


def build_openai_request(case: dict, model: str) -> dict:
    # No raw player names or OAuth/client credentials in this request.
    return {
        "model": model,
        "instructions": INSTRUCTIONS,
        "input": json.dumps(case, ensure_ascii=False, separators=(",", ":")),
        "store": False,
        "text": {"format": {
            "type": "json_schema",
            "name": "botc_production_drunk_v1",
            "strict": True,
            "schema": SCHEMA,
        }},
    }


def parse_openai_response(response: dict, allowed: set[str]) -> dict:
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
    if not isinstance(result, dict) or set(result) != set(SCHEMA["required"]):
        raise ValueError("Invalid response shape")
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
    return result


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

    def send_json(self, status: int, body: dict) -> None:
        data = json.dumps(body, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Cache-Control", "no-store")
        self.send_header("Content-Length", str(len(data)))
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
            allowed = validate_host_request(case)
        except (ValueError, TypeError, KeyError, IndexError, json.JSONDecodeError):
            return self.send_json(400, {"error": "invalid_request"})
        if not BUDGET.acquire():
            return self.send_json(429, {"error": "budget_exceeded"})
        if not CONCURRENCY.acquire(blocking=False):
            return self.send_json(429, {"error": "busy"})
        try:
            request = urllib.request.Request(
                API_URL,
                data=json.dumps(build_openai_request(case, os.environ["OPENAI_MODEL"])).encode("utf-8"),
                method="POST",
                headers={
                    "Authorization": "Bearer " + os.environ["OPENAI_API_KEY"],
                    "Content-Type": "application/json",
                },
            )
            with urllib.request.urlopen(request, timeout=35) as stream:
                data = stream.read(256_001)
                if len(data) > 256_000:
                    raise ValueError("Model response too large")
                result = parse_openai_response(json.loads(data), allowed)
            self.send_json(200, {
                "schemaId": "botc.storyteller-provider-response",
                "schemaVersion": 1,
                "decisionId": case["identity"]["decisionId"],
                "sourceRevision": case["sourceRevision"],
                **result,
            })
        except (urllib.error.URLError, ValueError, KeyError, TypeError):
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
