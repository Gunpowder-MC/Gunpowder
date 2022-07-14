pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        mavenCentral()
        gradlePluginPortal()
    }
}

buildscript {
    repositories {
        mavenCentral()
        maven("https://maven.martmists.com/releases")
    }
    dependencies {
        classpath("com.martmists.commons:commons-gradle:1.0.4")
    }
}

rootProject.name = "gunpowder"

include(":gunpowder-processor")
include(":gunpowder-base")
