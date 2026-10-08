#!/usr/bin/env python3
"""GSP API-0: opt-in, server-side Responses API runner for frozen MEM0 cases.

No Android integration, no recommendation authority, no automatic network use.
Do not place API keys or generated game transcripts in this repository.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import random
import sys
import urllib.error
import urllib.request
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_FIXTURE = ROOT / "docs/benchmarks/GSP_MEM0_TB8_SEQUENTIAL_SYNTHETIC_V1.json"
API_URL = "https://api.openai.com/v1/responses"
ARMS = ("A", "B", "C")
SYSTEM_INSTRUCTIONS = (
    "You are a Blood on the Clocktower Storyteller recommendation assistant. "
    "Only use the provided decision-time case, not previous chats or future events. "
    "Treat canonical facts and legal candidate IDs as authoritative; strategic notes "
    "are revisable intent, not facts. Aim for fair, solvable, enjoyable play with "
    "multiple reasonable worlds, not either team's victory. Propose a legal primary "
    "and a meaningfully different legal alternative. If information is incomplete, "
    "acknowledge uncertainty; never invent a registration or a historical claim."
)
RECOMMENDATION_SCHEMA: dict[str, Any] = {
    "type": "object",
    "properties": {
        "primary_candidate_id": {"type": "string"},
        "alternatives": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "candidate_id": {"type": "string"},
                    "why_distinct": {"type": "string"},
                },
                "required": ["candidate_id", "why_distinct"],
                "additionalProperties": False,
            },
        },
        "reasoning_summary": {"type": "string"},
        "impact_on_future_choices": {"type": "string"},
        "memory_used": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "source": {"type": "string", "enum": ["fact", "intent"]},
                    "text": {"type": "string"},
                },
                "required": ["source", "text"],
                "additionalProperties": False,
            },
        },
        "uncertainties": {"type": "array", "items": {"type": "string"}},
        "would_revise_prior_intent": {
            "type": "string", "enum": ["yes", "no", "not_applicable"]
        },
        "revision_reason": {"type": "string"},
    },
    "required": [
        "primary_candidate_id", "alternatives", "reasoning_summary",
        "impact_on_future_choices", "memory_used", "uncertainties",
        "would_revise_prior_intent", "revision_reason",
    ],
    "additionalProperties": False,
}


def load_fixture(path: Path) -> dict[str, Any]:
    data = json.loads(path.read_text(encoding="utf-8"))
    if data.get("schema") != "GSP-MEM0-SYNTHETIC-V1":
        raise ValueError("Unsupported MEM0 benchmark fixture schema")
    if [item["id"] for item in data["checkpoints"]] != ["D0", "D1", "D2"]:
        raise ValueError("Unexpected MEM0 decision checkpoints")
    for checkpoint in data["checkpoints"]:
        ids = [item["id"] for item in checkpoint["legal_candidates"]]
        if not ids or len(ids) != len(set(ids)):
            raise ValueError("Missing or duplicate legal candidate IDs")
    return data


def prompt_case(fixture: dict[str, Any], checkpoint: dict[str, Any], arm: str) -> str:
    if arm not in ARMS:
        raise ValueError("Unknown arm")
    # CRITICAL: never serialize fixture["seats"] or fixture["scenario"]. They contain
    # the teacher-forced D0 future Drunk identity. Use checkpoint-only as-of state.
    case: dict[str, Any] = {
        "script": fixture["script"],
        "player_count": fixture["player_count"],
        "player_levels": fixture["player_levels"],
        "checkpoint_id": checkpoint["id"],
        "phase": checkpoint["phase"],
        "question": checkpoint["question"],
        "canonical": checkpoint["canonical"],
        "legal_candidates": checkpoint["legal_candidates"],
    }
    if arm in ("B", "C"):
        case["prior_confirmed_facts"] = checkpoint["prior_facts"]
    if arm == "C":
        case["strategic_notes_INTENT_NOT_FACT"] = checkpoint["strategic_memory"]
    return json.dumps(case, ensure_ascii=False, sort_keys=True, separators=(",", ":"))


def build_request(model: str, prompt: str) -> dict[str, Any]:
    if not model.strip():
        raise ValueError("Model must be explicitly set")
    return {
        "model": model,
        "instructions": SYSTEM_INSTRUCTIONS,
        "input": prompt,
        "store": False,
        "text": {
            "format": {
                "type": "json_schema",
                "name": "botc_mem0_recommendation_v1",
                "strict": True,
                "schema": RECOMMENDATION_SCHEMA,
            }
        },
    }


def parse_response(response: dict[str, Any]) -> dict[str, Any]:
    if response.get("status") != "completed":
        raise ValueError("Incomplete or failed model response")
    texts: list[str] = []
    for item in response.get("output", []):
        if item.get("type") != "message":
            continue
        for block in item.get("content", []):
            if block.get("type") == "refusal":
                raise ValueError("Model refused")
            if block.get("type") == "output_text" and isinstance(block.get("text"), str):
                texts.append(block["text"])
    if not texts:
        raise ValueError("No structured output_text in completed response")
    result = json.loads("".join(texts))
    if not isinstance(result, dict):
        raise ValueError("Model response is not a JSON object")
    return result


def validate_recommendation(result: dict[str, Any], allowed: list[str]) -> None:
    if set(result) != set(RECOMMENDATION_SCHEMA["required"]):
        raise ValueError("Missing or extra response fields")
    selected = result["primary_candidate_id"]
    if not isinstance(selected, str) or selected not in allowed:
        raise ValueError("Primary candidate is not Host legal")
    alternatives = result["alternatives"]
    if not isinstance(alternatives, list):
        raise ValueError("Alternatives are not a list")
    if len(allowed) > 1 and not alternatives:
        raise ValueError("Missing meaningfully different alternative")
    seen = {selected}
    for alternative in alternatives:
        if not isinstance(alternative, dict) or set(alternative) != {"candidate_id", "why_distinct"}:
            raise ValueError("Malformed alternative")
        cid = alternative["candidate_id"]
        if not isinstance(cid, str) or cid not in allowed or cid in seen:
            raise ValueError("Alternative is illegal or duplicated")
        if not isinstance(alternative["why_distinct"], str) or not alternative["why_distinct"].strip():
            raise ValueError("Alternative lacks rationale")
        seen.add(cid)
    for key in ("reasoning_summary", "impact_on_future_choices", "revision_reason"):
        if not isinstance(result[key], str):
            raise ValueError("Expected text field: " + key)
    if result["would_revise_prior_intent"] not in ("yes", "no", "not_applicable"):
        raise ValueError("Invalid revision flag")
    for key in ("memory_used", "uncertainties"):
        if not isinstance(result[key], list):
            raise ValueError("Expected list field: " + key)
    for note in result["memory_used"]:
        if not isinstance(note, dict) or set(note) != {"source", "text"}:
            raise ValueError("Malformed memory attribution")
        if note["source"] not in ("fact", "intent") or not isinstance(note["text"], str):
            raise ValueError("Invalid memory attribution")
    if not all(isinstance(u, str) for u in result["uncertainties"]):
        raise ValueError("Invalid uncertainty entries")


def call_responses(request: dict[str, Any], api_key: str, timeout: int) -> dict[str, Any]:
    payload = json.dumps(request).encode("utf-8")
    req = urllib.request.Request(
        API_URL, data=payload, method="POST",
        headers={"Authorization": "Bearer " + api_key, "Content-Type": "application/json"},
    )
    try:
        with urllib.request.urlopen(req, timeout=timeout) as response:
            result = json.loads(response.read(3_000_000).decode("utf-8"))
        if not isinstance(result, dict):
            raise ValueError("Unexpected API response envelope")
        return result
    except urllib.error.HTTPError as exc:
        # Don't dump response bodies, secrets, or raw private game state.
        raise RuntimeError(f"Responses API HTTP {exc.code}") from exc


def cases(fixture: dict[str, Any], seed: int = 20261009) -> list[dict[str, Any]]:
    work = []
    for checkpoint in fixture["checkpoints"]:
        for arm in ARMS:
            prompt = prompt_case(fixture, checkpoint, arm)
            work.append({
                "checkpoint": checkpoint["id"], "arm": arm, "prompt": prompt,
                "legal_ids": [item["id"] for item in checkpoint["legal_candidates"]],
                "prompt_sha256": hashlib.sha256(prompt.encode("utf-8")).hexdigest(),
            })
    random.Random(seed).shuffle(work)
    return work


def output_path(value: str) -> Path:
    out = Path(value).expanduser().resolve()
    if out == ROOT or ROOT in out.parents:
        raise ValueError("Use an output directory OUTSIDE the repository (private games/results)")
    return out


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--fixture", type=Path, default=DEFAULT_FIXTURE)
    parser.add_argument("--model", default=os.getenv("OPENAI_MODEL", ""))
    parser.add_argument("--output-dir", help="Required for live requests, outside git repo")
    parser.add_argument("--seed", type=int, default=20261009)
    parser.add_argument("--live", action="store_true", help="Explicit opt-in to paid API calls")
    parser.add_argument("--max-requests", type=int, default=0, help="Budget cap: 1–9")
    parser.add_argument("--timeout", type=int, default=60)
    args = parser.parse_args(argv)
    fixture = load_fixture(args.fixture)
    work = cases(fixture, args.seed)
    if not args.live:
        print(json.dumps({
            "mode": "offline", "fixture_id": fixture["fixture_id"],
            "count": len(work), "arms": list(ARMS),
            "prompt_sha256": [item["prompt_sha256"] for item in work],
            "network_calls": 0,
        }, indent=2))
        return 0
    if not args.output_dir or not 1 <= args.max_requests <= 9:
        parser.error("--live requires --output-dir and --max-requests 1..9")
    if not args.model.strip() or not os.getenv("OPENAI_API_KEY"):
        parser.error("Set --model (or OPENAI_MODEL) and OPENAI_API_KEY in the developer environment")
    target = output_path(args.output_dir)
    if target.exists() and any(target.iterdir()):
        parser.error("Use a NEW empty output directory; never overwrite blind samples")
    target.mkdir(parents=True, exist_ok=True)
    mapping = []
    failures = 0
    for index, task in enumerate(work[:args.max_requests], 1):
        sample = f"S{index:03d}"
        info: dict[str, Any] = {
            "sample_id": sample, "checkpoint": task["checkpoint"],
            "arm": task["arm"], "model": args.model,
            "prompt_sha256": task["prompt_sha256"], "status": "error",
        }
        (target / f"{sample}.prompt.txt").write_text(task["prompt"], encoding="utf-8")
        try:
            answer = call_responses(
                build_request(args.model, task["prompt"]), os.environ["OPENAI_API_KEY"], args.timeout
            )
            parsed = parse_response(answer)
            validate_recommendation(parsed, task["legal_ids"])
            (target / f"{sample}.json").write_text(
                json.dumps(parsed, ensure_ascii=False, indent=2), encoding="utf-8"
            )
            info.update({
                "status": "ok", "response_id": answer.get("id"),
                "usage": answer.get("usage"),
            })
        except (ValueError, RuntimeError, urllib.error.URLError, TimeoutError) as exc:
            failures += 1
            info["error_type"] = type(exc).__name__
            # Error messages are deliberately not persisted to limit secret/raw-data leaks.
        mapping.append(info)
        (target / "private_arm_mapping.json").write_text(
            json.dumps(mapping, indent=2), encoding="utf-8"
        )
    print(json.dumps({
        "mode": "live", "attempted": len(mapping), "succeeded": len(mapping)-failures,
        "failed": failures, "output_dir": str(target),
        "note": "Keep private_arm_mapping.json separate from blind judges; never commit outputs.",
    }))
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
