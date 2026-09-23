# CampBoardGameHost — Next Development Handoff

> Updated: 2026-09-23 Australia/Sydney  
> Branch: `sde-3b-beginner-conservative-v1`  
> PR: **#153 — SDE-3B: implement BEGINNER_CONSERVATIVE_V1 policy**  
> PR #151 and #152 are merged. The SDE-3B PR **MUST remain draft**. Do not merge unless the user explicitly says **“授权合并”**.

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
10. this handoff

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

1. query live branch HEAD;
2. query PR #153 state/draft flag and confirm PR #152 remains merged;
3. query live `main`;
4. query current checks;
5. never assume the SHA recorded in a prior chat is still current.

The PR must stay **draft**.

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
SDE-3B BEGINNER_CONSERVATIVE_V1 interpretable policy       CURRENT
SDE-3C shadow recommendation / DecisionTrace / replay       NEXT
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
- repeated impaired information needs one shared persistent perceived-world/narrative owner;
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

## 7. Current engineering target — SDE-3B

**Do not start by inventing scoring weights or treating unavailable features as neutral.**

First perform a live architecture/fanout audit.

PR/branch boundary:

- PR #150 is the merged SDE-2D5 evidence/calibration checkpoint;
- PR #151/#152 are merged SDE-3A checkpoints;
- current branch is `sde-3b-beginner-conservative-v1`;
- current PR is **#153** and must remain draft until explicit user authorization to merge;
- current policy audit is `docs/SDE_3B_BEGINNER_CONSERVATIVE_V1_POLICY_AUDIT_2026-09-23.md`;
- completed 3B1 audit is `docs/SDE_3B1_HISTORICAL_LIFECYCLE_INPUT_BINDING_COMPLETION_AUDIT_2026-09-23.md`;
- completed 3B2 audit is `docs/SDE_3B2_CONFIRMATION_CHAIN_COMPLETION_AUDIT_2026-09-23.md`.

Current implemented boundary:

- policy can defer when upstream features or the required strategic topology are unavailable;
- the only generic hard rejection is exact Evil-topology retention reaching zero;
- non-zero retention is never rejected through an invented threshold;
- unsupported preference dimensions remain explicit limitations;
- viable candidates remain one survivor equivalence band until richer feature projectors justify ordering;
- seeded selection is deterministic, weight-free and survivor-only;
- structured shadow carries policy evaluation but production recommendation/confirmation remains unchanged;
- source ability state and actual-state semantic truth are now projected from existing legal-candidate semantics without importing legacy score/probability;
- confirmation-chain impact is now projected per candidate from exact recipient-visible committed history, while V1 policy ordering remains strategic-only.

Revised architecture status after the ClocktowerEvidenceLab C0 handoff:

- **3B1 COMPLETE:** lifecycle-safe historical structured shadow, canonical committed-prefix refs, typed contextual input binding, and no-hindsight enforcement are implemented and audited;
- `ActionFactTimeline + EpistemicObservationLog` remain canonical history; no second mutable narrative state exists;
- external owners such as Red Herring / Demon bluffs remain external and are referenced through typed bindings rather than copied into SDE;
- upstream `AbilityState` and canonical action/setup history remain the actual impairment authority; player-facing observation reliability is not used to infer impairment;
- **3B2 COMPLETE:** generic exact confirmation-chain projection is wired into structured shadow with recipient visibility, strict no-hindsight, explicit capability/unavailable handling, bounded R02/R04-style semantic regression, and no policy preference change;
- **3B3 CURRENT:** build the shared role-agnostic impaired-narrative projector as a derived view over canonical history plus authoritative impairment state;
- **3B4/3B5:** healthy-information utility and contextual role-function exposure;
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
- **3B2 is COMPLETE:** structured confirmation features are production-owned diagnostics but remain shadow-only and policy-neutral.

Immediate next action:

1. re-query live #153 / main / checks;
2. begin **3B3 impaired-narrative coherence/detectability** with an architecture/fanout audit before production edits;
3. keep `ActionFactTimeline + EpistemicObservationLog` as the only durable history owners; do not create a persisted perceived-world/narrative state;
4. use authoritative current `AbilityState` and canonical setup/action history for impairment; never infer actual impairment from player-facing `ObservationReliability`;
5. distinguish persistent Drunk trajectory pressure from temporary Poisoner episodes inside one generic projector rather than named-role policy branches;
6. define typed descriptive coherence/transition/detectability features first, with generic RED tests before production implementation;
7. use Evidence Lab R02/R04/R06 only as bounded E2 semantic regressions after generic semantics are stable;
8. do not add V1 soft preferences, numeric bands, fixture-specific rules, DecisionTrace persistence, or production cutover during 3B3 feature construction.

Keep #153 draft until explicit user **“授权合并”**.
