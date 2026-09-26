# SDE-2D5F — B4F-C targeted evidence-gap contract

> Date: 2026-09-23 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `sde-2d5-calibration-policy-evidence`  
> PR: #150 — must remain draft

## 1. Purpose

B4F-A/B established that existing SILVER evidence can generalize some GOLD-derived dimensions, but it cannot supply the missing calibration gates.

B4F-C therefore stops broad source collection and defines **targeted evidence requests**.

The app repository owns:

- the policy question;
- the required decision lifecycle state;
- the production legality / counterfactual requirements;
- the evidence maturity needed before D5F-C.

The external evidence-collection project should own:

- discovering candidate recordings/logs;
- extracting primary timestamps / screenshots / provenance;
- preserving source-level evidence.

Do not embed a broad web/video collection subsystem into CampBoardGameHost.

## 2. Gap A — healthy-information floor / middle band

### Why this is the highest-value blocker

D5F-C ultimately needs a defensible beginner-table middle band.

Existing evidence supports the qualitative shape:

```text
too much reliable information
    -> Evil topology collapses / confirmation chains become oppressive

too little reliable information
    -> Good has too little actionable structure
```

What is missing is evidence strong enough to calibrate where those failure regions begin across player-count bands.

### Target source

Prefer a real Trouble Brewing game with:

- experienced/trusted Storyteller;
- beginner or mixed-experience table context;
- reconstructable setup and Night-1 information bundle;
- explicit Storyteller rationale or postgame explanation that the bundle was intentionally made stronger/weaker because Good had too much/too little usable information;
- production-recoverable legal alternatives.

Especially valuable:

- a Storyteller explicitly rejecting an alternative because it would leave too little healthy information;
- a Storyteller explicitly softening a confirmation chain or dangerous truth while preserving another healthy channel.

### Minimum useful extraction

- complete seat/role map;
- Drunk shown identity if any;
- Poisoner target if already committed;
- Demon bluffs;
- Red Herring;
- all material first-night information;
- explicit rationale timestamp;
- player-experience context.

### Success threshold for this gap

One new independent GOLD rationale case would materially improve the evidence surface, but should still not by itself create numeric gates.

For numeric player-count gates, seek repeated independent evidence across at least more than one table size / ecology.

## 3. Gap B — independent-expert impaired-information believability

### Current state

Ben supplies explicit primary rationale:

- A Stud: Drunk Empath 0 because 2 was “a little unbelievable”;
- A Fond Farewell: Drunk Chef 4 as “a somewhat believable number”.

These share `st-ben-burns`.

### Target source

Prefer a different experienced Storyteller with a Drunk or poisoned information role where the source explicitly explains:

- why a truthful or false result is believable;
- why another legal result would be too obvious / implausible;
- or why continuity with earlier information matters.

Highest-value version:

- recurring information across multiple nights;
- explicit discussion of preserving a coherent perceived world.

### Success threshold

One independent GOLD explicit-rationale case can upgrade the dimension from single-expert explicit evidence to cross-expert foundation.

It still must not create a “prefer false” rule.

## 4. Gap C — role-function exposure severity

### Current state

The architecture contains plausible generic costs for:

- Librarian -> Recluse;
- Investigator -> Spy.

The rules allow them when legal/forced. What remains uncertain is policy severity when healthy alternatives exist.

### Target source

A high-value case must contain:

- actual Spy and/or Recluse;
- functioning Librarian or Investigator;
- at least one alternative legal clue so the choice is not forced;
- explicit Storyteller rationale, ideally considering/rejecting direct role-function exposure.

Useful questions:

- did the Storyteller avoid pointing Librarian at Recluse because it explains later registration too cleanly?
- did the Storyteller avoid pointing Investigator at Spy because it destroys the Spy's concealment function?
- did whole-bundle health override that concern?

### Success threshold

Do not infer severity from silent non-selection.

Prefer explicit rationale or explicit considered/rejected alternatives.

## 5. Gap D — Demon-bluff triplet preference ordering

### Current state

Primary/SILVER records provide authentic triplets:

- Evin: Recluse / Slayer / Soldier;
- ct-01: Chef / Investigator / Saint;
- ct-03: Saint / Monk / Investigator.

These prove variation, not preference.

### Target source

Prefer an experienced Storyteller who explicitly explains why one bluff set was selected over another legal set, especially for beginners.

High-value rationale dimensions:

- claim burden / cadence;
- distinct narrative routes;
- collision with actual Good information;
- support from Spy / poisoned / Drunk information;
- fallback after a bad Day-1 claim;
- avoiding direct collision with the Drunk shown identity.

### Success threshold

One rationale case can validate a dimension, not a numeric triplet score.

Repeated independent rationale is required before any ordering/weight is frozen.

## 6. Gap E — quantitative multi-axis tradeoff

### Current state

The strategic topology quotient is useful but repeatedly neutral across expert alternatives.

Relevant axes include:

- topology retention;
- Demon cover;
- confirmation chains;
- healthy-information utility;
- truth danger / credibility disruption;
- role-function exposure;
- bluff usability;
- narrative coherence;
- future flexibility.

### Target evidence

This gap is not solved by one quotation.

It requires a corpus of executable GOLD/SILVER cases where:

- legal alternatives are complete;
- observed choice is known;
- at least some alternatives differ on multiple interpretable axes;
- repeated choices/rationales reveal tradeoff direction.

### Success threshold

Do not fit an opaque scalar.

The first goal is only to identify:

- hard reject conditions;
- soft preference orderings;
- dimensions that remain incomparable / context-dependent.

## 7. Acquisition priority

For the next new primary source, prefer in this order:

1. healthy-information floor / middle-band rationale from an independent experienced Storyteller;
2. independent-expert impaired-information believability rationale;
3. explicit role-function exposure rationale;
4. explicit Demon-bluff triplet rationale;
5. additional generic real-game volume only when it contributes to the quantitative tradeoff corpus.

This ordering reflects **D5F-C blocking value**, not a claim that the underlying gameplay dimensions have that importance ranking.

## 8. Admission contract

A targeted case should be promoted into CampBoardGameHost B4 evidence only after:

1. source/provenance is recorded;
2. Storyteller expertise status is explicit;
3. material committed state is reconstructable;
4. observed Storyteller choice is primary-verified when GOLD is claimed;
5. legal alternatives are recovered through production owners;
6. unavailable fields remain UNKNOWN unless production semantics prove them irrelevant to the admitted decision slice;
7. explicit rationale is separated from rule-derived consequences;
8. Storyteller independence key is recorded.

## 9. Current blocker boundary

At the current SDE-3D checkpoint:

- B4F-A executable SILVER replay: COMPLETE;
- B4F-B bounded SILVER comparison: COMPLETE;
- B4F-C target specification: COMPLETE;
- B4F-C evidence acquisition: **EXTERNAL / CONTINUOUS / TARGETED**;
- historical D5F-C numeric gate/band derivation: still evidence-blocked and no longer the sole production critical path;
- SDE-3A/B/C: COMPLETE;
- SDE-3D: **IN PROGRESS / PARTIALLY EVIDENCE-BLOCKED**;
- SDE-3E: blocked per decision surface until its matching 3D gates are satisfied.

The current evidence set may justify stable descriptive feature surfaces and semantic regression where E1/E2 support exists. It does **not** by itself authorize a new candidate preference, rejection, numeric threshold or production V2. Each such policy delta must satisfy the matching E3/E4 gate and use a new explicit policy version after the frozen V1 baseline.
