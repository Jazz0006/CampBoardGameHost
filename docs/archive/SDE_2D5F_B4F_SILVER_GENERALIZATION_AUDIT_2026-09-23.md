# SDE-2D5F — B4F bounded SILVER generalization audit

> Date: 2026-09-23 Australia/Sydney  
> Branch: `sde-2d5-calibration-policy-evidence`  
> PR: #150 — must remain draft

## 1. Scope

B4F starts only after GOLD-derived / explicit-primary hypotheses exist.

This audit asks a narrower question than the earlier external-human pilot:

> Do existing SILVER records support, contradict, or fail to test the dimensions already exposed by GOLD evidence?

It does **not** use SILVER to invent new policy gates, infer negative labels from unchosen alternatives, or freeze numeric weights.

Current GOLD anchors:

- `st-ben-burns` — A Stud In Scarlet;
- `st-evin` — Evin 2019 first full playthrough;
- A Fond Farewell supplies additional primary Ben rationale but remains Traveller-execution-blocked.

## 2. Dedicated executable B4F workload

New evidence harness:

- test: `Sde2D5FB4FSilverGeneralizationExperiment`;
- task: `:app:sde2D5FB4FSilverGeneralization`;
- workflow: `SDE-2D5F B4F SILVER generalization`;
- artifact: `sde-2d5f-b4f-silver-generalization`.

Accepted run:

- workflow run **#3**;
- head: `17254848e47abf05c1b7648948ae8b47a89f9e6f`;
- conclusion: **SUCCESS**;
- end-to-end runtime: about **7 minutes**.

The workload reuses the existing executable ClockTracker ct-01 fixture and production-owned legality/topology surfaces.

It intentionally does **not** run the historical legacy setup-ranking comparison or the broad per-bluff support sweep.

The previous `sde-2d5-external-human-pilot.yml` workflow historically took about **2h48m** for a successful run and still invoked the old D5 calibration route. That automatic workflow has been retired. The ct-01 fixture itself remains available and is reused by the narrow B4F harness.

## 3. Executable ct-01 result

Source:

`ffb40a93-3d7b-42c4-bba8-bc9c363dcd30`

Current production replay confirms:

- observed Demon bluffs Chef / Investigator / Saint remain legal;
- observed Red Herring remains legal;
- observed Drunk-shown-Empath 0 remains in the complete legal domain;
- that observed 0 remains semantically false relative to the healthy state;
- the observed Night-1 public information bundle remains feasible for every recipient.

This is an external-validity / expressiveness result. It does not convert SILVER into GOLD.

## 4. Red Herring contextual utility

### GOLD hypothesis

Primary expert rationale now gives two different mechanisms:

- Ben: likely Fortune Teller target ecology;
- Evin: truth danger / credibility disruption of Doug's Chef information.

Cross-expert invariant:

> Red Herring placement is a contextual precommit whose value depends on downstream information ecology.

### SILVER generalization

- `ct-04`: Fortune Teller repeatedly hits the Red Herring and receives repeated YES results.
- `ct-01`: a real structured Night-1 ecology contains a legal committed Red Herring inside a fully feasible bundle.

Supported:

- Red Herring can have persistent downstream epistemic impact;
- static legal-seat equivalence is insufficient for policy reasoning.

Not supported:

- any particular seat ordering;
- neighbour bonus;
- role-strength bonus;
- maximizing expected Red-Herring hits.

B4F verdict:

`GENERALIZES_DOWNSTREAM_IMPORTANCE_NOT_SELECTION_ORDERING`

## 5. Truth danger / credibility disruption

### GOLD hypothesis

Evin explicitly chose Doug/Chef as Red Herring because Chef information was especially damaging to Evil and Red-Herring contamination could make the table doubt Doug and his Chef result.

### SILVER generalization

`ct-02` contains explicit Storyteller rationale: after observing Evil unexpectedly clustered, the Storyteller considered Drunk Chef versus Drunk Investigator and chose Investigator.

The lifecycle mechanism differs, so ct-02 is not a direct Red-Herring analogue.

It is nevertheless consistent with the higher-level dimension:

> the danger of a healthy information channel to the actual Evil topology matters when choosing among already-legal Storyteller control surfaces.

B4F verdict:

`GOLD_PLUS_SILVER_EXPLICIT_RATIONALE`

Boundary:

- do not infer “always suppress the strongest healthy information”;
- after setup commitment, functioning healthy roles cannot be made impaired merely because their truth is inconvenient;
- the control surface remains lifecycle-owned.

## 6. Impaired-information believability

### GOLD / primary state

Ben supplies explicit primary rationale in A Stud and A Fond Farewell around believable impaired information.

### SILVER result

ct-01's Drunk-Empath 0 is:

- legal;
- false;
- part of a feasible real Night-1 ecology.

But ct-01 contains no recovered choice-specific rationale showing that 0 was selected **because** it was more believable than 1 or 2.

Therefore ct-01 is compatible with the dimension but does not generalize the preference.

B4F verdict:

`COMPATIBLE_NOT_GENERALIZED`

Maturity remains:

`SINGLE_EXPERT_EXPLICIT_PLUS_REPEATED_QUALITATIVE`

## 7. Demon bluff joint output

Primary Evin evidence gives one verified expert triplet:

`Recluse / Slayer / Soldier`

SILVER observations include:

- ct-01: Chef / Investigator / Saint;
- ct-03: Saint / Monk / Investigator.

These are authentic examples of different operational mixes.

Without verified expertise plus choice-specific rationale or explicit rejected alternatives, they do not justify:

- a role ordering;
- a fixed route-diversity target;
- a shared-support threshold;
- a specific beginner triplet template.

B4F verdict:

`AUTHENTIC_VARIATION_WITHOUT_PREFERENCE_ORDERING`

## 8. Whole-bundle model validity

The executable ct-01 replay is the strongest B4F engineering result.

A real 14-player external ecology remains representable through the current production-owned:

- setup legality;
- numeric legal domain;
- first-night proposition materialization;
- public-good information projection;
- topology-first whole-bundle evaluator.

No fixture-local rules path was required.

B4F verdict:

`EXECUTABLE_SILVER_EXTERNAL_VALIDITY_GREEN`

## 9. Evidence-gap triage

The bounded SILVER pass does **not** unblock D5F-C.

Highest-value unresolved gaps are:

1. **healthy-information floor / middle band**
   - external postmortems support the existence of a lower bound;
   - evidence is not strong enough to derive numeric player-count gates.

2. **role-function exposure severity**
   - the architecture has a plausible generic cost;
   - evidence does not establish the relative severity or override conditions.

3. **independent-expert impaired-information believability**
   - Ben gives strong explicit rationale;
   - cross-expert primary confirmation is still missing.

4. **Demon-bluff triplet preference ordering**
   - real triplets exist;
   - rationale for choosing one legal triplet over another remains weak.

5. **quantitative tradeoff surface**
   - topology retention, confirmation chains, healthy-information utility, credibility disruption, bluff usability, and future flexibility must remain separate;
   - no defensible weights or gates exist yet.

## 10. Execution decision

B4F-A and B4F-B are complete:

- B4F-A — executable ct-01 SILVER replay: **COMPLETE**;
- B4F-B — bounded documentary comparison using ct-02 / ct-03 / ct-04: **COMPLETE**.

Next:

`B4F-C — EVIDENCE-GAP TRIAGE / TARGETED COLLECTION`

Rules for B4F-C:

- do not resume broad video collection;
- request / extract a new primary case only when it fills one named gap;
- do not verify more Ben material merely to increase sample count under `st-ben-burns`;
- prefer a new independent Storyteller when the missing claim is currently single-expert;
- use ClocktowerEvidenceLab as the long-term collection pipeline rather than embedding broad source collection into the app repository;
- keep D5F-C and SDE-3 blocked;
- keep Traveller implementation outside B4.

## 11. No policy freeze

This audit authorizes **evidence maturity changes only**.

It does not authorize:

- D5F-C numeric gates;
- a global weighted score;
- fixed Red-Herring bonuses;
- false-at-all-costs Drunk/Poison policy;
- role-specific bluff rankings;
- fixture-specific production branches.
