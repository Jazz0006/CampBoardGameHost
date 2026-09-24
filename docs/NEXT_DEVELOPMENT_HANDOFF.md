# CampBoardGameHost — Next Development Handoff

> Updated: 2026-09-24 Australia/Sydney  
> Branch: `sde-3c-decision-trace-shadow-replay`  
> Latest remotely accepted SDE-3C HEAD: `9cffa4e94b088342e1808c9945febb790a0b0232`  
> Draft PR: **#154 — `SDE-3C: add DecisionTrace shadow replay`**. At that exact HEAD, GitHub CI #3433 and R2 #3188 were SUCCESS; the PR was open, draft, and mergeable. Keep the PR **draft**; do not mark ready or merge unless the user explicitly says **“授权合并”**.

## 1. Read first

Use these as the active authorities, in order:

1. root `AGENTS.md`
2. `docs/TESTING_STRATEGY.md`
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
4. `docs/SDE_3_PROVISIONAL_POLICY_AND_CONTINUOUS_CALIBRATION_ROUTE_2026-09-23.md`
5. `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`
6. `docs/SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md`
7. `docs/SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md`
8. `docs/SDE_3B_STRUCTURE_AND_EXPERT_EVIDENCE_STAGING_2026-09-23.md`
9. `docs/SDE_3B2_CONFIRMATION_CHAIN_COMPLETION_AUDIT_2026-09-23.md`
10. `docs/SDE_3B3_IMPAIRED_NARRATIVE_COMPLETION_AUDIT_2026-09-23.md`
11. `docs/SDE_3B4_HEALTHY_INFORMATION_UTILITY_COMPLETION_AUDIT_2026-09-23.md`
12. `docs/SDE_3B5_ROLE_FUNCTION_EXPOSURE_COMPLETION_AUDIT_2026-09-24.md`
13. `docs/SDE_3B6_EXPERT_INFORMED_V1_SOFT_PRIORITY_ELIGIBILITY_AUDIT_2026-09-24.md`
14. `docs/SDE_3B6_EXPERT_INFORMED_V1_SOFT_PRIORITY_COMPLETION_AUDIT_2026-09-24.md`
15. `docs/SDE_3B_BEGINNER_CONSERVATIVE_V1_COMPLETION_AUDIT_2026-09-24.md`
16. `docs/SDE_3C0_DECISION_TRACE_SHADOW_REPLAY_ARCHITECTURE_AUDIT_2026-09-24.md`
17. `docs/SDE_3C3B_PERSISTENCE_CODEC_STORAGE_ARCHITECTURE_AUDIT_2026-09-24.md`
18. `docs/SDE_3C3B_STRICT_PERSISTENCE_CODEC_STORAGE_COMPLETION_AUDIT_2026-09-24.md`
19. this handoff

Evidence/provenance references when needed:

- `docs/SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv`
- `docs/SDE_2D5F_A_STUD_IN_SCARLET_PRIMARY_RECONSTRUCTION_2026-09-22.md`
- `docs/SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_PRIMARY_RECONSTRUCTION_2026-09-22.md`
- `docs/SDE_2D5F_A_FOND_FAREWELL_PRIMARY_RECONSTRUCTION_2026-09-22.md`
- `docs/SDE_2D5F_TRAVELLER_MODEL_BOUNDARY_AUDIT_2026-09-22.md`
- `docs/SDE_2D5F_B4F_SILVER_GENERALIZATION_AUDIT_2026-09-23.md`

Do not revive archived pre-SDE-3 execution routes as parallel authority.

## 2. Live-state rule

Before any executable edit:

1. query live branch HEAD for `sde-3c-decision-trace-shadow-replay`;
2. confirm PR #153 remains merged and query live `main`;
3. create/find the SDE-3C PR when GitHub control-plane access is available and confirm it is **draft**;
4. query current CI/R2 checks for the exact SDE-3C head;
5. never assume the SHA recorded in a prior chat is still current.

The SDE-3C PR must stay **draft** once created.

## 3. Current program state

Engineering through SDE-2D4 is complete.

SDE-2D5 evidence/calibration has reached a stable checkpoint:

- two independent admitted GOLD decision slices:
  - Ben Burns / `A Stud In Scarlet` / `st-ben-burns`;
  - Evin 2019 first full playthrough / `st-evin`;
- A Fond Farewell is primary-verified but remains Traveller-execution-blocked;
- executable expert counterfactual evidence is green;
- executable ct-01 SILVER external-validity replay is green;
- B4F targeted evidence gaps are explicit;
- broad video/source discovery is no longer the default.

Evidence acquisition is now **parallel / external / continuous**. It no longer blocks SDE-3A/B/C.

Current route:

~~~text
SDE-3A engine / feature / policy contract                  COMPLETE
SDE-3B BEGINNER_CONSERVATIVE_V1 interpretable policy       COMPLETE / T4 ACCEPTED / PR #153 MERGED
SDE-3C shadow recommendation / DecisionTrace / replay       CURRENT / 3C0–3C3A CODE CHECKPOINT
SDE-3D calibrated policy freeze                             BLOCKED ON EVIDENCE
SDE-3E automatic production cutover                         BLOCKED ON 3D
~~~

## 4. Why SDE-3 may proceed now

Current evidence is sufficient to establish architecture and qualitative policy dimensions, even though it is not sufficient to freeze final numeric weights / gates.

Stable foundations include:

- rules/canonical producers own legality;
- SDE owns policy among legal alternatives;
- player-controlled targets remain player-owned;
- Spy/Recluse registration is interaction-scoped;
- Drunk/poisoned information may be true or false;
- repeated impaired information needs one shared role-agnostic derived narrative projection over canonical history; no second mutable perceived-world/narrative owner is allowed;
- Demon bluffs are a joint SDE output until committed;
- Red Herring is a contextual setup precommit;
- topology is important but not sufficient;
- confirmation chains, healthy-information utility, truth danger / credibility disruption, role-function exposure, bluff usability, narrative coherence and future flexibility remain separate dimensions;
- no opaque global scalar;
- no fixture-specific policy branches.

What remains under-evidenced is **how strongly to trade these dimensions off**, not whether the engine should represent them.

## 5. Current evidence maturity

### Cross-expert foundation

**Red Herring contextual utility**

- Ben: likely Fortune Teller target ecology;
- Evin: Chef truth danger / credibility disruption.

Do not collapse these into a fixed neighbour bonus, role bonus or scalar weight.

### Strong but not final

**Truth danger / credibility disruption**

- Evin primary GOLD explicit rationale;
- ct-02 SILVER explicit Storyteller rationale;
- repeated supporting qualitative evidence.

Keep the dimension. Do not create a deterministic “suppress the strongest truth” rule.

### Single-expert explicit + qualitative

**Impaired-information believability / perceived-world coherence**

Ben supplies explicit primary rationale, but independent GOLD confirmation is still missing.

The architecture may implement the shared persistent narrative mechanism now. Final preference severity remains calibratable.

### Observation-only / weak preference evidence

- role-function exposure severity;
- Demon-bluff triplet ordering;
- exact healthy-information floor;
- quantitative multi-axis tradeoffs.

These do not justify final weights or hard production cutover.

## 6. External evidence track

ClocktowerEvidenceLab should continue collecting full real games.

CampBoardGameHost should request new evidence only for named gaps in:

`docs/SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md`

Priority:

1. independent experienced Storyteller with explicit healthy-information floor / middle-band rationale;
2. independent-expert impaired-information believability rationale, ideally cross-night;
3. explicit role-function exposure rationale;
4. explicit Demon-bluff triplet rationale;
5. broader executable corpus for multi-axis tradeoffs.

Do not resume broad source collection in this repository.

## 7. Completed engineering checkpoint — SDE-3B

**Do not start by inventing scoring weights or treating unavailable features as neutral.**

First perform a live architecture/fanout audit.

PR/branch boundary:

- PR #150 is the merged SDE-2D5 evidence/calibration checkpoint;
- PR #151/#152 are merged SDE-3A checkpoints;
- PR #153 is the merged SDE-3B checkpoint;
- current branch is `sde-3c-decision-trace-shadow-replay`;
- no SDE-3C PR had been created when this handoff was updated because GitHub control-plane tools were unavailable in the conversation; create it as draft when available;
- current policy audit is `docs/SDE_3B_BEGINNER_CONSERVATIVE_V1_POLICY_AUDIT_2026-09-23.md`;
- completed 3B1 audit is `docs/SDE_3B1_HISTORICAL_LIFECYCLE_INPUT_BINDING_COMPLETION_AUDIT_2026-09-23.md`;
- completed 3B2 audit is `docs/SDE_3B2_CONFIRMATION_CHAIN_COMPLETION_AUDIT_2026-09-23.md`;
- completed 3B3 audit is `docs/SDE_3B3_IMPAIRED_NARRATIVE_COMPLETION_AUDIT_2026-09-23.md`;
- completed 3B4 audit is `docs/SDE_3B4_HEALTHY_INFORMATION_UTILITY_COMPLETION_AUDIT_2026-09-23.md`.
- completed 3B5 architecture audit is `docs/SDE_3B5_ROLE_FUNCTION_EXPOSURE_ARCHITECTURE_AUDIT_2026-09-23.md`;
- completed 3B5 implementation audit is `docs/SDE_3B5_ROLE_FUNCTION_EXPOSURE_COMPLETION_AUDIT_2026-09-24.md`.

Current implemented boundary:

- policy can defer when upstream features or the required strategic topology are unavailable;
- the only generic hard rejection is exact Evil-topology retention reaching zero;
- non-zero retention is never rejected through an invented threshold;
- unsupported preference dimensions remain explicit limitations;
- viable candidates remain one survivor equivalence band until richer feature projectors justify ordering;
- seeded selection is deterministic, weight-free and survivor-only;
- structured shadow carries policy evaluation but production recommendation/confirmation remains unchanged;
- source ability state and actual-state semantic truth are now projected from existing legal-candidate semantics without importing legacy score/probability;
- confirmation-chain impact is now projected per candidate from exact recipient-visible committed history;
- impaired-narrative lifecycle/coherence and healthy-information utility are projected as score-free diagnostics;
- healthy information uses upstream legal `TruthRelation`: both functioning `TRUE_TO_ACTUAL_STATE` and `TRUE_TO_REGISTERED_STATE` are healthy, while actual-state `SemanticTruth` remains a separate feature;
- V1 policy ordering remains strategic-only.

Revised architecture status after the ClocktowerEvidenceLab C0 handoff:

- **3B1 COMPLETE:** lifecycle-safe historical structured shadow, canonical committed-prefix refs, typed contextual input binding, and no-hindsight enforcement are implemented and audited;
- `ActionFactTimeline + EpistemicObservationLog` remain canonical history; no second mutable narrative state exists;
- external owners such as Red Herring / Demon bluffs remain external and are referenced through typed bindings rather than copied into SDE;
- upstream `AbilityState` and canonical action/setup history remain the actual impairment authority; player-facing observation reliability is not used to infer impairment;
- **3B2 COMPLETE:** generic exact confirmation-chain projection is wired into structured shadow with recipient visibility, strict no-hindsight, explicit capability/unavailable handling, bounded R02/R04-style semantic regression, and no policy preference change;
- **3B3 COMPLETE:** the shared score-free impaired-narrative projector derives persistent setup-bound and temporary action-bound episodes from canonical history plus authoritative impairment state; structured shadow integration, R04/R06 E2 regressions and policy non-consumption are green;
- **3B4 COMPLETE:** healthy-information utility derives usable/independent whole-table routes, redundancy/contradiction, route loss and last-route removal from canonical history plus existing exact/confirmation evidence; baseline-already-infeasible history is guarded and legal registered truth remains healthy; R01/R04 bounded E2 regressions are green; V1 policy remains unchanged;
- **3B5 COMPLETE:** generic score-free role-function exposure semantics cover direct/new/already/confirmation-amplified and forced/avoidable exposure; registration ambiguity is the first rules-backed mechanism; canonical history/confirmation are reused; bounded `goldcand-ben-03` E2 regression is green; V1 policy remains unchanged;
- **3B6 COMPLETE:** the E3 eligibility audit authorizes no new soft ordering at this checkpoint; `preference-evidence-not-authorized` is explicit, survivor ties remain intact, and no new policy reason was introduced.
- only after each projector has semantic replay evidence may expert rationale become a V1 soft preference.

Evidence authority levels are now explicit:
- E1 architecture/feature existence — use now;
- E2 real-game semantic regression — use after structure/projector exists;
- E3 qualified expert qualitative policy — use only after feature stability;
- E4 numeric thresholds/tradeoffs — defer to SDE-3D.

## 8. Target SDE-3A contracts

The target typed architecture is:

~~~text
DecisionCandidate
    legal outcome at one exact lifecycle stage

DecisionFeatures
    interpretable independent consequence dimensions

PolicyEvaluation
    survivor / rejection / preference reasons
    policy version

DecisionTrace
    replayable diagnostic record
~~~

Feature families should remain explicit:

- strategic topology / cover;
- confirmation-chain structure;
- healthy-information utility;
- truth danger / credibility disruption;
- role-function exposure;
- impaired narrative coherence / detectability;
- bluff usability / route diversity;
- future flexibility.

V1 must not force them into one opaque score.

## 9. Target SDE-3B policy

First provisional profile:

`BEGINNER_CONSERVATIVE_V1`

Preferred policy shape:

~~~text
legal candidates
    ↓
hard lifecycle / legality boundaries
    ↓
catastrophic / near-catastrophic rejection
    ↓
ordered interpretable soft priorities
    ↓
equivalence band
    ↓
seeded random selection
~~~

Allow conservative qualitative policy before final numeric calibration.

Examples of acceptable V1 intent:

- avoid catastrophic confirmation / Evil-topology collapse when alternatives exist;
- do not leave Good with effectively no usable information;
- preserve a coherent impaired perceived world;
- treat obvious role-function exposure as a contextual cost;
- evaluate Red Herring in downstream information context;
- prefer usable beginner bluff routes;
- preserve future flexibility;
- use randomness instead of fake precision when evidence does not distinguish survivors.

## 10. SDE-3C trace/replay requirement

Current implementation checkpoint:

- **3C0 COMPLETE:** architecture/fanout/persistence ownership audit is committed; DecisionTrace is explicitly diagnostic/replay state, never canonical game state.
- **3C1 COMPLETE:** structured production shadow carries deterministic V1 `PolicySelection?`; Ready selects a survivor using stable decision identity + canonical game seed, Deferred has no selection, and visible recommendation/confirmation remains untouched.
- **3C2 COMPLETE:** `DecisionTrace` and a policy-version-neutral Ready/Deferred policy snapshot are implemented with schema version, evidence checkpoint, lifecycle/revision, canonical prefix reference, complete candidate IDs, typed features, recommendation and pending/committed actual-choice shape.
- **3C3A COMPLETE:** `DecisionTraceArchive` is a separate immutable replay archive keyed by canonical game/decision/lifecycle/revision/policy identity. Only `Global` history-prefix traces may enter it; identical append is idempotent and same-key different-content append fails closed.
- 3C1/3C2/3C3A were accepted at remote HEAD `a75f482dd5a9aa5a128525c2cfb47be127ded72b` with CI #3432 and R2 #3187 SUCCESS.
- **3C3B COMPLETE:** strict deterministic `DecisionTraceArchiveJsonCodec`, stateless immutable-archive `DecisionTraceArchiveStore`, dedicated SharedPreferences transport, and tests-first malformed/version/duplicate/conflict/canonical-prefix/idempotence coverage are implemented. Persistence is not wired as an automatic side effect of the read-only shadow evaluator; actual authoritative-choice correlation remains 3C4.
- Oracle `test:fast` was invoked before and after the 3C3B implementation, but both attempts are blocked during Gradle configuration before Kotlin compilation because the Oracle host has no Android SDK. Independent GitHub acceptance at exact HEAD `9cffa4e94b088342e1808c9945febb790a0b0232` is CI #3433 SUCCESS and R2 #3188 SUCCESS.
- **NEXT: 3C4 authoritative-choice correlation**, followed by 3C5 multi-policy replay.

Before automatic cutover, persist a replayable diagnostic trace with:

- policy version;
- evidence/corpus checkpoint;
- lifecycle / state revision;
- complete legal candidate IDs;
- typed feature values;
- rejection/survival reasons;
- recommended candidate;
- actual committed candidate;
- manual override flag;
- optional structured/textual override reason.

A human override is useful calibration evidence but is **not automatically a quality label**.

Historical game state must be replayable under multiple policy versions without mutating historical truth.

## 11. Continuous calibration model

No uncontrolled production online learning.

Do not train on win/loss as a direct Storyteller-quality label.

Long-term loop:

~~~text
ClocktowerEvidenceLab / exported DecisionTrace
        ↓
versioned evidence corpus
        ↓
material DecisionSlices
        ↓
CampBoardGameHost production legality
        ↓
feature projection
        ↓
offline policy comparison / replay
        ↓
review / regression gate
        ↓
explicit new policy version
~~~

Policy evolution is expected and intentional.

## 12. Blocked boundaries

Still blocked:

### SDE-3D calibrated policy freeze

Do not freeze:

- numeric healthy-information bands;
- exact role-function exposure severity;
- bluff-triplet preference weights;
- independent-expert impaired-believability strength;
- global multi-axis weights.

### SDE-3E automatic production cutover

Do not make the provisional policy the sole automatic authority before:

- SDE-3A typed contracts are stable;
- SDE-3B is regression-safe;
- SDE-3C shadow / trace / replay is demonstrated;
- relevant SDE-3D evidence gate is satisfied;
- manual Experienced-mode override remains available.

## 13. Legacy cleanup boundary

Do not delete legacy recommendation authority at the beginning of SDE-3A.

Retirement order:

~~~text
typed SDE-3 contracts
→ shadow evaluation
→ provisional advisory policy
→ evidence-calibrated policy
→ production cutover
→ fanout retirement audit
→ delete obsolete legacy authority
~~~

Known retirement targets include:

- `recommendation/dynamic/ConsequenceEvaluator`;
- heuristic `evilAdvantage` / related stale pressure state where no longer authoritative;
- setup `bluffDifficulty` strategic authority after SDE bluff ownership is validated;
- superseded recommendation tests/scaffolding after stronger typed coverage exists.

## 14. Validation

Follow `AGENTS.md` and `TESTING_STRATEGY.md`.

For the next architecture/audit step, documentation-only findings do not require Android regression.

Once shared contracts change:

- perform mandatory producer/consumer fanout audit;
- use the narrowest durable typed test at the true owner;
- run T1/T2 according to affected semantics;
- use T3 evidence harnesses only when their evidence surface is affected;
- use `[full-ci]` at the logical acceptance checkpoint.

Do not manufacture RED for behavior-preserving refactoring.

## 15. Historical evidence checkpoint

The bounded B4F SILVER workload is:

`:app:sde2D5FB4FSilverGeneralization`

Accepted evidence run:

- run #3;
- head `17254848e47abf05c1b7648948ae8b47a89f9e6f`;
- success;
- about 7 minutes.

The old external-human auto-workflow that could take ~2h48m has been retired.

## 16. Next conversation task

SDE-3A is merged. SDE-3B is active on draft PR #153.

Current SDE-3B checkpoint:

- policy audit completed before production edits;
- Ready/Deferred decision-level policy result and limitation surface implemented;
- exact zero Evil-topology contradiction gate implemented;
- no non-zero retention threshold exists;
- viable candidates remain explicitly tied when evidence cannot order them;
- seeded survivor-only selector implemented without probability weights;
- structured numeric shadow carries V1 policy evaluation without changing visible recommendation or canonical commit;
- source `AbilityState` and actual-state semantic truth are projected from upstream legal-candidate semantics;
- canonical semantic history remains the intended owner for cross-night narrative continuity;
- **3B2 is COMPLETE:** structured confirmation features are production-owned diagnostics but remain shadow-only and policy-neutral;
- **3B3 is COMPLETE:** structured impaired-narrative features are derived diagnostics over canonical history and authoritative impairment state; V1 policy remains unchanged;
- **3B4 is COMPLETE:** structured healthy-information features are derived diagnostics over canonical history, ability state, legal truth relation and confirmation provenance; functioning registered truth is preserved, collapsed baselines are not blamed on the current candidate, and V1 policy remains unchanged.

Immediate next action:

1. re-query live #153 / main / checks before any further mutation;
2. preserve PR #153 as **draft and unmerged** until explicit project-owner authorization;
3. SDE-3B T4 acceptance is complete at `30caec3dcd546f4395238809d1f1d285688cd814`: CI #3427 and R2 #3183 succeeded;
4. do not add more SDE-3B semantics after this accepted checkpoint unless a real acceptance defect is found;
5. after explicit authorization and merge of #153, begin **SDE-3C DecisionTrace / replay** with an architecture/fanout audit before persistence edits;
6. SDE-3C must preserve canonical history, version policy identity, capture typed feature/policy snapshots, and support replay without turning V1 into production authority;
7. SDE-3D numeric calibration and SDE-3E automatic cutover remain blocked.

Keep #153 draft until explicit user **“授权合并”**.
