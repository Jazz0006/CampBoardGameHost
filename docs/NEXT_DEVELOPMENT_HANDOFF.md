# NEXT DEVELOPMENT HANDOFF — First-Night Bundle Badness Experiment

> Updated: 2026-09-16 Australia/Sydney  
> Status: **CURRENT / canonical active handoff**  
> Current route decision: `docs/EPI_MQ_FIRST_NIGHT_BUNDLE_ROUTE_2026-09-16.md`  
> Prior `docs/EPI_MQ_ROUTE_REAUDIT_2026-09-15.md`: **superseded as execution authority**

## 0. Start here

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/EPI_MQ_FIRST_NIGHT_BUNDLE_ROUTE_2026-09-16.md`;
6. `docs/EPI_MQ_ROUTE_REAUDIT_2026-09-15.md` only as historical reasoning;
7. `docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md` as supporting architecture/history;
8. query live `main`, relevant open PRs/checks and current source before editing.

Do not use dated handoffs in `docs/archive/` as execution authority.

## 1. Immediate goal

The immediate target is:

**FN-BUNDLE-0 — live seam audit + experiment contract.**

Do not begin by tuning scores/thresholds, integrating an LLM, or selecting an “optimal” Investigator clue.

The next work must establish how the current code can represent and evaluate a **complete first-night information bundle** for a fixed Trouble Brewing setup and seating arrangement.

## 2. Current product decision

The recommendation engine should behave conceptually as:

```text
all legal first-night bundles
        ↓
exact / capability-aware epistemic diagnostics
        ↓
BEGINNER Badness Gates
        ↓
acceptable bundles
        ↓
uniform random selection
```

The product does **not** currently aim to rank every candidate into a total order or maximize one scalar score.

The first supported skill profile is **BEGINNER** only.

General-purpose LLM recommendation is deferred until this deterministic algorithm can be benchmarked.

## 3. Experiment unit is the whole Night 1 information ecology

Do not treat Investigator as the main independent experiment.

The bundle must include both:

### Rule/state-determined observations

Examples:

- healthy Empath result determined by neighbours;
- healthy Chef count;
- any other first-night information whose truthful result is fixed by the actual setup and legal registration semantics.

These values are constraints on the bundle and **must not be changed for balance**.

### Storyteller-controlled legal choices

Where present/applicable:

- Investigator pair / shown Minion;
- Washerwoman pair / shown Townsfolk;
- Librarian pair / shown Outsider or legal zero result;
- Fortune Teller Red Herring;
- Drunk shown role / false information in the Drunk stage;
- demon bluffs;
- legal registration decisions where Storyteller discretion exists.

Fortune Teller target selection itself is player-controlled and must not be planned as a Storyteller choice.

## 4. Why isolated clue scoring is insufficient

The target failure mode is cross-information collapse.

Example pattern:

```text
Washerwoman supports Empath
        +
Empath receives 0
        +
another clue clears one of the remaining suspect regions
        ↓
large trusted-good block / tiny evil cover
```

Every clue may look reasonable alone while the combined first-night structure makes the true world too easy to reconstruct.

Therefore:

> **Final Badness is a property of the composed bundle, not the sum of independent role scores.**

Role-level fixtures exist only to prove the evaluator is calculating observations correctly.

## 5. First evaluation profile

Use a deliberate stress profile:

```text
BEGINNER + PUBLIC_GOOD_INFO
```

Meaning:

> Assume healthy-good players publicly reveal their first-night information on Day 1.

This is not a rules assumption. It is the first behavioral test profile chosen to expose over-convergent opening information.

Do not implement multiple table-behavior profiles yet.

## 6. Diagnostics required before thresholds

FN-BUNDLE-0/1 should ensure the experiment can eventually expose at least:

- exact BEFORE / AFTER world counts;
- current demon-seat diversity / `demonCoverSize`;
- distinct evil-team seat configurations;
- evil topology retention;
- `evilCoverSize`;
- `forcedGoodSeats`;
- `forcedEvilSeats` where meaningful;
- leave-one-out or pairwise interaction evidence for confirmation chains;
- evidence that information is not nearly vacuous;
- structurally distinct alternative/counterworld evidence where tractable.

Raw world count alone is insufficient.

Do not call simple fractions of unweighted exact worlds Bayesian/posterior probabilities.

## 7. Current Badness hypotheses

Treat these as **hypotheses to test**, not final gates:

1. too few distinct evil/demon placements remain;
2. too many seats become logically forced good;
3. evil/demon cover becomes too small;
4. clues jointly form a confirmation cascade;
5. bundle is too weak to provide useful information;
6. surviving alternatives are cosmetic rather than coherent counterworlds.

The desired region is:

```text
too weak  -> bad
acceptable
 too strong -> bad
```

Do not guess numeric cutoffs before the corpus exists.

## 8. Staged experiment plan

### FN-BUNDLE-0 — CURRENT

Audit current live source and define the experiment contract.

Required questions:

- Which current components generate legal Washerwoman/Librarian/Investigator/etc. choices?
- Which Night 1 observations are deterministic from setup/seating?
- Where are Red Herring, demon bluffs, Drunk, Spy/Recluse registration and poison represented?
- What exact epistemic seam can evaluate several composed observations without mutation or double application?
- Is the old neutral evaluator extraction required first, or can current exact machinery support the experiment safely with a narrower adapter?
- How should `DEFERRED` propagate for unsupported exact semantics?
- How large is the complete bundle Cartesian product for representative 7-player setups?
- Where exact enumeration is too large, what deterministic bounded sampling/pruning preserves complete-bundle semantics?

Output a concrete typed experiment design before broad production cutover.

### FN-BUNDLE-1 — Experiment 0 correctness

Add the smallest durable fixtures needed for representative:

- Investigator;
- Washerwoman;
- Librarian;
- Empath;
- Chef;
- representative Drunk false observation.

Prove calculation/composition only.

### FN-BUNDLE-2 — Experiment 1 complete healthy Night 1

Use fixed 7-player Trouble Brewing setup/seating fixtures.

For each fixture:

1. include all fixed healthy first-night information;
2. generate complete legal Storyteller-controlled bundles;
3. evaluate the whole public-share bundle;
4. emit diagnostics;
5. inspect extreme and representative outputs.

The ten known 7-player real-Investigator/no-Recluse presets are useful stress cases, but do not reduce the experiment to Investigator-only candidate pairs.

### FN-BUNDLE-3 — BEGINNER labels and gate derivation

Build a manually reviewed corpus:

```text
BAD_TOO_STRONG
ACCEPTABLE
BAD_TOO_WEAK
UNCERTAIN
```

Use it to determine which diagnostics actually identify poor beginner bundles and only then define BEGINNER thresholds/gates.

### FN-BUNDLE-4 — shadow selection

After gates are validated:

```text
legal bundle pool
→ Badness rejection
→ uniform random survivor
```

Run in shadow against current production behavior before cutover.

### FN-BUNDLE-5 — uncertainty expansion

Add sequentially:

1. Drunk shown-role/false-information effects;
2. Spy/Recluse registration ambiguity;
3. Poisoner first-night target/dynamic impairment.

Do not assume healthy-only gates transfer unchanged.

## 9. Existing EPI-MQ foundation to preserve

EPI-MQ-0.5 remains complete and authoritative foundation:

```text
READY
DEFERRED(missing capabilities)
DEFERRED != UNSAT
```

Preserve/reuse:

- `EpistemicEvaluationCapabilityBoundary`;
- exact recipient-visible world semantics;
- historical replay;
- `EpistemicObservationDraft` / observation contracts;
- non-mutating preflight where appropriate;
- candidate applied exactly once;
- hidden-information boundaries;
- existing typed legal candidate ownership.

The old EPI-MQ-1 neutral evaluator extraction is now an **enabling option**, not automatically the next product milestone. During FN-BUNDLE-0, decide whether it is necessary to support complete-bundle evaluation cleanly.

## 10. Explicitly superseded immediate route

Do not continue mechanically with the old sequence:

```text
EPI-MQ-1 neutral evaluator
→ EPI-MQ-2 hard gates
→ EPI-MQ-2.5 truth+false shadow
→ EPI-MQ-3 unified scalar productive uncertainty
→ EPI-MQ-4 impaired truth/false cutover
```

Useful infrastructure/concepts may be reused later, but the current execution route is FN-BUNDLE-*.

The important product correction is:

> **evaluate complete first-night bundles, reject poor ones, then randomize among acceptable survivors.**

## 11. Beginner / experienced design boundary

Long-term, clue difficulty should adapt to the player receiving it and/or table composition.

A clue appropriate for a beginner can be over-revealing for an experienced player.

The architecture should avoid hardcoding Beginner forever, but the current experiment must not add multiple profiles before the metrics are validated.

Current scope:

```text
BEGINNER only
```

Future scope may introduce per-recipient experience and table skill imbalance after FN-BUNDLE-3/4 succeeds.

## 12. LLM boundary

Do not integrate a generic LLM in the current route.

Future LLM use may be evaluated as a **soft critic** only after deterministic legality and Badness filtering.

It must not own:

- legality;
- exact rules;
- possible-world consequences;
- initial Badness thresholds;
- hidden Storyteller state;
- automatic authoritative selection.

## 13. External-data direction

ClockTracker and expert Storyteller records are promising future calibration sources.

They should be used to validate questions like:

- whether expert Storytellers avoid our rejected regions;
- whether BEGINNER gates are too strict;
- whether acceptable choices show useful diversity.

Do not treat recorded games as automatically correct labels.

## 14. Ownership constraints

Preserve:

```text
rules
  -> legal information / registration semantics

session
  -> canonical actual state / timeline / durable authority

epistemic
  -> recipient/public-profile hypotheses and consequence diagnostics

recommendation
  -> compose legal bundles
  -> Badness policy
  -> random survivor selection

UI
  -> presentation / confirmation / future experience settings
```

Do not create a recommendation-owned parallel rules engine or leak hidden actual targets into recipient inference.

## 15. Test direction

Follow root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

Durable semantic behavior should be tested at the true owner seam.

Minimum route proof eventually includes:

```text
same setup/profile/bundle -> deterministic diagnostics
fixed truthful observations cannot be altered for balance
bundle composition contains only legal candidates
hypothetical bundle evaluation does not mutate durable state
candidate observations are applied exactly once
hidden actual state does not shortcut recipient/public inference
DEFERRED != UNSAT
seat changes that alter Empath/Chef information change bundle diagnostics appropriately
combined-clue collapse can be detected even when each isolated clue looks acceptable
```

Epistemic/enumeration changes trigger the affected T2/T3/T4 validation from `docs/TESTING_STRATEGY.md`.

Documentation-only planning changes do not require Android regression execution.

## 16. Stable UI contracts

Do not mix this route with current UI redesign.

Preserve:

- Beginner/Experienced share gameplay/rules/legal-candidate/recommendation/session/persistence pipeline;
- highlight semantics;
- `✓` truth/registration marker semantics;
- persistent yellow Red Herring badge;
- poison visual marker separate from eligibility;
- current recommendation/manual-alternative presentation;
- current bottom-navigation/domain-action separation.

## 17. Paused / unrelated

Pair-information display latency / old-device abnormal exit is paused.

PR #109 remains unrelated unless live status changes; query before touching.

## 18. First concrete action in the new conversation

After reading the canonical documents and querying live `main`:

1. audit the live source for every first-night information producer and candidate owner;
2. classify each first-night element as fixed observation, Storyteller-controlled legal choice, player-controlled choice, or deferred-complexity source;
3. map how complete candidate bundles can be composed without duplicating legality logic;
4. map the exact epistemic evaluation path for applying several observations once each under `BEGINNER + PUBLIC_GOOD_INFO`;
5. estimate candidate-space size for representative 7-player setups;
6. define the smallest typed `FirstNightInformationBundle` / diagnostics experiment contract;
7. identify whether any neutral evaluator extraction is a prerequisite;
8. propose the exact FN-BUNDLE-1/2 tests and harness;
9. **stop before inventing final Badness thresholds** and review the experimental design/evidence first.

## 19. Stable rule

> **The new active route evaluates the complete first-night information ecology. It rejects clearly poor legal bundles using interpretable, experimentally calibrated BEGINNER Badness criteria and then selects randomly from acceptable survivors. Investigator-only analysis is a lower-level correctness tool, skill adaptation comes after the beginner baseline, and general-purpose LLM recommendation is deferred.**
