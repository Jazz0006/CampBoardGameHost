# NEXT DEVELOPMENT HANDOFF — MS-S6D Closeout

> Updated: 2026-09-01 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **S6D LATE CLOSEOUT — PAIR TRUTH GREEN — FULL SEMANTIC-DOMAIN RED NEXT — S7 BLOCKED**

## 1. Live state and checkpoints

Re-query GitHub before implementation. At this handoff update:

```text
live main:
eed51bade5163790316a31e8295e2e841df90357

branch before docs-only update:
f1d85e54e9c42d7cbf983908bcca1de87a03797c

PR #61:
DRAFT / OPEN / UNMERGED
```

Important checkpoints:

```text
S6C full acceptance:
38a04c1353c883c3bda4b4a506085c3c1d2766bd

first-night numeric full acceptance:
2774c119872372f00522131d9133ccdfbd6d8348

pair production behavior RED:
df5bdfe9c6dbe91c39bf61555520a9c01f1fb0b2

pair truth semantic GREEN:
7d5d2639dff259845ec6f971f74b36a3b2b580cd

one-shot cleanup:
f1d85e54e9c42d7cbf983908bcca1de87a03797c
```

The pair one-shot run completed successfully. It ran:

1. `FirstNightPairInformationProductionSemanticsTest`;
2. `NaturalPairInformationCandidateGeneratorTest` + `PairInformationRecommenderTest`;
3. full `:app:testDebugUnitTest`;
4. `git diff --check`;
5. product commit + push;
6. temporary script/workflow cleanup + push.

All three Gradle invocations were GREEN before `7d5d263...` was committed.

## 2. Frozen S6D architecture

```text
actual identity
-> committed shown identity
-> perceived ability role
-> role semantic evaluator
-> complete healthy legal/truth semantic space
-> RELIABLE / POISONED / DRUNK
-> generic reliability policy
-> generic selector
-> AbilityObservation
-> UI
```

Non-negotiable:

- actual Drunk remains Drunk;
- shown identity is committed setup state and immutable to recommendation;
- Healthy/Poisoned/Drunk share the same role semantics before reliability;
- Spy/Recluse registration is semantic input, not an impairment special case;
- Host/UI compatibility projection cannot become a second truth authority;
- later epistemic-quality ranking is outside PR #61.

## 3. What is now complete

### 3.1 Pair truth classification

`projectFirstNightPairInformationOptions(...)` now projects Washerwoman/Librarian/Investigator visible clues against `NaturalPairInformationCandidateGenerator.generateHealthyInformationSpace(...)`.

The current GREEN specifically proves:

- actual Drunk shown Librarian can correctly treat `No Outsiders` as semantic truth when the source Drunk is the only actual Outsider and therefore cannot be the Librarian's own target information;
- actual Drunk shown Investigator preserves Recluse registration truth and the registered Minion metadata;
- later-night `ClocktowerPhase.Night` behavior remains outside this first-night projector.

Production no longer classifies unreliable pair truth merely by checking raw actual roles in the displayed seats.

### 3.2 Numeric family

Chef/Empath first-night production uses registration-aware semantic truth families before reliability. The structured Empath audit/commit path consumes the same projected truth authority.

### 3.3 Fortune Teller

Audit found no corresponding first-night S6D semantic split that justifies changing the Fortune Teller flow. Preserve its existing selected-two-player query, red herring, Demon/current-state and Recluse registration semantics. Do not change later-night authority.

## 4. Current remaining defect — NEXT

The remaining S6D defect is **not** false-information quality. It is candidate authority/order.

`RegistrationPolicy.recommendPair(candidates)` currently pre-culls pair candidates before the generic first-night selector. It keeps approximately one candidate per recommendation style plus a truthful fallback.

Therefore the production path is still effectively:

```text
complete pair effects
-> shared semantic truth classification
-> legacy recommendPair shortlist       <-- remaining gap
-> generic first-night selection
```

Target:

```text
complete relevant pair semantic domain
-> shared semantic truth classification
-> generic reliability policy / selector
-> presentation shortlist/projection as needed
```

The generic selector must not be starved of valid semantic alternatives by a legacy recommender upstream.

## 5. Next RED — behavior only

Create one focused production-level behavior test proving that, for an impaired first-night pair-information actor, **multiple distinct healthy-semantic alternatives that are legal for the perceived role remain available at the generic selection boundary**.

The test must verify durable behavior, not implementation shape.

Good fixture characteristics:

- use actual Drunk or Poisoned pair role so generic impairment path is exercised;
- use a setup with more than one semantically meaningful legal pair outcome;
- include registration-sensitive truth only if needed to distinguish semantic authority;
- assert that a candidate that old `recommendPair` would discard can still participate in generic selection / resulting pool;
- keep later-night behavior unchanged.

Do **not** assert:

- source text;
- helper/class existence;
- that `recommendPair` is or is not called;
- exact style constants;
- exact list ordering unrelated to behavior.

## 6. GREEN design constraint — semantic pool != presentation list

Do not solve the RED by blindly sending every generated pair effect to the current ASSISTED UI.

`UnifiedSelectionPool.candidatesFor(ASSISTED)` exposes all legal, verified, non-rejected candidates. A full pair domain may therefore create a very large visible list.

Preferred separation:

```text
SemanticCandidateDomain
  complete enough for reliability + generic selection

PresentationRecommendations
  curated/small enough for ASSISTED UI
```

AUTO/reliability selection must evaluate the full relevant semantic domain. ASSISTED presentation may remain curated, provided that curation is explicitly downstream from semantic authority and cannot change truth/legal semantics.

If the smallest safe implementation needs a new projection seam, keep it generic and first-night scoped. Do not create role-specific Drunk/Poisoned engines.

## 7. Validation after GREEN

Minimum focused evidence:

- new pair semantic-domain production behavior test;
- `FirstNightPairInformationProductionSemanticsTest`;
- `NaturalPairInformationCandidateGeneratorTest`;
- `PairInformationRecommenderTest`;
- relevant `UnifiedSelectionPool` / first-night migration tests;
- numeric production regressions;
- Fortune Teller phase-authority regression if touched indirectly.

Then run the risk-router logical checkpoint (`:app:testFast` if appropriate). At S6D-7 run full acceptance:

```text
Android FULL + assemble
ASP contract
Real Clingo
CI gate
R2
exact remote diff audit
```

If a bot-authored cleanup/doc commit is classified as `action_required` or skips meaningful lanes, create a deliberate `[full-ci]` logical checkpoint after the code tree is finalized, as previously done. Do not interpret a classifier skip as product GREEN evidence.

## 8. Explicitly deferred misinformation-quality work

The user-defined false-world principles are important but deliberately **not implemented in S6D**.

Future design:
`docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`

That later campaign asks:

> Given several semantically legal false answers, which one creates a credible, sustainable, interactive, eventually breakable and fair mistaken world for the player?

S6D only establishes the correct semantic candidate/truth foundation needed before that question can be answered safely.

## 9. Current stage order

```text
S6D-6a pair truth authority correction                         COMPLETE
S6D-6b full pair semantic-domain before generic selector       NEXT
S6D-6c six-family production consistency audit                 AFTER GREEN
S6D-7  full acceptance + checkpoint                            AFTER AUDIT
S7      BLOCKED UNTIL S6D ACCEPTED
```

After current PR #61 is eventually accepted and merged, misinformation quality / Productive Uncertainty moves to a fresh cognitive-consistency branch/PR. Do not stack it onto #61.

## 10. Scope guards

Do not:

- merge or mark PR #61 Ready without explicit user authorization;
- rebase or force-push;
- start S7 before S6D-7 acceptance;
- implement Productive Uncertainty now;
- change the accepted 90/10 family policy;
- change later-night Empath authority;
- rewrite Fortune Teller flow without concrete behavior evidence;
- start A3/A4/ZDD production rollout;
- start Host/App decomposition;
- start S8 NGJ;
- broaden persistence/recovery.

Follow risk-based tests-first from `AGENTS.md`: behavior REDs for durable/high-risk contracts, not tests for every internal refactor.

## 11. Resume protocol

1. read `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff;
4. optionally read the S6D-0 audit for provenance, not current instructions;
5. re-query live `main`, PR #61, branch head and checks;
6. inspect `RegistrationPolicy.recommendPair`, `UnifiedSelectionPool`, `unifiedFirstNightInformationPool`, and the production pair call path;
7. design the smallest behavior RED for full semantic-domain preservation;
8. keep presentation curation separate from semantic authority;
9. remain in S6D until full acceptance.
