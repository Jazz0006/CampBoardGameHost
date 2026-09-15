# NEXT DEVELOPMENT HANDOFF — Experienced Night Flow Correctness

> Date: 2026-09-14 Australia/Sydney  
> Status: **CLOSEOUT — S1/S2/S3/S4 automated acceptance complete; PR #123 merge authorized**
> Program: Clocktower Storyteller mobile flow correctness

This file is archived historical evidence. It is no longer execution authority. See `docs/NEXT_DEVELOPMENT_HANDOFF.md` for current work.

## Historical campaign summary

Real-device Experienced-mode testing exposed a black-screen regression after Demon night-kill confirmation. The campaign established the invariant that every legal night confirmation resolves to either a valid renderable next night step or explicit night completion/Dawn, never an out-of-range/blank ownerless state.

Completed slices:

- S1 removed the out-of-range dynamic night cursor sentinel and introduced typed post-confirm forward resolution.
- S2 made fullscreen surface ownership total through typed `ClocktowerNightSurfacePlan` ownership.
- S3 unified required single-target confirmation eligibility around upstream legal candidate domains.
- S4 added the Experienced night-flow regression matrix and corrected Poison/Monk draft-toggle interaction behavior.

The campaign was merged as PR #123. Detailed evidence remains in:

- `docs/EXPERIENCED_NIGHT_FLOW_S2_SURFACE_TOTALITY_AUDIT_2026-09-14.md`
- `docs/EXPERIENCED_NIGHT_FLOW_S3_TARGET_ELIGIBILITY_AUDIT_2026-09-14.md`
- `docs/EXPERIENCED_NIGHT_FLOW_S4_REGRESSION_MATRIX_AUDIT_2026-09-14.md`

Stable historical rule:

> After every legal night confirmation, the application resolves to a valid renderable next night step or explicit night-completion/Dawn transition; there is no third out-of-range, blank or ownerless UI state.
