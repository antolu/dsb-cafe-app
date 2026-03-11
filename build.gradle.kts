plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}