# SDE-3B6 — Expert-Informed V1 Soft-Priority Completion Audit

> Date: 2026-09-24 Australia/Sydney  
> Branch: `sde-3b-beginner-conservative-v1`  
> PR: **#153 — draft**  
> Status: **COMPLETE — NO NEW SOFT ORDERING AUTHORIZED AT CURRENT EVIDENCE CHECKPOINT**

## 1. Completion conclusion

SDE-3B6 is complete for the current evidence checkpoint.

The evidence-eligibility audit found no completed feature family with a sufficiently precise E3 bridge to authorize a new qualitative candidate ordering or rejection reason. That result is intentional and is now represented explicitly in the V1 policy contract rather than hidden behind a score, weight, threshold, or undocumented tie-break.

`BEGINNER_CONSERVATIVE_V1` therefore remains:

`legal candidates -> exact no-credible-Evil-world contradiction gate -> survivor equivalence band -> seeded survivor-only selection`

No new soft priority was added.

## 2. Accepted policy change

`BeginnerConservativeV1PolicyLimitations.PREFERENCE_EVIDENCE_NOT_AUTHORIZED` is present on Ready policy evaluations while the current evidence checkpoint does not authorize an additional preference rule.

This limitation is distinct from:

`PREFERENCE_DIMENSIONS_NOT_PROJECTED`

The distinction is important:

- a dimension may be missing from the current decision surface;
- a dimension may be projected correctly but still lack enough E3 policy evidence.

Neither state is treated as neutral evidence and neither state changes candidate ordering.

## 3. Regression guarantees

The accepted policy tests preserve all prior V1 semantics:

- viable candidates remain tied when no authorized preference distinguishes them;
- exact zero Evil-topology retention remains the only generic hard rejection;
- non-zero topology retention is never rejected through an invented threshold;
- all-contradictory candidate sets still defer rather than fabricating a survivor;
- no new `PolicyReasonCode` was introduced;
- seeded survivor selection remains unchanged;
- no visible recommendation or canonical commit path changed.

## 4. Evidence decision

The following completed feature families remain diagnostic for V1 ordering at this checkpoint:

- confirmation chain;
- impaired narrative;
- healthy-information utility;
- role-function exposure.

Their semantic projectors are accepted, but their proposed soft-priority predicates do not yet have a sufficiently direct E3 mapping.

Additional dimensions remain ineligible because their production projectors are not complete, including truth-danger / credibility-disruption, bluff narrative, future flexibility, and contextual Red Herring consumption inside SDE.

Role-function exposure specifically remains diagnostic because targeted Gap C still lacks qualifying severity rationale or considered-alternative evidence.

## 5. Acceptance evidence

Current accepted branch head before this completion document:

`0ab8053c01816b838479068cd224652d46163614`

Remote acceptance:

- CI #3423 / run `35938740056`: success;
- R2 #3179 / run `35938740025`: success;
- PR #153 remained open, draft, and mergeable.

The current head includes the typed limitation contract and the policy regression assertions that preserve survivor ties and the existing zero-topology gate.

## 6. Architecture invariants

SDE-3B6 introduced:

- no global score;
- no probability weight;
- no unsupported threshold;
- no named-role preference;
- no new rejection reason;
- no DecisionTrace persistence;
- no production recommendation cutover.

Evidence uncertainty remains explicit rather than encoded as fake precision.

## 7. Next boundary — overall SDE-3B T4 acceptance

SDE-3B1 through SDE-3B6 are complete for the current engineering/evidence checkpoint.

The next step is the overall SDE-3B T4 acceptance checkpoint:

1. synchronize roadmap/handoff to mark 3B6 complete;
2. run the repository's `[full-ci]` acceptance path;
3. audit the resulting full Android / required subsystem checks plus R2;
4. keep PR #153 draft after T4;
5. do not merge or mark ready without explicit project-owner authorization.

After successful T4 acceptance, the next engineering phase is SDE-3C DecisionTrace / replay.
