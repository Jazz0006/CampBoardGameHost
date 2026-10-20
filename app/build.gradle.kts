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

    testOptions {
        unitTests.isIncludeAndroidResources = true
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
    testImplementation(composeBom)
    testImplementation("androidx.compose.ui:ui-test-junit4")
    testImplementation("org.robolectric:robolectric:4.16.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

afterEvaluate {
    tasks.withType<Test>().configureEach {
        systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
    }
    val debugUnitTest = tasks.named<Test>("testDebugUnitTest")

    tasks.register("testFull") {
        group = "verification"
        description = "Runs the complete Android debug JVM regression suite."
        dependsOn(debugUnitTest)
    }

    tasks.register<Test>("testFast") {
        group = "verification"
        description = "Runs the Android JVM fast regression suite."
        val sourceTask = debugUnitTest.get()
        testClassesDirs = sourceTask.testClassesDirs
        classpath = sourceTask.classpath
        exclude("**/ClocktowerD2UiTest*")
    }
}
