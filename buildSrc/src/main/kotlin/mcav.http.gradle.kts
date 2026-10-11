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

plugins {
    id("mcav.module")
    id("mcav.publishing")
}

val node = rootProject.extensions.getByType<NodeExtension>()
val nodeSetup = rootProject.tasks.named("nodeSetup")
val npm = node.resolvedNodeDir.get().file(if (isWindows) "npm.cmd" else "bin/npm").asFile
val npmPath = npm.parentFile.absolutePath + File.pathSeparator + System.getenv("PATH")

val npmProjectInstall = tasks.register<Exec>("npmProjectInstall") {
    group = "build"
    description = "Installs the npm dependencies of the website"
    dependsOn(nodeSetup)
    workingDir = file("mcav-website")
    executable = npm.absolutePath
    environment("PATH", npmPath)
    args("ci")
    inputs.file("mcav-website/package.json")
    inputs.file("mcav-website/package-lock.json")
    outputs.file("mcav-website/node_modules/.package-lock.json")
}

val buildWebsite = tasks.register<Exec>("buildWebsite") {
    group = "build"
    description = "Builds the Next.js website"
    dependsOn(npmProjectInstall)
    workingDir = file("mcav-website")
    executable = npm.absolutePath
    environment("PATH", npmPath)
    environment("NODE_OPTIONS", "--max-old-space-size=4096")
    args("run", "build")
    inputs.dir("mcav-website/src")
    inputs.dir("mcav-website/public")
    inputs.file("mcav-website/package.json")
    inputs.file("mcav-website/next.config.ts")
    inputs.file("mcav-website/third-party-notices.mjs")
    inputs.file("mcav-website/package-lock.json")
    inputs.file("mcav-website/tsconfig.json")
    inputs.file("mcav-website/postcss.config.mjs")
    outputs.dir("mcav-website/out")
    outputs.cacheIf { false }
}

val testWebsite = tasks.register<Exec>("testWebsite") {
    group = "verification"
    description = "Runs the tests of the website"
    dependsOn(nodeSetup)
    workingDir = file("mcav-website")
    executable = npm.absolutePath
    environment("PATH", npmPath)
    args("test")
    inputs.dir("mcav-website/src")
    inputs.dir("mcav-website/test")
    inputs.file("mcav-website/package.json")
    val passed = layout.buildDirectory.file("website-tests/passed")
    outputs.file(passed)
    doLast {
        passed.get().asFile.writeText("passed\n")
    }
}

tasks.check {
    dependsOn(testWebsite)
}

tasks.jar {
    from(buildWebsite) {
        into("mcav/http/website")
    }
}

tasks.processTestResources {
    filesMatching("website/**") {
        path = "mcav/http/$path"
    }
}

tasks.named<Jar>("sourcesJar") {
    from(buildWebsite) {
        into("mcav/http/website")
    }
}

val builtJar = "-Dmcav.http.jar=" + layout.buildDirectory.file("libs/mcav-http.jar").get().asFile.absolutePath
tasks.test {
    dependsOn(tasks.jar)
    jvmArgs(builtJar)
}
tasks.named("pitest") {
    dependsOn(tasks.jar)
}
