# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-01 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation; documented checkpoints do not replace live-state verification.

## 1. Current development context

```text
live main at latest replan audit:
eed51bade5163790316a31e8295e2e841df90357

current branch:
codex/ms-setup-generic-architecture

current Draft PR:
#61 — MS-SETUP: generic multi-script setup architecture
DRAFT / OPEN / UNMERGED

latest accepted code/test checkpoint:
MS-S6C
38a04c1353c883c3bda4b4a506085c3c1d2766bd

focused S6C GREEN tree:
70e7f41d1e30e5e701c02ceb95660572a99d27d4

full acceptance:
CI #1290 / run 33393872108   SUCCESS
R2 #1207 / run 33393872097   SUCCESS
```

`38a04c1...` is an empty `[full-ci]` acceptance commit over the exact code tree from `70e7f41...`; compare contains zero changed files.

The pre-replan branch head `e5a49800fb716e6f8254aa1e9126608a06a713df` was one documentation-only carrier commit ahead of `38a04c1...`. Any later documentation-only replan commit likewise does **not** replace the accepted S6C code/test checkpoint.

Accepted slice checkpoints:

```text
MS-S1   f3d6b7f305ad09ab8e44f64cf476271ffc5c7a0b
MS-S1R  2a6d447398c9ab857ab48dd6ff3e5995fb73dd7e
MS-S2   d4001863f134ebbe7d26819f40ac34c7d1de200c
MS-S3   6b15822e75680fb8e718f5db24358e1a935b5523
MS-S4   6de0e8c99c89a091615c513255adbdb773b3cc69
MS-S5   86c3ce651025de9ccbe1094b161becc171514e69
MS-S6A  5823d66d0eb756a0005df86f1aea7db5902cae60
MS-S6B  d4cf3969aabcea7433b96b5b320171fbc821853e
MS-S6C  38a04c1353c883c3bda4b4a506085c3c1d2766bd
```

Current campaign status:

```text
MS-S0    ownership audit                                         COMPLETE
MS-S0.5  recovery scope reduction audit                          COMPLETE
MS-S1    CommittedClocktowerSetup + provenance                   COMPLETE / ACCEPTED
MS-S1R   setup persistence authority migration                   COMPLETE / ACCEPTED
MS-S2    candidate/source/provider contracts                     COMPLETE / ACCEPTED
MS-S3    optional TemplateRepository                             COMPLETE / ACCEPTED
MS-S4    deterministic generated actual-role source              COMPLETE / ACCEPTED
MS-S4.5  shown-identity ownership architecture correction       COMPLETE / ACCEPTED
MS-S5    actual-composition diversity/scorer/selector            COMPLETE / ACCEPTED
MS-S6A   shown-identity policy/options boundary                  COMPLETE / ACCEPTED
MS-S6B   deterministic shown-identity commitment                 COMPLETE / ACCEPTED
MS-S6C   generic information semantics + impairment ownership    COMPLETE / ACCEPTED
MS-S6D   first-night perceived-ability semantic completion       NEXT
MS-S7    TB 480-template controlled semantic cutover             AFTER S6D

ALG-B2R  first-night Epistemic Gate resumption                   AFTER S7 / SEPARATE CAMPAIGN
MS-S8    NGJ/no-template production cutover                      QUEUED AFTER B2R UNLESS REPRIORITIZED
MS-S9    future-script generic acceptance                        QUEUED

REC-R1   separate future unfinished-game stable-checkpoint work
```

Active handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md`

Latest accepted checkpoint:

`docs/MS_S6C_GENERIC_INFORMATION_SEMANTICS_CHECKPOINT_2026-08-31.md`

Historical S6C design/replan:

`docs/MS_S6C_GENERIC_IMPAIRED_INFORMATION_REPLAN_2026-08-31.md`

## 2. Why the roadmap is re-planned after accepted S6C

S6C is accepted and is **not reopened**. The replan corrects the next-stage interpretation of its coverage.

The global algorithm architecture has always separated:

```text
mechanical / identity correctness
-> role ability semantics
-> legal information space
-> impairment policy
-> storyteller selection
-> player observation/history
-> Possible Worlds epistemic quality evaluation
```

The relevant historical design authorities remain aligned:

- `CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md` defines A3/A4 exact player-world correctness, then B2 first-night `before -> observation -> after` evaluation, with production selector rollout deferred to C9;
- `phase_a_exit_review_2026-08-20.md` explicitly accepted A3 as the exact correctness baseline while keeping A4/ZDD and its caches shadow-only and leaving production recommendation on the existing path;
- `R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md` already established that Drunk and Poisoned information should share a generic impairment layer above role ability semantics;
- `design_plan_audit_2026-08-21.md` separated semantic-history ownership from later revision-driven recommendation unification.

The new audit found that current production is neither fully legacy nor fully A3/A4-driven:

```text
legacy / role-local candidate construction
-> first-night unified candidate pool
-> generic RELIABLE / DRUNK / POISONED selection
-> structured display / observation
```

`legacyInformationCandidates` therefore remains an important production migration seam. A3/A4 player-world results still do **not** generally own recommendation quality/ranking.

S6C successfully established generic semantics for its supported information domains, but that acceptance must not be read as proof that every Trouble Brewing first-night perceived information role has been migrated to a shared typed semantic source. That remaining coverage gap is MS-S6D.

## 3. Frozen target architecture through S6C

The causal setup order remains:

```text
Composition
-> Identity
-> Information
```

Accepted flow through S6C:

```text
script + playerCount + setupSeed
-> resolve script/ruleset setup provider
-> query optional template candidates or legal generated candidates
-> S5 select one candidate using ACTUAL-COMPOSITION diversity only          [ACCEPTED]
-> S6A resolve legal shown-identity options/policy                          [ACCEPTED]
-> S6B deterministically commit shown identity                              [ACCEPTED]
-> seat/deal materialization
-> CommittedClocktowerSetup(actualRole + shownRole)
-> S6C resolve perceived ability / healthy information semantics where supported [ACCEPTED]
-> S6C apply RELIABLE / DRUNK / POISONED generic information policy         [ACCEPTED]
-> recommendation generates information only                                [ACCEPTED]
```

Frozen ownership rules:

- shown identity is a setup fact, not a recommendation output;
- S5 actual-composition selection cannot consume shown identity or shown-identity history;
- S6A exposes legal shown-identity options only;
- S6B chooses/commits shown identity only;
- S6C/S6D may consume committed shown identity but may never change it;
- role-specific ability semantics define legal information shape/truth semantics;
- Healthy, Poisoned and Drunk must not own three different implementations of the same role ability;
- generic impairment policy owns unreliable truth/false-family behavior after the role semantic space exists;
- A3/A4 Possible Worlds quality evaluation is a later layer and must not be silently folded into setup migration.

## 4. Accepted foundation through S6C

### MS-S1 / S1R

`CommittedClocktowerSetup` is the immutable exact initial setup fact. Persistence/recovery stores and restores exact actual/shown identities and never reruns setup selection or recommendation.

### MS-S2

`SetupCandidate` is a canonical pre-seat **actual-role multiset**. It has no shown identity, seating, persistence schema or recommendation history.

### MS-S3

`TemplateRepository` owns actual-role template candidate lookup only. Template-specific shown-identity metadata is reached separately through S6A provenance-keyed policy sources.

### MS-S4 / S4.5

`GeneratedSetupCandidateSource` owns deterministic legal actual-role generation only. Shown identity is removed from composition authority; legacy TB selected/repeated Drunk shown-role weighting must never re-enter S5 actual-role candidate scoring.

### MS-S5

`SetupDiversityHistory` / scorer / selector use actual-role composition only. History is script + player-count scoped. Roles common to every candidate are excluded from overlap scoring. Selection is deterministic and order-independent under its seed.

### MS-S6A

`SetupShownIdentityPolicyResolver` resolves legal shown-identity options after composition selection. S6A performs no selection.

### MS-S6B

`SetupShownIdentityCommitter` consumes selected candidate + S6A policy + setup seed and deterministically commits shown identity under namespace `setup-shown-identity-v1`. S6B is pre-seat and cannot feed back into S5.

### MS-S6C

Accepted S6C pipeline:

```text
committed shown identity
-> perceived ability role
-> role-specific ability/display semantics
-> healthy information candidate space
-> InformationReliability.RELIABLE / DRUNK / POISONED
-> generic ImpairedInformationPolicy
-> generic consequence/history ranking inside the selected family
-> deterministic AbilityObservation
```

S6C retires active Drunk-Investigator-specific recommendation ownership while keeping compatibility schema where required. Drunk and Poisoned share generic impairment semantics for supported common information domains. The first-version family bias is explicit at approximately 90% false / 10% truthful rather than the old 97/3 default.

S6C also repaired the narrow healthy Investigator Recluse/Spy registration seam without a broad registration rewrite.

**Coverage clarification:** S6C proves the architecture and accepted behavior for its migrated domains. It does not by itself prove full first-night semantic coverage for every TB information role that a Drunk may perceive.

## 5. MS-S6D — NEXT — First-night Perceived-Ability Semantic Completion

S6D is a narrow semantic-completeness slice required before S7. It does not reopen accepted S6C behavior.

### 5.1 Goal

For every Trouble Brewing first-night information ability in the original B2 first-night set:

```text
Washerwoman
Librarian
Investigator
Chef
Empath
Fortune Teller
```

establish one shared role semantic source such that:

```text
committed perceived role
-> healthy legal information space / truthful result
-> RELIABLE / DRUNK / POISONED
-> generic impairment policy
-> deterministic information recommendation
```

The important invariant is:

```text
Empath semantics
  -> RELIABLE
  -> POISONED
  -> DRUNK
```

not three separate Healthy/Poisoned/Drunk Empath algorithms.

### 5.2 Required S6D audit

Before production edits, build a coverage matrix for all six first-night families and record for each:

- current actor/perceived-role resolution;
- current healthy truth source;
- current legal false candidate source;
- proposition/display shape;
- Spy/Recluse registration dependency if any;
- whether current production still depends on role-local/legacy `displayOptions`;
- whether a typed semantic candidate source already exists;
- whether an actual Drunk with committed `shownRole` reaches the same ability semantics as an actual healthy/poisoned holder.

### 5.3 S6D implementation boundary

S6D may:

- generalize the existing pair-information semantic seam to missing pair-family coverage such as Washerwoman;
- add the minimum typed numeric/boolean semantic seams required for Chef/Empath/Fortune Teller;
- ensure `shownRole`, not `actualRole`, determines the perceived ability for an actual Drunk;
- reuse the accepted generic impairment policy for both DRUNK and POISONED;
- project semantic output into the existing first-night migration/UI adapter as needed for parity evidence.

S6D must not:

- reselect/reroll Drunk shown identity;
- add role-specific Drunk or Poisoned storyteller strategies;
- change the accepted approximate 90/10 policy unless a separate explicit product decision reopens it;
- introduce A3/A4 `before -> observation -> after` scoring;
- perform broad Host/App decomposition;
- start NGJ cutover;
- rewrite registration beyond a concrete behavior-proven correctness seam.

### 5.4 Tests

Use risk-based behavior tests, not source-shape guards.

At minimum prove representative abstraction coverage across two different information shapes:

1. one missing pair-information perceived role, preferably Washerwoman;
2. one numeric/boolean perceived role, preferably Chef or Empath;
3. actual Drunk + committed shown role receives that role's legal information shape and remains DRUNK;
4. poisoned actual holder uses the same role semantic domain with POISONED reliability;
5. healthy holder still resolves the correct truthful result;
6. recommendation does not mutate committed shown identity.

Do not assert internal class/field existence, exact helper calls or exact internal probability constants.

### 5.5 S6D exit condition

S6D is complete only when all six TB first-night B2 information families have an explicit semantic-coverage disposition and there is no known case where a committed Drunk shown information role is skipped because production looks only for an actual holder of that role.

## 6. MS-S7 — AFTER S6D

S7 remains the **Trouble Brewing controlled semantic cutover**. Do not start S7 until S6D has its own accepted checkpoint.

Target flow:

```text
480 validated templates
-> template SetupCandidate values
-> S5 actual-composition selection
-> S6A template identity policy
-> S6B identity commitment
-> deal/materialize
-> CommittedClocktowerSetup
-> S6D-complete perceived ability semantics consume committed shownRole
```

S7 owns:

- production wiring from validated TB templates into the accepted S2–S6D architecture;
- TB-specific Minion/style diversity adaptation at the correct adapter boundary;
- retirement of legacy `selectedDrunkShownRole -> preset finalWeight` coupling;
- controlled migration from remaining legacy first-night candidate ownership where necessary to make committed perceived identity authoritative;
- parity/shadow evidence before removing a legacy authority;
- preservation of current TB legality and deterministic setup behavior during cutover;
- exact evidence that old and new ownership are not both active for the same decision.

S7 does **not** own A3/A4 epistemic quality scoring. `legacyInformationCandidates` may remain as a UI/compatibility projection while authority is migrated; do not delete it merely for architectural tidiness.

Before implementation, perform S7-0 live audit of the current TB production path from validated templates through deal/materialization and first-night recommendation consumption.

## 7. ALG-B2R — AFTER S7 — Separate algorithm-consistency campaign

After S7 is accepted, pause MS-SETUP production expansion before S8 unless the user explicitly reprioritizes.

ALG-B2R is **not part of PR #61**. Start it only after S7 acceptance and an explicit branch/PR disposition decision; preferred default is a fresh follow-up branch from the then-current accepted main rather than silently stacking a new algorithm campaign onto #61.

ALG-B2R restores the normative v2.2 first-night Epistemic Gate:

```text
semantic candidate
-> recipient PlayerWorldSet BEFORE
-> apply candidate observation
-> recipient PlayerWorldSet AFTER
-> exact epistemic metrics / quality gates
-> recommendation ranking
```

Initial TB scope remains the original B2 first-night families and local registration semantics:

```text
Washerwoman / Librarian / Investigator
Chef / Empath / Fortune Teller
Spy / Recluse local registration
```

ALG-B2R must preserve the Phase A boundary:

- A3 EnumeratedWorldSet remains the exact correctness baseline;
- A4/ZDD remains shadow/prototype until independently device-validated;
- failures/resource limits never become false UNSAT;
- actual-world narrative metrics remain separate from player-world epistemic metrics;
- candidate legality/semantic correctness stays below the epistemic quality layer.

This campaign should progressively replace heuristic-only information quality with the intended `before -> observation -> after` player-knowledge evaluation. It is not the same as C9 full production rollout.

## 8. Longer-term algorithm route

The normative route remains:

```text
ALG-B2R first-night Epistemic Gate
-> later Phase B historical/dynamic world integration as required
-> Phase C productive-uncertainty / fairness / quality / distribution gates
-> C9 Unified Selector Production Rollout
```

C9 remains the point where the Possible Worlds-based selector can become the general production authority after correctness, quality, distribution, replay and device gates pass.

Phase D forward search and Phase E soft belief remain outside the first production-complete requirement.

## 9. Protected predecessor correctness

Preserve throughout S6D/S7 and later migration:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
S5 actual-composition selection cannot consume shown identity.
S6A legality cannot be rewritten by S6B or recommendation.
S6B commitment cannot feed back into S5.
S6C/S6D recommendation cannot mutate setup identity.
Healthy/Poisoned/Drunk of the same perceived ability share role semantics before reliability policy.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore never reselects/rerolls an already committed setup.
Invalid template data never silently falls back to broad-random setup.
Background work cannot mutate committed identities.
Only true completed games enter setup diversity/rotation history.
Completion persistence is retry-safe.
```

Also preserve Dawn/Dusk retry convergence, Fortune Teller current/effective-state authority, poisoned Spy fail-safe semantics, living-Demon UI authority and current NGJ legality until explicit migration.

## 10. Validation cadence

Follow:

```text
AGENTS.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

Use risk-based evidence:

- T0 = smallest directly relevant behavior/evidence;
- `:app:testFast` = logical-checkpoint T1;
- trigger T2/T3 based on changed recommendation/rules/production scope;
- logical acceptance checkpoints use `:app:testFull` + `:app:assembleDebug`;
- ASP contract / Real Clingo run when required by the current router/risk surface;
- GitHub CI/R2 and exact remote diff audit remain acceptance requirements.

Do not create source-string REDs or source-level implementation guards when a stable typed behavior seam can prove the contract.

## 11. Writer / governance rules

Use GitHub connector for safe docs/tests/small-medium source changes according to root `AGENTS.md`.

Keep PR #61 Draft.

Do **not** merge, mark Ready, rebase or force-push without explicit user authorization.

## 12. Current documentation authority

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md
docs/MS_S6C_GENERIC_INFORMATION_SEMANTICS_CHECKPOINT_2026-08-31.md
docs/MS_S6C_GENERIC_IMPAIRED_INFORMATION_REPLAN_2026-08-31.md
docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md
docs/phase_a_exit_review_2026-08-20.md
docs/R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md
docs/design_plan_audit_2026-08-21.md
docs/MS_SETUP_RECOVERY_SCOPE_REDUCTION_AUDIT_2026-08-31.md
docs/MS_S1_COMMITTED_SETUP_CHECKPOINT_2026-08-31.md
docs/MS_S1R_SETUP_PERSISTENCE_CHECKPOINT_2026-08-31.md
docs/MS_S2_SETUP_PROVIDER_CONTRACT_CHECKPOINT_2026-08-31.md
docs/MS_S3_TEMPLATE_REPOSITORY_CHECKPOINT_2026-08-31.md
docs/MS_S4_GENERATED_SETUP_CANDIDATE_SOURCE_CHECKPOINT_2026-08-31.md
docs/MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md
docs/MS_S5_SETUP_DIVERSITY_SELECTOR_CHECKPOINT_2026-08-31.md
docs/MS_S6A_SHOWN_IDENTITY_POLICY_CHECKPOINT_2026-08-31.md
docs/MS_S6B_SHOWN_IDENTITY_COMMITMENT_CHECKPOINT_2026-08-31.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

## 13. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap;
3. read active handoff;
4. read `docs/MS_S6C_GENERIC_INFORMATION_SEMANTICS_CHECKPOINT_2026-08-31.md`;
5. read the v2.2 algorithm plan / Phase A exit / R6 impaired-information design only as needed for S6D semantic boundaries;
6. re-query live `main`, branch, Draft PR #61 and current checks;
7. distinguish any docs-only carrier head from accepted S6C code checkpoint `38a04c1353c883c3bda4b4a506085c3c1d2766bd`;
8. begin **S6D-0**, not S7-0, with the first-night perceived-ability semantic coverage audit;
9. use behavior-first REDs only for concrete missing semantic behavior;
10. do not introduce A3/A4 epistemic ranking during S6D/S7;
11. do not start S8, REC-R1 or broad App/Host work;
12. keep PR #61 Draft and unmerged.

## 14. Deferred / queued work registry

| Area | Status |
|---|---|
| MS-SETUP generic multi-script setup architecture | CURRENT — S6D NEXT |
| MS-S1R setup persistence authority migration | COMPLETE / ACCEPTED |
| MS-S2 generic candidate/provider contracts | COMPLETE / ACCEPTED |
| MS-S3 optional template repository | COMPLETE / ACCEPTED |
| MS-S4 deterministic generated source | COMPLETE / ACCEPTED |
| MS-S4.5 shown-identity ownership correction | COMPLETE / ACCEPTED |
| MS-S5 actual-composition diversity selector | COMPLETE / ACCEPTED |
| MS-S6A shown-identity policy/options boundary | COMPLETE / ACCEPTED |
| MS-S6B deterministic shown-identity commitment | COMPLETE / ACCEPTED |
| MS-S6C generic information semantics + impairment ownership | COMPLETE / ACCEPTED |
| MS-S6D first-night perceived-ability semantic completion | NEXT |
| MS-S7 TB controlled semantic cutover | AFTER S6D |
| ALG-B2R first-night Epistemic Gate resumption | AFTER S7 / NEW CAMPAIGN |
| MS-S8 NGJ/no-template production cutover | QUEUED AFTER B2R UNLESS REPRIORITIZED |
| MS-S9 future-script generic acceptance | QUEUED |
| C9 Unified Selector Production Rollout | FUTURE; AFTER B/C GATES |
| REC-R1 unfinished-game recovery simplification | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED; re-evaluate under REC-R1 |
| Dawn systematic crash cut-point matrix | DEFERRED; committed-state convergence remains relevant |
| App Root S9.2 Active Game Persistence Boundary | SUPERSEDED IN SCOPE BY MS-S1R + REC-R1 |
