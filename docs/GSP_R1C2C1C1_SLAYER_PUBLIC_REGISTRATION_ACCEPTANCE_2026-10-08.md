# GSP-R1C2C-1C1 — Public Slayer/Recluse Typed Causal Registration Acceptance

> Date: 2026-10-08 Australia/Sydney  
> Status: **GSP-R1C2C-1C1 COMPLETE / ACCEPTED; C2C-1C2 Virgin NEXT; 1C3 Klutz PENDING; C2C-1C, C2C-1, R1C2C/R1C OVERALL IN PROGRESS.**  
> PR: [#267](https://github.com/Jazz0006/CampBoardGameHost/pull/267); squash merge `57cfa93c6638c8c4f9e6e4b6feb1af5ddda65ed0`.  
> Exact accepted head: `b43dc81d04e3d1e05b18ad84cc0e27967ce39c87`; **CI #3919 T4 GREEN** (Android FULL unit tests + debug APK, ASP, Real Clingo) and **R2 #3571 GREEN**. No unresolved review threads at acceptance.

## Actual Host producer, scope, and legality

- Production confirmation is the **real `CampBoardGameHostApp.kt` Slayer shot** callback. Only when the actual living, functioning Slayer can apply the shot, the selected living healthy target is a Recluse, the target is explicitly adjudicated as registering Demon and the shot has not already been used does the Host call `DayAbilityRegistrationRulingProducerV1.confirmSlayer`.
- The producer confirms against **the current Trouble Brewing rules domain** (exact Imp/Demon legal option). It captures an immutable pre-action day snapshot, revision, global history cutoff and prior causal decisions **before the shot changes ability-used, death, alive observations or phase**. The registration is typed `RegistrationQuestion.DEMON` / `RegistrationReason.RECLUSE_ABILITY`, with the exact Imp role, Demon type, Evil alignment and interaction-local subject.
- This new `StorytellerProviderDecisionContextV1.DayAbilityRegistration` is **independent of private-information result confirmation**. No fabricated private observation or automatic deduction from a candidate/witness is created; false or untouched UI selections create no special ruling. The player's shot target and the mechanical death remain player action / rules effect, not an additional Storyteller recommendation.
- `StorytellerCausalDecisionJournalV1` and `ClocktowerCausalJournalPersistence` admit these facts only after verifying the dedicated day registration producer. Current-format Recovery validates the frozen actor/subject roles and life/poison status, DAY phase, confirmed candidate and registration fields, preserving the original history prefix and as-of chronology; malformed recovered rulings fail closed.
- Exact unit tests cover a functioning confirmed ruling, no private observation, journal duplicate rejection, strict Recovery parity/tamper rejection, non-Slayer claimant, poisoned/dead subject, poisoned actor and illegal role/phase. They passed on the exact accepted full-ci head.
- No claim that missed Slayer shots have a complete typed mechanical global action producer. This is a **Storyteller explicit registration ruling** slice, not broad public event capture.

## Open gaps / next

[Day-time producer audit](GSP_R1C2C1C_DAY_REGISTRATION_AUDIT_2026-10-08.md) identifies:

1. **C2C-1C2 Virgin**: capture the actual first-nomination interaction and Spy-as-Townsfolk *type* (not default Washerwoman role), distinguish explicit good / explicit actual / untouched, before irreversible Virgin execution. Keep the player's nomination separate from Storyteller discretion and do not invent a role.
2. **C2C-1C3 Klutz**: capture the dying player's choice of Spy and any **explicit** Spy-as-Good alignment determination before loss/phase transition. Check the interaction-time poison/death provenance and preserve old unknown state.
3. Re-audit other day/role interaction producers, legacy-only UI events, missing global action facts and strict Recovery/correction coverage before closing C2C-1 or R1C2C. Other C2C-2/3 producer batches still remain; GSP-R2/R3/R4 and production LLM API are **not authorized** by this acceptance.
