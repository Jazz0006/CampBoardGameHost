# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-01 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation or merge.

## 1. Current development context

```text
live main at last verification:
2c5e55ac708fc36abb2b58f99714efbfe97547ca

last merged PR:
#64 — UX-R2B: cut pair manual flow to legal-domain authority
MERGED 2026-09-01

final fully validated executable/code checkpoint before docs closeout:
ad2ec9b4de117ac74c02deb6a5a77e65c2a0e4b4

validated docs-closeout tree:
4dbc1235b1938495bfac97f88ceab55df5307968

UX-R2B status:
COMPLETE / VERIFIED / MERGED

next implementation slice:
UX-R3 — remove normal global Balanced/Aggressive/Conservative/Manual selector
```

PR #64 merged as:

`2c5e55ac708fc36abb2b58f99714efbfe97547ca`

The executable checkpoint `ad2ec9b4...` passed the accepted validation route:

- focused UX-R2B contract tests;
- full `:app:testFast` in the exact one-shot production-cutover checkpoint;
- R2 main-thread boundary;
- ASP contract tests;
- Real Clingo cross-validation;
- Android full unit tests + debug APK;
- final CI gate.

The docs-closeout head `4dbc123...` was exactly one docs-only commit after that executable checkpoint and passed CI + R2. A temporary workflow was used only to transition PR #64 from Draft to Ready after the connector's direct Ready action hit a GraphQL compatibility error; that workflow self-removed, and the final cleanup head had an identical file tree to `4dbc123...` before merge.

No UX-R3 production work has started yet. Start it from a fresh branch based on live `main`.

## 2. Campaign status

The generic multi-script setup campaign is complete and merged. The active campaign is the clue recommendation/manual-selection product boundary.

```text
MS-SETUP generic multi-script architecture                    COMPLETE / MERGED

UX-R1   audit current Automatic/Manual/RecommendationStyle
        dependencies and legal-domain authority              COMPLETE

UX-R2A  shared pair-information legal-domain authority        COMPLETE / MERGED

UX-R2B  pair Manual flow -> shared legal-domain authority
        + typed registration-preserving commit path           COMPLETE / VERIFIED / MERGED (#64)

UX-R3   remove normal global Balanced/Aggressive/
        Conservative/Manual selector                          NEXT

UX-R4   unified recommendation presentation
        Top-1 + 0–2 differentiated alternatives + Manual      QUEUED

UX-R5   small-domain specialization                           QUEUED

EPI-MQ / ALG Productive Uncertainty mainline                  NEXT PRIMARY ALGORITHM CAMPAIGN

UX-R6   replace legacy ranking behind stable UX contract      QUEUED AFTER EPI-MQ
```

Create a fresh branch from current live `main` before implementing UX-R3. Do not continue UX-R3 on the merged UX-R2B branch.

## 3. Frozen permanent setup / information architecture

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
- Spy/Recluse registration belongs to semantic truth construction, not role-specific recommendation heuristics;
- semantic legality/truth must not be owned by Host/UI compatibility projection;
- every supported information role must remain playable through a correct manual/generated clue path even if no recommendation provider supports that situation.

## 4. Recommendation / Manual authority

Legacy recommendation is maintenance-only and temporary.

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

Unsupported recommendation situations degrade to correct manual selection rather than losing functionality.

Do not invest further in exact legacy shortlist/ranking/distribution parity unless needed to prevent an illegal clue or major regression.

### UX-R2B accepted pair-information contract

For first-night Washerwoman/Librarian/Investigator:

1. `PairInformationLegalDomain` is the sole selectable semantic authority.
2. Manual availability is independent of recommendation coverage or RecommendationStyle.
3. Manual and automatic selection share the same complete legal semantic domain.
4. Legacy/recommended option sets may provide presentation templates or parity telemetry only; they do not define legality.
5. Selection commit resolves the structured proposition back through the legal domain; localized labels are not parsed to recover legality or registration.
6. Exact Spy/Recluse registration facts from the legal candidate are preserved in `AbilityObservation`.
7. Pair families may publish the authoritative domain even when it intentionally differs from the historical curated legacy shortlist.
8. Non-pair first-night families retain the existing migration/parity gate.
9. Investigator zero-minion remains illegal; Librarian zero-outsider remains legal according to shared display semantics.

UX-R2B therefore establishes the permanent acceptance condition:

```text
recommendation unavailable != manual unavailable
```

UX-R3/4/5 must preserve this authority split.

## 5. Approved clue-selection UX direction

Authority:

`docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`

The current user-facing global choice:

```text
Automatic — Balanced
Automatic — Aggressive
Automatic — Conservative
Manual
```

is the next removal target in UX-R3.

The replacement interaction model is per clue interaction:

```text
prominent primary contextual recommendation
        +
0–2 visually separated meaningful alternatives
        +
persistent manual control
```

Key product rules:

1. Recommendations are computed whenever supported; the user does not first choose a global recommendation style.
2. The strongest current recommendation is visually dominant.
3. Show at most two alternative recommendations in the normal interaction surface.
4. Alternatives should preferably represent distinct useful strategies/world explanations, not merely adjacent score values.
5. Manual control remains available on every relevant interaction even when recommendations are active.
6. Manual selection operates on the complete legal semantic domain, not the legacy recommendation shortlist.
7. Low-confidence/no-clear-winner states must be representable without pretending one weak option is authoritative.

### Small-domain specialization

For small numeric domains, show the primary recommended number plus all remaining legal numbers directly. A separate manual page is unnecessary when the full domain already fits naturally on-screen.

For Yes/No domains, show the recommended result prominently and the other legal result as the secondary choice.

### Combinatorial clue specialization

For Washerwoman/Librarian/Investigator and similar role+player/pair domains:

```text
Top-1 recommendation
Alternative 1
Alternative 2
--------------------
Manually choose clue
```

UX-R2B already established the permanent Manual authority and a structured role/player selection seam. UX-R4 may refine presentation around that seam; it must not move legality back into the recommendation shortlist.

## 6. RecommendationStyle / old mode policy

`Balanced`, `Aggressive`, and `Conservative` may remain temporarily as internal compatibility/scoring dimensions while migration is underway, but they are not the target permanent front-door UX.

`Manual` is not a recommendation style and should not share the same user-facing mode enum conceptually.

Long term, Productive Uncertainty should determine context-sensitive pressure from the current game/knowledge state. Former style concepts may survive as internal features, diagnostics, test scenarios or optional advanced policy inputs, but the user should not need to select one globally before the system can recommend a clue.

UX-R3 should remove the normal global selector without changing legal-domain truth semantics or deleting internal compatibility dimensions prematurely.

## 7. Immediate implementation route

Create a fresh branch from live `main`, then proceed in this order:

```text
UX-R3  remove the normal global Balanced/Aggressive/Conservative/Manual selector
       recommendation becomes always-on when supported
       Manual remains a per-interaction authority path

UX-R4  unified recommendation presentation
       -> prominent Top-1
       -> 0–2 differentiated alternatives
       -> persistent manual action for large/combinatorial domains

UX-R5  small-domain specialization
       -> Number: primary + all remaining legal values
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

UX-R6  replace legacy ranking behind the stable Top-1/alternatives/manual UI contract
```

UX-R3 through UX-R5 must remain deliberately thin. They establish the permanent product/authority boundary; they are not a reason to build another temporary recommendation engine before Productive Uncertainty.

Do not re-open UX-R2 legality ownership during UX-R3/4. Recommendation and Manual must stay downstream of the same legal semantic authority.

## 8. Cognitive-consistency / Productive Uncertainty route

Primary design authorities after the UI boundary is established:

- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`
- `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`

Correctness foundation:

```text
semantic candidate
-> recipient PlayerWorldSet BEFORE
-> hypothetical player-visible observation
-> recipient PlayerWorldSet AFTER
-> epistemic metrics
```

Quality extension:

```text
legal misinformation candidate
-> project into player-visible history
-> surviving perceived worlds
-> credibility / ambiguity / persistence
-> cross-role interaction
-> confirmation-lock risk
-> breakability / discovery paths
-> faction impact / player agency gates
-> Productive Uncertainty ranking
-> Recommendation Provider
```

The objective is to create credible, sustainable, interactive, eventually breakable and fair mistaken worlds rather than random or maximally false information.

A3 exact enumeration remains the correctness baseline. A4/ZDD remains shadow/prototype until equivalence and resource behavior are separately validated. Approximation/resource failure must never become false UNSAT.

## 9. Testing strategy

Authority: `docs/TESTING_STRATEGY.md`.

Use risk-based tests-first rather than mechanical test-first for every edit.

Accepted UX-R2B behavior contracts now include:

- manual legal selection exists when automatic mode is off and recommendation coverage is empty;
- pair Manual projection is exactly the shared legal domain;
- illegal presentation templates cannot expand the legal domain;
- selected structured clues commit exact semantic truth and Spy/Recluse registration facts;
- authoritative pair-domain publication may intentionally bypass historical shortlist parity;
- non-pair families cannot use that bypass;
- re-entering a displayed first-night decision does not create a second committed observation.

High-value upcoming UX-R3/R4/R5 contracts include:

- removing the global mode does not change clue legality/truth;
- recommendations remain available when supported without a global mode preselection;
- Manual remains available per interaction;
- combinatorial domains show no more than three normal recommendation choices before Manual navigation;
- small numeric domains expose every legal value directly;
- Yes/No domains expose both legal values;
- absent/low-confidence recommendation still permits correct manual play.

Avoid source-shape tests that only assert button/class/helper placement.

Full CI is reserved for logical acceptance checkpoints or risk-triggered cases according to `TESTING_STRATEGY.md`.

## 10. UX-R2B merge checkpoint

PR #64 merge closeout is complete.

Evidence:

1. final executable/code checkpoint: `ad2ec9b4de117ac74c02deb6a5a77e65c2a0e4b4`;
2. docs-closeout head: `4dbc1235b1938495bfac97f88ceab55df5307968`;
3. docs-closeout was one commit after executable checkpoint and changed only this roadmap;
4. CI and R2 were green on `4dbc123...`;
5. final cleanup head after the temporary Ready-transition workflow had the same file tree as `4dbc123...`;
6. exact permanent PR diff contained only the 9 expected production/test files plus this roadmap;
7. no review threads or review blockers existed;
8. PR #64 merged with expected-head protection;
9. merge commit: `2c5e55ac708fc36abb2b58f99714efbfe97547ca`.

Historical `action_required` checks on bot-generated cleanup heads had zero jobs and were platform-trigger behavior, not test failures. The identical tree had already passed the required CI/R2 validation.

## 11. Scope guards for UX-R3

Keep UX-R3 deliberately narrow. Do not add:

- new recommendation ranking behavior;
- Productive Uncertainty;
- PlayerWorldSet production recommendation integration;
- A3/A4/ZDD production rollout;
- new role-specific legacy recommendation heuristics;
- broad future-script support;
- Host/App decomposition;
- unrelated persistence/recovery work.

UX-R3 is a product-flow/ownership cleanup: remove the normal front-door global mode choice while keeping permanent Manual authority and supported recommendation behavior intact.

## 12. Current documentation authority

Current active set:

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md
docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md
docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

Historical setup closeout evidence remains useful but is no longer the active campaign authority:

```text
docs/MS_S7_S8_PR61_CLOSEOUT_CHECKPOINT_2026-09-01.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-01_MS_S7_PR_CLOSEOUT.md
```

PR #64 itself is historical evidence for the accepted UX-R2B contract.

## 13. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap;
3. re-query live `main` and confirm the current head;
4. treat `ad2ec9b4de117ac74c02deb6a5a77e65c2a0e4b4` as the final fully validated executable/code checkpoint for UX-R2B and `2c5e55ac708fc36abb2b58f99714efbfe97547ca` as its merge commit;
5. create a fresh UX-R3 branch from live `main`;
6. audit every remaining global Automatic/Manual/RecommendationStyle UI dependency;
7. remove only the normal front-door selector while preserving internal compatibility dimensions where still required;
8. preserve per-interaction Manual access backed by complete legal semantic authority;
9. proceed UX-R4 -> UX-R5;
10. return immediately to PlayerWorldSet / Productive Uncertainty and replace legacy ranking behind the stable UI contract;
11. keep A3 exact as correctness baseline and A4/ZDD shadow until separately validated.

## 14. Deferred / queued registry

| Area | Status |
|---|---|
| MS-SETUP generic multi-script architecture | COMPLETE / MERGED |
| Clue UX-R1 | COMPLETE |
| Clue UX-R2A legal-domain foundation | COMPLETE / MERGED |
| Clue UX-R2B pair Manual authority | COMPLETE / VERIFIED / MERGED (#64) |
| Clue UX-R3 global selector removal | NEXT IMMEDIATE SLICE |
| Clue UX-R4 recommendation presentation | QUEUED |
| Clue UX-R5 small-domain specialization | QUEUED |
| Legacy recommendation enhancement | MAINTENANCE-ONLY / NO NEW BROAD INVESTMENT |
| EPI-MQ Productive Uncertainty | NEXT PRIMARY ALGORITHM CAMPAIGN |
| ALG cognitive-consistency / PlayerWorldSet | NEXT PRIMARY ALGORITHM CAMPAIGN |
| A4/ZDD production rollout | SHADOW / FUTURE AFTER EXACT BASELINE GATES |
| REC-R1 | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid wake-history authority | DEFERRED FOLLOW-UP |
