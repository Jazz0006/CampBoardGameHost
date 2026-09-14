# EXPERIENCED NIGHT FLOW — S2 Full-Screen Surface Totality Audit

> Date: 2026-09-14 Australia/Sydney  
> Status: **CURRENT — read-only audit substantially advanced; production S2 not yet modified**  
> Program: `EXPERIENCED-NIGHT-FLOW-1`  
> Branch: `codex/experienced-night-flow-correctness`  
> PR: `#123` — draft / do not merge yet  
> Parent handoff: `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-14_EXPERIENCED_NIGHT_FLOW_CORRECTNESS.md`  
> Purpose of this document: authoritative S2 audit addendum and Work/local-worktree continuation point.

## 0. Read this first in Work

Before changing production code, read in this order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-14_EXPERIENCED_NIGHT_FLOW_CORRECTNESS.md`;
5. **this document**;
6. the current SNE-7 authoritative night-transaction document;
7. current UI-NAV-1 closeout/reference document;
8. `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`.

Then reconfirm live:

- `main`;
- `codex/experienced-night-flow-correctness`;
- PR #123 head/state/checks;
- latest production-code checkpoint vs docs-only head.

Do not assume the SHAs below remain live after this document is committed.

## 1. Live state at the point this audit was handed to Work

Immediately before this document was created:

```text
main
940ba1df68365974ba366985d66d1cb583682ba7
Merge PR #122 — UI: mark dead players clearly on square table

branch before this docs commit
codex/experienced-night-flow-correctness
d00fe2f7ac50e1d76ceb2525de9e50b8fe628f46

draft PR
#123 — Fix experienced night-flow progression ownership
state: open
draft: true
base: main
```

S1 production checkpoint remains:

```text
7796ba13012ec12801abba3c75a7a61b8706cd28
fix: keep dynamic night cursor renderable
```

S1 cleanup head before the previous handoff update:

```text
99c47bed6180839c0756b286540933cea90cc3b9
```

S1 typed RED:

```text
52299383dfe9d2f2f6eef9718179a73234248a45
test: expose invalid dynamic night cursor
```

No S2 production change has been made yet. The S2 findings below are audit findings, not an implementation checkpoint.

## 2. Campaign invariant

The campaign-level invariant is unchanged:

> **After every legal night confirmation, the application must resolve to exactly one valid renderable next night step, or an explicit Night → Dawn completion. There is no third out-of-range cursor, blank surface, ownerless UI state, or competing full-screen owner.**

S1 made the cursor portion executable and fixed the known out-of-range dynamic advance defect.

S2 now focuses on the surface portion:

> **Every production-valid materialized night step that claims full-screen ownership must resolve to exactly one concrete render surface in both Beginner and Experienced modes, or explicitly resolve to a valid legacy-inline fallback. Zero concrete surfaces and more than one competing concrete surface are both defects.**

The important words are **production-valid**, **exactly one**, and **authoritative owner**. Do not manufacture a RED from an impossible synthetic `ClocktowerNightStepUi` shape.

## 3. Why Work/local worktree is the preferred continuation environment

This S2 audit is now dominated by broad cross-file reachability work rather than one isolated edit. The path repeatedly crosses:

```text
canonical interaction / flow fact
-> interaction projector
-> materializer registry
-> role/action-specific materializer or information builder
-> ClocktowerNightStepUi shape
-> full-screen ownership classification
-> specialized predicate / nullable presentation helper
-> concrete Compose renderer
-> automatic effect / navigation
-> recovery / restored checkpoint reconstruction
```

A local worktree materially improves this work because it allows cheap whole-repository operations such as:

- `rg` over every producer/consumer;
- `git log -S`, `git log -G`, `git blame`, and commit-range diffing;
- quick inspection of adjacent definitions without repeated remote fetches;
- full test discovery and focused Gradle execution;
- exact file-size checks and local diffs;
- building an exhaustive producer → surface matrix before changing code.

Using Work does **not** relax architecture/test rules. `AGENTS.md` remains normative. The reason local execution is appropriate here is that the work genuinely requires a complete worktree and strongly coupled cross-file audit, which is one of the repository's valid local-execution cases.

## 4. S1 result that S2 must preserve

S1 identified the original Experienced-mode Demon-kill black-screen report as a general navigation ownership bug, not a DemonKill-special-case UI defect.

The old dynamic path could effectively produce:

```text
currentStepIndex == last valid index
confirmation may dynamically append more steps
-> MoveTo(currentStepIndex + 1)
-> currentStepIndex == currentStepCount
-> stored/current cursor is not renderable
-> renderer elsewhere clamps/coerces
-> delayed effect later decides whether flow expanded or should complete
```

This split the upstream navigation owner from the render owner and allowed the renderer to hide an invalid state.

S1 replaced that sentinel approach with a typed transient directive:

```kotlin
internal sealed interface ClocktowerNightAdvanceDirective {
    data class MoveTo(val stepIndex: Int) : ClocktowerNightAdvanceDirective
    data class AwaitRefreshedFlow(val currentStepIndex: Int) : ClocktowerNightAdvanceDirective
    data object CompleteNight : ClocktowerNightAdvanceDirective
}
```

Stable S1 behavior:

```text
current valid step
+ confirmation may expand flow
-> AwaitRefreshedFlow(current valid index)

refreshed flow contains a real next step
-> MoveTo(actual valid next index)

refreshed flow contains no next step
-> CompleteNight
```

Do not reintroduce an invalid index as a transient navigation sentinel during S2/S3.

## 5. Current S2 architecture map

### 5.1 Canonical production chain

The audited production chain is broadly:

```text
ClocktowerHostInteraction / canonical interaction projection
-> ClocktowerNightStepMaterializerRegistry
-> ClocktowerNightStepUi
-> clocktowerNightUsesFullScreenHostSurface(...)
-> many independent role/action-specific `uses...SquareTable` predicates
   and nullable presentation helpers
-> independent Compose rendering branches
```

`ClocktowerHostInteractionProjector` owns which host interactions exist and their ordering. It does **not** own the final concrete surface.

`ClocktowerNightStepMaterializerRegistry` maps stable interaction IDs to lazily built `ClocktowerNightStepUi` instances and has important production filtering/validation behavior:

- SYSTEM_BOUNDARY interactions are filtered;
- missing materializers fail fast;
- materializer/projected interaction IDs are expected to be unique;
- in OTHER_NIGHT, `ROLE_PHASE_ACTION` with `step.actor == null` is filtered.

Therefore an arbitrary actor-null Other Night role-action object is not a valid production RED candidate.

### 5.2 `ClocktowerNightStepUi` is a bag of fields, not a surface sum type

`ClocktowerNightStepUi` currently carries a broad set of loosely coupled fields including, among others:

- actor / title / explanatory text;
- `isRealAction`;
- action enum;
- display kind;
- display text/payload;
- legal/manual candidates;
- decision/result options;
- reliability state;
- numeric ranges/results;
- registration/result fields.

The representation allows semantically incomplete or overlapping combinations. Nothing in the type itself guarantees that a full-screen claim maps to one and only one concrete renderer.

This is a central architectural weakness in S2.

### 5.3 Boolean full-screen ownership loses information

The current `clocktowerNightUsesFullScreenHostSurface(...)` contract is Boolean. It can answer:

```text
this step claims full-screen ownership = true
```

but cannot answer:

```text
which concrete surface is the sole authoritative owner?
```

The render path then independently re-derives the answer with multiple predicates and nullable presentation objects.

This is information-lossy ownership.

A particularly important audited behavior is that a real action with `action == None` can still claim the full-screen Host surface. Thus an ordinary real information step can suppress legacy Host/header rendering before the renderer has proven that one specialized or generic information surface exists.

There is no general outer Host guarantee equivalent to:

```text
full-screen claimed
+ no specialized renderer matched
-> render safe generic fallback
```

Therefore zero-surface states are architecturally representable even if many current producers happen not to generate them.

### 5.4 Independent render branches permit both zero and multiple owners

`ClocktowerNightStepCardLocalized` currently derives several independent booleans/presentations, including families such as:

- Clockmaker;
- Fortune Teller;
- Undertaker;
- Chef;
- Empath;
- Ravenkeeper;
- Spy;
- pair-information;
- Sage;
- evil information;
- action/single-target/ruling surfaces;
- generic plain information.

The branches are not one exhaustive `when` over a typed surface plan. This means the architecture does not statically prevent:

```text
0 matching concrete renderers
```

or:

```text
2+ matching independent renderers
```

for a shape that already claimed full-screen ownership.

The S2 RED must prove a **production-reachable** instance of this invariant gap, or introduce a typed planner because that planner is the correct ownership boundary and then characterize current production shapes through it. Do not add a test-only abstraction.

## 6. Information-step builder findings

The audited information builder behavior is important for reachability.

Role display-kind resolution includes patterns such as:

```text
Chef / Empath / Clockmaker / Chambermaid -> Number
Fortune Teller -> YesNo
Ravenkeeper / Undertaker -> RoleReveal
Washerwoman / Librarian / Investigator -> EitherOne
otherwise -> supplied/default kind
```

The final display kind is effectively guarded by whether a reliable display payload can be directly shown. In particular, unreliable/manual-information states can deliberately carry:

```text
displayKind == None
```

while still carrying manual candidates/options that are expected to drive a full-screen selection/information surface.

Therefore:

> `displayKind == None` does **not** mean "legacy/no surface".

A generic fallback that keys only on non-`None` display kind is not sufficient to prove totality.

### 6.1 High-priority unresolved pair-information producer question

For Washerwoman / Librarian / Investigator-style pair information, the audit reached an important producer detail:

> `manualInformationCandidates` are derived from `legalSelectionOptions`; recommendation/legacy candidate data does not automatically repopulate this field.

This creates the current highest-priority reachability question:

```text
Can a production-valid unreliable/manual pair-information step have:
    isRealAction == true
    action == None
    displayKind == None
    legalSelectionOptions == empty
?
```

If yes, the likely consequence is:

```text
full-screen claim == true
pair-information specialized surface == false
plain-information fallback == false
legacy Host surface suppressed
=> zero concrete surface
```

Do **not** call this a confirmed defect until the local Work audit proves the producer can create that state. This was the exact S2 audit breakpoint when the previous Chat execution capacity ended.

## 7. Findings already cleared / downgraded

The following earlier suspicions were traced far enough to remove them as the leading S2 zero-surface defect candidates. Keep regression coverage where useful, but do not waste time reproving them first.

### 7.1 New Demon identity — currently has a concrete owner

`NewDemonIdentity` looked suspicious because it participates in full-screen ownership without an immediately obvious literal renderer branch.

The audit traced it through the evil-information square-table presentation path. Current production routing provides a concrete full-screen surface for real New Demon identity information.

Historical evidence shows this area has regressed before, so it remains useful as regression coverage, but it is not the current leading zero-surface defect.

### 7.2 Plain information — total once its predicate is reached

The generic plain-information square-table UI creates a concrete navigable full-screen scaffold/dialog once the plain-information predicate matches.

The risk is not that the plain renderer itself disappears; the risk is a legal shape that claims full-screen but fails both specialized predicates and the plain predicate.

### 7.3 Evil information — concrete surface exists

Minion/Demon/New-Demon evil-information paths have a concrete full-screen presentation when their production predicate is satisfied.

### 7.4 Spy — concrete surface exists

The Spy presentation path has a concrete full-screen surface once its predicate matches.

### 7.5 Ravenkeeper empty result — not a zero-surface defect

The dedicated Ravenkeeper square-table UI remains renderable even when result content is unavailable and can display a no-result/unavailable state rather than disappearing.

An earlier suspicion that Ravenkeeper might produce no surface because a result is empty was therefore rejected.

A separate early concern about simultaneous dedicated Ravenkeeper and generic single-target render ownership was also traced/downgraded; do not treat it as a confirmed production multi-surface defect without new evidence from the local exhaustive matrix.

### 7.6 Red Herring empty candidate set — not a zero-surface defect

The Red Herring single-target presentation initially looked vulnerable when candidate options were empty.

The audit found that the real NightStep UI's selection enablement is derived from `step.isRealAction`, not from candidate-list non-emptiness. A legal real Red Herring step therefore still obtains the concrete single-target square-table surface even with no selectable seats.

This may still merit separate UX/rules-edge review, but it is not the S2 blank-surface RED.

## 8. Specialist result-family audit — partially complete, reachability still required

Several role-specific result surfaces use predicates that also depend on non-empty result/decision choices or equivalent payloads. Families requiring final local proof include at least:

- Clockmaker;
- Chef;
- Empath;
- Sage;
- Undertaker;
- pair information;
- Fortune Teller;
- Chambermaid / numeric information paths where applicable.

For each family, answer all of the following from the production producer, not by constructing arbitrary UI objects:

1. What exact canonical interaction/flow fact produces the step?
2. Which materializer/builder fields are guaranteed?
3. Can required result choices/candidates be empty in a legal state?
4. If specialized predicate fails, which exact fallback is supposed to render?
5. Does full-screen ownership remain true when that predicate fails?
6. Is the state fresh-only, restore-only, both, or impossible?
7. Does Beginner automation bypass the shape while Experienced exposes it?

Classify each row as:

- **proven total**;
- **production-reachable zero-surface defect**;
- **production-reachable multi-surface defect**;
- **defensive gap only / currently unreachable**;
- **synthetic impossible shape**;
- **intentional non-rendering automatic transition** — only if the architecture explicitly owns that state and cannot get stuck.

## 9. Automatic Mayor redirect / Demon succession — unresolved ownership smell

`clocktowerNightRulingPresentation(...)` intentionally returns no manual ruling presentation when the ruling is automatic.

The NightStep UI then uses an effect-driven path for automatic Mayor redirect / Demon successor selection and calls forward navigation after deriving an automatic decision target.

This creates an ownership smell:

```text
step claims full-screen ownership
manual concrete ruling presentation == null
Compose effect is expected to derive target + advance
```

At minimum, this is a transient state with no concrete manual surface. It becomes a real blank/stuck defect if automatic target derivation can fail or become inconsistent after restore/refresh.

Known production gating found during audit:

- Mayor redirect interaction is tied to canonical `MAYOR_REDIRECT_ELIGIBLE` facts;
- Demon succession is tied to `DEMON_SUCCESSION_REQUIRED` and expected legal successor candidates.

The local audit must inspect `TemporaryAutomaticStorytellerPolicy` and its callers to prove whether automatic selection is total under those canonical gating facts.

Questions to close:

### Mayor

- Under every production `MAYOR_REDIRECT_ELIGIBLE` state, does an automatic redirect target always exist in Beginner/automatic mode?
- What happens when no other living Townsfolk exists?
- Can restored/stale selection or changed living state make the automatic resolver return null?
- If resolver returns null, does the UI remain ownerless indefinitely?

### Demon succession

- Does `DEMON_SUCCESSION_REQUIRED` guarantee non-empty legal successor seats?
- Can recovery reconstruct the interaction before the legal successor domain is restored?
- Can automatic policy return null despite the canonical fact being present?

Do not patch Mayor or succession independently if the deeper issue is "Boolean full-screen claim + effect-owned hidden transition".

## 10. Recovery / restore audit — still required

S2 is incomplete until restored-state paths are included. Audit at least:

```text
RecoverySnapshot / restored night checkpoint
-> current nightStepIndex
-> remembered target/decision state
-> reconstructed interactions
-> materialized current step
-> full-screen ownership
-> concrete surface plan
```

Look specifically for:

- stale remembered target no longer in the current legal domain;
- restored `nightStepIndex` pointing to a step whose dynamic prerequisites changed;
- mode restore (Beginner vs Experienced) changing manual/automatic presentation ownership;
- persisted selection surviving into a different materialized step;
- dynamic flow refresh after an action removing/reordering the expected next surface;
- a restored automatic ruling that expects a `LaunchedEffect` to fire but has no durable/manual fallback.

The target is not merely "no crash". The target is the same exactly-one-surface invariant after reconstruction.

## 11. Mode matrix — still required

Prove the surface invariant in both modes.

The audit must map the actual mode flag/authority contract rather than assuming names. In particular determine exactly how the production `automaticStorytellerInfo` or equivalent flag maps to Beginner vs Experienced authority.

For each relevant family, record:

```text
producer
-> mode
-> automatic/manual behavior
-> claimed full-screen?
-> concrete surface owner
-> confirmation/auto-advance owner
-> fallback
```

Experienced mode is especially important because it reaches manual branches that Beginner automation may bypass, including Mayor redirect, Demon succession and discretionary information selection.

Beginner must still be checked because the architectural fix must not fork gameplay pipelines.

## 12. Multi-surface audit — not complete until exhaustive

Do not only search for zero-surface states.

Because concrete renderers are selected by independent predicates, also prove that no production-valid shape matches two full-screen renderer families simultaneously.

Use the local worktree to create an exhaustive matrix of predicates and upstream shapes. At minimum check overlap among:

- action/single-target surface vs role-specific information surface;
- ruling surface vs generic action surface;
- pair-information vs plain-information;
- numeric/result-specific vs plain-information;
- evil information vs generic information;
- trigger-role result surfaces vs generic target surfaces;
- restored states carrying fields from a prior step.

A correct global design should make `>1 surface` unrepresentable rather than relying on branch ordering to hide the second match.

## 13. Historical/root-cause evidence already identified

The audit supports a systemic root-cause pattern rather than a one-off Demon screen bug.

### 13.1 Incremental square-table migration created parallel classification tables

Full-screen square-table migration happened incrementally across roles/actions. Ownership and rendering were not introduced as one exhaustive typed dispatch boundary.

Different files now independently answer related questions:

```text
Does this step exist?
Does it claim full-screen?
Which role-specific surface predicate matches?
Can a presentation object be built?
Which Compose dialog renders?
Should an automatic effect advance instead?
```

That duplication is the architectural source of drift.

### 13.2 New Demon history proves ownership/presentation drift is real

New Demon presentation required later restoration/fix work after earlier UI migration. This is concrete historical evidence that the split ownership model has already produced regressions.

### 13.3 S1 exposed the same general anti-pattern in navigation

The dynamic cursor bug followed the same pattern:

```text
upstream owner allows invalid/intermediate state
-> downstream renderer coerces/repairs/guesses
-> invalid state becomes difficult to detect
```

S2's Boolean ownership plus downstream renderer reclassification is the surface equivalent of that anti-pattern.

### 13.4 Source-string tests did not protect the user-visible invariant

Existing tests cover pieces such as:

- Boolean ownership classification;
- individual presentation helpers;
- auto-advance wiring strings;
- source-level renderer/wiring shape;
- specific action fallback behavior.

They do **not** collectively prove:

> production-valid materialized night step -> exactly one concrete render surface.

This is why multiple local tests can stay green while a black/ownerless UI state remains architecturally possible.

`ClocktowerDynamicNightAdvanceWiringTest` was already retired in S1 after typed behavior replaced its implementation-shaped contract. Apply the same principle in S2: do not preserve harmful architecture for old source-string tests.

### 13.5 Wide UI model permits contradictory combinations

`ClocktowerNightStepUi` can carry overlapping or incomplete action/display/candidate/result fields. The renderer then treats field combinations as an implicit union type.

This makes illegal semantic combinations easy to represent and forces many downstream predicates to reconstruct intent.

## 14. Likely architecture direction — hypothesis to validate, not pre-approved implementation

The strongest design direction from the audit is to replace information-lossy Boolean ownership plus independent renderer guesses with one typed authoritative surface plan.

Conceptually:

```kotlin
internal sealed interface ClocktowerNightSurfacePlan {
    data class FullScreen(
        val surface: ClocktowerNightFullScreenSurface,
    ) : ClocktowerNightSurfacePlan

    data object LegacyInline : ClocktowerNightSurfacePlan
}
```

The full-screen surface can itself be an exhaustive typed family, for example conceptually:

```text
SingleTarget
Ruling
EvilInfo
PairInfo
NumericInfo / role-specific result
FortuneTeller
Spy
Undertaker
Ravenkeeper
Sage
PlainInfo
...
```

The exact variants must come from the completed producer/renderer matrix; do not mechanically copy this list if the live code shows a better boundary.

Desired production chain:

```text
canonical interaction
-> materialized ClocktowerNightStepUi / semantic step
-> exactly one typed ClocktowerNightSurfacePlan
-> exhaustive renderer
```

Desired property:

```text
FullScreen + no renderer
```

is not representable.

Likewise, a single plan value prevents two independent full-screen owners from rendering simultaneously.

Important constraints:

- do not create the planner only to make a test easy;
- the planner must become the real production ownership boundary;
- do not move legality/rules into presentation;
- do not create a second persisted state owner;
- do not create Beginner/Experienced gameplay forks;
- preserve SNE-7 checkpoint/history/Dawn ownership;
- preserve UI-NAV-1 navigation-shell ownership.

## 15. Required S2 evidence strategy

Follow `AGENTS.md` risk-based behavior-first rules.

### Before production edits

Complete the producer/consumer fan-out matrix first.

For every surface family capture:

| Field | Required content |
|---|---|
| canonical producer | interaction/fact/phase |
| materializer | exact builder/materializer |
| legal `ClocktowerNightStepUi` shape | key action/display/candidate/result fields |
| modes | Beginner / Experienced |
| full-screen claim | yes/no + owner |
| candidate surface predicates | all that can match |
| concrete renderer count | 0 / 1 / >1 |
| fallback | explicit target |
| automatic effect | if any |
| restore behavior | fresh / restored |
| reachability | production / defensive / impossible |
| classification | total / defect / exemption |

### RED requirements

If a production-reachable defect is found, establish the smallest durable typed T0 evidence at the true ownership seam.

Good RED:

```text
production-valid materialized step
-> surface planner / owning callable contract
-> must produce exactly one plan
```

Bad RED:

```text
construct arbitrary impossible ClocktowerNightStepUi bag
-> observe no renderer
```

Also avoid source-string assertions over local variable names, callback spelling, render-function names, or `if` branch text.

### Implementation

Fix the shared owner, not the exposed role.

If the defect is caused by the split ownership model, do not patch Washerwoman/Mayor/DemonKill/Ravenkeeper one by one.

### Validation

At minimum:

```text
focused T0 RED/GREEN when applicable
-> owning unit/presentation tests
-> :app:testFast --rerun-tasks at checkpoint
-> triggered broader tests according to TESTING_STRATEGY
-> git diff --check
-> exact changed-file/scope audit
-> rerun producer/consumer search
```

Then perform the Experienced-mode regression matrix from the parent handoff, including real-device validation after a logical production checkpoint.

## 16. Exact continuation order in Work

Do not restart the audit from zero. Continue in this order unless live code disproves an earlier assumption.

### A. Resolve pair-information production reachability first

Search the complete production chain for Washerwoman / Librarian / Investigator and `legalSelectionOptions` / `manualInformationCandidates`.

Prove whether a legal unreliable/manual pair step can have empty legal options while still being a real full-screen information step.

If yes, record the exact production path and use it as the leading S2 RED candidate.

If no, document the upstream invariant that makes it impossible and the test/contract that protects that invariant.

### B. Finish specialist result-family totality

Close Clockmaker/Chef/Empath/Sage/Undertaker/FortuneTeller/Chambermaid/pair/numeric families. For every optional/empty result field, identify the exact production fallback.

### C. Prove automatic ruling totality

Inspect `TemporaryAutomaticStorytellerPolicy` and canonical gating for Mayor redirect and Demon succession.

Determine whether the transient no-manual-surface state is guaranteed to auto-resolve or can become permanent after fresh/restore states.

### D. Complete recovery audit

Trace restored checkpoint/selection/mode through materialization and surface dispatch.

### E. Complete exhaustive multi-surface audit

Prove no legal materialized shape can satisfy two independent full-screen render families.

### F. Inspect history at the ownership boundary

Use local history tools to inspect introduction/migration of:

- `ClocktowerNightFullScreenOwnership`;
- square-table role migrations;
- Ravenkeeper presentation migration;
- New Demon restoration/fixes;
- Mayor/succession automatic presentation;
- Red Herring ownership/auto-advance migration;
- source-string ownership/wiring tests.

Historical Red Herring-related commits previously identified during audit include:

```text
124aa251...
6a7cf68c...
ad91a3b7...
```

Treat these as leads; verify exact subjects/content locally before citing them in final documentation.

### G. Freeze the S2 ownership design and create evidence

Only after A-F:

- choose the real authoritative seam;
- create smallest durable typed RED/characterization as appropriate;
- implement the global ownership fix;
- retire/narrow superseded source-string tests only after typed coverage exists.

### H. Validate, checkpoint, document

Run required tests and exact diff/scope audit, then update:

- parent handoff;
- this S2 audit document with final matrix/result;
- roadmap if the checkpoint changes campaign state;
- PR #123 description/checkpoint notes if useful.

Keep PR #123 draft unless the user explicitly asks to merge.

## 17. Useful local search starting points

Use local `rg`/IDE navigation rather than repeated manual file reading. Search at least:

```text
clocktowerNightUsesFullScreenHostSurface
ClocktowerNightStepUi
ClocktowerNightStepMaterializerRegistry
ClocktowerHostInteractionProjector
ClocktowerInformationStepBuilder
manualInformationCandidates
legalSelectionOptions
usesRavenkeeperSquareTable
usesPairSquareTable
plainInformationDisplayStep
clocktowerSingleTargetAbilityPresentation
clocktowerNightRulingPresentation
TemporaryAutomaticStorytellerPolicy
MAYOR_REDIRECT_ELIGIBLE
DEMON_SUCCESSION_REQUIRED
NewDemonIdentity
ClocktowerNightFullScreenOwnershipTest
ClocktowerNightStepResultSurfaceOwnershipTest
ClocktowerAutomaticSquareTableFallbackTest
ClocktowerNightActionSquareTablePresentationTest
ClocktowerRedHerringAutoAdvanceWiringTest
ClocktowerPendingSuccessionFlowAuthorityTest
ClocktowerTemporaryAutomaticRulingsTest
```

The current Host screen location identified during audit is:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt
```

Do not accidentally inspect an obsolete/root-path Host file and assume it is the live UI owner.

## 18. Stop/report conditions for Work

The user's explicit preference for this campaign is to continue without frequent stops.

Do **not** stop after finding one suspicious branch, one RED candidate, one passing focused test, or one implementation checkpoint if the rest of the S2 audit can still proceed safely.

Continue through the complete audit and, when evidence supports it, implementation and validation.

Stop and report only for a genuine blocking condition such as:

- live branch/head conflicts with the expected campaign and cannot be safely reconciled;
- required repository/tool access is unavailable;
- the requested change would violate an authoritative architecture/rules contract and needs a product decision;
- the local build/test environment cannot execute required evidence after reasonable troubleshooting;
- the required implementation would exceed the current authorized campaign scope in a material way;
- execution/session hard limit prevents continuation.

If stopping, state the exact blocker and preserve an exact continuation checkpoint in docs.

## 19. Root principle

> **Do not fix the screen that happened to go black. Fix the ownership model that allowed a full-screen night step to have zero or competing concrete owners.**
