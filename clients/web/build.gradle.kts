plugins {
    id("wasm-web")
}

kotlin {
    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":clients:shared-ui"))
            implementation(project(":domain"))
            implementation(project(":application"))
            implementation(project(":infrastructure"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
        }
    }
}
