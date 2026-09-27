# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-27 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**  
> Historical checkpoint detail belongs in completion/audit documents under `docs/archive/` or the linked slice audits, not in this live roadmap.

## 1. Program status

~~~text
D6 decomposition / ownership cleanup                  COMPLETE
EPI-MQ capability boundary                            COMPLETE
SDE-0 / SDE-1                                        COMPLETE
SDE-2D1 / 2D2 / 2D3                                  COMPLETE
SDE-2D4 5–15 correctness/performance                  COMPLETE
SDE-2D5 calibration / policy evidence                 MERGED CHECKPOINT / EVIDENCE CONTINUES
SDE-3A engine / feature / policy contract             COMPLETE
SDE-3B BEGINNER_CONSERVATIVE_V1                       COMPLETE / IMMUTABLE V1
SDE-3C DecisionTrace / shadow replay                  COMPLETE
CR-A / CR-B / CR-C                                    COMPLETE
C4 / SDE-3D2                                          COMPLETE
IF-D durable App replay capture/rebuild               COMPLETE
RH-E runtime persistence/timing hardening             NEXT
C5 evidence-backed policy evolution                   BLOCKED ON QUALIFYING E3/E4
SDE-3E automatic production cutover                   BLOCKED PER SURFACE
~~~

## 2. Current repository boundary

Active continuation: `codex/sde-history-prefix-route-closure`.

Active PR: `#157 — SDE correctness repair: close CR-A CR-B CR-C`.

The PR title is historical relative to the branch contents. It now carries the accepted continuation through C4 and IF-D. Keep it **Draft**. Do not mark ready, merge, rebase, force-push or resolve the main conflict unless explicitly authorized.

Live `main` contains the later merged M8G5 Mini MCP workflow/control-plane commit that is not on the continuation, so #157 remains divergent / dirty against main. This is an integration gate, not a reason to interrupt RH-E.

Repository cleanup before RH-E:

- validation-only/superseded PRs #148, #154, #156, #158, #160 and #161 are closed without merge;
- diagnostic PR #109 is closed as obsolete for the current short-horizon Recovery contract after a fresh current-format atomicity audit;
- temporary validation remote branches from the first cleanup wave are gone;
- remaining historical branch pruning is hygiene only and must preserve unique archive material.

Always query live Git/PR state before executable work. Do not rely on a hard-coded live branch HEAD in this document.

## 3. Accepted executable checkpoints

These are historical acceptance identities, not current branch heads:

| Checkpoint | Accepted evidence |
| --- | --- |
| CR-A/B/C | validation head `4245ddb8c12782775a6b4c237f5dd7f0f1ce92c0`; CI #3459 + R2 #3212 GREEN |
| C4 / SDE-3D2 | formal `21206a8ca95896ffad83eaba5695a3a571c6cd74`, tree `a4701e0f4873ddd629dfa11163a94d2aa0cb9b8b`; exact-tree CI #3473 + R2 #3226 GREEN |
| IF-D | formal `4d1b6d7f39529402eb9ec1e6032eb80ef9a14e86`, tree `181a1d80d449c2d443a678e10033acda13c777d1`; exact-tree CI #3476 + R2 #3229 GREEN |

Later pre-RH-E changes are documentation/repository cleanup; do not re-label those historical CI/R2 runs as validating a different executable tree.

Historical implementation and cleanup detail moved to:

`docs/archive/checkpoints/sde/SDE_PRE_RHE_REPOSITORY_CLEANUP_CHECKPOINT_2026-09-27.md`

## 4. Current priority — RH-E

RH-E closes the remaining runtime diagnostic persistence/timing weakness found by the 2026-09-25 SDE audit.

Required behavior:

1. move diagnostic archive I/O to one serialized background persistence lane;
2. preserve atomic append/correlation and existing idempotency semantics;
3. avoid parallel read-modify-write races and lost trace updates;
4. record evaluation latency, persistence latency and end-to-end latency separately;
5. cover slow storage, backlog, cancellation/stale identity and archive growth;
6. define bounded retention/storage-growth behavior before runtime collection is broadened;
7. preserve failure isolation: diagnostic persistence failure must never invalidate a successful canonical game commit.

Canonical session state/history remain authoritative. DecisionTrace storage remains diagnostic only.

Before production edits:

- map every archive producer, append/correlation caller and persistence implementation;
- identify the single serialization owner and coroutine/lifecycle owner;
- prove which operations must be ordered together;
- define cancellation semantics without dropping already-committed canonical game state;
- define retention before changing storage format or runtime scope;
- establish the smallest durable typed tests at the persistence owner.

Do not put storage orchestration into the App/Compose root when it belongs in the diagnostic persistence owner.

## 5. Recovery product boundary

Recent Emergency Recovery is intentionally **short-horizon emergency continuation**, not a normal save-game product.

Current validity contract:

~~~text
current RecoverySnapshot format
+ exact current compatibility token
+ <= 4 hour age
-> validated current recovery plan
~~~

Do not add cross-version migration, old-format reconstruction, tolerant legacy repair or long-term save compatibility unless the product requirement changes explicitly.

PR #109 reproduced an old/abnormal event/observation half-state. Current production does not expose a physical persistence window for that intermediate state: the game event and semantic projection execute synchronously before later SideEffect/lifecycle Recovery persistence. #109 was therefore closed rather than converted into legacy compatibility code.

If a future **current-version** crash produces a fresh inconsistent current-format Recovery snapshot, treat it as a new current-format atomicity defect with new evidence.

## 6. Frozen architecture / policy decisions

- `BEGINNER_CONSERVATIVE_V1` remains immutable.
- No placeholder V2.
- No numeric weights/thresholds without the evidence level required by the affected policy semantics.
- Legal candidate ownership remains in rules/domain owners; SDE ranks only legal alternatives.
- Canonical session/history owners remain the only mutable game truth.
- DecisionTrace/replay/export are read-only diagnostic/calibration projections.
- Red Herring legality/commit ownership is not moved into SDE policy.
- Drunk/Poisoned information may be true or false; impaired narrative uses the accepted shared perceived-functioning projection.
- Spy/Recluse registration remains interaction-scoped.
- Demon bluffs remain a joint SDE output until committed.
- No fixture-specific or named-player policy branches.
- Traveller evidence remains outside the current mainline algorithm unless explicitly brought into scope.

## 7. Evidence track

ClocktowerEvidenceLab continues independently and does not block RH-E.

Evidence stages remain:

- E1 — architecture/lifecycle evidence;
- E2 — semantic regression evidence;
- E3 — qualitative policy evidence strong enough to justify a typed preference/reason;
- E4 — quantitative calibration evidence when a policy genuinely requires numeric strength.

Observed expert choices without adequate rationale are not automatically policy labels. Complete real games and expert Storyteller rationale remain preferred over synthetic clean-corpus calibration.

C5 remains blocked until a genuinely qualifying E3/E4 predicate exists.

## 8. Immediate execution order

~~~text
verify clean continuation / live PR state
-> RH-E owner + fanout + concurrency audit
-> smallest durable persistence/timing tests
-> RH-E implementation
-> focused GREEN
-> FAST + affected persistence/runtime validation
-> logical-checkpoint FULL/assemble and required remote acceptance
-> update compact roadmap/handoff
-> final main-integration audit
~~~

Do not resolve #157/main integration merely to start RH-E.

## 9. Current authorities

Read first:

1. `AGENTS.md`
2. `docs/TESTING_STRATEGY.md`
3. this roadmap
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`
5. `docs/SDE_POST_AUDIT_CORRECTNESS_REPAIR_ROUTE_2026-09-25.md` — RH-E contract and completed repair record
6. `docs/SDE_3D2_TRUTH_CREDIBILITY_RED_HERRING_ARCHITECTURE_AUDIT_2026-09-26.md` — completed C4 architecture/evidence boundary
7. `docs/SDE_3D0_CALIBRATED_POLICY_FREEZE_CUTOVER_GATE_ARCHITECTURE_AUDIT_2026-09-24.md` — freeze/cutover gates

Use older SDE-3A/B/C/C0–C3 completion documents only when a specific historical or ownership question requires them.

## 10. Testing cadence

Follow `docs/TESTING_STRATEGY.md`.

For RH-E:

- T0: owning archive/persistence/concurrency contract;
- T1: FAST;
- T2: affected persistence, runtime-shadow, correlation and recovery integration;
- T3 only when an explicitly expensive/manual evidence surface is affected;
- T4 logical checkpoint: FULL + assemble + selected external gates / remote acceptance.

N3150 or another supported Android build host may provide local execution evidence; GitHub remains the independent remote acceptance surface.

A documentation-only compaction does not require Android regression by itself.

