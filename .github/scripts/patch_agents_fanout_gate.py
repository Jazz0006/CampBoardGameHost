from pathlib import Path

path = Path("AGENTS.md")
text = path.read_text(encoding="utf-8")

anchor = """6. What is the cheapest reliable evidence for this slice: RED/GREEN, existing baseline, typed characterization, integration test, compile/static check, architecture guard, or exact diff audit?\n\nThe default evidence mapping is:\n"""

replacement = """6. What is the cheapest reliable evidence for this slice: RED/GREEN, existing baseline, typed characterization, integration test, compile/static check, architecture guard, or exact diff audit?\n\n### 3.1.1 Shared-contract / fan-out gate\n\nBefore changing a shared model, presentation contract, renderer, projector, persistence DTO, domain result, or any other fan-out seam, the implementation pre-flight **MUST** also map the full production fan-out:\n\n1. Find every production producer, constructor, adapter, mapper, and direct builder of the changed contract.\n2. Find every production consumer of the changed field or behavior.\n3. Classify each path as **must inherit** or **intentionally exempt**; every exemption needs an explicit reason.\n4. Prefer fixing the common semantic / projection / ownership boundary over caller-specific patches.\n5. If callers bypass the intended common projection, migrate them to it or explicitly document and test why separate construction remains necessary.\n6. Verify every relevant product phase or mode (for example Setup / Day / Night, beginner / expert, restored / fresh) rather than only the screen that exposed the issue.\n7. Re-run the producer/consumer search after implementation so no direct path silently relies on a default value, stale adapter, or incomplete projection.\n\nA shared renderer does **not** imply shared ownership. **\"Shared renderer without shared projection\" is an architecture smell** when callers independently assemble incomplete semantic state. Move the derivation toward the authoritative shared projection/owner instead of teaching each screen how to reconstruct it.\n\nFor cross-cutting UI state, visual state and interaction eligibility are separate contracts unless the domain explicitly couples them. A shared visual marker must not silently become a rule-level disabled/selectable decision.\n\nThe default evidence mapping is:\n"""

count = text.count(anchor)
if count != 1:
    raise SystemExit(f"AGENTS.md fan-out anchor: expected 1, found {count}")

next_text = text.replace(anchor, replacement)
path.write_text(next_text, encoding="utf-8")
print("Applied AGENTS shared-contract / fan-out gate")
