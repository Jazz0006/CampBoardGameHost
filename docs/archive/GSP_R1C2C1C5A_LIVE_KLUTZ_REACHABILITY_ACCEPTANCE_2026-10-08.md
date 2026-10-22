# GSP-R1C2C-1C5A — Real-Script Klutz Reachability and Unreachable Producer Retirement Acceptance

> Date: 2026-10-08 Australia/Sydney
> Status: **C2C-1C5A COMPLETE / ACCEPTED**, while **C2C-1C / C2C-1 / GSP-R1C2C / R1C remain IN PROGRESS**.
> Executable: [PR #276](https://github.com/Jazz0006/CampBoardGameHost/pull/276), squash `32f7a02d5f8d4a0320c5d50e52828f82bd579714`.
> Exact accepted HEAD `45f8709ff4d4d1a666e0215b7812e79ea49fc148`; **CI #3948 T4 GREEN** (Android FULL + debug APK, ASP contracts, Real Clingo), **R2 #3591 GREEN**, zero unresolved review threads, mergeable-clean gate.
> Scope: [1C5A implementation contract](GSP_R1C2C1C5A_REACHABLE_KLUTZ_RETIREMENT_SCOPE_2026-10-08.md); [1C4 failed producer-reachability audit](GSP_R1C2C1C4_LIVE_DAY_REGISTRATION_REACHABILITY_AUDIT_2026-10-08.md).

## Accepted production behavior

- Proven using `clocktowerRolesForScript` and `clocktowerRoleDefinitionsForScript`: Trouble Brewing includes Spy/Recluse/Virgin/Slayer and **not** Klutz; No Greater Joy includes Klutz/Artist and **not** Spy/Recluse/Virgin/Slayer or Poisoner. Regression contracts explicitly disallow treating synthetic `TB+Klutz+Spy` as live coverage.
- The actual `CampBoardGameHostApp.kt:onConfirmKlutzChoice` now uses **NoGreaterJoyKlutzChoiceRuleV1.resolve**, validating actual No Greater Joy role roster, a real dead Klutz, player-chosen living target and its true alignment. Choosing evil gives the Klutz's good team loss; choosing good does not. This is a rules-owned player-choice outcome, not a Storyteller choice or a special-registration witness.
- `ClocktowerHostScreen.kt` no longer exposes a fictitious Klutz/Spy alignment registration picker; the former control and live TB Klutz/Spy causal producer were removed from main sources. R2 now safeguards the real TB Virgin type-only control and prevents reintroduction of the impossible Klutz picker.
- Historical C2C-1C3A/1C3B synthetic event archive validation remains strict and **read-only** in `LegacyKlutzSpyRegistrationRecoveryValidatorV1`; its synthetic writer and tests are retained solely in the **test** tree as a legacy JSON/correction replay fixture. Old history is not silently upgraded, removed or declared a real script.
- The useful predeath `KlutzDeathTriggerEvidenceV1` still belongs to `ClocktowerGameSession`, attached to canonical global Death/Execution and durable in Recovery JSON, and is tested with real No Greater Joy roster. Preserving it does not mean death-time impairment proves ability condition **when the player learns of death**.
- Current-format Recovery regression checks a real No Greater Joy pending Klutz learning/choice checkpoint through the restore planner (including Klutz's dead seat and `pendingKlutzName`, `klutzReturnToDawn`) and the existing legacy archive decoder negative/tampering paths.

## Remaining obligations

The current NGJ script contains no Poisoner, which makes the actual supported mechanics deterministic for a true Klutz; **this is not an authority for hypothetical scripts** with changing impairment. Future/mixed-script Klutz resolution requires a genuine **learned-of-death** ability-state capture, and a globally ordered, typed **player choice** action. A death snapshot is not interchangeable with either.

**Next: GSP-R1C2C-1C5B — reachable TB Slayer/Recluse and Virgin/Spy player actions and registration correction.** Specifically address Slayer's explicit actual-vs-untouched distinction, ineffective-shot false registration narrative, globally typed shot/spent and Virgin nomination/vote replay and Recovery. Then audit NGJ Klutz learned-of-death action coverage as its independent mechanical-history lane before overall C2C-1C closure. C2C-2/3 information/setup producers remain open. No GSP-R2/R3/R4 or production LLM/provider integration.
