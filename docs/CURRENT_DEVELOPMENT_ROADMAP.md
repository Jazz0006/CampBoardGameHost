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

latest code-bearing branch head before this docs update:
f1d85e54e9c42d7cbf983908bcca1de87a03797c

latest pair semantic product GREEN:
7d5d2639dff259845ec6f971f74b36a3b2b580cd

latest fully accepted first-night numeric production checkpoint:
2774c119872372f00522131d9133ccdfbd6d8348
```

The one-shot pair semantic workflow at `8ed52b17...` completed successfully and pushed `7d5d263...`; its focused production behavior test, pair regressions, and full `:app:testDebugUnitTest` all passed before the product commit. Temporary one-shot files were then removed by `f1d85e5...`.

`2774c119...` remains the latest **full acceptance** checkpoint for the first-night numeric production integration: Android FULL, ASP contract, Real Clingo, CI gate, and R2 all succeeded. `7d5d263...` is a later validated product GREEN but has not yet received the final S6D full-acceptance checkpoint.

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
MS-S6D   first-night perceived-ability semantic completion       IN PROGRESS — CLOSEOUT
  S6D-0 six-role audit                                           COMPLETE
  S6D-1 durable behavior REDs                                    COMPLETE
  S6D-2 perceived-ability semantic seams                         COMPLETE
  S6D-3 pair truth semantics: WW/Librarian/Investigator          COMPLETE
  S6D-4 numeric truth semantics: Chef/Empath                     COMPLETE / PRODUCTION-WIRED
  S6D-5 Fortune Teller query semantics audit                     COMPLETE — NO FLOW CHANGE REQUIRED
  S6D-6 production consistency / authority cleanup               IN PROGRESS
  S6D-7 full acceptance + checkpoint                             PENDING
MS-S7    TB controlled semantic cutover                          BLOCKED UNTIL S6D ACCEPTED
```

Current next task inside S6D-6:

> Prove and repair the remaining pair-family **pre-culling** gap: the generic reliability selector must receive the complete relevant healthy semantic domain before recommendation ranking. Do this without accidentally exposing an enormous raw candidate list in ASSISTED UI.

Active handoff:
`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-01_MS_S6D_CLOSEOUT.md`

Future misinformation-quality design:
`docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`

Historical S6D-0 audit evidence:
`docs/MS_S6D_FIRST_NIGHT_PERCEIVED_ABILITY_AUDIT_2026-09-01.md`

## 3. Frozen causal architecture

The setup/information order remains:

```text
Composition
-> Identity
-> Information
```

For first-night information, S6D freezes the semantic order as:

```text
actual identity
-> committed shown identity
-> perceived ability role
-> role semantic evaluator
-> complete healthy legal/truth semantic space
-> RELIABLE / POISONED / DRUNK reliability policy
-> generic selector
-> AbilityObservation
-> UI projection
```

Rules:

- Drunk actual identity remains Drunk.
- shown identity is committed once; recommendation may never choose, reroll, or mutate it.
- Healthy, Poisoned and Drunk of the same perceived role share the same role semantics before impairment.
- Spy/Recluse registration belongs to semantic truth construction, before reliability.
- reliability chooses truthful/false family and recommendation policy; it does not redefine role truth.
- production must not contain a second raw-actual-role truth authority after the shared semantic evaluator.
- semantic-domain completeness and visible UI shortlist are separate concerns: the generic selector may require the full semantic domain even when ASSISTED presentation should remain curated.

## 4. S6D completed evidence

### Pair family

The behavior RED at `df5bdfe...` established two production contracts:

1. actual Drunk shown Librarian can truthfully receive `No Outsiders` when the only actual Outsider is the source Drunk itself;
2. actual Drunk shown Investigator preserves Recluse-as-Min\-ion registration truth and its registration metadata.

`7d5d263...` GREEN routes production pair truth through `NaturalPairInformationCandidateGenerator.generateHealthyInformationSpace(...)` via `projectFirstNightPairInformationOptions(...)` before the legacy recommendation scoring layer. The old Host-local `actualRole` truth check is no longer the pre-scoring semantic authority.

### Numeric family

Chef/Empath first-night production now projects the registration-aware truthful value/family before reliability. Healthy/Poisoned/Drunk share the same role semantic truth space. The accepted full checkpoint is `2774c119...`.

### Fortune Teller

The six-role production consistency audit found no equivalent S6D behavior gap requiring a Fortune Teller flow rewrite. Its first-night truth remains a chosen-two-player query over Demon/red-herring/Recluse semantics, with reliability applied after the query result. Preserve this boundary and do not broaden S6D into later-night Fortune Teller work.

## 5. Remaining S6D-6 gap — full semantic domain before selector

`RegistrationPolicy.recommendPair(candidates)` currently pre-culls pair candidates before the generic first-night selector:

- at most one candidate per recommendation style;
- plus at most one truthful fallback when needed.

That means the shared role evaluator can classify truth correctly while the downstream generic selector still receives only a legacy shortlist instead of the complete relevant healthy legal/truth family.

This is a correctness/authority issue, not yet a request to improve misinformation quality.

Required implementation discipline:

1. create a **behavior RED** proving multiple legal healthy semantic outcomes survive to the generic selection boundary;
2. do not assert source strings, helper calls, class names, exact internal constants, or that `recommendPair` is absent;
3. separate semantic candidate authority from presentation shortlist if needed;
4. preserve existing ASSISTED UX unless a separate behavior requirement authorizes expansion;
5. preserve later-night behavior;
6. after GREEN, run focused pair/numeric/FT regressions and S6D full acceptance.

## 6. Misinformation quality is deliberately deferred

Current S6D answers:

> **What information is semantically legal/true/false for this perceived ability?**

It does **not** answer:

> **Among legal false information, which choice creates the best misleading but fair player world?**

The latter is now a separate post-PR epistemic/cognitive-consistency campaign. Its design authority is:
`docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`.

The key future idea is to score the *world created by misinformation*, not merely how far the answer is from current truth. Credibility, persistence, breakability, cross-role interaction, social impact, productive uncertainty, anti-Drunk-exposure, anti-confirmation-lock, narrative value, and player agency/fairness all belong there.

Do not implement those policies in PR #61.

## 7. Post-PR epistemic route

After the current MS-SETUP PR is accepted and merged, resume the player-cognition route on a fresh branch.

The existing v2.2 direction remains the correctness foundation:

```text
semantic candidate
-> recipient PlayerWorldSet BEFORE
-> hypothetical observation
-> PlayerWorldSet AFTER
-> epistemic metrics
```

The new misinformation-quality stage extends it:

```text
candidate misinformation
-> project candidate into player-visible history
-> enumerate / represent surviving perceived worlds
-> measure credibility + ambiguity + persistence
-> measure interaction + confirmation-lock risk
-> estimate later breakability / discovery paths
-> evaluate faction impact and player agency
-> rank Productive Uncertainty
-> storyteller policy / generic selector
```

A3 exact enumeration remains the correctness baseline. A4/ZDD remains shadow/prototype until separately validated. Do not let approximation failure become false UNSAT.

## 8. S6D-7 acceptance gate

Before S6D may be called complete:

1. six first-night B2 families have one explicit semantic owner/disposition;
2. actual Drunk + committed shownRole reaches the shown role semantic space without `actualRole==shownRole` shortcuts;
3. registration is resolved before reliability;
4. pair semantic domain reaches the generic selector without legacy pre-culling authority;
5. semantic pool and ASSISTED presentation do not accidentally collapse into one unbounded UI list;
6. committed shown identity remains immutable;
7. focused behavior suites pass;
8. Android FULL, ASP contract, Real Clingo, CI gate and R2 pass at a dedicated logical checkpoint;
9. exact remote diff audit passes.

Only then may S7 be considered. Do not start S7 before this checkpoint.

## 9. Scope guards

Do not in the current S6D closeout:

- implement Productive Uncertainty / misinformation-world quality;
- introduce A3/A4 ranking into production;
- change the accepted 90/10 impairment-family product policy;
- create role-specific Drunk/Poisoned strategy engines;
- change later-night Empath authority;
- rewrite Fortune Teller flow without a concrete S6D behavior defect;
- start S7/S8/REC-R1;
- begin Host/App decomposition;
- broaden persistence/recovery;
- merge or mark PR #61 Ready without explicit user authorization;
- rebase or force-push.

## 10. Documentation authority

Current active set:

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-01_MS_S6D_CLOSEOUT.md
docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md
docs/MS_S6D_FIRST_NIGHT_PERCEIVED_ABILITY_AUDIT_2026-09-01.md
docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

Historical S6C and earlier checkpoint docs are evidence only; they are not current execution instructions.

## 11. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap;
3. read the active S6D closeout handoff;
4. re-query live main / PR #61 / branch head / checks;
5. treat `7d5d263...` as the latest pair product GREEN and `2774c119...` as the latest full-acceptance first-night numeric checkpoint;
6. continue **S6D-6 pair semantic-domain pre-culling behavior RED**, not S7;
7. keep misinformation-quality work deferred to the post-PR epistemic campaign;
8. keep PR #61 Draft and unmerged.

## 12. Deferred / queued registry

| Area | Status |
|---|---|
| MS-SETUP | CURRENT — S6D CLOSEOUT |
| MS-S6D pair truth semantics | GREEN at `7d5d263...` |
| MS-S6D pair full semantic-domain boundary | NEXT |
| MS-S6D-7 full acceptance | PENDING |
| MS-S7 TB controlled semantic cutover | BLOCKED ON S6D |
| EPI-MQ Productive Uncertainty misinformation quality | POST-PR / DESIGNED, NOT IMPLEMENTED |
| ALG-B2R first-night Epistemic Gate | POST-PR COGNITIVE-CONSISTENCY CAMPAIGN |
| MS-S8 NGJ/no-template cutover | QUEUED |
| MS-S9 future-script acceptance | QUEUED |
| C9 Unified Selector Production Rollout | FUTURE AFTER EPISTEMIC QUALITY GATES |
| REC-R1 | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid wake-history authority | DEFERRED FOLLOW-UP |
