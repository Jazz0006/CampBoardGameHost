# GSP-R1C2C-1C2 — Virgin / Spy Townsfolk-Type Day Registration Acceptance

> Date: 2026-10-08 Australia/Sydney  
> Status: **GSP-R1C2C-1C2 COMPLETE / ACCEPTED**; **C2C-1C3 KLUTZ NEXT**. C2C-1C and GSP-R1C2C remain IN PROGRESS.  
> Executable PR: [#269](https://github.com/Jazz0006/CampBoardGameHost/pull/269); squash merge `3e702dcd8ac16a9c693feab8ee88ee9c15c6b7dc`.  
> Accepted exact head: `8cdcabfa54cec9c1d6dbfae715eb56cadb8e13b5`; **CI #3925 T4 GREEN** (Android FULL unit tests, debug APK, ASP contracts, Real Clingo), **R2 #3575 GREEN**. Zero unresolved review threads and a clean merge gate were verified.  
> Implementation semantics: [Virgin Townsfolk-type scope](GSP_R1C2C1C2_VIRGIN_TOWNSFOLK_TYPE_SCOPE_2026-10-08.md). Day-inventory reference: [C2C-1C audit](GSP_R1C2C1C_DAY_REGISTRATION_AUDIT_2026-10-08.md).

## Verified real production behavior

1. `ClocktowerHostScreen.kt`'s Virgin first-nomination confirmation now distinguishes three UI states: **unselected** (`null`), **explicit actual Spy** (`false`), **explicit Spy-as-Townsfolk** (`true`). It no longer records the old pre-decision localized `RoleAction` or invokes `chooseSpy(defaultRole="Washerwoman")`.
2. The Virgin control is explicitly **type-only**, labelled “登记为镇民 / Register as Townsfolk”; no named-role picker is shown. A first nomination by a Spy explicitly registered as Townsfolk is correctly described in the UI as a player **registering** as Townsfolk, not an actual Townsfolk player.
3. The real `CampBoardGameHostApp.kt:onVirginNomination` captures an explicit Spy ruling **before** `clocktowerVirginUsed`, the Virgin execution, public alive events or phase/result mutation. Nomination by a player is still the player's action, not the Storyteller's choice. The old public AliveAt preflight is retained with the corrected localized-event offset.
4. `DayAbilityRegistrationRulingProducerV1.confirmVirginSpy` validates current GLOBAL_V1 game identity, script, day/round revisions, true healthy living Virgin and true healthy living Spy, rules-owned **Townsfolk CHARACTER_TYPE** availability, and confirmed execution consistency. An explicit **special** outcome writes one `RegistrationFact` with `registeredRole = null`, `registeredType = TOWNSFOLK`, `registeredAlignment = GOOD`, `registrationQuestion = CHARACTER_TYPE`, `reason = SPY_ABILITY`. An **explicit actual** outcome records an alternate typed decision with no special fact; untouched produces no typed decision.
5. The existing `StorytellerCausalDecisionJournalV1` and current-format Recovery strictly revalidate frozen actor/subject, life/poison and day state, type-specific payload/selected candidate, execution agreement, and forbid a forged named role. Same-interaction duplicate first-nomination captures are rejected; no private observation is synthesized.
6. Test coverage: type-only positive and negative explicit decisions, absence of private observation, causal as-of and round-trip Recovery, disallowed duplicate, forged/malfunctioning identity, illegal time/role, incorrect execution, and Recovery tampering. CI #3925 T4 and R2 #3575 passed on the same exact accepted head.

## Not completed, and next executable stage

- **C2C-1C3 — Klutz and Spy registration** remains pending. Do not treat a Klutz's **player-chosen target** as Storyteller discretion. A genuine explicit Spy-as-GOOD `ALIGNMENT` ruling must be captured before a Klutz win/loss and phase transition, with **death-time** poisoning/canonical context considered. Remove the remaining legacy `defaultRole="Washerwoman"` in Klutz UI; handle untouched versus deliberate actual and no invented named role.
- Virgin first-nomination **mechanical action fact** itself is not made a new globally typed ActionFact by this ruling slice; it remains a producer-coverage item. Legacy UI-only history cannot be upgraded retrospectively from string descriptions.
- Completion of Slayer #267 and Virgin #269 does **not** close C2C-1C, C2C-1 or wider R1C2C. Proceed with Klutz and re-audit remaining non-private/registration families before C2C-2/3. Do not begin GSP-R2/R3/R4 or LLM provider/API.
