buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
    dependencies {

        classpath("com.google.gms:google-services:4.4.1")
        classpath("com.android.tools.build:gradle:7.1.3")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.0")
    }
}

plugins {
    id("com.android.application") version "8.0.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}