# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-02 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation or merge.

## 1. Current development context

```text
last merged product slice:
#68 — UX-R5: specialize small-domain presentation
COMPLETE / VERIFIED / MERGED

final fully validated UX-R5 checkpoint:
2f56649e71d38c21f66df598e1e8df0c990090dd

UX-R5 merge commit:
563470a2c3b4e3dc10732e00827e33ebee00884a

active next campaign:
EPI-MQ / ALG — Productive Uncertainty / PlayerWorldSet mainline
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
UX-R5   small-domain specialization                           COMPLETE / VERIFIED / MERGED

EPI-MQ / ALG
        Productive Uncertainty / PlayerWorldSet mainline      NEXT PRIMARY CAMPAIGN

UX-R6   replace legacy ranking behind stable UX contract      QUEUED AFTER EPI-MQ
```

Create a fresh EPI-MQ branch from live `main`. Do not reuse the merged UX-R5 branch or a docs-maintenance branch.

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
```

Permanent conditions:

```text
recommendation unavailable != manual unavailable
```

and normal product execution remains Storyteller-confirmed / ASSISTED.

For combinatorial clue domains established by UX-R4:

```text
Top-1 recommendation
+ 0–2 alternatives
+ persistent Manual over the complete legal domain
```

For naturally small legal domains established by UX-R5:

```text
primary recommendation
+ all remaining legal outcomes when the whole domain comfortably fits
```

Additional permanent UX-R5 conditions:

- legality is upstream of recommendation;
- recommendation may mark the primary outcome but cannot create, remove, or hide legal outcomes;
- recommendation absence leaves the complete legal interaction playable;
- numeric/Boolean semantic identity is typed end-to-end;
- localized display labels are never parsed to reconstruct semantic values;
- Fortune Teller Yes/No remains bound to the exact actor and selected two seats;
- small-domain confirmation continues through the Foundation structured commit authority.

## 5. Active target — EPI-MQ / Productive Uncertainty

Primary authorities:

- `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`
- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`

Target algorithm route:

```text
PlayerKnowledgeSnapshot
-> PlayerWorldSet BEFORE
-> hypothetical player-visible observation
-> PlayerWorldSet AFTER
-> epistemic metrics
-> misinformation-world quality
-> Productive Uncertainty
-> cognitive-consistency Recommendation Provider
```

The next campaign should improve recommendation intelligence **behind the stable UX contract**, not redesign the selection surface.

Core misinformation-quality goals:

- credible rather than random;
- sustainable across future observations;
- interactive with other roles and public claims;
- not trivially self-confirming or impossible to break;
- eventually breakable through meaningful discovery paths;
- fair to both factions and compatible with player agency.

## 6. EPI-MQ implementation route

Before production ranking changes, re-audit the current epistemic path:

```text
PlayerKnowledgeSnapshot
-> PlayerWorldSet / exact historical replay
-> candidate hypothetical observation
-> BEFORE / AFTER world sets
-> measurable epistemic effects
-> Productive Uncertainty score/features
-> Recommendation Provider
-> existing UX-R4 / UX-R5 presentation
```

Initial questions:

- Which player-visible observations already have durable typed identity suitable for hypothetical replay?
- Which epistemic metrics are already available without introducing hidden Storyteller facts into player knowledge?
- How should credibility, ambiguity, persistence, cross-role interaction, confirmation-lock risk, breakability, faction impact, and player agency be represented?
- Which metrics can be validated against A3 exact enumeration before any ranking behavior changes?
- What resource/timeout behavior must fail closed without becoming false UNSAT?
- Which existing legacy ranking inputs can remain compatibility-only until UX-R6?

Preferred sequencing:

1. establish typed epistemic metric contracts with behavior-first tests;
2. compute BEFORE/AFTER world-set effects on exact A3 baseline;
3. add Productive Uncertainty features without changing UI contracts;
4. validate ranking behavior in shadow/characterization form;
5. only then connect the cognitive-consistency provider to production ranking;
6. leave UX-R6 to remove legacy ranking compatibility behind the already stable surface.

## 7. Testing strategy

Authority: `docs/TESTING_STRATEGY.md`.

Use risk-based tests-first.

High-value EPI-MQ contracts:

- durable player-visible observations are consumed exactly once by historical replay;
- hypothetical observations modify player worlds without leaking actual hidden Storyteller target/fact state;
- exact A3 enumeration remains the correctness oracle for epistemic metrics;
- resource failure / timeout cannot be interpreted as logical UNSAT;
- Productive Uncertainty metrics are deterministic for the same semantic state;
- ranking changes do not alter legal-domain authority;
- UX-R2B Manual, UX-R3 ASSISTED execution, UX-R4 combinatorial presentation, and UX-R5 small-domain presentation remain unchanged.

Use focused tests first, `:app:testFast` at logical checkpoints, and full CI at major algorithm/risk checkpoints.

## 8. Active scope guards

Do not expand EPI-MQ into:

- UI redesign of the stable UX-R4/UX-R5 surfaces;
- new legality ownership in recommendation code;
- broad Host/App decomposition;
- A4/ZDD production rollout before separate equivalence/resource gates;
- deletion of legacy preferences/enums merely for cleanup;
- unrelated persistence/recovery work;
- future-script expansion not required by the selected epistemic contract.

Do not reopen UX-R2B/UX-R4/UX-R5 presentation ownership unless a real regression is found.

## 9. Documentation authority / resume protocol

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
4. read the two EPI-MQ / algorithm authorities above;
5. re-query live `main` rather than trusting stored SHA;
6. create a fresh EPI-MQ branch from live `main`;
7. preserve the stable legal-domain / Manual / presentation contracts;
8. establish behavior-first epistemic metric REDs before ranking changes;
9. keep A3 exact as correctness baseline and A4/ZDD shadow until separately validated;
10. after EPI-MQ, proceed to UX-R6 legacy-ranking replacement behind the stable surface.

## 10. Deferred / queued registry

| Area | Status |
|---|---|
| EPI-MQ Productive Uncertainty | NEXT PRIMARY CAMPAIGN |
| ALG cognitive-consistency / PlayerWorldSet | NEXT PRIMARY CAMPAIGN |
| UX-R6 legacy ranking replacement | QUEUED AFTER EPI-MQ |
| Legacy recommendation enhancement | MAINTENANCE-ONLY / NO NEW BROAD INVESTMENT |
| A4/ZDD production rollout | SHADOW / FUTURE AFTER EXACT BASELINE GATES |
| REC-R1 | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid wake-history authority | DEFERRED FOLLOW-UP |
