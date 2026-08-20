package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Stable identity for one existing production night UI step.
 *
 * This deliberately does not contain localized copy or Compose state. It only bridges the current
 * production UI step model onto the stable interaction IDs already emitted by the R5.5 planner.
 * Ordering comes exclusively from the projected interaction list.
 */
internal data class ClocktowerProductionNightStepIdentity private constructor(
    private val kind: Kind,
    private val roleId: RoleId? = null,
) {
    fun interactionId(phase: ClocktowerNightFlowPhase): ClocktowerInteractionId = when (kind) {
        Kind.ROLE -> ClocktowerInteractionId(
            "${phase.idPrefix()}:role:${requireNotNull(roleId).value}",
        )
        Kind.MINION_INFO -> {
            require(phase == ClocktowerNightFlowPhase.FIRST_NIGHT) {
                "Minion information is only a first-night production interaction."
            }
            ClocktowerInteractionId("first_night:system:minion_info")
        }
        Kind.DEMON_INFO -> {
            require(phase == ClocktowerNightFlowPhase.FIRST_NIGHT) {
                "Demon information is only a first-night production interaction."
            }
            ClocktowerInteractionId("first_night:system:demon_info")
        }
        Kind.FORTUNE_TELLER_RED_HERRING -> {
            require(phase == ClocktowerNightFlowPhase.FIRST_NIGHT) {
                "Fortune Teller red-herring setup is only a first-night interaction."
            }
            ClocktowerInteractionId("first_night:fortune_teller:red_herring")
        }
        Kind.NEW_DEMON_IDENTITY -> {
            require(phase == ClocktowerNightFlowPhase.OTHER_NIGHT) {
                "New-Demon identity is only an other-night production interaction."
            }
            ClocktowerInteractionId("other_night:event:imp:new_demon_identity")
        }
        Kind.DEMON_SUCCESSOR -> {
            require(phase == ClocktowerNightFlowPhase.OTHER_NIGHT) {
                "Demon succession is only an other-night production interaction."
            }
            ClocktowerInteractionId("other_night:event:imp:demon_successor")
        }
        Kind.MAYOR_REDIRECT -> {
            require(phase == ClocktowerNightFlowPhase.OTHER_NIGHT) {
                "Mayor death resolution is only an other-night production interaction."
            }
            ClocktowerInteractionId("other_night:event:mayor:death_resolution")
        }
    }

    companion object {
        fun role(roleId: RoleId): ClocktowerProductionNightStepIdentity =
            ClocktowerProductionNightStepIdentity(Kind.ROLE, roleId)

        fun minionInfo(): ClocktowerProductionNightStepIdentity =
            ClocktowerProductionNightStepIdentity(Kind.MINION_INFO)

        fun demonInfo(): ClocktowerProductionNightStepIdentity =
            ClocktowerProductionNightStepIdentity(Kind.DEMON_INFO)

        fun fortuneTellerRedHerring(): ClocktowerProductionNightStepIdentity =
            ClocktowerProductionNightStepIdentity(Kind.FORTUNE_TELLER_RED_HERRING)

        fun newDemonIdentity(): ClocktowerProductionNightStepIdentity =
            ClocktowerProductionNightStepIdentity(Kind.NEW_DEMON_IDENTITY)

        fun demonSuccessor(): ClocktowerProductionNightStepIdentity =
            ClocktowerProductionNightStepIdentity(Kind.DEMON_SUCCESSOR)

        fun mayorRedirect(): ClocktowerProductionNightStepIdentity =
            ClocktowerProductionNightStepIdentity(Kind.MAYOR_REDIRECT)
    }

    private enum class Kind {
        ROLE,
        MINION_INFO,
        DEMON_INFO,
        FORTUNE_TELLER_RED_HERRING,
        NEW_DEMON_IDENTITY,
        DEMON_SUCCESSOR,
        MAYOR_REDIRECT,
    }
}

/**
 * Orders the existing production UI steps by the planner projection and verifies exact parity.
 *
 * Dusk/dawn system boundaries are flow-only transitions and therefore are intentionally excluded
 * from the production UI-step equality check. Every other projected interaction must have exactly
 * one production step, and production may not invent an extra step that the planner did not emit.
 */
internal object ClocktowerProductionInteractionOrderer {
    fun <T> order(
        phase: ClocktowerNightFlowPhase,
        projectedInteractions: List<ClocktowerHostInteraction>,
        productionSteps: List<T>,
        identityOf: (T) -> ClocktowerProductionNightStepIdentity,
    ): List<T> {
        require(projectedInteractions.all { it.phase == phase }) {
            "Clocktower production ordering cannot mix planner phases."
        }

        val actionableInteractions = projectedInteractions.filterNot { interaction ->
            interaction.kind == ClocktowerHostInteractionKind.SYSTEM_BOUNDARY
        }
        val projectedIds = actionableInteractions.map(ClocktowerHostInteraction::id)
        require(projectedIds.distinct().size == projectedIds.size) {
            "Clocktower projected production interaction IDs must be unique."
        }

        val identifiedSteps = productionSteps.map { step ->
            identityOf(step).interactionId(phase) to step
        }
        val productionIds = identifiedSteps.map { it.first }
        require(productionIds.distinct().size == productionIds.size) {
            "Clocktower production night steps must map to unique interaction IDs."
        }

        val projectedSet = projectedIds.toSet()
        val productionSet = productionIds.toSet()
        require(projectedSet == productionSet) {
            val missing = (projectedSet - productionSet).map { it.value }.sorted()
            val extra = (productionSet - projectedSet).map { it.value }.sorted()
            "Clocktower production/planner interaction mismatch; missing=$missing extra=$extra"
        }

        val rank = projectedIds.withIndex().associate { (index, id) -> id to index }
        return identifiedSteps
            .sortedBy { (id, _) -> requireNotNull(rank[id]) }
            .map { (_, step) -> step }
    }
}

private fun ClocktowerNightFlowPhase.idPrefix(): String = when (this) {
    ClocktowerNightFlowPhase.FIRST_NIGHT -> "first_night"
    ClocktowerNightFlowPhase.OTHER_NIGHT -> "other_night"
}
