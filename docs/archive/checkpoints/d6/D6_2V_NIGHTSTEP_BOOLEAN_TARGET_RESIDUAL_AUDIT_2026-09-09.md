# D6.2v — NightStep boolean / target interaction residual audit

> Date: 2026-09-09 Australia/Sydney  
> Status: COMPLETE / READ-ONLY / NO-GO FOR A GENERIC NIGHTSTEP INTERACTION OWNER  
> Branch: `codex/d6-2-ui-composition`; PR #115 OPEN / DRAFT.  
> D6.2u docs checkpoint: `17c0bde6458ed85f36a65ad811c4390efc1c7c2b`.  
> Latest validated production checkpoint remains `a692cc722f1e597e747154bf05a2689fee9bed4c`; final validated code/test head remains `b2263cd08bc2ce223598698324bf2b22243c91f2`.

## Decision

Do **not** introduce a generic `NightStepInteraction`, `InformationInteractionContext`, `NightStepArgs`, or all-family renderer.

The boolean, pair and target branches already have specialized typed owners with materially different state machines and authority boundaries. `ClocktowerNightStepUi.kt` is now primarily the composition/router that chooses among those families and connects their typed events to outer Host/session callbacks.

A generic owner would therefore move the existing fan-out rather than reduce it. It would need a broad union of recommendation, pair legality, target eligibility, registration, display publication, navigation and role-specific inputs. That is exactly the parameter-bag anti-pattern prohibited by the D6 plan.

D6 should stop extracting this NightStep surface at the current semantic boundary and move to the next R2 candidate.

## Boolean ownership

### Structured boolean decisions

The structured boolean path already parallels the structured numeric architecture:

- `StructuredBooleanInformationAdapter.kt` owns typed boolean request/decision semantics;
- `ClocktowerStructuredInformationPreparation.kt` prepares the applicable boolean interaction from the current step and recommendation context;
- the structured storyteller information UI owns decision presentation, validation/warning handling and confirmation;
- `ClocktowerBooleanDisplayOptionSelection.kt` resolves a typed Boolean proposition back to the existing localized display option without relying on display strings.

This is already a narrow semantic stack. Moving it behind another NightStep wrapper would not establish new ownership.

### Fortune Teller is intentionally not a generic boolean-only interaction

Fortune Teller combines two-player target selection with a legal Boolean result set. `ClocktowerFortuneTellerSquareTableUi.kt` already owns that cohesive interaction surface:

- first/second selected-seat presentation;
- selectable-seat presentation;
- completion of the pair before result selection;
- legal result ordering;
- recommended result prioritization;
- automatic-storyteller result presentation;
- determined-single-result vs storyteller-choice presentation;
- previous/next navigation callbacks.

That state machine is different from a simple structured Boolean result. It should remain a dedicated pair+boolean owner rather than being normalized into a generic Boolean renderer.

## Pair-information ownership

Washerwoman/Librarian/Investigator manual pair information is already separated below NightStep:

- `ClocktowerPairManualAuthority` treats `PairInformationLegalDomain` as the only legality authority;
- localized display options are presentation templates only and cannot create/remove legal outcomes;
- selected observations are reconstructed from structured propositions and the same legal domain, not from UI strings;
- dedicated pair manual selection/presentation UI files own the visual/manual interaction.

This is stronger ownership than a generic branch router could provide. The remaining NightStep code should not reclaim or hide this authority.

## Single-target ownership

Single-target actions are also already typed and split by responsibility:

- `ClocktowerSingleTargetInteractionPresentation.kt` defines immutable selection/presentation values and a small sealed event set;
- target eligibility is explicitly supplied, not recomputed as a second rules owner;
- ordinary player-ability targets and storyteller-only rulings are separated into different presentation types;
- `ClocktowerSingleTargetInteractionUi.kt` renders the already-prepared task and emits only `SelectSeat`, `ShowResult`, `Previous` and `Next` events.

This is the D6 target shape: typed presentation + small events, without roster/rules/session bags inside the renderer.

## Why the apparent common orchestration is not yet an honest owner

Across numeric, Boolean, pair and target branches, NightStep still performs some common-looking work:

- resolve current step / actor;
- choose automatic/assisted/manual branch;
- build selection-audit context;
- route current recommendation identity/revision;
- choose the specialized renderer;
- translate typed result/event back to outer callbacks;
- preserve previous/next navigation.

However the payload and commit semantics differ substantially:

- structured number -> confirmed numeric result -> player display;
- structured Boolean -> confirmed Boolean result -> matching display option/player display;
- Fortune Teller -> pair selection + legal Boolean outcome;
- manual pair information -> legal-domain-backed structured observation/display;
- single target -> action-specific seat event, sometimes with a secondary result reveal;
- registration -> separate interaction-local registration ruling.

A shared container would therefore need a large sealed union plus many family-specific dependencies. That may become worthwhile only if a later architecture campaign deliberately introduces a first-class interaction state machine. It is **not** a safe D6 decomposition extraction justified by the current code alone.

## Rejected shapes

This audit rejects:

1. `NightStepArgs` or a broad context data class containing current NightStep parameters;
2. one generic Composable switching over every interaction type;
3. moving target legality or pair legality into UI routing;
4. merging Fortune Teller pair+Boolean state with ordinary Boolean decision state;
5. moving recommendation coordinator, selection-audit recorder, publication, Recovery or session authority into a renderer;
6. introducing a new sealed interaction hierarchy solely to reduce the current file's line count.

## D6 consequence

The current `ClocktowerNightStepUi.kt` is about 45.7 KiB, already below the project's approximate 50 KiB maintainability signal. More importantly, its remaining complexity is now mostly cross-family orchestration over specialized typed owners rather than duplicated domain/presentation ownership.

Per `AGENTS.md`, this is a legitimate stopping point for the NightStep extraction line: further decomposition is likely to increase coupling unless a new first-class interaction-state architecture is intentionally designed later.

### Next: D6.2w — Day vote orchestration residual audit

Move to the next R2 candidate from the global D6 audit. Re-audit the surviving Day nomination/vote composition after D6.2i and identify whether any remaining vote orchestration can move behind the existing typed `ClocktowerTableVoteState` / vote transaction boundary without moving durable vote authority, Recovery state or `dayModeState`.

The audit should specifically distinguish:

- local pending voter selection/count already owned by the vote table;
- nomination/transient state already moved Judge-local in D6.2i;
- ghost-vote authority and highest-vote state that remain durable/external;
- route/navigation orchestration around the vote table;
- any remaining callbacks that only forward typed vote events.

If the remaining Day path is already at an honest boundary, record NO-GO and proceed to the next D6 global-audit family rather than manufacturing a wrapper.

## Validation classification

This slice is documentation/read-only architecture work only. No production source, tests, workflow, persistence, recommendation semantics, interaction order or runtime behavior changed. Per `AGENTS.md` and `TESTING_STRATEGY.md`, no Android RED/GREEN or broad regression run is required for this docs-only checkpoint.
