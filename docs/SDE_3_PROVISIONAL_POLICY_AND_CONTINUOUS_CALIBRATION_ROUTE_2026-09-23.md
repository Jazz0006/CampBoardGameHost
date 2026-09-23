# SDE-3 — Provisional Policy and Continuous Calibration Route

> Date: 2026-09-23 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Status: **CURRENT SDE-3 EXECUTION AUTHORITY**  
> Parent architecture: `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`  
> Current evidence authority: `docs/SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md`  
> Current SDE-3A audit: `docs/SDE_3A_ENGINE_FEATURE_POLICY_CONTRACT_AUDIT_2026-09-23.md`  
> Targeted evidence contract: `docs/SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md`

## 1. Decision

The Storyteller project must continue even though final expert-policy calibration is incomplete.

Evidence shortage blocks **final policy freeze and automatic production cutover**, not the engineering architecture that makes future calibration possible.

The route is therefore split:

~~~text
SDE-3A engine / feature / policy contract                  COMPLETE ON PR #152 / PENDING MERGE
SDE-3B BEGINNER_CONSERVATIVE_V1 interpretable policy       NEXT AFTER SDE-3A MERGE
SDE-3C shadow recommendation / DecisionTrace / replay       NEXT
SDE-3D calibrated policy freeze                             BLOCKED ON EVIDENCE
SDE-3E automatic production cutover                         BLOCKED ON 3D
~~~

External evidence collection continues in parallel rather than sitting on the critical engineering path.

## 2. Product strategy

The first usable automatic Storyteller policy is not intended to imitate a world-class human Storyteller perfectly.

Its target is:

> **For ordinary / beginner Trouble Brewing tables, avoid clearly destructive Storyteller decisions, preserve playable information for Good, preserve credible Evil narratives, keep impaired information coherent, and make every recommendation explainable.**

Name the first provisional profile:

`BEGINNER_CONSERVATIVE_V1`

It is deliberately conservative and versioned.

It may improve later as EvidenceLab supplies stronger expert evidence.

## 3. Stable architecture versus continuously calibrated policy

Separate the long-lived engine from the changeable policy.

~~~text
canonical rules / lifecycle state
        ↓
legal candidate generation
        ↓
hypothetical consequence projection
        ↓
typed interpretable feature vector
        ↓
versioned StorytellerPolicy
        ↓
accepted/rejected candidates + reasons
        ↓
seeded selection among healthy survivors
        ↓
DecisionTrace
~~~

The top three layers should become increasingly stable.

The policy layer is expected to evolve:

~~~text
BEGINNER_CONSERVATIVE_V1
BEGINNER_CONSERVATIVE_V2
...
~~~

A policy revision must not require rewriting rules or creating fixture-specific branches.

## 4. SDE-3A — typed candidate / feature / policy contract

### 4.1 Candidate contract

A candidate represents one legal Storyteller-controlled output at one exact lifecycle stage.

It must preserve:

- decision type;
- current lifecycle / round;
- source ability / interaction;
- committed state revision;
- already-made player-controlled choices;
- legal outcome identity;
- hypothetical semantic proposition/effect;
- production-owned legality provenance.

Do not mix illegal-candidate filtering with policy preference.

### 4.2 Feature contract

The engine should project explicit independent dimensions rather than one opaque score.

Candidate dimensions may include:

#### Strategic structure

- Demon cover retention;
- Evil topology retention;
- Evil cover retention;
- forced-Good / forced-Evil structure;
- confirmation-chain / multi-channel collapse.

#### Healthy information

- remaining actionable healthy information;
- role-information utility;
- confirmation value;
- whether the candidate would leave Good with too little usable structure.

The existence of this dimension is required now. Numeric healthy-information gates remain unfrozen.

#### Truth danger / credibility disruption

- how damaging a functioning truthful channel is to the actual Evil topology;
- whether a legal misinformation route can plausibly contaminate confidence in that channel;
- whether the effect is lifecycle-valid rather than hindsight leakage.

Evin's Red-Herring rationale plus SILVER rationale evidence justify retaining this dimension.

#### Role-function exposure

- whether the candidate directly exposes mechanics whose intended function depends on ambiguity, e.g. Spy / Recluse interactions.

This is a soft contextual dimension until stronger calibration exists.

#### Impaired narrative

- semantic truth / falsehood;
- consistency with previously committed impaired information;
- perceived-world continuity;
- impairment detectability;
- transition cost when the prior perceived world becomes impossible.

The shared owner must be role-agnostic. Named roles contribute legal domains / semantic propositions only.

#### Bluff usability

For Demon-bluff joint outputs:

- claim burden;
- cadence / ongoing maintenance burden;
- narrative-route diversity;
- collision with actual Good information;
- collision with Drunk shown identity;
- support from available misinformation channels;
- future recoverability / fallback routes.

No fixed role ranking is authorized.

#### Future flexibility

- whether the choice unnecessarily consumes future misinformation / registration / narrative options;
- whether it creates brittle commitment chains.

### 4.3 No opaque global scalar in V1

V1 must not invent arbitrary numeric weights merely to force total ordering.

Preferred structure:

~~~text
hard legality
    ↓
hard / near-hard safety gates
    ↓
ordered interpretable soft priorities
    ↓
equivalence band
    ↓
seeded random selection among healthy survivors
~~~

A later evidence-driven policy may introduce calibrated bands or pairwise ordering, but the feature surfaces must remain inspectable.

## 5. SDE-3B — BEGINNER_CONSERVATIVE_V1

V1 may use evidence-backed qualitative constraints even when final numeric thresholds are unknown.

### 5.1 Hard boundaries

Always preserve:

- production legality;
- lifecycle ownership;
- immutable committed history;
- player-controlled target ownership;
- setup persistence of shown identity / Red Herring / revealed Demon bluffs;
- interaction-local Spy/Recluse registration semantics.

### 5.2 Conservative reject / avoid conditions

V1 may reject or strongly avoid candidates that are clearly bad for reasons already established by rules, architecture, or repeated evidence, for example:

- catastrophic Evil-topology / confirmation collapse when healthy alternatives exist;
- a whole bundle that leaves Good with effectively no usable healthy information;
- an impaired output that needlessly contradicts an already-established believable perceived world;
- an avoidable direct role-function exposure with healthier alternatives;
- a Demon bluff triplet that is mechanically legal but obviously unusable for the target beginner because all routes impose high claim burden or direct collisions.

These conditions must be expressed through generic features, not named fixture branches.

### 5.3 Contextual soft priorities

Among survivors, V1 may prefer:

- more credible Evil cover without making Good information inert;
- fewer destructive confirmation chains;
- coherent impaired narratives;
- contextual Red-Herring utility;
- believable misinformation rather than false-at-all-costs;
- usable, diverse bluff routes;
- preserved future flexibility.

If evidence cannot distinguish several survivors, use seeded randomness rather than fake precision.

## 6. SDE-3C — shadow recommendation, DecisionTrace and replay

SDE-3C is required before automatic cutover.

### 6.1 DecisionTrace

For every discretionary interaction, persist a replayable trace containing at least:

- policy version;
- evidence/corpus version or evidence checkpoint identifier;
- decision context / lifecycle revision;
- complete legal candidate IDs;
- projected interpretable features;
- rejection / survival reasons;
- recommended candidate;
- actual committed candidate;
- whether a human manually overrode the recommendation;
- optional structured override reason when supplied.

The trace is diagnostic/replay data, not a second canonical game state.

### 6.2 Human override data

Experienced/manual Storyteller mode may choose a different legal candidate.

An override can become useful future calibration evidence:

~~~text
engine recommended A
human selected B
reason = optional structured / textual rationale
~~~

Do not treat an override as automatically proving A was wrong or B was expert-quality.

### 6.3 Historical replay

A recorded real or app-hosted game should be replayable under multiple policy versions:

~~~text
same committed game history
    ↓
Policy V1 recommendation
Policy V2 recommendation
Policy V3 recommendation
~~~

This enables offline comparison without mutating historical truth.

## 7. Continuous offline calibration

Do not perform uncontrolled online learning in production.

Game outcome is not a Storyteller-decision quality label.

The supported loop is:

~~~text
ClocktowerEvidenceLab / exported app traces
        ↓
versioned evidence corpus
        ↓
material DecisionSlices
        ↓
CampBoardGameHost production legality reconstruction
        ↓
candidate feature projection
        ↓
offline policy comparison / replay
        ↓
evidence review / regression gate
        ↓
new explicit policy version
~~~

Policy updates are deliberate releases.

The app must not silently change weights from one local game result.

## 8. ClocktowerEvidenceLab boundary

ClocktowerEvidenceLab owns long-term external source collection and canonical real-game history.

CampBoardGameHost owns production legality, counterfactual candidate generation, feature projection and policy evaluation.

EvidenceLab should export facts such as:

- source / provenance;
- Storyteller independence key;
- complete or material committed game state;
- observed Storyteller choice;
- explicit rationale;
- timestamps;
- unknown fields;
- evidence tier / verification status.

It should **not** freeze CampBoardGameHost legal alternative sets as source truth.

CampBoardGameHost reconstructs those alternatives with its current production owners.

Current target gaps are defined in:

`SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md`

Targeted evidence acquisition continues in parallel with SDE-3A/B/C.

## 9. SDE-3D — calibrated policy freeze — BLOCKED

The following remain insufficient for final calibrated gates / orderings:

- healthy-information floor / middle-band numeric thresholds;
- role-function exposure severity;
- independent-expert impaired-information believability;
- Demon-bluff triplet preference ordering;
- quantitative multi-axis tradeoffs.

Therefore SDE-3D must not:

- freeze arbitrary scalar weights;
- publish unsupported player-count thresholds;
- convert one expert observation into a deterministic rule;
- claim calibrated expert parity.

Evidence collection can progressively unblock individual policy dimensions.

## 10. SDE-3E — automatic production cutover — BLOCKED

Automatic cutover requires:

1. SDE-3A typed contracts stable;
2. SDE-3B conservative policy regression-safe;
3. SDE-3C shadow / trace / replay demonstrated on real scenarios;
4. SDE-3D evidence gate satisfied for the policy surface being cut over;
5. product UX preserves manual Experienced-mode override;
6. old authoritative heuristic path has a completed fanout retirement plan.

Until then, recommendations may run in shadow / advisory form.

## 11. Legacy retirement boundary

Do not delete legacy behavior merely because SDE-3A begins.

Retirement is staged:

~~~text
new typed engine
    ↓
shadow parity / trace
    ↓
provisional advisory policy
    ↓
evidence-calibrated policy
    ↓
production cutover
    ↓
legacy fanout audit
    ↓
delete obsolete authority
~~~

`ConsequenceEvaluator`, legacy `evilAdvantage` heuristics, setup bluff-difficulty authority and related stale policy paths remain retirement targets, not immediate deletion targets.

## 12. Testing strategy

SDE-3A contract work should be tested at typed ownership boundaries.

Expected pattern:

- candidate contract → focused legality/orchestration tests;
- feature projector → deterministic typed feature tests;
- persistent impaired narrative owner → cross-role information-shape tests;
- policy → table-driven qualitative ordering / rejection tests;
- DecisionTrace → persistence/replay contract tests;
- production shadow wiring → integration tests;
- expensive evidence replay → dedicated T3 harness, not ordinary FAST.

Do not turn expert source examples into fixture-specific production tests.

## 13. Immediate implementation order

Start with an architecture/fanout audit before production edits.

PR #150 is merged as the SDE-2D5 evidence/calibration checkpoint. PR #151 merged the first SDE-3A ownership/typed-contract checkpoint. Draft PR #152 completes the SDE-3A structured feature-projection proof and passed the required T4 checkpoint at `dd82af3d8da9c17bc62d5606f045ddcc82c21bf0` (CI `35806237753`, R2 `35806237770`). Keep #152 draft until explicit user authorization to merge. SDE-3B starts only from live `main` after #152 is merged.

Recommended sequence:

1. audit current `StorytellerDecisionEngine`, decision context, candidate/result contracts and production shadow integration;
2. inventory existing exact/topology diagnostics that can populate the stable feature contract;
3. define the minimal generic `DecisionCandidate` / `DecisionFeatures` / `PolicyEvaluation` contract without migrating behavior prematurely;
4. define `BEGINNER_CONSERVATIVE_V1` hard-boundary and reason-code vocabulary;
5. add versioned `DecisionTrace` / replay contract;
6. migrate one narrow first-night decision surface into shadow evaluation;
7. fan out to other first-night Storyteller-controlled surfaces;
8. only then begin cross-night persistent impaired-narrative implementation;
9. keep automatic cutover off.

Every shared-contract change requires the AGENTS fanout audit before implementation.

## 14. Success criteria for the next development conversation

The next conversation should **not** resume evidence collection.

It should begin SDE-3A by auditing the live production architecture and produce:

- one authoritative owner map;
- proposed typed contracts;
- explicit reuse map for current legality / topology / bundle infrastructure;
- legacy surfaces that remain shadow-only versus future retirement targets;
- smallest first implementation slice and its durable tests.

No numeric policy calibration is required to begin.
