# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-01 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation or merge.

## 1. Current development context

```text
live main at last verification:
6111ffe3863713895d2b21ab086cf31abcca4a4e

current branch:
codex/ux-r2b-pair-manual-authority

current Draft PR:
#64 — UX-R2B: cut pair manual flow to legal-domain authority
DRAFT / OPEN / UNMERGED / MERGEABLE

final fully validated executable/code checkpoint before docs closeout:
ad2ec9b4de117ac74c02deb6a5a77e65c2a0e4b4

UX-R2B status:
COMPLETE / VERIFIED / MERGE-READY AFTER DOCS-ONLY CLOSEOUT
```

The executable checkpoint `ad2ec9b4...` passed the accepted validation route:

- focused UX-R2B contract tests;
- full `:app:testFast` in the exact one-shot production-cutover checkpoint;
- R2 main-thread boundary;
- ASP contract tests;
- Real Clingo cross-validation;
- Android full unit tests + debug APK;
- final CI gate.

Temporary one-shot patch workflow/script were removed before the final functional diff audit. The remaining PR diff before this roadmap update contained only the expected production/test files.

PR #64 must not be merged until live head/checks are re-queried after this docs-only closeout commit. The user has explicitly authorized merge once that final closeout verification is green.

## 2. Campaign status

The generic multi-script setup campaign is complete and merged. The active campaign is now the clue recommendation/manual-selection product boundary.

```text
MS-SETUP generic multi-script architecture                    COMPLETE / MERGED

UX-R1   audit current Automatic/Manual/RecommendationStyle
        dependencies and legal-domain authority              COMPLETE

UX-R2A  shared pair-information legal-domain authority        COMPLETE / MERGED

UX-R2B  pair Manual flow -> shared legal-domain authority
        + typed registration-preserving commit path           COMPLETE / VERIFIED / PR #64

UX-R3   remove normal global Balanced/Aggressive/
        Conservative/Manual selector                          NEXT

UX-R4   unified recommendation presentation
        Top-1 + 0–2 differentiated alternatives + Manual      QUEUED

UX-R5   small-domain specialization                           QUEUED

EPI-MQ / ALG Productive Uncertainty mainline                  NEXT PRIMARY ALGORITHM CAMPAIGN

UX-R6   replace legacy ranking behind stable UX contract      QUEUED AFTER EPI-MQ
```

Do not begin UX-R3 on PR #64. After #64 is merged, create a fresh branch from live `main`.

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

This is the required acceptance condition proving:

```text
recommendation unavailable != manual unavailable
```

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

UX-R2B has already established the permanent Manual authority and a structured role/player selection seam. UX-R4 may refine presentation around that seam; it must not move legality back into the recommendation shortlist.

## 6. RecommendationStyle / old mode policy

`Balanced`, `Aggressive`, and `Conservative` may remain temporarily as internal compatibility/scoring dimensions while migration is underway, but they are not the target permanent front-door UX.

`Manual` is not a recommendation style and should not share the same user-facing mode enum conceptually.

Long term, Productive Uncertainty should determine context-sensitive pressure from the current game/knowledge state. Former style concepts may survive as internal features, diagnostics, test scenarios or optional advanced policy inputs, but the user should not need to select one globally before the system can recommend a clue.

UX-R3 should remove the normal global selector without changing legal-domain truth semantics or deleting internal compatibility dimensions prematurely.

## 7. Immediate implementation route after PR #64

After PR #64 is merged, create a fresh branch from the new live `main`.

Proceed in this order:

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

## 10. PR #64 merge-readiness rules

Before the authorized merge is executed:

1. re-query live `main` and PR #64 head/state/mergeability;
2. verify this roadmap commit is docs-only relative to validated executable checkpoint `ad2ec9b4...`;
3. confirm required checks on the new docs-only head are green/skipped as expected;
4. confirm exact PR diff contains no temporary one-shot workflow/script or unrelated file;
5. inspect unresolved review threads/comments if any;
6. mark the Draft PR ready for review if GitHub requires that before merge;
7. merge only if the expected PR head SHA still matches the audited head.

The user explicitly authorized this merge on 2026-09-01 after the above closeout information is updated and verified.

## 11. Scope guards

Do not add to PR #64 during closeout:

- UX-R3 selector removal;
- new recommendation ranking behavior;
- Productive Uncertainty;
- PlayerWorldSet production recommendation integration;
- A3/A4/ZDD production rollout;
- new role-specific legacy recommendation heuristics;
- broad future-script support;
- Host/App decomposition;
- unrelated persistence/recovery work.

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

## 13. New-conversation resume protocol

Before PR #64 merge:

1. read root `AGENTS.md`;
2. read this roadmap;
3. re-query live `main`, PR #64 head/state/mergeability/checks;
4. treat `ad2ec9b4de117ac74c02deb6a5a77e65c2a0e4b4` as the final fully validated executable/code checkpoint before the docs-only closeout commit;
5. perform merge-readiness only; do not begin UX-R3 on this branch.

After PR #64 merge:

1. re-query the resulting live `main` merge SHA;
2. create a fresh branch;
3. begin UX-R3 with an audit of every remaining global Automatic/Manual/RecommendationStyle UI dependency;
4. remove only the normal front-door selector while preserving internal compatibility dimensions where still required;
5. proceed UX-R4 -> UX-R5;
6. return immediately to PlayerWorldSet / Productive Uncertainty and replace legacy ranking behind the stable UI contract;
7. keep A3 exact as correctness baseline and A4/ZDD shadow until separately validated.

## 14. Deferred / queued registry

| Area | Status |
|---|---|
| MS-SETUP generic multi-script architecture | COMPLETE / MERGED |
| Clue UX-R1 | COMPLETE |
| Clue UX-R2A legal-domain foundation | COMPLETE / MERGED |
| Clue UX-R2B pair Manual authority | COMPLETE / VERIFIED / PR #64 MERGE CLOSEOUT |
| Clue UX-R3 global selector removal | NEXT IMMEDIATE SLICE |
| Clue UX-R4 recommendation presentation | QUEUED |
| Clue UX-R5 small-domain specialization | QUEUED |
| Legacy recommendation enhancement | MAINTENANCE-ONLY / NO NEW BROAD INVESTMENT |
| EPI-MQ Productive Uncertainty | NEXT PRIMARY ALGORITHM CAMPAIGN |
| ALG cognitive-consistency / PlayerWorldSet | NEXT PRIMARY ALGORITHM CAMPAIGN |
| A4/ZDD production rollout | SHADOW / FUTURE AFTER EXACT BASELINE GATES |
| REC-R1 | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid wake-history authority | DEFERRED FOLLOW-UP |
