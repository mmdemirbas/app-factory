import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

// Apply plugins imperatively — avoids the plugins{} block compilation stage
// which has restrictions on plugin resolution in precompiled script plugins.
apply(plugin = "org.jetbrains.kotlin.multiplatform")
// AGP 9.0+: com.android.kotlin.multiplatform.library replaces the old
// com.android.library + kotlin.multiplatform combination.
apply(plugin = "com.android.kotlin.multiplatform.library")
apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
apply(plugin = "org.jetbrains.kotlinx.kover")

// Configure Android library target via the KotlinMultiplatformAndroidLibraryTarget
// extension registered on the kotlin extension by the AGP KMP plugin.
// Generated type-safe accessors are not available in precompiled script plugins,
// so we use explicit getByName + cast.
((extensions.getByName("kotlin") as org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension)
    as org.gradle.api.plugins.ExtensionAware)
    .extensions.getByName("android")
    .let { (it as com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget).apply {
        compileSdk = 35
        minSdk = 26
        namespace = "com.appfactory" + project.path.replace(":", ".").replace("-", "")
    }}

extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
    jvmToolchain(17)
    jvm()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }
}

tasks.withType<Test> { useJUnitPlatform() }

tasks.withType<AbstractTestTask>().configureEach {
    // Workaround for KMP Native test runners discovering test files but no runnable Kotest specs.
    // Kotest is only configured for JVM in this repository.
    try {
        if (hasProperty("failOnNoDiscoveredTests")) {
            setProperty("failOnNoDiscoveredTests", false)
        }
    } catch (e: Exception) {
        // Ignore reflection failures
    }
}
