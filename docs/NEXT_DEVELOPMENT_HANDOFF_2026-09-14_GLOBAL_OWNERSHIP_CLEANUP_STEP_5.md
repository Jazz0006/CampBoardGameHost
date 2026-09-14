# Global Ownership Cleanup — Step 5 Handoff

> Updated: 2026-09-14 Australia/Sydney
> Status: local gates pass; remote validation and user acceptance pending

## 1. Baseline and scope

Step 5 began from merged `main` `092ca62f7806bd353e683c2dee1c7a134e4304f5`, which includes
Step 4 via PR #127. The live fan-out and architecture pre-flight are recorded in section 12 of
`docs/GLOBAL_CODE_OWNERSHIP_AND_DEAD_CODE_AUDIT_2026-09-14.md`.

This slice is limited to the No Greater Joy production setup cutover and the duplicated base-role
distribution owner exposed by that cutover. It does not change Trouble Brewing template selection,
rotation history, recommendation behavior, gameplay rules, recovery schema or Step 6 Host/App and
square-table presentation ownership.

## 2. Implemented boundary

- `NoGreaterJoyProductionSetupPreparer` composes the registered generated provider, deterministic
  candidate source, shown-identity policy/commitment and deterministic seat assignment into one
  `CommittedClocktowerSetup`.
- App root resolves the committed role IDs to existing localized `ClocktowerRole` cards and owns
  only lifecycle/presentation wiring.
- `clocktowerSetupDistribution` is the common setup-layer base distribution used by generated setup,
  TB preset validation and setup-screen presentation.
- The App-root `generateClocktowerAssignments`, `ClocktowerAssignment`, distribution table and
  recommendation-based second Drunk identity choice are removed.

## 3. Evidence

- Meaningful typed RED: the new production preparer reference was absent before implementation.
- Focused GREEN: new NGJ production preparation test plus generated source, provider registry,
  shown-policy/commitment, NGJ architecture/product and TB preset validator contracts.
- Forced `:app:testFast`: pass.
- Forced `:app:testFull :app:assembleDebug`: pass; 1,353 JVM tests, zero failures.
- `git diff --check`: pass.
- Final legacy-owner and producer/consumer search: pass.

## 4. Acceptance gate

- Verify exact parent is merged `main` `092ca62f7806bd353e683c2dee1c7a134e4304f5`.
- Verify changed-file scope is limited to NGJ/generic setup wiring, canonical distribution, owning
  tests and campaign documents.
- Require remote Android Full, ASP, real-Clingo, aggregate CI and R2 boundary validation.
- Keep the PR Draft and unmerged until the user explicitly authorizes merge.

## 5. Remaining sequence

6. Reduce Host/App gameplay ownership and converge square-table presentation, including shared
   phase conversion and mechanical-projection ownership.

Do not begin Step 6 in the Step 5 PR.

## 6. Stable rule

> NGJ actual composition, seat assignment and shown identity are one seeded setup transaction;
> App presentation must consume the committed result and must not reconstruct any of those facts.
