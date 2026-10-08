# GSP-R1C2C-1C6C — Generic Drunk Shown-Identity Recovery Gate

> Date: 2026-10-09 Australia/Sydney
> Source: valid post-merge **P2** review on PR #284, thread `PRRT_kwDOTN0Fp86qjJ-S`.
> Status: executable T4/R2 pending, not accepted until exact-head checks pass.
> Supersedes no part of the real TB/NGJ public-day facts; closes a missing **setup-contract invariant during Recovery**.

The accepted 1C6B repair correctly uses `AbilityFunctioningSemantics.perceivedRole` for actual Drunk shown Virgin on their first nominal interaction, even if they die later. But it allows a malformed recovered roster with **both an actual Virgin in play and an actual Drunk also SHOWN Virgin**. `SetupShownIdentityPolicy.resolveGenerated/validateDrunkOptions` already forbids that: an actual Drunk's shown character must be **a Townsfolk not already present in actual in-play roles**. Such a combination cannot originate from a legal current setup.

Implement this invariant at `RecoveryRestorePlanner.validateClocktower` immediately after validating every player's actual and shown role, for every actual Drunk (TB or NGJ). Require shown role belongs to Townsfolk and is absent from the recovered actual in-play role set. Do NOT special-case the Virgin; the later first-nomination `perceivedRole` rule remains correct and retains legal Drunk shown Virgin when Virgin is **not** actually in play. Do NOT re-infer ability functioning from whether a dead Drunk is currently alive or poisoned.

Regression: legal 8-seat TB 5 Townsfolk + Drunk(shown Virgin) + Spy + Imp remains Ready even after later death; same legal composition with **actual Virgin substituted for Investigator** and Drunk still shown Virgin must be REJECTED. Also reject the invalid shown-role combination **without a Virgin nomination event at all**: this is a generic recovered setup identity gate, not an incidental special-day interaction validation.

On accepted full Android FULL/debug APK, ASP, Real Clingo and R2, reply/resolve original #284 P2 thread and add a closure note to roadmap and handoff. The C2C-1C day family stays complete **with these post-merge corrections**; C2C-1 / R1C2C / R1C remain IN PROGRESS, next C2C-2 information results. No ML/provider API.
