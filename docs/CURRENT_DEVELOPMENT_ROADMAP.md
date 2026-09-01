# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-01 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation or merge.

## 1. Current development context

```text
live main after UX-R3 merge:
f5a0e2cf8776866441bcd32729fcdc43d4f70f9b

last merged PR:
#65 — UX-R3: remove global storyteller mode selector
MERGED 2026-09-01

final fully validated UX-R3 executable/code checkpoint:
6cb9cb542b9e25d718a2a035e37475f99388ed2e

validated docs-closeout checkpoint before merge:
e84997f48e70e565eddad9b6f14d06b3db1a6efa

UX-R3 status:
COMPLETE / VERIFIED / MERGED

next implementation slice:
UX-R4 — unified recommendation presentation
```

PR #65 merged as `f5a0e2cf8776866441bcd32729fcdc43d4f70f9b`. The final Ready-transition cleanup head had an identical file tree to the validated docs-closeout checkpoint before merge.

The executable checkpoint `6cb9cb5...` passed the accepted UX-R3 validation route:

- behavior-first RED confirmed by real PR CI before the policy existed;
- fail-closed exact-anchor production cutover on the locked App-root blob;
- focused UX-R3 policy tests;
- UX-R2B Manual-authority regression tests;
- `:app:testFast` inside the production-cutover checkpoint;
- R2 main-thread boundary;
- final normal PR Android FAST;
- Real Clingo cross-validation;
- final CI gate.

The roadmap update after that checkpoint is documentation-only. Treat `6cb9cb5...` as the final validated executable checkpoint unless a later executable diff is introduced and separately validated.

## 2. Campaign status

The generic multi-script setup campaign and the legal-domain/manual-authority foundation are complete. The active campaign is the clue recommendation/manual-selection product boundary.

```text
MS-SETUP generic multi-script architecture                    COMPLETE / MERGED

UX-R1   audit Automatic/Manual/RecommendationStyle
        dependencies and legal-domain authority              COMPLETE

UX-R2A  shared pair-information legal-domain authority        COMPLETE / MERGED

UX-R2B  pair Manual flow -> shared legal-domain authority
        + typed registration-preserving commit path           COMPLETE / VERIFIED / MERGED (#64)

UX-R3   remove normal global storyteller mode selector        COMPLETE / VERIFIED / MERGED (#65)

UX-R4   unified recommendation presentation
        Top-1 + 0–2 differentiated alternatives + Manual      NEXT

UX-R5   small-domain specialization                           QUEUED

EPI-MQ / ALG Productive Uncertainty mainline                  NEXT PRIMARY ALGORITHM CAMPAIGN

UX-R6   replace legacy ranking behind stable UX contract      QUEUED AFTER EPI-MQ
```

Create a fresh UX-R4 branch from current live `main`. Do not reuse the merged UX-R3 branch.

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
- Spy/Recluse registration belongs to semantic truth construction, not recommendation heuristics;
- semantic legality/truth must not be owned by Host/UI compatibility projection;
- every supported information role must remain playable through a correct manual/generated clue path even when no recommendation provider supports that situation;
- recommendation ranking must remain downstream of the legal semantic candidate domain;
- Manual must remain a user authority path, not a recommendation style.

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

Unsupported recommendation situations degrade to correct Manual selection rather than losing functionality.

### UX-R2B accepted pair-information contract

For first-night Washerwoman/Librarian/Investigator:

1. `PairInformationLegalDomain` is the sole selectable semantic authority.
2. Manual availability is independent of recommendation coverage or RecommendationStyle.
3. Manual and recommendation paths share the same complete legal semantic domain.
4. Legacy/recommended option sets may provide presentation templates or compatibility telemetry only; they do not define legality.
5. Structured selection commits by resolving through the legal domain; localized labels are not parsed to recover legality or registration.
6. Exact Spy/Recluse registration facts are preserved in `AbilityObservation`.
7. Pair-family authoritative publication may intentionally differ from the historical curated shortlist.
8. Non-pair first-night families retain their existing migration/parity gate.
9. Investigator zero-minion remains illegal; Librarian zero-outsider remains legal.

Permanent acceptance condition:

```text
recommendation unavailable != manual unavailable
```

UX-R3/4/5 must preserve this authority split.

## 5. UX-R3 accepted product-flow contract

The old normal Settings selector is removed.

The actual legacy persisted enum values are:

```text
MANUAL
AUTO_BALANCED
AUTO_AGGRESSIVE
AUTO_GENTLE
```

Earlier roadmap prose sometimes called the fourth style “Conservative”; the implementation compatibility enum uses `GENTLE`. UX-R3 does not rename or delete the legacy enum because it remains migration/internal compatibility state.

For the normal product UX, **every legacy stored value is normalized** to:

```text
automaticExecution = false
recommendationStyle = BALANCED   # temporary compatibility ranking input
```

This is intentional:

- “recommendation always on when supported” means recommendation content is available without global preselection;
- it does **not** mean automatically applying Storyteller rulings;
- the normal interaction remains Storyteller-confirmed / ASSISTED;
- old AUTO preferences cannot silently restore automatic execution;
- old Aggressive/Gentle preferences cannot survive as hidden front-door policy;
- per-interaction Manual remains available and uses the legal-domain authority established by UX-R2B;
- internal `RecommendationStyle` compatibility dimensions may remain temporarily until later ranking replacement.

UX-R3 deliberately does **not** delete `StorytellerAutomationMode` or preference plumbing. That cleanup can happen only after downstream compatibility no longer depends on it.

## 6. UX-R3 validation checkpoint

### RED

Real PR CI confirmed the behavior-first RED at head:

`71638e977cb69066fbe5de09c7825e9254e89d06`

Evidence:

- CI run `33498257864`;
- Android test compilation failed exactly because `StorytellerRecommendationUxPolicy` did not yet exist;
- Real Clingo cross-validation was green;
- an earlier malformed temporary workflow produced zero jobs and was explicitly **not** counted as RED evidence.

### GREEN production cutover

The large `CampBoardGameHostApp.kt` production wiring was changed through the repository-approved fail-closed one-shot route.

Successful one-shot run:

`33498816111`

It verified:

- exact branch head;
- locked App-root target blob;
- each exact multiline anchor occurred once;
- only the intended App-root file changed during the patch step;
- focused UX-R3 policy tests passed;
- UX-R2B Manual-authority regressions passed;
- `:app:testFast` passed;
- production commit was created only after those gates;
- temporary Python patch script and workflow self-removed.

A first workflow attempt failed before job creation because an unquoted YAML `if:` expression contained a commit message with `chore:`. No production patch ran in that failed attempt. The YAML was corrected and the successful run above is the authoritative cutover evidence.

### Final normal PR validation

Final executable head:

`6cb9cb542b9e25d718a2a035e37475f99388ed2e`

Validation:

- R2 main-thread boundary run `33499085403`: SUCCESS;
- CI run `33499085434`: SUCCESS;
- Android FAST: SUCCESS;
- Real Clingo cross-validation: SUCCESS;
- CI gate: SUCCESS;
- full Android / ASP: skipped by the repository change classifier for this slice.

Final permanent PR diff at that checkpoint is exactly four files:

```text
app/src/main/java/com/codex/campboardgamehost/AppSettingsScreen.kt
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/clocktower/domain/StorytellerRecommendationUxPolicy.kt
app/src/test/java/com/codex/campboardgamehost/clocktower/domain/StorytellerRecommendationUxPolicyTest.kt
```

No temporary one-shot workflow/script remains. No review threads or submitted review blockers existed at last verification.

## 7. Approved clue-selection UX direction

Authority:

`docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`

After UX-R3, the intended per-interaction product model is:

```text
prominent primary contextual recommendation
        +
0–2 visually separated meaningful alternatives
        +
persistent Manual control
```

Key rules:

1. Recommendations are computed whenever supported; the user does not first select a global style.
2. The strongest current recommendation is visually dominant.
3. Show at most two meaningful alternatives in the normal interaction surface.
4. Alternatives should represent distinct useful strategies/world explanations when possible, not merely adjacent scores.
5. Manual remains available on every relevant interaction even when recommendations are active.
6. Manual operates on the complete legal semantic domain, never the recommendation shortlist.
7. Low-confidence/no-clear-winner states must be representable without pretending a weak option is authoritative.

### Combinatorial domains — UX-R4

For Washerwoman/Librarian/Investigator and similar role+player/pair domains:

```text
Top-1 recommendation
Alternative 1
Alternative 2
--------------------
Manually choose clue
```

UX-R2B already provides the permanent legal-domain Manual seam. UX-R4 should refine presentation around that seam, not reopen legality ownership.

### Small domains — UX-R5

- Number domains: show the primary recommendation plus all remaining legal values directly when the domain fits naturally.
- Yes/No domains: show the recommended value prominently and the other legal value as the secondary choice.

## 8. Immediate implementation route

Create a fresh branch from current live `main`, then proceed:

```text
UX-R4  unified recommendation presentation
       -> prominent Top-1
       -> 0–2 differentiated alternatives
       -> persistent Manual action for combinatorial domains

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

UX-R6  replace legacy ranking behind the stable Top-1/alternatives/Manual UI contract
```

UX-R4 and UX-R5 must remain deliberately thin. They establish a stable product surface before the Productive Uncertainty ranking engine replaces legacy ranking.

Do not build another temporary recommendation engine during UX-R4/5.

## 9. Cognitive-consistency / Productive Uncertainty route

Primary design authorities:

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

The objective is credible, sustainable, interactive, eventually breakable and fair mistaken worlds rather than random or maximally false information.

A3 exact enumeration remains the correctness baseline. A4/ZDD remains shadow/prototype until equivalence and resource behavior are separately validated. Approximation/resource failure must never become false UNSAT.

## 10. Testing strategy

Authority: `docs/TESTING_STRATEGY.md`.

Use risk-based tests-first rather than mechanical test-first for every edit.

Accepted UX-R3 behavior contracts now include:

- every legacy global mode normalizes to the same normal product policy;
- legacy AUTO preferences cannot restore automatic execution;
- the compatibility ranking style is BALANCED at this migration boundary;
- removing the Settings selector does not move legality into recommendation state;
- per-interaction Manual authority from UX-R2B remains available in the normal ASSISTED flow.

High-value UX-R4/R5 contracts include:

- recommendation and Manual paths continue to share one legal semantic authority;
- combinatorial domains show no more than three normal recommendation choices before Manual navigation;
- selecting an alternative commits exactly that clue and its registration semantics;
- small numeric domains expose every legal value directly;
- Yes/No domains expose both legal values;
- absent/low-confidence recommendation still permits correct Manual play.

Avoid source-shape tests that only assert button/class/helper placement.

Full CI is reserved for logical acceptance checkpoints or risk-triggered cases according to `TESTING_STRATEGY.md`.

## 11. PR #65 merge checkpoint

PR #65 merge closeout is complete.

Evidence:

1. final fully validated executable checkpoint: `6cb9cb542b9e25d718a2a035e37475f99388ed2e`;
2. docs-closeout checkpoint: `e84997f48e70e565eddad9b6f14d06b3db1a6efa`;
3. the docs-closeout checkpoint was exactly one docs-only commit after the executable checkpoint and changed only this roadmap;
4. CI run `33499530332` and R2 run `33499530280` were green on the docs-closeout checkpoint;
5. no review threads or submitted review blockers existed;
6. the direct Ready connector hit the known `fullDatabaseId` GraphQL compatibility error, so a one-shot Ready-transition workflow was used and self-removed;
7. cleanup head `c67a02ec80889a534254fb5eb40e83da3a8fbf3b` had zero file differences from `e84997f...`;
8. PR #65 was merged with expected-head protection;
9. merge commit: `f5a0e2cf8776866441bcd32729fcdc43d4f70f9b`.

UX-R4 must start from a fresh branch based on the resulting live `main`.

## 12. Scope guards for UX-R4

Keep UX-R4 deliberately thin. Do not expand it into:

- small-domain UX-R5 specialization;
- new recommendation ranking behavior;
- Productive Uncertainty;
- PlayerWorldSet production recommendation integration;
- A3/A4/ZDD production rollout;
- deletion of legacy prefs/enums merely for cleanup;
- new role-specific legacy recommendation heuristics;
- broad future-script support;
- Host/App decomposition;
- unrelated persistence/recovery work.

## 13. Current documentation authority

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

Historical evidence:

- PR #64 and its closeout commits establish UX-R2B;
- PR #65 and the checkpoint evidence above establish merged UX-R3;
- older MS-SETUP handoffs remain historical context only.

## 14. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap;
3. re-query live `main` and confirm it includes PR #65 merge commit `f5a0e2cf8776866441bcd32729fcdc43d4f70f9b` (or a later docs-only successor);
4. treat `6cb9cb542b9e25d718a2a035e37475f99388ed2e` as the final fully validated UX-R3 executable checkpoint and `f5a0e2cf8776866441bcd32729fcdc43d4f70f9b` as the UX-R3 merge commit;
5. create a fresh UX-R4 branch from live `main`;
6. implement Top-1 + 0–2 differentiated alternatives + persistent Manual using the existing legal-domain authority;
7. proceed to UX-R5 small-domain specialization;
8. return immediately to PlayerWorldSet / Productive Uncertainty;
9. replace legacy ranking behind the stable UI contract in UX-R6;
10. keep A3 exact as correctness baseline and A4/ZDD shadow until separately validated.

## 15. Deferred / queued registry

| Area | Status |
|---|---|
| MS-SETUP generic multi-script architecture | COMPLETE / MERGED |
| Clue UX-R1 | COMPLETE |
| Clue UX-R2A legal-domain foundation | COMPLETE / MERGED |
| Clue UX-R2B pair Manual authority | COMPLETE / VERIFIED / MERGED (#64) |
| Clue UX-R3 global selector removal | COMPLETE / VERIFIED / MERGED (#65) |
| Clue UX-R4 recommendation presentation | NEXT IMMEDIATE SLICE |
| Clue UX-R5 small-domain specialization | QUEUED |
| Legacy recommendation enhancement | MAINTENANCE-ONLY / NO NEW BROAD INVESTMENT |
| EPI-MQ Productive Uncertainty | NEXT PRIMARY ALGORITHM CAMPAIGN |
| ALG cognitive-consistency / PlayerWorldSet | NEXT PRIMARY ALGORITHM CAMPAIGN |
| A4/ZDD production rollout | SHADOW / FUTURE AFTER EXACT BASELINE GATES |
| REC-R1 | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid wake-history authority | DEFERRED FOLLOW-UP |
