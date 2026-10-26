package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import java.security.MessageDigest

internal enum class OpeningPlayerExperienceV1 { BEGINNER, REGULAR, VETERAN, UNKNOWN }

/** Immutable request facts and identities, with no legal-output enumeration or writable game state. */
internal class InitialOpeningRequestV2 private constructor(
    val snapshot: TroubleBrewingGameSnapshotV1,
    val hasDrunk: Boolean,
    val knownRoleIds: Set<String>,
    val playerExperience: Map<Int, OpeningPlayerExperienceV1>,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val rosterFingerprint: String,
    /** Seats the Host would accept as Red Herring ([redHerringEligible]). */
    val redHerringEligibleSeats: Set<Int>,
) {
    val shownRoles: Map<Int, String> = snapshot.grimoireSeats.associate {
        it.seat to (it.shownRoleId as SnapshotField.Known<String>).value
    }
    val information: Map<String, Int> = shownRoles.filterValues {
        it in setOf("washerwoman", "librarian", "investigator", "chef", "empath", "fortuneteller")
    }.keys.associateBy { "first-night:seat-$it" }
    // These are pending player-input identities, not predicted targets, results or rule verdicts.
    val events: Map<String, Pair<String, Int>> = buildMap {
        shownRoles.forEach { (seat, role) ->
            val event = when (role) {
                "poisoner" -> "poisoner.target"
                "fortuneteller" -> "fortune_teller.targets:seat-$seat"
                else -> null
            }
            if (event != null) put("event:$event", event to seat)
        }
    }

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
            // Shown roles match actual alignment and Recluse status for every seat: the Drunk shows a good Townsfolk.
            val redHerringEligible = seats.filter { seat ->
                val shown = requireNotNull(registry.findByExternalId((seat.shownRoleId as SnapshotField.Known<String>).value))
                redHerringEligible(shown.team == ClocktowerCatalogTeam.TOWNSFOLK || shown.team == ClocktowerCatalogTeam.OUTSIDER,
                    shown.id.value)
            }.mapTo(linkedSetOf()) { it.seat }
            return InitialOpeningRequestV2(snapshot, hasDrunk, roles,
                seats.associate { it.seat to (playerExperience[it.seat] ?: OpeningPlayerExperienceV1.BEGINNER) },
                gameStateRevision, playerInputRevision, fingerprint, redHerringEligible)
        }
    }
}
