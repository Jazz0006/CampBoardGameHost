# DLB-3B Drunk Setup Consequence Envelope — Architecture Audit — 2026-09-29

> Status: **DLB-3B1 COMPLETE / ACCEPTED — SCORE-FREE CONSEQUENCE ENVELOPE**
>
> Baseline: `main@ed6674e20ba482ba0809f4501557d02956c4a1e3`
>
> Scope: first DLB-3B slice only.

## 1. Purpose

DLB-3A already carries every rules-legal late-bound Drunk candidate through a pure SetupPrecommit shadow, candidate-specific hypothetical setup, first-night ecology census, DecisionTrace and multi-policy replay.

C1D has now validated one real historical Drunk-assignment prefix across the EvidenceLab -> Host boundary.

DLB-3B therefore needs a stable setup-level consequence contract before any Drunk-seat preference can be projected.

This first slice is deliberately descriptive and score-free.

It does **not** modify:

- `DecisionFeatures`;
- `BEGINNER_CONSERVATIVE_V1`;
- selector behavior;
- canonical setup/session state;
- App/Host/UI;
- persistence/Recovery;
- Drunk legality;
- fixed-Drunk misinformation evaluators.

## 2. Evidence pressure

The current three-case evidence package contains at least two distinct rationale families.

### Topology / healthy-information consequence

G10 Game 2:

- the full shown-role layout is fixed;
- shown Empath is selected as Drunk;
- explicit rationale: Empath is adjacent to the Demon.

This requires the consequence contract to retain setup topology around each candidate without hard-coding Empath or Demon preference.

### Impaired-information opportunity

A Fond Farewell:

- full layout is fixed;
- shown Chef is selected as Drunk;
- explicit rationale includes using a deliberately extreme Chef misinformation route.

This requires the consequence contract to retain whether the selected shown role has a Storyteller-controlled first-night information factor and whether that factor has more than one legal output.

It does not justify selecting one output in advance.

## 3. Ownership

Existing owners remain authoritative:

- `TroubleBrewingDrunkCandidateDomain` owns legal Drunk candidates;
- `TroubleBrewingDrunkHypotheticalProjector` owns candidate-specific hypothetical GameState;
- `TroubleBrewingFirstNightBundleCandidateSpaceAuditor` owns first-night factor identities, controls and legal option domains;
- `DrunkSetupShadowAdapter` owns the DLB-3 shadow envelope/replay wiring.

DLB-3B adds a pure SDE-facing consequence projector over those existing outputs.

It must not regenerate legality.

## 4. Proposed typed contract

Add a candidate-local:

`DrunkSetupConsequenceEnvelope`

with independent fields.

### 4.1 Setup topology

Retain factual circular-seat context:

- candidate seat;
- immediately previous seat;
- immediately next seat;
- adjacent Evil seats;
- adjacent Demon seats;
- adjacent Minion seats.

These are descriptive facts from the candidate hypothetical setup.

No field means:

- "good candidate";
- "dangerous role";
- "prefer this topology";
- numeric preference strength.

### 4.2 First-night impaired-information opportunity

Read only factors already emitted by the first-night ecology audit whose:

- `sourceSeat == candidate.seat`;
- `profileExposure == PUBLIC_GOOD_INFO`.

For each such factor retain:

- stable factor ID;
- factor kind;
- current control owner;
- option count;
- whether more than one legal output exists.

This describes the information route available if this candidate is Drunk.

It does not:

- choose an output;
- score option count;
- compare role names;
- infer later-night behavior not present in the current first-night census.

### 4.3 Explicit limitations

Carry forward candidate-space limitations already owned by the ecology audit:

- excluded player-controlled elements;
- deferred first-night complexities.

This is required so Fortune Teller target choice, Poisoner target and Spy/Recluse registration are not silently treated as resolved setup-time inputs.

## 5. Projection rules

The projector consumes exactly:

```text
TroubleBrewingDrunkCandidate
+ TroubleBrewingDrunkHypotheticalSetup
+ FirstNightBundleCandidateSpaceAudit
-> DrunkSetupConsequenceEnvelope
```

It must verify candidate identity is preserved.

Circular adjacency uses canonical seat order from the projected GameState. The DLB intermediate contract already guarantees seats 1..N.

No history or future setup choice is read.

## 6. Integration into DLB-3A shadow

Add the consequence envelope beside the existing:

- candidate;
- hypothetical setup;
- ecology audit;
- SDE candidate;
- proposed commit ref.

Do **not** copy this envelope into `DecisionFeatures` in this slice.

Therefore the existing 3A behavior remains:

- strategic feature = `NOT_PROJECTED_YET`;
- V1 = `Deferred(STRATEGIC_FEATURE_UNAVAILABLE)`;
- selection = null;
- DecisionTrace actualChoice = Pending.

This is intentional.

## 7. Stable tests

Add a dedicated typed test that proves:

1. G10 seat-1 Empath topology records seat 2 as adjacent Evil + Demon and seat 9 as the other adjacent seat;
2. G10 Empath exposes its own first-night public-good-info factor as Storyteller-controlled with multiple legal outputs;
3. the Spy in G10 remains represented as deferred Spy/Recluse registration complexity;
4. a Drunk Chef candidate exposes a first-night Storyteller-controlled numeric route with multiple legal outputs;
5. a non-first-night-information Townsfolk candidate may have no candidate-seat public-good-info factors without error;
6. shadow candidate order is unchanged;
7. frozen V1 still defers and no selection is produced.

The tests must not assert that Empath or Chef is the preferred Drunk.

## 8. Allowlist

Production:

- new `DrunkSetupConsequenceEnvelope.kt`;
- `DrunkSetupShadowAdapter.kt` only to attach the new envelope.

Tests:

- new `DrunkSetupConsequenceEnvelopeTest.kt`;
- existing DLB-3A tests only if a constructor assertion requires a mechanical update.

Docs:

- this audit;
- roadmap/handoff only after GREEN acceptance.

## 9. Explicit NO-GO

- no scalar score;
- no role-name ranking;
- no hard-coded G10 seat/layout branch;
- no Empath-specific policy rule;
- no Chef-specific policy rule;
- no output selection;
- no use of Red Herring/Demon bluffs/later Night-1 events as pre-assignment evidence;
- no player-skill policy in this slice;
- no V1 mutation;
- no Beginner automatic Drunk authority;
- no canonical commit.

## 10. Next gate

After this envelope is accepted, the next DLB-3B slice may decide which envelope facts are sufficiently evidence-qualified to project into a dedicated Drunk-assignment feature surface.

That later step must remain separately versioned from frozen `BEGINNER_CONSERVATIVE_V1`.


## 11. Test-first evidence

### RED

Test-first contract head:

`2d0f305ee8f33fdb9fb6bafb1181c5bee035af95`

CI #3539 reached Android unit-test compilation and failed exactly on the not-yet-implemented DLB-3B contract:

- unresolved `DrunkSetupConsequenceProjector`;
- unresolved consequence-factor fields;
- unresolved `DrunkSetupShadowCandidate.consequenceEnvelope`.

R2 #3278 was GREEN.

No unrelated production or test failure was observed.

### GREEN / T1

Production GREEN head:

`641e1077bf1bfb31a7d4a9e3aed66cda8ed2c4ec`

CI #3541:

- Android FAST unit tests: GREEN;
- aggregate CI gate: GREEN.

R2 #3280: GREEN.

Exact executable scope is limited to:

- new `DrunkSetupConsequenceEnvelope.kt`;
- narrow attachment in `DrunkSetupShadowAdapter.kt`;
- owning `DrunkSetupConsequenceEnvelopeTest.kt`.

The existing DLB-3A `DecisionFeatures`, frozen V1 policy and selector remain unchanged.

## 12. Exact diff / fan-out audit

The PR executable diff has one new descriptive projector and one existing production consumer.

Production producer/consumer map:

```text
DLB-2 hypothetical projector
+ existing first-night ecology auditor
        ↓
DrunkSetupConsequenceProjector
        ↓
DrunkSetupShadowCandidate.consequenceEnvelope
```

No other production constructor or consumer owns this new envelope.

The projector:

- reuses existing candidate/hypothetical/ecology authorities;
- does not regenerate legal Drunk candidates;
- does not regenerate legal information-output domains;
- does not read future setup/history;
- does not write canonical state;
- does not map into `DecisionFeatures`.

T4 full-CI acceptance is requested for this stable setup/recommendation contract.



## 13. T4 acceptance

Accepted DLB-3B1 checkpoint:

`84dcf50d7538fad477be692e027c232b9884618a`

The executable production tree is the preceding GREEN implementation `641e1077bf1bfb31a7d4a9e3aed66cda8ed2c4ec`; the accepted head adds only this audit's validation record and full-CI trigger.

Exact-head acceptance:

- CI #3542 / run `36526798307`: GREEN;
- Android full unit tests + debug APK assemble: GREEN;
- ASP contract tests: GREEN;
- Real Clingo cross-validation: GREEN;
- aggregate CI gate: GREEN;
- R2 #3281 / run `36526798331`: GREEN.

DLB-3B1 is COMPLETE / ACCEPTED.

Accepted semantics:

- every DLB-3 shadow candidate now carries a score-free setup consequence envelope;
- circular adjacent Evil / Demon / Minion topology is factual only;
- candidate-seat first-night public-good-info factor shape is projected from the existing ecology owner;
- option count and Storyteller control remain descriptive and do not select an output;
- player-controlled exclusions and deferred complexities remain explicit;
- `DecisionFeatures`, frozen V1, selector, canonical setup/session, UI and persistence are unchanged;
- policy evaluation therefore still defers with no Drunk selection.

## 14. DLB-3B2 next gate

DLB-3B2 must define a **dedicated Drunk-assignment feature surface** over the accepted consequence envelope.

It may project only evidence-qualified descriptive semantics.

It must not:

- reuse ordinary first-night `DecisionFeatures` as if Drunk assignment were an information-output choice;
- mutate `BEGINNER_CONSERVATIVE_V1`;
- introduce weights, scalar scores or candidate ordering;
- hard-code Empath/Chef/G10;
- imply later-night narrative consequences that the current envelope cannot represent.

The first design question for 3B2 is whether topology consequence and first-night impaired-information opportunity should be represented as independent typed projections with explicit availability, leaving later-night narrative opportunity unavailable until a broader consequence owner exists.
