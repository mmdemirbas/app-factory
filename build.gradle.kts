// Root build file — no code here.
// Each module has its own build.gradle.kts using convention plugins from build-logic.
plugins {
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.detekt) apply false
}
