# GSP-R1C2C-0 — Producer Coverage Audit & Generic Explicit Registration Choice Acceptance

> Date: 2026-10-08 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Status: **C2C-0 COMPLETE / ACCEPTED; C2C-1 typed canonical ruling / Recovery NOT YET IMPLEMENTED; overall GSP-R1C IN PROGRESS**.  
> Executable PR: [#260](https://github.com/Jazz0006/CampBoardGameHost/pull/260), squash merge `d75d00031bd83ecf340230041443e7612f2e080b`.  
> Validated exact PR head: `d7c5ef420872e6c2952917681acb887b40e0730a`, **CI #3880 GREEN** (Android tests / selected FAST) and **R2 #3539 GREEN**. Android full + debug APK, ASP, and Real Clingo were not part of this exact selected run; **not a T4 checkpoint**.  
> Coverage inventory: [GSP-R1C2C producer audit](GSP_R1C2C_PRODUCER_COVERAGE_AUDIT_2026-10-08.md). Invariants: [registration result–witness separation](GSP_REGISTRATION_AMBIGUITY_RESULT_FIRST_HISTORY_CONTRACT_2026-10-08.md) and [PR #254 acceptance](GSP_REGISTRATION_RESULT_WITNESS_SEPARATION_FIX_ACCEPTANCE_2026-10-08.md).

## Delivered

1. Inspected live `main@e3f76f076e6568e30521fb24bb4ee50fda2207a8` before making changes; local/remote equal, working tree clean. Rechecked production App, Host UI, Session, legal candidate, causal journal and Recovery source, with five-category producer classification. Distinguishes Storyteller discretion, player decisions/mechanical `ActionFact` outcomes, player-visible `EpistemicObservation`, and presentation-only localized `RoleAction`.
2. Added a **generic explicit-only, interaction-local UI registration selection snapshot** in `ClocktowerRegistrationInteractionState.kt`, used by the shared `manualChoicesMatchResult` path. Explicit `false` is distinguishable from untouched input. An optional specific registered role is retained only while the special mode is active; a deliberate alignment without a role **does not fabricate a particular role**.
3. Added behavioral tests for ambiguous witness compatibility across `Number`, `YesNo`, and `RoleReveal`; explicit versus untouched false; role-only edits; role omissions and mismatches; incompatible combination rejection; distinct interaction keys. This is a shared contract, **not a role-specific Empath/Chef policy**.
4. Confirmed #230/#232 remain legacy/superseded open Draft PRs; left unchanged. No reintroduction of weighted heuristic, old named policies, provider/LLM API or recommendation authority.

## Deliberately NOT delivered

- An explicit selection snapshot is **not** a committed typed `RegistrationFact`, verified legality ruling, historical `UNRESOLVED_NOT_REQUIRED` status, complete information observation, or durable Replay/Recovery. `ClocktowerCausalJournalPersistence` and `StorytellerCausalDecisionJournalV1.commit` continue to reject nonempty registrations without a verified producer. The existing Mayor-only durable journal remains the sole full production vertical.
- The source `registrationKey` / UI recording marker is not itself a universally stable globally causal decision identity, and night/day code paths do not all share one publication boundary. A typed ruling must use genuine ability-interaction identity and Host confirmed result, not localized event text.
- No assertion of complete producer coverage, full Android T4, historical registration provenance for old Recovery saves, or Player/Model recommendation integration.

## C2C-1 next: typed explicit registration producer and Recovery

1. Define Host-owned validated ability-interaction registration confirmation using legal rules-domain candidates (never take dedup-first witness). Include actual subject seat, querying ability interaction, concrete explicit attributes, positive unresolved-known status when a displayed result is confirmed without a ruling, and distinct old `UNAVAILABLE_OR_UNRECORDED` semantics. Do not force witnesses for malfunctioning Drunk/Poisoned results.
2. Freeze exact predecision `GLOBAL_V1` prefix and player/snapshot inputs, then append ordered causal confirmation/correction records through the **existing journal**. Reconfirm/cancel must not rewrite old captures or cause unconfirmed UI toggles to become facts. Respect Day and Night entry points including special Day rulings, not solely Chef/Empath.
3. Expand typed Recovery codec / strict decode / restore planner only after a verified producer is present. Validate interaction-local scope, legal selected outcome, ordinal chronology and corruption rejection. Old missing sidecars must remain unknown, not zero known rulings.
4. Tests: shared numeric/boolean/role results with multiple witnesses; explicit vs known unresolved; other interactions independently adjudicated; poisoned/drunk arbitrary observation; real Host confirmation path; same-revision decisions; re-confirmation/correction/cancel; save/restore as-of equality; malformed references rejected. Run Android affected suites and full T4 checkpoint if in scope.

Subsequent R1C2C batches should migrate shared confirmed information publication before per-role fallbacks, then handle Drunk precommit, red herring, demon bluffs and other genuine Storyteller discretion. Player-selected poison/protection/attack targets remain player mechanical history and **must not** be duplicated as Storyteller recommendation choices.
