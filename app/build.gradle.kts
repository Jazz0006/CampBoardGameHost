import org.gradle.api.tasks.testing.Test

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.codex.campboardgamehost"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.codex.campboardgamehost"
        minSdk = 24
        targetSdk = 35
        versionCode = 5
        versionName = "0.1.4"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}

afterEvaluate {
    val debugUnitTest = tasks.named<Test>("testDebugUnitTest")

    tasks.register("testFull") {
        group = "verification"
        description = "Runs the complete Android debug JVM unit-test suite."
        dependsOn(debugUnitTest)
    }

    tasks.register<Test>("testFast") {
        group = "verification"
        description = "Runs the Android JVM fast regression suite."
        val sourceTask = debugUnitTest.get()
        testClassesDirs = sourceTask.testClassesDirs
        classpath = sourceTask.classpath

        filter {
            excludeTestsMatching("com.codex.campboardgamehost.clocktower.recommendation.setup.SetupMigrationTest")
            excludeTestsMatching("com.codex.campboardgamehost.clocktower.epistemic.ZddPlayerWorldSetTest")
            excludeTestsMatching("com.codex.campboardgamehost.clocktower.review.ExpertRecommendationReviewTest")
            excludeTestsMatching("com.codex.campboardgamehost.clocktower.simulation.StorytellerV4BaselineSimulationTest")
            excludeTestsMatching("com.codex.campboardgamehost.clocktower.epistemic.A4ZddBenchmarkTest")
        }
    }
}
