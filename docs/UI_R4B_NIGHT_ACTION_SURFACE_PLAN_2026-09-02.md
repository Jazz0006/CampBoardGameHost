# UI-R4B — Night Action Square-Table Surface

## Status

ACTIVE pre-R5 slice. Stacked after the Monk/Ravenkeeper target-legality hotfix.

## Objective

Unify common night player-target interactions around the reusable square-table surface before UI-R5 field-test freeze.

This slice changes presentation/orchestration only. Existing target legality, role semantics, information legality, recommendation, and confirmation authorities remain upstream.

## Scope

### A. Reusable single-target action surface

Use the square table for actions that already supply a legal target set and one current selection.

Initial production migrations:

- Monk protection;
- Ravenkeeper target selection.

The surface must:

- show every stable seat around the table;
- render supplied legal targets as Selectable;
- render the current selected target as SelectedFirst;
- render all non-legal seats as Disabled;
- never infer legality from alive/dead state, role text, or labels;
- use the supplied callback unchanged.

The Monk/Ravenkeeper legality hotfix remains the authority for their supplied candidates.

### B. Reusable two-target action surface

Use one square table for Chambermaid rather than two independent seat selectors.

The surface must:

- retain SelectedFirst and SelectedSecond visual states;
- use the existing Chambermaid candidate authority;
- allow only legal second-seat continuations;
- keep result legality/number semantics in the existing Chambermaid information authority;
- preserve stale-pair invalidation behavior.

Fortune Teller already has a dedicated two-target square-table owner; UI-R4B does not replace its Boolean adjudication owner.

### C. Typed subject-seat highlight projection

Player-facing final display should preserve the semantic subject seats of the information where typed data already identifies them.

Required first coverage:

- setup pair information (existing EitherOne / AnyOf(RoleAt, RoleAt));
- Fortune Teller BooleanResult subjectSeats;
- Chambermaid numeric proposition subjectSeats;
- Ravenkeeper role reveal target seat where typed proposition identifies the subject.

The display layer must not parse localized strings or reconstruct target identity from labels.

If a current typed proposition does not carry enough subject identity, keep that case neutral and add a narrow typed handoff rather than parsing display text.

## Tests-first contracts

Permanent tests should cover:

1. single-target seat state consumes only supplied selectable seat ids;
2. current single target remains SelectedFirst even when the selectable set is empty after selection;
3. Chambermaid first/second selections remain ordered and only supplied continuations are selectable;
4. subject-seat highlight extraction from supported typed propositions;
5. unsupported / insufficiently typed information remains neutral;
6. no source-string / Compose-shape assertions.

## Scope guards

Do not add:

- recommendation scoring/ranking changes;
- legal-domain redesign;
- new Ravenkeeper/Monk rules inside UI code;
- EPI-MQ / Productive Uncertainty;
- Mayor redirect redesign;
- Imp succession redesign;
- broad Host/App decomposition;
- animation/theme expansion.

## Completion

UI-R4B is complete when Monk, Ravenkeeper, and Chambermaid use table-first target selection, Fortune Teller/Chambermaid/Ravenkeeper final information retains typed subject-seat context where available, focused tests and `:app:testFast` pass, temporary large-file patch tooling is removed, and final ordinary CI/R2 is green.
