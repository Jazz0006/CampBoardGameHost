# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-29 Australia/Sydney  
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

The new Trouble Brewing setup contract makes Drunk assignment a post-seat Storyteller/SDE decision rather than a template-owned pre-seat input. DLB-0 established the typed intermediate state; DLB-1 cut production preparation over to visible-roster seating; DLB-2 provides the rules-owned legal Drunk-seat domain and pure hypothetical GameState projection; DLB-3A now carries that domain through the normal SDE SetupPrecommit / DecisionTrace / replay lifecycle while deliberately deferring frozen V1. DLB-3 is now complete through DLB-3B3. The accepted shadow surface carries rules-legal candidates through hypothetical setup/consequence evidence, dedicated Drunk-assignment features, DecisionTrace correlation, and a separate versioned policy-experiment replay lane. `DRUNK_ASSIGNMENT_SHADOW_V1` explicitly defers for missing longitudinal capability and unauthorized ordering evidence; ordinary DecisionFeatures and frozen V1 remain unchanged. No 3B4 is required by the authoritative route. DLB-4 canonical Drunk commit is COMPLETE / ACCEPTED at `5606371c68b97beb01418ede2bb2c19db7e69053` with CI #3580 / R2 #3313 GREEN; DLB-4A Experienced assisted Drunk selection is COMPLETE / ACCEPTED at `c85448831e73c82868e118c7f47a0ed889267c0c` with CI #3586 / R2 #3318 GREEN; DLB-5 staged first-night dependency planning is next. Automatic Beginner Drunk-selection authority remains blocked behind the later shadow/evidence cutover gate; the current Beginner compatibility-confirmed candidate is transitional only. The full engineering sequence remains DLB-0 -> DLB-7, with a shadow-to-production cutover gate before Beginner automatic Drunk authority.

The 2026-09-28 App/Host decomposition audit is a guardrail for this work, not a prerequisite campaign:

- A1 preferences storage and A2 archive storage remain independent maintenance slices;
- H1 first-night evil-information presentation waits for DLB-5's dependency lifecycle;
- H2 and A3 are re-audited after DLB boundaries stabilize;
- R3 generic transaction extraction and generic setup-effect ownership remain NO-GO.

Targeted EvidenceLab C1C Drunk-assignment acquisition is complete at 3 / 3 replayable cases, and C1D downstream replay is accepted. EvidenceLab now serves two distinct lanes:

1. DLB Drunk-assignment evidence/trace replay and consequence-contract calibration for the new decision surface;
2. the existing C5/V2 E3/E4 policy gate.

Do not conflate those gates. C5 / BEGINNER_CONSERVATIVE_V2 remains blocked. DLB may implement legality, projection, canonical commit, Experienced assisted UX, staged dependencies and shadow trace before any new evidence-backed production preference is authorized.

The frozen `BEGINNER_CONSERVATIVE_V1` must not silently absorb Drunk candidate ordering/rejection semantics. Any production Drunk-selection policy delta requires an explicitly versioned decision-surface contract.

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
-> DLB-5 dependency-barrier first-night planner NEXT
-> DLB-5H1 presentation extraction only if the boundary remains cohesive
-> shadow/evidence cutover gate
-> Beginner automatic Drunk authority
-> DLB-6 old-contract retirement
-> DLB-7 acceptance
-> re-audit H2 / A3
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
7. `docs/DRUNK_LATE_BINDING_AND_FIRST_NIGHT_DECISION_SEQUENCE_AUDIT_2026-09-28.md` — DLB architecture evidence
8. `docs/APP_HOST_DECOMPOSITION_PLAN_AUDIT_2026-09-28.md` — bounded decomposition evidence/guardrails
9. `docs/SDE_E3_E4_QUALIFICATION_AUDIT_2026-09-27.md` — C5/V2 evidence qualification result / targeted acquisition gaps
10. `docs/CURRENT_ONLY_RECOVERY_MINIMAL_STATE_AUDIT_2026-09-28.md` — completed R0–R6 current-only Recovery cleanup contract / acceptance record
11. `docs/SDE_3D2_TRUTH_CREDIBILITY_RED_HERRING_ARCHITECTURE_AUDIT_2026-09-26.md` — completed C4 architecture/evidence boundary
12. `docs/SDE_3D0_CALIBRATED_POLICY_FREEZE_CUTOVER_GATE_ARCHITECTURE_AUDIT_2026-09-24.md` — freeze/cutover gates

Use older SDE-3A/B/C/C0–C3 completion documents only when a specific historical or ownership question requires them.

## 10. Testing cadence

Follow `docs/TESTING_STRATEGY.md`.

RH-E completed at T4 with exact-tree CI #3478 and R2 #3231 GREEN, including Android FULL + assemble, ASP contracts and Real Clingo cross-validation.

The #157 live-main integration head `371ebf624898c08747203aceaed1254647867214` also completed exact-head acceptance with CI #3479 and R2 #3232 GREEN. This integration changed workflow/documentation only relative to the previously accepted continuation head.

GitHub CI/R2 remains the normal Android execution and independent remote acceptance surface. The N3150 execution-host experiment remains retired from the default workflow unless the user explicitly reopens it.

A documentation-only compaction does not require Android regression by itself.

