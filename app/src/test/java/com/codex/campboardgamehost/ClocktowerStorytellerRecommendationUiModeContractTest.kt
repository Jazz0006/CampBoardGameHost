package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerStorytellerRecommendationUiModeContractTest {
    @Test
    fun `setup recommendation UI does not expose legacy recommendation styles`() {
        val source = recommendationUiSource()

        assertFalse(source.contains("RecommendationStyle.entries.forEach"))
        assertFalse(source.contains("showOtherPlans"))
        assertFalse(source.contains("fun styleName(style: RecommendationStyle)"))
        assertFalse(source.contains("默认选择平衡方案；熟练说书人可比较三种风格。"))
        assertFalse(source.contains("全自动模式已采用平衡方案，不显示其他候选裁定。"))
        assertFalse(source.contains("查看其他方案"))
        assertFalse(source.contains("只看默认方案"))
    }

    @Test
    fun `experienced setup recommendation UI keeps recommendation and manual override`() {
        val source = recommendationUiSource()

        assertTrue(source.contains("系统推荐"))
        assertTrue(source.contains("修改裁定"))
        assertTrue(source.contains("if (!automaticStorytellerInfo && editingDecisions)"))
    }

    @Test
    fun `beginner setup recommendation UI keeps failure states but no strategic controls`() {
        val source = recommendationUiSource()

        assertTrue(source.contains("RecommendationUiState.Loading"))
        assertTrue(source.contains("RecommendationUiState.Empty"))
        assertTrue(source.contains("is RecommendationUiState.Error"))
        assertTrue(source.contains("if (!automaticStorytellerInfo)"))
    }

    private fun recommendationUiSource(): String {
        val relative = Path.of("src/main/java/com/codex/campboardgamehost/ClocktowerStorytellerRecommendationUi.kt")
        val fromRoot = Path.of("app").resolve(relative)
        val path = when {
            Files.exists(relative) -> relative
            Files.exists(fromRoot) -> fromRoot
            else -> error("ClocktowerStorytellerRecommendationUi.kt source not found from ${Path.of("").toAbsolutePath()}")
        }
        return String(Files.readAllBytes(path), Charsets.UTF_8)
    }
}
