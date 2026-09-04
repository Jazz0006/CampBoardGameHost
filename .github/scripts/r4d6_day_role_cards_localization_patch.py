from pathlib import Path

role_path = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerRoleLocalization.kt")
host_path = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")

role_text = role_path.read_text(encoding="utf-8")
host_text = host_path.read_text(encoding="utf-8")

old_helper = '''internal fun clocktowerRoleLabel(
    roleId: RoleId,
    language: String,
): String {
    val role = ClocktowerScript.values()
        .asSequence()
        .flatMap { script -> clocktowerRolesForScript(script).asSequence() }
        .firstOrNull { candidate -> candidate.enName == roleId.value }
        ?: return roleId.value
    return if (language == "en") role.enName else role.zhName
}
'''
new_helper = '''internal fun clocktowerRoleLabel(
    roleId: RoleId,
    language: String,
): String {
    val role = ClocktowerScript.values()
        .asSequence()
        .flatMap { script -> clocktowerRolesForScript(script).asSequence() }
        .firstOrNull { candidate -> candidate.enName == roleId.value }
        ?: return roleId.value
    return role.nameFor(language)
}

/**
 * Resolves a Host-table role label from the current game's PlayerCard role objects first.
 * This keeps Day presentation on the same localization authority as Night presentation and
 * only falls back to the catalog for defensive compatibility.
 */
internal fun clocktowerRoleLabel(
    roleId: RoleId,
    language: String,
    cards: List<PlayerCard>,
): String {
    val role = cards.asSequence()
        .flatMap { card -> sequenceOf(card.clocktowerRole, card.clocktowerShownRole) }
        .filterNotNull()
        .firstOrNull { candidate -> candidate.enName == roleId.value }
        ?: return clocktowerRoleLabel(roleId, language)
    return role.nameFor(language)
}
'''
if role_text.count(old_helper) != 1:
    raise SystemExit("Expected exactly one Clocktower role localization helper anchor")
role_text = role_text.replace(old_helper, new_helper, 1)

old_resolver = 'roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },'
new_resolver = 'roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language, cards) },'
if host_text.count(old_resolver) != 6:
    raise SystemExit(f"Expected exactly 6 Day role resolvers, found {host_text.count(old_resolver)}")
if new_resolver in host_text:
    raise SystemExit("PlayerCard-backed Day resolver already present")
host_text = host_text.replace(old_resolver, new_resolver)
if host_text.count(new_resolver) != 6:
    raise SystemExit("Expected exactly 6 PlayerCard-backed Day role resolvers after patch")

role_path.write_text(role_text, encoding="utf-8", newline="\n")
host_path.write_text(host_text, encoding="utf-8", newline="\n")
