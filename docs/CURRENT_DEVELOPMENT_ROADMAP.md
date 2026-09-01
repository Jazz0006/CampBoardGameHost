# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-01 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation or merge.

## 1. Current development context

```text
live main at roadmap-split branch creation:
cf604f490eb0a4683f641088216e2077426387e9

last merged product slice:
#65 — UX-R3: remove global storyteller mode selector
COMPLETE / VERIFIED / MERGED

final fully validated UX-R3 executable checkpoint:
6cb9cb542b9e25d718a2a035e37475f99388ed2e

active next slice:
UX-R4 — unified recommendation presentation
```

Completed slice contracts, RED/GREEN evidence, CI checkpoints, and merge details now live in:

`docs/COMPLETED_DEVELOPMENT_HISTORY.md`

Do not grow this active roadmap with repeated historical closeout detail. When an active slice is complete and merged, move its detailed evidence to the history archive in one batch and advance this file to the next slice.

## 2. Campaign status

```text
UX-R1   dependency / legal-authority audit                     COMPLETE
UX-R2A  shared pair legal-domain authority                    COMPLETE / MERGED
UX-R2B  pair Manual -> legal-domain authority                 COMPLETE / VERIFIED / MERGED
UX-R3   remove global storyteller mode selector               COMPLETE / VERIFIED / MERGED

UX-R4   unified recommendation presentation                   NEXT
        Top-1 + 0–2 differentiated alternatives + Manual

UX-R5   small-domain specialization                           QUEUED

EPI-MQ / ALG
        Productive Uncertainty / PlayerWorldSet mainline      NEXT PRIMARY ALGORITHM CAMPAIGN

UX-R6   replace legacy ranking behind stable UX contract      QUEUED AFTER EPI-MQ
```

Create a fresh UX-R4 branch from live `main`. Do not reuse the merged UX-R3 branch or the roadmap-maintenance branch.

## 3. Frozen permanent architecture

The permanent causal order remains:

```text
Composition
-> committed actual identity
-> committed shown identity
-> perceived ability
-> complete healthy legal/truth semantic domain
-> interaction-scoped registration
-> RELIABLE / POISONED / DRUNK reliability state
-> recommendation/manual selection
-> AbilityObservation
-> durable player-visible history
-> UI
```

Permanent invariants:

- Drunk actual identity remains Drunk;
- shown identity is committed once and is not recommendation state;
- Healthy, Poisoned and Drunk of the same perceived role share role semantics before reliability;
- Spy/Recluse registration belongs to semantic truth construction, not recommendation heuristics;
- semantic legality/truth must not be owned by Host/UI compatibility projection;
- every supported information role must remain playable through a correct Manual/generated clue path even when recommendation support is absent;
- recommendation ranking remains downstream of the complete legal semantic domain;
- Manual is a permanent user authority path, not a recommendation style;
- A3 exact enumeration remains the correctness baseline;
- A4/ZDD remains shadow/prototype until equivalence and resource behavior are separately validated;
- approximation/resource failure must never become false UNSAT.

## 4. Recommendation / Manual authority

Long-term authority:

```text
Complete legal semantic candidate domain
        |
        +--> Manual clue selection                  # permanent user authority
        |
        +--> Recommendation Provider
                 |
                 +--> legacy compatibility provider # temporary fallback
                 +--> cognitive-consistency provider
                         -> PlayerWorldSet
                         -> epistemic metrics
                         -> Productive Uncertainty
```

The Recommendation Provider ranks legal candidates. It must never define the complete set of clues the Storyteller is allowed to choose.

Permanent acceptance condition established by UX-R2B:

```text
recommendation unavailable != manual unavailable
```

UX-R3 established the normal front-door policy:

```text
automaticExecution = false
recommendationStyle = BALANCED   # temporary compatibility ranking input
```

The old global Manual/Balanced/Aggressive/Gentle selector is no longer normal product UX. Recommendation content may be available without global preselection, but Storyteller decisions remain confirmed rather than automatically executed.

Detailed UX-R2B and UX-R3 contracts are archived in `docs/COMPLETED_DEVELOPMENT_HISTORY.md`.

## 5. UX-R4 target

Authority:

`docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`

UX-R4 establishes one stable recommendation presentation for combinatorial clue interactions:

```text
prominent Top-1 recommendation
        +
0–2 visually separated meaningful alternatives
        +
persistent Manual control
```

Initial target is the existing pair-information flow, especially first-night:

- Washerwoman;
- Librarian;
- Investigator.

The UX-R2B structured Manual/legal-domain seam must be reused rather than replaced.

### UX-R4 product rules

1. Recommendations are computed whenever supported; no global style selection is required.
2. The strongest current recommendation is visually dominant.
3. Normal presentation exposes at most two alternatives after the Top-1 recommendation.
4. Alternatives should be meaningfully differentiated when the provider has such information; UX-R4 must not invent a new ranking engine merely to force diversity.
5. Manual remains visibly available for the interaction.
6. Manual operates on the complete legal semantic domain, never on the recommendation shortlist.
7. Selecting Top-1 or an alternative must commit exactly that structured clue and its registration semantics.
8. Recommendation absence or low confidence must degrade to correct Manual play rather than disabling the interaction.
9. UX-R4 is a presentation/ownership slice, not the Productive Uncertainty algorithm implementation.

## 6. UX-R4 planning / implementation route

Before production edits, audit the current pair-information presentation path from recommendation candidates to UI and commit callback.

Expected questions to resolve:

```text
Where is recommendation ordering currently materialized?
Which model currently represents candidate rank/order?
Does UI receive enough typed candidate identity to render Top-1 vs alternatives safely?
Are recommendation candidates already limited/ordered, or does UI flatten a legacy pool?
Which callback commits a selected recommendation candidate?
Can Manual remain one persistent action without changing legal-domain authority?
What happens when recommendation candidates are empty or only one/two are available?
```

Preferred implementation boundary:

```text
legal semantic domain
      |
      +--> Manual
      |
      +--> existing Recommendation Provider / compatibility ranking
                 |
                 +--> presentation adapter
                         -> primary candidate
                         -> 0–2 alternatives
                 |
                 +--> existing structured selection commit path
```

Do not create UX-R4 ranking heuristics merely to choose “better” alternatives. If the existing provider only supplies ordered candidates, preserve that ordering and make the presentation contract stable; richer differentiation belongs to the later cognitive-consistency/Productive Uncertainty provider.

## 7. UX-R5 and algorithm route

After UX-R4:

```text
UX-R5  small-domain specialization
       -> Number: primary + all remaining legal values when domain fits
       -> Yes/No: primary + the other legal result

EPI-MQ / ALG mainline
       -> PlayerKnowledgeSnapshot
       -> PlayerWorldSet BEFORE
       -> hypothetical player-visible observation
       -> PlayerWorldSet AFTER
       -> epistemic metrics
       -> misinformation-world quality
       -> Productive Uncertainty
       -> cognitive-consistency Recommendation Provider

UX-R6  replace legacy ranking behind the stable
       Top-1 / alternatives / Manual presentation contract
```

UX-R4 and UX-R5 must remain deliberately thin. Do not build another temporary recommendation engine before Productive Uncertainty.

Primary algorithm authorities:

- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`
- `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`

## 8. Testing strategy

Authority: `docs/TESTING_STRATEGY.md`.

Use risk-based tests-first, not mechanical test-first for every edit.

High-value UX-R4 contracts:

- recommendation and Manual paths continue to share one legal semantic authority;
- combinatorial domains expose at most Top-1 + two normal recommendation alternatives before Manual navigation;
- Top-1 remains the provider's strongest ordered candidate;
- selecting any displayed recommendation commits exactly that candidate and its registration facts;
- Manual remains available independently of recommendation count;
- empty recommendation state still permits correct Manual play;
- one or two available recommendation candidates render without synthetic filler choices;
- UX-R4 presentation changes do not restore global automation/style behavior removed by UX-R3.

Avoid source-shape tests that only assert button/class/helper placement.

Use focused tests first; run `:app:testFast` at the logical UX-R4 checkpoint and broader CI only according to repository risk classification and merge policy.

## 9. UX-R4 scope guards

Do not expand UX-R4 into:

- small-domain UX-R5 specialization;
- new recommendation ranking/scoring behavior;
- Productive Uncertainty;
- PlayerWorldSet production recommendation integration;
- A3/A4/ZDD production rollout;
- deletion of legacy preference/enums merely for cleanup;
- new role-specific legacy recommendation heuristics;
- broad future-script support;
- Host/App decomposition;
- unrelated persistence/recovery work.

Do not reopen UX-R2 legality ownership. Recommendation and Manual must remain downstream of the same legal semantic authority.

## 10. Documentation authority

Active set:

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/COMPLETED_DEVELOPMENT_HISTORY.md
docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md
docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md
docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

Historical handoffs/checkpoints remain evidence only; do not load them by default unless a current question requires them.

## 11. Resume protocol

For a new development conversation:

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read `docs/COMPLETED_DEVELOPMENT_HISTORY.md` only when completed-slice detail is needed;
4. re-query live `main` rather than trusting a stored SHA;
5. create a fresh UX-R4 branch from live `main`;
6. audit the existing recommendation presentation/order/selection path before editing production code;
7. preserve UX-R2B Manual/legal-domain authority and UX-R3 Storyteller-confirmed front-door policy;
8. implement only the thin Top-1 + 0–2 alternatives + persistent Manual presentation contract;
9. proceed to UX-R5, then return to PlayerWorldSet / Productive Uncertainty;
10. keep A3 exact as correctness baseline and A4/ZDD shadow until separately validated.

## 12. Deferred / queued registry

| Area | Status |
|---|---|
| Clue UX-R4 recommendation presentation | NEXT IMMEDIATE SLICE |
| Clue UX-R5 small-domain specialization | QUEUED |
| Legacy recommendation enhancement | MAINTENANCE-ONLY / NO NEW BROAD INVESTMENT |
| EPI-MQ Productive Uncertainty | NEXT PRIMARY ALGORITHM CAMPAIGN |
| ALG cognitive-consistency / PlayerWorldSet | NEXT PRIMARY ALGORITHM CAMPAIGN |
| A4/ZDD production rollout | SHADOW / FUTURE AFTER EXACT BASELINE GATES |
| REC-R1 | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid wake-history authority | DEFERRED FOLLOW-UP |
