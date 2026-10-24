# SDE-3B6 — Expert-Informed V1 Soft-Priority Eligibility Audit

> Date: 2026-09-24 Australia/Sydney  
> Branch: `sde-3b-beginner-conservative-v1`  
> PR: **#153 — draft**  
> Status: **EVIDENCE / POLICY PRE-FLIGHT COMPLETE — NO NEW SOFT ORDERING AUTHORIZED**

## 1. Audit conclusion

SDE-3B6 must not add a qualitative preference merely because a feature projector now exists.

Applying the E1/E2/E3/E4 authority model to every currently relevant V1 feature family produces a negative but important result:

**no new candidate-ordering or rejection reason is currently authorized by the evidence.**

BEGINNER_CONSERVATIVE_V1 therefore remains:

`legal candidates -> exact no-credible-Evil-world contradiction gate -> survivor equivalence band -> seeded tie-breaking`

This is an evidence decision, not an implementation failure.

The policy should now distinguish two different limitations:

1. a preference dimension is not projected/captured on the current decision surface;
2. a feature may exist and be semantically valid, but the evidence does not yet authorize a V1 qualitative preference from it.

The second state should be explicit in the typed policy result. It must not be represented as a fake score, a hidden tie-break, or a candidate rejection.

## 2. Eligibility matrix

| Feature family | Projector / semantic status | E3 status | 3B6 policy authority |
| --- | --- | --- | --- |
| Strategic Evil topology | accepted exact consequence feature | exact-zero structural contradiction does not require calibrated strength | existing `no-credible-evil-world` gate remains authorized |
| Confirmation chain | 3B2 complete; bounded R02/R04 E2 | no explicit qualified rationale recovered for the generic candidate-ordering predicate “avoid this destructive chain when an alternative exists” | **NOT AUTHORIZED** |
| Impaired narrative | 3B3 complete; R04/R06 E2 | Ben gives explicit believability rationale for individual impaired outputs, but that rationale does not establish the current projector's cross-history predicate “avoid an avoidable prior-narrative break”; Gap B still requests independent/cross-night rationale | **NOT AUTHORIZED** |
| Healthy-information utility | 3B4 complete; R01/R04 E2 | Gap A explicitly remains under-evidenced for the healthy floor / last-route policy boundary and requests an independent GOLD rationale case | **NOT AUTHORIZED** |
| Role-function exposure | 3B5 complete; `goldcand-ben-03` bounded E2 | Gap C explicitly lacks severity rationale / considered alternatives; silent observed choice is not preference evidence | **NOT AUTHORIZED** |
| Truth danger / credibility disruption | design/evidence dimension exists | Evin/Ben evidence motivates the dimension, but the current `truthCredibility` production feature remains unavailable | **NOT ELIGIBLE — projector missing** |
| Bluff narrative / route diversity | unavailable | triplet variation is not preference ordering; Gap D remains open | **NOT ELIGIBLE** |
| Candidate relationships / future flexibility | unavailable | no stable matching E3 policy contract | **NOT ELIGIBLE** |
| Red Herring contextual policy | external setup owner exists, contextual SDE feature projector not yet present | cross-expert contextual rationale exists but cannot be consumed through a stable matching feature | **NOT ELIGIBLE** |

## 3. Why impaired believability does not authorize the current narrative predicate

The strongest tempting shortcut is the impaired-information evidence.

Ben's primary rationale includes:

- A Stud: Drunk Empath 0 because 2 would be “a little unbelievable”;
- A Fond Farewell: Drunk Chef 4 as “a somewhat believable number”.

Those are genuine expert qualitative signals.

However, `ImpairedNarrativeFeatures` currently distinguishes:

- no prior impaired narrative;
- compatibility with prior impaired observations;
- an avoidable or forced break from prior impaired observations;
- an already-infeasible prior narrative.

It does **not** model first-interaction plausibility or “believable number” quality.

Using Ben's first-interaction rationale to prefer `COMPATIBLE_WITH_PRIOR_IMPAIRED_NARRATIVE` over `BREAKS_PRIOR_IMPAIRED_NARRATIVE` would therefore cross an evidence/feature semantic boundary. That would be a policy inference, not a supported mapping.

Gap B correctly keeps the highest-value missing evidence as recurring/cross-night impaired information with explicit continuity rationale.

## 4. Why exact last-route removal is still not an automatic rejection

`HealthyInformationUtilityFeatures.removesLastUsableHealthyRoute` is an exact categorical fact.

It is not automatically a universal badness label.

The table may still have:

- other not-yet-delivered information;
- public/social information outside this route model;
- future abilities;
- a game-state reason why the current route loss is acceptable or forced.

Gap A explicitly asks for an experienced Storyteller rationale rejecting an alternative because it leaves too little healthy information. Until that E3 bridge exists, the projector remains diagnostic.

## 5. Role-function exposure remains diagnostic

3B5 proves that registration-ambiguity exposure can be:

- direct;
- already exposed or new;
- confirmation-amplified;
- forced or avoidable.

It does not prove policy severity.

The `goldcand-ben-03` observed Librarian clue is useful E2 evidence because the actual Recluse is directly identified and later still has interaction-scoped registration relevance. The source does not provide choice-specific rationale establishing that direct exposure should be avoided.

Therefore no `obvious-role-function-exposure` policy reason is authorized in 3B6.

## 6. Required minimal production change

The V1 policy result should explicitly report:

`preference-evidence-not-authorized`

for a Ready evaluation while this policy version intentionally retains survivors tied because no additional soft preference has passed the E3 gate.

This is a **policy limitation**, not:

- a rejection reason;
- a score;
- a weight;
- a threshold;
- an ordering;
- a new selection rule.

Existing `preference-dimensions-not-projected` remains separately useful when the current decision surface still lacks feature projections.

Both limitations may be present simultaneously.

## 7. Tests-first acceptance contract

The first 3B6 implementation slice should prove:

- a fully viable candidate set remains tied exactly as before;
- `preference-evidence-not-authorized` is present on Ready V1 results;
- existing `preference-dimensions-not-projected` remains present when applicable;
- no new `PolicyReasonCode` is introduced;
- zero-Evil-topology behavior is unchanged;
- seeded survivor selection is unchanged.

No visible recommendation or canonical commit changes.

## 8. Completion / next boundary

Once the typed limitation is accepted:

- SDE-3B6 is complete for the current evidence checkpoint;
- no further soft-priority implementation should be invented inside SDE-3B;
- overall SDE-3B may proceed to its T4 acceptance checkpoint;
- evidence acquisition continues externally for Gap A/B/C/D/E;
- future evidence can justify a new versioned policy revision rather than silently mutating the accepted semantics;
- SDE-3C remains the next engineering phase for DecisionTrace / replay.

PR #153 must remain draft until explicit project-owner authorization.
