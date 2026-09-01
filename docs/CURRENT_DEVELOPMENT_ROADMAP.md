# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-01 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation or merge.

## 1. Current development context

```text
live main at last verification:
eed51bade5163790316a31e8295e2e841df90357

current branch:
codex/ms-setup-generic-architecture

current Draft PR:
#61 — MS-SETUP: generic multi-script setup architecture
DRAFT / OPEN / UNMERGED

S6D accepted checkpoint:
a861c515a73834a4071c4a54bce953eba5c075a6

final fully validated executable/code checkpoint:
678785db60750325950754ec4c3a867ed1338673

first post-acceptance docs-only decision commit:
6e52d8156b5a56f1bb218e812f830aa73275c649
```

PR #61 must remain Draft/Open/Unmerged until the user explicitly authorizes merge.

The final fully validated code checkpoint `678785db...` passed the logical full acceptance route: Android FULL + debug assemble, ASP contract, Real Clingo cross-validation, CI gate and R2. Later commits are documentation-only unless a new executable diff is explicitly introduced.

## 2. MS-SETUP campaign status

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
MS-S6D   first-night perceived-ability semantic completion       COMPLETE / ACCEPTED
MS-S7    TB controlled production cutover                        COMPLETE / ACCEPTED
MS-S8    NGJ / no-template second-script proof                   COMPLETE / ACCEPTED — MINIMAL ARCHITECTURE PROOF
MS-S9    broad future-script acceptance                          DEFERRED / NOT PR #61 BLOCKER
```

PR #61 is now in **closeout / merge-readiness audit only**. Do not add new setup architecture, recommendation-quality work, Productive Uncertainty, or UI redesign to this PR.

Closeout evidence:

- `docs/MS_S7_S8_PR61_CLOSEOUT_CHECKPOINT_2026-09-01.md`
- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-01_MS_S7_PR_CLOSEOUT.md` remains historical execution context for the closeout sequence.

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

## 4. Recommendation architecture decision

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

## 5. New approved clue-selection UX direction

Authority:

`docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`

The current user-facing global choice:

```text
Automatic — Balanced
Automatic — Aggressive
Automatic — Conservative
Manual
```

is planned for removal from normal product UX after PR #61.

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

The manual action opens a structured selection surface rather than expanding the entire combinatorial domain as buttons on the night-step card.

## 6. RecommendationStyle / old mode policy

`Balanced`, `Aggressive`, and `Conservative` may remain temporarily as internal compatibility/scoring dimensions while migration is underway, but they are not the target permanent front-door UX.

`Manual` is not a recommendation style and should not share the same user-facing mode enum conceptually.

Long term, Productive Uncertainty should determine context-sensitive pressure from the current game/knowledge state. Former style concepts may survive as internal features, diagnostics, test scenarios or optional advanced policy inputs, but the user should not need to select one globally before the system can recommend a clue.

## 7. Immediate post-PR #61 implementation route

After PR #61 is merged, create a fresh branch. Do not continue on the MS-SETUP branch.

Proceed in this order:

```text
UX-R1  audit all current Automatic/Manual/RecommendationStyle UI dependencies

UX-R2  establish permanent legal-domain -> manual-selection UI authority
       manual availability must be independent of recommendation coverage

UX-R3  remove the normal global Balanced/Aggressive/Conservative/Manual selector
       recommendation becomes always-on when supported

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

UX-R1 through UX-R5 must remain deliberately thin. They establish the permanent product/authority boundary; they are not a reason to build another temporary recommendation engine before Productive Uncertainty.

Where it avoids temporary architecture, UX-R1/R2 may share the first post-PR branch with the epistemic seam, but UI behavior and world-model correctness must remain independently testable.

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

For the post-PR UX boundary, high-value behavior contracts include:

- manual selection remains available when recommendations are enabled;
- recommendation and manual paths share one legal semantic authority;
- selecting an alternative commits exactly that clue and its interaction-scoped registration semantics;
- small numeric domains expose every legal value directly;
- combinatorial domains show no more than three normal recommendations before manual navigation;
- removal of the global mode does not change clue legality/truth;
- absent/low-confidence recommendation still permits correct manual play.

Avoid source-shape tests that only assert button/class/helper placement.

Full CI is reserved for logical acceptance checkpoints or risk-triggered cases according to `TESTING_STRATEGY.md`.

## 10. PR #61 merge-readiness rules

Before merge authorization is acted on:

1. re-query live `main`, PR #61 head/state/mergeability and checks;
2. confirm all executable changes after the last full-acceptance checkpoint are absent, or rerun the required acceptance route if executable code changed;
3. distinguish later docs-only head from the validated code checkpoint;
4. perform exact PR diff / unexpected-file audit;
5. inspect unresolved review threads/comments if any;
6. verify no current roadmap/handoff claims conflict with actual accepted scope;
7. keep PR Draft and do not merge until explicit user authorization.

## 11. Scope guards

Do not add to PR #61 during closeout:

- clue-selection UX implementation;
- removal of RecommendationStyle/global mode code;
- Productive Uncertainty;
- PlayerWorldSet production recommendation integration;
- A3/A4/ZDD production rollout;
- new NGJ-specific legacy recommendation heuristics;
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

PR #61 closeout evidence:

```text
docs/MS_S7_S8_PR61_CLOSEOUT_CHECKPOINT_2026-09-01.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-01_MS_S7_PR_CLOSEOUT.md
```

Earlier S6D and older handoffs are historical evidence unless explicitly referenced for a checkpoint.

## 13. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap;
3. re-query live `main`, PR #61, branch head and checks;
4. if PR #61 is still unmerged, perform only closeout/merge-readiness work unless a concrete regression is found;
5. treat `678785db60750325950754ec4c3a867ed1338673` as the last fully validated executable/code checkpoint unless later executable commits are verified;
6. do not implement the new clue UX on PR #61;
7. after user-authorized merge, create a fresh branch;
8. execute UX-R1 -> UX-R5 to establish the stable recommendation/manual boundary;
9. return immediately to PlayerWorldSet / Productive Uncertainty and replace legacy ranking behind that stable UI contract;
10. keep A3 exact as correctness baseline and A4/ZDD shadow until separately validated.

## 14. Deferred / queued registry

| Area | Status |
|---|---|
| MS-SETUP / PR #61 | CLOSEOUT / MERGE-READINESS AUDIT |
| MS-S6D semantic completion | COMPLETE / ACCEPTED |
| MS-S7 TB controlled cutover | COMPLETE / ACCEPTED |
| MS-S8 NGJ/no-template proof | COMPLETE / ACCEPTED — MINIMAL ARCHITECTURE PROOF |
| MS-S9 broad future-script acceptance | DEFERRED / NOT PR #61 BLOCKER |
| Clue UX-R1..R5 | NEXT IMMEDIATE POST-PR BOUNDARY SLICE |
| Legacy recommendation enhancement | MAINTENANCE-ONLY / NO NEW BROAD INVESTMENT |
| EPI-MQ Productive Uncertainty | NEXT PRIMARY ALGORITHM CAMPAIGN |
| ALG cognitive-consistency / PlayerWorldSet | NEXT PRIMARY ALGORITHM CAMPAIGN |
| A4/ZDD production rollout | SHADOW / FUTURE AFTER EXACT BASELINE GATES |
| REC-R1 | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid wake-history authority | DEFERRED FOLLOW-UP |
