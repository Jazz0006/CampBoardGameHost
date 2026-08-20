package com.codex.campboardgamehost

/**
 * Creates stable Werewolf persistence identity from the S3 board model. Legacy v1 settings can be
 * migrated only from persisted mechanical fields; no display-name or current-catalog guessing is
 * involved.
 */
internal object WerewolfPersistenceIdentityFactory {
    const val SEMANTIC_VERSION = "werewolf-r5_5-v1"

    fun fromBoard(
        board: WerewolfBoardDefinition,
        ruleOptions: WerewolfRuleOptions,
        sourceRevision: String? = null,
    ): PersistedWerewolfGameIdentity = PersistedWerewolfGameIdentity(
        board = PersistedGameContentIdentity(
            kind = PersistedVariantKind.WEREWOLF_BOARD,
            variantId = board.id.value,
            contentHash = board.contentHash,
            semanticVersion = SEMANTIC_VERSION,
            sourceRevision = sourceRevision,
        ),
        ruleOptions = ruleOptions,
    )

    fun fromLegacySetup(
        playerCount: Int,
        werewolfCount: Int,
        includeSeer: Boolean,
        includeWitch: Boolean,
        includeHunter: Boolean,
        ruleOptions: WerewolfRuleOptions,
        roleRegistry: WerewolfRoleRegistry,
        boardRegistry: WerewolfBoardRegistry,
    ): PersistedWerewolfGameIdentity {
        require(playerCount > 0) { "Legacy Werewolf playerCount must be positive." }
        require(werewolfCount > 0) { "Legacy Werewolf werewolfCount must be positive." }
        val specialCount = listOf(includeSeer, includeWitch, includeHunter).count { it }
        val villagerCount = playerCount - werewolfCount - specialCount
        require(villagerCount >= 0) { "Legacy Werewolf role composition exceeds player count." }

        val deck = linkedMapOf<WerewolfRoleId, Int>().apply {
            put(WerewolfRoleIds.WEREWOLF, werewolfCount)
            if (includeSeer) put(WerewolfRoleIds.SEER, 1)
            if (includeWitch) put(WerewolfRoleIds.WITCH, 1)
            if (includeHunter) put(WerewolfRoleIds.HUNTER, 1)
            if (villagerCount > 0) put(WerewolfRoleIds.VILLAGER, villagerCount)
        }
        deck.keys.forEach(roleRegistry::require)

        val candidate = WerewolfBoardDefinition.create(
            id = WerewolfBoardId("legacy_candidate"),
            name = "Legacy candidate",
            roleDeck = deck,
        )
        val builtInMatch = boardRegistry.definitions.singleOrNull { it.roleDeck == candidate.roleDeck }
        val resolvedBoard = builtInMatch ?: WerewolfBoardDefinition.create(
            id = WerewolfBoardId("legacy_custom_${candidate.contentHash.take(16)}"),
            name = "Legacy custom board",
            roleDeck = deck,
        )
        return fromBoard(
            board = resolvedBoard,
            ruleOptions = ruleOptions,
            sourceRevision = if (builtInMatch != null) "builtin-r5_5-s3" else "legacy-v1-derived",
        )
    }
}
