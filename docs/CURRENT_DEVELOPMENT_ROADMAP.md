# CampBoardGameHost — Current Development Roadmap

> **Current authority: 2026-10-16.** Approved architecture and acceptance: [ARCH-CLEANUP-2 — single Session / ordered facts / practical recovery](ARCH_CLEANUP_2_SINGLE_SESSION_AND_PRACTICAL_RECOVERY_2026-10-16.md). Root [AGENTS.md](../AGENTS.md) sets permanent replace-and-retire and safe-recovery rules. This roadmap describes **intended work**, not finished implementation.

## Current live baseline

- Repository: Jazz0006/CampBoardGameHost; developer machine Mini MCP alias: clocktower; branch: cleanup/arch-cleanup-1-20261015; one open [Draft PR #315](https://github.com/Jazz0006/CampBoardGameHost/pull/315) targeting main, **unmerged**.
- 2026-10-16 starting production SHA: ec6596a56cd23981acdc3e30b2cfcf6b23820ca0; Android CI #4213 and R2 #3829 SUCCESS. These checks **do not** validate subsequent source edits or Android real-device flows.
- ARCH-CLEANUP-1 previous cleanup: old scoring / consistency / Shadow recommendation paths removed; B = YES for existing no-candidate model wire contract, C = YES for retired scoring paths, **A = PARTIAL** (second Compose PlayerCard state, non-atomic confirmed facts, incomplete observed-information logging, duplicate recovery/replay). Historical evidence: [ARCH-CLEANUP-1 audit](ARCH_CLEANUP_1_ARCHITECTURE_CONVERGENCE_2026-10-15.md).
- ARCH-CLEANUP-2 **A1 first ten slices**: exact Android-verified source `16f2413f33e2151e5676298162aae850bae52b10`; [CI #4215](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/38852526023) Android `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:testFull`, ASP, Clingo and gate SUCCESS; [R2 #3831](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/38852526096) SUCCESS. Earlier CI #4214's Fortune Teller declaration order compile error was fixed. These ten slices unified actual decisions/publications and removed duplicate narrative revision; the following eleventh slice deals with Mayor.
- **Latest A1 eleventh source slice (Mayor):** replacing UI's separate causal-journal write and standalone game-state revision, `ClocktowerGameSession.commitConfirmedMayorRedirect` stages the verified legal decision/correction and accepts a `MayorRedirectChoice` through the existing atomic ActionFact+revision+global cursor path. Correctly handles same-seat Mayor (no bounce), stale/illegal candidates, idempotence and chronology; action survives JSON roundtrip and appears only in private Host AI projection. Initial source `dd7392cdba097509130e3641b8b7830752c8adc5`: [CI #4217](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/38854159448) + [R2 #3832](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/38854159617) SUCCESS. **Most recent Android-verified executable source SHA `f010720493e3f79a5db145f3b1da4fb655276b30`**: [CI #4218](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/38854537664) Android tests, ASP, Clingo, gate SUCCESS. R2 not re-triggered for the later domain/persistence/test-only correction due to R2 path filter; previous R2 must not be claimed for that SHA.
- **A1 remains PARTIAL, with tighter acceptance scope after decision-history audit:** next inspect DemonSuccessor: keep draft as transient UI, preserve an unambiguous final accepted successor/actual `RoleChange`, but do not add separate permanent AI events for draft, interim confirmation and Dawn application. Then audit ownerless revision callbacks and instance `ClocktowerGameSession.commitGlobalActionFact(draft)` test users (none in production). Artist's future free-form **AI question/answer UI is deferred** and is not an A1 blocker; its current manual-only UI lacks question text. A2–A6 unstarted. Developer VM still lacks SDK35; use exact-source GitHub CI; device/ADB acceptance remains pending.
- Draft PR #315 currently reports **mergeable=false / dirty** against main; both branches touched AGENTS.md and TESTING_STRATEGY.md. This must be resolved separately before eventual merge, without assuming a clean developer worktree proves mergeability.

## The only active implementation: A1 → A6

| Order | Slice | Required completed behavior / deletion |
| --- | --- | --- |
| **A1** | Single confirmed-step Session transaction | Host-valid step confirmation atomically writes true current state + ordered typed fact, idempotently. Audit/delete superseded scattered writers/revisions/synchronizers. |
| **A2** | Remove whole-bundle adoption | Retire both opening/residual one-click buttons and obsolete proposal/adopted state machines. Initial strategic joint planning stays; AI_ASSISTED confirms each recommended step, AI_AUTOMATIC automatically commits the current legal step, MANUAL shares the same rule-bound commit. |
| **A3** | Eliminate second mutable GameState | Session alone owns real Clocktower roles, status, phase and decisions; PlayerCard is presentation. Remove production cards.toClocktowerGameState() reverse reads and duplicated state-sync layers; Undercover remains unchanged. |
| **A4** | Complete the **one minimal game log** | AI reads a cursor-based delta; completed-game player review reads the full same log. Include Host-confirmed first-night Drunk choice as an event, actual received clues, important actions and outcomes, nominations/votes, and human-entered player public role claims. Never encode recommendation source versus manual override; a claim must never become actual identity. Exclude technical phase/poison-expiry facts and duplicate publication/death records. Do not add parallel persistent logs; retain only essential Host rule/cursor internals. |
| **A5** | Safe minimal Recovery and code retirement | Save last reliable Session + typed history + minimum safe progress. Relaunch shows correct grimoire and previously published information, then resumes safely. No arbitrary-time GameState reconstruction, exact UI restore, full Undo, deprecated save compatibility. Remove unused replay/recovery adapters after consumer audit. |
| **A6** | Exact-head independent acceptance | Reviewed diff/deletion inventory, CI/R2, targeted invariants, Android Studio/ADB TB MANUAL/AI_ASSISTED/AI_AUTOMATIC with next steps/overrides/day/night/restart. No paid API calls without separately approved budget. |

**Non-negotiable engineering rule:** every new unified API must enumerate the old API/field/caller/recovery data/tests it replaces; complete migration and **delete** old paths, no ongoing two-source parity/sync. Preserve Host legality, private-information permissions, actual public knowledge and already validated TBGS/EvidenceLab read-only protocols. An unconfirmed draft or AI plan is not a game fact; a committed or shown result cannot be silently overwritten. A previous-step correction is not an A prerequisite.

## Product contract after cleanup (resume later)

1. One full joint opening strategic AI plan as soon as seating/show roles are known; do not separately plan Drunk.
2. Only the currently requested semantic answer for subsequent necessary decisions. AI never receives a menu of legal candidate IDs; Host validates each actual result, and skips model calls when the answer is rule-determined or cached recommendation remains valid.
3. Same-game strategy may continue through a verified Responses anchor; authoritative state and complete actual history always reside in Host. Unadopted model output must not advance a confirmed conversation anchor.
4. During A4, wire a minimal confirmed manual public-role-claim entry into the common AI/review game log. After A1–A6 and P1 end-to-end acceptance: **P2 AI-EVENT-JIT**, **P3 PLAYER-CLAIMS** (richer claim intelligence/workflow beyond this basic recording), **P4 CROSS-GAME-STRATEGY**, **P5 MULTI-NIGHT-FIELD-EVAL**. No additional P2 features before A is closed.

## References

- [ARCH-CLEANUP-2 authoritative implementation contract](ARCH_CLEANUP_2_SINGLE_SESSION_AND_PRACTICAL_RECOVERY_2026-10-16.md)
- [NEXT implementation handoff](NEXT_DEVELOPMENT_HANDOFF.md)
- [Evidence-first testing](TESTING_STRATEGY.md)
- [AI conversation and long-term strategy target](AI_STORYTELLER_CONTINUOUS_CONVERSATION_AND_CROSS_GAME_MEMORY_2026-10-15.md)
- [TBGS/UGSM dated evidence only](TBGS_UGSM_DECISION_TIME_AND_AI_EVENT_INPUT_AUDIT_2026-10-15.md)
- [Previous roadmap checkpoint](archive/checkpoints/CURRENT_DEVELOPMENT_ROADMAP_PRE_CONTINUITY_SYNC_2026-10-15.md)



