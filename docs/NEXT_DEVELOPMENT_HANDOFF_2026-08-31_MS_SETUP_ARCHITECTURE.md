# NEXT DEVELOPMENT HANDOFF — MS-SETUP Generic Multi-Script Setup Architecture

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **MS-S4 COMPLETE / ACCEPTED — MS-S5 NEXT**

## 1. Accepted live checkpoints

Campaign baseline main:

`eed51bade5163790316a31e8295e2e841df90357`

Merged / fully validated TBSP checkpoint:

`98ee982ef3590822cd06ac72a047b49afac3cfd6`

Current campaign branch:

`codex/ms-setup-generic-architecture`

Current campaign PR:

`#61 — MS-SETUP: generic multi-script setup architecture — DRAFT / OPEN`

Accepted slice checkpoints:

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

MS-S4 validation:

```text
CI #1236 / run 33359464789   SUCCESS
Android FAST unit tests      SUCCESS
CI aggregate gate            SUCCESS
R2 #1153 / run 33359464788   SUCCESS
Full Android                 SKIPPED by risk router
ASP contract tests           SKIPPED by risk router
Real Clingo                  SKIPPED by risk router
```

Later documentation commits are carriers and do not replace the validated MS-S4 code/test checkpoint.

Always re-query live GitHub state before the next production write.

## 2. Product goal

Build one script-neutral Clocktower setup pipeline:

```text
script + playerCount + seed + diversity history
-> resolve script/ruleset setup provider
-> query optional template candidates
-> templates exist: validated template candidates
-> no templates: legal generated candidates
-> common deterministic diversity selector
-> commit shown-identity decisions
-> CommittedClocktowerSetup
```

The setup engine ends at `CommittedClocktowerSetup`. Persistence/recovery is an outer consumer. App/Host must not become the owner of script-specific setup policy.

## 3. Campaign sequence

```text
MS-S0   fresh live-state + TB/NGJ/setup ownership audit                            COMPLETE
MS-S0.5 recovery scope reduction audit + product boundary                          COMPLETE
MS-S1   generic persistence-independent CommittedClocktowerSetup + provenance      COMPLETE / ACCEPTED
MS-S1R  exact setup persistence authority migration + TB restore retirement         COMPLETE / ACCEPTED
MS-S2   generic SetupCandidate + source/provider registry contracts                 COMPLETE / ACCEPTED
MS-S3   optional TemplateRepository keyed by script + player count                  COMPLETE / ACCEPTED
MS-S4   deterministic seeded legal GeneratedSetupCandidateSource                    COMPLETE / ACCEPTED
MS-S5   common deterministic SetupDiversityHistory / scorer / selector facade       NEXT
MS-S6   generic shown-identity commitment policy
MS-S7   adapt TB 480-preset pipeline; preserve TB behavior/parity
MS-S8   adapt NGJ/no-template path; legality parity + deterministic seeded evidence
MS-S9   future no-template script needs no App-root branch

REC-R1  separate future unfinished-game stable-checkpoint simplification
```

Do not implement several slices together merely because they share the campaign.

## 4. Accepted setup-domain boundary through MS-S4

### Exact committed fact — MS-S1

```text
CommittedClocktowerSetup
├─ script: ScriptId
├─ setupSeed: Long
├─ assignments: ordered seats with actualRole + shownRole
└─ provenance: sourceKind + providerId + candidateId?
```

Exact committed identities, not provenance, are restore authority.

### Direct setup persistence — MS-S1R

```text
persisted exact CommittedClocktowerSetup
-> decode + validate
-> restore exact setup
```

TB completion/diversity history uses a compact committed rotation record. Restore no longer reloads the 480-preset asset or reconstructs `TroubleBrewingSetupPresetSelection`.

### Candidate/provider boundary — MS-S2

```text
SetupCandidate
├─ script
├─ canonical pre-seat actual-role multiset
├─ derived playerCount
└─ provenance

SetupCandidateRequest(script, playerCount, setupSeed)
SetupCandidateSource
ClocktowerSetupProvider
ClocktowerSetupProviderRegistry
```

Candidates deliberately exclude seating, shown identity, persistence and diversity history.

### Optional template lookup — MS-S3

```text
TemplateBucketKey(script, playerCount)
TemplateRepository
├─ find(script, playerCount)
└─ SetupCandidateSource.candidates(request)
```

Accepted behavior includes exact script/player-count lookup, normal empty result for an absent bucket, immutable/canonical results, TEMPLATE-only provenance with durable candidate IDs, and no seed/diversity/shown-role dependency.

### Deterministic generated source — MS-S4

Accepted production source:

`app/src/main/java/com/codex/campboardgamehost/clocktower/setup/GeneratedSetupCandidateSource.kt`

Accepted typed test:

`app/src/test/java/com/codex/campboardgamehost/clocktower/setup/GeneratedSetupCandidateSourceTest.kt`

Accepted boundary:

```text
SetupCandidateRequest(script, playerCount, setupSeed)
+ injected ValidatedClocktowerRuleset
+ providerId
-> deterministic legal pre-seat generated SetupCandidate
```

Accepted behavior:

- same request/ruleset produces the same candidate;
- different seeds can vary legal composition where choices exist;
- seeded ranking uses script + player count + seed + team + role ID;
- no unseeded `.random()` / `.shuffled()` path;
- generated provenance/provider attribution is stable;
- generated roles all belong to the injected ruleset;
- current 5–15 Clocktower base distribution is preserved;
- selected Baron applies one `+2 Outsider` adjustment, capped to available Outsiders, with Townsfolk reduced by the actual delta;
- unsupported selected setup modifiers fail explicitly rather than being silently ignored;
- no seat, shown identity, diversity/history, persistence, Android `Context`, UI state or production App/Host wiring enters the source.

No template-vs-generated fallback orchestration exists yet. MS-S3 missing-template `emptyList()` remains a normal repository result.

Authoritative checkpoint:

`docs/MS_S4_GENERATED_SETUP_CANDIDATE_SOURCE_CHECKPOINT_2026-08-31.md`

## 5. MS-S4 legacy parity notes to preserve

The legacy NGJ `generateClocktowerAssignments(...)` remains untouched and still mixes:

```text
actual-role composition generation
+ shuffled assignment order
+ Drunk shown-role selection
```

MS-S4 introduced a new pure source but did **not** cut NGJ production over to it.

Existing `NoGreaterJoySetupRegressionTest` remains unchanged parity evidence for the legacy NGJ role pool and 5/6-player base distribution/startability.

The current legacy Baron adjustment semantics preserved by MS-S4 are:

```text
select Minion(s)
-> if Baron selected, request +2 Outsiders
-> cap to Outsiders available in the script
-> reduce Townsfolk by the actual Outsider increase
```

Do not re-apply this modifier in a later stage.

## 6. MS-S5 immediate objective

The next production slice is the common deterministic diversity layer.

Target direction already established by the campaign:

```text
legal SetupCandidate values
+ generic setup diversity history
+ deterministic selection seed/context
-> one selected SetupCandidate
```

MS-S5 owns diversity/history scoring and deterministic candidate selection policy. It must not absorb candidate legality generation or shown-identity commitment.

Do **not** begin MS-S5 automatically from this handoff; start only after a fresh live-state audit in the next development turn.

## 7. MS-S5 boundaries to preserve

When MS-S5 begins, keep these responsibilities separate:

```text
MS-S3  template candidate storage/lookup
MS-S4  generated actual-role composition legality
MS-S5  diversity history + scoring + candidate selection
MS-S6  shown-identity commitment
MS-S7  TB production adaptation
MS-S8  NGJ production cutover
```

A selector may rank or choose among legal candidates; it must not become a second rules engine that changes role legality or applies Baron/setup modifiers again.

The selector must consume `SetupCandidate` as the existing canonical pre-seat actual-role multiset. Seat assignment and Drunk shown identity remain later concerns.

## 8. Evidence strategy for the next slice

Follow root `AGENTS.md`, `docs/TESTING_STRATEGY.md`, and `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`.

Use risk-based typed evidence:

- existing MS-S2/S3/S4 tests count as predecessor evidence;
- add the smallest typed contracts that freeze generic diversity-history identity, deterministic scoring/tie-breaking and selector behavior;
- do not manufacture source-string RED when typed behavior proof is practical;
- run focused evidence first, then the risk-routed `:app:testFast` checkpoint when the logical slice is ready;
- observe GitHub CI/R2 before acceptance.

Do not require a full merge-level T4 gate for every micro-slice unless risk or workflow routing requires it.

## 9. Explicit MS-S5 non-goals

Do not broaden the next slice into:

- Drunk/shown-identity commitment — MS-S6;
- TB 480-preset production adaptation — MS-S7;
- NGJ production cutover — MS-S8;
- seat assignment/shuffle;
- generic template-vs-generated fallback orchestration unless explicitly made part of a later policy slice;
- setup persistence changes;
- App/Host decomposition;
- broad unfinished-game recovery cleanup;
- Mayor / Imp / Monk / Attack-Protect / A3 / A4 / ZDD work;
- PR Ready/merge/rebase/force-push changes.

## 10. Recovery product boundary

Supported recovery goal remains:

```text
best-effort crash / process-death recovery
-> latest supported stable committed checkpoint
-> restore committed setup + committed game facts exactly
-> resume/restart at a safe domain/action boundary
```

Broad unfinished-night simplification remains future REC-R1 work and is outside MS-SETUP setup-selection slices.

## 11. Protected predecessor invariants

Preserve throughout MS-SETUP:

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

Also preserve Dawn/Dusk retry convergence, Fortune Teller current/effective-state authority, poisoned Spy fail-safe semantics, current living-Demon UI authority and NGJ setup legality until explicit migration.

## 12. Exact MS-S4 scope audit

Compared with pre-MS-S4 docs carrier `e33aa8fcad84fd0d5f99e7ee3843527a1296c520`, accepted code/test checkpoint `6de0e8c99c89a091615c513255adbdb773b3cc69` changed exactly two files:

```text
ADDED  app/src/main/java/com/codex/campboardgamehost/clocktower/setup/GeneratedSetupCandidateSource.kt
ADDED  app/src/test/java/com/codex/campboardgamehost/clocktower/setup/GeneratedSetupCandidateSourceTest.kt
```

No legacy App, TB preset, NGJ production, Host, persistence, recovery, template repository or unrelated rules-engine file changed.

## 13. Documentation authority

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
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

## 14. Resume guard

Treat `6de0e8c99c89a091615c513255adbdb773b3cc69` as the accepted MS-S4 code/test checkpoint unless a later production commit deliberately supersedes it.

At the next development turn:

1. read root `AGENTS.md`, roadmap, this handoff and the MS-S4 checkpoint;
2. re-query live `main`, branch, Draft PR #61 and current checks;
3. distinguish docs-only carrier head from the accepted code/test checkpoint;
4. start with an MS-S5 audit/design and typed contract boundary;
5. do not edit App/Host production flow merely to wire the new selector early;
6. keep PR #61 Draft and do not merge, mark Ready, force-push or rebase without explicit authorization.
