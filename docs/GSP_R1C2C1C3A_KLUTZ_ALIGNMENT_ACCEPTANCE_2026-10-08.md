# GSP-R1C2C-1C3A — Klutz/Spy Alignment With Verified Death-Time Prefix: Acceptance

> Date: 2026-10-08 Australia/Sydney
> Status: **C2C-1C3A COMPLETE / ACCEPTED; 1C3B DEATH-TRIGGER COMPLETENESS NEXT. C2C-1C/C2C-1/R1C2C/R1C IN PROGRESS.**
> Executable: [PR #271](https://github.com/Jazz0006/CampBoardGameHost/pull/271); squash `e5653499200898cc0c74839d855370d10dba055c`.
> Exact accepted head `81775a83d8b40a5b38d24d55c143c2f4ca1b50db`: **CI #3932 T4 GREEN** (Android FULL, debug APK, ASP, Real Clingo), **R2 #3580 GREEN**, zero unresolved review threads and mergeable-clean gate.
> [Detailed decision and provenance scope](GSP_R1C2C1C3_KLUTZ_ALIGNMENT_DEATH_PROVENANCE_SCOPE_2026-10-08.md).

## Accepted behavior

The **player's Klutz choice** is separate from the Storyteller's **Spy-as-GOOD alignment** choice. The actual Host choice-confirmation callback now passes `effectiveSpyRegistersGood: Boolean` and **nullable explicit** `Boolean?`. Untouched is not an `EXPLICIT_ACTUAL` choice; explicit true and explicit false create different typed decisions. The old role grid and default `Washerwoman` have been removed for Klutz, and the premature localized Spy-registration event is no longer a causal authority.

The newly wired `KlutzSpyDayRegistrationProducerV1` writes a typed `DayAbilityRegistration` fact before any Klutz-choice win/loss, public event, phase or next-round mutation, only for a dead actual Klutz choosing a living functioning actual Spy. Special selection yields **only `registeredAlignment=GOOD`**, with role/type unset, and actual Spy selection yields no special `RegistrationFact`. No fake private observation, first-candidate witness or speculative role.

Critical proof boundary: the Session's **current dead Klutz poison flag is not death-time evidence**. Before recording typed history the Host requires a committed GLOBAL `ActionFact.Death`/`Execution` for that Klutz and an explicit earlier `ActionFact.Poison` whose target was NOT Klutz. The producer persists exact death/poison action IDs and global sequences, revalidates them against the **frozen before-choice prefix** on Recovery, and fails closed on forged causal content. If no such evidence exists or the last known poison target was Klutz, the ordinary game resolution still runs, but **no typed registration claim** is made. Repeated same-interaction captures are forbidden. Negative tests cover missing provenance, poisoned death, forged actor/subject, duplicate ruling and Recovery corruption.

During the first R2 #3577, an outdated shape guard still demanded a named-role Spy control in the Host; that guard was updated to require the *real* Virgin Townsfolk-type and Klutz Good-alignment controls, without banning future legitimate named-role controls. Exact final R2 #3580 passed. Earlier CI attempts are not accepted heads.

## Remaining scope — C2C-1C3B

This accepted **bounded** producer does NOT prove all valid Klutz deaths. E.g. a game with no Poisoner and therefore no recorded `Poison` event has **unknown** death-time provenance under current audit constraints, even when the Klutz was in fact sober; a source ability may be invalidated without a later poison action. The next increment must establish a Host/game-engine-owned **actual death-trigger functioning snapshot**, stable causal ID and full Recovery parity, rather than reconstructing a death-time condition from the dead player's current fields. Then audit and close Klutz 1C3, remaining day registration/event producers and C2C-2/3. No production LLM/provider API or GSP-R2/R3/R4 in this acceptance.
