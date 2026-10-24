# GSP-R1C2C-1A — Typed Registration-Resolution Causal / Recovery Foundation Acceptance

> Date: 2026-10-08 Australia/Sydney  
> Status: **GSP-R1C2C-1A COMPLETE / ACCEPTED; 1B REAL HOST UI WIRING NEXT; R1C2C OVERALL IN PROGRESS**  
> PR: [#263](https://github.com/Jazz0006/CampBoardGameHost/pull/263), squash merge `9d7b844d37c8ebad1448836ad3946b26af006d5c`.  
> Accepted exact PR head: `ab7bb7f69781c4b5b732da21a0941dce6d9d5521`. CI **#3893 GREEN** (Android **FAST** successful; full Android / debug APK and ASP/Real Clingo skipped by change selection). R2 **#3549 GREEN**. **Not T4.**

## Delivered, and the exact authority boundary

- Added typed `StorytellerProviderDecisionContextV1.RegistrationResolution`, `RegistrationResolutionStatusV1` and legal candidate payload `RegistrationChoice`, separate from information-result choice and derived candidate witness sets.
- `StorytellerRegistrationRulingProducerV1.confirm` accepts **only an already published, globally bound private semantic observation** with matching source and a semantically referenced subject. It checks rule-owned `TroubleBrewingRegistrationDomain` options for the current subject, permitted roles, poisoning and observed reliability; rejects stale/older observations rather than reinterpreting them from later current-state facts.
- A concrete selected special ruling becomes a typed `RegistrationFact` tied to interaction, subject and registration question. When no role was expressly selected, only its verified alignment is recorded: **no fabricated first role**. Explicit actual registration and positively confirmed no-specific-witness resolution are different typed outcomes; known malfunction uses NOT_APPLICABLE, not a fictional special registration. `UNAVAILABLE_OR_UNRECORDED` is never an executable selected candidate.
- The existing `StorytellerCausalDecisionJournalV1` now admits nonempty registration facts **only for validated registration-resolution commits**; unrelated Mayor/other decisions still reject candidate witness promotion. Ordered capture, commit and correction preserve as-of prior snapshots and independent same-revision decisions.
- `ClocktowerCausalJournalPersistence` writes/strictly decodes typed optional registration arrays. Old v1 commits with no registration array remain empty as their original schema guaranteed; a **missing causal sidecar** remains explicitly unknown, never proven-empty historical rulings. Strict Recovery restore revalidates typed facts against frozen observation subject/identity, special reason, alignment, selected candidate and poison/reliability.
- Behavioral tests cover positive unresolved, explicit special role and alignment-only, explicit actual, independent Spy/Recluse interactions, poisoned arbitrary result, correction/as-of equality, save/restore parity, malformed recovered subject and missing observation. Shared proposition seat-scope helper was made internal for validation reuse.

## NOT delivered / next actual production gate: GSP-R1C2C-1B

This PR **does not wire** `ClocktowerHostScreen`'s explicit-choice snapshots / `onShowPlayerDisplay` to `StorytellerRegistrationRulingProducerV1`. No UI event is yet a typed canonical registration decision. This is foundational callable production code with executable tests, **not a live Host registration producer**, and neutral-prefix registration coverage must remain partial/unknown.

1. Connect a shared **real Host confirmed result** boundary (not on option preview, manual toggle or night-step advance), use a stable `ClocktowerInteractionId`, canonical source/subject seat, the just-published typed observation record ID, ruleset-catalog legal role domain, and actual explicit-only manual choices. Resolve role defaults: automatically choosing `firstOrNull` in a UI toggle **must not become an explicit specific-role decision**. Preserve explicit false and untouched separately.
2. Check concrete manual registrations against **all rules-legal witnesses for that displayed result** before committing; support Number, YesNo and RoleReveal without per-ability hardcoded heuristics. When result is arbitrary due to Drunk/Poisoned, record no explanatory witness. Never dedup-first.
3. Record an interaction-specific `UNRESOLVED_NOT_REQUIRED` only after positive confirmation of that observed result with a relevant legal domain. Do not infer that status for older records. Consider actual target relevance and two or more independent subjects.
4. Make re-confirmation, corrected result, cancellation and save/restore idempotent and causally ordered; old prefix and corrections must remain immutable and historically as-of. Add integration tests starting from actual Host confirmation callbacks and Recovery restore planner.
5. Audit Day registrations (Virgin, Klutz, Slayer) and other affected role abilities, explicitly identify those without a compatible typed observation; they may need distinct legal ability-event producers. No broadened claim that all registration families are captured.

Subsequent C2C-2/3 integrate remaining real confirmed information/setup decision producers. No LLM provider/R2/R3/R4 in this stage.
