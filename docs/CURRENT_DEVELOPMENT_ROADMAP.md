# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-01 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation.

## 1. Current development context

```text
live main at S6D-0 audit:
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

S6C full acceptance:
CI #1290 / run 33393872108   SUCCESS
R2 #1207 / run 33393872097   SUCCESS
```

`38a04c1...` is an empty `[full-ci]` acceptance commit over the exact `70e7f41...` code tree. Later S6D planning/audit commits are docs-only and do not replace this accepted code checkpoint.

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
MS-S6D   first-night perceived-ability semantic completion       IN PROGRESS
  S6D-0 coverage/ownership audit                                 COMPLETE
  S6D-1 high-value behavior REDs                                 NEXT
MS-S7    TB 480-template controlled semantic cutover             BLOCKED ON S6D

ALG-B2R  first-night Epistemic Gate resumption                   AFTER S7 / SEPARATE CAMPAIGN
MS-S8    NGJ/no-template production cutover                      QUEUED AFTER B2R UNLESS REPRIORITIZED
MS-S9    future-script generic acceptance                        QUEUED
REC-R1   unfinished-game stable-checkpoint work                  QUEUED SEPARATE CAMPAIGN
```

Active handoff:
`docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md`

S6D-0 audit:
`docs/MS_S6D_FIRST_NIGHT_PERCEIVED_ABILITY_AUDIT_2026-09-01.md`

Latest accepted code checkpoint doc:
`docs/MS_S6C_GENERIC_INFORMATION_SEMANTICS_CHECKPOINT_2026-08-31.md`

## 2. Frozen architecture

The causal setup order remains:

```text
Composition
-> Identity
-> Information
```

Accepted setup/information ownership through S6C:

```text
script + playerCount + setupSeed
-> candidate/provider legality                    [S2/S3/S4]
-> actual-composition diversity selection         [S5]
-> shown-identity legal options                    [S6A]
-> deterministic shown-identity commitment         [S6B]
-> seat/deal materialization
-> CommittedClocktowerSetup(actualRole + shownRole)
-> perceived ability / healthy semantics where migrated [S6C]
-> RELIABLE / DRUNK / POISONED impairment          [S6C]
-> deterministic AbilityObservation                [S6C]
```

Frozen rules:

- shown identity is a committed setup fact and recommendation may never choose/reroll it;
- S5 sees actual composition only;
- S6A owns shown-identity legality/options only;
- S6B owns deterministic identity commitment only;
- S6C/S6D own information semantics only;
- role ability semantics define legal display/truth before reliability is applied;
- Healthy, Poisoned and Drunk of the same perceived role must share one semantic definition;
- impairment policy owns truthful/false-family choice after the semantic space exists;
- A3/A4 Possible Worlds quality is a later layer, not part of S6D/S7.

## 3. Why S6D exists after accepted S6C

S6C is accepted and is not reopened. Its generic information architecture is correct for the domains it migrated, including the accepted 90/10 configurable impaired-family policy and Investigator Recluse/Spy registration repair.

The global algorithm design still has a later layer:

```text
role semantic candidate
-> recipient PlayerWorldSet BEFORE
-> candidate observation
-> PlayerWorldSet AFTER
-> epistemic quality/ranking
```

That is the future `ALG-B2R` route from v2.2 and is not yet production authority.

Current production is a hybrid:

```text
perceived-role-aware actor resolution
-> role-local / legacy candidate construction
-> legacyInformationCandidates
-> unified first-night pool
-> generic RELIABLE / DRUNK / POISONED selection
-> structured display/observation
```

S6D exists because the candidate-semantic layer is not yet complete for all six TB first-night B2 roles.

## 4. S6D-0 audit result — COMPLETE

The audited roles are:

```text
Washerwoman
Librarian
Investigator
Chef
Empath
Fortune Teller
```

See the dedicated audit for the full matrix. The durable findings are:

1. **Actor/waking identity is already correct.** First-night `roleActor()` uses perceived-role semantics, so actual Drunk + committed `shownRole=X` can be found as X. The general defect is not waking.
2. **Pair semantics are mixed.** Washerwoman remains Host-local; Librarian/Investigator have partial/strong typed healthy sources, but the typed first-night precompute still filters actual Librarian/Investigator holders only.
3. **Unreliable pair truth is raw-identity based.** It does not consistently reuse the registration-aware healthy truth space. Investigator/Recluse and WW/Librarian/Spy therefore expose a layering mismatch.
4. **Chef/Empath contain a direct P0 semantic split.** Healthy values are registration-aware, while unreliable actors explicitly switch to raw actual-identity values. Reliability currently changes the truth definition itself.
5. **Fortune Teller is query-dependent.** Existing code/test protects current/effective Demon authority, but not full Drunk/Poisoned semantic equivalence; preserve it as a separate boolean/query slice.
6. Existing migration/Foundation/UI tests prove parity, projection and selection mechanics but do not prove real-game perceived-role semantic generation from the same registration-aware source.

This confirms a real S6D correctness gap and justifies behavior REDs. No production/test code changed during S6D-0.

## 5. S6D implementation plan

```text
S6D-0  six-role coverage/ownership audit                         COMPLETE
S6D-1  high-value semantic behavior REDs                         NEXT
S6D-2  generic perceived-ability semantic request/result seam
S6D-3  pair family completion: WW / Librarian / Investigator
S6D-4  numeric family completion: Chef / Empath
S6D-5  query-dependent boolean completion: Fortune Teller
S6D-6  FirstNightInformationMigration shadow/parity integration
S6D-7  full acceptance + checkpoint
```

S6D-1 should establish only durable behavior contracts:

- registration-aware pair truth is invariant across RELIABLE/POISONED/DRUNK for the same perceived ability/display;
- actual Drunk + committed shownRole reaches the shown role semantic space without requiring actualRole==shownRole;
- Empath or Chef derives the same role-semantic truthful numeric value for healthy, poisoned and Drunk forms of the same perceived ability; reliability changes selection policy, not truth definition;
- committed shown identity is never mutated.

Do not test internal constants, helper calls, class existence, source strings or exact scoring details.

S6D must not introduce A3/A4 epistemic ranking, change the accepted 90/10 product policy, create role-specific Drunk/Poisoned engines, or broadly rewrite registration.

## 6. S7 — BLOCKED until S6D acceptance

After S6D acceptance, S7 performs the TB controlled semantic cutover:

```text
480 validated templates
-> S5 actual composition
-> S6A identity policy
-> S6B identity commitment
-> deal/materialize
-> CommittedClocktowerSetup
-> S6D-complete perceived-ability semantics
-> UI projection
```

S7 owns controlled production wiring, TB-specific adapter concerns, retirement of `selectedDrunkShownRole -> preset finalWeight`, parity/shadow evidence, and elimination of dual authority.

`legacyInformationCandidates` may remain temporarily as a UI/compatibility projection. Do not delete it until the new semantic authority is behaviorally proven.

S7 does not own A3/A4 epistemic quality scoring.

## 7. ALG-B2R — after S7, separate campaign

After S7 acceptance, pause MS-SETUP expansion before S8 by default and resume the v2.2 first-night Epistemic Gate in a separate campaign/PR:

```text
semantic candidate
-> recipient PlayerWorldSet BEFORE
-> apply observation
-> recipient PlayerWorldSet AFTER
-> exact epistemic quality metrics
-> ranking
```

Initial TB scope remains WW/Librarian/Investigator, Chef/Empath/Fortune Teller and local Spy/Recluse registration.

Preserve:

- A3 EnumeratedWorldSet as exact correctness baseline;
- A4/ZDD as shadow/prototype until separately device validated;
- failure/resource limits never become false UNSAT;
- actual-world narrative metrics remain separate from player-world epistemic metrics.

Longer route remains `ALG-B2R -> later Phase B as required -> Phase C quality/distribution gates -> C9 Unified Selector Production Rollout`.

## 8. Protected predecessor correctness

Preserve throughout S6D/S7:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
S5 cannot consume shown identity.
S6A legality cannot be rewritten by S6B/recommendation.
S6B commitment cannot feed back into S5.
S6C/S6D cannot mutate committed identity.
Healthy/Poisoned/Drunk share role semantics before impairment.
Start commits setup only once.
Restore never rerolls committed setup.
Invalid template data never silently broad-random-falls back.
Background work cannot mutate committed identities.
Only true completed games enter diversity/rotation history.
Completion persistence remains retry-safe.
```

Also preserve Dawn/Dusk retry convergence, Fortune Teller current/effective-state authority, poisoned Spy fail-safe behavior, living-Demon UI authority and current NGJ legality.

## 9. Validation and governance

Follow:

```text
AGENTS.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

Use risk-based T0/T1/T2/T3 evidence. Start with the smallest behavior RED. Use `:app:testFast` at logical T1 checkpoints and full Android/ASP/Real Clingo when required by the risk router/acceptance boundary. GitHub CI/R2 and exact remote diff audit remain acceptance requirements.

Keep PR #61 Draft. Do not merge, mark Ready, rebase or force-push without explicit user authorization.

## 10. Current documentation authority

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md
docs/MS_S6D_FIRST_NIGHT_PERCEIVED_ABILITY_AUDIT_2026-09-01.md
docs/MS_S6C_GENERIC_INFORMATION_SEMANTICS_CHECKPOINT_2026-08-31.md
docs/MS_S6C_GENERIC_IMPAIRED_INFORMATION_REPLAN_2026-08-31.md
docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md
docs/phase_a_exit_review_2026-08-20.md
docs/R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md
docs/design_plan_audit_2026-08-21.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

## 11. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap and active handoff;
3. read `docs/MS_S6D_FIRST_NIGHT_PERCEIVED_ABILITY_AUDIT_2026-09-01.md`;
4. preserve accepted S6C checkpoint `38a04c1353c883c3bda4b4a506085c3c1d2766bd` as the latest accepted code tree until S6D has its own acceptance;
5. re-query live main/branch/PR/checks;
6. begin **S6D-1 behavior RED**, not S7;
7. prefer registration-sensitive pair + numeric behavior proofs over copy-pasted six-role tests;
8. keep A3/A4 epistemic ranking out of S6D/S7;
9. do not start S8/REC-R1/broad App/Host work;
10. keep PR #61 Draft and unmerged.

## 12. Deferred / queued registry

| Area | Status |
|---|---|
| MS-SETUP | CURRENT — S6D IN PROGRESS |
| MS-S6C | COMPLETE / ACCEPTED |
| MS-S6D-0 audit | COMPLETE |
| MS-S6D-1 behavior REDs | NEXT |
| MS-S7 TB controlled semantic cutover | BLOCKED ON S6D |
| ALG-B2R first-night Epistemic Gate | AFTER S7 / NEW CAMPAIGN |
| MS-S8 NGJ/no-template cutover | QUEUED AFTER B2R UNLESS REPRIORITIZED |
| MS-S9 future-script acceptance | QUEUED |
| C9 Unified Selector Production Rollout | FUTURE AFTER B/C GATES |
| REC-R1 | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 checkpoint identity hardening | DEFERRED; re-evaluate under REC-R1 |
| Dawn crash cut-point matrix | DEFERRED |
