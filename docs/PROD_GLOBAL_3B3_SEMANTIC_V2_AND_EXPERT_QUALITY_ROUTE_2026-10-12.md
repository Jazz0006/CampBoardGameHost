# PROD-GLOBAL-3B3 — Semantic V2 / Expert Prompt / Independent Acceptance

> 2026-10-12 · EXPERIMENTAL / NO PRODUCTION CUTOVER · GPT-6 Luna only.
> Baseline: 3B2 Draft PR #299 on `experiment/prod-global-3b2-expert-probe-20261012`.
> This document supersedes the 3B2 V1 output proposal for **future experiments**, not the running Android API.

## Verified state and critical boundaries

- At 3B3 start: live `main=10e5da8f165cbae0288b42d33c50bd0344b116d8`; 3B2 local head `3f87a397c542c64532623bb62694d0b3467a1c87`; clean; Draft #299, CI #4112 and R2 #3734 GREEN, unmerged.
- 3B2 E05/E02/E03/E04 actual Luna run figures and structural anomalies are provided in the 3B3 handoff. The actual four frozen `plan.json` files were additionally retrieved read-only from the private Project file library (on 2026-10-12); this is a **V1 shape diagnostic**, not a V2 model test or Host legal-domain validation. Source files were not modified, uploaded to Git or converted to synthetic gold. See compact evidence below. Real-model V2 protocol equivalence and Host semantic acceptance have NOT been established.
- Mini MCP `experiment_run` currently says **OFFLINE MOCK ONLY**. EXPT-2A mock completed; EXPT-2B paid-disabled registered. No supported paid execution via this interface. Keep private results private; do not copy actual hidden-world responses into a public repository.
- Production `FirstNightOneShotRequestV1`, `FirstNightOneShotPlanV1` and `PersonalDirectOpenAiV1` currently consume a *post-Drunk* candidate-ID schema and remain untouched. Production manual/AI-assisted/AI-automatic behavior is unchanged, so existing tests stay relevant.
- Local Mini MCP direct editing is used for these experimental source changes; Codex is not used for design or modifications.

## 3B3A — independent experimental semantic contract

Authority hierarchy: current Host game facts, role definitions, legal-domain reasoning and timing > schema structure > AI advisory plan/memory. V2 is a **proposal and offline screening module**, not a Host validator.

Source: `tools/prod_global_3b3_semantic_v2.py`. Canonical role IDs use Host catalog external IDs: lowercase alphanumeric (e.g. `fortuneteller`), not locale-dependent labels, parenthetical statuses or free text. They map losslessly to known names via the supplied `roleKnowledge` catalog; production adapter must verify the identity mapping against its live `ClocktowerCharacterRegistry`.

- **World input:** shown role ID and prospective actual-role status are distinct. For a pending Drunk candidate, no actual Drunk seat is asserted; one potential town role can eventually be Drunk. Include explicit `hasDrunk`, current revision, player experience with UNKNOWN provenance, events already confirmed, events genuinely pending, and optional short recentGames. Input does not enumerate legal options or expose grading/answer data.
- **Single output:** `openingPlan` (one drunk seat/null, red-herring/null, three valid demon bluff IDs when applicable, typed `information` and pending `deferredActions`), `strategyMemory` (tensions, conditional intentions with risks, uncertainties), and `tradeoffSummary`.
- **Information element:** `sourceSeat` + `abilityRoleId` identify the shown player and seen ability; `effectiveAbilityRoleId` separately identifies the usable ability (null for a Drunk shown as a Townsfolk), while `basis` denotes FUNCTIONING / DRUNK_MISINFORMATION / STATUS_DEPENDENT / RULE_DETERMINED. It is *not* an additional role label. `resolution` distinguishes DECISION, HOST_DETERMINED, CONDITIONAL_INTENT or DEFERRED. CONDITIONAL_INTENT preserves an optional proposed observation across an unsatisfied event dependency; the stored proposal is NOT a published or authoritative result. Host must re-evaluate it against confirmed state before any real publication. `subject` distinguishes EVIDENCE_PAIR (explicit witness seats), FIXED_NEIGHBORS (Host computes; empty seats), PLAYER_QUERY_PENDING (not yet selected; empty seats), NONE. `observation` is exactly one of NUMBER / ROLE / BOOLEAN / NONE with disjoint value slots. Unresolved future actions are explicit `waitingForEvents`: `DEFERRED` has no observation, whereas `CONDITIONAL_INTENT` can contain an *uncommitted proposed* observation; neither is a confirmed player-facing fact.
- **Critical registration distinction:** a NUMBER output (for example from a fixed-neighbor ability) is an observable result and does not carry those neighbors as `targetSeats`. Legal witness ambiguity due to variable registrations must remain uncommitted even when several witnesses give the same number. The model may propose a legal result; Host must independently verify it, not infer a unique witness.
- **Drunk or poison:** Drunk shown role and actual status are separately tracked. A Drunk could receive fabricated information; poison affected observations remain conditional until its actual target/event is confirmed. Neither condition authorizes an out-of-order display.
- **Determinism:** `HOST_DETERMINED` returns NO observed value; the Host calculates and publishes it at the correct step. If registration or status makes more than one lawful result, it is NOT uniquely determined. The Host must reject incorrect classification.
- **Completeness:** screening currently verifies strict types, IDs, freshness marker, some obvious Drunk/red-herring/bluff constraints and typed payload invariants, but **does not prove** full decision coverage, real registration legality, information reliability, exact role effect, Host commitability or session revision match. Those require production Host adapter and full as-of validation; do not label current output legal/accepted.

### Whole-bundle acceptance design (not implemented in Android)

Host must independently derive the current conditional legal domain and expected decision inventory from its live authoritative snapshot. Under an exact game/roster fingerprint, revision and pending event prefix, resolve the proposed Drunk branch, translate the machine role IDs and observation propositions to Host-native typed candidate semantics; verify all required choices once, all deferred dependencies and deterministic boundaries, real rule/legal registration interpretation (without inventing witness), cross-field identities/bluffs/RH, and true event timing. **Atomic check first**; only then accept one plan as private pending intent. No partial state commit or invented future observation. Confirmation/publication proceeds through normal rule-owned event steps.

On malformed, incomplete, stale or illegal output: reject the **entire proposed bundle** without substituting one item or auto-picking the first available candidate. A future bounded full-bundle repair may be requested only with refreshed Host state, an explicit call/expense limit and separate telemetry; if it fails, pause AI automatic or revert to explicit manual takeover. Prior valid human-confirmed and published observations must never be erased.

## 3B3B — frozen prompt V2

See `docs/PROD_GLOBAL_3B3_EXPERT_PROMPT_V2.txt`. It is script-agnostic in decision philosophy and uses supplied role catalog, script rules, current events and experience. It deliberately avoids seat numbers, examples, “best Drunk” answers, role-specific ranking rules and long internal explanation. Explicitly requests a single joint plan, meaningful tradeoffs, severe-outcome evaluation, player-visible worlds and conditional strategic memory. It cannot be judged effective until real external model output has been evaluated; length/cost improvement is not assumed.

## Frozen four-sample read-only diagnostic (2026-10-12)

This diagnostic used actual existing uploaded E02/E03/E04/E05 `plan.json` files, not rewritten outputs. It checks source-seat shown-role text, inappropriate numeric targetSeats and presence of proposed information awaiting unresolved Poisoner events; it does **not** run the independent Host validator.

| Frozen sample | Proposed information rows | Findings | Host rule acceptance |
| --- | ---: | --- | --- |
| E05 | 2 | no V1 mechanical annotation anomalies | NOT PERFORMED |
| E02 | 4 | all four candidate observed values are conditional on unknown Poisoner target, not already published | NOT PERFORMED |
| E03 | 4 | one NUMBER value also carries fixed-neighbor seats in `targetSeats` | NOT PERFORMED |
| E04 | 3 | one shown role ID contains a Drunk description; one NUMBER value includes neighbors | NOT PERFORMED |

**Why conditional proposal is required:** In E02 the model intentionally coordinated four initial information suggestions but correctly stated the Poisoner's target remained unknown. Dropping every proposed value would lose that global plan. V2 therefore supports `CONDITIONAL_INTENT` with `STATUS_DEPENDENT`, nonempty `waitingForEvents`, and an *uncommitted proposed observation*. Until the causal event and current Host legality have been reverified, it is neither an issued observation nor a valid Host commit. `DEFERRED` without an observation remains available when no concrete conditional result has been selected. This is a general causal-dependency protocol, not a Poisoner-only strategy or a claim that the suggestions are independently legal.

## 3B3C — evidence and independently frozen holdouts

- First offline characterization tests are `tools/tests/test_prod_global_3b3_semantic_v2.py`. Tests use **synthetic structural witnesses**, not original 3B2 Luna responses or inferred expert-quality gold answers. Known E03/E04 V1 formatting ambiguities are diagnostic data only, no rewritten original files or retroactive passing claim.
- `legacy_diagnostics()` classifies annotated role strings and NUMBER's incidental target seats **without changing their original meaning**. It does not upgrade original V1 results into valid V2 or claim Host legality. A future bounded developer-only process should load private E02/E03/E04/E05 frozen `.plan.json` into diagnostics; results must remain outside Git and be compared against Host rules separately.
- Newly frozen, **unused-for-V2-prompt-tuning** blind suite: `docs/PROD_GLOBAL_3B3_BLIND_HOLDOUTS_V1.json` includes N01–N05 (Poisoner status dependency, Baron/Drunk multiple information roles, Spy/Recluse registration, consequence-sensitive Good roles, optional recent-games history, and an expertise-only N02/N05 contrast). It contains no gold plan or expected recommended seat.
- Four independent verdicts for each future model output: STRUCTURE (JSON fields/types/protocol), HOST_SEMANTIC (canonical actual-role, registration, dependency/time/coverage/revisions), EXPERT_STRATEGY (fairness, plausible worlds, nonrepeat opportunity cost and serious-outcome justification), PRODUCTION_ACCEPTANCE (one-plan UX, legal Host commit/restore/one-tap/no double publish on device). All four are separate, and model output does not certify itself.
- The prior **26–43 s** latency baseline is retained (E05 27.941s/5266 tokens, E02 26.424s/5107, E03 32.349s/5977, E04 43.047s/6709); do not claim timing improvement until measured at same conditions.

## Next gate before production wiring

1. Confirm offline tests/CI on exact head, and read (not rewrite) private frozen 3B2 raw reports via a future explicitly authorized read-only report interface or user-driven offline analysis command. Do not use generic write-capable task execution to bypass report-read permissions.
2. Finalize a **Host-native interpreter+independent full-bundle validator** with fixture-backed semantic parity for each decision and intentional deferred dependency. This is a separate production-ownership change and must be typed tested before any cutover.
3. Only when a paid EXPT interface actually allows controlled model calls, audit preflight cost, timeout, tokens, private storage, never-auto-retry and cumulative budget. Test a small held-out Luna batch. Retain raw inputs/responses privately, evaluate four axes blindly and keep historical E02–E05 frozen.
4. Integrate into single background precommit global plan alongside identity reveal, continue on confirmed Host events only, retire old **production** one-shot/Drunk forks only after phone/CI/R2 semantic acceptance.

This branch contains experimental tooling and docs only. No API calls, Host mutations, model training, obsolete compatibility adapters or old heuristic authority have been added.
