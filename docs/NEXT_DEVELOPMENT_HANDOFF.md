# CampBoardGameHost — Next Development Handoff

> **Implementation handoff 2026-10-16; A1 first three slices implemented, exact-head verification required.** Continue A1 of [ARCH-CLEANUP-2](ARCH_CLEANUP_2_SINGLE_SESSION_AND_PRACTICAL_RECOVERY_2026-10-16.md). The user approved A1→A6 implementation, replacing old arbitrary-time replay / exact UI restore / one-click whole-plan adoption requirements. Do not spend the new conversation re-debating the accepted architecture or work on P2 before A.

## Starting repository state (recheck live)

- Repository Jazz0006/CampBoardGameHost; developer machine via Mini MCP alias **clocktower**.
- Branch **cleanup/arch-cleanup-1-20261015**, starting SHA **ec6596a56cd23981acdc3e30b2cfcf6b23820ca0**, clean and equal origin on last check. Actual live state may change: first query Mini MCP git_state and GitHub PR/CI.
- Single open [Draft PR #315](https://github.com/Jazz0006/CampBoardGameHost/pull/315), base main SHA 770ed32f446d03cf81f226901afa78cbdcf287bf, **not merged**; 2026-10-16 recheck reports **mergeable=false / dirty**, not clean. Main has changed AGENTS.md and TESTING_STRATEGY.md relative to common ancestor. Exact conflict resolution remains pending. P1 old #304/#306/#307/#308 and #310–#314 were closed unmerged; commits already embedded in #315 branch. Never resurrect old PR stack.
- Baseline code SHA Android CI #4213 SUCCESS and R2 #3829 SUCCESS; ASP/Real Clingo skipped at that exact run. This is evidence for existing code only. A = PARTIAL; B/C old cleanups have source-level evidence but real gameplay acceptance is pending.
- No paid API call or main merge authorized. Root AGENTS.md requires evidence-first, reviewed exact diff/CI/R2, and permanent **replace-and-retire** rule.

## User-approved irreducible product contract

**One always-correct current ClocktowerGameSession + one logically ordered typed history of actual confirmed Host decisions and player observations, suitable for AI.**

- UI PlayerCard, provider responses, recommendations, Recovery, Archive and TBGS are consumers/projections, **never** second writable game truth. ActionFactTimeline and EpistemicObservationLog keep their types and one global order; no new generic EventStore.
- Actual Host confirmation is the unit of commit, commonly “下一步” but also explicit confirmation/publication. State update + typed fact + sequence/revision must be one verified idempotent Session transition. Unaccepted AI recommendations are temporary only.
- Keep *one* opening joint plan, no separate Drunk plan. Remove two whole-package one-click adoption controls **and** redundant adoption state machines; AI_ASSISTED offers current-step preselection, AI_AUTOMATIC commits current safe legal step without adoption click, MANUAL uses same Host pathway. Invalidate dependent future recommendations on manual overrides or changed facts.
- All **actually shown information**, including drunk/poisoned fictitious information, must enter structured history with actual semantic value, recipient and reliability. Preserve all nominations and individual votes; do not silently rewrite what players know.
- Recovery **only** needs correct grimoire, confirmed published information and safe continuation after crash; unconfirmed current interaction may be redone. No exact UI restoration, arbitrary historical re-materialization, old save migration, or general Undo/Redo. Last-step correction is optional later and never allowed to contradict player knowledge or AI confirmed history.
- **Critical permanent lesson:** each newly unified seam must enumerate and delete replaced old interfaces/fields/call paths/recovery data/tests. No indefinite dual-write, double-source reconciliation or compatibility stacks. Do not delete still-live Host legality, registration witnesses, EvidenceLab/TBGS verified interfaces.

## A1 first implementation checkpoint (2026-10-16; PARTIAL)

- Live baseline before first source edits: `470b5dbe4a48d4788646cc7d2313e6131ed2470a`, development worktree clean and equal origin. See ARCH-CLEANUP-2 implementation log for exact modified call site and retirement inventory.
- `NightCheckpointHostTransaction.confirmPoison` accepted confirmation is now the mechanical+action+revision atomic Session boundary. `commitPoisonTargetBoundary` removed, corresponding tests updated for global chronology and rejection/idempotency. No claim yet that the remaining role/death/day/phase/observation call sites share this boundary.
- **Next A1 work, not A2** (updated by second checkpoint below): migrate and retire remaining actual-role change, poison/night materialization, phase advancement and information publication writers at their real Host confirmation boundaries. Preserve Klutz pre-death ability proof and observation availability for actual player reveals. Verify with latest exact source CI/R2; keep #315 Draft, do not merge main.
- Do not claim A1 done until all confirmed-step mechanical+typed history transitions and remaining consumers are audited and migrated.

## A1 second implementation checkpoint (2026-10-16; PARTIAL)

- On `550ed797044d1864f45068566e45ae960dbe464d` (first poison migration), migrated confirmed Slayer hit, Virgin auto-execution, ordinary day execution and Dawn death into a single Session `commitConfirmedPlayerDeath` call. The Session snapshots Klutz alive/poison evidence **before** mutation and atomically stores death/execution + optional actual public AliveAt observation + revision and shared global cursor. UI only reflects confirmed state.
- **Actually deleted** the old `synchronizePlayerDeathWithinCurrentRevision` production helper and its test references, and the `addClocktowerEvent` narrative-based mechanical fact/observation projection and `projectSemanticHistory` parameter. Existing tests for real NGJ Klutz and synthetic archival provenance were migrated to the confirmed Session boundary, not removed wholesale.
- **Next A1 work, not A2:** actual-role change and Dawn role materialization; poison at Dusk/Dawn and attack/phase steps; vote/no-execution/revision ownership; actual information publication preflight and durable fact commit. Audit NightDawnDurableMaterializationPlanner's remaining state/action/observation repair branches, especially those made obsolete by atomic Session commits. Do not erase Host legal evidence or lose an already-public result. Root UI/PlayerCard second state is deliberately A3.
- Check exact new source HEAD CI/R2 and GitHub conflict before any acceptance claim; #315 remains Draft and main unmerged. No paid model calls approved.

## A1 third implementation checkpoint (2026-10-16; PARTIAL)

- Starting HEAD before this batch: `d6742aca56e164d9459be7d9a67591a3f45e883d`, local == remote and clean. The real Scarlet Woman and Dawn Imp succession writers used independent `recordClocktowerRoleChangeAction` then `commitActualRoleBoundary`, leaving a partial state/fact window.
- Migrated both to `commitConfirmedActualRoleChange` using existing Session accepted-step atomic transition and stable Dawn planner action IDs. Reflected UI card role only after Session confirmation. No separate `recordSemanticHistory=false` switch. Reject the obsolete state/history half-repair state instead of silently accepting a second truth.
- **Delete inventory**: `recordClocktowerRoleChangeAction`, `commitActualRoleBoundary`, unused `commitShownRoleBoundary`, `commitGameStateBoundary` and obsolete generic self-test; prior targeted role test now checks actual role + typed fact + global sequence + no-op duplicate/stale rejection. The shown-role helper had no production consumer.
- **Continue A1, not A2**: prioritize poison carry / dusk expiry and Dawn materialization fact+mechanics paths, then phase/attack and vote/no-execution sequencing. `addClocktowerEvent` still increments revision for narrative events after some authoritative role/death commits; audit and retire redundant increments once remaining event consumers are migrated (do not confuse one Session transaction with exactly one revision for an entire UI callback). Check remaining `NightDawnDurableMaterializationPlanner` state/fact split retry branches and their tests, delete when truly superseded. A3 PlayerCard mechanical authority removal, A4 information completeness, A5 Recovery reduction remain separate later slices. Obtain exact HEAD independent Android CI/R2; PR #315 stays Draft, no main merge or paid calls.

## Historical first implementation brief: A1

1. Read AGENTS.md, [ARCH-CLEANUP-2](ARCH_CLEANUP_2_SINGLE_SESSION_AND_PRACTICAL_RECOVERY_2026-10-16.md), [ARCH-CLEANUP-1 evidence](ARCH_CLEANUP_1_ARCHITECTURE_CONVERGENCE_2026-10-15.md), this handoff, roadmap and testing policy. Verify live SHA / worktree / #315 / main / checks.
2. Make an exact caller/writer map from real code: ClocktowerGameSession.commitGameStateBoundary / synchronizeGameStateWithinCurrentRevision / advanceGameStateRevision / commitGlobalActionFact / commitGlobalEpistemicObservation; UI PlayerCard mutations and information-reveal handoff; recovery snapshot producer/consumer. Identify *the confirmed step boundary* and which old paths will be retired by this A1 edit.
3. Implement **minimal production Session confirmed-step transaction** for the actual most valuable Host boundaries (not a generic command bus), with legality check, global ordering, state+fact commit and idempotency. Migrate actual callers in a safe increment, delete each superseded route at its completed boundary, inspect exact diff; record any still-open owner/consumer gaps. Do not claim A1 DONE without code/call-site verification.
4. Use existing targeted tests or add only high-value stable invariants where needed, then actual Android CI/R2. Do not use mandatory mechanical RED tests. Keep PR Draft, push reviewable commits. Report exact HEAD, removed old pathways and next A1→A2 work.

**Follow-on implementation:** A2 delete opening/residual whole-adoption UI and states; A3 eliminate active-game cards.toClocktowerGameState reverse authority; A4 fill every real player-information publication; A5 simplify recovery and remove replay-only leftovers after consumer audit; A6 UI/device/CI multi-mode end-to-end acceptance. Only then consider main merge or P2.

## Known exact technical gaps for targeted audit

- ClocktowerSessionView currently omits GameState, while multiple ClocktowerHostScreen and CampBoardGameHostApp locations call cards.toClocktowerGameState() on gameplay/UI paths.
- Opening one-click screen: ClocktowerJointOpeningReviewScreen.kt (button “一键采纳完整首夜方案”), CampBoardGameHostApp.adoptJointOpeningAfterReveal. Later one-click: ClocktowerHostScreen.kt (“一键采纳剩余方案”), CampBoardGameHostApp.adoptWholeResidualProposal, residualProposed*/residualAdopted*.
- Actual info gap: ClocktowerHostScreen.recordReliablePrivateInformation exits for unreliable step without displayProposition; this can drop actual fabricated Drunk/poison information.
- Session contains accepted red herring/bluff fields + sequenced choice facts, causalDecisionJournal now Session-owned; Virgin/Slayer derived from canonical action facts. These are genuine prior A work: **retain** while deleting redundant UI/recovery writers.
- RecoverySnapshot still carries cards and overlapping mechanical fields and optional SdeHistoricalReplayInput JSON. Audit all consumers before deleting; preserve safe restoration without depending on arbitrary historical replay.
- Note: Undercover Screen.Game elimination path is not a Clocktower redundant writer.

## Acceptance/governance

- Minimum current acceptance: exact facts in Session, atomic confirmed step + typed record, reliable AI-input history, no second mutable Clocktower authority, correct playable recovery, no orphan old implementation.
- Preserve Host rule correctness, user data integrity, field visibility, prefix chronology, stale-plan rejection, same-game AI memory that is *not* fact truth.
- Validate actual Android FAST/FULL and relevant rule tests as appropriate on real source commits; CI/R2 and later Android Studio/ADB walkthrough of manual/assisted/automatic 8-player TB, first/second night, bad info, override, death, nomination/vote and restart. Fixed legal canned responses suffice for cleanup; **never trigger paid live model** without separate authorization.
- Do not mark A closed, convert PR Ready, or merge main on documentation-only changes. Leave an explicit remaining-debt checklist with each slice.

## Authoritative documents

[AGENTS.md](../AGENTS.md) · [ARCH-CLEANUP-2](ARCH_CLEANUP_2_SINGLE_SESSION_AND_PRACTICAL_RECOVERY_2026-10-16.md) · [Roadmap](CURRENT_DEVELOPMENT_ROADMAP.md) · [Testing](TESTING_STRATEGY.md) · [ARCH-CLEANUP-1 historical evidence](ARCH_CLEANUP_1_ARCHITECTURE_CONVERGENCE_2026-10-15.md) · [AI continuation target](AI_STORYTELLER_CONTINUOUS_CONVERSATION_AND_CROSS_GAME_MEMORY_2026-10-15.md). Previous dated audits and archived checkpoints are historical evidence, not alternate current authority.



