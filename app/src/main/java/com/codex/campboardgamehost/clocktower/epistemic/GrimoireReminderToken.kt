package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Identifies a physical/ruleset reminder token that may legitimately appear in a Spy-visible
 * Grimoire. This is deliberately not a generic note/marker type: construction from runtime rules
 * content must resolve against [ClocktowerCharacterDefinition.reminders] or
 * [ClocktowerCharacterDefinition.globalReminders].
 *
 * [occurrence] distinguishes duplicate physical tokens with the same printed label.
 */
enum class GrimoireReminderTokenScope { CHARACTER, GLOBAL }

data class GrimoireReminderTokenRef(
    val sourceRole: RoleId,
    val scope: GrimoireReminderTokenScope,
    val label: String,
    val occurrence: Int,
) : Comparable<GrimoireReminderTokenRef> {
    init {
        require(sourceRole.value.isNotBlank()) { "Grimoire reminder token source role cannot be blank." }
        require(label.isNotBlank()) { "Grimoire reminder token label cannot be blank." }
        require(occurrence > 0) { "Grimoire reminder token occurrence must be positive." }
    }

    override fun compareTo(other: GrimoireReminderTokenRef): Int = compareValuesBy(
        this,
        other,
        { it.sourceRole.value },
        { it.scope.ordinal },
        GrimoireReminderTokenRef::label,
        GrimoireReminderTokenRef::occurrence,
    )
}

/**
 * Resolve one declared reminder-token occurrence from validated character metadata.
 *
 * Callers never infer a rule-backed token from display text alone. Duplicate labels are resolved by
 * their one-based occurrence within the source list so physical copies remain distinguishable.
 */
internal fun ClocktowerCharacterDefinition.grimoireReminderToken(
    scope: GrimoireReminderTokenScope,
    occurrence: Int,
): GrimoireReminderTokenRef {
    require(occurrence > 0) { "Grimoire reminder token occurrence must be positive." }
    val declared = when (scope) {
        GrimoireReminderTokenScope.CHARACTER -> reminders
        GrimoireReminderTokenScope.GLOBAL -> globalReminders
    }
    require(occurrence <= declared.size) {
        "Reminder token occurrence $occurrence is not declared for ${id.value} in $scope metadata."
    }
    return GrimoireReminderTokenRef(
        sourceRole = id,
        scope = scope,
        label = declared[occurrence - 1],
        occurrence = occurrence,
    )
}
