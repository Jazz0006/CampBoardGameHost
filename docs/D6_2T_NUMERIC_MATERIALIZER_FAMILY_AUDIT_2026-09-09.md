# D6.2t — Numeric materializer-family audit

> Date: 2026-09-09 Australia/Sydney  
> Status: COMPLETE / READ-ONLY / NO-GO FOR A NEW SHARED MATERIALIZER FAMILY  
> Branch: `codex/d6-2-ui-composition`; PR #115 OPEN / DRAFT.  
> Audit branch head before this document: `773df86a9d94a7f25f12579564675f568c1cc2a0`.  
> Latest validated production checkpoint remains `a692cc722f1e597e747154bf05a2689fee9bed4c`; final validated code/test head remains `b2263cd08bc2ce223598698324bf2b22243c91f2`.

## Decision

Do **not** add a new Clockmaker/Chef/Empath `numeric materializer family` layer.

The honest shared shell already exists in `ClocktowerInformationStepBuilder`, and D6.2o already extracted the honest shared numeric recommendation/history presentation seam in `ClocktowerNumericInformationOptionPreparation`.

A further family wrapper would have to absorb role-specific registration semantics, Empath cross-night history, or optional fields that Clockmaker does not own. That would either duplicate the existing builder, introduce nullable cross-role bags, or require role switching. All three outcomes violate the D6 decomposition rule that cohesion and ownership outrank byte-count reduction.

Chambermaid remains a successful **role-local** pilot because its two phase closures were genuinely the same stable step shape and could reuse one entry without importing Host lifecycle authority. That proof does not generalize to the remaining numeric roles.

## Read-only evidence

### Clockmaker

Clockmaker is a thin first-night numeric step. Its registry entry supplies:

- stable Clockmaker identity;
- localized role/content/instruction text;
- the already-computed `clockmakerNumber`;
- one footer;
- one lazy unreliable-number `displayOptions` provider.

It does **not** need the Chef/Empath registration domain, numeric proposition assembly, legal registration choices, or Empath history state. Moving this entry behind a new family object would mostly rename an existing `ClocktowerInformationStepBuilder.build(...)` call.

### Chef

Chef is not merely `number + text`. Before materialization, Host prepares registration-sensitive state for Spy/Recluse handling, including registration keys, actual/registered reference values, preview text and the bounded maximum.

The step then carries:

- `InformationProposition.NumericResult(ADJACENT_EVIL_PAIRS, ...)`;
- explicit numeric min/max;
- recommendation options whose propositions use the Chef metric and all seats;
- `legalSelectionOptions` backed by `resultFirstNumericRegistrationOptions(...)` and Chef-specific witness evaluation.

Those values form one Chef registration contract. They should not become nullable fields on a generic Clockmaker/Chef/Empath materializer.

### Empath

Empath has additional state/lifecycle semantics beyond Chef:

- other-night effective state is sampled at the Empath interaction boundary before computing mechanically alive neighbours;
- Spy/Recluse registration keys and reference values are neighbour-sensitive;
- subject seats are the current living neighbours, not all seats;
- `previousShownNumber` is read from prior unreliable-information history;
- Empath exists in both first-night and other-night registries.

The two current Empath builder closures are also not source-identical: the first-night unreliable display-option path attaches `propositionForValue`, while the other-night call currently does not. This audit does **not** declare that difference a bug or normalize it. It is evidence that a single shared entry must not be created mechanically before the semantic difference is deliberately understood.

## Existing shared owners are already at the right level

`ClocktowerInformationStepBuilder` already owns the true common UI-step mechanics:

- actor lookup / missing-role placeholder behavior;
- ability reliability projection;
- automatic/manual/legal candidate routing;
- numeric display-kind selection for Clockmaker, Chef, Empath and Chambermaid;
- common `ClocktowerNightStepUi` construction;
- effective Spy/Recluse registration-key suppression when the information ability is unreliable.

`ClocktowerNumericInformationOptionPreparation` already owns the other genuine numeric-common seam:

- prior unreliable-number parsing;
- recommendation-to-`ClocktowerDisplayOption` projection;
- style/warning metadata;
- misinformation-pressure projection;
- optional typed proposition projection.

`ClocktowerNightStepMaterializerRegistry` already owns identity-to-lazy-materializer binding and canonical projected interaction ordering. Another generic materializer family between these owners would add indirection without acquiring a distinct responsibility.

## Rejected shapes

The following are explicitly rejected for this campaign:

1. one new materializer file per remaining numeric role solely to reduce Host bytes;
2. `NumericRoleContext` / `NumericStepArgs` with nullable Chef/Empath registration fields;
3. a sealed or `when(role)` family whose only purpose is to redispatch into the existing builder;
4. moving `recommendationCoordinator`, history lookup, style/pressure policy, telemetry, publication, A4 prewarming or Recovery into a materializer;
5. normalizing the two Empath phase closures without a separate semantic audit.

## D6 consequence

D6.2t closes the current R1 numeric-materializer exploration. The remaining R1 gain is not large enough to justify another wrapper around these roles.

The next higher-value boundary is the R2 interaction layer already identified by the global D6 audit: `ClocktowerNightStepUi.kt` still combines numeric/boolean candidate projection, AUTO/ASSISTED/MANUAL behavior, structured confirmation, registration controls and navigation.

### Next: D6.2u — NightStep numeric interaction ownership audit

Perform a read-only audit of the numeric display/confirmation path in `ClocktowerNightStepUi.kt` and its existing typed helpers (`StructuredNumberInformationUiModel`, information-decision preparation/foundation, numeric option preparation). Determine whether one cohesive numeric interaction owner can receive a narrow immutable model plus actions without creating `NightStepArgs` or moving decision/publication authority.

The audit must distinguish:

- pure display/model preparation;
- local transient selection;
- registration-choice UI;
- durable confirmation/publication callbacks;
- AUTO/ASSISTED/MANUAL policy ownership.

No production change is authorized by this audit document itself.

## Validation classification

This slice is documentation/read-only architecture work only. No production source, test, workflow, persistence schema, recommendation semantics, registry order or runtime behavior changed. Per `AGENTS.md` and `TESTING_STRATEGY.md`, no Android RED/GREEN or broad regression run is required for this docs-only checkpoint.
