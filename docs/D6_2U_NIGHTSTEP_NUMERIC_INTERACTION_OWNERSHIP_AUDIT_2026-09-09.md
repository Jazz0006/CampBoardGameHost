# D6.2u — NightStep numeric interaction ownership audit

> Date: 2026-09-09 Australia/Sydney  
> Status: COMPLETE / READ-ONLY / NO NEW GENERIC NUMERIC UI OWNER  
> Branch: `codex/d6-2-ui-composition`; PR #115 OPEN / DRAFT.  
> D6.2t docs checkpoint: `e8b0782f55010a0bb5581663207d0e32101d3bf6`.  
> Latest validated production checkpoint remains `a692cc722f1e597e747154bf05a2689fee9bed4c`; final validated code/test head remains `b2263cd08bc2ce223598698324bf2b22243c91f2`.

## Decision

Do **not** create another generic `NumericInteractionUi`, `NumericInteractionArgs`, or numeric presentation family around `ClocktowerNightStepUi.kt`.

The numeric path is already split across honest typed owners. The remaining code in `ClocktowerNightStepUi.kt` is primarily cross-feature orchestration: it selects which interaction branch is active, prepares shared selection-audit/publication wiring, and connects typed decisions to the durable `onShowPlayerDisplay` boundary. Extracting that residual as a numeric-only owner would duplicate infrastructure that is also used by boolean, pair and target interactions.

The correct next step is therefore to audit the remaining boolean/target branches together before deciding whether a broader **information-interaction orchestration** seam exists. Do not invent that broader seam in this slice.

## Ownership map

### 1. Structured numeric decision semantics and preparation

Existing typed owners already cover this boundary:

- `StructuredNumericInformationAdapter.kt` owns structured numeric request/decision/draft semantics and coordinator interaction;
- `ClocktowerStructuredInformationPreparation.kt` maps the current `ClocktowerNightStepUi` plus actor/recommended option into `ClocktowerNumericInformationPreparation` and then into `StructuredNumberInformationUiModel`;
- `StructuredNumberInformationUiModel.kt` carries the immutable UI-facing decision model and deliberately leaves legality in the Foundation/decision layer.

The current factory is intentionally **not universal**. It returns no structured numeric preparation when Spy/Recluse registration keys are present, supports Empath through its typed living-neighbour metric, and supports Chef only for the applicable unreliable-number path. Clockmaker does not become part of this contract merely because it displays a number.

That is evidence of a real semantic boundary, not an abstraction gap.

### 2. Structured numeric transient selection and validation UI

`StructuredNumberInformationDecisionPanel` in `clocktower/ui/StructuredStorytellerInformationUi.kt` already owns the actual structured-number interaction surface:

- local numeric selection;
- AUTO / ASSISTED / MANUAL presentation behavior supplied by the decision model/policy;
- warning and blocked-decision presentation;
- revision/status staleness checks;
- final typed `onConfirmed(confirmed, value)` output.

A wrapper that simply forwards the same model/actions would add indirection without acquiring ownership.

### 3. Result-first registration is a separate interaction family

The result-first Spy/Recluse path must **not** be folded into the structured-number panel.

`ClocktowerRegistrationUi.kt` already owns registration-specific interaction state and presentation, including:

- actual-vs-special registration selection;
- registered-role selection when relevant;
- AUTO/ASSISTED recommendation selection for registration rulings;
- selection-audit preview/commit for the registration decision;
- poisoned/disabled registration behavior;
- interaction-local Spy/Recluse presentation.

Its lifecycle and state vocabulary are registration semantics, not generic numeric semantics. The numeric result happens to consume that ruling; it does not make the ruling a numeric UI concern.

### 4. Numeric player-display resolution is already isolated

`resolveClocktowerNumericPlayerDisplay(...)` in `ClocktowerPlayerDisplayResolution.kt` already owns the pure final numeric display materialization from:

- current step;
- display-option template;
- selected value;
- truthful/confirmed state;
- expected structured context snapshot.

The resolver returns the typed `ClocktowerPlayerDisplay`. It does not own publication, telemetry or session mutation.

### 5. Durable confirmation/publication remains outside the numeric UI

`ClocktowerNightStepUi.kt` still correctly owns the final orchestration callback that connects a confirmed numeric decision to outer Host/session behavior:

- optional selection-audit commit for the automatic recommendation;
- `resolveClocktowerNumericPlayerDisplay(...)`;
- `onShowPlayerDisplay(...)`.

The broader recommendation path also retains `onApplyRecommendedDisplayOption(...)` where that path is actually selected.

These callbacks cross from transient interaction/presentation into durable publication/application authority. They must not move into a reusable numeric Composable or model adapter merely to shorten NightStep.

## Why a numeric-only extraction is the wrong boundary

The residual numeric branch in NightStep shares orchestration infrastructure with non-numeric branches:

- current step and actor resolution;
- automatic/assisted candidate projection;
- selection audit;
- recommendation identity/revision;
- branch selection;
- show-player-display publication;
- next/back navigation.

If only the numeric branch is extracted now, the new owner would either:

1. receive a broad copy of those shared inputs/callbacks;
2. hide publication/recommendation lifecycle inside presentation code; or
3. force a premature generic `InformationInteractionContext` / `NightStepArgs` bag.

All three outcomes increase coupling.

## Rejected shapes

This audit explicitly rejects:

1. a `NumericInteractionArgs` parameter bag;
2. wrapping `StructuredNumberInformationDecisionPanel` only to remove lines from NightStep;
3. merging result-first registration state with structured numeric selection;
4. moving `recommendationCoordinator`, selection-audit recorder, `onShowPlayerDisplay`, telemetry/publication or Recovery into a numeric UI owner;
5. changing the current AUTO/ASSISTED/MANUAL semantics while decomposing;
6. changing numeric truth/proposition/registration behavior as part of a structural slice.

## D6 consequence

D6.2u closes the numeric-specific R2 extraction attempt as **already sufficiently decomposed at the semantic level**.

The remaining NightStep size is a composition/orchestration problem, but the next boundary must be chosen after comparing the other information interaction families rather than by continuing to peel numeric code mechanically.

### Next: D6.2v — NightStep boolean / target interaction residual audit

Perform a read-only audit of the remaining boolean and target/pair information branches in `ClocktowerNightStepUi.kt` and their existing specialized owners.

The audit must determine:

- which branches already have typed preparation/panel owners comparable to structured numeric;
- which local transient selection remains legitimately in NightStep;
- whether boolean + numeric + target branches expose one real common orchestration contract;
- whether extracting that contract would reduce NightStep dependencies rather than merely move its parameter list.

Do **not** create a generic `NightStepArgs`, `InformationInteractionContext`, all-role renderer, or broad callback bag. If the shared contract is not narrow and stable, record NO-GO and move to the next R2 boundary.

## Validation classification

This slice is documentation/read-only architecture work only. No production source, tests, workflow, persistence, recommendation semantics, registry order or runtime behavior changed. Per `AGENTS.md` and `TESTING_STRATEGY.md`, no Android RED/GREEN or broad regression run is required for this docs-only checkpoint.
