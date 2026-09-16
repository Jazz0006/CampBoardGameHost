# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-16 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current state

```text
Persistence Simplification                         COMPLETE / merged
D6.1 Clocktower session authority                 COMPLETE / merged
D6.2 UI Composition                               COMPLETE / merged
D6 decomposition campaign                         COMPLETE
UI-R5 square-table convergence                    COMPLETE / PR #117
UI-NAV-1 global navigation visual unification     COMPLETE / PR #118
ROLE-ROTATION-1 recent role rotation              COMPLETE / PR #119
UX-MODE-1 Beginner / Experienced mode             COMPLETE / PR #120
UI-INFO-1 information filtering & layout          COMPLETE / PR #121
Dead-player square-table marking                  COMPLETE / PR #122
EXPERIENCED-NIGHT-FLOW-1 correctness              COMPLETE / PR #123
GLOBAL-OWNERSHIP-CLEANUP steps 1-6                COMPLETE / PRs #124-#129
EXPERIENCED-UI-1 compact TB host surfaces         COMPLETE / PR #130
EXPERIENCED-UI-2 night information surfaces       COMPLETE / PR #131
EXPERIENCED-UI-3 device-feedback refinements      COMPLETE / PR #132
EXPERIENCED-UI-4 poison + ranked recommendations  COMPLETE / PR #133
DAY-UI-1 centered daytime domain actions          COMPLETE / PR #134
EPI-MQ-0.5 epistemic capability boundary          COMPLETE / PR #135
Exact historical hypothetical bundle seam         COMPLETE / PR #137
First-night experiment contract                   COMPLETE / PR #138
FN-BUNDLE-0 candidate-space audit + pair cleanup  COMPLETE / PR #139

CURRENT:
FN-BUNDLE-1 — evaluator correctness and PUBLIC_GOOD_INFO identity semantics

NEXT:
FN-BUNDLE-2 — complete healthy 7-player first-night harness
FN-BUNDLE-3 — BEGINNER Badness corpus / manual labels / gate derivation
then Drunk -> Spy/Recluse registration -> Poisoner staged expansion

DEFERRED:
General-purpose LLM recommendation / critic
Old unified scalar productive-uncertainty cutover route
Pair-information display latency / old-device ADB diagnosis
UX-R6 recommendation-provider replacement
```

Merged FN-BUNDLE-0 code baseline:

`55da0b8366eae6daa01df9bef668c56b6eecfaec`

This is the squash merge of PR #139:

`FN-BUNDLE-0: audit first-night candidate space and retire setup pair route`

Documentation-only handoff commits may move `main` beyond this SHA. Always query live `main` before editing; use `55da0b8` as the merged FN-BUNDLE-0 code baseline, not as a permanently current branch tip.

The current route decision remains:

`docs/EPI_MQ_FIRST_NIGHT_BUNDLE_ROUTE_2026-09-16.md`

The prior route:

`docs/EPI_MQ_ROUTE_REAUDIT_2026-09-15.md`

is historical design evidence only and is superseded as execution authority.

## 2. FN-BUNDLE-0 completion evidence

FN-BUNDLE-0 is complete and merged.

Delivered:

- candidate-space census for complete healthy Night 1 producer factors;
- exact known-product versus incomplete-complete-count distinction;
- representative 7-player census showing raw `110,000` combinations;
- `BEGINNER_PUBLIC_GOOD_INFO` represented projection upper bound of `100` before materialization/deduplication;
- explicit exclusion of player-controlled Fortune Teller target selection;
- explicit deferred complexity flags for Drunk, Spy/Recluse registration and Poisoner target effects;
- documented lossless quotient-first plan before any bounded sampling;
- retirement of obsolete setup-owned pair-information generation.

Pair-information ownership is now:

```text
NaturalPairInformationCandidateGenerator
        ↓
PairInformationLegalDomain / canonical pair consumers
        ↓
runtime/manual/recommendation/FN-BUNDLE consumers
```

`SetupCandidateGenerator` no longer owns Washerwoman/Librarian/Investigator pair generation. It remains responsible only for setup-domain choices that actually belong there, such as Red Herring and demon bluffs.

The surviving first-night precompute transport may cache compatibility data, but its pair candidates are sourced from the canonical pair generator rather than a second setup-owned pair rules path.

## 3. Current open PR — #140

PR #140:

`FN-BUNDLE-1: materialize first-night information for exact evaluation`

is open. It was rebuilt cleanly on the merged FN-BUNDLE-0 code baseline and contains only FN-BUNDLE-1 code/test changes. Documentation-only commits may subsequently make the branch appear behind live `main`; sync those without reintroducing already-merged FN-BUNDLE-0 diff.

Rebuilt branch/head snapshot:

```text
branch: fn-bundle-1-evaluator-correctness
head:   edcdbccdf6c8e2110afcae5d04219d347a2f2da7
base code baseline: 55da0b8366eae6daa01df9bef668c56b6eecfaec
```

The #140 code diff at that snapshot contains only:

- `FirstNightInformationPropositionMaterializer.kt`;
- `FirstNightInformationPropositionMaterializerTest.kt`;
- `FirstNightBundleExperimentExactFixtureTest.kt`.

Already implemented in #140:

- thin `EffectDraft.PlayerInformation -> InformationProposition` materialization;
- Washerwoman/Librarian/Investigator pair proposition materialization without taking ownership of legality;
- Librarian zero-Outsider materialization;
- Chef and Empath numeric proposition materialization using existing rule structure;
- exact fixtures for Spy/Recluse registration-aware pair semantics;
- representative Drunk false Chef information under `MECHANICALLY_CREDIBLE` versus `FUNCTIONING_ONLY`;
- compact healthy public-bundle exact conjunction fixture.

## 4. FN-BUNDLE-1 semantic gap still open

FN-BUNDLE-1 is **not complete**.

The first experiment assumes healthy-good players publicly reveal both their role identity and first-night information. The current projection only republishes clue observations. It therefore under-models confirmation chains such as:

```text
Washerwoman publicly claims Washerwoman
+ Washerwoman information supports Empath identity
+ Empath publicly claims Empath
+ Empath publicly shares 0
```

Current missing work:

1. complete the `InformationProposition` fanout audit before adding a new proposition type;
2. add `InformationProposition.ShownRoleAt(seat, role)`;
3. exact evaluator semantics must compare against `EnumeratedWorld.shownRolesBySeat`;
4. `ShownRoleAt` must not use Spy/Recluse registration semantics;
5. `ShownRoleAt` must not be weakened merely because an ability is malfunctioning;
6. PUBLIC_GOOD_INFO projection must generate one public shown-role claim per sharing source, deduplicated deterministically;
7. a Drunk who believes/shares a shown role such as Chef must contribute `ShownRoleAt(seat, Chef)`, never `RoleAt(seat, Drunk)` and never leak the actual Drunk identity;
8. add healthy confirmation-chain fixtures;
9. add explicit Drunk shown-role fixtures;
10. run the full required CI after #140 contains these semantics.

As of this handoff, `ShownRoleAt` does not exist in #140 and PUBLIC_GOOD_INFO still only republishes clue observations.

As of the clean rebuild snapshot, the retargeted #140 head had **no workflow run**. Do not claim #140 CI is green until a run for the actual current head completes successfully.

## 5. Main product decision

The Storyteller recommendation algorithm does not target one globally optimal clue or one opaque maximum score.

Target behavior:

> **Generate rules-legal complete first-night information bundles, reject bundles that are clearly poor for the intended skill profile, then select randomly from the remaining acceptable bundle pool.**

Initial profile:

```text
BEGINNER / PUBLIC_GOOD_INFO
```

Initial validated selection semantics:

```text
Hard legality
    ↓
Badness filtering
    ↓
uniform random among acceptable survivors
```

Do not add LLM ranking or a unified scalar score before the deterministic baseline is validated.

## 6. Evaluation unit — complete first-night bundle

The semantic unit is the complete first-night information ecology, not Investigator in isolation.

A bundle conceptually contains:

```text
rule-determined Night 1 observations
+ Storyteller-controlled legal choices
+ relevant impairment / registration semantics
```

Rule-determined examples include healthy Chef/Empath information.

Storyteller-controlled examples include legal Washerwoman/Librarian/Investigator pair information, Red Herring, demon bluffs and later staged Drunk/registration choices.

Fortune Teller nightly target choice is player-controlled and must not be optimized by the Storyteller planner.

## 7. Required diagnostics before Badness thresholds

Before defining rejection gates, expose interpretable evidence including at least:

```text
BEFORE / AFTER exact world counts
demon-seat diversity / demonCoverSize
distinct evil-team seat configurations
evil topology retention
evilCoverSize
forcedGoodSeats / count
forcedEvilSeats / count where meaningful
leave-one-out / pair interaction evidence
minimum information-value evidence
structurally distinct counterworld evidence where tractable
```

Raw world count alone is insufficient. Do not label unweighted possible-world fractions as posterior probabilities without an explicit prior/weighting model.

Working Badness categories remain hypotheses, not calibrated gates:

- evil topology collapse;
- large forced-good/trusted block;
- tiny evil/demon cover;
- confirmation-chain interaction collapse;
- too little information value;
- weak counterworld viability.

The acceptable region remains:

```text
too weak  -> reject
acceptable
 too strong -> reject
```

## 8. Experiment sequence

### FN-BUNDLE-0 — COMPLETE / merged #139

Candidate ownership/census/experiment seam audit and pair-route cleanup complete.

### FN-BUNDLE-1 — CURRENT

Finish evaluator correctness before building the 7-player whole-bundle harness.

Required completion condition:

```text
materializer semantics proven
+ ShownRoleAt fanout complete
+ PUBLIC_GOOD_INFO shown-role claims deduplicated
+ healthy confirmation-chain fixture
+ Drunk shown-role fixture
+ current-head CI green
```

No production selector or Badness threshold in this phase.

### FN-BUNDLE-2 — NEXT

Build the complete healthy 7-player Night 1 harness:

1. compose all fixed healthy first-night observations;
2. compose complete legal Storyteller-controlled choices;
3. canonicalize PUBLIC_GOOD_INFO projected signatures;
4. group complete bundles by identical projected signature while retaining bundle provenance/multiplicity;
5. exact-evaluate each distinct signature once;
6. add deterministic bounded sampling only if the quotient remains too large and measured cost justifies it;
7. emit diagnostics without final rejection thresholds.

### FN-BUNDLE-3 — BEGINNER corpus / gates

Build a human-reviewed corpus:

```text
BAD_TOO_STRONG
ACCEPTABLE
BAD_TOO_WEAK
UNCERTAIN
```

Derive gates only from evidence.

### FN-BUNDLE-4+

Shadow random survivor selection, then staged uncertainty expansion:

1. Drunk shown-role / false-information bundle effects;
2. Spy/Recluse registration ambiguity;
3. Poisoner target/dynamic impairment;
4. later skill-profile expansion;
5. optional soft preference / LLM research only after deterministic validation.

## 9. Architecture ownership

Preserve:

```text
rules
  -> legal information / registration semantics

session
  -> canonical actual state / timeline / commit authority

epistemic
  -> recipient-visible hypotheses / exact consequence diagnostics

recommendation
  -> compose legal first-night bundles
  -> consume epistemic diagnostics
  -> apply Badness policy
  -> random selection among acceptable bundles

UI
  -> presentation / confirmation / future experience settings
```

Do not build a second rules engine inside Badness or projection code.

## 10. Exact foundation to preserve

The merged EPI-MQ foundation remains authoritative:

- `EpistemicEvaluationCapabilityBoundary`;
- `READY / DEFERRED` and `DEFERRED != UNSAT`;
- exact recipient-visible possible worlds;
- historical replay;
- mutation-free hypothetical evaluation;
- apply-once semantics;
- hidden-information boundary;
- `ExactHistoricalHypotheticalObservationBundleEvaluator` for exact bundle conjunctions.

## 11. Testing policy

Follow:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`.

For `ShownRoleAt`, first map every producer/consumer/exhaustive `when`/codec/symbolic representation before editing. Then fix the common semantic owner and re-search fanout.

Because `ShownRoleAt` changes epistemic proposition semantics, run the escalation required by `TESTING_STRATEGY.md`, including any relevant ASP/Clingo/exact cross-validation triggered by the changed files/contracts.

Do not treat cached/up-to-date results as execution proof.

## 12. Stable UI and paused work

Do not mix FN-BUNDLE semantic work with UI redesign.

Pair-information display latency / old-device abnormal exit remains paused.

PR #109 remains unrelated unless live status changes; query before touching.

## 13. Next-conversation reading order

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
5. `docs/EPI_MQ_FIRST_NIGHT_BUNDLE_ROUTE_2026-09-16.md`;
6. query live `main`, PR #140 and current checks;
7. continue FN-BUNDLE-1 from the `ShownRoleAt` fanout audit.

Historical dated handoffs are not execution authority.

## 14. Stable rule

> **The current algorithm route evaluates the complete first-night information ecology, not isolated clues. FN-BUNDLE-0 is merged. FN-BUNDLE-1 must now make PUBLIC_GOOD_INFO semantically complete by representing public shown-role claims as well as clue contents, prove healthy and Drunk identity semantics, and obtain current-head CI before moving to the complete 7-player harness.**
