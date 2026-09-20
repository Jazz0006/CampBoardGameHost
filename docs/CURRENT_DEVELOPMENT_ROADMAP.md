# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-21 Australia/Sydney  
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
SDE-2D5 calibration / policy evidence                 CURRENT / PR #150 draft
    D5A–D5E                                            COMPLETE
    D5F infrastructure                                COMPLETE
    D5F-B3 correction                                 HISTORICAL CHECKPOINT
    D5F-B4 expert-observed policy calibration         CURRENT
D5F-C gate/band derivation                            BLOCKED
sealed holdout                                        CLOSED
SDE-3                                                 BLOCKED
~~~

## 2. Current branch / PR

Branch: `sde-2d5-calibration-policy-evidence`

PR: **#150 — SDE-2D5: calibrate strategic policy evidence**

PR #150 remains **draft**. Do not merge unless the user explicitly says **“授权合并”**.

Always query live refs before executable edits.

## 3. Current authorities

- First-night policy: [`SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md`](SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md)
- External evidence seed: [`SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv`](SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv)
- Long-term pre-SDE-3 route: [`SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`](SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md)
- Global SDE architecture: [`STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`](STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md)

## 4. Frozen architecture / policy decisions

- Optimize only variables still controllable at the current lifecycle stage.
- Rules/canonical producers own legality; epistemic/topology layers own consequences; SDE owns policy/selection.
- Drunk shown identity is setup-persistent; committed information is immutable.
- Demon bluffs and Red Herring are persistent once revealed/committed.
- Fortune Teller target pair and Poisoner target are player-controlled.
- Spy/Recluse registration is per interaction.
- BEGINNER thematic prior: Spy normally registers as Good/Townsfolk/Outsider; Recluse normally registers as Evil/Minion/Demon.
- Actual registration is a fallback when the thematic default materially improves whole-bundle health.
- Avoid Librarian -> Recluse when a healthy alternative exists; strongly avoid Investigator -> Spy when a healthy alternative exists.
- Chef/Empath are rule-determined only when every legal registration branch yields the same healthy value.
- Drunk/poisoned information may accidentally be true; repeated/history-dependent impaired information must use a persistent role-agnostic narrative state so later outputs remain coherent with the perceived world already established.
- Strategic Evil topology is primary structural evidence, but role-information utility, confirmation chains, role-function exposure, bluff usability and information floor remain separate.
- No opaque global scalar.
- No named-role / known-fixture policy patches for cross-interaction coherence. Role-specific code owns legality only; shared semantic/history policy owns narrative continuity.

### Shared impaired-narrative implementation invariant

When D5F/SDE-3 reaches cross-night impaired information, implement one reusable persistent narrative-state mechanism over generic information propositions/history.

Do not create separate coherence algorithms for individual roles or calibration fixtures.

Acceptance for that future implementation must show:

- one shared owner for persistent impaired narrative intent/history;
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

Current external catalog contains SILVER / QUALITATIVE seed evidence only. It contains no verified GOLD cases yet.

Interpretation rules:

- chosen A does not imply every unchosen B/C/D was bad;
- explicit rationale/rejection is stronger than silent choice;
- repeated comparable choices and cross-source consistency strengthen evidence;
- final game winner is not a Storyteller-quality label;
- human review is secondary: reconstruction checks, conflict adjudication, and BEGINNER-specific adaptation.

## 7. Immediate execution order

### D5F-B4A — retire obsolete clean calibration artifacts

Audit and delete if redundant:

- `Sde2D5FRepresentativeHealthyInformationCorpus.kt`;
- `Sde2D5FRepresentativeHealthyInformationCorpusTest.kt`;
- related report/task wiring and source-controlled artifacts.

Do not delete canonical production legality/topology infrastructure.

### D5F-B4B — discover and verify GOLD sources

Search high-quality Trouble Brewing real games, prioritizing official/TPI-affiliated material, clearly experienced Storytellers, complete recordings/Grimoire state, and explicit rationale.

Record why each source qualifies or does not qualify as GOLD.

### D5F-B4C — reconstruct expert Night 1 decisions

Capture setup/seats, Drunk shown identity, Demon bluffs, Red Herring, relevant Spy/Recluse registrations, Poisoner context, first-night outputs, lifecycle stage, rationale, and player experience when known.

### D5F-B4D — recover legal counterfactuals

Use existing production legality owners to reconstruct the exact alternatives available at each decision point.

### D5F-B4E — compare observed choices to alternatives

Run existing whole-bundle/topology diagnostics over observed expert choices and their legal counterfactuals. Extract repeated interpretable preferences, not a black-box score.

### D5F-B4F — generalize with SILVER evidence

Use ClockTracker/high-fidelity logs after GOLD patterns exist.

### D5F-B4G — bounded human adjudication

Use human review only for ambiguous/conflicting evidence and BEGINNER-product adaptation.

## 8. D5F-C remains blocked

Do not derive/freeze gates from old human labels, inspect sealed holdout, tune on holdout, cut production policy, begin SDE-3, or merge PR #150.

D5F-C may begin only after expert-observed evidence covers the material first-night policy variables and legal-counterfactual comparisons support repeated interpretable constraints.

## 9. Compatibility code intentionally retained

Do not remove merely as part of D5F-B4 evidence work:

- impaired-information approximate 90/10 false-family bridge;
- legacy `MalfunctionPolicy`;
- legacy registration/scoring paths still used by production.

Their retirement belongs to later production cutover/cleanup.

## 10. Testing cadence

Follow root `AGENTS.md` and [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md).

Documentation-only cleanup needs exact diff/reference audit, not manufactured runtime tests. Deleting obsolete test-only calibration artifacts requires proving no unique durable contract is lost and then running the smallest affected test tier.
