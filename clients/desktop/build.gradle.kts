plugins {
    id("jvm-desktop")
}

compose.desktop {
    application {
        mainClass = "com.appfactory.desktop.MainKt"
    }
}

dependencies {
    implementation(project(":clients:shared-ui"))
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation(project(":infrastructure"))
    implementation(compose.desktop.currentOs)
    implementation(libs.koin.core)
}
