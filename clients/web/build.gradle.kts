plugins {
    id("wasm-web")
}

val syncModeProperty = providers.gradleProperty("appfactory.sync.mode").orElse("powersync")
val backendBaseUrlProperty = providers.gradleProperty("appfactory.backend.baseUrl").orElse("http://localhost:8081")
val generatedSyncProfileDir = layout.buildDirectory.dir("generated/source/syncProfile/wasmJsMain/kotlin")

abstract class GenerateWebSyncProfileTask : DefaultTask() {
    @get:Input
    abstract val syncMode: Property<String>

    @get:Input
    abstract val backendBaseUrl: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val mode = syncMode.get().lowercase()
        val modeExpression = when (mode) {
            "powersync" -> "SyncEngineMode.PowerSync"
            "backend" -> "SyncEngineMode.BackendTransport(\"${backendBaseUrl.get().escapeForKotlin()}\")"
            else -> error("Unsupported appfactory.sync.mode='$mode'. Supported values: powersync, backend.")
        }

        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package com.appfactory.web

            import com.appfactory.infrastructure.sync.SyncEngineMode

            internal val configuredSyncMode: SyncEngineMode = $modeExpression
            """.trimIndent() + "\n"
        )
    }

    private fun String.escapeForKotlin(): String = replace("\\", "\\\\").replace("\"", "\\\"")
}

val generateWebSyncProfile by tasks.registering(GenerateWebSyncProfileTask::class) {
    syncMode.set(syncModeProperty)
    backendBaseUrl.set(backendBaseUrlProperty)
    outputFile.set(generatedSyncProfileDir.map { it.file("com/appfactory/web/SyncProfileConfig.kt") })
}

kotlin {
    sourceSets {
        val wasmJsMain by getting {
            kotlin.srcDir(generatedSyncProfileDir)
            dependencies {
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
}

tasks.named("compileKotlinWasmJs").configure {
    dependsOn(generateWebSyncProfile)
}
