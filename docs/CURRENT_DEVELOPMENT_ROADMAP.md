# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-01 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation.

## 1. Current development context

```text
live main:
eed51bade5163790316a31e8295e2e841df90357

current branch:
codex/ms-setup-generic-architecture

current Draft PR:
#61 — MS-SETUP: generic multi-script setup architecture
DRAFT / OPEN / UNMERGED

S6D full acceptance checkpoint:
a861c515a73834a4071c4a54bce953eba5c075a6

latest branch head before this roadmap update:
869ca4cdb63f965512a18f4809a909fc06910546
```

PR #61 remains Draft, Open, Unmerged and mergeable. Do not mark Ready or merge without explicit user authorization.

## 2. Current campaign status

```text
MS-S0    ownership audit                                         COMPLETE
MS-S0.5  recovery scope reduction audit                          COMPLETE
MS-S1    CommittedClocktowerSetup + provenance                   COMPLETE / ACCEPTED
MS-S1R   setup persistence authority migration                   COMPLETE / ACCEPTED
MS-S2    candidate/source/provider contracts                     COMPLETE / ACCEPTED
MS-S3    optional TemplateRepository                             COMPLETE / ACCEPTED
MS-S4    deterministic generated actual-role source              COMPLETE / ACCEPTED
MS-S4.5  shown-identity ownership architecture correction       COMPLETE / ACCEPTED
MS-S5    actual-composition diversity/scorer/selector            COMPLETE / ACCEPTED
MS-S6A   shown-identity policy/options boundary                  COMPLETE / ACCEPTED
MS-S6B   deterministic shown-identity commitment                 COMPLETE / ACCEPTED
MS-S6C   generic information semantics + impairment ownership    COMPLETE / ACCEPTED
MS-S6D   first-night perceived-ability semantic completion       COMPLETE / ACCEPTED
MS-S7    TB controlled production cutover                        CURRENT — ACCELERATED CLOSEOUT
MS-S8    NGJ / no-template second-script proof                   QUEUED — MINIMAL ACCEPTANCE ONLY
MS-S9    future-script acceptance                                DEFERRED; NOT REQUIRED FOR PR #61
```

**Project priority:** close PR #61 as soon as the generic architecture has the minimum convincing TB production cutover and second-script/no-template proof needed for acceptance, then return immediately to the cognitive-consistency recommendation campaign.

Active handoff:
`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-01_MS_S7_PR_CLOSEOUT.md`

The former `NEXT_DEVELOPMENT_HANDOFF_2026-09-01_MS_S6D_CLOSEOUT.md` is now historical evidence only.

## 3. S6D is fully accepted

S6D acceptance checkpoint:

```text
a861c515a73834a4071c4a54bce953eba5c075a6
```

Acceptance evidence:

- Android FULL unit tests + `assembleDebug`: SUCCESS
- ASP contract: SUCCESS
- Real Clingo cross-validation: SUCCESS
- CI gate: SUCCESS
- R2: SUCCESS
- exact diff / one-shot cleanup audit: PASS

Late-S6D behavior established:

- Washerwoman / Librarian / Investigator use shared pair semantic truth authority;
- Spy and Recluse interaction-scoped registration semantics are preserved;
- Chef / Empath first-night numeric truth uses shared registration-aware semantics;
- Fortune Teller retains its existing Demon / red-herring / Recluse query semantics;
- actual Drunk remains Drunk while committed shown identity determines perceived ability;
- semantic candidate completeness is separate from visible ASSISTED shortlist curation;
- generic automatic selection can receive the full relevant semantic domain without exposing an unbounded raw list in the UI.

Do not reopen S6D unless a concrete regression is discovered.

## 4. Frozen permanent architecture

The permanent causal order remains:

```text
Composition
-> committed actual identity
-> committed shown identity
-> perceived ability
-> complete healthy legal/truth semantic domain
-> interaction-scoped registration
-> RELIABLE / POISONED / DRUNK reliability state
-> recommendation/manual selection
-> AbilityObservation
-> durable player-visible history
-> UI
```

Permanent invariants:

- Drunk actual identity remains Drunk;
- shown identity is committed once and is not recommendation state;
- Healthy, Poisoned and Drunk of the same perceived role share role semantics before reliability;
- Spy/Recluse registration belongs to semantic truth construction, not role-specific recommendation heuristics;
- semantic legality/truth must not be owned by Host/UI compatibility projection;
- every supported information role must remain playable through a correct manual/generate-clue path even if no recommendation provider supports that situation.

## 5. New product strategy — legacy recommendation is maintenance-only

A deliberate architecture/product decision now changes the remaining MS-SETUP route.

Long-term target:

```text
Complete legal semantic candidate domain
        ├── Manual / generated clue selection        # permanent
        └── Recommendation Provider
                ├── Legacy recommender               # temporary fallback
                └── Cognitive-consistency recommender
                        -> PlayerWorldSet
                        -> epistemic metrics
                        -> Productive Uncertainty
```

The legacy recommendation system is no longer a feature area to expand. It is a temporary compatibility provider.

### Legacy recommendation MUST

1. avoid generating illegal information on existing supported paths;
2. avoid major user-visible regressions before replacement;
3. fail/fall back safely to manual selection when unsupported.

### Legacy recommendation does NOT need

- exact shortlist parity with old behavior;
- exact ranking/distribution parity;
- full `RecommendationStyle` migration;
- access to every legal semantic candidate;
- new role-specific Drunk/Poisoned recommendation strategy;
- new script-specific recommendation heuristics;
- fine-tuned misinformation quality.

Do not spend PR #61 development time making the legacy recommender a complete generic multi-script recommendation engine.

## 6. MS-S7 — TB controlled production cutover

S7 is now intentionally narrow.

### S7 MUST complete

- Trouble Brewing production setup uses the accepted generic setup authority rather than a parallel legacy composition authority;
- `CommittedClocktowerSetup` remains the single committed setup/identity boundary;
- accepted semantic boundaries are used by first-night production information;
- manual/generate-clue operation remains complete and reliable for supported TB information roles;
- durable `AbilityObservation` / history behavior remains compatible;
- legacy recommendation cannot emit illegal information;
- no major user-visible TB production regression is introduced.

### S7 exit does NOT require

- exact old recommendation parity;
- exact shortlist ordering or probability distribution;
- legacy recommendation support for the entire semantic domain;
- complete RecommendationStyle generalization;
- quality tuning of false information;
- Productive Uncertainty or PlayerWorldSet production integration.

### S7 next action

Audit the current TB production setup path against:

```text
SetupCandidateSource
GeneratedSetupCandidateSource
TemplateRepository              # optional compatibility source
SetupDiversityHistory
Generic setup selector
CommittedClocktowerSetup
```

Find the **smallest real remaining parallel authority/divergence**.

- If a durable/high-risk production defect exists: establish one focused behavior RED and repair it.
- If no meaningful divergence remains: document the audit and close S7 rather than inventing compatibility work.

Risk-based tests-first applies. Do not create source-shape tests merely to prove helper/class usage.

## 7. MS-S8 — minimal NGJ / no-template proof

S8 is reduced from a broad second-script compatibility campaign to the smallest convincing proof that the generic architecture is not secretly TB-specific.

### S8 MUST demonstrate

- a second script can obtain legal setup without depending on TB preset/template assumptions;
- the chosen acceptance slice uses generic role/setup semantics rather than TB-specific ownership;
- supported information interactions retain a legal/manual path;
- generic UI/history contracts remain usable;
- correctness does not depend on adding NGJ-specific legacy recommendation heuristics.

### S8 legacy recommendation policy

Existing generic legacy recommendation behavior may be reused where it works naturally, but S8 must **not** add new NGJ-specific legacy recommendation engines simply to obtain recommendation parity.

Unsupported recommendation cases may remain manual-only until the cognitive-consistency recommender replaces legacy recommendation.

### S8 exit philosophy

S8 proves architecture, not completeness of the old recommender.

If a narrow no-template/second-script acceptance slice is sufficient to prove the architecture, do not expand S8 into exhaustive NGJ feature work inside PR #61.

## 8. PR #61 accelerated closeout route

New priority order:

```text
S7 minimal TB controlled cutover
-> S8 smallest credible no-template / second-script proof, if still needed
-> logical full acceptance checkpoint
-> exact remote diff / temporary-file audit
-> user authorization
-> mark Ready / merge
-> fresh cognitive-consistency branch
```

Do not let optional legacy recommendation parity keep this PR open.

A separate S9/future-script campaign can validate broader multi-script coverage later if needed; it is not a prerequisite for returning to the cognitive-consistency algorithm.

## 9. Immediate post-PR route — cognitive consistency

After PR #61 is merged, the primary development route returns immediately to:

- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`
- `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`

Correctness foundation:

```text
semantic candidate
-> recipient PlayerWorldSet BEFORE
-> hypothetical player-visible observation
-> PlayerWorldSet AFTER
-> epistemic metrics
```

Quality extension:

```text
legal misinformation candidate
-> project into player-visible history
-> enumerate / represent surviving perceived worlds
-> credibility / ambiguity / persistence metrics
-> cross-role interaction and confirmation-lock risk
-> breakability / discovery paths
-> faction impact / player agency gates
-> Productive Uncertainty ranking
-> Recommendation Provider
-> AUTO / ASSISTED UI
```

The objective is not merely to make false information "wrong". It is to create a credible, sustainable, interactive, eventually breakable and fair mistaken world.

A3 exact enumeration remains the correctness baseline. A4/ZDD remains shadow/prototype until separately validated. Approximation failure must never become false UNSAT.

## 10. Testing strategy for remaining PR #61 work

Use risk-based tests-first, not mechanical test-first for every edit.

For S7/S8:

- behavior REDs only for durable/high-risk contracts or confirmed defects;
- focused tests for each migration increment;
- no broad legacy recommendation parity fixture expansion;
- no source-shape tests as substitutes for behavior;
- full CI only at a logical acceptance checkpoint or when risk routing requires it;
- exact diff audit before closeout.

The purpose of S7/S8 testing is to prove generic authority and product safety, not freeze disposable recommendation heuristics.

## 11. Scope guards

Do not during PR #61 closeout:

- implement Productive Uncertainty;
- connect A3/A4/ZDD to production recommendation;
- expand legacy recommendation quality or script-specific heuristics without a concrete legality/regression need;
- change accepted S6D semantic authority without concrete evidence;
- begin broad future-script support beyond the minimum S8 proof;
- begin Host/App decomposition;
- broaden persistence/recovery;
- rebase or force-push;
- mark PR #61 Ready or merge without explicit user authorization.

## 12. Documentation authority

Current active set:

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-01_MS_S7_PR_CLOSEOUT.md
docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md
docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

Historical S6D audit/handoff and earlier checkpoint docs are evidence only, not current execution instructions.

## 13. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap;
3. read `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-01_MS_S7_PR_CLOSEOUT.md`;
4. re-query live `main`, PR #61, branch head and checks;
5. treat `a861c515a73834a4071c4a54bce953eba5c075a6` as accepted S6D baseline;
6. continue S7 by auditing the smallest remaining TB production setup authority divergence;
7. do not expand legacy recommendation except to prevent illegal behavior or a major regression;
8. close S7 quickly;
9. reduce S8 to the smallest convincing no-template/second-script proof;
10. finish PR #61, then resume the cognitive-consistency / Productive Uncertainty campaign on a fresh branch.

## 14. Deferred / queued registry

| Area | Status |
|---|---|
| MS-SETUP / PR #61 | CURRENT — ACCELERATED CLOSEOUT |
| MS-S6D semantic completion | COMPLETE / ACCEPTED at `a861c515...` |
| MS-S7 TB controlled cutover | CURRENT — MINIMAL REQUIRED SCOPE |
| MS-S8 NGJ/no-template proof | QUEUED — MINIMAL SECOND-SCRIPT PROOF |
| MS-S9 broad future-script acceptance | DEFERRED / NOT PR #61 BLOCKER |
| Legacy recommendation enhancement | MAINTENANCE-ONLY / NO NEW BROAD INVESTMENT |
| EPI-MQ Productive Uncertainty | NEXT PRIMARY CAMPAIGN AFTER PR #61 |
| ALG cognitive-consistency / PlayerWorldSet | NEXT PRIMARY CAMPAIGN AFTER PR #61 |
| A4/ZDD production rollout | SHADOW / FUTURE AFTER EXACT BASELINE GATES |
| REC-R1 | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid wake-history authority | DEFERRED FOLLOW-UP |
