package com.codex.campboardgamehost

import java.security.MessageDigest

@JvmInline
internal value class WerewolfBoardId(val value: String) {
    init {
        require(ID_PATTERN.matches(value)) { "Werewolf board id must be lowercase alphanumeric/underscore." }
    }

    private companion object {
        val ID_PATTERN = Regex("[a-z0-9_]{1,64}")
    }
}

internal data class WerewolfBoardDefinition private constructor(
    val id: WerewolfBoardId,
    val name: String,
    val roleDeck: Map<WerewolfRoleId, Int>,
    val contentHash: String,
) {
    val playerCount: Int = roleDeck.values.sum()

    init {
        require(name.isNotBlank()) { "Werewolf board name cannot be blank." }
        require(roleDeck.isNotEmpty()) { "Werewolf board role deck cannot be empty." }
        require(roleDeck.values.all { it > 0 }) { "Werewolf board role counts must be positive." }
        require(CONTENT_HASH_PATTERN.matches(contentHash)) {
            "Werewolf board contentHash must be a 128-bit lowercase hexadecimal SHA-256 prefix."
        }
    }

    companion object {
        private val CONTENT_HASH_PATTERN = Regex("[0-9a-f]{32}")

        fun create(
            id: WerewolfBoardId,
            name: String,
            roleDeck: Map<WerewolfRoleId, Int>,
        ): WerewolfBoardDefinition {
            val normalizedDeck = roleDeck
                .filterValues { it > 0 }
                .toSortedMap(compareBy { it.value })
            require(normalizedDeck.isNotEmpty()) { "Werewolf board role deck cannot be empty." }
            return WerewolfBoardDefinition(
                id = id,
                name = name,
                roleDeck = normalizedDeck,
                contentHash = WerewolfBoardContentHasher.hash(normalizedDeck),
            )
        }
    }
}

internal class WerewolfBoardRegistry private constructor(
    val definitions: List<WerewolfBoardDefinition>,
    roleRegistry: WerewolfRoleRegistry,
) {
    private val byId = definitions.associateBy { it.id }

    init {
        kotlin.require(definitions.isNotEmpty()) { "Werewolf board registry cannot be empty." }
        kotlin.require(byId.size == definitions.size) { "Werewolf board ids must be unique." }
        definitions.forEach { board ->
            kotlin.require(board.roleDeck.keys.all { roleRegistry.find(it) != null }) {
                "Werewolf board '${board.id.value}' contains an unregistered role."
            }
        }
    }

    fun find(boardId: WerewolfBoardId): WerewolfBoardDefinition? = byId[boardId]

    fun require(boardId: WerewolfBoardId): WerewolfBoardDefinition =
        find(boardId) ?: error("Unknown Werewolf board id '${boardId.value}'.")

    companion object {
        fun builtIn(roleRegistry: WerewolfRoleRegistry): WerewolfBoardRegistry = WerewolfBoardRegistry(
            definitions = werewolfTemplates.map { template -> template.toBoardDefinition() },
            roleRegistry = roleRegistry,
        )
    }
}

private fun WerewolfTemplate.toBoardDefinition(): WerewolfBoardDefinition {
    val specialCount = listOf(includeSeer, includeWitch, includeHunter).count { it }
    val villagerCount = playerCount - werewolfCount - specialCount
    require(villagerCount >= 0) { "Legacy Werewolf template has more roles than players." }

    val deck = linkedMapOf<WerewolfRoleId, Int>().apply {
        put(WerewolfRoleIds.WEREWOLF, werewolfCount)
        if (includeSeer) put(WerewolfRoleIds.SEER, 1)
        if (includeWitch) put(WerewolfRoleIds.WITCH, 1)
        if (includeHunter) put(WerewolfRoleIds.HUNTER, 1)
        if (villagerCount > 0) put(WerewolfRoleIds.VILLAGER, villagerCount)
    }
    return WerewolfBoardDefinition.create(
        id = WerewolfBoardId("classic_$playerCount"),
        name = "Classic $playerCount",
        roleDeck = deck,
    )
}

private object WerewolfBoardContentHasher {
    fun hash(roleDeck: Map<WerewolfRoleId, Int>): String {
        val canonical = roleDeck.entries
            .sortedBy { it.key.value }
            .joinToString(prefix = "{", postfix = "}", separator = ",") { (roleId, count) ->
                "${roleId.value}:$count"
            }
        return MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
            .take(16)
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }
}
