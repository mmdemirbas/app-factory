plugins {
    id("kmp-library")
    id("detekt-rules")
    id("org.jetbrains.kotlinx.kover")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Approved language-level libraries only — no vendor SDKs.
            // This is enforced by ArchUnit (DomainArchitectureTest).
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.uuid)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotest.assertions.core)
            implementation(libs.kotest.property)
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmTest.dependencies {
            // ArchUnit hard gate — runs as a test task in CI
            implementation(libs.archunit.core)
            implementation(libs.kotest.runner.junit5)
        }
    }
}
