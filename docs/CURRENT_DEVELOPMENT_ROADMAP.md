# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-01 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Role: **SINGLE CURRENT PROJECT-STATUS / EXECUTION-SEQUENCE AUTHORITY**  
> Always re-query live GitHub state before implementation or merge.

## 1. Live development context

```text
live main at last verification:
6111ffe3863713895d2b21ab086cf31abcca4a4e

current branch:
codex/clue-ux-r2-manual-pair-selector

current Draft PR:
#63 — UX-R2: establish structured manual pair selection
DRAFT / OPEN / UNMERGED

last fully validated executable/code checkpoint on #63:
b014345afe2b003bad2a2ee23cb2cf809a2a4eb2

validation at that checkpoint:
CI #1373 — SUCCESS
R2 main-thread boundary #1288 — SUCCESS
```

Documentation-only commits may advance the PR head beyond the executable checkpoint above. Do not infer a new executable checkpoint from a docs-only head.

The 2026-09-01 documentation convergence and archive cleanup are docs-only: 35 completed/superseded/deferred documents were moved under `docs/archive/`, while 19 active/foundational Markdown files remain at `docs/` root. This is a historical cleanup checkpoint, not a permanent numeric constraint.

PR #63 remains Draft and must not be merged, marked ready, rebased, force-pushed, or otherwise broadened without explicit user authorization.

## 2. Completed foundation

The following campaigns are complete and no longer current execution work:

- MS-SETUP generic multi-script setup architecture, including TB production cutover and minimal NGJ/no-template architecture proof;
- PR #61 closeout and integration into `main`;
- pair-information display semantics authority for Washerwoman / Librarian / Investigator;
- `PairInformationLegalDomain` as the shared complete legal semantic domain for pair clues;
- final production zero-result legality gate: Librarian may show zero Outsiders, Investigator may not show zero Minions;
- pair recommender migration to consume `PairInformationLegalDomain` rather than owning clue legality;
- PR #62 integration into `main` through the validated tree represented by `6111ffe...`.

Historical MS-SETUP handoffs/checkpoints remain evidence only. They are not active execution instructions unless this roadmap explicitly reactivates them.

## 3. Frozen permanent information architecture

The permanent causal order is:

```text
actual game state
+ committed shown identity
+ perceived ability
+ interaction-scoped registration semantics
        ↓
complete legal semantic candidate domain
        ↓
reliability state: RELIABLE / DRUNK / POISONED
        ↓
shared information-decision authority
        ├── manual selection
        └── recommendation provider
        ↓
confirmed candidate identity + immutable decision snapshot
        ↓
AbilityObservation / durable player-visible history
        ↓
UI / review / epistemic replay
```

Permanent invariants:

- recommendation ranks legal candidates; it never defines the legal candidate set;
- manual selection remains available independently of recommendation coverage, confidence, style, or rollout state;
- manual and recommendation acceptance must converge on the same stable candidate identity and confirmation authority;
- registration is interaction-scoped semantic evidence, not a permanent mutation of Spy/Recluse identity;
- RELIABLE pair clues expose only currently truthful legal outcomes, including legal registration truth;
- DRUNK/POISONED pair clues expose the complete legal display space of the perceived ability;
- UI/presentation code must not recreate role legality rules already owned by semantic/domain layers;
- stale decision contexts must not be confirmable after relevant game/player-input revision changes.

## 4. Pair-information semantic contract

Current pair-role authority is:

```text
PairInformationDisplaySemantics
        ↓
PairInformationLegalDomain
        ├── RELIABLE     -> truthful legal outcomes only
        └── DRUNK/POISONED -> complete legal display space
```

High-value product scenarios that must remain explicitly protected:

### Washerwoman

- functioning, no Spy registration path: only truthful Townsfolk clues;
- functioning with Spy: legal Spy-as-Townsfolk registration truths may also be selected;
- Drunk/Poisoned: every Townsfolk on the current script × every legal unordered player pair excluding the source;
- never a zero-character result.

### Librarian

- functioning with actual Outsider(s): truthful Outsider clues, including legal Spy registration truth where applicable;
- functioning with zero Outsiders and no Spy registration path: exactly `No Outsiders`;
- functioning with zero actual Outsiders plus Spy: `No Outsiders` and any legal Spy-as-Outsider registered truths may coexist;
- Drunk/Poisoned: every Outsider on the current script × every legal unordered pair, plus `No Outsiders`.

### Investigator

- functioning: actual Minion truth plus legal Recluse-as-Minion registration truth;
- Drunk/Poisoned: every Minion on the current script × every legal unordered pair;
- never a `No Minions` result.

These rules belong to semantic/domain tests, not UI string logic.

## 5. Current #63 scope — UX-R2 foundation

PR #63 is a **foundation PR**, not the full product UI cutover.

Already implemented at validated checkpoint `b014345...`:

- `PairInformationManualSelection` provides a structured role/pair view over supplied `PairInformationLegalCandidate` values;
- every supplied legal candidate is preserved exactly once;
- seat order is normalized for manual resolution;
- exact candidate identity and interaction-scoped registration facts are preserved;
- zero-result is exposed only when supplied by the upstream legal domain;
- `ClocktowerNightStepUi` and `ClocktowerInformationStepBuilder` can transport a precomputed typed pair manual model without taking legality ownership.

### UX-R2A — next inside #63

Add durable typed pair semantic scenario contracts for Washerwoman, Librarian and Investigator, including functioning/impaired behavior and Spy/Recluse registration exceptions.

### UX-R2B — then inside #63

Converge pair selection on the existing shared information-decision lifecycle rather than creating a parallel confirmation path.

Target:

```text
PairInformationLegalDomain
-> shared InformationDecisionContext-style authority
-> structured pair manual projection
-> candidateId
-> confirm(MANUAL or RECOMMENDATION_ACCEPTED)
-> ConfirmedInformationDecision
```

Narrowly generalize the existing Foundation only if needed. Preserve existing Number behavior, candidate identity, registration facts, stale-context rejection, and recommendation-subset validation.

Do not duplicate `PairInformationOutcome` merely to satisfy the current generic bound.

Stop #63 before full production Host/UI pair-picker cutover.

## 6. Route after #63

```text
UX-R2C
pair production vertical slice

UX-R2D
manual-authority audit across Number / Yes-No / role-category-reveal families

UX-R3/R4
only after major-family manual authority is independent:
remove global Automatic/Manual/Style front door
+ freeze provider-neutral primary + 0–2 alternatives + manual shell

UX-R5
thin family-specific presentation polish only

EPI-MQ
scenario corpus
-> hypothetical visible observation
-> PlayerWorldSet BEFORE/AFTER
-> hard gates
-> credibility / persistence / breakability
-> cross-role interaction / confirmation-lock / fairness
-> Productive Uncertainty ranking
-> shadow evaluation
-> controlled rollout

UX-R6
replace legacy provider behind the unchanged UI/decision contract
```

Critical sequencing rule:

> Do not remove the global Manual front-door path until every currently supported major information family has an independent correct manual-authority path.

## 7. Productive Uncertainty boundary

The quality campaign owns ranking inside the already-legal domain. It does not own legality.

Conceptual flow:

```text
legal semantic candidate
-> hypothetical player-visible observation
-> recipient PlayerWorldSet BEFORE / AFTER
-> credibility
-> persistence
-> breakability
-> interaction value
-> confirmation-lock/fairness gates
-> Productive Uncertainty result
```

Do not build new permanent legacy recommendation heuristics merely to fill a future Top-3 UI. The provider contract allows 0–2 alternatives; legacy compatibility may legitimately provide fewer.

A3 exact remains the correctness oracle for small Trouble Brewing states. A4/ZDD remains shadow/deferred until separately reactivated and proven; timeout/resource exhaustion is unknown/degraded evaluation, never UNSAT.

## 8. Documentation authority / lifecycle

Current reading order:

1. root `AGENTS.md`;
2. this roadmap;
3. `NEXT_DEVELOPMENT_HANDOFF_2026-09-01_UX_R2_DECISION_FOUNDATION.md` — the one ACTIVE handoff;
4. `CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md` for clue product/manual/recommendation architecture;
5. `EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md` for the future quality campaign;
6. `CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md` for Possible Worlds / epistemic foundations;
7. workflow/testing authorities as named by `AGENTS.md`.

Dated handoffs/checkpoints are historical evidence by default. A handoff is active only when this roadmap explicitly names it as ACTIVE.

Completed/superseded/deferred documentation belongs under `docs/archive/`; archive content is context/evidence only unless this roadmap explicitly reactivates it.

## 9. Paused / deferred work

Not current execution scope:

- A4/ZDD production rollout;
- App-root/Host decomposition campaigns;
- older same-night follow-on work not explicitly reopened;
- legacy recommendation enhancement for its own sake;
- broad future-script clue-family expansion before the current authority boundary is stable.

## 10. Immediate resume instruction

Before the next executable edit:

1. re-query live `main`, PR #63 head/state/checks;
2. distinguish last executable checkpoint `b014345...` from later docs-only heads;
3. read the active UX-R2 decision-foundation handoff;
4. begin UX-R2A with typed pair semantic scenario contracts;
5. then proceed to UX-R2B shared `InformationDecision` adoption;
6. do not enter the large Host UI until #63 is closed and a separate R2C slice is opened.
