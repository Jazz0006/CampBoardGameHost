# TBSP-6L — Trouble Brewing provenance durability repair

> Date: 2026-08-31 Australia/Sydney  
> Branch: `codex/trouble-brewing-setup-presets-v2`  
> PR: #57  
> Scope: post-6K narrow persistence-ordering repair only  
> Status: **COMPLETE / ACCEPTED**

## 1. Why 6L exists

The post-6K global audit found a narrow crash cut-point in Trouble Brewing start persistence.

Before this repair, the production ordering was effectively:

```text
resetDealState(...)
  -> persist active-game snapshot while committedTroubleBrewingSetupSelection is null
return from reset
committedTroubleBrewingSetupSelection = preparedSetup.selection
Compose/later lifecycle save normally persists provenance
```

Normal gameplay converged on the correct saved state, but an Android process death after the selection assignment and before the next lifecycle-driven save could leave a recoverable active game whose cards/seed existed while exact TB setup provenance was absent.

This did not reroll already dealt player identities, but it could lose proof of the original preset selection and prevent that completed game from contributing the correct original selection to rotation history.

## 2. Durable regression evidence

The existing coarse App-root provenance wiring guard was strengthened rather than creating a new source-string test class:

`app/src/test/java/com/codex/campboardgamehost/TroubleBrewingActiveGameProvenanceWiringTest.kt`

New invariant:

```text
inside startTroubleBrewingGame():
committedTroubleBrewingSetupSelection = preparedSetup.selection
must be followed by
persistActiveGameStateIfNeeded()
before the start function returns
```

Test checkpoint:

```text
8406bdf39a1203d8c69f5a51f7c94474516477ff
`test: lock TB setup provenance durability`
```

The one-shot repair run proved this guard RED against the pre-repair App source before applying production code.

## 3. Production repair

Production checkpoint:

```text
4c8108c91be188d33435233efb9aba26397f6b87
`fix: durably persist TB setup provenance`
```

Exact production diff:

```diff
 committedTroubleBrewingSetupSelection = preparedSetup.selection
+persistActiveGameStateIfNeeded()
 troubleBrewingFirstNightPrecomputeCoordinator.prewarm(...)
```

No setup selection, deal materialization, Drunk identity, recommendation, First Night precompute, NGJ, Mayor/Imp, A3/A4/ZDD, or Host behavior was changed.

The durable checkpoint now contains the committed cards/seed/game identity together with the exact original TB setup provenance before start continues into First Night background precompute.

## 4. Focused validation

One-shot repair workflow run:

```text
33344478383 — SUCCESS
```

The run verified, in order:

```text
exact branch head / App blob / test blob        PASS
focused provenance guard before repair          RED PASS
exact-anchor production patch                   PASS
same focused provenance guard after repair      GREEN PASS
:app:testFast --rerun-tasks                     PASS
git diff --check                                PASS
production changed-file allowlist               App file only
exact post-selection persistence ordering       PASS
```

One-shot CI files were removed after the product commit.

Cleanup carrier:

```text
d0bcbb6f6eaf9bfe31a81bc0f9c7efd73dc591fd
`chore(ci): remove TBSP 6L one-shot workflow`
```

## 5. Final acceptance gate

Full-checkpoint trigger:

```text
45a60a3c32c7471c68d89b7fb886c4dbb00f1781
`[full-ci] test: run TBSP 6L durability acceptance`
```

Same-head acceptance evidence:

```text
CI #1167 / run 33344886176                    SUCCESS
full checkpoint classification                SUCCESS
Android :app:testFull                         SUCCESS
Android :app:assembleDebug                    SUCCESS
ASP contract tests                            SUCCESS
Real Clingo cross-validation                  SUCCESS
CI aggregate gate                             SUCCESS
R2 #1090 / run 33344886170                    SUCCESS
```

TBSP-1 through TBSP-6L are now complete and accepted. PR #57 remains Draft solely because Ready/merge requires explicit user authorization.

MS-SETUP generic multi-script setup architecture remains a separate post-merge campaign and has not started.
