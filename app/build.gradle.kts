import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    id("jacoco")
    id("org.jlleitschuh.gradle.ktlint")
    kotlin("plugin.serialization") version "2.1.20"
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.mobile.micasaestucasa"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mobile.micasaestucasa"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableUnitTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    @Suppress("DEPRECATION")
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    secrets {
        // Reads MAPS_API_KEY from local.properties
        propertiesFileName = "local.properties"
        defaultPropertiesFileName = "local.defaults.properties"
    }
}

dependencies {
    implementation(libs.androidx.material3)
    implementation(libs.compose.material.icons.extended)
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    // compose-runtime is provided transitively by the Compose BOM
    val nav_version = "2.9.7"
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("io.coil-kt.coil3:coil-compose:3.4.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.4.0")
    // Google Maps Compose
    implementation("com.google.maps.android:maps-compose:6.5.2")
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.android)
    testImplementation(libs.mockk.agent.jvm)
    testImplementation(libs.kotlinx.coroutines.test)
    // Needed by ImageStyleTest: Dp is a pure-JVM value class from ui-unit
    testImplementation("androidx.compose.ui:ui-unit:1.7.8")

    // UI Testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")

    // Debug dependencies
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    implementation("com.google.firebase:firebase-messaging")

    // Navigation
    implementation("androidx.navigation:navigation-compose:$nav_version")
    implementation("androidx.navigation:navigation-fragment:$nav_version")
    implementation("androidx.navigation:navigation-ui:$nav_version")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    // Google Places API
    implementation("com.google.android.libraries.places:places:3.4.0")
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore.ktx)
    implementation("com.google.firebase:firebase-storage")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Google Credential Manager
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Resolve conflict for androidx.concurrent:concurrent-futures
    configurations.all {
        resolutionStrategy {
            force(libs.androidx.concurrent.futures)
            // Forziamo anche le versioni di test per evitare conflitti con la BOM di Compose
            force(libs.androidx.junit)
            force(libs.androidx.espresso.core)
            // Pin kotlin-stdlib to the declared Kotlin version to prevent transitive upgrades
            force("org.jetbrains.kotlin:kotlin-stdlib:2.1.20")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.1.20")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.20")
        }
    }
}

val fileFilter = listOf(
    "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
    "**/*Test*.*", "android/**/*.*", "**/*Compose*.*", "**/*_Provide*.*",
    "**/*_Factory*.*", "**/*_HiltModules*.*", "**/*Hilt*.*", "**/dagger/hilt/**/*.*",
    "**/ui/screens/**/*.*", "**/ui/theme/**/*.*", "**/ui/navigation/**/*.*",
    "**/*Screen*.*", "**/*Activity*.*"
)

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val kotlinClasses = fileTree("${layout.buildDirectory.get()}/intermediates/kotlin-classes/debug") { exclude(fileFilter) }
    val javaClasses = fileTree("${layout.buildDirectory.get()}/intermediates/javac/debug/classes") { exclude(fileFilter) }

    sourceDirectories.setFrom(files("$projectDir/src/main/java"))
    classDirectories.setFrom(files(kotlinClasses, javaClasses))
    executionData.setFrom(
        fileTree(layout.buildDirectory.get()) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "jacoco/testDebugUnitTest.exec",
                "outputs/jacoco/testDebugUnitTest.exec"
            )
        }
    )
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("jacocoTestReport")
    val kotlinClasses = fileTree("${layout.buildDirectory.get()}/intermediates/kotlin-classes/debug") { exclude(fileFilter) }
    val javaClasses = fileTree("${layout.buildDirectory.get()}/intermediates/javac/debug/classes") { exclude(fileFilter) }

    sourceDirectories.setFrom(files("$projectDir/src/main/java"))
    classDirectories.setFrom(files(kotlinClasses, javaClasses))
    executionData.setFrom(
        fileTree(layout.buildDirectory.get()) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "jacoco/testDebugUnitTest.exec",
                "outputs/jacoco/testDebugUnitTest.exec"
            )
        }
    )

    violationRules {
        rule {
            limit {
                minimum = 0.70.toBigDecimal()
            }
        }
    }
}
