# GSP-R1C2C-1C3B — Canonical Klutz Death-Trigger Ability Snapshot / Strict Recovery

> Date: 2026-10-08 Australia/Sydney
> Executable: [PR #273](https://github.com/Jazz0006/CampBoardGameHost/pull/273)
> Stage: **PENDING exact-head T4/CI and independent R2**; no premature C2C-1C closure.

## Design

The first C2C-1C3A proof relied on a positively recorded Poison action before death; it did **not** cover games without a Poisoner or any Poison action. The latest global `GameState` clears `poisoned` when a player dies, so using current `poisoned=false` cannot prove they were functioning when the ability triggered.

The authoritative `ClocktowerGameSession.commitGlobalActionFact` now inspects the **living pre-death canonical player state**, and for an actual living Klutz commits one immutable `KlutzDeathTriggerEvidenceV1` attached to the very `ActionFact.Death` or `ActionFact.Execution` that kills that player. It includes actualRole, wasAlive, wasPoisoned, and source game-state revision. **The UI cannot provide or override these fields.** Successful normal `Death`/ `Execution` actions for other roles keep their existing payload and canonical digest.

The app's real **Day Klutz execution** path now commits the execution action *before* `synchronizePlayerDeathWithinCurrentRevision`, and excludes the duplicate event-to-action projection after public event emission. The actual night/dawn death path already commits the action before clearing the player's poison status. Old Death/Execution records with no Klutz evidence remain genuinely UNKNOWN, never `functioning` by default.

`ClocktowerSemanticHistoryPersistence` encodes/decodes an optional, strictly typed `klutzDeathTrigger` object on the existing canonical action timeline. The new evidence is included in the exact action fingerprint and the frozen causal decision prefix. No second mutable recovery-state table or fake private observation is created.

At the real Klutz player-choice confirmation, `KlutzDeathTriggerProvenanceResolverV1` prefers the **captured death-time evidence**. If the Klutz was confirmed poisoned at death, no functioning/registration decision is produced even though the current dead Klutz appears unpoisoned. If the Klutz was confirmed sober at death, the producer may record the separate explicit Spy-as-GOOD alignment ruling before loss/phase transition. The player chose the target; only the registration is the Storyteller's discretionary decision. The resulting `DayAbilityRegistration` carries the anchored death action ID and sequence, a `SESSION_TRIGGER` provenance discriminator and the original predeath game revision.

## Compatible historical records and safety limits

Earlier accepted C2C-1C3A records that rely on a proven older poison-target action retain their old exact causal fields and strict replay validation. This is migration compatibility, **not permission to infer healthy death-time state from missing Poison history**. A death without snapshot or independently verified old poison chronology still abstains.

The authoritative capture reflects the game's canonical pre-death `poisoned` and actual role at the action boundary, not an invented effect. Future source-ability-invalidation rules must update canonical poisoning state before death. The mechanical *player Klutz choice* itself is still only localized; a separate ActionFact/choice-history coverage audit is required before calling overall GSP-R1C2C complete.

## Acceptance gates

- Tests: actual unpoisoned Klutz death with **no Poison action** still yields a functioning death-trigger proof; actual execution and night death both capture it; poisoned at death stays impaired despite later cleared flag; type-only Spy Good outcome, strict causal Recovery/as-of, malformed payload and old pre-snapshot compatibility.
- The same exact PR SHA must pass full Android JVM suite/debug APK, ASP contract tests and Real Clingo (T4), plus independent R2 boundary and no unresolved reviews.
- On acceptance: **C2C-1C3B COMPLETE** only if the real production source and restore gates pass; then run a **C2C-1C registration coverage re-audit** before claiming C2C-1C/1 closed. C2C-2/3, GSP-R2/R3/R4 and LLM API are explicitly out of scope.
