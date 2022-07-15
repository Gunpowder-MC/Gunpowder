import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.benmanes.gradle.versions.updates.gradle.GradleReleaseChannel
import com.martmists.commons.*

plugins {
    kotlin("jvm") version "1.7.10" apply false
    id("com.github.ben-manes.versions") version "0.42.0"
    id("net.saliman.properties") version "1.5.2"
}

group = "io.github.gunpowder"
version = "2.0.1"

subprojects {
    buildDir = file(rootProject.buildDir.absolutePath + "/" + project.name)
}

allprojects {
    repositories {
        mavenCentral()
        martmists()
    }

    group = rootProject.group
    version = rootProject.version

    tasks {
        withType<DependencyUpdatesTask> {
            gradleReleaseChannel = GradleReleaseChannel.CURRENT.id
            rejectVersionIf {
                isStable(currentVersion) && !isStable(candidate.version)
            }
        }
    }
}
