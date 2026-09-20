import org.gradle.api.tasks.testing.Test

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val fieldTestVersionCode = providers.gradleProperty("fieldTestVersionCode").orNull?.toInt()
val fieldTestVersionName = providers.gradleProperty("fieldTestVersionName").orNull

android {
    namespace = "com.codex.campboardgamehost"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.codex.campboardgamehost"
        minSdk = 24
        targetSdk = 35
        versionCode = fieldTestVersionCode ?: 5
        versionName = fieldTestVersionName ?: "0.1.4"
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".fieldtest"
            versionNameSuffix = "-fieldtest"
        }
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
    val fnBundle3CalibrationExperiment =
        "com.codex.campboardgamehost.clocktower.review.FirstNightBundleBeginnerCorpusExperiment"
    val sde2D4ScaleBenchmark =
        "com.codex.campboardgamehost.clocktower.epistemic.Sde2D4ScaleBenchmarkTest"
    val sde2D5CalibrationExperiment =
        "com.codex.campboardgamehost.clocktower.review.Sde2D5CalibrationExperiment"
    val sde2D5FManifestGenerationExperiment =
        "com.codex.campboardgamehost.clocktower.review.Sde2D5FManifestGenerationExperiment"

    // Corpus generation and raw-enumerator scale measurement are explicit T3 evidence harnesses,
    // not regression tests. Keep them out of the default Android unit-test task so FULL remains
    // bounded while each harness stays directly runnable through its dedicated task below.
    debugUnitTest.configure {
        filter {
            excludeTestsMatching(fnBundle3CalibrationExperiment)
            excludeTestsMatching(sde2D4ScaleBenchmark)
            excludeTestsMatching(sde2D5CalibrationExperiment)
        }
    }

    tasks.register("testFull") {
        group = "verification"
        description = "Runs the complete bounded Android debug JVM regression suite."
        dependsOn(debugUnitTest)
    }

    tasks.register<Test>("testFast") {
        group = "verification"
        description = "Runs the Android JVM fast regression suite."
        val sourceTask = debugUnitTest.get()
        testClassesDirs = sourceTask.testClassesDirs
        classpath = sourceTask.classpath

        filter {
            // testFast is a separate Test task and does not inherit debugUnitTest's filter.
            excludeTestsMatching(fnBundle3CalibrationExperiment)
            excludeTestsMatching(sde2D5CalibrationExperiment)
            excludeTestsMatching("com.codex.campboardgamehost.clocktower.recommendation.setup.SetupMigrationTest")
            excludeTestsMatching("com.codex.campboardgamehost.clocktower.epistemic.ZddPlayerWorldSetTest")
            excludeTestsMatching("com.codex.campboardgamehost.clocktower.review.ExpertRecommendationReviewTest")
            excludeTestsMatching("com.codex.campboardgamehost.clocktower.simulation.StorytellerV4BaselineSimulationTest")
            excludeTestsMatching("com.codex.campboardgamehost.clocktower.epistemic.A4ZddBenchmarkTest")
            excludeTestsMatching(sde2D4ScaleBenchmark)
            excludeTestsMatching("com.codex.campboardgamehost.clocktower.epistemic.Sde2D4TopologyBundlePerformanceTest")
            excludeTestsMatching("com.codex.campboardgamehost.clocktower.epistemic.TroubleBrewingTopologySetupWitnessDifferentialTest")
            excludeTestsMatching("com.codex.campboardgamehost.clocktower.epistemic.TroubleBrewingTopologyObservationDifferentialTest")
            excludeTestsMatching("com.codex.campboardgamehost.clocktower.epistemic.TroubleBrewingTopologyBundleDifferentialTest")
            excludeTestsMatching("com.codex.campboardgamehost.clocktower.recommendation.sde.DemonBluffJointOutputEvaluatorTest")
        }
    }

    tasks.register<Test>("sde2D4ScaleBenchmark") {
        group = "verification"
        description = "Runs the explicit SDE-2D4 raw-enumerator scale evidence harness."
        val sourceTask = debugUnitTest.get()
        testClassesDirs = sourceTask.testClassesDirs
        classpath = sourceTask.classpath

        filter {
            includeTestsMatching(sde2D4ScaleBenchmark)
        }

        outputs.upToDateWhen { false }
    }

    tasks.register<Test>("sde2D5Calibration") {
        group = "verification"
        description = "Runs the explicit SDE-2D5 cross-regime calibration evidence workload."
        val sourceTask = debugUnitTest.get()
        testClassesDirs = sourceTask.testClassesDirs
        classpath = sourceTask.classpath

        filter {
            includeTestsMatching(sde2D5CalibrationExperiment)
        }

        outputs.upToDateWhen { false }
    }

    tasks.register<Test>("sde2D5FManifestGeneration") {
        group = "verification"
        description = "Temporarily materializes the corrected D5F-B3 v2 review manifest."
        val sourceTask = debugUnitTest.get()
        testClassesDirs = sourceTask.testClassesDirs
        classpath = sourceTask.classpath

        filter {
            includeTestsMatching(sde2D5FManifestGenerationExperiment)
        }

        outputs.upToDateWhen { false }
    }

    tasks.register<Test>("fnBundle3Calibration") {
        group = "verification"
        description = "Runs the explicit FN-BUNDLE-3 calibration corpus experiment."
        val sourceTask = debugUnitTest.get()
        testClassesDirs = sourceTask.testClassesDirs
        classpath = sourceTask.classpath

        filter {
            includeTestsMatching(fnBundle3CalibrationExperiment)
        }

        outputs.upToDateWhen { false }
    }
}
