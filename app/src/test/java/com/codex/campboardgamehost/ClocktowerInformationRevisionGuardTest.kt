package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerInformationRevisionGuardTest {
    @Test fun `Next may commit last preview only while canonical revision is unchanged`() {
        val atPreview = StorytellerDecisionRevision(5, 6)
        assertTrue(clocktowerInformationPublicationAllowed(atPreview, atPreview))
        assertFalse(clocktowerInformationPublicationAllowed(
            atPreview, atPreview.copy(gameStateRevision = 7),
        ))
        assertFalse(clocktowerInformationPublicationAllowed(
            atPreview, atPreview.copy(playerInputRevision = 7),
        ))
    }
}
