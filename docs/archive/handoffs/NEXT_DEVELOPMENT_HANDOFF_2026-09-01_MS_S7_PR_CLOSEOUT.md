# NEXT DEVELOPMENT HANDOFF — MS-S7 / PR #61 Accelerated Closeout

> Updated: 2026-09-01 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **S6D ACCEPTED — S7 CURRENT — ACCELERATED PR CLOSEOUT**

## 1. Live baseline

Re-query GitHub before implementation.

```text
live main:
eed51bade5163790316a31e8295e2e841df90357

S6D full acceptance checkpoint:
a861c515a73834a4071c4a54bce953eba5c075a6

PR #61:
DRAFT / OPEN / UNMERGED
```

S6D acceptance evidence at `a861c515...`:

- Android FULL unit tests + `assembleDebug`: SUCCESS
- ASP contract: SUCCESS
- Real Clingo cross-validation: SUCCESS
- CI gate: SUCCESS
- R2: SUCCESS
- exact cleanup/diff audit: PASS

Important late-S6D checkpoints:

```text
pair full-domain RED:
a185589e4400ced6983d25603a013f4f36d2e235

pair full-domain GREEN:
977ba44...   # re-fetch exact SHA if needed for audit

pair-domain cleanup:
92f9ce3f53ec0b3b3125b59f459753659745afe4

Spy registration RED:
5b87b91b368c9922dfd6cbf9c8abf1cfecd30439

Spy registration GREEN:
dbd5632...   # re-fetch exact SHA if needed for audit

Spy cleanup:
3450c0f...

S6D full acceptance:
a861c515a73834a4071c4a54bce953eba5c075a6
```

## 2. Product strategy decision — legacy recommendation is maintenance-only

The long-term product target is:

```text
complete legal semantic domain
        ├── Manual / generated clue selection     # permanent safety path
        └── Recommendation Provider
                ├── Legacy recommendation         # temporary fallback only
                └── Cognitive-consistency recommender
                        -> PlayerWorldSet simulation
                        -> epistemic metrics
                        -> Productive Uncertainty
```

From this checkpoint onward, **do not expand legacy recommendation quality or role coverage merely to preserve parity with the old system**.

Legacy recommendation requirements are now limited to:

1. existing supported paths must not produce illegal information;
2. obvious user-visible regressions should be fixed;
3. unsupported/new semantic cases may fall back to manual selection;
4. no new role-specific heuristic engine is required unless it is the smallest compatibility fix needed to close PR #61.

Do not spend S7/S8 effort on:

- exact legacy shortlist parity;
- RecommendationStyle parity across all generic roles;
- legacy distribution tuning;
- new role-specific misinformation heuristics;
- broad legacy Drunk/Poisoned strategy expansion;
- making every future script fully supported by the legacy recommender.

## 3. Permanent architecture that MUST remain strong

The following are not legacy and must remain correct because the cognitive-consistency recommender depends on them:

```text
Composition
-> committed actual identity
-> committed shown identity
-> perceived ability
-> complete legal semantic candidate domain
-> interaction-scoped registration
-> truth / false classification
-> reliability state
-> AbilityObservation / durable visible history
```

Permanent product contract:

> Every supported information role must remain playable through a correct manual/generate-clue path even when no recommendation provider supports the situation.

Recommendation is replaceable. Semantic correctness and manual operation are not.

## 4. S7 — Trouble Brewing controlled production cutover

### MUST complete

- TB production setup uses the generic setup authority rather than a parallel legacy composition engine;
- committed identity remains the single setup identity authority;
- first-night information consumes the accepted generic semantic boundaries;
- legal/manual information selection remains complete and usable;
- durable `AbilityObservation` / history remains compatible;
- legacy recommendation cannot generate an illegal result;
- existing TB production behavior has no major user-visible regression.

### NOT required for S7 exit

- exact legacy recommendation parity;
- exact shortlist ordering/distribution parity;
- full `RecommendationStyle` migration;
- legacy recommender access to every semantic candidate;
- quality tuning of false clues;
- new legacy heuristics that will be replaced by the cognitive-consistency engine.

### Implementation strategy

Audit the TB production setup path and find the smallest remaining parallel authority/divergence between production and:

```text
SetupCandidateSource
GeneratedSetupCandidateSource
TemplateRepository (optional compatibility source)
SetupDiversityHistory / generic selector
CommittedClocktowerSetup
```

Use behavior RED only for a durable/high-risk production contract. Avoid source-shape tests and broad refactors.

If no meaningful production divergence remains, do not invent work: document the audit, run the appropriate acceptance checkpoint, and close S7.

## 5. S8 — NGJ/no-template proof, deliberately minimal

S8 exists to prove the generic architecture works for a second script without TB-specific template/recommendation assumptions.

### MUST complete

- setup can run without TB preset/template dependence;
- role semantics are represented correctly for the chosen S8 acceptance slice;
- complete legal/manual information path exists for supported information interactions;
- generic UI/history contracts remain usable;
- no TB-specific recommendation code is required for correctness.

### OPTIONAL

- reuse legacy recommendation only where existing generic capabilities already support the role naturally.

### NOT required

- new NGJ-specific legacy recommendation heuristics;
- legacy recommendation quality tuning;
- exact TB recommendation experience parity;
- broad support for every NGJ information interaction if a narrower second-script proof is sufficient to establish the generic architecture.

S8 should be reduced to the **smallest convincing second-script/no-template acceptance proof** needed before PR #61 can close.

## 6. PR #61 closeout priority

The priority order is now:

```text
S7 minimal controlled TB cutover
-> S8 smallest credible second-script/no-template proof (only if still needed for MS-SETUP acceptance)
-> full acceptance / exact diff audit
-> user-authorized Ready / merge
-> fresh cognitive-consistency branch
```

Do not let optional legacy recommendation compatibility keep PR #61 open.

## 7. Post-PR main route — cognitive consistency recommender

After PR #61 is accepted and merged, return immediately to:

- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`
- `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`

Target pipeline:

```text
legal semantic candidate
-> recipient PlayerWorldSet BEFORE
-> hypothetical player-visible observation
-> PlayerWorldSet AFTER
-> epistemic / structural / narrative metrics
-> safety and fairness gates
-> Productive Uncertainty ranking
-> recommendation provider
-> AUTO / ASSISTED UI
```

A3 exact enumeration remains the correctness baseline. A4/ZDD remains shadow/prototype until separately validated.

## 8. Scope guards

Do not during PR #61 closeout:

- implement Productive Uncertainty;
- connect A3/A4/ZDD to production recommendation;
- add large new legacy recommendation engines;
- tune false-clue quality;
- start Host/App decomposition;
- broaden persistence/recovery;
- rebase or force-push;
- mark PR #61 Ready or merge without explicit user authorization.

Follow risk-based tests-first: durable behavior REDs for actual risks, not a new test for every internal refactor.

## 9. Resume protocol

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff;
4. re-query live `main`, PR #61, head and checks;
5. treat `a861c515...` as accepted S6D baseline;
6. audit S7 for the smallest real TB production authority divergence;
7. do not expand legacy recommendation unless required for legality or a major regression;
8. close S7 quickly, reduce S8 to the smallest convincing no-template proof, and finish PR #61;
9. after merge, resume cognitive-consistency / Productive Uncertainty work on a fresh branch.
