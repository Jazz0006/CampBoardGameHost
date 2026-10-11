#!/usr/bin/env python3
"""One-time deterministic TB template migration from three Drunk options to a full roster."""
import json
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ASSET = ROOT / "app/src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json"
AUDIT = ROOT / "docs/TB_SETUP_SIMPLIFICATION_TEMPLATE_AUDIT.json"
LEGACY_OPTION_FIELD = "drunk_as_" + "options"

# Coarse functional families are used only to break down each template's original three choices.
# Existing interactions are valued: information with registration/poison roles; execution,
# survival, and death-trigger abilities with roles that create those events. No game legality or
# runtime choice is inferred from this table.
INFO = {"washerwoman", "librarian", "investigator", "chef", "empath", "fortuneteller", "undertaker"}
EXECUTION = {"slayer", "virgin", "mayor"}
SURVIVAL = {"monk", "soldier", "mayor"}
DEATH = {"ravenkeeper", "undertaker"}
REGISTRATION = {"recluse", "spy", "poisoner"}
DEATH_PRESSURE = {"imp", "scarletwoman", "baron"}


def functional_score(candidate, preset):
    roles = set(preset["townsfolk"] + preset["outsiders"] + preset["minions"] + preset["demons"])
    score = 0
    if candidate in INFO and roles & REGISTRATION:
        score += 5
    if candidate in INFO and "poisoner" in roles:
        score += 2
    if candidate in EXECUTION and roles & {"spy", "recluse", "scarletwoman"}:
        score += 3
    if candidate in SURVIVAL and roles & DEATH_PRESSURE:
        score += 3
    if candidate in DEATH and roles & DEATH_PRESSURE:
        score += 2
    return score


def main():
    data = json.loads(ASSET.read_text())
    prior = {}
    if AUDIT.exists():
        prior = {item["id"]: item for item in json.loads(AUDIT.read_text())["templates"]}
    usage = Counter()
    audit = {"schema_version": 1, "source_dataset_id": data["dataset_id"],
             "selection_method": "max functional interaction score; then least-selected original option globally; then lexical ID",
             "templates": []}
    for pool in data["pools"].values():
        for p in pool:
            options = p.pop(LEGACY_OPTION_FIELD, None)
            has_drunk = p.get("drunk") is True or "drunk" in p["outsiders"]
            if has_drunk and options is None:
                previous = prior.get(p["id"])
                if not previous or not previous.get("chosen"):
                    raise ValueError(f"{p['id']}: cannot reconstruct original choices without the migration audit")
                p["townsfolk"].remove(previous["chosen"])
                options = previous["old_three"]
            elif has_drunk and p.get("drunk") is True and p["townsfolk"] and p["id"] in prior:
                # An already converted tree is reconstructed above from the audit mapping.
                pass
            p["drunk"] = has_drunk
            p["outsiders"] = [role for role in p["outsiders"] if role != "drunk"]
            chosen = None
            rationale = "no Drunk; no additional Townsfolk"
            if has_drunk:
                if not options or len(options) != 3 or len(set(options)) != 3:
                    raise ValueError(f"{p['id']}: expected original three distinct options")
                scores = {role: functional_score(role, p) for role in options}
                chosen = min(options, key=lambda role: (-scores[role], usage[role], role))
                usage[chosen] += 1
                p["townsfolk"].append(chosen)
                rationale = f"interaction score {scores[chosen]}; global use {usage[chosen]}"
            audit["templates"].append({"id": p["id"], "drunk": has_drunk,
                "old_three": options if has_drunk else [], "chosen": chosen,
                "rationale": rationale})
    data["schema_version"] = 3
    data["important_note"] = "Curated Trouble Brewing presets store the complete shown roster. drunk is a boolean; outsiders never contains drunk. When drunk is true, one displayed Townsfolk is committed as the actual Drunk after the visible roster is fixed."
    data.pop("drunk_shown_role_policy", None)
    data.pop("drunk_shown_role_status", None)
    ASSET.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n")
    audit["aggregate"] = {"presets": len(audit["templates"]), "drunk_true": sum(x["drunk"] for x in audit["templates"]),
                          "drunk_false": sum(not x["drunk"] for x in audit["templates"]), "selected_usage": dict(sorted(usage.items()))}
    AUDIT.write_text(json.dumps(audit, ensure_ascii=False, indent=2) + "\n")

if __name__ == "__main__":
    main()
