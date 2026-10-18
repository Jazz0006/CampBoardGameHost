package com.codex.campboardgamehost.clocktower.session

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

internal data class HostOpeningConditionalIntentV2(
    val decisionId: String,
    val proposed: HostOpeningSemanticValueV2?,
    val waitingForEvents: List<String>,
)

/**
 * Bounded editable multi-night *strategic intent*, not a causal fact.
 * Its conditions apply to ANY role, observed action, intervention or game phase;
 * neither this type nor its producer chooses a future role-specific outcome.
 */
internal data class HostOpeningStrategicIntentionV2(
    val trigger: String,
    val approach: String,
    val risk: String,
) {
    init {
        require(listOf(trigger, approach, risk).all { it.isNotBlank() && it.length <= 300 }) {
            "Opening strategic intention exceeded its bounded text contract."
        }
    }
}

internal data class HostOpeningStrategyMemoryV2(
    val tensions: List<String>,
    val intentions: List<HostOpeningStrategicIntentionV2>,
    val uncertainties: List<String>,
) {
    init {
        require(tensions.size <= 3 && intentions.size <= 3 && uncertainties.size <= 3) {
            "Opening strategy memory exceeded three entries per section."
        }
        require((tensions + uncertainties).all { it.isNotBlank() && it.length <= 300 }) {
            "Opening strategy memory contains blank or oversized text."
        }
    }
}

/** Private AI advice; only the existing Session confirmation path can record facts. */
internal data class HostOpeningSemanticAcceptanceV2(
    val selection: OpeningSelectionV2,
    val conditional: List<HostOpeningConditionalIntentV2>,
    val strategyMemory: HostOpeningStrategyMemoryV2? = null,
)

internal data class OpeningSelectionV2(
    val gameId: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val rosterFingerprint: String,
    val drunkSeat: Int?,
    val redHerringSeat: Int?,
    val demonBluffs: List<String>?,
    val information: Map<Int, HostOpeningSemanticValueV2>,
) {
    fun validFor(request: InitialOpeningRequestV2): Boolean =
        gameId == request.snapshot.gameId && gameStateRevision == request.gameStateRevision &&
            playerInputRevision == request.playerInputRevision && rosterFingerprint == request.rosterFingerprint
}
