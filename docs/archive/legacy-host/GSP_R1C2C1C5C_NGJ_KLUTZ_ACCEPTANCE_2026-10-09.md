# GSP-R1C2C-1C5C — No Greater Joy Klutz Learned-Death and Public Choice Acceptance

> Date: 2026-10-09 Australia/Sydney
> Status: **C2C-1C5C COMPLETE / ACCEPTED**, **C2C-1C / C2C-1 / GSP-R1C2C / R1C IN PROGRESS**.
> Executable: [PR #280](https://github.com/Jazz0006/CampBoardGameHost/pull/280), squash merge `1f457783341d5bc9c230f804eac20e653cac6a04`.
> Exact accepted executable head `ca57affc3ecb3c27a77d58a78fc7f89d1e0cd66c`, **CI #3960 T4 GREEN** (Android FULL + debug APK, ASP, Real Clingo), **R2 #3599 GREEN**, mergeable-clean and zero unresolved review threads.
> First CI #3959 failed at Kotlin compilation due to an unhandled `PublicKlutzChoice` branch in enumerated historical replay; the accepted follow-up exact head includes the proper world-neutral player-event handling.
> Full scope: [C2C-1C5C learned-death/choice contract](GSP_R1C2C1C5C_NGJ_KLUTZ_LEARN_CHOICE_SCOPE_2026-10-09.md).

## Production & Recovery coverage

- Engine-side `NoGreaterJoyKlutzHistoryProducerV1` derives an actual NGJ Klutz's **learned-of-death** event from the **prior committed real** Death/Execution action and an actual already-dead Klutz. It does NOT synthesize a learned event when old history has no predeath proof. The trigger is captured at the Host's public dead-player announcement / entry to the Klutz selection UI, not retroactively at night death.
- An entirely separate **player-chosen** `KlutzChoice` is committed when the player confirms a **living** target before Host result/phase changes. This mechanical player action is NOT a discretionary Storyteller registration or recommendation.
- Current NGJ script contains no Poisoner and a real Klutz cannot simultaneously be the Drunk; this **script-limited** functioning confirmation is not generalized to future mixed/poisoning scripts. The original `KlutzDeathTriggerEvidenceV1` still describes **death time**, not learning time. No Klutz/Spy in valid TB/NGJ roster is introduced.
- Both action facts have ordered `GLOBAL_V1` identities, typed action draft/commit, strict semantic JSON, formal JSON, stable canonical fingerprints, and current-format Recovery. Recovery validates genuine prior death, actor identity, script/phase, unique learned/choice event, public target existence/alive-at-choice, referenced earlier learned action and causal sequence. Forged/unanchored events reject safely.
- Public history reveals the confirmed chosen seats but **does not** leak actual Klutz role, ability functioning, target alignment, or a false world constraint. `EnumeratedHistoricalWorldReplay` treats the **choice alone** as world-neutral evidence.
- Real NGJ tests verify death/announcement/choice ordering, idempotence/duplicates, script roster, dead-target rejection, legacy unknown, JSON round-trip, public replay, successful pending learned-death recovery, and forged-choice Recovery rejection. No inferred role-based policy or fake private observation.

## Next authority

**GSP-R1C2C-1C6 — combined registration/day-action producer coverage and strict Recovery audit**: Verify the complete set of *reachable* TB/NGJ families, preflight/commit/Recovery ordering, legacy partial prefixes, and current known product gaps. Do not declare C2C-1C overall complete until every reachable family has an acceptance verdict. Remaining C2C-2/3 cover other confirmed private results and setup choices. GSP-R2/R3/R4 or production LLM/provider authority remain out of scope.
