# NEXT DEVELOPMENT HANDOFF — Global Ownership Cleanup Step 3

> Date: 2026-09-14 Australia/Sydney
> Status: **IMPLEMENTED on `codex/global-ownership-cleanup-3`; remote acceptance pending**
> Goal: **one production owner for registration legality, candidate construction and automatic selection**

## 0. Start here in the next conversation

1. Read root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.
2. Read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`.
3. Read `docs/GLOBAL_CODE_OWNERSHIP_AND_DEAD_CODE_AUDIT_2026-09-14.md`.
4. Query live `main`, PR #124 and PR #125 rather than assuming this handoff's recorded state.
5. Confirm the worktree is clean and branch from the merged `main`.
6. Perform a read-only producer/consumer and persistence/recovery fan-out audit before editing.

## 1. Completed campaign slices

Step 1 fixed the Virgin Spy automatic-registration identity:

- PR #124;
- accepted head `8f5d76682f5003af04d5cf745c81595d34e3d083`;
- decision identity includes game, phase, round and registration identity;
- typed identity regression and Android/ASP/Clingo/aggregate/R2 checks passed.

Step 2 completed behavior-neutral dead-code cleanup:

- PR #125, originally stacked on #124;
- accepted code head before documentation closeout `590cac5526ca4417a71785757a2651f93ab5e853`;
- removed the obsolete Setup subtree, dead UI/helpers, six unread value parameters, two unused
  constants, 790 unused imports and 181 string keys per locale (168 baseline keys plus 13 newly
  orphaned keys);
- full Android `testFull + assembleDebug`, ASP, real Clingo, aggregate CI and R2 passed.

Re-query exact merge commits and final heads after closeout; documentation commits may advance the
recorded PR #125 head without changing production code.

## 2. Step 3 problem statement

Registration currently has multiple owners that happen to agree:

1. `TroubleBrewingRegistrationSemantics` is the formal domain model but is test-only.
2. Night UI uses `ClocktowerAutomaticRegistrationEffect` and the temporary 90/10 policy.
3. Host day behavior for Virgin, Slayer and Klutz constructs legality/state and calls
   `WeightedStableSelector.selectStyle` inline.
4. `unifiedRegistrationPool` exists but is test-only.

The risk is future drift between legal registration candidates, projected registration facts and
automatic rulings. UI may render/select a domain-provided legal option; it must not recreate role or
team legality.

## 3. Required first audit

Before choosing an implementation seam, map:

- every production and test caller of the formal semantics, automatic effect, unified pool and
  direct weighted-selection paths;
- Spy/Recluse candidate construction for night and day actions;
- Virgin, Slayer and Klutz consumption of registration facts;
- Beginner and Experienced behavior;
- fresh and restored session paths;
- persisted or reconstructed fields whose schema/meaning must remain stable;
- automatic Storyteller identity inputs and deterministic-selection policy boundaries.

Classify each caller as must-inherit, intentional adapter or obsolete parallel path. Record the
result before moving production ownership.

## 4. Target ownership contract

The target domain/session seam must own:

- whether a character may register as another alignment/role for a given interaction;
- the complete legal candidate set;
- the registration fact projected to the consuming mechanic;
- the request passed to the temporary automatic-selection policy.

The temporary policy may continue to select among legal candidates. Presentation may display a
selection and submit intent. Neither layer may independently reconstruct legality.

Do not change registration probabilities, role rules, visible behavior or persisted schema merely
to complete the ownership migration.

## 5. Testing policy

- Add a typed RED only for a real stable behavior or identity invariant that current tests do not
  cover.
- Do not manufacture RED checkpoints for mechanical cleanup or seam movement.
- Prefer existing characterization, domain and integration tests for behavior-preserving ownership
  migration.
- Do not add source-string tests for local variables, callback spelling or Compose structure.
- Final acceptance must cover affected day/night consumers, Beginner/Experienced where applicable,
  and fresh/restored paths when the fan-out audit proves they consume the seam.

## 6. Compatibility paths that remain live

Do not delete solely by name:

- `TemporaryAutomaticStorytellerPolicy`;
- remaining `FirstNightInformationMigration` parity shadow/fallback;
- legacy epistemic JSON readers;
- `LegacyRulesetCatalogAdapter`;
- Ghost Vote missing-field recovery.

The step 3 issue is registration caller sprawl and duplicate candidate construction, not the mere
existence of a temporary or compatibility boundary.

## 7. Remaining user-confirmed sequence

3. Unify registration domain and automatic-selection ownership.
4. Make production restore and restore tests consume one composition boundary.
5. Cut No Greater Joy setup over to the generic provider/source/shown-identity pipeline.
6. Finally reduce Host/App gameplay ownership and converge square-table presentation, including
   shared phase conversion and mechanical-projection ownership.

Keep these as independently reviewable slices. Do not start steps 4-6 inside the step 3 PR.

## 8. Repository writing rule for this environment

Use normal local edits, Git commits and authenticated terminal `git push`. Do not use Connector
whole-file writes or a one-shot GitHub workflow for large source files. Before each push, verify the
worktree, exact diff and full-file integrity; never upload a truncated large-file representation.

## 9. Stable rule

> Registration legality and candidate construction have one domain/session owner; automatic policy
> selects only from that authoritative result, and UI never recreates the rule.
