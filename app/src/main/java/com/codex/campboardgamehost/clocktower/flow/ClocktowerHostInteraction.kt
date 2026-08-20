package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.domain.RoleId

@JvmInline
internal value class ClocktowerInteractionId(val value: String) {
    init {
        require(value.isNotBlank()) { "Clocktower interaction id cannot be blank." }
    }
}

internal enum class ClocktowerHostInteractionKind {
    SYSTEM_BOUNDARY,
    EVIL_INFORMATION,
    ROLE_PHASE_ACTION,
    STORYTELLER_SETUP,
}

internal enum class ClocktowerInteractionCompletionPolicy {
    SYSTEM_TRANSITION,
    INFORMATION_DISPLAY,
    ROLE_RESOLUTION,
    STORYTELLER_SELECTION,
}

/**
 * Stable flow-layer projection. Localized copy, Compose state and concrete widgets intentionally
 * stay outside this model. A later adapter may add actor seats once the flow context owns seating.
 */
internal data class ClocktowerHostInteraction(
    val id: ClocktowerInteractionId,
    val phase: ClocktowerNightFlowPhase,
    val roleId: RoleId?,
    val kind: ClocktowerHostInteractionKind,
    val completionPolicy: ClocktowerInteractionCompletionPolicy,
    val decisionPointId: String? = null,
) {
    init {
        require(decisionPointId == null || decisionPointId.isNotBlank()) {
            "Clocktower decisionPointId cannot be blank."
        }
        require(
            kind == ClocktowerHostInteractionKind.SYSTEM_BOUNDARY ||
                kind == ClocktowerHostInteractionKind.EVIL_INFORMATION ||
                roleId != null,
        ) {
            "Role-backed Clocktower interactions require a roleId."
        }
    }
}
