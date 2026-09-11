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

internal data class TroubleBrewingSetupDealAssignment(
    val seat: Int,
    val playerName: String,
    val actualRoleId: String,
    val shownRoleId: String,
)

internal data class TroubleBrewingSetupDealPlan(
    val datasetId: String,
    val schemaVersion: Int,
    val presetId: String,
    val playerCount: Int,
    val gameSeed: Long,
    val selectedDrunkShownRole: String?,
    val assignments: List<TroubleBrewingSetupDealAssignment>,
)

internal object TroubleBrewingSetupDealPlanner {
    fun plan(
        selection: TroubleBrewingSetupPresetSelection,
        orderedPlayerNames: List<String>,
        previousPlayerStartingIdentities: List<TroubleBrewingPlayerStartingIdentity> = emptyList(),
    ): TroubleBrewingSetupDealPlan = planWithRecentHistory(
        selection = selection,
        orderedPlayerNames = orderedPlayerNames,
        recentPlayerStartingIdentityHistory = if (previousPlayerStartingIdentities.isEmpty()) {
            TroubleBrewingPlayerStartingIdentityHistory.EMPTY
        } else {
            TroubleBrewingPlayerStartingIdentityHistory(
                recentGames = listOf(previousPlayerStartingIdentities),
            )
        },
    )

    fun plan(
        selection: TroubleBrewingSetupPresetSelection,
        orderedPlayerNames: List<String>,
        recentPlayerStartingIdentityHistory: TroubleBrewingPlayerStartingIdentityHistory,
    ): TroubleBrewingSetupDealPlan = planWithRecentHistory(
        selection = selection,
        orderedPlayerNames = orderedPlayerNames,
        recentPlayerStartingIdentityHistory = recentPlayerStartingIdentityHistory,
    )

    private fun planWithRecentHistory(
        selection: TroubleBrewingSetupPresetSelection,
        orderedPlayerNames: List<String>,
        recentPlayerStartingIdentityHistory: TroubleBrewingPlayerStartingIdentityHistory,
    ): TroubleBrewingSetupDealPlan {
        require(selection.playerCount == selection.preset.playerCount) {
            "Selected Trouble Brewing preset player count does not match selection provenance."
        }
        require(orderedPlayerNames.size == selection.playerCount) {
            "Ordered Trouble Brewing player identities must match selected preset player count."
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

        val actualRoleIds = (
            selection.preset.townsfolk +
                selection.preset.outsiders +
                selection.preset.minions +
                selection.preset.demons
            ).sorted()
        require(actualRoleIds.size == selection.playerCount) {
            "Selected Trouble Brewing preset role count does not match player count."
        }
        val selectedDrunkShownRole = validatedDrunkShownRole(
            selection = selection,
            actualRoleIds = actualRoleIds,
        )
        val roleTokens = actualRoleIds.map { actualRoleId ->
            RoleToken(
                actualRoleId = actualRoleId,
                shownRoleId = if (actualRoleId == DRUNK_ROLE_ID) {
                    requireNotNull(selectedDrunkShownRole)
                } else {
                    actualRoleId
                },
                actualRoleCategory = categoryOf(selection.preset, actualRoleId),
            )
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
                selection = selection,
                orderedPlayerNames = orderedPlayerNames,
                roleTokens = roleTokens,
                recentByPlayer = recentByPlayer,
            )
        } else {
            legacySeatOrderedRoleTokens(
                selection = selection,
                roleTokens = roleTokens,
            )
        }

        return TroubleBrewingSetupDealPlan(
            datasetId = selection.datasetId,
            schemaVersion = selection.schemaVersion,
            presetId = selection.presetId,
            playerCount = selection.playerCount,
            gameSeed = selection.gameSeed,
            selectedDrunkShownRole = selectedDrunkShownRole,
            assignments = orderedPlayerNames.mapIndexed { index, playerName ->
                val roleToken = seatOrderedRoleTokens[index]
                TroubleBrewingSetupDealAssignment(
                    seat = index + 1,
                    playerName = playerName,
                    actualRoleId = roleToken.actualRoleId,
                    shownRoleId = roleToken.shownRoleId,
                )
            },
        )
    }

    private fun legacySeatOrderedRoleTokens(
        selection: TroubleBrewingSetupPresetSelection,
        roleTokens: List<RoleToken>,
    ): List<RoleToken> = roleTokens.sortedWith(
        Comparator { left, right ->
            val leftKey = seatOrderKey(selection, left.actualRoleId)
            val rightKey = seatOrderKey(selection, right.actualRoleId)
            val keyComparison = java.lang.Long.compareUnsigned(leftKey, rightKey)
            if (keyComparison != 0) {
                keyComparison
            } else {
                left.actualRoleId.compareTo(right.actualRoleId)
            }
        },
    )

    private fun minimumRotationCostAssignment(
        selection: TroubleBrewingSetupPresetSelection,
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
                        val leftKey = rotationTieKey(selection, playerIndex, playerName, left)
                        val rightKey = rotationTieKey(selection, playerIndex, playerName, right)
                        val keyComparison = java.lang.Long.compareUnsigned(leftKey, rightKey)
                        when {
                            keyComparison != 0 -> keyComparison
                            left.actualRoleId != right.actualRoleId -> left.actualRoleId.compareTo(right.actualRoleId)
                            left.shownRoleId != right.shownRoleId -> left.shownRoleId.compareTo(right.shownRoleId)
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
        preset: TroubleBrewingSetupPreset,
        roleId: String,
    ): TroubleBrewingStartingRoleCategory = when (roleId) {
        in preset.townsfolk -> TroubleBrewingStartingRoleCategory.TOWNSFOLK
        in preset.outsiders -> TroubleBrewingStartingRoleCategory.OUTSIDER
        in preset.minions -> TroubleBrewingStartingRoleCategory.MINION
        in preset.demons -> TroubleBrewingStartingRoleCategory.DEMON
        else -> error("Trouble Brewing role $roleId is not part of the selected preset.")
    }

    private fun validatedDrunkShownRole(
        selection: TroubleBrewingSetupPresetSelection,
        actualRoleIds: List<String>,
    ): String? {
        if (DRUNK_ROLE_ID !in actualRoleIds) {
            require(selection.selectedDrunkShownRole == null) {
                "Non-Drunk Trouble Brewing preset must not carry a Drunk shown role."
            }
            return null
        }

        val shownRoleId = requireNotNull(selection.selectedDrunkShownRole) {
            "Drunk Trouble Brewing preset requires the selector-owned shown role."
        }
        require(shownRoleId in selection.preset.drunkAsOptions) {
            "Selected Drunk shown role must come from the selected preset options."
        }
        require(shownRoleId !in actualRoleIds) {
            "Selected Drunk shown role must not already be an actual in-play role."
        }
        return shownRoleId
    }

    private fun seatOrderKey(
        selection: TroubleBrewingSetupPresetSelection,
        roleId: String,
    ): Long = MurmurHash3.low64Utf8(
        "tb-seat-v1|${selection.datasetId}|${selection.playerCount}|${selection.presetId}|" +
            "${selection.gameSeed}|$roleId",
    )

    private fun rotationTieKey(
        selection: TroubleBrewingSetupPresetSelection,
        playerIndex: Int,
        playerName: String,
        roleToken: RoleToken,
    ): Long = MurmurHash3.low64Utf8(
        "$ROTATION_NAMESPACE|${selection.datasetId}|${selection.playerCount}|${selection.presetId}|" +
            "${selection.gameSeed}|$playerIndex|$playerName|${roleToken.actualRoleId}|${roleToken.shownRoleId}",
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

    private const val DRUNK_ROLE_ID = "drunk"
    private const val ROTATION_NAMESPACE = "tb-role-rotation-v1"
    private const val MAX_OPTIMIZED_PLAYER_COUNT = 15
    private const val TWO_GAMES_AGO_WEIGHT = 2
    private const val THREE_GAMES_AGO_WEIGHT = 1
}
