# DLB Drunk-Assignment Production Cutover Gate Audit — 2026-09-30

> Status: **COMPLETE / GATE NOT PASSED — ORIGINAL PRE-TBGS-1 AUDIT**
> Current-status successor: `DLB_DRUNK_ASSIGNMENT_POST_TBGS1_CUTOVER_RECHECK_2026-09-30.md`
> Baseline: `main@19fc0c03e9ebf8444d918d9fa2289985e04007f3`
> Scope: Trouble Brewing Drunk-assignment shadow -> Beginner automatic production authority only.
> Production code changed by this audit: **none**.

## 1. Decision

The current Drunk-assignment surface is ready to remain in shadow/assisted use, but it is **not ready to become Beginner automatic production authority**.

The gate fails for policy/evidence reasons rather than for legality, setup-commit, or replay-infrastructure reasons.

Do not:

- feed a Drunk recommendation into the Beginner router;
- mutate frozen `BEGINNER_CONSERVATIVE_V1`;
- reinterpret `DRUNK_ASSIGNMENT_SHADOW_V1` as a production policy;
- invent weights, thresholds, candidate tiers, or deterministic ranking from the current evidence;
- begin DLB-6 retirement of the transitional compatibility selection while Beginner still depends on it.

The existing Beginner `CompatibilityImmediate` route remains the explicit playable fallback until a later cutover gate passes.

## 2. Gate result

| Gate condition | Result | Current evidence |
| --- | --- | --- |
| 1. Stable rules-owned legal candidate domain | **PASS** | `TroubleBrewingDrunkCandidateDomain` owns the exact legal domain; DLB-2 and later router/commit tests consume rather than regenerate it. |
| 2. Stable hypothetical consequence projection | **PASS, with declared limitations** | `TroubleBrewingDrunkHypotheticalProjector`, DLB-3B consequence envelopes, and dedicated Drunk-assignment features provide immutable candidate projections. Longitudinal narrative remains explicitly unavailable rather than silently zeroed. |
| 3. DecisionTrace/replay coverage over real/reconstructable evidence | **PASS for the replay contract** | DecisionTrace + dedicated versioned Drunk shadow replay are implemented. EvidenceLab C1 has 3 PREFIX_RECONSTRUCTABLE assignment cases; G10 Game 2 crossed into Host replay and is accepted by `DrunkAssignmentEvidenceReplayTest`. This proves the replay boundary, not a policy winner. |
| 4. Explicit fallback behavior | **PASS** | `TroubleBrewingDrunkSelectionRouter` uses `CompatibilityImmediate` for Beginner, revalidates the compatibility candidate against the current legal domain, and rejects any Beginner recommendation before cutover. |
| 5. Evidence sufficient for every ordering/preference consumed | **FAIL** | Current evidence supports descriptive/contextual feature families and several expert rationales, but not a general legal-candidate ordering. Historical G05/G10 have explicit reasons but no observed alternative-candidate comparison; G01 assignment rationale is unknown. Creator guidance explicitly permits player-first, role-first, or whole-setup-first assignment. |
| 6. Explicitly versioned production Drunk-assignment selection contract | **FAIL** | `DRUNK_ASSIGNMENT_SHADOW_V1` is versioned but intentionally deferral-only. It always emits no selection and includes `ORDERING_EVIDENCE_NOT_AUTHORIZED`; it is not a production selection contract. |
| 7. Affected T2/T4 + independent remote acceptance for production cutover | **NOT REACHED** | Existing DLB shadow/replay/setup surfaces have remote acceptance, but there is intentionally no production cutover implementation to validate while conditions 5–6 fail. |

Overall result:

```text
1 PASS
2 PASS with explicit unavailable capability
3 PASS for replay contract
4 PASS
5 FAIL
6 FAIL
7 BLOCKED / not reached
-------------------------
production cutover = NOT AUTHORIZED
```

## 3. Why current evidence does not authorize ordering

ClocktowerEvidenceLab C1 is complete for its stated purpose:

- 3 / 3 reconstructable Drunk-assignment cases;
- 2 historical cases with explicit assignment rationale;
- at least one case replayed through Host;
- creator / experienced-Storyteller guidance describing assignment context.

That is enough to validate the **decision surface and evidence handoff**.

It is not equivalent to evidence for a generic production ranking.

### 3.1 Historical cases

EvidenceLab C1C records:

- G01: Empath chosen as Drunk; assignment rationale UNKNOWN.
- G05: Chef chosen as Drunk to support an extreme Chef misinformation narrative; no explicit alternative Drunk candidates observed.
- G10 Game 2: Empath chosen as Drunk because the Empath is adjacent to the Demon; no explicit alternative Drunk candidates observed.

These show legitimate reasons that can matter. They do not establish how to rank all current legal candidates when those reasons conflict or are absent.

### 3.2 Creator-level guidance broadens rather than narrows the input contract

The human-reviewed Steven Medway assignment window supports three legitimate approaches:

1. player-first;
2. role-first;
3. whole-setup-first.

EvidenceLab also retains experienced-Storyteller guidance where the same seating geometry may lead to a different Drunk assignment depending on player experience.

Therefore a production contract that ranks only the current topology / first-night role features would silently discard an evidenced assignment dimension.

The correct response is not to invent a numeric proxy. A future production policy must either:

- receive the relevant context and state how it is used; or
- explicitly declare that context optional/unsupported and defer/fallback when the policy cannot justify a choice.

## 4. Current feature/capability blockers

### 4.1 Longitudinal narrative capability is explicitly missing

`DrunkAssignmentFeaturesProjector` always projects:

`longitudinalNarrativeOpportunity = Unavailable(MISSING_CAPABILITY)`.

`DRUNK_ASSIGNMENT_SHADOW_V1` checks that condition and records:

`LONGITUDINAL_NARRATIVE_MISSING_CAPABILITY`.

This is correct fail-closed behavior. Do not convert the absence into a neutral score.

### 4.2 Player-level assignment context is not represented

A producer/consumer search of the Clocktower Drunk-assignment implementation finds no player-experience / games-played input on the current feature surface.

That does not require a broad player-profile subsystem before any future Drunk policy can exist. It does require the eventual versioned selection contract to state clearly whether player context is:

- required;
- optional enrichment;
- or unsupported with explicit deferral/fallback semantics.

Do not silently treat missing player context as evidence that all players are equivalent.

## 5. Existing fallback must remain until replacement authority exists

The current Beginner route is intentionally transitional:

```text
prepared setup
+ compatibilityConfirmedDrunkCandidate
+ no recommendation
-> revalidate against current legal domain
-> CompatibilityImmediate
-> shared canonical DLB-4 commit
```

This is not an SDE recommendation.

Because Beginner still depends on this fallback, DLB-6 must **not yet** remove:

- the compatibility-confirmed candidate producer;
- the old compatibility deal-plan data needed solely to construct that candidate;
- any persistence/history contract still required to preserve current playable behavior.

DLB-6 may begin only after a replacement selection authority/fallback contract is explicit and production callers are cut over.

## 6. What would make a later cutover auditable

Do not create a placeholder production policy.

Before re-running this gate, provide all of the following:

### 6.1 Policy-neutral capability contract

Define the exact Drunk recommendation request context needed by the proposed policy.

At minimum, explicitly disposition:

- current topology features;
- first-night impaired-information opportunity;
- longitudinal/history-aware narrative opportunity;
- player-level context such as experience, if the proposed policy intends to use it.

Missing optional context must stay typed as unavailable/unknown rather than defaulting to a score.

### 6.2 Evidence for the actual preference semantics

Evidence collection should target the missing shape rather than increase raw game count:

- explicit comparison between multiple plausible Drunk candidates; or
- explicit rejection/acceptance rationale that can justify one bounded ordering predicate;
- rationale tied to the context available at assignment time;
- provenance strong enough to distinguish expert guidance from community speculation.

EvidenceLab C1 explicitly permits reopening targeted acquisition when downstream replay exposes a concrete missing evidence field. This audit identifies that field: **candidate-comparison / ordering evidence**, not more result-only Drunk games.

### 6.3 New versioned selection contract

Only after evidence supports a concrete predicate, define a new production-capable Drunk-assignment policy version.

It must not mutate `BEGINNER_CONSERVATIVE_V1` and must not rename `DRUNK_ASSIGNMENT_SHADOW_V1` into production authority.

The version must specify:

- legal-domain input identity;
- required vs optional context;
- deferral conditions;
- fallback behavior;
- candidate equivalence/tie semantics;
- deterministic selection method only after survivor/equivalence semantics are evidence-authorized.

### 6.4 Replay before cutover

Replay the new version against available reconstructable assignment cases while preserving actual historical choice as evidence, not a correctness label.

A mismatch with an expert historical choice is diagnostic evidence to inspect, not automatic proof that either side is wrong.

### 6.5 Production acceptance

Only then add the Beginner cutover wiring and run the affected focused/T2 tests plus a T4 exact-head acceptance and independent R2 audit.

## 7. Immediate route after this audit

This audit closes the current gate check, but it does **not** unblock automatic authority.

```text
DLB-5H1 COMPLETE / ACCEPTED
-> Drunk-assignment production cutover gate audit COMPLETE / NOT PASSED
-> Drunk recommendation-context capability contract COMPLETE / POLICY-NEUTRAL
-> TBGS-0 canonical TB snapshot contract
-> TBGS-1 Drunk snapshot vertical slice / EvidenceLab interoperability
|| EvidenceLab targeted candidate-comparison / rejection evidence continues
-> new versioned Drunk selection contract only when evidence permits, consuming snapshot + typed request
-> shadow replay of that contract
-> re-run production cutover gate
-> Beginner automatic authority only if PASS
-> DLB-6 old-contract retirement only after replacement selection/fallback authority exists
-> DLB-7 acceptance
-> TBGS-2 incremental runtime recommendation-state migration
```

TBGS-0/1 are architecture/read-boundary work only and do not satisfy the missing ordering-evidence gate by themselves. Authority: `docs/TB_CANONICAL_GAME_SNAPSHOT_INTEGRATION_ROUTE_2026-09-30.md`.

C5 / `BEGINNER_CONSERVATIVE_V2` remains a separate evidence gate and must not be conflated with this Drunk-assignment-specific route.
