# SDE-3D0 — Calibrated Policy Freeze / Cutover Gate Architecture Audit

> Date: 2026-09-24 Australia/Sydney  
> Branch: `sde-3c-decision-trace-shadow-replay`  
> Entry HEAD: `deec6e3ad848f168290d40dfbc392a1a5e6df5e4`  
> Status: **ARCHITECTURE / EVIDENCE ROUTE ACCEPTED — NO PRODUCTION POLICY CHANGE**

## 1. Audit decision

Execution follow-up (2026-09-25): 3D1 is complete. [Integration closure C0–C3](SDE_INTEGRATION_CLOSURE_ROUTE_2026-09-25.md) reached an accepted checkpoint at `8855d461` with full CI #3451 success. A later audit found bounded replay/feature correctness defects; [CR-A/B/C correctness repair](SDE_POST_AUDIT_CORRECTNESS_REPAIR_ROUTE_2026-09-25.md) now precedes C4/3D2 production feature edits. This preserves the evidence/cutover gates below and does not authorize V2 or automatic production cutover. R2/base integration remains pending. Current status belongs to the roadmap, not this historical audit.

SDE-3C is structurally complete. The next phase must not be modeled as one monolithic
"collect every missing expert datum, derive final weights, then freeze everything" gate.

The current evidence and implementation support a narrower and safer conclusion:

- `BEGINNER_CONSERVATIVE_V1` is ready to become an **immutable provisional baseline**;
- SDE-3D may begin now;
- SDE-3D is only **partially evidence-blocked**;
- individual policy surfaces may become freeze-eligible independently when their matching evidence
  gate is satisfied;
- a real `BEGINNER_CONSERVATIVE_V2` must not be created until at least one candidate-ordering or
  rejection semantic change is actually authorized;
- lack of a global numeric score, exact player-count coefficient, or complete multi-axis weighting
  is not by itself a blocker for a conservative qualitative production policy;
- automatic production cutover remains blocked per decision surface until that surface has a frozen
  policy version, stable consumed features, replay evidence, explicit fallback behavior, and the
  required evidence authority.

This replaces the overly coarse interpretation:

```text
SDE-3D = completely blocked until every evidence gap is closed
```

with:

```text
SDE-3D
    = freeze the accepted baseline
    + admit evidence-backed policy deltas incrementally
    + version every semantic policy change
    + replay each candidate version over canonical real-game history
    + freeze only the production surfaces whose gates are satisfied
```

No production recommendation authority changes in SDE-3D0.

## 2. Current V1 boundary

The accepted `BEGINNER_CONSERVATIVE_V1` policy remains:

```text
legal candidates
    ->
exact no-credible-Evil-world rejection
    ->
survivor equivalence band
    ->
deterministic seeded survivor-only selection
```

The following feature families are already structurally projected but remain diagnostic for V1
ordering:

- confirmation-chain impact;
- impaired-narrative continuity / detectability;
- healthy-information utility;
- contextual role-function exposure.

The following important policy dimensions remain unavailable or not yet represented by a stable
matching production projector:

- truth danger / credibility disruption;
- contextual Red-Herring policy consumption;
- Demon-bluff narrative / route diversity;
- future flexibility / candidate-relationship consequences.

Therefore V1 is valuable precisely because it does **not** pretend that unavailable or
under-evidenced dimensions are neutral or numerically known.

## 3. Four calibration authority classes

SDE-3D must classify policy facts before deciding whether they can affect production behavior.

### 3.1 STRUCTURAL_SAFETY

Semantics fixed by rules, lifecycle ownership, architecture, or already-accepted typed contracts.

Examples:

- production legality remains upstream authority;
- committed history is immutable;
- player-controlled targets remain player-owned;
- Spy/Recluse registration is interaction-scoped;
- Drunk shown identity / Red Herring / revealed Demon bluffs preserve their lifecycle ownership;
- replay uses canonical history rather than persisted old feature snapshots;
- unsupported evidence must remain explicit rather than silently treated as neutral.

These require correctness/regression evidence, not new expert calibration.

### 3.2 KNOWN_HARD_REJECTION

A generic candidate boundary strong enough to reject rather than merely prefer.

At the current checkpoint the only accepted generic policy rejection is:

- exact strategic Evil-topology retention reaches zero / no credible Evil world remains.

Do not promote a non-zero threshold, last-healthy-route predicate, role-exposure predicate, narrative
break, or bluff burden into a hard rejection without evidence specifically supporting that strength.

### 3.3 WEAK_PREFERENCE

A generic partial ordering supported by a stable typed projector plus sufficiently direct E3 expert
policy evidence.

A weak preference:

- must remain interpretable through typed predicates/reason codes;
- must not become an opaque scalar merely to total-order candidates;
- may leave multiple survivors equivalent;
- must preserve seeded ties when evidence does not distinguish candidates.

No new weak preference is authorized at the SDE-3D0 checkpoint.

### 3.4 UNRESOLVED_CALIBRATION

A plausible dimension whose existence or semantics may already be valid but whose policy strength,
boundary, or ordering remains under-evidenced.

This state is normal and must not automatically block unrelated surfaces.

## 4. Evidence gaps and what they actually block

### Gap A — healthy-information floor / middle band

Current status: **highest-value policy blocker**.

The typed feature surface already represents route preservation/loss, including
`removesLastUsableHealthyRoute`, but the current evidence does not yet justify converting that fact
into a universal reject or preference.

What would materially advance it:

- independent experienced Storyteller rationale explicitly avoiding a legal alternative because it
  leaves too little usable healthy information;
- preferably a case where another legal candidate preserves a healthy channel;
- repeated evidence across table ecologies before any numeric player-count band is frozen.

Blocks:

- healthy-information soft preference;
- healthy-information near-hard gate;
- numeric middle-band calibration.

Does **not** block:

- freezing V1 as the current baseline;
- other independently supported policy surfaces;
- SDE-3D architecture/versioning work.

### Gap B — independent-expert impaired-information believability

Current status: Ben supplies real explicit believability rationale, but the strongest evidence is
single-expert and does not directly establish the current cross-history narrative-break predicate.

Blocks:

- automatic cross-night impaired-narrative preference strength;
- any deterministic "prefer false" or "avoid truth" rule.

Does not block the generic narrative projector or V1 freeze.

### Gap C — role-function exposure severity

Current status: semantic projector complete; severity evidence insufficient.

Blocks:

- automatic preference against avoidable direct registration-ambiguity exposure;
- hard/strong avoidance of Librarian -> Recluse or Investigator -> Spy merely from role identity.

Does not block the diagnostic feature.

### Gap D — Demon-bluff triplet ordering

Current status: authentic expert/SILVER triplets prove variation, not preference.

Blocks:

- automatic bluff-triplet partial ordering or weighting.

Does not block the joint-output legality/feature architecture.

### Gap E — quantitative multi-axis tradeoff

Current status: intentionally unresolved.

This gap must **not** be treated as a prerequisite for the first conservative production policy.
SDE may remain partial-order based indefinitely.

Gap E becomes blocking only if a future policy explicitly requires:

- numeric thresholds;
- player-count coefficients;
- weighted tradeoffs;
- a scalar objective.

Do not manufacture those constructs merely to claim that calibration is complete.

## 5. Current external evidence supports these conclusions now

ClocktowerEvidenceLab / admitted B4 evidence is already strong enough to preserve the following
policy architecture:

1. **Spy/Recluse registration is interaction-scoped.**
   It must not become persistent/global registration state.

2. **Impaired information may be true or false.**
   There is no evidence-authorized "false at all costs" policy.

3. **Strategic topology is important but insufficient.**
   Multiple real-game legal domains are topology-neutral across alternatives; non-topology policy
   dimensions remain first-class.

4. **Red Herring is contextual and downstream-aware.**
   Ben's target-ecology rationale and Evin's truth-danger / credibility-disruption rationale are
   independent mechanisms supporting a shared high-level conclusion: Red Herring selection must not
   collapse to a fixed neighbor bonus, role bonus, or random seat heuristic.

5. **Truth danger / credibility disruption is a real policy dimension.**
   Current evidence is sufficient to preserve/build the feature surface, but not to freeze a
   deterministic suppression rule.

6. **Impaired-information believability is a real policy dimension.**
   Current evidence supports the dimension but not yet a generic cross-night ordering predicate.

Observed expert choice alone remains non-label evidence. Final win/loss remains invalid as a direct
Storyteller-decision quality label.

## 6. Synthetic / human-label evidence policy

The following remain downgraded:

- pathological Chef + Empath / already-collapsed synthetic tables -> **DIAGNOSTIC_ONLY**;
- the retired "clean" scenarios that deliberately excluded Drunk/modifier-rich context -> do not
  restore as an active calibration stratum;
- isolated DecisionSlices without whole-game context -> secondary analysis artifacts;
- historical one-reviewer labels -> compatibility/history only, not current policy truth.

Modifier-rich real bundles containing Drunk / Spy / Recluse are part of the actual Trouble Brewing
balance model and must not be excluded by default from calibration.

Synthetic fixtures remain useful for:

- mechanics;
- exact regression;
- boundary sensitivity;
- failure reproduction.

They must not become the primary calibration standard.

## 7. V1 freeze rule

`BEGINNER_CONSERVATIVE_V1` should now be treated as an immutable versioned baseline.

After SDE-3D1 freeze:

- the existing zero-topology rejection semantics must not change under the same policy version;
- survivor equivalence semantics must not change under the same policy version;
- seeded selector behavior must not silently change under the same policy version;
- evidence/limitation reporting may only change if it is proven non-semantic for recommendation
  behavior and trace compatibility;
- any new candidate rejection, preference, or survivor-refinement semantic requires a new explicit
  policy version.

Do not create a placeholder V2.

Create `BEGINNER_CONSERVATIVE_V2` only when a real evidence-authorized semantic delta exists.

## 8. Revised SDE-3D slices

### SDE-3D0 — calibrated freeze / cutover gate architecture audit

Status: **COMPLETE by this audit once roadmap/handoff are synchronized.**

Deliverables:

- separate structural safety, hard rejection, weak preference and unresolved calibration;
- define evidence-gap-to-policy-surface blocking;
- define immutable V1/version-transition rule;
- define surface-scoped 3D/3E gates;
- preserve evidence uncertainty explicitly.

No production behavior change.

### SDE-3D1 — V1 immutable baseline freeze

Goal:

- make the accepted V1 behavior and evidence checkpoint an explicit frozen policy baseline;
- record/replay representative current-policy traces as the comparison control;
- add version-integrity regression only if an uncovered durable contract exists;
- do not add new candidate ordering.

This is an engineering/version-governance slice, not a calibration slice.

### SDE-3D2 — calibration-ready feature completion

Post-audit prerequisite: CR-A impaired-narrative semantics, CR-B typed game/request identity, and CR-C strict nested replay decoding must be accepted before production feature edits in this slice. The architecture/evidence/fanout audit may be prepared earlier.

Only add missing projectors that already have enough E1/E2/cross-expert basis to justify a stable
feature surface.

First audit candidate:

- truth danger / credibility disruption;
- contextual Red-Herring downstream policy input.

The feature may become production-owned while remaining policy-neutral.

Do not implement a preference simply because the feature exists.

### SDE-3D3 — first evidence-authorized policy delta

Wait for the first policy predicate that genuinely passes the E3 gate.

Potential sources include Gap A/B/C/D, but do not preselect a winner.

When a real semantic delta is authorized:

- create `BEGINNER_CONSERVATIVE_V2`;
- encode the generic typed predicate/reason;
- preserve partial ordering;
- keep unrelated unresolved dimensions tied.

### SDE-3D4 — V1/V2 canonical real-corpus replay

Replay the same committed game histories through both versions using SDE-3C5.

Acceptance must check:

- structural invariants and hard safety regressions;
- exact policy-version/evidence provenance;
- behavior on cases with explicit Storyteller rationale;
- no hindsight;
- no mutation of historical truth;
- no conversion of observed expert choice into an unconditional positive label.

### SDE-3D5 — surface-scoped calibrated freeze

Freeze only the production surfaces whose consumed predicates satisfy their evidence and replay
gates.

Unsupported decision families may remain:

- legacy-authoritative;
- advisory/shadow;
- manual;
- explicitly deferred.

SDE-3D completion does not require every future policy dimension to be numerically solved.

## 9. SDE-3D completion condition

Do not define completion as "Gap A through E are all closed."

SDE-3D is complete for the first production release when:

1. at least one explicit production cutover scope is named;
2. the policy version used by that scope is immutable/frozen;
3. every feature consumed by that policy surface is structurally stable;
4. every active preference/rejection has evidence authority appropriate to its strength;
5. canonical real-game replay for the version has passed its regression gate;
6. unresolved dimensions cannot silently influence that surface;
7. unsupported surfaces have an explicit fallback;
8. no policy relies on fixture-specific branches, unsupported thresholds, or hidden legacy scalar
   imports.

Further evidence may later create V3/V4 without reopening the correctness of the already frozen
version.

## 10. Revised SDE-3E automatic cutover gate

SDE-3E should be evaluated per decision surface rather than as one global switch.

A surface is cutover-eligible only when all applicable conditions hold:

```text
structural correctness
+ explicit frozen policy version
+ stable consumed feature projectors
+ evidence-authorized active predicates
+ canonical real-game replay accepted
+ DecisionTrace / actual-choice correlation available
+ manual Experienced-mode override preserved
+ explicit fallback for unsupported/unavailable dimensions
+ legacy-authority retirement/fanout plan for that surface
```

Critical rule:

> A technically available V1 selection is not sufficient reason for automatic cutover when the
> material decision dimensions remain unsupported and the policy is effectively selecting among a
> large equivalence class.

In that situation production must retain legacy/manual/deferred authority rather than treating
unsupported dimensions as neutral.

## 11. Explicit non-goals

SDE-3D0 does not:

- change production policy behavior;
- create V2;
- add weights;
- add numeric thresholds;
- add player-count bands;
- implement Traveller support;
- revive retired clean/extreme calibration fixtures;
- promote observed expert choices to labels;
- authorize automatic production cutover;
- merge or mark PR #154 ready.

## 12. Immediate next task

After this audit is synchronized, the next executable slice should be:

**SDE-3D1 — BEGINNER_CONSERVATIVE_V1 immutable baseline freeze.**

Start with a contract/fanout audit of:

- policy-version constants/identity;
- evidence checkpoint identity;
- DecisionTrace/replay version binding;
- tests that currently define V1 recommendation semantics;
- any code path that could silently change V1 behavior without changing the version.

Only after that audit should code changes be considered.

The next feature-development candidate after V1 freeze is a separate SDE-3D2 architecture audit for
truth danger / credibility disruption and contextual Red-Herring projection.