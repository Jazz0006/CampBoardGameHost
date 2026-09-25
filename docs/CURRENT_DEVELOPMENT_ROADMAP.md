# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-25 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Program status

~~~text
D6 decomposition / ownership cleanup                  COMPLETE
EPI-MQ capability boundary                            COMPLETE / PR #135
Exact historical hypothetical bundle seam             COMPLETE / PR #137
First-night experiment contract                       COMPLETE / PR #138
FN-BUNDLE-0 / 1 / 2                                  COMPLETE / PR #139/#140/#142
SDE-0 BEGINNER strategic corpus                       COMPLETE / PR #143
SDE-1 orchestration / lifecycle / shadow              COMPLETE / PR #144
SDE-2D1 Drunk whole-bundle                            COMPLETE / PR #145
SDE-2D2 Demon bluff joint-output                      COMPLETE / PR #146
SDE-2D3 strategic-world quotient                      COMPLETE / PR #147
SDE-2D4 5–15 correctness/performance                  COMPLETE / PR #149
SDE-2D5 calibration / policy evidence                 CHECKPOINT MERGED / PARALLEL EVIDENCE
    D5A–D5E                                            COMPLETE
    D5F infrastructure                                COMPLETE
    D5F-B3 correction                                 HISTORICAL CHECKPOINT
    D5F-B4 engineering / evidence checkpoint          COMPLETE UP TO EXTERNAL WAIT
        B4A clean-corpus retirement                    COMPLETE
        B4B GOLD source discovery                     DEMAND-DRIVEN
        B4C expert reconstruction                     COMPLETE FOR CURRENT ANCHORS
        B4D legal counterfactual recovery              COMPLETE FOR CURRENT EXECUTABLE ANCHORS
        B4E observed-vs-alternative analysis           CROSS-EXPERT CHECKPOINT COMPLETE
        B4F-A executable SILVER replay                 COMPLETE
        B4F-B documentary SILVER comparison            COMPLETE
        B4F-C target specification                     COMPLETE
        B4F-C targeted evidence acquisition            EXTERNAL / CONTINUOUS
D5F-C final gate/band derivation                      BLOCKED ON EVIDENCE
sealed holdout                                        CLOSED
SDE-3A engine / feature / policy contract             COMPLETE / PR #151/#152
SDE-3B BEGINNER_CONSERVATIVE_V1                       COMPLETE / T4 ACCEPTED / PR #153 MERGED
SDE-3C shadow / DecisionTrace / replay                 COMPLETE / 3C0–3C5
SDE-3D calibrated policy freeze                       IN PROGRESS / 3D0–3D1 COMPLETE / PARTIALLY EVIDENCE-BLOCKED
SDE-3E automatic production cutover                   BLOCKED PER SURFACE ON 3D GATES
~~~

## 2. Current branch / PR

Local continuation: `codex/sde-history-prefix-route-closure`; C0–C3 code checkpoint `8855d4615a4c24a4c3141241f360cfe70d8d239e` is pushed both to this remote branch and the Draft PR #154 head `sde-3c-decision-trace-shadow-replay`. This is not `main`. The audited `main` remains `cc5adee5`; the development/main merge conflict in `AGENTS.md` remains a separate, unauthorized integration action.

### Current priority: integration closure before 3D2

Follow [SDE integration closure contract](SDE_INTEGRATION_CLOSURE_ROUTE_2026-09-25.md): C0 correlation/route closure → C1 durable replay input → C2 offline vertical slice → C3 measured runtime shadow (**C0–C3 complete at `8855d461`**) → C4 resume 3D2 (**next**) → C5 evidence-backed policy/cutover.

| Dimension | Current assessment |
| --- | --- |
| Typed contracts / modules | 3C0–3C5 and 3D0–3D1 complete; preserve those checkpoints |
| Runtime reachability | Debug-only Trouble Brewing 5-player structured-number shadow is connected with cancellation, identity/budget gates and failure isolation; visible/canonical authority is unchanged |
| Durable replay loop | C1 strict export/restore plus C2 real historical decision → trace → canonical choice → reload → replay loop complete locally |
| Acceptance | Local FAST/debug build GREEN; exact code HEAD `8855d461` full CI #3451 SUCCESS. PR/main conflict prevented automatic PR checks and R2; R2 plus conflict resolution remain separate merge gates |

Use local filesystem/Git/Gradle for this explicitly authorized Codex continuation. Mini MCP / Oracle VM are not prerequisites.

### C0 local verification — 2026-09-25

C0 code/document implementation is complete at the remotely synchronized C0–C3 checkpoint `8855d461`. C4/3D2 is now next.

- Typed RED: `:app:testFast --tests '*DecisionTraceAuthoritativeChoiceCorrelationTest'` executed 8 tests; the 2 new regressions failed on the original correlator (missing/invalid history accepted).
- Focused GREEN: correlation, archive/persistence, multi-policy replay and immutable V1 definition tests passed after the common-owner fix.
- Checkpoint: `./gradlew :app:testFast :app:assembleDebug --no-daemon` succeeded locally in 2m57s; FAST actually executed **1518 tests / 349 suites, 0 failures/errors/skipped**, and debug APK assembly succeeded. Unchanged compilation dependencies reused Gradle cache/up-to-date outputs.
- `git diff --check` passed; local Markdown links in the 10 changed/new documentation files resolved.
- No schema, V1 policy, rules/solver, canonical commit or App wiring changes. No tests retired. The store is the sole production correlator caller and inherits the fix.
- The later combined C0–C3 exact-head full CI #3451 supplied Android full/debug APK, ASP and Real Clingo evidence. R2 remains pending because the existing PR/main conflict prevented automatic PR checks; no rebase, ready transition or merge was performed.

### C1 local implementation — 2026-09-25

- Added the versioned immutable `SdeHistoricalReplayInput`, fresh/durable materialization provenance, strict JSON codec and reconstruction of committed setup plus baseline `GameSnapshot`.
- The contract carries exact ruleset identity, setup provenance/assignments, seed, canonical player-seat names, revisions, action timeline, observations and global cursor. It reuses existing timeline/observation codecs and owners.
- Typed RED first proved the seam absent; a second RED proved caller-owned lists could mutate the first implementation. Both are GREEN after implementation and defensive copying.
- Focused C1 + production-shadow + multi-policy-replay tests pass.
- Checkpoint: `./gradlew :app:testFast :app:assembleDebug --no-daemon` succeeded locally in 2m57s; FAST actually executed **1521 tests / 350 suites, 0 failures/errors/skipped**, and debug APK assembly succeeded. Unchanged tasks reused Gradle up-to-date outputs.
- `git diff --check` passed and all local links in the 10 changed/new Markdown documents resolve.
- C1 itself added no App wiring, recovery-schema migration, UI, policy or gameplay mutation. C2/C3 subsequently consumed this contract through their bounded offline/runtime seams.

### C2/C3 local implementation — 2026-09-25

- C2 proves a restored real Empath Night-2 decision across canonical history, exact historical features, pending trace, manual canonical choice, archive reload and deterministic V1 replay.
- C3 wires the same typed context through a Debug-only diagnostic shadow. Production admission is intentionally limited to Trouble Brewing, exactly 5 players, at most 16 history entries and 1500 ms. Stale/cancelled/over-budget/ineligible/I/O paths do not publish a trace or block canonical commit; missing exact capability remains an explicit stored deferral.
- Forced local samples: 5 players/history 0 = 62–68 ms and 0-byte coarse heap delta; 5 players/history 3 = 235–563 ms and 34,786,984–69,759,576-byte coarse heap delta. The 6/8/12/15 probes are fast admission rejection only (319–371/5–7/2–3/2 µs); broader actual-chain support is not claimed.
- App fanout is two narrow callbacks: prepared structured-number decision and confirmed structured decision. Legality/confirmation, session commit, historical evaluation and archive ownership remain in their existing typed owners.
- Focused C1–C3/correlation tests and production compilation are GREEN. Final `:app:testFast :app:assembleDebug` succeeded in 2m52s: **1528 tests / 352 suites, 0 failures or skips**, plus successful debug APK assembly. `git diff --check` is GREEN.
- Exact code HEAD `8855d461` was pushed to Draft PR #154 and accepted by manually dispatched full CI #3451 / run `36126033315`: Android full JVM + debug APK, ASP contract, Real Clingo cross-validation and final CI gate all succeeded. The PR remains `DIRTY` against `main`; automatic PR checks/R2 did not run, so R2 and conflict resolution remain explicit merge gates rather than implied successes.
- **Next local slice: C4 / SDE-3D2 architecture, evidence and fanout audit before feature production edits.** V1 stays immutable and no placeholder V2 is authorized.

Upstream development branch: `sde-3c-decision-trace-shadow-replay` (local continuation branch above).

Latest integration code checkpoint: `8855d4615a4c24a4c3141241f360cfe70d8d239e` — GitHub full CI #3451 SUCCESS; R2 pending for the conflicted PR. Earlier accepted checkpoints remain SDE-3D1 `f562887c` (CI #3446 / R2 #3201) and SDE-3C5 `db7d5575`. Documentation-only synchronization commits may advance the branch; always query the live branch/PR head.

Draft PR #154 — `SDE-3C: add DecisionTrace shadow replay` — remains the active SDE-3C/3D continuation PR. Query its exact live head/checks before executable work. Keep it **draft** and do not merge unless the user explicitly says **“授权合并”**.

PR #153 is merged into `main`; merged `main` entry SHA is `7045ae746fd11a26371127c584c91e2a183c4c75`.

Always query live refs before executable edits.

## 3. Current authorities

Read these first for current execution:

- Integration implementation / acceptance: [`SDE_INTEGRATION_CLOSURE_ROUTE_2026-09-25.md`](SDE_INTEGRATION_CLOSURE_ROUTE_2026-09-25.md)
- Current state / priority: [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md)
- Current handoff: [`NEXT_DEVELOPMENT_HANDOFF.md`](NEXT_DEVELOPMENT_HANDOFF.md)
- Current SDE-3 route: [`SDE_3_PROVISIONAL_POLICY_AND_CONTINUOUS_CALIBRATION_ROUTE_2026-09-23.md`](SDE_3_PROVISIONAL_POLICY_AND_CONTINUOUS_CALIBRATION_ROUTE_2026-09-23.md)
- SDE-3D freeze/cutover gate: [`SDE_3D0_CALIBRATED_POLICY_FREEZE_CUTOVER_GATE_ARCHITECTURE_AUDIT_2026-09-24.md`](SDE_3D0_CALIBRATED_POLICY_FREEZE_CUTOVER_GATE_ARCHITECTURE_AUDIT_2026-09-24.md)
- Frozen V1 release definition: [`SDE_3D1_V1_IMMUTABLE_BASELINE_FREEZE_COMPLETION_AUDIT_2026-09-24.md`](SDE_3D1_V1_IMMUTABLE_BASELINE_FREEZE_COMPLETION_AUDIT_2026-09-24.md)
- First-night policy/evidence synthesis: [`SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md`](SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md)
- Targeted evidence gaps: [`SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md`](SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md)
- Global architecture: [`STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`](STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md)

Read historical slice audits only when a specific ownership/semantic question requires them. In particular, SDE-3A/B/C completion documents are no longer mandatory startup context and their embedded historical PR/"next" state is not current authority.

For this local continuation, inspect files and Git directly. The separate Mini MCP workflow applies only when that environment is selected; memory is advisory, never current-state authority.

Evidence/provenance references on demand:

- [`SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv`](SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv)
- [`SDE_2D5F_B4F_SILVER_GENERALIZATION_AUDIT_2026-09-23.md`](SDE_2D5F_B4F_SILVER_GENERALIZATION_AUDIT_2026-09-23.md)
- primary reconstructions for Ben / A Stud, Evin 2019 and A Fond Farewell;
- Traveller boundary audit only when Traveller-containing evidence is relevant.

## 4. Frozen architecture / policy decisions

- Optimize only variables still controllable at the current lifecycle stage.
- Rules/canonical producers own legality; epistemic/topology layers own consequences; SDE owns policy/selection.
- Drunk shown identity is setup-persistent; committed information is immutable.
- Demon bluffs and Red Herring are persistent once revealed/committed.
- Fortune Teller target pair and Poisoner target are player-controlled.
- Spy/Recluse registration is per interaction.
- BEGINNER thematic prior: Spy normally registers as Good/Townsfolk/Outsider; Recluse normally registers as Evil/Minion/Demon.
- Thematic registration is a design hypothesis, not an extra frozen V1 ordering rule; a future evidence-backed override requires material whole-bundle benefit over the default.
- Role-function exposure is a generic contextual diagnostic, not a frozen named-role preference; current evidence does not justify Librarian/Recluse or Investigator/Spy severity.
- Chef/Empath are rule-determined only when every legal registration branch yields the same healthy value.
- Drunk/poisoned information may accidentally be true; repeated/history-dependent impaired information must use one role-agnostic **derived narrative projection over canonical history** so later outputs can be checked for coherence without introducing a second mutable narrative state.
- Strategic Evil topology is primary structural evidence, but role-information utility, confirmation chains, role-function exposure, bluff usability and information floor remain separate.
- No opaque global scalar.
- No named-role / known-fixture policy patches for cross-interaction coherence. Role-specific code owns legality only; shared semantic/history policy owns narrative continuity.

### Shared impaired-narrative implementation invariant

When D5F/SDE-3 reaches cross-night impaired information, implement one reusable **derived narrative projector** over generic information propositions and canonical history.

Do not create separate coherence algorithms for individual roles or calibration fixtures.

Acceptance for that future implementation must show:

- `ActionFactTimeline + EpistemicObservationLog` remain the only durable history owners; the projector owns only derived features;
- role adapters contribute legal domains / semantic propositions only;
- previous committed observations constrain later selection;
- the mechanism generalizes across multiple information shapes;
- tests prove the abstraction at its shared owner plus representative fanout, rather than hard-coding every named example.

## 5. Calibration correction

The previous D5F route over-relied on synthetic/isolated fixtures and one-person human labels.

Those paths are no longer active.

- The pathological 7-player fixture is historical diagnostic evidence only.
- The later seven “clean” 7–9 scenarios deliberately excluded Drunk and froze registration-dependent context; do not add diagnostics or labels to them.
- Their test-only implementation should be deleted if canonical generator/legality tests already protect the durable contracts.
- Old D5 correction/design documents have been removed from active docs; Git history preserves traceability.
- Existing human-label manifests are historical/compatibility artifacts, not current policy truth.

## 6. D5F-B4 — expert-observed policy calibration

Evidence hierarchy:

~~~text
GOLD
    verified experienced/trusted Storyteller
    reconstructable real game
    explicit rationale preferred

SILVER
    high-fidelity structured real-game record
    Storyteller expertise not independently verified

QUALITATIVE
    tutorials / postmortems / repeated experienced-community guidance

DIAGNOSTIC_ONLY
    synthetic / extreme / counterfactual fixtures
~~~

Current external catalog now contains SILVER / QUALITATIVE seed evidence plus five tracked expert-source cases. **Two primary-verified GOLD decision slices now exist from two independent Storytellers**: A Stud In Scarlet under `st-ben-burns`, and Evin's 2019 first full playthrough under `st-evin`. Evin's postgame primary image verifies Demon bluffs Recluse / Slayer / Soldier, and the postgame review gives explicit Red-Herring rationale: Doug/Chef was selected because Chef information was especially damaging to Evil and Red-Herring contamination could undermine its credibility. The other two executable Ben reconstructions still require primary-video verification. `A Fond Farewell` has primary-verified material Night-1 state and explicit choice-specific rationale, but its executable legal-counterfactual gate is blocked because the live table contains five Travellers and the current production rules/domain model has no Traveller surface.

Primary-source extraction on 2026-09-22 also completed `A Stud In Scarlet` verification from its primary video. The canonical provenance record is [`SDE_2D5F_A_STUD_IN_SCARLET_PRIMARY_RECONSTRUCTION_2026-09-22.md`](SDE_2D5F_A_STUD_IN_SCARLET_PRIMARY_RECONSTRUCTION_2026-09-22.md). The video directly verifies the player-visible outputs; hidden Recluse registration acts remain production-derived legal witnesses rather than primary-observed declarations.

Primary-source extraction on 2026-09-22 completed the material `A Fond Farewell` Night-1 reconstruction from the official video, including full radial seating/roles, Traveller alignments, Drunk shown Chef, Demon bluffs, Red Herring, Poisoner target, Washerwoman clue, Fortune Teller targets, and two explicit Ben rationales. The canonical record is [`SDE_2D5F_A_FOND_FAREWELL_PRIMARY_RECONSTRUCTION_2026-09-22.md`](SDE_2D5F_A_FOND_FAREWELL_PRIMARY_RECONSTRUCTION_2026-09-22.md). This is primary verification, not GOLD admission: Traveller-aware production legality is still missing. The official 2019 Evin video has now been directly inspected. The primary reconstruction recovers the complete eight-seat role map plus the material Night-1 WW/Chef/RH/FT slice and is executable through production legality owners. Postgame primary imagery also verifies Demon bluffs Recluse / Slayer / Soldier, and Evin explicitly explains the Red-Herring choice around Chef truth danger / credibility disruption. See [`SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_SOURCE_AUDIT_2026-09-22.md`](SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_SOURCE_AUDIT_2026-09-22.md) and [`SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_PRIMARY_RECONSTRUCTION_2026-09-22.md`](SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_PRIMARY_RECONSTRUCTION_2026-09-22.md).

Interpretation rules:

- chosen A does not imply every unchosen B/C/D was bad;
- explicit rationale/rejection is stronger than silent choice;
- repeated comparable choices and cross-source consistency strengthen evidence;
- final game winner is not a Storyteller-quality label;
- human review is secondary: reconstruction checks, conflict adjudication, and BEGINNER-specific adaptation.

## 7. Immediate execution order

### Evidence track — PARALLEL / EXTERNAL

Current evidence work has reached a stable checkpoint:

- two independent primary-verified GOLD anchors exist: Ben / A Stud and Evin 2019;
- executable expert and SILVER replay harnesses are green;
- Red Herring contextual utility is the strongest cross-expert foundation;
- truth danger / credibility disruption has GOLD + SILVER explicit rationale;
- targeted evidence gaps are explicitly defined;
- broad source collection is no longer the default.

Targeted evidence acquisition continues through ClocktowerEvidenceLab according to:

`SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md`

This track does **not** block SDE-3A/B/C.

### SDE-3A — engine / feature / policy contract — COMPLETE

Authority:

`SDE_3_PROVISIONAL_POLICY_AND_CONTINUOUS_CALIBRATION_ROUTE_2026-09-23.md`

PR #151 established the ownership audit and score-free typed contract. PR #152 completes the structured numeric shadow path from legal candidate -> exact consequence -> `DecisionFeatures`, including explicit deferred-capability handling and Chef/Empath coverage, while visible recommendation and canonical commit authority remain unchanged.

SDE-3A acceptance evidence:

- T4 checkpoint commit: `dd82af3d8da9c17bc62d5606f045ddcc82c21bf0`;
- CI workflow run `35806237753`: full Android unit tests + debug APK, ASP contract tests, and Real Clingo cross-validation all succeeded;
- R2 workflow run `35806237770`: succeeded;
- final fanout audit: production changes remain limited to the SDE feature-evaluation adapter and structured shadow integration; no rules, canonical session commit, UI authority, legacy scoring, policy selection, or persistence ownership moved.

PR #151/#152/#153 are merged. SDE-3C proceeds from merged `main` on `sde-3c-decision-trace-shadow-replay`.

### SDE-3B — BEGINNER_CONSERVATIVE_V1 — COMPLETE / T4 ACCEPTED

Current implementation checkpoint on PR #153:

- decision-level Ready/Deferred policy contract with explicit limitations;
- exact zero Evil-topology contradiction gate only — no numeric strength threshold;
- all otherwise viable candidates remain in an explicit survivor equivalence band;
- deterministic, order-independent seeded survivor selector with no weights;
- policy evaluation attached to Chef/Empath structured shadow without visible cutover;
- source `AbilityState` is preserved from the existing legal candidate owner;
- actual-state semantic truth is projected into `DecisionFeatures` for the current numeric seam;
- **3B2 confirmation-chain features are projected from recipient-visible committed history plus each current candidate using exact leave-one-out diagnostics;**
- **3B3 impaired-narrative features are derived from canonical setup/action/observation history with persistent setup-bound versus temporary action-bound impairment lifetimes;**
- **3B4 healthy-information utility is projected from canonical history, authoritative ability state, upstream legal truth relation and existing confirmation provenance; functioning actual truth and legal registered truth are both healthy, while malfunctioning channels are excluded;**
- baseline-already-infeasible history cannot be blamed on the current candidate as removal of the last healthy route;
- V1 policy still does not consume confirmation-chain, impaired-narrative or healthy-information features for ranking or rejection;
- legacy score/probability/pressure is not imported into SDE policy.

SDE-3B staged status on PR #153:

1. **3B1 historical lifecycle / input binding — COMPLETE** — later structured information can evaluate against the canonical committed prefix; typed contextual input refs preserve external ownership; no-hindsight is enforced; no second history owner exists.
2. **3B2 confirmation-chain projector — COMPLETE** — generic support/contradiction/independent-contribution, ambiguity restoration, source authentication and multi-channel collapse are projected from exact recipient-visible historical replay; R02/R04-style E2 semantic regressions are bounded by available evidence; V1 policy remains unchanged.
3. **3B3 impaired-narrative projector — COMPLETE** — score-free role-agnostic projection distinguishes persistent setup-bound versus temporary action-bound impairment episodes, historical compatibility/breaks, forced versus avoidable transitions and detectability; bounded R04/R06 E2 regressions are green and V1 policy remains unchanged.
4. **3B4 healthy-information utility — COMPLETE** — score-free whole-table healthy routes distinguish usable/independent routes before and after, redundancy/contradiction, lost routes and last-route removal; bounded R01/R04 E2 regressions are green; registered truth is preserved without conflating it with actual-state `SemanticTruth`; V1 policy remains unchanged.
5. **3B5 contextual role-function exposure — COMPLETE** — score-free registration-ambiguity exposure, historical/confirmation context and bounded `goldcand-ben-03` E2 regression are accepted; severity remains E3-gated;
6. **3B6 expert-informed V1 soft priorities — COMPLETE** — current E3 audit authorizes no new soft ordering; Ready policy results explicitly report `preference-evidence-not-authorized`, while viable survivors remain tied and the existing zero-topology gate is unchanged.

Overall SDE-3B implementation and reserved T4 `[full-ci]` acceptance are complete for the current evidence checkpoint. T4 head `30caec3dcd546f4395238809d1f1d285688cd814` passed CI #3427 (full Android JVM + debug APK, ASP contracts, Real Clingo, aggregate CI gate) and R2 #3183. PR #153 has since been merged into `main`.

Expert evidence participates immediately at the architecture/feature-priority level and later as semantic regression. It becomes candidate preference only after the matching feature is stable. Numeric thresholds and multi-axis tradeoff strength remain SDE-3D.

The future conservative policy design direction remains (not additional frozen V1 predicates):

- hard legality/lifecycle boundaries;
- generic catastrophic / near-catastrophic rejection;
- healthy-information preservation;
- contextual Red-Herring utility;
- coherent impaired narrative;
- role-function exposure retained as a diagnostic until qualifying E3 evidence supports a soft contextual reason;
- bluff usability / route diversity;
- seeded randomness among effectively equivalent healthy survivors.

Do not invent unsupported numeric weights.

### SDE-3C — shadow / DecisionTrace / replay — COMPLETE

Current code checkpoint:

1. **3C0 architecture / fanout / persistence audit — COMPLETE** — `StructuredInformationShadowAdapter` is the common aggregation owner; `InformationDecisionContext` remains legal/confirmation authority; canonical timelines remain the only game-history owners; DecisionTrace gets a separate diagnostic owner.
2. **3C1 production shadow recommendation — COMPLETE** — Ready V1 shadow evaluation carries deterministic `PolicySelection`; Deferred policy carries no selection; visible legacy recommendation and canonical commit paths are unchanged. Accepted by GitHub CI/R2 before the SDE-3C3B Oracle checkpoint.
3. **3C2 typed DecisionTrace contract — COMPLETE** — versioned trace records evidence checkpoint, lifecycle/revision, canonical-prefix reference, complete candidate IDs, typed feature evaluation, policy-neutral Ready/Deferred snapshot, shadow recommendation, and pending/committed actual-choice shape. Accepted by GitHub CI/R2 before the SDE-3C3B Oracle checkpoint.
4. **3C3A replay archive ownership — COMPLETE** — immutable archive admits only traces bound to canonical global history prefixes; same decision/revision/policy key is idempotent only for identical trace content and conflicts otherwise. Accepted by GitHub CI/R2 before the SDE-3C3B Oracle checkpoint.
5. **3C3B strict persistence codec / storage integration — COMPLETE** — adds a strict deterministic archive codec, immutable raw-store adapter, and dedicated SharedPreferences transport. Malformed/incompatible payloads and duplicate/conflicting keys fail closed; storage remains diagnostic and is not auto-invoked from the read-only production shadow. Accepted at remote HEAD `9cffa4e94b088342e1808c9945febb790a0b0232` with CI #3433 and R2 #3188 SUCCESS.
6. **3C4 authoritative-choice correlation — COMPLETE** — finalizes only an existing pending trace after exact authoritative confirmation plus canonical post-commit observation/session evidence; identity/revision/domain/lifecycle mismatches, preflight-only records, and conflicting retries fail closed. The only permitted same-key archive transition is identical content except `actualChoice: Pending -> Committed`. Accepted at exact code HEAD `bf5c2c763e33f69ce6a12567be25e5224c10270b` with CI #3436 and R2 #3191 SUCCESS.
7. **3C5 multi-policy replay — COMPLETE** — introduces an explicit policy replay runner/registry, production registry containing only real V1, policy-neutral replay input extracted from freshly recomputed canonical shadow features, exact source-trace identity/prefix/domain correlation, per-policy evidence checkpoints, deterministic requested-version ordering, and preservation of authoritative actual choice without mutating historical truth or auto-persisting replay output. Accepted at exact code HEAD `db7d5575dd28dc5f584be34b3556b511ccaf3e35` with CI #3440 and R2 #3195 SUCCESS.

Validation note: Oracle `test:fast` remains blocked before Kotlin compilation because the ARM64 host has no Android SDK. Independent GitHub validation accepted 3C1/3C2/3C3A at the prior checkpoint, accepted 3C3B at remote HEAD `9cffa4e94b088342e1808c9945febb790a0b0232` with CI #3433 and R2 #3188 SUCCESS, and accepted 3C4 at exact code HEAD `bf5c2c763e33f69ce6a12567be25e5224c10270b` with CI #3436 and R2 #3191 SUCCESS, and accepted 3C5 at exact code HEAD `db7d5575dd28dc5f584be34b3556b511ccaf3e35` with CI #3440 and R2 #3195 SUCCESS.

Before cutover, the durable trace still must capture policy/evidence versioning, the complete candidate/feature/reason record, recommendation, actual committed choice and optional human override rationale. Historical replay must support comparing multiple policy versions against the same committed game history.

### SDE-3D — IN PROGRESS / SDE-3D0–3D1 COMPLETE / SDE-3D2 NEXT

Authority: `SDE_3D0_CALIBRATED_POLICY_FREEZE_CUTOVER_GATE_ARCHITECTURE_AUDIT_2026-09-24.md`.

SDE-3D is no longer treated as one monolithic evidence gate. The accepted V1 semantics are now frozen as an immutable provisional baseline, while individual preference/rejection surfaces remain evidence-gated.

Current unresolved calibration gaps remain:

- healthy-information floor / middle-band policy boundary — highest-value current policy blocker;
- role-function exposure severity;
- independent-expert impaired-information believability / cross-night continuity strength;
- Demon-bluff triplet preference ordering;
- quantitative multi-axis tradeoff only if a future policy explicitly requires numeric weighting.

Gap E does **not** block a qualitative partial-order production policy. Do not invent weights, thresholds or player-count coefficients merely to complete SDE-3D.

Current slices:

1. **3D0 calibrated freeze / cutover gate architecture — COMPLETE**;
2. **3D1 V1 immutable baseline freeze — COMPLETE** — frozen definition binds V1 policy version, `sde-3b-merged-2026-09-24` evidence checkpoint and `SEEDED_HASH_V1`; accepted at `f562887cf4e90d02d364eef5534a1f709922a0f8` with CI #3446 and R2 #3201;
3. **3D2 calibration-ready missing feature completion — NEXT / C1–C3 LOCALLY COMPLETE** — truth-danger / credibility-disruption plus contextual Red-Herring architecture/evidence audit; feature work remains policy-neutral and uses the accepted replay/shadow loop;
4. **3D3 first evidence-authorized policy delta — WAITING FOR QUALIFYING E3**;
5. **3D4 V1/V2 canonical real-corpus replay — REQUIRES REAL V2**;
6. **3D5 surface-scoped calibrated freeze — REQUIRES A CUTOVER-ELIGIBLE SURFACE**.

Do not create a placeholder V2. Any new candidate rejection, preference or survivor-refinement semantic after V1 freeze requires a new explicit policy version.

### SDE-3E — BLOCKED PER SURFACE

Automatic cutover is not one global switch. A decision surface remains blocked until its frozen policy version, consumed features, evidence-authorized predicates, canonical replay, DecisionTrace correlation, manual override and explicit unsupported-surface fallback are all accepted.

A technically available V1 seeded selection is not sufficient for cutover when material dimensions remain unsupported and all non-catastrophic candidates are still one large equivalence class.

No automatic online learning is authorized. Policy improvement remains offline, versioned and evidence-reviewed.

## 8. Historical D5F-C numeric gate route — still evidence-blocked / not the current critical path

Do not derive/freeze gates from old human labels, inspect sealed holdout for tuning, tune on holdout, freeze unsupported numeric policy, or cut automatic production policy.

The old D5F-C goal of deriving final numeric gates/bands remains evidence-blocked. SDE-3D0 supersedes the assumption that this must finish before engineering can progress: qualitative, surface-scoped policy may advance without numeric weights, while any future threshold/band semantics still require the corresponding stronger evidence.

## 9. Compatibility code intentionally retained

Do not remove merely as part of D5F-B4 evidence work:

- impaired-information approximate 90/10 false-family bridge;
- legacy `MalfunctionPolicy`;
- legacy registration/scoring paths still used by production.

Their retirement belongs to later production cutover/cleanup.

## 10. Testing cadence

Follow root `AGENTS.md` and [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md).

Documentation-only cleanup needs exact diff/reference audit, not manufactured runtime tests. Deleting obsolete test-only calibration artifacts requires proving no unique durable contract is lost and then running the smallest affected test tier.
