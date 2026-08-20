# Unified semantic model (A1 + A1.1)

> Milestone: Phase A / PR A1.1  
> Schema: `epistemic-semantic-v2`  
> Production rollout: not connected; recommendation output is unchanged

## Purpose

The semantic boundary separates storyteller truth, information actually delivered, and facts available to one recipient. A1.1 hardens that boundary before any `PlayerWorldSet` implementation is introduced.

Schema v2 is intentionally incompatible with v1. Persisted v1 roots fail with an explicit migration error; they are never silently interpreted as v2.

## Contract map

| Object | Responsibility | Boundary |
|---|---|---|
| `FormalGameState` | actual roles, statuses, hidden setup facts and timeline truth | storyteller-only; never a player cache key |
| `EpistemicObservation` | a proposition delivered to named recipients at one point | contains visible reliability, not hidden truth |
| `PlayerKnowledgeSnapshot` | public facts plus private facts addressed to one seat | excludes actual roles, poison target, red herring and unaddressed decisions |
| `RegistrationQuery` | one detecting ability's question at one `interactionId` and `TimelinePoint` | registration is not a permanent identity rewrite |
| `RegistrationProfile` | one rules-permitted response to a registration query | capability only; not proof that it was selected |
| `RegistrationFact` | the registration selected in one complete legal choice | must match `LegalEpistemicChoice.interactionId` |
| `EpistemicHypothesis` | world-construction assumption | included in world-set identity and cache keys |
| `WorldCardinality` | exact arbitrary-precision count or explicit lower bound | no `Long` overflow or cap-as-exact ambiguity |
| `PlayerWorldSetIdentity` | SHA-256 identity of visible knowledge + ruleset + recipient + hypothesis + schema | excludes formal snapshot and caller-generated IDs |
| `CandidateFamilyId` | storyteller decision/clue family | distinct from world explanations |
| `WorldExplanationClusterId` | mechanism explaining surviving worlds | distinct from candidate families |

## Interaction-scoped registration

`TroubleBrewingRegistrationSemantics` always returns the actual profile and may add a special Spy/Recluse profile when the query target is compatible with the official ability:

- Spy: good and Townsfolk/Outsider;
- Recluse: evil and Minion/Demon.

A role-level query must carry the ruleset-resolved role, character type and alignment. This prevents an unconstrained role ID from being treated as a legal special registration.

`isLegalSelection(...)` verifies the selected fact against the same interaction, subject, question and possible profiles. Special selections must also carry the matching `SPY_ABILITY` or `RECLUSE_ABILITY` reason.

The query includes `interactionId`, timeline point, detecting ability, official `RegistrationQuestion`, and target values. Two abilities in the same night therefore remain independent. `LegalEpistemicChoice` also carries the interaction ID and rejects any selected `RegistrationFact` from another interaction; multiple subjects may still register independently within the same interaction. Capability and selection are never conflated.

Fortune Teller red-herring setup is an explicit exception. `FormalGameState.eligibleRedHerringSeats()` uses `actualAlignment == GOOD`:

- Spy is ineligible even though it may register as good in an interaction;
- Recluse is eligible even though it may register as evil or Demon in an interaction.

## Spy grimoire observation

`InformationProposition.GrimoireState` records the current displayed token, alive state and stable reminder-token IDs per seat. It is valid only inside a private observation sourced from the Spy ability at the relevant wake point. The model has no demon-bluff field; bluffs are not inferred into Spy knowledge without a separate official rule or product event.

## Knowledge-based identity

`PlayerWorldSetIdentity.create(...)` hashes canonical schema-v2 data containing:

```text
ruleset identity
+ recipient seat
+ perceived role
+ visible public/private observation semantics
+ setup knowledge
+ EpistemicHypothesis
+ schema version
```

It deliberately omits:

- `FormalGameState.snapshotId` / `PlayerKnowledgeSnapshot.formalSnapshotId`;
- `knowledgeSnapshotId`;
- observation IDs, hidden global sequence numbers and other private recipients;
- actual roles and storyteller-only propositions.

Those IDs remain useful for storage joins and audit, but cannot make identical player knowledge produce different world-cache identities. A changed visible fact, ruleset, recipient, hypothesis or schema does change the identity.

## Serialization and validation

- every persisted root uses `schemaVersion = 2`;
- canonical JSON sorts object keys, recipient sets, reminder-token IDs and grimoire seats;
- timeline/list order remains meaningful where appropriate;
- new A1.1 values have round-trip encoders and decoders;
- unknown kinds, enum values and schema v1 fail closed;
- `WorldCardinality` writes arbitrary-precision values as decimal strings.

## Verification

`EpistemicSemanticModelTest` covers:

- A1 and A1.1 schema-v2 round trips;
- explicit schema-v1 rejection;
- interaction-local Spy/Recluse registration capability;
- selected registration facts remaining bound to legal choices;
- actual-alignment red-herring eligibility;
- world-set identity independence from formal secret IDs and dependence on visible knowledge/hypothesis;
- private timed Spy grimoire observations with no implicit bluff field;
- arbitrary-precision exact and lower-bound cardinality;
- separate candidate and explanation taxonomies;
- cross-recipient private-information rejection.

No production recommendation call site, candidate generator, selector, scoring policy or UI path is changed by A1.1.
