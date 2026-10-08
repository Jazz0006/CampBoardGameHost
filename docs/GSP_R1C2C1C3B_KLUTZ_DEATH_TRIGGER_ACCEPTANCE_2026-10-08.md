# GSP-R1C2C-1C3B — Canonical Klutz Death-Time State / Recovery Acceptance

> Date: 2026-10-08 Australia/Sydney
> **2026-10-08 semantic/reachability correction:** [C2C-1C4 audit](GSP_R1C2C1C4_LIVE_DAY_REGISTRATION_REACHABILITY_AUDIT_2026-10-08.md) supersedes the earlier claim that Klutz/Spy registration is a reachable production vertical: TB has no Klutz, NGJ has no Spy. This stage's **pre-death evidence infrastructure** remains merged and T4-tested, but `wasPoisoned` when dying does not itself prove ability functioning **when Klutz learns of death**. Treat live choice/ability coverage as unaccepted pending 1C5A. Historical exact CI/merge results remain unchanged.  
> Status: **C2C-1C3B COMPLETE / ACCEPTED; C2C-1C4 NON-PRIVATE REGISTRATION COVERAGE RE-AUDIT NEXT**. C2C-1C, C2C-1, R1C2C/R1C remain IN PROGRESS pending audit.  
> Executable [PR #273](https://github.com/Jazz0006/CampBoardGameHost/pull/273), squash merge `fb67736fe5659a7601bdda5f6962afe5d5c87ed7`.  
> Exact accepted head `0e3b969a3242a35353ec3d33f1caa8267e0e177d`; **CI #3939 T4 GREEN** (Android FULL/debug APK, ASP, Real Clingo), **R2 #3585 GREEN**, no unresolved review threads, verified mergeable-clean gate.  
> Bounded implementation details: [C2C-1C3B proof scope](GSP_R1C2C1C3B_KLUTZ_DEATH_TRIGGER_SCOPE_2026-10-08.md). Historical compatibility: [C2C-1C3A](GSP_R1C2C1C3A_KLUTZ_ALIGNMENT_ACCEPTANCE_2026-10-08.md).

## Delivered / tested

1. The **authoritative ClocktowerGameSession** now stamps a typed pre-death `KlutzDeathTriggerEvidenceV1` onto the same canonical `ActionFact.Death` or `ActionFact.Execution` used by the game, only while the actual Klutz is alive. It captures the actual role, prior alive flag, prior poisoned flag and exact source game-state revision. Callers cannot supply their own death-trigger proof.
2. The real day execution path commits Klutz's `Execution` **before** death removes poison, then emits the public event without writing the same action a second time. The dawn/night Death path already commits before death synchronization.
3. The predeath evidence is stored through the existing `ClocktowerSemanticHistoryPersistence` action-timeline JSON and contributes to canonical action identity and frozen causal history. For preexisting actions with no proof, historical canonical payload and saved evidence remain unchanged; absence does not become an assertion of functioning.
4. `KlutzDeathTriggerProvenanceResolverV1` uses the directly committed death-time state when present. A poisoned-at-death Klutz cannot become a functioning Klutz merely because the dead player's current poison flag has been reset. An unpoisoned Klutz can now yield a verified typed Spy-as-GOOD alignment ruling even when no Poisoner, Poison event or prior poison target exists.
5. Current-format Recovery validates the exact original death action ID, global sequence, predeath revision, registered Spy subject/choice and the frozen historical prefix. Accepted C2C-1C3A decisions using their old positive poison chronology remain replayable under their existing exact schema; no retrospective backfill is invented.
6. Regression tests passed: unpoisoned Klutz with **no poison action**, poisoned Klutz then cleared death flag, day execution versus dawn death, no duplicate action, malformed/forged death evidence, typed journal restore and historical cutoff parity.

## Remaining audit before family closure

**Do not mark overall C2C-1C complete solely because three named abilities are wired.** C2C-1C4 must re-check all real day and non-private producers, death-trigger functioning versus when the player learns of death, duplicate/missed action sources, arbitrary poison-source invalidation, legacy old-game UNKNOWN semantics, and whether mechanics (not only causal-history capture) use the verified ability state correctly. Player-selected Klutz target is still not itself a complete globally typed mechanical ActionFact.

Preserve the distinction between the player choosing a target, the game engine resolving the rule, and the Storyteller's explicit registration ruling. No LLM/provider integration or GSP-R2/R3/R4 follows from this acceptance.
