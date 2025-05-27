// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false // Ensure this matches compose compiler
    id("com.google.dagger.hilt.android") version "2.48.1" apply false
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false // Ensure this matches kotlin version
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10") // Ensure this matches compose compiler
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.48.1")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
