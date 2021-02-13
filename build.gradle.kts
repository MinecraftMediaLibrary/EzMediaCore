/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/12/2021
 * ============================================================================
 */

plugins {
    `java-library`
    `maven-publish`
}

defaultTasks("clean", "build", "MinecraftMediaLibrary:shadowJar", "publishToMavenLocal")

allprojects {
    project.group = "com.github.pulsebeat02"
    project.version = "RELEASE-1.4.0"
}

subprojects {

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    project.java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    project.publishing {
        publications {
            create("mavenJava", MavenPublication::class) {
                from(project.components["java"])
            }
        }
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
    }

    repositories {
        mavenCentral()
        maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
        maven { url = uri("https://libraries.minecraft.net") }
        maven { url = uri("https://jitpack.io") }
        mavenLocal()
    }

}