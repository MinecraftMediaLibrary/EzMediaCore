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

import me.brandonli.mcav.gradle.JcstressBudget
import me.brandonli.mcav.gradle.JcstressConsole
import me.brandonli.mcav.gradle.libraryOf
import me.brandonli.mcav.gradle.libs
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("mcav.java-library")
    id("mcav.formatting")
    id("mcav.lint")
}

dependencies {
    implementation(libs.libraryOf("jcstress-core"))
    annotationProcessor(libs.libraryOf("jcstress-core"))
}

checkerFramework {
    skipCheckerFramework = true
}

tasks.named<JavaCompile>("compileJava") {
    options.compilerArgs.remove("-Werror")
    options.errorprone {
        excludedPaths = ".*/build/generated/.*"
    }
}

val jcstressJar = tasks.register<Jar>("jcstressJar") {
    group = "build"
    description = "Packs the jcstress tests with everything they run"
    archiveClassifier = "jcstress"
    manifest {
        attributes("Main-Class" to "org.openjdk.jcstress.Main")
    }
    from(sourceSets.main.get().output)
    val runtimeClasspath = configurations.runtimeClasspath
    dependsOn(runtimeClasspath)
    from(runtimeClasspath.map { classpath -> classpath.map { if (it.isDirectory) it else zipTree(it) } }) {
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val ITERATION_TIME = "jcstress: {} tests in {} mode within {} minutes: {} ms per iteration"
val mode = providers.gradleProperty("jcstress.mode").getOrElse("quick")
val budgetMinutes = providers.gradleProperty("jcstress.timeBudgetMinutes").map { it.toInt() }
val selection = providers.gradleProperty("jcstress.tests")
val cpus = providers.gradleProperty("jcstress.cpus").getOrElse(minOf(4, Runtime.getRuntime().availableProcessors()).toString())

tasks.register<JavaExec>("jcstressTest") {
    group = "verification"
    description = "Runs the jcstress concurrency tests; -Pjcstress.mode, -Pjcstress.timeBudgetMinutes, -Pjcstress.tests"
    val jar = jcstressJar.flatMap { it.archiveFile }
    inputs.file(jar)
    classpath = files(jar)
    mainClass = "org.openjdk.jcstress.Main"
    val workingDirectory = layout.buildDirectory.dir("jcstress")
    workingDir = workingDirectory.get().asFile
    val arguments = mutableListOf("-m", mode, "-c", cpus, "-r", "results", "-v")
    if (selection.isPresent) {
        arguments += listOf("-t", selection.get())
    }
    args(arguments)
    val budget = budgetMinutes.orNull
    val tests = selection.orNull
    val cpuCount = cpus.toInt()
    val stressMode = mode
    val iterationTime = ITERATION_TIME
    val console = workingDirectory.map { it.file("console.txt") }
    doFirst {
        workingDirectory.get().asFile.mkdirs()
        standardOutput = JcstressConsole.tee(System.out, console.get().asFile)
        if (budget != null) {
            val testCount = JcstressBudget.countTests(jar.get().asFile, tests)
            val iterationMillis = JcstressBudget.iterationMillis(stressMode, budget, testCount, cpuCount)
            logger.lifecycle(iterationTime, testCount, stressMode, budget, iterationMillis)
            args("-time", iterationMillis.toString())
        }
    }
    doLast {
        standardOutput.close()
        JcstressConsole.checkNoneSkipped(console.get().asFile)
    }
    outputs.upToDateWhen { false }
}
