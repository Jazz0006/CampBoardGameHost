package com.codex.campboardgamehost.clocktower.session

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Test

class PairInformationDecisionArchitectureTest {
    @Test
    fun `RES engine decision owners do not import recommendation implementation`() {
        val engineOwnedPaths = listOf(
            "src/main/java/com/codex/campboardgamehost/clocktower/rules/PairInformationLegalDomain.kt",
            "src/main/java/com/codex/campboardgamehost/clocktower/rules/NaturalPairInformationCandidateGenerator.kt",
            "src/main/java/com/codex/campboardgamehost/clocktower/session/StorytellerDecisionFoundation.kt",
            "src/main/java/com/codex/campboardgamehost/clocktower/session/PairInformationDecisionBoundary.kt",
            "src/main/java/com/codex/campboardgamehost/clocktower/session/MayorRedirectDecisionBoundary.kt",
            "src/main/java/com/codex/campboardgamehost/clocktower/session/TroubleBrewingRuntimeGameProjector.kt",
        )

        engineOwnedPaths.forEach { path ->
            val source = File(path).readText(Charsets.UTF_8)
            assertFalse(
                "$path must not import recommendation implementation",
                source.contains("import com.codex.campboardgamehost.clocktower.recommendation."),
            )
        }
    }
}
