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

plugins {
    id("mcav.module")
    id("mcav.licensing")
}

repositories {
    exclusiveContent {
        forRepository {
            maven("https://maven.fabricmc.net/") {
                name = "Fabric"
            }
        }
        filter {
            includeGroupAndSubgroups("net.fabricmc")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://maven.neoforged.net/releases") {
                name = "NeoForged"
                metadataSources {
                    artifact()
                }
            }
        }
        filter {
            includeGroupAndSubgroups("net.neoforged")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://libraries.minecraft.net") {
                name = "Minecraft libraries"
            }
        }
        filter {
            includeGroup("com.mojang")
        }
    }
    exclusiveContent {
        forRepository {
            ivy("https://piston-data.mojang.com/v1/objects/") {
                name = "Minecraft client"
                patternLayout {
                    artifact("[revision]/[module].[ext]")
                }
                metadataSources {
                    artifact()
                }
            }
        }
        filter {
            includeModule("net.minecraft", "client")
        }
    }
}

tasks.processResources {
    val modVersion = rootProject.version.toString()
    inputs.property("modVersion", modVersion)
    filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) {
        expand("version" to modVersion)
    }
}

tasks.jar {
    archiveClassifier = "plain"
}

val fabricJar = tasks.register<Jar>("fabricJar") {
    group = "build"
    description = "Assembles the mod for Fabric"
    archiveClassifier = "fabric"
    from(sourceSets.main.map { it.output })
    exclude("**/NeoForge*.class", "META-INF/neoforge.mods.toml")
}

val neoforgeJar = tasks.register<Jar>("neoforgeJar") {
    group = "build"
    description = "Assembles the mod for NeoForge"
    archiveClassifier = "neoforge"
    from(sourceSets.main.map { it.output })
    exclude("**/Fabric*.class", "fabric.mod.json")
}

tasks.assemble {
    dependsOn(fabricJar, neoforgeJar)
}
