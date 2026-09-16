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

CURRENT:
FN-BUNDLE-0 — live seam audit + experiment contract

NEXT:
FN-BUNDLE-1 — Experiment 0 evaluator correctness fixtures
FN-BUNDLE-2 — Experiment 1 complete healthy 7-player first-night harness
FN-BUNDLE-3 — BEGINNER Badness corpus / manual labels / gate derivation
then Drunk -> Spy/Recluse registration -> Poisoner staged expansion

DEFERRED:
General-purpose LLM recommendation / critic
Old unified scalar productive-uncertainty cutover route
Pair-information display latency / old-device ADB diagnosis
UX-R6 recommendation-provider replacement
```

Current live `main` before this documentation update was `f18c5a6e32c5cb79ed4a8a1473a52bc43df05fe3`.

The current route decision is:

`docs/EPI_MQ_FIRST_NIGHT_BUNDLE_ROUTE_2026-09-16.md`

The prior route:

`docs/EPI_MQ_ROUTE_REAUDIT_2026-09-15.md`

is now **historical design evidence only** and is superseded as execution authority.

The earlier foundation audit remains useful supporting architecture/history:

`docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`

## 2. Core product decision

The Storyteller recommendation algorithm no longer targets one globally optimal clue or one opaque maximum score.

The target behavior is:

> **Generate rules-legal information choices, reject complete first-night information bundles that are clearly poor for the intended skill profile, then select randomly from the remaining acceptable bundle pool.**

Initial production/experiment profile:

```text
BEGINNER only
```

Initial selection semantics after validation:

```text
Hard legality
    ↓
Badness filtering
    ↓
uniform random among acceptable survivors
```

Do not add LLM ranking or soft weighting until the deterministic baseline is validated.

## 3. Main evaluation unit — complete first-night bundle

The main experiment is **not Investigator in isolation**.

A `FirstNightInformationBundle` conceptually contains:

```text
rule-determined Night 1 observations
+ Storyteller-controlled legal choices
+ relevant impairment / registration semantics
```

Rule-determined information includes, where present, healthy Chef and Empath results determined by setup/seating.

Storyteller-controlled choices include, where present and legal:

- Investigator pair / shown Minion information;
- Washerwoman pair / shown Townsfolk information;
- Librarian pair / shown Outsider information or legal zero result;
- Fortune Teller Red Herring;
- Drunk shown role / false information;
- demon bluffs;
- permitted registration choices.

Fortune Teller nightly target choice is player-controlled and must not be optimized by the Storyteller planner.

Role-level tests remain necessary only to prove evaluator semantics and diagnostics. Final Badness decisions must use the complete bundle because cross-confirmation and seating interactions can make individually reasonable clues collectively destructive.

## 4. First behavioral stress profile

The first experiment uses:

> **PUBLIC_GOOD_INFO / BEGINNER:** assume healthy-good players publicly reveal their first-night information on Day 1.

This is a stress/evaluation profile, not a rules claim.

It exists to expose bundles that cause the true evil structure to emerge too quickly under aggressive public sharing.

Do not yet implement multiple table-behavior profiles.

## 5. Initial Badness diagnostics

Before defining thresholds, the experiment must expose interpretable evidence including at least:

```text
BEFORE / AFTER exact world counts
current demon-seat diversity / demonCoverSize
distinct evil-team seat configurations
evil topology retention
evilCoverSize
forcedGoodSeats / count
forcedEvilSeats / count where meaningful
leave-one-out / pair interaction evidence for confirmation chains
minimum information-value evidence
structurally distinct alternative/counterworld evidence where tractable
```

Raw world count alone is explicitly insufficient.

Many worlds that share the same evil topology but differ only in good-role permutations do not represent meaningful strategic uncertainty.

Do not label unweighted world support fractions as posterior probabilities unless an explicit prior/weighting model is introduced later.

## 6. Badness model direction

The current working categories are:

1. **Evil topology collapse** — too few distinct plausible evil-team/demon placements remain.
2. **Large forced-good / trusted block** — too many seats become logically cleared.
3. **Tiny evil/demon cover** — the table splits into a safe zone and a small mechanical execution zone.
4. **Confirmation chain / interaction collapse** — clues jointly resolve far more than they do individually.
5. **Too little information value** — a bundle is legal but nearly vacuous.
6. **Weak counterworld viability** — remaining alternatives are cosmetic or implausible rather than strategically coherent.

The acceptable region is not maximum uncertainty:

```text
too weak  -> reject
acceptable
 too strong -> reject
```

Do not compress these into one arbitrary scalar score before experiments establish useful thresholds/relationships.

## 7. Experiment sequence

### FN-BUNDLE-0 — live seam audit + experiment contract — CURRENT

Audit current production ownership needed to represent and evaluate a complete first-night bundle.

Required output:

- identify all fixed Night 1 observations versus Storyteller-controlled choices;
- identify existing legal candidate generators that must be reused;
- identify exact/capability-aware epistemic evaluation seams;
- define the smallest experiment-only typed bundle/result contract;
- ensure complete-bundle evaluation is recipient/public-profile safe, mutation-free and apply-once;
- plan bounded enumeration/sampling if the bundle space is too large.

Do not implement final Badness thresholds in this phase.

### FN-BUNDLE-1 — Experiment 0 evaluator correctness

Create only the focused fixtures needed to prove semantics for representative:

- Investigator;
- Washerwoman;
- Librarian;
- Empath;
- Chef;
- Drunk false information.

Purpose: prove exact BEFORE/AFTER behavior and bundle composition, not recommendation quality.

### FN-BUNDLE-2 — Experiment 1 complete healthy first night

Use fixed 7-player Trouble Brewing setup/seating fixtures.

For each fixture:

1. include all rule-determined healthy first-night observations;
2. generate all relevant complete legal Storyteller-controlled bundles, or a deterministic bounded sample if exact composition is too large;
3. evaluate the whole public-share bundle;
4. emit diagnostics without final rejection thresholds;
5. inspect extreme and representative cases.

The existing ten 7-player real-Investigator/no-Recluse presets remain useful stress fixtures but must not reduce the experiment to Investigator-only analysis.

### FN-BUNDLE-3 — BEGINNER corpus and Badness Gates

Build an initial human-reviewed corpus with labels:

```text
BAD_TOO_STRONG
ACCEPTABLE
BAD_TOO_WEAK
UNCERTAIN
```

Then determine which diagnostics actually separate bad from acceptable beginner bundles.

Only after this evidence exists define first BEGINNER gates.

### FN-BUNDLE-4 — initial random selector shadow

Apply validated BEGINNER gates in shadow mode:

```text
legal complete bundles
→ reject bad bundles
→ uniform random from survivors
```

Compare selected/surviving bundles against current production choices without cutover first.

### FN-BUNDLE-5 — staged uncertainty expansion

Add, in this order unless evidence changes priority:

1. Drunk shown-role / false-information bundle effects;
2. Spy / Recluse registration ambiguity;
3. Poisoner first-night target / dynamic impairment.

Recalibrate only with evidence; do not silently reuse healthy-only thresholds.

### FN-BUNDLE-6 — skill profiles

After BEGINNER works, add recipient/table-aware difficulty profiles.

Long-term architecture must allow information suitable for one recipient to differ from information suitable for another.

Do not implement Intermediate/Experienced/Expert thresholds now.

### FN-BUNDLE-7 — optional soft preference / LLM research

Only after the deterministic algorithm has a validated baseline:

- test whether bounded soft weights improve variety/quality;
- benchmark general-purpose LLM as an optional critic over already legal and Badness-qualified choices;
- measure incremental value instead of assuming AI advice is superior.

LLM must not own legality, exact semantics, Badness Gates or hidden-state authority.

## 8. Relationship to EPI-MQ foundation

EPI-MQ-0.5 remains complete and valuable.

Preserve/reuse:

- `EpistemicEvaluationCapabilityBoundary`;
- `READY / DEFERRED` and `DEFERRED != UNSAT`;
- exact recipient-visible possible worlds;
- historical replay;
- mutation-free hypothetical evaluation;
- candidate apply-once semantics;
- hidden-information boundary;
- rules/recommendation ownership of legal candidates.

The old route's neutral evaluator extraction may still be required as an enabling slice during FN-BUNDLE-0/1, but it is no longer an independent product milestone that must be completed before the bundle experiment if the existing exact seam can support the experiment safely.

The following old-route targets are no longer the immediate sequence:

```text
EPI-MQ-2 hard gates
EPI-MQ-2.5 truth+false shadow
EPI-MQ-3 unified scalar productive-uncertainty model
EPI-MQ-4 truth/false production cutover
```

Useful concepts from that route may be reused later, but the execution order is replaced by FN-BUNDLE-* above.

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
  -> presentation / confirmation / future player-experience settings
```

Do not build a second rules engine inside Badness policy.

Avoid hidden actual Storyteller facts leaking into recipient/public inference except where they are legitimately used to generate legal truthful observations/candidate legality.

## 10. Combination-space rule

The semantic unit is always the **complete bundle**, even if implementation cannot enumerate every bundle.

Implementation strategy may vary:

```text
small space -> exact enumeration
large space -> constraint pruning / deterministic sampling / bounded expansion
```

Do not fall back to independent per-role scoring merely because the Cartesian product is large.

Any approximation must still evaluate complete composed bundles.

## 11. External calibration

ClockTracker and expert Storyteller records are promising calibration/validation sources.

Use them later to test whether validated gates align with strong human practice and whether thresholds are too strict/permissive.

Do not use real-game records as automatic ground-truth labels and do not imitate every recorded Storyteller choice.

## 12. LLM decision

General-purpose LLM use is explicitly deferred.

The future candidate architecture is at most:

```text
legal bundles
→ deterministic Badness qualification
→ optional LLM soft critic
→ bounded preference/random selection
```

Do not integrate an LLM before a deterministic benchmark exists.

## 13. Stable UI contracts

Recent UI behavior remains stable and is unrelated to this algorithm experiment:

- Beginner and Experienced share one gameplay/rules/legal-candidate/recommendation/session/persistence pipeline;
- highlight = waking / acted-on / selected player;
- `✓` = Storyteller-facing truth / typed registration truth hit;
- yellow corner badge = persistent host-only special state such as Fortune Teller Red Herring;
- poison marker = effective poison visual only, not interaction eligibility;
- Experienced recommendation surfaces show up to three real recommendations followed by legal manual alternatives;
- bottom navigation is utility/navigation; in-table domain actions remain centered.

Do not mix FN-BUNDLE work with presentation redesign.

## 14. Testing policy

Follow:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`.

New experiment/evaluator contracts are semantic algorithm work and should use typed tests at the true owner seam.

Minimum durable proof as the route progresses includes:

```text
same setup/profile/bundle -> deterministic diagnostics
legal bundle composition only
fixed observations cannot be illegally changed for balance
hypothetical bundle evaluation is mutation-free
observations are applied exactly once
recipient/public-profile hypotheses do not receive hidden actual-state shortcuts
DEFERRED remains distinct from UNSAT
bundle metrics differ when seating/fixed Empath/Chef information differs
interaction diagnostics can expose a combined collapse not visible in isolated clue metrics
Drunk/registration stages preserve exact rule semantics when introduced
```

Epistemic/enumeration changes trigger the escalation in `docs/TESTING_STRATEGY.md`, including relevant exact/oracle validation at logical checkpoints.

## 15. Paused / unrelated work

Pair-information display latency / old-device abnormal exit remains paused.

PR #109 (`Reproduce restored execution preflight crash`) remains unrelated unless live status changes; re-query before touching.

Do not fold UI or unrelated crash work into FN-BUNDLE algorithm commits.

## 16. Next-conversation reading order

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
5. `docs/EPI_MQ_FIRST_NIGHT_BUNDLE_ROUTE_2026-09-16.md`;
6. `docs/EPI_MQ_ROUTE_REAUDIT_2026-09-15.md` only as superseded historical reasoning;
7. `docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md` as supporting foundation/history;
8. query live `main`, open PR/check state;
9. begin FN-BUNDLE-0 live seam audit.

Historical dated handoffs are not execution authority.

## 17. Stable rule

> **The current algorithm route evaluates the complete first-night information ecology, not isolated clues. It does not seek one optimal clue: it rejects clearly poor legal bundles using interpretable, evidence-calibrated BEGINNER Badness criteria, then selects randomly from the acceptable pool. Thresholds must come from experiments, skill adaptation comes after the beginner baseline, and general-purpose LLM advice is deferred until the deterministic algorithm can be benchmarked.**
