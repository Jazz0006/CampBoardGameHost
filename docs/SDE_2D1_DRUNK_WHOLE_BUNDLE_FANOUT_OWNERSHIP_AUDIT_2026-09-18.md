# SDE-2D1 Drunk Whole-Bundle Fanout / Ownership Audit

> Date: 2026-09-18 Australia/Sydney  
> Branch: `sde-2d1-drunk-whole-bundle`  
> Base: `5e209b00df4ba2b83a0ae15d726d982a345f3b1d`  
> Status: **AUDIT COMPLETE — implementation not yet cut over**

## 1. Scope

This audit implements the focused pre-executable gate required by:

- `AGENTS.md`;
- `docs/TESTING_STRATEGY.md`;
- `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
- `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
- `docs/SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`.

The target is **SDE-2D1 only**:

- Drunk shown-role identity remains setup/session-owned persistent truth;
- an unshown Drunk clue becomes a first-class planned whole-bundle Storyteller choice;
- candidate legality remains rules/domain-owned;
- exact consequence remains epistemic-owned;
- public-claim semantics become impairment-capable without leaking actual Drunk/Poisoner state;
- evaluation exposes `HealthyCore`, `FullBundle`, and `DrunkMarginal`;
- no production recommendation cutover is part of this slice.

SDE-2D2 Demon bluff migration, SDE-2D3 strategic quotient extraction, and 5–15 player performance work remain out of scope.

## 2. Live-state baseline

Live `main` was re-queried before this audit.

Current head:

`5e209b00df4ba2b83a0ae15d726d982a345f3b1d` — `docs: clarify completed SDE-2 foundation`

The commits after PR #144 are documentation-only. No new executable code has superseded the PR #144 implementation baseline.

The current `main` head has no associated workflow run because it is documentation-only. The last executable SDE foundation remains the validated PR #144 line recorded in the roadmap/handoff.

## 3. Frozen ownership result

The central ownership decision is:

```text
setup/session
    owns actual Drunk identity + persistent shown role

rules / legal candidate domains
    own every surface-valid clue allowed by the shown ability

SDE whole-bundle composer
    owns which still-uncommitted clue candidate is being evaluated

epistemic exact layer
    owns whether each candidate bundle is mechanically/epistemically supportable

session semantic history
    owns the clue only after it is actually shown/committed
```

SDE must not:

- reselect the Drunk shown role;
- create a second Drunk-specific rules engine;
- copy persistent shown-role truth into a new recommendation state store;
- mutate committed observations while replanning;
- collapse unreliable information to “always false” or “discounted healthy information”.

## 4. Fanout map

| Surface | Current owner / behavior | SDE-2D1 classification |
| --- | --- | --- |
| actual role + shown role persistence | `ClocktowerGameSession` / `PlayerState` / committed setup | **INHERIT** |
| Drunk planned-freshness invalidation | existing `gameStateRevision` + `playerInputRevision` / `PlannedDecisionRef` | **INHERIT** |
| committed Drunk clue immutability | session epistemic observation log | **INHERIT** |
| shown ability resolution | `AbilityFunctioningSemantics.perceivedRole` | **INHERIT** |
| pair display shape | `PairInformationDisplaySemantics` | **INHERIT** |
| pair legal candidate domain | `PairInformationLegalDomain` | **INHERIT** |
| pair truthful registration witnesses | natural pair generator + registration domain | **INHERIT; do not duplicate** |
| numeric healthy truth set | `FirstNightNumericInformationSemantics` | **INHERIT** |
| numeric unreliable full-range generation | `DynamicCandidateGenerator.generateNumeric` once bounds are supplied | **INHERIT** |
| numeric surface-valid bounds | currently not exposed as one clean rules-domain API; Chef bounds leak through Host preparation | **GENERALIZE BEFORE NUMERIC D1 SUPPORT** |
| Fortune Teller legal target pairs | `FortuneTellerInformationSemantics.legalTargetPairs` | **INHERIT** |
| Fortune Teller healthy result | `FortuneTellerInformationSemantics.healthyResult` | **INHERIT** |
| Fortune Teller unreliable Yes/No domain | structured Boolean + dynamic categorical generation | **INHERIT** |
| first-night proposition materialization | `TroubleBrewingFirstNightInformationPropositionMaterializer` supports pair/Chef/Empath | **GENERALIZE for FT Boolean materialization** |
| bundle candidate-space audit | `TroubleBrewingFirstNightBundleCandidateSpaceAuditor` unconditionally defers Drunk | **CHANGE** |
| healthy whole-bundle harness | rejects `shownRole != actualRole` | **GENERALIZE behind compatibility wrapper** |
| public claim projection | healthy-only: evil OR (shown-role + truthful clue) | **CHANGE** |
| exact public-claim prefilter | recognizes only healthy two-branch claim envelope | **CHANGE** |
| exact world shown-role state | only guarantees the recipient Drunk's shown role; another-seat Drunk may have no shown role in the world | **CHANGE / exactness prerequisite** |
| structured SDE shadow | evaluates already-legal typed candidates, does not compose whole bundles | **INHERIT, not whole-bundle owner** |
| first-night migration | publication/display lifecycle compatibility | **EXEMPT from bundle ownership** |
| production Host/UI recommendation | existing behavior | **NO CUTOVER in SDE-2D1** |

## 5. Existing legality already solves most Drunk candidate generation

### 5.1 Pair roles

`PairInformationLegalDomain` already has the exact contract SDE-2D1 needs.

For `ReliabilityState.DRUNK` it exposes the complete legal display space defined by `PairInformationDisplaySemantics`, including:

- mechanically false outputs;
- mechanically true outputs;
- accidentally true outputs;
- registration-truth alternatives where applicable.

Therefore SDE-2D1 must **not** generate Drunk Washerwoman/Librarian/Investigator candidates by calling the healthy generator directly and then inventing false variants.

The correct path is:

```text
persistent shown role
    -> ability role
    -> PairInformationLegalDomain(..., DRUNK)
    -> all surface-valid pair candidates
```

The current bundle auditor violates this ownership because it filters by `actualRole` and globally marks Drunk deferred.

### 5.2 Numeric roles

`FirstNightNumericInformationSemantics` already resolves healthy truth using the perceived role, so a Drunk shown as Chef/Empath reaches the correct shown-ability truth semantics.

`DynamicCandidateGenerator.generateNumeric` already enumerates every integer in the supplied legal range and correctly distinguishes functioning versus Drunk/Poisoned candidate families.

The remaining ownership gap is **the surface-valid numeric domain itself**. The bundle layer must not copy Host/UI range constants. Before numeric Drunk whole-bundle support, expose a rules-owned first-night numeric display domain, for example through `FirstNightNumericInformationSemantics`, and have both bundle/SDE and Host adapters consume that domain.

### 5.3 Fortune Teller

The underlying capability also already exists:

- legal player target pairs: `FortuneTellerInformationSemantics.legalTargetPairs`;
- healthy truth: `FortuneTellerInformationSemantics.healthyResult`;
- unreliable result domain: typed Yes/No structured Boolean generation.

The target pair is **player-controlled**, not a Storyteller whole-bundle choice. SDE must evaluate it as a robustness case, while the Red Herring remains the Storyteller-controlled factor.

Therefore Fortune Teller should not be modeled as one giant Storyteller Cartesian factor of target-pair × result.

## 6. Current blockers in the old bundle harness

### 6.1 Candidate-space auditor

`TroubleBrewingFirstNightBundleCandidateSpaceAuditor` currently:

- builds pair/numeric factors from the source's `actualRole`;
- excludes poisoned sources;
- adds `FirstNightBundleDeferredComplexity.DRUNK` whenever any Drunk is in play.

That means a Drunk shown Washerwoman/Investigator/Chef/etc. never becomes an information factor.

SDE-2D1 must change this from an identity-based exclusion to capability-based support:

```text
actual Drunk
+ persistent shown role is a supported information ability
    => supported unreliable factor

actual Drunk
+ shown role outside currently supported D1 capability
    => explicit unsupported/deferred capability
```

Do not retain one global “Drunk means unknown whole-bundle count” switch.

### 6.2 Healthy harness

`TroubleBrewingFirstNightHealthyBundleHarness` currently requires:

```text
shownRole == actualRole
```

for every player.

That requirement is not a rules invariant; it is an SDE-0 staging boundary.

Do not silently mutate a class named “Healthy” into the new mixed-ability authority. Introduce a generalized first-night whole-bundle harness/composer and keep the existing healthy harness as a compatibility/regression wrapper.

The generalized path should be the future owner of factor composition. The old healthy harness should prove unchanged healthy behavior, not own Drunk-specific branches.

## 7. Public-claim semantics must become impairment-capable for every information claim

The current `FirstNightPublicGoodInfoProjection` emits:

```text
speaker evil
OR
(shown role matches AND clue true)
```

That was only valid because Drunk/Poisoner counterworlds were staged out.

Once malfunctioning counterworlds are allowed, **every** claimed information-role observation must use the impairment-capable interpretation, not only entries that happen to come from the canonical Drunk setup:

```text
speaker evil
OR
(
    shown role matches
    AND source ability is malfunctioning
)
OR
(
    shown role matches
    AND source ability is functioning
    AND clue is mechanically true
)
```

This is important because a canonically healthy Chef's public claim may still be explained, from another player's perspective, by a counterworld where that speaker is the Drunk shown Chef.

The projection must not add “actual speaker is Drunk” as a public fact. Hidden actual identity and hidden poison state remain latent world explanations.

The existing proposition model already has the required atom:

`InformationProposition.AbilityStateAt`

with `FUNCTIONING`, `MALFUNCTIONING_DRUNK`, and `MALFUNCTIONING_POISONED` represented in exact worlds.

## 8. Exactness prerequisite: hidden shown role for a non-recipient Drunk

This is the most important newly discovered fanout.

`TroubleBrewingWorldEnumerator` currently constructs shown-role state as follows:

- every non-Drunk has shown role == actual role;
- when the **knowledge recipient** is the Drunk, the recipient's perceived role is recorded as the Drunk shown role;
- when **another seat** is the Drunk, that seat may have no entry in `shownRolesBySeat`.

That was sufficient while Drunk public-claim worlds were excluded. It is insufficient for SDE-2D1.

Example:

```text
recipient seat 1 hears seat 4 claim:
"I am Chef; I saw 0."

counterworld:
seat 4 is actually Drunk, shown Chef
```

The public-claim proposition must be able to prove that the shown role really is Chef. A world that stores only “seat 4 = Drunk” but no hidden shown role cannot evaluate `ShownRoleAt(4, Chef)` exactly.

### Required ownership

Do not fix this by:

- leaking canonical setup `shownRole` to every player's knowledge snapshot;
- weakening `ShownRoleAt` into “could have been shown”;
- special-casing public claims with a recommendation-owned Drunk identity rule.

The exact epistemic world layer must own the latent shown-role state.

For the initial exact implementation, a Drunk at a non-recipient seat should enumerate the mechanically legal hidden Townsfolk shown-role alternatives; a recipient Drunk remains pinned to the recipient's actual perceived role. `EnumeratedWorldMechanicalIdentity` already includes `shownRolesBySeat`, so the data model is prepared for this distinction.

This is a real exact-world semantic expansion and may change raw mechanical world cardinalities. That is acceptable as correctness work; raw role-world multiplicity is already demoted to secondary evidence by SDE-2D.

Because this touches exact-world representation, affected validation must include the epistemic representation/oracle family required by `TESTING_STRATEGY.md`, not only recommendation tests.

## 9. Exact-evaluator fanout

### 9.1 Public-claim prefilter

`ExactHistoricalHypotheticalObservationBundleEvaluator.healthyPublicClaimShownRole(...)` recognizes only the current healthy two-branch envelope.

After public-claim generalization, leaving the optimizer unchanged would lose the shown-role necessary prefilter for impairment-capable claims.

Generalize this helper into a shape-independent “public claim shown-role envelope” recognizer. It must remain a necessary-only optimization; exact proposition evaluation remains authoritative.

### 9.2 FUNCTIONING_ONLY hypothesis

Public claims are `NOT_ABILITY_INFORMATION`, so the top-level current evaluator does not automatically reject malfunction branches under `EpistemicHypothesis.FUNCTIONING_ONLY`.

When the new proposition explicitly contains non-functioning `AbilityStateAt` branches, recursive exact evaluation must preserve the hypothesis contract:

- `MECHANICALLY_CREDIBLE`: malfunction branch may satisfy the public claim;
- `FUNCTIONING_ONLY`: non-functioning explanation branch must not satisfy it.

Do not encode this policy in the public-claim projector; hypothesis interpretation remains exact-evaluator ownership.

## 10. Registration interaction

SDE-2B already established:

- `TroubleBrewingRegistrationDomain` as legality owner;
- complete successful registration witness alternatives in exact evaluation;
- optional selected `ExactRegistrationWitnessBinding`;
- `PairInformationExactConsequenceAdapter` as a pure adapter for already-legal truthful pair candidates.

SDE-2D1 must inherit this rather than reopen registration semantics.

For the first Drunk whole-bundle fixture, it is acceptable to avoid Spy/Recluse so Drunk semantics can reach GREEN independently. The existing explicit registration deferred boundary can then be removed in a separate composition step using the already-built SDE-2B witness contract.

Do not make Drunk support depend on a second registration implementation.


## 10A. Global malfunction explanations are mechanically scarce world resources

Drunk/Poison explanations must not be evaluated as observation-local existential escape hatches.

Every claimed clue in a bundle is interpreted inside the **same mechanically legal world**. Therefore identity/effect resources compete globally:

- if a world contains the Drunk, there is exactly one Drunk identity in that world;
- two independent claims cannot both consume "the speaker is the Drunk" unless they refer to that same Drunk seat;
- Poisoner poisoning is sourced from an in-play, functioning Poisoner and is constrained by that role's legal target capacity for the relevant night;
- if setup/world constraints establish that the only Minion is Baron, the same world cannot also contain Poisoner, so Poisoner-origin malfunction explanations disappear;
- setup-profile, role-count, identity, registration, and impairment explanations must remain jointly satisfiable in one world.

This is an acceptance invariant, not a scoring heuristic:

```text
all bundle observations
    -> one shared mechanically legal world
    -> finite identity/effect resources are consumed consistently
```

Do not optimize the exact layer by independently asking whether each observation has *some* Drunk/Poison explanation and then combining those answers. That would admit impossible bundles.

## 10B. Future-proof poisoning ownership: source-local effects, not a global one-poisoned-player invariant

Trouble Brewing currently permits a compact representation because Poisoner is the only ordinary poison source in scope. That must **not** harden into a cross-script invariant such as:

```text
world.poisonedSeat: Int?
```

or:

```text
at most one poisoned player globally
```

Future scripts can contain multiple impairment sources with different lifecycles. For example, a Poisoner-origin poison and a Pukka-origin poison may coexist, while each source still obeys its own target/timeline rules.

The durable ownership direction is:

```text
mechanical source/effect state
    -> derives current AbilityState / functioning status
    -> recommendation/exact consumers ask whether the ability functions
```

A future generalized model may need source provenance and timeline, conceptually:

```text
ImpairmentEffect(
    targetSeat,
    sourceRole,
    sourceSeat,
    effectType,
    startPhase,
    endCondition,
)
```

SDE-2D1 does **not** implement that generalized effect system. It only freezes two constraints:

1. do not expose Trouble Brewing's current "single Poisoner target" representation as a universal API contract;
2. keep exact-world ownership capable of later distinguishing *why* a seat is malfunctioning without changing SDE policy semantics.

This preserves the current lightweight Trouble Brewing implementation while avoiding a future rewrite when roles such as Pukka introduce source-specific, cross-night impairment state.

## 11. HealthyCore / FullBundle / DrunkMarginal owner

The exact evaluator should continue to answer world consequences only.

The SDE whole-bundle layer should construct paired exact requests for each candidate:

```text
HealthyCore
    = projected public observations for the whole healthy core
      excluding the candidate Drunk clue

FullBundle
    = the same core
      + projected candidate Drunk clue
```

The existing exact diagnostics already expose structural sets including:

- possible Demon seats;
- evil-team seat configurations;
- forced-good seats;
- forced-evil seats;
- evil-cover seats.

Therefore SDE-2D1 does not need a second world solver to obtain a useful first `DrunkMarginal`.

The SDE diagnostic layer may compare:

```text
HealthyCore.afterStructure
vs
FullBundle.afterStructure
```

and expose set differences/retention as the initial marginal evidence.

Do **not** introduce an arbitrary scalar “Drunk weight”. SDE-2D3 will later extract/generalize strategic-world quotient ownership.

## 12. Proposed executable slices

### SDE-2D1A — exact/public-claim foundation

Durable changed contracts:

1. impairment-capable public-claim proposition;
2. mechanically credible Drunk false clue can explain a good shown-role claim;
3. functioning-good false clue still fails;
4. functioning-good true clue still passes;
5. evil speaker still passes;
6. `FUNCTIONING_ONLY` does not accept the malfunction explanation;
7. non-recipient Drunk worlds carry a legal hidden shown role consistently.

Existing staged test:

`FirstNightBundleExperimentExactFixtureTest.healthy public claim model does not silently treat Drunk false information as truthful exact evidence`

is a genuine future-contract placeholder and should become the RED/GREEN seam for the public-claim change.

### SDE-2D1B — pair whole-bundle

Use `PairInformationLegalDomain(..., DRUNK)`.

Change candidate-space audit so a supported Drunk shown pair role produces a real factor and no longer adds global `DRUNK` deferred complexity.

Add generalized whole-bundle evaluation returning per Drunk candidate:

- `HealthyCore`;
- `FullBundle`;
- `DrunkMarginal`.

Keep the existing healthy acceptance fixture unchanged through the compatibility wrapper.

### SDE-2D1C — numeric whole-bundle

First create/reuse one rules-owned legal numeric display domain.

Then support Drunk shown:

- Chef;
- Empath.

Materialization remains typed `NumericResult`.

### SDE-2D1D — Fortune Teller whole-bundle robustness

Add typed bundle materialization for `BooleanResult(Demon-or-Red-Herring)`.

Treat:

- Red Herring as Storyteller-controlled;
- target pair as player-controlled robustness input;
- Drunk result as complete Yes/No surface-valid domain.

Do not multiply target-pair choice into the Storyteller factor space.

## 13. Test / validation plan

Per `docs/TESTING_STRATEGY.md`:

### T0

Focused real RED/GREEN around:

- existing Drunk false-public-claim staged fixture;
- new non-recipient Drunk shown-role exact-world fixture;
- Drunk pair legal factor / whole-bundle fixture;
- later numeric and Fortune Teller capability fixtures.

### T1

`:app:testFast`

after each executable checkpoint that changes source.

### T2 affected

Because SDE-2D1A changes exact-world / epistemic semantics, affected validation must include the exact-world representation family. In particular, `ZddPlayerWorldSetTest` is an affected T2/T3-execution test for epistemic-world changes under the current testing strategy.

Any exact/oracle semantic change must also use the repository's selected exact/oracle cross-validation gate as required by the classifier/strategy.

### T3/T4

Do not run large calibration/performance corpora merely for D1 pair plumbing. Escalate full exact/oracle validation at the logical semantic checkpoint, and reserve SDE-2D4 performance evidence for its planned stage.

## 14. Files expected to change

Initial D1A/D1B expected fanout, subject to exact live re-check before edits:

### exact / epistemic

- `clocktower/epistemic/TroubleBrewingWorldEnumerator.kt`
- `clocktower/epistemic/EnumeratedWorldSet.kt` only if hypothesis-aware recursive proposition evaluation requires it
- `clocktower/epistemic/ExactHistoricalHypotheticalObservationBundleEvaluator.kt`

### first-night bundle

- `clocktower/recommendation/FirstNightBundleExperimentContract.kt`
- `clocktower/recommendation/FirstNightBundleCandidateSpaceAudit.kt`
- `clocktower/recommendation/FirstNightBundleHealthyHarness.kt` or a new generalized whole-bundle owner plus a compatibility wrapper
- focused tests under the same packages

### later D1C/D1D only

- numeric rules-domain API
- FT Boolean first-night materialization / bundle support

No Host UI, Compose, production selection cutover, or session persistence rewrite is required for the first semantic slices.

## 15. Stable decisions

1. **Drunk shown role remains PERSISTENT input. SDE selects only the unshown clue.**
2. **Pair Drunk legality already exists in `PairInformationLegalDomain`; do not create a new Drunk pair generator.**
3. **Numeric/Boolean dynamic generation already supports unreliable candidates; add only missing rules-owned surface-domain/materialization seams.**
4. **Public information claims become impairment-capable globally once malfunction counterworlds are admitted; do not tag canonical Drunk entries with a public or epistemic “Drunk” flag.**
5. **Exact epistemic worlds must represent a non-recipient Drunk's latent shown role; do not weaken `ShownRoleAt` or leak setup secrets into player knowledge.**
6. **`ExactHistoricalHypotheticalObservationBundleEvaluator` remains the consequence oracle.**
7. **SDE owns HealthyCore/FullBundle/DrunkMarginal comparison, not a second world solver.**
8. **Fortune Teller target pairs are robustness cases, not Storyteller-controlled bundle factors.**
9. **Keep the current healthy harness as regression evidence; introduce/generalize a whole-bundle owner instead of hiding mixed semantics under a “Healthy” class name.**
10. **No production cutover in SDE-2D1.**
11. **Malfunction explanations are shared-world resources: Drunk identity, Minion occupancy, Poisoner presence, and per-source target capacity must remain jointly legal in one exact world.**
12. **Do not freeze a global single-poisoned-seat invariant; future generalized impairment must preserve source provenance/timeline while `AbilityState` may remain a derived consumer-facing state.**

## 16. Next executable action

Start **SDE-2D1A** with the existing staged Drunk public-claim test as the real behavior-change RED, plus a focused non-recipient hidden shown-role exactness test.

Only after D1A exact/public-claim semantics are GREEN should the candidate-space auditor and generalized whole-bundle composer be wired to `PairInformationLegalDomain(..., DRUNK)` for D1B.
