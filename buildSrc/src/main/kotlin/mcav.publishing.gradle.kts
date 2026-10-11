/*
 * This file is part of mcav, a media playback library for Java
 * Copyright (C) Brandon Li <https://brandonli.me/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import me.brandonli.mcav.gradle.McavPublishingExtension

plugins {
    java
    `maven-publish`
    id("mcav.licensing")
}

val settings = extensions.create<McavPublishingExtension>("mcavPublishing")
settings.gradleModuleMetadata.convention(true)

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    enabled = settings.gradleModuleMetadata.get()
}

publishing {
    repositories {
        maven {
            name = "endToEnd"
            url = rootProject.layout.buildDirectory.dir("e2e-repository").get().asFile.toURI()
        }
        maven {
            name = "brandonli"
            url = uri("https://repo.brandonli.me/snapshots")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

afterEvaluate {
    publishing.publications.create<MavenPublication>("maven") {
        groupId = rootProject.group.toString()
        artifactId = project.name
        version = rootProject.version.toString()
        pom {
            name = project.name
            description = "MCAV multimedia library: ${project.name}"
            url = "https://github.com/PulseBeat02/mcav"
            licenses {
                license {
                    name = "GNU General Public License, version 3 or later"
                    url = "https://www.gnu.org/licenses/gpl-3.0.html"
                    distribution = "repo"
                }
            }
            scm {
                connection = "scm:git:https://github.com/PulseBeat02/mcav.git"
                developerConnection = "scm:git:ssh://git@github.com/PulseBeat02/mcav.git"
                url = "https://github.com/PulseBeat02/mcav"
            }
        }
        if (settings.bundledJar.isPresent) {
            artifact(settings.bundledJar.get())
            artifact(tasks.named("sourcesJar").get())
            artifact(tasks.named("javadocJar").get())
        } else {
            from(components["java"])
        }
        if (!settings.gradleModuleMetadata.get()) {
            suppressAllPomMetadataWarnings()
        }
    }
    val generatedPom = tasks.named<GenerateMavenPom>("generatePomFileForMavenPublication")
    val publishedPom = "-Dmcav.published.pom=" + generatedPom.get().destination.absolutePath
    tasks.named<Test>("test") {
        dependsOn(generatedPom)
        jvmArgs(publishedPom)
    }
    pluginManager.withPlugin("info.solidsoft.pitest") {
        tasks.named("pitest") {
            dependsOn(generatedPom)
        }
    }
}
