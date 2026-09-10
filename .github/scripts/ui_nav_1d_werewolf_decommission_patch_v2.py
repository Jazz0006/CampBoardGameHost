from pathlib import Path

base_script = Path(".github/scripts/ui_nav_1d_werewolf_decommission_patch.py")
source = base_script.read_text(encoding="utf-8")

old = '''root = replace_exact(\n    root,\n    """    Screen.WerewolfSettings,\\n""",\n    "",\n    label="inactive screen WerewolfSettings",\n)\n'''
new = '''werewolf_settings_ref = "    Screen.WerewolfSettings,\\n"\nif root.count(werewolf_settings_ref) != 2:\n    raise SystemExit(\n        f"Screen.WerewolfSettings refs: expected 2 exact matches, got {root.count(werewolf_settings_ref)}"\n    )\nroot = root.replace(werewolf_settings_ref, "", 1)\n'''

if source.count(old) != 1:
    raise SystemExit(f"base patch navigation anchor count {source.count(old)}, expected 1")
source = source.replace(old, new)

exec(compile(source, str(base_script), "exec"), {"__name__": "__main__"})
