# CampBoardGameHost — Next Development Handoff

> Updated: 2026-10-03 Australia/Sydney  
> Current baseline: live `main` — query exact HEAD/PR state at session start  
> Last completed executable validation checkpoint: **C5-E latest-main integrated production cutover — `fdf3a2be07ed0c70753ba724f15b5572e0dea584`; CI #3720 / R2 #3424 GREEN, including Android FULL/assemble, ASP and Real Clingo**  
> DLB-6 merged checkpoint: **PR #195 — `DLB-6: retire old Drunk setup contracts` — squash merge `5223c2fb610ba63d2c5c9b2f6ca17cb14adb0ce7`**  
> DLB-7 delivery PR: **#196 — `DLB-7: finalize Drunk late-binding acceptance`; exact executable T4 accepted at `b760117351e57cfe78cc0bc9483b42306757c22f`**  
> Current cutover verdict: **DLB campaign COMPLETE / ACCEPTED; Q04 production cutover and Beginner automatic Drunk authority remain ACCEPTED**  
> Evidence checkpoint: **ClocktowerEvidenceLab `78f672868ea6603317aeefa20ad91686c5886db9` — G10 `16:52` Librarian future-flexibility E3 PASS; Q04 remains previously accepted**  
> C5-E functioning-Librarian automatic production cutover is **COMPLETE / ACCEPTED** at `6afc3b08416eaf6f3fb74a53bb48b8e144044341` (CI #3717 / R2 #3421). Latest-main integration revalidation is also GREEN at `fdf3a2be07ed0c70753ba724f15b5572e0dea584` (CI #3720 / R2 #3424). The accepted product behavior is V2-first on the exact functioning Librarian automatic surface with canonical-domain rebind and legacy fallback; Experienced/manual/publication semantics remain unchanged. In the parallel ML lane, **HOST-ML1 typed neutral `RecommendationDecisionExportV1` is COMPLETE / ACCEPTED after PR #208 + #209; final executable checkpoint `b91fcd298621e9064f11c26c1a0398c5c14fc13c` (CI #3710 / R2 #3416), with PR #209 squash merge `2d4681115b9c373bfbfa85a7b385aba4e59e1062`**. Machine-readable materialization remains deferred until a concrete offline consumer requires an interchange format. Re-evaluate the next product lane after C5-E merge; TBGS-2D Demon succession remains implementation-ready.

This file is deliberately compact. Completed checkpoint detail belongs in linked completion/audit/archive documents rather than being copied forward indefinitely.

## 1. Read first

1. `AGENTS.md`
2. `docs/AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md`
3. `docs/TESTING_STRATEGY.md`
4. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
5. this handoff
6. `docs/SDE_C5_G10_PAIR_FUTURE_FLEXIBILITY_REENTRY_AUDIT_2026-10-03.md` — C5 re-entry / SDE-3D3 evidence and replay authority; C5-A–D complete / accepted
7. `docs/SDE_C5E_LIBRARIAN_PRODUCTION_CUTOVER_GATE_AUDIT_2026-10-03.md` — C5-E functioning-Librarian production cutover acceptance authority
8. `docs/DLB_AND_APP_HOST_DECOMPOSITION_IMPLEMENTATION_ROUTE_2026-09-29.md` — current DLB implementation authority
9. `docs/DLB_7_FINAL_ACCEPTANCE_2026-10-03.md` — final DLB acceptance matrix and exact T4 authority
10. `docs/DLB_6_OLD_CONTRACT_RETIREMENT_COMPLETION_2026-10-03.md` — DLB-6 accepted retirement boundary
11. `docs/DLB_DRUNK_ASSIGNMENT_C3_Q04_REENTRY_AUDIT_2026-10-02.md` — Q04 evidence/policy/cutover acceptance authority
12. `docs/DLB_DRUNK_ASSIGNMENT_POST_TBGS1_CUTOVER_RECHECK_2026-09-30.md` — historical post-TBGS-1 NOT-PASSED verdict before Q04 verification
13. `docs/DLB_DRUNK_ASSIGNMENT_PRODUCTION_CUTOVER_GATE_AUDIT_2026-09-30.md` — original pre-TBGS-1 gate audit / historical blocker detail
14. `docs/DLB_DRUNK_RECOMMENDATION_CONTEXT_CAPABILITY_CONTRACT_2026-09-30.md` — current policy-neutral required/enrichment/unavailable context boundary and EvidenceLab handoff contract
15. `docs/TB_CANONICAL_GAME_SNAPSHOT_INTEGRATION_ROUTE_2026-09-30.md` — TB-only standard snapshot / Drunk vertical slice / EvidenceLab interoperability route; TBGS-0/1 complete, TBGS-2 active
16. `docs/TBGS_2_RUNTIME_RECOMMENDATION_STATE_MIGRATION_AUDIT_2026-10-03.md` — TBGS-2 consumer inventory and accepted 2A/2B/2C lineage
17. `docs/TBGS_2C_SETUP_COORDINATION_SNAPSHOT_CONTEXT_AUDIT_2026-10-03.md` — TBGS-2C setup-coordination acceptance authority
18. `docs/TBGS_2D_DYNAMIC_STATE_CONSUMER_SELECTION_AUDIT_2026-10-03.md` — post-2C DynamicGameState family audit and exact TBGS-2D Demon-succession implementation boundary
19. `docs/CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md` — long-horizon Setup / Game Engine / Storyteller Recommendation / canonical-session target; use as an ownership guardrail, not as permission to broaden the current slice
20. `docs/HOST_ML_READINESS_MODELLAB_BOUNDARY_AUDIT_2026-10-03.md` — HOST-ML0 accepted ownership/export/model-training boundary
21. `docs/HOST_ML1_NEUTRAL_DECISION_EXPORT_IMPLEMENTATION_2026-10-03.md` — HOST-ML1 complete typed neutral export acceptance, candidate semantic payload, leakage boundary and deferred machine-readable materialization
22. `docs/DLB_5_STAGED_FIRST_NIGHT_DEPENDENCY_PLANNER_AUDIT_2026-09-29.md` — completed DLB-5 closeout record

Read the 2026-09-28 source audits, completed Recovery audit, or older SDE checkpoints only when a concrete history/ownership question requires them. They are not default startup context.

## 2. Startup rule

Do not assume recorded branch/HEAD/PR state is still live.

Start by checking live remote state through the **GitHub Connector**: current `main`, any newly active branch/PR, and the relevant evidence inputs for the requested slice.

If the task depends on local unpushed state, also inspect the configured local workspace before editing so that current work is never overwritten or lost.

Requirements:

- preserve the current working tree;
- do not reset/discard unrelated work;
- treat merged PR #157 and its historical continuation branch as completed evidence, not as the current branch;
- treat DLB-0 through DLB-4A as completed/accepted history; do not reopen their implementation contracts while closing DLB-5;
- fixed-Drunk misinformation evaluators must still not be converted into a Drunk seat-selection score without the explicit evidence-backed consequence/selection contract;
- treat merged PR #165 and `codex/current-only-recovery-cleanup` as completed historical maintenance, not the active development branch;
- completed, fully accepted, mergeable PRs may be marked ready and merged directly under the standing 2026-09-29 authorization; rebase, force-push, destructive history changes, scope broadening, or merging incomplete/failing work still require explicit user authorization.

The 2026-09-27 #157 integration and the 2026-09-28 #165 Recovery cleanup squash merges are complete. Do not redo or rewrite that history.

## 3. Current state

~~~text
SDE-3A                                  COMPLETE
SDE-3B / BEGINNER_CONSERVATIVE_V1      COMPLETE / FROZEN
SDE-3C DecisionTrace / replay          COMPLETE
CR-A / CR-B / CR-C                     COMPLETE
C4 / SDE-3D2                           COMPLETE
IF-D durable replay                    COMPLETE
RH-E                                    COMPLETE
C5 / V2                                C5-A/C5-B/C5-C/C5-D/C5-E COMPLETE / ACCEPTED
SDE-3E cutover                         FUNCTIONING LIBRARIAN SURFACE ACCEPTED; OTHER SURFACES GATED
DLB-0 typed intermediate setup              COMPLETE / ACCEPTED
DLB-1 visible-roster deal cutover              COMPLETE / ACCEPTED
DLB-2 legal candidate + hypothetical projector   COMPLETE / ACCEPTED
DLB-3A shadow envelope + DecisionTrace            COMPLETE / ACCEPTED
DLB-3B setup-level consequence bridge              COMPLETE / ACCEPTED
DLB-4 canonical Drunk commit before reveal         COMPLETE / ACCEPTED
DLB-4A Experienced assisted Drunk selection        COMPLETE / ACCEPTED
DLB-5.1 typed dependency planner                    COMPLETE / GREEN
DLB-5.2 Red Herring production cutover             COMPLETE / GREEN
DLB-5.3 Demon bluff production cutover             COMPLETE / GREEN
DLB-5.4 information / poison convergence audit     COMPLETE / NO PRODUCTION GAP
DLB-5.5 obsolete setup auto-apply cleanup          COMPLETE / GREEN
DLB-5 overall                                      COMPLETE / ACCEPTED
DLB-5H1 presentation extraction                    COMPLETE / ACCEPTED
Drunk-assignment cutover gate audit                COMPLETE / HISTORICAL NOT-PASSED VERDICT
TBGS-0 canonical TB snapshot contract              COMPLETE / ACCEPTED
TBGS-1 Drunk snapshot vertical slice / interop     COMPLETE / ACCEPTED
Post-TBGS-1 cutover recheck                        COMPLETE / HISTORICAL NOT-PASSED VERDICT
C3-Q04 production-policy re-entry                  COMPLETE / EVIDENCE GATE PASSED
DRUNK_ASSIGNMENT_Q04_V1 + replay                   COMPLETE / ACCEPTED
Q04 production cutover                             COMPLETE / ACCEPTED
Beginner automatic Drunk authority                 COMPLETE / ACCEPTED
DLB-6 old-contract retirement                      COMPLETE / ACCEPTED
DLB-7 final acceptance                             COMPLETE / ACCEPTED
DLB campaign                                       COMPLETE / ACCEPTED
TBGS-2 runtime recommendation projection migration IN PROGRESS — 2A/2B/2C COMPLETE / ACCEPTED; 2D AUDIT COMPLETE / IMPLEMENTATION READY — Demon succession
App/Host bounded decomposition           GUARDRAIL / NO BROAD CAMPAIGN
~~~

Repository cleanup state:

- validation-only PR #162 is closed without merge after exact-tree RH-E acceptance; older validation-only PRs remain closed;
- PR #109 is closed after confirming its old half-state is outside the current short-horizon Recovery contract;
- first-wave temporary validation remote branches are removed;
- PR #165 current-only Recovery cleanup is squash-merged at `b297484cd055b6aa5cfaf1c9b1c4093832af77da`; R0–R6 are complete and R7 is separate follow-up only;
- remaining branch pruning is hygiene only and must preserve unique historical documentation/evidence.

## 4. Recovery boundary

Recovery is for recent emergency process interruption only.

Accepted contract:

~~~text
current format
+ exact current compatibility token
+ <=4h freshness
-> current validated recovery
~~~

Do **not** introduce old-format compatibility, migration, tolerant reconstruction or long-term save semantics.

The 2026-09-28 current-only Recovery audit/cleanup is complete through R6 and merged in PR #165. The authority/acceptance record is `docs/CURRENT_ONLY_RECOVERY_MINIMAL_STATE_AUDIT_2026-09-28.md`. R7 bookkeeping/transport/lifecycle ownership remains a separate follow-up rather than unfinished cleanup.

#109 must not be resurrected as a compatibility project. A future bug requires a fresh reproduction from a current-format/current-version write.

## 5. Historical acceptance references

Completed RH-E / #157 / correctness-repair detail is historical evidence, not startup context:

- `docs/archive/checkpoints/sde/SDE_RH_E_RUNTIME_PERSISTENCE_TIMING_COMPLETION_2026-09-27.md`
- `docs/archive/checkpoints/sde/SDE_PR157_MAIN_INTEGRATION_COMPLETION_2026-09-27.md`
- `docs/archive/checkpoints/sde/SDE_POST_AUDIT_CORRECTNESS_REPAIR_ROUTE_2026-09-25.md`
- `docs/archive/checkpoints/sde/SDE_INTEGRATION_CLOSURE_ROUTE_2026-09-25.md`

## 6. Current execution workflow

Use `docs/AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md` as authority:

- GitHub Connector is the default repository / PR / CI / result-analysis interface;
- Mini MCP + Luna is the large/truncated-file mechanical execution supplement after Chat fixes the design;
- strong Codex may be used read-only when complete local context is genuinely required before design;
- N3150 remains retired from the default validation path;
- GitHub CI/R2 remains the Android validation and independent acceptance surface.

## 7. Active development lane

DLB-0 accepted executable checkpoint: `f81350ec1341cb83f2b4c2f31d80b9c61c7cec52`; CI #3506 and R2 #3251 GREEN.

DLB-1 accepted executable checkpoint: `d065e21bcf1780fe3375a7fd380815259cc59e5c`; CI #3515 and R2 #3259 GREEN. DLB-1 establishes visible-roster seating as the production pre-Drunk path; the compatibility deal plan is temporary downstream wiring until DLB-4.

DLB-2 accepted executable checkpoint: `73de263327d7530e2d6ac128753aca2fd8766766`; CI #3521 and R2 #3264 GREEN. DLB-2 owns legal Drunk-seat enumeration and immutable hypothetical GameState projection.

DLB-3A accepted checkpoint: `0a59c29af047b91b5d10b62ce4019f60df632e83`; CI #3526 and R2 #3268 GREEN. The shadow setup-precommit surface now emits normal SDE candidates, ecology census, DecisionTrace and replay input; frozen V1 remains deferred and no Drunk candidate is selected.

DLB-3B1 accepted checkpoint: `84dcf50d7538fad477be692e027c232b9884618a`; CI #3542 and R2 #3281 GREEN. Every Drunk shadow candidate now also carries a score-free setup consequence envelope covering factual adjacent topology, candidate-seat first-night information-route shape, and explicit unresolved/player-controlled limitations. No DecisionFeatures/V1/selector semantics changed.

DLB-3B2 accepted checkpoint: `cc6f3972ebc6128cd88941f60242eef0d147c813`; CI #3554 and R2 #3291 GREEN. Drunk assignment now has a dedicated candidate-aligned feature surface separate from ordinary DecisionFeatures: topology and first-night information opportunity are projected descriptively, longitudinal narrative remains MISSING_CAPABILITY, and frozen V1 still defers with no selection.

DLB-3B3 accepted checkpoint: `1a5403def6e4075b4d45b31e58db8bf3c356a9d4`; CI #3566 and R2 #3301 GREEN. The dedicated feature surface now has a separate versioned shadow replay/experiment lane. `DRUNK_ASSIGNMENT_SHADOW_V1` explicitly defers for missing longitudinal capability and unauthorized ordering evidence, while source actual-choice metadata remains calibration evidence only. DLB-3 is complete; no 3B4 is required by the authoritative route.

DLB-4 accepted executable checkpoint: `5606371c68b97beb01418ede2bb2c19db7e69053`; CI #3580 and R2 #3313 GREEN. The canonical commit seam now owns intermediate + confirmed legal candidate -> final `CommittedClocktowerSetup` / initial `GameState`; production startup, session initialization, rotation/completion and first-night/setup prewarm consume that final truth. Initial Recovery persistence is deferred until finalized setup publication.

DLB-4A accepted executable checkpoint: `c85448831e73c82868e118c7f47a0ed889267c0c`; CI #3586 and R2 #3318 GREEN. Experienced Trouble Brewing Drunk setups now pause after shown-seat preparation and before canonical commit, expose the exact rules-legal Townsfolk candidate domain, and resume through the shared DLB-4 commit only after explicit confirmation. Pending selection is memory-only and backing out returns to setup without Recovery persistence. No Drunk recommendation is presented because no production ordering policy is authorized. Beginner retains only the DLB-4 compatibility-confirmed candidate as a transitional playable baseline; automatic authority remains behind the explicit cutover gate.

DLB-5 current executable checkpoint: `bdc31a31bc4652be13e31d7d4c8c6bacd68b9dd5`; CI #3600 and R2 #3331 GREEN on PR #183. Implemented slices are DLB-5.1 typed dependency planning, DLB-5.2 Red Herring latest-safe commitment, DLB-5.3 Demon-bluff commit at Demon-info presentation dependency, DLB-5.5 removal of obsolete setup auto-apply wiring, and DLB-5.4 Poisoner / first-night information convergence audit. DLB-5.4 found no production gap: poison confirmation owns one canonical revision, unshown drafts are invalidated, displayed observations/history remain committed, and subsequent information replans against current poison state plus committed history. DLB-5 is COMPLETE / ACCEPTED. PR #183 passed final exact-head T4 at `3fd1714ce2e18b969e5039bcf6a58f8775745b9d` with CI #3603 / R2 #3334 GREEN and was squash-merged into `main` at `6293a3bb94778338db61f5a1708a1d283d6e8b6f`.

The current product-development lane is **TBGS-2E Mayor redirect final acceptance**. Implementation checkpoint `f512fa2339677de4e9e8c12ea3694938867ed584` passed CI #3730 / R2 #3432 GREEN. Run exact-head FULL/assemble + ASP + Real Clingo, then close TBGS-2E and perform a fresh post-2E family re-audit.

Current C5 authority:

`docs/SDE_C5_G10_PAIR_FUTURE_FLEXIBILITY_REENTRY_AUDIT_2026-10-03.md`

DLB historical implementation authority remains:

`docs/DLB_AND_APP_HOST_DECOMPOSITION_IMPLEMENTATION_ROUTE_2026-09-29.md`

Architecture convergence guardrail: `docs/CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md` records the intended separation of Setup Generation, canonical Game Session/Game Engine, and read-only Storyteller Recommendation. `docs/TB_CANONICAL_GAME_SNAPSHOT_INTEGRATION_ROUTE_2026-09-30.md` refines the read boundary through immutable `TroubleBrewingGameSnapshotV1` projections. The Q04 evidence gate and production cutover have now passed; DLB-6 removed the obsolete compatibility ownership without changing the bounded Q04 policy.

Immediate sequence:

```text
DLB-0 typed intermediate setup COMPLETE
-> DLB-1 visible-roster deal cutover COMPLETE
-> DLB-2 legal Drunk candidate + hypothetical projector COMPLETE
-> DLB-3A shadow SDE decision / DecisionTrace COMPLETE
-> DLB-3B1 score-free consequence envelope COMPLETE
-> DLB-3B2 dedicated Drunk-assignment feature surface COMPLETE
-> DLB-3B3 shadow replay / versioned policy experiment COMPLETE
-> DLB-4 canonical Drunk commit before reveal COMPLETE
-> DLB-4A Experienced assisted UI COMPLETE
-> DLB-5.1 / 5.2 / 5.3 / 5.4 / 5.5 COMPLETE
-> DLB-5 final acceptance + PR #183 merge COMPLETE / ACCEPTED
-> DLB-5H1 narrow presentation extraction COMPLETE / ACCEPTED
-> Drunk-assignment production cutover gate audit COMPLETE / HISTORICAL NOT-PASSED VERDICT
-> Drunk recommendation-context capability contract COMPLETE / POLICY-NEUTRAL
-> TBGS-0 canonical TB snapshot contract COMPLETE / ACCEPTED
-> TBGS-1 Host snapshot-backed Drunk vertical slice + EvidenceLab interop COMPLETE / ACCEPTED
-> C3-Q04 VERIFIED / ACCEPTED
-> DRUNK_ASSIGNMENT_Q04_V1 + dedicated replay COMPLETE / ACCEPTED
-> Q04 production cutover PASS / ACCEPTED
-> Beginner automatic Drunk authority COMPLETE / ACCEPTED
-> DLB-6 old-contract retirement COMPLETE / ACCEPTED; `52c2e73ca455a62c31065ce0e6fca4edda8713ec`, CI #3639 / R2 #3358 GREEN
-> DLB-7 final acceptance COMPLETE / ACCEPTED; `b760117351e57cfe78cc0bc9483b42306757c22f`, CI #3643 / R2 #3361 GREEN
-> DLB campaign COMPLETE / ACCEPTED
-> TBGS-2A first-night natural-pair snapshot context COMPLETE / ACCEPTED; `9021ef26b65033b69911fa4a79124fdd2137284d`, CI #3647 / R2 #3364 GREEN
-> TBGS-2B pair manual/publication COMPLETE / ACCEPTED; `fa07e382fdad1b03aeadc27ce0d0d939f66f8b67`, CI #3654 / R2 #3370 GREEN
-> TBGS-2C setup-recommendation mechanical/rules base-context migration COMPLETE / ACCEPTED; `a8116d8b333cc40e0a599ab208cf7a9a3ea80207`, CI #3663 / R2 #3378 GREEN
-> TBGS-2D Demon succession COMPLETE / ACCEPTED; final T4 `1f610a25a16c14a7ca7bd38562ff39425508c243`, CI #3726 / R2 #3429 GREEN
-> EvidenceLab G10 `16:52` Librarian future-flexibility predicate E3 PASS at `78f672868ea6603317aeefa20ad91686c5886db9`
-> C5 re-entry audit COMPLETE / PASS; SDE-3D3 implementation AUTHORIZED
-> C5-A pair future-flexibility projector COMPLETE / ACCEPTED; `5b516e10e0f01848a1dad7a06dd214e73faa5a97`, CI #3673 / R2 #3385 GREEN
-> C5-B pair shadow/replay bridge COMPLETE / ACCEPTED; final T4 `fdb24f67c010759a123a6e648bef8d4015832cb1`, CI #3683 / R2 #3393 GREEN
-> C5-C BEGINNER_CONSERVATIVE_V2 COMPLETE / ACCEPTED; final T4 `001330f32e151b67ead1760a67702450faaab685`, CI #3690 / R2 #3399 GREEN
-> C5-D canonical V1/V2 replay COMPLETE / ACCEPTED; `5241518e002e65993eed594632604e13e2979bfd`, CI #3693 / R2 #3401 GREEN
-> C5-E functioning-Librarian production cutover COMPLETE / ACCEPTED; `6afc3b08416eaf6f3fb74a53bb48b8e144044341`, CI #3717 / R2 #3421 GREEN
|| HOST-ML1 typed neutral decision export COMPLETE / ACCEPTED; final `b91fcd298621e9064f11c26c1a0398c5c14fc13c`, CI #3710 / R2 #3416 GREEN; pair semantic payload included
|| machine-readable corpus materialization DEFERRED until concrete offline consumer; do not block C5-E
|| TBGS-2E Mayor redirect IMPLEMENTATION GREEN / T4 PENDING
-> A3 presentation catalog READY / independent maintenance only
```

The App/Host decomposition audit is a constraint on this work, not a prerequisite campaign:

- A1 preferences and A2 archive storage are independent maintenance slices only;
- H1 is COMPLETE / ACCEPTED through DLB-5H1;
- former H2 is now split: TBGS-0/1 are complete standard-snapshot foundations; TBGS-2 is the active incremental runtime migration. TBGS-2A first-night natural-pair precompute, TBGS-2B pair manual/publication, and TBGS-2C setup-recommendation mechanical/rules base-context migration are COMPLETE / ACCEPTED. The post-2C focused family audit selected TBGS-2D Demon succession, which is now COMPLETE / ACCEPTED. The post-2D bounded-family re-audit selected TBGS-2E Mayor redirect; implementation is GREEN pending final T4. Special registration and cross-cutting balance/style remain later candidates; history-producer cutover, protection ownership and spent-ability ownership remain explicit dependencies rather than hidden assumptions. A3 remains READY as a separate maintenance slice, not as a TBGS-2 prerequisite;
- R3 generic transaction extraction and the generic setup-effect owner remain NO-GO.

Recovery R0–R6 stays complete. R7 remains separate.

EvidenceLab C1C/C1D established the replayable Drunk-assignment surface; later VERIFIED C3-Q04 supplied the bounded Q04 Drunk predicate. Separately, EvidenceLab `78f672868ea6603317aeefa20ad91686c5886db9` now supplies a C5-qualified G10 Librarian future-flexibility predicate. That new evidence reopens C5/SDE-3D3 but authorizes only the bounded, generic pair-information weak-preference route recorded in the C5 audit; it does not broaden Q04 or authorize numeric calibration.

The frozen `BEGINNER_CONSERVATIVE_V1` must not silently absorb Drunk candidate preference/rejection semantics. DLB-3 is shadow-first; Beginner automatic Drunk authority requires the explicit cutover gate in the DLB route.

A separate product-owner calibration now exists at `docs/IMP_PODCAST_PRODUCT_POLICY_CALIBRATION_2026-10-02.md`. It captures the current app-specific interpretation of the Imp podcast semantic review: strong plausible/coherent misinformation defaults, cross-night coherence, Empath-as-Drunk as a contextual positive with repeat penalty, bluff-set synergy/complexity, star-pass ability preservation, mechanical-over-inferred lead assessment, player-experience balancing, and Evil intended-plan enrichment. This calibration is explicitly non-evidence and does not unblock the production gate by itself.

## 8. C5 E3/E4 qualification result — re-entry passed

The 2026-09-27 audit remains the historical definition of the gate, but its “no policy delta authorized” verdict is superseded for one bounded predicate by the 2026-10-03 G10 Librarian evidence.

Current result:

- **pair-information future flexibility:** E3 PASS for one qualitative weak-preference predicate;
- source/state/choice/legal-alternative/rationale/generic-feature requirements are all closed;
- E4 remains unproven and is **not required** for the first weight-free qualitative delta;
- healthy-information balance, impaired-information believability, role-function exposure severity, Demon-bluff triplet ordering and multi-axis calibrated weights remain separately unproven unless later evidence closes their own gates.

Implementation route:

```text
C5 re-entry audit                         COMPLETE / PASS
-> C5-A future-flexibility projector     COMPLETE / ACCEPTED
-> C5-B pair shadow/replay bridge        COMPLETE / ACCEPTED
-> C5-C BEGINNER_CONSERVATIVE_V2 weak preference COMPLETE / ACCEPTED
-> C5-D canonical V1/V2 replay COMPLETE / ACCEPTED
-> C5-E surface-scoped functioning-Librarian production cutover COMPLETE / ACCEPTED
```

`BEGINNER_CONSERVATIVE_V1` remains immutable. V2 must not be created as a placeholder: its first implementation is permitted only in C5-C after the C5-A projector and C5-B pair replay seam are accepted.

Current authority: `docs/SDE_C5_G10_PAIR_FUTURE_FLEXIBILITY_REENTRY_AUDIT_2026-10-03.md`.

## 8.1 HOST-ML0 accepted / HOST-ML1 typed neutral export accepted

EvidenceLab EL-ML0 and Host HOST-ML0 froze the long-horizon ML ownership boundary. HOST-ML1 now implements the bounded Host-side neutral export without changing the current product route:

```text
EvidenceLab source-backed evidence
-> Host canonical state + complete legal domain + typed context/features/replay
-> RecommendationDecisionExportV1 typed neutral export COMPLETE / ACCEPTED
-> future deterministic machine-readable materialization when a concrete offline consumer requires it
-> future ModelLab dataset recipe / train-eval split / SFT-DPO-training
```

HOST-ML1 is accepted after PR #208 + #209; final executable checkpoint `b91fcd298621e9064f11c26c1a0398c5c14fc13c` with CI #3710 / R2 #3416 GREEN and PR #209 squash merge `2d4681115b9c373bfbfa85a7b385aba4e59e1062`. It physically separates input-eligible state/context/features from historical target, replay/evaluation metadata and provenance, preserves the complete ordered legal domain, and derives only `OBSERVED_CHOICE` versus `LEGAL_UNCHOSEN`. It deliberately does not define source-backed rejection/comparison semantics, training examples, prompts, embeddings, model dependencies or an external-model policy runner. Machine-readable materialization is deferred as a separate future follow-up until a concrete offline consumer fixes the interchange need; trigger EL-ML1 only if source-backed machine-readable evidence seeds are required. Authorities: `docs/HOST_ML_READINESS_MODELLAB_BOUNDARY_AUDIT_2026-10-03.md` and `docs/HOST_ML1_NEUTRAL_DECISION_EXPORT_IMPLEMENTATION_2026-10-03.md`.

## 9. Scope still blocked

Do not add policy weights/thresholds, broaden player-count/Traveller scope, or perform production cutover from the G10 evidence. `BEGINNER_CONSERVATIVE_V2` may begin only at C5-C after C5-A/C5-B acceptance, and only with the bounded future-flexibility weak preference. SDE-3E remains blocked per surface until C5-D replay and the separate C5-E cutover gate pass.
