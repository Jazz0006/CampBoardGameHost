# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation; documented checkpoints do not replace live-state verification.

## 1. Current development context

```text
campaign baseline main at last audit:
eed51bade5163790316a31e8295e2e841df90357

merged / fully validated TBSP checkpoint:
98ee982ef3590822cd06ac72a047b49afac3cfd6

current branch:
codex/ms-setup-generic-architecture

current Draft PR:
#61 — MS-SETUP: generic multi-script setup architecture
DRAFT / OPEN

last audited incomplete S6C production head:
11b18d1ea06fed20490fe1db07f6c189b7ffbe64
CI #1267 / run 33367729757   FAILURE in Android full validation
R2 #1184                        SUCCESS

latest accepted code/test checkpoint:
MS-S6B
d4cf3969aabcea7433b96b5b320171fbc821853e
```

Later S6C commits and documentation carrier commits do **not** replace the accepted S6B checkpoint unless a later explicit acceptance section says so.

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
MS-S6C   generic information semantics + impairment ownership    REPLANNED / NEXT
MS-S7    TB 480-template controlled semantic cutover
MS-S8    NGJ/no-template production cutover
MS-S9    future-script generic acceptance

REC-R1   separate future unfinished-game stable-checkpoint work
```

Active handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md`

Authoritative S6C replan:

`docs/MS_S6C_GENERIC_IMPAIRED_INFORMATION_REPLAN_2026-08-31.md`

Latest accepted checkpoint:

`docs/MS_S6B_SHOWN_IDENTITY_COMMITMENT_CHECKPOINT_2026-08-31.md`

## 2. Frozen target architecture

The setup/information causal order remains:

```text
Composition
-> Identity
-> Information
```

Current target flow:

```text
script + playerCount + setupSeed
-> resolve script/ruleset setup provider
-> query optional template candidates or legal generated candidates
-> MS-S5 select one candidate using ACTUAL-COMPOSITION diversity only
-> MS-S6A resolve legal shown-identity options/policy               [COMPLETE]
-> MS-S6B deterministically commit shown identity                   [COMPLETE]
-> seat/deal materialization
-> CommittedClocktowerSetup(actualRole + shownRole)
-> MS-S6C resolve perceived ability semantics from committed state  [NEXT]
-> MS-S6C apply RELIABLE / DRUNK / POISONED information policy     [NEXT]
-> recommendation generates information only
```

Frozen ownership rules:

- shown identity is a setup fact, not a recommendation output;
- S5 actual-composition selection cannot consume shown identity or shown-identity history;
- S6A exposes legal shown-identity options only;
- S6B chooses/commits shown identity only;
- S6C may consume committed shown identity but cannot change it;
- role-specific ability semantics may define legal information shape/truth semantics;
- generic impairment/recommendation policy owns how unreliable information is selected.

## 3. Accepted foundation through S6B

### MS-S1 / S1R

`CommittedClocktowerSetup` is the immutable exact initial setup fact. Persistence/recovery stores and restores exact actual/shown identities and never reruns setup selection or recommendation.

### MS-S2

`SetupCandidate` is a canonical pre-seat **actual-role multiset**. It has no shown identity, seating, persistence schema or recommendation history.

### MS-S3

`TemplateRepository` owns actual-role template candidate lookup only. Template-specific shown-identity metadata is reached separately through S6A provenance-keyed policy sources.

### MS-S4

`GeneratedSetupCandidateSource` owns deterministic legal actual-role generation only and preserves current player-count distribution / Baron setup legality without unseeded randomness.

### MS-S4.5

Shown identity was removed from composition authority. Legacy TB selected/repeated Drunk shown-role weighting must never re-enter S5 actual-role candidate scoring.

### MS-S5

`SetupDiversityHistory` / scorer / selector use actual-role composition only. History is script + player-count scoped. Roles common to every candidate are excluded from overlap scoring. Selection is deterministic and order-independent under its seed.

### MS-S6A

`SetupShownIdentityPolicyResolver` resolves legal shown-identity options after composition selection.

For generated Drunk setup, legal options are script Townsfolk not already actual in play. Template metadata is normalized through a separate provenance-keyed source. S6A performs no selection.

### MS-S6B

`SetupShownIdentityCommitter` consumes selected candidate + S6A policy + setup seed and deterministically commits shown identity under namespace:

```text
setup-shown-identity-v1
```

S6B is pre-seat, uses canonical option pools, does not consume history/recommendation state and fails closed on inconsistent policy/candidate input.

The accepted S6B checkpoint remains:

`d4cf3969aabcea7433b96b5b320171fbc821853e`

## 4. MS-S6C — REPLANNED / NEXT

S6C is no longer a narrow “Drunk shown as Investigator” patch.

Authoritative design:

`docs/MS_S6C_GENERIC_IMPAIRED_INFORMATION_REPLAN_2026-08-31.md`

Target pipeline:

```text
committed shown identity
-> perceived ability role
-> role-specific ability/display semantics
-> healthy information candidate space
-> InformationReliability.RELIABLE / DRUNK / POISONED
-> generic ImpairedInformationPolicy
-> generic consequence/history ranking inside the selected family
-> deterministic information recommendation
```

### 4.1 Investigator: semantics special, strategy generic

Preserve Investigator-specific ability semantics such as:

```text
shown information = one Minion character
candidate seats = exactly two
healthy truth / registration semantics
UI/display shape = pair / EitherOne
```

Retire Drunk-Investigator-specific storyteller strategy.

Current legacy concepts to remove from **active new production ownership** include, directly or indirectly:

```text
StorytellerDecision.DrunkInvestigatorInfo
StorytellerDecisionKind.DRUNK_INVESTIGATOR_INFO
candidate family "drunk-investigator-info"
drunkInvestigatorShownMinion
clocktowerRecommendedDrunkInvestigatorRoleName
clocktowerRecommendedDrunkInvestigatorSeats
investigatorDisplaySuitability
special Drunk-Investigator real-Evil hit / avoid scoring
```

Legacy schema/types may temporarily remain for old-save/history compatibility. Do not perform destructive schema cleanup inside S6C.

### 4.2 Shared Drunk / Poisoned impairment semantics

Use one impairment layer wherever the perceived ability information domain is the same.

```text
Drunk:
abilityRole = committed shownRole

Poisoned:
abilityRole = current/actual role ability
```

Prefer reuse of existing infrastructure:

```text
AbilityFunctioningSemantics
InformationReliability
ImpairedInformationPolicy
DynamicCandidateGenerator
MalfunctionPolicy
ConsequenceEvaluator
DecisionCandidate
EffectDraft.PlayerInformation
TruthRelation
WeightedStableSelector
```

Do not create a parallel setup-only Drunk misinformation engine.

### 4.3 Retire fixed 97/3 impairment default

Current generic impaired-family policy uses approximately:

```text
false      97%
truthful    3%
```

This is now explicitly targeted for replacement.

Approved first-version generic product target:

```text
false-family weight      90%
truthful-family weight   10%
```

Requirements:

- make this an explicit policy/tuning boundary, not scattered magic-number literals;
- preserve deterministic selection;
- treat 90/10 as a first-version product choice, not an official BotC rule or immutable constant;
- calibrate future alternatives through deterministic simulation/gameplay evidence;
- `GENTLE / BALANCED / AGGRESSIVE` should mainly control **severity inside a legal false family**, not become another independent truth-probability system.

### 4.4 Remove Investigator-specific scoring heuristics

Retire special behavior equivalent to:

```text
real Evil in impaired Investigator pair -> special quality penalty
a pair avoiding real Evil -> special beginner-safety reward
shown Minion role -> investigatorDisplaySuitability bonus
Drunk-Investigator-only family weighting
```

Generic consequence/history scoring may still distinguish outcomes based on actual game consequences. It must not do so merely because the ability is Investigator.

Manual clue choice remains available for experienced Storytellers.

## 5. S6C test strategy — meaningful RED only

Follow root `AGENTS.md`: risk-based tests-first, not RED ceremony.

Before adding a new test ask:

> If implementation internals are substantially refactored later but the intended product behavior remains correct, should this test still pass and remain useful?

If no, do not add it merely to force an implementation shape.

### Core new behavior REDs

Keep the set to roughly 4–5 durable scenarios:

1. **Committed identity ownership**  
   actual Drunk + committed shownRole X -> recommendation cannot change/reroll X.

2. **Shared impairment semantics**  
   Drunk and Poisoned with the same perceived ability information domain can use the same legal information semantics under their reliability state; do not test exact internal calls.

3. **No Investigator-specific Evil downgrade**  
   otherwise-equivalent legal impaired Investigator outcomes are not forced to lower quality solely because one pair contains actual Evil; do not assert exact scores/rule IDs.

4. **Non-Investigator Drunk path**  
   use a currently supported role such as Empath if appropriate and prove Drunk shown as that role receives role-compatible information without Investigator fallback/shown-role reroll.

5. **Stale recommendation protection**  
   when an ability-state dependency changes, stale prior recommendation must not remain current/visible.

### Do not create brittle REDs for

```text
class/type absence
helper/generator call shape
DTO/cache field names
source-string absence
exact internal numeric literals for 90/10
```

For 90/10, use policy-level/distribution evidence and deterministic simulation rather than implementation-shape assertions.

## 6. S6C implementation sequence

```text
S6C-0  live main / PR head / checks audit
        distinguish docs-only carriers and incomplete S6C work from accepted S6B
        capture exact CI #1267 Android failure
        complete current Investigator/Drunk-Investigator legacy audit

S6C-1  add only high-value behavior REDs exposing real gaps

S6C-2  establish/reuse generic ability-semantics seam

S6C-3  unify Drunk/Poisoned impairment path
        retire 97/3
        establish explicit 90/10 first-version policy

S6C-4  remove Investigator-specific recommendation heuristics

S6C-5  stop active new production generation/ownership of DrunkInvestigatorInfo
        retain compatibility only where required

S6C-6  genericize directly affected effect/history representation

S6C-7  genericize directly affected provisional information invalidation
        protect behavior, not variable names

S6C-8  audit healthy Investigator registration correctness
        especially Recluse/Spy interactions
        fix only minimum required correctness seam

S6C-9  focused GREEN + triggered T1/T2/T3
        :app:testFull + :app:assembleDebug
        CI/R2 + exact diff audit
        checkpoint docs
STOP
```

S6C remains IN PROGRESS until acceptance validation is green.

## 7. Investigator registration audit

During S6C, verify whether healthy Investigator candidate generation correctly consumes registration semantics rather than only raw actual role type.

Focused question:

```text
Healthy Investigator
+ Recluse / Spy registration interactions
-> is truthful candidate generation correct?
```

Reuse existing registration authority where available.

If a correctness gap exists and blocks the generic Investigator semantics, repair the minimum seam. Do not expand into a complete registration-subsystem rewrite.

## 8. Strict S6C non-goals

Do not in S6C:

- change `SetupCandidate`;
- regenerate/rescore S5 actual-role candidates;
- let shown identity/history affect S5;
- change S6A legal shown-role resolution;
- change S6B commitment selection or namespace;
- let recommendation mutate setup identity;
- perform TB production cutover — S7;
- perform NGJ production cutover — S8;
- perform broad persistence/recovery redesign;
- aggressively remove legacy persisted schema;
- invent misinformation algorithms for unsupported future roles merely for completeness;
- perform broad App/Host decomposition;
- rewrite the whole registration subsystem.

## 9. MS-S7 / MS-S8 boundaries

### S7 — Trouble Brewing controlled cutover

```text
480 validated templates
-> template SetupCandidate values
-> S5 actual-composition selection
-> S6A template identity policy
-> S6B identity commitment
-> deal/materialize
-> CommittedClocktowerSetup
-> S6C information semantics consume committed shownRole
```

S7 owns TB-specific Minion/style diversity adaptation and retirement of legacy `selectedDrunkShownRole -> preset finalWeight` coupling.

### S8 — NGJ / no-template cutover

```text
GeneratedSetupCandidateSource
-> S5
-> S6A generated identity policy
-> S6B commitment
-> deal/materialize
-> CommittedClocktowerSetup
-> S6C information semantics consume committed shownRole
```

Do not start either cutover until S6C is accepted.

## 10. Protected predecessor correctness

Preserve throughout migration:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
S5 actual-composition selection cannot consume shown identity.
S6A legality cannot be rewritten by S6B or recommendation.
S6B commitment cannot feed back into S5.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore never reselects/rerolls an already committed setup.
Invalid template data never silently falls back to broad-random setup.
Background work cannot mutate committed identities.
Only true completed games enter setup diversity/rotation history.
Completion persistence is retry-safe.
```

Also preserve Dawn/Dusk retry convergence, Fortune Teller current/effective-state authority, poisoned Spy fail-safe semantics, living-Demon UI authority and current NGJ legality until explicit migration.

## 11. Validation cadence

Follow:

```text
AGENTS.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

Use risk-based evidence:

- T0 = smallest directly relevant behavior/evidence;
- `:app:testFast` = logical-checkpoint T1;
- trigger T2/T3 based on recommendation/rules scope;
- S6C acceptance requires `:app:testFull` and `:app:assembleDebug`;
- run ASP contract / Real Clingo when required by current strategy/risk router;
- GitHub CI/R2 and exact remote diff audit remain acceptance requirements.

Do not create source-string REDs or source-level implementation guards when a stable typed behavior seam can prove the contract.

## 12. Writer / governance rules

Use GitHub connector for safe docs/tests/small-medium source changes according to root `AGENTS.md`.

Keep PR #61 Draft.

Do **not** merge, mark Ready, rebase or force-push without explicit user authorization.

## 13. Current documentation authority

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md
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

## 14. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap;
3. read active handoff;
4. read `docs/MS_S6C_GENERIC_IMPAIRED_INFORMATION_REPLAN_2026-08-31.md`;
5. read S4.5, S5, S6A and S6B checkpoint docs;
6. re-query live `main`, branch, Draft PR #61 and current checks;
7. distinguish docs-only carrier head / incomplete S6C work from accepted S6B checkpoint `d4cf3969aabcea7433b96b5b320171fbc821853e`;
8. inspect exact current full-CI failure before production modification;
9. begin S6C-0 and proceed with meaningful behavior REDs only;
10. preserve the approved first-version 90/10 generic impairment-bias direction unless explicitly revisited with the user;
11. do not start S7/S8 or broad persistence/App/Host work;
12. keep PR #61 Draft and unmerged.

## 15. Deferred / queued work registry

| Area | Status |
|---|---|
| MS-SETUP generic multi-script setup architecture | CURRENT — S6C REPLANNED / NEXT |
| MS-S1R setup persistence authority migration | COMPLETE / ACCEPTED |
| MS-S2 generic candidate/provider contracts | COMPLETE / ACCEPTED |
| MS-S3 optional template repository | COMPLETE / ACCEPTED |
| MS-S4 deterministic generated source | COMPLETE / ACCEPTED |
| MS-S4.5 shown-identity ownership correction | COMPLETE / ACCEPTED |
| MS-S5 actual-composition diversity selector | COMPLETE / ACCEPTED |
| MS-S6A shown-identity policy/options boundary | COMPLETE / ACCEPTED |
| MS-S6B deterministic shown-identity commitment | COMPLETE / ACCEPTED |
| MS-S6C generic information semantics + impairment ownership | REPLANNED / NEXT |
| REC-R1 unfinished-game recovery simplification | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED; re-evaluate under REC-R1 |
| Dawn systematic crash cut-point matrix | DEFERRED; committed-state convergence remains relevant |
| App Root S9.2 Active Game Persistence Boundary | SUPERSEDED IN SCOPE BY MS-S1R + REC-R1 |
