# SDE-2B Spy / Recluse Interaction-Scoped Registration Branch Audit

> Date: 2026-09-18 Australia/Sydney  
> Branch: `sde-1-orchestration-seam`  
> PR: #144  
> Status: **COMPLETE**

## 1. Objective

Model one Storyteller-selected Spy/Recluse registration branch in exact hypothetical consequence evaluation without:

- mutating canonical player identity;
- duplicating registration legality;
- turning registration into durable game state;
- changing production selection yet.

## 2. Existing legality authority is sufficient

`TroubleBrewingRegistrationDomain` already owns the complete Trouble Brewing legality rule.

It receives the effective interaction-time subject and:

- disables special registration when the subject is poisoned;
- allows Spy to register as legal good Townsfolk/Outsider values;
- allows Recluse to register as legal evil Minion/Demon values;
- normalizes candidates according to the registration question;
- projects typed `RegistrationFact` values.

No SDE or epistemic evaluator should duplicate these rules.

## 3. Existing exact world semantics are already registration-aware

`TroubleBrewingWorldObservationEvaluator` already evaluates registration per observation and never mutates `EnumeratedWorld` identity.

Existing behavior covers:

- `RoleAt`, `AlignmentAt`, and `CharacterTypeAt`;
- pair `AnyOf` / `AllOf`;
- Chef/Empath numeric alignment registration;
- Fortune Teller Demon registration;
- poisoned Spy/Recluse disabling the optional branch.

`WorldObservationResult` currently exposes:

```text
matches
explanation clusters
registrationFacts
```

and existing tests prove registration facts are interaction-local to the observation.

## 4. Exact-query gap

`ExactHypotheticalObservationBundleQuery` currently carries only:

```text
bundleId
recipientSeat
observations
```

The exact evaluator calls:

```text
TroubleBrewingWorldObservationEvaluator.evaluate(...).matches
```

Therefore current semantics are existential:

> retain the world if the observation can be explained by **any** legal registration path.

That is correct for an unbound epistemic observation, but insufficient once the Storyteller has selected one concrete registration witness.

## 5. Why registrationFacts union is not enough

The current `registrationFacts` result is a union over all matching paths.

Example:

```text
AnyOf(
  seat 2 is Empath,
  seat 4 is Empath
)
```

In one world the proposition may be true naturally; in another it may be true only because seat 2 is a Spy registering as Empath. In a numeric observation, several different registration assignments may produce the same number.

A union answers:

> which special registrations appear in at least one successful explanation?

It does **not** answer:

> which complete registration witness set produced this successful explanation?

Consequently, simply adding `RegistrationFact` to the exact query and testing membership in the current union would be unsound for compound/numeric information.

## 6. Production candidate evidence

The legality side already carries a selected witness.

`NaturalPairInformationCandidateGenerator` creates truthful Spy/Recluse candidates with typed `RegistrationFact` values from `TroubleBrewingRegistrationDomain`.

`PairInformationLegalDomain` then canonicalizes one truthful witness per player-visible outcome:

- prefer a natural truthful candidate with no registration witness;
- otherwise retain a deterministic special-registration witness.

`ClocktowerPairManualAuthority` preserves that witness through the structured pair-information domain.

The missing connection is therefore:

```text
existing legal candidate witness
→ exact hypothetical registration-witness binding
```

not a new legality generator.

## 7. Interaction identity mismatch is projection, not legality

Current candidate-generation registration facts use generator-local interaction IDs/questions, while the exact world evaluator binds generated explanation facts to the hypothetical observation interaction.

For example, Investigator/Recluse legality may be generated under `SPECIFIC_MINION`, while the resulting `RoleAt(...)` observation is evaluated as a role proposition.

SDE-2B must not compare generator-local IDs/questions as if they were canonical identity.

The exact binding should project the already-legal selected witness onto the hypothetical observation interaction. Legality stays upstream.

## 8. Required semantic extension

The exact world result must preserve **registration witness alternatives**.

Conceptually:

```text
world + observation
→ zero or more successful witness sets

natural truth
→ { {} }

Spy special truth
→ { {spy-registration} }

natural OR Spy-special truth
→ { {}, {spy-registration} }

numeric result with several legal registration assignments
→ { witness-set-A, witness-set-B, ... }
```

The existing union of registration facts can remain as a compatibility/diagnostic projection of these alternatives.

## 9. Exact query binding semantics

The exact hypothetical boundary needs an optional selected witness binding per observation.

Required distinction:

```text
NO BINDING
    legacy / exploratory semantics
    any legal witness may satisfy the observation

BOUND EMPTY WITNESS
    this candidate is true without special registration
    require a successful empty witness path

BOUND SPECIAL WITNESS
    this candidate selected a concrete Spy/Recluse witness
    require a successful path containing that selected semantic witness
```

This preserves SDE-1E behavior because existing observation-only queries remain unbound/existential.

## 10. Matching identity

A selected witness is interaction-local because it is attached to one exact observation binding.

Generator-local `interactionId` and `registrationQuestion` are provenance and may differ from the exact observation projection. Matching must use the semantic selected registration:

- subject seat;
- registered role/type/alignment values;
- Spy/Recluse reason;

while the exact binding supplies the observation interaction scope.

This does not re-run legality.

## 11. Tests-first acceptance

Before production/SDE wiring, add focused epistemic contracts proving:

1. one world can expose both natural and special witness alternatives without collapsing them into one union;
2. `AnyOf` preserves separate natural and Spy-special witness paths;
3. numeric registration preserves complete witness sets rather than only the flattened union;
4. a poisoned Spy/Recluse has no special witness path;
5. unbound exact queries retain existing existential behavior;
6. a bound empty witness excludes worlds that require special registration;
7. a bound special witness excludes worlds that only satisfy the observation naturally;
8. no `EnumeratedWorld` identity/state mutation occurs.

## 12. SDE-2B implementation boundary

After the epistemic contract is green:

- add the smallest typed witness-binding field to the exact hypothetical request boundary;
- forward optional witness binding through `ExactConsequenceCandidate`;
- add a pure adapter from an already-legal registration-bearing candidate into the exact observation binding;
- keep production selection shadow-only.

Do not migrate the full legacy `RegistrationPolicy` selection stack in this slice.

## 13. Frozen conclusion

> **Spy/Recluse legality already has a single owner and exact world mechanics already support interaction-local registration. SDE-2B must preserve successful registration witness alternatives and let an exact hypothetical candidate bind one selected witness. Registration remains per interaction, never canonical identity. No second registration rules engine is required.**


## 14. Implemented exact branch contract

SDE-2B implemented the smallest complete branch-binding seam.

### Exact world result

`WorldObservationResult` now preserves:

```text
registrationWitnesses: Set<Set<RegistrationFact>>
```

The existing flattened `registrationFacts` view remains available as the union of those successful witness alternatives.

This preserves distinctions such as:

```text
natural truth             -> {}
Spy-only explanation      -> {Spy}
Spy + Recluse explanation -> {Spy, Recluse}
```

rather than collapsing them into one falsely-conjunctive set.

### Exact hypothetical query

`ExactRegistrationWitnessBinding` binds one selected witness to one hypothetical observation.

Semantics:

```text
no binding      -> any legal witness may explain the observation
empty binding   -> require a natural/no-special witness
special binding -> require that selected interaction-local witness
```

Generator-local `interactionId` / `registrationQuestion` values are not treated as canonical identity. Matching uses selected registration semantics while the hypothetical observation provides interaction scope.

### SDE forwarding

`ExactConsequenceCandidate` now carries optional registration witness bindings and `StorytellerDecisionEngine` forwards them unchanged to the exact evaluator.

`PairInformationExactConsequenceAdapter` projects an already-legal truthful `PairInformationLegalCandidate` into that exact boundary.

The adapter:

- does not call registration legality again;
- does not materialize a new proposition;
- does not mutate player identity/state;
- explicitly binds an empty witness for natural truthful candidates;
- explicitly binds the candidate's existing typed witness for Spy/Recluse registered truth.

## 15. Executable evidence

Core exact registration semantics:

`a977c01c0f4ae634dd60e4999759838f4005228c`

Validation:

```text
R2 main-thread boundary        SUCCESS
Android FAST unit tests        SUCCESS (executed)
Real Clingo cross-validation   SUCCESS
CI gate                        SUCCESS
```

Final SDE forwarding / pair projection head:

`ec970aaa302b7ea6ba5f869aec43cf8f3a82b950`

Validation:

```text
R2 main-thread boundary        SUCCESS
Android FAST unit tests        SUCCESS (executed)
CI gate                        SUCCESS
Real Clingo cross-validation   SKIPPED by classifier
ASP contract tests             SKIPPED by classifier
Full Android/APK step          SKIPPED by classifier
```

CI run: `35294683751`.

Focused regression contracts:

- `RegistrationWitnessAlternativesTest`
- `ExactRegistrationWitnessBindingTest`
- `Sde2RegistrationWitnessForwardingTest`

## 16. Frozen SDE-2B result

> **Spy/Recluse registration remains interaction-scoped and rules-owned. Exact world evaluation preserves complete successful witness alternatives; exact hypothetical candidates may bind one selected witness; SDE forwards that binding without re-deciding legality. Canonical player identity is never mutated and production selection is still unchanged.**
