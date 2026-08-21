package com.codex.campboardgamehost.clocktower.catalog

import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Reminder-token metadata frozen from The Pandemonium Institute's public botc-release data.
 *
 * Source: ThePandemoniumInstitute/botc-release resources/data/roles.json at [SOURCE_REVISION].
 * Only reminder-token identity is mirrored here. Ability text, behavior, night order and other
 * mechanical semantics remain owned by the existing validated rules/catalog layers.
 */
internal object OfficialReminderTokenMetadata {
    const val SOURCE_REVISION: String = "f10cd02e3401af227ce406287eaae7bb99a06a42"

    private data class ReminderMetadata(
        val reminders: List<String> = emptyList(),
        val globalReminders: List<String> = emptyList(),
    )

    private val byRoleId: Map<RoleId, ReminderMetadata> = mapOf(
        RoleId("Washerwoman") to ReminderMetadata(listOf("Townsfolk", "Wrong")),
        RoleId("Librarian") to ReminderMetadata(listOf("Outsider", "Wrong")),
        RoleId("Investigator") to ReminderMetadata(listOf("Minion", "Wrong")),
        RoleId("Chef") to ReminderMetadata(),
        RoleId("Empath") to ReminderMetadata(),
        RoleId("Fortune Teller") to ReminderMetadata(listOf("Red Herring")),
        RoleId("Undertaker") to ReminderMetadata(listOf("Died Today")),
        RoleId("Monk") to ReminderMetadata(listOf("Safe")),
        RoleId("Ravenkeeper") to ReminderMetadata(),
        RoleId("Virgin") to ReminderMetadata(listOf("No Ability")),
        RoleId("Slayer") to ReminderMetadata(listOf("No Ability")),
        RoleId("Soldier") to ReminderMetadata(),
        RoleId("Mayor") to ReminderMetadata(),
        RoleId("Butler") to ReminderMetadata(listOf("Master")),
        RoleId("Drunk") to ReminderMetadata(globalReminders = listOf("Is The Drunk")),
        RoleId("Recluse") to ReminderMetadata(),
        RoleId("Saint") to ReminderMetadata(),
        RoleId("Poisoner") to ReminderMetadata(listOf("Poisoned")),
        RoleId("Spy") to ReminderMetadata(),
        RoleId("Scarlet Woman") to ReminderMetadata(listOf("Is The Demon")),
        RoleId("Baron") to ReminderMetadata(),
        RoleId("Imp") to ReminderMetadata(listOf("Dead")),
        RoleId("Clockmaker") to ReminderMetadata(),
        RoleId("Chambermaid") to ReminderMetadata(),
        RoleId("Artist") to ReminderMetadata(listOf("No Ability")),
        RoleId("Sage") to ReminderMetadata(),
        RoleId("Klutz") to ReminderMetadata(),
    )

    fun applyTo(registry: ClocktowerCharacterRegistry): ClocktowerCharacterRegistry {
        val missing = registry.definitions.map(ClocktowerCharacterDefinition::id).filterNot(byRoleId::containsKey)
        require(missing.isEmpty()) {
            "Official reminder-token metadata is missing built-in roles: ${missing.joinToString { it.value }}"
        }
        return ClocktowerCharacterRegistry(
            registry.definitions.map { definition ->
                val metadata = requireNotNull(byRoleId[definition.id])
                definition.copy(
                    reminders = metadata.reminders,
                    globalReminders = metadata.globalReminders,
                )
            },
        )
    }
}
