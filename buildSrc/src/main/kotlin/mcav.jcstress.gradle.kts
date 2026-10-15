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

val jcstressAlias: String = "jcstress-core"
val generatedSourcePattern: String = ".*/build/generated/.*"
val jcstressClassifier: String = "jcstress"
val jcstressMainClass: String = "org.openjdk.jcstress.Main"
val signatureFiles: List<String> = listOf("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

val modeProperty: String = "jcstress.mode"
val budgetProperty: String = "jcstress.timeBudgetMinutes"
val selectionProperty: String = "jcstress.tests"
val cpusProperty: String = "jcstress.cpus"
val defaultMode: String = "quick"
val maximumDefaultCpus: Int = 4

val workingPath: String = "jcstress"
val resultsPath: String = "results"
val consolePath: String = "console.txt"

dependencies {
    implementation(libs.libraryOf(jcstressAlias))
    annotationProcessor(libs.libraryOf(jcstressAlias))
}

checkerFramework {
    skipCheckerFramework = true
}

tasks.named<JavaCompile>("compileJava") {
    options.compilerArgs.remove("-Werror")
    options.errorprone {
        excludedPaths = generatedSourcePattern
    }
}

val jcstressJar = tasks.register<Jar>("jcstressJar") {
    group = "build"
    description = "Packs the jcstress tests with everything they run"
    archiveClassifier = jcstressClassifier
    manifest {
        attributes("Main-Class" to jcstressMainClass)
    }
    from(sourceSets.main.get().output)
    val runtimeClasspath = configurations.runtimeClasspath
    dependsOn(runtimeClasspath)
    from(runtimeClasspath.map { classpath -> classpath.map { if (it.isDirectory) it else zipTree(it) } }) {
        exclude(signatureFiles)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val ITERATION_TIME = "Running {} jcstress tests in {} mode within {} minutes, at {} ms per iteration."
val mode = providers.gradleProperty(modeProperty).getOrElse(defaultMode)
val budgetMinutes = providers.gradleProperty(budgetProperty).map { it.toInt() }
val selection = providers.gradleProperty(selectionProperty)
val defaultCpus = minOf(maximumDefaultCpus, Runtime.getRuntime().availableProcessors()).toString()
val cpus = providers.gradleProperty(cpusProperty).getOrElse(defaultCpus)

tasks.register<JavaExec>("jcstressTest") {
    group = "verification"
    description = "Runs the jcstress concurrency tests; -Pjcstress.mode, -Pjcstress.timeBudgetMinutes, -Pjcstress.tests"
    val jar = jcstressJar.flatMap { it.archiveFile }
    inputs.file(jar)
    classpath = files(jar)
    mainClass = jcstressMainClass
    val workingDirectory = layout.buildDirectory.dir(workingPath)
    workingDir = workingDirectory.get().asFile
    val arguments = mutableListOf("-m", mode, "-c", cpus, "-r", resultsPath, "-v")
    if (selection.isPresent) {
        arguments += listOf("-t", selection.get())
    }
    args(arguments)
    val budget = budgetMinutes.orNull
    val tests = selection.orNull
    val cpuCount = cpus.toInt()
    val stressMode = mode
    val iterationTime = ITERATION_TIME
    val console = workingDirectory.map { it.file(consolePath) }
    doFirst {
        workingDirectory.get().asFile.mkdirs()
        standardOutput = JcstressConsole.tee(System.out, console.get().asFile)
        if (budget != null) {
            val testCount = JcstressBudget.countTests(jar.get().asFile, tests)
            val iterationMilliseconds = JcstressBudget.iterationMilliseconds(stressMode, budget, testCount, cpuCount)
            logger.lifecycle(iterationTime, testCount, stressMode, budget, iterationMilliseconds)
            args("-time", iterationMilliseconds.toString())
        }
    }
    doLast {
        standardOutput.close()
        JcstressConsole.checkNoneSkipped(console.get().asFile)
    }
    outputs.upToDateWhen { false }
}
