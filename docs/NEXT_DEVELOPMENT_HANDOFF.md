# CampBoardGameHost — Next Development Handoff

> Updated: 2026-10-08 Australia/Sydney  
> Current baseline: live `main` — query exact HEAD/PR state at session start  
> RES checkpoint: **RES-0/1/2/3/4/5 COMPLETE / ACCEPTED; RES CAMPAIGN CLOSED.** RES-5 completion authority is `docs/RES_5_PHYSICAL_MODULE_DEPENDENCY_CONVERGENCE_ACCEPTANCE_2026-10-07.md`. Final T4 checkpoint `cdf5dba23eaad23f4fea3899ab54df97553a55cd` passed CI #3832 and R2 #3509, including full Android unit tests + debug assemble, ASP contracts and Real Clingo.  
> Architecture invariant: **a provider may be stateless, but recommendation context is not memoryless.** The Host owns and reconstructs current canonical state, current-game longitudinal/narrative history, relevant cross-game player experience and recommendation/diversity history. LLM/chat conversation memory is never correctness authority.  
> Current execution target: **GSP-R1C2C-1C6 — real TB/NGJ day registration and mechanical action coverage/recovery re-audit**. **C2C-1C5C NGJ KLUTZ LEARNED-DEATH / PUBLIC PLAYER CHOICE COMPLETE / ACCEPTED** via [PR #280](https://github.com/Jazz0006/CampBoardGameHost/pull/280), squash `1f457783341d5bc9c230f804eac20e653cac6a04`, exact-head `ca57affc3ecb3c27a77d58a78fc7f89d1e0cd66c`, **CI #3960 FULL T4 GREEN** (Android FULL/debug APK, ASP, Real Clingo) and **R2 #3599 GREEN**. [Acceptance](GSP_R1C2C1C5C_NGJ_KLUTZ_ACCEPTANCE_2026-10-09.md) / [scope](GSP_R1C2C1C5C_NGJ_KLUTZ_LEARN_CHOICE_SCOPE_2026-10-09.md): actual NGJ Host public death announcement separately commits `KlutzLearnedDeath` with a real earlier Death/Execution proof; actual Klutz's subsequent *player-selected* living seat commits `KlutzChoice` before outcome and phase mutation. Strict GLOBAL_V1 and Recovery requires original death → learned event → choice chronological sequence, rejects fabricated/duplicate actions, old missing proof stays UNKNOWN. Public replay hides real character, learned ability condition and target actual alignment. Current NGJ has no Poisoner; this does NOT create a generic cross-script learned-time poison subsystem or any fictitious TB Klutz/Spy. Earlier **1C5B TB Slayer/Virgin day convergence ACCEPTED** in [PR #278](https://github.com/Jazz0006/CampBoardGameHost/pull/278), CI #3955 T4 / R2 #3596; **1C5A true roster fix ACCEPTED** [#276](https://github.com/Jazz0006/CampBoardGameHost/pull/276), CI #3948 T4 / R2 #3591. **1C6 NEXT**: audit *all* actually reachable day registration/ability/choice family producer paths, explicit actual/untouched registration, strict as-of Recovery and legacy unknown against real TB and NGJ rosters. Do not automatically declare C2C-1C complete; C2C-2/3 remain, **GSP-R1C2C / R1C remain IN PROGRESS**, and do not start GSP-R2/R3/R4 or production LLM API. **C2C-1B PRIVATE NIGHT RESULT VERTICAL COMPLETE / ACCEPTED** via [PR #265](https://github.com/Jazz0006/CampBoardGameHost/pull/265), squash `81ee09066a9f4648cc42f8ff1abe3a0d157f5ae7`, exact-head **CI #3914 T4 GREEN** (Android full + debug APK, ASP, Real Clingo) and **R2 #3568 GREEN**. [Acceptance](GSP_R1C2C1B_LIVE_REGISTRATION_ACCEPTANCE_2026-10-08.md) / [scope matrix](GSP_R1C2C1B_LIVE_REGISTRATION_PRODUCER_SCOPE_2026-10-08.md): genuine Host result-first preflight and typed journal writer now exist for verifiable private night number/boolean/role results, with multiple-witness legality, explicit-versus-unresolved meaning, repeated confirmation/correction and Recovery preservation. Do **not** assume all night or day abilities covered. Day Virgin/Klutz/Slayer have no matching private-observation anchor: their explicit registrations require separately typed **mechanical/ability event** ownership, not fabricated observations or localized `RoleAction` replay. **C2C-1 OVERALL PARTIAL; C2C/R1C IN PROGRESS.** **C2C-1A FOUNDATION ACCEPTED** via PR #263 (squash `9d7b844d37c8ebad1448836ad3946b26af006d5c`; CI #3893 Android FAST GREEN / R2 #3549 GREEN), see [foundation acceptance](GSP_R1C2C1A_TYPED_REGISTRATION_RECOVERY_ACCEPTANCE_2026-10-08.md). **GSP-R1C2C-0 COMPLETE / ACCEPTED** via [PR #260](https://github.com/Jazz0006/CampBoardGameHost/pull/260) (squash `d75d00031bd83ecf340230041443e7612f2e080b`, CI #3880 Android tests GREEN, R2 #3539 GREEN; no T4). [Live producer inventory](GSP_R1C2C_PRODUCER_COVERAGE_AUDIT_2026-10-08.md) and [C2C-0 exact acceptance](GSP_R1C2C0_GENERIC_REGISTRATION_CHOICE_ACCEPTANCE_2026-10-08.md) establish the five-category production matrix and shared explicit-only registration UI selection snapshot. **This is NOT a typed canonical ruling or Recovery producer; R1C2C overall remains IN PROGRESS.** **GSP-R1C2B MAYOR VERTICAL COMPLETE / ACCEPTED**: PR #258, squash `7d6255922e8db33b7ce297958d6ee467c339a9f4`; CI #3874 T4 GREEN (Android FULL/debug APK, ASP, Real Clingo), R2 #3535 GREEN. Real Mayor confirmation now commits a frozen typed causal outcome; re-confirmation creates a correction; current-format Recovery preserves the journal and validates old prefixes before Ready. This **does not complete all decision families or typed registration rulings**. See `docs/GSP_R1C2B_MAYOR_DURABLE_CAUSAL_RECOVERY_ACCEPTANCE_2026-10-08.md`.  **GSP-R1C2A COMPLETE / ACCEPTED** via PR #256 (merge `38503670d93d925603e12cfc6388990550421c44`; CI #3864 / R2 #3527 GREEN, Android FAST). R1C2A delivered an exact GLOBAL_V1 frozen predecision prefix and an independent **in-memory reference causal journal**, but **no production writer/Recovery codec**. R1C overall remains IN PROGRESS. See `docs/GSP_R1C2A_FROZEN_CAUSAL_DECISION_PREFIX_ACCEPTANCE_2026-10-08.md`.  **R1C1 COMPLETE / ACCEPTED**: PR #252, merge `fae562f0e52d2d8650b28b6b11964e64a41a3766`, CI #3853 / R2 #3520 GREEN (Android FAST + Real Clingo, no full Android T4); real Recovery JSON/strict decode/planner/restored Session Provider-prefix parity, explicit missing-historical-cutoff deferral, Spy/Recluse Empath=1 no fabricated witness. **R1C overall remains IN PROGRESS**. R1B PR #250 remains COMPLETE / ACCEPTED. **GSP-R1A code-level capture/prefix audit is documented** at `docs/GSP_R1A_CANONICAL_HISTORY_CAPTURE_PREFIX_COVERAGE_AUDIT_2026-10-08.md`. Its findings: typed action/observation history is partial; Host registration/UI display text is not a semantic log; production/Recovery decision archive is incomplete; corrections lack causal time; no historical cutoff may be inferred from revisions alone. **Registration ambiguity is a separate accepted semantic constraint:** displayed information is canonical; multiple legal Spy/Recluse witnesses do not select a ruling; retain unresolved or explicitly unknown status rather than promoting the first deduplicated witness. See `docs/GSP_REGISTRATION_AMBIGUITY_RESULT_FIRST_HISTORY_CONTRACT_2026-10-08.md`. Start from freshly checked live `main`. PR #244/#245/#246 remains COMPLETE / ACCEPTED. Preserve `StorytellerProviderContractV1`, canonical Host history and engine legal domains; no `StorytellerPolicy*`, named selectors, SDE-owned prefix types, heuristic rankers or API.  
> R1B acceptance/R1C handoff: `docs/GSP_R1B_NEUTRAL_LIVE_HISTORY_PREFIX_ACCEPTANCE_2026-10-08.md`. R1B materializes only frozen GLOBAL_V1 **live** history, not a historical decision cutoff. All five producer-coverage categories are reported conservatively; registration-ruling and prior-decision chronology remain UNKNOWN. R1C requires genuine Recovery equality at the same cutoff, rejects unfrozen historical replay, and must preserve ambiguous result-first Spy/Recluse semantics. No live LLM API.
> Historical  `docs/GSP_R1A_CANONICAL_HISTORY_CAPTURE_PREFIX_COVERAGE_AUDIT_2026-10-08.md` (code producer/consumer/Recovery matrix), then `docs/GSP_R1_POST_APP_HOST_SPLIT_REAUDIT_2026-10-08.md` and `docs/GSP_POST_RES5_REENTRY_AUDIT_2026-10-08.md`. Current Recovery player context is complete, not the canonical decision archive. `ClocktowerInformationHistoryPayload` remains localized UI history, not epistemic history; `LEGACY_LOCAL` cannot masquerade as globally ordered. Android remains offline-first.

### Superseded GSP-2B3 PR cleanup — 2026-10-08

- [PR #230](https://github.com/Jazz0006/CampBoardGameHost/pull/230) (`gsp-2b3-recovery-player-context`, head `2825a81409d2c7abfcb8a6a94d3708a19b5369a7`) was **CLOSED / UNMERGED / SUPERSEDED**. Current `main` already owns typed Storyteller per-seat player context and current-format Recovery persistence. Do not cherry-pick the old Recovery-v3 rewrite. Its remote branch is **eligible for deletion**; no local same-name branch existed in the Mini MCP checkout at audit.
- [PR #232](https://github.com/Jazz0006/CampBoardGameHost/pull/232) (`gsp-2b3b-player-context-edit-surface`, head `75e1943d7b7eb2145273e766100a2fb734f12c83`) was **CLOSED / UNMERGED / SUPERSEDED**, but **KEEP THE REMOTE BRANCH AS AN R2 IMPLEMENTATION REFERENCE**. Its `ClocktowerPlayerContextEditing.kt` and `PlayerExperienceProfileCodec.kt`, App settings/game-review editors and tests have not been adopted into `main`. When R2 is actually selected, re-audit against live architecture for per-game role claims/pressure, durable cross-game experience profile, stable player IDs, consent and UI ownership; do not merge this stale feature branch wholesale.
- Neither closure advances GSP-R2. Active priority remains **GSP-R1C2C-1**. Closure rationale/hand-off details are preserved on the respective PR discussion threads. Remote #230 branch deletion may require separate GitHub branch-management UI because current connected interfaces expose closure but not branch deletion.

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
15. `docs/TB_CANONICAL_GAME_SNAPSHOT_INTEGRATION_ROUTE_2026-09-30.md` — TB-only standard snapshot / Drunk vertical slice / EvidenceLab interoperability route; TBGS-0/1 complete, TBGS-2 paused after accepted 2A–2E
16. `docs/TBGS_2_RUNTIME_RECOMMENDATION_STATE_MIGRATION_AUDIT_2026-10-03.md` — TBGS-2 consumer inventory and accepted 2A/2B/2C lineage
17. `docs/TBGS_2C_SETUP_COORDINATION_SNAPSHOT_CONTEXT_AUDIT_2026-10-03.md` — TBGS-2C setup-coordination acceptance authority
18. `docs/TBGS_2D_DYNAMIC_STATE_CONSUMER_SELECTION_AUDIT_2026-10-03.md` — post-2C DynamicGameState family audit and accepted TBGS-2D Demon-succession boundary
19. `docs/TBGS_2E_POST_2D_MAYOR_REDIRECT_REAUDIT_2026-10-04.md` — accepted TBGS-2E Mayor-redirect boundary
20. `docs/RES_ENGINE_RECOMMENDATION_SEPARATION_AND_PURGE_ROUTE_2026-10-06.md` — RES architecture authority; RES-0 through RES-5 complete / accepted
21. `docs/GSP_GENERAL_STORYTELLER_POLICY_ROUTE_2026-10-05.md` — current provider/benchmark authority after RES separation; GSP-R1 next
22. `docs/GSP_POST_RES5_REENTRY_AUDIT_2026-10-08.md` — post-RES live-code audit, stale-gap correction and GSP-R1/R2/R3/R4 route authority
    - `docs/GSP_R1A_CANONICAL_HISTORY_CAPTURE_PREFIX_COVERAGE_AUDIT_2026-10-08.md` — R1A code-level canonical capture, missing producers, correction/prefix/Recovery risks; governing R1B implementation boundary
    - `docs/GSP_REGISTRATION_AMBIGUITY_RESULT_FIRST_HISTORY_CONTRACT_2026-10-08.md` — **accepted result-first Spy/Recluse registration ambiguity invariant**; generic selection UI fixed PR #254, typed registration producer/Recovery still open; distinguishes shown result, derived witness, explicit adjudication and unknown historical status
    - `docs/GSP_REGISTRATION_RESULT_WITNESS_SEPARATION_FIX_ACCEPTANCE_2026-10-08.md` — **accepted PR #254**, CI #3860/R2 #3525, explains root-cause non-unique Chef/Empath/Fortune Teller/role results, explicit-only record and pending durable semantics
    - `docs/GSP_R1B_NEUTRAL_LIVE_HISTORY_PREFIX_ACCEPTANCE_2026-10-08.md` — **GSP-R1B accepted** PR #250 and CI/R2; current R1C handoff, Recovery equality/correction/frozen-prefix/registration-ambiguity gates
    - `docs/GSP_R1C1_RECOVERY_PREFIX_PARITY_AND_HISTORICAL_CUTOFF_ACCEPTANCE_2026-10-08.md` — **GSP-R1C1 accepted** PR #252 with actual FAST tests; **R1C2 next** causal predecision cutoffs, later correction and historical replay safety
    - `docs/GSP_R1C2A_FROZEN_CAUSAL_DECISION_PREFIX_ACCEPTANCE_2026-10-08.md` — **GSP-R1C2A accepted** PR #256 with FAST + R2; R1C2B successor completed Mayor vertical real production capture and durable Recovery/as-of replay; no upgrade of legacy registration candidates
    - `docs/GSP_R1C2B_MAYOR_DURABLE_CAUSAL_RECOVERY_ACCEPTANCE_2026-10-08.md` — **GSP-R1C2B Mayor vertical accepted**, PR #258 T4 and R2; **R1C2C next** broaden authoritative producer/typed-registration coverage, no implicit witness
    - `docs/GSP_R1C2C_PRODUCER_COVERAGE_AUDIT_2026-10-08.md` — live producer inventory, distinct player action/observation/Storyteller ruling and prioritized typed capture gaps
    - `docs/GSP_R1C2C0_GENERIC_REGISTRATION_CHOICE_ACCEPTANCE_2026-10-08.md` — PR #260 explicit-only generic UI choice acceptance; **NOT** typed ruling/Recovery
23. `docs/LRE_1_MANUAL_SAFETY_GATE_COMPLETION_2026-10-04.md` — accepted fail-closed Manual safety foundation; its old LRE-P continuation is superseded by GSP
24. `docs/LEGACY_RECOMMENDATION_ENGINE_RETIREMENT_AUDIT_2026-10-04.md` — whole-engine retirement evidence/inventory authority; preserve legal/manual/SDE infrastructure
25. `docs/archive/LRE_STAGED_POLICY_REPLACEMENT_AND_RETIREMENT_ROUTE_2026-10-04.md` — superseded historical route; do not use its family-by-family LRE-P loop as current authority
26. `docs/archive/TBGS_2_POST_2E_STYLE_RETIREMENT_REAUDIT_2026-10-04.md` — historical post-2E audit; no TBGS-2F selected
27. `docs/archive/RECOMMENDATION_STYLE_RETIREMENT_PLAYER_LEVEL_AUDIT_2026-10-04.md` — historical RSR-0 style/player-level audit
28. `docs/archive/RSR_1_STYLE_NEUTRAL_POLICY_READINESS_AUDIT_2026-10-04.md` — historical RSR-1A accepted ownership-decoupling record
29. `docs/CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md` — long-horizon Setup / Game Engine / Storyteller Recommendation / canonical-session target; use as an ownership guardrail, not as permission to broaden the current slice
30. `docs/HOST_ML_READINESS_MODELLAB_BOUNDARY_AUDIT_2026-10-03.md` — HOST-ML0 accepted ownership/export/model-training boundary
31. `docs/HOST_ML1_NEUTRAL_DECISION_EXPORT_IMPLEMENTATION_2026-10-03.md` — HOST-ML1 complete typed neutral export acceptance, candidate semantic payload, leakage boundary and deferred machine-readable materialization
32. `docs/DLB_5_STAGED_FIRST_NIGHT_DEPENDENCY_PLANNER_AUDIT_2026-09-29.md` — completed DLB-5 closeout record

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
TBGS-2 runtime recommendation projection migration PAUSED AFTER 2E — 2A/2B/2C/2D/2E COMPLETE / ACCEPTED; NO 2F SELECTED
App/Host bounded decomposition           RES-5 A1/A2/H0/H1 COMPLETE / ACCEPTED; A3 OPTIONAL; broader transaction/assembly work requires fresh audit
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

LRE-1 is **COMPLETE / ACCEPTED** under `docs/LRE_1_MANUAL_SAFETY_GATE_COMPLETION_2026-10-04.md` and remains the safety foundation: unique legal outcomes may auto-resolve; unsupported multi-choice decisions must expose the complete Manual legal domain and may not fall back to legacy heuristics. PR #222 later merged INV1-A under the then-current LRE-P approach. GSP-0/1 and GSP-2A/2B1/2B2 remain useful historical/provider foundations; their former immediate continuation was superseded by RES. **RES-0 through RES-5 are COMPLETE / ACCEPTED; neutral-contract GSP re-entry is the next executable target.**

Current C5 authority:

`docs/SDE_C5_G10_PAIR_FUTURE_FLEXIBILITY_REENTRY_AUDIT_2026-10-03.md`

DLB historical implementation authority remains:

`docs/DLB_AND_APP_HOST_DECOMPOSITION_IMPLEMENTATION_ROUTE_2026-09-29.md`

Architecture convergence guardrail: `docs/CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md` records the intended separation of Setup Generation, canonical Game Session/Game Engine, and read-only Storyteller Recommendation. `docs/TB_CANONICAL_GAME_SNAPSHOT_INTEGRATION_ROUTE_2026-09-30.md` refines the read boundary through immutable `TroubleBrewingGameSnapshotV1` projections. GSP preserves these foundations while changing the policy layer: a future provider reasons over Host-owned canonical context and legal candidates; it never owns legality or state mutation. Q04/Librarian/INV1-A now remain historical/shadow/reference evidence after GSP-1 removed their discretionary automatic production authority.

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
|| TBGS-2E Mayor redirect COMPLETE / ACCEPTED
-> post-2E fresh re-audit COMPLETE — no TBGS-2F selected
-> RSR-0 RecommendationStyle / player-level audit COMPLETE / ACCEPTED
-> RSR-1A Storyteller-mode/style ownership decoupling COMPLETE / ACCEPTED; final T4 `d8ac254b8f40d2a5106c871dcd9eaa567ab93fde`, CI #3740 / R2 #3439; PR #217 merged `881d4252c1a242c5ea523718fb8f780f693e9ab5`
-> LRE-0 whole legacy heuristic recommendation-engine audit COMPLETE / RETIREMENT AUTHORIZED
-> LRE-1 Manual fallback + fail-closed recommendation authority COMPLETE / ACCEPTED; executable `045b3a6884f765149d4f1802d5e369671d38d938`, CI #3752 / R2 #3448
-> INV1-A functioning-Investigator special-policy cutover merged PR #222; merge `420eef4e21769e3adf8928f187009f2afc1e5fa2`
-> LRE-P family-by-family special-policy continuation SUPERSEDED / DO NOT CONTINUE
-> GSP-0 route reset COMPLETE / DOCS-ONLY
-> GSP-1 discretionary automatic authority revocation COMPLETE / ACCEPTED
-> GSP-2A/2B1/2B2 provider/context foundations COMPLETE / ACCEPTED; old 2B3/2C/2D continuation PAUSED
-> RES-0 separation/purge audit COMPLETE / ACCEPTED
-> RES-1 generic engine-owned pending-decision boundary COMPLETE / ACCEPTED
-> RES-2 neutral provider contract + direct pair/Mayor/Drunk materialization COMPLETE / ACCEPTED
|| Host-owned context-memory invariant: current-game longitudinal/narrative + relevant cross-game player + soft diversity history; provider chat memory not authoritative
-> RES-3 legacy heuristic/style/weighted product reachability + physical purge COMPLETE / ACCEPTED — checkpoint 5b4b2a94; CI #3815 / R2 #3495 GREEN
-> RES-4 named deterministic special-policy purge COMPLETE / ACCEPTED — T4 00aefd1b; CI #3824 / R2 #3502 GREEN
-> RES-5 physical module/dependency convergence COMPLETE / ACCEPTED — T4 `cdf5dba23eaad23f4fea3899ab54df97553a55cd`; CI #3832 / R2 #3509 GREEN
-> GSP-R0 post-RES-5 re-entry audit COMPLETE
-> GSP-R1 neutral current-game longitudinal context NEXT
-> GSP-R2 Storyteller player-context edit + durable experience profile
-> GSP-R3 generic cross-game recommendation/diversity context
-> GSP-R4 prompt/response materializer + local validator
-> GSP-3A manual blind benchmark -> repeated/cross-model benchmark -> optional API adapter
|| Android product remains offline-first; remote LLM optional only
|| future player profile/management: per-player BEGINNER / NORMAL / EXPERT; default NORMAL
|| A3 presentation catalog remains READY / independent maintenance only
```

The App/Host decomposition audit is a constraint on this work, not a prerequisite campaign:

- A1 preferences and A2 archive storage are independent maintenance slices only;
- H1 is COMPLETE / ACCEPTED through DLB-5H1;
- former H2 is now split: TBGS-0/1 are complete standard-snapshot foundations; TBGS-2A through 2E are COMPLETE / ACCEPTED and TBGS-2 pauses after 2E. Historical RSR-0/1A completed the style/player-level ownership cleanup. LRE-1 removed unsafe legacy fallback authority and remains COMPLETE / ACCEPTED. LRE-P is superseded by GSP: no new expert-case predicate should become a production named policy. Special-registration typed history and other context remain useful to a future general provider/benchmark, not as reasons to preserve or extend legacy ranking. A3 remains READY as a separate maintenance slice, not as a TBGS-2 prerequisite;
- R3 generic transaction extraction and the generic setup-effect owner remain NO-GO.

Recovery R0–R6 stays complete. R7 remains separate.

EvidenceLab C1C/C1D established the replayable Drunk-assignment surface; later VERIFIED C3-Q04 supplied the bounded Q04 Drunk predicate. Separately, EvidenceLab `78f672868ea6603317aeefa20ad91686c5886db9` now supplies a C5-qualified G10 Librarian future-flexibility predicate. That new evidence reopens C5/SDE-3D3 but authorizes only the bounded, generic pair-information weak-preference route recorded in the C5 audit; it does not broaden Q04 or authorize numeric calibration.

The frozen `BEGINNER_CONSERVATIVE_V1` must not silently absorb Drunk candidate preference/rejection semantics. DLB-3 is shadow-first; Beginner automatic Drunk authority requires the explicit cutover gate in the DLB route.

`docs/IMP_PODCAST_PRODUCT_POLICY_CALIBRATION_2026-10-02.md` is retained as historical product-owner reasoning/context, not as an executable policy target. Its useful considerations — plausible/coherent misinformation, cross-night coherence, contextual Empath-as-Drunk value with repeat penalty, bluff-set synergy/complexity, star-pass ability preservation, mechanical-over-inferred lead assessment, player-experience balancing and Evil intended-plan context — may inform GSP benchmark/provider context, but must not be translated directly into another named special-case selector.

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

Do not add new hand-authored policy weights/thresholds, broaden player-count/Traveller scope as part of RES, or create another evidence-case deterministic selector. Historical C5/V2/SDE-3E gates remain acceptance history only; they are not prerequisites for RES. RES must preserve their useful evidence/replay facts while removing obsolete executable policy authority.


## RES architecture reset — 2026-10-06

The previous immediate GSP continuation was paused by the 2026-10-06 RES reset. That live-code audit confirmed that authority revocation did not physically remove old recommendation behavior: `ClocktowerHostScreen` still reaches legacy registration/pair/number recommendation paths and `GameBalanceEvaluator`, `RecommendationStyle` still flows through Host/night information surfaces, and `ClocktowerRecommendationCoordinator` still combines legal-domain, scoring, selection, history and explanation responsibilities. At the same time the new `StorytellerPolicyRequestV1` remains coupled to SDE/TB-specific export types.

Current sequence is now: **RES-0..5 COMPLETE / ACCEPTED -> GSP-R0 COMPLETE -> GSP-R1 neutral current-game longitudinal context NEXT -> R2 player context/profile -> R3 cross-game/diversity -> R4 prompt materializer -> GSP-3A manual benchmark**.

RES-1 proved the generic engine-owned pending-decision seam with pair information and Mayor redirect. RES-2 established the neutral provider protocol and direct pair/Mayor/Drunk request materialization. PR #237 also freezes the context-memory ownership rule: provider invocations remain stateless and independently reconstructable, while the Host owns current-game longitudinal/narrative memory plus relevant cross-game player and soft-diversity history. RES-3 removed live product/UI/session dependence on the old recommendation ranking stack and physically deleted its dynamic/setup recommenders, style/profile/weighted selectors, local rank/weight metadata and dead ranking tests/benchmarks. Historical `StorytellerDecisionEvent` score/probability/pressure/explanation fields remain only as archival/replay compatibility data; they are not current recommendation authority. RES-3 T4 acceptance is GREEN at `5b4b2a94c332bde462124b133955514a31f23183` (CI #3815 / R2 #3495).


### GSP-2B1 acceptance — 2026-10-06

GSP-2B1 adds model-neutral per-seat enrichment defaults (NORMAL experience, empty claims, absent manual pressure), prefix-bounded effective prior-decision projection, a stateless-provider invocation envelope and immutable decision-episode records. Legacy selector scores/probabilities/candidate audits are intentionally excluded from provider context. The GSP-2B1 data remains valid, but the old GSP-2B continuation was superseded by RES; resumed work must target the neutral provider contract. Recommendation must remain a read-only consumer.


### GSP-2B2 session ownership acceptance — 2026-10-06

Player experience / claimed roles / Storyteller-declared pressure are no longer recommendation-owned types. The neutral input model lives in the domain layer; `ClocktowerSessionState` owns sparse overrides and editing them increments only `playerInputRevision`. Defaults remain NORMAL / empty claims / absent pressure. This gives multi-round provider freshness without introducing another mutable state owner. Recovery serialization and UI/edit wiring are deferred until RES establishes the engine/provider ownership boundary; they are no longer the immediate next checkpoint.
