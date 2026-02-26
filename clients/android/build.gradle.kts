plugins {
    id("android-app")
}

android {
    namespace = "com.appfactory.android"
}

dependencies {
    implementation(project(":clients:shared-ui"))
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation(project(":infrastructure"))
    implementation(libs.activity.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
}
