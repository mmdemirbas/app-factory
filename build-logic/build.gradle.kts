plugins {
    `kotlin-dsl`
}

group = "com.appfactory.buildlogic"

dependencies {
    // implementation (not compileOnly) is required in Gradle 9.x so that plugin
    // JARs are on the runtime classpath of the included build and their plugin
    // descriptors are visible to the main build's plugin registry.
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.kotlin.compose.gradlePlugin)
    implementation(libs.kotlin.serialization.gradlePlugin)
    implementation(libs.kover.gradlePlugin)
}
