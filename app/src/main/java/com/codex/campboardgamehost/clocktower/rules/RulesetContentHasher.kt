package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.RoleId
import java.security.MessageDigest

internal object RulesetContentHasher {
    fun hash(
        knowledge: RulesetKnowledge,
        inPlayRoleIds: Set<RoleId>,
    ): String {
        val canonical = canonicalJson(knowledge, inPlayRoleIds)
        return MessageDigest
            .getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
            .take(16)
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }

    fun canonicalJson(
        knowledge: RulesetKnowledge,
        inPlayRoleIds: Set<RoleId>,
    ): String {
        require(inPlayRoleIds.isNotEmpty()) { "At least one in-play role is required." }
        val knownRoleIds = knowledge.characters.map { it.roleId }.toSet()
        require(inPlayRoleIds.all { it in knownRoleIds }) { "All in-play roles must exist in the ruleset knowledge." }

        val characters = knowledge.characters
            .filter { it.roleId in inPlayRoleIds }
            .sortedBy { it.roleId.value }
            .joinToString(",") { character ->
                "{\"id\":${quoted(character.roleId.value)},\"text\":${quoted(character.abilityText)}}"
            }
        val firstNight = knowledge.firstNightOrder
            .filter { it in inPlayRoleIds }
            .joinToString(",") { quoted(it.value) }
        val otherNights = knowledge.otherNightOrder
            .filter { it in inPlayRoleIds }
            .joinToString(",") { quoted(it.value) }
        val jinxes = knowledge.jinxes
            .filter { it.firstRoleId in inPlayRoleIds && it.secondRoleId in inPlayRoleIds }
            .sortedWith(compareBy<RuleJinx> { it.firstRoleId.value }.thenBy { it.secondRoleId.value })
            .joinToString(",") { jinx ->
                "{\"firstRoleId\":${quoted(jinx.firstRoleId.value)}," +
                    "\"secondRoleId\":${quoted(jinx.secondRoleId.value)},\"text\":${quoted(jinx.text)}}"
            }
        return "{\"characters\":[$characters],\"firstNightOrder\":[$firstNight]," +
            "\"jinxes\":[$jinxes],\"otherNightOrder\":[$otherNights]}"
    }

    private fun quoted(value: String): String = buildString {
        append('"')
        value.forEach { character ->
            when (character) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (character.code < 0x20) {
                    append("\\u%04x".format(character.code))
                } else {
                    append(character)
                }
            }
        }
        append('"')
    }
}
