# Trouble Brewing setup presets

`trouble_brewing_setup_presets_v2_final.json` retains its established filename and dataset ID; its payload is schema version 3. The complete visible Townsfolk roster is stored in `townsfolk`. `drunk` is a boolean and **`drunk` must never appear in `outsiders`**. When true, the actual Drunk is committed from the displayed Townsfolk after the shown roster is fixed; all listed Townsfolk remain legal shown identities and later Drunk candidates.

The completed one-time conversion is recorded in `docs/TB_SETUP_SIMPLIFICATION_TEMPLATE_AUDIT.json`; its migration script is retired. The schema is `trouble_brewing_setup_presets_v3.schema.json`; semantic composition and character validation remain in the typed Kotlin validator.
