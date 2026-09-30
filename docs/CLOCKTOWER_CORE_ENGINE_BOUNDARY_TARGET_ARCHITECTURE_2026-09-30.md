# Clocktower Core Engine Boundary Target Architecture — 2026-09-30

> Date: 2026-09-30 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Status: **LONG-HORIZON TARGET ARCHITECTURE / CONVERGENCE GUARDRAIL**  
> This document records the intended ownership model. It is **not** authorization for a broad rewrite and does not replace the current DLB execution route.

## 1. Architecture decision

Clocktower should converge toward three separately evolvable engines/modules connected through explicit typed contracts:

```text
Setup Generation
      |
      | setup composition result
      v
Canonical Game Session / Truth
      |
      +---------------------+
      |                     |
      v                     v
Game Engine          Recommendation Context Builder
                            |
                            v
                  Storyteller Recommendation Engine
                            |
                            v
                     Recommendations
                            |
                    accepted/manual choice
                            |
                            v
                        Game Engine
```

The central rule is that there is **one mutable source of game truth**. Setup generation and Storyteller recommendation must not become competing game-state owners.

## 2. Canonical game truth

The canonical owner is the game session aggregate, currently closest to `ClocktowerGameSession` / `ClocktowerSessionState`.

It owns or coordinates the authoritative facts required to answer “what is true in this game now?”, including:

- current mechanical player state and stable seat identity;
- actual/shown identity after those facts are committed;
- alive/dead and other mechanical status;
- game/state revisions;
- mechanical action chronology;
- recipient-scoped information already delivered;
- Storyteller decision history and relevant committed decision facts.

The existing `GameState` may remain a bounded mechanical component rather than being expanded into one giant object. The architectural requirement is **single authority**, not a particular class name or one monolithic DTO.

UI state, `PlayerCard` presentation state, recommendation contexts, replay views and player/public projections are derived views or transient inputs. They must not silently become independent canonical truth.

## 3. Setup Generation boundary

Setup generation should be independently testable and replaceable.

Conceptual request:

```text
SetupGenerationRequest
- script
- playerCount
- deterministic seed/config when required by implementation
```

Conceptual result:

```text
SetupGenerationResult
- generated role / roster composition for the requested player count
- hasDrunk: true | false
- provenance / generator identity where useful
```

The exact representation of the role list is intentionally not frozen here. DLB already establishes the important semantic boundary: `hasDrunk` means the setup requires one Drunk, but setup generation does **not** choose which seated Townsfolk becomes the Drunk.

Setup generation must not own:

- seating;
- final actual-vs-shown seat mapping;
- Drunk seat choice;
- first-night information;
- runtime phase progression;
- Recovery or UI state.

This allows preset/template, generated, evidence-calibrated and future alternative setup generators to evolve without changing the Game Engine.

## 4. Game Engine boundary

The Game Engine owns rules-authoritative progression and accepted state transitions.

Examples include:

- phase/day/night progression;
- legal action and target semantics;
- death/execution/succession transitions;
- impairment/protection/effective-state mechanics;
- committing an accepted Storyteller or player decision into canonical state;
- producing authoritative game events/facts.

The Game Engine may request a recommendation, but it must remain able to distinguish:

```text
what choices are legal
from
which legal choice is strategically preferable
```

Rules/domain owners keep legality. Recommendation code must not become a second rules engine.

## 5. Storyteller Recommendation Engine definition

The Storyteller Recommendation Engine is a **read-only decision-support engine**.

Its generic responsibility is:

```text
decision point
+ required context
+ optional/enrichment context
+ already-legal candidate domain
        ->
qualified/ranked candidate recommendations
+ rationale/diagnostics
```

It does not directly mutate canonical game state. A recommendation becomes game truth only after the appropriate automatic-authority or human-confirmation path sends an accepted command/decision back through the Game Engine/session owner.

This definition is broader than the current first exact-consequence SDE seam but consistent with it.

## 6. Required context vs optional/enrichment context

Every recommendation surface should explicitly separate information required for correctness from information that only improves recommendation quality.

Example — Drunk selection:

Required:
- current seated roster / relevant actual and shown setup facts;
- rules-owned eligible Townsfolk candidate domain.

Optional/enrichment:
- player experience;
- recent cross-game role history;
- future evidence-backed player-specific context.

Example — second-night information:

Required:
- current canonical grimoire/mechanical truth relevant to the ability;
- alive/dead state;
- prior committed actions and information already delivered;
- current phase/round and revision/freshness identity.

Optional/enrichment:
- player experience;
- current public claims;
- evil-side claimed identities / narrative being constructed;
- other strategic interpretation that can be absent without making the base recommendation invalid.

Missing optional context should reduce recommendation sophistication, not corrupt mechanical correctness.

Do not implement one universal God `StorytellerDecisionContext`. Prefer a shared semantic envelope plus typed per-decision required/enrichment context.

## 7. State and history remain distinct concepts

Recommendation often depends on both the current state and how the game reached it.

Do not flatten all history into one undifferentiated list merely to match this target architecture. The current separation is useful:

- mechanical/action timeline — what happened;
- epistemic/observation timeline — what recipients were told or observed;
- Storyteller decision history — why/what decision was committed;
- cross-game history — prior-game context where explicitly allowed.

The canonical session may aggregate/co-ordinate these without merging their semantics.

## 8. Projection model

Consumers should receive the narrowest immutable view required for their responsibility.

Target conceptual projections include:

```text
Canonical Session
├── GameEngine command/state view
├── StorytellerRecommendationContext(decisionPoint)
├── Moderator/Host view
├── PlayerView(playerId)
└── PublicView
```

A recommendation context builder/projector should derive typed snapshots from canonical state/history plus explicitly supplied enrichment data.

This is preferable to allowing each recommendation family or UI screen to reconstruct game truth independently.

## 9. Current repository mapping and known convergence gaps

The current repository is already materially aligned with this model:

- `ClocktowerGameSession` / `ClocktowerSessionState` already act as the closest canonical mutable aggregate.
- `GameSnapshot` already provides an immutable ruleset-backed snapshot for bounded consumers.
- `ActionFactTimeline`, `EpistemicObservationLog`, decision history and cross-game history already preserve distinct semantic histories.
- `StorytellerDecisionEngine` already documents a read-only, non-committing exact-consequence seam.
- setup provider/source abstractions already separate candidate generation from UI/persistence concerns.
- DLB already moves Drunk seat identity from setup input to a later rules/SDE/session decision.

Remaining convergence work is primarily boundary cleanup rather than a replacement architecture:

1. production UI / `PlayerCard` remains a presentation-side source for some projections and should not become a competing game-truth owner;
2. legacy `DynamicGameState`-carrying recommendation request shapes should be retired or narrowed when their owning surfaces are touched;
3. setup generation should eventually expose the Drunk-presence concept explicitly at its stable external contract;
4. recommendation surfaces should converge on explicit decision-point + required-context + enrichment-context semantics;
5. H2 or later projection work should derive recommendation input from canonical session facts rather than inventing another mutable state model.

## 10. Convergence policy

This target should be reached incrementally at natural feature/cleanup boundaries.

Current DLB convergence order is:

- DLB-5H1 is COMPLETE / ACCEPTED and preserved this ownership model by extracting presentation only;
- the Drunk-assignment shadow/evidence cutover gate audit is now immediate, and Beginner automatic Drunk authority remains prohibited until that gate passes;
- DLB-6 may retire old setup/recommendation contracts in a direction consistent with this document;
- H2 should be re-audited against this target after the relevant DLB boundaries stabilize.

Do not start a new broad “rewrite GameState / rewrite Host / rewrite SDE” campaign solely because this document exists.

A future slice should be justified by a concrete ownership defect, feature need, stale duplicate-state path or already-planned retirement boundary.

## 11. Non-goals

This document does not authorize:

- a second session or second canonical state owner;
- a giant context object shared by every decision;
- moving legality into SDE;
- merging all history types;
- making setup generation choose a Drunk seat;
- giving recommendation code direct mutation authority;
- replacing current working typed owners merely to match new names;
- policy/ranking changes without the required evidence.

The goal is stable ownership and replaceable engines, not architectural churn.
