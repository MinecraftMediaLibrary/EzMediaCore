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

import me.brandonli.mcav.gradle.PythonEnvironment
import me.brandonli.mcav.gradle.PythonTests
import me.brandonli.mcav.gradle.addLwjglTestNatives
import me.brandonli.mcav.gradle.libraryOf
import me.brandonli.mcav.gradle.libs

plugins {
    java
}

dependencies {
    val libraries = listOf("lwjgl", "lwjgl-shaderc", "lwjgl-spvc").map { libs.libraryOf(it) }
    libraries.forEach { testImplementation(it) }
    addLwjglTestNatives(libraries)
}

val writeToolsClasspath = tasks.register("writeMcv2ToolsClasspath") {
    group = "verification"
    description = "Writes the test runtime classpath that Mcv2ToolsTest starts Mcv2Tools with"
    val output = layout.buildDirectory.file("mcv2-tools-classpath.txt")
    val classpath = sourceSets.test.map { it.runtimeClasspath }
    inputs.files(classpath)
    outputs.file(output)
    doLast {
        output.get().asFile.writeText(classpath.get().asPath)
    }
}

tasks.test {
    inputs.files(writeToolsClasspath)
}
pluginManager.withPlugin("info.solidsoft.pitest") {
    tasks.named("pitest") {
        inputs.files(writeToolsClasspath)
    }
}

val pythonEnvironment = rootProject.tasks.named<PythonEnvironment>("installPythonEnvironment")
val pythonSources = layout.projectDirectory.dir("src/test/python")
val pythonTest = tasks.register<PythonTests>("pythonTest") {
    group = "verification"
    description = "Runs the Python codec tests in parallel processes, then the timing tests alone"
    useEnvironment(pythonEnvironment)
    parallelTests.from(fileTree(pythonSources) {
        include("test_*.py")
        exclude("test_shader_timing.py")
    })
    serialTests.from(pythonSources.file("test_shader_timing.py"))
    sources.from(
        fileTree(pythonSources) { exclude("test_*.py", "**/__pycache__/**") },
        layout.projectDirectory.dir("src/test/resources/mcv2"),
        layout.projectDirectory.dir("src/main/resources/mcav/mcv2/pack"),
        layout.settingsDirectory.dir("mcav-docs/images/mcv2")
    )
    testDirectory = pythonSources
    processCount = gradle.startParameter.maxWorkerCount
    reportDirectory = layout.buildDirectory.dir("test-results/pythonTest")
}
tasks.named("check") {
    dependsOn(pythonTest)
}
