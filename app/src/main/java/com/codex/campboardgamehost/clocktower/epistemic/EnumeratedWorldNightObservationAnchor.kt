package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.NightOrderToken
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase

/**
 * Knowledge-safe position of one received night observation inside one possible world's canonical
 * waking schedule. This is not a durable timeline identity and deliberately carries no
 * [TimelinePoint] or synthetic global sequence.
 */
internal data class EnumeratedWorldNightObservationAnchor(
    val roleId: RoleId,
    val scheduleIndex: Int,
) {
    init {
        require(scheduleIndex >= 0) { "Night observation scheduleIndex cannot be negative." }
    }
}

internal object EnumeratedWorldNightObservationAnchoring {
    fun anchorOrNull(
        ruleset: ValidatedClocktowerRuleset,
        phase: ClocktowerNightFlowPhase,
        world: EnumeratedWorld,
        record: RecordedEpistemicObservation,
    ): EnumeratedWorldNightObservationAnchor? {
        val expectedStorytellerPhase = when (phase) {
            ClocktowerNightFlowPhase.FIRST_NIGHT -> StorytellerPhase.FIRST_NIGHT
            ClocktowerNightFlowPhase.OTHER_NIGHT -> StorytellerPhase.NIGHT
        }
        if (record.phase != expectedStorytellerPhase) return null
        val sourceSeat = record.sourceSeat ?: return null
        val sourceAbility = record.sourceAbility ?: return null

        val actualRole = world.rolesBySeat[sourceSeat] ?: return null
        val shownRole = world.shownRolesBySeat[sourceSeat]
        if (sourceAbility != actualRole && sourceAbility != shownRole) return null

        val token = NightOrderToken.Character(sourceAbility)
        val scheduleIndex = EnumeratedWorldNightSchedule.plan(
            ruleset = ruleset,
            phase = phase,
            world = world,
        ).indexOf(token)
        if (scheduleIndex < 0) return null

        return EnumeratedWorldNightObservationAnchor(
            roleId = sourceAbility,
            scheduleIndex = scheduleIndex,
        )
    }
}
