# GCR blocker acceptance checkpoint — 2026-08-28

> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Policy: keep draft/open/unmerged until explicit authorization.

## Accepted blocker checkpoints

### GCR-1 — Current Demon authority / cross-night succession

Accepted executable checkpoint:

```text
974f617adffd08cc7de0924f6fea4f96f3d73f0c
```

Evidence:

```text
CI #959 / run 33174380352 SUCCESS
R2 run 33174380336 SUCCESS
```

### GCR-2 — Poisoned Spy fail-safe information policy

Accepted policy-regression checkpoint:

```text
deced404a1a93d59794f882f7cbefa9f9b0e37fe
```

Accepted product policy:

```text
healthy Spy
-> wake normally
-> show the true Grimoire

poisoned Spy
-> wake normally
-> show no Grimoire information
-> Host may explicitly identify the poisoned state
-> do not create/persist a Spy Grimoire information observation
```

This is an intentional product simplification / house-rule deviation. Fabricated Grimoire generation is not authorized.

Evidence at the policy-regression checkpoint:

```text
CI #962 / run 33175402009
- Android FAST unit tests SUCCESS
- CI gate SUCCESS
R2 run 33175402008 SUCCESS
```

The production information-publication guard returns before recording an observation when the display proposition is absent and the actor is typed as DRUNK/POISONED, so the accepted poisoned-Spy path does not persist a Grimoire observation.

## Final blocker acceptance gate

This checkpoint requests a real full CI execution. Acceptance requires all selected full routes to execute successfully, including:

```text
:app:testFull
:app:assembleDebug
ASP contract tests
Real Clingo cross-validation
CI gate
R2 main-thread boundary
```

Skipped, cached-only, zero-job or `UP-TO-DATE` routes are not sufficient proof when the route is required.

After full acceptance succeeds, update `docs/CURRENT_DEVELOPMENT_ROADMAP.md` and the active GCR handoff with the final accepted full checkpoint. Do not merge or mark PR #54 ready automatically.
