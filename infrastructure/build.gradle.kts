plugins {
    id("kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain"))
            implementation(project(":application"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation("com.benasher44:uuid:0.8.4") // KMP UUID library matching domain usage
            implementation("io.github.jan-tennert.supabase:supabase-kt:3.1.2")
            implementation("io.github.jan-tennert.supabase:auth-kt:3.1.2")
            implementation("io.github.jan-tennert.supabase:postgrest-kt:3.1.2")
            implementation("io.github.jan-tennert.supabase:realtime-kt:3.1.2")
        }
        
        androidMain.dependencies {
            // Placeholder: Custom Android implementation or native SQLite directly, 
            // since app.cash.sqldelight:android-driver breaks AGP 9.0 BaseVariant init
        }
        
        jvmMain.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
        
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        
        wasmJsMain.dependencies {
            implementation(libs.sqldelight.web.driver)
            implementation(libs.kotlinx.browser)
        }
    }
}

kotlin {
    android {
        namespace = "com.appfactory.infrastructure"
    }
}
