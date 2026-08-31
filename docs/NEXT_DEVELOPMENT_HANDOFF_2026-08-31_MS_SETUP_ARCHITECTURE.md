# NEXT DEVELOPMENT HANDOFF — MS-SETUP Generic Multi-Script Setup Architecture

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **MS-S6C COMPLETE / ACCEPTED — MS-S7 NEXT**

## 1. Live / accepted checkpoints

Live `main` at the S6C acceptance audit:

`eed51bade5163790316a31e8295e2e841df90357`

Campaign branch:

`codex/ms-setup-generic-architecture`

Draft PR:

`#61 — MS-SETUP: generic multi-script setup architecture — DRAFT / OPEN / UNMERGED`

Accepted S6C code/test checkpoint:

`38a04c1353c883c3bda4b4a506085c3c1d2766bd`

This is an empty `[full-ci]` checkpoint over the exact focused-GREEN tree `70e7f41d1e30e5e701c02ceb95660572a99d27d4`; compare contains zero files.

Acceptance evidence:

```text
Focused GREEN:
70e7f41d1e30e5e701c02ceb95660572a99d27d4
CI #1289 / run 33393595657: Android FAST SUCCESS / CI gate SUCCESS
R2 #1206 / run 33393595784: SUCCESS

Full checkpoint:
38a04c1353c883c3bda4b4a506085c3c1d2766bd
CI #1290 / run 33393872108:
  Android :app:testFull + :app:assembleDebug SUCCESS
  ASP contract SUCCESS
  Real Clingo SUCCESS
  CI gate SUCCESS
R2 #1207 / run 33393872097: SUCCESS
```

Any later docs-only carrier commit is **not** a replacement for accepted code checkpoint `38a04c1...`.

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

Latest accepted checkpoint document:

`docs/MS_S6C_GENERIC_INFORMATION_SEMANTICS_CHECKPOINT_2026-08-31.md`

## 2. Frozen architecture through S6C

The causal order remains:

```text
Composition
-> Identity
-> Information
```

Accepted flow:

```text
script + playerCount + setupSeed
-> candidate legality/source                       [S2/S3/S4 ACCEPTED]
-> actual-composition diversity selection          [S5 ACCEPTED]
-> shown-identity policy/options                    [S6A ACCEPTED]
-> deterministic shown-identity commitment          [S6B ACCEPTED]
-> seat/deal materialization
-> CommittedClocktowerSetup(actualRole + shownRole)
-> perceived ability + healthy truth semantics     [S6C ACCEPTED]
-> RELIABLE / DRUNK / POISONED generic policy      [S6C ACCEPTED]
-> recommendation produces information only        [S6C ACCEPTED]
```

Governing ownership rules:

- shown identity is a committed setup fact; recommendation may consume but may never choose/reroll it;
- S5 sees actual composition only;
- S6A owns shown-identity legality/options only;
- S6B owns deterministic identity commitment only;
- S6C owns information semantics only;
- role-specific ability code owns legal information shape/truth semantics;
- generic impairment policy owns reliability-family behavior and generic ranking.

## 3. S6C accepted result

S6C completed the redesign from a narrow Drunk-Investigator strategy into generic information semantics.

Accepted pipeline:

```text
committed shown identity
-> perceived ability role
-> role-specific ability/display semantics
-> healthy information candidate space
-> InformationReliability.RELIABLE / DRUNK / POISONED
-> generic ImpairedInformationPolicy
-> generic consequence/history ranking
-> deterministic AbilityObservation
```

Key accepted behavior:

- active recommendation no longer owns/rerolls Drunk shown identity;
- legacy Drunk-Investigator payloads remain compatibility-only where required rather than required active decisions;
- Drunk and Poisoned share the same impairment-family semantics for supported common information domains;
- first-version generic impaired-family policy is approximately 90% false / 10% truthful, explicitly tunable and not an official rule;
- recommendation style primarily ranks severity inside a family rather than creating role-specific truth odds;
- directly affected history/provisional information paths use generic information semantics;
- old setup simulation reads committed identity/generic observations rather than retired recommendation-owned identity.

## 4. Investigator / Recluse / Spy correctness

S6C-8 found and repaired the healthy Investigator registration gap without rewriting the registration subsystem.

Accepted rules:

```text
actual Minion / Spy truth
-> TRUE_TO_ACTUAL_STATE
-> no synthetic registration required

Recluse used as Investigator Minion truth
-> TRUE_TO_REGISTERED_STATE
-> explicit RECLUSE_ABILITY registration
-> registeredType MINION
-> registeredAlignment EVIL
-> SPECIFIC_MINION interaction
```

The production role catalog allows Recluse to register as an out-of-play Minion from the current script, not only the Minion actually in play.

If a displayed pair is already true because it contains the actual Minion, actual-state truth wins and no unnecessary registration is persisted. If truth genuinely depends on Recluse, the resulting `AbilityObservation` carries registration metadata.

See the S6C checkpoint doc for the two durable REDs and exact CI evidence.

## 5. Next slice — MS-S7

**MS-S7 is next. Do not start it as part of the S6C checkpoint carrier commit.**

S7 is the Trouble Brewing controlled semantic cutover.

Target production flow:

```text
480 validated templates
-> template SetupCandidate values
-> S5 actual-composition selection
-> S6A template identity policy
-> S6B identity commitment
-> deal/materialize
-> CommittedClocktowerSetup
-> S6C accepted information semantics
```

S7 specifically owns:

- tracing and replacing the remaining legacy TB setup-selection wiring;
- wiring the 480 validated templates into the accepted generic candidate/provider path;
- TB-specific Minion/style diversity adaptation at the adapter boundary rather than inside generic S5;
- retirement of `selectedDrunkShownRole -> preset finalWeight` coupling;
- ensuring only one authority owns composition, identity and information at each stage;
- preserving deterministic TB behavior and current legality during cutover.

### S7-0 first action

At the next development turn, do a read-only audit before writing tests or production:

```text
validated TB template asset / repository
-> current production setup selection
-> candidate/provider registry
-> S5 selector
-> S6A policy
-> S6B committer
-> seat/deal materialization
-> CommittedClocktowerSetup
-> S6C recommendation consumption
```

Identify exactly which legacy TB path is still authoritative and where the smallest controlled cutover boundary belongs.

Then design a small set of durable S7 behavior REDs. Do not create source-string or helper-call-shape tests merely to force the architecture.

## 6. S7 non-goals

Do not during initial S7 work:

- start NGJ/no-template production cutover — S8;
- change S5 generic diversity ownership;
- change S6A shown-identity legality;
- change S6B commitment namespace/algorithm;
- redesign accepted S6C impairment policy;
- perform broad persistence/recovery redesign — REC-R1;
- perform broad App/Host decomposition;
- aggressively delete compatibility schema unless directly required by the controlled TB cutover;
- merge or mark PR #61 Ready.

## 7. Protected predecessor invariants

Preserve:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
S5 actual-composition selection cannot consume shown identity.
S6A legality cannot be rewritten by S6B or recommendation.
S6B commitment cannot feed back into S5.
S6C recommendation cannot mutate committed identity.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore never reselects/rerolls an already committed setup.
Invalid template data never silently falls back to broad-random setup.
Background work cannot mutate committed identities.
Only true completed games enter setup diversity/rotation history.
Completion persistence is retry-safe.
```

Also preserve Dawn/Dusk retry convergence, Fortune Teller current/effective-state authority, poisoned Spy fail-safe semantics, living-Demon UI authority and current NGJ legality until explicit migration.

## 8. Validation workflow

Follow:

```text
AGENTS.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

Use risk-based T0/T1/T2/T3 evidence. Logical checkpoints require GitHub CI/R2 and exact remote diff audit; use full Android build/test and ASP/Real Clingo when the changed surface/risk router requires them.

Keep PR #61 Draft. Do not merge, mark Ready, rebase or force-push without explicit user authorization.

## 9. Documentation authority

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md
docs/MS_S6C_GENERIC_INFORMATION_SEMANTICS_CHECKPOINT_2026-08-31.md
docs/MS_S6C_GENERIC_IMPAIRED_INFORMATION_REPLAN_2026-08-31.md
docs/MS_S6B_SHOWN_IDENTITY_COMMITMENT_CHECKPOINT_2026-08-31.md
docs/MS_S6A_SHOWN_IDENTITY_POLICY_CHECKPOINT_2026-08-31.md
docs/MS_S5_SETUP_DIVERSITY_SELECTOR_CHECKPOINT_2026-08-31.md
docs/MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

## 10. New-conversation resume guard

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff;
4. read `docs/MS_S6C_GENERIC_INFORMATION_SEMANTICS_CHECKPOINT_2026-08-31.md`;
5. re-query live `main`, branch and Draft PR #61 head/state/checks;
6. distinguish any docs-only carrier head from accepted S6C code checkpoint `38a04c1353c883c3bda4b4a506085c3c1d2766bd`;
7. begin S7-0 with the read-only TB production-path audit above;
8. preserve S5/S6A/S6B/S6C ownership boundaries;
9. do not start S8/REC-R1/broad App/Host work;
10. keep PR #61 Draft and unmerged.
