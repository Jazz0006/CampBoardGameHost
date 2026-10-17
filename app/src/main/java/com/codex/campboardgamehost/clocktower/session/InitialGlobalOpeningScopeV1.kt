package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidateDomain
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

/**
 * PROD-GLOBAL-3B1: a pure precommit Host/LLM contract, NOT a producer of legal clues or a commit.
 *
 * The future Host legal-domain owner supplies one conditional branch for each rules-legal
 * Drunk hypothesis. A branch has independent deterministic facts, discretionary legal
 * candidate IDs, and explicit event dependencies. Never cross-product branches or infer
 * a unique Spy/Recluse registration witness from a displayed result.
 */
internal data class InitialOpeningDecisionV1(
    val decisionId: String,
    val family: String,
    val sourceSeat: Int?,
    val legalCandidates: Map<String, String>,
) {
    init {
        require(decisionId.isNotBlank() && decisionId.length <= 180)
        require(family.isNotBlank() && family.length <= 80)
        require(sourceSeat == null || sourceSeat > 0)
        require(legalCandidates.isNotEmpty() && legalCandidates.size <= 512)
        require(legalCandidates.keys.all { it.isNotBlank() && it.length <= 280 })
        require(legalCandidates.values.all { it.isNotBlank() && it.length <= 600 })
    }
}

/** A rule-determined value is strategic context, never a candidate to select. */
internal data class InitialOpeningDeterminedV1(
    val decisionId: String,
    val resultId: String,
    val description: String,
) {
    init {
        require(decisionId.isNotBlank() && resultId.isNotBlank())
        require(description.isNotBlank() && description.length <= 600)
    }
}

/** Missing event dependencies are explicitly represented, not silently assumed. */
internal data class InitialOpeningDeferredV1(
    val decisionId: String,
    val waitingForEvents: List<String>,
) {
    init {
        require(decisionId.isNotBlank())
        require(waitingForEvents.isNotEmpty() && waitingForEvents.size <= 8)
        require(waitingForEvents.distinct().size == waitingForEvents.size)
        require(waitingForEvents.all { it.isNotBlank() && it.length <= 120 })
    }
}

internal data class InitialOpeningHypothesisV1(
    val drunkSeat: Int?,
    val discretionary: List<InitialOpeningDecisionV1>,
    val determined: List<InitialOpeningDeterminedV1>,
    val deferred: List<InitialOpeningDeferredV1>,
) {
    init {
        val all = discretionary.map { it.decisionId } +
            determined.map { it.decisionId } + deferred.map { it.decisionId }
        require(all.size == all.distinct().size) {
            "A conditional branch must assign each decision exactly one status."
        }
    }
}

/** Only actual recorded proficiency is authoritative; missing input remains UNKNOWN. */
internal enum class OpeningPlayerExperienceV1 { BEGINNER, REGULAR, VETERAN, UNKNOWN }

internal class InitialGlobalOpeningScopeV1 private constructor(
    val snapshot: TroubleBrewingGameSnapshotV1,
    val hasDrunk: Boolean,
    val hypotheses: List<InitialOpeningHypothesisV1>,
    val legalDemonBluffs: List<String>?,
    val playerExperience: Map<Int, OpeningPlayerExperienceV1>,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val rosterFingerprint: String,
) {
    private val shownRoles = snapshot.grimoireSeats.map { seat ->
        (seat.shownRoleId as SnapshotField.Known<String>).value
    }
    val scopeFingerprint: String by lazy {
        sha256(legalScopeJson().toString() + "|" + playerExperience.entries.sortedBy { it.key }.joinToString("|") { "${it.key}:${it.value.name}" })
    }

    fun hypothesis(drunkSeat: Int?): InitialOpeningHypothesisV1 =
        requireNotNull(hypotheses.singleOrNull { it.drunkSeat == drunkSeat }) {
            "Unknown legal opening hypothesis."
        }

    private fun legalScopeJson(): JSONObject = JSONObject()
        .put("legalDemonBluffRoleIds",
            legalDemonBluffs?.let(::JSONArray) ?: JSONObject.NULL)
        .put("hypotheses", JSONArray().also { out ->
            hypotheses.forEach { branch ->
                out.put(JSONObject()
                    .put("drunkSeat", branch.drunkSeat ?: JSONObject.NULL)
                    .put("conditionalNotCommitted", true)
                    .put("conditionalActualRoles", JSONArray().also { roles ->
                        snapshot.grimoireSeats.forEach { seat ->
                            val shown = (seat.shownRoleId as SnapshotField.Known<String>).value
                            roles.put(JSONObject().put("seat", seat.seat)
                                .put("roleId", if (seat.seat == branch.drunkSeat) "drunk" else shown))
                        }
                    })
                    .put("discretionary", JSONArray().also { choices ->
                        branch.discretionary.forEach { decision ->
                            choices.put(JSONObject()
                                .put("decisionId", decision.decisionId)
                                .put("family", decision.family)
                                .put("sourceSeat", decision.sourceSeat ?: JSONObject.NULL)
                                .put("legalCandidates", JSONObject(decision.legalCandidates)))
                        }
                    })
                    .put("determined", JSONArray().also { deterministic ->
                        branch.determined.forEach { item ->
                            deterministic.put(JSONObject()
                                .put("decisionId", item.decisionId)
                                .put("resultId", item.resultId)
                                .put("description", item.description))
                        }
                    })
                    .put("deferred", JSONArray().also { dependencies ->
                        branch.deferred.forEach { item ->
                            dependencies.put(JSONObject()
                                .put("decisionId", item.decisionId)
                                .put("waitingForEvents", JSONArray(item.waitingForEvents)))
                        }
                    }))
            }
        })

    companion object {
        fun fromHost(
            snapshot: TroubleBrewingGameSnapshotV1,
            characterRegistry: ClocktowerCharacterRegistry,
            hypotheses: List<InitialOpeningHypothesisV1>,
            legalDemonBluffs: List<String>?,
            playerExperience: Map<Int, OpeningPlayerExperienceV1> = emptyMap(),
            gameStateRevision: Long = 0,
            playerInputRevision: Long = 0,
        ): InitialGlobalOpeningScopeV1 {
            require(snapshot.position.stage == TroubleBrewingSnapshotStage.SETUP_PRECOMMIT)
            require(gameStateRevision >= 0 && playerInputRevision >= 0)
            val seats = snapshot.grimoireSeats
            require(seats.all { it.shownRoleId is SnapshotField.Known<*> })
            val hasDrunk = (snapshot.setupState.hasDrunk as? SnapshotField.Known<Boolean>)?.value
                ?: error("Precommit scope requires known hasDrunk.")
            val legalSeats = TroubleBrewingDrunkCandidateDomain
                .legalCandidateRefs(snapshot, characterRegistry).map { it.seat }
            require(legalSeats.distinct().size == legalSeats.size)
            val expectedBranches: Set<Int?> =
                if (hasDrunk) legalSeats.map<Int, Int?> { it }.toSet() else setOf(null)
            require(expectedBranches.isNotEmpty() && hypotheses.size == expectedBranches.size &&
                hypotheses.map { it.drunkSeat }.toSet() == expectedBranches) {
                "Exactly one Host-generated legal scope is required per Drunk hypothesis."
            }
            if (!hasDrunk) {
                require(snapshot.setupState.drunkAssignmentSeat === SnapshotField.NotApplicable)
                require(seats.all { it.actualRoleId is SnapshotField.Known<*> })
            }
            if (hasDrunk) require(snapshot.setupState.drunkAssignmentSeat === SnapshotField.Uncommitted)
            require(playerExperience.keys.all { it in 1..seats.size })
            val shown = seats.map { (it.shownRoleId as SnapshotField.Known<String>).value }
            require(shown.distinct().size == shown.size)
            legalDemonBluffs?.let { bluffs ->
                require(bluffs.size >= 3 && bluffs.distinct().size == bluffs.size)
                require(bluffs.all { it.isNotBlank() && it !in shown &&
                    characterRegistry.findByExternalId(it)?.team in
                        setOf(ClocktowerCatalogTeam.TOWNSFOLK, ClocktowerCatalogTeam.OUTSIDER) })
            }
            val demonPresent = seats.any { seat ->
                val roleId = (seat.shownRoleId as SnapshotField.Known<String>).value
                characterRegistry.findByExternalId(roleId)?.team == ClocktowerCatalogTeam.DEMON
            }
            if (seats.size >= 7 && demonPresent) require(legalDemonBluffs != null)
            hypotheses.forEach { branch ->
                branch.discretionary.forEach {
                    require(it.sourceSeat == null || it.sourceSeat in 1..seats.size)
                    require(it.legalCandidates.keys.size == it.legalCandidates.keys.toSet().size)
                }
            }
            val rosterKey = buildString {
                append(snapshot.gameId).append('|').append(snapshot.gameSeed)
                append('|').append(hasDrunk)
                seats.forEach { seat ->
                    append('|').append(seat.seat).append(':')
                    append((seat.shownRoleId as SnapshotField.Known<String>).value)
                    append(':').append(when (val actual = seat.actualRoleId) {
                        is SnapshotField.Known<*> -> actual.value
                        SnapshotField.Uncommitted -> "UNCOMMITTED"
                        SnapshotField.Unknown -> "UNKNOWN"
                        SnapshotField.NotApplicable -> "NOT_APPLICABLE"
                    })
                }
            }
            return InitialGlobalOpeningScopeV1(
                snapshot, hasDrunk, hypotheses.sortedBy { it.drunkSeat ?: 0 },
                legalDemonBluffs?.sorted(), (1..seats.size).associateWith { seat ->
                    playerExperience[seat] ?: OpeningPlayerExperienceV1.BEGINNER
                }, gameStateRevision, playerInputRevision, sha256(rosterKey),
            )
        }

        private fun sha256(value: String): String =
            MessageDigest.getInstance("SHA-256")
                .digest(value.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }
}

/** One output can adopt exactly one complete legal conditional branch, never an invented action. */
internal data class InitialGlobalOpeningSelectionV1(
    val gameId: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val rosterFingerprint: String,
    val scopeFingerprint: String,
    val drunkSeat: Int?,
    val choices: Map<String, String>,
    val demonBluffs: List<String>?,
    val deferredDecisionIds: List<String>,
) {
    fun validFor(scope: InitialGlobalOpeningScopeV1): Boolean =
        runCatching { validate(scope) }.isSuccess

    fun validate(scope: InitialGlobalOpeningScopeV1) {
        require(gameId == scope.snapshot.gameId &&
            gameStateRevision == scope.gameStateRevision &&
            playerInputRevision == scope.playerInputRevision &&
            rosterFingerprint == scope.rosterFingerprint &&
            scopeFingerprint == scope.scopeFingerprint) { "Stale opening scope." }
        val branch = scope.hypothesis(drunkSeat)
        val legal = branch.discretionary.associateBy { it.decisionId }
        require(choices.keys == legal.keys) { "Incomplete opening choices." }
        choices.forEach { (id, candidate) ->
            require(candidate in requireNotNull(legal[id]).legalCandidates.keys) {
                "Illegal opening candidate."
            }
        }
        require(deferredDecisionIds == branch.deferred.map { it.decisionId }) {
            "Future actions cannot be invented or dropped."
        }
        val bluffs = scope.legalDemonBluffs
        if (bluffs == null) {
            require(demonBluffs == null)
        } else {
            val chosen = requireNotNull(demonBluffs) { "Missing required Demon bluffs." }
            require(chosen.size == 3 && chosen.distinct().size == 3 &&
                chosen.all { it in bluffs }) { "Illegal Demon bluff trio." }
        }
    }

    companion object {
        fun decode(raw: JSONObject, scope: InitialGlobalOpeningScopeV1): InitialGlobalOpeningSelectionV1 {
            require(raw.has("drunkSeat"))
            val array = raw.getJSONArray("choices")
            require(array.length() <= 64)
            val choices = linkedMapOf<String, String>()
            repeat(array.length()) { i ->
                val item = array.getJSONObject(i)
                require(item.length() == 2)
                require(choices.put(item.getString("decisionId"),
                    item.getString("candidateId")) == null)
            }
            val bluffRaw = raw.get("demonBluffRoleIds")
            val bluffs = when (bluffRaw) {
                JSONObject.NULL -> null
                is JSONArray -> (0 until bluffRaw.length()).map(bluffRaw::getString)
                else -> error("Invalid bluff shape")
            }
            val defer = raw.getJSONArray("deferredDecisionIds")
            val selection = InitialGlobalOpeningSelectionV1(
                raw.getString("gameId"),
                raw.getLong("gameStateRevision"),
                raw.getLong("playerInputRevision"),
                raw.getString("rosterFingerprint"),
                raw.getString("scopeFingerprint"),
                if (raw.isNull("drunkSeat")) null else raw.getInt("drunkSeat"),
                choices, bluffs,
                (0 until defer.length()).map(defer::getString),
            )
            selection.validate(scope)
            return selection
        }
    }
}
