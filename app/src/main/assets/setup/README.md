# Trouble Brewing setup presets

`trouble_brewing_setup_presets_v2_final.json` retains its established filename and dataset ID for current-format recovery compatibility; its payload is schema version 3. The complete visible Townsfolk roster is stored in `townsfolk`. `drunk` is a boolean that must agree with the presence of `drunk` in `outsiders`. The Drunk remains an actual outsider until the later canonical assignment, while all listed Townsfolk are eligible shown identities and later Drunk candidates.

The one-time deterministic conversion and its exact original-option mapping are in `tools/setup/migrate_tb_drunk_templates.py` and `docs/TB_SETUP_SIMPLIFICATION_TEMPLATE_AUDIT.json`. The static script is not part of runtime setup selection. The schema is `trouble_brewing_setup_presets_v3.schema.json`; semantic composition and character validation remain in the typed Kotlin validator.
