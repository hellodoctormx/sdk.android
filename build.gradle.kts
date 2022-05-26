// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id ("com.android.library") version "7.2.0" apply false
    id ("org.jetbrains.kotlin.android") version "1.6.10" apply false
    id ("org.jetbrains.kotlin.multiplatform") version "1.6.10"
    id ("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"
    id ("com.google.gms.google-services") version "4.3.10"
}

kotlin {
    jvm()
}

buildscript {
    extra.apply {
        set("compileSdk", 32)
    }
}