# GSP-R1C2C-1C3A — Klutz / Spy Choice-Time Alignment and Death-Provenance Contract

> Date: 2026-10-08 Australia/Sydney
> Status: **IMPLEMENTATION PENDING EXACT CI/R2 ACCEPTANCE**. This is a bounded, fail-closed Klutz vertical; no claim of exhaustive death-trigger coverage.
> Executable: [PR #271](https://github.com/Jazz0006/CampBoardGameHost/pull/271)
> Preceding accepted stages: Slayer [#267](https://github.com/Jazz0006/CampBoardGameHost/pull/267) and Virgin [#269](https://github.com/Jazz0006/CampBoardGameHost/pull/269).

## Result and agency semantics

- The real Klutz **player** publicly chooses a living player after learning of death. That chosen seat is NOT a Storyteller recommendation. In the specific chosen-Spy situation, the Storyteller can deliberately make the actual Spy register GOOD. This is an **ALIGNMENT** fact; it does not imply a named Townsfolk or Outsider or even a specific registered type.
- Klutz UI now offers a specific `register as good` / `actual evil` toggle with explicit/untouched distinction and **no** default Washerwoman or role grid. The Host receives `effectiveSpyGood:Boolean` for normal mechanical resolution and `explicitSpyGood:Boolean?` for history. Untouched/null is **not** an explicit actual ruling. The original Klutz decision and win/loss handling is preserved.
- The Host-owned producer is invoked only on an actual Klutz-selection confirmation while the selected character is a living, functioning actual Spy and a real already-dead actual Klutz is pending; it freezes the pre-outcome snapshot before the localized player choice, game outcome, phase, or next-round mutation.
- It writes `StorytellerProviderDecisionContextV1.DayAbilityRegistration` with `RegistrationQuestion.ALIGNMENT`. Special produces exactly one typed `RegistrationFact(registeredRole=null, registeredType=null, registeredAlignment=GOOD, reason=SPY_ABILITY)`; explicit actual is a different typed outcome with no special fact. No fake private observation is created.

## Death-time functioning MUST be proven, not presumed

The canonical `GameState` reports the Klutz dead at choice time and normally clears its poison flag on death. This is **not evidence** the Klutz was sober when their ability triggered. `KlutzDeathTriggerProvenanceResolverV1` therefore requires:

1. An actually committed **global** `ActionFact.Death` or `ActionFact.Execution` for that Klutz.
2. A preceding, explicitly committed `ActionFact.Poison` establishing a known poison target **other than that Klutz** at the death sequence.
3. Stable identities, present living/healthy chosen Spy and actual dead Klutz in the frozen current game.
4. Exact death and last-prior-poison action IDs and global sequences, persisted in selected outcome; Recovery recomputes the proof from the original frozen global prefix and rejects changed death identity, chronology, outcome or registered alignment.

A missing Poison fact is **UNKNOWN**, not proof of no poison. If the last known poison target was the Klutz or the actual death is unrecorded, the typed producer **abstains**. Normal gameplay continues unchanged; no evidence is invented or silently backfilled on restart.

## Explicit remaining gap — C2C-1C3B

This narrow gate intentionally cannot cover a sober Klutz where the history has no affirmative poison-target event (for example, no Poisoner in play or an untracked historical poison state). It also cannot prove that a stale poison action continued to represent exact death-time mechanics if a source ability was invalidated without a later action. A separate **C2C-1C3B death-trigger snapshot/ability-provenance producer** should capture exact death-time functioning in the authoritative game engine/Host and persist/restore it without relying on the current dead-player poison flag or inventing a target. Only after this later audit can broad Klutz coverage be considered complete.

The existing global ActionFact timeline does not yet capture the *player's Klutz choice* as a standalone typed mechanical action. That must not be conflated with this Storyteller ruling. GSP-R1C2C-1C/R1C2C and GSP-R1C remain IN PROGRESS; other C2C-2/3 producer families, GSP-R2/R3/R4 and LLM provider work are not part of this slice.

## Execution gates

Exact PR head must pass independent R2 and full T4 CI (Android full unit tests + debug APK, ASP contracts, Real Clingo). Test representative positive alignment, explicit actual, no phantom role/type, duplicate decision, unknown death/poison, poison-at-death, false chosen identity, and tampered Recovery. A change to the structural R2 assertion must preserve existing architecture boundaries and should permit future legitimate named-role interactions.
