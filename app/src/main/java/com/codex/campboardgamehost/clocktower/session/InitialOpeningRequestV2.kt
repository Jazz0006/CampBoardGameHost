package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import java.security.MessageDigest

internal enum class OpeningPlayerExperienceV1 { BEGINNER, REGULAR, VETERAN, UNKNOWN }

/** Immutable pre-commit request facts and identities; [InitialOpeningDomainV3] derives the model's choices. */
internal class InitialOpeningRequestV2 private constructor(
    val snapshot: TroubleBrewingGameSnapshotV1,
    val hasDrunk: Boolean,
    val playerExperience: Map<Int, OpeningPlayerExperienceV1>,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val rosterFingerprint: String,
) {
    val shownRoles: Map<Int, String> = snapshot.grimoireSeats.associate {
        it.seat to (it.shownRoleId as SnapshotField.Known<String>).value
    }
    /** Every shown Townsfolk is still a possible Drunk at this boundary. */
    val drunkCandidateSeats: List<Int> = snapshot.grimoireSeats
        .filter { hasDrunk && it.actualRoleId == SnapshotField.Uncommitted }.map { it.seat }.sorted()

    companion object {
        fun fromHostSnapshot(
            snapshot: TroubleBrewingGameSnapshotV1,
            registry: ClocktowerCharacterRegistry,
            playerExperience: Map<Int, OpeningPlayerExperienceV1> = emptyMap(),
            gameStateRevision: Long = 0,
            playerInputRevision: Long = 0,
        ): InitialOpeningRequestV2 {
            require(snapshot.position.stage == TroubleBrewingSnapshotStage.SETUP_PRECOMMIT)
            require(gameStateRevision >= 0 && playerInputRevision >= 0)
            val seats = snapshot.grimoireSeats
            require(seats.map { it.seat }.sorted() == (1..seats.size).toList())
            val roles = registry.definitions.map { it.externalId }.toSet()
            require(seats.all { (it.shownRoleId as? SnapshotField.Known<String>)?.value in roles })
            require(playerExperience.keys.all { it in 1..seats.size })
            val hasDrunk = (snapshot.setupState.hasDrunk as? SnapshotField.Known<Boolean>)?.value
                ?: error("Host hasDrunk must be known.")
            val rosterKey = buildString {
                append(snapshot.gameId).append('|').append(snapshot.gameSeed).append('|').append(hasDrunk)
                seats.sortedBy { it.seat }.forEach { seat ->
                    append('|').append(seat.seat).append(':')
                    append((seat.shownRoleId as SnapshotField.Known<String>).value).append(':')
                    append(when (val actual = seat.actualRoleId) {
                        is SnapshotField.Known<*> -> actual.value
                        SnapshotField.Uncommitted -> "UNCOMMITTED"
                        SnapshotField.Unknown -> "UNKNOWN"
                        SnapshotField.NotApplicable -> "NOT_APPLICABLE"
                    })
                }
            }
            val fingerprint = MessageDigest.getInstance("SHA-256")
                .digest(rosterKey.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it.toInt() and 0xff) }
            return InitialOpeningRequestV2(snapshot, hasDrunk,
                seats.associate { it.seat to (playerExperience[it.seat] ?: OpeningPlayerExperienceV1.BEGINNER) },
                gameStateRevision, playerInputRevision, fingerprint)
        }
    }
}
