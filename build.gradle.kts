buildscript {
    repositories {
        google()

    }
    dependencies {

        classpath("com.google.gms:google-services:4.4.1")
        classpath("com.android.tools.build:gradle:7.1.3")

    }
}

plugins {
    id("com.android.application") version "8.1.3" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}