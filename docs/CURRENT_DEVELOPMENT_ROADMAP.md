# CampBoardGameHost — Current Development Roadmap

> **Current authority: 2026-10-16.** Approved architecture and acceptance: [ARCH-CLEANUP-2 — single Session / ordered facts / practical recovery](ARCH_CLEANUP_2_SINGLE_SESSION_AND_PRACTICAL_RECOVERY_2026-10-16.md). Root [AGENTS.md](../AGENTS.md) sets permanent replace-and-retire and safe-recovery rules. This roadmap describes **intended work**, not finished implementation.

## Current live baseline

- Repository: Jazz0006/CampBoardGameHost; developer machine Mini MCP alias: clocktower; branch: cleanup/arch-cleanup-1-20261015; one open [Draft PR #315](https://github.com/Jazz0006/CampBoardGameHost/pull/315) targeting main, **unmerged**.
- 2026-10-16 starting production SHA: ec6596a56cd23981acdc3e30b2cfcf6b23820ca0; Android CI #4213 and R2 #3829 SUCCESS. These checks **do not** validate subsequent source edits or Android real-device flows.
- ARCH-CLEANUP-1 previous cleanup: old scoring / consistency / Shadow recommendation paths removed; B = YES for existing no-candidate model wire contract, C = YES for retired scoring paths, **A = PARTIAL** (second Compose PlayerCard state, non-atomic confirmed facts, incomplete observed-information logging, duplicate recovery/replay). Historical evidence: [ARCH-CLEANUP-1 audit](ARCH_CLEANUP_1_ARCHITECTURE_CONVERGENCE_2026-10-15.md).
- ARCH-CLEANUP-2 **A1 first seven source slices implemented; exact-head independent CI/R2 still missing**: confirmed poison/death/role/phase/attack/protect/slayer/nomination/vote/no-execution and NGJ Klutz actions use Session transactions. Night close no longer adds a redundant advance before actual commits; fallback invalidates only if no accepted Session step changed revision. Actual private information observation, not `Information`/`UnreliableInformation` narrative text, owns playerInputRevision/global cursor; repeat publication checks use live Session revisions. **Unresolved A1/A4:** untyped unreliable displayed information can still lack a proposition, first-night publish/observation/reveal isn't one transaction, registration journal remains separate; A3 UI progress authority and A5 Recovery are outstanding. Developer machine has Java 21/Gradle 9.5.0 but **no Android SDK 35**, so no local Android/JVM compile was run and Mini MCP `:app:testFast` is not allow-listed. **A1–A6 none is complete**. Do not merge main or run paid API tests implicitly.
- Draft PR #315 currently reports **mergeable=false / dirty** against main; both branches touched AGENTS.md and TESTING_STRATEGY.md. This must be resolved separately before eventual merge, without assuming a clean developer worktree proves mergeability.

## The only active implementation: A1 → A6

| Order | Slice | Required completed behavior / deletion |
| --- | --- | --- |
| **A1** | Single confirmed-step Session transaction | Host-valid step confirmation atomically writes true current state + ordered typed fact, idempotently. Audit/delete superseded scattered writers/revisions/synchronizers. |
| **A2** | Remove whole-bundle adoption | Retire both opening/residual one-click buttons and obsolete proposal/adopted state machines. Initial strategic joint planning stays; AI_ASSISTED confirms each recommended step, AI_AUTOMATIC automatically commits the current legal step, MANUAL shares the same rule-bound commit. |
| **A3** | Eliminate second mutable GameState | Session alone owns real Clocktower roles, status, phase and decisions; PlayerCard is presentation. Remove production cards.toClocktowerGameState() reverse reads and duplicated state-sync layers; Undercover remains unchanged. |
| **A4** | Complete structured actual history | Global-ordered actions + epistemic observations include exactly what each player was actually shown (including drunk/poisoned false numeric/boolean/pair/role outputs), setup commitments, manual overrides, nomination and all votes. No extra parallel event store. |
| **A5** | Safe minimal Recovery and code retirement | Save last reliable Session + typed history + minimum safe progress. Relaunch shows correct grimoire and previously published information, then resumes safely. No arbitrary-time GameState reconstruction, exact UI restore, full Undo, deprecated save compatibility. Remove unused replay/recovery adapters after consumer audit. |
| **A6** | Exact-head independent acceptance | Reviewed diff/deletion inventory, CI/R2, targeted invariants, Android Studio/ADB TB MANUAL/AI_ASSISTED/AI_AUTOMATIC with next steps/overrides/day/night/restart. No paid API calls without separately approved budget. |

**Non-negotiable engineering rule:** every new unified API must enumerate the old API/field/caller/recovery data/tests it replaces; complete migration and **delete** old paths, no ongoing two-source parity/sync. Preserve Host legality, private-information permissions, actual public knowledge and already validated TBGS/EvidenceLab read-only protocols. An unconfirmed draft or AI plan is not a game fact; a committed or shown result cannot be silently overwritten. A previous-step correction is not an A prerequisite.

## Product contract after cleanup (resume later)

1. One full joint opening strategic AI plan as soon as seating/show roles are known; do not separately plan Drunk.
2. Only the currently requested semantic answer for subsequent necessary decisions. AI never receives a menu of legal candidate IDs; Host validates each actual result, and skips model calls when the answer is rule-determined or cached recommendation remains valid.
3. Same-game strategy may continue through a verified Responses anchor; authoritative state and complete actual history always reside in Host. Unadopted model output must not advance a confirmed conversation anchor.
4. After A1–A6 and P1 end-to-end acceptance: **P2 AI-EVENT-JIT**, then **P3 PLAYER-CLAIMS**, **P4 CROSS-GAME-STRATEGY**, **P5 MULTI-NIGHT-FIELD-EVAL**. No new P2 features before A is closed.

## References

- [ARCH-CLEANUP-2 authoritative implementation contract](ARCH_CLEANUP_2_SINGLE_SESSION_AND_PRACTICAL_RECOVERY_2026-10-16.md)
- [NEXT implementation handoff](NEXT_DEVELOPMENT_HANDOFF.md)
- [Evidence-first testing](TESTING_STRATEGY.md)
- [AI conversation and long-term strategy target](AI_STORYTELLER_CONTINUOUS_CONVERSATION_AND_CROSS_GAME_MEMORY_2026-10-15.md)
- [TBGS/UGSM dated evidence only](TBGS_UGSM_DECISION_TIME_AND_AI_EVENT_INPUT_AUDIT_2026-10-15.md)
- [Previous roadmap checkpoint](archive/checkpoints/CURRENT_DEVELOPMENT_ROADMAP_PRE_CONTINUITY_SYNC_2026-10-15.md)



