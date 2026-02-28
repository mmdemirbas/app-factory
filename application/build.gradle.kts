plugins {
    id("kmp-library")
    id("detekt-rules")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain"))
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(project(":domain"))
            implementation(kotlin("test"))
            implementation(libs.kotest.assertions.core)
            implementation(libs.kotest.property)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlinx.datetime)
        }
        
        
        // Expose domain fakes to application tests without duplicating the domain tests during execution
        commonTest {
            kotlin.srcDir(project(":domain").file("src/commonTest/kotlin"))
            kotlin.exclude("**/*Test.kt", "**/*Spec.kt")
        }
        jvmTest.dependencies {
            implementation(libs.archunit.core)
            implementation(libs.kotest.runner.junit5)
        }
    }
}
