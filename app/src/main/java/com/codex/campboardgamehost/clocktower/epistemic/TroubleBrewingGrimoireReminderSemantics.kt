package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Reminder-token meanings that the current Trouble Brewing possible-world model can evaluate
 * exactly. A token not listed here remains part of the Spy's physical Grimoire observation but is
 * deliberately under-constrained until the corresponding world/history dimension is modeled.
 */
internal enum class GrimoireReminderWorldConstraint {
    POISONER_TARGET,
    RED_HERRING,
    DRUNK_IDENTITY,
}

/**
 * Audited semantic bridge from rule-backed physical token identity to current world constraints.
 *
 * The lookup key is role + scope + occurrence. Printed labels are validated against the frozen
 * identity but never interpreted as executable rules. A recognized identity with a changed label
 * fails closed so a future official metadata revision cannot silently inherit stale semantics.
 */
internal object TroubleBrewingGrimoireReminderSemantics {
    private data class TokenKey(
        val sourceRole: RoleId,
        val scope: GrimoireReminderTokenScope,
        val occurrence: Int,
    )

    private data class AuditedConstraint(
        val expectedLabel: String,
        val constraint: GrimoireReminderWorldConstraint,
    )

    private val audited = mapOf(
        TokenKey(RoleId("Poisoner"), GrimoireReminderTokenScope.CHARACTER, 1) to
            AuditedConstraint("Poisoned", GrimoireReminderWorldConstraint.POISONER_TARGET),
        TokenKey(RoleId("Fortune Teller"), GrimoireReminderTokenScope.CHARACTER, 1) to
            AuditedConstraint("Red Herring", GrimoireReminderWorldConstraint.RED_HERRING),
        TokenKey(RoleId("Drunk"), GrimoireReminderTokenScope.GLOBAL, 1) to
            AuditedConstraint("Is The Drunk", GrimoireReminderWorldConstraint.DRUNK_IDENTITY),
    )

    fun worldConstraint(token: GrimoireReminderTokenRef): GrimoireReminderWorldConstraint? {
        val semantic = audited[TokenKey(token.sourceRole, token.scope, token.occurrence)] ?: return null
        require(token.label == semantic.expectedLabel) {
            "Reminder token ${token.sourceRole.value}/${token.scope}/${token.occurrence} label '${token.label}' " +
                "does not match audited Trouble Brewing label '${semantic.expectedLabel}'."
        }
        return semantic.constraint
    }
}
