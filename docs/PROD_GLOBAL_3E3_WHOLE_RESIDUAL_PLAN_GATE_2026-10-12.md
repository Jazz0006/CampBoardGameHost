# PROD-GLOBAL-3E3 — Coupled current residual scope and whole-package gate

> 2026-10-12, first implementation checkpoint (not end-to-end production acceptance).
> Based on #306 (which is stacked on #304). #305 ADB diagnostics is a PARALLEL child of #304 and is not accidentally incorporated into #306 / 3E3.

## Product invariant

One expert Storyteller plans the whole game across roles and nights from bounded defeasible memory, actual current Host truth and the explicit causal history. A single call returns all *currently source-verified open legal* decisions as a **joint** remaining plan and one revised memory, not independent recommendations. Future unperformed player actions are neither simulated nor published. All prior player-observed information and confirmed actions are immutable. Host decides applicability, legal scopes and confirmation.

## Implemented in this checkpoint

- Existing `StorytellerStrategicContinuationV2` (not a new per-role model protocol) now supports multiple live `OpenDecision` items with typed Host-owned candidate domains and their concise public meanings. Explicitly uncertain registration witnesses are not upgraded to canonical facts.
- Model schema now requires `remainingChoices` as one array covering exactly each supplied Host-open legal domain. Decoder rejects omission, duplication, illegal/closed choice, stale revision/cutoff/memory and unexpected fields as an entire bundle. No partial success. The same response revises KEEP/REVISE/RETIRE policy and carries bounded memory for subsequent nights.
- `HostCurrentOpenDecisionInventoryV2` carries the live pending legal domains and ability context forward; its existing Host game/revision/published observation checks remain the legal-scope source.
- `StorytellerResidualPackageGateV2.verify` is an all-or-nothing immutable proposal handoff. Before any adoption, it requires the EXACT current Host domain (including public semantics, not merely opaque IDs), same game/revisions/causal cutoff and cursor version. It returns a validated suggestion only; it does **not** publish, infer registration or commit. `StorytellerStrategicChangeCheckpointV2.accept` cannot advance memory on an incomplete/illegal legal bundle.
- Tests added for multi-decision same-response selection and changed payload behind same candidate ID, missing/duplicate/illegal/foreign/stale choices using real `GLOBAL_V1` Host session fixture. No golden role-specific planning score.

## Still required for actual 3E3 completion

1. Host needs a **complete authoritative current-phase open decision producer**. The current production App only forwards the current UI step's one `StorytellerProviderRequestV1`; calling the new protocol with only that item must NOT be advertised as whole-board continuation. Include all currently open related decisions, explicit not-yet-open dependency barriers and already confirmed/published exclusions. Never invent future player targets. This is the immediate next owner.
2. Wire the single new continuation request to the 3E2 material-change checkpoint. Deduplicate/coalesce changes, cancel stale responses, and keep whole scope plus cursor stable until fresh Host revalidation. Strictly avoid a per-step paid request loop.
3. Feed validated residual proposals into existing Host-specific current decision UI confirmation surfaces. Assisted: allow human adopt/override, then generic Host transition review. Automatic: confirm ONLY when current full batch is valid and Host transaction supports safe publication; otherwise fail closed/manual. The package gate intentionally does not mutate authoritative Host state.
4. Multi-night real-session producer/consumer test with changes in life/claims/host decisions/observation; concurrent stale callback and multi-request suppression; paid Luna schema/quality; Android Studio/ADB `BotcAiWire` DEBUG validation with #305 integrated on device; real 5–10-seat Android play. Retire superseded V1 pointwise code only **after** functional parity and device acceptance.

## Acceptance status

This checkpoint is **implementation underway**, not a claim of finished dynamic planning. CI/R2 must be verified against the eventual exact pushed head, and no phone test has been executed from this 3E3 branch. Keep Draft; do not merge the stack early.
