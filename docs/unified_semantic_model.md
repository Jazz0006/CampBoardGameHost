# Unified semantic model (A1)

> Milestone: Phase A / PR A1  
> Schema: `epistemic-semantic-v1`  
> Production rollout: not connected; recommendation output is unchanged

## Purpose

The A1 model separates three kinds of state that the current recommendation DTOs could not safely distinguish:

1. storyteller truth (`FormalGameState`);
2. information actually delivered (`EpistemicObservation`);
3. facts available to one recipient (`PlayerKnowledgeSnapshot`).

This boundary is required before A2/A3 can ask whether an observation is SAT from a player's perspective without leaking actual roles, poison targets, red-herring identity or storyteller decisions into that perspective.

## Model map

| A1 object | Responsibility | Explicitly excluded |
|---|---|---|
| `FormalGameState` | immutable actual roles, statuses, public facts and storyteller-only facts at one timeline point | UI labels; player claims; probability |
| `InformationProposition` | typed logical or numeric statement consumed by solver adapters | localized descriptions; free-form rule text |
| `EpistemicObservation` | who received which proposition, when and under what visible reliability | hidden actual reliability that the player was not told |
| `StorytellerDecisionPoint` | stable query context before a legal output is selected | recommendation score and selected candidate |
| `LegalChoiceSet` | complete official-legal outputs, with local registration facts bound to each choice | policy ranking; sampling; illegal fallback output |
| `PlayerKnowledgeSnapshot` | public observations, addressed private observations, perceived token and setup knowledge for one seat | actual role list and storyteller-only propositions |

Existing `GameSnapshot`, `GameState`, `RulesetRef`, `RoleId`, `RegistrationFact`, `AbilityState` and `StorytellerPhase` are reused. Existing recommendation candidates and production selection are not replaced in A1.

## Stable identity and serialization

- every persisted root contains `schemaVersion = 1`;
- semantic type IDs use lowercase machine IDs rather than UI strings;
- `SemanticStableId` uses the first 128 bits of SHA-256 over a caller-supplied canonical semantic payload;
- `EpistemicSemanticJson` sorts object keys and recipient sets, uses enum names and stable IDs, and has round-trip decoders for all six A1 roots;
- list order remains meaningful for timelines and logical expressions;
- unknown proposition kinds or enum values fail closed during decoding.

This JSON is the fixture boundary for A2's ASP adapter and A3's enumerated-world baseline. A schema change requires a new schema version and migration; changing translated text does not change serialized semantics.

## Guardrails

- A public observation must have no explicit recipient list.
- A private observation must name at least one recipient.
- A knowledge snapshot rejects private observations addressed to another seat or another formal snapshot.
- `FormalGameState` validates that all referenced seats exist.
- `LegalChoiceSet` requires a non-empty unique choice set and keeps registration facts inside the complete choice.
- malfunction and registration remain separate concepts.

## A1 verification

`EpistemicSemanticModelTest` covers:

- round-trip serialization for all six required root objects;
- canonical output independent of set insertion order;
- deterministic semantic IDs;
- rejection of cross-recipient private-information leakage.

Because A1 adds only the isolated `clocktower.epistemic` package, tests and this document, no production recommendation call site or selection policy changes in this milestone.
