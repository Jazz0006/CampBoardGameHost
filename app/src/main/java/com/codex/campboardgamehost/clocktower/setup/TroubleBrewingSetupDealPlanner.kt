package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.MurmurHash3

internal enum class TroubleBrewingStartingRoleCategory {
    TOWNSFOLK,
    OUTSIDER,
    MINION,
    DEMON,
}

internal data class TroubleBrewingPlayerStartingIdentity(
    val playerKey: String,
    val actualRoleId: String,
    val shownRoleId: String,
    val actualRoleCategory: TroubleBrewingStartingRoleCategory,
)

internal object TroubleBrewingSetupDealPlanner {
    /**
     * DLB visible-roster seating path.
     *
     * Only identities players can actually be shown participate here. A Drunk-bearing setup has no
     * Drunk token in this seating surface; the extra visible Townsfolk is therefore treated exactly
     * like any other Townsfolk until the later canonical Drunk commitment.
     */
    fun planVisibleRoster(
        datasetId: String,
        schemaVersion: Int,
        preset: TroubleBrewingSetupPreset,
        gameSeed: Long,
        visibleRoster: TroubleBrewingVisibleRoster,
        orderedPlayerNames: List<String>,
        recentPlayerStartingIdentityHistory: TroubleBrewingPlayerStartingIdentityHistory =
            TroubleBrewingPlayerStartingIdentityHistory.EMPTY,
    ): List<TroubleBrewingShownSeatAssignment> {
        require(visibleRoster.visibleRoleIds.size == preset.playerCount) {
            "Trouble Brewing visible roster must match the selected preset player count."
        }
        require(orderedPlayerNames.size == preset.playerCount) {
            "Ordered Trouble Brewing player identities must match the visible roster player count."
        }
        recentPlayerStartingIdentityHistory.recentGames.forEachIndexed { gameIndex, identities ->
            require(identities.map { it.playerKey }.distinct().size == identities.size) {
                "Trouble Brewing player starting identities for recent game $gameIndex must contain unique player keys."
            }
            identities.forEach { identity ->
                require(identity.playerKey.isNotBlank()) {
                    "Previous Trouble Brewing player key must not be blank."
                }
                require(identity.actualRoleId.isNotBlank() && identity.shownRoleId.isNotBlank()) {
                    "Previous Trouble Brewing starting identity roles must not be blank."
                }
            }
        }

        val context = SeatingContext(
            datasetId = datasetId,
            schemaVersion = schemaVersion,
            presetId = preset.id,
            playerCount = preset.playerCount,
            gameSeed = gameSeed,
        )
        val roleTokens = visibleRoster.visibleRoleIds
            .sorted()
            .map { roleId ->
                RoleToken(
                    actualRoleId = roleId,
                    shownRoleId = roleId,
                    actualRoleCategory = categoryOf(visibleRoster, roleId),
                )
            }
        require(roleTokens.size == context.playerCount) {
            "Trouble Brewing visible role count does not match player count."
        }

        val recentByPlayer = recentPlayerStartingIdentityHistory.recentGames.map { identities ->
            identities.associateBy { it.playerKey }
        }
        val currentPlayerKeys = orderedPlayerNames.toSet()
        val hasUsableRotationHistory = recentByPlayer.any { game ->
            game.keys.any(currentPlayerKeys::contains)
        }
        val seatOrderedRoleTokens = if (hasUsableRotationHistory) {
            minimumRotationCostAssignment(
                context = context,
                orderedPlayerNames = orderedPlayerNames,
                roleTokens = roleTokens,
                recentByPlayer = recentByPlayer,
            )
        } else {
            legacySeatOrderedRoleTokens(
                context = context,
                roleTokens = roleTokens,
            )
        }

        return orderedPlayerNames.mapIndexed { index, playerName ->
            TroubleBrewingShownSeatAssignment(
                seat = index + 1,
                playerName = playerName,
                shownRoleId = seatOrderedRoleTokens[index].shownRoleId,
            )
        }
    }

    private fun legacySeatOrderedRoleTokens(
        context: SeatingContext,
        roleTokens: List<RoleToken>,
    ): List<RoleToken> = roleTokens.sortedWith(
        Comparator { left, right ->
            val leftKey = seatOrderKey(context, left.actualRoleId)
            val rightKey = seatOrderKey(context, right.actualRoleId)
            val keyComparison = java.lang.Long.compareUnsigned(leftKey, rightKey)
            if (keyComparison != 0) {
                keyComparison
            } else {
                left.actualRoleId.compareTo(right.actualRoleId)
            }
        },
    )

    private fun minimumRotationCostAssignment(
        context: SeatingContext,
        orderedPlayerNames: List<String>,
        roleTokens: List<RoleToken>,
        recentByPlayer: List<Map<String, TroubleBrewingPlayerStartingIdentity>>,
    ): List<RoleToken> {
        val playerCount = orderedPlayerNames.size
        require(playerCount <= MAX_OPTIMIZED_PLAYER_COUNT) {
            "Trouble Brewing role-rotation optimizer supports at most $MAX_OPTIMIZED_PLAYER_COUNT players."
        }
        val stateCount = 1 shl playerCount
        val bestCosts = arrayOfNulls<RotationCost>(stateCount)
        bestCosts[stateCount - 1] = RotationCost.ZERO

        fun solve(usedMask: Int): RotationCost {
            bestCosts[usedMask]?.let { return it }

            val playerIndex = Integer.bitCount(usedMask)
            val playerName = orderedPlayerNames[playerIndex]
            var best: RotationCost? = null
            roleTokens.indices.forEach { tokenIndex ->
                val bit = 1 shl tokenIndex
                if (usedMask and bit == 0) {
                    val token = roleTokens[tokenIndex]
                    val candidate = edgeCost(playerName, recentByPlayer, token) + solve(usedMask or bit)
                    if (best == null || candidate < requireNotNull(best)) {
                        best = candidate
                    }
                }
            }
            return requireNotNull(best).also { bestCosts[usedMask] = it }
        }

        solve(0)
        var usedMask = 0
        return buildList(playerCount) {
            orderedPlayerNames.forEachIndexed { playerIndex, playerName ->
                val targetCost = solve(usedMask)
                val candidates = roleTokens.indices.filter { tokenIndex ->
                    val bit = 1 shl tokenIndex
                    if (usedMask and bit != 0) {
                        false
                    } else {
                        edgeCost(playerName, recentByPlayer, roleTokens[tokenIndex]) + solve(usedMask or bit) == targetCost
                    }
                }
                val chosenTokenIndex = candidates.minWithOrNull(
                    Comparator { leftIndex, rightIndex ->
                        val left = roleTokens[leftIndex]
                        val right = roleTokens[rightIndex]
                        val leftKey = rotationTieKey(context, playerIndex, playerName, left)
                        val rightKey = rotationTieKey(context, playerIndex, playerName, right)
                        val keyComparison = java.lang.Long.compareUnsigned(leftKey, rightKey)
                        when {
                            keyComparison != 0 -> keyComparison
                            left.actualRoleId != right.actualRoleId ->
                                left.actualRoleId.compareTo(right.actualRoleId)
                            left.shownRoleId != right.shownRoleId ->
                                left.shownRoleId.compareTo(right.shownRoleId)
                            else -> leftIndex.compareTo(rightIndex)
                        }
                    },
                ) ?: error("Trouble Brewing role-rotation optimizer could not reconstruct an assignment.")

                add(roleTokens[chosenTokenIndex])
                usedMask = usedMask or (1 shl chosenTokenIndex)
            }
        }
    }

    private fun edgeCost(
        playerName: String,
        recentByPlayer: List<Map<String, TroubleBrewingPlayerStartingIdentity>>,
        token: RoleToken,
    ): RotationCost {
        val last = recentByPlayer.getOrNull(0)?.get(playerName)
        val twoGamesAgo = recentByPlayer.getOrNull(1)?.get(playerName)
        val threeGamesAgo = recentByPlayer.getOrNull(2)?.get(playerName)
        return RotationCost(
            lastExactRepeats = exactRepeat(last, token),
            lastSpecialCategoryRepeats = specialCategoryRepeat(last, token),
            olderWeightedExactRepeats =
                TWO_GAMES_AGO_WEIGHT * exactRepeat(twoGamesAgo, token) +
                    THREE_GAMES_AGO_WEIGHT * exactRepeat(threeGamesAgo, token),
            olderWeightedSpecialCategoryRepeats =
                TWO_GAMES_AGO_WEIGHT * specialCategoryRepeat(twoGamesAgo, token) +
                    THREE_GAMES_AGO_WEIGHT * specialCategoryRepeat(threeGamesAgo, token),
        )
    }

    private fun exactRepeat(
        previous: TroubleBrewingPlayerStartingIdentity?,
        token: RoleToken,
    ): Int = if (previous?.shownRoleId == token.shownRoleId) 1 else 0

    private fun specialCategoryRepeat(
        previous: TroubleBrewingPlayerStartingIdentity?,
        token: RoleToken,
    ): Int = if (
        previous != null &&
        token.actualRoleCategory != TroubleBrewingStartingRoleCategory.TOWNSFOLK &&
        previous.actualRoleCategory == token.actualRoleCategory
    ) {
        1
    } else {
        0
    }

    private fun categoryOf(
        visibleRoster: TroubleBrewingVisibleRoster,
        roleId: String,
    ): TroubleBrewingStartingRoleCategory = when (roleId) {
        in visibleRoster.townsfolkRoleIds -> TroubleBrewingStartingRoleCategory.TOWNSFOLK
        in visibleRoster.outsiderRoleIds -> TroubleBrewingStartingRoleCategory.OUTSIDER
        in visibleRoster.minionRoleIds -> TroubleBrewingStartingRoleCategory.MINION
        in visibleRoster.demonRoleIds -> TroubleBrewingStartingRoleCategory.DEMON
        else -> error("Trouble Brewing visible role $roleId is not part of the visible roster.")
    }

    private fun seatOrderKey(
        context: SeatingContext,
        roleId: String,
    ): Long = MurmurHash3.low64Utf8(
        "tb-seat-v1|${context.datasetId}|${context.playerCount}|${context.presetId}|" +
            "${context.gameSeed}|$roleId",
    )

    private fun rotationTieKey(
        context: SeatingContext,
        playerIndex: Int,
        playerName: String,
        roleToken: RoleToken,
    ): Long = MurmurHash3.low64Utf8(
        "$ROTATION_NAMESPACE|${context.datasetId}|${context.playerCount}|${context.presetId}|" +
            "${context.gameSeed}|$playerIndex|$playerName|${roleToken.actualRoleId}|${roleToken.shownRoleId}",
    )

    private data class SeatingContext(
        val datasetId: String,
        val schemaVersion: Int,
        val presetId: String,
        val playerCount: Int,
        val gameSeed: Long,
    )

    private data class RoleToken(
        val actualRoleId: String,
        val shownRoleId: String,
        val actualRoleCategory: TroubleBrewingStartingRoleCategory,
    )

    private data class RotationCost(
        val lastExactRepeats: Int,
        val lastSpecialCategoryRepeats: Int,
        val olderWeightedExactRepeats: Int,
        val olderWeightedSpecialCategoryRepeats: Int,
    ) : Comparable<RotationCost> {
        override fun compareTo(other: RotationCost): Int = when {
            lastExactRepeats != other.lastExactRepeats ->
                lastExactRepeats.compareTo(other.lastExactRepeats)
            lastSpecialCategoryRepeats != other.lastSpecialCategoryRepeats ->
                lastSpecialCategoryRepeats.compareTo(other.lastSpecialCategoryRepeats)
            olderWeightedExactRepeats != other.olderWeightedExactRepeats ->
                olderWeightedExactRepeats.compareTo(other.olderWeightedExactRepeats)
            else -> olderWeightedSpecialCategoryRepeats.compareTo(other.olderWeightedSpecialCategoryRepeats)
        }

        operator fun plus(other: RotationCost): RotationCost = RotationCost(
            lastExactRepeats = lastExactRepeats + other.lastExactRepeats,
            lastSpecialCategoryRepeats = lastSpecialCategoryRepeats + other.lastSpecialCategoryRepeats,
            olderWeightedExactRepeats = olderWeightedExactRepeats + other.olderWeightedExactRepeats,
            olderWeightedSpecialCategoryRepeats =
                olderWeightedSpecialCategoryRepeats + other.olderWeightedSpecialCategoryRepeats,
        )

        companion object {
            val ZERO = RotationCost(
                lastExactRepeats = 0,
                lastSpecialCategoryRepeats = 0,
                olderWeightedExactRepeats = 0,
                olderWeightedSpecialCategoryRepeats = 0,
            )
        }
    }

    private const val ROTATION_NAMESPACE = "tb-role-rotation-v1"
    private const val MAX_OPTIMIZED_PLAYER_COUNT = 15
    private const val TWO_GAMES_AGO_WEIGHT = 2
    private const val THREE_GAMES_AGO_WEIGHT = 1
}
