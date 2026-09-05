# Beginner Storyteller Mode — Automatic Clue & Evil-Assistance Policy

> Date: 2026-09-06 Australia/Sydney  
> Status: **PRODUCT / STRATEGY DECISION — DEFERRED UNTIL COGNITIVE-CONSISTENCY FOUNDATION IS READY**  
> Related authorities:
> - `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
> - `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`
> - `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`
> - `docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`

## 1. Product goal

Add a dedicated **Beginner Storyteller Mode** for truly new Storytellers.

The user-facing objective is:

> **Minimum decisions, maximum clarity.**

The app should absorb as much Storyteller judgement as safely possible so that a beginner can focus on the physical hosting loop:

```text
wake player
-> let player act when needed
-> show the generated result
-> continue
```

Beginner Mode should not ask the user to understand recommendation styles, compare multiple clue candidates, reason about registration choices, or manually decide how impairment should mislead a player.

## 2. Core UX contract

In Beginner Mode:

- clue / information output is selected automatically;
- no recommendation shortlist is shown during normal flow;
- no manual registration choice is required during normal flow;
- no Drunk/Poisoned truth-vs-false choice is required;
- advanced rationale and diagnostics stay hidden from the primary interaction surface;
- the primary screen should present only the action the Storyteller needs to perform next.

Conceptually:

```text
system evaluates interaction
-> system selects one clue/result
-> UI displays exactly what to show/do
-> Storyteller continues
```

A separate Standard/Expert experience may continue to expose alternatives, diagnostics and manual control. Beginner Mode must not redefine the underlying legal semantic domain.

## 3. Impairment policy: misleading by default, not mechanically always false

For Drunk / Poisoned information abilities, Beginner Mode should strongly prefer a useful misleading result.

Do **not** encode the permanent semantic rule as:

```text
impaired => false
```

Instead use:

```text
impaired
-> enumerate legal information candidates
-> remove obviously poor / self-exposing candidates
-> strongly prefer useful misinformation
-> allow a technically truthful result only when the alternatives are materially worse
```

Reasons:

- official mechanics allow impaired information to happen to be true;
- an absolute `impaired => false` rule creates deterministic app meta;
- some false answers can be absurd, self-exposing, or more damaging to game quality than a plausible truthful result.

From the beginner Storyteller's perspective this remains fully automatic.

## 4. Spy / Recluse registration policy: automatic and strategically useful, not mechanically always false

Beginner Mode should not ask the Storyteller to decide Spy/Recluse registration per interaction.

The system should automatically resolve registration inside the legal interaction semantics.

Prefer unusual/misregistration when it creates a useful, plausible interaction, but do **not** encode:

```text
Spy always registers as good
Recluse always registers as evil
```

The chosen registration should be interaction-scoped, legal, explainable, and useful to the current game state.

This avoids a fixed app meta while preserving the beginner-mode goal of zero registration decisions.

## 5. New strategic objective: filter bad clues, then preferentially help the evil team

Observed beginner-game experience suggests that inexperienced evil players often struggle to survive to the final round even when the rules are functioning correctly. Beginner Mode may therefore provide a deliberate, bounded Storyteller-assistance bias toward the evil team.

The policy should be:

> **First eliminate clearly unreasonable or harmful clues; then, among the remaining plausible candidates, prefer the clue that best improves evil-team survivability and supports a coherent mistaken world.**

This is not equivalent to maximizing raw falsity.

A useful clue should ideally:

- help the Demon or Minion sustain a believable bluff;
- preserve multiple plausible evil-location worlds;
- create productive doubt between good players;
- support a coherent alternative explanation rather than a random contradiction;
- interact with Demon bluffs, Outsider count, Spy/Recluse registration and other information roles;
- delay premature convergence on the actual evil team;
- remain discoverable rather than becoming an irreversible Storyteller-created lock.

## 6. Hard gates before evil-utility ranking

Evil-team assistance must only operate **after** semantic legality and clue-quality gates.

Candidates should be rejected or heavily penalized when they are:

- illegal under the current ability semantics;
- immediately contradictory to strong player-visible/public facts;
- obviously absurd or likely to reveal impairment for free;
- disconnected random misinformation with little gameplay value;
- likely to hard-lock one player as good/evil with little plausible recovery path;
- dependent on Storyteller-hidden action facts that players could not reasonably infer;
- so strong that the Storyteller effectively decides the faction outcome instead of creating inference space.

Therefore the architecture should remain:

```text
legal semantic domain
-> cognitive-consistency / hypothetical-world evaluation
-> plausibility + safety gates
-> evil-assistance utility ranking
-> final Beginner policy choice
```

Never reverse this order.

## 7. Candidate EvilUtility dimensions

Exact weights are intentionally not frozen yet.

Possible factors include:

```text
EvilUtility =
    DemonSurvivalValue
  + MinionSurvivalValue
  + BluffSupportValue
  + MistakenWorldCoherence
  + GoodTeamCrossSuspicionValue
  + CorrectWorldConvergenceDelay
  + CrossRoleInteractionValue
  - ObviousAnomalyPenalty
  - ConfirmationLockPenalty
  - ExcessiveStorytellerControlPenalty
  - ComplexityPenaltyForBeginnerGames
```

The primary objective is not “hurt the good team”. It is to keep a beginner evil team competitively alive long enough for the social game to function.

## 8. Dynamic assistance rather than fixed maximum bias

The assistance strength should eventually respond to game state rather than use one fixed aggressive setting for the entire game.

Conceptually:

```text
AssistanceLevel =
    BeginnerModeBaseBias
  + CurrentEvilDisadvantage
  - CurrentEvilAdvantage
```

Examples of states that may justify stronger assistance:

- Demon bluff is close to collapsing;
- several evil players are already strongly suspected;
- good-team information has nearly converged on the real world;
- one more strong clue would effectively solve the evil location.

Examples where assistance should be reduced:

- Demon is already socially safe;
- good players are heavily split across multiple coherent worlds;
- evil team has strong positional advantage near the final round;
- repeated pro-evil clues would create a detectable app pattern or unfair lock.

The first implementation does not need a perfect win-probability model. A transparent bounded heuristic can be introduced later, after the epistemic foundation is stable.

## 9. Required architecture boundary

This feature must **not** be built into the cognitive-consistency engine itself.

The responsibilities are separate:

### Cognitive-consistency / EPI-MQ layer

Answers:

> Which legal candidate clues create plausible, coherent, sustainable and discoverable player-perceived worlds?

### Beginner Storyteller policy layer

Answers:

> Among the acceptable candidates, which one best supports a beginner game, including bounded assistance to an inexperienced evil team?

This separation allows the same semantic/epistemic engine to serve:

- Beginner Mode: automatic, bounded evil assistance;
- Standard Mode: recommendations + manual override;
- Expert Mode: richer alternatives/diagnostics/manual control.

The exact product naming and number of modes can be finalized later; the architecture must support this separation now.

## 10. Dependency / implementation order

**Do not implement this policy before the cognitive-consistency work is sufficiently complete.**

Recommended order:

```text
1. legal semantic authority is stable
2. player-visible cognitive-consistency / historical replay is stable
3. hypothetical candidate observation evaluation exists
4. EPI-MQ can reject obviously unreasonable misinformation
5. candidate quality / Productive Uncertainty ranking is usable
6. add EvilUtility features as a separate policy dimension
7. add dynamic Beginner assistance level
8. integrate one-choice Beginner UI
9. real-game calibration with beginner groups
```

This avoids building a temporary role-specific heuristic system that would later need to be replaced by the cognitive-consistency engine.

## 11. Testing direction when implementation begins

Prefer scenario-level and comparative behavior tests.

Useful contracts include:

- Beginner Mode requires no normal clue-selection decision;
- impaired information usually prefers plausible misinformation over truthful output when good misinformation exists;
- impaired information is not constrained by a permanent `must be false` invariant;
- Spy/Recluse registration is automatically selected but remains interaction-scoped and legal;
- obviously unreasonable candidates are rejected before faction utility is considered;
- a coherent bluff-supporting candidate may outrank disconnected random falsehood;
- pro-evil assistance weakens when evil already has a large epistemic/social advantage;
- policy ranking cannot change semantic legality, committed shown identity, or historical truth authority.

Avoid brittle exact-weight assertions unless a specific weight becomes a deliberate product contract.

## 12. Explicit non-goals for now

Do not currently:

- change production clue ranking for this feature;
- add role-specific Beginner hacks ahead of the epistemic foundation;
- make `Drunk/Poisoned == always false` a semantic invariant;
- make Spy/Recluse always misregister;
- optimize directly for an exact evil win percentage;
- weaken Manual authority in Standard/Expert flows;
- expand the active Night Step UI decomposition scope to include this work.

## 13. Acceptance intent

The eventual Beginner Mode should make this experience possible:

> **A new Storyteller can host a full game with very few judgement calls. The app automatically supplies legal, understandable clues; misleading information is purposeful rather than random; registration decisions are automatic; and when the evil team is inexperienced, the system gives bounded strategic assistance without becoming visibly arbitrary or removing player agency.**
