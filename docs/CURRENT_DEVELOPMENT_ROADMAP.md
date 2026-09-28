# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-28 Australia/Sydney  
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
RH-E runtime persistence/timing hardening             COMPLETE
C5 evidence-backed policy evolution                   BLOCKED ON QUALIFYING E3/E4
SDE-3E automatic production cutover                   BLOCKED PER SURFACE
~~~

## 2. Current repository boundary

Current canonical baseline: `main`.

PR #157 — `SDE correctness repair: close CR-A CR-B CR-C` — was explicitly authorized and squash-merged on 2026-09-27.

PR #157 merged executable integration baseline:

`02845a470761a988f0041d8c1027b0e2c58a7e05`

Current canonical `main` after the 2026-09-28 current-only Recovery cleanup:

`b297484cd055b6aa5cfaf1c9b1c4093832af77da`

PR #165 — `Recovery: enforce current-only minimal persistence` — was explicitly authorized and squash-merged on 2026-09-28. Recovery R0–R6 are complete; R7 remains a separate ownership follow-up and is not part of the completed cleanup boundary.

The previous `5d2982a7...` baseline is historical. The executable SDE policy boundary remains unchanged by #165.

The merged scope contains the accepted continuation through CR-A/B/C, C4/SDE-3D2, IF-D, RH-E, and the audited workflow/document integration. The previous continuation branch `codex/sde-history-prefix-route-closure` is now historical rather than the active development baseline.

There is currently **no active SDE production implementation PR**. Any documentation-only post-merge closure branch/PR is bookkeeping, not a new product-development authority.

The 2026-09-27 live-main integration audit found no production/gameplay/SDE semantic conflict. The accepted resolution preserved the newer GitHub Connector-first workflow in root `AGENTS.md` and archived the superseded 2026-09-24 M8G5 workflow document under `docs/archive/workflows/`.

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
| RH-E | formal `f51a295983e8e203119dd50693af343c1ec23906`, tree `2babb1fba00b46dfc676efb6f090386b7a73826f`; validation-only PR #162 head `bf66363385420f507f92a729b496ff002791dee5`; exact-tree CI #3478 + R2 #3231 GREEN |

Later documentation-only changes must not be re-labelled as validating a different executable tree.

Completion/cleanup detail:

- `docs/archive/checkpoints/sde/SDE_PRE_RHE_REPOSITORY_CLEANUP_CHECKPOINT_2026-09-27.md`
- `docs/archive/checkpoints/sde/SDE_RH_E_RUNTIME_PERSISTENCE_TIMING_COMPLETION_2026-09-27.md`
- `docs/archive/checkpoints/sde/SDE_PR157_MAIN_INTEGRATION_COMPLETION_2026-09-27.md`
- `docs/SDE_E3_E4_QUALIFICATION_AUDIT_2026-09-27.md`

## 4. Current priority — targeted evidence acquisition after E3/E4 audit

RH-E, #157 merge closure, #163 documentation closure, and #165 current-only Recovery cleanup are COMPLETE.

The 2026-09-27 E3/E4 qualification audit found:

- **no current predicate passes E3** for a new typed policy preference/reason;
- **no current predicate passes E4** for numeric strength;
- current evidence does support descriptive feature surfaces and semantic regression targets;
- therefore **do not create `BEGINNER_CONSERVATIVE_V2` yet**.

Explicitly non-qualifying current candidates:

- healthy-information floor / middle band;
- impaired-information believability / continuity;
- confirmation-chain impact as a candidate-ordering rule;
- role-function exposure severity;
- truth-danger / Red-Herring candidate ordering;
- Demon-bluff triplet ordering;
- multi-axis scalar/weights.

Red Herring remains the strongest descriptive evidence family, but the accepted C4 audit still classifies Evin as E1 yes / E2 candidate / E3 no. Beardy's independent guidance strengthens context dependence for confirmation / role-function exposure but does not provide a replayable choice-over-legal-alternatives policy case.

The next action is **targeted evidence acquisition**, not implementation. Search only for cases that can close a named E3 gap or concrete replay blocker. C5/V2 and SDE-3E remain blocked until that evidence gate is satisfied.

Recovery maintenance no longer blocks the SDE lane: #165 is merged, R0–R6 are complete, and R7 is a separate follow-up only if its ownership work becomes worth doing.

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

The 2026-09-28 current-only Recovery cleanup removed those persistence-only compatibility/minimal-state residues through R0–R6 and was squash-merged as #165 at `b297484cd055b6aa5cfaf1c9b1c4093832af77da`. The completed contract and acceptance record live in `docs/CURRENT_ONLY_RECOVERY_MINIMAL_STATE_AUDIT_2026-09-28.md`. R7 bookkeeping/transport/lifecycle ownership is a separate follow-up, not unfinished cleanup.

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
main@b297484c... is the clean merged baseline
-> E3/E4 qualification audit COMPLETE: no qualifying predicate
-> targeted acquisition for Gap A / B / C / D only
-> reconstruct committed state + legal alternatives + explicit rationale
-> rerun E3 qualification
-> only after PASS create the smallest C5 / V2 slice on a fresh branch
-> preserve BEGINNER_CONSERVATIVE_V1 unchanged
~~~

Recovery R0–R6 and #157/RH-E closure are complete; do not reopen them unless new current-version evidence exposes a regression. R7 remains separate follow-up work rather than an active lane.

## 9. Current authorities

Read first:

1. `AGENTS.md`
2. `docs/AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md`
3. `docs/TESTING_STRATEGY.md`
4. this roadmap
5. `docs/NEXT_DEVELOPMENT_HANDOFF.md`
6. `docs/CURRENT_ONLY_RECOVERY_MINIMAL_STATE_AUDIT_2026-09-28.md` — completed R0–R6 current-only Recovery cleanup contract / acceptance record
7. `docs/SDE_POST_AUDIT_CORRECTNESS_REPAIR_ROUTE_2026-09-25.md` — RH-E contract and completed repair record
8. `docs/SDE_3D2_TRUTH_CREDIBILITY_RED_HERRING_ARCHITECTURE_AUDIT_2026-09-26.md` — completed C4 architecture/evidence boundary
9. `docs/SDE_3D0_CALIBRATED_POLICY_FREEZE_CUTOVER_GATE_ARCHITECTURE_AUDIT_2026-09-24.md` — freeze/cutover gates

Use older SDE-3A/B/C/C0–C3 completion documents only when a specific historical or ownership question requires them.

## 10. Testing cadence

Follow `docs/TESTING_STRATEGY.md`.

RH-E completed at T4 with exact-tree CI #3478 and R2 #3231 GREEN, including Android FULL + assemble, ASP contracts and Real Clingo cross-validation.

The #157 live-main integration head `371ebf624898c08747203aceaed1254647867214` also completed exact-head acceptance with CI #3479 and R2 #3232 GREEN. This integration changed workflow/documentation only relative to the previously accepted continuation head.

GitHub CI/R2 remains the normal Android execution and independent remote acceptance surface. The N3150 execution-host experiment remains retired from the default workflow unless the user explicitly reopens it.

A documentation-only compaction does not require Android regression by itself.

