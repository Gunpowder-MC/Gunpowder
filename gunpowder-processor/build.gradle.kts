import com.martmists.commons.martmistsPublish

plugins {
    `maven-publish`

    kotlin("jvm")
    kotlin("kapt")
}

repositories {
    maven("https://maven.fabricmc.net/")
}

dependencies {
    kapt("com.google.auto.service:auto-service:1.0.1")
    implementation("com.google.auto.service:auto-service-annotations:1.0.1")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("net.fabricmc:sponge-mixin:0.11.4+mixin.0.8.5")
}

val publishEnabled: String? by project

if ((publishEnabled ?: "false").toBoolean()) {
    val publishUser: String by project
    val publishPassword: String by project
    val publishSnapshot: String by project
    val publishVersion: String? by project

    publishing {
        repositories {
            martmistsPublish(publishUser, publishPassword, publishSnapshot.toBoolean())
        }

        publications {
            create<MavenPublication>("jvm") {
                groupId = project.group as String
                artifactId = project.name
                version = publishVersion ?: project.version as String

                from(components["java"])
            }
        }
    }
}
