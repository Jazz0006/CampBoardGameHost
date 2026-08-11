#!/usr/bin/env python3
"""Cross-validate CampBoardGameHost epistemic fixtures against frozen botc-asp.

This is a development/test tool. It is intentionally outside the Android app
source set and uses only the Python standard library.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import shutil
import subprocess
import sys
import tempfile
import time
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


SCHEMA_VERSION = 1
ORACLE_REPOSITORY = "https://github.com/pnkfelix/botc-asp.git"
ORACLE_REVISION = "616e61b720cc853af031f2623fd6bde33b869865"
ORACLE_NAME = "pnkfelix/botc-asp"
ATOM = re.compile(r"^[a-z][a-z0-9_]*(?:\([a-z0-9_,()]+\))?$")
STATE_ID = re.compile(r"^[a-z][a-z0-9_]*$")
ROLE_ID = re.compile(r"^[A-Za-z][A-Za-z0-9 _-]*$")
SCENARIO_ID = re.compile(r"^TB-[A-Z0-9]+-[0-9]{2}$")


class FixtureError(ValueError):
    pass


@dataclass(frozen=True)
class RunResult:
    status: str
    atoms: frozenset[str]
    command: tuple[str, ...]
    duration_ms: int
    error: str | None = None


def canonical_json(value: Any) -> str:
    return json.dumps(value, ensure_ascii=False, sort_keys=True, separators=(",", ":"))


def sha256_json(value: Any) -> str:
    return hashlib.sha256(canonical_json(value).encode("utf-8")).hexdigest()


def load_catalog(path: Path) -> dict[str, Any]:
    with path.open(encoding="utf-8") as handle:
        catalog = json.load(handle)
    validate_catalog(catalog)
    return catalog


def validate_catalog(catalog: dict[str, Any]) -> None:
    if catalog.get("schemaVersion") != SCHEMA_VERSION:
        raise FixtureError("Unsupported fixture schemaVersion")
    oracle = catalog.get("oracle", {})
    if oracle.get("repository") != ORACLE_NAME or oracle.get("revision") != ORACLE_REVISION:
        raise FixtureError("Fixture catalog must use the frozen botc-asp reference")
    states = catalog.get("formalStates")
    scenarios = catalog.get("scenarios")
    if not isinstance(states, dict) or not states:
        raise FixtureError("formalStates must be a non-empty object")
    if not isinstance(scenarios, list) or not scenarios:
        raise FixtureError("scenarios must be a non-empty array")
    seen: set[str] = set()
    for name, state in states.items():
        if not STATE_ID.fullmatch(name):
            raise FixtureError(f"Invalid state name: {name}")
        _validate_state(state, name)
    for scenario in scenarios:
        scenario_id = scenario.get("scenarioId", "")
        if not SCENARIO_ID.fullmatch(scenario_id) or scenario_id in seen:
            raise FixtureError(f"Invalid or duplicate scenarioId: {scenario_id}")
        seen.add(scenario_id)
        if scenario.get("stateRef") not in states:
            raise FixtureError(f"{scenario_id}: unknown stateRef")
        if not isinstance(scenario.get("perspectiveSeat"), int):
            raise FixtureError(f"{scenario_id}: perspectiveSeat is required")
        if scenario.get("expectedStatus") not in {"SAT", "UNSAT"}:
            raise FixtureError(f"{scenario_id}: expectedStatus must be SAT or UNSAT")
        query = scenario.get("query", {})
        if query.get("kind") not in {
            "setup-profile", "pair-info", "numeric-info", "yes-no", "no-outsiders", "registration"
        }:
            raise FixtureError(f"{scenario_id}: unsupported query kind")
        disposition = scenario.get("mismatchDisposition", "UNEXPLAINED_MISMATCH")
        if disposition not in {
            "UNEXPLAINED_MISMATCH", "EXPECTED_COVERAGE_GAP", "KNOWN_SEMANTIC_VARIANCE"
        }:
            raise FixtureError(f"{scenario_id}: invalid mismatchDisposition")
        for assertion in scenario.get("outputAssertions", []):
            if assertion.get("relation") not in {"CONTAINS", "EXCLUDES"}:
                raise FixtureError(f"{scenario_id}: invalid output assertion")
            atoms = assertion.get("atoms")
            if not isinstance(atoms, list) or not all(ATOM.fullmatch(x or "") for x in atoms):
                raise FixtureError(f"{scenario_id}: invalid expected atom")


def _validate_state(state: dict[str, Any], name: str) -> None:
    required = {"snapshotId", "gameId", "gameStateRevision", "rulesetRef", "phase", "round", "players", "schemaVersion"}
    missing = required - state.keys()
    if missing:
        raise FixtureError(f"{name}: FormalGameState missing {sorted(missing)}")
    if state["schemaVersion"] != 1 or state["round"] < 1:
        raise FixtureError(f"{name}: invalid FormalGameState version or round")
    ruleset = state["rulesetRef"]
    if not re.fullmatch(r"[0-9a-f]{32}", ruleset.get("scriptContentHash", "")):
        raise FixtureError(f"{name}: scriptContentHash must match RulesetRef")
    if ruleset.get("coverage") not in {"VERIFIED", "PARTIAL", "UNVERIFIED"}:
        raise FixtureError(f"{name}: coverage must match RuleCoverage")
    players = state["players"]
    seats = [player.get("seat") for player in players]
    if not players or len(seats) != len(set(seats)) or sorted(seats) != list(range(1, len(players) + 1)):
        raise FixtureError(f"{name}: seats must be unique and contiguous from 1")
    for player in players:
        if not ROLE_ID.fullmatch(player.get("actualRole", "")):
            raise FixtureError(f"{name}: invalid actualRole")
        shown = player.get("shownRole")
        if shown is not None and not ROLE_ID.fullmatch(shown):
            raise FixtureError(f"{name}: invalid shownRole")
        if player.get("actualAlignment") not in {"GOOD", "EVIL"}:
            raise FixtureError(f"{name}: invalid Alignment")
        if player.get("actualType") not in {"TOWNSFOLK", "OUTSIDER", "MINION", "DEMON"}:
            raise FixtureError(f"{name}: invalid CharacterType")


def fetch_oracle(checkout_dir: Path) -> None:
    git = shutil.which("git")
    if not git:
        raise RuntimeError("git is required to fetch the frozen oracle")
    checkout_dir.parent.mkdir(parents=True, exist_ok=True)
    if not (checkout_dir / ".git").exists():
        subprocess.run([git, "init", str(checkout_dir)], check=True)
        subprocess.run([git, "-C", str(checkout_dir), "remote", "add", "origin", ORACLE_REPOSITORY], check=True)
    subprocess.run(
        [git, "-C", str(checkout_dir), "fetch", "--depth", "1", "origin", ORACLE_REVISION],
        check=True,
    )
    subprocess.run([git, "-C", str(checkout_dir), "checkout", "--detach", "FETCH_HEAD"], check=True)
    actual = subprocess.check_output([git, "-C", str(checkout_dir), "rev-parse", "HEAD"], text=True).strip()
    if actual != ORACLE_REVISION:
        raise RuntimeError(f"Frozen oracle revision mismatch: {actual}")


def _seat(seat: int) -> str:
    if not isinstance(seat, int) or seat < 1:
        raise FixtureError(f"Invalid seat: {seat}")
    return f"seat_{seat}"


def _role(role: str) -> str:
    if not ROLE_ID.fullmatch(role or ""):
        raise FixtureError(f"Invalid role ID: {role}")
    return re.sub(r"[ -]+", "_", role.strip()).lower()


def render_scenario(catalog: dict[str, Any], scenario: dict[str, Any]) -> str:
    state = catalog["formalStates"][scenario["stateRef"]]
    query = scenario["query"]
    players = state["players"]
    lines = [
        f"% generated scenario: {scenario['scenarioId']}",
        f"% fixture sha256: {sha256_json(scenario)}",
        f"#const player_count = {len(players)}.",
        "name(" + ";".join(_seat(p["seat"]) for p in players) + ").",
    ]
    lines.extend(f"chair({_seat(p['seat'])}, {p['seat'] - 1})." for p in players)
    if query["kind"] == "setup-profile":
        for role in query.get("requiredRoles", []):
            lines.append(f"assert_distrib({_role(role)}).")
    else:
        for player in players:
            seat = _seat(player["seat"])
            lines.append(f"assert_assigned(0, {seat}, {_role(player['actualRole'])}).")
            shown = player.get("shownRole") or player["actualRole"]
            lines.append(f"assert_received({seat}, {_role(shown)}).")
        if state["round"] > 1:
            lines.append(f"needs_night({state['round']}).")
        for player in players:
            if player.get("poisoned"):
                lines.append(f"oracle_force_impaired({_seat(player['seat'])}).")
        lines.append("impaired(P,T) :- oracle_force_impaired(P), time(T).")
    lines.extend(_render_query(query, scenario))
    lines.extend([
        "#show oracle_output/1.",
        "#show oracle_registration/1.",
    ])
    return "\n".join(lines) + "\n"


def _render_query(query: dict[str, Any], scenario: dict[str, Any]) -> list[str]:
    kind = query["kind"]
    lines: list[str] = []
    if kind == "setup-profile":
        profile = query["profile"]
        category_map = {"townsfolk": "townsfolk", "outsiders": "outsider", "minions": "minion", "demons": "demon"}
        for key, predicate in category_map.items():
            lines.append(f":- #count {{ P,R : assigned(0,P,R), {predicate}(R) }} != {int(profile[key])}.")
        lines.append("oracle_output(setup_profile).")
        return lines

    ability = _role(query["ability"])
    source = _seat(query["sourceSeat"])
    if kind == "pair-info":
        first, second = sorted(query["pairSeats"])
        role = _role(query["shownRole"])
        lines.append(
            f"oracle_output(info(A,B,R)) :- st_tells_core({ability},{source},info(A,B,R),T)."
        )
        expected_atom = f"info({_seat(first)},{_seat(second)},{role})"
        if query.get("constrainObservation", True):
            lines.append(f":- not oracle_output({expected_atom}).")
        lines.append(
            f"oracle_registration(registration(P,C)) :- st_tells_core({ability},{source},_,T), registers_as(P,C,T)."
        )
    elif kind == "numeric-info":
        value = int(query["value"])
        lines.append(f"oracle_output(count(N)) :- st_tells_core({ability},{source},count(N),T).")
        if query.get("constrainObservation", True):
            lines.append(f":- not oracle_output(count({value})).")
    elif kind == "yes-no":
        answer = query["answer"]
        if answer not in {"yes", "no"}:
            raise FixtureError("yes-no answer must be yes or no")
        lines.append(f"oracle_output(yes) :- st_tells_core({ability},{source},yes,T).")
        lines.append(f"oracle_output(no) :- st_tells_core({ability},{source},no,T).")
        lines.extend(f"oracle_allowed_choice({_seat(seat)})." for seat in query["chosenSeats"])
        lines.append(f":- ft_choice({source},P,T), not oracle_allowed_choice(P).")
        if "redHerringSeat" in query:
            lines.append(f"assert_reminder_on(ft_red_herring,{_seat(query['redHerringSeat'])},night(1,0,0)).")
        if query.get("constrainObservation", True):
            lines.append(f":- not oracle_output({answer}).")
        lines.append(
            f"oracle_registration(registration(P,C)) :- st_tells_core({ability},{source},_,T), registers_as(P,C,T)."
        )
    elif kind == "no-outsiders":
        lines.append(f"oracle_output(no_outsiders) :- st_tells_core({ability},{source},no_outsiders,T).")
        if query.get("constrainObservation", True):
            lines.append(":- not oracle_output(no_outsiders).")
    elif kind == "registration":
        subject = _seat(query["subjectSeat"])
        category = _role(query["category"])
        lines.append(f"oracle_output(registration_query).")
        lines.append(f"oracle_registration(registration({subject},{category})) :- registers_as({subject},{category},T).")
        if query.get("required", True):
            lines.append(f":- not oracle_registration(registration({subject},{category})).")
    else:
        raise FixtureError(f"Unsupported query kind: {kind}")
    return lines


def invoke_clingo(clingo: str, oracle_dir: Path, program: Path, timeout: int, enumerate_all: bool) -> RunResult:
    command = (
        clingo,
        str(oracle_dir / "botc.lp"),
        str(oracle_dir / "tb.lp"),
        str(program),
        "--outf=2",
        "--models=0" if enumerate_all else "--models=1",
    )
    started = time.monotonic()
    try:
        completed = subprocess.run(command, capture_output=True, text=True, timeout=timeout)
    except FileNotFoundError:
        return RunResult("NOT_RUN", frozenset(), command, 0, f"clingo executable not found: {clingo}")
    except subprocess.TimeoutExpired:
        return RunResult("NOT_RUN", frozenset(), command, timeout * 1000, f"timeout after {timeout}s")
    duration_ms = int((time.monotonic() - started) * 1000)
    try:
        payload = json.loads(completed.stdout)
    except json.JSONDecodeError:
        detail = (completed.stderr or completed.stdout)[-500:]
        return RunResult("NOT_RUN", frozenset(), command, duration_ms, f"invalid clingo JSON: {detail}")
    result = payload.get("Result", "")
    status = "UNSAT" if result == "UNSATISFIABLE" else "SAT" if "SATISFIABLE" in result else "NOT_RUN"
    atoms: set[str] = set()
    for call in payload.get("Call", []):
        for witness in call.get("Witnesses", []):
            atoms.update(witness.get("Value", []))
    error = None if status != "NOT_RUN" else f"unrecognized clingo result: {result}"
    return RunResult(status, frozenset(atoms), command, duration_ms, error)


def classify(scenario: dict[str, Any], run: RunResult) -> tuple[str, list[str]]:
    if run.status == "NOT_RUN":
        return "NOT_RUN", [run.error or "oracle did not run"]
    mismatches: list[str] = []
    if run.status != scenario["expectedStatus"]:
        mismatches.append(f"status expected {scenario['expectedStatus']}, got {run.status}")
    for assertion in scenario.get("outputAssertions", []):
        expected = set(assertion["atoms"])
        relation = assertion["relation"]
        if relation == "CONTAINS":
            missing = expected - run.atoms
            if missing:
                mismatches.append(f"missing atoms: {sorted(missing)}")
        elif relation == "EXCLUDES":
            unexpected = expected & run.atoms
            if unexpected:
                mismatches.append(f"unexpected atoms: {sorted(unexpected)}")
    if not mismatches:
        return "AGREE", []
    return scenario.get("mismatchDisposition", "UNEXPLAINED_MISMATCH"), mismatches


def run_catalog(catalog: dict[str, Any], oracle_dir: Path, clingo: str, timeout: int) -> dict[str, Any]:
    if not (oracle_dir / "botc.lp").is_file() or not (oracle_dir / "tb.lp").is_file():
        raise RuntimeError(f"Not a botc-asp checkout: {oracle_dir}")
    results = []
    with tempfile.TemporaryDirectory(prefix="campboardgamehost-asp-") as tmp:
        tmp_dir = Path(tmp)
        for scenario in catalog["scenarios"]:
            program = tmp_dir / f"{scenario['scenarioId']}.lp"
            program.write_text(render_scenario(catalog, scenario), encoding="utf-8")
            base_run = invoke_clingo(clingo, oracle_dir, program, timeout, enumerate_all=False)
            observed_atoms: set[str] = set(base_run.atoms)
            commands = [" ".join(base_run.command)]
            duration_ms = base_run.duration_ms
            probe_error = base_run.error
            # Query atom existence directly instead of enumerating every complete
            # world. This preserves exactness while avoiding combinatorial output.
            if base_run.status != "NOT_RUN":
                for index, assertion in enumerate(scenario.get("outputAssertions", [])):
                    for atom_index, atom in enumerate(assertion["atoms"]):
                        probe = tmp_dir / f"{scenario['scenarioId']}-probe-{index}-{atom_index}.lp"
                        probe.write_text(program.read_text(encoding="utf-8") + f":- not {atom}.\n", encoding="utf-8")
                        atom_run = invoke_clingo(clingo, oracle_dir, probe, timeout, enumerate_all=False)
                        commands.append(" ".join(atom_run.command))
                        duration_ms += atom_run.duration_ms
                        if atom_run.status == "SAT":
                            observed_atoms.add(atom)
                        elif atom_run.status == "NOT_RUN":
                            probe_error = atom_run.error
                            base_run = RunResult("NOT_RUN", frozenset(), atom_run.command, duration_ms, probe_error)
                            break
                    if base_run.status == "NOT_RUN":
                        break
            run = RunResult(
                base_run.status,
                frozenset(observed_atoms),
                tuple(commands),
                duration_ms,
                probe_error,
            )
            comparison, details = classify(scenario, run)
            results.append({
                "scenarioId": scenario["scenarioId"],
                "comparison": comparison,
                "expectedStatus": scenario["expectedStatus"],
                "actualStatus": run.status,
                "details": details,
                "durationMs": run.duration_ms,
                "commands": list(run.command),
                "observedAtoms": sorted(run.atoms),
                "scenarioSha256": sha256_json(scenario),
            })
    counts = {name: sum(r["comparison"] == name for r in results) for name in (
        "AGREE", "EXPECTED_COVERAGE_GAP", "KNOWN_SEMANTIC_VARIANCE", "UNEXPLAINED_MISMATCH", "NOT_RUN"
    )}
    return {
        "schemaVersion": 1,
        "generatedAt": datetime.now(timezone.utc).isoformat(),
        "oracle": {"repository": ORACLE_NAME, "revision": ORACLE_REVISION, "timeoutSeconds": timeout},
        "fixtureCatalogSha256": sha256_json(catalog),
        "summary": counts,
        "results": results,
    }


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest="command", required=True)
    fetch = sub.add_parser("fetch", help="fetch the exact frozen botc-asp revision")
    fetch.add_argument("--checkout-dir", type=Path, required=True)
    validate = sub.add_parser("validate", help="validate fixture schema and renderability")
    validate.add_argument("--fixtures", type=Path, required=True)
    render = sub.add_parser("render", help="render fixture scenarios to ASP programs")
    render.add_argument("--fixtures", type=Path, required=True)
    render.add_argument("--out-dir", type=Path, required=True)
    run = sub.add_parser("run", help="run cross-validation and write a JSON report")
    run.add_argument("--fixtures", type=Path, required=True)
    run.add_argument("--botc-asp-dir", type=Path, required=True)
    run.add_argument("--report", type=Path, required=True)
    run.add_argument("--clingo", default="clingo")
    run.add_argument("--timeout", type=int, default=30)
    args = parser.parse_args(argv)

    if args.command == "fetch":
        fetch_oracle(args.checkout_dir)
        return 0
    catalog = load_catalog(args.fixtures)
    if args.command == "validate":
        for scenario in catalog["scenarios"]:
            render_scenario(catalog, scenario)
        print(f"Validated {len(catalog['scenarios'])} scenarios; catalog sha256={sha256_json(catalog)}")
        return 0
    if args.command == "render":
        args.out_dir.mkdir(parents=True, exist_ok=True)
        for scenario in catalog["scenarios"]:
            (args.out_dir / f"{scenario['scenarioId']}.lp").write_text(
                render_scenario(catalog, scenario), encoding="utf-8"
            )
        return 0
    report = run_catalog(catalog, args.botc_asp_dir, args.clingo, args.timeout)
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report["summary"], sort_keys=True))
    return 1 if report["summary"]["UNEXPLAINED_MISMATCH"] or report["summary"]["NOT_RUN"] else 0


if __name__ == "__main__":
    sys.exit(main())
