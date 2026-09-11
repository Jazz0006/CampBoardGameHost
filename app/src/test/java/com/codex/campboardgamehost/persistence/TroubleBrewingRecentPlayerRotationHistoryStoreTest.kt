package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingPlayerStartingIdentity
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingStartingRoleCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class TroubleBrewingRecentPlayerRotationHistoryStoreTest {
    @Test
    fun `recent player rotation history preserves newest three games across player counts including empty legacy gap`() {
        var raw: String? = null
        val store = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded -> raw = encoded; true },
        )
        val oldest = record(
            presetId = "oldest-five",
            playerCount = 5,
            identities = identities(5, "oldest"),
        )
        val thirdNewest = record(
            presetId = "third-newest-six",
            playerCount = 6,
            identities = identities(6, "third"),
        )
        val secondNewestLegacyGap = record(
            presetId = "second-newest-seven",
            playerCount = 7,
            identities = emptyList(),
        )
        val newest = record(
            presetId = "newest-eight",
            playerCount = 8,
            identities = identities(8, "newest"),
        )

        store.recordCompletedGame("oldest", oldest)
        store.recordCompletedGame("third", thirdNewest)
        store.recordCompletedGame("second", secondNewestLegacyGap)
        store.recordCompletedGame("newest", newest)

        val history = store.recentPlayerStartingIdentityHistoryFor(
            datasetId = "test-dataset",
            schemaVersion = 2,
        )

        assertEquals(
            listOf(
                newest.playerStartingIdentities,
                emptyList<TroubleBrewingPlayerStartingIdentity>(),
                thirdNewest.playerStartingIdentities,
            ),
            history.recentGames,
        )
    }

    @Test
    fun `recent player rotation history ignores other dataset and schema without changing recency slots`() {
        var raw: String? = null
        val store = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded -> raw = encoded; true },
        )
        val matchingOlder = record(
            presetId = "matching-older",
            playerCount = 5,
            identities = identities(5, "matching-older"),
        )
        val otherDataset = record(
            presetId = "other-dataset",
            playerCount = 6,
            datasetId = "other-dataset",
            identities = identities(6, "other-dataset"),
        )
        val otherSchema = record(
            presetId = "other-schema",
            playerCount = 7,
            schemaVersion = 3,
            identities = identities(7, "other-schema"),
        )
        val matchingNewest = record(
            presetId = "matching-newest",
            playerCount = 8,
            identities = identities(8, "matching-newest"),
        )

        store.recordCompletedGame("matching-older", matchingOlder)
        store.recordCompletedGame("other-dataset", otherDataset)
        store.recordCompletedGame("other-schema", otherSchema)
        store.recordCompletedGame("matching-newest", matchingNewest)

        val history = store.recentPlayerStartingIdentityHistoryFor(
            datasetId = "test-dataset",
            schemaVersion = 2,
        )

        assertEquals(
            listOf(
                matchingNewest.playerStartingIdentities,
                matchingOlder.playerStartingIdentities,
            ),
            history.recentGames,
        )
    }

    private fun record(
        presetId: String,
        playerCount: Int,
        datasetId: String = "test-dataset",
        schemaVersion: Int = 2,
        identities: List<TroubleBrewingPlayerStartingIdentity>,
    ): TroubleBrewingSetupRotationRecord {
        val nonDemonRoles = (1 until playerCount).map { index -> "role_${presetId}_$index" }.toSet()
        val normalizedIdentities = if (identities.isEmpty()) {
            emptyList()
        } else {
            identities.mapIndexed { index, identity ->
                if (index == playerCount - 1) {
                    identity.copy(
                        actualRoleId = "imp",
                        shownRoleId = "imp",
                        actualRoleCategory = TroubleBrewingStartingRoleCategory.DEMON,
                    )
                } else {
                    val roleId = nonDemonRoles.elementAt(index)
                    identity.copy(actualRoleId = roleId, shownRoleId = roleId)
                }
            }
        }
        return TroubleBrewingSetupRotationRecord(
            datasetId = datasetId,
            schemaVersion = schemaVersion,
            presetId = presetId,
            playerCount = playerCount,
            realNonDemonRoleIds = nonDemonRoles,
            minionRoleIds = emptySet(),
            primaryStyleTag = "test",
            selectedDrunkShownRole = null,
            playerStartingIdentities = normalizedIdentities,
        )
    }

    private fun identities(
        playerCount: Int,
        prefix: String,
    ): List<TroubleBrewingPlayerStartingIdentity> = (1..playerCount).map { index ->
        TroubleBrewingPlayerStartingIdentity(
            playerKey = "Player $index",
            actualRoleId = "placeholder_${prefix}_$index",
            shownRoleId = "placeholder_${prefix}_$index",
            actualRoleCategory = TroubleBrewingStartingRoleCategory.TOWNSFOLK,
        )
    }
}
