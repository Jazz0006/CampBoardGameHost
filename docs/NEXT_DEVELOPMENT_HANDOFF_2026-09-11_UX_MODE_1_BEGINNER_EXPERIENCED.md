# NEXT DEVELOPMENT HANDOFF — UX-MODE-1 Beginner / Experienced Storyteller Mode

> Date: 2026-09-11 Australia/Sydney  
> Status: **CURRENT — next independent implementation task**  
> Program: Storyteller Experience Modes  
> Predecessor decision: EPI-MQ-0 audit completed; EPI-MQ implementation intentionally paused until this task is complete  
> Design record: `docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`

## 1. Product goal

Add two explicit Storyteller experience modes without creating two gameplay engines or two recommendation systems.

### Beginner mode

Target user:

- first-time app user;
- inexperienced Blood on the Clocktower Storyteller;
- user who wants the app to make information decisions automatically.

Expected experience:

```text
minimum decision burden
few buttons
recommended information is automatically used as the available action
clearer / slightly more detailed explanation and host instructions
user mainly performs physical hosting actions: wake, show, confirm, continue
```

The beginner should not be asked to choose among legal misinformation candidates during ordinary information flow.

### Experienced mode

Target user:

- experienced Storyteller;
- user already familiar with this app;
- user who wants fast access to recommendation plus manual legal alternatives.

Expected experience:

```text
recommended information remains primary
all rule-valid manual alternatives remain directly available where supported
shorter explanations
lower visual/instructional overhead
fast and efficient host operation
```

## 2. Critical architecture invariant

This feature is a **presentation / interaction-policy distinction**, not a rules or recommendation-policy fork.

For the same canonical game state, history and recommendation inputs:

```text
Beginner mode
Experienced mode
```

must consume the same:

- legal candidate set;
- candidate legality;
- truth/reliability semantics;
- recommendation ranking;
- selected recommended candidate before presentation policy is applied;
- registration semantics;
- session/history authority.

Only the presentation and allowed interaction affordances differ.

Conceptual pipeline:

```text
legal candidates
-> recommendation
-> common information decision context
-> StorytellerExperienceMode presentation policy
    -> BEGINNER: recommendation-only interaction + richer guidance
    -> EXPERIENCED: recommendation + legal manual alternatives + concise guidance
```

Do not implement separate beginner candidate generation, separate beginner misinformation rules, or separate beginner recommendation scoring.

## 3. Important semantic non-goal

Beginner mode does **not** mean:

```text
Drunk/Poisoned => always show false information
```

Beginner mode means:

```text
use the current recommendation automatically
```

The recommendation policy remains authoritative. A truthful impaired result may still be selected when the existing policy or future EPI-MQ judges it appropriate.

Do not change the current impaired truth/false family policy as part of UX-MODE-1.

## 4. First step — read-only ownership audit

Before production edits, re-query live `main` and trace the existing user-facing control path for:

- `automaticStorytellerInfo` and any related automatic/manual setting;
- `StorytellerAutomationMode` or equivalent automation state;
- where setup/settings UI owns these choices;
- whether the current automatic/manual preference is persisted or session-local;
- how `ClocktowerJudgeScreen` receives the setting;
- structured number information UI;
- structured Boolean/Fortune Teller information UI;
- first-night pair-information UI;
- any other information surfaces that still have role-specific/manual presentation;
- host instruction / explanation text ownership;
- any settings tab moved under Host Tools by UI-NAV-1.

The purpose is to replace or subsume overlapping booleans cleanly rather than layering an experience-mode enum on top of contradictory legacy state.

Record an architecture pre-flight before substantial edits, especially if `ClocktowerHostScreen.kt` or another >1000 LOC file must change.

## 5. Proposed typed mode seam

Prefer one explicit typed product concept, conceptually:

```kotlin
enum class StorytellerExperienceMode {
    BEGINNER,
    EXPERIENCED,
}
```

Exact name/package may be adjusted after the ownership audit, but there should be one authoritative product mode rather than several UI booleans representing overlapping concepts.

A small presentation-policy seam should derive behavior such as:

```text
showManualAlternatives
instructionDetail
showSecondaryExplanation
primaryActionPolicy
```

from the experience mode.

Do not put rule legality or recommendation score logic into this policy.

## 6. Implementation slices

### UX-MODE-1A — ownership / migration audit

Read-only first.

Output:

- current mode/automation owners;
- persistence/session ownership;
- all affected information surfaces;
- legacy booleans to replace, retain or adapt;
- exact large-file edit strategy if required;
- focused test owner(s).

### UX-MODE-1B — typed experience-mode policy

Introduce the single authoritative experience-mode representation and small presentation-policy seam.

Required invariant:

```text
same semantic inputs
-> same legal candidates and same recommendation
-> only presentation policy differs
```

Where an existing boolean is a strict projection of the new mode, migrate it toward the typed policy rather than maintaining two independent user settings.

### UX-MODE-1C — information UI convergence

Apply the policy consistently to the existing information surfaces.

Beginner:

- primary recommended result only in normal operation;
- no ordinary manual candidate-selection burden;
- clear show/confirm/continue flow;
- richer explanation / host instruction where useful.

Experienced:

- recommended result visibly primary;
- remaining rule-valid manual alternatives available;
- concise explanation / instruction;
- avoid unnecessary confirmation steps unless they protect an actual warning/invariant.

Do not use UI mode to bypass `InformationDecisionContext` candidate validation.

### UX-MODE-1D — setup/settings integration and acceptance

Expose the mode in the appropriate setup/settings surface and ensure it follows the global navigation/Host Tools ownership established by UI-NAV-1.

The audit must determine the safest default/migration behavior from existing automatic/manual settings. Do not silently change an existing persisted user's behavior without an explicit migration decision.

Finish with focused validation, `:app:testFast` at the logical checkpoint, and any affected UI/session integration evidence required by `docs/TESTING_STRATEGY.md`.

## 7. Testing strategy

This task introduces a new stable user-visible interaction policy, so use meaningful typed tests at the presentation-policy or structured-UI-model boundary where practical.

High-value contracts include:

1. Beginner and Experienced receive the same legal semantic candidate IDs for the same input.
2. Beginner presentation exposes only the recommended ordinary choice.
3. Experienced presentation exposes the recommended choice plus remaining legal choices.
4. Both modes confirm through the same validated information-decision context.
5. Stale/illegal candidate protection remains identical across modes.
6. Beginner mode does not alter impaired truth/false recommendation semantics.
7. Mode changes do not mutate game/session/epistemic history by themselves.
8. Existing automatic/manual preference migration is deterministic once its owner is confirmed.

Prefer typed tests over source-string tests. Do not create domain REDs for purely visual text/layout changes.

Use existing tests as evidence for behavior-preserving wiring where they already protect the contract. Add a new RED only for genuinely new stable behavior that is not covered.

## 8. Large-file rule

`ClocktowerHostScreen.kt` is currently a very large handwritten source file. Do not perform an unsafe whole-file connector replacement.

If UX-MODE-1 requires localized edits there:

- follow root `AGENTS.md` architecture pre-flight;
- use the repository's large-file SOP and the safest approved execution path;
- prefer extracting/using a small typed presentation policy rather than adding another broad conditional tree inside Host.

Do not reopen the completed D6 decomposition campaign merely because the file is large. Extract only when the mode responsibility has a clear owner and the extraction is part of this feature's architecture.

## 9. Scope fence

During UX-MODE-1 do not:

- implement EPI-MQ ranking, world-count scoring or hypothetical evaluation;
- change current misinformation weights or impaired truth/false family probability;
- replace the recommendation provider;
- add Moonchild, Pukka or other new roles;
- generalize the world engine for dynamic scripts yet;
- activate A4/ZDD;
- change persistence/recovery ordering unless mode-setting ownership truly requires a narrow migration;
- redesign UI-R5 square-table interaction or UI-NAV-1 global navigation beyond what is needed to expose the mode;
- create separate beginner gameplay/rules semantics.

## 10. Exit criteria

UX-MODE-1 is complete when:

```text
[ ] one authoritative Beginner / Experienced experience-mode concept exists
[ ] overlapping legacy automatic/manual state is cleanly migrated/adapted
[ ] Beginner information flow requires no ordinary clue choice
[ ] Experienced flow retains recommendation + legal manual alternatives
[ ] explanation density differs by mode without changing semantics
[ ] both modes share the same candidate legality and recommendation pipeline
[ ] focused typed/integration evidence passes
[ ] :app:testFast passes at the logical checkpoint
[ ] required broader affected validation passes
[ ] live UI behavior is suitable for a new Storyteller and for a fast experienced host
[ ] roadmap/handoff are updated to resume EPI-MQ
```

## 11. What follows immediately after completion

After UX-MODE-1 is merged and accepted, resume the epistemic program.

Do **not** restart from the old 2026-09-01 EPI-MQ implementation proposal.

Read:

`docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`

Then continue:

```text
EPI-MQ-0.5  dynamic-script extensibility guard
EPI-MQ-1    neutral hypothetical observation evaluator
EPI-MQ-2    credibility / contradiction / impairment-exposure gates
EPI-MQ-3+   productive-uncertainty ranking
```

## 12. Default reading order for the next execution conversation

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. live `main` / branches / PR state;
6. only the source/tests needed for UX-MODE-1A ownership audit.

Do not load the full EPI-MQ reference stack until UX-MODE-1 is complete unless a narrow semantic dependency must be checked.
