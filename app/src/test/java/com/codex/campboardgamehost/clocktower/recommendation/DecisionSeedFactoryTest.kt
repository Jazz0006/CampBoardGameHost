package com.codex.campboardgamehost.clocktower.recommendation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DecisionSeedFactoryTest {
    private val material = DecisionSeedMaterial(
        persistedGameSeed = 20260806L,
        gameId = "game-42",
        idempotencyKey = "game-42:first-night:investigator:0",
        gameStateRevision = 7,
        playerInputRevision = 3,
        historyDigest = "history-a4f2",
        rulesetVersion = "trouble-brewing-v1",
        algorithmConfigVersion = "v4-pr4",
        selectorVersion = "weighted-stable-v1",
    )

    @Test
    fun `same persisted inputs always produce the same seed`() {
        assertEquals(DecisionSeedFactory.create(material), DecisionSeedFactory.create(material))
    }

    @Test
    fun `decision seed v1 golden vector remains stable`() {
        assertEquals(7_509_587_780_382_099_828L, DecisionSeedFactory.create(material))
    }

    @Test
    fun `changing a replay-relevant input changes the seed`() {
        assertNotEquals(
            DecisionSeedFactory.create(material),
            DecisionSeedFactory.create(material.copy(historyDigest = "history-b7c9")),
        )
        assertNotEquals(
            DecisionSeedFactory.create(material),
            DecisionSeedFactory.create(material.copy(selectorVersion = "weighted-stable-v2")),
        )
    }

    @Test
    fun `length-prefixed canonical encoding prevents field-boundary collisions`() {
        val first = material.copy(gameId = "ab", idempotencyKey = "c")
        val second = material.copy(gameId = "a", idempotencyKey = "bc")

        assertNotEquals(DecisionSeedFactory.create(first), DecisionSeedFactory.create(second))
    }

    @Test
    fun `characterization - current setup seed has no phase observation or locked-decision fields`() {
        val firstNightBeforeObservation = material
        // The model cannot express a later phase, a newly shown observation, or a changed lock.
        // Consequently distinct dynamic contexts collapse to this same legacy setup-seed material.
        val nightTwoAfterObservation = material

        assertEquals(
            DecisionSeedFactory.create(firstNightBeforeObservation),
            DecisionSeedFactory.create(nightTwoAfterObservation),
        )
    }
}
