# CampBoardGameHost — Next Development Handoff

> Updated: 2026-09-30 Australia/Sydney  
> Current baseline: live `main` — query exact HEAD/PR state at session start  
> Last completed validation checkpoint: **DLB-5 final exact-head T4 — `3fd1714ce2e18b969e5039bcf6a58f8775745b9d`; CI #3603 / R2 #3334 GREEN; Android `testFull + assembleDebug`, ASP and Real Clingo all GREEN**  
> Merged checkpoint: **PR #183 — `DLB-5: stage first-night dependency barriers` — squash merge `6293a3bb94778338db61f5a1708a1d283d6e8b6f`**  
> Product next gate: **DLB-5H1 narrow first-night evil-information presentation boundary audit; extract presentation only if the seam remains cohesive. Beginner automatic Drunk-selection authority remains blocked behind the later cutover gate**

This file is deliberately compact. Completed checkpoint detail belongs in linked completion/audit/archive documents rather than being copied forward indefinitely.

## 1. Read first

1. `AGENTS.md`
2. `docs/AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md`
3. `docs/TESTING_STRATEGY.md`
4. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
5. this handoff
6. `docs/DLB_AND_APP_HOST_DECOMPOSITION_IMPLEMENTATION_ROUTE_2026-09-29.md` — current DLB implementation authority
7. `docs/CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md` — long-horizon Setup / Game Engine / Storyteller Recommendation / canonical-session target; use as an ownership guardrail, not as permission to broaden the current slice
8. `docs/DLB_5_STAGED_FIRST_NIGHT_DEPENDENCY_PLANNER_AUDIT_2026-09-29.md` — current DLB-5 closeout authority

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
C5 / V2                                BLOCKED ON E3/E4
SDE-3E cutover                         BLOCKED PER SURFACE
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

The current product-development lane is **Drunk late-binding and staged first-night sequencing**.

Authority:

`docs/DLB_AND_APP_HOST_DECOMPOSITION_IMPLEMENTATION_ROUTE_2026-09-29.md`

Architecture convergence guardrail: `docs/CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md` now records the intended separation of Setup Generation, canonical Game Session/Game Engine, and read-only Storyteller Recommendation. The immediate DLB-5H1 task remains narrow and unchanged. Do not turn H1 into a canonical-state rewrite; apply the target architecture when DLB-6 retires stale contracts and when H2 is re-audited.

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
-> DLB-5H1 narrow presentation-extraction boundary audit NEXT
-> cutover gate before Beginner automatic authority
-> DLB-6 old-contract retirement
-> DLB-7 acceptance
```

The App/Host decomposition audit is a constraint on this work, not a prerequisite campaign:

- A1 preferences and A2 archive storage are independent maintenance slices only;
- H1 waits for DLB-5 and may then extract presentation if the seam is genuinely narrow;
- H2 and A3 remain deferred for post-DLB re-audit;
- R3 generic transaction extraction and the generic setup-effect owner remain NO-GO.

Recovery R0–R6 stays complete. R7 remains separate.

EvidenceLab C1C Drunk-assignment acquisition is complete at 3 / 3 replayable cases and C1D G10 downstream replay is accepted. DLB-3 is complete through DLB-3B3; DLB-4 and DLB-4A are accepted. DLB-5 has reached the GREEN implementation checkpoint `bdc31a31bc4652be13e31d7d4c8c6bacd68b9dd5` for 5.1/5.2/5.3/5.5; 5.4 convergence audit remains next before final DLB-5 acceptance. Missing longitudinal capability and ordering evidence remain explicit cutover-gate blockers for Beginner automatic authority. The existing C5/V2 E3/E4 gate remains separate and still blocks `BEGINNER_CONSERVATIVE_V2`.

The frozen `BEGINNER_CONSERVATIVE_V1` must not silently absorb Drunk candidate preference/rejection semantics. DLB-3 is shadow-first; Beginner automatic Drunk authority requires the explicit cutover gate in the DLB route.

## 8. E3/E4 qualification result — no policy delta authorized

The 2026-09-27 audit checked the current EvidenceLab TB corpus, targeted expert rationale, Red-Herring C4 evidence classification, and the SDE evidence-gap contract.

Result:

- healthy-information floor: E3 FAIL / E4 FAIL;
- impaired-information believability: E3 FAIL for a new preference / E4 FAIL;
- confirmation-chain importance: E1/E2 support only for current purpose;
- role-function exposure severity: E3 FAIL;
- truth danger / contextual Red Herring: E1/E2 strong, E3 FAIL for candidate ordering;
- Demon-bluff triplet ordering: E3 FAIL;
- multi-axis weights: E4 FAIL.

Therefore there is **no automatic C5 production slice** and no placeholder V2.

Next acquisition must be narrowly targeted to one of:

1. a reconstructable expert beginner/mixed TB game with explicit healthy-information balance rationale and recoverable legal alternatives;
2. a non-Ben expert Drunk/poison case with explicit believable/coherent choice-over-alternatives rationale;
3. a functioning Librarian/Investigator + Spy/Recluse case with healthy alternatives and explicit exposure rationale;
4. an expert Demon-bluff triplet choice explicitly compared with another legal set.

Stop treating a source as an E3 candidate once qualified Storyteller identity, committed state, observed choice, recoverable alternatives, or explicit rationale is missing.

Detailed audit: `docs/SDE_E3_E4_QUALIFICATION_AUDIT_2026-09-27.md`.

## 9. Scope still blocked

Do not start `BEGINNER_CONSERVATIVE_V2`, add new policy weights/thresholds, broaden player-count/Traveller scope, or perform production cutover without the required E3/E4 evidence. C5 and SDE-3E remain blocked as recorded in the roadmap.
