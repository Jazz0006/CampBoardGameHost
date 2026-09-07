# NEXT DEVELOPMENT HANDOFF — Night Step UI Ownership Decomposition

> Role: **HISTORICAL / SUPERSEDED by the 2026-09-07 D6 handoff**
> Revised: 2026-09-06 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`

## 1. Authority and current route

Read root `AGENTS.md`, `CURRENT_DEVELOPMENT_ROADMAP.md`, then this handoff.
Current progress and validation status live only in the roadmap. The earlier S1–S5 / Pair-Manual-first
sequence is superseded by the user-accepted D1–D6 route there.

The user explicitly authorized a fresh architectural decision and local editing of large files.
Connector/one-shot/Luna writer-routing restrictions do not apply to this complete local workspace.
This exception does not change gameplay invariants, evidence requirements or merge authorization.

## Historical handoff — UI-R5 acceptance preparation

D1–D5 and the global change-set audit are complete; use the roadmap for exact evidence/current status.
Next prepare a traceable installable APK and run the device acceptance matrix in roadmap section 4.10.
Keep this branch stable, reproduce/fix only concrete defects, and collect real-device evidence before
requesting merge authorization. No immediate D6, algorithm-policy changes or broad visual redesign.
The prior implementation contracts below are historical scope, not instructions to repeat extraction.

## Prior implementation contract — D5.2

Extract Host-to-first-night request conversion verbatim into a dedicated adapter with explicit inputs.
Keep pair legality with the existing authority and localized legacy conversion only for parity/fallback.
Give migration a typed publication resolution: Published updates state; AlreadyDisplayed reopens only;
LegacyFallback retains old reveal behavior without a new migration fact. Parity telemetry stays before
resolution and uses the same migration/request shadow. No state/effect lifetime or failure-order change.

Keep upstream registration/selection telemetry at its current role-specific owner after audit. D5.2
closes the selected structural campaign on full CI/R2 success; D6 is a separate Host/App-state campaign.
Then proceed to architecture acceptance and UI-R5 real-device stabilization. No merge authorization.
Current progress/evidence lives only in the roadmap.

## Prior implementation contract — D5.1

Extend the existing player-reveal handoff seam to own synchronous Host effect ordering and the narrow
confirmation/snapshot/revision authorization check. Keep authorization lazy, publication short-circuited
on denial, duplicate publication reveal-only, and private observation before history before reveal.
Exceptions propagate without rollback or subsequent callbacks. Preserve the private recorder recheck.

No registration/telemetry/confirmation/migration/history-format/state-owner change. Existing history
body remains in its callback. Add direct order/rejection/duplicate/failure tests and real-adapter freshness
coverage. Run full checkpoint CI and R2 because central publication orchestration is touched. No merge.
D5.2 upstream effects require a fresh ownership decision; no atomicity or global exactly-once claim.
Current evidence lives only in the roadmap.

## Prior implementation contract — D4.2

Continue from the validated D4.1 checkpoint. Retain existing Fortune Teller/Chambermaid renderers:
Boolean confirmation and determined/option results are distinct contracts, not duplication to erase.

Complete the Manual typed-input seam: the authority adapter interprets its existing pair-key grammar
once and prepares selectable roles/seats plus exact original options. The pure selection model lives
outside Compose. UI consumes the prepared input without proposition parsing. Preserve supplied
candidate order, malformed filtering, duplicate/zero first-match semantics and candidate-change reset
identity (including ignored options). Keep the interaction key and show/dismiss/confirm state lifetime.

Use existing selection/authority/display tests plus focused characterization for projection and reset
identity. No source guards, Host changes, publication side effects, or merge. Current evidence and
D4 closure live in the roadmap; D5 begins with a separate publication-transaction audit.

## Prior implementation contract — D4.1

After D3 the user requested continuing. Split D4 into independently verifiable D4.1 single-target/
ruling renderers, then D4.2 two-target/Manual contracts. Current evidence/status stays in roadmap.

Use immutable target selection plus separate ability/ruling presentation. New renderers accept
prepared data and a scoped target event callback, never the whole Host/step or game services.
Keep target legality upstream and all mutation/publication callbacks at their existing owner.
Preserve action-specific visibility, actor cues, Ravenkeeper reveal permission and Mayor secondary
action. Key grouped renderer calls by action to preserve child-state reset boundaries.

Leave Fortune Teller/Chambermaid and later information UI unchanged in D4.1. Six direct presentation
tests supplement existing legality/table tests. No new source-string guard, D5, or merge.

## Prior implementation contract — D3

The user requested continuing after D2. Current progress/evidence lives in the roadmap.
Introduce a Compose-free numeric/Boolean preparation owner and adapter request types. Keep
identity/freshness as narrow immutable data and continue calling the existing legality adapters.

Preserve numeric fallback/default order, history/pressure/style arguments, registration gating,
Chef bounds, and exact ordered Boolean actor/target matching. Leave target selection and
publication effects in place. Direct tests must cover these input contracts and adapter parity.
Replace only the source-string recommendation-priority assertion superseded by typed coverage.
Do not retire telemetry or Host-history guards here. No D4/D5 or merge in this slice.

## Prior implementation contract — D2

Following the user-authorized D1 checkpoint, the user requested continuing with D2. Use a separate
commit on existing draft PR #106; current status/evidence stays in the roadmap. Preserve D1's
validated source and do not merge.

Extend `ClocktowerPlayerDisplayResolution.kt` with narrow numeric, Boolean and legacy-unreliable
conversion entry points. Reuse private option/confirmation projection within that file. Replace
only the three inline display-payload construction blocks in `ClocktowerNightStepUi.kt`.

Preserve numeric per-field null fallbacks, including empty-string semantics; Boolean missing-option
fallback; the legacy recommendation-list difference; exact confirmation and expected snapshot.
Never replace the caller's expected snapshot with the confirmation's snapshot, as that can erase a
publication rejection. Projection does not authorize or publish. Keep all surrounding callback,
telemetry and registration order unchanged.

Add typed coverage in the existing `ClocktowerPlayerDisplayResolutionTest.kt`. Do not remove
preparation/telemetry source guards until their contracts have a corresponding typed replacement.
No Host/session changes, new state, visibility widening or generic context. Stop at the D2
FAST/R2 checkpoint; D3 is the following slice.

The D1 contract below is retained as historical scope for the preceding commit.

## 2. D1 scope — legacy night-path cleanup

The first slice removes the old night-rendering fallback in `ClocktowerHostScreen.kt`.
The earlier persistent-table night route already handles started, nonempty nights and returns.
Separate ready screens handle unstarted FirstNight and Night and return.

The empty-list case is not a second usable renderer: it currently fails at `coerceIn(0, -1)`.
Make the nonempty precondition explicit in the active started-night branch and retain fail-closed
behavior. Do not auto-complete the night, mutate checkpoint state or silently render a Day screen.
An empty-night recovery design would be a separate behavior change.

Delete only the legacy night block. Preserve the surrounding Dawn/Day fallback bodies, active
night-step callbacks, draft/confirmed reads, registration/publication/history ordering and Compose
state/effect lifetime. Do not copy behavior from the superseded callbacks into the active path.

## 3. Owners and file boundaries

- Authoritative state owners are unchanged: App/checkpoint/session and Host-local UI state.
- The existing active Host night composition remains the rendering/wiring owner.
- No new API, shared context, visibility expansion, helper or state holder is needed for D1.
- Production allowlist: `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt`.
- Documentation: root README, docs README, roadmap, this handoff and existing audit reference.
- Tests: reuse existing owning tests; change tests only for a demonstrated durable coverage gap.

Expected benefit: eliminate a second night callback graph that developers otherwise have to compare
when editing selection, registration, publication or progression behavior.

## 4. Evidence and completion

For this behavior-preserving deletion:

1. Verify live main and target head, then establish the exact baseline.
2. Inspect the two early-return ready routes and active night route.
3. Use existing materializer, checkpoint transaction and first-night reveal-handoff tests.
4. Compare active night callback region and trailing Dawn/Day bodies byte-for-byte against baseline.
5. Run `git diff --check` and affected compile/tests; run T1 at this logical checkpoint.
6. If local dependencies are unavailable, use existing PR CI and distinguish attempted local tests
   from tests that actually executed remotely.
7. Update the roadmap with concrete evidence and remaining gates; stop after D1.

No manufactured RED and no new permanent source-string guard for deletion. Existing Empath
source-string debt belongs to D2/D3 when a stable typed replacement actually exists.
Do not merge or mark a draft ready without explicit user authorization.

## 5. Following slices

D2 extends the existing display conversion seam; it must preserve numeric/boolean confirmation,
expected snapshot, proposition, truth flags and fallback differences. A full `ClocktowerNightStepUi`
is not yet a privacy-restricted Player Reveal payload.

D3 reuses the existing structured adapters and moves preparation outside renderers. D4 groups by
interaction ownership and completes typed Pair Manual input; the full-screen Manual dialog and
selection model already exist and should not be recreated.

D5 publication coordination is a separate high-risk boundary. D6 Host/App-root state/persistence
work is a follow-on campaign; never move everything into a single ViewModel.

## 6. Permanent product and architecture constraints

- Preserve the persistent square table and stable physical seat identity.
- Inline actor/wake cue remains orthogonal to target selection; no separate WAKE acknowledgement.
- Legality remains upstream; recommendation ranking and Manual authority are unchanged.
- Preserve actual/shown identity separation and player-visible observation privacy.
- Keep transient UI state at its lowest correct owner and one authority per mutable state value.
- Do not mix recommendation-quality changes, game-rule fixes or visual redesign into decomposition.

After the selected architecture checkpoint, continue UI-R5 stabilization, EPI-MQ, then UX-R6 as
specified by the roadmap.
