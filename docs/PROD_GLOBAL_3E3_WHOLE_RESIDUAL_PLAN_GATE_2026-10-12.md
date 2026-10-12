# PROD-GLOBAL-3E3 — Coupled current residual scope and whole-package gate

> 2026-10-12, first implementation checkpoint (not end-to-end production acceptance).
> Based on #306 (which is stacked on #304). #305 ADB diagnostics is a PARALLEL child of #304 and is not accidentally incorporated into #306 / 3E3.

## Product invariant

One expert Storyteller plans the whole game across roles and nights from bounded defeasible memory, actual current Host truth and the explicit causal history. A single call returns all *currently source-verified open legal* decisions as a **joint** remaining plan and one revised memory, not independent recommendations. Future unperformed player actions are neither simulated nor published. All prior player-observed information and confirmed actions are immutable. Host decides applicability, legal scopes and confirmation.

## Implemented in this checkpoint

- Existing `StorytellerStrategicContinuationV2` (not a new per-role model protocol) now supports multiple live `OpenDecision` items with typed Host-owned candidate domains and their concise public meanings. Explicitly uncertain registration witnesses are not upgraded to canonical facts.
- Model schema now requires `remainingChoices` as one array covering exactly each supplied Host-open legal domain. Decoder rejects omission, duplication, illegal/closed choice, stale revision/cutoff/memory and unexpected fields as an entire bundle. No partial success. The same response revises KEEP/REVISE/RETIRE policy and carries bounded memory for subsequent nights.
- `HostCurrentOpenDecisionInventoryV2` carries the live pending legal domains and their exact typed current-decision context forward (including reliability, information metric and already chosen query subjects), not only a role or opaque candidate ID. Its Host game/revision/published observation checks remain the legal-scope source. A source seat cannot be duplicated as two first-night information observations under separate request IDs.
- `StorytellerResidualPackageGateV2.verify` is an all-or-nothing immutable proposal handoff. Before any adoption, it requires the EXACT current Host domain (including public semantics, not merely opaque IDs), same game/revisions/causal cutoff and cursor version. It returns a validated suggestion only; it does **not** publish, infer registration or commit. `StorytellerStrategicChangeCheckpointV2.accept` cannot advance memory on an incomplete/illegal legal bundle.
- Tests added for multi-decision same-response selection and changed payload behind same candidate ID, missing/duplicate/illegal/foreign/stale choices using real `GLOBAL_V1` Host session fixture. No golden role-specific planning score.

## Second checkpoint — partial production integration

- New `ClocktowerCurrentResidualScopeV2` walks actual current Host first-night steps from the current position. It can aggregate multiple current legal pair/scalar domains (including a current player-selected query). It stops at any unresolved player/Host action or unsupported typed Host domain, carries a reason code and refuses to invent a future target or witness. Already published observations and settled Red Herring are skipped. Existing TB later-night Mayor redirect is included only after the actual attack opens that decision. This is a **conservative current causal segment**, not an unbounded future-nights catalogue; future independent legal-domain coverage still needs audit.
- A third increment generalizes the same typed scalar Host pending domain to later nights (numeric/Boolean with actual selected subjects). The old first-night-only function is replaced, not retained as a second public adapter. Existing current-phase Night Empath-like scalar information uses the same single model policy and protection rules; role reveal and other unsupported night domains still remain explicit barriers. Added real second-night Host session/domain tests.
- Production assisted path now uses the existing 3E2 checkpoint and 3E3 single Responses call with ALL generated segment decisions and prior versioned strategy memory, retaining the original opening on `NO_MATERIAL_CHANGE` without a second paid call. The exact frozen Host object, phase/round, live advice key, chronological version and Host-validated package are checked before advancing memory and exposing an adoptable private plan. `RESIDUAL_*` debug events support future #305 ADB tracing.
- Runtime coalesces a burst of state changes for 450ms before paying for the joint call, checks the identical state/cursor immediately before the request, and discards stale in-flight responses. Assisted adoption also **regenerates the full current Host legal domain at the button boundary** and refuses the entire package if any candidate context or availability differs, not merely when revision numbers differ.
- Assisted mode offers a one-click **private adoption** of the validated joint package. `ClocktowerResidualDisplayMatcherV2` safely matches the recommended typed pair/scalar candidate to the current manual options; it never narrows the manual legal domain, forces a registration witness or directly publishes an observation. Actual Host step confirmation remains required. Package is invalidated by any Host revision or new global observation prefix.

## Current slice — typed existing-target role information (2026-10-12)

- Reused the **same** generic `ScalarInformation` legal-domain and single full-board continuation protocol for `ROLE` results, rather than adding an Undertaker/Ravenkeeper-specific AI recommender. A role choice requires a real Host-fixed target and an exact `InformationProposition.RoleAt` sourced from the current Host step/typed result options; no candidate is opened for a future unselected query. `ClocktowerNeutralInformationPreparation.roleRevealOptions` now attaches **presentation-only** typed target/role metadata to misleading options where the target already exists; critically it does NOT create an epistemic `RoleAt` proposition for untruthful display candidates.
- Distinct registration witnesses collapse into one semantic role-result candidate (`role-result:targetSeat:roleId`), so candidate ID carries no unique Spy/Recluse registration witness. Only uniquely matching, witness-free current Host UI options are preselected; explicit special registration options remain manually resolved. Completed/published observations remain excluded by existing Host inventory.
- Added tests for typed Role results in the same Night Host legal inventory, player-action dependency barriers, missing target, and inability to auto-commit ambiguous registration. This does **not** make the preselection engine an automatic publication authority.

## Still required for actual 3E3 completion

1. Expand authoritative complete multi-night/current-phase legal-domain coverage beyond current first-night pair, any-night typed number/boolean/role-result and active Mayor redirect, without guessing actions; prove same-causal-segment completeness and support independent downstream choices when dependencies allow.
2. Improve production deduplication/change coalescing, explicit cancellation and fresh Host scope regeneration at every adoption boundary; add comprehensive real multi-night integration tests and parallel-response failure handling.
3. Complete **AI automatic** Host-confirmed adoption (currently the newly wired runtime continuation is **assisted only**). Never auto-display earlier proposed information while its prerequisites or current replan are unresolved. Human override must trigger generic strategy revision once the Host records it.
4. Paid Luna schema/strategy tests, Android Studio/ADB end-to-end with #305 diagnostics integrated, real 5–10-seat phone play. Retire superseded V1 per-step paths only after functional parity and device acceptance.

## Acceptance status

This checkpoint is **implementation underway**, not a claim of finished dynamic planning. CI/R2 must be verified against the eventual exact pushed head, and no phone test has been executed from this 3E3 branch. Keep Draft; do not merge the stack early.
