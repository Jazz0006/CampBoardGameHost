package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import org.json.JSONArray
import org.json.JSONObject

/**
 * 3B3D-1 experimental, read-only bridge from one semantic V2 plan to the existing
 * Host-authoritative precommit scope. It does NOT build any legal candidate domain,
 * commit Drunk, choose a registration witness, publish a clue or call a model.
 *
 * Authoritative producers must provide [HostOpeningSemanticProjectionV2] from the
 * actual Host rules domain, never from provider text or a model-supplied menu.
 */
internal data class HostOpeningSemanticValueV2(
    val sourceSeat: Int,
    val abilityRoleId: String,
    val effectiveAbilityRoleId: String?,
    val basis: String,
    val subjectMode: String,
    val subjectSeats: List<Int>,
    val observationKind: String,
    val number: Int? = null,
    val roleId: String? = null,
    val boolean: Boolean? = null,
) {
    init {
        require(sourceSeat > 0)
        require(abilityRoleId.isNotBlank())
        require(basis in setOf("FUNCTIONING", "DRUNK_MISINFORMATION",
            "STATUS_DEPENDENT", "RULE_DETERMINED"))
        require(subjectMode in setOf("NONE", "EVIDENCE_PAIR", "FIXED_NEIGHBORS",
            "PLAYER_SELECTED_TARGETS", "PLAYER_QUERY_PENDING"))
        require(subjectSeats.size == subjectSeats.distinct().size &&
            subjectSeats.all { it > 0 })
        require(when (subjectMode) {
            "EVIDENCE_PAIR" -> subjectSeats.size == 2
            "PLAYER_SELECTED_TARGETS" -> subjectSeats.isNotEmpty()
            else -> subjectSeats.isEmpty()
        })
        require(when (observationKind) {
            "NUMBER" -> number != null && roleId == null && boolean == null
            "ZERO_OUTSIDERS" -> number == null && roleId == null && boolean == null &&
                subjectMode == "NONE"
            "ROLE" -> roleId != null && number == null && boolean == null &&
                subjectMode in setOf("EVIDENCE_PAIR", "PLAYER_SELECTED_TARGETS")
            "BOOLEAN" -> boolean != null && number == null && roleId == null
            "NONE" -> number == null && roleId == null && boolean == null
            else -> false
        })
    }
}

/** Evidence from the Host's *current branch*, not a second legality engine. */
internal data class HostOpeningSemanticProjectionV2(
    val scope: InitialGlobalOpeningScopeV1,
    val drunkSeat: Int?,
    val knownRoleIds: Set<String>,
    // Complete current Host candidate IDs and their canonical public semantics.
    val candidatesByDecision: Map<String, Map<String, HostOpeningSemanticValueV2>>,
    // If red herring is a Host discretionary decision, resolve by canonical Good seat.
    val redHerringDecisionId: String? = null,
    val redHerringCandidatesBySeat: Map<Int, String> = emptyMap(),
    // Which Host-determined decisions are actually player-information fields.
    // Other facts (setup counts, etc.) cannot be impersonated as observations.
    val determinedInformationIds: Set<String> = emptySet(),
    // Deferred *player actions* that do not have an information observation.
    val deferredActions: Map<String, Pair<String, Int?>> = emptyMap(),
) {
    val branch = scope.hypothesis(drunkSeat)

    init {
        require(knownRoleIds.isNotEmpty())
        require(branch.discretionary.map { it.decisionId }.toSet() ==
            candidatesByDecision.keys +
                listOfNotNull(redHerringDecisionId)) {
            "Every discretionary Host decision needs exact semantic evidence."
        }
        branch.discretionary.filter { it.decisionId != redHerringDecisionId }.forEach { decision ->
            val mapping = requireNotNull(candidatesByDecision[decision.decisionId])
            require(mapping.keys == decision.legalCandidates.keys) {
                "Semantic mapping must cover the entire authoritative legal domain."
            }
            // Distinct legal witnesses may describe one identical public observation.
            require(mapping.values.all { it.sourceSeat == decision.sourceSeat })
        }
        require(redHerringDecisionId == null || branch.discretionary.any {
            it.decisionId == redHerringDecisionId &&
                it.legalCandidates.keys == redHerringCandidatesBySeat.values.toSet() &&
                redHerringCandidatesBySeat.keys.all { seat -> seat in 1..scope.snapshot.grimoireSeats.size }
        })
        require(determinedInformationIds.all { id ->
            branch.determined.any { it.decisionId == id }
        })
        require(deferredActions.keys.all { id -> branch.deferred.any { it.decisionId == id } })
        require(deferredActions.values.all { (event, seat) ->
            event.isNotBlank() && (seat == null || seat in 1..scope.snapshot.grimoireSeats.size)
        })
    }
}

internal data class HostOpeningConditionalIntentV2(
    val decisionId: String,
    val proposed: HostOpeningSemanticValueV2?,
    val waitingForEvents: List<String>,
)

/** A validated *private proposal*, never a committed or published observation. */
internal data class HostOpeningSemanticAcceptanceV2(
    val selection: InitialGlobalOpeningSelectionV1,
    val conditional: List<HostOpeningConditionalIntentV2>,
    /** All matching Host candidates; representative selection IDs never settle registration. */
    val matchingCandidateIds: Map<String, Set<String>> = emptyMap(),
)

internal object HostOpeningSemanticGateV2 {
    private fun JSONObject.exact(vararg keys: String) {
        require(this.keys().asSequence().toSet() == keys.toSet()) {
            "Unexpected or missing semantic protocol fields."
        }
    }

    private fun JSONArray.strings(): List<String> =
        (0 until length()).map(::getString)

    private fun JSONArray.ints(): List<Int> =
        (0 until length()).map(::getInt)

    private fun JSONObject.nullableString(key: String): String? =
        if (isNull(key)) null else getString(key)

    private fun JSONObject.nullableInt(key: String): Int? =
        if (isNull(key)) null else getInt(key)

    private fun parseValue(
        raw: JSONObject,
        projection: HostOpeningSemanticProjectionV2,
    ): HostOpeningSemanticValueV2 {
        val subject = raw.getJSONObject("subject")
        subject.exact("mode", "seats")
        val observation = raw.getJSONObject("observation")
        observation.exact("kind", "number", "roleId", "boolean")
        val number = observation.nullableInt("number")
        val role = observation.nullableString("roleId")
        val bool = if (observation.isNull("boolean")) null else observation.getBoolean("boolean")
        val value = HostOpeningSemanticValueV2(
            sourceSeat = raw.getInt("sourceSeat"),
            abilityRoleId = raw.getString("abilityRoleId"),
            effectiveAbilityRoleId = raw.nullableString("effectiveAbilityRoleId"),
            basis = raw.getString("basis"),
            subjectMode = subject.getString("mode"),
            subjectSeats = subject.getJSONArray("seats").ints(),
            observationKind = observation.getString("kind"),
            number = number, roleId = role, boolean = bool,
        )
        val shown = requireNotNull(projection.scope.snapshot.grimoireSeats
            .singleOrNull { it.seat == value.sourceSeat })
        val seenRole = (shown.shownRoleId as? SnapshotField.Known<*>)?.value
        require(value.abilityRoleId == seenRole && value.abilityRoleId in projection.knownRoleIds)
        require(value.subjectSeats.all { seat ->
            projection.scope.snapshot.grimoireSeats.any { it.seat == seat }
        })
        require(value.roleId == null || value.roleId in projection.knownRoleIds)
        require(if (value.sourceSeat == projection.drunkSeat) {
            value.effectiveAbilityRoleId == null && value.basis in
                setOf("DRUNK_MISINFORMATION", "STATUS_DEPENDENT")
        } else {
            value.effectiveAbilityRoleId == value.abilityRoleId &&
                value.basis != "DRUNK_MISINFORMATION"
        })
        return value
    }

    /**
     * Fail closed on an incomplete or stale *whole* plan. Source revisions and
     * candidate matches must be checked against the exact same Host snapshot.
     */
    fun accept(
        raw: JSONObject,
        projection: HostOpeningSemanticProjectionV2,
    ): HostOpeningSemanticAcceptanceV2 {
        raw.exact("schemaVersion", "asOfRevision", "requestIdentity",
            "openingPlan", "strategyMemory", "tradeoffSummary")
        require(raw.getInt("schemaVersion") == 2)
        val scope = projection.scope
        val identity = raw.getJSONObject("requestIdentity")
        identity.exact("gameId", "gameStateRevision",
            "playerInputRevision", "rosterFingerprint")
        require(identity.getString("gameId") == scope.snapshot.gameId &&
            identity.getLong("gameStateRevision") == scope.gameStateRevision &&
            identity.getLong("playerInputRevision") == scope.playerInputRevision &&
            identity.getString("rosterFingerprint") == scope.rosterFingerprint &&
            raw.getLong("asOfRevision") == scope.gameStateRevision) {
            "Stale opening semantic plan."
        }

        // Strategy is advisory. Still bound its shape; never parse it as a fact.
        val strategy = raw.getJSONObject("strategyMemory")
        strategy.exact("tensions", "intentions", "uncertainties")
        require(strategy.getJSONArray("tensions").length() <= 3 &&
            strategy.getJSONArray("intentions").length() <= 3 &&
            strategy.getJSONArray("uncertainties").length() <= 3)
        strategy.getJSONArray("tensions").strings()
        strategy.getJSONArray("uncertainties").strings()
        for (i in 0 until strategy.getJSONArray("intentions").length()) {
            strategy.getJSONArray("intentions").getJSONObject(i)
                .exact("trigger", "approach", "risk")
        }
        require(raw.getString("tradeoffSummary").length <= 1200)

        val plan = raw.getJSONObject("openingPlan")
        plan.exact("drunkSeat", "redHerringSeat", "demonBluffRoleIds",
            "information", "deferredActions")
        val drunk = plan.nullableInt("drunkSeat")
        require(drunk == projection.drunkSeat)
        val bluffs = if (plan.isNull("demonBluffRoleIds")) null
            else plan.getJSONArray("demonBluffRoleIds").strings()
        val redSeat = plan.nullableInt("redHerringSeat")
        val picked = linkedMapOf<String, String>()
        val matchingCandidateIds = linkedMapOf<String, Set<String>>()
        if (projection.redHerringDecisionId == null) {
            require(redSeat == null)
        } else {
            picked[projection.redHerringDecisionId] =
                requireNotNull(projection.redHerringCandidatesBySeat[redSeat]) {
                    "Red herring is not in the current Host legal domain."
                }
        }
        val pending = linkedMapOf<String, HostOpeningConditionalIntentV2>()
        val usedDecisionIds = mutableSetOf<String>()
        val hostDeferred = projection.branch.deferred.associateBy { it.decisionId }
        val hostDetermined = projection.branch.determined.associateBy { it.decisionId }
        val items = plan.getJSONArray("information")
        require(items.length() <= 64)
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            item.exact("decisionId", "sourceSeat", "abilityRoleId",
                "effectiveAbilityRoleId", "resolution", "basis",
                "subject", "observation", "waitingForEvents")
            val id = item.getString("decisionId")
            require(usedDecisionIds.add(id)) { "Duplicate semantic decision ID." }
            require(id !in picked && id !in pending)
            val value = parseValue(item, projection)
            val waiting = item.getJSONArray("waitingForEvents").strings()
            require(waiting.size == waiting.distinct().size)
            if (value.sourceSeat == projection.drunkSeat &&
                item.getString("resolution") == "DECISION") {
                require(value.basis == "DRUNK_MISINFORMATION")
            }
            when (item.getString("resolution")) {
                "DECISION" -> {
                    require(waiting.isEmpty() && value.observationKind != "NONE")
                    val candidates = requireNotNull(projection.candidatesByDecision[id]) {
                        "Information is not a current Host discretion."
                    }
                    val matches = candidates.entries.filter { it.value == value }
                        .map { it.key }.toSortedSet()
                    require(matches.isNotEmpty()) { "No authoritative Host semantic match." }
                    // Deterministic representative for existing V1 selection validation ONLY.
                    // Do not publish it as a unique registration witness.
                    picked[id] = matches.first()
                    matchingCandidateIds[id] = matches
                }
                "HOST_DETERMINED" -> {
                    require(id in hostDetermined && id in projection.determinedInformationIds && waiting.isEmpty() &&
                        value.observationKind == "NONE" && value.basis == "RULE_DETERMINED")
                }
                "DEFERRED", "CONDITIONAL_INTENT" -> {
                    val branchWaiting = requireNotNull(hostDeferred[id]).waitingForEvents
                    require(waiting == branchWaiting)
                    if (item.getString("resolution") == "DEFERRED") {
                        require(value.observationKind == "NONE")
                    } else {
                        require(value.observationKind != "NONE" &&
                            value.basis == "STATUS_DEPENDENT")
                    }
                    pending[id] = HostOpeningConditionalIntentV2(
                        id, value.takeIf { value.observationKind != "NONE" }, waiting)
                }
                else -> error("Unknown information resolution.")
            }
        }
        val actions = plan.getJSONArray("deferredActions")
        require(actions.length() <= 64)
        for (i in 0 until actions.length()) {
            val action = actions.getJSONObject(i)
            action.exact("decisionId", "event", "actorSeat")
            val id = action.getString("decisionId")
            require(usedDecisionIds.add(id)) { "Duplicate semantic decision ID." }
            require(id !in picked && id !in pending)
            val event = action.getString("event")
            val seat = action.nullableInt("actorSeat")
            require(projection.deferredActions[id] == (event to seat) &&
                requireNotNull(hostDeferred[id]).waitingForEvents.contains(event))
            pending[id] = HostOpeningConditionalIntentV2(
                id, null, hostDeferred.getValue(id).waitingForEvents)
        }

        val discretionary = projection.branch.discretionary.map { it.decisionId }.toSet()
        require(usedDecisionIds.intersect(hostDetermined.keys) == projection.determinedInformationIds) {
            "Incomplete Host-determined information inventory."
        }
        require(picked.keys == discretionary) { "Incomplete discretionary opening bundle." }
        require(pending.keys == hostDeferred.keys) { "Incomplete deferred opening bundle." }
        // Non-information deterministic setup facts are owned by Host; no model
        // candidates may select them. Any information-determined id was verified.
        val selection = InitialGlobalOpeningSelectionV1(
            gameId = scope.snapshot.gameId,
            gameStateRevision = scope.gameStateRevision,
            playerInputRevision = scope.playerInputRevision,
            rosterFingerprint = scope.rosterFingerprint,
            scopeFingerprint = scope.scopeFingerprint,
            drunkSeat = drunk,
            choices = picked,
            demonBluffs = bluffs,
            deferredDecisionIds = projection.branch.deferred.map { it.decisionId },
        ).also { it.validate(scope) }
        return HostOpeningSemanticAcceptanceV2(selection, pending.values.toList(), matchingCandidateIds)
    }
}
