# MS-S6D First-night Perceived-Ability Semantic Audit

> Date: 2026-09-01 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **S6D-0 COMPLETE — READ-ONLY AUDIT — S6D-1 RED NEXT**

## 1. Purpose and boundary

S6C remains complete and accepted at code/test checkpoint:

`38a04c1353c883c3bda4b4a506085c3c1d2766bd`

S6D does not reopen S6C. S6D closes a coverage gap discovered after acceptance: a committed Drunk shown identity can already participate in first-night waking and generic impairment selection, but the six Trouble Brewing first-night information roles do not yet share one complete perceived-ability semantic source before reliability is applied.

S6D-0 is audit only. No production or test code is changed by this checkpoint.

The target remains:

```text
committed perceived role
-> role semantic legal/truth space
-> RELIABLE / POISONED / DRUNK
-> shared impairment policy
-> deterministic recommendation
```

Healthy, Poisoned and Drunk must not own separate semantic implementations of the same perceived ability.

A3/A4 Possible Worlds `before -> observation -> after` quality evaluation is explicitly out of S6D. That remains future `ALG-B2R` after S7.

## 2. Global production finding

Current first-night production is a migration hybrid:

```text
perceived-role-aware actor resolution
-> role-local / legacy candidate construction
-> legacyInformationCandidates
-> unified first-night pool
-> RELIABLE / POISONED / DRUNK selection
-> structured display / observation
```

This means the waking/identity boundary is more advanced than the candidate-semantic boundary.

`ClocktowerHostScreen.roleActor()` uses `AbilityFunctioningSemantics.interactsAs(...)` during first night. `AbilityFunctioningSemantics.perceivedRole()` returns `shownRole` for an actual Drunk, and `stateFor()` independently returns `DRUNK`, `POISONED`, or `FUNCTIONING`. Therefore an actual Drunk shown as Empath/Investigator/etc. can be found and awakened as that perceived role.

The remaining problem is downstream: several candidate generators or truth-reference helpers still depend on raw actual identity, or use a different truth basis for unreliable actors than for healthy actors.

## 3. Six-role coverage matrix

| Role | Actor / perceived role | Healthy semantic truth owner | Unreliable candidate owner today | Registration semantics | Typed semantic coverage | S6D disposition |
|---|---|---|---|---|---|---|
| Washerwoman | perceived-role aware via `roleActor()` | Host-local `recommendedPairInformationOptions`; actual Townsfolk plus optional Spy registration | Host-local `recommendedUnreliablePairInformationOptions` | Spy may register as good/Townsfolk for healthy information | No S6C typed pair ability generator for Washerwoman | **GAP** — add to shared pair semantic family; registration-aware truth must precede impairment |
| Librarian | perceived-role aware | hybrid: typed natural Outsider candidates plus Host-local registration/display projection | Host-local unreliable pair enumeration | Spy registration can affect healthy result | Partial typed coverage exists, but first-night natural precompute selects actual Librarian only | **GAP** — perceived Drunk must reach same healthy semantic space; unreliable truth cannot fall back to raw actual identity |
| Investigator | perceived-role aware | typed natural Minion candidates including accepted Recluse registered-state truth, plus Host-local projection | legacy `recommendedDrunkInvestigatorOption` compatibility candidate plus Host-local unreliable pair enumeration | Recluse may register as Minion; S6C correctly preserves actual Spy truth and registered Recluse truth | Strong healthy typed coverage | **GAP** — impaired truth labeling/precompute must reuse healthy registration-aware space; legacy UI candidate cannot remain semantic authority |
| Chef | perceived-role aware | Host computes registration-aware `chefValue` | `recommendedNumberOptions` using `chefReferenceValue` | Spy/Recluse registration may change healthy evil-pair count | Generic typed numeric decision machinery exists, but it receives a caller-supplied scalar `trueValue` | **P0 GAP** — unreliable path explicitly switches to raw `chefActualIdentityValue`; true semantic value changes with reliability |
| Empath | perceived-role aware; neighbors/effective state already handled | Host computes registration-aware `empathValue` | `recommendedNumberOptions` using `empathReferenceValue` | Spy/Recluse registration may change healthy living-evil-neighbour count | Strong typed Foundation/UI adapter exists, but it receives caller-supplied scalar `trueValue` | **P0 GAP** — unreliable path explicitly switches to raw `empathActualIdentityValue`; same perceived ability therefore has two truth definitions |
| Fortune Teller | perceived-role aware | query-dependent `fortuneTellerMatched` over chosen two players, Demon/red-herring and current registration/effective-state inputs | `recommendedYesNoOptions` after the query result exists | Recluse interaction and red herring are separate semantic inputs; UI registration key is suppressed when information ability is unreliable | Structured boolean proposition/display exists; no complete generic perceived-ability semantic owner comparable to pair seam | **AUDIT/COMPLETE LATER IN S6D-5** — preserve query-dependent shape and existing current/effective-state authority; add only behavior-proven fixes |

## 4. Confirmed actor / identity behavior

The audit does **not** find a general first-night wake-up bug.

For first night, `roleActor(enName)` searches cards using `AbilityFunctioningSemantics.interactsAs(card.abilitySubject(...), enName)`. An actual Drunk therefore interacts as the committed `shownRole`.

`actorIsUnreliable()` maps the role actor through the same ability-state authority, so:

```text
actual healthy X       -> FUNCTIONING -> RELIABLE
actual poisoned X      -> POISONED    -> POISONED
actual Drunk shown X   -> DRUNK       -> DRUNK
```

The defect is not identity recognition. It is semantic candidate/truth ownership after identity recognition.

## 5. Confirmed pair-family defects

### 5.1 Typed precompute still filters by actual role

`SetupRecommendationModule.naturalPairCandidates()` delegates to `SetupCandidateGenerator.generatePairInformationCandidates(game)`.

That generator currently precomputes only players whose `actualRole` is Librarian or Investigator. Therefore:

```text
actual Librarian                 -> typed natural precompute reachable
actual Investigator              -> typed natural precompute reachable
actual Drunk shown Librarian     -> not reachable through that source
actual Drunk shown Investigator  -> not reachable through that source
```

This does not mean the Drunk is skipped by the UI; the UI actor is found correctly. It means the Drunk does not consume the same typed healthy semantic source before impairment.

S6D must not simply weaken the functioning-only invariant of `NaturalPairInformationCandidateGenerator.generate(...)`. The preferred architecture is to reuse the already-separated healthy semantic-space seam from a perceived-ability layer, then apply DRUNK/POISONED reliability independently.

### 5.2 Unreliable pair truth is raw-identity truth

`recommendedUnreliablePairInformationOptions(...)` enumerates legal role/pair display shapes, but marks a displayed role/pair truthful by checking actual role identity in the named seats.

That is not equivalent to the healthy registration-aware semantic space:

- healthy Investigator may truthfully use Recluse via registered Minion state;
- healthy Washerwoman/Librarian may depend on Spy registration;
- the same displayed clue can therefore be semantically truthful for the role while the current unreliable helper labels it false because no matching raw actual role is present.

Reliability must not redefine semantic truth. Registration-aware role truth must be determined first; impairment selects truthful vs false family second.

### 5.3 Legacy Drunk-Investigator UI projection remains

The Investigator first-night `displayOptions` still includes `recommendedDrunkInvestigatorOption(actor)` as a compatibility/UI candidate before generic unreliable pair options.

S6C correctly retired `DrunkInvestigatorInfo` from active setup recommendation ownership. S6D should preserve compatibility only as needed, but must not let this legacy projection remain the semantic authority for a new Drunk Investigator recommendation.

## 6. Confirmed numeric-family defect

Chef and Empath provide the clearest S6D correctness failure.

Healthy Chef/Empath compute a registration-aware value. For an unreliable actor, the Host currently selects a separate raw-actual-identity reference value:

```text
Chef:
  healthy      -> chefValue             (registration-aware)
  unreliable   -> chefActualIdentityValue

Empath:
  healthy      -> empathValue           (registration-aware)
  unreliable   -> empathActualIdentityValue
```

That violates the frozen layering:

```text
role semantic truth
-> reliability
```

because the truth definition itself changes after the actor becomes POISONED or DRUNK.

The generic numeric Foundation is not the source of this defect. `UnreliableNumberContext` and the coordinator accept a `trueValue` supplied by the caller and correctly build/rank numeric candidates around it. Existing structured Empath tests therefore prove Foundation/UI behavior but do not prove that the caller supplied the correct role-semantic truth.

## 7. Fortune Teller disposition

Fortune Teller is structurally different and should not be forced into a pair/numeric abstraction.

Its semantic question is chosen-player dependent. Current code computes `fortuneTellerMatched` after two targets are selected and protects first-night vs other-night Demon authority separately. Existing `ClocktowerFortuneTellerPhaseAuthorityTest` proves only that phase/effective-state boundary; it does not prove Drunk/Poisoned semantic equivalence.

`RegistrationInteractionRules.effectiveRegistrationKey(...)` suppresses interactive registration UI when the information ability is unreliable. That is a presentation/control boundary, not a complete semantic truth generator.

S6D-5 should therefore audit/complete Fortune Teller through a query-dependent boolean semantic seam while preserving:

- red-herring rules;
- current/effective Demon authority;
- legal Recluse registration behavior;
- selected two-player query identity;
- shared DRUNK/POISONED impairment after the truthful query result is known.

Do not mix this with S6D-1 pair/numeric REDs unless a test proves a common defect.

## 8. Existing tests: what is already protected

The audit found substantial infrastructure, but no existing test proves the missing end-to-end semantic contract.

### `NaturalPairInformationCandidateGeneratorTest`

Protects healthy Librarian/Investigator natural semantics, including:

- actual Spy/Minion truth remains actual-state truth;
- Recluse Investigator truth is registered-state truth;
- Recluse may register as an out-of-play script Minion;
- selected registered truth carries registration metadata;
- poisoned and Drunk sources do not enter the functioning `generate(...)` API.

This is correct S6C coverage and should be preserved.

### `FirstNightInformationMigrationTest`

Protects six-family shadow parity, mismatch fail-safe, invalidation, selected-candidate commit and idempotence. Its candidates are constructed directly in the test; it does not prove how real game state generates those candidates.

### `StructuredNumberInformationUiModelTest` / `StructuredEmpathInformationAdapterTest`

Protect typed Foundation-to-UI projection, confirmation, stale-context behavior, poisoned Empath selection/warnings and a supplied numeric `trueValue`. They do not prove that Chef/Empath `trueValue` came from a shared registration-aware semantic evaluator.

### `MalfunctionNumberPolicyTest`

Protects generic numeric legal values, style behavior, history continuity and validation. It is not a role semantic test.

### `ClocktowerFortuneTellerPhaseAuthorityTest`

Protects first-night base-Demon vs later-night effective-state authority. It does not cover Drunk/Poisoned semantic equivalence.

## 9. S6D-1 durable RED plan

S6D-1 should use behavior-first REDs. Do not create source-string, class-existence, helper-call or exact-constant tests.

### RED A — pair semantic truth survives reliability change

Use a registration-sensitive pair role. Preferred primary proof is Investigator + Recluse because S6C already defines the correct healthy typed truth; Washerwoman + Spy is the companion coverage for the missing pair family.

The behavior contract:

```text
same game + same perceived ability + same displayed clue
-> semantic truth determined by role/registration semantics
-> RELIABLE / POISONED / DRUNK may change recommendation family/provenance
-> reliability must not change whether that clue is semantically truthful
```

Also prove `actual Drunk + committed shownRole=X` reaches X semantic space without changing the committed shown identity.

### RED B — numeric truth survives reliability change

Use Empath (or Chef if fixture construction is materially smaller) with a Spy/Recluse registration ruling that makes the role-legal numeric result differ from raw actual-identity count.

The behavior contract:

```text
healthy Empath
poisoned Empath
Drunk shown Empath
```

must derive the same role-semantic truthful numeric value from the same game/registration context. Only reliability and subsequent impaired-family selection differ.

Current production is expected to fail this because the unreliable Host path substitutes `empathActualIdentityValue` / `chefActualIdentityValue`.

### RED C — only if needed after A/B

Add one Washerwoman perceived-role test if RED A uses Investigator and does not expose the missing Washerwoman typed family. Do not duplicate every pair-role test mechanically.

Fortune Teller is deferred to S6D-5 unless A/B implementation changes expose a concrete regression.

## 10. Implementation slices after S6D-0

```text
S6D-0  six-role coverage/ownership audit                         COMPLETE
S6D-1  high-value semantic behavior REDs                         NEXT
S6D-2  generic perceived-ability semantic request/result seam
S6D-3  pair family completion: WW / Librarian / Investigator
S6D-4  numeric family completion: Chef / Empath
S6D-5  query-dependent boolean completion: Fortune Teller
S6D-6  FirstNightInformationMigration shadow/parity integration
S6D-7  full acceptance + checkpoint
```

Slices may be collapsed when one small implementation satisfies multiple REDs, but scope must not expand beyond the six-role first-night semantic-completeness contract.

## 11. S6D exit condition

S6D is complete only when:

1. all six TB first-night B2 families have an explicit semantic owner/disposition;
2. an actual Drunk with committed shownRole X reaches X's semantic domain without actualRole==X shortcuts;
3. healthy, poisoned and Drunk forms of the same perceived ability share one role truth/legal-space definition;
4. registration is resolved in role semantics before impairment family selection;
5. first-night migration/parity remains fail-safe;
6. committed shown identity cannot be mutated by recommendation;
7. focused and full acceptance evidence pass;
8. S7 remains blocked until this checkpoint exists.

## 12. Non-goals

Do not in S6D:

- implement A3/A4 `before -> observation -> after` epistemic quality ranking;
- promote ZDD;
- change S5/S6A/S6B ownership;
- reselect Drunk identity;
- create role-specific Drunk/Poisoned strategy engines;
- tune the accepted 90/10 impaired-family product policy;
- broadly rewrite registration;
- start S7/S8/REC-R1 or App/Host decomposition;
- merge or Ready PR #61.
