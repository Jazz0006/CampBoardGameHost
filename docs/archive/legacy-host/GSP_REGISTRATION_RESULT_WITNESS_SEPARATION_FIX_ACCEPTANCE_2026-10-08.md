# Registration Result–Witness Separation — Production Fix Acceptance

> Date: 2026-10-08 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Status: **GENERIC RESULT–WITNESS SEPARATION ACCEPTED; TYPED CANONICAL RULING PRODUCER/RECOVERY STILL PENDING**  
> Executable PR: [#254](https://github.com/Jazz0006/CampBoardGameHost/pull/254), squash merge `b4d6fd0eb0f0b00025e5f82aa45fd7dbdd6bce7a`  
> Independent validation: CI **#3860 GREEN** (Android FAST) / R2 **#3525 GREEN**. Android full build, external solver and ASP suites were *not* selected on the exact final executable commit. Do not call this a full T4 checkpoint.
> Design authority: [Spy/Recluse Result-First Registration Ambiguity](GSP_REGISTRATION_AMBIGUITY_RESULT_FIRST_HISTORY_CONTRACT_2026-10-08.md).

## Root-cause correction, not an Empath-specific rule

Previous code in `ClocktowerRegistrationResultDomain.kt` used `distinctBy` on the visible result and retained the first candidate's Spy/Recluse witness. The Host's `onApplyRecommendedDisplayOption` then automatically applied and recorded that witness. Even a truthful numeric result of **1** could therefore silently assert an explanation never explicitly chosen by the Storyteller.

PR #254 repairs the **generic shared projection and callback**:

1. `distinctClocktowerFinalInformationResults` now groups all legal witness candidates per player-visible typed option, preserving the complete distinct alternatives in `ClocktowerDisplayOption.legalRegistrationWitnesses` while clearing the option-level witness ruling flags and role names. Grouping is idempotent, stable with candidate order variation, and works across result families; *none* of the witnesses becomes a canonical adjudication merely by grouping.
2. `ClocktowerRegistrationResultPresentation` numeric, Fortune Teller boolean and role-reveal producers share the same grouping and separation.
3. `ClocktowerRegistrationInteractionState` only treats `chooseSpy`/`chooseRecluse` as explicit manual selections. Implicit default false is not an explicit Spy ruling. No `applyRecommendedWitness` side effect on displaying a result.
4. `ClocktowerNightStepUi` + `ClocktowerHostScreen` show a conflict warning and prevent publishing a result when it contradicts an **explicit** manual choice; compatible manual choices may still be recorded once. The transition `advanceNightStep` cannot quietly record an untouched default as a ruling.
5. `ClocktowerChefSquareTableUi` and `ClocktowerEmpathSquareTableUi` do not render an unresolved candidate witness's specific contribution as if it had been selected. Actual evil identities can remain separate Storyteller-only hints.
6. The previous test expecting “duplicate final result keeps the first registration witness” was replaced by the opposite invariant. Regression checks cover **Chef, Empath, Fortune Teller, role reveal, order reversal, idempotence, explicit/manual mismatch, no silent records, and ambiguous square-table highlights**.

## Example: same result, multiple explanations

When a functioning Empath is between Spy and Recluse and receives **1**, A (Spy registers Evil, Recluse Good) and B (Spy Good, Recluse Evil) can both explain it.

Chef has an equivalent general ambiguity: adjacent `Spy — Imp — Recluse` can produce **1** via either the `Spy–Imp` pair or the `Imp–Recluse` pair, depending on the interaction-local registrations.

**Only the visible numeric result is selected.** The legal candidate explanations are derived, not written as authoritative past rulings.

## Explicit remaining limits

- This is **not** a new canonical, typed `ExplicitRegistrationRuling` producer. Existing localized `ClocktowerEventType.RoleAction` events are UI history; they cannot be parsed or promoted to Provider historical ground truth.
- Deliberately unresolved and legacy unavailable/unrecorded states are not yet durably differentiated in Recovery. That work belongs to a dedicated registration recording/persistence slice, prior to claiming complete registration history coverage.
- Revisit optional advanced controls to explicitly choose one legal explanation, including what to display for an explicitly selected interpretation. The fix primarily removes *implicit* registration authority; it does not yet implement full explicit-ruling visual annotation or complete multi-interaction Recovery.
- Keep producer/result mapping generalized by **typed visible result -> all legal alternative witnesses -> optional deliberate selection**, never per-role special cases.
- GSP-R1C2 still needs **durable at-decision history/correction causality**. This UI fix is a prerequisite for reliable future recording, not a substitute for R1C2.
- No production LLM API or recommendation authority was added.

## Future regression guard

If adding another information character or another special-registration rule, the authoritative test is not “did the first witness produce a legal result?” It is **“when two or more witnesses produce the same player-visible result, does selecting that result leave adjudication unresolved until a Storyteller expressly chooses?”** Also verify recorded history/Recovery and Storyteller-only diagrams do not elevate an arbitrary explanation.
