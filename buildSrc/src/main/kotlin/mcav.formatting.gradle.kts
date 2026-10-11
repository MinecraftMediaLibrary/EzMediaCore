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

import com.github.gradle.node.NodeExtension
import me.brandonli.mcav.gradle.isWindows
import me.brandonli.mcav.gradle.libs
import me.brandonli.mcav.gradle.versionOf

plugins {
    java
    id("com.diffplug.spotless")
}

val node = rootProject.extensions.getByType<NodeExtension>()
val nodeSetup = rootProject.tasks.named("nodeSetup")
val nodeExecutable = node.resolvedNodeDir.map { it.file(if (isWindows) "node.exe" else "bin/node").asFile }

spotless {
    java {
        prettier(mapOf("prettier" to libs.versionOf("prettier"), "prettier-plugin-java" to libs.versionOf("prettier-plugin-java")))
            .config(mapOf("parser" to "java", "tabWidth" to 2, "plugins" to listOf("prettier-plugin-java"), "printWidth" to 140))
            .nodeExecutable(nodeExecutable)
        licenseHeaderFile(rootProject.file("HEADER"))
        importOrder()
        removeUnusedImports()
        formatAnnotations()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("resources") {
        target(
            "src/**/*.json",
            "src/**/*.yml",
            "src/**/*.yaml",
            "src/**/*.properties",
            "src/**/*.astub",
            "coverage-exceptions.txt",
            "*.md"
        )
        targetExclude("**/build/**", "**/node_modules/**")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.matching { it.name.startsWith("spotlessJava") }.configureEach {
    dependsOn(nodeSetup)
}
