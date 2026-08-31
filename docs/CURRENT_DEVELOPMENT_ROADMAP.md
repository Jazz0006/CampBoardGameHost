# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation; documented checkpoints do not replace live-state verification.

## 1. Current development context

```text
live main at latest acceptance audit:
eed51bade5163790316a31e8295e2e841df90357

merged / fully validated TBSP checkpoint:
98ee982ef3590822cd06ac72a047b49afac3cfd6

current branch:
codex/ms-setup-generic-architecture

current Draft PR:
#61 — MS-SETUP: generic multi-script setup architecture
DRAFT / OPEN / UNMERGED

latest accepted code/test checkpoint:
MS-S6C
38a04c1353c883c3bda4b4a506085c3c1d2766bd

full acceptance:
CI #1290 / run 33393872108   SUCCESS
R2 #1207 / run 33393872097   SUCCESS
```

`38a04c1...` is an empty `[full-ci]` acceptance commit over the exact code tree from `70e7f41d1e30e5e701c02ceb95660572a99d27d4`; compare contains zero changed files.

Any later documentation-only carrier commit does **not** replace the accepted code/test checkpoint.

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
MS-S7    TB 480-template controlled semantic cutover             NEXT
MS-S8    NGJ/no-template production cutover
MS-S9    future-script generic acceptance

REC-R1   separate future unfinished-game stable-checkpoint work
```

Active handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md`

Latest accepted checkpoint:

`docs/MS_S6C_GENERIC_INFORMATION_SEMANTICS_CHECKPOINT_2026-08-31.md`

Historical S6C design/replan:

`docs/MS_S6C_GENERIC_IMPAIRED_INFORMATION_REPLAN_2026-08-31.md`

## 2. Frozen target architecture

The causal order remains:

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
-> MS-S5 select one candidate using ACTUAL-COMPOSITION diversity only      [ACCEPTED]
-> MS-S6A resolve legal shown-identity options/policy                      [ACCEPTED]
-> MS-S6B deterministically commit shown identity                          [ACCEPTED]
-> seat/deal materialization
-> CommittedClocktowerSetup(actualRole + shownRole)
-> MS-S6C resolve perceived ability / healthy information semantics        [ACCEPTED]
-> MS-S6C apply RELIABLE / DRUNK / POISONED generic information policy     [ACCEPTED]
-> recommendation generates information only                               [ACCEPTED]
```

Frozen ownership rules:

- shown identity is a setup fact, not a recommendation output;
- S5 actual-composition selection cannot consume shown identity or shown-identity history;
- S6A exposes legal shown-identity options only;
- S6B chooses/commits shown identity only;
- S6C consumes committed shown identity but cannot change it;
- role-specific ability semantics define legal information shape/truth semantics;
- generic impairment/recommendation policy owns unreliable information family selection and within-family ranking.

## 3. Accepted foundation through S6C

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

S6C accepts the committed identity and owns information semantics only.

Accepted pipeline:

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

S6C retires active Drunk-Investigator-specific recommendation ownership while keeping compatibility schema where required. Drunk and Poisoned share generic impairment semantics for the same supported information domain. The first-version family bias is explicit at approximately 90% false / 10% truthful rather than the old 97/3 default.

Investigator remains specialized only where its ability semantics require it: one Minion character, exactly two candidate seats, healthy truth/registration semantics and pair display shape.

Healthy Investigator registration correctness is now explicit:

- actual Spy/Minion truth remains `TRUE_TO_ACTUAL_STATE` with no unnecessary registration;
- Recluse may provide `TRUE_TO_REGISTERED_STATE` Minion truth;
- Recluse may register as an out-of-play Minion from the current script role catalog;
- selected truth that genuinely depends on Recluse registration carries registration metadata into `AbilityObservation`.

See `docs/MS_S6C_GENERIC_INFORMATION_SEMANTICS_CHECKPOINT_2026-08-31.md` for RED/GREEN and acceptance evidence.

## 4. MS-S7 — NEXT

S7 is the **Trouble Brewing controlled semantic cutover**. Do not start S8 until S7 has its own accepted checkpoint.

Target flow:

```text
480 validated templates
-> template SetupCandidate values
-> S5 actual-composition selection
-> S6A template identity policy
-> S6B identity commitment
-> deal/materialize
-> CommittedClocktowerSetup
-> S6C accepted information semantics consume committed shownRole
```

S7 owns:

- production wiring from validated TB templates into the accepted S2–S6C architecture;
- TB-specific Minion/style diversity adaptation at the correct adapter boundary;
- retirement of legacy `selectedDrunkShownRole -> preset finalWeight` coupling;
- preservation of current TB legality and deterministic setup behavior during cutover;
- exact migration evidence that old and new ownership are not both active for the same decision.

S7 does **not** own:

- NGJ/no-template production cutover — S8;
- broad persistence/recovery redesign — REC-R1;
- new information algorithms beyond accepted S6C;
- broad App/Host decomposition;
- destructive legacy schema removal unless directly required for safe TB cutover.

Before implementation, perform an S7-0 live audit and map the current TB production path from the 480 validated templates through deal/materialization and recommendation. Design focused behavior evidence before changing production ownership.

## 5. S6C acceptance evidence

Focused final GREEN:

```text
70e7f41d1e30e5e701c02ceb95660572a99d27d4
CI #1289 / run 33393595657: Android FAST SUCCESS / CI gate SUCCESS
R2 #1206 / run 33393595784: SUCCESS
```

Full acceptance:

```text
38a04c1353c883c3bda4b4a506085c3c1d2766bd
CI #1290 / run 33393872108:
  Android :app:testFull + :app:assembleDebug SUCCESS
  ASP contract SUCCESS
  Real Clingo SUCCESS
  CI gate SUCCESS
R2 #1207 / run 33393872097 SUCCESS
```

S6C-8 durable REDs were separately observed as single-failure tests before their GREEN implementations; details are in the S6C checkpoint doc.

## 6. Protected predecessor correctness

Preserve throughout S7 and later migration:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
S5 actual-composition selection cannot consume shown identity.
S6A legality cannot be rewritten by S6B or recommendation.
S6B commitment cannot feed back into S5.
S6C recommendation cannot mutate setup identity.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore never reselects/rerolls an already committed setup.
Invalid template data never silently falls back to broad-random setup.
Background work cannot mutate committed identities.
Only true completed games enter setup diversity/rotation history.
Completion persistence is retry-safe.
```

Also preserve Dawn/Dusk retry convergence, Fortune Teller current/effective-state authority, poisoned Spy fail-safe semantics, living-Demon UI authority and current NGJ legality until explicit migration.

## 7. Validation cadence

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

## 8. Writer / governance rules

Use GitHub connector for safe docs/tests/small-medium source changes according to root `AGENTS.md`.

Keep PR #61 Draft.

Do **not** merge, mark Ready, rebase or force-push without explicit user authorization.

## 9. Current documentation authority

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md
docs/MS_S6C_GENERIC_INFORMATION_SEMANTICS_CHECKPOINT_2026-08-31.md
docs/MS_S6C_GENERIC_IMPAIRED_INFORMATION_REPLAN_2026-08-31.md
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

## 10. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap;
3. read active handoff;
4. read `docs/MS_S6C_GENERIC_INFORMATION_SEMANTICS_CHECKPOINT_2026-08-31.md`;
5. re-query live `main`, branch, Draft PR #61 and current checks;
6. distinguish any docs-only carrier head from accepted S6C code checkpoint `38a04c1353c883c3bda4b4a506085c3c1d2766bd`;
7. begin **S7-0** with a read-only audit of current TB 480-template production wiring before adding REDs or modifying production;
8. preserve S5/S6A/S6B/S6C ownership boundaries;
9. do not start S8, REC-R1 or broad App/Host work;
10. keep PR #61 Draft and unmerged.

## 11. Deferred / queued work registry

| Area | Status |
|---|---|
| MS-SETUP generic multi-script setup architecture | CURRENT — S7 NEXT |
| MS-S1R setup persistence authority migration | COMPLETE / ACCEPTED |
| MS-S2 generic candidate/provider contracts | COMPLETE / ACCEPTED |
| MS-S3 optional template repository | COMPLETE / ACCEPTED |
| MS-S4 deterministic generated source | COMPLETE / ACCEPTED |
| MS-S4.5 shown-identity ownership correction | COMPLETE / ACCEPTED |
| MS-S5 actual-composition diversity selector | COMPLETE / ACCEPTED |
| MS-S6A shown-identity policy/options boundary | COMPLETE / ACCEPTED |
| MS-S6B deterministic shown-identity commitment | COMPLETE / ACCEPTED |
| MS-S6C generic information semantics + impairment ownership | COMPLETE / ACCEPTED |
| MS-S7 TB controlled semantic cutover | NEXT |
| REC-R1 unfinished-game recovery simplification | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED; re-evaluate under REC-R1 |
| Dawn systematic crash cut-point matrix | DEFERRED; committed-state convergence remains relevant |
| App Root S9.2 Active Game Persistence Boundary | SUPERSEDED IN SCOPE BY MS-S1R + REC-R1 |
