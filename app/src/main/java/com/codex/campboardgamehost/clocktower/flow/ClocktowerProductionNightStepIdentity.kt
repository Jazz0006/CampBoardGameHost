package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Stable identity for one existing production night UI step.
 *
 * This deliberately does not contain localized copy or Compose state. It only bridges the current
 * UI step type to a stable interaction ID without owning any game state or ordering.
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

        fun mayorRedirect(): ClocktowerProductionNightStepIdentity =
            ClocktowerProductionNightStepIdentity(Kind.MAYOR_REDIRECT)
    }

    private enum class Kind {
        ROLE,
        MINION_INFO,
        DEMON_INFO,
        FORTUNE_TELLER_RED_HERRING,
        NEW_DEMON_IDENTITY,
        MAYOR_REDIRECT,
    }
}


private fun ClocktowerNightFlowPhase.idPrefix(): String = when (this) {
    ClocktowerNightFlowPhase.FIRST_NIGHT -> "first_night"
    ClocktowerNightFlowPhase.OTHER_NIGHT -> "other_night"
}