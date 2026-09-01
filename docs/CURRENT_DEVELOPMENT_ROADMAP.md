# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-01 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation or merge.

## 1. Current development context

```text
last merged product slice:
#67 — UX-R4: unify recommendation presentation
COMPLETE / VERIFIED / MERGED

final fully validated UX-R4 executable checkpoint:
6f10e8792e9535c1d125fae9b07e32e81fdfa2a3

UX-R4 merge commit:
d626093f5f527edfba181641cd2b07a50a559929

active next slice:
UX-R5 — small-domain specialization
```

Completed slice contracts, RED/GREEN evidence, CI checkpoints, and merge details live in:

`docs/COMPLETED_DEVELOPMENT_HISTORY.md`

Do not grow this active roadmap with repeated historical closeout detail. When an active slice is complete and merged, archive its detailed evidence in one batch and advance this file.

## 2. Campaign status

```text
UX-R1   dependency / legal-authority audit                     COMPLETE
UX-R2A  shared pair legal-domain authority                    COMPLETE / MERGED
UX-R2B  pair Manual -> legal-domain authority                 COMPLETE / VERIFIED / MERGED
UX-R3   remove global storyteller mode selector               COMPLETE / VERIFIED / MERGED
UX-R4   Top-1 + 0–2 alternatives + persistent Manual         COMPLETE / VERIFIED / MERGED

UX-R5   small-domain specialization                           NEXT

EPI-MQ / ALG
        Productive Uncertainty / PlayerWorldSet mainline      NEXT PRIMARY ALGORITHM CAMPAIGN

UX-R6   replace legacy ranking behind stable UX contract      QUEUED AFTER EPI-MQ
```

Create a fresh UX-R5 branch from live `main`. Do not reuse the merged UX-R4 branch or a docs-maintenance branch.

## 3. Frozen permanent architecture

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

## 4. Stable recommendation / Manual UX contract

Authority:

`docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`

Long-term ownership:

```text
Complete legal semantic candidate domain
        |
        +--> Manual clue selection
        |
        +--> Recommendation Provider
                 -> stable presentation adapter
                        -> Top-1
                        -> 0–2 alternatives
```

Accepted permanent conditions:

```text
recommendation unavailable != manual unavailable
```

and:

```text
automaticExecution = false
recommendationStyle = BALANCED   # temporary compatibility ranking input
```

UX-R4 additionally established:

- provider ordering and typed identity are preserved by presentation;
- normal combinatorial presentation exposes one primary plus at most two alternatives;
- sparse recommendation sets stay sparse;
- no synthetic filler alternatives;
- Manual continues to operate on the complete legal domain, not the recommendation shortlist;
- selecting a recommendation commits that exact typed candidate and registration semantics.

## 5. UX-R5 target — small-domain specialization

UX-R5 extends the stable presentation contract to interactions whose legal display domain is naturally small and should not use pair-style multi-step Manual UX.

Initial audit targets:

- Chef / other numeric information;
- Empath numeric information;
- Fortune Teller Yes/No information;
- other existing Yes/No or small finite-domain information flows only where already supported by current production semantics.

Target presentation rules:

### Number / small finite domain

When the legal display domain is genuinely small:

```text
primary recommendation
+
all remaining legal values when the complete domain comfortably fits the interaction
+
Manual authority remains explicit in semantics even if the UI can show the whole domain directly
```

Do not artificially cap a two- or three-value legal domain to the UX-R4 combinatorial shortlist if showing every legal value is clearer.

### Yes / No domain

```text
primary recommendation
+
the other legal result
```

There is no value in hiding one of only two legal outcomes behind a separate Manual drill-down.

### UX-R5 product rules

1. legal-domain authority remains upstream of recommendation;
2. presentation may expose the entire legal domain when the domain is small enough;
3. provider ranking determines which legal outcome is primary, not which outcomes exist;
4. recommendation absence must still leave the legal interaction playable;
5. exact typed outcome identity must flow into the existing structured commit path;
6. do not parse localized labels to reconstruct semantic values;
7. do not add new recommendation scoring/diversity heuristics;
8. do not expand UX-R5 into Productive Uncertainty or PlayerWorldSet production ranking;
9. avoid new role-specific UI branches when a typed small-domain adapter can express the same contract.

## 6. UX-R5 planning / implementation route

Before production edits, audit the existing small-domain paths:

```text
legal outcome generation
-> recommendation ordering / legacy compatibility projection
-> presentation model
-> NightStep UI
-> exact selected-outcome callback
-> AbilityObservation publication
```

Questions to answer first:

- Which roles already expose a complete typed legal outcome set?
- Which paths still conflate recommended choices with legal choices?
- Are numeric values represented structurally or only as display labels?
- Are Yes/No outcomes represented structurally?
- Can one generic small-domain presentation adapter cover number and boolean domains?
- Which current callbacks already commit exact structured outcomes?
- Where does Manual still need a separate control versus simply displaying every legal value?

Preferred boundary:

```text
complete legal small domain
      |
      +--> ordered recommendation view
      |
      +--> small-domain presentation adapter
              -> primary
              -> remaining legal outcomes
      |
      +--> existing structured commit path
```

Keep UX-R5 deliberately thin. Its job is stable presentation/ownership, not better recommendation intelligence.

## 7. Algorithm route after UX-R5

```text
EPI-MQ / ALG mainline
       -> PlayerKnowledgeSnapshot
       -> PlayerWorldSet BEFORE
       -> hypothetical player-visible observation
       -> PlayerWorldSet AFTER
       -> epistemic metrics
       -> misinformation-world quality
       -> Productive Uncertainty
       -> cognitive-consistency Recommendation Provider

UX-R6  replace legacy ranking behind the stable presentation contract
       without redesigning the user-facing selection surface
```

Primary algorithm authorities:

- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`
- `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`

## 8. Testing strategy

Authority: `docs/TESTING_STRATEGY.md`.

Use risk-based tests-first.

High-value UX-R5 contracts:

- small-domain legality is independent of recommendation coverage;
- provider ordering changes which legal outcome is primary, not which outcomes are selectable;
- Yes/No exposes both legal results without synthetic states;
- small numeric domains expose all legal values when the full domain fits the UX contract;
- selecting any displayed value commits the exact typed semantic outcome;
- recommendation absence still permits correct play;
- UX-R5 does not regress UX-R2B Manual/legal authority, UX-R3 Storyteller-confirmed execution, or UX-R4 pair presentation.

Avoid source-shape tests that only assert button/class/helper placement.

Use focused tests first, `:app:testFast` at the logical checkpoint, and broader CI according to repository risk classification and merge policy.

## 9. UX-R5 scope guards

Do not expand UX-R5 into:

- new recommendation ranking/scoring behavior;
- Productive Uncertainty;
- PlayerWorldSet production recommendation integration;
- A3/A4/ZDD production rollout;
- deletion of legacy preferences/enums merely for cleanup;
- broad future-script support;
- Host/App decomposition;
- unrelated persistence/recovery work.

Do not reopen UX-R2 legality ownership or UX-R4 pair presentation unless a real regression is found.

## 10. Documentation authority / resume protocol

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

For a new development conversation:

1. read root `AGENTS.md`;
2. read this active roadmap;
3. read completed history only when older slice detail is needed;
4. re-query live `main` rather than trusting stored SHA;
5. create a fresh UX-R5 branch;
6. audit typed legal-domain / recommendation / presentation / commit ownership for small-domain roles before editing;
7. preserve UX-R2B, UX-R3 and UX-R4 permanent contracts;
8. implement only the thin small-domain specialization;
9. then return to EPI-MQ / Productive Uncertainty;
10. keep A3 exact as correctness baseline and A4/ZDD shadow until separately validated.

## 11. Deferred / queued registry

| Area | Status |
|---|---|
| Clue UX-R5 small-domain specialization | NEXT IMMEDIATE SLICE |
| Legacy recommendation enhancement | MAINTENANCE-ONLY / NO NEW BROAD INVESTMENT |
| EPI-MQ Productive Uncertainty | NEXT PRIMARY ALGORITHM CAMPAIGN |
| ALG cognitive-consistency / PlayerWorldSet | NEXT PRIMARY ALGORITHM CAMPAIGN |
| UX-R6 legacy ranking replacement | QUEUED AFTER EPI-MQ |
| A4/ZDD production rollout | SHADOW / FUTURE AFTER EXACT BASELINE GATES |
| REC-R1 | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid wake-history authority | DEFERRED FOLLOW-UP |
