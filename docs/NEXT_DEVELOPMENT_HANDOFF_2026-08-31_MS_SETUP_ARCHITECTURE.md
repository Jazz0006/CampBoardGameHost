# NEXT DEVELOPMENT HANDOFF — MS-SETUP Generic Multi-Script Setup Architecture

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **MS-S4.5 COMPLETE / ACCEPTED — MS-S5 NEXT**

## 1. Live / accepted checkpoints

Campaign baseline main:

`eed51bade5163790316a31e8295e2e841df90357`

Merged / fully validated TBSP checkpoint:

`98ee982ef3590822cd06ac72a047b49afac3cfd6`

Current campaign branch:

`codex/ms-setup-generic-architecture`

Current campaign PR:

`#61 — MS-SETUP: generic multi-script setup architecture — DRAFT / OPEN`

Accepted code/test slice checkpoints:

```text
MS-S1:
f3d6b7f305ad09ab8e44f64cf476271ffc5c7a0b

MS-S1R:
2a6d447398c9ab857ab48dd6ff3e5995fb73dd7e

MS-S2:
d4001863f134ebbe7d26819f40ac34c7d1de200c

MS-S3:
6b15822e75680fb8e718f5db24358e1a935b5523

MS-S4:
6de0e8c99c89a091615c513255adbdb773b3cc69
```

MS-S4 validation remains:

```text
CI #1236 / run 33359464789   SUCCESS
Android FAST unit tests      SUCCESS
CI aggregate gate            SUCCESS
R2 #1153 / run 33359464788   SUCCESS
Full Android                 SKIPPED by risk router
ASP contract tests           SKIPPED by risk router
Real Clingo                  SKIPPED by risk router
```

MS-S4.5 is a docs-only architecture correction and does not replace the accepted MS-S4 production checkpoint.

Authoritative S4.5 checkpoint:

`docs/MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md`

Always re-query live GitHub state before the next production write.

## 2. Corrected architecture — freeze this before MS-S5

The setup/recommendation causal order is now:

```text
Composition
-> Identity
-> Information
```

Target pipeline:

```text
script + playerCount + setupSeed
-> resolve legal setup candidates
-> MS-S5 choose one SetupCandidate using actual-composition diversity only
-> MS-S6A resolve legal shown-identity options/policy
-> MS-S6B deterministically commit shown identity
-> seat/deal materialization
-> CommittedClocktowerSetup(actualRole + shownRole)
-> setup/first-night recommendation consumes PlayerState.shownRole
-> recommendation produces information only
```

The governing rule is:

> Shown identity is a committed setup fact. Recommendation may consume it, but may not select, replace, reroll or optimize it as an output decision.

Do not redesign this ownership boundary inside an implementation slice without an explicit new architecture decision.

## 3. Accepted foundation remains unchanged

### MS-S1

`CommittedClocktowerSetup` stores exact ordered seats with exact `actualRole` and exact `shownRole`.

### MS-S1R

Persistence/recovery restores exact committed setup directly. No selector/recommendation rerun is allowed during restore.

### MS-S2

`SetupCandidate` remains a canonical **pre-seat actual-role multiset**. Do not add shown-role fields to carry later disguise metadata.

### MS-S3

`TemplateRepository` remains actual-role candidate storage/lookup only. Template shown-identity metadata will be resolved later through a separate provenance-keyed policy/metadata boundary.

### MS-S4

`GeneratedSetupCandidateSource` remains deterministic legal actual-role generation only. It owns no seating, shown identity, diversity/history, persistence or production wiring.

## 4. S4.5 audit conclusions to preserve

The global audit found:

1. `TroubleBrewingSetupPreset.drunkAsOptions` is valid useful template metadata and existing validation should remain.
2. Legacy TB setup scoring currently lets `selectedDrunkShownRole` affect final preset weight; that dependency must not enter MS-S5.
3. `TroubleBrewingSetupRotationRecord.selectedDrunkShownRole` may remain as persisted/history data for compatibility/audit, but must stop acting as actual-composition authority after migration.
4. Generic `HistoricalClueSignature` / `HistoryCooldown` also treat Drunk shown role as recommendation-owned diversity; that ownership must be retired/narrowed during S6C rather than copied forward.
5. `PlayerState.shownRole` already exists, and `PlayerCard -> GameState` already transports the committed shown identity into recommendation input.
6. `TroubleBrewingSetupRecommendationLock` is a migration bridge: it converts an already committed shown role back into a locked recommendation decision. It should retire after recommendation reads `shownRole` directly.
7. Existing first-night role-family infrastructure should be audited/reused for Drunk perceived-role information instead of creating duplicate fake-information algorithms.

## 5. Corrected Trouble Brewing parity contract

MS-S7 must preserve:

- frozen 480-preset dataset;
- template legality and player-count pools;
- actual-role composition semantics;
- exact-repeat policy where still applicable;
- actual-role overlap/novelty behavior;
- Minion-set and style diversity semantics;
- Baron/TB composition legality;
- `drunkAsOptions` legal option metadata;
- deterministic deal/commit behavior under the corrected pipeline;
- true-completion rotation/history gating and accepted durability behavior.

MS-S7 is deliberately allowed to change:

- repeated Drunk shown role no longer changes actual-role preset weight;
- exact old seed/history -> preset identity is not required where the legacy result depended on shown-role weighting;
- shown identity is selected after actual-role candidate selection.

Treat those changes as intentional semantic correction, not accidental parity regression.

## 6. Campaign sequence from here

```text
MS-S0    fresh live-state + setup ownership audit                    COMPLETE
MS-S0.5  recovery scope reduction audit                             COMPLETE
MS-S1    CommittedClocktowerSetup + provenance                      COMPLETE / ACCEPTED
MS-S1R   exact setup persistence authority migration                COMPLETE / ACCEPTED
MS-S2    SetupCandidate/source/provider contracts                   COMPLETE / ACCEPTED
MS-S3    optional TemplateRepository                                COMPLETE / ACCEPTED
MS-S4    deterministic GeneratedSetupCandidateSource                COMPLETE / ACCEPTED
MS-S4.5  shown-identity ownership architecture correction          COMPLETE / ACCEPTED
MS-S5    actual-composition diversity history/scorer/selector       NEXT
MS-S6A   shown-identity policy/options boundary
MS-S6B   deterministic shown-identity commitment
MS-S6C   recommendation ownership inversion
MS-S7    TB 480-template controlled semantic cutover
MS-S8    NGJ/no-template production cutover
MS-S9    future-script generic acceptance

REC-R1   separate future unfinished-game stable-checkpoint work
```

Do not collapse several slices into one merely because they share the end-to-end setup flow.

## 7. MS-S5 immediate objective

The next production slice is strictly:

```text
legal SetupCandidate values
+ actual-composition diversity history
+ deterministic selection seed/context
-> one selected SetupCandidate
```

MS-S5 owns:

- generic actual-composition diversity history contract;
- deterministic scoring/ranking/tie-breaking;
- selection of one already-legal `SetupCandidate`.

MS-S5 does **not** own candidate legality generation or shown-identity commitment.

### Mandatory S5 invariant

Changing shown-identity metadata/history must not change actual-role candidate selection.

### Forbidden S5 inputs

Do not consume/score:

- TB `drunkAsOptions`;
- selected Drunk shown role;
- shown-role history/cooldown;
- `PlayerState.shownRole`;
- first-night clue candidates;
- setup recommendation decisions.

### Forbidden S5 work

Do not perform:

- seat assignment or shuffle;
- Drunk/shown-identity selection;
- Baron/setup modifier reapplication;
- TB 480-template production adaptation;
- NGJ production cutover;
- persistence changes;
- App/Host setup-flow expansion;
- recommendation ownership inversion.

## 8. Suggested MS-S5 audit/design start

Before writing S5 production code:

1. audit current TB rotation history/scorer/selector structures for reusable **actual-composition-only** semantics;
2. identify which existing TB metadata is generic enough for S5 and which remains TB-specific (`styleTags`, Minion-set, dataset metadata, shown identity);
3. design the smallest pure Kotlin generic history record that can represent candidate actual-role overlap without importing TB preset models;
4. design deterministic score/tie-break semantics independent of source list order;
5. establish typed evidence before implementation for stable S5 contracts.

Minimum typed evidence should include:

- same candidates/history/seed -> same selected candidate;
- input candidate order does not change result;
- recent actual-role overlap meaningfully influences diversity score/selection;
- candidates remain unchanged/immutable through scoring;
- cross-script/provider identity is not accidentally conflated;
- shown-identity metadata/history cannot affect composition selection;
- single candidate selects trivially and deterministically;
- empty candidate behavior is explicit and owned by the selector/facade contract rather than hidden fallback.

Do not manufacture source-string RED when a typed pure contract is practical.

## 9. MS-S6 future boundaries — do not pull forward

### MS-S6A

Resolve legal shown-identity options after one candidate is selected:

```text
selected SetupCandidate
+ validated ruleset
+ provenance
-> legal shown-identity options/policy
```

Template candidates use durable `(providerId, candidateId)` to access template metadata such as `drunkAsOptions`. Generated candidates derive unused legal script Townsfolk from the ruleset.

Do not add shown identity to `SetupCandidate`.

### MS-S6B

Deterministically choose/commit one legal shown identity after composition selection. No unseeded random/shuffle, no recommendation participation, fail closed if a required identity has no legal option.

History-based shown-role diversity is not required in the first generic implementation and, if added later, may never feed back into MS-S5.

### MS-S6C

Make `PlayerState.shownRole` the perceived-identity input authority for recommendation.

Target:

```text
actual Drunk + shownRole X
-> generate X-compatible information behavior only
-> never select or replace X
```

Audit existing first-night information families before creating new role-information logic.

Legacy recommendation-owned Drunk identity types/locks/history should retire only after typed replacement evidence and call-site audits.

## 10. MS-S7 / S8 cutover targets

### MS-S7 Trouble Brewing

```text
480 validated templates
-> template SetupCandidate values
-> MS-S5 composition selector
-> selected template
-> provenance-keyed shown metadata
-> MS-S6 identity commitment
-> deal/materialize
-> CommittedClocktowerSetup
-> recommendation reads shownRole
```

Retire the legacy `selectedDrunkShownRole -> preset finalWeight` path.

### MS-S8 NGJ/no-template

```text
GeneratedSetupCandidateSource
-> MS-S5
-> MS-S6 generated shown options
-> deterministic identity commitment
-> seat/deal materialization
-> CommittedClocktowerSetup
-> recommendation reads shownRole
```

Retire legacy unseeded role/shown selection and recommendation-time shown-role replacement only at S8.

Generated Drunk shown identity must be a legal script Townsfolk not already actually in play. Do not use a broad fallback that can choose an in-play Townsfolk.

## 11. Protected predecessor invariants

Preserve:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore never reselects/rerolls an already committed setup.
Invalid template data never silently falls back to broad-random setup.
Background work cannot mutate committed identities.
Only true completed games enter diversity/rotation history.
Completion persistence is retry-safe.
```

Also preserve Dawn/Dusk retry convergence, Fortune Teller current/effective-state authority, poisoned Spy fail-safe semantics, current living-Demon UI authority and NGJ legality/current behavior until explicit migration.

## 12. Validation / workflow

Follow:

- root `AGENTS.md`;
- `docs/TESTING_STRATEGY.md`;
- `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`.

S4.5 itself is docs-only: no manufactured RED or Android test is required. Normal docs-only CI/R2 routing is sufficient for the carrier.

For S5, use the smallest typed T0 evidence first, then `:app:testFast` at the logical checkpoint and observe applicable GitHub CI/R2 before acceptance.

Use the GitHub connector for safe small/medium docs/tests/source. Keep App/Host out of S5.

## 13. Explicit non-goals for next slice

Do not broaden MS-S5 into:

- shown-identity policy/commitment — S6A/S6B;
- recommendation ownership inversion — S6C;
- TB production cutover — S7;
- NGJ production cutover — S8;
- setup persistence changes;
- App/Host decomposition;
- broad unfinished-game recovery cleanup;
- Mayor / Imp / Monk / Attack-Protect / A3 / A4 / ZDD work;
- PR Ready/merge/rebase/force-push changes.

## 14. Documentation authority

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md
docs/MS_SETUP_RECOVERY_SCOPE_REDUCTION_AUDIT_2026-08-31.md
docs/MS_S1_COMMITTED_SETUP_CHECKPOINT_2026-08-31.md
docs/MS_S1R_SETUP_PERSISTENCE_CHECKPOINT_2026-08-31.md
docs/MS_S2_SETUP_PROVIDER_CONTRACT_CHECKPOINT_2026-08-31.md
docs/MS_S3_TEMPLATE_REPOSITORY_CHECKPOINT_2026-08-31.md
docs/MS_S4_GENERATED_SETUP_CANDIDATE_SOURCE_CHECKPOINT_2026-08-31.md
docs/MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

## 15. Resume guard

Treat `6de0e8c99c89a091615c513255adbdb773b3cc69` as the accepted MS-S4 production code/test checkpoint unless a later production commit deliberately supersedes it.

At the next development turn:

1. re-query live `main`, branch, Draft PR #61 and current checks;
2. distinguish docs-only S4.5 carrier head from accepted production checkpoint;
3. read the S4.5 architecture correction before designing S5;
4. start S5 with actual-composition-only history/scoring/selection audit and typed contract design;
5. do not pull S6 shown identity or S7/S8 production cutovers forward;
6. keep PR #61 Draft and do not merge, mark Ready, force-push or rebase without explicit authorization.
