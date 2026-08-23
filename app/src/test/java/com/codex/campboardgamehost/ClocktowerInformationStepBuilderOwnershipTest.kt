package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerInformationStepBuilderOwnershipTest {
    private fun findRepositoryRoot(): File {
        val knownHostSource = "app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt"
        val workingDirectory = System.getProperty("user.dir") ?: error("Working directory is unavailable")
        var directory = File(workingDirectory).absoluteFile
        while (true) {
            if (File(directory, knownHostSource).isFile) return directory
            val parent = directory.parentFile ?: error("Repository root not found from ${directory.path}")
            if (parent == directory) error("Repository root not found from ${directory.path}")
            directory = parent
        }
    }

    private val repoRoot = findRepositoryRoot()
    private val host = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    )
    private val builder = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/ClocktowerInformationStepBuilder.kt",
    )

    @Test
    fun `generic information step packaging has a dedicated stateless owner`() {
        assertTrue("Host source must exist", host.isFile)
        assertTrue("Dedicated information-step builder source must exist", builder.isFile)

        val hostText = host.readText()
        val builderText = builder.readText()

        assertFalse(
            "ClocktowerJudgeScreen must no longer own the generic infoStep implementation",
            hostText.contains("fun infoStep("),
        )
        assertTrue(
            "ClocktowerJudgeScreen must construct the dedicated information-step builder",
            hostText.contains("ClocktowerInformationStepBuilder("),
        )
        assertTrue(
            "ClocktowerJudgeScreen must route information-step construction through the builder",
            hostText.contains("informationStepBuilder.build("),
        )
        assertTrue(
            "Dedicated owner must expose ClocktowerInformationStepBuilder",
            builderText.contains("internal class ClocktowerInformationStepBuilder("),
        )
        assertTrue(
            "Dedicated owner must own the generic build seam",
            builderText.contains("fun build("),
        )
        assertTrue(
            "Dedicated owner must preserve complete legacy candidate packaging",
            builderText.contains("legacyInformationCandidates ="),
        )
        assertTrue(
            "Dedicated owner must preserve effective registration-key handling",
            builderText.contains("RegistrationInteractionRules.effectiveRegistrationKey("),
        )
        assertTrue(
            "Dedicated owner must preserve information reliability mapping",
            builderText.contains("informationReliability ="),
        )

        listOf("remember(", "mutableStateOf(", "LaunchedEffect(", "MutableState<").forEach { forbidden ->
            assertFalse("Information-step builder must not own Compose state/effect lifecycle: $forbidden", builderText.contains(forbidden))
        }
        listOf(
            "ClocktowerRecommendationCoordinator",
            "recommendNumber(",
            "recommendPair(",
            "recommendRegistration(",
            "resolveDynamicDecision(",
        ).forEach { forbidden ->
            assertFalse("Information-step builder must not absorb recommendation/dynamic-decision ownership: $forbidden", builderText.contains(forbidden))
        }
    }
}
