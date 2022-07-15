import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.martmists.commons.commonJVMModule
import com.martmists.commons.martmistsPublish
import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import groovy.lang.Closure
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    kotlin("jvm")
    id("fabric-loom") version "0.11-SNAPSHOT"

    kotlin("kapt")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.matthewprenger.cursegradle") version "1.4.0"
}

val minecraftVersion = "1.18.2"
val javaVersion = JavaVersion.VERSION_17
project.version = "${version}+${minecraftVersion}"

repositories {
    maven("https://maven.nucleoid.xyz")
}

val shade by configurations.creating

fun ExternalModuleDependency.excludeKotlin() {
    exclude(mapOf(
        "group" to "org.jetbrains.kotlin",
    ))
    exclude(mapOf(
        "group" to "org.jetbrains.kotlinx",
    ))
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$minecraftVersion+build.3:v2")
    modImplementation("net.fabricmc:fabric-loader:0.14.8")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.57.0+${minecraftVersion}")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.8.0+kotlin.1.7.0")

    include(modApi("fr.catcore:server-translations-api:1.4.12+1.18.2")!!)

    shade(api(commonJVMModule("logging")) {
        excludeKotlin()
    })
    shade(api(commonJVMModule("database")){
        excludeKotlin()
    })
    shade(api(commonJVMModule("config")){
        excludeKotlin()
    })

    // Dependency injection
    shade(api("io.insert-koin:koin-core:3.2.0") {
        excludeKotlin()
    })

    // Database driver
    shade(implementation("org.xerial:sqlite-jdbc:3.36.0.3")!!)

    // YAML loading
    shade(implementation("org.yaml:snakeyaml:1.30")!!)
    shade(implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.3")!!)

    // Automatically collect mixins
    kapt(project(":gunpowder-processor"))
}

loom {
    accessWidenerPath.set(file("${project.projectDir.absolutePath}/src/main/resources/gunpowder_base.accesswidener"))
}

kapt {
    useBuildCache = false

    arguments {
        arg("mixin.package", "io.github.gunpowder.mixin")
        arg("mixin.name", "base")
        arg("mixin.plugin", "false")
    }
}

java {
    withSourcesJar()
}

sourceSets {
    val stub by creating
    val main by getting {
        resources.srcDirs("${project.buildDir.absolutePath}/generated/source/kapt/main/resources")
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
            freeCompilerArgs += listOf("-Xcontext-receivers")
        }
    }

    withType<ProcessResources> {
        dependsOn("kaptKotlin")

        filesMatching("fabric.mod.json") {
            expand(
                "version" to version
            )
        }
    }

    named<Jar>("jar") {
        enabled = false
    }

    val shadowJar by named<ShadowJar>("shadowJar") {
        configurations = listOf(shade)
        destinationDirectory.set(file("${project.buildDir.absolutePath}/devlibs/"))
    }

    val remapJar by named<RemapJarTask>("remapJar") {
        inputFile.set(shadowJar.archiveFile)
    }

    val apiJar = task<Jar>("apiJar") {
        dependsOn(remapJar)
        archiveClassifier.set("maven")

        from(sourceSets["stub"].output)
        from(zipTree(remapJar.archiveFile)) {
            include("io/github/gunpowder/api/**")
        }
    }

    named("build") {
        dependsOn("remapJar", apiJar)
    }
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
                groupId = project.group.toString()
                artifactId = rootProject.name
                version = publishVersion?.let { "$it+${minecraftVersion}" } ?: project.version.toString()

                artifact(tasks.named("apiJar")) {
                    classifier = ""
                }
                artifact(tasks.named("sourcesJar")) {
                    classifier = "sources"
                }

                // Only publish runtime jar for release builds
                // I'm not storing 15MB for every commit smh
                if (!publishSnapshot.toBoolean()) {
                    artifact(tasks.named("remapJar")) {
                        classifier = "runtime"
                    }
                }
            }
        }
    }

    val curseToken: String? by project
    val curseId: String by project

    if (curseToken != null) {
        curseforge {
            apiKey = curseToken
            curseProjects.add(CurseProject().apply {
                apiKey = this@curseforge.apiKey
                id = curseId
                releaseType = if (publishSnapshot.toBoolean()) "alpha" else "release"
                changelogType = "markdown"
                changelog = rootProject.file("CHANGELOG.md").readText().split("---").first()
                addGameVersion(minecraftVersion)
                addGameVersion("Fabric")
                addGameVersion("Java $javaVersion")

                curseRelations = mutableSetOf<Closure<*>>().also {
                    it.add(closureOf<CurseRelation> {
                        requiredDependency("fabric-api")
                        requiredDependency("fabric-language-kotlin")
                        embeddedLibrary("server-translation-api")
                    })
                }

                mainArtifact(tasks.getByName<RemapJarTask>("remapJar").archiveFile, closureOf<CurseArtifact> {
                    displayName = "gunpowder-${project.version}${if (publishSnapshot.toBoolean()) "-$publishVersion" else ""}.jar"
                })
            })

            curseGradleOptions.apply {
                forgeGradleIntegration = false
                javaIntegration = false
            }
        }
    }
}
