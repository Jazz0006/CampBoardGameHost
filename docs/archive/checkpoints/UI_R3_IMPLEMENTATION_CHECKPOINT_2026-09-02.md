# UI-R3 Implementation Checkpoint — Full-Screen Player Information Display

> Date: 2026-09-02 Australia/Sydney  
> Slice: UI-R3  
> Branch: `codex/ui-r3-player-information-square-table`  
> Base after validation: `codex/ui-r2-pair-manual-square-table`  
> Status: implementation complete pending final ordinary CI/R2 evidence and real-device walkthrough

## Scope

UI-R3 replaces the ordinary player-facing information surface with the reusable full-screen square-table presentation introduced in UI-R1.

This slice intentionally does not redesign information legality, recommendation ranking, publication/history ordering, or Fortune Teller target/result interaction. Fortune Teller interaction remains UI-R4.

## Player-facing presentation

For non-Grimoire displays:

- all players are rendered around the reusable square-table surface using stable seat identity;
- the table occupies the dominant portion of the viewport;
- pair information highlights the two typed proposition seats;
- Number, Yes/No, EvilInfo, RoleReveal and Plain displays keep the table neutral unless this slice has an explicit typed highlight contract;
- the result is rendered in the center region;
- player-facing content does not expose reliability/truth/recommendation diagnostics.

Grimoire remains on its existing dense dedicated display because it presents the complete true-role roster rather than one seat-targeted clue.

## Typed highlight contract

`clocktowerPlayerDisplayHighlightedSeats(step)` derives pair highlights only when:

- `displayKind == EitherOne`;
- `displayProposition` is `InformationProposition.AnyOf`;
- the proposition contains exactly two `RoleAt` alternatives;
- both alternatives refer to the same shown role;
- the two seat identities are distinct.

The adapter never parses `displaySecondary` or other localized/display text to discover seat identity.

When the typed proposition does not satisfy that contract, the square table remains neutral. Existing display text may still be shown as a fallback in the center, preserving unsupported/legacy presentation without pretending it is typed seat data.

## Lifecycle and safety boundaries

The existing Host lifecycle is unchanged:

1. validate information-decision publication authority;
2. publish first-night information where applicable;
3. record the private epistemic observation;
4. determine reliability for event classification;
5. record the existing information event/history entry;
6. assign `playerDisplayStep` and render the player-facing surface.

UI-R3 changes only the final presentation layer. It does not move or duplicate publication/history callbacks.

The existing `ClocktowerDisplayedInformationReliabilityTest` remains part of focused acceptance so `EvilInfo` with `roleEnName == null` cannot regress into the first-night display crash.

## Tests-first evidence

RED was established before the presentation adapter existed.

Normal PR CI run `33588114175` failed during test compilation only because `clocktowerPlayerDisplayHighlightedSeats` was unresolved. Production compilation reached the test boundary, establishing the intended RED rather than a source-string test.

`ClocktowerPlayerDisplayPresentationTest` protects durable typed behavior:

- pair seat highlights come from typed proposition seats rather than misleading display text;
- a zero pair result leaves the table neutral;
- Number display remains neutral even when a proposition contains seat-like data;
- `EvilInfo` with no role ability identity is presentation-safe and neutral.

No permanent source-string or Compose-shape test was added.

## Large-file wiring

`ClocktowerHostScreen.kt` remains a protected orchestration owner. UI-R3 needed exactly one integration change: pass the existing `cards` list into the dedicated player-display owner.

The integration was applied through the standard self-removing exact-anchor one-shot workflow.

One-shot run `33588455372` passed:

- exact checkout/remote head and blob locks;
- exact unique Host call-site replacement;
- `git diff --check`;
- HostScreen-only changed-file allowlist;
- `ClocktowerPlayerDisplayPresentationTest` GREEN;
- `ClocktowerDisplayedInformationReliabilityTest` GREEN;
- `./gradlew :app:testFast --rerun-tasks --no-daemon` GREEN;
- remote-head recheck before write;
- automatic deletion of the temporary workflow and patch script.

Product Host wiring commit: `56698597afd6c5c61d42e48e36f65aa3e6964f36`.

Cleanup commit: `9e478aa53ee29b59e2d7311fa99ff7e76b38db3d`.

## Scope guards retained

UI-R3 does not implement:

- Fortune Teller two-target/result redesign;
- EPI-MQ / Productive Uncertainty;
- recommendation scoring/ranking changes;
- legal-domain changes;
- new history/persistence semantics;
- Host/App decomposition;
- broad theme or animation framework work.

## Remaining acceptance

This document commit intentionally requests `[full-ci]` so the final ordinary user-authored checkpoint validates full Android unit tests/debug APK plus repository CI/R2 gates after the bot-authored one-shot cleanup.

After those gates pass, restore PR #72 to the stacked UI-R2 base and verify the final stacked diff contains only UI-R3-owned production/test/checkpoint files. Do not merge without explicit authorization.
