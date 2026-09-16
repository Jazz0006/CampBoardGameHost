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
FN-BUNDLE-1 evaluator/shown-role semantics        COMPLETE / merged PR #140

CURRENT:
FN-BUNDLE-2 — complete healthy 7-player first-night harness / PR #142

NEXT:
FN-BUNDLE-3 — BEGINNER Badness corpus / manual labels / gate derivation
External calibration against real Storyteller data where usable
then Drunk -> Spy/Recluse registration -> Poisoner staged expansion

DEFERRED:
General-purpose LLM recommendation / critic
Old unified scalar productive-uncertainty cutover route
Pair-information display latency / old-device ADB diagnosis
UX-R6 recommendation-provider replacement
```

Live FN-BUNDLE-1 squash-merge baseline:

`efedf87a7d2434ea7ddb75bfdc8516a27d125666`

Always query live `main` before editing; documentation-only movement may advance it.

The current route decision remains:

`docs/EPI_MQ_FIRST_NIGHT_BUNDLE_ROUTE_2026-09-16.md`

The prior route `docs/EPI_MQ_ROUTE_REAUDIT_2026-09-15.md` is historical design evidence only.

## 2. Stable completed foundation

### FN-BUNDLE-0 — merged #139

Delivered:

- canonical complete healthy Night-1 candidate-space census;
- representative 7-player raw product of `110,000` complete combinations;
- represented `PUBLIC_GOOD_INFO` factor-product upper bound of `100`;
- explicit exclusion of player-controlled Fortune Teller target selection;
- explicit deferred complexity for Drunk, Spy/Recluse registration and Poisoner target effects;
- retirement of the duplicated setup-owned pair-information rules path.

Pair-information semantic ownership remains:

```text
NaturalPairInformationCandidateGenerator
        ↓
PairInformationLegalDomain / canonical consumers
```

Do not repeat the FN-BUNDLE-0 candidate-space or setup pair ownership audit.

### FN-BUNDLE-1 — merged #140

Delivered:

- thin first-night information -> epistemic proposition materialization;
- `InformationProposition.ShownRoleAt(seat, role)` with strict shown-role semantics;
- deterministic deduplicated `PUBLIC_GOOD_INFO` shown-role claims;
- no hidden Drunk identity leakage through public shown-role claims;
- healthy confirmation-chain exact fixture;
- Drunk shown-role exact fixture;
- complete proposition fanout / JSON / exact ownership audit;
- full T4 acceptance before merge.

`ShownRoleAt` remains distinct from `RoleAt`: no Spy/Recluse registration and no malfunction weakening.

## 3. FN-BUNDLE-2 — current PR #142

PR:

`FN-BUNDLE-2: evaluate complete healthy first-night bundles`

Branch:

`fn-bundle-2-healthy-harness`

The phase remains **experimental harness only**. It does not define BEGINNER Badness thresholds and does not replace the production recommendation selector.

### Implemented harness behavior

For the representative healthy 7-player Trouble Brewing first night:

```text
canonical legal factor producers
    ↓
complete-bundle factorized provenance
    ↓
PUBLIC_GOOD_INFO materialization
    ↓
canonical projected signature
    ↓
lossless quotient by identical signature
    ↓
exact epistemic diagnostics once per distinct signature
    ↓
leave-one-out diagnostics
```

Current representative contract:

```text
raw complete bundle count:              110,000
represented public factor combinations:     100
distinct projected signatures:              100
latent multiplicity per public combination: 1,100
sampling:                                    none
```

The harness preserves complete bundle identity/provenance without materializing all `110,000` bundle objects.

### Exact diagnostic owner

Structural consequence diagnostics belong to `epistemic`, not recommendation policy.

Current descriptive outputs include:

- exact BEFORE / AFTER world counts;
- possible demon seats / `demonCoverSize`;
- distinct evil-team seat configurations;
- forced-good seats;
- forced-evil seats;
- evil cover / `evilCoverSize`;
- leave-one-out information interaction evidence.

No scalar quality score or rejection threshold is embedded in the evaluator.

### 7-player scalability fix

The first implementation exposed a real memory problem: materializing the unconstrained seven-player exact baseline, and then the existing prefix-trie ZDD construction, could exhaust CI memory.

The accepted experiment path does not hide this by shrinking the fixture. For pristine Night 1 it instead uses constant-memory source enumeration:

1. scan BEFORE lazily;
2. group queries by strict public `ShownRoleAt` claims;
3. re-enumerate lazily for each small identity group;
4. apply cheap shown-role filtering before retaining worlds;
5. materialize only the much smaller retained family for clue conjunction / leave-one-out analysis.

Historical multi-night replay remains on the existing historical exact path. This experiment does not change the production A4/ZDD rollout decision.

### Healthy counterworld domain

FN-BUNDLE-2 deliberately validates the first staged domain only. Candidate legality still uses the complete official Trouble Brewing rules/catalog, but the diagnostic counterworld domain excludes the uncertainty sources reserved for later stages:

```text
Drunk
Spy
Recluse
Poisoner
```

This prevents the supposedly healthy experiment from silently mixing in Drunk shown-role uncertainty, Spy/Recluse registration ambiguity, or Poisoner impairment before those stages are intentionally validated.

### Current validation

A retained acceptance fixture now proves the representative healthy 7-player harness actually executes and verifies:

- `110,000` raw complete bundles;
- `100` represented/distinct public signatures;
- lossless multiplicity totaling `110,000`;
- explicit staged counterworld-role exclusion;
- no bounded sampling;
- non-empty exact survivors for every evaluated representative signature.

Current-head FAST acceptance is green. FN-BUNDLE-2 still requires an explicit `[full-ci]` T4 checkpoint before merge authorization.

## 4. Experimental testing rule

Do not manufacture RED tests merely to satisfy process for exploratory experiment/spike work.

Use this distinction:

```text
exploratory spike / measurement / feasibility work
    -> implementation-first is allowed
    -> use compile, experiment output and existing regression evidence while exploring

behavior or architecture retained for merge
    -> add only the necessary stable contract / regression tests before acceptance
    -> run the affected validation and T4 checkpoint required by TESTING_STRATEGY.md
```

A failing test is evidence when it demonstrates a real missing stable behavior. It is not a goal by itself.

## 5. Product decision

The Storyteller recommendation algorithm does not target one globally optimal clue or one opaque maximum score.

Target behavior remains:

> **Generate rules-legal complete first-night information bundles, reject bundles that are clearly poor for the intended skill profile, then select randomly from the remaining acceptable bundle pool.**

Initial profile:

```text
BEGINNER / PUBLIC_GOOD_INFO
```

Target selection semantics:

```text
Hard legality
    ↓
Badness filtering
    ↓
uniform random among acceptable survivors
```

Do not add an LLM ranker or unified scalar quality score before the deterministic baseline is validated.

## 6. Diagnostics before Badness thresholds

Before defining rejection gates, use interpretable evidence including:

```text
BEFORE / AFTER exact world counts
demon-seat diversity / demonCoverSize
distinct evil-team seat configurations
evil topology retention
evilCoverSize
forcedGoodSeats / count
forcedEvilSeats / count where meaningful
leave-one-out / interaction evidence
minimum information-value evidence
structurally distinct counterworld evidence where tractable
```

Raw world count alone is insufficient. Do not label unweighted possible-world fractions as posterior probabilities without an explicit prior/weighting model.

Working Badness categories remain hypotheses only:

- evil topology collapse;
- large forced-good / trusted block;
- tiny evil/demon cover;
- confirmation-chain interaction collapse;
- too little information value;
- weak counterworld viability.

No final thresholds are allowed before a human-reviewed corpus.

## 7. FN-BUNDLE-3 — next after #142 merge

Build a human-reviewed corpus with labels such as:

```text
BAD_TOO_STRONG
ACCEPTABLE
BAD_TOO_WEAK
UNCERTAIN
```

Use a calibration set and holdout set. Add adversarial examples where individually plausible clues collapse only in combination.

Where ClockTracker or another real Storyteller source exposes enough first-night decision detail, use it as external calibration/validation evidence rather than treating every historical Storyteller choice as a unique ground-truth label.

Do not start FN-BUNDLE-3 before FN-BUNDLE-2 is accepted and merged.

## 8. Later staged uncertainty

After the healthy corpus/gates are understood, widen the exact diagnostic domain deliberately:

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
  -> compose legal complete first-night bundles
  -> consume epistemic diagnostics
  -> apply future Badness policy
  -> random selection among acceptable survivors

UI
  -> presentation / confirmation / future experience settings
```

Do not build a second rules engine inside bundle composition, Badness, or projection code.

## 10. Exact foundation to preserve

The EPI-MQ foundation remains authoritative:

- `EpistemicEvaluationCapabilityBoundary`;
- `READY / DEFERRED` and `DEFERRED != UNSAT`;
- exact recipient-visible possible worlds;
- historical replay;
- mutation-free hypothetical evaluation;
- apply-once semantics;
- hidden-information boundary;
- `ExactHistoricalHypotheticalObservationBundleEvaluator` for exact bundle conjunctions.

## 11. Acceptance / merge boundary

FN-BUNDLE-2 is complete only when the current PR head has:

```text
[x] complete healthy seven-player harness
[x] quotient-first exact evaluation
[x] lossless complete-bundle provenance/multiplicity
[x] interpretable exact structure diagnostics
[x] leave-one-out diagnostics
[x] constant-memory pristine Night-1 baseline path
[x] explicit staged healthy counterworld domain
[x] retained representative acceptance fixture
[x] ordinary FAST CI green
[ ] current-head [full-ci] T4 green
```

Do not merge PR #142 without explicit user authorization.

Do not begin FN-BUNDLE-3 implementation before #142 is merged.

## 12. Next-conversation reading order

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
5. `docs/EPI_MQ_FIRST_NIGHT_BUNDLE_ROUTE_2026-09-16.md`;
6. query live `main`, PR #142 and current checks;
7. continue only from the remaining FN-BUNDLE-2 acceptance state.

Historical dated handoffs are not execution authority.

## 13. Stable rule

> **FN-BUNDLE-0 and FN-BUNDLE-1 are complete and merged. FN-BUNDLE-2 evaluates the complete healthy 7-player first-night ecology by quotienting legal complete bundles before exact reasoning, preserving full provenance while emitting interpretable epistemic diagnostics. Do not add Badness thresholds, production selector cutover, or later uncertainty stages until this harness passes current-head T4 and is explicitly authorized for merge.**
