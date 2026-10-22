# GSP-R1C2C-1C2 — Virgin First Nomination / Spy Townsfolk-Type Registration Scope

> Date: 2026-10-08 Australia/Sydney
> Scope: executable C2C-1C2 **Virgin** producer only; Klutz and other day producers remain IN PROGRESS.
> Implementation: PR #269. Acceptance requires exact-head full Android/debug APK, ASP, Real Clingo and R2.

## Verified semantic decision contract

- The **player** nominates the Virgin; it is not a Storyteller recommendation. When a *living, functioning* actual Virgin is nominated for the first time by a *living, functioning* actual Spy, the Storyteller may explicitly decide whether the Spy registers as **TOWNSFOLK CHARACTER_TYPE**.
- UI manual state is three-valued: untouched = `null` (NOT an explicit decision); explicit actual = `false`; explicit Townsfolk = `true`. The actual outcome is checked against the confirmed choice, but absence must never be promoted to a positive `EXPLICIT_ACTUAL`.
- **No named Townsfolk role is required or selected**. Remove old `defaultRole="Washerwoman"` and its Virgin role-picker. An explicit special `RegistrationFact` has `registeredRole=null`, `registeredType=TOWNSFOLK`, `registeredAlignment=GOOD`, `registrationQuestion=CHARACTER_TYPE`, `reason=SPY_ABILITY`. It is not evidence of having registered as Washerwoman.
- A real confirmed Virgin outcome may execute the nominator immediately. Capture the rules-legal explicit registration at the App's real `onVirginNomination` boundary BEFORE `virginUsed`, player death, public alive observations, game-result calculation and phase advance. The existing mechanical execution and public `AliveAt` remain separate.
- The daytime `StorytellerProviderDecisionContextV1.DayAbilityRegistration` is the decision family. It does not create a private/night information observation or reconstruct chronology from localized RoleAction.
- `DayAbilityRegistrationRulingProducerV1.confirmVirginSpy` validates current game ID/script/revisions/day, the *actual* healthy living Virgin and Spy, rules-owned Townsfolk-type permission, and agreement between explicit ruling and confirmed execution. Explicit actual produces an empty registration fact list, *not* a special witness. Duplicate first-nomination decision IDs are rejected.
- The journal invokes strict `DayAbilityRegistrationRulingProducerV1.validateCommitted` on both live commit and current-format Recovery restore. It rejects changed actor/subject, invalid status, execution mismatch, poison, a fabricated named role, and invalid `CHARACTER_TYPE`/alignment. The current-format causal JSON schema is unchanged.
- Old UI-local history and `LEGACY_LOCAL` cannot be reclassified as typed causal proof. The lost/untouched state remains `UNAVAILABLE_OR_UNRECORDED` for historical decisions; do not infer `UNRESOLVED_NOT_REQUIRED`.

## Explicit gaps / follow-up

- The first-nomination fact itself is not newly added as a typed *mechanical* ActionFact; this change only captures the genuine explicit Storyteller registration ruling. Existing public execution/death observations remain separately owned by the runtime.
- C2C-1C3 must separately capture **Klutz's player-chosen Spy** as an explicit `ALIGNMENT=GOOD` registration at the actual choice/outcome boundary, with the original death time and no default Washerwoman role.
- Other incomplete information/action producers from the C2C-0 audit are not closed. GSP-R1C2C-1C, GSP-R1C2C, GSP-R1C remain IN PROGRESS. No new LLM provider or R2 phase work.
