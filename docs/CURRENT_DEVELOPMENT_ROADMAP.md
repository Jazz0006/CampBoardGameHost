# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-10-02 Australia/Sydney  
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
DLB-0 typed intermediate setup                         COMPLETE / ACCEPTED
DLB-1 visible-roster deal cutover                      COMPLETE / ACCEPTED
DLB-2 legal Drunk candidate + hypothetical projector   COMPLETE / ACCEPTED
DLB-3A shadow envelope + DecisionTrace/replay            COMPLETE / ACCEPTED
DLB-3B setup-level consequence feature bridge              COMPLETE / ACCEPTED
DLB-4 canonical Drunk commit before reveal                 COMPLETE / ACCEPTED
DLB-4A Experienced assisted Drunk selection                COMPLETE / ACCEPTED
DLB-5.1 / 5.2 / 5.3 / 5.4 / 5.5 staged dependency work    COMPLETE
DLB-5 overall                                               COMPLETE / ACCEPTED
DLB-5H1 first-night evil-information presentation extraction COMPLETE / ACCEPTED
DLB Drunk-assignment initial production cutover audit        COMPLETE / HISTORICAL NOT-PASSED VERDICT
TBGS-0 canonical TB snapshot contract                       COMPLETE / ACCEPTED
TBGS-1 Drunk snapshot vertical slice / EvidenceLab interop   COMPLETE / ACCEPTED
Post-TBGS-1 cutover recheck                                 COMPLETE / HISTORICAL NOT-PASSED VERDICT
C3-Q04 production-policy re-entry audit                     COMPLETE / EVIDENCE GATE PASSED
DRUNK_ASSIGNMENT_Q04_V1 implementation / replay              COMPLETE / ACCEPTED
Q04 V1 production cutover                                   COMPLETE / ACCEPTED
Beginner automatic Drunk authority                          COMPLETE / ACCEPTED — Q04 V1
DLB-6 old-contract retirement                               COMPLETE / ACCEPTED
DLB-7 final DLB acceptance                                  NEXT
TBGS-2 runtime recommendation projection migration           DEFERRED / POST DLB-7
Bounded App/Host decomposition                         MAINTENANCE GUARDRAIL / NO BROAD CAMPAIGN
C5 evidence-backed policy evolution                   BLOCKED ON QUALIFYING E3/E4
SDE-3E automatic production cutover                   BLOCKED PER SURFACE
~~~

## 2. Current repository boundary

Current canonical baseline: `main`.

PR #157 — `SDE correctness repair: close CR-A CR-B CR-C` — was explicitly authorized and squash-merged on 2026-09-27.

PR #157 merged executable integration baseline:

`02845a470761a988f0041d8c1027b0e2c58a7e05`

Current canonical baseline is the live `main`; query its actual HEAD before executable work instead of copying a branch SHA into this live document.

PR #165 — `Recovery: enforce current-only minimal persistence` — was explicitly authorized and squash-merged on 2026-09-28 at historical merge checkpoint `b297484cd055b6aa5cfaf1c9b1c4093832af77da`. Recovery R0–R6 are complete; R7 remains a separate ownership follow-up and is not part of the completed cleanup boundary.

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

DLB-0 accepted executable checkpoint: `f81350ec1341cb83f2b4c2f31d80b9c61c7cec52`; CI #3506 and R2 #3251 GREEN. DLB-0 added only the typed intermediate setup contract and owning test.

DLB-1 accepted executable checkpoint: `d065e21bcf1780fe3375a7fd380815259cc59e5c`; CI #3515 and R2 #3259 GREEN. Trouble Brewing production now seats visible identities into the DLB intermediate contract before any canonical Drunk-seat decision; current App/history remain behind an explicitly temporary compatibility bridge until DLB-4.

DLB-2 accepted executable checkpoint: `73de263327d7530e2d6ac128753aca2fd8766766`; CI #3521 and R2 #3264 GREEN. Every dealt Townsfolk seat is now a rules-legal peer Drunk candidate, and each legal choice can be projected into an immutable hypothetical GameState without session mutation or compatibility-bridge authority.

DLB-3A accepted checkpoint: `0a59c29af047b91b5d10b62ce4019f60df632e83`; CI #3526 and R2 #3268 GREEN. The shadow surface preserves the complete legal domain, candidate-specific hypothetical/ecology evidence, DecisionTrace and replay, while frozen V1 correctly defers with no Drunk recommendation.

TBGS-0 accepted executable checkpoint: `f367c0d3ec23ebf452c924ff7c0921cd978a800f`; CI #3618 and R2 #3343 GREEN. `TroubleBrewingGameSnapshotV1` now provides the TB-only immutable read/interchange contract with explicit `KNOWN / UNCOMMITTED / UNKNOWN / NOT_APPLICABLE` semantics, stable external role IDs, precommit/committed/runtime projectors, deterministic V1 JSON, and the G10 precommit golden fixture. No recommendation policy, mutable owner, Host/UI migration, Recovery behavior, or non-TB generalization changed.

TBGS-1A Host vertical slice accepted executable checkpoint: `ae4dc2400325d233da033d3c86d2863bde1bd485`; CI #3621 and R2 #3345 GREEN. The rules-owned Drunk legal domain can now derive stable `(seat, shownRoleId)` candidate refs directly from `TroubleBrewingGameSnapshotV1`; a typed policy-neutral `DrunkAssignmentDecisionContext` carries snapshot, freshness revision, legal candidates, decision identity and selection seed; the existing shadow path consumes that context without changing candidate order, DecisionTrace/replay behavior, `BEGINNER_CONSERVATIVE_V1`, or `DRUNK_ASSIGNMENT_SHADOW_V1` deferral semantics. The G10 V1 JSON fixture is consumed without Host setup objects.

TBGS-1B EvidenceLab interoperability is now complete at observed EvidenceLab checkpoint `970e7e6430f7088ac004cd1e5696759da4d52003`. EvidenceLab materializes the same G10 pre-Drunk V1 semantics from one historical prefix without copying Host legality or policy. A fresh Host-side comparison confirmed the Host and EvidenceLab G10 fixture payloads are byte-for-byte identical (2115 bytes each). TBGS-1 overall is therefore COMPLETE / ACCEPTED. The post-TBGS-1 cutover recheck remains NOT PASSED solely because no C3 Stage-1 VERIFIED candidate-comparison/rejection evidence and therefore no production-capable versioned Drunk policy exist yet.

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

## 4. Current priority — DLB implementation with targeted evidence in parallel

The product route changed on 2026-09-29 after the Drunk late-binding and App/Host decomposition audits were reconciled.

DLB-0 is implemented and accepted. The current implementation authority remains:

`docs/DLB_AND_APP_HOST_DECOMPOSITION_IMPLEMENTATION_ROUTE_2026-09-29.md`

The new Trouble Brewing setup contract makes Drunk assignment a post-seat Storyteller/SDE decision rather than a template-owned pre-seat input. DLB-0 established the typed intermediate state; DLB-1 cut production preparation over to visible-roster seating; DLB-2 provides the rules-owned legal Drunk-seat domain and pure hypothetical GameState projection; DLB-3 carries that domain through SetupPrecommit / DecisionTrace / replay and the dedicated shadow policy-experiment lane while frozen V1 remains unchanged. DLB-4 canonical Drunk commit is COMPLETE / ACCEPTED at `5606371c68b97beb01418ede2bb2c19db7e69053` with CI #3580 / R2 #3313 GREEN; DLB-4A Experienced assisted Drunk selection is COMPLETE / ACCEPTED at `c85448831e73c82868e118c7f47a0ed889267c0c` with CI #3586 / R2 #3318 GREEN. DLB-5 is COMPLETE through 5.1 typed dependency planning, 5.2 Red Herring latest-safe commitment, 5.3 Demon-bluff presentation-barrier commitment, 5.4 Poisoner / first-night information convergence audit, and 5.5 obsolete setup auto-apply cleanup. The latest production executable checkpoint remains `bdc31a31bc4652be13e31d7d4c8c6bacd68b9dd5` with CI #3600 / R2 #3331 GREEN; 5.4 found no production gap and required documentation only. Final exact-head T4 acceptance ran on `3fd1714ce2e18b969e5039bcf6a58f8775745b9d` with CI #3603 / R2 #3334 GREEN, including Android `testFull + assembleDebug`, ASP contracts and Real Clingo cross-validation. PR #183 was squash-merged into `main` at `6293a3bb94778338db61f5a1708a1d283d6e8b6f`. That historical Beginner compatibility fallback was superseded by the accepted Q04 V1 production cutover and has now been retired by DLB-6.

The 2026-09-28 App/Host decomposition audit remains a guardrail rather than a prerequisite campaign, but the former H2 projection item has now been refined by the TB snapshot audit:

- A1 preferences storage and A2 archive storage remain independent maintenance slices;
- H1 is COMPLETE / ACCEPTED through DLB-5H1;
- former H2 is split into **TBGS-0/1 now** (standard TB snapshot + Drunk vertical slice) and **TBGS-2 later** (incremental runtime consumer migration);
- A3 remains deferred until DLB setup/presentation fan-out stabilizes;
- R3 generic transaction extraction and generic setup-effect ownership remain NO-GO.

TBGS-0/1 is not a broad Host decomposition campaign. It establishes the read-only semantic boundary required before the first new production-capable Drunk recommendation request is introduced.

Targeted EvidenceLab C1C Drunk-assignment acquisition is complete at 3 / 3 replayable cases, and C1D downstream replay is accepted. EvidenceLab now serves two distinct lanes:

1. DLB Drunk-assignment evidence/trace replay and consequence-contract calibration for the new decision surface;
2. the existing C5/V2 E3/E4 policy gate.

Do not conflate those gates. C5 / BEGINNER_CONSERVATIVE_V2 remains blocked. DLB may implement legality, projection, canonical commit, Experienced assisted UX, staged dependencies and shadow trace before any new evidence-backed production preference is authorized.

The frozen `BEGINNER_CONSERVATIVE_V1` must not silently absorb Drunk candidate ordering/rejection semantics. Any production Drunk-selection policy delta requires an explicitly versioned decision-surface contract.

The 2026-09-30 Drunk-assignment production cutover audit remains the historical NOT-PASSED verdict. On 2026-10-02, EvidenceLab Q04 became the first primary-audio VERIFIED C3 Stage-1 conditional preference at checkpoint `08d95a0c258f687187c0476a0f430fa5ff8229cb`. The bounded ordering-evidence gate passed, `DRUNK_ASSIGNMENT_Q04_V1` was implemented as the smallest evidence-authorized Q04-only Empath -> Monk override with explicit baseline fallback, and its dedicated replay was accepted at `5cf72a54a62c87763279c02014485a847724b72e` with CI #3631 / R2 #3351 GREEN. Beginner production wiring was then completed at `0ef3760ee233b0e20fa0dd272b12380abe8ee4a4`. Final exact-head T4 acceptance `9762d5a759bf0eaa81a1f6cb5af1aa28281d3ac2` passed CI #3633 and R2 #3353, including Android `testFull + assembleDebug`, ASP contracts and Real Clingo. The Q04 production cutover gate is therefore PASS and Beginner automatic Drunk authority is accepted. `DRUNK_ASSIGNMENT_SHADOW_V1` remains deferral-only and `BEGINNER_CONSERVATIVE_V1` remains immutable. DLB-6 then retired the old DealPlan/pre-seat selector/scorer, `CompatibilityImmediate`, prepared-setup compatibility-candidate ownership and current-model `selectedDrunkShownRole`; final executable checkpoint `52c2e73ca455a62c31065ce0e6fca4edda8713ec` passed CI #3639 / R2 #3358 GREEN. DLB-6 is COMPLETE / ACCEPTED; DLB-7 final acceptance is NEXT.

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
- Long-horizon convergence target: `docs/CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md`. Setup Generation, Game Engine and Storyteller Recommendation Engine remain separately evolvable behind typed contracts; this is a guardrail, not a new broad refactor campaign.
- Trouble Brewing first converges through `TroubleBrewingGameSnapshotV1`, an immutable projection over canonical setup/session/history owners. It is the standard read boundary for Game/rules consumers, recommendation context builders and EvidenceLab interoperability; it is not a second mutable authority.
- The TB snapshot must distinguish `KNOWN(value)`, `UNCOMMITTED`, `UNKNOWN`, and `NOT_APPLICABLE`; EvidenceLab provenance/verification does not belong inside Host game state.
- Setup generation should ultimately expose setup composition plus explicit Drunk presence without choosing the seated Drunk; seating/final actual-vs-shown commit remains downstream.
- Storyteller recommendation remains read-only over already-legal candidates and typed decision-point context. Required mechanical/history context must be distinguished from optional enrichment such as player experience, recent role history or public/evil claims.
- Product-owner calibration from the Imp podcast semantic review is recorded in `docs/IMP_PODCAST_PRODUCT_POLICY_CALIBRATION_2026-10-02.md`. It is a revisable policy target only: it does not convert machine findings into VERIFIED evidence, does not satisfy the C3 cutover evidence gate, and does not mutate `BEGINNER_CONSERVATIVE_V1`.
- `ClocktowerGameSession` / its owned aggregate remains canonical truth; `PlayerCard`, UI state, replay and recommendation contexts must converge toward derived projections rather than parallel authorities.
- DecisionTrace/replay/export are read-only diagnostic/calibration projections.
- Red Herring legality/commit ownership is not moved into SDE policy.
- Drunk/Poisoned information may be true or false; impaired narrative uses the accepted shared perceived-functioning projection.
- Spy/Recluse registration remains interaction-scoped.
- Demon bluffs remain a joint SDE output until committed, but DLB stages their commitment at the latest safe presentation dependency rather than assuming one immutable setup-time bundle.
- Drunk seat assignment is now late-bound after shown identities are seated; rules/setup own its legal candidate domain, SDE may evaluate legal alternatives, and canonical setup/session owns the finalized commit.
- Red Herring commitment follows a generic observation/dependency barrier; do not encode a named `if (Spy)` policy shortcut.
- Player-controlled choices such as Poisoner target remain player-owned and may invalidate only still-uncommitted downstream plans.
- No fixture-specific or named-player policy branches.
- Traveller evidence remains outside the current mainline algorithm unless explicitly brought into scope.

## 7. Evidence track

ClocktowerEvidenceLab is the current policy-evidence lane. RH-E is already complete and no longer a gate.

Evidence stages remain:

- E1 — architecture/lifecycle evidence;
- E2 — semantic regression evidence;
- E3 — qualitative policy evidence strong enough to justify a typed preference/reason;
- E4 — quantitative calibration evidence when a policy genuinely requires numeric strength.

Observed expert choices without adequate rationale are not automatically policy labels. Complete real games and expert Storyteller rationale remain preferred over synthetic clean-corpus calibration.

C5 remains blocked until a genuinely qualifying E3/E4 predicate exists.

## 8. Immediate execution order

~~~text
query live main / workspace
-> DLB document authority convergence (2026-09-29 route)
-> DLB-0 typed intermediate setup COMPLETE
-> DLB-1 visible-roster deal cutover COMPLETE
-> DLB-2 legal Drunk candidate domain + hypothetical projector COMPLETE
-> DLB-3A shadow envelope + DecisionTrace/replay COMPLETE
-> DLB-3B setup-level consequence feature bridge COMPLETE / ACCEPTED
-> DLB-4 canonical Drunk commit before reveal COMPLETE / ACCEPTED
-> DLB-4A Experienced assisted selection UX COMPLETE / ACCEPTED
-> DLB-5.1 typed dependency planner COMPLETE / GREEN
-> DLB-5.2 Red Herring production cutover COMPLETE / GREEN
-> DLB-5.3 Demon bluff production cutover COMPLETE / GREEN
-> DLB-5.4 first-night information / poison convergence audit COMPLETE / NO PRODUCTION GAP
-> DLB-5.5 obsolete setup auto-apply cleanup COMPLETE / GREEN
-> DLB-5 final acceptance + PR #183 merge COMPLETE / ACCEPTED
-> DLB-5H1 narrow presentation extraction COMPLETE / ACCEPTED; executable checkpoint `47136b7d03d72452b70bf3defa847578b30fb011`, CI #3609 / R2 #3337 GREEN
-> Drunk-assignment production cutover gate audit COMPLETE / NOT PASSED
-> Drunk recommendation-context capability contract COMPLETE / POLICY-NEUTRAL
-> TBGS-0 canonical TB snapshot contract COMPLETE / ACCEPTED; executable checkpoint `f367c0d3ec23ebf452c924ff7c0921cd978a800f`, CI #3618 / R2 #3343 GREEN
-> TBGS-1A Host snapshot-backed Drunk decision context/shadow COMPLETE / ACCEPTED; `ae4dc2400325d233da033d3c86d2863bde1bd485`, CI #3621 / R2 #3345 GREEN
-> TBGS-1B EvidenceLab G10 historical materializer + cross-project semantic equivalence COMPLETE; observed EvidenceLab checkpoint `970e7e6430f7088ac004cd1e5696759da4d52003`, byte-for-byte Host fixture match
-> TBGS-1 overall COMPLETE / ACCEPTED
-> post-TBGS-1 cutover recheck COMPLETE / historical NOT-PASSED verdict
-> EvidenceLab C3-Q04 VERIFIED / Stage-1 accepted at `08d95a0c258f687187c0476a0f430fa5ff8229cb`
-> Q04 re-entry audit COMPLETE / evidence gate PASS
-> `DRUNK_ASSIGNMENT_Q04_V1` bounded Empath -> Monk override COMPLETE / ACCEPTED
-> dedicated replay COMPLETE / ACCEPTED; `5cf72a54a62c87763279c02014485a847724b72e`, CI #3631 / R2 #3351 GREEN
-> Beginner production wiring COMPLETE; `0ef3760ee233b0e20fa0dd272b12380abe8ee4a4`
-> Q04 production cutover T4 COMPLETE / ACCEPTED; `9762d5a759bf0eaa81a1f6cb5af1aa28281d3ac2`, CI #3633 / R2 #3353 GREEN
-> Beginner automatic Drunk authority COMPLETE / ACCEPTED
-> DLB-6 old-contract retirement COMPLETE / ACCEPTED; `52c2e73ca455a62c31065ce0e6fca4edda8713ec`, CI #3639 / R2 #3358 GREEN
-> DLB-7 acceptance NEXT
-> TBGS-2 incremental runtime recommendation-state migration / A3 re-audit after DLB-7
~~~

A1/A2 may proceed as separate maintenance PRs without blocking DLB. C5/V2 remains a separate evidence-gated lane.

## 9. Current authorities

Read first:

1. `AGENTS.md`
2. `docs/AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md`
3. `docs/TESTING_STRATEGY.md`
4. this roadmap
5. `docs/NEXT_DEVELOPMENT_HANDOFF.md`
6. `docs/DLB_AND_APP_HOST_DECOMPOSITION_IMPLEMENTATION_ROUTE_2026-09-29.md` — current DLB/decomposition implementation authority
7. `docs/DLB_DRUNK_ASSIGNMENT_PRODUCTION_CUTOVER_GATE_AUDIT_2026-09-30.md` — current Drunk-assignment cutover verdict / blocker authority
8. `docs/DLB_DRUNK_RECOMMENDATION_CONTEXT_CAPABILITY_CONTRACT_2026-09-30.md` — current policy-neutral required/enrichment/unavailable context boundary and EvidenceLab handoff contract
9. `docs/TB_CANONICAL_GAME_SNAPSHOT_INTEGRATION_ROUTE_2026-09-30.md` — current TB-only canonical snapshot / Drunk vertical slice / EvidenceLab interoperability route
10. `docs/CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md` — long-horizon core-engine/state boundary target; consult when a slice changes setup, recommendation context, canonical-state ownership or projection boundaries
11. `docs/DLB_5_STAGED_FIRST_NIGHT_DEPENDENCY_PLANNER_AUDIT_2026-09-29.md` — completed DLB-5 closeout record; read only when that history is needed

The 2026-09-28 source audits, completed Recovery/C4 audits, SDE freeze/cutover audits, and older SDE checkpoints are historical or specialized evidence. Read them only when the current slice raises a concrete ownership/evidence question.

## 10. Testing cadence

Follow `docs/TESTING_STRATEGY.md`.

RH-E completed at T4 with exact-tree CI #3478 and R2 #3231 GREEN, including Android FULL + assemble, ASP contracts and Real Clingo cross-validation.

The #157 live-main integration head `371ebf624898c08747203aceaed1254647867214` also completed exact-head acceptance with CI #3479 and R2 #3232 GREEN. This integration changed workflow/documentation only relative to the previously accepted continuation head.

GitHub CI/R2 remains the normal Android execution and independent remote acceptance surface. The N3150 execution-host experiment remains retired from the default workflow unless the user explicitly reopens it.

A documentation-only compaction does not require Android regression by itself.

