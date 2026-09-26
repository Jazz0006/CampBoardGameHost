# SDE-3B — BEGINNER_CONSERVATIVE_V1 Policy Audit

> Date: 2026-09-23 Australia/Sydney  
> Branch: `sde-3b-beginner-conservative-v1`  
> Status: **HISTORICAL POLICY PRE-FLIGHT — SDE-3B COMPLETE / V1 NOW FROZEN BY SDE-3D1; not current execution authority**

## 1. Baseline

SDE-3A is merged through PR #152 at `2a9051f1b0282bd25d01d46d36fe797857cc429d`.

The current SDE pipeline is deliberately split:

```text
production legality
    -> SdeDecisionCandidate
    -> exact consequence
    -> DecisionFeatures
    -> policy
```

SDE-3B owns only the last step. It must not regenerate legality, mutate canonical session state, select player-controlled targets, or replace visible production recommendation authority.

## 2. Current feature reality

The production-facing structured shadow currently projects:

- Demon-cover retention;
- Evil strategic-topology retention;
- Evil-cover retention;
- forced-Good seats/fraction;
- forced-Evil seats/fraction;
- actual-state semantic truth where the upstream legal candidate provides it;
- confirmation-chain impact from exact recipient-visible committed history;
- impaired-narrative coherence/detectability derived from canonical history and authoritative impairment state for malfunctioning structured interactions; functioning interactions are explicitly `NOT_APPLICABLE`;
- healthy-information utility derived from canonical history, authoritative ability state, upstream legal truth relation and exact confirmation provenance; functioning actual truth and legal registered truth are both healthy routes.

The following remain explicit `FeatureProjection.Unavailable` on that surface:

- truth danger / credibility disruption;
- role-function exposure;
- bluff narrative;
- candidate collision/support;
- future flexibility.

This is the most important SDE-3B constraint.

**Unavailable must never be treated as neutral, zero, healthy, or policy-equivalent evidence.**

A provisional policy may act only on the feature evidence actually present. Missing dimensions must remain visible as policy limitations until their projectors exist.

## 3. BEGINNER_CONSERVATIVE_V1 policy boundary

The first V1 implementation should be qualitative and conservative.

Allowed now:

1. preserve upstream candidate identity/order;
2. refuse to evaluate when the required strategic feature itself is unavailable;
3. reject an exact structural contradiction where a candidate leaves no credible Evil structure;
4. otherwise preserve candidates in one survivor equivalence band when current evidence cannot justify ordering them;
5. expose explicit policy limitations for missing preference dimensions;
6. later perform deterministic seeded selection only among final survivors.

Not allowed now:

- numeric healthy-information thresholds;
- weighted sums;
- fixed player-count cutoffs;
- fixed role bonuses/penalties;
- `forcedGoodFraction > X` / `forcedEvilFraction > Y` gates;
- interpreting a missing projector as a zero cost;
- using one GOLD/SILVER example as a deterministic rule;
- selecting Fortune Teller or Poisoner player choices;
- visible production cutover.

## 4. Hard rejection that is justified without calibration

The only first-slice structural rejection should be an exact **no-credible-Evil-world** condition.

A candidate is structurally contradictory in the first generic V1 gate only when **Evil strategic-topology retention is defined and reaches zero**.

This is not a calibrated strength threshold. It is an exact collapse to no viable Evil interpretation.

Do not use live Demon-cover retention as this generic contradiction test. In later historical phases the Demon may already be dead, so live possible-Demon-seat cover can legitimately be zero while coherent historical worlds still exist. Evil-cover and Demon-cover remain descriptive inputs until a lifecycle-aware policy rule explicitly owns them.

Do **not** reject merely because:

- only one Demon seat remains;
- forced-Good or forced-Evil seats appear;
- a retention ratio is small but non-zero.

Those require policy evidence / feature context not yet frozen.

If every legal candidate reaches the structural contradiction condition, policy must defer rather than invent a survivor.

## 5. Policy readiness / limitations contract

The per-candidate `PolicyEvaluation` from SDE-3A remains valid.

SDE-3B should add a decision-level result:

```text
BeginnerConservativePolicyEvaluation
  Ready
    evaluations[]
    limitations[]
  Deferred
    candidateIds[]
    reasons[]
```

Required invariants:

- candidate IDs remain exactly the upstream legal candidate order;
- `Deferred` never fabricates per-candidate policy results;
- `Ready` has at least one SURVIVOR;
- a rejected candidate has an explicit typed reason;
- all current viable candidates may be SURVIVOR/TIED when unsupported dimensions prevent a justified soft ordering;
- policy limitations are distinct from candidate rejection reasons.

Initial limitation vocabulary should include:

- preference dimensions not yet projected.

Initial deferral vocabulary should include:

- upstream feature evaluation deferred;
- strategic feature unavailable;
- no non-contradictory survivor.

## 6. Initial reason vocabulary

First stable candidate rejection reason:

- `no-credible-evil-world`

Do not pre-create speculative reason codes for features whose production projector does not exist yet.

Future SDE-3B slices may add typed reasons for:

- confirmation-chain collapse;
- healthy-information starvation;
- impaired narrative incoherence;
- obvious role-function exposure;
- Red-Herring contextual utility;
- bluff usability;
- future-flexibility loss.

Those must arrive with their owning feature projector and durable tests.

## 7. Soft preference semantics

The full V1 route allows ordered interpretable soft priorities, but the current numeric feature surface is not rich enough to justify them safely.

Therefore the first policy slice deliberately has this shape:

```text
legal candidates
    -> exact structural contradiction gate
    -> remaining candidates
    -> one explicit equivalence band
```

This is not a claim that the candidates are globally equal. It means the current V1 evidence surface does not yet justify a stronger ordering.

The decision-level `limitations` field carries that distinction.

## 8. Seeded survivor selection

Seeded selection belongs in SDE-3B, but should be a separate pure seam after evaluation is stable.

Requirements:

- select only from SURVIVOR candidates;
- deterministic for the same game/decision seed and candidate set;
- independent of incidental iteration order;
- never select a REJECTED or ACCEPTED candidate;
- no probability weights.

Do not couple seeded selection to visible production recommendation yet.

## 9. First shadow integration

The existing Chef / Empath structured numeric shadow is the first policy consumer.

Target pipeline:

```text
InformationDecisionContext
    -> SdeDecisionCandidate
    -> ExactConsequenceEvaluation
    -> DecisionFeatureEvaluation
    -> BeginnerConservativePolicyEvaluation
```

The result remains shadow-only.

Visible choice ordering, current legacy recommendation, confirmation, and canonical commit must remain unchanged.

## 10. Test strategy

Tests-first for the first policy slice:

- deferred feature evaluation -> policy Deferred;
- strategic feature unavailable -> policy Deferred;
- zero Evil structure -> explicit rejection reason;
- no numeric threshold: any non-zero strategic retention remains viable in the first slice;
- one viable candidate -> SURVIVOR / Unique;
- multiple viable candidates -> SURVIVOR / Tied with the complete survivor set;
- all structurally contradictory -> policy Deferred;
- candidate order preserved;
- structured Chef / Empath shadow attaches policy evaluation without mutating visible recommendation or session state.

Use T1 FAST for the logical implementation checkpoint. Run T4 `[full-ci]` only at SDE-3B acceptance.

## 11. Fanout / ownership risk

Low-risk first slice:

- new policy evaluator beside existing shadow output;
- typed policy readiness / limitation contract;
- tests at the policy owner.

High-risk and out of scope:

- modifying legacy `DecisionEvaluation<T>`;
- replacing `SetupEvaluator` or `ConsequenceEvaluator`;
- changing legal candidate domains;
- UI cutover;
- DecisionTrace persistence;
- broad feature inference inside policy code.

## 12. Implementation sequence

The original core steps 1–5 are now complete on PR #153.

The continuation order is revised by the ClocktowerEvidenceLab C0 handoff:

1. **3B1 structure — COMPLETE:** historical lifecycle-safe shadow + explicit contextual input binding;
2. **3B2 feature — COMPLETE:** confirmation-chain impact is projected generically from canonical recipient-visible history; V1 policy remains unchanged;
3. **3B3 feature — COMPLETE:** impaired-narrative coherence/detectability is derived generically from canonical history and authoritative impairment episodes; V1 policy remains unchanged;
4. **3B4 feature — COMPLETE:** healthy-information utility derives usable/independent routes, redundancy/contradiction, route loss and last-route removal; functioning registered truth is preserved and collapsed baselines are guarded; V1 policy remains unchanged;
5. **3B5 feature — CURRENT:** contextual role-function exposure;
6. **3B6 policy:** only then add expert-informed qualitative soft priorities over stable projectors;
7. keep seeded tie-breaking for unresolved equivalence;
8. keep SDE-3C DecisionTrace persistence separate;
9. keep numeric thresholds and multi-axis calibration in SDE-3D.

See `SDE_3B_STRUCTURE_AND_EXPERT_EVIDENCE_STAGING_2026-09-23.md` for the evidence authority model.


## 13. Cross-night impaired narrative ownership audit

The repository already has the correct canonical history owners:

- `EpistemicObservationLog` stores durable player-visible semantic observations;
- `ActionFactTimeline` stores durable mechanical history;
- both support one global timeline ordering;
- `ExactHistoricalHypotheticalObservationBundleEvaluator` replays that history and evaluates a hypothetical next observation against the surviving historical world set.

Therefore SDE-3B should **not** persist a second mutable perceived-world history.

The required “persistent impaired narrative state” should be implemented as a **derived role-agnostic projection over canonical history**:

```text
canonical action timeline
+ canonical observation log
+ current legal candidate proposition
+ source impairment binding
    ->
historical perceived-world consequence
    ->
impaired narrative coherence / detectability features
```

This still satisfies persistence semantically: previous committed observations constrain every later projection because they live in canonical history.

### Ability-state / historical binding status

The SDE candidate/shadow path now carries the source `AbilityState` projected from the existing legal-candidate semantics, and 3B1 adds lifecycle-safe canonical-history/input refs. Actual impairment must continue to come from rules/session semantics, not from `ObservationReliability.RECEIVED_AS_FUNCTIONING`, which only describes how information was received by the player.

SDE-3B1 through SDE-3B4 are complete. The immediate next slice is 3B5 contextual role-function exposure. Policy remains unchanged until that projector derives generic lifecycle-aware exposure facts without named-role penalties or a second state owner.

### Minimal coherence already available

The current exact consequence path already detects the strongest possible narrative failure: adding a candidate can reduce the recipient's historically credible world set to zero. The first V1 `no-credible-evil-world` gate therefore provides a minimal contradiction guard.

It does **not** yet measure:

- simplicity of the surviving perceived world;
- unnecessary narrative switching;
- impairment detectability among several still-credible continuations;
- transition cost after deaths/role changes/public claims.

Those require the shared derived narrative projector, not additional thresholds inside the policy evaluator.
