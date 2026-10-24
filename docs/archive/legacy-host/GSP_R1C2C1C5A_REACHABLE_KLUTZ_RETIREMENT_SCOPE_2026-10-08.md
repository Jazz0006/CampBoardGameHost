# GSP-R1C2C-1C5A — Real-Script Klutz Reachability Repair and Dead Producer Retirement

> Date: 2026-10-08 Australia/Sydney
> Execution: [PR #276](https://github.com/Jazz0006/CampBoardGameHost/pull/276)
> Status: IMPLEMENTED / PENDING exact-head full T4 plus independent R2 and merge.
> Authority: [1C4 reachability audit](GSP_R1C2C1C4_LIVE_DAY_REGISTRATION_REACHABILITY_AUDIT_2026-10-08.md).
> Scope: restore genuine **No Greater Joy** Klutz mechanical ownership, remove unreachable **Trouble Brewing Klutz/Spy** special-decision producer. **Not** a new Storyteller recommendation policy and **not** full historical player-choice capture.

## Actual script roster, not hypothetical pair combinations

`clocktowerRolesForScript(TroubleBrewing)` includes Spy/Recluse/Virgin/Slayer, but **not** Klutz or Artist.
`clocktowerRolesForScript(NoGreaterJoy)` includes Klutz/Artist/Imp/Baron, but **not** Spy/Recluse/Virgin/Slayer or Poisoner.
Production-catalog tests assert these membership facts and reject synthetic cross-script `TB + Klutz + Spy` as coverage evidence. Registration and mechanics may only use roles actually in the supported script.

## Removed unreachable authority

- `CampBoardGameHostApp.kt:onConfirmKlutzChoice` no longer invokes the TB-only `KlutzSpyDayRegistrationProducerV1.confirm` or checks `Spy-as-GOOD`. The actual Klutz's target is a *player's public choice*, not an independent Storyteller recommendation.
- The Klutz screen no longer shows a fictitious Spy-alignment picker, and the unused `ClocktowerSpyGoodAlignmentDecisionControls` was removed.
- The former synthetically successful event writer is **test-only**, explicitly named `LegacyKlutzSpyArchiveFixtureV1`. Historical synthetic producer tests are renamed `LegacyKlutzSpyArchiveRecoveryCompatibilityTest`. These fixtures establish **archive compatibility only**, never live production reachability.
- Production keeps only `LegacyKlutzSpyRegistrationRecoveryValidatorV1.validateCommitted` for strict read-only historical **already persisted** legacy events, including the exact old death/poison chronology schema and tamper checks. No production path creates such an event. The existing shared `DayAbilityRegistrationRulingProducerV1` delegates legacy `abilityRole=Klutz` only for strict restore.
- Real `KlutzDeathTriggerEvidenceV1` captured by `ClocktowerGameSession` on its canonical Death/Execution before death clears poison remains intact. This is **death-history evidence**, not a proof the learned-of-death trigger fired.

## Real No Greater Joy mechanical rule

`NoGreaterJoyKlutzChoiceRuleV1.resolve` accepts `GameState`, chosen/dead seat, and the authoritative script's `RoleDefinition` roster, rejects other scripts and any out-of-roster actual roles or injected Spy/Poisoner, and requires an **actual dead Klutz** selecting an **actual living player other than self**. The actual chosen alignment decides good-team loss, without a fabricated special Spy registration.

This bounded rule is safe for the currently supported **No Greater Joy** roster: there is no Poisoner and a true Klutz cannot simultaneously be Drunk. It **does not** generalize to new scripts or mixed/poisoning contexts. Klutz's printed ability triggers **when the Klutz learns that they died**, distinct from the prior death instant. `wasPoisoned` at death must not be silently interpreted as learned-of-death ability state. Cross-script support would require a separately captured learned-of-death trigger state.

Recovery regression coverage includes a realistic No Greater Joy pending Klutz choice restored from current-format active-game snapshot, the role roster, and the preserved canonical Klutz death action JSON. The player's selection remains a pending/confirmed UI state; an independent GLOBAL `KlutzChoice` ActionFact, learn-time trigger capture and repeated-confirmation / as-of action coverage are still incomplete.

## Scope and acceptance

After exact-head T4 (Android FULL/debug APK, ASP, Real Clingo) and R2 pass, 1C5A can be accepted as **production dead-code retirement + real NGJ mechanical fallback**; it does not close the overall C2C-1C or GSP-R1C2C coverage.

**Next: C2C-1C5B** — reachable TB Slayer/Recluse explicit-false vs untouched, ineffective-shot narrative contradiction, actual player-shot action / ability-spend history, and Virgin nomination action/recovery convergence. NGJ Klutz learned-of-death fact and choice action are separate real mechanical-history obligations. No GSP-R2/R3/R4 or LLM provider/API.
