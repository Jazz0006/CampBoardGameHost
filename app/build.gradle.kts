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
    val sde2D4ScaleBenchmark =
        "com.codex.campboardgamehost.clocktower.epistemic.Sde2D4ScaleBenchmarkTest"
    val sde2D5FExpertObservedCalibrationExperiment =
        "com.codex.campboardgamehost.clocktower.review.Sde2D5FExpertObservedCalibrationExperiment"
    val c5bG10FullDomainTest =
        "com.codex.campboardgamehost.clocktower.recommendation.sde.PairInformationShadowReplayBridgeG10FullDomainTest"

    val manualCostExperiments = listOf(
        "com.codex.campboardgamehost.clocktower.epistemic.Sde2D4TopologyBundlePerformanceTest",
    )

    // Remaining explicit scale, calibration and topology evidence harnesses are not
    // ordinary regression tests. Keep FULL bounded; run these through dedicated tasks.
    debugUnitTest.configure {
        filter {
            manualCostExperiments.forEach { excludeTestsMatching(it) }
            excludeTestsMatching(sde2D4ScaleBenchmark)
            excludeTestsMatching(sde2D5FExpertObservedCalibrationExperiment)
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
            manualCostExperiments.forEach { excludeTestsMatching(it) }
            excludeTestsMatching(sde2D5FExpertObservedCalibrationExperiment)
            excludeTestsMatching(c5bG10FullDomainTest)
            excludeTestsMatching("com.codex.campboardgamehost.clocktower.epistemic.ZddPlayerWorldSetTest")
            excludeTestsMatching(sde2D4ScaleBenchmark)
        }
    }

    tasks.register<Test>("testCostExperiments") {
        group = "verification"
        description = "Runs historical topology performance experiments explicitly."
        val sourceTask = debugUnitTest.get()
        testClassesDirs = sourceTask.testClassesDirs
        classpath = sourceTask.classpath
        filter { manualCostExperiments.forEach { includeTestsMatching(it) } }
        outputs.upToDateWhen { false }
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

    tasks.register<Test>("sde2D5FExpertObservedCalibration") {
        group = "verification"
        description = "Runs the explicit SDE-2D5F-B4 expert-observed evidence workload."
        val sourceTask = debugUnitTest.get()
        testClassesDirs = sourceTask.testClassesDirs
        classpath = sourceTask.classpath

        filter {
            includeTestsMatching(sde2D5FExpertObservedCalibrationExperiment)
        }

        outputs.upToDateWhen { false }
    }


}
