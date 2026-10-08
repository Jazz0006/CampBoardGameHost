# GSP-R1C2C-1C5C — No Greater Joy Klutz Learned-Death and Player-Choice History

> Date: 2026-10-09 Australia/Sydney
> Phase: **IMPLEMENTATION / EXACT T4 AND R2 PENDING**
> Baseline: `main@d1480f96dd83c900280944eb91b2f84c913e84b8` clean, C2C-1C5B #278 T4 accepted.
> Limits: NGJ current fixed production script only. No imaginary Trouble Brewing Klutz/Spy producer, no LLM provider, no Storyteller discretion.

## Authority and chronology

The official Klutz ability states **“When you learn that you died, publicly choose 1 alive player: if they are evil, your team loses.”** The original death time is **not** the ability-choice/learning time. NGJ has **no Poisoner**, and its actual roster requires a Klutz who is not Drunk. The producer therefore evaluates confirmed learned-of-death functioning only in the real No Greater Joy script; it refuses the other script and unproven old death histories, rather than inferring a learn trigger from a corpse's reset poison flag.

- **Death/Execution** remains the original canonical, independently persisted mechanical event and predeath-state snapshot. It is NOT the learned-death trigger.
- **`KlutzLearnedDeath`** is committed when the Host actually announces day execution / night death and hands off to the public Klutz choice screen. It links the real preceding Death/Execution action ID and records the current script-supported functioning status, separately from any death-time poisoned value. The true Klutz role and functioning are hidden from the player-safe replay; no fake private observation is invented.
- **`KlutzChoice`** commits only upon the actual player's confirmed public selection of an alive target, before the existing outcome/public event/phase change. It references the earlier learned action ID and stores only the chosen/actor seats in player-safe history. The engine's real NGJ choice-resolution rule still owns outcome; no Spy/Recluse registration is involved.
- Type/phase/seat/domain/ordering/one-shot controls are independently checked by draft/domain, canonical global timeline, JSON, and strict current-format Recovery. A forged choice with no prior learned action or mismatched death reference is rejected. Legacy pending Klutz Recovery with no canonical death proof is **UNKNOWN**, with no manufactured causal fact: existing gameplay remains available rather than backfilling fake events.

## Code owners

- `clocktower/domain/ActionFact.kt`, `clocktower/epistemic/ActionFactDraft.kt`: distinct immutable player/mechanical facts.
- `NoGreaterJoyKlutzHistoryProducerV1.kt`: deterministic Host engine-state preflight, linked proof and legal roster.
- `CampBoardGameHostApp.kt`: real public announcement transition plus confirmed selection boundary, before irreversible result.
- `ClocktowerSemanticHistoryPersistence`, `EpistemicSemanticJson`, `EpistemicSemanticModel`: full durable and canonical representations, strict typed fields.
- `RecoveryRestorePlanner`: exact chronological verify; player-safe projection hides learn-time role/ability facts and retains public selection seats.

## Acceptance criteria

T4 Android JVM full and debug assemble, ASP and Real Clingo plus independent R2; meaningful NGJ real-roster tests for night death, learned-before-choice, real player selection, repeated confirmation, old archive UNKNOWN, private metadata not leaked and malformed/forged Recovery. This is a bounded **NGJ mechanics/history** slice, not a generic cross-script poison timing engine or a standalone all-producers coverage claim. After acceptance, re-audit C2C-1C whole family before declaring complete; C2C-2/3 and GSP-R2/LLM remain out of scope.
