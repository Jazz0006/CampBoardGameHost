# NEXT DEVELOPMENT HANDOFF — Night Step UI Ownership Decomposition

> Role: **CURRENT ACTIVE HANDOFF**
> Revised: 2026-09-06 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`

## 1. Authority and current route

Read root `AGENTS.md`, `CURRENT_DEVELOPMENT_ROADMAP.md`, then this handoff.
Current progress and validation status live only in the roadmap. The earlier S1–S5 / Pair-Manual-first
sequence is superseded by the user-accepted D1–D6 route there.

The user explicitly authorized a fresh architectural decision and local editing of large files.
Connector/one-shot/Luna writer-routing restrictions do not apply to this complete local workspace.
This exception does not change gameplay invariants, evidence requirements or merge authorization.

## Active implementation contract — D3

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
